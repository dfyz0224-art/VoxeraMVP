package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanoprojects.voxera.R
import com.vanoprojects.voxera.ui.strings.LocalStrings
import com.vanoprojects.voxera.ui.theme.*
import com.vanoprojects.voxera.ui.theme.TextWithShadow

@Composable
fun ResultScreen(
  onNewAnalysis: () -> Unit,
  onShare: () -> Unit,
  onHistory: () -> Unit
) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val strings = LocalStrings.current
  val titleFontSize = 24.sp
  val subtitleFontSize = 16.sp
  
  Box(modifier = Modifier.fillMaxSize()) {
    // Фон: для светлой темы - белый, для стеклянной - bg_stars, для темной - bg_reverse_stars
    when (theme.type) {
      ThemeType.LIGHT -> {
        Image(
          painter = painterResource(R.drawable.bg_light),
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
      }
      ThemeType.GLASS -> {
        Image(
          painter = painterResource(R.drawable.bg_stars),
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
      }
      ThemeType.DARK -> {
        Image(
          painter = painterResource(R.drawable.bg_reverse_stars),
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
      }
    }
    
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp, vertical = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(34.dp))
      // В светлой теме - белый, в остальных - backgroundTextPrimary
      val titleColor = if (theme.type == ThemeType.LIGHT) {
        Color.White
      } else {
        colors.backgroundTextPrimary
      }
      Text(
        text = strings.result,
        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 42.sp),
        color = titleColor,
        fontWeight = FontWeight.Light,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(48.dp))

      MetricCard(title = strings.emotionalBackground, value = strings.tense, progress = 0.72f, gradientIndex = 0)
      Spacer(modifier = Modifier.height(12.dp))
      MetricCard(title = strings.stressLevel, value = strings.medium, progress = 0.55f, gradientIndex = 1)
      Spacer(modifier = Modifier.height(12.dp))
      MetricCard(title = strings.innerAnxiety, value = strings.mediumF, progress = 0.58f, gradientIndex = 2)
      Spacer(modifier = Modifier.height(12.dp))
      MetricCard(title = strings.resource, value = strings.reduced, progress = 0.40f, gradientIndex = 3)

      Spacer(modifier = Modifier.weight(1f))

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ThemedOutlinedButton(
          text = strings.share,
          onClick = onShare,
          modifier = Modifier.weight(1f)
        )

        ThemedFilledButton(
          text = strings.newAnalysis,
          onClick = onNewAnalysis,
          modifier = Modifier.weight(1f)
        )
      }
      Spacer(modifier = Modifier.height(10.dp))
    }
  }
}

@Composable
private fun MetricCard(title: String, value: String, progress: Float, gradientIndex: Int) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  
  ThemedCard(gradientIndex = gradientIndex) {
  Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.SpaceBetween
  ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        TextWithShadow(
          text = title,
          color = colors.textSecondary,
          style = MaterialTheme.typography.bodyMedium
        )
        TextWithShadow(
          text = "›",
          color = colors.textSecondary,
          style = MaterialTheme.typography.bodyMedium
        )
      }
      TextWithShadow(
        text = value,
        color = colors.textPrimary,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
      )
    LinearProgressIndicator(
      progress = { progress },
        color = colors.primaryGlow.copy(alpha = 0.7f),
        trackColor = colors.primaryGlow.copy(alpha = 0.12f),
        modifier = Modifier.height(4.dp)
      )
    }
  }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ResultScreenPreview() {
  VoxeraTheme {
    ResultScreen(
      onNewAnalysis = {},
      onShare = {},
      onHistory = {}
    )
  }
}
