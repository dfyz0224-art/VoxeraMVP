package com.vanoprojects.voxera.billing

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.vanoprojects.voxera.ui.screens.stringsToAppLanguage
import com.vanoprojects.voxera.ui.strings.AppLanguage
import com.vanoprojects.voxera.ui.strings.Strings

object VoxeraSubscriptionProduct {
  const val STANDARD = "voxera_standard_monthly"
  const val PRO = "voxera_pro_monthly"
  const val UNLIMITED = "voxera_unlimited_monthly"
  val paid = listOf(STANDARD, PRO, UNLIMITED)

  fun rank(id: String?): Int = when (id) {
    UNLIMITED -> 3
    PRO -> 2
    STANDARD -> 1
    else -> 0
  }
}

class PlaySubscriptionBilling(context: Context) {
  var products by mutableStateOf<Map<String, ProductDetails>>(emptyMap())
    private set
  var activeProductId by mutableStateOf<String?>(null)
    private set
  var message by mutableStateOf<String?>(null)
  var isBusy by mutableStateOf(false)
    private set

  private val main = Handler(Looper.getMainLooper())
  private var activePurchaseToken: String? = null
  private var russian = false

  private val billingClient: BillingClient = BillingClient.newBuilder(context.applicationContext)
    .setListener { result, purchases ->
      main.post {
        isBusy = false
        when (result.responseCode) {
          BillingClient.BillingResponseCode.OK -> purchases?.forEach { handlePurchase(it) }
          BillingClient.BillingResponseCode.USER_CANCELED -> Unit
          BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> queryActive()
          else -> message = result.debugMessage.ifBlank { "Billing error ${result.responseCode}" }
        }
      }
    }
    .enablePendingPurchases(
      PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
    )
    .build()

  fun start(strings: Strings) {
    russian = when (stringsToAppLanguage(strings)) {
      AppLanguage.RU, AppLanguage.UK -> true
      else -> false
    }
    if (billingClient.isReady) {
      queryProducts()
      queryActive()
      return
    }
    billingClient.startConnection(object : BillingClientStateListener {
      override fun onBillingSetupFinished(result: BillingResult) {
        main.post {
          if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            queryProducts()
            queryActive()
          } else {
            message = result.debugMessage.ifBlank { unavailable() }
          }
        }
      }

      override fun onBillingServiceDisconnected() {}
    })
  }

  fun end() {
    if (billingClient.isReady) billingClient.endConnection()
  }

  fun purchase(activity: Activity, productId: String) {
    val details = products[productId]
    if (details == null) {
      message = unavailable()
      return
    }
    val offer = details.subscriptionOfferDetails?.firstOrNull()
    val offerToken = offer?.offerToken
    if (offerToken == null) {
      message = unavailable()
      return
    }
    val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
      .setProductDetails(details)
      .setOfferToken(offerToken)
      .build()
    val flow = BillingFlowParams.newBuilder()
      .setProductDetailsParamsList(listOf(productParams))
    val oldToken = activePurchaseToken
    if (!oldToken.isNullOrBlank() && activeProductId != null && activeProductId != productId) {
      flow.setSubscriptionUpdateParams(
        BillingFlowParams.SubscriptionUpdateParams.newBuilder()
          .setOldPurchaseToken(oldToken)
          .setSubscriptionReplacementMode(
            BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.WITH_TIME_PRORATION
          )
          .build()
      )
    }
    isBusy = true
    val launched = billingClient.launchBillingFlow(activity, flow.build())
    if (launched.responseCode != BillingClient.BillingResponseCode.OK) {
      isBusy = false
      message = launched.debugMessage.ifBlank { unavailable() }
    }
  }

  fun restore() {
    queryActive()
  }

  fun priceLabel(productId: String): String? {
    val offer = products[productId]?.subscriptionOfferDetails?.firstOrNull() ?: return null
    val phases = offer.pricingPhases.pricingPhaseList
    val paid = phases.lastOrNull { it.priceAmountMicros > 0 } ?: phases.lastOrNull()
    return paid?.formattedPrice
  }

  fun subscribeTitle(price: String): String =
    if (russian) "Оформить · $price" else "Subscribe · $price"

  fun subscribePlain(): String =
    if (russian) "Оформить" else "Subscribe"

  fun restoreTitle(): String =
    if (russian) "Восстановить покупки" else "Restore purchases"

  private fun queryProducts() {
    val requested = VoxeraSubscriptionProduct.paid.map { id ->
      QueryProductDetailsParams.Product.newBuilder()
        .setProductId(id)
        .setProductType(BillingClient.ProductType.SUBS)
        .build()
    }
    val params = QueryProductDetailsParams.newBuilder().setProductList(requested).build()
    billingClient.queryProductDetailsAsync(params) { result, productDetailsList ->
      main.post {
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
          message = result.debugMessage.ifBlank { unavailable() }
          return@post
        }
        products = productDetailsList.associateBy { it.productId }
        if (productDetailsList.isEmpty()) message = unavailable()
      }
    }
  }

  private fun queryActive() {
    val params = QueryPurchasesParams.newBuilder()
      .setProductType(BillingClient.ProductType.SUBS)
      .build()
    billingClient.queryPurchasesAsync(params) { result, purchases ->
      main.post {
        if (result.responseCode != BillingClient.BillingResponseCode.OK) return@post
        purchases.forEach { handlePurchase(it) }
        val owns = purchases.any { purchase ->
          purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
            purchase.products.any { VoxeraSubscriptionProduct.rank(it) > 0 }
        }
        if (!owns) {
          activeProductId = null
          activePurchaseToken = null
        }
        EntitlementStore.hasActiveSubscription = owns
      }
    }
  }

  private fun handlePurchase(purchase: Purchase) {
    if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
    val id = purchase.products.maxByOrNull { VoxeraSubscriptionProduct.rank(it) } ?: return
    if (VoxeraSubscriptionProduct.rank(id) == 0) return
        if (VoxeraSubscriptionProduct.rank(id) >= VoxeraSubscriptionProduct.rank(activeProductId)) {
          activeProductId = id
          activePurchaseToken = purchase.purchaseToken
          EntitlementStore.hasActiveSubscription = true
        }
    if (!purchase.isAcknowledged) {
      val ack = AcknowledgePurchaseParams.newBuilder()
        .setPurchaseToken(purchase.purchaseToken)
        .build()
      billingClient.acknowledgePurchase(ack) { }
    }
  }

  private fun unavailable(): String =
    if (russian) {
      "Подписки пока недоступны. Создайте продукты в Google Play Console."
    } else {
      "Subscriptions are not available yet. Create the products in Google Play Console."
    }
}

fun Context.findActivity(): Activity? {
  var current: Context = this
  while (current is ContextWrapper) {
    if (current is Activity) return current
    current = current.baseContext
  }
  return null
}

/** Updates [EntitlementStore] from Play subscriptions without opening the plans screen. */
fun refreshPlaySubscription(context: Context) {
  val appContext = context.applicationContext
  val client = BillingClient.newBuilder(appContext)
    .setListener { _, _ -> }
    .enablePendingPurchases(
      PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
    )
    .build()
  client.startConnection(object : BillingClientStateListener {
    override fun onBillingSetupFinished(result: BillingResult) {
      if (result.responseCode != BillingClient.BillingResponseCode.OK) {
        client.endConnection()
        return
      }
      val params = QueryPurchasesParams.newBuilder()
        .setProductType(BillingClient.ProductType.SUBS)
        .build()
      client.queryPurchasesAsync(params) { queryResult, purchases ->
        if (queryResult.responseCode == BillingClient.BillingResponseCode.OK) {
          EntitlementStore.hasActiveSubscription = purchases.any { purchase ->
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
              purchase.products.any { VoxeraSubscriptionProduct.rank(it) > 0 }
          }
        }
        client.endConnection()
      }
    }

    override fun onBillingServiceDisconnected() {}
  })
}
