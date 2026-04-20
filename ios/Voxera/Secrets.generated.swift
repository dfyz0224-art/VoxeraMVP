import Foundation

/// API token (mirror Android `VOXERA_API_TOKEN`). Copy `Secrets.xcconfig.example` to `Secrets.xcconfig` and set `VOXERA_API_TOKEN`.
enum Secrets {
  static var voxeraApiToken: String {
    Bundle.main.object(forInfoDictionaryKey: "VOXERA_API_TOKEN") as? String ?? ""
  }
}
