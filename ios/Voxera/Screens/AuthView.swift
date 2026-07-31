import SwiftUI
import UIKit

struct AuthView: View {
  @Binding var path: NavigationPath
  @EnvironmentObject private var prefs: PreferencesStore
  @EnvironmentObject private var locale: LocaleStore

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
          .foregroundColor(.white)
          Text(s.authTitle)
            .font(.title3.bold())
            .foregroundColor(.white)
            .frame(maxWidth: .infinity, alignment: .leading)
          ThemedCard(gradientIndex: 0) {
            AuthCardContent(
              showSkipButton: true,
              onAuthComplete: {
                path = NavigationPath()
              },
              onSkip: {
                path = NavigationPath()
                prefs.setAuthCompleted(true)
              }
            )
          }
        }
        .padding(.horizontal, 20)
        .padding(.bottom, 32)
      }
    }
  }
}
