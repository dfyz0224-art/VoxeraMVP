import Foundation

enum VoxeraAPIError: Error {
  case noToken
  case status(Int, String?)
  case decode
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

    let (respData, response) = try await URLSession.shared.data(for: request)
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
