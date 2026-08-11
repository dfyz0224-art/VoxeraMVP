package com.vanoprojects.voxera.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.vanoprojects.voxera.ui.strings.AppLanguage
import com.vanoprojects.voxera.ui.theme.ThemeType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "voxera_prefs")

class PreferencesManager(private val context: Context) {
    companion object {
        private val CONSENT_GIVEN_KEY = booleanPreferencesKey("consent_given")
        private val THEME_TYPE_KEY = stringPreferencesKey("theme_type")
        private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
        private val LANGUAGE_KEY = stringPreferencesKey("language")
        private val AUTH_COMPLETED_KEY = booleanPreferencesKey("auth_completed")
        private val PROFILE_PHOTO_PATH_KEY = stringPreferencesKey("profile_photo_path")
        private val PROFILE_PHONE_KEY = stringPreferencesKey("profile_phone")
    }

    val consentGiven: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[CONSENT_GIVEN_KEY] ?: false
    }

    suspend fun setConsentGiven(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CONSENT_GIVEN_KEY] = value
        }
    }
    
    val themeType: Flow<ThemeType> = context.dataStore.data.map { preferences ->
        val themeName = preferences[THEME_TYPE_KEY] ?: ThemeType.GLASS.name
        when (themeName) {
            ThemeType.LIGHT.name -> ThemeType.LIGHT
            ThemeType.GLASS.name -> ThemeType.GLASS
            "DARK" -> ThemeType.GLASS // ранее сохранённая тёмная тема
            else -> ThemeType.GLASS
        }
    }
    
    suspend fun setThemeType(themeType: ThemeType) {
        context.dataStore.edit { preferences ->
            preferences[THEME_TYPE_KEY] = themeType.name
        }
    }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED_KEY] ?: false
    }

    suspend fun setOnboardingCompleted(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED_KEY] = value
        }
    }

    val appLanguage: Flow<AppLanguage> = context.dataStore.data.map { preferences ->
        val lang = preferences[LANGUAGE_KEY] ?: AppLanguage.RU.name
        try {
            AppLanguage.valueOf(lang)
        } catch (e: IllegalArgumentException) {
            AppLanguage.RU
        }
    }

    suspend fun setAppLanguage(lang: AppLanguage) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = lang.name
        }
    }

    val authCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTH_COMPLETED_KEY] ?: false
    }

    suspend fun setAuthCompleted(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTH_COMPLETED_KEY] = value
        }
    }

    /** After sign-out: language / welcome / policy must run again for the next session. */
    suspend fun resetOnboardingAndConsent() {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED_KEY] = false
            preferences[CONSENT_GIVEN_KEY] = false
            preferences[AUTH_COMPLETED_KEY] = false
        }
    }

    val profilePhotoPath: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PROFILE_PHOTO_PATH_KEY]
    }

    suspend fun setProfilePhotoPath(path: String?) {
        context.dataStore.edit { preferences ->
            if (path != null) preferences[PROFILE_PHOTO_PATH_KEY] = path
            else preferences.remove(PROFILE_PHOTO_PATH_KEY)
        }
    }

    val profilePhone: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PROFILE_PHONE_KEY]
    }

    suspend fun setProfilePhone(phone: String?) {
        context.dataStore.edit { preferences ->
            if (phone != null) preferences[PROFILE_PHONE_KEY] = phone
            else preferences.remove(PROFILE_PHONE_KEY)
        }
    }

}
