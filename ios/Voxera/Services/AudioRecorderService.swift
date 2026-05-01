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

    let settings: [String: Any] = [
      AVFormatIDKey: Int(kAudioFormatMPEG4AAC),
      AVSampleRateKey: 44_100,
      AVNumberOfChannelsKey: 1,
      AVEncoderAudioQualityKey: AVAudioQuality.high.rawValue,
    ]
    fileURL = url
    recorder = try AVAudioRecorder(url: url, settings: settings)
    recorder?.prepareToRecord()
    recorder?.record()
    isRecording = true
  }

  func stopRecording() -> URL? {
    recorder?.stop()
    isRecording = false
    recorder = nil
    return fileURL
  }
}
