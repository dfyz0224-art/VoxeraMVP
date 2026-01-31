package com.vanoprojects.voxera

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    // Устанавливаем splash screen ПЕРЕД super.onCreate() - это критично!
    val splashScreen = installSplashScreen()
    
    super.onCreate(savedInstanceState)
    
    // Устанавливаем тему приложения
    setTheme(R.style.Theme_Voxera)
    
    WindowCompat.setDecorFitsSystemWindows(window, false)
    
    setContent {
      VoxeraApp()
    }
    
    // Убираем splash screen сразу после загрузки UI
    // Устанавливаем условие, чтобы splash убрался сразу
    splashScreen.setKeepOnScreenCondition { false }
  }
}
