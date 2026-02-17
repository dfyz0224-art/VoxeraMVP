package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import com.vanoprojects.voxera.R
import com.vanoprojects.voxera.ui.strings.LocalStrings
import com.vanoprojects.voxera.ui.theme.*
import com.vanoprojects.voxera.ui.theme.TextWithShadow

@Composable
fun HistoryScreen() {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val strings = LocalStrings.current
  
  // Фон: для светлой темы - белый, для остальных - VoxeraBackground
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
        .padding(20.dp)
    ) {
      Spacer(modifier = Modifier.height(10.dp))
      TopBar(title = strings.historyTitle)
      Spacer(modifier = Modifier.height(14.dp))

      HistoryItem(date = strings.today, state = strings.lightTension, stress = strings.medium, gradientIndex = 0)
      Spacer(modifier = Modifier.height(10.dp))
      HistoryItem(date = strings.yesterday, state = strings.stable, stress = strings.low, gradientIndex = 1)
      Spacer(modifier = Modifier.height(10.dp))
      HistoryItem(date = strings.twoDaysAgo, state = strings.overload, stress = strings.high, gradientIndex = 2)
    }
  }
}

@Composable
private fun HistoryItem(date: String, state: String, stress: String, gradientIndex: Int) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val strings = LocalStrings.current
  
  ThemedCard(gradientIndex = gradientIndex) {
    Column {
      TextWithShadow(
        text = date,
        color = colors.textSecondary,
      style = MaterialTheme.typography.bodySmall
    )
      Spacer(modifier = Modifier.height(6.dp))
      TextWithShadow(
        text = state,
        color = colors.textPrimary,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(4.dp))
      TextWithShadow(
        text = "${strings.stressLabel}: $stress",
        color = colors.textSecondary,
      style = MaterialTheme.typography.bodyMedium
    )
    }
  }
}
