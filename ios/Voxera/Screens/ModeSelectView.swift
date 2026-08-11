import SwiftUI
import UIKit

struct ModeSelectView: View {
  @Binding var path: NavigationPath
  @EnvironmentObject private var prefs: PreferencesStore
  @EnvironmentObject private var session: AnalysisSession
  @EnvironmentObject private var locale: LocaleStore

  var s: AppStrings { locale.strings }

  var body: some View {
    GeometryReader { geo in
      let short = geo.size.height < 700
      let logoH: CGFloat = short ? 52 : 64
      let topPad: CGFloat = short ? 12 : 24
      let cardH = max(112, min(150, (geo.size.height - 280) / 2))

      ZStack {
        BackgroundImageName()
        VStack(spacing: 0) {
          Spacer().frame(height: topPad)
          Group {
            if UIImage(named: "ic_voxera_logo_text") != nil {
              Image("ic_voxera_logo_text").resizable().scaledToFit()
            } else {
              Text("VOXERA").font(.system(size: 36, weight: .bold, design: .rounded))
            }
          }
          .frame(height: logoH)
          .foregroundColor(.white)

          Text(s.selectMode)
            .font(.title3.bold())
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .padding(.top, 12)
            .padding(.bottom, 10)

          VStack(spacing: 10) {
            modeCard(
              title: s.universalMode,
              asset: "universal_2",
              tag: "teen",
              gradient: 0,
              height: cardH
            )
            modeCard(
              title: s.deepAnalysis,
              asset: "deep_2",
              tag: "quick",
              gradient: 1,
              height: cardH
            )
          }

          Spacer(minLength: 14)

          VStack(spacing: 8) {
            HStack(spacing: 12) {
              navButton(s.about) { path.append(AppRoute.about) }
              navButton(s.settings) { path.append(AppRoute.settings) }
            }
            HStack(spacing: 12) {
              navButton(s.help) { path.append(AppRoute.help) }
              navButton(s.forBusiness) { path.append(AppRoute.forBusiness) }
            }
            navButton(s.history) { path.append(AppRoute.history) }
          }
          .padding(.bottom, 12)
        }
        .padding(.horizontal, 20)
      }
    }
  }

  private func modeCard(title: String, asset: String, tag: String, gradient: Int, height: CGFloat) -> some View {
    let iconSize: CGFloat = height < 130 ? 72 : 88
    let fontSize: CGFloat = height < 130 ? 15 : 17
    return ThemedCard(gradientIndex: gradient, onTap: {
      session.analysisType = tag == "quick" ? "psytype" : "emostate"
      if prefs.consentGiven {
        path.append(AppRoute.recording)
      } else {
        path.append(AppRoute.consent)
      }
    }) {
      HStack(alignment: .center, spacing: 14) {
        Image(asset)
          .renderingMode(.template)
          .resizable()
          .scaledToFit()
          .frame(width: iconSize, height: iconSize)
          .foregroundColor(.white)
        Text(title)
          .font(.system(size: fontSize, weight: .semibold))
          .foregroundColor(.white)
          .multilineTextAlignment(.leading)
          .frame(maxWidth: .infinity, alignment: .leading)
      }
      .padding(.vertical, 8)
    }
    .frame(height: height)
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
        .padding(.vertical, 14)
        .background(Color.white.opacity(prefs.themeType == .light ? 0.55 : 0.12))
        .cornerRadius(16)
    }
    .overlay(
      RoundedRectangle(cornerRadius: 16, style: .continuous)
        .stroke(stroke, lineWidth: prefs.themeType == .light ? 1.5 : 0)
    )
  }
}
