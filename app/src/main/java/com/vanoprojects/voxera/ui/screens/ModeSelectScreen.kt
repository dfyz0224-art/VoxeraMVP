package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanoprojects.voxera.R
import com.vanoprojects.voxera.ui.strings.LocalStrings
import com.vanoprojects.voxera.ui.theme.*
import com.vanoprojects.voxera.ui.theme.TextWithShadow

@Composable
fun ModeSelectScreen(
  onBack: () -> Unit,
  onModeChosen: (String) -> Unit,
  onOpenSettings: () -> Unit = {}
) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val strings = LocalStrings.current

  Box(modifier = Modifier.fillMaxSize()) {
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
        .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
      Spacer(modifier = Modifier.height(48.dp))
      
      // Логотип ic_voxera_logo_text - увеличенный размер, по центру
      Box(
        modifier = Modifier
          .height(70.dp)
          .wrapContentHeight(),
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = painterResource(R.drawable.ic_voxera_logo_text),
          contentDescription = null,
          modifier = Modifier
            .fillMaxWidth(),
          contentScale = ContentScale.Fit,

        )
      }
      
      Spacer(modifier = Modifier.height(28.dp))
      
      // Заголовок "Выберите режим" под логотипом, перед карточками - по центру, ближе к карточкам
      // В светлой теме - белый, в остальных - backgroundTextPrimary
      val titleColor = if (theme.type == ThemeType.LIGHT) {
        Color.White
      } else {
        colors.backgroundTextPrimary
      }
      Text(
        text = strings.selectMode,
        style = MaterialTheme.typography.headlineSmall,
        color = titleColor,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
      )
      
      Spacer(modifier = Modifier.height(12.dp))

      // Карточки с фиксированной высотой (одинаковый размер для всех)
      val modeCardHeight = 150.dp
      ModeCard(
        iconRes = R.drawable.ic_mother,
        title = strings.parentMode,
        subtitle = strings.parentModeSubtitle,
        onClick = { onModeChosen("mom") },
        gradientIndex = 0,
        height = modeCardHeight
      )
      Spacer(modifier = Modifier.height(16.dp))
      ModeCard(
        iconRes = R.drawable.ic_teen,
        title = strings.universalMode,
        subtitle = strings.universalModeSubtitle,
        onClick = { onModeChosen("teen") },
        gradientIndex = 1,
        height = modeCardHeight
      )
      Spacer(modifier = Modifier.height(16.dp))
      ModeCard(
        iconRes = R.drawable.ic_quick,
        title = strings.deepAnalysis,
        subtitle = strings.deepAnalysisSubtitle,
        onClick = { onModeChosen("quick") },
        gradientIndex = 2,
        height = modeCardHeight
      )

      Spacer(modifier = Modifier.weight(1f))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        ThemedOutlinedButton(
          text = strings.history,
        onClick = { onModeChosen("history") },
          modifier = Modifier.weight(1f)
        )
        ThemedFilledButton(
          text = strings.settings,
          onClick = onOpenSettings,
          modifier = Modifier.weight(1f)
        )
      }
      Spacer(modifier = Modifier.height(10.dp))
    }
  }
}

@Composable
private fun ModeCard(
  iconRes: Int,
  title: String,
  subtitle: String,
  onClick: () -> Unit,
  gradientIndex: Int,
  height: Dp
) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  
  ThemedCard(onClick = onClick, gradientIndex = gradientIndex, height = height) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Иконка слева - еще больше
      Image(
        painter = painterResource(iconRes),
        contentDescription = null,
        modifier = Modifier.size(88.dp),
        colorFilter = ColorFilter.tint(colors.primaryGlow.copy(alpha = 0.9f))
        )
      Spacer(modifier = Modifier.width(24.dp))
      
      Column(modifier = Modifier.weight(1f)) {
        TextWithShadow(
        text = title,
        style = MaterialTheme.typography.titleMedium,
          color = colors.textPrimary,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.height(6.dp))
        TextWithShadow(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium,
          color = colors.textSecondary
        )
      }
      TextWithShadow(
        text = "›",
        style = MaterialTheme.typography.titleLarge,
        color = colors.primaryGlow.copy(alpha = 0.7f),
        modifier = Modifier.padding(start = 8.dp)
      )
    }
  }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ModeSelectScreenPreview() {
  VoxeraTheme {
    ModeSelectScreen(
      onBack = {},
      onModeChosen = {}
    )
  }
}
