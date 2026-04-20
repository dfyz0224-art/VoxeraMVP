import SwiftUI
import UIKit

struct AuthView: View {
  @Binding var path: NavigationPath
  @EnvironmentObject private var prefs: PreferencesStore
  @EnvironmentObject private var locale: LocaleStore
  @State private var email = ""
  @State private var password = ""
  @State private var isRegister = false

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
            .foregroundColor(prefs.themeType == .light ? .white : .white)
            .frame(maxWidth: .infinity, alignment: .leading)
          ThemedCard(gradientIndex: 0) {
            VStack(alignment: .leading, spacing: 14) {
              TextField(s.authEmail, text: $email)
                .textContentType(.emailAddress)
                .keyboardType(.emailAddress)
                .autocapitalization(.none)
                .padding(12)
                .background(Color.white.opacity(0.12))
                .cornerRadius(12)
                .foregroundColor(.white)
              SecureField(s.authPassword, text: $password)
                .padding(12)
                .background(Color.white.opacity(0.12))
                .cornerRadius(12)
                .foregroundColor(.white)
              Button(isRegister ? s.authRegister : s.authLogin) {
                path = NavigationPath()
                prefs.setAuthCompleted(true)
              }
              .buttonStyle(.borderedProminent)
              .tint(.white.opacity(0.35))
              Button(s.authSkip) {
                path = NavigationPath()
                prefs.setAuthCompleted(true)
              }
              .foregroundColor(.white.opacity(0.9))
              Button(isRegister ? s.authLoginHint : s.authRegisterHint) {
                isRegister.toggle()
              }
              .font(.footnote)
              .foregroundColor(.white.opacity(0.85))
            }
          }
        }
        .padding(.horizontal, 20)
      }
    }
  }
}
