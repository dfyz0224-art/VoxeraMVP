import Foundation

struct HistoryEntry: Codable, Identifiable, Equatable {
  let id: String
  let timestamp: TimeInterval
  let analysisType: String
  let responseJson: String
  let rawApiResponse: String?
}

@MainActor
final class HistoryStore: ObservableObject {
  @Published private(set) var entries: [HistoryEntry] = []

  private let fileURL: URL
  private let lock = NSLock()

  init() {
    let dir = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
    fileURL = dir.appendingPathComponent("history_voxera.json")
    load()
  }

  private func load() {
    guard let data = try? Data(contentsOf: fileURL) else { return }
    let dec = JSONDecoder()
    if let list = try? dec.decode([HistoryEntry].self, from: data) {
      entries = list.sorted { $0.timestamp > $1.timestamp }
    }
  }

  private func save() {
    lock.lock()
    defer { lock.unlock() }
    let enc = JSONEncoder()
    enc.outputFormatting = [.sortedKeys]
    if let data = try? enc.encode(entries) {
      try? data.write(to: fileURL)
    }
  }

  func add(analysisType: String, response: AnalysisResponse, rawApi: String?) {
    let enc = JSONEncoder()
    guard let jsonData = try? enc.encode(response),
      let jsonStr = String(data: jsonData, encoding: .utf8)
    else { return }
    let e = HistoryEntry(
      id: UUID().uuidString,
      timestamp: Date().timeIntervalSince1970 * 1000,
      analysisType: analysisType,
      responseJson: jsonStr,
      rawApiResponse: rawApi
    )
    entries = ([e] + entries).sorted { $0.timestamp > $1.timestamp }
    save()
  }

  func clear() {
    entries = []
    try? FileManager.default.removeItem(at: fileURL)
  }
}
