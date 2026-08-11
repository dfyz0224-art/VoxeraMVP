package com.vanoprojects.voxera.ui.screens

import com.vanoprojects.voxera.ui.strings.AppLanguage
import com.vanoprojects.voxera.ui.strings.Strings
import com.vanoprojects.voxera.ui.strings.resolveAppLanguage

/**
 * Краткие пояснения психотипов для кнопки (i) на экране результата.
 * Ключ — имя типа из API в нижнем регистре.
 */
internal fun psytypeHintTitleBody(typeKey: String, strings: Strings): Pair<String, String> {
  val display = typeKey.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
  val lang = strings.resolveAppLanguage()
  val body = psytypeHintBody(typeKey.lowercase(), lang)
  return display to body
}

private fun psytypeHintBody(key: String, lang: AppLanguage): String {
  val ru = mapOf(
    "realizer" to "Ориентирован на результат и доведение дел до конца. Практичен, последователен, ценит эффективность и конкретный эффект от действий.",
    "leader" to "Склонен вести за собой, брать ответственность и задавать направление. Уверенно принимает решения и влияет на окружение.",
    "manager" to "Организует процессы, распределяет задачи и держит контроль. Сильная сторона — структура, планирование и исполнительская дисциплина.",
    "visionary" to "Смотрит вперёд, генерирует идеи и видит возможности. Силён в стратегии и образе будущего, меньше — в рутинной деталировке.",
    "creator" to "Проявляет креативность, гибкость и поиск нестандартных решений. Легко включается в новое и меняет подходы.",
    "communicator" to "Легко устанавливает контакт, убеждает и поддерживает диалог. Сильная сторона — общение, эмпатия и влияние через речь.",
    "stabilizer" to "Ценит устойчивость, предсказуемость и баланс. Сглаживает конфликты, поддерживает спокойный ритм и надёжность.",
    "expert" to "Глубоко погружается в тему, опирается на знания и точность. Силён в анализе, качестве и экспертной оценке."
  )
  val en = mapOf(
    "realizer" to "Focused on results and finishing what was started. Practical, consistent, values efficiency and tangible outcomes.",
    "leader" to "Tends to lead, take responsibility, and set direction. Confident in decisions and influence on others.",
    "manager" to "Organizes processes, assigns tasks, and keeps control. Strengths: structure, planning, and delivery discipline.",
    "visionary" to "Looks ahead, generates ideas, and sees opportunities. Strong in strategy and future vision; less in routine detail.",
    "creator" to "Shows creativity, flexibility, and unconventional solutions. Quickly engages with the new and changes approaches.",
    "communicator" to "Easily builds rapport, persuades, and sustains dialogue. Strengths: conversation, empathy, and influence through speech.",
    "stabilizer" to "Values stability, predictability, and balance. Softens conflict and supports a calm, reliable pace.",
    "expert" to "Goes deep into the subject, relies on knowledge and precision. Strong in analysis, quality, and expert judgment."
  )
  val fallbackRu = "Краткое описание этого психотипа пока недоступно."
  val fallbackEn = "A short description for this psychotype is not available yet."
  return when (lang) {
    AppLanguage.RU, AppLanguage.UK -> ru[key] ?: fallbackRu
    AppLanguage.EN, AppLanguage.ZH, AppLanguage.KZ, AppLanguage.KA ->
      en[key] ?: fallbackEn
  }
}
