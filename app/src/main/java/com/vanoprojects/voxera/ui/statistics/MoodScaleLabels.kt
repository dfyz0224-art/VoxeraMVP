package com.vanoprojects.voxera.ui.statistics

import com.vanoprojects.voxera.ui.strings.EmoScaleNames
import com.vanoprojects.voxera.ui.strings.Strings
import com.vanoprojects.voxera.ui.strings.resolveAppLanguage

/** Шесть шкал для графика статистики (порядок = цвета на графике). */
object MoodScaleLabels {
  val ORDER: List<String> = listOf(
    "emo_engage",
    "self_control",
    "stress_tolerance",
    "authority",
    "person_harmonicity",
    "energy_level"
  )

  fun label(key: String, strings: Strings): String =
    EmoScaleNames.translate(key, strings.resolveAppLanguage())
}
