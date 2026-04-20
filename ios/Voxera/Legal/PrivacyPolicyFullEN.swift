import Foundation

/// Same legal text as `legal/PrivacyPolicyContent.kt` (English).
enum PrivacyPolicyFullEN {
  static let text: String = {
    if let url = Bundle.main.url(forResource: "PrivacyPolicyFull", withExtension: "txt"),
      let s = try? String(contentsOf: url, encoding: .utf8)
    {
      return s
    }
    return """
    Privacy policy text: add PrivacyPolicyFull.txt to the app bundle or copy from Android PrivacyPolicyContent.kt.
    """
  }()
}
