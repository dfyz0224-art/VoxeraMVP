import SwiftUI
import UIKit

struct AuthView: View {
  @Binding var path: NavigationPath
  @EnvironmentObject private var locale: LocaleStore
  @EnvironmentObject private var auth: GoogleAuthSession

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
            VStack(alignment: .leading, spacing: 12) {
              if let message = auth.message, !message.isEmpty {
                Text(message)
                  .font(.footnote)
                  .foregroundColor(.white)
              }
              Button(s.authGoogle) {
                guard let controller = topViewController() else { return }
                auth.signIn(presenting: controller)
              }
              .buttonStyle(.borderedProminent)
              .tint(.white.opacity(0.35))
              .frame(maxWidth: .infinity)
            }
          }
        }
        .padding(.horizontal, 20)
        .padding(.bottom, 32)
      }
    }
  }
}

func topViewController() -> UIViewController? {
  let scene = UIApplication.shared.connectedScenes
    .compactMap { $0 as? UIWindowScene }
    .first { $0.activationState == .foregroundActive }
  var controller = scene?.keyWindow?.rootViewController
  while let presented = controller?.presentedViewController {
    controller = presented
  }
  return controller
}
