package com.vanoprojects.voxera

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    val splashScreen = installSplashScreen()
    super.onCreate(savedInstanceState)
    setTheme(R.style.Theme_Voxera)
    WindowCompat.setDecorFitsSystemWindows(window, false)
    setContent { VoxeraApp() }
    splashScreen.setKeepOnScreenCondition { false }
    // Убираем затухание — сплеш исчезает мгновенно, визуально один экран с AppSplashScreen
    splashScreen.setOnExitAnimationListener { splashScreenView ->
      splashScreenView.remove()
    }
  }
}
