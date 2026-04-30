import SwiftUI
import UIKit
import UniformTypeIdentifiers

struct ConsentView: View {
  @Binding var path: NavigationPath
  @EnvironmentObject private var prefs: PreferencesStore
  @EnvironmentObject private var locale: LocaleStore
  var s: AppStrings { locale.strings }

  var body: some View {
    ZStack {
      BackgroundImageName()
      ScrollView {
        ThemedCard(gradientIndex: 0) {
          VStack(alignment: .leading, spacing: 12) {
            Text(s.consentDescription)
              .foregroundColor(.white)
            Text(s.consentCardSummary)
              .foregroundColor(.white.opacity(0.92))
              .font(.callout)
            Button(s.consentOpenPrivacyPolicyButton) { path.append(AppRoute.privacyPolicy) }
              .foregroundColor(.white)
            Button(s.back) { path.removeLast() }
              .foregroundColor(.white.opacity(0.85))
            Button(s.start) {
              prefs.setConsentGiven(true)
              if !path.isEmpty {
                path.removeLast()
              }
              path.append(AppRoute.recording)
            }
            .buttonStyle(.borderedProminent)
            .tint(.white.opacity(0.35))
          }
        }
        .padding(20)
      }
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
  @State private var timerSec = 30
  @State private var isRecording = false
  @State private var timerExpired = false
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

  var body: some View {
    ZStack(alignment: .top) {
      BackgroundImageName()
      RecordingBreathReader(isRecording: isRecording)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .allowsHitTesting(false)
      VStack(spacing: 0) {
        Spacer(minLength: 8)
        VStack(spacing: 12) {
          Text(isRecording ? s.recordingInProgress : s.voiceRecording)
            .font(.system(size: 32, weight: .regular, design: .default))
            .foregroundColor(titleColor)
            .multilineTextAlignment(.center)
            .padding(.horizontal)
          if isRecording {
            if timerExpired {
              Text(s.tapToStop)
                .font(.body)
                .foregroundColor(secondaryOnBackground)
                .multilineTextAlignment(.center)
            } else {
              Text("\(timerSec) \(s.secondsShort)")
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
        Spacer()
        ZStack {
          if isRecording {
            GeometryReader { g in
              let c = CGPoint(x: g.size.width / 2, y: g.size.height / 2)
              RecordingWaterRings(
                isActive: isRecording,
                center: c,
                buttonRadius: RecordingVisualTokens.recordButtonSize / 2
              )
            }
            .allowsHitTesting(false)
          }
          LiquidGlassRecordButton(
            isRecording: isRecording,
            breathScale: breathScale
          ) {
            Task { await toggleRecord() }
          }
        }
        .frame(maxWidth: .infinity)
        .frame(minHeight: 320)
        .padding(.vertical, 8)
        Spacer()
        Button(s.uploadAudio) { showImporter = true }
          .foregroundColor(titleColor)
          .padding(.vertical, 8)
          .disabled(isRecording)
        Spacer(minLength: 8)
      }
      .padding()
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
    .onReceive(Timer.publish(every: 1, on: .main, in: .common).autoconnect()) { _ in
      guard isRecording, timerSec > 0 else { return }
      timerSec -= 1
      if timerSec == 0 {
        timerExpired = true
      }
    }
    .onDisappear {
      if recorder.isRecording { _ = recorder.stopRecording() }
    }
  }

  private func toggleRecord() async {
    if isRecording {
      let url = recorder.stopRecording()
      isRecording = false
      timerExpired = false
      if let url, FileManager.default.fileExists(atPath: url.path) {
        let attrs = (try? FileManager.default.attributesOfItem(atPath: url.path)) as? [FileAttributeKey: Any]
        let size = attrs?[.size] as? Int64 ?? 0
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
      timerExpired = false
      timerSec = 30
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

  private func run() async {
    guard let url = session.recordedFileURL else {
      session.lastResultJson = "Ошибка: нет файла"
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

struct ResultView: View {
  @Binding var path: NavigationPath
  @EnvironmentObject private var session: AnalysisSession
  @EnvironmentObject private var locale: LocaleStore
  @EnvironmentObject private var prefs: PreferencesStore

  var s: AppStrings { locale.strings }

  private var lang: AppLanguage { prefs.appLanguage }
  private var themeColors: ThemeColors { prefs.themeType.colors() }

  private var titleColor: Color {
    prefs.themeType == .light ? .white : themeColors.backgroundTextPrimary
  }

  private var secondaryColor: Color {
    prefs.themeType == .light ? Color.white.opacity(0.88) : themeColors.backgroundTextSecondary
  }

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
          }

          HStack(spacing: 12) {
            Button(s.newAnalysis) {
              path = NavigationPath()
            }
            .buttonStyle(.borderedProminent)
            .tint(.white.opacity(0.35))
            Spacer(minLength: 8)
            Button(s.share) { path.append(AppRoute.share) }
              .foregroundColor(titleColor)
            Button(s.history) { path.append(AppRoute.history) }
              .foregroundColor(titleColor)
          }
          .padding(.top, 8)
        }
        .padding(20)
      }
    }
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
    let desc = stripHtmlTagsIOS(descriptionRaw)

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

struct ShareView: View {
  @EnvironmentObject private var session: AnalysisSession
  @EnvironmentObject private var locale: LocaleStore
  @State private var item: [Any] = []
  var s: AppStrings { locale.strings }

  var body: some View {
    ZStack {
      BackgroundImageName()
      VStack(spacing: 16) {
        Text(s.shareResult).font(.title2.bold()).foregroundColor(.white)
        if let t = session.lastResultJson {
          ShareLink(item: t, subject: Text(s.shareSubject)) {
            Text(s.share)
              .padding()
              .frame(maxWidth: .infinity)
              .background(Color.white.opacity(0.2))
              .cornerRadius(12)
          }
        } else {
          Text(s.shareNoData).foregroundColor(.white)
        }
      }
      .padding(20)
    }
  }
}

// Экран записи: LiquidGlassRecordButton, кольца и т.д. — в RecordingVisuals.swift (тот же target, что и FlowScreens).
