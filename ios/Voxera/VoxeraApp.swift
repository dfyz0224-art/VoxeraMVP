import SwiftUI
import GoogleSignIn

@main
struct VoxeraApp: App {
  @StateObject private var prefs = PreferencesStore()
  @StateObject private var history = HistoryStore()
  @StateObject private var locale = LocaleStore()
  @StateObject private var auth = GoogleAuthSession()

  var body: some Scene {
    WindowGroup {
      VoxeraRootView()
        .environmentObject(prefs)
        .environmentObject(AnalysisSession.shared)
        .environmentObject(history)
        .environmentObject(locale)
        .environmentObject(auth)
        .preferredColorScheme(.dark)
        .onOpenURL { url in
          GIDSignIn.sharedInstance.handle(url)
        }
        .onAppear {
          locale.update(language: prefs.appLanguage)
          Task {
            await DailyReminderScheduler.refresh(
              enabled: prefs.dailyRemindersEnabled,
              language: prefs.appLanguage
            )
          }
        }
        .onChange(of: prefs.appLanguage) { _, lang in
          locale.update(language: lang)
          Task {
            await DailyReminderScheduler.refresh(
              enabled: prefs.dailyRemindersEnabled,
              language: lang
            )
          }
        }
        .onChange(of: prefs.dailyRemindersEnabled) { _, enabled in
          Task {
            await DailyReminderScheduler.refresh(
              enabled: enabled,
              language: prefs.appLanguage
            )
          }
        }
    }
  }
}
