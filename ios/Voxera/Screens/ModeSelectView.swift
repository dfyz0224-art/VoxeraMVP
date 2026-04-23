import SwiftUI
import UIKit

struct ModeSelectView: View {
  @Binding var path: NavigationPath
  @EnvironmentObject private var prefs: PreferencesStore
  @EnvironmentObject private var session: AnalysisSession
  @EnvironmentObject private var locale: LocaleStore

  var s: AppStrings { locale.strings }

  var body: some View {
    ZStack {
      BackgroundImageName()
      ScrollView {
        VStack(spacing: 10) {
          Spacer().frame(height: 24)
          Group {
            if UIImage(named: "ic_voxera_logo_text") != nil {
              Image("ic_voxera_logo_text").resizable().scaledToFit()
            } else {
              Text("VOXERA").font(.system(size: 36, weight: .bold, design: .rounded))
            }
          }
          .frame(height: 70)
          .foregroundColor(.white)
          Text(s.selectMode)
            .font(.title3.bold())
            .foregroundColor(prefs.themeType == .light ? .white : .white)
            .frame(maxWidth: .infinity)
            .padding(.bottom, 2)
          VStack(spacing: 8) {
            modeCard(
              title: s.parentMode,
              subtitle: s.parentModeSubtitle,
              asset: "parent_2",
              tag: "mom",
              gradient: 0
            )
            modeCard(
              title: s.universalMode,
              subtitle: s.universalModeSubtitle,
              asset: "universal_2",
              tag: "teen",
              gradient: 1
            )
            modeCard(
              title: s.deepAnalysis,
              subtitle: s.deepAnalysisSubtitle,
              asset: "deep_2",
              tag: "quick",
              gradient: 2
            )
          }
          HStack(spacing: 12) {
            navButton(s.history) {
              path.append(AppRoute.history)
            }
            navButton(s.settings) {
              path.append(AppRoute.settings)
            }
          }
          .padding(.top, 8)
        }
        .padding(.horizontal, 20)
        .padding(.bottom, 32)
      }
    }
  }

  private func modeCard(title: String, subtitle: String, asset: String, tag: String, gradient: Int) -> some View {
    ThemedCard(gradientIndex: gradient, onTap: {
      if tag == "history" {
        path.append(AppRoute.history)
        return
      }
      session.analysisType = tag == "quick" ? "psytype" : "emostate"
      if prefs.consentGiven {
        path.append(AppRoute.recording)
      } else {
        path.append(AppRoute.consent)
      }
    }) {
      HStack(alignment: .top, spacing: 12) {
        Image(asset)
          .renderingMode(.template)
          .resizable()
          .scaledToFit()
          .frame(width: 64, height: 64)
          .foregroundColor(.white)
        VStack(alignment: .leading, spacing: 4) {
          Text(title)
            .font(.headline)
            .foregroundColor(.white)
          Text(subtitle)
            .font(.subheadline)
            .foregroundColor(.white.opacity(0.88))
        }
      }
      .padding(.vertical, 6)
    }
    .frame(minHeight: 128)
  }

  private func navButton(_ title: String, action: @escaping () -> Void) -> some View {
    Button(action: action) {
      Text(title)
        .font(.headline)
        .foregroundColor(prefs.themeType == .light ? Color(red: 0.04, green: 0.09, blue: 0.16) : .white)
        .frame(maxWidth: .infinity)
        .padding(.vertical, 16)
        .background(Color.white.opacity(prefs.themeType == .light ? 0.55 : 0.12))
        .cornerRadius(16)
    }
  }
}
