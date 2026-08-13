import AVFoundation
import Foundation

/// Records with AAC (stable on device/simulator), then converts to WAV for the API.
/// Native OGG/Opus recording is not available on iOS without third-party codecs.
final class AudioRecorderService: NSObject, ObservableObject, AVAudioRecorderDelegate {
  @Published var isRecording = false
  @Published var permissionDenied = false

  private var recorder: AVAudioRecorder?
  private var m4aURL: URL?
  private var wavURL: URL?

  func requestPermission() async -> Bool {
    await AVAudioApplication.requestRecordPermission()
  }

  func startRecording(to wavDestination: URL) throws {
    // Finish any previous session without tearing down audio I/O mid-flight.
    if recorder?.isRecording == true {
      recorder?.stop()
    }
    recorder = nil

    let session = AVAudioSession.sharedInstance()
    try session.setCategory(.playAndRecord, mode: .default, options: [.defaultToSpeaker])
    try session.setActive(true)

    let m4a = FileManager.default.temporaryDirectory
      .appendingPathComponent("recording_\(UUID().uuidString).m4a")
    try? FileManager.default.removeItem(at: m4a)
    try? FileManager.default.removeItem(at: wavDestination)

    // AAC/M4A is the reliable AVAudioRecorder path; Linear PCM often crashes on AQClient.
    let settings: [String: Any] = [
      AVFormatIDKey: Int(kAudioFormatMPEG4AAC),
      AVSampleRateKey: 44_100.0,
      AVNumberOfChannelsKey: 1,
      AVEncoderBitRateKey: 128_000,
      AVEncoderAudioQualityKey: AVAudioQuality.high.rawValue,
    ]

    let rec = try AVAudioRecorder(url: m4a, settings: settings)
    rec.delegate = self
    guard rec.prepareToRecord() else {
      throw NSError(
        domain: "AudioRecorderService",
        code: 1,
        userInfo: [NSLocalizedDescriptionKey: "prepareToRecord failed"]
      )
    }

    // Strong retain BEFORE record() — otherwise AQClient can EXC_BAD_ACCESS.
    m4aURL = m4a
    wavURL = wavDestination
    recorder = rec

    guard rec.record() else {
      recorder = nil
      m4aURL = nil
      wavURL = nil
      throw NSError(
        domain: "AudioRecorderService",
        code: 2,
        userInfo: [NSLocalizedDescriptionKey: "AVAudioRecorder.record() failed"]
      )
    }
    isRecording = true
  }

  /// Stops without waiting for WAV conversion (e.g. leaving the screen).
  func cancelRecording() {
    recorder?.stop()
    isRecording = false
    recorder = nil
    if let m4aURL {
      try? FileManager.default.removeItem(at: m4aURL)
    }
    m4aURL = nil
    wavURL = nil
  }

  /// Stops recording and returns a WAV file URL suitable for `integrations/analyze`.
  func stopRecording() async -> URL? {
    guard let rec = recorder else { return wavURL ?? m4aURL }
    rec.stop()
    isRecording = false

    let m4a = m4aURL
    let wav = wavURL
    // Keep recorder alive briefly so AudioQueue teardown can finish.
    try? await Task.sleep(nanoseconds: 150_000_000)
    recorder = nil
    m4aURL = nil
    wavURL = nil

    guard let m4a else { return nil }
    let destination =
      wav
      ?? FileManager.default.temporaryDirectory.appendingPathComponent(
        "recording_\(UUID().uuidString).wav")

    do {
      try Self.convertToWav(source: m4a, destination: destination)
      try? FileManager.default.removeItem(at: m4a)
      return destination
    } catch {
      // Last resort: send M4A with correct MIME (audio/mp4).
      let fallback = destination.deletingPathExtension().appendingPathExtension("m4a")
      try? FileManager.default.removeItem(at: fallback)
      try? FileManager.default.copyItem(at: m4a, to: fallback)
      try? FileManager.default.removeItem(at: m4a)
      return fallback
    }
  }

  func audioRecorderEncodeErrorDidOccur(_ recorder: AVAudioRecorder, error: Error?) {
    isRecording = false
  }

  private static func convertToWav(source: URL, destination: URL) throws {
    let inputFile = try AVAudioFile(forReading: source)
    let inFormat = inputFile.processingFormat
    let frameCount = AVAudioFrameCount(inputFile.length)
    guard frameCount > 0,
      let inputBuffer = AVAudioPCMBuffer(pcmFormat: inFormat, frameCapacity: frameCount)
    else {
      throw NSError(
        domain: "AudioRecorderService",
        code: 3,
        userInfo: [NSLocalizedDescriptionKey: "Empty AAC buffer"]
      )
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
    if inFormat == outFormat {
      try outputFile.write(from: inputBuffer)
      return
    }

    guard let converter = AVAudioConverter(from: inFormat, to: outFormat) else {
      throw NSError(
        domain: "AudioRecorderService",
        code: 4,
        userInfo: [NSLocalizedDescriptionKey: "AVAudioConverter init failed"]
      )
    }

    let capacity = AVAudioFrameCount(Double(inputBuffer.frameLength) * outFormat.sampleRate / inFormat.sampleRate) + 32
    guard let converted = AVAudioPCMBuffer(pcmFormat: outFormat, frameCapacity: max(capacity, 1))
    else {
      throw NSError(
        domain: "AudioRecorderService",
        code: 5,
        userInfo: [NSLocalizedDescriptionKey: "PCM buffer alloc failed"]
      )
    }

    var convError: NSError?
    var consumed = false
    let status = converter.convert(to: converted, error: &convError) { _, outStatus in
      if consumed {
        outStatus.pointee = .noDataNow
        return nil
      }
      consumed = true
      outStatus.pointee = .haveData
      return inputBuffer
    }
    if let convError { throw convError }
    guard status != .error, converted.frameLength > 0 else {
      throw NSError(
        domain: "AudioRecorderService",
        code: 6,
        userInfo: [NSLocalizedDescriptionKey: "WAV convert failed"]
      )
    }
    try outputFile.write(from: converted)
  }
}
