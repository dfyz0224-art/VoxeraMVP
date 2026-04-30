import SwiftUI
import UIKit

/// Как `AppSplashScreen.kt`: чёрный фон, лого, вспышка размытым «следом» того же маркера, затем исчезновение.
struct SplashView: View {
  var onComplete: () -> Void
  @State private var logoOpacity: CGFloat = 1
  @State private var flashOpacity: CGFloat = 0

  private let logoSide: CGFloat = 160
  private var flashSide: CGFloat { logoSide * 1.45 }

  var body: some View {
    ZStack {
      Color.black.ignoresSafeArea()
      splashMark
        .frame(width: logoSide, height: logoSide)
        .opacity(logoOpacity)
      if flashOpacity > 0.001 {
        splashMark
          .frame(width: flashSide, height: flashSide)
          .blur(radius: 18)
          .opacity(flashOpacity * 0.9)
      }
    }
    .onAppear {
      Task {
        try? await Task.sleep(nanoseconds: 100_000_000)
        withAnimation(.linear(duration: 0.35)) { flashOpacity = 1 }
        try? await Task.sleep(nanoseconds: 350_000_000)
        withAnimation(.linear(duration: 0.35)) { flashOpacity = 0 }
        try? await Task.sleep(nanoseconds: 350_000_000)
        withAnimation(.easeOut(duration: 0.4)) { logoOpacity = 0 }
        try? await Task.sleep(nanoseconds: 400_000_000)
        onComplete()
      }
    }
  }

  @ViewBuilder
  private var splashMark: some View {
    if UIImage(named: "ic_x_white") != nil {
      Image("ic_x_white")
        .resizable()
        .scaledToFit()
    } else {
      Image(systemName: "sparkle")
        .font(.system(size: 118, weight: .thin))
        .foregroundStyle(.white)
        .symbolRenderingMode(.monochrome)
    }
  }
}
