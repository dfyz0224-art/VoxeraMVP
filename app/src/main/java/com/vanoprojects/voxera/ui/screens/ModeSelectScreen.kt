package com.vanoprojects.voxera.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
  onOpenSettings: () -> Unit = {},
  onAbout: () -> Unit = {},
  onHelp: () -> Unit = {},
  onForBusiness: () -> Unit = {}
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

    BoxWithConstraints(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
      // Keep cards + 5 buttons on one screen without scroll on short devices.
      val buttonBlockApprox = 188.dp
      val topChromeApprox = 108.dp // logo + title + spacers
      val gapCardsToButtons = 14.dp
      val cardGap = 10.dp
      val freeForCards = (maxHeight - buttonBlockApprox - topChromeApprox - gapCardsToButtons)
        .coerceAtLeast(220.dp)
      val modeCardHeight = ((freeForCards - cardGap) / 2f).coerceIn(112.dp, 150.dp)
      val logoHeight = if (maxHeight < 680.dp) 52.dp else 64.dp
      val topSpacer = if (maxHeight < 680.dp) 12.dp else 28.dp

      Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(topSpacer))

        Box(
          modifier = Modifier
            .height(logoHeight)
            .fillMaxWidth(),
          contentAlignment = Alignment.Center
        ) {
          Image(
            painter = painterResource(R.drawable.ic_voxera_logo_text),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Fit
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(10.dp))

        ModeCard(
          iconRes = R.drawable.universal_2,
          label = strings.universalMode,
          onClick = { onModeChosen("teen") },
          gradientIndex = 0,
          height = modeCardHeight
        )
        Spacer(modifier = Modifier.height(cardGap))
        ModeCard(
          iconRes = R.drawable.deep_2,
          label = strings.deepAnalysis,
          onClick = { onModeChosen("quick") },
          gradientIndex = 1,
          height = modeCardHeight
        )

        Spacer(modifier = Modifier.weight(1f, fill = true))
        Spacer(modifier = Modifier.height(gapCardsToButtons))

        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            ThemedOutlinedButton(
              text = strings.about,
              onClick = onAbout,
              modifier = Modifier.weight(1f)
            )
            ThemedFilledButton(
              text = strings.settings,
              onClick = onOpenSettings,
              modifier = Modifier.weight(1f)
            )
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            ThemedOutlinedButton(
              text = strings.help,
              onClick = onHelp,
              modifier = Modifier.weight(1f)
            )
            ThemedOutlinedButton(
              text = strings.forBusiness,
              onClick = onForBusiness,
              modifier = Modifier.weight(1f)
            )
          }
          ThemedOutlinedButton(
            text = strings.history,
            onClick = { onModeChosen("history") },
            modifier = Modifier.fillMaxWidth()
          )
        }
        Spacer(modifier = Modifier.height(12.dp))
      }
    }
  }
}

private val ModeCardCorner = 16.dp

@Composable
private fun ModeCard(
  iconRes: Int,
  label: String,
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

  val iconSize = if (height < 130.dp) 72.dp else 88.dp
  val fontSize = if (height < 130.dp) 15.sp else 17.sp
  val lineHeight = if (height < 130.dp) 20.sp else 24.sp

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
      contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .fillMaxHeight(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier.size(iconSize),
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
        Spacer(modifier = Modifier.width(14.dp))
        Text(
          text = label,
          style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = fontSize,
            lineHeight = lineHeight,
            fontWeight = FontWeight.SemiBold
          ),
          color = colors.textPrimary,
          modifier = Modifier.weight(1f)
        )
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
