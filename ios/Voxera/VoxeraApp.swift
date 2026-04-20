import SwiftUI

@main
struct VoxeraApp: App {
  @StateObject private var prefs = PreferencesStore()
  @StateObject private var history = HistoryStore()
  @StateObject private var locale = LocaleStore()

  var body: some Scene {
    WindowGroup {
      VoxeraRootView()
        .environmentObject(prefs)
        .environmentObject(AnalysisSession.shared)
        .environmentObject(history)
        .environmentObject(locale)
        .preferredColorScheme(.dark)
        .onAppear {
          locale.update(language: prefs.appLanguage)
        }
    }
  }
}
