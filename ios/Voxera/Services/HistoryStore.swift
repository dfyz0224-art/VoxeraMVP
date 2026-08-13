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
  /// Shared across actors; not MainActor-isolated.
  nonisolated static let guestAccountKey = "guest"

  @Published private(set) var entries: [HistoryEntry] = []

  private var accountKey: String = guestAccountKey
  private let lock = NSLock()
  private var documentsDir: URL {
    FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
  }

  private var fileURL: URL {
    documentsDir.appendingPathComponent("history_\(accountKey).json")
  }

  private var legacyFileURL: URL {
    documentsDir.appendingPathComponent("history_voxera.json")
  }

  init() {
    load()
  }

  func setAccountKey(_ key: String) {
    let normalized = key.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
      ? Self.guestAccountKey
      : key.lowercased()
    guard normalized != accountKey else { return }
    accountKey = normalized
    load()
  }

  private func load() {
    migrateLegacyIfNeeded()
    guard let data = try? Data(contentsOf: fileURL) else {
      entries = []
      return
    }
    let dec = JSONDecoder()
    if let list = try? dec.decode([HistoryEntry].self, from: data) {
      entries = list.sorted { $0.timestamp > $1.timestamp }
    } else {
      entries = []
    }
  }

  private func migrateLegacyIfNeeded() {
    let fm = FileManager.default
    guard fm.fileExists(atPath: legacyFileURL.path),
      !fm.fileExists(atPath: fileURL.path)
    else { return }
    try? fm.copyItem(at: legacyFileURL, to: fileURL)
    try? fm.removeItem(at: legacyFileURL)
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
