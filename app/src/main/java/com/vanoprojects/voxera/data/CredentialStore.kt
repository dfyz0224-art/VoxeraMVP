package com.vanoprojects.voxera.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Stores email/password when user enables "Remember password".
 * Uses EncryptedSharedPreferences (AES).
 */
class CredentialStore(context: Context) {
  private val prefs: SharedPreferences

  init {
    val masterKey = MasterKey.Builder(context.applicationContext)
      .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
      .build()
    prefs = EncryptedSharedPreferences.create(
      context.applicationContext,
      PREFS_NAME,
      masterKey,
      EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
      EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
  }

  fun save(email: String, password: String) {
    prefs.edit()
      .putString(KEY_EMAIL, email.trim())
      .putString(KEY_PASSWORD, password)
      .putBoolean(KEY_REMEMBER, true)
      .apply()
  }

  fun clear() {
    prefs.edit().clear().apply()
  }

  fun isRememberEnabled(): Boolean = prefs.getBoolean(KEY_REMEMBER, false)

  fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)?.takeIf { it.isNotBlank() }

  fun getPassword(): String? = prefs.getString(KEY_PASSWORD, null)?.takeIf { it.isNotBlank() }

  fun load(): Pair<String, String>? {
    val email = getEmail() ?: return null
    val password = getPassword() ?: return null
    if (!isRememberEnabled()) return null
    return email to password
  }

  companion object {
    private const val PREFS_NAME = "voxera_auth_creds"
    private const val KEY_EMAIL = "email"
    private const val KEY_PASSWORD = "password"
    private const val KEY_REMEMBER = "remember"
  }
}
