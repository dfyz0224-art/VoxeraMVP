package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vanoprojects.voxera.R
import com.vanoprojects.voxera.data.PreferencesManager
import com.vanoprojects.voxera.ui.strings.AppLanguage
import com.vanoprojects.voxera.ui.strings.LocalStrings
import com.vanoprojects.voxera.ui.theme.*
import com.vanoprojects.voxera.ui.theme.TextWithShadow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
  onComplete: () -> Unit,
  onOpenPrivacyPolicy: () -> Unit,
  prefsManager: PreferencesManager
) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val strings = LocalStrings.current
  val currentLanguage by prefsManager.appLanguage.collectAsState(initial = AppLanguage.RU)
  val scope = rememberCoroutineScope()
  // Сохраняем шаг и галочки при переходе на политику и системном «Назад» (иначе step сбрасывался в 0)
  var step by rememberSaveable { mutableStateOf(0) }
  var agree1 by rememberSaveable { mutableStateOf(false) }
  var agree2 by rememberSaveable { mutableStateOf(false) }
  var showLanguageSheet by remember { mutableStateOf(false) }
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  // Фон как в ModeSelect
  Box(modifier = Modifier.fillMaxSize()) {
    when (theme.type) {
      ThemeType.LIGHT -> {
        Image(
          painter = painterResource(R.drawable.bg_light),
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
      }
      ThemeType.GLASS -> {
        Image(
          painter = painterResource(R.drawable.bg_stars),
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
      }
    }

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
      Spacer(modifier = Modifier.height(48.dp))

      // Логотип как в ModeSelect
      Box(
        modifier = Modifier
          .height(70.dp)
          .fillMaxWidth(),
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = painterResource(R.drawable.ic_voxera_logo_text),
          contentDescription = null,
          modifier = Modifier.fillMaxWidth(),
          contentScale = ContentScale.Fit
        )
      }

      Spacer(modifier = Modifier.height(28.dp))

      when (step) {
        0 -> {
          Column(
            modifier = Modifier
              .weight(1f)
              .fillMaxWidth()
              .verticalScroll(rememberScrollState())
          ) {
            ThemedCard(
              modifier = Modifier.wrapContentHeight(),
              gradientIndex = 0
            ) {
              Column(modifier = Modifier.fillMaxWidth()) {
                TextWithShadow(
                  text = strings.selectLanguage,
                  style = MaterialTheme.typography.titleMedium,
                  color = colors.textPrimary,
                  fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                val currentLangLabel = when (currentLanguage) {
                  AppLanguage.RU -> strings.languageRu
                  AppLanguage.EN -> strings.languageEn
                  AppLanguage.ZH -> strings.languageZh
                  AppLanguage.KZ -> strings.languageKz
                  AppLanguage.UK -> strings.languageUk
                  AppLanguage.KA -> strings.languageKa
                }
                OutlinedButton(
                  onClick = { showLanguageSheet = true },
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(12.dp),
                  colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colors.textPrimary,
                    containerColor = colors.buttonBackground.copy(alpha = 0.5f)
                  )
                ) {
                  Text(currentLangLabel)
                }
              }
            }
            Spacer(modifier = Modifier.height(16.dp))
            ThemedFilledButton(
              text = strings.next,
              onClick = { step = 1 },
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
        1 -> {
          ConsentAcceptanceBody(
            agree1 = agree1,
            agree2 = agree2,
            onAgree1 = { agree1 = it },
            onAgree2 = { agree2 = it },
            onOpenFullPrivacyPolicy = onOpenPrivacyPolicy,
            onAccept = {
              if (agree1 && agree2) {
                scope.launch {
                  prefsManager.setConsentGiven(true)
                  prefsManager.setOnboardingCompleted(true)
                  onComplete()
                }
              }
            },
            canContinue = agree1 && agree2,
            modifier = Modifier.weight(1f),
            useTextWithShadow = true
          )
        }
      }
    }
  }

  // Шторка выбора языка
  if (showLanguageSheet) {
    ModalBottomSheet(
      onDismissRequest = { showLanguageSheet = false },
      sheetState = sheetState
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp)
      ) {
        Text(
          text = strings.selectLanguage,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(16.dp))
        listOf(
          AppLanguage.RU to strings.languageRu,
          AppLanguage.EN to strings.languageEn,
          AppLanguage.ZH to strings.languageZh,
          AppLanguage.KZ to strings.languageKz,
          AppLanguage.UK to strings.languageUk,
          AppLanguage.KA to strings.languageKa
        ).forEach { (lang, label) ->
          TextButton(
            onClick = {
              scope.launch {
                prefsManager.setAppLanguage(lang)
                showLanguageSheet = false
              }
            },
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(label)
          }
        }
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}
