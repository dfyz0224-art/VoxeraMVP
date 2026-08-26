package com.vanoprojects.voxera.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.vanoprojects.voxera.MainActivity
import com.vanoprojects.voxera.ui.strings.AppLanguage
import java.util.Calendar

object DailyReminderScheduler {
  const val CHANNEL_ID = "voxera_daily_reminders"
  const val EXTRA_SLOT = "slot"
  private const val REQ_MORNING = 4101
  private const val REQ_EVENING = 4102
  private const val NOTIF_MORNING = 4103
  private const val NOTIF_EVENING = 4104

  const val HOUR_MORNING = 9
  const val HOUR_EVENING = 20

  fun ensureChannel(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val mgr = context.getSystemService(NotificationManager::class.java) ?: return
    val channel = NotificationChannel(
      CHANNEL_ID,
      "Daily reminders",
      NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
      description = "Morning and evening emotional check-in reminders"
    }
    mgr.createNotificationChannel(channel)
  }

  fun reschedule(context: Context, enabled: Boolean, language: AppLanguage) {
    ensureChannel(context)
    cancel(context)
    if (!enabled) return
    scheduleNext(context, ReminderCopy.Slot.MORNING, language)
    scheduleNext(context, ReminderCopy.Slot.EVENING, language)
  }

  fun scheduleNext(context: Context, slot: ReminderCopy.Slot, language: AppLanguage) {
    val triggerAt = nextTriggerMillis(slot)
    val intent = Intent(context, DailyReminderReceiver::class.java).apply {
      action = "com.vanoprojects.voxera.DAILY_REMINDER"
      putExtra(EXTRA_SLOT, slot.name)
      putExtra("language", language.name)
    }
    val req = if (slot == ReminderCopy.Slot.MORNING) REQ_MORNING else REQ_EVENING
    val pi = PendingIntent.getBroadcast(
      context,
      req,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
    } else {
      @Suppress("DEPRECATION")
      am.set(AlarmManager.RTC_WAKEUP, triggerAt, pi)
    }
  }

  fun cancel(context: Context) {
    val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    listOf(REQ_MORNING, REQ_EVENING).forEach { req ->
      val intent = Intent(context, DailyReminderReceiver::class.java).apply {
        action = "com.vanoprojects.voxera.DAILY_REMINDER"
      }
      val pi = PendingIntent.getBroadcast(
        context,
        req,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )
      am.cancel(pi)
    }
  }

  fun showNow(context: Context, slot: ReminderCopy.Slot, language: AppLanguage) {
    ensureChannel(context)
    if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
    val msg = ReminderCopy.pick(language, slot)
    val open = PendingIntent.getActivity(
      context,
      0,
      Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      },
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val notif = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(android.R.drawable.ic_dialog_info)
      .setContentTitle(msg.title)
      .setContentText(msg.body)
      .setStyle(NotificationCompat.BigTextStyle().bigText(msg.body))
      .setContentIntent(open)
      .setAutoCancel(true)
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .build()
    val id = if (slot == ReminderCopy.Slot.MORNING) NOTIF_MORNING else NOTIF_EVENING
    try {
      NotificationManagerCompat.from(context).notify(id, notif)
    } catch (_: SecurityException) {
      // POST_NOTIFICATIONS not granted
    }
  }

  fun nextTriggerMillis(slot: ReminderCopy.Slot): Long {
    val hour = if (slot == ReminderCopy.Slot.MORNING) HOUR_MORNING else HOUR_EVENING
    val cal = Calendar.getInstance().apply {
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.HOUR_OF_DAY, hour)
      if (timeInMillis <= System.currentTimeMillis()) {
        add(Calendar.DAY_OF_YEAR, 1)
      }
    }
    return cal.timeInMillis
  }
}
