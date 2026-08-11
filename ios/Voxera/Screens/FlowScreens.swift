import SwiftUI
import UIKit
import UniformTypeIdentifiers
import LiquidGlass

struct ConsentView: View {
  @Binding var path: NavigationPath
  @EnvironmentObject private var prefs: PreferencesStore
  @EnvironmentObject private var locale: LocaleStore
  @State private var agree1 = false
  @State private var agree2 = false
  var s: AppStrings { locale.strings }

  var body: some View {
    ZStack {
      BackgroundImageName()
      VStack(alignment: .leading, spacing: 14) {
        Text(s.privacyAndConsent)
          .font(.title3.bold())
          .foregroundColor(.white)
          .padding(.top, 8)
        ScrollView {
          ThemedCard(gradientIndex: 0) {
            Text(s.consentCardSummary)
              .foregroundColor(.white.opacity(0.95))
              .font(.callout)
              .fixedSize(horizontal: false, vertical: true)
          }
        }
        Toggle(s.consentVoice, isOn: $agree1).foregroundColor(.white)
        Toggle(s.consentPrivacy, isOn: $agree2).foregroundColor(.white)
        Button(s.consentOpenPrivacyPolicyButton) { path.append(AppRoute.privacyPolicy) }
          .foregroundColor(.white)
        Button(s.back) { path.removeLast() }
          .foregroundColor(.white.opacity(0.85))
        Button(s.start) {
          guard agree1 && agree2 else { return }
          prefs.setConsentGiven(true)
          if !path.isEmpty {
            path.removeLast()
          }
          path.append(AppRoute.recording)
        }
        .disabled(!agree1 || !agree2)
        .buttonStyle(.borderedProminent)
        .tint(.white.opacity(0.35))
      }
      .padding(.horizontal, 20)
      .padding(.bottom, 20)
    }
  }
}

struct PrivacyPolicyView: View {
  @Binding var path: NavigationPath
  @EnvironmentObject private var prefs: PreferencesStore
  @EnvironmentObject private var locale: LocaleStore
  var s: AppStrings { locale.strings }

  var body: some View {
    ZStack {
      BackgroundImageName()
      ScrollView {
        VStack(alignment: .leading, spacing: 12) {
          Button(s.back) { path.removeLast() }
            .foregroundColor(prefs.themeType.colors().backgroundTextPrimary)
          Text(s.privacyPolicyFullTitle)
            .font(.title2.bold())
            .foregroundColor(prefs.themeType.colors().backgroundTextPrimary)
          Text(s.privacyPolicyFullEnglishNote)
            .font(.caption)
            .foregroundColor(prefs.themeType.colors().backgroundTextSecondary)
          ThemedCard(gradientIndex: 0) {
            Text(PrivacyPolicyFullEN.text)
              .foregroundColor(.white)
              .font(.body)
          }
        }
        .padding(20)
      }
    }
  }
}

struct RecordingView: View {
  @Binding var path: NavigationPath
  @EnvironmentObject private var session: AnalysisSession
  @EnvironmentObject private var prefs: PreferencesStore
  @EnvironmentObject private var locale: LocaleStore
  @StateObject private var recorder = AudioRecorderService()
  @State private var recordingStartedAt: Date?
  @State private var isRecording = false
  @State private var showImporter = false
  @State private var breathScale: CGFloat = 1.0
  #if DEBUG
  @State private var testAudioError: String?
  #endif

  var s: AppStrings { locale.strings }

  private var titleColor: Color {
    let c = prefs.themeType.colors()
    return c.backgroundTextPrimary
  }

  private var secondaryOnBackground: Color {
    prefs.themeType.colors().backgroundTextSecondary
  }

  private func recordingRemainingSeconds(at date: Date) -> Int {
    guard isRecording, let t0 = recordingStartedAt else { return 30 }
    return max(0, 30 - Int(date.timeIntervalSince(t0)))
  }

  private func recordingTimerExpired(at date: Date) -> Bool {
    isRecording && recordingStartedAt != nil && recordingRemainingSeconds(at: date) == 0
  }

  var body: some View {
    GeometryReader { geo in
      let screenCenter = CGPoint(x: geo.size.width / 2, y: geo.size.height / 2)
      let ringMaxR = hypot(geo.size.width, geo.size.height) * 0.62
      ZStack {
        BackgroundImageName()
          .ignoresSafeArea()
        if isRecording {
          RecordingWaterRings(
            isActive: isRecording,
            center: screenCenter,
            buttonRadius: RecordingVisualTokens.recordButtonSize / 2,
            maxRingRadius: ringMaxR
          )
          .frame(width: geo.size.width, height: geo.size.height)
          .allowsHitTesting(false)
        }
        LiquidGlassRecordButton(
          isRecording: isRecording,
          breathScale: breathScale
        ) {
          Task { await toggleRecord() }
        }
        .position(screenCenter)
      }
      .frame(width: geo.size.width, height: geo.size.height)
      .overlay(alignment: .top) {
        TimelineView(.periodic(from: .now, by: 0.2)) { timeline in
          ScrollView(.vertical, showsIndicators: false) {
            VStack(spacing: 12) {
              Text(isRecording ? s.recordingInProgress : s.voiceRecording)
                .font(.system(size: 32, weight: .regular, design: .default))
                .foregroundColor(titleColor)
                .multilineTextAlignment(.center)
              if isRecording {
                if recordingTimerExpired(at: timeline.date) {
                  Text(s.tapToStop)
                    .font(.body)
                    .foregroundColor(secondaryOnBackground)
                    .multilineTextAlignment(.center)
                } else {
                  Text("\(recordingRemainingSeconds(at: timeline.date)) \(s.secondsShort)")
                    .font(.system(size: 32, weight: .semibold, design: .default))
                    .foregroundColor(titleColor)
                }
              } else {
                Text(s.tapToStart)
                  .font(.body)
                  .foregroundColor(secondaryOnBackground)
                  .multilineTextAlignment(.center)
                Text(s.saySentences)
                  .font(.body)
                  .foregroundColor(secondaryOnBackground)
                  .multilineTextAlignment(.center)
              }
            }
            .padding(.horizontal, 20)
            .padding(.top, max(2, geo.safeAreaInsets.top - 10))
            .frame(maxWidth: .infinity)
          }
          .frame(maxHeight: max(120, geo.size.height * 0.28), alignment: .top)
        }
      }
      .overlay(alignment: .bottom) {
        Group {
          if !isRecording {
            Button(s.uploadAudio) { showImporter = true }
              .font(.body.weight(.semibold))
              .foregroundColor(
                prefs.themeType == .light
                  ? Color(red: 0.04, green: 0.09, blue: 0.16)
                  : titleColor
              )
              .padding(.horizontal, 20)
              .padding(.vertical, 12)
              .background(
                RoundedRectangle(cornerRadius: 14, style: .continuous)
                  .fill(prefs.themeType == .light ? Color.white.opacity(0.92) : Color.white.opacity(0.16))
              )
              .overlay(
                RoundedRectangle(cornerRadius: 14, style: .continuous)
                  .stroke(
                    prefs.themeType == .light ? Color.black.opacity(0.12) : Color.white.opacity(0.38),
                    lineWidth: 1
                  )
              )
              .padding(.bottom, 12)
              .transition(.opacity.combined(with: .move(edge: .bottom)))
          }
        }
        .animation(.easeInOut(duration: 0.25), value: isRecording)
      }
    }
    .ignoresSafeArea(edges: .bottom)
    .overlay {
      RecordingBreathReader(isRecording: isRecording)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .allowsHitTesting(false)
        .accessibilityHidden(true)
    }
    .onPreferenceChange(RecordingBreathKey.self) { breathScale = $0 }
    #if DEBUG
    .overlay(alignment: .topTrailing) {
      Button {
        useTestBundleAudio()
      } label: {
        Text("Тест")
          .font(.body.weight(.semibold))
          .foregroundColor(recordingTestButtonColor)
          .padding(.horizontal, 10)
          .padding(.vertical, 8)
          .contentShape(Rectangle())
      }
      .buttonStyle(.borderless)
      .padding(.top, 6)
      .padding(.trailing, 4)
      .zIndex(99)
      .accessibilityLabel("Test audio (debug)")
    }
    .alert("Тест: аудио", isPresented: Binding(
      get: { testAudioError != nil },
      set: { if !$0 { testAudioError = nil } }
    )) {
      Button("OK", role: .cancel) { testAudioError = nil }
    } message: {
      if let testAudioError {
        Text(testAudioError)
      }
    }
    #endif
    .fileImporter(
      isPresented: $showImporter,
      allowedContentTypes: [.mpeg4Audio, .mp3, .wav, .audio],
      allowsMultipleSelection: false
    ) { result in
      guard case .success(let urls) = result, let url = urls.first else { return }
      guard url.startAccessingSecurityScopedResource() else { return }
      defer { url.stopAccessingSecurityScopedResource() }
      let dest = FileManager.default.temporaryDirectory.appendingPathComponent("upload.m4a")
      try? FileManager.default.removeItem(at: dest)
      try? FileManager.default.copyItem(at: url, to: dest)
      session.recordedFileURL = dest
      session.lastAudioMimeType = "audio/m4a"
      path.append(AppRoute.processing)
    }
    .onDisappear {
      recordingStartedAt = nil
      if recorder.isRecording { _ = recorder.stopRecording() }
    }
  }

  @MainActor
  private func toggleRecord() async {
    if isRecording {
      let url = recorder.stopRecording()
      isRecording = false
      recordingStartedAt = nil
      if let url, FileManager.default.fileExists(atPath: url.path) {
        let size: Int64
        if let attrs = try? FileManager.default.attributesOfItem(atPath: url.path) {
          size = attrs[.size] as? Int64 ?? 0
        } else {
          size = 0
        }
        if size > 0 {
          session.recordedFileURL = url
          session.lastAudioMimeType = nil
          path.append(AppRoute.processing)
        }
      }
      return
    }
    let ok = await recorder.requestPermission()
    guard ok else { return }
    let url = FileManager.default.temporaryDirectory.appendingPathComponent("recording_\(UUID().uuidString).m4a")
    try? FileManager.default.removeItem(at: url)
    do {
      try recorder.startRecording(to: url)
      isRecording = true
      recordingStartedAt = Date()
    } catch {}
  }

  #if DEBUG
  private var recordingTestButtonColor: Color {
    switch prefs.themeType {
    case .light:
      return Color(red: 0.05, green: 0.11, blue: 0.23)
    case .glass:
      return .white.opacity(0.9)
    }
  }

  /// Debug: bundle audio (as `android:copy from assets`); tries several known filenames.
  private func useTestBundleAudio() {
    let candidates: [(name: String, ext: String, mime: String)] = [
      ("audio_test", "ogg", "audio/ogg"),
      ("audio_test_2", "ogg", "audio/ogg"),
      ("test", "wav", "audio/wav")
    ]
    for c in candidates {
      if let u = Bundle.main.url(forResource: c.name, withExtension: c.ext) {
        tryRunTestCopy(
          from: u, mime: c.mime, tried: candidates.map { "\($0.name).\($0.ext)" })
        return
      }
    }
    testAudioError =
      "В bundle нет тестового файла. Добавьте `audio_test.ogg` в Voxera/Resources (или `test.wav`) и сгенерируйте проект."
  }

  private func tryRunTestCopy(from src: URL, mime: String, tried: [String]) {
    testAudioError = nil
    let ext = src.pathExtension.isEmpty ? "ogg" : src.pathExtension
    let dest = FileManager.default.temporaryDirectory.appendingPathComponent("recording_test.\(ext)")
    do {
      try? FileManager.default.removeItem(at: dest)
      try FileManager.default.copyItem(at: src, to: dest)
      session.recordedFileURL = dest
      session.lastAudioMimeType = mime
      path.append(AppRoute.processing)
    } catch {
      testAudioError = "Не удалось скопировать тест: \(error.localizedDescription). Проверьте, что в Copy Bundle Resources есть: \(tried.joined(separator: ", "))."
    }
  }
  #endif
}

/// Снимает шаги записи и обработки со стека, чтобы с экрана результата «Назад» вело на главный выбор режима.
private func popRecordingPipelineFromPath(_ path: inout NavigationPath) {
  if path.count >= 2 {
    path.removeLast(2)
  } else if path.count == 1 {
    path.removeLast()
  }
}

struct ProcessingView: View {
  @Binding var path: NavigationPath
  @EnvironmentObject private var session: AnalysisSession
  @EnvironmentObject private var history: HistoryStore
  @EnvironmentObject private var locale: LocaleStore
  @State private var errorText: String?

  var s: AppStrings { locale.strings }

  var body: some View {
    ZStack {
      BackgroundImageName()
      VStack(spacing: 20) {
        ProgressView()
          .tint(.white)
          .scaleEffect(1.4)
        Text(s.analyzing).font(.title2.bold()).foregroundColor(.white)
        Text(s.analyzingSubtitle).foregroundColor(.white.opacity(0.85))
        if let errorText {
          Text(errorText).foregroundColor(.red.opacity(0.9)).padding()
        }
      }
    }
    .onAppear {
      Task { await run() }
    }
  }

  @MainActor
  private func run() async {
    guard let url = session.recordedFileURL else {
      session.lastResultJson = "Ошибка: нет файла"
      popRecordingPipelineFromPath(&path)
      path.append(AppRoute.result)
      return
    }
    do {
      let (resp, raw) = try await VoxeraAPI.analyze(
        audioURL: url,
        audioMime: session.lastAudioMimeType,
        analysisType: session.analysisType
      )
      session.lastAnalysisResponse = resp
      session.lastRawApiResponse = raw
      if let enc = try? JSONEncoder().encode(resp),
        let pretty = String(data: enc, encoding: .utf8)
      {
        session.lastResultJson = pretty
      } else {
        session.lastResultJson = raw
      }
      history.add(analysisType: session.analysisType, response: resp, rawApi: raw)
      popRecordingPipelineFromPath(&path)
      path.append(AppRoute.result)
    } catch {
      session.lastAnalysisResponse = nil
      session.lastRawApiResponse = nil
      if let err = error as? VoxeraAPIError, case .noToken = err {
        session.lastResultJson = """
          Ошибка: нет API-токена (noToken).
          1) Скопируйте ios/Voxera/Secrets.xcconfig.example → Secrets.xcconfig и укажите VOXERA_API_TOKEN (как в Android secrets.properties), пересоберите.
          2) Либо в Debug: Product → Scheme → Edit Scheme… → Run → Environment Variables: VOXERA_API_TOKEN = <ваш_токен>
          """
      } else {
        session.lastResultJson = "Ошибка: \(error)"
      }
      popRecordingPipelineFromPath(&path)
      path.append(AppRoute.result)
    }
  }
}

// MARK: - Result helpers (parity with Android ResultScreen)

private func formatPsyTypeNameIOS(_ name: String) -> String {
  guard let f = name.first else { return name }
  return String(f).uppercased() + name.dropFirst().lowercased()
}

private func stripHtmlTagsIOS(_ html: String) -> String {
  html.replacingOccurrences(of: "<[^>]+>", with: "", options: .regularExpression)
    .replacingOccurrences(of: "&nbsp;", with: " ")
    .replacingOccurrences(of: " &lt; ", with: "<")
    .trimmingCharacters(in: .whitespacesAndNewlines)
}

/// Android formatEmostateDescriptionPlain: paragraphs before "2. …", "3. …".
private func formatEmostateDescriptionPlainIOS(_ raw: String) -> String {
  let plain = stripHtmlTagsIOS(raw)
    .replacingOccurrences(of: "\r\n", with: "\n")
    .replacingOccurrences(of: "\r", with: "\n")
    .trimmingCharacters(in: .whitespacesAndNewlines)
  guard !plain.isEmpty else { return plain }
  let spaced = plain.replacingOccurrences(
    of: #"(?<!^)\s+(?=\d+\.\s+)"#,
    with: "\n\n",
    options: .regularExpression
  )
  return spaced.replacingOccurrences(
    of: #"\n{3,}"#,
    with: "\n\n",
    options: .regularExpression
  ).trimmingCharacters(in: .whitespacesAndNewlines)
}

private func formatEmostateDescriptionTextIOS(_ raw: String) -> Text {
  let plain = formatEmostateDescriptionPlainIOS(raw)
  var result = Text("")
  let paragraphs = plain.components(separatedBy: "\n\n")
  for (index, paragraph) in paragraphs.enumerated() {
    if index > 0 { result = result + Text("\n\n") }
    if let match = paragraph.range(of: #"^\d+\.\s+\S+"#, options: .regularExpression) {
      let head = String(paragraph[match])
      let rest = String(paragraph[match.upperBound...])
      result = result + Text(head).fontWeight(.bold) + Text(rest)
    } else {
      result = result + Text(paragraph)
    }
  }
  return result
}

@MainActor
private func extractDescriptionFromSession(_ session: AnalysisSession) -> String {
  let fromModel = session.lastAnalysisResponse?.result?.description?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
  if !fromModel.isEmpty { return fromModel }
  if let raw = session.lastRawApiResponse, let d = extractDescFromJsonString(raw), !d.isEmpty { return d }
  if let j = session.lastResultJson, j.hasPrefix("{"), let d = extractDescFromJsonString(j), !d.isEmpty { return d }
  return ""
}

private func extractDescFromJsonString(_ jsonStr: String) -> String? {
  guard let data = jsonStr.data(using: .utf8),
    let obj = try? JSONSerialization.jsonObject(with: data) as? [String: Any]
  else { return nil }
  if let result = obj["result"] as? [String: Any], let desc = result["description"] as? String { return desc }
  if let desc = obj["description"] as? String { return desc }
  return nil
}

// MARK: - Scale labels (в FlowScreens, чтобы таргет Xcode не зависел от отдельных .swift)
enum MoodStatisticsData {
  static var scaleKeys: [String] { MoodTimeSeriesBuild.scaleKeys }

  static func canonicalEmoKey(_ raw: String) -> String {
    let s = raw.lowercased().trimmingCharacters(in: .whitespacesAndNewlines)
    let aliases: [String: String] = [
      "expressivity": "energy_level",
      "жизнерадостность": "energy_level",
      "cheerfulness": "energy_level",
      "vitality": "energy_level",
      "energy level": "energy_level",
      "ability to attract": "ability_to_attract",
      "openness to new": "openness_to_new",
      "emotional confidence": "emotional_confidence",
      "stress tolerance": "stress_tolerance",
      "person manifestation": "person_manifestation",
      "person harmonicity": "person_harmonicity",
      "ability to set goals": "ability_to_set_goals",
      "ability to assert": "ability_to_assert",
      "self control": "self_control",
      "emo engage": "emo_engage"
    ]
    if let c = aliases[s] { return c }
    return s.replacingOccurrences(of: " ", with: "_")
  }

  static func emoScaleDisplayName(apiName: String, language: AppLanguage) -> String {
    let key = canonicalEmoKey(apiName)
    let sixPack = label(forKey: key, language: language)
    if sixPack != key { return sixPack }
    if let e = extendedLabel(key: key, language: language) { return e }
    // API may already return a Russian label — try reverse lookup via RU maps.
    if let canonical = reverseRuKey(apiName) {
      let again = label(forKey: canonical, language: language)
      if again != canonical { return again }
      if let e = extendedLabel(key: canonical, language: language) { return e }
    }
    return apiName.replacingOccurrences(of: "_", with: " ")
  }

  private static func reverseRuKey(_ name: String) -> String? {
    let allRu = [
      "emo_engage": "Вдохновенность",
      "self_control": "Самоконтроль",
      "stress_tolerance": "Стрессоустойчивость",
      "authority": "Властность",
      "person_harmonicity": "Уравновешенность",
      "energy_level": "Жизнерадостность"
    ].merging(extraRu) { _, b in b }
    return allRu.first(where: { $0.value.caseInsensitiveCompare(name) == .orderedSame })?.key
  }

  private static func extendedLabel(key: String, language: AppLanguage) -> String? {
    let k = key.lowercased()
    switch language {
    case .ru: return extraRu[k]
    case .uk: return extraUk[k] ?? extraRu[k]
    case .en: return extraEn[k]
    case .zh: return extraZh[k] ?? extraEn[k]
    case .kz: return extraKz[k] ?? extraEn[k]
    case .ka: return extraKa[k] ?? extraEn[k]
    }
  }

  private static let extraRu: [String: String] = [
    "ability_to_attract": "Притягательность",
    "expressivity": "Экспрессивность",
    "person_manifestation": "Демонстративность",
    "kindness": "Дружелюбие",
    "openness_to_new": "Открытость к опыту",
    "ability_to_set_goals": "Реализованность",
    "ability_to_assert": "Независимость",
    "emotional_confidence": "Эмоциональность"
  ]
  private static let extraEn: [String: String] = [
    "ability_to_attract": "Attractiveness",
    "expressivity": "Expressiveness",
    "person_manifestation": "Showmanship",
    "kindness": "Friendliness",
    "openness_to_new": "Openness to experience",
    "ability_to_set_goals": "Goal fulfillment",
    "ability_to_assert": "Independence",
    "emotional_confidence": "Emotionality"
  ]
  private static let extraZh: [String: String] = [
    "ability_to_attract": "吸引力",
    "expressivity": "表现力",
    "person_manifestation": "表现欲",
    "kindness": "友善",
    "openness_to_new": "开放性",
    "ability_to_set_goals": "目标实现感",
    "ability_to_assert": "独立性",
    "emotional_confidence": "情绪性"
  ]
  private static let extraKz: [String: String] = [
    "ability_to_attract": "Тартымдылық",
    "expressivity": "Экспрессивтілік",
    "person_manifestation": "Демонстративтілік",
    "kindness": "Достық",
    "openness_to_new": "Жаңалыққа ашықтық",
    "ability_to_set_goals": "Мақсатқа жету",
    "ability_to_assert": "Тәуелсіздік",
    "emotional_confidence": "Эмоционалдылық"
  ]
  private static let extraUk: [String: String] = [
    "ability_to_attract": "Привабливість",
    "expressivity": "Експресивність",
    "person_manifestation": "Демонстративність",
    "kindness": "Дружелюбність",
    "openness_to_new": "Відкритість до досвіду",
    "ability_to_set_goals": "Реалізованість",
    "ability_to_assert": "Незалежність",
    "emotional_confidence": "Емоційність"
  ]
  private static let extraKa: [String: String] = [
    "ability_to_attract": "მიმზიდველობა",
    "expressivity": "ექსპრესიულობა",
    "person_manifestation": "დემონსტრატიულობა",
    "kindness": "კეთილგანწყობა",
    "openness_to_new": "გახსნილობა გამოცდილებისადმი",
    "ability_to_set_goals": "მიზნების მიღწევა",
    "ability_to_assert": "დამოუკიდებლობა",
    "emotional_confidence": "ემოციურობა"
  ]

  static func label(forKey key: String, language: AppLanguage) -> String {
    let k = key.lowercased()
    let ru: [String: String] = [
      "emo_engage": "Вдохновенность",
      "self_control": "Самоконтроль",
      "stress_tolerance": "Стрессоустойчивость",
      "authority": "Властность",
      "person_harmonicity": "Уравновешенность",
      "energy_level": "Жизнерадостность"
    ]
    let en: [String: String] = [
      "emo_engage": "Inspiration",
      "self_control": "Self-control",
      "stress_tolerance": "Stress resistance",
      "authority": "Dominance",
      "person_harmonicity": "Balance",
      "energy_level": "Cheerfulness"
    ]
    let zh: [String: String] = [
      "emo_engage": "灵感动机",
      "self_control": "自我控制",
      "stress_tolerance": "抗压能力",
      "authority": "主导性",
      "person_harmonicity": "心理平衡",
      "energy_level": "生活热情"
    ]
    let kz: [String: String] = [
      "emo_engage": "Шабыттылық",
      "self_control": "Өзін-өзі басқару",
      "stress_tolerance": "Стреске төзімділік",
      "authority": "Басқарушылық",
      "person_harmonicity": "Тұрақтылық",
      "energy_level": "Өмір қуанышы"
    ]
    let uk: [String: String] = [
      "emo_engage": "Натхненність",
      "self_control": "Самоконтроль",
      "stress_tolerance": "Стресостійкість",
      "authority": "Владність",
      "person_harmonicity": "Врівноваженість",
      "energy_level": "Життєрадісність"
    ]
    let ka: [String: String] = [
      "emo_engage": "შთაგონება",
      "self_control": "თვითკონტროლი",
      "stress_tolerance": "სტრესგამძლეობა",
      "authority": "დომინანტობა",
      "person_harmonicity": "წონასწორობა",
      "energy_level": "სიცოცხლისუნარიანობა"
    ]
    switch language {
    case .ru: return ru[k] ?? key
    case .en: return en[k] ?? key
    case .zh: return zh[k] ?? en[k] ?? key
    case .kz: return kz[k] ?? en[k] ?? key
    case .uk: return uk[k] ?? ru[k] ?? key
    case .ka: return ka[k] ?? en[k] ?? key
    }
  }

  static func chartColors(glass: Bool) -> [Color] {
    if glass {
      return [
        Color(red: 1, green: 0.43, blue: 0.6),
        Color(red: 0.49, green: 0.78, blue: 1),
        Color(red: 0.6, green: 0.9, blue: 0.61),
        Color(red: 1, green: 0.78, blue: 0.44),
        Color(red: 0.88, green: 0.69, blue: 0.96),
        Color(red: 0.43, green: 0.93, blue: 1)
      ]
    }
    return [
      Color(red: 0.85, green: 0.12, blue: 0.42),
      Color(red: 0.11, green: 0.45, blue: 0.85),
      Color(red: 0.16, green: 0.55, blue: 0.2),
      Color(red: 1, green: 0.44, blue: 0.1),
      Color(red: 0.45, green: 0.2, blue: 0.77),
      Color(red: 0.04, green: 0.52, blue: 0.47)
    ]
  }
}

struct ResultView: View {
  @Binding var path: NavigationPath
  @EnvironmentObject private var session: AnalysisSession
  @EnvironmentObject private var locale: LocaleStore
  @EnvironmentObject private var prefs: PreferencesStore

  var s: AppStrings { locale.strings }

  private var lang: AppLanguage { prefs.appLanguage }
  private var themeColors: ThemeColors { prefs.themeType.colors() }

  private var titleColor: Color { themeColors.backgroundTextPrimary }

  private var secondaryColor: Color { themeColors.backgroundTextSecondary }

  var body: some View {
    ZStack {
      BackgroundImageName()
      ScrollView {
        VStack(alignment: .leading, spacing: 16) {
          Text(s.result)
            .font(.title.bold())
            .foregroundColor(titleColor)
            .frame(maxWidth: .infinity, alignment: .center)

          if let r = session.lastAnalysisResponse {
            if session.analysisType == "psytype", let types = r.result?.psyTypes, !types.isEmpty {
              psytypeContent(types: types, descriptionRaw: extractDescriptionFromSession(session))
            } else if session.analysisType == "emostate", let scales = r.result?.emoScales, !scales.isEmpty {
              emostateContent(scales: scales, descriptionRaw: extractDescriptionFromSession(session))
            } else if session.analysisType == "psytype" {
              let desc = stripHtmlTagsIOS(extractDescriptionFromSession(session))
              if !desc.isEmpty {
                Text(s.psytypeResultTitle)
                  .font(.title3.weight(.semibold))
                  .foregroundColor(titleColor)
                  .multilineTextAlignment(.center)
                  .frame(maxWidth: .infinity)
                Text(desc)
                  .font(.body)
                  .foregroundColor(secondaryColor)
                  .padding(14)
                  .frame(maxWidth: .infinity, alignment: .leading)
                  .background(
                    LinearGradient(colors: themeColors.cardGradient, startPoint: .topLeading, endPoint: .bottomTrailing)
                  )
                  .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
              } else if let raw = session.lastResultJson, !raw.isEmpty {
                Text(raw).font(.system(.body, design: .monospaced)).foregroundColor(titleColor)
              } else {
                Text(s.shareNoData).foregroundColor(secondaryColor)
              }
            } else if let raw = session.lastResultJson, !raw.isEmpty {
              Text(raw).font(.system(.body, design: .monospaced)).foregroundColor(titleColor)
            } else {
              Text(s.shareNoData).foregroundColor(secondaryColor)
            }
          } else if let errText = session.lastResultJson, !errText.isEmpty {
            Text(errText)
              .font(.body)
              .foregroundColor(titleColor)
              .frame(maxWidth: .infinity, alignment: .leading)
          } else if let raw = session.lastRawApiResponse, !raw.isEmpty {
            Text(raw)
              .font(.system(.body, design: .monospaced))
              .foregroundColor(titleColor)
          } else {
            Text(s.shareNoData).foregroundColor(secondaryColor)
          }

          HStack(spacing: 12) {
            Button(s.share) { presentSystemShare() }
              .buttonStyle(.bordered)
              .tint(titleColor)
              .frame(maxWidth: .infinity)
            Button(s.goHome) {
              path = NavigationPath()
            }
              .buttonStyle(.borderedProminent)
              .tint(.white.opacity(0.35))
              .frame(maxWidth: .infinity)
          }
          Button(s.statesChart) { path.append(AppRoute.history) }
            .buttonStyle(.borderedProminent)
            .tint(.white.opacity(0.28))
            .frame(maxWidth: .infinity)
            .padding(.top, 4)
        }
        .padding(20)
      }
    }
  }

  private func presentSystemShare() {
    let text = buildSharePlainTextIOS(session: session, s: s, lang: lang)
    guard let text, !text.isEmpty else { return }
    let av = UIActivityViewController(activityItems: [text], applicationActivities: nil)
    guard let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene else { return }
    let root = scene.windows.first(where: \.isKeyWindow)?.rootViewController
      ?? scene.windows.first?.rootViewController
    guard var presenter = root else { return }
    while let presented = presenter.presentedViewController { presenter = presented }
    presenter.present(av, animated: true)
  }

  @ViewBuilder
  private func psytypeContent(types: [PsyType], descriptionRaw: String) -> some View {
    let sorted = types.sorted { $0.value > $1.value }
    let leading = sorted.first
    let active = sorted.dropFirst().first
    let desc = stripHtmlTagsIOS(descriptionRaw)

    Text(s.psytypeResultTitle)
      .font(.title3.weight(.semibold))
      .foregroundColor(titleColor)
      .multilineTextAlignment(.center)
      .frame(maxWidth: .infinity)

    leadingActivePsyCard(
      leading: leading.map { (formatPsyTypeNameIOS($0.name), $0.value) },
      active: active.map { (formatPsyTypeNameIOS($0.name), $0.value) }
    )

    Text("\(s.psytypeAllTypes):")
      .font(.headline)
      .foregroundColor(titleColor)
      .padding(.top, 8)

    ForEach(Array(sorted.enumerated()), id: \.offset) { _, pt in
      psyTypeRow(name: formatPsyTypeNameIOS(pt.name), value: pt.value)
    }

    if !desc.isEmpty {
      Text(s.psytypeReportTitle)
        .font(.headline)
        .foregroundColor(titleColor)
        .padding(.top, 12)
      Text(desc)
        .font(.body)
        .foregroundColor(secondaryColor)
        .padding(14)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(
          LinearGradient(colors: themeColors.cardGradient, startPoint: .topLeading, endPoint: .bottomTrailing)
        )
        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
        .overlay(
          RoundedRectangle(cornerRadius: 14, style: .continuous)
            .stroke(themeColors.borderGlass, lineWidth: 1)
        )
    }
  }

  private func leadingActivePsyCard(
    leading: (String, Double)?,
    active: (String, Double)?
  ) -> some View {
    let l = leading ?? ("—", 0)
    let a = active ?? ("—", 0)
    return VStack(alignment: .leading, spacing: 10) {
      Text("\(s.leadingType): \(l.0) (\(String(format: "%.2f", l.1))%)")
        .font(.headline)
        .foregroundColor(themeColors.textPrimary)
      Text("\(s.activeType): \(a.0) (\(String(format: "%.2f", a.1))%)")
        .font(.subheadline.weight(.semibold))
        .foregroundColor(themeColors.textPrimary)
    }
    .padding(16)
    .frame(maxWidth: .infinity, alignment: .leading)
    .background(
      LinearGradient(colors: themeColors.cardGradient, startPoint: .topLeading, endPoint: .bottomTrailing)
    )
    .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
    .overlay(
      RoundedRectangle(cornerRadius: 14, style: .continuous)
        .stroke(themeColors.borderGlass, lineWidth: 1)
    )
  }

  private func psyTypeRow(name: String, value: Double) -> some View {
    let progress = CGFloat(min(1, max(0, value / 100)))
    return VStack(alignment: .leading, spacing: 8) {
      HStack {
        Text("\(name):")
          .font(.body.weight(.medium))
          .foregroundColor(themeColors.textPrimary)
        Spacer()
        Text("\(String(format: "%.2f", value))%")
          .font(.body.weight(.semibold))
          .foregroundColor(themeColors.textPrimary)
      }
      GeometryReader { g in
        ZStack(alignment: .leading) {
          Capsule()
            .fill(Color.white.opacity(0.22))
            .frame(height: 6)
          Capsule()
            .fill(Color.white.opacity(0.95))
            .frame(width: max(4, g.size.width * progress), height: 6)
        }
      }
      .frame(height: 6)
    }
    .padding(14)
    .background(
      LinearGradient(colors: themeColors.cardGradient, startPoint: .topLeading, endPoint: .bottomTrailing)
    )
    .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
    .overlay(
      RoundedRectangle(cornerRadius: 14, style: .continuous)
        .stroke(themeColors.borderGlass, lineWidth: 1)
    )
  }

  @ViewBuilder
  private func emostateContent(scales: [EmoScale], descriptionRaw: String) -> some View {
    let sorted = scales.sorted { $0.value > $1.value }
    let desc = formatEmostateDescriptionPlainIOS(descriptionRaw)

    Text(s.emostateResultTitle)
      .font(.title3.weight(.semibold))
      .foregroundColor(titleColor)
      .multilineTextAlignment(.center)
      .frame(maxWidth: .infinity)

    Text("\(s.emostateParameters):")
      .font(.headline)
      .foregroundColor(titleColor)

    ForEach(Array(sorted.enumerated()), id: \.offset) { _, sc in
      emoScaleRow(
        label: MoodStatisticsData.emoScaleDisplayName(apiName: sc.name, language: lang),
        value: sc.value
      )
    }

    if !desc.isEmpty {
      Text(s.emostateReportTitle)
        .font(.headline)
        .foregroundColor(titleColor)
        .padding(.top, 12)
      formatEmostateDescriptionTextIOS(descriptionRaw)
        .font(.body)
        .foregroundColor(secondaryColor)
        .multilineTextAlignment(.leading)
        .padding(14)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(
          LinearGradient(colors: themeColors.cardGradient, startPoint: .topLeading, endPoint: .bottomTrailing)
        )
        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
        .overlay(
          RoundedRectangle(cornerRadius: 14, style: .continuous)
            .stroke(themeColors.borderGlass, lineWidth: 1)
        )
    }
  }

  private func emoScaleRow(label: String, value: Int) -> some View {
    let progress = CGFloat(min(1, max(0, Double(value) / 100)))
    return VStack(alignment: .leading, spacing: 8) {
      HStack {
        Text("\(label):")
          .font(.body.weight(.medium))
          .foregroundColor(themeColors.textPrimary)
        Spacer()
        Text("\(value)")
          .font(.body.weight(.semibold))
          .foregroundColor(themeColors.textPrimary)
      }
      GeometryReader { g in
        ZStack(alignment: .leading) {
          Capsule()
            .fill(Color.white.opacity(0.22))
            .frame(height: 6)
          Capsule()
            .fill(Color.white.opacity(0.95))
            .frame(width: max(4, g.size.width * progress), height: 6)
        }
      }
      .frame(height: 6)
    }
    .padding(14)
    .background(
      LinearGradient(colors: themeColors.cardGradient, startPoint: .topLeading, endPoint: .bottomTrailing)
    )
    .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
    .overlay(
      RoundedRectangle(cornerRadius: 14, style: .continuous)
        .stroke(themeColors.borderGlass, lineWidth: 1)
    )
  }
}

@MainActor
private func buildSharePlainTextIOS(
  session: AnalysisSession,
  s: AppStrings,
  lang: AppLanguage
) -> String? {
  guard let r = session.lastAnalysisResponse, r.success, let result = r.result else { return nil }
  let analysisType = session.analysisType
  var lines: [String] = [s.shareSubject, ""]
  if analysisType == "psytype" {
    let types = (result.psyTypes ?? []).sorted { $0.value > $1.value }
    guard !types.isEmpty else { return nil }
    lines.append(s.psytypeResultTitle)
    lines.append("")
    if let lead = types.first {
      lines.append("\(s.leadingType): \(formatPsyTypeNameIOS(lead.name)) (\(String(format: "%.2f", lead.value))%)")
    }
    if types.count > 1 {
      let act = types[1]
      lines.append("\(s.activeType): \(formatPsyTypeNameIOS(act.name)) (\(String(format: "%.2f", act.value))%)")
    }
  } else {
    let scales = (result.emoScales ?? []).sorted { $0.value > $1.value }
    guard !scales.isEmpty else { return nil }
    lines.append(s.emostateResultTitle)
    lines.append("")
    for sc in scales {
      lines.append("\(MoodStatisticsData.emoScaleDisplayName(apiName: sc.name, language: lang)): \(sc.value)")
    }
  }
  let desc = stripHtmlTagsIOS(extractDescriptionFromSession(session))
  if !desc.isEmpty {
    lines.append("")
    lines.append(desc)
  }
  return lines.joined(separator: "\n")
}

@MainActor
private func sharePreviewLinesIOS(
  session: AnalysisSession,
  s: AppStrings,
  lang: AppLanguage
) -> (String, String) {
  guard let r = session.lastAnalysisResponse, r.success, let result = r.result else {
    return (s.shareNoData, "")
  }
  let analysisType = session.analysisType
  if analysisType == "psytype" {
    let types = result.psyTypes ?? []
    if types.isEmpty { return (s.shareNoData, "") }
    guard let lead = types.max(by: { $0.value < $1.value }) else { return (s.shareNoData, "") }
    let line =
      "\(s.leadingType): \(formatPsyTypeNameIOS(lead.name)) (\(String(format: "%.2f", lead.value))%)"
    return (s.psytypeResultTitle, line)
  } else {
    let scales = result.emoScales ?? []
    if scales.isEmpty { return (s.shareNoData, "") }
    let top3 = scales.sorted { $0.value > $1.value }.prefix(3)
    let subtitle = top3.map { sc in
      "\(MoodStatisticsData.emoScaleDisplayName(apiName: sc.name, language: lang)): \(sc.value)"
    }.joined(separator: "\n")
    return (s.emostateResultTitle, subtitle)
  }
}

private struct SharePreviewCardView: View {
  @Environment(\.voxeraTheme) private var theme
  let previewTitle: String
  let previewSubtitle: String

  var body: some View {
    let colors = theme.colors()
    ThemedCard(gradientIndex: 0) {
      VStack(spacing: 0) {
        Group {
          if UIImage(named: "ic_voxera_logo_text") != nil {
            Image("ic_voxera_logo_text")
              .resizable()
              .scaledToFit()
              .frame(height: 44)
              .frame(maxWidth: .infinity)
          } else {
            Text("VOXERA")
              .font(.title2.weight(.bold))
              .foregroundColor(colors.textPrimary)
              .frame(maxWidth: .infinity)
          }
        }
        Spacer().frame(height: 16)
        Text(previewTitle)
          .font(.body)
          .foregroundColor(colors.textSecondary)
          .multilineTextAlignment(.center)
          .frame(maxWidth: .infinity)
        if !previewSubtitle.isEmpty {
          Spacer().frame(height: 8)
          Text(previewSubtitle)
            .font(.title3.weight(.regular))
            .foregroundColor(colors.textPrimary)
            .multilineTextAlignment(.center)
            .frame(maxWidth: .infinity)
        }
      }
      .frame(minHeight: 120)
    }
  }
}

struct ShareView: View {
  @EnvironmentObject private var session: AnalysisSession
  @EnvironmentObject private var locale: LocaleStore
  @EnvironmentObject private var prefs: PreferencesStore

  var s: AppStrings { locale.strings }

  private var headlineColor: Color {
    prefs.themeType == .glass ? .white : prefs.themeType.colors().backgroundTextPrimary
  }

  private var preview: (String, String) {
    sharePreviewLinesIOS(session: session, s: s, lang: prefs.appLanguage)
  }

  var body: some View {
    ZStack {
      BackgroundImageName()
      ScrollView {
        VStack(spacing: 20) {
          Spacer(minLength: 10)
          Text(s.shareResult)
            .font(.system(size: 34, weight: .light))
            .foregroundColor(headlineColor)
            .multilineTextAlignment(.center)
            .frame(maxWidth: .infinity)
          SharePreviewCardView(previewTitle: preview.0, previewSubtitle: preview.1)
          Spacer(minLength: 16)
          if session.lastResultJson != nil {
            ShareLink(item: session.lastResultJson ?? "", subject: Text(s.shareSubject)) {
              Text(s.share)
                .font(.body.weight(.semibold))
                .foregroundColor(
                  prefs.themeType == .glass
                    ? .white
                    : prefs.themeType.colors().backgroundTextPrimary
                )
                .frame(maxWidth: .infinity)
                .padding(.vertical, 16)
                .background(
                  RoundedRectangle(cornerRadius: 14, style: .continuous)
                    .fill(prefs.themeType == .glass ? Color.white.opacity(0.22) : Color.white.opacity(0.92))
                )
                .overlay(
                  RoundedRectangle(cornerRadius: 14, style: .continuous)
                    .stroke(
                      prefs.themeType == .glass ? Color.white.opacity(0.35) : Color.black.opacity(0.1),
                      lineWidth: 1
                    )
                )
            }
          } else {
            Text(s.shareNoData)
              .foregroundColor(headlineColor.opacity(0.88))
          }
          Spacer(minLength: 8)
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 24)
      }
    }
  }
}

// MARK: - Recording visuals (в том же файле, что и RecordingView — не нужен отдельный таргет)

enum RecordingVisualTokens {
  static let primaryGlow = Color.white
  static let recordButtonSize: CGFloat = 200
  static let micImageSize: CGFloat = 140
  static let ringColor = Color.white
}

struct RecordingWaterRings: View {
  var isActive: Bool
  var center: CGPoint
  var buttonRadius: CGFloat
  /// Радиус, до которого доходят кольца (диагональ экрана и запас).
  var maxRingRadius: CGFloat

  var body: some View {
    TimelineView(.animation(minimumInterval: 1 / 60, paused: !isActive)) { context in
      Canvas { c, size in
        let t = context.date.timeIntervalSince1970
        let ringDuration: Double = 3.5
        let ringGap: Double = 0.2
        let minR = buttonRadius
        let maxR = max(maxRingRadius, minR + 8)

        for i in 0..<3 {
          let delay = Double(i) * ringGap
          var phase = (t - delay).truncatingRemainder(dividingBy: ringDuration) / ringDuration
          if phase < 0 { phase += 1 }
          let phaseF = CGFloat(phase)
          let currentRadius = minR + phaseF * (maxR - minR)
          let strokeW = CGFloat(
            min(max(3.0 - 2.0 * Double(phaseF), 0.5), 3.0)
          )
          let finalAlpha: CGFloat
          if phaseF < 0.05 {
            finalAlpha = phaseF / 0.05
          } else if phaseF < 0.3 {
            finalAlpha = 1
          } else {
            finalAlpha = 1 * (1 - (phaseF - 0.3) / 0.7)
          }
          if finalAlpha < 0.01 { continue }

          for idx in 1...3 {
            let glowR = currentRadius + CGFloat(idx) * 3
            let glowW = strokeW + CGFloat(idx) * 1.5
            let a = finalAlpha * (0.15 / CGFloat(max(idx, 1)))
            let o = CGRect(
              x: center.x - glowR,
              y: center.y - glowR,
              width: glowR * 2,
              height: glowR * 2
            )
            let pg = Path(ellipseIn: o)
            c.stroke(
              pg,
              with: .color(RecordingVisualTokens.ringColor.opacity(a)),
              style: StrokeStyle(lineWidth: glowW, lineCap: .round, lineJoin: .round)
            )
          }

          let mainRect = CGRect(
            x: center.x - currentRadius,
            y: center.y - currentRadius,
            width: currentRadius * 2,
            height: currentRadius * 2
          )
          c.stroke(
            Path(ellipseIn: mainRect),
            with: .color(RecordingVisualTokens.primaryGlow.opacity(finalAlpha)),
            style: StrokeStyle(lineWidth: strokeW, lineCap: .round, lineJoin: .round)
          )
        }
      }
    }
  }
}

struct RecordingBreathReader: View {
  var isRecording: Bool
  @State private var t0 = Date()

  var body: some View {
    TimelineView(.animation(minimumInterval: 1 / 60, paused: false)) { context in
      let period = isRecording ? 1.2 : 2.0
      let amplitude: CGFloat = isRecording ? 0.038 : 0.012
      let elapsed = context.date.timeIntervalSince(t0)
      let phase = CGFloat(elapsed / period * 2 * .pi)
      let scale: CGFloat = 1.0 + amplitude * sin(phase)
      Color.clear
        .preference(key: RecordingBreathKey.self, value: scale)
    }
    .onChange(of: isRecording) { _, _ in
      t0 = Date()
    }
  }
}

struct RecordingBreathKey: PreferenceKey {
  static let defaultValue: CGFloat = 1
  static func reduce(value: inout CGFloat, nextValue: () -> CGFloat) {
    value = nextValue()
  }
}

struct LiquidGlassRecordButton: View {
  @Environment(\.voxeraTheme) private var voxeraTheme
  var isRecording: Bool
  var breathScale: CGFloat
  var action: () -> Void

  private var cornerRadius: CGFloat { RecordingVisualTokens.recordButtonSize / 2 }

  /// Параметры ближе к Android `liquid { frost, refraction, tint }` — сильный blur, слабый тинт.
  private var glassBlur: CGFloat {
    switch voxeraTheme {
    case .light:
      return isRecording ? 0.46 : 0.4
    case .glass:
      return isRecording ? 0.42 : 0.36
    }
  }

  private var glassTintAlpha: CGFloat {
    switch voxeraTheme {
    case .light:
      return isRecording ? 0.055 : 0.048
    case .glass:
      return isRecording ? 0.042 : 0.035
    }
  }

  var body: some View {
    Button(action: action) {
      ZStack {
        FlowScreensRecordingButtonNeonRings(isRecording: isRecording)
        if UIImage(named: "ic_mic_2") != nil {
          Image("ic_mic_2")
            .resizable()
            .renderingMode(.template)
            .scaledToFit()
            .frame(width: RecordingVisualTokens.micImageSize, height: RecordingVisualTokens.micImageSize)
            .foregroundColor(micTint)
        } else {
          Image(systemName: "mic.fill")
            .resizable()
            .scaledToFit()
            .frame(width: 88, height: 88)
            .foregroundColor(micTint)
        }
      }
      .frame(width: RecordingVisualTokens.recordButtonSize, height: RecordingVisualTokens.recordButtonSize)
      .overlay {
        Circle()
          .stroke(
            LinearGradient(
              colors: [
                Color.white.opacity(voxeraTheme == .glass ? 0.55 : 0.38),
                Color(red: 0.45, green: 0.78, blue: 0.95).opacity(isRecording ? 0.5 : 0.3)
              ],
              startPoint: .topLeading,
              endPoint: .bottomTrailing
            ),
            lineWidth: isRecording ? 2 : 1.2
          )
      }
    }
    .liquidGlassBackground(
      cornerRadius: cornerRadius,
      updateMode: .continuous(interval: isRecording ? 0.085 : 0.1),
      blurScale: glassBlur,
      tintColor: UIColor.white.withAlphaComponent(glassTintAlpha)
    )
    .compositingGroup()
    .clipShape(Circle())
    .contentShape(Circle())
    .buttonStyle(FlowScreensRecordingPressStyle(breathScale: breathScale))
  }

  private var micTint: Color {
    switch voxeraTheme {
    case .light:
      return Color(red: 0.1, green: 0.15, blue: 0.22)
    case .glass:
      return .white.opacity(isRecording ? 1 : 0.9)
    }
  }
}

private struct FlowScreensRecordingPressStyle: ButtonStyle {
  var breathScale: CGFloat = 1

  func makeBody(configuration: Configuration) -> some View {
    configuration.label
      .scaleEffect((configuration.isPressed ? 0.92 : 1.0) * breathScale)
  }
}

private struct FlowScreensRecordingButtonNeonRings: View {
  var isRecording: Bool
  var body: some View {
    let glow = RecordingVisualTokens.primaryGlow
    Canvas { c, s in
      let center = CGPoint(x: s.width / 2, y: s.height / 2)
      let baseR = min(s.width, s.height) / 2 - 2
      let glowAlpha: CGFloat = isRecording ? 1 : 0.8
      for g in 1...3 {
        let r = baseR + CGFloat(g) * 2
        let w = 4 + CGFloat(g) * 2
        let a = glowAlpha * (0.2 / CGFloat(g))
        c.stroke(
          Path(ellipseIn: CGRect(x: center.x - r, y: center.y - r, width: 2 * r, height: 2 * r)),
          with: .color(glow.opacity(a)),
          style: StrokeStyle(lineWidth: w, lineCap: .round, lineJoin: .round)
        )
      }
      let sw: CGFloat = isRecording ? 4 : 2.5
      c.stroke(
        Path(ellipseIn: CGRect(
          x: center.x - baseR, y: center.y - baseR,
          width: 2 * baseR, height: 2 * baseR
        )),
        with: .color(glow.opacity(glowAlpha)),
        style: StrokeStyle(lineWidth: sw, lineCap: .round, lineJoin: .round)
      )
    }
  }
}
