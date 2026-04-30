import SwiftUI
import UIKit

// Вынесено из FlowScreens: подключите файл в Target Membership приложения Voxera (как HistoryMoodPeriod.swift).
// Mirrors Android `RecordingScreen.kt` — liquid glass, neon rim, water rings, breathing scale.

enum RecordingVisualTokens {
  static let primaryGlow = Color.white
  static let recordButtonSize: CGFloat = 200
  static let micImageSize: CGFloat = 140
  static let ringColor = Color.white
}

// MARK: - Water rings (Android `WaterRings`)

struct RecordingWaterRings: View {
  var isActive: Bool
  var center: CGPoint
  var buttonRadius: CGFloat

  var body: some View {
    TimelineView(.animation(minimumInterval: 1 / 60, paused: !isActive)) { context in
      Canvas { c, size in
        let t = context.date.timeIntervalSince1970
        let ringDuration: Double = 3.5
        let ringGap: Double = 0.2
        let minR = buttonRadius
        let maxR = max(size.width, size.height) * 1.5

        for i in 0..<3 {
          let delay = Double(i) * ringGap
          var phase = (t - delay).truncatingRemainder(dividingBy: ringDuration) / ringDuration
          if phase < 0 { phase += 1 }
          let phaseF = CGFloat(phase)
          let currentRadius = minR + phaseF * (maxR - minR)
          let strokeW = (3.0 - 2.0 * phaseF).clamped(to: 0.5...3)
          let finalAlpha: CGFloat
          if phaseF < 0.05 {
            finalAlpha = phaseF / 0.05
          } else if phaseF < 0.3 {
            finalAlpha = 1
          } else {
            finalAlpha = 1 * (1 - (phaseF - 0.3) / 0.7)
          }
          if finalAlpha < 0.01 { continue }

          for idx in 1...3 {
            let glowR = currentRadius + CGFloat(idx) * 3
            let glowW = strokeW + CGFloat(idx) * 1.5
            let a = finalAlpha * (0.15 / CGFloat(max(idx, 1)))
            let o = CGRect(
              x: center.x - glowR,
              y: center.y - glowR,
              width: glowR * 2,
              height: glowR * 2
            )
            let pg = Path(ellipseIn: o)
            c.stroke(
              pg,
              with: .color(RecordingVisualTokens.ringColor.opacity(a)),
              style: StrokeStyle(lineWidth: glowW, lineCap: .round, lineJoin: .round)
            )
          }

          let mainRect = CGRect(
            x: center.x - currentRadius,
            y: center.y - currentRadius,
            width: currentRadius * 2,
            height: currentRadius * 2
          )
          c.stroke(
            Path(ellipseIn: mainRect),
            with: .color(RecordingVisualTokens.primaryGlow.opacity(finalAlpha)),
            style: StrokeStyle(lineWidth: strokeW, lineCap: .round, lineJoin: .round)
          )
        }
      }
    }
  }
}

private extension Comparable {
  func clamped(to r: ClosedRange<Self>) -> Self {
    min(max(self, r.lowerBound), r.upperBound)
  }
}

// MARK: - Breathing (idle 0.98…1.02 / hold 0.94…1.06)

struct RecordingBreathReader: View {
  var isRecording: Bool
  @State private var t0 = Date()

  var body: some View {
    TimelineView(.animation(minimumInterval: 1 / 30)) { context in
      let period = isRecording ? 1.2 : 2.0
      let elapsed = context.date.timeIntervalSince(t0)
      let phase = elapsed / period * 2 * .pi
      let scale: CGFloat = isRecording
        ? 1.0 + 0.06 * sin(phase)
        : 1.0 + 0.02 * sin(phase)
      Color.clear
        .preference(key: RecordingBreathKey.self, value: scale)
    }
    .onChange(of: isRecording) { _, _ in
      t0 = Date()
    }
  }
}

struct RecordingBreathKey: PreferenceKey {
  static var defaultValue: CGFloat = 1
  static func reduce(value: inout CGFloat, nextValue: () -> CGFloat) {
    value = nextValue()
  }
}

// MARK: - Liquid-glass style record button

struct LiquidGlassRecordButton: View {
  @Environment(\.voxeraTheme) private var voxeraTheme
  var isRecording: Bool
  var breathScale: CGFloat
  var action: () -> Void

  var body: some View {
    let glow = RecordingVisualTokens.primaryGlow
    let tintRecording = Color.white.opacity(0.02)
    Button(action: action) {
      ZStack {
        Circle()
          .fill(
            .linearGradient(
              colors: [
                Color.white.opacity(voxeraTheme == .light ? 0.42 : 0.22),
                Color.white.opacity(voxeraTheme == .light ? 0.12 : 0.06)
              ],
              startPoint: .topLeading,
              endPoint: .bottomTrailing
            )
          )
          .background {
            Circle()
              .fill(.ultraThinMaterial)
          }
          .background {
            Circle()
              .fill(
                .radialGradient(
                  colors: [
                    glow.opacity(isRecording ? 0.08 : 0.04),
                    .clear, .clear
                  ],
                  center: .init(x: 0.3, y: 0.25),
                  startRadius: 0,
                  endRadius: 120
                )
              )
          }
          .overlay {
            if isRecording {
              Circle()
                .fill(tintRecording)
            }
          }
        RecordingButtonNeonRings(isRecording: isRecording)
        if UIImage(named: "ic_mic_2") != nil {
          Image("ic_mic_2")
            .resizable()
            .renderingMode(.template)
            .scaledToFit()
            .frame(width: RecordingVisualTokens.micImageSize, height: RecordingVisualTokens.micImageSize)
            .foregroundColor(micTint)
        } else {
          Image(systemName: "mic.fill")
            .resizable()
            .scaledToFit()
            .frame(width: 88, height: 88)
            .foregroundColor(micTint)
        }
      }
      .frame(width: RecordingVisualTokens.recordButtonSize, height: RecordingVisualTokens.recordButtonSize)
      .scaleEffect(breathScale)
      .shadow(
        color: glow.opacity(isRecording ? 0.32 : 0.18),
        radius: isRecording ? 32 : 20,
        y: isRecording ? 10 : 6
      )
      .shadow(
        color: Color(red: 0.45, green: 0.78, blue: 0.95).opacity(isRecording ? 0.22 : 0.1),
        radius: isRecording ? 18 : 12,
        y: 4
      )
    }
    .buttonStyle(RecordingPressScaleButtonStyle())
  }

  private var micTint: Color {
    switch voxeraTheme {
    case .light:
      return Color(red: 0.1, green: 0.15, blue: 0.22)
    case .glass:
      return .white.opacity(isRecording ? 1 : 0.9)
    }
  }
}

private struct RecordingPressScaleButtonStyle: ButtonStyle {
  func makeBody(configuration: Configuration) -> some View {
    configuration.label
      .scaleEffect(configuration.isPressed ? 0.92 : 1)
  }
}

private struct RecordingButtonNeonRings: View {
  var isRecording: Bool
  var body: some View {
    let glow = RecordingVisualTokens.primaryGlow
    Canvas { c, s in
      let center = CGPoint(x: s.width / 2, y: s.height / 2)
      let baseR = min(s.width, s.height) / 2 - 2
      let glowAlpha: CGFloat = isRecording ? 1 : 0.8
      for g in 1...3 {
        let r = baseR + CGFloat(g) * 2
        let w = 4 + CGFloat(g) * 2
        let a = glowAlpha * (0.2 / CGFloat(g))
        c.stroke(
          Path(ellipseIn: CGRect(x: center.x - r, y: center.y - r, width: 2 * r, height: 2 * r)),
          with: .color(glow.opacity(a)),
          style: StrokeStyle(lineWidth: w, lineCap: .round, lineJoin: .round)
        )
      }
      let sw: CGFloat = isRecording ? 4 : 2.5
      c.stroke(
        Path(ellipseIn: CGRect(
          x: center.x - baseR, y: center.y - baseR,
          width: 2 * baseR, height: 2 * baseR
        )),
        with: .color(glow.opacity(glowAlpha)),
        style: StrokeStyle(lineWidth: sw, lineCap: .round, lineJoin: .round)
      )
    }
  }
}
