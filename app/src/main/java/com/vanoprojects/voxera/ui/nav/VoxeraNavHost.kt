package com.vanoprojects.voxera.ui.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.vanoprojects.voxera.data.AnalysisSession
import com.vanoprojects.voxera.data.AnalysisUploadCoordinator
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
  var firebaseUid by remember {
    mutableStateOf(FirebaseAuth.getInstance().currentUser?.uid)
  }
  DisposableEffect(Unit) {
    val auth = FirebaseAuth.getInstance()
    val listener = FirebaseAuth.AuthStateListener { firebaseUid = it.currentUser?.uid }
    auth.addAuthStateListener(listener)
    onDispose { auth.removeAuthStateListener(listener) }
  }
  LaunchedEffect(firebaseUid) {
    historyRepository.setAccountKey(firebaseUid ?: HistoryRepository.GUEST_ACCOUNT_KEY)
  }
  val startDestination = remember(onboardingCompleted, authCompleted) {
    when (onboardingCompleted) {
      null -> null
      false -> Routes.Onboarding
      true -> if (!authCompleted) Routes.Auth else Routes.Mode
    }
  }

  LaunchedEffect(onboardingCompleted, authCompleted) {
    if (onboardingCompleted == null) return@LaunchedEffect
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    when {
      !onboardingCompleted && currentRoute != null && currentRoute != Routes.Onboarding ->
        navController.navigate(Routes.Onboarding) {
          popUpTo(navController.graph.id) { inclusive = true }
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
      onboardingCompleted && !authCompleted &&
        currentRoute != null &&
        currentRoute != Routes.Auth &&
        currentRoute != Routes.Onboarding &&
        currentRoute != Routes.PrivacyPolicy ->
        navController.navigate(Routes.Auth) {
          popUpTo(navController.graph.id) { inclusive = true }
        }
    }
  }

  if (startDestination == null) {
    Box(Modifier.fillMaxSize().background(Color.Black))
    return
  }

  NavHost(navController = navController, startDestination = startDestination) {
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
        onOpenSettings = { navController.navigate(Routes.Settings) },
        onAbout = { navController.navigate(Routes.About) },
        onHelp = { navController.navigate(Routes.Help) },
        onForBusiness = { navController.navigate(Routes.ForBusiness) }
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
        onGoProcessing = {
          AnalysisUploadCoordinator.reset()
          navController.navigate(Routes.Processing)
        }
      )
    }
    composable(Routes.Processing) {
      ProcessingScreen(
        historyRepository = historyRepository,
        onDone = {
          navController.navigate(Routes.Result) {
            // Drop Recording/Processing/Consent so system back returns to Mode.
            popUpTo(Routes.Mode) { inclusive = false }
          }
        }
      )
    }
    composable(Routes.Result) {
      ResultScreen(
        onHistory = { navController.navigate(Routes.History) },
        onGoHome = {
          navController.popBackStack(Routes.Mode, inclusive = false)
        }
      )
    }
    composable(Routes.History) {
      HistoryScreen(
        historyRepository = historyRepository,
        onItemClick = { entry ->
          AnalysisSession.lastAnalysisResponse = entry.toAnalysisResponse()
          AnalysisSession.analysisType = entry.analysisType
          AnalysisSession.lastResultJson = entry.responseJson
          AnalysisSession.lastRawApiResponse = entry.rawApiResponse
          navController.navigate(Routes.Result)
        }
      )
    }
    composable(Routes.Settings) {
      SettingsScreen(
        prefsManager = prefsManager,
        onPrivacyPolicy = { navController.navigate(Routes.PrivacyPolicy) },
        onProfile = { navController.navigate(Routes.Profile) },
        onSubscriptions = { navController.navigate(Routes.Subscriptions) }
      )
    }
    composable(Routes.Profile) {
      ProfileScreen(prefsManager = prefsManager)
    }
    composable(Routes.Subscriptions) {
      SubscriptionsScreen(
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
