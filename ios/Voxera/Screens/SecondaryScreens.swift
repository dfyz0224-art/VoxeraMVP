import Foundation
import SwiftUI

@MainActor
private func themedOutlineButton(
  _ title: String, fg: Color = .white, lightStroke: Color? = nil, action: @escaping () -> Void
) -> some View {
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
  .overlay(
    RoundedRectangle(cornerRadius: 12, style: .continuous)
      .stroke(lightStroke ?? .clear, lineWidth: lightStroke != nil ? 1.5 : 0)
  )
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
          subscriptionsNavCard
          themeCard
          languageCard
          themedOutlineButton(s.about, fg: outlineFg, lightStroke: prefs.themeType == .light ? outlineFg.opacity(0.45) : nil) { path.append(AppRoute.about) }
          themedOutlineButton(s.privacyPolicyShortLink, fg: outlineFg, lightStroke: prefs.themeType == .light ? outlineFg.opacity(0.45) : nil) { path.append(AppRoute.privacyPolicy) }
          themedOutlineButton(s.help, fg: outlineFg, lightStroke: prefs.themeType == .light ? outlineFg.opacity(0.45) : nil) { path.append(AppRoute.help) }
          themedOutlineButton(s.forBusiness, fg: outlineFg, lightStroke: prefs.themeType == .light ? outlineFg.opacity(0.45) : nil) { path.append(AppRoute.forBusiness) }
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
          Button { setLang(.uk); showLanguageSheet = false } label: { Text(s.languageUk) }
          Button { setLang(.ka); showLanguageSheet = false } label: { Text(s.languageKa) }
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

  private var subscriptionsNavCard: some View {
    ThemedCard(gradientIndex: 1, onTap: { path.append(AppRoute.subscriptions) }) {
      Text(s.manageSubscriptions)
        .font(.headline)
        .foregroundColor(.white)
        .frame(maxWidth: .infinity, alignment: .leading)
        .frame(minHeight: 36)
    }
  }

  private var displayName: String {
    AuthBackend.currentEmail ?? s.profileGuestName
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
    case .uk: return s.languageUk
    case .ka: return s.languageKa
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
          themedOutlineButton(s.aboutFullDescriptionButton, fg: titleOnBackground, lightStroke: prefs.themeType == .light ? titleOnBackground.opacity(0.4) : nil) {
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
    let subject = "\(s.questionnaireEmailSubject) — \(orgName)"
    let encodedSubject = subject.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
    let encodedBody = body.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
    if let url = URL(string: "mailto:voxera2026@gmail.com?subject=\(encodedSubject)&body=\(encodedBody)") {
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
  @State private var authEpoch = 0
  var s: AppStrings { locale.strings }

  private var signedIn: Bool {
    _ = authEpoch
    return AuthBackend.isSignedIn
  }

  var body: some View {
    ZStack {
      BackgroundImageName()
      ScrollView {
        VStack {
          Spacer().frame(minHeight: 80)
          if signedIn {
            ThemedCard(gradientIndex: 0) {
              VStack(alignment: .leading, spacing: 10) {
                Text(s.profile)
                  .font(.headline)
                  .foregroundColor(.white)
                Text(AuthBackend.currentEmail ?? s.userName)
                  .font(.system(size: 17))
                  .foregroundColor(.white.opacity(0.85))
                Button {
                  AuthBackend.signOut()
                  prefs.setAuthCompleted(false)
                  authEpoch += 1
                } label: {
                  Text(s.profileSignOut)
                    .foregroundColor(.red.opacity(0.95))
                    .frame(maxWidth: .infinity)
                }
                .padding(.top, 4)
              }
            }
          } else {
            ThemedCard(gradientIndex: 0) {
              AuthCardContent(
                showSkipButton: false,
                onAuthComplete: { authEpoch += 1 }
              )
            }
          }
          Spacer().frame(minHeight: 80)
        }
        .frame(maxWidth: .infinity)
        .padding(20)
      }
    }
  }
}

// MARK: - Mood chart (ex-MoodChartData.swift, в том же файле что History — один таргет Xcode)
struct MoodDayModel: Identifiable {
  var id: Int { index }
  let index: Int
  let label: String
  let values: [String: Int?]
  var xInWindow: Double? = nil
}

struct MoodChartResult {
  let points: [MoodDayModel]
  let hasAny: Bool
  let windowStartMs: TimeInterval?
  let windowEndMs: TimeInterval?
}

enum MoodTimeSeriesBuild {
  static let scaleKeys: [String] = [
    "emo_engage",
    "self_control",
    "stress_tolerance",
    "authority",
    "person_harmonicity",
    "energy_level"
  ]

  static func build(entries: [HistoryEntry], period: HistoryMoodPeriod) -> MoodChartResult {
    let emo = collectEmoSamples(from: entries)
    switch period {
    case .last24:
      return buildLast24(samples: emo, scaleKeys: scaleKeys)
    case .week:
      return buildDayWindow(samples: emo, nDays: 7, scaleKeys: scaleKeys)
    case .month:
      return buildDayWindow(samples: emo, nDays: 30, scaleKeys: scaleKeys)
    case .customFromTo(let from, let to):
      return buildCustomDay(samples: emo, from: from, to: to, scaleKeys: scaleKeys)
    }
  }

  private struct EmoSample { let tMs: TimeInterval; let v: [String: Int] }

  private static func collectEmoSamples(from entries: [HistoryEntry]) -> [EmoSample] {
    var out: [EmoSample] = []
    for e in entries {
      guard e.analysisType == "emostate",
        let data = e.responseJson.data(using: .utf8),
        let res = try? JSONDecoder().decode(AnalysisResponse.self, from: data),
        let scales = res.result?.emoScales, !scales.isEmpty
      else { continue }
      var m: [String: Int] = [:]
      for s in scales {
        let raw = s.name.lowercased()
        m[raw] = s.value
        let energyAliases = ["жизнерадостность", "cheerfulness", "vitality", "energy level", "expressivity"]
        if energyAliases.contains(raw), m["energy_level"] == nil {
          m["energy_level"] = s.value
        }
      }
      var by: [String: Int] = [:]
      for k in scaleKeys {
        if let v = m[k] ?? m[k.replacingOccurrences(of: "_", with: " ")] {
          by[k] = v
        }
      }
      if by.isEmpty { continue }
      out.append(EmoSample(tMs: e.timestamp, v: by))
    }
    return out.sorted { $0.tMs < $1.tMs }
  }

  private static func average(_ maps: [[String: Int]], scaleKeys: [String]) -> [String: Int?] {
    if maps.isEmpty { return Dictionary(uniqueKeysWithValues: scaleKeys.map { ($0, nil) }) }
    var r: [String: Int?] = [:]
    for k in scaleKeys {
      let vals = maps.compactMap { $0[k] }
      if vals.isEmpty { r[k] = nil }
      else { r[k] = Int((Double(vals.reduce(0, +)) / Double(vals.count)).rounded() + 1e-6) }
    }
    return r
  }

  private static func buildLast24(
    samples: [EmoSample], scaleKeys: [String]
  ) -> MoodChartResult {
    let h24: TimeInterval = 24 * 60 * 60 * 1000
    if samples.isEmpty {
      return MoodChartResult(points: [], hasAny: false, windowStartMs: nil, windowEndMs: nil)
    }
    let s = samples
    let lastA = s.last!.tMs
    let prevBeforeLast: TimeInterval? = s.count >= 2 ? s[s.count - 2].tMs : nil
    let gapOver = prevBeforeLast != nil && (lastA - prevBeforeLast! > h24)

    let tStart: TimeInterval
    let tEnd: TimeInterval
    if s.count == 1 || gapOver {
      tStart = lastA
      tEnd = lastA + h24
    } else {
      let inLast = s.filter { $0.tMs >= lastA - h24 && $0.tMs <= lastA }
      tStart = inLast.map(\.tMs).min()!
      tEnd = tStart + h24
    }

    let inW = s.filter { $0.tMs >= tStart && $0.tMs < tEnd }
    var pts: [MoodDayModel] = []
    var hasAny = false
    let tf = DateFormatter()
    tf.dateFormat = "HH:mm"
    for (i, sp) in inW.enumerated() {
      let raw = (sp.tMs - tStart) / h24
      let x = max(0, min(1, raw))
      let d = Date(timeIntervalSince1970: sp.tMs / 1000)
      var vals: [String: Int?] = [:]
      for k in scaleKeys {
        if let n = sp.v[k] { vals[k] = n; hasAny = true } else { vals[k] = nil }
      }
      pts.append(MoodDayModel(
        index: i,
        label: tf.string(from: d),
        values: vals,
        xInWindow: x
      ))
    }
    return MoodChartResult(
      points: pts,
      hasAny: hasAny,
      windowStartMs: tStart,
      windowEndMs: tEnd
    )
  }

  private static func buildDayWindow(
    samples: [EmoSample], nDays: Int, scaleKeys: [String]
  ) -> MoodChartResult {
    let cal = Calendar.current
    if samples.isEmpty {
      let endD = cal.startOfDay(for: Date())
      guard let startD = cal.date(byAdding: .day, value: -(nDays - 1), to: endD) else {
        return MoodChartResult(points: [], hasAny: false, windowStartMs: nil, windowEndMs: nil)
      }
      return emptyDayGrid(from: startD, n: nDays, scaleKeys: scaleKeys)
    }

    func dayStart(_ tMs: TimeInterval) -> Date {
      let d = Date(timeIntervalSince1970: tMs / 1000)
      return cal.startOfDay(for: d)
    }
    let firstD = dayStart(samples.map(\.tMs).min()!)
    let lastD = dayStart(samples.map(\.tMs).max()!)
    let span = (cal.dateComponents([.day], from: firstD, to: lastD).day ?? 0) + 1
    let startD: Date
    if span >= nDays, let s = cal.date(byAdding: .day, value: -(nDays - 1), to: lastD) {
      startD = s
    } else {
      startD = firstD
    }
    var byDay: [Date: [[String: Int]]] = [:]
    for sp in samples {
      let d0 = dayStart(sp.tMs)
      byDay[d0, default: []].append(sp.v)
    }
    var pts: [MoodDayModel] = []
    var hasAny = false
    let df = DateFormatter()
    df.dateStyle = .short
    for i in 0..<nDays {
      guard let d = cal.date(byAdding: .day, value: i, to: startD) else { continue }
      let maps = byDay[d] ?? []
      let av = average(maps, scaleKeys: scaleKeys)
      for (_, v) in av where v != nil { hasAny = true }
      pts.append(MoodDayModel(
        index: i,
        label: df.string(from: d),
        values: av,
        xInWindow: nil
      ))
    }
    return MoodChartResult(
      points: pts,
      hasAny: hasAny,
      windowStartMs: nil,
      windowEndMs: nil
    )
  }

  private static func emptyDayGrid(
    from startD: Date, n: Int, scaleKeys: [String]
  ) -> MoodChartResult {
    let cal = Calendar.current
    let df = DateFormatter()
    df.dateStyle = .short
    var pts: [MoodDayModel] = []
    for i in 0..<n {
      let d = cal.date(byAdding: .day, value: i, to: startD) ?? startD
      pts.append(MoodDayModel(
        index: i,
        label: df.string(from: d),
        values: Dictionary(uniqueKeysWithValues: scaleKeys.map { ($0, nil) })
      ))
    }
    return MoodChartResult(points: pts, hasAny: false, windowStartMs: nil, windowEndMs: nil)
  }

  private static func buildCustomDay(
    samples: [EmoSample], from: Date, to: Date, scaleKeys: [String]
  ) -> MoodChartResult {
    let cal = Calendar.current
    let a = min(cal.startOfDay(for: from), cal.startOfDay(for: to))
    let b = max(cal.startOfDay(for: from), cal.startOfDay(for: to))
    let n = (cal.dateComponents([.day], from: a, to: b).day ?? 0) + 1
    if n < 1 { return MoodChartResult(points: [], hasAny: false, windowStartMs: nil, windowEndMs: nil) }
    var byDay: [Date: [[String: Int]]] = [:]
    for sp in samples {
      let d0 = cal.startOfDay(for: Date(timeIntervalSince1970: sp.tMs / 1000))
      if d0 < a || d0 > b { continue }
      byDay[d0, default: []].append(sp.v)
    }
    var pts: [MoodDayModel] = []
    var hasAny = false
    let df = DateFormatter()
    df.dateStyle = .short
    for i in 0..<n {
      let d = cal.date(byAdding: .day, value: i, to: a) ?? a
      let maps = byDay[d] ?? []
      let av = average(maps, scaleKeys: scaleKeys)
      for (_, v) in av where v != nil { hasAny = true }
      pts.append(MoodDayModel(
        index: i,
        label: df.string(from: d),
        values: av,
        xInWindow: nil
      ))
    }
    return MoodChartResult(
      points: pts,
      hasAny: hasAny,
      windowStartMs: nil,
      windowEndMs: nil
    )
  }
}

struct MoodLineChartView: View {
  let dayPoints: [MoodDayModel]
  let scaleKeys: [String]
  let colors: [Color]
  let isLight: Bool
  var activeFilter: Set<String> = []
  var windowStartMs: TimeInterval? = nil
  var windowEndMs: TimeInterval? = nil

  var body: some View {
    Canvas { ctx, size in
      let w = size.width
      let h = size.height
      let pl: CGFloat = 36
      let pr: CGFloat = 12
      let pt: CGFloat = 8
      let pb: CGFloat = 28
      let axis = isLight ? Color(red: 0.1, green: 0.1, blue: 0.1) : Color.white.opacity(0.92)
      let grid = isLight ? Color.black.opacity(0.12) : Color.white.opacity(0.16)
      let pointRing = Color.white
      let lineW: CGFloat = 4
      let pointR: CGFloat = 3
      let ringW: CGFloat = 1
      for t in 0...4 {
        let y = pt + (h - pt - pb) * (CGFloat(t) / 4)
        let g = pathLine(from: CGPoint(x: pl, y: y), to: CGPoint(x: w - pr, y: y))
        ctx.stroke(g, with: .color(grid), lineWidth: 1)
      }
      let allKeys = Set(scaleKeys)
      let keys: [String] = {
        if activeFilter.isEmpty { return scaleKeys }
        if activeFilter == allKeys { return scaleKeys }
        return scaleKeys.filter { activeFilter.contains($0) }
      }()
      for key in keys {
        guard let ki = scaleKeys.firstIndex(of: key) else { continue }
        let c = colors[min(ki, colors.count - 1)]
        let segs = splitSegments(dayPoints, key: key)
        for seg in segs {
          guard !seg.isEmpty else { continue }
          if seg.count >= 2 {
            let p = buildLinePath(seg: seg, w: w, h: h, pl: pl, pr: pr, pt: pt, pb: pb)
            ctx.stroke(
              p,
              with: .color(c),
              style: StrokeStyle(lineWidth: lineW, lineCap: .round, lineJoin: .round)
            )
          }
          let innerW = w - pl - pr
          let innerH = h - pt - pb
          for xyn in seg {
            let x = pl + xyn.0 * innerW
            let yp = pt + (1 - xyn.1) * innerH
            let dot = Path(ellipseIn: CGRect(x: x - pointR, y: yp - pointR, width: pointR * 2, height: pointR * 2))
            ctx.fill(dot, with: .color(c))
            ctx.stroke(
              dot,
              with: .color(pointRing),
              lineWidth: ringW
            )
          }
        }
      }
      if dayPoints.isEmpty { return }
      if let ws = windowStartMs, let we = windowEndMs, we > ws {
        for k in 0..<5 {
          let t = ws + (we - ws) * TimeInterval(k) / 4
          let d = Date(timeIntervalSince1970: t / 1000)
          let f = DateFormatter()
          f.dateFormat = "HH:mm"
          let xNorm = CGFloat(k) / 4
          let x = pl + xNorm * (w - pl - pr)
          ctx.draw(
            Text(f.string(from: d))
              .font(.system(size: 9))
              .foregroundStyle(axis),
            at: CGPoint(x: x, y: h - 4),
            anchor: .bottom
          )
        }
      } else {
        let count = dayPoints.count
        let labelIndices: [Int] = {
          if count == 1 { return [0] }
          if count <= 3 { return Array(0..<count) }
          if count <= 12 { return [0, count / 2, count - 1] }
          return (0..<5).map { k in (k * (count - 1)) / 4 }
        }()
        for i in Set(labelIndices) {
          guard i < dayPoints.count else { continue }
          let p = dayPoints[i]
          let xNorm: CGFloat
          if let xw = p.xInWindow { xNorm = CGFloat(xw) }
          else if count == 1 { xNorm = 0.5 }
          else { xNorm = CGFloat(i) / CGFloat(max(1, count - 1)) }
          let x = pl + xNorm * (w - pl - pr)
          ctx.draw(
            Text(p.label)
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

  private func splitSegments(_ points: [MoodDayModel], key: String) -> [[(CGFloat, CGFloat)]] {
    var cur: [(CGFloat, CGFloat)] = []
    for (i, p) in points.enumerated() {
      guard let opt = p.values[key], let yv = opt else { continue }
      let N = max(1, points.count - 1)
      let xn: CGFloat
      if let xw = p.xInWindow { xn = CGFloat(xw) }
      else { xn = points.count == 1 ? 0.5 : CGFloat(i) / CGFloat(N) }
      let yf = max(0, min(1, CGFloat(yv) / 100))
      cur.append((xn, yf))
    }
    if cur.isEmpty { return [] }
    if cur.count == 1 { return [[cur[0], cur[0]]] }
    return [cur]
  }

  private func buildLinePath(
    seg: [(CGFloat, CGFloat)], w: CGFloat, h: CGFloat, pl: CGFloat, pr: CGFloat, pt: CGFloat, pb: CGFloat
  ) -> Path {
    let innerW = w - pl - pr
    let innerH = h - pt - pb
    func tx(_ x: CGFloat) -> CGFloat { pl + x * innerW }
    func ty(_ y: CGFloat) -> CGFloat { pt + (1 - y) * innerH }
    if seg.isEmpty { return Path() }
    if seg.count == 1 {
      var p = Path()
      let a = CGPoint(x: tx(seg[0].0), y: ty(seg[0].1))
      p.move(to: a)
      p.addLine(to: a)
      return p
    }
    var path = Path()
    path.move(to: CGPoint(x: tx(seg[0].0), y: ty(seg[0].1)))
    for i in 1..<seg.count {
      path.addLine(to: CGPoint(x: tx(seg[i].0), y: ty(seg[i].1)))
    }
    return path
  }
}
