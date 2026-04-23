import Foundation

/// API token (mirror Android `VOXERA_API_TOKEN`). Copy `Secrets.xcconfig.example` to `Secrets.xcconfig` and set `VOXERA_API_TOKEN`, **или** (только Debug) задайте переменную среды `VOXERA_API_TOKEN` в Xcode: Scheme → Run → Environment Variables.
enum Secrets {
  static var voxeraApiToken: String {
    if let s = Bundle.main.object(forInfoDictionaryKey: "VOXERA_API_TOKEN") as? String {
      let t = s.trimmingCharacters(in: .whitespacesAndNewlines)
      if !t.isEmpty { return t }
    }
    #if DEBUG
    if let e = ProcessInfo.processInfo.environment["VOXERA_API_TOKEN"] {
      let t = e.trimmingCharacters(in: .whitespacesAndNewlines)
      if !t.isEmpty { return t }
    }
    #endif
    return ""
  }
}
