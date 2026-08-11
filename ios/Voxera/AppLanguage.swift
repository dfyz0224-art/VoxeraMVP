import Foundation

enum AppLanguage: String, CaseIterable, Identifiable {
  case ru, en, zh, kz, uk, ka
  var id: String { rawValue }
  var displayLabel: String {
    switch self {
    case .ru: return "Русский"
    case .en: return "English"
    case .zh: return "中文"
    case .kz: return "Қазақша"
    case .uk: return "Українська"
    case .ka: return "ქართული"
    }
  }
}

extension AppStrings {
  static func pack(for lang: AppLanguage) -> AppStrings {
    switch lang {
    case .ru: return .ru
    case .en: return .en
    case .zh: return .zh
    case .kz: return .kz
    case .uk: return .uk
    case .ka: return .ka
    }
  }
}
