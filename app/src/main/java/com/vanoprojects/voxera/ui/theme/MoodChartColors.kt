package com.vanoprojects.voxera.ui.theme

import androidx.compose.ui.graphics.Color

/** 6 линий: хорошо различимы на тёмной (glass) и светлой теме. */
object MoodChartColors {
  val glass: List<Color> = listOf(
    Color(0xFFFF5C8A),
    Color(0xFF64B5F6),
    Color(0xFF81C784),
    Color(0xFFFFB74D),
    Color(0xFFCE93D8),
    Color(0xFF4DD0E1)
  )
  val light: List<Color> = listOf(
    Color(0xFFC2185B),
    Color(0xFF1565C0),
    Color(0xFF2E7D32),
    Color(0xFFE65100),
    Color(0xFF6A1B9A),
    Color(0xFF00695C)
  )
}
