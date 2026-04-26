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
          Text(s.historyTitle)
            .font(.title2.bold())
            .foregroundColor(historyScreenTitleColor)
            .shadow(
              color: prefs.themeType == .light ? Color.black.opacity(0.4) : .clear,
              radius: 5,
              x: 0,
              y: 1.5
            )
            .frame(maxWidth: .infinity, alignment: .leading)
          HistoryMoodChartBlock()
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
        .padding(.top, 8)
      }
    }
  }

  private var historyScreenTitleColor: Color { .white }

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

private struct HistoryMoodChartBlock: View {
  @EnvironmentObject private var history: HistoryStore
  @EnvironmentObject private var prefs: PreferencesStore
  @EnvironmentObject private var locale: LocaleStore
  @State private var kind: HistoryMoodKind = .week
  @State private var customFrom: Date = Calendar.current.date(byAdding: .day, value: -6, to: Date())!
  @State private var customTo: Date = Date()
  @State private var activeFilter: Set<String> = []
  var s: AppStrings { locale.strings }

  var body: some View {
    let result = MoodTimeSeriesBuild.build(entries: history.entries, period: resolvedPeriod())
    let scaleKeys = MoodStatisticsData.scaleKeys
    let colors = MoodStatisticsData.chartColors(glass: true)
    let c = prefs.themeType.colors()
    VStack(alignment: .leading, spacing: 12) {
        ScrollView(.horizontal, showsIndicators: false) {
          HStack(spacing: 6) {
            periodButton(title: s.statisticsPeriod24h, selected: kind == .h24) { kind = .h24 }
            periodButton(title: s.statisticsPeriodWeek, selected: kind == .week) { kind = .week }
            periodButton(title: s.statisticsPeriodMonth, selected: kind == .month) { kind = .month }
            periodButton(title: s.statisticsPeriodCustom, selected: kind == .custom) { kind = .custom }
          }
        }
        if kind == .custom {
          HStack(spacing: 8) {
            VStack(alignment: .leading, spacing: 4) {
              Text(s.statisticsDateFrom).font(.caption2).foregroundColor(.white.opacity(0.8))
              DatePicker("", selection: $customFrom, displayedComponents: .date)
                .labelsHidden()
                .tint(.white)
                .colorScheme(.dark)
            }
            .frame(maxWidth: .infinity)
            VStack(alignment: .leading, spacing: 4) {
              Text(s.statisticsDateTo).font(.caption2).foregroundColor(.white.opacity(0.8))
              DatePicker("", selection: $customTo, displayedComponents: .date)
                .labelsHidden()
                .tint(.white)
                .colorScheme(.dark)
            }
            .frame(maxWidth: .infinity)
          }
        }
        if !result.hasAny {
          Text(s.statisticsNoData)
            .font(.subheadline)
            .foregroundColor(.white.opacity(0.88))
        } else {
          MoodLineChartView(
            dayPoints: result.points,
            scaleKeys: scaleKeys,
            colors: colors,
            isLight: false,
            activeFilter: activeFilter,
            windowStartMs: result.windowStartMs,
            windowEndMs: result.windowEndMs
          )
          .frame(height: 220)
          LazyVGrid(
            columns: [GridItem(.adaptive(minimum: 100), spacing: 8)],
            alignment: .leading,
            spacing: 8
          ) {
            ForEach(Array(scaleKeys.enumerated()), id: \.offset) { i, key in
              let lineColor = colors[i]
              let chipOn = !activeFilter.isEmpty && activeFilter.contains(key)
              Button {
                var s = activeFilter
                if s.contains(key) { s.remove(key) } else { s.insert(key) }
                activeFilter = s
              } label: {
                HStack(spacing: 8) {
                  RoundedRectangle(cornerRadius: 3)
                    .fill(lineColor)
                    .frame(width: 6, height: 18)
                    .overlay(
                      RoundedRectangle(cornerRadius: 3)
                        .stroke(Color.white.opacity(0.6), lineWidth: 1)
                    )
                  Text(MoodStatisticsData.label(forKey: key, language: prefs.appLanguage))
                    .font(.subheadline.weight(.medium))
                    .lineLimit(1)
                    .foregroundColor(c.textPrimary)
                }
                .padding(.horizontal, 10)
                .padding(.vertical, 9)
                .background(
                  (chipOn ? lineColor.opacity(0.42) : Color.white.opacity(0.14)),
                  in: RoundedRectangle(cornerRadius: 12, style: .continuous)
                )
                .overlay(
                  RoundedRectangle(cornerRadius: 12, style: .continuous)
                    .stroke(
                      chipOn ? lineColor : Color.white.opacity(0.32),
                      lineWidth: chipOn ? 2 : 1.5
                    )
                )
              }
              .buttonStyle(.plain)
            }
          }
        }
    }
    .frame(maxWidth: .infinity, alignment: .leading)
    .padding(18)
    .background(
      historyMoodCardGradient,
      in: RoundedRectangle(cornerRadius: 16, style: .continuous)
    )
    .overlay(
      RoundedRectangle(cornerRadius: 16, style: .continuous)
        .stroke(c.borderGlass, lineWidth: 1)
    )
  }

  private var historyMoodCardGradient: LinearGradient {
    if prefs.themeType == .light {
      return LinearGradient(
        colors: [
          Color(red: 0.06, green: 0.2, blue: 0.47),
          Color(red: 0.1, green: 0.45, blue: 0.9)
        ],
        startPoint: .topLeading,
        endPoint: .bottomTrailing
      )
    }
    return LinearGradient(
      colors: [Color.white.opacity(0.22), Color.white.opacity(0.1)],
      startPoint: .topLeading,
      endPoint: .bottomTrailing
    )
  }

  private func periodButton(title: String, selected: Bool, action: @escaping () -> Void) -> some View {
    Button(action: action) {
      Text(title)
        .font(.subheadline.weight(.semibold))
        .padding(.horizontal, 10)
        .padding(.vertical, 8)
        .background(
          selected
            ? (prefs.themeType == .light
              ? Color(red: 0.18, green: 0.37, blue: 0.62).opacity(0.28)
              : Color.white.opacity(0.22))
            : (prefs.themeType == .light ? Color.black.opacity(0.08) : Color.white.opacity(0.12))
        )
        .cornerRadius(8)
    }
    .buttonStyle(.plain)
    .foregroundColor(.white)
  }

  private func resolvedPeriod() -> HistoryMoodPeriod {
    let cal = Calendar.current
    switch kind {
    case .h24: return .last24
    case .week: return .week
    case .month: return .month
    case .custom:
      let a = cal.startOfDay(for: customFrom)
      let b = cal.startOfDay(for: customTo)
      return .customFromTo(from: min(a, b), to: max(a, b))
    }
  }
}

private enum HistoryMoodKind: String, CaseIterable {
  case h24, week, month, custom
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
