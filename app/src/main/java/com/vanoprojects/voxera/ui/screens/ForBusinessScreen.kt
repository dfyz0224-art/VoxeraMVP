package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.vanoprojects.voxera.R
import com.vanoprojects.voxera.ui.strings.LocalStrings
import com.vanoprojects.voxera.ui.theme.*

@Composable
fun ForBusinessScreen(
  onFillQuestionnaire: () -> Unit
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
        .padding(20.dp)
    ) {
      Spacer(modifier = Modifier.height(10.dp))

      ThemedFilledButton(
        text = strings.fillQuestionnaire,
        onClick = onFillQuestionnaire,
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(16.dp))

      ThemedCard(
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentHeight(),
        gradientIndex = 1,
        contentPadding = PaddingValues(horizontal = 30.dp, vertical = 26.dp)
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = strings.forBusinessIntro,
            color = colors.textPrimary,
            style = cardParagraphTextStyle()
          )
          Spacer(modifier = Modifier.height(CardTextSpacing.BetweenParagraphs))
          Text(
            text = "• ${strings.forBusinessBullet1}",
            color = colors.textPrimary,
            style = cardParagraphTextStyle()
          )
          Text(
            text = "• ${strings.forBusinessBullet2}",
            color = colors.textPrimary,
            style = cardParagraphTextStyle()
          )
          Text(
            text = "• ${strings.forBusinessBullet3}",
            color = colors.textPrimary,
            style = cardParagraphTextStyle()
          )
          Text(
            text = "• ${strings.forBusinessBullet4}",
            color = colors.textPrimary,
            style = cardParagraphTextStyle()
          )
          Text(
            text = "• ${strings.forBusinessBullet5}",
            color = colors.textPrimary,
            style = cardParagraphTextStyle()
          )
          Text(
            text = "• ${strings.forBusinessBullet6}",
            color = colors.textPrimary,
            style = cardParagraphTextStyle()
          )
          Spacer(modifier = Modifier.height(CardTextSpacing.BetweenParagraphs))
          Text(
            text = strings.forBusinessOutro,
            color = colors.textPrimary,
            style = cardParagraphTextStyle()
          )
        }
      }
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
