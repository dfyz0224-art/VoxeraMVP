import Foundation
import UniformTypeIdentifiers

/// MIME for upload — parity with Android `AudioUploadHelper`.
enum AudioUploadMime {
  static func fromFileName(_ fileName: String) -> String? {
    let lower = fileName.lowercased()
    if lower.hasSuffix(".wav") { return "audio/wav" }
    if lower.hasSuffix(".mp3") { return "audio/mpeg" }
    if lower.hasSuffix(".ogg") || lower.hasSuffix(".oga") { return "audio/ogg" }
    if lower.hasSuffix(".webm") { return "audio/webm" }
    if lower.hasSuffix(".m4a") { return "audio/mp4" }
    if lower.hasSuffix(".aac") { return "audio/aac" }
    if lower.hasSuffix(".flac") { return "audio/flac" }
    if lower.hasSuffix(".opus") { return "audio/opus" }
    return nil
  }

  static func normalize(_ mime: String?, fileName: String) -> String {
    if let byExt = fromFileName(fileName) { return byExt }
    let trimmed = (mime ?? "").trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
    switch trimmed {
    case "audio/x-wav", "audio/wave", "audio/vnd.wave": return "audio/wav"
    case "audio/mp3": return "audio/mpeg"
    case "application/ogg", "audio/vorbis": return "audio/ogg"
    case "audio/x-m4a", "audio/m4a": return "audio/mp4"
    case "audio/x-flac": return "audio/flac"
    case "", "application/octet-stream": return "application/octet-stream"
    default: return trimmed
    }
  }

  static func fromContentType(_ type: UTType?, fileName: String) -> String {
    if let byExt = fromFileName(fileName) { return byExt }
    guard let type else { return "application/octet-stream" }
    if type.conforms(to: .wav) { return "audio/wav" }
    if type.conforms(to: .mp3) { return "audio/mpeg" }
    if type.conforms(to: .mpeg4Audio) { return "audio/mp4" }
    return type.preferredMIMEType ?? "application/octet-stream"
  }
}
