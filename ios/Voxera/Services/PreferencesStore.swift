import Combine
import Foundation

enum ThemeType: String, CaseIterable {
  case glass, light
}

@MainActor
final class PreferencesStore: ObservableObject {
  @Published private(set) var themeType: ThemeType
  @Published private(set) var appLanguage: AppLanguage
  @Published private(set) var consentGiven: Bool
  @Published private(set) var onboardingCompleted: Bool
  @Published private(set) var authCompleted: Bool
  @Published var profilePhotoPath: String?
  @Published var profilePhone: String?

  private let d = UserDefaults.standard
  private enum K {
    static let theme = "theme_type"
    static let lang = "language"
    static let consent = "consent_given"
    static let onboarding = "onboarding_completed"
    static let auth = "auth_completed"
    static let photo = "profile_photo_path"
    static let phone = "profile_phone"
  }

  init() {
    let rawTheme = d.string(forKey: K.theme) ?? ThemeType.glass.rawValue
    themeType = ThemeType(rawValue: rawTheme) ?? .glass
    let rawLang = d.string(forKey: K.lang) ?? AppLanguage.ru.rawValue
    appLanguage = AppLanguage(rawValue: rawLang) ?? .ru
    consentGiven = d.bool(forKey: K.consent)
    if d.object(forKey: K.onboarding) == nil {
      onboardingCompleted = false
    } else {
      onboardingCompleted = d.bool(forKey: K.onboarding)
    }
    if d.object(forKey: K.auth) == nil {
      authCompleted = false
    } else {
      authCompleted = d.bool(forKey: K.auth)
    }
    profilePhotoPath = d.string(forKey: K.photo)
    profilePhone = d.string(forKey: K.phone)
  }

  func setTheme(_ t: ThemeType) {
    themeType = t
    d.set(t.rawValue, forKey: K.theme)
  }

  func setLanguage(_ l: AppLanguage) {
    appLanguage = l
    d.set(l.rawValue, forKey: K.lang)
  }

  func setConsentGiven(_ v: Bool) {
    consentGiven = v
    d.set(v, forKey: K.consent)
  }

  func setOnboardingCompleted(_ v: Bool) {
    onboardingCompleted = v
    d.set(v, forKey: K.onboarding)
  }

  func setAuthCompleted(_ v: Bool) {
    authCompleted = v
    d.set(v, forKey: K.auth)
  }

  func setProfilePhotoPath(_ path: String?) {
    profilePhotoPath = path
    if let path {
      d.set(path, forKey: K.photo)
    } else {
      d.removeObject(forKey: K.photo)
    }
  }

  func setProfilePhone(_ phone: String?) {
    profilePhone = phone
    if let phone {
      d.set(phone, forKey: K.phone)
    } else {
      d.removeObject(forKey: K.phone)
    }
  }
}
