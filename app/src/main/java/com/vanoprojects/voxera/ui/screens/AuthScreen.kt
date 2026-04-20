package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import com.vanoprojects.voxera.R
import com.vanoprojects.voxera.data.PreferencesManager
import com.vanoprojects.voxera.ui.strings.LocalStrings
import com.vanoprojects.voxera.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
  onComplete: () -> Unit,
  prefsManager: PreferencesManager
) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val strings = LocalStrings.current
  val scope = rememberCoroutineScope()

  fun onSkip() {
    scope.launch {
      prefsManager.setAuthCompleted(true)
      onComplete()
    }
  }

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
        .verticalScroll(rememberScrollState())
    ) {
      Spacer(modifier = Modifier.height(48.dp))

      Box(
        modifier = Modifier
          .height(70.dp)
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

      Spacer(modifier = Modifier.height(28.dp))

      val titleColor = if (theme.type == ThemeType.LIGHT) {
        androidx.compose.ui.graphics.Color.White
      } else {
        colors.backgroundTextPrimary
      }
      Text(
        text = strings.authTitle,
        style = MaterialTheme.typography.headlineSmall,
        color = titleColor,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(24.dp))

      ThemedCard(
        modifier = Modifier.wrapContentHeight(),
        gradientIndex = 0
      ) {
        AuthCardContent(
          prefsManager = prefsManager,
          onAuthComplete = onComplete,
          onSkip = { onSkip() },
          showSkipButton = true
        )
      }

      Spacer(modifier = Modifier.weight(1f))
    }
  }
}

@Composable
private fun GoogleSignInButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  OutlinedButton(
    onClick = onClick,
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    colors = ButtonDefaults.outlinedButtonColors(
      contentColor = colors.buttonText,
      containerColor = when (theme.type) {
        ThemeType.LIGHT -> colors.buttonBackground.copy(alpha = 0.5f)
        else -> colors.buttonBackground
      }
    ),
    border = BorderStroke(
      width = 1.dp,
      color = if (theme.type == ThemeType.LIGHT)
        androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.08f)
      else colors.buttonBorder
    )
  ) {
    Image(
      painter = painterResource(R.drawable.ic_google),
      contentDescription = null,
      modifier = Modifier.size(20.dp)
    )
    Spacer(modifier = Modifier.width(12.dp))
    Text(text = text, style = MaterialTheme.typography.bodyLarge)
  }
}
