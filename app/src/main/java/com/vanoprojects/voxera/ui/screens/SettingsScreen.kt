package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
fun SettingsScreen(
  onAbout: () -> Unit,
  onHelp: () -> Unit,
  onForBusiness: () -> Unit
) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val strings = LocalStrings.current
  val context = LocalContext.current
  val prefsManager = remember { PreferencesManager(context) }
  val currentTheme by prefsManager.themeType.collectAsState(initial = ThemeType.GLASS)
  val currentLanguage by prefsManager.appLanguage.collectAsState(initial = AppLanguage.RU)
  val scope = rememberCoroutineScope()
  
  var keepHistory by remember { mutableStateOf(true) }
  var shareMinimal by remember { mutableStateOf(true) }
  var showLanguageSheet by remember { mutableStateOf(false) }
  val languageSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  // Фон: для светлой темы - белый, для остальных - VoxeraBackground
  Box(modifier = Modifier.fillMaxSize()) {
    if (theme.type == ThemeType.LIGHT) {
      Image(
        painter = painterResource(R.drawable.bg_light),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )
    } else {
      VoxeraBackground {}
    }
    
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(20.dp)
    ) {
      Spacer(modifier = Modifier.height(10.dp))

      SettingCard(
        title = strings.keepHistory,
        subtitle = strings.keepHistorySubtitle,
        checked = keepHistory,
        onChecked = { keepHistory = it },
        gradientIndex = 0
      )
      Spacer(modifier = Modifier.height(16.dp))
      SettingCard(
        title = strings.shareBriefOnly,
        subtitle = strings.shareBriefSubtitle,
        checked = shareMinimal,
        onChecked = { shareMinimal = it },
        gradientIndex = 1
      )

      Spacer(modifier = Modifier.height(16.dp))
      ThemeSelectorCard(
        currentTheme = currentTheme,
        onThemeSelected = { themeType ->
          scope.launch {
            prefsManager.setThemeType(themeType)
          }
        }
      )

      Spacer(modifier = Modifier.height(16.dp))
      LanguageSelectorCard(
        currentLanguage = currentLanguage,
        strings = strings,
        onOpenSheet = { showLanguageSheet = true }
      )

      Spacer(modifier = Modifier.height(18.dp))
      ThemedOutlinedButton(
        text = strings.about,
        onClick = onAbout,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(8.dp))
      ThemedOutlinedButton(
        text = strings.help,
        onClick = onHelp,
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(8.dp))
      ThemedOutlinedButton(
        text = strings.forBusiness,
        onClick = onForBusiness,
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(24.dp))
    }

    if (showLanguageSheet) {
      ModalBottomSheet(
        onDismissRequest = { showLanguageSheet = false },
        sheetState = languageSheetState
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
            AppLanguage.KZ to strings.languageKz
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
}

@Composable
private fun SettingCard(
  title: String,
  subtitle: String,
  checked: Boolean,
  onChecked: (Boolean) -> Unit,
  gradientIndex: Int
) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  
  ThemedCard(gradientIndex = gradientIndex) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        TextWithShadow(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          color = colors.textPrimary,
          fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        TextWithShadow(
          text = subtitle,
          style = MaterialTheme.typography.bodyMedium,
          color = colors.textSecondary
        )
      }
      Switch(
        checked = checked,
        onCheckedChange = onChecked
      )
    }
  }
}

@Composable
private fun ThemeSelectorCard(
  currentTheme: ThemeType,
  onThemeSelected: (ThemeType) -> Unit
) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val strings = LocalStrings.current
  
  ThemedCard(modifier = Modifier.height(240.dp), gradientIndex = 2) {
    Column(
      modifier = Modifier.fillMaxSize()
    ) {
      TextWithShadow(
        text = strings.themeTitle,
        style = MaterialTheme.typography.titleMedium,
        color = colors.textPrimary,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.height(16.dp))
      
      ThemeOption(
        title = strings.themeGlass,
        themeType = ThemeType.GLASS,
        isSelected = currentTheme == ThemeType.GLASS,
        onClick = { onThemeSelected(ThemeType.GLASS) }
      )
      Spacer(modifier = Modifier.height(16.dp))
      ThemeOption(
        title = strings.themeLight,
        themeType = ThemeType.LIGHT,
        isSelected = currentTheme == ThemeType.LIGHT,
        onClick = { onThemeSelected(ThemeType.LIGHT) }
      )
      Spacer(modifier = Modifier.height(16.dp))
      ThemeOption(
        title = strings.themeDark,
        themeType = ThemeType.DARK,
        isSelected = currentTheme == ThemeType.DARK,
        onClick = { onThemeSelected(ThemeType.DARK) }
      )
    }
  }
}

@Composable
private fun LanguageSelectorCard(
  currentLanguage: AppLanguage,
  strings: com.vanoprojects.voxera.ui.strings.Strings,
  onOpenSheet: () -> Unit
) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors

  val currentLangLabel = when (currentLanguage) {
    AppLanguage.RU -> strings.languageRu
    AppLanguage.EN -> strings.languageEn
    AppLanguage.ZH -> strings.languageZh
    AppLanguage.KZ -> strings.languageKz
  }

  ThemedCard(gradientIndex = 3) {
    Column(modifier = Modifier.fillMaxWidth()) {
      TextWithShadow(
        text = strings.language,
        style = MaterialTheme.typography.titleMedium,
        color = colors.textPrimary,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.height(16.dp))
      OutlinedButton(
        onClick = onOpenSheet,
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
}

@Composable
private fun ThemeOption(
  title: String,
  themeType: ThemeType,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(48.dp)
      .clickable(onClick = onClick),
    verticalAlignment = Alignment.CenterVertically
  ) {
    RadioButton(
      selected = isSelected,
      onClick = onClick,
      colors = RadioButtonDefaults.colors(
        selectedColor = colors.primaryGlow
      )
    )
    Spacer(modifier = Modifier.width(8.dp))
    TextWithShadow(
      text = title,
      color = colors.textPrimary,
      style = MaterialTheme.typography.bodyMedium
    )
  }
}
