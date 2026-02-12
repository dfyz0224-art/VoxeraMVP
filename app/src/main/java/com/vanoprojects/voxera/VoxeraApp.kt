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
import com.vanoprojects.voxera.ui.theme.ThemeType
import com.vanoprojects.voxera.ui.theme.VoxeraTheme

@Composable
fun VoxeraApp() {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val prefsManager = remember { PreferencesManager(context) }
  val themeType by prefsManager.themeType.collectAsState(initial = ThemeType.GLASS)
  val consentGiven by prefsManager.consentGiven.collectAsState(initial = false)
  val onboardingCompleted by prefsManager.onboardingCompleted.collectAsState(initial = false)
  
  VoxeraTheme(themeType = themeType) {
    val backgroundColor = when (themeType) {
      ThemeType.LIGHT -> Color.Transparent // Прозрачный для светлой темы, чтобы был виден bg_light
      else -> Color.Black
    }
    Surface(modifier = Modifier.fillMaxSize(), color = backgroundColor) {
      val navController = rememberNavController()
      VoxeraNavHost(
        navController = navController,
        consentGiven = consentGiven,
        onConsentGiven = { prefsManager.setConsentGiven(true) },
        onboardingCompleted = onboardingCompleted,
        onOnboardingComplete = { scope.launch { prefsManager.setOnboardingCompleted(true) } }
      )
    }
  }
}
