import SwiftUI

struct SubscriptionsView: View {
  @Binding var path: NavigationPath
  @EnvironmentObject private var locale: LocaleStore
  @EnvironmentObject private var prefs: PreferencesStore

  var s: AppStrings { locale.strings }
  private var c: ThemeColors { prefs.themeType.colors() }

  var body: some View {
    ZStack {
      BackgroundImageName()
      ScrollView {
        VStack(alignment: .leading, spacing: 12) {
          Spacer().frame(height: 10)
          Text(s.manageSubscriptions)
            .font(.title2.weight(.semibold))
            .foregroundColor(c.backgroundTextPrimary)
            .frame(maxWidth: .infinity, alignment: .leading)

          planCard(
            title: s.planBasic,
            description: s.planBasicDesc,
            isCurrent: true,
            gradient: 0,
            onTap: {}
          )
          planCard(
            title: s.planStandard,
            description: s.planStandardDesc,
            isCurrent: false,
            gradient: 1,
            onTap: {}
          )
          planCard(
            title: s.planPro,
            description: s.planProDesc,
            isCurrent: false,
            gradient: 2,
            onTap: {}
          )
          planCard(
            title: s.planUnlimited,
            description: s.planUnlimitedDesc,
            isCurrent: false,
            gradient: 3,
            onTap: {}
          )
          planCard(
            title: s.planBusiness,
            description: s.planBusinessDesc,
            isCurrent: false,
            gradient: 4,
            onTap: { path.append(AppRoute.forBusiness) }
          )
          Spacer().frame(height: 24)
        }
        .padding(.horizontal, 20)
      }
    }
  }

  private func planCard(
    title: String,
    description: String,
    isCurrent: Bool,
    gradient: Int,
    onTap: @escaping () -> Void
  ) -> some View {
    ThemedCard(gradientIndex: gradient, onTap: onTap) {
      VStack(alignment: .leading, spacing: 8) {
        if isCurrent {
          Text(s.currentPlan)
            .font(.system(size: 13))
            .foregroundColor(.white.opacity(0.75))
        }
        Text(title)
          .font(.system(size: 20, weight: .semibold))
          .foregroundColor(.white)
        Text(description)
          .font(.system(size: 16))
          .foregroundColor(.white.opacity(0.85))
          .fixedSize(horizontal: false, vertical: true)
      }
    }
  }
}
