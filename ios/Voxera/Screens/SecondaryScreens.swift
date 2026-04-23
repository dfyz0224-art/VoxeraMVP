import Foundation
import SwiftUI

private func themedOutlineButton(_ title: String, fg: Color = .white, action: @escaping () -> Void) -> some View {
  Button(action: action) {
    Text(title)
      .font(.headline)
      .frame(maxWidth: .infinity)
      .padding(.vertical, 14)
      .background(Color.white.opacity(0.12))
      .cornerRadius(12)
  }
  .buttonStyle(.plain)
  .foregroundColor(fg)
}

// MARK: - History

struct HistoryView: View {
  @Binding var path: NavigationPath
  @EnvironmentObject private var prefs: PreferencesStore
  @EnvironmentObject private var session: AnalysisSession
  @EnvironmentObject private var history: HistoryStore
  @EnvironmentObject private var locale: LocaleStore
  var s: AppStrings { locale.strings }

  var body: some View {
    ZStack {
      BackgroundImageName()
      ScrollView {
        VStack(alignment: .leading, spacing: 12) {
          HStack(alignment: .firstTextBaseline) {
            Text(s.historyTitle)
              .font(.title2.bold())
              .foregroundColor(titleColor)
            Spacer()
            Button { path.append(AppRoute.statistics) } label: {
              Text(s.statisticsButton)
                .font(.subheadline.weight(.semibold))
            }
            .buttonStyle(.plain)
            .foregroundColor(secondaryColor)
          }
          if history.entries.isEmpty {
            Text(s.historyEmpty).foregroundColor(secondaryColor)
          } else {
            ForEach(Array(history.entries.enumerated()), id: \.element.id) { index, entry in
              ThemedCard(gradientIndex: index % 4, onTap: {
                applyHistoryEntry(entry)
                path.append(AppRoute.result)
              }) {
                VStack(alignment: .leading, spacing: 6) {
                  Text(formatHistoryDate(entry.timestamp))
                    .font(.subheadline)
                    .foregroundColor(.white.opacity(0.75))
                  Text(historyTypeLabel(entry.analysisType))
                    .font(.headline)
                    .foregroundColor(.white)
                }
              }
            }
          }
        }
        .padding(20)
      }
    }
  }

  private var titleColor: Color {
    prefs.themeType == .light ? prefs.themeType.colors().backgroundTextPrimary : .white
  }

  private var secondaryColor: Color {
    prefs.themeType == .light ? prefs.themeType.colors().backgroundTextSecondary : .white.opacity(0.85)
  }

  private func historyTypeLabel(_ t: String) -> String {
    t == "psytype" ? s.historyTypePsytype : s.historyTypeEmostate
  }

  private func formatHistoryDate(_ ms: TimeInterval) -> String {
    let d = Date(timeIntervalSince1970: ms / 1000)
    let f = DateFormatter()
    f.dateStyle = .short
    f.timeStyle = .short
    return f.string(from: d)
  }

  private func applyHistoryEntry(_ entry: HistoryEntry) {
    session.analysisType = entry.analysisType
    session.lastRawApiResponse = entry.rawApiResponse
    session.lastResultJson = entry.responseJson
    if let data = entry.responseJson.data(using: .utf8),
      let decoded = try? JSONDecoder().decode(AnalysisResponse.self, from: data)
    {
      session.lastAnalysisResponse = decoded
    } else {
      session.lastAnalysisResponse = nil
    }
  }
}

// MARK: - Settings

struct SettingsView: View {
  @Binding var path: NavigationPath
  @EnvironmentObject private var prefs: PreferencesStore
  @EnvironmentObject private var locale: LocaleStore
  @State private var showLanguageSheet = false
  var s: AppStrings { locale.strings }
  private var c: ThemeColors { prefs.themeType.colors() }

  var body: some View {
    ZStack {
      BackgroundImageName()
      ScrollView {
        VStack(spacing: 16) {
          Spacer().frame(height: 8)
          profileCard
          themeCard
          languageCard
          themedOutlineButton(s.about, fg: outlineFg) { path.append(AppRoute.about) }
          themedOutlineButton(s.privacyPolicyShortLink, fg: outlineFg) { path.append(AppRoute.privacyPolicy) }
          themedOutlineButton(s.help, fg: outlineFg) { path.append(AppRoute.help) }
          themedOutlineButton(s.forBusiness, fg: outlineFg) { path.append(AppRoute.forBusiness) }
        }
        .padding(20)
      }
    }
    .sheet(isPresented: $showLanguageSheet) {
      NavigationStack {
        List {
          Button { setLang(.ru); showLanguageSheet = false } label: { Text(s.languageRu) }
          Button { setLang(.en); showLanguageSheet = false } label: { Text(s.languageEn) }
          Button { setLang(.zh); showLanguageSheet = false } label: { Text(s.languageZh) }
          Button { setLang(.kz); showLanguageSheet = false } label: { Text(s.languageKz) }
        }
        .navigationTitle(s.selectLanguage)
        .toolbar {
          ToolbarItem(placement: .cancellationAction) {
            Button(s.back) { showLanguageSheet = false }
          }
        }
      }
      .presentationDetents([.medium])
    }
  }

  private var profileCard: some View {
    ThemedCard(gradientIndex: 0, onTap: { path.append(AppRoute.profile) }) {
      HStack(spacing: 16) {
        Image(systemName: "person.crop.circle.fill")
          .font(.system(size: 44))
          .foregroundColor(c.textPrimary)
        VStack(alignment: .leading, spacing: 4) {
          Text(s.profile).font(.headline).foregroundColor(.white)
          Text(displayName).font(.subheadline).foregroundColor(.white.opacity(0.8))
        }
        Spacer()
      }
    }
  }

  private var displayName: String {
    s.profileGuestName
  }

  private var themeCard: some View {
    ThemedCard(gradientIndex: 2) {
      VStack(alignment: .leading, spacing: 12) {
        Text(s.themeTitle).font(.headline).foregroundColor(.white)
        themeRow(s.themeGlass, selected: prefs.themeType == .glass) { prefs.setTheme(.glass) }
        themeRow(s.themeLight, selected: prefs.themeType == .light) { prefs.setTheme(.light) }
      }
    }
  }

  private func themeRow(_ title: String, selected: Bool, action: @escaping () -> Void) -> some View {
    Button(action: action) {
      HStack {
        Image(systemName: selected ? "checkmark.circle.fill" : "circle")
          .foregroundColor(.white)
        Text(title).foregroundColor(.white)
        Spacer()
      }
    }
    .buttonStyle(.plain)
  }

  private var languageCard: some View {
    ThemedCard(gradientIndex: 3) {
      VStack(alignment: .leading, spacing: 12) {
        Text(s.language).font(.headline).foregroundColor(.white)
        Button {
          showLanguageSheet = true
        } label: {
          Text(languageLabel(prefs.appLanguage))
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 12)
            .background(Color.white.opacity(0.15))
            .cornerRadius(12)
        }
      }
    }
  }

  private func languageLabel(_ l: AppLanguage) -> String {
    switch l {
    case .ru: return s.languageRu
    case .en: return s.languageEn
    case .zh: return s.languageZh
    case .kz: return s.languageKz
    }
  }

  private func setLang(_ l: AppLanguage) {
    prefs.setLanguage(l)
    locale.update(language: l)
  }

  private var outlineFg: Color {
    prefs.themeType == .light ? c.backgroundTextPrimary : .white
  }
}

// MARK: - About

struct AboutView: View {
  @Binding var path: NavigationPath
  @EnvironmentObject private var prefs: PreferencesStore
  @EnvironmentObject private var locale: LocaleStore
  var s: AppStrings { locale.strings }

  private var titleOnBackground: Color {
    prefs.themeType == .light ? Color(red: 0.05, green: 0.11, blue: 0.23) : .white
  }

  var body: some View {
    ZStack {
      BackgroundImageName()
      ScrollView {
        VStack(spacing: 16) {
          Image(systemName: "xmark.circle.fill")
            .font(.system(size: 72))
            .padding(.bottom, 4)
            .foregroundColor(prefs.themeType == .light ? prefs.themeType.colors().backgroundTextPrimary : .white)
          themedOutlineButton(s.aboutFullDescriptionButton, fg: titleOnBackground) {
            path.append(AppRoute.aboutFull)
          }
          Text(s.aboutBriefSectionTitle)
            .font(.title3.bold())
            .foregroundColor(titleOnBackground)
            .multilineTextAlignment(.center)
            .frame(maxWidth: .infinity)
          shortSectionsCard
          Text(s.aboutResearchLinksTitle)
            .font(.title3.bold())
            .foregroundColor(titleOnBackground)
            .multilineTextAlignment(.center)
            .padding(.top, 8)
          researchLinksCard
        }
        .padding(22)
      }
    }
  }

  private var shortSectionsCard: some View {
    ThemedCard(gradientIndex: 0) {
      let sections = AboutContent.shortSections(for: prefs.appLanguage)
      VStack(alignment: .leading, spacing: 16) {
        ForEach(Array(sections.enumerated()), id: \.offset) { _, sec in
          VStack(alignment: .leading, spacing: 8) {
            Text(sec.title)
              .font(.headline)
              .foregroundColor(.white)
            ForEach(Array(sec.blocks.enumerated()), id: \.offset) { _, block in
              switch block {
              case .paragraph(let t):
                Text(t).foregroundColor(.white.opacity(0.9)).font(.body)
              case .bullets(let lines):
                ForEach(Array(lines.enumerated()), id: \.offset) { _, line in
                  Text(line).foregroundColor(.white.opacity(0.9)).font(.body)
                    .padding(.vertical, 2)
                }
              }
            }
          }
        }
      }
    }
  }

  private var researchLinksCard: some View {
    ThemedCard(gradientIndex: 0) {
      VStack(alignment: .leading, spacing: 18) {
        ForEach(AboutResearchLinks.all) { item in
          Link(destination: URL(string: item.url)!) {
            VStack(alignment: .leading, spacing: 6) {
              Text("• \(item.title)")
                .foregroundColor(.white.opacity(0.88))
                .font(.body)
              Text(item.url)
                .font(.body)
                .foregroundColor(prefs.themeType.colors().cardGradient.last ?? .cyan)
                .underline()
            }
          }
        }
      }
    }
  }
}

// MARK: - About full (slides)

struct AboutFullDescriptionView: View {
  @Binding var path: NavigationPath
  @EnvironmentObject private var prefs: PreferencesStore
  @EnvironmentObject private var locale: LocaleStore
  @State private var page = 0
  var s: AppStrings { locale.strings }

  private var slides: [AboutSlideData] {
    AboutContent.presentationSlides(for: prefs.appLanguage)
  }

  private var safePage: Int {
    let last = max(0, slides.count - 1)
    return min(max(0, page), last)
  }

  private var titleOnBackground: Color {
    prefs.themeType == .light ? Color(red: 0.05, green: 0.11, blue: 0.23) : .white
  }

  var body: some View {
    ZStack {
      BackgroundImageName()
      VStack(spacing: 12) {
        Button {
          path.removeLast()
        } label: {
          Image(systemName: "xmark.circle.fill")
            .font(.system(size: 56))
            .foregroundColor(prefs.themeType == .light ? prefs.themeType.colors().backgroundTextPrimary : .white)
        }
        Text(s.aboutFullDescriptionButton)
          .font(.title2.bold())
          .foregroundColor(titleOnBackground)
          .multilineTextAlignment(.center)
        Spacer(minLength: 8)
        if slides.indices.contains(safePage) {
          ThemedCard(gradientIndex: 0) {
            VStack(alignment: .leading, spacing: 12) {
              Text(slides[safePage].title)
                .font(.headline)
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
              Text(slides[safePage].body)
                .foregroundColor(.white.opacity(0.9))
            }
          }
        }
        HStack(spacing: 6) {
          ForEach(0..<slides.count, id: \.self) { i in
            Circle()
              .fill(i == safePage ? Color.white : Color.white.opacity(0.35))
              .frame(width: 8, height: 8)
          }
        }
        Button {
          if safePage < slides.count - 1 {
            page = safePage + 1
          } else {
            path.removeLast()
          }
        } label: {
          Text(safePage < slides.count - 1 ? s.aboutPresentationNext : s.aboutPresentationDone)
            .font(.headline)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 14)
            .background(Color.white.opacity(0.25))
            .cornerRadius(14)
        }
        .foregroundColor(.white)
        .padding(.top, 8)
      }
      .padding(22)
    }
  }
}

// MARK: - Help

struct HelpView: View {
  @EnvironmentObject private var prefs: PreferencesStore
  @EnvironmentObject private var locale: LocaleStore
  var s: AppStrings { locale.strings }
  private var c: ThemeColors { prefs.themeType.colors() }

  var body: some View {
    ZStack {
      BackgroundImageName()
      ScrollView {
        VStack(spacing: 16) {
          Image(systemName: "xmark.circle.fill")
            .font(.system(size: 56))
            .foregroundColor(prefs.themeType == .light ? c.backgroundTextPrimary : .white)
          Text(s.helpTitle)
            .font(.title2.bold())
            .foregroundColor(c.backgroundTextPrimary)
          Text(s.helpSubtitle)
            .font(.body)
            .foregroundColor(c.backgroundTextSecondary)
            .multilineTextAlignment(.center)
          ThemedCard(gradientIndex: 0) {
            VStack(alignment: .leading, spacing: 20) {
              helpBlock(s.helpQuickStartTitle, s.helpQuickStartBody)
              helpBlock(s.helpRecordingTitle, s.helpRecordingBody)
              helpBlock(s.helpModesTitle, s.helpModesBody)
              helpBlock(s.helpResultsTitle, s.helpResultsBody)
              helpBlock(s.helpPrivacyTitle, s.helpPrivacyBody)
              helpBlock(s.helpTroubleshootTitle, s.helpTroubleshootBody)
            }
          }
        }
        .padding(22)
      }
    }
  }

  private func helpBlock(_ title: String, _ body: String) -> some View {
    VStack(alignment: .leading, spacing: 8) {
      Text(title).font(.headline).foregroundColor(.white)
      Text(body).foregroundColor(.white.opacity(0.9)).font(.body)
    }
  }
}

// MARK: - For business

struct ForBusinessView: View {
  @Binding var path: NavigationPath
  @EnvironmentObject private var locale: LocaleStore
  var s: AppStrings { locale.strings }

  var body: some View {
    ZStack {
      BackgroundImageName()
      ScrollView {
        VStack(alignment: .leading, spacing: 16) {
          Button {
            path.append(AppRoute.questionnaire)
          } label: {
            Text(s.fillQuestionnaire)
              .font(.headline)
              .foregroundColor(.white)
              .frame(maxWidth: .infinity)
              .padding(.vertical, 14)
              .background(Color.white.opacity(0.28))
              .cornerRadius(14)
          }
          ThemedCard(gradientIndex: 1) {
            VStack(alignment: .leading, spacing: 12) {
              Text(s.forBusinessIntro).foregroundColor(.white)
              bullet(s.forBusinessBullet1)
              bullet(s.forBusinessBullet2)
              bullet(s.forBusinessBullet3)
              bullet(s.forBusinessBullet4)
              bullet(s.forBusinessBullet5)
              bullet(s.forBusinessBullet6)
              Text(s.forBusinessOutro).foregroundColor(.white).padding(.top, 6)
            }
          }
        }
        .padding(20)
      }
    }
  }

  private func bullet(_ t: String) -> some View {
    Text("• \(t)").foregroundColor(.white)
  }
}

// MARK: - Questionnaire

private enum QuestionnairePurpose: String, CaseIterable {
  case financial, hrSports, safety, other
}

struct QuestionnaireView: View {
  @Binding var path: NavigationPath
  @EnvironmentObject private var locale: LocaleStore
  @Environment(\.openURL) private var openURL
  @State private var purpose: QuestionnairePurpose?
  @State private var orgName = ""
  @State private var fieldOfActivity = ""
  @State private var fieldAndGoal = ""
  @State private var contactsFio = ""
  @State private var contactsEmail = ""
  @State private var contactsPhone = ""
  @State private var approxClientsPerDay = ""
  @State private var approxPeopleAndFrequency = ""
  @State private var approxEmployeesPerDay = ""
  @State private var approxEmployeesPerDayMonth = ""
  @State private var specialConditions = ""
  @State private var showIncomplete = false
  var s: AppStrings { locale.strings }

  var body: some View {
    ZStack {
      BackgroundImageName()
      ScrollView {
        VStack(alignment: .leading, spacing: 16) {
          ThemedCard(gradientIndex: 4) {
            VStack(alignment: .leading, spacing: 12) {
              Text(s.purposeOfUse).font(.headline).foregroundColor(.white)
              purposeRow(.financial, s.purposeFinancial)
              purposeRow(.hrSports, s.purposeHrSports)
              purposeRow(.safety, s.purposeSafety)
              purposeRow(.other, s.purposeOther)
              if purpose != nil {
                qField(s.orgName, text: $orgName)
                switch purpose {
                case .financial, .hrSports:
                  qField(s.fieldOfActivity, text: $fieldOfActivity)
                case .safety, .other:
                  qField(s.fieldAndGoal, text: $fieldAndGoal)
                case .none:
                  EmptyView()
                }
                Text(s.contacts).font(.headline).foregroundColor(.white)
                qField(s.contactsFio, text: $contactsFio)
                qField(s.contactsEmail, text: $contactsEmail)
                qField(s.contactsPhone, text: $contactsPhone)
                extraFields
              }
            }
          }
          Button(action: submit) {
            Text(s.submitQuestionnaire)
              .font(.headline)
              .foregroundColor(.white)
              .frame(maxWidth: .infinity)
              .padding(.vertical, 14)
              .background(Color.white.opacity(0.28))
              .cornerRadius(14)
          }
        }
        .padding(20)
      }
    }
    .alert(s.questionnaireIncomplete, isPresented: $showIncomplete) {
      Button("OK", role: .cancel) {}
    }
  }

  @ViewBuilder
  private var extraFields: some View {
    switch purpose {
    case .financial:
      qField(s.approxClientsPerDay, text: $approxClientsPerDay)
      qField(s.specialConditions, text: $specialConditions, multi: true)
    case .hrSports:
      qField(s.approxPeopleAndFrequency, text: $approxPeopleAndFrequency)
      qField(s.specialConditions, text: $specialConditions, multi: true)
    case .safety:
      qField(s.approxEmployeesPerDay, text: $approxEmployeesPerDay)
    case .other:
      qField(s.approxEmployeesPerDayMonth, text: $approxEmployeesPerDayMonth)
    case .none:
      EmptyView()
    }
  }

  private func purposeRow(_ p: QuestionnairePurpose, _ label: String) -> some View {
    Button {
      purpose = p
    } label: {
      HStack {
        Image(systemName: purpose == p ? "largecircle.fill.circle" : "circle")
        Text(label).foregroundColor(.white)
        Spacer()
      }
    }
    .buttonStyle(.plain)
  }

  private func qField(_ label: String, text: Binding<String>, multi: Bool = false) -> some View {
    VStack(alignment: .leading, spacing: 6) {
      Text(label).font(.caption).foregroundColor(.white.opacity(0.8))
      if multi {
        TextField("", text: text, axis: .vertical)
          .lineLimit(3...8)
          .padding(10)
          .background(Color.white.opacity(0.1))
          .cornerRadius(10)
          .foregroundColor(.white)
      } else {
        TextField("", text: text)
          .padding(10)
          .background(Color.white.opacity(0.1))
          .cornerRadius(10)
          .foregroundColor(.white)
      }
    }
  }

  private func submit() {
    guard let purpose else {
      showIncomplete = true
      return
    }
    if orgName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
      || contactsFio.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
      || contactsEmail.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
    {
      showIncomplete = true
      return
    }
    let purposeLabel: String = {
      switch purpose {
      case .financial: return s.purposeFinancial
      case .hrSports: return s.purposeHrSports
      case .safety: return s.purposeSafety
      case .other: return s.purposeOther
      }
    }()
    let body = buildEmailBody(purposeLabel: purposeLabel)
    let subject = "Voxera B2B — \(orgName)"
    let encodedSubject = subject.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
    let encodedBody = body.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
    if let url = URL(string: "mailto:support@voxera.kz?subject=\(encodedSubject)&body=\(encodedBody)") {
      openURL(url)
    }
    if path.count >= 2 {
      path.removeLast()
      path.removeLast()
    } else if path.count == 1 {
      path.removeLast()
    }
  }

  private func buildEmailBody(purposeLabel: String) -> String {
    [
      s.purposeOfUse + ": \(purposeLabel)",
      s.orgName + ": \(orgName)",
      s.fieldOfActivity + ": \(fieldOfActivity)",
      s.fieldAndGoal + ": \(fieldAndGoal)",
      s.contactsFio + ": \(contactsFio)",
      s.contactsEmail + ": \(contactsEmail)",
      s.contactsPhone + ": \(contactsPhone)",
      s.approxClientsPerDay + ": \(approxClientsPerDay)",
      s.approxPeopleAndFrequency + ": \(approxPeopleAndFrequency)",
      s.approxEmployeesPerDay + ": \(approxEmployeesPerDay)",
      s.approxEmployeesPerDayMonth + ": \(approxEmployeesPerDayMonth)",
      s.specialConditions + ": \(specialConditions)"
    ].joined(separator: "\n")
  }
}

// MARK: - Profile

struct ProfileView: View {
  @Binding var path: NavigationPath
  @EnvironmentObject private var prefs: PreferencesStore
  @EnvironmentObject private var locale: LocaleStore
  var s: AppStrings { locale.strings }

  var body: some View {
    ZStack {
      BackgroundImageName()
      ScrollView {
        VStack(alignment: .leading, spacing: 16) {
          ThemedCard(gradientIndex: 0) {
            VStack(alignment: .center, spacing: 16) {
              Text(s.profileGuestTitle)
                .font(.title3.bold())
                .multilineTextAlignment(.center)
                .foregroundColor(.white)
              Image(systemName: "person.crop.circle.fill")
                .font(.system(size: 56))
                .foregroundColor(.white)
              Text(s.profileGuestName)
                .foregroundColor(.white.opacity(0.9))
            }
            .frame(maxWidth: .infinity)
          }
          Button {
            path.append(AppRoute.forBusiness)
          } label: {
            Text(s.forBusiness)
              .foregroundColor(.white)
              .frame(maxWidth: .infinity)
              .padding(.vertical, 14)
              .background(Color.white.opacity(0.15))
              .cornerRadius(12)
          }
        }
        .padding(20)
      }
    }
  }
}

// MARK: - Statistics (same file: ensures symbol is always in target after git pull w/o xcodegen)

enum MoodStatsPeriod: String, CaseIterable {
  case week
  case month
}

enum MoodStatisticsData {
  static let scaleKeys: [String] = [
    "emo_engage",
    "self_control",
    "stress_tolerance",
    "authority",
    "person_harmonicity",
    "expressivity"
  ]

  static func label(forKey key: String, language: AppLanguage) -> String {
    let k = key.lowercased()
    let ru: [String: String] = [
      "emo_engage": "Вдохновенность",
      "self_control": "Самоконтроль",
      "stress_tolerance": "Стрессоустойчивость",
      "authority": "Властность",
      "person_harmonicity": "Уравновешенность",
      "expressivity": "Экспрессивность"
    ]
    let en: [String: String] = [
      "emo_engage": "Inspiration",
      "self_control": "Self-control",
      "stress_tolerance": "Stress resistance",
      "authority": "Dominance",
      "person_harmonicity": "Balance",
      "expressivity": "Expressiveness"
    ]
    let zh: [String: String] = [
      "emo_engage": "灵感动机",
      "self_control": "自我控制",
      "stress_tolerance": "抗压能力",
      "authority": "主导性",
      "person_harmonicity": "心理平衡",
      "expressivity": "表达力"
    ]
    let kz: [String: String] = [
      "emo_engage": "Шабыттылық",
      "self_control": "Өзін-өзі басқару",
      "stress_tolerance": "Стреске төзімділік",
      "authority": "Басқарушылық",
      "person_harmonicity": "Тұрақтылық",
      "expressivity": "Еркіндік"
    ]
    switch language {
    case .ru: return ru[k] ?? key
    case .en: return en[k] ?? key
    case .zh: return zh[k] ?? en[k] ?? key
    case .kz: return kz[k] ?? en[k] ?? key
    }
  }

  static func chartColors(glass: Bool) -> [Color] {
    if glass {
      return [
        Color(red: 1, green: 0.36, blue: 0.54),
        Color(red: 0.39, green: 0.65, blue: 0.96),
        Color(red: 0.4, green: 0.73, blue: 0.42),
        Color(red: 1, green: 0.72, blue: 0.3),
        Color(red: 0.8, green: 0.55, blue: 0.87),
        Color(red: 0.2, green: 0.86, blue: 0.89)
      ]
    }
    return [
      Color(red: 0.76, green: 0.09, blue: 0.36),
      Color(red: 0.05, green: 0.28, blue: 0.63),
      Color(red: 0.11, green: 0.37, blue: 0.13),
      Color(red: 0.9, green: 0.33, blue: 0),
      Color(red: 0.29, green: 0, blue: 0.51),
      Color(red: 0, green: 0.41, blue: 0.39)
    ]
  }

  static func dayPoints(
    entries: [HistoryEntry],
    period: MoodStatsPeriod
  ) -> (points: [MoodDayModel], hasAny: Bool) {
    let days = period == .week ? 7 : 30
    let cal = Calendar.current
    let today = cal.startOfDay(for: Date())
    guard let start = cal.date(byAdding: .day, value: -(days - 1), to: today) else {
      return ([], false)
    }
    var byDay: [String: [String: Int]] = [:]
    let keyDay: (Date) -> String = { d in
      let c = cal.dateComponents([.year, .month, .day], from: d)
      return "\(c.year!)-\(c.month!)-\(c.day!)"
    }
    let sorted = entries
      .filter { $0.analysisType == "emostate" }
      .sorted { $0.timestamp < $1.timestamp }
    for e in sorted {
      guard let data = e.responseJson.data(using: .utf8),
        let res = try? JSONDecoder().decode(AnalysisResponse.self, from: data),
        let scales = res.result?.emoScales, !scales.isEmpty
      else { continue }
      let date = Date(timeIntervalSince1970: e.timestamp / 1000)
      let d0 = cal.startOfDay(for: date)
      if d0 < start || d0 > today { continue }
      var m: [String: Int] = [:]
      for s in scales {
        m[s.name.lowercased()] = s.value
      }
      var dayMap = byDay[keyDay(d0), default: [:]]
      for k in scaleKeys {
        if let v = m[k] { dayMap[k] = v }
      }
      if !dayMap.isEmpty {
        byDay[keyDay(d0)] = dayMap
      }
    }
    let fmt = DateFormatter()
    fmt.dateStyle = .short
    var out: [MoodDayModel] = []
    var hasAny = false
    for i in 0..<days {
      guard let d = cal.date(byAdding: .day, value: i, to: start) else { continue }
      let dict = byDay[keyDay(d)] ?? [:]
      var vals: [String: Int?] = [:]
      for k in scaleKeys {
        let v = dict[k]
        vals[k] = v
        if v != nil { hasAny = true }
      }
      out.append(MoodDayModel(index: i, label: fmt.string(from: d), values: vals))
    }
    return (out, hasAny)
  }
}

struct MoodDayModel: Identifiable {
  var id: Int { index }
  let index: Int
  let label: String
  let values: [String: Int?]
}

struct StatisticsView: View {
  @Binding var path: NavigationPath
  @EnvironmentObject private var history: HistoryStore
  @EnvironmentObject private var prefs: PreferencesStore
  @EnvironmentObject private var locale: LocaleStore
  @State private var period: MoodStatsPeriod = .week
  @State private var selectedFilterKey: String?

  private var s: AppStrings { locale.strings }
  private var scaleKeys: [String] { MoodStatisticsData.scaleKeys }
  private var colors: [Color] {
    MoodStatisticsData.chartColors(glass: prefs.themeType == .glass)
  }

  var body: some View {
    let built = MoodStatisticsData.dayPoints(entries: history.entries, period: period)
    let dayPoints = built.points
    let hasData = built.hasAny

    ZStack {
      BackgroundImageName()
      ScrollView {
        VStack(alignment: .leading, spacing: 16) {
          HStack {
            Button {
              if !path.isEmpty { path.removeLast() }
            } label: {
              Text("‹  \(s.back)")
            }
            .foregroundColor(heading)
            Spacer()
          }
          Text(s.statisticsTitle)
            .font(.title2.bold())
            .foregroundColor(heading)
            .frame(maxWidth: .infinity, alignment: .leading)
          Picker("", selection: $period) {
            Text(s.statisticsPeriodWeek).tag(MoodStatsPeriod.week)
            Text(s.statisticsPeriodMonth).tag(MoodStatsPeriod.month)
          }
          .pickerStyle(.segmented)
          if !hasData {
            Text(s.statisticsNoData)
              .foregroundColor(prefs.themeType.colors().backgroundTextSecondary)
              .padding(.top, 8)
          } else {
            ThemedCard(gradientIndex: 0) {
              VStack(alignment: .leading, spacing: 12) {
                MoodLineChartView(
                  dayPoints: dayPoints,
                  scaleKeys: scaleKeys,
                  colors: colors,
                  isLight: prefs.themeType == .light,
                  selectedOnlyKey: selectedFilterKey
                )
                .frame(height: 220)
                LazyVGrid(
                  columns: [GridItem(.adaptive(minimum: 100), spacing: 8)],
                  alignment: .leading,
                  spacing: 8
                ) {
                  ForEach(Array(scaleKeys.enumerated()), id: \.offset) { i, key in
                    let lineColor = colors[i]
                    let isSolo = selectedFilterKey == key
                    Button {
                      if selectedFilterKey == key { selectedFilterKey = nil } else { selectedFilterKey = key }
                    } label: {
                      HStack(spacing: 6) {
                        RoundedRectangle(cornerRadius: 2)
                          .fill(lineColor)
                          .frame(width: 4, height: 14)
                        Text(MoodStatisticsData.label(forKey: key, language: prefs.appLanguage))
                          .font(.caption)
                          .lineLimit(1)
                          .foregroundColor(.white)
                      }
                      .padding(.horizontal, 10)
                      .padding(.vertical, 8)
                      .background(isSolo ? lineColor.opacity(0.35) : Color.white.opacity(0.1))
                      .cornerRadius(12)
                    }
                    .buttonStyle(.plain)
                  }
                }
              }
            }
          }
        }
        .padding(20)
      }
    }
  }

  private var heading: Color {
    if prefs.themeType == .light { return prefs.themeType.colors().backgroundTextPrimary }
    return .white
  }
}

private struct MoodLineChartView: View {
  let dayPoints: [MoodDayModel]
  let scaleKeys: [String]
  let colors: [Color]
  let isLight: Bool
  let selectedOnlyKey: String?

  var body: some View {
    Canvas { ctx, size in
      let w = size.width
      let h = size.height
      let pl: CGFloat = 36
      let pr: CGFloat = 12
      let pt: CGFloat = 8
      let pb: CGFloat = 28
      let axis = isLight ? Color(red: 0.1, green: 0.1, blue: 0.1) : Color.white.opacity(0.88)
      let grid = isLight ? Color.black.opacity(0.12) : Color.white.opacity(0.12)
      for t in 0...4 {
        let y = pt + (h - pt - pb) * (CGFloat(t) / 4)
        var g = pathLine(from: CGPoint(x: pl, y: y), to: CGPoint(x: w - pr, y: y))
        ctx.stroke(g, with: .color(grid), lineWidth: 1)
      }
      let keys = selectedOnlyKey != nil ? [selectedOnlyKey!] : scaleKeys
      for key in keys {
        guard let ki = scaleKeys.firstIndex(of: key) else { continue }
        let c = colors[min(ki, colors.count - 1)]
        let segs = splitSegments(dayPoints, key: key, width: w - pl - pr)
        for seg in segs {
          guard seg.count >= 2 else { continue }
          var p = buildSmoothPath(seg: seg, w: w, h: h, pl: pl, pr: pr, pt: pt, pb: pb)
          ctx.stroke(p, with: .color(c), style: StrokeStyle(lineWidth: 2.5, lineCap: .round, lineJoin: .round))
        }
      }
      if !dayPoints.isEmpty {
        let count = dayPoints.count
        let labelIndices: [Int] = {
          if count == 1 { return [0] }
          if count <= 3 { return Array(0..<count) }
          return [0, count / 2, count - 1]
        }()
        for i in Set(labelIndices) {
          let d = dayPoints[i]
          let x: CGFloat
          if count == 1 { x = (pl + w - pr) / 2 } else { x = pl + CGFloat(d.index) / CGFloat(max(1, count - 1)) * (w - pl - pr) }
          ctx.draw(
            Text(d.label)
              .font(.system(size: 9))
              .foregroundStyle(axis),
            at: CGPoint(x: x, y: h - 4),
            anchor: .bottom
          )
        }
      }
    }
  }

  private func pathLine(from: CGPoint, to: CGPoint) -> Path {
    var p = Path()
    p.move(to: from)
    p.addLine(to: to)
    return p
  }

  private func splitSegments(_ points: [MoodDayModel], key: String, width: CGFloat) -> [[(CGFloat, CGFloat)]] {
    let N = max(1, points.count - 1)
    var segs: [[(CGFloat, CGFloat)]] = []
    var cur: [(CGFloat, CGFloat)] = []
    for p in points {
      guard let opt = p.values[key], let yv = opt else {
        if cur.count >= 2 { segs.append(cur) }
        cur = []
        continue
      }
      let xn = N == 0 ? 0.5 : CGFloat(p.index) / CGFloat(N)
      let yf = max(0, min(1, CGFloat(yv) / 100))
      cur.append((xn, yf))
    }
    if !cur.isEmpty {
      if cur.count == 1 { segs.append([cur[0], cur[0]]) } else { segs.append(cur) }
    }
    return segs
  }

  private func buildSmoothPath(
    seg: [(CGFloat, CGFloat)], w: CGFloat, h: CGFloat, pl: CGFloat, pr: CGFloat, pt: CGFloat, pb: CGFloat
  ) -> Path {
    let innerW = w - pl - pr
    let innerH = h - pt - pb
    func tx(_ x: CGFloat) -> CGFloat { pl + x * innerW }
    func ty(_ y: CGFloat) -> CGFloat { pt + (1 - y) * innerH }
    if seg.count < 2 { return Path() }
    if seg.count == 2, seg[0] == seg[1] {
      var p1 = Path()
      let a = CGPoint(x: tx(seg[0].0), y: ty(seg[0].1))
      p1.move(to: a)
      p1.addLine(to: CGPoint(x: a.x + 0.5, y: a.y))
      return p1
    }
    var path = Path()
    let p = seg.map { CGPoint(x: tx($0.0), y: ty($0.1)) }
    path.move(to: p[0])
    for i in 0..<(p.count - 1) {
      let p0 = i == 0 ? p[0] : p[i - 1]
      let p1 = p[i]
      let p2 = p[i + 1]
      let p3 = i + 2 < p.count ? p[i + 2] : p2
      let c1 = CGPoint(
        x: p1.x + (p2.x - p0.x) / 6,
        y: p1.y + (p2.y - p0.y) / 6
      )
      let c2 = CGPoint(
        x: p2.x - (p3.x - p1.x) / 6,
        y: p2.y - (p3.y - p1.y) / 6
      )
      path.addCurve(to: p2, control1: c1, control2: c2)
    }
    return path
  }
}
