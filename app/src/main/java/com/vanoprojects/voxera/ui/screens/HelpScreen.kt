package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.vanoprojects.voxera.R
import com.vanoprojects.voxera.ui.strings.LocalStrings
import com.vanoprojects.voxera.ui.theme.*

@Composable
fun HelpScreen() {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val strings = LocalStrings.current
  val scroll = rememberScrollState()

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
        .padding(horizontal = 22.dp)
        .padding(top = 16.dp, bottom = 28.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Image(
        painter = painterResource(R.drawable.ic_x_white),
        contentDescription = null,
        modifier = Modifier
          .size(72.dp)
          .padding(bottom = 8.dp),
        colorFilter = when (theme.type) {
          ThemeType.LIGHT -> ColorFilter.tint(colors.backgroundTextPrimary)
          else -> null
        }
      )

      Text(
        text = strings.helpTitle,
        color = colors.backgroundTextPrimary,
        style = MaterialTheme.typography.headlineSmall.copy(
          fontWeight = FontWeight.SemiBold,
          lineHeight = 28.sp
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = strings.helpSubtitle,
        color = colors.backgroundTextSecondary,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontSize = 17.sp,
          lineHeight = 26.sp,
          letterSpacing = 0.01.em
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(20.dp))

      ThemedCard(
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentHeight(),
        gradientIndex = 0,
        contentPadding = PaddingValues(horizontal = 30.dp, vertical = 26.dp)
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          HelpSectionTitle(strings.helpQuickStartTitle, colors)
          Spacer(modifier = Modifier.height(CardTextSpacing.AfterSectionTitle))
          HelpCardParagraph(strings.helpQuickStartBody, colors)

          Spacer(modifier = Modifier.height(CardTextSpacing.BetweenSections))
          HelpSectionTitle(strings.helpRecordingTitle, colors)
          Spacer(modifier = Modifier.height(CardTextSpacing.AfterSectionTitle))
          HelpCardParagraph(strings.helpRecordingBody, colors)

          Spacer(modifier = Modifier.height(CardTextSpacing.BetweenSections))
          HelpSectionTitle(strings.helpModesTitle, colors)
          Spacer(modifier = Modifier.height(CardTextSpacing.AfterSectionTitle))
          HelpCardParagraph(strings.helpModesBody, colors)

          Spacer(modifier = Modifier.height(CardTextSpacing.BetweenSections))
          HelpSectionTitle(strings.helpResultsTitle, colors)
          Spacer(modifier = Modifier.height(CardTextSpacing.AfterSectionTitle))
          HelpCardParagraph(strings.helpResultsBody, colors)

          Spacer(modifier = Modifier.height(CardTextSpacing.BetweenSections))
          HelpSectionTitle(strings.helpPrivacyTitle, colors)
          Spacer(modifier = Modifier.height(CardTextSpacing.AfterSectionTitle))
          HelpCardParagraph(strings.helpPrivacyBody, colors)

          Spacer(modifier = Modifier.height(CardTextSpacing.BetweenSections))
          HelpSectionTitle(strings.helpTroubleshootTitle, colors)
          Spacer(modifier = Modifier.height(CardTextSpacing.AfterSectionTitle))
          HelpCardParagraph(strings.helpTroubleshootBody, colors)
        }
      }
    }
  }
}

@Composable
private fun HelpSectionTitle(text: String, colors: ThemeColors) {
  Text(
    text = text,
    color = colors.textPrimary,
    style = cardSectionTitleOnCardStyle(),
    modifier = Modifier.fillMaxWidth()
  )
}

@Composable
private fun HelpCardParagraph(text: String, colors: ThemeColors) {
  Text(
    text = text,
    color = colors.textSecondary,
    style = cardParagraphTextStyle(),
    modifier = Modifier.fillMaxWidth()
  )
}
