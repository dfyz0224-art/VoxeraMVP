import SwiftUI
import StoreKit

struct SubscriptionsView: View {
  @Binding var path: NavigationPath
  @EnvironmentObject private var locale: LocaleStore
  @EnvironmentObject private var prefs: PreferencesStore
  @StateObject private var store = SubscriptionStore()

  var s: AppStrings { locale.strings }
  private var c: ThemeColors { prefs.themeType.colors() }
  private var russian: Bool { s.currentPlan == "Текущий план" }

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

          if let message = store.message, !message.isEmpty {
            Text(message)
              .font(.footnote)
              .foregroundColor(c.backgroundTextPrimary.opacity(0.9))
          }

          planCard(
            title: s.planBasic,
            description: s.planBasicDesc,
            productId: nil,
            isCurrent: store.activeProductId == nil,
            gradient: 0,
            onTap: {}
          )
          paidCard(title: s.planStandard, description: s.planStandardDesc, productId: VoxeraSubscriptionProduct.standard, gradient: 1)
          paidCard(title: s.planPro, description: s.planProDesc, productId: VoxeraSubscriptionProduct.pro, gradient: 2)
          paidCard(title: s.planUnlimited, description: s.planUnlimitedDesc, productId: VoxeraSubscriptionProduct.unlimited, gradient: 3)
          planCard(
            title: s.planBusiness,
            description: s.planBusinessDesc,
            productId: nil,
            isCurrent: false,
            gradient: 4,
            onTap: { path.append(AppRoute.forBusiness) }
          )

          Button {
            Task { await store.restore() }
          } label: {
            Text(SubscriptionStore.restoreTitle(russian: russian))
              .font(.body.weight(.semibold))
              .frame(maxWidth: .infinity)
              .padding(.vertical, 12)
          }
          .disabled(store.isBusy)
          .foregroundColor(c.backgroundTextPrimary)

          Spacer().frame(height: 24)
        }
        .padding(.horizontal, 20)
      }
    }
    .task {
      store.messageUsesRussian = russian
      await store.load()
    }
  }

  private func paidCard(title: String, description: String, productId: String, gradient: Int) -> some View {
    let price = store.productsById[productId]?.displayPrice
    let action = price.map { SubscriptionStore.subscribeTitle(russian: russian, price: $0) }
    return planCard(
      title: title,
      description: description,
      productId: productId,
      isCurrent: store.activeProductId == productId,
      gradient: gradient,
      actionTitle: action,
      onTap: {
        guard !store.isBusy, store.activeProductId != productId else { return }
        Task { await store.purchase(productId) }
      }
    )
  }

  private func planCard(
    title: String,
    description: String,
    productId: String?,
    isCurrent: Bool,
    gradient: Int,
    actionTitle: String? = nil,
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
        if let actionTitle, !isCurrent {
          Text(actionTitle)
            .font(.system(size: 16, weight: .semibold))
            .foregroundColor(.white)
        }
        if productId != nil, actionTitle == nil, !isCurrent {
          Text(russian ? "Оформить" : "Subscribe")
            .font(.system(size: 16, weight: .semibold))
            .foregroundColor(.white.opacity(0.7))
        }
      }
    }
  }
}
