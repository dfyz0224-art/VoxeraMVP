import SwiftUI

struct ResearchLinkItem: Identifiable {
  let id = UUID()
  let title: String
  let url: String
}

enum AboutResearchLinks {
  static let all: [ResearchLinkItem] = [
    ResearchLinkItem(
      title: "Emotional Speech Recognition: Resources, Features, and Methods",
      url: "https://doi.org/10.1016/j.specom.2006.04.003"
    ),
    ResearchLinkItem(
      title: "Speech Emotion Recognition Approaches: A Systematic Review (2023)",
      url: "https://doi.org/10.1016/j.specom.2023.102974"
    ),
    ResearchLinkItem(
      title: "A Generalizable Speech Emotion Recognition Model Reveals Depression and Remission",
      url: "https://doi.org/10.1111/acps.13388"
    ),
    ResearchLinkItem(
      title: "The Geneva Minimalistic Acoustic Parameter Set (GeMAPS)",
      url: "https://doi.org/10.1109/TAFFC.2015.2457417"
    ),
    ResearchLinkItem(
      title: "Attention Guided Learnable Time-Domain Filterbanks for Speech Depression Detection",
      url: "https://doi.org/10.1016/j.neunet.2023.05.041"
    )
  ]
}
