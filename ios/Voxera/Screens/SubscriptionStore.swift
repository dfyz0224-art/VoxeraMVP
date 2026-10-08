import Foundation
import StoreKit

enum VoxeraSubscriptionProduct {
  static let standard = "voxera_standard_monthly"
  static let pro = "voxera_pro_monthly"
  static let unlimited = "voxera_unlimited_monthly"
  static let paid = [standard, pro, unlimited]

  static func rank(_ id: String) -> Int {
    switch id {
    case unlimited: return 3
    case pro: return 2
    case standard: return 1
    default: return 0
    }
  }
}

@MainActor
final class SubscriptionStore: ObservableObject {
  @Published private(set) var productsById: [String: Product] = [:]
  @Published private(set) var activeProductId: String?
  @Published private(set) var isBusy = false
  @Published var message: String?

  private var updates: Task<Void, Never>?

  init() {
    updates = Task { [weak self] in
      await self?.observeTransactions()
    }
  }

  func load() async {
    do {
      let products = try await Product.products(for: VoxeraSubscriptionProduct.paid)
      productsById = Dictionary(uniqueKeysWithValues: products.map { ($0.id, $0) })
      await refreshEntitlements()
      if productsById.isEmpty {
        message = Self.unavailable(russian: messageUsesRussian)
      }
    } catch {
      message = error.localizedDescription
    }
  }

  func purchase(_ id: String) async {
    guard let product = productsById[id] else {
      message = Self.unavailable(russian: messageUsesRussian)
      return
    }
    isBusy = true
    defer { isBusy = false }
    do {
      let result = try await product.purchase()
      switch result {
      case .success(let verification):
        let transaction = try Self.checkVerified(verification)
        await transaction.finish()
        await refreshEntitlements()
        message = nil
      case .userCancelled:
        break
      case .pending:
        message = Self.pending(russian: messageUsesRussian)
      @unknown default:
        break
      }
    } catch {
      message = error.localizedDescription
    }
  }

  func restore() async {
    isBusy = true
    defer { isBusy = false }
    do {
      try await AppStore.sync()
      await refreshEntitlements()
    } catch {
      message = error.localizedDescription
    }
  }

  var messageUsesRussian = false

  private func observeTransactions() async {
    for await result in Transaction.updates {
      guard case .verified(let transaction) = result else { continue }
      await transaction.finish()
      await refreshEntitlements()
    }
  }

  private func refreshEntitlements() async {
    var best: String?
    for await result in Transaction.currentEntitlements {
      guard case .verified(let transaction) = result else { continue }
      guard transaction.productType == .autoRenewable else { continue }
      guard VoxeraSubscriptionProduct.paid.contains(transaction.productID) else { continue }
      if VoxeraSubscriptionProduct.rank(transaction.productID) > VoxeraSubscriptionProduct.rank(best ?? "") {
        best = transaction.productID
      }
    }
    activeProductId = best
  }

  private static func checkVerified<T>(_ result: VerificationResult<T>) throws -> T {
    switch result {
    case .unverified:
      throw SubscriptionStoreError.failedVerification
    case .verified(let value):
      return value
    }
  }

  static func unavailable(russian: Bool) -> String {
    russian
      ? "Подписки пока недоступны. Создайте продукты в App Store Connect."
      : "Subscriptions are not available yet. Create the products in App Store Connect."
  }

  static func pending(russian: Bool) -> String {
    russian ? "Оплата ожидает подтверждения." : "Payment is pending approval."
  }

  static func subscribeTitle(russian: Bool, price: String) -> String {
    russian ? "Оформить · \(price)" : "Subscribe · \(price)"
  }

  static func restoreTitle(russian: Bool) -> String {
    russian ? "Восстановить покупки" : "Restore purchases"
  }
}

private enum SubscriptionStoreError: Error {
  case failedVerification
}
