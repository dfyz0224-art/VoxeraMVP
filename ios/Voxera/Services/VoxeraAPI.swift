import Foundation

enum VoxeraAPIError: Error, LocalizedError {
  case noToken
  case status(Int, String?)
  case decode
  case network(String)

  var errorDescription: String? {
    switch self {
    case .noToken:
      return "Нет API-токена в сборке. Задайте VOXERA_API_TOKEN в Scheme (Debug) или в Secrets.xcconfig и сделайте Clean Build."
    case .status(let code, let body):
      let snippet = (body ?? "")
        .trimmingCharacters(in: .whitespacesAndNewlines)
      let short = snippet.count > 180 ? String(snippet.prefix(180)) + "…" : snippet
      if short.isEmpty {
        return "Сервер ответил HTTP \(code)."
      }
      return "Сервер HTTP \(code): \(short)"
    case .decode:
      return "Ответ сервера не удалось разобрать (неожиданный JSON)."
    case .network(let message):
      return "Сеть: \(message)"
    }
  }
}

final class VoxeraAPI {
  private static let baseURL = URL(string: "https://tg.voxera.kz/api/v1/")!

  static func analyze(audioURL: URL, audioMime: String?, analysisType: String) async throws -> (
    AnalysisResponse, String?
  ) {
    let token = Secrets.voxeraApiToken
    guard !token.isEmpty else { throw VoxeraAPIError.noToken }

    var mime = audioMime ?? "audio/m4a"
    if mime.isEmpty { mime = "application/octet-stream" }

    var request = URLRequest(url: Self.baseURL.appendingPathComponent("integrations/analyze"))
    request.httpMethod = "POST"
    request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
    request.setValue("application/json", forHTTPHeaderField: "Accept")
    request.timeoutInterval = 180

    let boundary = "Boundary-\(UUID().uuidString)"
    request.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")

    let fileData = try Data(contentsOf: audioURL)
    let filename = audioURL.lastPathComponent

    var body = Data()
    body.append("--\(boundary)\r\n".data(using: .utf8)!)
    body.append(
      "Content-Disposition: form-data; name=\"audio\"; filename=\"\(filename)\"\r\n".data(using: .utf8)!)
    body.append("Content-Type: \(mime)\r\n\r\n".data(using: .utf8)!)
    body.append(fileData)
    body.append("\r\n".data(using: .utf8)!)

    body.append("--\(boundary)\r\n".data(using: .utf8)!)
    body.append(
      "Content-Disposition: form-data; name=\"analysis_type\"\r\n\r\n".data(using: .utf8)!)
    body.append(analysisType.data(using: .utf8)!)
    body.append("\r\n".data(using: .utf8)!)
    body.append("--\(boundary)--\r\n".data(using: .utf8)!)

    request.httpBody = body

    let respData: Data
    let response: URLResponse
    do {
      (respData, response) = try await URLSession.shared.data(for: request)
    } catch {
      throw VoxeraAPIError.network(error.localizedDescription)
    }
    let rawString = String(data: respData, encoding: .utf8)

    guard let http = response as? HTTPURLResponse else {
      throw VoxeraAPIError.status(-1, rawString)
    }
    guard http.statusCode == 200 else {
      throw VoxeraAPIError.status(http.statusCode, rawString)
    }

    let dec = JSONDecoder()
    if let model = try? dec.decode(AnalysisResponse.self, from: respData) {
      return (model, rawString)
    }
    throw VoxeraAPIError.decode
  }
}
