import AVFoundation
import Foundation

@MainActor
final class AudioRecorderService: NSObject, ObservableObject {
  @Published var isRecording = false
  @Published var permissionDenied = false

  private var recorder: AVAudioRecorder?
  private var fileURL: URL?

  func requestPermission() async -> Bool {
    await AVAudioApplication.requestRecordPermission()
  }

  func startRecording(to url: URL) throws {
    let session = AVAudioSession.sharedInstance()
    try session.setCategory(.playAndRecord, mode: .default, options: [.defaultToSpeaker])
    try session.setActive(true)

    // WAV/PCM — как Android test.wav: сервер стабильно читает duration
    // (M4A/AAC на API часто даёт 422 "Could not determine audio duration").
    let settings: [String: Any] = [
      AVFormatIDKey: Int(kAudioFormatLinearPCM),
      AVSampleRateKey: 44_100,
      AVNumberOfChannelsKey: 1,
      AVLinearPCMBitDepthKey: 16,
      AVLinearPCMIsFloatKey: false,
      AVLinearPCMIsBigEndianKey: false,
      AVLinearPCMIsNonInterleaved: false,
    ]
    fileURL = url
    let rec = try AVAudioRecorder(url: url, settings: settings)
    rec.prepareToRecord()
    guard rec.record() else {
      throw NSError(
        domain: "AudioRecorderService",
        code: 1,
        userInfo: [NSLocalizedDescriptionKey: "AVAudioRecorder.record() failed"]
      )
    }
    recorder = rec
    isRecording = true
  }

  func stopRecording() -> URL? {
    recorder?.stop()
    recorder = nil
    isRecording = false
    try? AVAudioSession.sharedInstance().setActive(false, options: .notifyOthersOnDeactivation)
    return fileURL
  }
}
