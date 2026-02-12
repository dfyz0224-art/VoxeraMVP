package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.Canvas
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
import com.vanoprojects.voxera.ui.theme.*
import com.vanoprojects.voxera.ui.theme.TextWithShadow
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
  onAbout: () -> Unit,
  onHelp: () -> Unit
) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val context = LocalContext.current
  val prefsManager = remember { PreferencesManager(context) }
  val currentTheme by prefsManager.themeType.collectAsState(initial = ThemeType.GLASS)
  val scope = rememberCoroutineScope()
  
  var keepHistory by remember { mutableStateOf(true) }
  var shareMinimal by remember { mutableStateOf(true) }

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
        .padding(20.dp)
    ) {
      Spacer(modifier = Modifier.height(10.dp))
      TopBar(title = "Настройки")
      Spacer(modifier = Modifier.height(16.dp))

      SettingCard(
        title = "Сохранять историю",
        subtitle = "Хранить результаты локально на устройстве",
        checked = keepHistory,
        onChecked = { keepHistory = it },
        gradientIndex = 0
      )
      Spacer(modifier = Modifier.height(16.dp))
      SettingCard(
        title = "Делиться только кратко",
        subtitle = "Без деталей и чувствительных данных",
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

      Spacer(modifier = Modifier.height(18.dp))
      ThemedOutlinedButton(
        text = "О приложении",
        onClick = onAbout,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(8.dp))
      ThemedOutlinedButton(
        text = "Помощь",
        onClick = onHelp,
        modifier = Modifier.fillMaxWidth()
      )
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
  
  ThemedCard(modifier = Modifier.height(240.dp), gradientIndex = 2) {
    Column(
      modifier = Modifier.fillMaxSize()
    ) {
      TextWithShadow(
        text = "Тема оформления",
        style = MaterialTheme.typography.titleMedium,
        color = colors.textPrimary,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.height(16.dp))
      
      ThemeOption(
        title = "Стеклянная",
        themeType = ThemeType.GLASS,
        isSelected = currentTheme == ThemeType.GLASS,
        onClick = { onThemeSelected(ThemeType.GLASS) }
      )
      Spacer(modifier = Modifier.height(16.dp))
      ThemeOption(
        title = "Светлая",
        themeType = ThemeType.LIGHT,
        isSelected = currentTheme == ThemeType.LIGHT,
        onClick = { onThemeSelected(ThemeType.LIGHT) }
      )
      Spacer(modifier = Modifier.height(16.dp))
      ThemeOption(
        title = "Темная",
        themeType = ThemeType.DARK,
        isSelected = currentTheme == ThemeType.DARK,
        onClick = { onThemeSelected(ThemeType.DARK) }
      )
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
