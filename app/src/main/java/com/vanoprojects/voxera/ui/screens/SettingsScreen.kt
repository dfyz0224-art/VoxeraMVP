package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
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
  prefsManager: PreferencesManager,
  onAbout: () -> Unit,
  onPrivacyPolicy: () -> Unit,
  onHelp: () -> Unit,
  onForBusiness: () -> Unit,
  onProfile: () -> Unit = {},
  onSubscriptions: () -> Unit = {}
) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val strings = LocalStrings.current
  val scope = rememberCoroutineScope()
  val currentTheme by prefsManager.themeType.collectAsState(initial = ThemeType.GLASS)
  val currentLanguage by prefsManager.appLanguage.collectAsState(initial = AppLanguage.RU)
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

      ProfileCard(
        prefsManager = prefsManager,
        onClick = onProfile
      )
      Spacer(modifier = Modifier.height(16.dp))
      SubscriptionsNavCard(onClick = onSubscriptions)
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
        text = strings.privacyPolicyShortLink,
        onClick = onPrivacyPolicy,
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
private fun ProfileCard(
  prefsManager: PreferencesManager,
  onClick: () -> Unit
) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val strings = LocalStrings.current
  val firebaseUser = FirebaseAuth.getInstance().currentUser
  val profilePhotoPath by prefsManager.profilePhotoPath.collectAsState(initial = null)
  val displayName = firebaseUser?.displayName
    ?: firebaseUser?.email?.substringBefore("@")
    ?: strings.profileGuestName
  val hasCustomPhoto = !profilePhotoPath.isNullOrBlank()
  val photoUrl = when {
    hasCustomPhoto -> "file://$profilePhotoPath"
    firebaseUser?.photoUrl != null -> firebaseUser.photoUrl!!.toString()
    else -> null
  }

  ThemedCard(
    gradientIndex = 0,
    height = 100.dp,
    onClick = onClick
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (photoUrl != null) {
        AsyncImage(
          model = photoUrl,
          contentDescription = null,
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
        )
      } else {
        Image(
          painter = painterResource(R.drawable.ic_profile),
          contentDescription = null,
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape),
          colorFilter = ColorFilter.tint(colors.textPrimary)
        )
      }
      Spacer(modifier = Modifier.width(20.dp))
      Column(modifier = Modifier.weight(1f)) {
        TextWithShadow(
          text = strings.profile,
          style = MaterialTheme.typography.titleMedium,
          color = colors.textPrimary,
          fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        TextWithShadow(
          text = displayName,
          style = cardParagraphTextStyle(),
          color = colors.textSecondary
        )
      }
    }
  }
}

@Composable
private fun SubscriptionsNavCard(onClick: () -> Unit) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val strings = LocalStrings.current

  ThemedCard(
    gradientIndex = 1,
    height = 72.dp,
    onClick = onClick
  ) {
    Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.CenterStart
    ) {
      TextWithShadow(
        text = strings.manageSubscriptions,
        style = MaterialTheme.typography.titleMedium,
        color = colors.textPrimary,
        fontWeight = FontWeight.SemiBold
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
  
  ThemedCard(modifier = Modifier.height(200.dp), gradientIndex = 2) {
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
      style = cardParagraphTextStyle(),
      modifier = Modifier.weight(1f)
    )
  }
}
