package com.vanoprojects.voxera.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
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
      }

      Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
      // Весовой Spacer между карточками и кнопками на узких экранах схлопывается в 0.
      // Верх — в scroll; низ — фиксированный отступ + кнопки, чтобы зазор не исчезал.
      val scroll = rememberScrollState()
      Column(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .verticalScroll(scroll)
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
        val modeCardHeight = 162.dp
        ModeCard(
          iconRes = R.drawable.parent_2,
          title = strings.parentMode,
          subtitle = strings.parentModeSubtitle,
          onClick = { onModeChosen("mom") },
          gradientIndex = 0,
          height = modeCardHeight
        )
        Spacer(modifier = Modifier.height(16.dp))
        ModeCard(
          iconRes = R.drawable.universal_2,
          title = strings.universalMode,
          subtitle = strings.universalModeSubtitle,
          onClick = { onModeChosen("teen") },
          gradientIndex = 1,
          height = modeCardHeight
        )
        Spacer(modifier = Modifier.height(16.dp))
        ModeCard(
          iconRes = R.drawable.deep_2,
          title = strings.deepAnalysis,
          subtitle = strings.deepAnalysisSubtitle,
          onClick = { onModeChosen("quick") },
          gradientIndex = 2,
          height = modeCardHeight
        )

        Spacer(modifier = Modifier.height(24.dp))
      }

      Spacer(modifier = Modifier.height(20.dp))
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
      Spacer(modifier = Modifier.height(35.dp))
    }
  }
}

private val ModeCardCorner = 16.dp

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
  val density = LocalDensity.current
  val interactionSource = remember { MutableInteractionSource() }
  val pressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (pressed) 0.97f else 1f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessHigh
    ),
    label = "modeCardScale"
  )
  val pressOffset by animateDpAsState(
    targetValue = if (pressed) 2.dp else 0.dp,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioNoBouncy,
      stiffness = Spring.StiffnessHigh
    ),
    label = "modeCardNudge"
  )

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(height)
      .graphicsLayer {
        scaleX = scale
        scaleY = scale
        translationY = with(density) { pressOffset.toPx() }
      }
      .clip(RoundedCornerShape(ModeCardCorner))
      .clickable(
        interactionSource = interactionSource,
        indication = ripple(
          bounded = true,
          color = colors.primaryGlow.copy(alpha = 0.22f)
        ),
        onClick = onClick
      )
  ) {
    ThemedCard(
      onClick = null,
      gradientIndex = gradientIndex,
      height = height,
      contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .fillMaxHeight(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier.size(88.dp),
          contentAlignment = Alignment.Center
        ) {
          Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(Color.White)
          )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.Center
        ) {
          Text(
            text = title,
            style = cardSectionTitleOnCardStyle().copy(
              fontSize = 20.sp,
              lineHeight = 26.sp,
              fontWeight = FontWeight.Bold
            ),
            color = colors.textPrimary,
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium.copy(
              fontSize = 16.sp,
              lineHeight = 24.sp,
              fontWeight = FontWeight.Normal
            ),
            color = colors.textSecondary.copy(alpha = 0.92f),
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
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
