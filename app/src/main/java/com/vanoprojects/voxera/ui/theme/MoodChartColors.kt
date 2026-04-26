package com.vanoprojects.voxera.ui.theme

import androidx.compose.ui.graphics.Color

/** 6 линий: хорошо различимы на тёмной (glass) и светлой теме. */
object MoodChartColors {
  val glass: List<Color> = listOf(
    Color(0xFFFF6E9A),
    Color(0xFF7EC8FF),
    Color(0xFF9AE69C),
    Color(0xFFFFC870),
    Color(0xFFE0B0F5),
    Color(0xFF6DEEFF)
  )
  val light: List<Color> = listOf(
    Color(0xFFD81F6A),
    Color(0xFF1C72D8),
    Color(0xFF3A9D42),
    Color(0xFFFF7018),
    Color(0xFF7E2FC4),
    Color(0xFF0B8578)
  )
}
