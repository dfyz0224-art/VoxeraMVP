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
  @EnvironmentObject private var locale: LocaleStore
  @StateObject private var recorder = AudioRecorderService()
  @State private var timerSec = 30
  @State private var isRecording = false
  @State private var timerExpired = false
  @State private var showImporter = false

  var s: AppStrings { locale.strings }

  var body: some View {
    ZStack {
      BackgroundImageName()
      VStack(spacing: 24) {
        Spacer()
        Text(isRecording ? s.recordingInProgress : s.voiceRecording)
          .font(.title2.bold())
          .foregroundColor(.white)
          .multilineTextAlignment(.center)
          .padding(.horizontal)
        if isRecording {
          if timerExpired {
            Text(s.tapToStop).foregroundColor(.white).multilineTextAlignment(.center)
          } else {
            Text("\(timerSec) \(s.secondsShort)")
              .font(.largeTitle.bold())
              .foregroundColor(.white)
          }
        } else {
          Text(s.tapToStart).foregroundColor(.white.opacity(0.95)).multilineTextAlignment(.center)
          Text(s.saySentences).font(.callout).foregroundColor(.white.opacity(0.88)).multilineTextAlignment(.center)
        }
        Button {
          Task { await toggleRecord() }
        } label: {
          ZStack {
            Circle()
              .fill(.ultraThinMaterial)
              .frame(width: 200, height: 200)
            (UIImage(named: "ic_mic_2") != nil
              ? Image("ic_mic_2")
              : Image(systemName: "mic.circle.fill"))
              .resizable()
              .scaledToFit()
              .frame(width: 120, height: 120)
          }
        }
        .padding(.vertical, 24)
        Button(s.uploadAudio) { showImporter = true }
          .foregroundColor(.white)
          .disabled(isRecording)
        Spacer()
      }
      .padding()
    }
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
      if let url = recorder.stopRecording() {
        session.recordedFileURL = url
        session.lastAudioMimeType = nil
        isRecording = false
        timerExpired = false
        path.append(AppRoute.processing)
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
      session.lastResultJson = "Ошибка: \(error)"
      path.append(AppRoute.result)
    }
  }
}

struct ResultView: View {
  @Binding var path: NavigationPath
  @EnvironmentObject private var session: AnalysisSession
  @EnvironmentObject private var locale: LocaleStore

  var s: AppStrings { locale.strings }

  var body: some View {
    ZStack {
      BackgroundImageName()
      ScrollView {
        VStack(alignment: .leading, spacing: 16) {
          Text(s.result).font(.title.bold()).foregroundColor(.white)
          if let r = session.lastAnalysisResponse {
            if session.analysisType == "psytype" {
              Text(s.psytypeResultTitle).foregroundColor(.white.opacity(0.9))
            } else {
              Text(s.emostateResultTitle).foregroundColor(.white.opacity(0.9))
            }
            if let desc = r.result?.description {
              Text(desc).foregroundColor(.white).padding(.vertical, 8)
            }
            if let scales = r.result?.emoScales {
              ForEach(Array(scales.enumerated()), id: \.offset) { _, sc in
                HStack {
                  Text(sc.name)
                  Spacer()
                  Text("\(sc.value)")
                }
                .foregroundColor(.white)
                .padding(10)
                .background(Color.white.opacity(0.08))
                .cornerRadius(12)
              }
            }
          } else if let err = session.lastResultJson {
            Text(err).foregroundColor(.white)
          }
          HStack {
            Button(s.newAnalysis) {
              path = NavigationPath()
            }
            .buttonStyle(.borderedProminent)
            Spacer()
            Button(s.share) { path.append(AppRoute.share) }
            Button(s.history) { path.append(AppRoute.history) }
          }
        }
        .padding(20)
      }
    }
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
