package com.vanoprojects.voxera.billing

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object EntitlementStore {
  var hasActiveSubscription by mutableStateOf(false)
}

object TrialAccess {
  private const val PREFS = "voxera_trial"
  private const val TRIAL_MS = 7L * 24L * 60L * 60L * 1000L

  fun startIfNeeded(context: Context, uid: String) {
    val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    if (!prefs.contains(uid)) {
      prefs.edit().putLong(uid, System.currentTimeMillis()).apply()
    }
  }

  fun isInTrial(context: Context, uid: String): Boolean {
    val start = context.applicationContext
      .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
      .getLong(uid, 0L)
    if (start == 0L) return false
    return System.currentTimeMillis() - start < TRIAL_MS
  }

  fun canAnalyze(context: Context, uid: String?): Boolean {
    if (uid.isNullOrBlank()) return false
    return isInTrial(context, uid) || EntitlementStore.hasActiveSubscription
  }
}
