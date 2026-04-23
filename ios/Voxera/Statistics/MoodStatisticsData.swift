import Foundation
import SwiftUI

enum MoodStatsPeriod: String, CaseIterable {
  case week
  case month
}

enum MoodStatisticsData {
  static let scaleKeys: [String] = [
    "emo_engage",
    "self_control",
    "stress_tolerance",
    "authority",
    "person_harmonicity",
    "expressivity"
  ]

  static func label(forKey key: String, language: AppLanguage) -> String {
    let k = key.lowercased()
    let ru: [String: String] = [
      "emo_engage": "Вдохновенность",
      "self_control": "Самоконтроль",
      "stress_tolerance": "Стрессоустойчивость",
      "authority": "Властность",
      "person_harmonicity": "Уравновешенность",
      "expressivity": "Экспрессивность"
    ]
    let en: [String: String] = [
      "emo_engage": "Inspiration",
      "self_control": "Self-control",
      "stress_tolerance": "Stress resistance",
      "authority": "Dominance",
      "person_harmonicity": "Balance",
      "expressivity": "Expressiveness"
    ]
    let zh: [String: String] = [
      "emo_engage": "灵感动机",
      "self_control": "自我控制",
      "stress_tolerance": "抗压能力",
      "authority": "主导性",
      "person_harmonicity": "心理平衡",
      "expressivity": "表达力"
    ]
    let kz: [String: String] = [
      "emo_engage": "Шабыттылық",
      "self_control": "Өзін-өзі басқару",
      "stress_tolerance": "Стреске төзімділік",
      "authority": "Басқарушылық",
      "person_harmonicity": "Тұрақтылық",
      "expressivity": "Еркіндік"
    ]
    switch language {
    case .ru: return ru[k] ?? key
    case .en: return en[k] ?? key
    case .zh: return zh[k] ?? en[k] ?? key
    case .kz: return kz[k] ?? en[k] ?? key
    }
  }

  static func chartColors(glass: Bool) -> [Color] {
    if glass {
      return [
        Color(red: 1, green: 0.36, blue: 0.54),
        Color(red: 0.39, green: 0.65, blue: 0.96),
        Color(red: 0.4, green: 0.73, blue: 0.42),
        Color(red: 1, green: 0.72, blue: 0.3),
        Color(red: 0.8, green: 0.55, blue: 0.87),
        Color(red: 0.2, green: 0.86, blue: 0.89)
      ]
    }
    return [
      Color(red: 0.76, green: 0.09, blue: 0.36),
      Color(red: 0.05, green: 0.28, blue: 0.63),
      Color(red: 0.11, green: 0.37, blue: 0.13),
      Color(red: 0.9, green: 0.33, blue: 0),
      Color(red: 0.29, green: 0, blue: 0.51),
      Color(red: 0, green: 0.41, blue: 0.39)
    ]
  }

  /// День 0..n-1, подписи по дате, значения по ключам (nil если нет замера).
  static func dayPoints(
    entries: [HistoryEntry],
    period: MoodStatsPeriod
  ) -> (points: [MoodDayModel], hasAny: Bool) {
    let days = period == .week ? 7 : 30
    let cal = Calendar.current
    let today = cal.startOfDay(for: Date())
    guard let start = cal.date(byAdding: .day, value: -(days - 1), to: today) else {
      return ([], false)
    }
    var byDay: [String: [String: Int]] = [:]
    let keyDay: (Date) -> String = { d in
      let c = cal.dateComponents([.year, .month, .day], from: d)
      return "\(c.year!)-\(c.month!)-\(c.day!)"
    }
    let sorted = entries
      .filter { $0.analysisType == "emostate" }
      .sorted { $0.timestamp < $1.timestamp }
    for e in sorted {
      guard let data = e.responseJson.data(using: .utf8),
        let res = try? JSONDecoder().decode(AnalysisResponse.self, from: data),
        let scales = res.result?.emoScales, !scales.isEmpty
      else { continue }
      let date = Date(timeIntervalSince1970: e.timestamp / 1000)
      let d0 = cal.startOfDay(for: date)
      if d0 < start || d0 > today { continue }
      var m: [String: Int] = [:]
      for s in scales {
        m[s.name.lowercased()] = s.value
      }
      var dayMap = byDay[keyDay(d0), default: [:]]
      for k in scaleKeys {
        if let v = m[k] { dayMap[k] = v }
      }
      if !dayMap.isEmpty {
        byDay[keyDay(d0)] = dayMap
      }
    }
    let fmt = DateFormatter()
    fmt.dateStyle = .short
    var out: [MoodDayModel] = []
    var hasAny = false
    for i in 0..<days {
      guard let d = cal.date(byAdding: .day, value: i, to: start) else { continue }
      let dict = byDay[keyDay(d)] ?? [:]
      var vals: [String: Int?] = [:]
      for k in scaleKeys {
        let v = dict[k]
        vals[k] = v
        if v != nil { hasAny = true }
      }
      out.append(MoodDayModel(index: i, label: fmt.string(from: d), values: vals))
    }
    return (out, hasAny)
  }
}

struct MoodDayModel: Identifiable {
  var id: Int { index }
  let index: Int
  let label: String
  let values: [String: Int?]
}
