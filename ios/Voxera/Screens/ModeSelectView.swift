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
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .padding(.bottom, 2)
          VStack(spacing: 8) {
            modeCard(
              title: s.universalMode,
              asset: "universal_2",
              tag: "teen",
              gradient: 0
            )
            modeCard(
              title: s.deepAnalysis,
              asset: "deep_2",
              tag: "quick",
              gradient: 1
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

  private func modeCard(title: String, asset: String, tag: String, gradient: Int) -> some View {
    ThemedCard(gradientIndex: gradient, onTap: {
      session.analysisType = tag == "quick" ? "psytype" : "emostate"
      if prefs.consentGiven {
        path.append(AppRoute.recording)
      } else {
        path.append(AppRoute.consent)
      }
    }) {
      HStack(alignment: .center, spacing: 12) {
        Image(asset)
          .renderingMode(.template)
          .resizable()
          .scaledToFit()
          .frame(width: 72, height: 72)
          .foregroundColor(.white)
        Text(title)
          .font(.system(size: 17, weight: .semibold))
          .foregroundColor(.white)
          .multilineTextAlignment(.leading)
          .frame(maxWidth: .infinity, alignment: .leading)
      }
      .padding(.vertical, 8)
    }
    .frame(minHeight: 150)
  }

  private func navButton(_ title: String, action: @escaping () -> Void) -> some View {
    let stroke = prefs.themeType == .light
      ? Color(red: 0.04, green: 0.09, blue: 0.16).opacity(0.35) : Color.clear
    return Button(action: action) {
      Text(title)
        .font(.headline)
        .foregroundColor(prefs.themeType == .light ? Color(red: 0.04, green: 0.09, blue: 0.16) : .white)
        .multilineTextAlignment(.center)
        .frame(maxWidth: .infinity)
        .padding(.vertical, 16)
        .background(Color.white.opacity(prefs.themeType == .light ? 0.55 : 0.12))
        .cornerRadius(16)
    }
    .overlay(
      RoundedRectangle(cornerRadius: 16, style: .continuous)
        .stroke(stroke, lineWidth: prefs.themeType == .light ? 1.5 : 0)
    )
  }
}
