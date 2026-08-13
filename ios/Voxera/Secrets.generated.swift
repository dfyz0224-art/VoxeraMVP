import Foundation

/// API token (mirror Android `VOXERA_API_TOKEN`).
/// Release/TestFlight: baked via `scripts/GenerateAPIToken.sh` from `Secrets.xcconfig`.
/// Debug: also accepts Scheme → Run → Environment Variables → `VOXERA_API_TOKEN`.
enum Secrets {
  static var voxeraApiToken: String {
    let fromGenerated = normalized(APIToken.value)
    if !fromGenerated.isEmpty { return fromGenerated }

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
    if t.hasPrefix("$(") { return "" }
    if t.count >= 2, t.first == "\"", t.last == "\"" {
      t = String(t.dropFirst().dropLast())
    }
    if t == "paste_token_here" || t == "your_token_here" { return "" }
    return t.trimmingCharacters(in: .whitespacesAndNewlines)
  }
}
