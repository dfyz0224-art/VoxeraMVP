package com.vanoprojects.voxera.ui.screens

import com.vanoprojects.voxera.ui.strings.AppLanguage
import com.vanoprojects.voxera.ui.strings.Strings
import com.vanoprojects.voxera.ui.strings.resolveAppLanguage

/**
 * Текст для шторки подсказки по шкале (ключ API в нижнем регистре). Заголовок не используется — только тело.
 */
internal fun emostateMetricHintTitleBody(scaleKey: String, strings: Strings): Pair<String, String> {
  val lang = strings.resolveAppLanguage()
  val body = metricHintBody(scaleKey.lowercase(), lang, strings)
  return "" to body
}

private fun metricHintBody(key: String, lang: AppLanguage, strings: Strings): String {
  val ru = mapOf(
    "emo_engage" to """
      Вдохновенность (Inspiration Index)

      Что измеряет:
       • уровень внутренней энергии
       • мотивацию к действию
       • наличие интереса и вовлечённости

      Сигналы в голосе:
       • вариативность интонации
       • динамика темпа
       • эмоциональная окраска

      Интерпретация:
       • 70–100 → высокая мотивация, драйв
       • 40–70 → нейтральное состояние
       • <40 → апатия, снижение интереса
    """.trimIndent(),
    "authority" to """
      Властность (Dominance Index)

      Что измеряет:
       • уверенность
       • склонность к контролю и лидерству
       • способность принимать решения

      Сигналы:
       • стабильность голоса
       • низкая дрожь
       • уверенный тембр

      Интерпретация:
       • 70–100 → лидер, доминирование
       • 40–70 → сбалансированное поведение
       • <40 → неуверенность, подчинённость
    """.trimIndent(),
    "stress_tolerance" to """
      Стрессоустойчивость (Stress Resistance)

      Что измеряет:
       • способность сохранять стабильность под нагрузкой
       • реакцию на давление

      Сигналы:
       • микродрожание голоса
       • сбои ритма
       • скачки громкости

      Интерпретация:
       • 70–100 → устойчив к стрессу
       • 40–70 → средняя устойчивость
       • <40 → высокая реактивность, риск срывов
    """.trimIndent(),
    "self_control" to """
      Самоконтроль (Self-Control)

      Что измеряет:
       • управление эмоциями
       • импульсивность
       • способность держать поведение в рамках

      Сигналы:
       • паузы перед ответом
       • контроль темпа речи
       • отсутствие резких скачков

      Интерпретация:
       • 70–100 → высокий контроль
       • 40–70 → допустимый уровень
       • <40 → импульсивность, риск агрессии
    """.trimIndent(),
    "person_harmonicity" to """
      Уравновешенность (Emotional Balance)

      Что измеряет:
       • общее психоэмоциональное равновесие
       • стабильность состояния

      Сигналы:
       • ровность интонации
       • отсутствие резких перепадов
       • гармоничность речи

      Интерпретация:
       • 70–100 → стабильная психика
       • 40–70 → допустимое колебание
       • <40 → нестабильность, внутреннее напряжение
    """.trimIndent()
  )
  val en = mapOf(
    "emo_engage" to """
      Inspiration (Inspiration Index)

      What it measures:
       • inner energy level
       • motivation to act
       • interest and engagement

      Voice signals:
       • intonation variability
       • pace dynamics
       • emotional colouring

      Interpretation:
       • 70–100 → high motivation, drive
       • 40–70 → neutral state
       • <40 → apathy, reduced interest
    """.trimIndent(),
    "authority" to """
      Dominance (Dominance Index)

      What it measures:
       • confidence
       • tendency toward control and leadership
       • decision-making ability

      Signals:
       • vocal stability
       • low tremor
       • confident timbre

      Interpretation:
       • 70–100 → leader, dominance
       • 40–70 → balanced behaviour
       • <40 → uncertainty, submissiveness
    """.trimIndent(),
    "stress_tolerance" to """
      Stress resistance (Stress Resistance)

      What it measures:
       • ability to stay stable under load
       • reaction to pressure

      Signals:
       • voice micro-tremor
       • rhythm breaks
       • volume spikes

      Interpretation:
       • 70–100 → stress-resilient
       • 40–70 → moderate resilience
       • <40 → high reactivity, risk of breakdowns
    """.trimIndent(),
    "self_control" to """
      Self-control (Self-Control)

      What it measures:
       • emotion regulation
       • impulsivity
       • keeping behaviour within bounds

      Signals:
       • pauses before answering
       • controlled speech pace
       • no sharp jumps

      Interpretation:
       • 70–100 → high control
       • 40–70 → acceptable level
       • <40 → impulsivity, aggression risk
    """.trimIndent(),
    "person_harmonicity" to """
      Balance (Emotional Balance)

      What it measures:
       • overall psycho-emotional balance
       • state stability

      Signals:
       • even intonation
       • no sharp swings
       • harmonious speech

      Interpretation:
       • 70–100 → stable psyche
       • 40–70 → acceptable fluctuation
       • <40 → instability, inner tension
    """.trimIndent()
  )
  return when (lang) {
    AppLanguage.RU, AppLanguage.UK -> ru[key] ?: strings.emostateParamHintFallback
    AppLanguage.EN, AppLanguage.ZH, AppLanguage.KZ, AppLanguage.KA ->
      en[key] ?: strings.emostateParamHintFallback
  }
}

/** Полный текст шторки «интерпретация описания» (негативные состояния и правила). */
internal fun emostateDescriptionInterpretationBody(strings: Strings): String =
  when (strings.resolveAppLanguage()) {
    AppLanguage.RU, AppLanguage.UK -> DESCRIPTION_INTERPRETATION_RU
    AppLanguage.EN, AppLanguage.ZH, AppLanguage.KZ, AppLanguage.KA ->
      DESCRIPTION_INTERPRETATION_EN
  }

private val DESCRIPTION_INTERPRETATION_RU = buildString {
  append("1. Где «живут» негативные состояния\n\n")
  append("1.1 Агрессия / злость\n\n")
  append("Формируется через:\n")
  append(" • ↓ Самоконтроль\n")
  append(" • ↓ Уравновешенность\n")
  append(" • ↑ Властность (в сочетании с низким контролем)\n\n")
  append("Голосовые маркеры:\n")
  append(" • резкие атаки звука\n")
  append(" • повышение громкости\n")
  append(" • ускоренный темп\n")
  append(" • напряжённый тембр\n\n")
  append("Правило Voxera:\n\n")
  append("если (Самоконтроль < 40) + (Уравновешенность < 40) → риск агрессии\n")
  append("если дополнительно Властность > 60 → активная агрессия / давление\n")
  append("\n\n")
  append("1.2 Депрессивное состояние\n\n")
  append("Формируется через:\n")
  append(" • ↓ Вдохновенность\n")
  append(" • ↓ Жизнерадостность (если используешь)\n")
  append(" • ↓ Энергетика речи\n\n")
  append("Голосовые маркеры:\n")
  append(" • монотонность\n")
  append(" • замедление речи\n")
  append(" • слабая амплитуда интонации\n")
  append(" • длинные паузы\n\n")
  append("Правило:\n\n")
  append("если Вдохновенность < 40 → снижение мотивации\n")
  append("если < 30 → депрессивный паттерн\n")
  append("\n\n")
  append("1.3 Тревожность / внутреннее напряжение\n\n")
  append("Формируется через:\n")
  append(" • ↓ Стрессоустойчивость\n")
  append(" • ↓ Уравновешенность\n\n")
  append("Голосовые маркеры:\n")
  append(" • микродрожание\n")
  append(" • сбивчивость\n")
  append(" • скачки темпа\n")
  append(" • частые вдохи\n\n")
  append("Правило:\n\n")
  append("если Стрессоустойчивость < 40 → тревожность\n")
  append("если + Уравновешенность < 30 → высокая тревожность\n")
  append("\n\n")
  append("1.4 Эмоциональная нестабильность\n\n")
  append("Формируется через:\n")
  append(" • ↓ Уравновешенность\n")
  append(" • скачкообразные изменения параметров\n\n")
  append("Сигналы:\n")
  append(" • резкие перепады интонации\n")
  append(" • нестабильный темп\n")
  append(" • смена эмоций в коротком интервале\n")
  append("\n\n")
  append("1.5 Импульсивность (риск поведения)\n\n")
  append("Формируется через:\n")
  append(" • ↓ Самоконтроль\n")
  append(" • нормальная или высокая Властность\n\n")
  append("Сигналы:\n")
  append(" • отсутствие пауз\n")
  append(" • быстрые реакции\n")
  append(" • перебивание\n\n")
  append("Вывод\n")
  append(" • Агрессия, депрессия, тревожность — это не отдельные шкалы\n")
  append(" • Это интерпретация комбинации метрик\n")
  append(" • Это усиливает продукт:\n")
  append(" • проще UX\n")
  append(" • мощнее аналитика\n")
  append(" • масштабируемо под армию / медицину / HR")
}

private val DESCRIPTION_INTERPRETATION_EN = buildString {
  append("1. Where negative states show up\n\n")
  append("1.1 Aggression / anger\n\n")
  append("Driven by:\n")
  append(" • ↓ Self-control\n")
  append(" • ↓ Balance\n")
  append(" • ↑ Authority (with low control)\n\n")
  append("Voice markers:\n")
  append(" • sharp sound attacks\n")
  append(" • increased volume\n")
  append(" • faster pace\n")
  append(" • tense timbre\n\n")
  append("Voxera rule:\n\n")
  append("if (Self-control < 40) + (Balance < 40) → aggression risk\n")
  append("if Authority > 60 in addition → active aggression / pressure\n")
  append("\n\n")
  append("1.2 Depressive pattern\n\n")
  append("Driven by:\n")
  append(" • ↓ Inspiration\n")
  append(" • ↓ Vitality (if used)\n")
  append(" • ↓ Speech energy\n\n")
  append("Voice markers:\n")
  append(" • monotony\n")
  append(" • slower speech\n")
  append(" • weak intonation range\n")
  append(" • long pauses\n\n")
  append("Rule:\n\n")
  append("if Inspiration < 40 → lower motivation\n")
  append("if < 30 → depressive pattern\n")
  append("\n\n")
  append("1.3 Anxiety / inner tension\n\n")
  append("Driven by:\n")
  append(" • ↓ Stress tolerance\n")
  append(" • ↓ Balance\n\n")
  append("Voice markers:\n")
  append(" • micro-tremor\n")
  append(" • stumbling\n")
  append(" • tempo jumps\n")
  append(" • frequent breaths\n\n")
  append("Rule:\n\n")
  append("if Stress tolerance < 40 → anxiety\n")
  append("if Balance < 30 as well → high anxiety\n")
  append("\n\n")
  append("1.4 Emotional instability\n\n")
  append("Driven by:\n")
  append(" • ↓ Balance\n")
  append(" • abrupt parameter swings\n\n")
  append("Signals:\n")
  append(" • sharp intonation jumps\n")
  append(" • unstable tempo\n")
  append(" • emotion shifts in a short window\n")
  append("\n\n")
  append("1.5 Impulsivity (behaviour risk)\n\n")
  append("Driven by:\n")
  append(" • ↓ Self-control\n")
  append(" • normal or high Authority\n\n")
  append("Signals:\n")
  append(" • no pauses\n")
  append(" • fast reactions\n")
  append(" • interrupting\n\n")
  append("Takeaway\n")
  append(" • Aggression, depression, anxiety are not separate scales\n")
  append(" • They interpret combinations of metrics\n")
  append(" • Stronger product UX, analytics, and scaling for defence / healthcare / HR")
}
