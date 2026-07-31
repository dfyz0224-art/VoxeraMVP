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
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanoprojects.voxera.R
import com.vanoprojects.voxera.ui.strings.LocalStrings
import com.vanoprojects.voxera.ui.theme.LocalVoxeraTheme
import com.vanoprojects.voxera.ui.theme.ThemeType
import com.vanoprojects.voxera.ui.theme.ThemedCard
import com.vanoprojects.voxera.ui.theme.cardParagraphTextStyle

@Composable
fun SubscriptionsScreen(
  onForBusiness: () -> Unit
) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val strings = LocalStrings.current

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

      SubscriptionPlanCard(
        title = strings.planBasic,
        description = strings.planBasicDesc,
        isCurrent = true,
        gradientIndex = 0,
        onClick = {}
      )
      SubscriptionPlanCard(
        title = strings.planStandard,
        description = strings.planStandardDesc,
        isCurrent = false,
        gradientIndex = 1,
        onClick = {}
      )
      SubscriptionPlanCard(
        title = strings.planPro,
        description = strings.planProDesc,
        isCurrent = false,
        gradientIndex = 2,
        onClick = {}
      )
      SubscriptionPlanCard(
        title = strings.planUnlimited,
        description = strings.planUnlimitedDesc,
        isCurrent = false,
        gradientIndex = 3,
        onClick = {}
      )
      SubscriptionPlanCard(
        title = strings.planBusiness,
        description = strings.planBusinessDesc,
        isCurrent = false,
        gradientIndex = 4,
        onClick = onForBusiness
      )
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun SubscriptionPlanCard(
  title: String,
  description: String,
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
    }
  }
}
