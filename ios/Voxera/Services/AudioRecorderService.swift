import AVFoundation
import Foundation

enum AudioRecorderError: LocalizedError {
  case inputUnavailable
  case prepareFailed
  case recordFailed
  case emptyCapture

  var errorDescription: String? {
    switch self {
    case .inputUnavailable:
      #if targetEnvironment(simulator)
      return """
      Симулятор не видит микрофон (0 каналов входа).
      • I/O → Audio Input в меню Simulator, или
      • кнопка «Тест» справа сверху (готовый audio_test.ogg), или
      • проверка записи на реальном iPhone.
      """
      #else
      return "Микрофон недоступен. Проверьте разрешение в Настройки → Voxera."
      #endif
    case .prepareFailed, .recordFailed:
      #if targetEnvironment(simulator)
      return """
      Не удалось начать запись на симуляторе.
      Используйте «Тест» (Debug) или проверьте Simulator → I/O → Audio Input.
      """
      #else
      return "Не удалось начать запись. Проверьте микрофон и повторите."
      #endif
    case .emptyCapture:
      return "Запись пуста. Говорите ближе к микрофону или используйте «Тест»."
    }
  }
}

/// AAC capture (native), then convert to WAV for API duration parsing.
/// OGG is not available on iOS without third-party codecs.
final class AudioRecorderService: NSObject, ObservableObject, AVAudioRecorderDelegate {
  @Published var isRecording = false
  @Published var permissionDenied = false

  private var recorder: AVAudioRecorder?
  private var m4aURL: URL?
  private var wavURL: URL?

  func requestPermission() async -> Bool {
    let ok = await AVAudioApplication.requestRecordPermission()
    permissionDenied = !ok
    return ok
  }

  func startRecording(to wavDestination: URL) throws {
    if recorder?.isRecording == true {
      recorder?.stop()
    }
    recorder = nil
    m4aURL = nil
    wavURL = nil

    let session = AVAudioSession.sharedInstance()
    // `.record` avoids some playAndRecord converter glitches on Simulator.
    do {
      try session.setCategory(.record, mode: .measurement, options: [])
    } catch {
      try session.setCategory(.playAndRecord, mode: .default, options: [.defaultToSpeaker])
    }
    try session.setActive(true, options: [])

    guard session.isInputAvailable else {
      throw AudioRecorderError.inputUnavailable
    }

    // Match hardware rate when possible (Simulator often reports 0 input channels).
    let hwRate = session.sampleRate > 0 ? session.sampleRate : 44_100
    try? session.setPreferredSampleRate(hwRate)
    try? session.setPreferredInputNumberOfChannels(1)

    if session.inputNumberOfChannels < 1 {
      throw AudioRecorderError.inputUnavailable
    }

    let m4a = FileManager.default.temporaryDirectory
      .appendingPathComponent("recording_\(UUID().uuidString).m4a")
    try? FileManager.default.removeItem(at: m4a)
    try? FileManager.default.removeItem(at: wavDestination)

    let settings: [String: Any] = [
      AVFormatIDKey: Int(kAudioFormatMPEG4AAC),
      AVSampleRateKey: hwRate,
      AVNumberOfChannelsKey: 1,
      AVEncoderAudioQualityKey: AVAudioQuality.high.rawValue,
    ]

    let rec = try AVAudioRecorder(url: m4a, settings: settings)
    rec.delegate = self
    // Retain before prepare/record — AudioQueue callbacks need a live object.
    m4aURL = m4a
    wavURL = wavDestination
    recorder = rec

    guard rec.prepareToRecord() else {
      cleanupRecorderFiles()
      throw AudioRecorderError.prepareFailed
    }
    guard rec.record() else {
      cleanupRecorderFiles()
      throw AudioRecorderError.recordFailed
    }
    isRecording = true
  }

  func cancelRecording() {
    recorder?.stop()
    isRecording = false
    cleanupRecorderFiles()
  }

  func stopRecording() async -> URL? {
    guard let rec = recorder else { return nil }
    rec.stop()
    isRecording = false

    let m4a = m4aURL
    let wav = wavURL
    try? await Task.sleep(nanoseconds: 200_000_000)
    recorder = nil
    m4aURL = nil
    wavURL = nil

    guard let m4a else { return nil }
    let destination =
      wav
      ?? FileManager.default.temporaryDirectory.appendingPathComponent(
        "recording_\(UUID().uuidString).wav")

    let size = (try? FileManager.default.attributesOfItem(atPath: m4a.path)[.size] as? NSNumber)?
      .intValue ?? 0
    if size < 1024 {
      try? FileManager.default.removeItem(at: m4a)
      return nil
    }

    do {
      try Self.convertToWav(source: m4a, destination: destination)
      try? FileManager.default.removeItem(at: m4a)
      return destination
    } catch {
      let fallback = destination.deletingPathExtension().appendingPathExtension("m4a")
      try? FileManager.default.removeItem(at: fallback)
      try? FileManager.default.moveItem(at: m4a, to: fallback)
      return fallback
    }
  }

  func audioRecorderEncodeErrorDidOccur(_ recorder: AVAudioRecorder, error: Error?) {
    isRecording = false
  }

  private func cleanupRecorderFiles() {
    recorder = nil
    if let m4aURL { try? FileManager.default.removeItem(at: m4aURL) }
    m4aURL = nil
    wavURL = nil
  }

  private static func convertToWav(source: URL, destination: URL) throws {
    let inputFile = try AVAudioFile(forReading: source)
    let inFormat = inputFile.processingFormat
    let frameCount = AVAudioFrameCount(inputFile.length)
    guard frameCount > 0,
      let inputBuffer = AVAudioPCMBuffer(pcmFormat: inFormat, frameCapacity: frameCount)
    else {
      throw AudioRecorderError.emptyCapture
    }
    try inputFile.read(into: inputBuffer)

    let wavSettings: [String: Any] = [
      AVFormatIDKey: Int(kAudioFormatLinearPCM),
      AVSampleRateKey: inFormat.sampleRate,
      AVNumberOfChannelsKey: 1,
      AVLinearPCMBitDepthKey: 16,
      AVLinearPCMIsFloatKey: false,
      AVLinearPCMIsBigEndianKey: false,
      AVLinearPCMIsNonInterleaved: false,
    ]

    try? FileManager.default.removeItem(at: destination)
    let outputFile = try AVAudioFile(
      forWriting: destination,
      settings: wavSettings,
      commonFormat: .pcmFormatInt16,
      interleaved: true
    )

    let outFormat = outputFile.processingFormat
    if inFormat.channelCount == outFormat.channelCount,
      inFormat.sampleRate == outFormat.sampleRate,
      inFormat.commonFormat == outFormat.commonFormat
    {
      try outputFile.write(from: inputBuffer)
      return
    }

    guard let converter = AVAudioConverter(from: inFormat, to: outFormat) else {
      throw AudioRecorderError.prepareFailed
    }

    let capacity =
      AVAudioFrameCount(Double(inputBuffer.frameLength) * outFormat.sampleRate / inFormat.sampleRate)
      + 32
    guard let converted = AVAudioPCMBuffer(pcmFormat: outFormat, frameCapacity: max(capacity, 1))
    else {
      throw AudioRecorderError.prepareFailed
    }

    // Avoid Sendable warnings from capturing mutable locals in converter callback.
    final class Once: @unchecked Sendable {
      var done = false
      let buffer: AVAudioPCMBuffer
      init(_ buffer: AVAudioPCMBuffer) { self.buffer = buffer }
    }
    let once = Once(inputBuffer)

    var convError: NSError?
    let status = converter.convert(to: converted, error: &convError) { _, outStatus in
      if once.done {
        outStatus.pointee = .endOfStream
        return nil
      }
      once.done = true
      outStatus.pointee = .haveData
      return once.buffer
    }
    if let convError { throw convError }
    guard status != .error, converted.frameLength > 0 else {
      throw AudioRecorderError.emptyCapture
    }
    try outputFile.write(from: converted)
  }
}
