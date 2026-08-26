package com.vanoprojects.voxera.notifications

import com.vanoprojects.voxera.ui.strings.AppLanguage
import java.util.Calendar

/** Neutral daily reminder copy — morning / evening, rotated by day. */
object ReminderCopy {
  enum class Slot { MORNING, EVENING }

  data class Message(val title: String, val body: String)

  fun pick(language: AppLanguage, slot: Slot, whenMillis: Long = System.currentTimeMillis()): Message {
    val day = Calendar.getInstance().apply { timeInMillis = whenMillis }.get(Calendar.DAY_OF_YEAR)
    val list = messages(language, slot)
    return list[day % list.size]
  }

  private fun messages(language: AppLanguage, slot: Slot): List<Message> = when (language) {
    AppLanguage.RU -> if (slot == Slot.MORNING) morningRu else eveningRu
    AppLanguage.EN -> if (slot == Slot.MORNING) morningEn else eveningEn
    AppLanguage.ZH -> if (slot == Slot.MORNING) morningZh else eveningZh
    AppLanguage.KZ -> if (slot == Slot.MORNING) morningKz else eveningKz
    AppLanguage.UK -> if (slot == Slot.MORNING) morningUk else eveningUk
    AppLanguage.KA -> if (slot == Slot.MORNING) morningEn else eveningEn
  }

  private val morningRu = listOf(
    Message("Voxera", "Не забудьте проверить своё эмоциональное состояние"),
    Message("Voxera", "Утренний чек: короткая запись голоса покажет ваш настрой"),
    Message("Voxera", "Как вы сегодня? Сделайте короткий анализ состояния"),
    Message("Voxera", "Пара минут на запись — и вы увидите уровень энергии и стресса"),
  )
  private val eveningRu = listOf(
    Message("Voxera", "Вечерний чек-ап: не забудьте отметить, как прошёл день"),
    Message("Voxera", "Перед отдыхом проверьте эмоциональное состояние"),
    Message("Voxera", "Короткая вечерняя запись поможет увидеть изменения за день"),
    Message("Voxera", "Завершите день коротким анализом голоса"),
  )

  private val morningEn = listOf(
    Message("Voxera", "Don’t forget to check your emotional state"),
    Message("Voxera", "Morning check-in: a short voice recording shows your energy"),
    Message("Voxera", "How are you today? Take a quick state check"),
    Message("Voxera", "A few minutes of recording — see your stress and energy levels"),
  )
  private val eveningEn = listOf(
    Message("Voxera", "Evening check-in: note how your day went"),
    Message("Voxera", "Before you rest, check your emotional state"),
    Message("Voxera", "A short evening recording helps you see how the day changed you"),
    Message("Voxera", "Finish the day with a quick voice analysis"),
  )

  private val morningZh = listOf(
    Message("Voxera", "别忘了检查一下自己的情绪状态"),
    Message("Voxera", "晨间打卡：短录音可了解今日精力"),
    Message("Voxera", "今天感觉如何？做一次简短状态分析"),
    Message("Voxera", "花几分钟录音，看看压力与能量水平"),
  )
  private val eveningZh = listOf(
    Message("Voxera", "晚间打卡：记录今天过得怎么样"),
    Message("Voxera", "休息前检查一下情绪状态"),
    Message("Voxera", "晚间短录音有助于看到一天的变化"),
    Message("Voxera", "用一次简短语音分析结束今天"),
  )

  private val morningKz = listOf(
    Message("Voxera", "Эмоционалдық күйіңізді тексеруді ұмытпаңыз"),
    Message("Voxera", "Таңғы тексеру: қысқа жазба көңіл күйіңізді көрсетеді"),
    Message("Voxera", "Бүгін қалайсыз? Қысқа күй талдауын жасаңыз"),
    Message("Voxera", "Бірнеше минут жазба — энергия мен стресс деңгейін көресіз"),
  )
  private val eveningKz = listOf(
    Message("Voxera", "Кешкі тексеру: күн қалай өткенін белгілеңіз"),
    Message("Voxera", "Демалыс алдында эмоционалдық күйіңізді тексеріңіз"),
    Message("Voxera", "Кешкі қысқа жазба күндегі өзгерісті көруге көмектеседі"),
    Message("Voxera", "Күнді қысқа дауыс талдауымен аяқтаңыз"),
  )

  private val morningUk = listOf(
    Message("Voxera", "Не забудьте перевірити свій емоційний стан"),
    Message("Voxera", "Ранкова перевірка: короткий запис голосу покаже ваш настрій"),
    Message("Voxera", "Як ви сьогодні? Зробіть короткий аналіз стану"),
    Message("Voxera", "Кілька хвилин запису — і ви побачите рівень енергії та стресу"),
  )
  private val eveningUk = listOf(
    Message("Voxera", "Вечірня перевірка: зазначте, як минув день"),
    Message("Voxera", "Перед відпочинком перевірте емоційний стан"),
    Message("Voxera", "Короткий вечірній запис допоможе побачити зміни за день"),
    Message("Voxera", "Завершіть день коротким аналізом голосу"),
  )
}
