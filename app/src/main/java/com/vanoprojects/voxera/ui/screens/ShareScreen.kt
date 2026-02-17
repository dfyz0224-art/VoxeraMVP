package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
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
import io.github.fletchmckee.liquid.liquid
import io.github.fletchmckee.liquid.liquefiable
import io.github.fletchmckee.liquid.rememberLiquidState

@Composable
fun ShareScreen() {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val strings = LocalStrings.current
  val liquidState = rememberLiquidState()
  
  Box(modifier = Modifier.fillMaxSize()) {
    // Фон: для светлой темы - белый, для стеклянной - bg_stars, для темной - bg_clean с liquid эффектом
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
          painter = painterResource(R.drawable.bg_clean),
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .fillMaxSize()
            .liquefiable(liquidState)
        )
      }
    }
    
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
      Spacer(modifier = Modifier.height(18.dp))
      // Кастомный заголовок - в стеклянной теме белый, в остальных - shadowColor
      val titleColor = if (theme.type == ThemeType.GLASS) {
        Color.White
      } else {
        colors.shadowColor
      }
      Text(
        text = strings.shareResult,
        color = titleColor,
        style = MaterialTheme.typography.headlineMedium.copy(
          fontSize = 42.sp, // Увеличенный размер
          fontWeight = FontWeight.Light,
          lineHeight = 40.sp // Увеличенное расстояние между строками
        ),
        modifier = Modifier.height(95.dp),
        textAlign = TextAlign.Center
      )
      Spacer(modifier = Modifier.height(22.dp))
      Text(
        text = strings.shareOnlyBrief,
        color = colors.backgroundTextSecondary,
        style = MaterialTheme.typography.bodyLarge.copy(
          fontSize = 18.sp, // Увеличенный размер
          fontWeight = FontWeight.Normal
        ),
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
      )
      Spacer(modifier = Modifier.height(32.dp))

      // Preview card в стиле ModeSelect - большая карточка
      SharePreviewCard(strings = strings)

      Spacer(modifier = Modifier.weight(1f))

      // Быстрые кнопки
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        ShareButton(
          label = "TikTok",
          iconRes = R.drawable.ic_tiktok,
          liquidState = liquidState,
          onClick = { },
          modifier = Modifier.weight(1f),
          gradientIndex = 0
        )
        ShareButton(
          label = "Instagram",
          iconRes = R.drawable.ic_instagram,
          liquidState = liquidState,
          onClick = { },
          modifier = Modifier.weight(1f),
          gradientIndex = 1
        )
        ShareButton(
          label = "Telegram",
          iconRes = R.drawable.ic_telegram,
          liquidState = liquidState,
          onClick = { },
          modifier = Modifier.weight(1f),
          gradientIndex = 2
        )
        ShareButton(
          label = strings.link,
          iconRes = R.drawable.ic_link,
          liquidState = liquidState,
          onClick = { },
          modifier = Modifier.weight(1f),
          gradientIndex = 3
        )
      }

      Spacer(modifier = Modifier.height(18.dp))
      ThemedFilledButton(
        text = strings.more,
        onClick = { },
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(10.dp))
    }
  }
}

@Composable
private fun SharePreviewCard(strings: com.vanoprojects.voxera.ui.strings.Strings = LocalStrings.current) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  
  ThemedCard(modifier = Modifier.height(200.dp), gradientIndex = 0) {
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Center
      ) {
        Image(
          painter = painterResource(R.drawable.ic_voxera_logo_text),
          contentDescription = null,
          modifier = Modifier.height(44.dp)
        )
      Spacer(modifier = Modifier.height(16.dp))
      TextWithShadow(
        text = strings.state,
        color = colors.textSecondary,
        style = MaterialTheme.typography.bodySmall
      )
      Spacer(modifier = Modifier.height(4.dp))
      TextWithShadow(
        text = strings.stableLightTension,
        color = colors.textPrimary,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Normal
      )
    }
  }
}

@Composable
private fun ShareButton(
  label: String,
  iconRes: Int,
  liquidState: io.github.fletchmckee.liquid.LiquidState,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  gradientIndex: Int
) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  
  // В светлой теме иконки и текст белые, в остальных - тоже белые
  val iconColor = Color.White
  val textColor = Color.White
  
  // Используем ThemedCard для всех тем, передаем liquidState для стеклянной темы
  ThemedCard(
    modifier = modifier.height(100.dp),
    onClick = onClick,
    liquidState = liquidState,
    gradientIndex = gradientIndex
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.fillMaxSize()
    ) {
      Image(
        painter = painterResource(iconRes),
        contentDescription = null,
        modifier = Modifier.size(50.dp), // Увеличен размер иконок
        colorFilter = ColorFilter.tint(iconColor)
      )
      Spacer(modifier = Modifier.height(6.dp))
      TextWithShadow(
        text = label,
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
        color = textColor
      )
    }
  }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ShareScreenPreview() {
  VoxeraTheme {
    ShareScreen()
  }
}
