package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanoprojects.voxera.R
import com.vanoprojects.voxera.legal.privacyPolicyFullTextEn
import com.vanoprojects.voxera.ui.strings.LocalStrings
import com.vanoprojects.voxera.ui.theme.*

@Composable
fun PrivacyPolicyScreen(
  onBack: () -> Unit
) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val strings = LocalStrings.current
  val scroll = rememberScrollState()
  val cardShape = RoundedCornerShape(16.dp)
  // Как у ThemedCard gradientIndex 0 (тёмно-синий)
  val gradient = when (theme.type) {
    ThemeType.LIGHT -> listOf(Color(0xFF001F5C), Color(0xFF0055BD))
    ThemeType.GLASS -> colors.cardBackgroundGradient
  }
  val bodyOnCard = when (theme.type) {
    ThemeType.LIGHT -> Color.White
    ThemeType.GLASS -> colors.textPrimary
  }
  val noteOnCard = when (theme.type) {
    ThemeType.LIGHT -> Color.White.copy(alpha = 0.88f)
    ThemeType.GLASS -> colors.textSecondary
  }
  val borderColor = when (theme.type) {
    ThemeType.LIGHT -> Color.Black.copy(alpha = 0.08f)
    ThemeType.GLASS -> Color.White.copy(alpha = 0.35f)
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
        .verticalScroll(scroll)
        .padding(horizontal = 20.dp)
        .padding(top = 8.dp, bottom = 24.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        TextButton(onClick = onBack) {
          Text(
            text = strings.back,
            color = colors.backgroundTextPrimary,
            style = MaterialTheme.typography.titleMedium
          )
        }
      }
      Text(
        text = strings.privacyPolicyFullTitle,
        style = MaterialTheme.typography.headlineSmall,
        color = colors.backgroundTextPrimary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 12.dp)
      )
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(cardShape)
          .background(Brush.linearGradient(gradient))
          .border(1.dp, borderColor, cardShape)
          .padding(horizontal = 18.dp, vertical = 20.dp)
      ) {
        Text(
          text = strings.privacyPolicyFullEnglishNote,
          style = MaterialTheme.typography.bodySmall,
          color = noteOnCard,
          modifier = Modifier.padding(bottom = 14.dp)
        )
        Text(
          text = privacyPolicyFullTextEn(),
          style = MaterialTheme.typography.bodyMedium,
          color = bodyOnCard,
          lineHeight = 22.sp
        )
      }
    }
  }
}
