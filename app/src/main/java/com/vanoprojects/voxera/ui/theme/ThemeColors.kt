package com.vanoprojects.voxera.ui.theme

import androidx.compose.ui.graphics.Color

sealed class ThemeColors {
    abstract val background: Color
    abstract val cardBackground: Color
    abstract val cardBackgroundGradient: List<Color>
    abstract val primaryGlow: Color
    abstract val textPrimary: Color // Текст на карточках
    abstract val textSecondary: Color // Текст на карточках
    abstract val backgroundTextPrimary: Color // Текст вне карточек
    abstract val backgroundTextSecondary: Color // Текст вне карточек
    abstract val buttonBackground: Color
    abstract val buttonBorder: Color
    abstract val buttonText: Color
    abstract val shadowColor: Color
    abstract val buttonFilledBackground: Color
    
    object Light : ThemeColors() {
        override val background = Color(0xFFF5F3F8) // Светло-фиолетовый/лавандовый фон (чуть темнее белого)
        override val cardBackground = Color(0xFF1E3A5F) // Темно-синий
        override val cardBackgroundGradient = listOf(
            Color(0xFF0D1B3A), // Темно-синий (верх градиента - темнее)
            Color(0xFF2E4A7A)  // Светло-синий (низ градиента - светлее)
        )
        override val primaryGlow = Color(0xFFFFFFFF) // Белый для иконок и акцентов
        override val textPrimary = Color(0xFFFFFFFF) // Белый текст на карточках
        override val textSecondary = Color(0xFFFFFFFF).copy(alpha = 0.9f) // Белый текст на карточках с небольшой прозрачностью
        override val backgroundTextPrimary = Color(0xFF0A1628) // Очень темно-синий/почти черный для контраста на светло-синем фоне
        override val backgroundTextSecondary = Color(0xFF1A2F4A) // Темно-синий для вторичного текста
        override val buttonBackground = Color(0xFF5B8EC9) // Приглушённый синий для outlined (История, Поделиться)
        override val buttonBorder = Color(0xFF4A7AB8) // Для совместимости (в LIGHT используется subtle border)
        override val buttonText = Color(0xFFFFFFFF) // Белый текст
        override val shadowColor = Color.Black // Для теней карточек
        override val buttonFilledBackground = Color(0xFF2E5F9E) // Тёмно-синий для filled (Настройки, Новый анализ)
    }
    
    object Glass : ThemeColors() {
        override val background = Color(0xFF000000)
        override val cardBackground = Color.White.copy(alpha = 0.1f) // Полупрозрачный
        override val cardBackgroundGradient = listOf(
            Color.White.copy(alpha = 0.15f),
            Color.White.copy(alpha = 0.05f)
        )
        override val primaryGlow = Color(0xFFFFFFFF)
        override val textPrimary = Color(0xFFFFFFFF)
        override val textSecondary = Color(0xFFFFFFFF).copy(alpha = 0.7f)
        override val backgroundTextPrimary = Color(0xFFFFFFFF) // Текст на фоне
        override val backgroundTextSecondary = Color(0xFFFFFFFF).copy(alpha = 0.7f) // Текст на фоне
        override val buttonBackground = Color.White.copy(alpha = 0.1f)
        override val buttonBorder = Color.White.copy(alpha = 0.3f)
        override val buttonText = Color(0xFFFFFFFF)
        override val shadowColor = Color.Black.copy(alpha = 0.3f)
        override val buttonFilledBackground = Color.White.copy(alpha = 0.2f)
    }
}
