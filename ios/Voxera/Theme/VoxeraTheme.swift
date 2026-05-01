import SwiftUI
import UIKit

struct ThemeColors {
  let backgroundTextPrimary: Color
  let backgroundTextSecondary: Color
  let textPrimary: Color
  let textSecondary: Color
  let cardGradient: [Color]
  let borderGlass: Color
}

struct VoxeraThemeKey: EnvironmentKey {
  static let defaultValue = ThemeType.glass
}

extension EnvironmentValues {
  var voxeraTheme: ThemeType {
    get { self[VoxeraThemeKey.self] }
    set { self[VoxeraThemeKey.self] = newValue }
  }
}

extension ThemeType {
  func colors() -> ThemeColors {
    switch self {
    case .light:
      return ThemeColors(
        backgroundTextPrimary: Color(red: 0.04, green: 0.09, blue: 0.16),
        backgroundTextSecondary: Color(red: 0.1, green: 0.18, blue: 0.29),
        textPrimary: .white,
        textSecondary: Color.white.opacity(0.88),
        cardGradient: [Color(red: 0, green: 0.12, blue: 0.36), Color(red: 0, green: 0.33, blue: 0.74)],
        borderGlass: Color.black.opacity(0.08)
      )
    case .glass:
      return ThemeColors(
        backgroundTextPrimary: .white,
        backgroundTextSecondary: Color.white.opacity(0.7),
        textPrimary: .white,
        textSecondary: Color.white.opacity(0.75),
        cardGradient: [Color.white.opacity(0.15), Color.white.opacity(0.05)],
        borderGlass: Color.white.opacity(0.35)
      )
    }
  }
}

struct BackgroundImageName: View {
  @Environment(\.voxeraTheme) private var theme
  var body: some View {
    Group {
      switch theme {
      case .light:
        Image("bg_light")
          .resizable()
          .ignoresSafeArea()
      case .glass:
        Image("bg_stars")
          .resizable()
          .ignoresSafeArea()
      }
    }
  }
}

/// Как на Android `RecordingScreen`: для светлой темы — `bg_light_reverse` (фон для liquid/blur).
struct RecordingScreenBackground: View {
  @Environment(\.voxeraTheme) private var theme
  var body: some View {
    Group {
      switch theme {
      case .light:
        Image("bg_light_reverse")
          .resizable()
          .ignoresSafeArea()
      case .glass:
        Image("bg_stars")
          .resizable()
          .ignoresSafeArea()
      }
    }
  }
}

/// Сильнее «стекло», меньше «пластик»: тонкий blur + пониженная непрозрачность слоя (как лёгкая вуаль поверх фона).
struct UIKitBlurMaterialCircle: UIViewRepresentable {
  var isLight: Bool

  func makeUIView(context: Context) -> UIVisualEffectView {
    let style: UIBlurEffect.Style = isLight ? .systemUltraThinMaterialLight : .systemUltraThinMaterialDark
    let v = UIVisualEffectView(effect: UIBlurEffect(style: style))
    v.clipsToBounds = true
    v.alpha = isLight ? 0.52 : 0.48
    v.backgroundColor = .clear
    return v
  }

  func updateUIView(_ uiView: UIVisualEffectView, context: Context) {
    let style: UIBlurEffect.Style = isLight ? .systemUltraThinMaterialLight : .systemUltraThinMaterialDark
    uiView.effect = UIBlurEffect(style: style)
    uiView.alpha = isLight ? 0.52 : 0.48
    uiView.backgroundColor = .clear
  }
}

struct ThemedCard<Content: View>: View {
  let gradientIndex: Int
  let onTap: (() -> Void)?
  @ViewBuilder let content: () -> Content
  @Environment(\.voxeraTheme) private var theme

  init(gradientIndex: Int, onTap: (() -> Void)? = nil, @ViewBuilder content: @escaping () -> Content) {
    self.gradientIndex = gradientIndex
    self.onTap = onTap
    self.content = content
  }

  var body: some View {
    let colors = theme.colors()
    let g = colors.cardGradient
    let shape = RoundedRectangle(cornerRadius: 16, style: .continuous)
    Group {
      if let onTap {
        Button(action: onTap) {
          inner(colors: colors, g: g, shape: shape)
        }
        .buttonStyle(.plain)
      } else {
        inner(colors: colors, g: g, shape: shape)
      }
    }
  }

  @ViewBuilder
  private func inner(colors: ThemeColors, g: [Color], shape: RoundedRectangle) -> some View {
    content()
      .padding(18)
      .frame(maxWidth: .infinity, alignment: .leading)
      .background(
        LinearGradient(colors: g, startPoint: .topLeading, endPoint: .bottomTrailing)
      )
      .clipShape(shape)
      .overlay(shape.stroke(colors.borderGlass, lineWidth: 1))
  }
}
