import Foundation

/// Result (i) hint sheets — parity with Android EmostateResultHints / PsytypeResultHints.
enum ResultHints {
  static func psytypeBody(typeKey: String, language: AppLanguage) -> String {
    let key = typeKey.lowercased()
    let ru: [String: String] = [
      "realizer": "Ориентирован на результат и доведение дел до конца. Практичен, последователен, ценит эффективность и конкретный эффект от действий.",
      "leader": "Склонен вести за собой, брать ответственность и задавать направление. Уверенно принимает решения и влияет на окружение.",
      "manager": "Организует процессы, распределяет задачи и держит контроль. Сильная сторона — структура, планирование и исполнительская дисциплина.",
      "visionary": "Смотрит вперёд, генерирует идеи и видит возможности. Силён в стратегии и образе будущего, меньше — в рутинной деталировке.",
      "creator": "Проявляет креативность, гибкость и поиск нестандартных решений. Легко включается в новое и меняет подходы.",
      "communicator": "Легко устанавливает контакт, убеждает и поддерживает диалог. Сильная сторона — общение, эмпатия и влияние через речь.",
      "stabilizer": "Ценит устойчивость, предсказуемость и баланс. Сглаживает конфликты, поддерживает спокойный ритм и надёжность.",
      "expert": "Глубоко погружается в тему, опирается на знания и точность. Силён в анализе, качестве и экспертной оценке."
    ]
    let en: [String: String] = [
      "realizer": "Focused on results and finishing what was started. Practical, consistent, values efficiency and tangible outcomes.",
      "leader": "Tends to lead, take responsibility, and set direction. Confident in decisions and influence on others.",
      "manager": "Organizes processes, assigns tasks, and keeps control. Strengths: structure, planning, and delivery discipline.",
      "visionary": "Looks ahead, generates ideas, and sees opportunities. Strong in strategy and future vision; less in routine detail.",
      "creator": "Shows creativity, flexibility, and unconventional solutions. Quickly engages with the new and changes approaches.",
      "communicator": "Easily builds rapport, persuades, and sustains dialogue. Strengths: conversation, empathy, and influence through speech.",
      "stabilizer": "Values stability, predictability, and balance. Softens conflict and supports a calm, reliable pace.",
      "expert": "Goes deep into the subject, relies on knowledge and precision. Strong in analysis, quality, and expert judgment."
    ]
    switch language {
    case .ru, .uk: return ru[key] ?? "Краткое описание этого психотипа пока недоступно."
    case .en, .zh, .kz, .ka: return en[key] ?? "A short description for this psychotype is not available yet."
    }
  }

  static func emoMetricBody(scaleKey: String, language: AppLanguage, fallback: String) -> String {
    let key = scaleKey.lowercased()
    let ru: [String: String] = [
      "emo_engage": """
      Вдохновенность (Inspiration Index)

      Что измеряет:
       • уровень внутренней энергии
       • мотивацию к действию
       • наличие интереса и вовлечённости

      Интерпретация:
       • 70–100 → высокая мотивация, драйв
       • 40–70 → нейтральное состояние
       • <40 → апатия, снижение интереса
      """,
      "authority": """
      Властность (Dominance Index)

      Что измеряет:
       • уверенность
       • склонность к контролю и лидерству
       • способность принимать решения

      Интерпретация:
       • 70–100 → лидер, доминирование
       • 40–70 → сбалансированное поведение
       • <40 → неуверенность, подчинённость
      """,
      "stress_tolerance": """
      Стрессоустойчивость (Stress Resistance)

      Что измеряет:
       • способность сохранять стабильность под нагрузкой
       • реакцию на давление

      Интерпретация:
       • 70–100 → устойчив к стрессу
       • 40–70 → средняя устойчивость
       • <40 → высокая реактивность, риск срывов
      """,
      "self_control": """
      Самоконтроль (Self-Control)

      Что измеряет:
       • управление эмоциями
       • импульсивность
       • способность держать поведение в рамках

      Интерпретация:
       • 70–100 → высокий контроль
       • 40–70 → допустимый уровень
       • <40 → импульсивность, риск агрессии
      """,
      "person_harmonicity": """
      Уравновешенность (Emotional Balance)

      Что измеряет:
       • общий психоэмоциональный баланс
       • стабильность состояния

      Интерпретация:
       • 70–100 → стабильная психика
       • 40–70 → допустимое колебание
       • <40 → нестабильность, внутреннее напряжение
      """,
      "energy_level": """
      Жизнерадостность (Cheerfulness)

      Что измеряет:
       • энергию и позитивный тонус речи
       • готовность к активности

      Интерпретация:
       • 70–100 → высокий ресурс
       • 40–70 → средний тонус
       • <40 → сниженная энергия
      """
    ]
    let en: [String: String] = [
      "emo_engage": """
      Inspiration (Inspiration Index)

      What it measures:
       • inner energy level
       • motivation to act
       • interest and engagement

      Interpretation:
       • 70–100 → high motivation, drive
       • 40–70 → neutral state
       • <40 → apathy, reduced interest
      """,
      "authority": """
      Dominance (Dominance Index)

      What it measures:
       • confidence
       • tendency toward control and leadership
       • decision-making ability

      Interpretation:
       • 70–100 → leader, dominance
       • 40–70 → balanced behaviour
       • <40 → uncertainty, submissiveness
      """,
      "stress_tolerance": """
      Stress resistance (Stress Resistance)

      What it measures:
       • ability to stay stable under load
       • reaction to pressure

      Interpretation:
       • 70–100 → stress-resilient
       • 40–70 → moderate resilience
       • <40 → high reactivity, risk of breakdowns
      """,
      "self_control": """
      Self-control (Self-Control)

      What it measures:
       • emotion regulation
       • impulsivity
       • keeping behaviour within bounds

      Interpretation:
       • 70–100 → high control
       • 40–70 → acceptable level
       • <40 → impulsivity, aggression risk
      """,
      "person_harmonicity": """
      Balance (Emotional Balance)

      What it measures:
       • overall psycho-emotional balance
       • state stability

      Interpretation:
       • 70–100 → stable psyche
       • 40–70 → acceptable fluctuation
       • <40 → instability, inner tension
      """,
      "energy_level": """
      Cheerfulness

      What it measures:
       • energy and positive tone of speech
       • readiness for activity

      Interpretation:
       • 70–100 → high resource
       • 40–70 → medium tone
       • <40 → reduced energy
      """
    ]
    switch language {
    case .ru, .uk: return ru[key] ?? fallback
    case .en, .zh, .kz, .ka: return en[key] ?? fallback
    }
  }

  static func descriptionInterpretation(language: AppLanguage) -> String {
    switch language {
    case .ru, .uk:
      return """
      Негативные состояния — это интерпретация комбинации метрик, а не отдельные шкалы.

      Агрессия / злость: ↓ самоконтроль + ↓ уравновешенность (+ ↑ властность).
      Депрессивный паттерн: ↓ вдохновенность / энергия речи.
      Тревожность: ↓ стрессоустойчивость (+ ↓ уравновешенность).
      Импульсивность: ↓ самоконтроль при нормальной/высокой властности.

      Смотрите на значения в совокупности с остальными метриками.
      """
    case .en, .zh, .kz, .ka:
      return """
      Negative states are interpretations of metric combinations, not separate scales.

      Aggression/anger: ↓ self-control + ↓ balance (+ ↑ dominance).
      Depressive pattern: ↓ inspiration / speech energy.
      Anxiety: ↓ stress resistance (+ ↓ balance).
      Impulsivity: ↓ self-control with normal/high dominance.

      Interpret values together with the other metrics.
      """
    }
  }
}
