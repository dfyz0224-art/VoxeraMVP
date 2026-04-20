import SwiftUI
import UIKit

struct OnboardingView: View {
  @Binding var path: NavigationPath
  @EnvironmentObject private var prefs: PreferencesStore
  @EnvironmentObject private var locale: LocaleStore

  @State private var step = 0
  @State private var agree1 = false
  @State private var agree2 = false
  @State private var showLanguage = false

  var s: AppStrings { locale.strings }

  var body: some View {
    ZStack {
      BackgroundImageName()
      ScrollView {
        VStack(spacing: 20) {
          Spacer().frame(height: 36)
          Group {
            if UIImage(named: "ic_voxera_logo_text") != nil {
              Image("ic_voxera_logo_text").resizable().scaledToFit()
            } else {
              Text("VOXERA").font(.system(size: 36, weight: .bold, design: .rounded))
            }
          }
          .frame(height: 70)
          .padding(.horizontal, 8)
          .foregroundColor(.white)
          content
        }
        .padding(.horizontal, 20)
        .padding(.bottom, 32)
      }
    }
    .sheet(isPresented: $showLanguage) {
      languageSheet
    }
  }

  @ViewBuilder private var content: some View {
    let titles = [s.selectLanguage, s.privacyAndConsent, s.privacyAndConsent, s.privacyAndConsent]
    switch step {
    case 0:
      ThemedCard(gradientIndex: 0) {
        VStack(alignment: .leading, spacing: 12) {
          Text(titles[0]).font(.headline).foregroundColor(.white)
          ForEach(AppLanguage.allCases) { lang in
            Button {
              prefs.setLanguage(lang)
              locale.update(language: lang)
              showLanguage = false
            } label: {
              HStack {
                Text(lang.displayLabel).foregroundColor(.white)
                Spacer()
                if prefs.appLanguage == lang { Image(systemName: "checkmark.circle.fill").foregroundColor(.white) }
              }
              .padding(.vertical, 6)
            }
          }
          Button(s.next) { step = 1 }
            .buttonStyle(.borderedProminent)
            .tint(.white.opacity(0.25))
        }
      }
    case 1:
      ThemedCard(gradientIndex: 0) {
        Text(s.onboardingText1)
          .foregroundColor(.white)
          .font(.body)
        Button(s.next) { step = 2 }
          .buttonStyle(.borderedProminent)
          .tint(.white.opacity(0.25))
          .padding(.top, 8)
      }
    case 2:
      ThemedCard(gradientIndex: 1) {
        Text(s.onboardingText2)
          .foregroundColor(.white)
          .font(.body)
        Button(s.next) { step = 3 }
          .buttonStyle(.borderedProminent)
          .tint(.white.opacity(0.25))
          .padding(.top, 8)
      }
    case 3:
      ThemedCard(gradientIndex: 2) {
        VStack(alignment: .leading, spacing: 12) {
          Text(s.consentCardSummary)
            .foregroundColor(.white.opacity(0.95))
            .font(.callout)
          Toggle(s.consentVoice, isOn: $agree1).foregroundColor(.white)
          Toggle(s.consentPrivacy, isOn: $agree2).foregroundColor(.white)
          Button(s.consentOpenPrivacyPolicyButton) {
            path.append(AppRoute.privacyPolicy)
          }
          .foregroundColor(.white)
          Button(s.start) {
            guard agree1 && agree2 else { return }
            path = NavigationPath()
            prefs.setOnboardingCompleted(true)
          }
          .disabled(!agree1 || !agree2)
          .buttonStyle(.borderedProminent)
          .tint(.white.opacity(0.35))
        }
      }
    default:
      EmptyView()
    }
  }

  private var languageSheet: some View {
    NavigationStack {
      List(AppLanguage.allCases) { lang in
        Button(lang.displayLabel) {
          prefs.setLanguage(lang)
          locale.update(language: lang)
        }
      }
      .navigationTitle(s.language)
      .toolbar {
        ToolbarItem(placement: .cancellationAction) {
          Button(s.back) { showLanguage = false }
        }
      }
    }
  }
}
