package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanoprojects.voxera.ui.theme.ThemeColors
import com.vanoprojects.voxera.ui.theme.cardParagraphTextStyle

data class ResearchLink(
  val title: String,
  val url: String
)

/** Ссылки на публикации (англ. названия — как в DOI). */
val aboutResearchLinksList = listOf(
  ResearchLink(
    title = "Emotional Speech Recognition: Resources, Features, and Methods",
    url = "https://doi.org/10.1016/j.specom.2006.04.003"
  ),
  ResearchLink(
    title = "Speech Emotion Recognition Approaches: A Systematic Review (2023)",
    url = "https://doi.org/10.1016/j.specom.2023.102974"
  ),
  ResearchLink(
    title = "A Generalizable Speech Emotion Recognition Model Reveals Depression and Remission",
    url = "https://doi.org/10.1111/acps.13388"
  ),
  ResearchLink(
    title = "The Geneva Minimalistic Acoustic Parameter Set (GeMAPS)",
    url = "https://doi.org/10.1109/TAFFC.2015.2457417"
  ),
  ResearchLink(
    title = "Attention Guided Learnable Time-Domain Filterbanks for Speech Depression Detection",
    url = "https://doi.org/10.1016/j.neunet.2023.05.041"
  )
)

@Composable
fun AboutResearchLinksCard(colors: ThemeColors) {
  val uriHandler = LocalUriHandler.current
  val bodyStyle = cardParagraphTextStyle()
  val linkColor = colors.primaryGlow

  Column(modifier = Modifier.fillMaxWidth()) {
    aboutResearchLinksList.forEachIndexed { index, link ->
      if (index > 0) {
        Spacer(modifier = Modifier.height(18.dp))
      }
      Text(
        text = "• ${link.title}",
        color = colors.textSecondary,
        style = bodyStyle,
        modifier = Modifier
          .fillMaxWidth()
          .clickable { uriHandler.openUri(link.url) }
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = link.url,
        color = linkColor,
        style = bodyStyle.copy(
          fontSize = 16.sp,
          lineHeight = 24.sp,
          textDecoration = TextDecoration.Underline
        ),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { uriHandler.openUri(link.url) }
      )
    }
  }
}
