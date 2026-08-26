package com.vanoprojects.voxera.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.vanoprojects.voxera.data.PreferencesManager
import com.vanoprojects.voxera.ui.strings.AppLanguage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class DailyReminderReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent?) {
    val slotName = intent?.getStringExtra(DailyReminderScheduler.EXTRA_SLOT) ?: return
    val slot = runCatching { ReminderCopy.Slot.valueOf(slotName) }.getOrNull() ?: return
    val prefs = PreferencesManager(context.applicationContext)
    val enabled = runBlocking { prefs.dailyRemindersEnabled.first() }
    if (!enabled) return
    val language = runBlocking { prefs.appLanguage.first() }
    val langExtra = intent.getStringExtra("language")
    val lang = langExtra?.let {
      runCatching { AppLanguage.valueOf(it) }.getOrNull()
    } ?: language

    DailyReminderScheduler.showNow(context.applicationContext, slot, lang)
    DailyReminderScheduler.scheduleNext(context.applicationContext, slot, lang)
  }
}

class BootCompletedReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent?) {
    if (intent?.action != Intent.ACTION_BOOT_COMPLETED &&
      intent?.action != Intent.ACTION_MY_PACKAGE_REPLACED
    ) return
    val prefs = PreferencesManager(context.applicationContext)
    runBlocking {
      val enabled = prefs.dailyRemindersEnabled.first()
      val lang = prefs.appLanguage.first()
      DailyReminderScheduler.reschedule(context.applicationContext, enabled, lang)
    }
  }
}
