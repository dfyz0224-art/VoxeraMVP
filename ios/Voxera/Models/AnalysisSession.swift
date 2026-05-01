import Foundation

/// Mirrors Android `AnalysisSession` (single-flight session for navigation).
@MainActor
final class AnalysisSession: ObservableObject {
  static let shared = AnalysisSession()

  @Published var analysisType: String = "emostate"
  var recordedFileURL: URL?
  var lastAudioMimeType: String?
  var lastResultJson: String?
  var lastAnalysisResponse: AnalysisResponse?
  var lastRawApiResponse: String?

  private init() {}
}
