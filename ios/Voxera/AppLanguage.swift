import Foundation

enum AppLanguage: String, CaseIterable, Identifiable {
  case ru, en, zh, kz
  var id: String { rawValue }
  var displayLabel: String {
    switch self {
    case .ru: return "Русский"
    case .en: return "English"
    case .zh: return "中文"
    case .kz: return "Қазақша"
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
    }
  }
}
