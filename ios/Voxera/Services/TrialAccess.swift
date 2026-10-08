import Foundation

enum TrialAccess {
  private static let keyPrefix = "voxera.trial."
  private static let trialSeconds: TimeInterval = 7 * 24 * 60 * 60

  static func startIfNeeded(uid: String) {
    let key = keyPrefix + uid
    if UserDefaults.standard.object(forKey: key) == nil {
      UserDefaults.standard.set(Date().timeIntervalSince1970, forKey: key)
    }
  }

  static func isInTrial(uid: String) -> Bool {
    let start = UserDefaults.standard.double(forKey: keyPrefix + uid)
    guard start > 0 else { return false }
    return Date().timeIntervalSince1970 - start < trialSeconds
  }

  static func canAnalyze(uid: String?, hasSubscription: Bool) -> Bool {
    guard let uid, !uid.isEmpty else { return false }
    return isInTrial(uid: uid) || hasSubscription
  }
}
