package com.vanoprojects.voxera.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanoprojects.voxera.R
import com.vanoprojects.voxera.data.AnalysisSession
import com.vanoprojects.voxera.data.buildSharePlainText
import com.vanoprojects.voxera.data.sharePreviewLines
import com.vanoprojects.voxera.ui.strings.LocalStrings
import com.vanoprojects.voxera.ui.theme.*

@Composable
fun ShareScreen() {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val strings = LocalStrings.current
  val context = LocalContext.current

  val response = AnalysisSession.lastAnalysisResponse
  val analysisType = AnalysisSession.analysisType
  val (previewTitle, previewSubtitle) = sharePreviewLines(response, analysisType, strings)

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
      Spacer(modifier = Modifier.height(18.dp))
      val titleColor = if (theme.type == ThemeType.GLASS) {
        Color.White
      } else {
        colors.shadowColor
      }
      Text(
        text = strings.shareResult,
        color = titleColor,
        style = MaterialTheme.typography.headlineMedium.copy(
          fontSize = 42.sp,
          fontWeight = FontWeight.Light,
          lineHeight = 40.sp
        ),
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(min = 48.dp, max = 120.dp),
        textAlign = TextAlign.Center
      )
      Spacer(modifier = Modifier.height(32.dp))

      SharePreviewCard(
        previewTitle = previewTitle,
        previewSubtitle = previewSubtitle
      )

      Spacer(modifier = Modifier.weight(1f))

      ThemedFilledButton(
        text = strings.share,
        onClick = {
          val text = buildSharePlainText(
            response = response,
            analysisType = analysisType,
            briefOnly = false,
            strings = strings
          )
          if (text.isNullOrBlank()) {
            Toast.makeText(context, strings.shareNoData, Toast.LENGTH_SHORT).show()
          } else {
            val send = Intent(Intent.ACTION_SEND).apply {
              type = "text/plain"
              putExtra(Intent.EXTRA_TEXT, text)
              putExtra(Intent.EXTRA_SUBJECT, strings.shareSubject)
            }
            context.startActivity(Intent.createChooser(send, strings.share))
          }
        },
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(10.dp))
    }
  }
}

@Composable
private fun SharePreviewCard(
  previewTitle: String,
  previewSubtitle: String
) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors

  ThemedCard(
    modifier = Modifier.heightIn(min = 160.dp, max = 280.dp),
    gradientIndex = 0
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Image(
        painter = painterResource(R.drawable.ic_voxera_logo_text),
        contentDescription = null,
        modifier = Modifier.height(44.dp)
      )
      Spacer(modifier = Modifier.height(16.dp))
      TextWithShadow(
        text = previewTitle,
        color = colors.textSecondary,
        style = MaterialTheme.typography.bodyMedium
      )
      if (previewSubtitle.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        TextWithShadow(
          text = previewSubtitle,
          color = colors.textPrimary,
          style = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Center),
          fontWeight = FontWeight.Normal
        )
      }
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
