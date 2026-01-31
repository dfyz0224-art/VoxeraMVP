package com.vanoprojects.voxera.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vanoprojects.voxera.ui.screens.*
import kotlinx.coroutines.launch

@Composable
fun VoxeraNavHost(
  navController: NavHostController = rememberNavController(),
  consentGiven: Boolean = false,
  onConsentGiven: suspend () -> Unit = {}
) {
  val scope = rememberCoroutineScope()
  
  NavHost(navController = navController, startDestination = Routes.Mode) {
    composable(Routes.Mode) {
      ModeSelectScreen(
        onBack = { /* no-op */ },
        onModeChosen = { mode ->
          if (mode == "history") {
            navController.navigate(Routes.History)
          } else {
            // Если согласие уже дано, идём сразу на запись
            if (consentGiven) {
              navController.navigate(Routes.Recording)
            } else {
              navController.navigate(Routes.Consent)
            }
          }
      }
      )
    }
    composable(Routes.Consent) {
      ConsentScreen(
        onBack = { navController.popBackStack() },
        onAccept = {
          scope.launch {
            onConsentGiven()
            navController.navigate(Routes.Recording) {
              popUpTo(Routes.Mode) { inclusive = false }
            }
          }
        }
      )
    }
    composable(Routes.Recording) {
      RecordingScreen(
        onBack = { navController.popBackStack() },
        onGoProcessing = { navController.navigate(Routes.Processing) }
      )
    }
    composable(Routes.Processing) {
      ProcessingScreen(
        onDone = { navController.navigate(Routes.Result) { popUpTo(Routes.Recording) { inclusive = false } } }
      )
    }
    composable(Routes.Result) {
      ResultScreen(
        onNewAnalysis = { navController.navigate(Routes.Recording) { popUpTo(Routes.Result) { inclusive = true } } },
        onShare = { navController.navigate(Routes.Share) },
        onHistory = { navController.navigate(Routes.History) }
      )
    }
    composable(Routes.Share) {
      ShareScreen(
        onBack = { navController.popBackStack() }
      )
    }
    composable(Routes.History) {
      HistoryScreen(
        onBack = { navController.popBackStack() },
        onOpenSettings = { navController.navigate(Routes.Settings) }
      )
    }
    composable(Routes.Settings) {
      SettingsScreen(
        onBack = { navController.popBackStack() },
        onAbout = { navController.navigate(Routes.About) },
        onHelp = { navController.navigate(Routes.Help) }
      )
    }
    composable(Routes.About) {
      AboutScreen(onBack = { navController.popBackStack() })
    }
    composable(Routes.Help) {
      HelpScreen(onBack = { navController.popBackStack() })
    }
  }
}
