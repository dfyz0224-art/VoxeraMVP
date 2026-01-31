package com.vanoprojects.voxera

import android.content.Context
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.vanoprojects.voxera.data.PreferencesManager
import com.vanoprojects.voxera.ui.nav.VoxeraNavHost
import com.vanoprojects.voxera.ui.theme.VoxeraTheme

@Composable
fun VoxeraApp() {
  VoxeraTheme {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
      val context = LocalContext.current
      val prefsManager = remember { PreferencesManager(context) }
      val consentGiven by prefsManager.consentGiven.collectAsState(initial = false)
      val navController = rememberNavController()
      VoxeraNavHost(
        navController = navController,
        consentGiven = consentGiven,
        onConsentGiven = { prefsManager.setConsentGiven(true) }
      )
    }
  }
}
