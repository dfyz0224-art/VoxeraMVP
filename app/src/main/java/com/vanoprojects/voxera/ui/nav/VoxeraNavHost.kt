package com.vanoprojects.voxera.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vanoprojects.voxera.data.AnalysisSession
import com.vanoprojects.voxera.data.HistoryRepository
import com.vanoprojects.voxera.data.PreferencesManager
import com.vanoprojects.voxera.ui.screens.*
import kotlinx.coroutines.launch

@Composable
fun VoxeraNavHost(
  navController: NavHostController = rememberNavController(),
  prefsManager: PreferencesManager,
  consentGiven: Boolean = false,
  onConsentGiven: suspend () -> Unit = {},
  onboardingCompleted: Boolean? = null,
  authCompleted: Boolean = false,
  onOnboardingComplete: () -> Unit = {}
) {
  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  val historyRepository = remember { HistoryRepository(context) }
  val startDestination = Routes.Splash
  var splashComplete by remember { mutableStateOf(false) }

  LaunchedEffect(splashComplete, onboardingCompleted, authCompleted) {
    if (!splashComplete || onboardingCompleted == null) return@LaunchedEffect
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    if (currentRoute != Routes.Splash) return@LaunchedEffect
    when {
      !onboardingCompleted -> navController.navigate(Routes.Onboarding) {
        popUpTo(Routes.Splash) { inclusive = true }
      }
      !authCompleted -> navController.navigate(Routes.Auth) {
        popUpTo(Routes.Splash) { inclusive = true }
      }
      else -> navController.navigate(Routes.Mode) {
        popUpTo(Routes.Splash) { inclusive = true }
      }
    }
  }

  LaunchedEffect(onboardingCompleted, authCompleted) {
    if (onboardingCompleted == null) return@LaunchedEffect
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    if (currentRoute == Routes.Splash) return@LaunchedEffect
    when {
      !onboardingCompleted && currentRoute == Routes.Mode ->
        navController.navigate(Routes.Onboarding) {
          popUpTo(Routes.Mode) { inclusive = true }
        }
      onboardingCompleted && !authCompleted && currentRoute == Routes.Mode ->
        navController.navigate(Routes.Auth) {
          popUpTo(Routes.Mode) { inclusive = true }
        }
      onboardingCompleted && authCompleted && currentRoute == Routes.Onboarding ->
        navController.navigate(Routes.Mode) {
          popUpTo(Routes.Onboarding) { inclusive = true }
        }
      onboardingCompleted && authCompleted && currentRoute == Routes.Auth ->
        navController.navigate(Routes.Mode) {
          popUpTo(Routes.Auth) { inclusive = true }
        }
    }
  }

  NavHost(navController = navController, startDestination = startDestination) {
    composable(Routes.Splash) {
      AppSplashScreen(
        onComplete = { splashComplete = true }
      )
    }
    composable(Routes.Onboarding) {
      OnboardingScreen(
        onComplete = {
          navController.navigate(Routes.Auth) {
            popUpTo(Routes.Onboarding) { inclusive = true }
          }
        },
        onOpenPrivacyPolicy = { navController.navigate(Routes.PrivacyPolicy) },
        prefsManager = prefsManager
      )
    }
    composable(Routes.Auth) {
      AuthScreen(
        onComplete = {
          navController.navigate(Routes.Mode) {
            popUpTo(Routes.Auth) { inclusive = true }
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
            AnalysisSession.analysisType = when (mode) {
              "quick" -> "psytype"
              else -> "emostate"
            }
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
        onOpenFullPrivacyPolicy = { navController.navigate(Routes.PrivacyPolicy) },
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
    composable(Routes.PrivacyPolicy) {
      PrivacyPolicyScreen(onBack = { navController.popBackStack() })
    }
    composable(Routes.Recording) {
      RecordingScreen(
        onGoProcessing = { navController.navigate(Routes.Processing) }
      )
    }
    composable(Routes.Processing) {
      ProcessingScreen(
        historyRepository = historyRepository,
        onDone = { navController.navigate(Routes.Result) { popUpTo(Routes.Recording) { inclusive = false } } }
      )
    }
    composable(Routes.Result) {
      ResultScreen(
        onNewAnalysis = { navController.navigate(Routes.Mode) { popUpTo(Routes.Result) { inclusive = true } } },
        onShare = { navController.navigate(Routes.Share) },
        onHistory = { navController.navigate(Routes.History) }
      )
    }
    composable(Routes.Share) {
      ShareScreen()
    }
    composable(Routes.History) {
      HistoryScreen(
        historyRepository = historyRepository,
        onOpenStatistics = { navController.navigate(Routes.Statistics) },
        onItemClick = { entry ->
          AnalysisSession.lastAnalysisResponse = entry.toAnalysisResponse()
          AnalysisSession.analysisType = entry.analysisType
          AnalysisSession.lastResultJson = entry.responseJson
          AnalysisSession.lastRawApiResponse = entry.rawApiResponse
          navController.navigate(Routes.Result)
        }
      )
    }
    composable(Routes.Statistics) {
      MoodStatisticsScreen(
        historyRepository = historyRepository,
        onBack = { navController.popBackStack() }
      )
    }
    composable(Routes.Settings) {
      SettingsScreen(
        prefsManager = prefsManager,
        onAbout = { navController.navigate(Routes.About) },
        onPrivacyPolicy = { navController.navigate(Routes.PrivacyPolicy) },
        onHelp = { navController.navigate(Routes.Help) },
        onForBusiness = { navController.navigate(Routes.ForBusiness) },
        onProfile = { navController.navigate(Routes.Profile) }
      )
    }
    composable(Routes.Profile) {
      ProfileScreen(
        prefsManager = prefsManager,
        onForBusiness = { navController.navigate(Routes.ForBusiness) }
      )
    }
    composable(Routes.About) {
      AboutScreen(
        onOpenFullDescription = { navController.navigate(Routes.AboutFullDescription) }
      )
    }
    composable(Routes.AboutFullDescription) {
      AboutFullDescriptionScreen(onBack = { navController.popBackStack() })
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
