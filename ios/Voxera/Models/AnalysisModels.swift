import Foundation

struct AnalysisResponse: Codable, Equatable {
  var success: Bool = false
  var analysisType: String = ""
  var result: AnalysisResult?

  enum CodingKeys: String, CodingKey {
    case success
    case analysisType = "analysis_type"
    case result
  }
}

struct AnalysisResult: Codable, Equatable {
  var psyTypes: [PsyType]?
  var emoScales: [EmoScale]?
  var description: String?

  enum CodingKeys: String, CodingKey {
    case psyTypes = "psy_types"
    case emoScales = "emo_scales"
    case description
  }
}

struct PsyType: Codable, Equatable {
  let id: Int
  let name: String
  let value: Double
}

struct EmoScale: Codable, Equatable {
  let id: Int
  let name: String
  let value: Int
}
