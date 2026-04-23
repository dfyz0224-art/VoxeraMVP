package com.vanoprojects.voxera.ui.statistics

import com.vanoprojects.voxera.ui.strings.AppLanguage
import com.vanoprojects.voxera.ui.strings.Strings

/** Шесть шкал для графика статистики (порядок = цвета на графике). */
object MoodScaleLabels {
  val ORDER: List<String> = listOf(
    "emo_engage",
    "self_control",
    "stress_tolerance",
    "authority",
    "person_harmonicity",
    "expressivity"
  )

  fun label(key: String, strings: Strings): String {
    val lang = strings.resolveAppLanguageForMood()
    val k = key.lowercase()
    return when (lang) {
      AppLanguage.RU -> RU[k] ?: key
      AppLanguage.EN -> EN[k] ?: key
      AppLanguage.ZH -> ZH[k] ?: EN[k] ?: key
      AppLanguage.KZ -> KZ[k] ?: EN[k] ?: key
    }
  }

  private val RU: Map<String, String> = mapOf(
    "emo_engage" to "Вдохновенность",
    "self_control" to "Самоконтроль",
    "stress_tolerance" to "Стрессоустойчивость",
    "authority" to "Властность",
    "person_harmonicity" to "Уравновешенность",
    "expressivity" to "Экспрессивность"
  )
  private val EN: Map<String, String> = mapOf(
    "emo_engage" to "Inspiration",
    "self_control" to "Self-control",
    "stress_tolerance" to "Stress resistance",
    "authority" to "Dominance",
    "person_harmonicity" to "Balance",
    "expressivity" to "Expressiveness"
  )
  private val ZH: Map<String, String> = mapOf(
    "emo_engage" to "灵感动机",
    "self_control" to "自我控制",
    "stress_tolerance" to "抗压能力",
    "authority" to "主导性",
    "person_harmonicity" to "心理平衡",
    "expressivity" to "表达力"
  )
  private val KZ: Map<String, String> = mapOf(
    "emo_engage" to "Шабыттылық",
    "self_control" to "Өзін-өзі басқару",
    "stress_tolerance" to "Стреске төзімділік",
    "authority" to "Басқарушылық",
    "person_harmonicity" to "Тұрақтылық",
    "expressivity" to "Еркіндік"
  )
}

private fun Strings.resolveAppLanguageForMood(): AppLanguage = when (this) {
  Strings.Ru -> AppLanguage.RU
  Strings.En -> AppLanguage.EN
  Strings.Zh -> AppLanguage.ZH
  Strings.Kz -> AppLanguage.KZ
  else -> AppLanguage.RU
}
