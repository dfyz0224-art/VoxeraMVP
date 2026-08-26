import Foundation
import UserNotifications

/// Local morning (09:00) + evening (20:00) reminders with rotating neutral copy.
enum DailyReminderScheduler {
  private static let morningPrefix = "voxera.reminder.morning."
  private static let eveningPrefix = "voxera.reminder.evening."
  static let hourMorning = 9
  static let hourEvening = 20
  private static let daysAhead = 14

  enum Slot {
    case morning, evening
  }

  static func refresh(enabled: Bool, language: AppLanguage) async {
    let center = UNUserNotificationCenter.current()
    await cancelAll()
    guard enabled else { return }

    let settings = await center.notificationSettings()
    var granted = settings.authorizationStatus == .authorized || settings.authorizationStatus == .provisional
    if settings.authorizationStatus == .notDetermined {
      granted = (try? await center.requestAuthorization(options: [.alert, .sound, .badge])) ?? false
    }
    guard granted else { return }

    let cal = Calendar.current
    let now = Date()
    for dayOffset in 0..<daysAhead {
      guard let day = cal.date(byAdding: .day, value: dayOffset, to: now) else { continue }
      scheduleOne(slot: .morning, on: day, language: language, calendar: cal)
      scheduleOne(slot: .evening, on: day, language: language, calendar: cal)
    }
  }

  static func cancelAll() async {
    let center = UNUserNotificationCenter.current()
    let pending = await center.pendingNotificationRequests()
    let ids = pending.map(\.identifier).filter {
      $0.hasPrefix(morningPrefix) || $0.hasPrefix(eveningPrefix)
    }
    center.removePendingNotificationRequests(withIdentifiers: ids)
  }

  private static func scheduleOne(slot: Slot, on day: Date, language: AppLanguage, calendar: Calendar) {
    var comps = calendar.dateComponents([.year, .month, .day], from: day)
    comps.hour = slot == .morning ? hourMorning : hourEvening
    comps.minute = 0
    comps.second = 0
    guard let fire = calendar.date(from: comps), fire > Date() else { return }

    let dayIndex = calendar.ordinality(of: .day, in: .year, for: fire) ?? 0
    let msg = ReminderCopy.pick(language: language, slot: slot, dayIndex: dayIndex)

    let content = UNMutableNotificationContent()
    content.title = msg.title
    content.body = msg.body
    content.sound = .default

    let trigger = UNCalendarNotificationTrigger(dateMatching: comps, repeats: false)
    let prefix = slot == .morning ? morningPrefix : eveningPrefix
    let id = prefix + "\(comps.year!)-\(comps.month!)-\(comps.day!)"
    let req = UNNotificationRequest(identifier: id, content: content, trigger: trigger)
    UNUserNotificationCenter.current().add(req)
  }
}

enum ReminderCopy {
  struct Message {
    let title: String
    let body: String
  }

  static func pick(language: AppLanguage, slot: DailyReminderScheduler.Slot, dayIndex: Int) -> Message {
    let list: [Message]
    switch (language, slot) {
    case (.ru, .morning): list = morningRu
    case (.ru, .evening): list = eveningRu
    case (.en, .morning): list = morningEn
    case (.en, .evening): list = eveningEn
    case (.zh, .morning): list = morningZh
    case (.zh, .evening): list = eveningZh
    case (.kz, .morning): list = morningKz
    case (.kz, .evening): list = eveningKz
    }
    return list[abs(dayIndex) % list.count]
  }

  private static let morningRu: [Message] = [
    .init(title: "Voxera", body: "Не забудьте проверить своё эмоциональное состояние"),
    .init(title: "Voxera", body: "Утренний чек: короткая запись голоса покажет ваш настрой"),
    .init(title: "Voxera", body: "Как вы сегодня? Сделайте короткий анализ состояния"),
    .init(title: "Voxera", body: "Пара минут на запись — и вы увидите уровень энергии и стресса"),
  ]
  private static let eveningRu: [Message] = [
    .init(title: "Voxera", body: "Вечерний чек-ап: не забудьте отметить, как прошёл день"),
    .init(title: "Voxera", body: "Перед отдыхом проверьте эмоциональное состояние"),
    .init(title: "Voxera", body: "Короткая вечерняя запись поможет увидеть изменения за день"),
    .init(title: "Voxera", body: "Завершите день коротким анализом голоса"),
  ]
  private static let morningEn: [Message] = [
    .init(title: "Voxera", body: "Don’t forget to check your emotional state"),
    .init(title: "Voxera", body: "Morning check-in: a short voice recording shows your energy"),
    .init(title: "Voxera", body: "How are you today? Take a quick state check"),
    .init(title: "Voxera", body: "A few minutes of recording — see your stress and energy levels"),
  ]
  private static let eveningEn: [Message] = [
    .init(title: "Voxera", body: "Evening check-in: note how your day went"),
    .init(title: "Voxera", body: "Before you rest, check your emotional state"),
    .init(title: "Voxera", body: "A short evening recording helps you see how the day changed you"),
    .init(title: "Voxera", body: "Finish the day with a quick voice analysis"),
  ]
  private static let morningZh: [Message] = [
    .init(title: "Voxera", body: "别忘了检查一下自己的情绪状态"),
    .init(title: "Voxera", body: "晨间打卡：短录音可了解今日精力"),
    .init(title: "Voxera", body: "今天感觉如何？做一次简短状态分析"),
    .init(title: "Voxera", body: "花几分钟录音，看看压力与能量水平"),
  ]
  private static let eveningZh: [Message] = [
    .init(title: "Voxera", body: "晚间打卡：记录今天过得怎么样"),
    .init(title: "Voxera", body: "休息前检查一下情绪状态"),
    .init(title: "Voxera", body: "晚间短录音有助于看到一天的变化"),
    .init(title: "Voxera", body: "用一次简短语音分析结束今天"),
  ]
  private static let morningKz: [Message] = [
    .init(title: "Voxera", body: "Эмоционалдық күйіңізді тексеруді ұмытпаңыз"),
    .init(title: "Voxera", body: "Таңғы тексеру: қысқа жазба көңіл күйіңізді көрсетеді"),
    .init(title: "Voxera", body: "Бүгін қалайсыз? Қысқа күй талдауын жасаңыз"),
    .init(title: "Voxera", body: "Бірнеше минут жазба — энергия мен стресс деңгейін көресіз"),
  ]
  private static let eveningKz: [Message] = [
    .init(title: "Voxera", body: "Кешкі тексеру: күн қалай өткенін белгілеңіз"),
    .init(title: "Voxera", body: "Демалыс алдында эмоционалдық күйіңізді тексеріңіз"),
    .init(title: "Voxera", body: "Кешкі қысқа жазба күндегі өзгерісті көруге көмектеседі"),
    .init(title: "Voxera", body: "Күнді қысқа дауыс талдауымен аяқтаңыз"),
  ]
}
