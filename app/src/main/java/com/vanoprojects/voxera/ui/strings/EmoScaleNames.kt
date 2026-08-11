package com.vanoprojects.voxera.ui.strings

/** Localized display names for emo_scales API keys (descriptions stay server-side). */
object EmoScaleNames {

  fun translate(name: String, language: AppLanguage): String {
    val key = name.lowercase().trim()
    val fromKey = mapFor(language)[key]
    if (fromKey != null) return fromKey
    // API may already return a Russian label — map via RU reverse lookup, then re-translate.
    val canonical = RU.entries.firstOrNull { it.value.equals(name, ignoreCase = true) }?.key
    if (canonical != null) {
      return mapFor(language)[canonical] ?: EN[canonical] ?: name
    }
    return mapFor(language)[key] ?: EN[key] ?: name
  }

  fun translate(name: String, strings: Strings): String =
    translate(name, strings.resolveAppLanguage())

  private fun mapFor(language: AppLanguage): Map<String, String> = when (language) {
    AppLanguage.RU -> RU
    AppLanguage.EN -> EN
    AppLanguage.ZH -> ZH
    AppLanguage.KZ -> KZ
    AppLanguage.UK -> UK
    AppLanguage.KA -> KA
  }

  private val RU = mapOf(
    "ability_to_attract" to "Притягательность",
    "expressivity" to "Экспрессивность",
    "authority" to "Властность",
    "person_manifestation" to "Демонстративность",
    "kindness" to "Дружелюбие",
    "self_control" to "Самоконтроль",
    "openness_to_new" to "Открытость к опыту",
    "energy_level" to "Жизнерадостность",
    "emo_engage" to "Вдохновенность",
    "ability_to_set_goals" to "Реализованность",
    "ability_to_assert" to "Независимость",
    "person_harmonicity" to "Уравновешенность",
    "emotional_confidence" to "Эмоциональность",
    "stress_tolerance" to "Стрессоустойчивость"
  )

  private val EN = mapOf(
    "ability_to_attract" to "Attractiveness",
    "expressivity" to "Expressiveness",
    "authority" to "Dominance",
    "person_manifestation" to "Showmanship",
    "kindness" to "Friendliness",
    "self_control" to "Self-control",
    "openness_to_new" to "Openness to experience",
    "energy_level" to "Cheerfulness",
    "emo_engage" to "Inspiration",
    "ability_to_set_goals" to "Goal fulfillment",
    "ability_to_assert" to "Independence",
    "person_harmonicity" to "Balance",
    "emotional_confidence" to "Emotionality",
    "stress_tolerance" to "Stress resistance"
  )

  private val ZH = mapOf(
    "ability_to_attract" to "吸引力",
    "expressivity" to "表现力",
    "authority" to "主导性",
    "person_manifestation" to "表现欲",
    "kindness" to "友善",
    "self_control" to "自我控制",
    "openness_to_new" to "开放性",
    "energy_level" to "生活热情",
    "emo_engage" to "灵感动机",
    "ability_to_set_goals" to "目标实现感",
    "ability_to_assert" to "独立性",
    "person_harmonicity" to "心理平衡",
    "emotional_confidence" to "情绪性",
    "stress_tolerance" to "抗压能力"
  )

  private val KZ = mapOf(
    "ability_to_attract" to "Тартымдылық",
    "expressivity" to "Экспрессивтілік",
    "authority" to "Басқарушылық",
    "person_manifestation" to "Демонстративтілік",
    "kindness" to "Достық",
    "self_control" to "Өзін-өзі басқару",
    "openness_to_new" to "Жаңалыққа ашықтық",
    "energy_level" to "Өмір қуанышы",
    "emo_engage" to "Шабыттылық",
    "ability_to_set_goals" to "Мақсатқа жету",
    "ability_to_assert" to "Тәуелсіздік",
    "person_harmonicity" to "Тұрақтылық",
    "emotional_confidence" to "Эмоционалдылық",
    "stress_tolerance" to "Стреске төзімділік"
  )

  private val UK = mapOf(
    "ability_to_attract" to "Привабливість",
    "expressivity" to "Експресивність",
    "authority" to "Владність",
    "person_manifestation" to "Демонстративність",
    "kindness" to "Дружелюбність",
    "self_control" to "Самоконтроль",
    "openness_to_new" to "Відкритість до досвіду",
    "energy_level" to "Життєрадісність",
    "emo_engage" to "Натхненність",
    "ability_to_set_goals" to "Реалізованість",
    "ability_to_assert" to "Незалежність",
    "person_harmonicity" to "Врівноваженість",
    "emotional_confidence" to "Емоційність",
    "stress_tolerance" to "Стресостійкість"
  )

  private val KA = mapOf(
    "ability_to_attract" to "მიმზიდველობა",
    "expressivity" to "ექსპრესიულობა",
    "authority" to "დომინანტობა",
    "person_manifestation" to "დემონსტრატიულობა",
    "kindness" to "კეთილგანწყობა",
    "self_control" to "თვითკონტროლი",
    "openness_to_new" to "გახსნილობა გამოცდილებისადმი",
    "energy_level" to "სიცოცხლისუნარიანობა",
    "emo_engage" to "შთაგონება",
    "ability_to_set_goals" to "მიზნების მიღწევა",
    "ability_to_assert" to "დამოუკიდებლობა",
    "person_harmonicity" to "წონასწორობა",
    "emotional_confidence" to "ემოციურობა",
    "stress_tolerance" to "სტრესგამძლეობა"
  )
}

fun Strings.resolveAppLanguage(): AppLanguage = when (this) {
  Strings.Ru -> AppLanguage.RU
  Strings.En -> AppLanguage.EN
  Strings.Zh -> AppLanguage.ZH
  Strings.Kz -> AppLanguage.KZ
  Strings.Uk -> AppLanguage.UK
  Strings.Ka -> AppLanguage.KA
  else -> AppLanguage.RU
}
