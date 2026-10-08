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
    val account = accountId(context, uid)
    return isInTrial(context, account) || EntitlementStore.hasActiveSubscription
  }

  /** Stable id for guest mode so the 7-day trial is not restarted on every launch. */
  fun accountId(context: Context, firebaseUid: String?): String {
    if (!firebaseUid.isNullOrBlank()) return firebaseUid
    return guestUid(context)
  }

  fun guestUid(context: Context): String {
    val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    val existing = prefs.getString(GUEST_KEY, null)
    if (!existing.isNullOrBlank()) return existing
    val created = "guest-" + java.util.UUID.randomUUID().toString()
    prefs.edit().putString(GUEST_KEY, created).apply()
    return created
  }

  private const val GUEST_KEY = "guest_uid"
}
