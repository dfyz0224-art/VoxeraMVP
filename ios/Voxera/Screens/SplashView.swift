import SwiftUI

/// Пиксельно по `AppSplashScreen.kt`: `R.drawable.ic_x_white`, чёрный фон, под лого слой вспышки (крупнее + blur), затем исчезновение лого.
struct SplashView: View {
  var onComplete: () -> Void
  @State private var logoOpacity: CGFloat = 1
  @State private var flashOpacity: CGFloat = 0

  private let logoSide: CGFloat = 160
  private var flashSide: CGFloat { logoSide * 1.45 }

  var body: some View {
    ZStack {
      Color.black.ignoresSafeArea()
      // Вспышка снизу (как в Box: сначала glow, потом лого)
      if flashOpacity > 0.001 {
        Image("ic_x_white")
          .resizable()
          .renderingMode(.template)
          .foregroundStyle(.white)
          .scaledToFit()
          .frame(width: flashSide, height: flashSide)
          .blur(radius: 18)
          .opacity(flashOpacity * 0.9)
      }
      Image("ic_x_white")
        .resizable()
        .scaledToFit()
        .frame(width: logoSide, height: logoSide)
        .opacity(logoOpacity)
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
}
