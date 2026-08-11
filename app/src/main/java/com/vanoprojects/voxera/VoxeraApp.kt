package com.vanoprojects.voxera

import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.vanoprojects.voxera.data.PreferencesManager
import com.vanoprojects.voxera.data.isAllowedIntoApp
import com.vanoprojects.voxera.ui.nav.VoxeraNavHost
import com.vanoprojects.voxera.ui.strings.AppLanguage
import com.vanoprojects.voxera.ui.strings.LocalStrings
import com.vanoprojects.voxera.ui.strings.Strings
import com.vanoprojects.voxera.ui.theme.ThemeType
import com.vanoprojects.voxera.ui.theme.VoxeraTheme

@Composable
fun VoxeraApp() {
  val context = LocalContext.current
  val prefsManager = remember { PreferencesManager(context) }
  val themeType by prefsManager.themeType.collectAsState(initial = ThemeType.GLASS)
  val appLanguage by prefsManager.appLanguage.collectAsState(initial = AppLanguage.RU)
  val consentGiven by prefsManager.consentGiven.collectAsState(initial = false)
  val onboardingCompleted by prefsManager.onboardingCompleted.collectAsState(initial = null)
  val authCompletedByPrefs by prefsManager.authCompleted.collectAsState(initial = null)
  var firebaseUser by remember {
    mutableStateOf(FirebaseAuth.getInstance().currentUser)
  }
  DisposableEffect(Unit) {
    val auth = FirebaseAuth.getInstance()
    val listener = FirebaseAuth.AuthStateListener { firebaseUser = it.currentUser }
    auth.addAuthStateListener(listener)
    onDispose { auth.removeAuthStateListener(listener) }
  }
  // Guest: prefs only. Signed-in email/password: must be verified.
  val currentUser = firebaseUser
  val authCompleted = when {
    currentUser != null -> currentUser.isAllowedIntoApp()
    authCompletedByPrefs == true -> true
    else -> false
  }
  val strings = when (appLanguage) {
    AppLanguage.RU -> Strings.Ru
    AppLanguage.EN -> Strings.En
    AppLanguage.ZH -> Strings.Zh
    AppLanguage.KZ -> Strings.Kz
    AppLanguage.UK -> Strings.Uk
    AppLanguage.KA -> Strings.Ka
  }

  VoxeraTheme(themeType = themeType) {
    androidx.compose.runtime.CompositionLocalProvider(LocalStrings provides strings) {
      val backgroundColor = when (themeType) {
        ThemeType.LIGHT -> Color.Transparent
        else -> Color.Black
      }
      Surface(
        modifier = Modifier
          .fillMaxSize()
          .windowInsetsPadding(WindowInsets.navigationBars),
        color = backgroundColor
      ) {
        val navController = rememberNavController()
        VoxeraNavHost(
          navController = navController,
          prefsManager = prefsManager,
          consentGiven = consentGiven,
          onConsentGiven = { prefsManager.setConsentGiven(true) },
          onboardingCompleted = onboardingCompleted,
          authCompleted = authCompleted
        )
      }
    }
  }
}
