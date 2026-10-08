package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanoprojects.voxera.R
import com.vanoprojects.voxera.billing.PlaySubscriptionBilling
import com.vanoprojects.voxera.billing.VoxeraSubscriptionProduct
import com.vanoprojects.voxera.billing.findActivity
import com.vanoprojects.voxera.ui.strings.LocalStrings
import com.vanoprojects.voxera.ui.theme.LocalVoxeraTheme
import com.vanoprojects.voxera.ui.theme.ThemeType
import com.vanoprojects.voxera.ui.theme.ThemedCard
import com.vanoprojects.voxera.ui.theme.ThemedFilledButton
import com.vanoprojects.voxera.ui.theme.cardParagraphTextStyle

@Composable
fun SubscriptionsScreen(
  onForBusiness: () -> Unit
) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val strings = LocalStrings.current
  val context = LocalContext.current
  val billing = remember { PlaySubscriptionBilling(context) }

  DisposableEffect(strings) {
    billing.start(strings)
    onDispose { billing.end() }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    if (theme.type == ThemeType.LIGHT) {
      Image(
        painter = painterResource(R.drawable.bg_light),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )
    } else {
      VoxeraBackground {}
    }

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = strings.manageSubscriptions,
        style = MaterialTheme.typography.titleLarge,
        color = colors.textPrimary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(4.dp))

      if (!billing.message.isNullOrBlank()) {
        Text(
          text = billing.message.orEmpty(),
          style = MaterialTheme.typography.bodyMedium,
          color = colors.textPrimary,
          modifier = Modifier.fillMaxWidth()
        )
      }

      SubscriptionPlanCard(
        title = strings.planBasic,
        description = strings.planBasicDesc,
        action = null,
        isCurrent = billing.activeProductId == null,
        gradientIndex = 0,
        onClick = {}
      )
      paidPlanCard(
        productId = VoxeraSubscriptionProduct.STANDARD,
        title = strings.planStandard,
        description = strings.planStandardDesc,
        gradientIndex = 1,
        billing = billing
      )
      paidPlanCard(
        productId = VoxeraSubscriptionProduct.PRO,
        title = strings.planPro,
        description = strings.planProDesc,
        gradientIndex = 2,
        billing = billing
      )
      paidPlanCard(
        productId = VoxeraSubscriptionProduct.UNLIMITED,
        title = strings.planUnlimited,
        description = strings.planUnlimitedDesc,
        gradientIndex = 3,
        billing = billing
      )
      SubscriptionPlanCard(
        title = strings.planBusiness,
        description = strings.planBusinessDesc,
        action = null,
        isCurrent = false,
        gradientIndex = 4,
        onClick = onForBusiness
      )
      ThemedFilledButton(
        text = billing.restoreTitle(),
        onClick = { billing.restore() },
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun paidPlanCard(
  productId: String,
  title: String,
  description: String,
  gradientIndex: Int,
  billing: PlaySubscriptionBilling
) {
  val context = LocalContext.current
  val price = billing.priceLabel(productId)
  val isCurrent = billing.activeProductId == productId
  SubscriptionPlanCard(
    title = title,
    description = description,
    action = when {
      isCurrent -> null
      price != null -> billing.subscribeTitle(price)
      else -> billing.subscribePlain()
    },
    isCurrent = isCurrent,
    gradientIndex = gradientIndex,
    onClick = {
      if (billing.isBusy || isCurrent) return@SubscriptionPlanCard
      val activity = context.findActivity() ?: return@SubscriptionPlanCard
      billing.purchase(activity, productId)
    }
  )
}

@Composable
private fun SubscriptionPlanCard(
  title: String,
  description: String,
  action: String?,
  isCurrent: Boolean,
  gradientIndex: Int,
  onClick: () -> Unit
) {
  val colors = LocalVoxeraTheme.current.colors
  val strings = LocalStrings.current

  ThemedCard(
    modifier = Modifier
      .fillMaxWidth()
      .wrapContentHeight(),
    gradientIndex = gradientIndex,
    onClick = onClick,
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp)
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      if (isCurrent) {
        Text(
          text = strings.currentPlan,
          style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
          color = colors.textSecondary,
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))
      }
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp, lineHeight = 26.sp),
        color = colors.textPrimary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = description,
        style = cardParagraphTextStyle().copy(fontSize = 16.sp, lineHeight = 24.sp),
        color = colors.textSecondary,
        modifier = Modifier.fillMaxWidth()
      )
      if (!action.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = action,
          style = MaterialTheme.typography.titleSmall,
          color = colors.textPrimary,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }
}
