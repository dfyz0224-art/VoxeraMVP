package com.vanoprojects.voxera.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vanoprojects.voxera.data.PreferencesManager
import com.vanoprojects.voxera.ui.screens.*
import kotlinx.coroutines.launch

@Composable
fun VoxeraNavHost(
  navController: NavHostController = rememberNavController(),
  prefsManager: PreferencesManager,
  consentGiven: Boolean = false,
  onConsentGiven: suspend () -> Unit = {},
  onboardingCompleted: Boolean = false,
  onOnboardingComplete: () -> Unit = {}
) {
  val scope = rememberCoroutineScope()
  val startDestination = if (onboardingCompleted) Routes.Mode else Routes.Onboarding

  LaunchedEffect(onboardingCompleted) {
    if (onboardingCompleted && navController.currentBackStackEntry?.destination?.route == Routes.Onboarding) {
      navController.navigate(Routes.Mode) {
        popUpTo(Routes.Onboarding) { inclusive = true }
      }
    }
  }

  NavHost(navController = navController, startDestination = startDestination) {
    composable(Routes.Onboarding) {
      OnboardingScreen(
        onComplete = {
          navController.navigate(Routes.Mode) {
            popUpTo(Routes.Onboarding) { inclusive = true }
          }
        },
        prefsManager = prefsManager
      )
    }
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
        },
        onOpenSettings = { navController.navigate(Routes.Settings) }
      )
    }
    composable(Routes.Consent) {
      ConsentScreen(
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
      ShareScreen()
    }
    composable(Routes.History) {
      HistoryScreen()
    }
    composable(Routes.Settings) {
      SettingsScreen(
        onAbout = { navController.navigate(Routes.About) },
        onHelp = { navController.navigate(Routes.Help) },
        onForBusiness = { navController.navigate(Routes.ForBusiness) }
      )
    }
    composable(Routes.About) {
      AboutScreen()
    }
    composable(Routes.Help) {
      HelpScreen()
    }
    composable(Routes.ForBusiness) {
      ForBusinessScreen(
        onFillQuestionnaire = { navController.navigate(Routes.ForBusinessQuestionnaire) }
      )
    }
    composable(Routes.ForBusinessQuestionnaire) {
      ForBusinessQuestionnaireScreen(
        onSubmit = {
          navController.popBackStack(Routes.Settings, inclusive = false)
        }
      )
    }
  }
}
