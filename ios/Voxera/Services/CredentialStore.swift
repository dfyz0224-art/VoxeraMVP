import Foundation
import Security

/// Keychain-backed email/password remember store (Android CredentialStore parity).
enum CredentialStore {
  private static let service = "com.vanoprojects.voxera.credentials"
  private static let emailKey = "email"
  private static let passwordKey = "password"
  private static let rememberKey = "remember_enabled"

  static var isRememberEnabled: Bool {
    UserDefaults.standard.bool(forKey: rememberKey)
  }

  static func setRememberEnabled(_ enabled: Bool) {
    UserDefaults.standard.set(enabled, forKey: rememberKey)
    if !enabled { clear() }
  }

  static func save(email: String, password: String) {
    setRememberEnabled(true)
    set(password, account: passwordKey)
    set(email, account: emailKey)
  }

  static func load() -> (email: String, password: String)? {
    guard isRememberEnabled,
          let email = get(account: emailKey), !email.isEmpty,
          let password = get(account: passwordKey), !password.isEmpty
    else { return nil }
    return (email, password)
  }

  static func clear() {
    delete(account: emailKey)
    delete(account: passwordKey)
    UserDefaults.standard.set(false, forKey: rememberKey)
  }

  private static func set(_ value: String, account: String) {
    let data = Data(value.utf8)
    let query: [String: Any] = [
      kSecClass as String: kSecClassGenericPassword,
      kSecAttrService as String: service,
      kSecAttrAccount as String: account
    ]
    SecItemDelete(query as CFDictionary)
    var add = query
    add[kSecValueData as String] = data
    SecItemAdd(add as CFDictionary, nil)
  }

  private static func get(account: String) -> String? {
    let query: [String: Any] = [
      kSecClass as String: kSecClassGenericPassword,
      kSecAttrService as String: service,
      kSecAttrAccount as String: account,
      kSecReturnData as String: true,
      kSecMatchLimit as String: kSecMatchLimitOne
    ]
    var out: AnyObject?
    let status = SecItemCopyMatching(query as CFDictionary, &out)
    guard status == errSecSuccess, let data = out as? Data else { return nil }
    return String(data: data, encoding: .utf8)
  }

  private static func delete(account: String) {
    let query: [String: Any] = [
      kSecClass as String: kSecClassGenericPassword,
      kSecAttrService as String: service,
      kSecAttrAccount as String: account
    ]
    SecItemDelete(query as CFDictionary)
  }
}
