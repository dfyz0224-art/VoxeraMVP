import Foundation

/// API token (mirror Android `VOXERA_API_TOKEN`).
/// Prefer Debug: Scheme → Run → Environment Variables → `VOXERA_API_TOKEN`.
/// Or: `Secrets.xcconfig` / token line **after** `#include` in `Config.xcconfig`, then Clean Build.
enum Secrets {
  static var voxeraApiToken: String {
    if let s = Bundle.main.object(forInfoDictionaryKey: "VOXERA_API_TOKEN") as? String {
      let t = normalized(s)
      if !t.isEmpty { return t }
    }
    #if DEBUG
    if let e = ProcessInfo.processInfo.environment["VOXERA_API_TOKEN"] {
      let t = normalized(e)
      if !t.isEmpty { return t }
    }
    #endif
    return ""
  }

  private static func normalized(_ raw: String) -> String {
    var t = raw.trimmingCharacters(in: .whitespacesAndNewlines)
    // Unexpanded build setting left in Info.plist
    if t.hasPrefix("$(") { return "" }
    // Accidental quotes from xcconfig / copy-paste
    if t.count >= 2, t.first == "\"", t.last == "\"" {
      t = String(t.dropFirst().dropLast())
    }
    if t == "paste_token_here" || t == "your_token_here" { return "" }
    return t.trimmingCharacters(in: .whitespacesAndNewlines)
  }
}
