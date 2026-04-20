package com.vanoprojects.voxera.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// Design Tokens from ТЗ (оставляем для обратной совместимости)
object VoxeraColors {
    val Background = Color(0xFF000000) // Основной фон
    val CardBackground = Color(0xFF070A0F) // Фон карточек
    val PrimaryGlow = Color(0xFFFFFFFF) // Холодное свечение
    val TextPrimary = Color(0xFFEAF6FF) // Основной текст
    val TextSecondary = Color(0xFF93A4B5) // Вторичный текст
    val Warning = Color(0xFFFFCC66) // Мягкое предупреждение
    val Success = Color(0xFF7DFFC2) // Успех
}

data class VoxeraTheme(
    val type: ThemeType,
    val colors: ThemeColors
)

val LocalVoxeraTheme = compositionLocalOf<VoxeraTheme> {
    error("No VoxeraTheme provided")
}

private val DarkScheme = darkColorScheme(
  primary = VoxeraColors.PrimaryGlow,
  secondary = VoxeraColors.PrimaryGlow,
  background = VoxeraColors.Background,
  surface = VoxeraColors.CardBackground,
  onPrimary = Color.White,
  onSecondary = Color.White,
  onBackground = VoxeraColors.TextPrimary,
  onSurface = VoxeraColors.TextPrimary
)

private val LightScheme = lightColorScheme(
  primary = Color(0xFF2196F3),
  secondary = Color(0xFF2196F3),
  background = Color(0xFFFFFFFF),
  surface = Color(0xFFE3F2FD),
  onPrimary = Color.White,
  onSecondary = Color.White,
  onBackground = Color(0xFF1E3A5F),
  onSurface = Color(0xFF1E3A5F)
)

@Composable
fun VoxeraTheme(
    themeType: ThemeType = ThemeType.GLASS,
  content: @Composable () -> Unit
) {
    val colors = when (themeType) {
        ThemeType.LIGHT -> ThemeColors.Light
        ThemeType.GLASS -> ThemeColors.Glass
    }
    
    val theme = VoxeraTheme(themeType, colors)
    val colorScheme = when (themeType) {
        ThemeType.LIGHT -> LightScheme
        ThemeType.GLASS -> DarkScheme // Glass: тёмная Material-схема
    }
    
    CompositionLocalProvider(LocalVoxeraTheme provides theme) {
  MaterialTheme(
            colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
    }
}
