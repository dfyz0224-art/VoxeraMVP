package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vanoprojects.voxera.R
import com.vanoprojects.voxera.ui.strings.LocalStrings
import com.vanoprojects.voxera.ui.theme.*

@Composable
fun ConsentScreen(
  onAccept: () -> Unit
) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val strings = LocalStrings.current
  var agree1 by remember { mutableStateOf(false) }
  var agree2 by remember { mutableStateOf(false) }

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
        .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
      Spacer(modifier = Modifier.height(18.dp))
      Text(
        text = strings.privacyAndConsent,
        style = MaterialTheme.typography.headlineSmall,
        color = colors.backgroundTextPrimary,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = strings.consentDescription,
        style = MaterialTheme.typography.bodyMedium,
        color = colors.backgroundTextSecondary,
      )
      Spacer(modifier = Modifier.height(24.dp))

      ConsentRow(
        checked = agree1,
        onChecked = { agree1 = it },
        text = strings.consentVoice
      )
      Spacer(modifier = Modifier.height(12.dp))
      ConsentRow(
        checked = agree2,
        onChecked = { agree2 = it },
        text = strings.consentPrivacy
      )

      Spacer(modifier = Modifier.weight(1f))

      ThemedFilledButton(
        text = strings.start,
          onClick = onAccept,
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(10.dp))
    }
  }
}

@Composable
private fun ConsentRow(checked: Boolean, onChecked: (Boolean) -> Unit, text: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    val theme = LocalVoxeraTheme.current
    val colors = theme.colors
    
    Checkbox(
      checked = checked,
      onCheckedChange = onChecked,
      colors = CheckboxDefaults.colors(
        checkedColor = colors.primaryGlow
      )
    )
    Spacer(modifier = Modifier.width(12.dp))
    Text(
      text = text,
      color = colors.backgroundTextPrimary,
      style = MaterialTheme.typography.bodyMedium,
      modifier = Modifier.weight(1f)
    )
  }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ConsentScreenPreview() {
  VoxeraTheme {
    ConsentScreen(
      onAccept = {}
    )
  }
}
