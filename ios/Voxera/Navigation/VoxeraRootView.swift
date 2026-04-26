import SwiftUI

enum AppRoute: Hashable {
  case consent
  case privacyPolicy
  case recording
  case processing
  case result
  case share
  case history
  case settings
  case about
  case aboutFull
  case help
  case forBusiness
  case questionnaire
  case profile
}

struct VoxeraRootView: View {
  @EnvironmentObject private var prefs: PreferencesStore
  @EnvironmentObject private var session: AnalysisSession
  @EnvironmentObject private var history: HistoryStore
  @EnvironmentObject private var locale: LocaleStore

  @State private var splashDone = false
  @State private var path = NavigationPath()

  var body: some View {
    ZStack {
      NavigationStack(path: $path) {
        Group {
          if !prefs.onboardingCompleted {
            OnboardingView(path: $path)
          } else if !prefs.authCompleted {
            AuthView(path: $path)
          } else {
            ModeSelectView(path: $path)
          }
        }
        .navigationDestination(for: AppRoute.self) { route in
          switch route {
          case .consent: ConsentView(path: $path)
          case .privacyPolicy: PrivacyPolicyView(path: $path)
          case .recording: RecordingView(path: $path)
          case .processing: ProcessingView(path: $path)
          case .result: ResultView(path: $path)
          case .share: ShareView()
          case .history: HistoryView(path: $path)
          case .settings: SettingsView(path: $path)
          case .about: AboutView(path: $path)
          case .aboutFull: AboutFullDescriptionView(path: $path)
          case .help: HelpView()
          case .forBusiness: ForBusinessView(path: $path)
          case .questionnaire: QuestionnaireView(path: $path)
          case .profile: ProfileView(path: $path)
          }
        }
      }
      .environment(\.voxeraTheme, prefs.themeType)
      .onChange(of: prefs.appLanguage) { _, new in
        locale.update(language: new)
      }

      if !splashDone {
        SplashView(onComplete: {
          withAnimation(.easeOut(duration: 0.35)) {
            splashDone = true
          }
        })
        .transition(.opacity)
        .zIndex(1)
      }
    }
  }
}

// Старые экраны/кнопки: `path.append(AppRoute.statistics)` — тот же стек, что и `.history` (график настроения).
extension AppRoute {
  static var statistics: AppRoute { .history }
}
