import SwiftUI

struct StatisticsView: View {
  @Binding var path: NavigationPath
  @EnvironmentObject private var history: HistoryStore
  @EnvironmentObject private var prefs: PreferencesStore
  @EnvironmentObject private var locale: LocaleStore
  @State private var period: MoodStatsPeriod = .week
  @State private var selectedFilterKey: String?

  private var s: AppStrings { locale.strings }
  private var scaleKeys: [String] { MoodStatisticsData.scaleKeys }
  private var colors: [Color] {
    MoodStatisticsData.chartColors(glass: prefs.themeType == .glass)
  }

  var body: some View {
    let built = MoodStatisticsData.dayPoints(entries: history.entries, period: period)
    let dayPoints = built.points
    let hasData = built.hasAny

    ZStack {
      BackgroundImageName()
      ScrollView {
        VStack(alignment: .leading, spacing: 16) {
          HStack {
            Button {
              if !path.isEmpty { path.removeLast() }
            } label: {
              Text("‹  \(s.back)")
            }
            .foregroundColor(heading)
            Spacer()
          }
          Text(s.statisticsTitle)
            .font(.title2.bold())
            .foregroundColor(heading)
            .frame(maxWidth: .infinity, alignment: .leading)
          Picker("", selection: $period) {
            Text(s.statisticsPeriodWeek).tag(MoodStatsPeriod.week)
            Text(s.statisticsPeriodMonth).tag(MoodStatsPeriod.month)
          }
          .pickerStyle(.segmented)
          if !hasData {
            Text(s.statisticsNoData)
              .foregroundColor(prefs.themeType.colors().backgroundTextSecondary)
              .padding(.top, 8)
          } else {
            ThemedCard(gradientIndex: 0) {
              VStack(alignment: .leading, spacing: 12) {
                MoodLineChartView(
                  dayPoints: dayPoints,
                  scaleKeys: scaleKeys,
                  colors: colors,
                  isLight: prefs.themeType == .light,
                  selectedOnlyKey: selectedFilterKey
                )
                .frame(height: 220)
                LazyVGrid(
                  columns: [GridItem(.adaptive(minimum: 100), spacing: 8)],
                  alignment: .leading,
                  spacing: 8
                ) {
                  ForEach(Array(scaleKeys.enumerated()), id: \.offset) { i, key in
                    let lineColor = colors[i]
                    let isSolo = selectedFilterKey == key
                    Button {
                      if selectedFilterKey == key { selectedFilterKey = nil } else { selectedFilterKey = key }
                    } label: {
                      HStack(spacing: 6) {
                        RoundedRectangle(cornerRadius: 2)
                          .fill(lineColor)
                          .frame(width: 4, height: 14)
                        Text(MoodStatisticsData.label(forKey: key, language: prefs.appLanguage))
                          .font(.caption)
                          .lineLimit(1)
                          .foregroundColor(.white)
                      }
                      .padding(.horizontal, 10)
                      .padding(.vertical, 8)
                      .background(isSolo ? lineColor.opacity(0.35) : Color.white.opacity(0.1))
                      .cornerRadius(12)
                    }
                    .buttonStyle(.plain)
                  }
                }
              }
            }
          }
        }
        .padding(20)
      }
    }
  }

  private var heading: Color {
    if prefs.themeType == .light { return prefs.themeType.colors().backgroundTextPrimary }
    return .white
  }
}

/// Вью без Swift Charts — сглаженные кривые в Canvas, как на Android.
private struct MoodLineChartView: View {
  let dayPoints: [MoodDayModel]
  let scaleKeys: [String]
  let colors: [Color]
  let isLight: Bool
  let selectedOnlyKey: String?

  var body: some View {
    Canvas { ctx, size in
      let w = size.width
      let h = size.height
      let pl: CGFloat = 36
      let pr: CGFloat = 12
      let pt: CGFloat = 8
      let pb: CGFloat = 28
      let axis = isLight ? Color(red: 0.1, green: 0.1, blue: 0.1) : Color.white.opacity(0.88)
      let grid = isLight ? Color.black.opacity(0.12) : Color.white.opacity(0.12)
      for t in 0...4 {
        let y = pt + (h - pt - pb) * (CGFloat(t) / 4)
        var g = pathLine(from: CGPoint(x: pl, y: y), to: CGPoint(x: w - pr, y: y))
        ctx.stroke(g, with: .color(grid), lineWidth: 1)
      }
      // y: 0–100 сетка без подписей (как в минималистичном варианте)
      let keys = selectedOnlyKey != nil ? [selectedOnlyKey!] : scaleKeys
      for key in keys {
        guard let ki = scaleKeys.firstIndex(of: key) else { continue }
        let c = colors[min(ki, colors.count - 1)]
        let segs = splitSegments(dayPoints, key: key, width: w - pl - pr)
        for seg in segs {
          guard seg.count >= 2 else { continue }
          var p = buildSmoothPath(seg: seg, w: w, h: h, pl: pl, pr: pr, pt: pt, pb: pb)
          ctx.stroke(p, with: .color(c), style: StrokeStyle(lineWidth: 2.5, lineCap: .round, lineJoin: .round))
        }
      }
      if !dayPoints.isEmpty {
        let count = dayPoints.count
        let labelIndices: [Int] = {
          if count == 1 { return [0] }
          if count <= 3 { return Array(0..<count) }
          return [0, count / 2, count - 1]
        }()
        for i in Set(labelIndices) {
          let d = dayPoints[i]
          let x: CGFloat
          if count == 1 { x = (pl + w - pr) / 2 } else { x = pl + CGFloat(d.index) / CGFloat(max(1, count - 1)) * (w - pl - pr) }
          ctx.draw(
            Text(d.label)
              .font(.system(size: 9))
              .foregroundStyle(axis),
            at: CGPoint(x: x, y: h - 4),
            anchor: .bottom
          )
        }
      }
    }
  }

  private func pathLine(from: CGPoint, to: CGPoint) -> Path {
    var p = Path()
    p.move(to: from)
    p.addLine(to: to)
    return p
  }

  private func splitSegments(_ points: [MoodDayModel], key: String, width: CGFloat) -> [[(CGFloat, CGFloat)]] {
    let N = max(1, points.count - 1)
    var segs: [[(CGFloat, CGFloat)]] = []
    var cur: [(CGFloat, CGFloat)] = []
    for p in points {
      guard let yv = p.values[key] else {
        if cur.count >= 2 { segs.append(cur) }
        cur = []
        continue
      }
      let xn = N == 0 ? 0.5 : CGFloat(p.index) / CGFloat(N)
      let yf = max(0, min(1, CGFloat(yv) / 100))
      cur.append((xn, yf))
    }
    if !cur.isEmpty {
      if cur.count == 1 { segs.append([cur[0], cur[0]]) } else { segs.append(cur) }
    }
    return segs
  }

  private func buildSmoothPath(
    seg: [(CGFloat, CGFloat)], w: CGFloat, h: CGFloat, pl: CGFloat, pr: CGFloat, pt: CGFloat, pb: CGFloat
  ) -> Path {
    let innerW = w - pl - pr
    let innerH = h - pt - pb
    func tx(_ x: CGFloat) -> CGFloat { pl + x * innerW }
    func ty(_ y: CGFloat) -> CGFloat { pt + (1 - y) * innerH }
    if seg.count < 2 { return Path() }
    if seg.count == 2, seg[0] == seg[1] {
      var p1 = Path()
      let a = CGPoint(x: tx(seg[0].0), y: ty(seg[0].1))
      p1.move(to: a)
      p1.addLine(to: CGPoint(x: a.x + 0.5, y: a.y))
      return p1
    }
    var path = Path()
    let p = seg.map { CGPoint(x: tx($0.0), y: ty($0.1)) }
    path.move(to: p[0])
    for i in 0..<(p.count - 1) {
      let p0 = i == 0 ? p[0] : p[i - 1]
      let p1 = p[i]
      let p2 = p[i + 1]
      let p3 = i + 2 < p.count ? p[i + 2] : p2
      let c1 = CGPoint(
        x: p1.x + (p2.x - p0.x) / 6,
        y: p1.y + (p2.y - p0.y) / 6
      )
      let c2 = CGPoint(
        x: p2.x - (p3.x - p1.x) / 6,
        y: p2.y - (p3.y - p1.y) / 6
      )
      path.addCurve(to: p2, control1: c1, control2: c2)
    }
    return path
  }
}
