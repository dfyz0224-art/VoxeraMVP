import Foundation

/// Период агрегации для графика настроения (история / «статистика»).
/// Вынесен в отдельный файл, чтобы был виден всем исходникам одного модуля независимо от группы в Xcode.
enum HistoryMoodPeriod: Equatable {
  case last24
  case week
  case month
  case customFromTo(from: Date, to: Date)
}
