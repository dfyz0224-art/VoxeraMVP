import Combine
import SwiftUI

@MainActor
final class LocaleStore: ObservableObject {
  @Published private(set) var strings: AppStrings

  init(language: AppLanguage = .ru) {
    strings = AppStrings.pack(for: language)
  }

  func update(language: AppLanguage) {
    strings = AppStrings.pack(for: language)
  }
}
