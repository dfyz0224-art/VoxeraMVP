package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanoprojects.voxera.R
import com.vanoprojects.voxera.ui.strings.LocalStrings
import com.vanoprojects.voxera.ui.theme.*

@Composable
fun AboutScreen(onOpenFullDescription: () -> Unit = {}) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val strings = LocalStrings.current
  val scroll = rememberScrollState()
  val lang = remember(strings) { stringsToAppLanguage(strings) }
  val shortSections = remember(lang) { aboutShortSections(lang) }

  val titleOnBackground = when (theme.type) {
    ThemeType.LIGHT -> Color(0xFF0D1B3A)
    ThemeType.GLASS -> colors.backgroundTextPrimary
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
        .padding(horizontal = 22.dp)
        .padding(top = 16.dp, bottom = 28.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Image(
        painter = painterResource(R.drawable.ic_x_white),
        contentDescription = null,
        modifier = Modifier
          .size(88.dp)
          .padding(bottom = 12.dp),
        colorFilter = when (theme.type) {
          ThemeType.LIGHT -> ColorFilter.tint(colors.backgroundTextPrimary)
          else -> null
        }
      )

      ThemedOutlinedButton(
        text = strings.aboutFullDescriptionButton,
        onClick = onOpenFullDescription,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = strings.aboutBriefSectionTitle,
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.SemiBold,
          fontSize = 18.sp
        ),
        color = titleOnBackground,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(12.dp))

      ThemedCard(
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentHeight(),
        gradientIndex = 0,
        contentPadding = PaddingValues(horizontal = 30.dp, vertical = 26.dp)
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          shortSections.forEachIndexed { index, section ->
            if (index > 0) {
              Spacer(modifier = Modifier.height(CardTextSpacing.BetweenSections))
            }
            AboutSectionTitle(section.title, colors)
            Spacer(modifier = Modifier.height(CardTextSpacing.AfterSectionTitle))
            section.blocks.forEachIndexed { bi, block ->
              if (bi > 0) {
                val prev = section.blocks[bi - 1]
                val gap = when {
                  block is AboutBlock.Bullets && prev is AboutBlock.Paragraph -> 10.dp
                  block is AboutBlock.Paragraph && prev is AboutBlock.Bullets ->
                    CardTextSpacing.BetweenParagraphs
                  else -> CardTextSpacing.BetweenParagraphs
                }
                Spacer(modifier = Modifier.height(gap))
              }
              when (block) {
                is AboutBlock.Paragraph -> AboutCardParagraph(block.text, colors)
                is AboutBlock.Bullets -> block.lines.forEach { line ->
                  AboutCardBulletLine(line, colors)
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      Text(
        text = strings.aboutResearchLinksTitle,
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.SemiBold,
          fontSize = 18.sp
        ),
        color = titleOnBackground,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(12.dp))

      ThemedCard(
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentHeight(),
        gradientIndex = 0,
        contentPadding = PaddingValues(horizontal = 30.dp, vertical = 26.dp)
      ) {
        AboutResearchLinksCard(colors = colors)
      }
    }
  }
}

@Composable
private fun AboutSectionTitle(text: String, colors: ThemeColors) {
  Text(
    text = text,
    color = colors.textPrimary,
    style = cardSectionTitleOnCardStyle(),
    modifier = Modifier.fillMaxWidth()
  )
}

@Composable
private fun AboutCardParagraph(text: String, colors: ThemeColors) {
  Text(
    text = text,
    color = colors.textSecondary,
    style = cardParagraphTextStyle(),
    modifier = Modifier.fillMaxWidth()
  )
}

@Composable
private fun AboutCardBulletLine(text: String, colors: ThemeColors) {
  Text(
    text = text,
    color = colors.textSecondary,
    style = cardParagraphTextStyle(),
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp)
  )
}
