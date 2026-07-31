import SwiftUI

struct SplashView: View {
  var onComplete: () -> Void
  @State private var logoOpacity: CGFloat = 1
  @State private var flashOpacity: CGFloat = 0

  var body: some View {
    ZStack {
      Color.black.ignoresSafeArea()
      Image("ic_x_white")
        .resizable()
        .scaledToFit()
        .frame(width: 160, height: 160)
        .opacity(logoOpacity)

      if flashOpacity > 0.01 {
        Image("ic_x_white")
          .resizable()
          .scaledToFit()
          .frame(width: 220, height: 220)
          .blur(radius: 18)
          .opacity(flashOpacity * 0.9)
      }
    }
    .onAppear {
      Task {
        try? await Task.sleep(nanoseconds: 100_000_000)
        withAnimation(.easeInOut(duration: 0.35)) { flashOpacity = 1 }
        try? await Task.sleep(nanoseconds: 350_000_000)
        withAnimation(.easeInOut(duration: 0.35)) { flashOpacity = 0 }
        withAnimation(.easeOut(duration: 0.4)) { logoOpacity = 0 }
        try? await Task.sleep(nanoseconds: 400_000_000)
        onComplete()
      }
    }
  }
}
