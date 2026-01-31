package com.vanoprojects.voxera.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "voxera_prefs")

class PreferencesManager(private val context: Context) {
    companion object {
        private val CONSENT_GIVEN_KEY = booleanPreferencesKey("consent_given")
    }

    val consentGiven: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[CONSENT_GIVEN_KEY] ?: false
    }

    suspend fun setConsentGiven(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CONSENT_GIVEN_KEY] = value
        }
    }
}
