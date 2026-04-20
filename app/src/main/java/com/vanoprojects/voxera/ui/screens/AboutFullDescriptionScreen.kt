package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.vanoprojects.voxera.ui.theme.LocalVoxeraTheme
import com.vanoprojects.voxera.ui.theme.ThemeType
import com.vanoprojects.voxera.ui.theme.ThemedCard
import com.vanoprojects.voxera.ui.theme.ThemedFilledButton
import com.vanoprojects.voxera.ui.theme.cardParagraphTextStyle
import com.vanoprojects.voxera.ui.theme.cardSectionTitleOnCardStyle

@Composable
fun AboutFullDescriptionScreen(onBack: () -> Unit) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val strings = LocalStrings.current
  val lang = remember(strings) { stringsToAppLanguage(strings) }
  val slides = remember(lang) { aboutPresentationSlides(lang) }

  var page by rememberSaveable { mutableIntStateOf(0) }
  val lastIndex = (slides.size - 1).coerceAtLeast(0)
  val safePage = page.coerceIn(0, lastIndex)
  val slide = slides[safePage]
  val contentScroll = rememberScrollState()

  LaunchedEffect(lastIndex) {
    if (page > lastIndex) page = lastIndex
  }

  LaunchedEffect(safePage) {
    contentScroll.scrollTo(0)
  }

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
        .padding(horizontal = 22.dp)
        .padding(top = 12.dp, bottom = 20.dp)
    ) {
      Image(
        painter = painterResource(R.drawable.ic_x_white),
        contentDescription = null,
        modifier = Modifier
          .size(72.dp)
          .align(Alignment.CenterHorizontally)
          .clickable(onClick = onBack),
        colorFilter = when (theme.type) {
          ThemeType.LIGHT -> ColorFilter.tint(colors.backgroundTextPrimary)
          else -> null
        }
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = strings.aboutFullDescriptionButton,
        style = MaterialTheme.typography.titleLarge.copy(
          fontWeight = FontWeight.SemiBold,
          fontSize = 22.sp
        ),
        color = titleOnBackground,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(16.dp))

      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(contentScroll)
        ) {
          ThemedCard(
            modifier = Modifier
              .fillMaxWidth()
              .wrapContentHeight(),
            gradientIndex = 0,
            contentPadding = PaddingValues(horizontal = 26.dp, vertical = 22.dp)
          ) {
            Column(modifier = Modifier.fillMaxWidth()) {
              Text(
                text = slide.title,
                color = colors.textPrimary,
                style = cardSectionTitleOnCardStyle().copy(textAlign = TextAlign.Center),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
              )
              Spacer(modifier = Modifier.height(14.dp))
              Text(
                text = slide.body,
                color = colors.textSecondary,
                style = cardParagraphTextStyle(),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        slides.indices.forEach { i ->
          Box(
            modifier = Modifier
              .padding(horizontal = 4.dp)
              .size(8.dp)
              .clip(CircleShape)
              .background(
                if (i == safePage) colors.primaryGlow
                else colors.textSecondary.copy(alpha = 0.35f)
              )
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      ThemedFilledButton(
        text = if (safePage < lastIndex) {
          strings.aboutPresentationNext
        } else {
          strings.aboutPresentationDone
        },
        onClick = {
          if (safePage < lastIndex) {
            page = safePage + 1
          } else {
            onBack()
          }
        },
        modifier = Modifier.fillMaxWidth()
      )
    }
  }
}
