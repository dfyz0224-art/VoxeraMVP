package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

@Composable
fun ConsentScreen(
  onOpenFullPrivacyPolicy: () -> Unit,
  onAccept: () -> Unit
) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val strings = LocalStrings.current
  var agree1 by rememberSaveable { mutableStateOf(false) }
  var agree2 by rememberSaveable { mutableStateOf(false) }
  val canContinue = agree1 && agree2
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
        .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
      Spacer(modifier = Modifier.height(18.dp))
      Text(
        text = strings.privacyAndConsent,
        style = MaterialTheme.typography.headlineSmall,
        color = colors.backgroundTextPrimary,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.height(CardTextSpacing.AfterSectionTitle))

      ThemedCard(
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentHeight(),
        gradientIndex = 0,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp)
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = strings.consentCardSummary,
            style = cardParagraphTextStyle(),
            color = colors.textPrimary
          )
          Spacer(modifier = Modifier.height(18.dp))
          ConsentRow(
            checked = agree1,
            onChecked = { agree1 = it },
            text = strings.consentVoice,
            textColor = colors.textPrimary
          )
          Spacer(modifier = Modifier.height(CardTextSpacing.BetweenParagraphs))
          ConsentRow(
            checked = agree2,
            onChecked = { agree2 = it },
            text = strings.consentPrivacy,
            textColor = colors.textPrimary
          )
          Spacer(modifier = Modifier.height(12.dp))
          TextButton(
            onClick = onOpenFullPrivacyPolicy,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColors(contentColor = colors.textPrimary),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
          ) {
            Text(
              text = strings.consentOpenPrivacyPolicyButton,
              style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 13.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium
              ),
              textAlign = TextAlign.Center,
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      ThemedFilledButton(
        text = strings.start,
        onClick = onAccept,
        modifier = Modifier.fillMaxWidth(),
        enabled = canContinue
      )
      Spacer(modifier = Modifier.height(10.dp))
    }
  }
}

@Composable
private fun ConsentRow(
  checked: Boolean,
  onChecked: (Boolean) -> Unit,
  text: String,
  textColor: Color = LocalVoxeraTheme.current.colors.backgroundTextPrimary
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Checkbox(
      checked = checked,
      onCheckedChange = onChecked,
      colors = voxeraCheckboxColors()
    )
    Spacer(modifier = Modifier.width(12.dp))
    Text(
      text = text,
      color = textColor,
      style = cardParagraphTextStyle(),
      modifier = Modifier.weight(1f)
    )
  }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ConsentScreenPreview() {
  VoxeraTheme {
    ConsentScreen(
      onOpenFullPrivacyPolicy = {},
      onAccept = {}
    )
  }
}
