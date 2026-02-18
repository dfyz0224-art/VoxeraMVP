package com.vanoprojects.voxera

import android.content.Context
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.vanoprojects.voxera.data.PreferencesManager
import com.vanoprojects.voxera.ui.nav.VoxeraNavHost
import com.vanoprojects.voxera.ui.strings.AppLanguage
import com.vanoprojects.voxera.ui.strings.LocalStrings
import com.vanoprojects.voxera.ui.strings.Strings
import com.vanoprojects.voxera.ui.theme.ThemeType
import com.vanoprojects.voxera.ui.theme.VoxeraTheme

@Composable
fun VoxeraApp() {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val prefsManager = remember { PreferencesManager(context) }
  val themeType by prefsManager.themeType.collectAsState(initial = ThemeType.LIGHT)
  val appLanguage by prefsManager.appLanguage.collectAsState(initial = AppLanguage.RU)
  val consentGiven by prefsManager.consentGiven.collectAsState(initial = false)
  val onboardingCompleted by prefsManager.onboardingCompleted.collectAsState(initial = true)
  val strings = when (appLanguage) {
    AppLanguage.RU -> Strings.Ru
    AppLanguage.EN -> Strings.En
    AppLanguage.ZH -> Strings.Zh
    AppLanguage.KZ -> Strings.Kz
  }

  VoxeraTheme(themeType = themeType) {
    androidx.compose.runtime.CompositionLocalProvider(LocalStrings provides strings) {
      val backgroundColor = when (themeType) {
        ThemeType.LIGHT -> Color.Transparent
        else -> Color.Black
      }
      Surface(modifier = Modifier.fillMaxSize(), color = backgroundColor) {
        val navController = rememberNavController()
        VoxeraNavHost(
          navController = navController,
          prefsManager = prefsManager,
          consentGiven = consentGiven,
          onConsentGiven = { prefsManager.setConsentGiven(true) },
          onboardingCompleted = onboardingCompleted
        )
      }
    }
  }
}
