import Foundation
import SwiftUI

// График настроения для History: MoodLineChartView, MoodTimeSeriesBuild, MoodStatisticsData.
// Лежит рядом с SecondaryScreens — убедитесь, что файл в Target Membership приложения Voxera.

// MARK: - Models

struct MoodDayModel: Identifiable {
  var id: Int { index }
  let index: Int
  let label: String
  let values: [String: Int?]
  var xInWindow: Double? = nil
}

struct MoodChartResult {
  let points: [MoodDayModel]
  let hasAny: Bool
  let windowStartMs: TimeInterval?
  let windowEndMs: TimeInterval?
}

// MARK: - Build (aligned with Android MoodTimeSeries)

enum MoodTimeSeriesBuild {
  static let scaleKeys: [String] = [
    "emo_engage",
    "self_control",
    "stress_tolerance",
    "authority",
    "person_harmonicity",
    "energy_level"
  ]

  static func build(entries: [HistoryEntry], period: HistoryMoodPeriod) -> MoodChartResult {
    let emo = collectEmoSamples(from: entries)
    switch period {
    case .last24:
      return buildLast24(samples: emo, scaleKeys: scaleKeys)
    case .week:
      return buildDayWindow(samples: emo, nDays: 7, scaleKeys: scaleKeys)
    case .month:
      return buildDayWindow(samples: emo, nDays: 30, scaleKeys: scaleKeys)
    case .customFromTo(let from, let to):
      return buildCustomDay(samples: emo, from: from, to: to, scaleKeys: scaleKeys)
    }
  }

  // MARK: samples

  private struct EmoSample { let tMs: TimeInterval; let v: [String: Int] }

  private static func collectEmoSamples(from entries: [HistoryEntry]) -> [EmoSample] {
    var out: [EmoSample] = []
    for e in entries {
      guard e.analysisType == "emostate",
        let data = e.responseJson.data(using: .utf8),
        let res = try? JSONDecoder().decode(AnalysisResponse.self, from: data),
        let scales = res.result?.emoScales, !scales.isEmpty
      else { continue }
      var m: [String: Int] = [:]
      for s in scales {
        let raw = s.name.lowercased()
        m[raw] = s.value
        let energyAliases = ["жизнерадостность", "cheerfulness", "vitality", "energy level", "expressivity"]
        if energyAliases.contains(raw), m["energy_level"] == nil {
          m["energy_level"] = s.value
        }
      }
      var by: [String: Int] = [:]
      for k in scaleKeys {
        if let v = m[k] ?? m[k.replacingOccurrences(of: "_", with: " ")] {
          by[k] = v
        }
      }
      if by.isEmpty { continue }
      out.append(EmoSample(tMs: e.timestamp, v: by))
    }
    return out.sorted { $0.tMs < $1.tMs }
  }

  private static func average(_ maps: [[String: Int]], scaleKeys: [String]) -> [String: Int?] {
    if maps.isEmpty { return Dictionary(uniqueKeysWithValues: scaleKeys.map { ($0, nil) }) }
    var r: [String: Int?] = [:]
    for k in scaleKeys {
      let vals = maps.compactMap { $0[k] }
      if vals.isEmpty { r[k] = nil }
      else { r[k] = Int((Double(vals.reduce(0, +)) / Double(vals.count)).rounded() + 1e-6) }
    }
    return r
  }

  // MARK: 24h

  private static func buildLast24(
    samples: [EmoSample], scaleKeys: [String]
  ) -> MoodChartResult {
    let h24: TimeInterval = 24 * 60 * 60 * 1000
    if samples.isEmpty {
      return MoodChartResult(points: [], hasAny: false, windowStartMs: nil, windowEndMs: nil)
    }
    let s = samples
    let lastA = s.last!.tMs
    let prevBeforeLast: TimeInterval? = s.count >= 2 ? s[s.count - 2].tMs : nil
    let gapOver = prevBeforeLast != nil && (lastA - prevBeforeLast! > h24)

    let tStart: TimeInterval
    let tEnd: TimeInterval
    if s.count == 1 || gapOver {
      tStart = lastA
      tEnd = lastA + h24
    } else {
      let inLast = s.filter { $0.tMs >= lastA - h24 && $0.tMs <= lastA }
      tStart = inLast.map(\.tMs).min()!
      tEnd = tStart + h24
    }

    let inW = s.filter { $0.tMs >= tStart && $0.tMs < tEnd }
    var pts: [MoodDayModel] = []
    var hasAny = false
    let tf = DateFormatter()
    tf.dateFormat = "HH:mm"
    for (i, sp) in inW.enumerated() {
      let raw = (sp.tMs - tStart) / h24
      let x = max(0, min(1, raw))
      let d = Date(timeIntervalSince1970: sp.tMs / 1000)
      var vals: [String: Int?] = [:]
      for k in scaleKeys {
        if let n = sp.v[k] { vals[k] = n; hasAny = true } else { vals[k] = nil }
      }
      pts.append(MoodDayModel(
        index: i,
        label: tf.string(from: d),
        values: vals,
        xInWindow: x
      ))
    }
    return MoodChartResult(
      points: pts,
      hasAny: hasAny,
      windowStartMs: tStart,
      windowEndMs: tEnd
    )
  }

  // MARK: week / month

  private static func buildDayWindow(
    samples: [EmoSample], nDays: Int, scaleKeys: [String]
  ) -> MoodChartResult {
    let cal = Calendar.current
    if samples.isEmpty {
      let endD = cal.startOfDay(for: Date())
      guard let startD = cal.date(byAdding: .day, value: -(nDays - 1), to: endD) else {
        return MoodChartResult(points: [], hasAny: false, windowStartMs: nil, windowEndMs: nil)
      }
      return emptyDayGrid(from: startD, n: nDays, scaleKeys: scaleKeys)
    }

    func dayStart(_ tMs: TimeInterval) -> Date {
      let d = Date(timeIntervalSince1970: tMs / 1000)
      return cal.startOfDay(for: d)
    }
    let firstD = dayStart(samples.map(\.tMs).min()!)
    let lastD = dayStart(samples.map(\.tMs).max()!)
    let span = (cal.dateComponents([.day], from: firstD, to: lastD).day ?? 0) + 1
    let startD: Date
    if span >= nDays, let s = cal.date(byAdding: .day, value: -(nDays - 1), to: lastD) {
      startD = s
    } else {
      startD = firstD
    }
    var byDay: [Date: [[String: Int]]] = [:]
    for sp in samples {
      let d0 = dayStart(sp.tMs)
      byDay[d0, default: []].append(sp.v)
    }
    var pts: [MoodDayModel] = []
    var hasAny = false
    let df = DateFormatter()
    df.dateStyle = .short
    for i in 0..<nDays {
      guard let d = cal.date(byAdding: .day, value: i, to: startD) else { continue }
      let maps = byDay[d] ?? []
      let av = average(maps, scaleKeys: scaleKeys)
      for (_, v) in av where v != nil { hasAny = true }
      pts.append(MoodDayModel(
        index: i,
        label: df.string(from: d),
        values: av,
        xInWindow: nil
      ))
    }
    return MoodChartResult(
      points: pts,
      hasAny: hasAny,
      windowStartMs: nil,
      windowEndMs: nil
    )
  }

  private static func emptyDayGrid(
    from startD: Date, n: Int, scaleKeys: [String]
  ) -> MoodChartResult {
    let cal = Calendar.current
    let df = DateFormatter()
    df.dateStyle = .short
    var pts: [MoodDayModel] = []
    for i in 0..<n {
      let d = cal.date(byAdding: .day, value: i, to: startD) ?? startD
      pts.append(MoodDayModel(
        index: i,
        label: df.string(from: d),
        values: Dictionary(uniqueKeysWithValues: scaleKeys.map { ($0, nil) })
      ))
    }
    return MoodChartResult(points: pts, hasAny: false, windowStartMs: nil, windowEndMs: nil)
  }

  // MARK: custom

  private static func buildCustomDay(
    samples: [EmoSample], from: Date, to: Date, scaleKeys: [String]
  ) -> MoodChartResult {
    let cal = Calendar.current
    let a = min(cal.startOfDay(for: from), cal.startOfDay(for: to))
    let b = max(cal.startOfDay(for: from), cal.startOfDay(for: to))
    let n = (cal.dateComponents([.day], from: a, to: b).day ?? 0) + 1
    if n < 1 { return MoodChartResult(points: [], hasAny: false, windowStartMs: nil, windowEndMs: nil) }
    var byDay: [Date: [[String: Int]]] = [:]
    for sp in samples {
      let d0 = cal.startOfDay(for: Date(timeIntervalSince1970: sp.tMs / 1000))
      if d0 < a || d0 > b { continue }
      byDay[d0, default: []].append(sp.v)
    }
    var pts: [MoodDayModel] = []
    var hasAny = false
    let df = DateFormatter()
    df.dateStyle = .short
    for i in 0..<n {
      let d = cal.date(byAdding: .day, value: i, to: a) ?? a
      let maps = byDay[d] ?? []
      let av = average(maps, scaleKeys: scaleKeys)
      for (_, v) in av where v != nil { hasAny = true }
      pts.append(MoodDayModel(
        index: i,
        label: df.string(from: d),
        values: av,
        xInWindow: nil
      ))
    }
    return MoodChartResult(
      points: pts,
      hasAny: hasAny,
      windowStartMs: nil,
      windowEndMs: nil
    )
  }
}

// MARK: - Chart (Canvas)

struct MoodLineChartView: View {
  let dayPoints: [MoodDayModel]
  let scaleKeys: [String]
  let colors: [Color]
  let isLight: Bool
  /// Пусто или весь набор `scaleKeys` — все линии; иначе только выбранные.
  var activeFilter: Set<String> = []
  var windowStartMs: TimeInterval? = nil
  var windowEndMs: TimeInterval? = nil

  var body: some View {
    Canvas { ctx, size in
      let w = size.width
      let h = size.height
      let pl: CGFloat = 36
      let pr: CGFloat = 12
      let pt: CGFloat = 8
      let pb: CGFloat = 28
      let axis = isLight ? Color(red: 0.1, green: 0.1, blue: 0.1) : Color.white.opacity(0.92)
      let grid = isLight ? Color.black.opacity(0.12) : Color.white.opacity(0.16)
      let pointRing = Color.white
      let lineW: CGFloat = 4
      let pointR: CGFloat = 3
      let ringW: CGFloat = 1
      for t in 0...4 {
        let y = pt + (h - pt - pb) * (CGFloat(t) / 4)
        var g = pathLine(from: CGPoint(x: pl, y: y), to: CGPoint(x: w - pr, y: y))
        ctx.stroke(g, with: .color(grid), lineWidth: 1)
      }
      let allKeys = Set(scaleKeys)
      let keys: [String] = {
        if activeFilter.isEmpty { return scaleKeys }
        if activeFilter == allKeys { return scaleKeys }
        return scaleKeys.filter { activeFilter.contains($0) }
      }()
      for key in keys {
        guard let ki = scaleKeys.firstIndex(of: key) else { continue }
        let c = colors[min(ki, colors.count - 1)]
        let segs = splitSegments(dayPoints, key: key)
        for seg in segs {
          guard !seg.isEmpty else { continue }
          if seg.count >= 2 {
            let p = buildLinePath(seg: seg, w: w, h: h, pl: pl, pr: pr, pt: pt, pb: pb)
            ctx.stroke(
              p,
              with: .color(c),
              style: StrokeStyle(lineWidth: lineW, lineCap: .round, lineJoin: .round)
            )
          }
          let innerW = w - pl - pr
          let innerH = h - pt - pb
          for xyn in seg {
            let x = pl + xyn.0 * innerW
            let yp = pt + (1 - xyn.1) * innerH
            var dot = Path(ellipseIn: CGRect(x: x - pointR, y: yp - pointR, width: pointR * 2, height: pointR * 2))
            ctx.fill(dot, with: .color(c))
            ctx.stroke(
              dot,
              with: .color(pointRing),
              lineWidth: ringW
            )
          }
        }
      }
      if dayPoints.isEmpty { return }
      if let ws = windowStartMs, let we = windowEndMs, we > ws {
        for k in 0..<5 {
          let t = ws + (we - ws) * TimeInterval(k) / 4
          let d = Date(timeIntervalSince1970: t / 1000)
          let f = DateFormatter()
          f.dateFormat = "HH:mm"
          let xNorm = CGFloat(k) / 4
          let x = pl + xNorm * (w - pl - pr)
          ctx.draw(
            Text(f.string(from: d))
              .font(.system(size: 9))
              .foregroundStyle(axis),
            at: CGPoint(x: x, y: h - 4),
            anchor: .bottom
          )
        }
      } else {
        let count = dayPoints.count
        let labelIndices: [Int] = {
          if count == 1 { return [0] }
          if count <= 3 { return Array(0..<count) }
          if count <= 12 { return [0, count / 2, count - 1] }
          return (0..<5).map { k in (k * (count - 1)) / 4 }
        }()
        for i in Set(labelIndices) {
          guard i < dayPoints.count else { continue }
          let p = dayPoints[i]
          let xNorm: CGFloat
          if let xw = p.xInWindow { xNorm = CGFloat(xw) }
          else if count == 1 { xNorm = 0.5 }
          else { xNorm = CGFloat(i) / CGFloat(max(1, count - 1)) }
          let x = pl + xNorm * (w - pl - pr)
          ctx.draw(
            Text(p.label)
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

  private func splitSegments(_ points: [MoodDayModel], key: String) -> [[(CGFloat, CGFloat)]] {
    var cur: [(CGFloat, CGFloat)] = []
    for (i, p) in points.enumerated() {
      guard let opt = p.values[key], let yv = opt else { continue }
      let N = max(1, points.count - 1)
      let xn: CGFloat
      if let xw = p.xInWindow { xn = CGFloat(xw) }
      else { xn = points.count == 1 ? 0.5 : CGFloat(i) / CGFloat(N) }
      let yf = max(0, min(1, CGFloat(yv) / 100))
      cur.append((xn, yf))
    }
    if cur.isEmpty { return [] }
    if cur.count == 1 { return [[cur[0], cur[0]]] }
    return [cur]
  }

  private func buildLinePath(
    seg: [(CGFloat, CGFloat)], w: CGFloat, h: CGFloat, pl: CGFloat, pr: CGFloat, pt: CGFloat, pb: CGFloat
  ) -> Path {
    let innerW = w - pl - pr
    let innerH = h - pt - pb
    func tx(_ x: CGFloat) -> CGFloat { pl + x * innerW }
    func ty(_ y: CGFloat) -> CGFloat { pt + (1 - y) * innerH }
    if seg.isEmpty { return Path() }
    if seg.count == 1 {
      var p = Path()
      let a = CGPoint(x: tx(seg[0].0), y: ty(seg[0].1))
      p.move(to: a)
      p.addLine(to: a)
      return p
    }
    var path = Path()
    path.move(to: CGPoint(x: tx(seg[0].0), y: ty(seg[0].1)))
    for i in 1..<seg.count {
      path.addLine(to: CGPoint(x: tx(seg[i].0), y: ty(seg[i].1)))
    }
    return path
  }
}

// MARK: - Scale labels & colors — см. `MoodStatisticsData` в FlowScreens.swift (один модуль)
