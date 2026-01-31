package com.vanoprojects.voxera.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Design Tokens from ТЗ
object VoxeraColors {
    val Background = Color(0xFF000000) // Основной фон
    val CardBackground = Color(0xFF070A0F) // Фон карточек
    val PrimaryGlow = Color(0xFFFFFFFF) // Холодное свечение
    val TextPrimary = Color(0xFFEAF6FF) // Основной текст
    val TextSecondary = Color(0xFF93A4B5) // Вторичный текст
    val Warning = Color(0xFFFFCC66) // Мягкое предупреждение
    val Success = Color(0xFF7DFFC2) // Успех
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

@Composable
fun VoxeraTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = DarkScheme,
    typography = Typography,
    content = content
  )
}
