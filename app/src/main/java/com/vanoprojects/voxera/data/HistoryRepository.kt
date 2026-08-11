package com.vanoprojects.voxera.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vanoprojects.voxera.data.model.AnalysisResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.util.UUID

data class HistoryEntry(
    val id: String,
    val timestamp: Long,
    val analysisType: String,
    val responseJson: String,
    val rawApiResponse: String?
) {
    fun toAnalysisResponse(): AnalysisResponse? = try {
        Gson().fromJson(responseJson, AnalysisResponse::class.java)
    } catch (_: Exception) { null }
}

/**
 * Local history scoped by account key (Firebase uid or [GUEST_ACCOUNT_KEY]).
 * Each account has its own file; switching accounts reloads that account's entries.
 */
class HistoryRepository(private val context: Context) {
    private val gson = Gson()
    private val typeToken = object : TypeToken<List<HistoryEntry>>() {}.type
    private val mutex = Mutex()
    private val _entries = MutableStateFlow<List<HistoryEntry>>(emptyList())
    val entries: Flow<List<HistoryEntry>> = _entries.asStateFlow()

    @Volatile
    private var accountKey: String = GUEST_ACCOUNT_KEY

    init {
        loadEntries()
    }

    /** Switch visible/writable history to [key] (uid or guest). */
    fun setAccountKey(key: String) {
        val normalized = key.ifBlank { GUEST_ACCOUNT_KEY }
        if (normalized == accountKey) return
        accountKey = normalized
        loadEntries()
    }

    fun currentAccountKey(): String = accountKey

    private fun getHistoryFile(): File =
        File(context.filesDir, "history_${accountKey}.json")

    private fun legacyHistoryFile(): File = File(context.filesDir, "history.json")

    private fun loadEntries() {
        try {
            migrateLegacyIfNeeded()
            val file = getHistoryFile()
            if (file.exists()) {
                val json = file.readText()
                val list: List<HistoryEntry> = gson.fromJson(json, typeToken) ?: emptyList()
                _entries.value = list.sortedByDescending { it.timestamp }
            } else {
                _entries.value = emptyList()
            }
        } catch (_: Exception) {
            _entries.value = emptyList()
        }
    }

    /** One-time: move old shared history.json into the current account file. */
    private fun migrateLegacyIfNeeded() {
        val legacy = legacyHistoryFile()
        val target = getHistoryFile()
        if (!legacy.exists() || target.exists()) return
        try {
            legacy.copyTo(target, overwrite = false)
            legacy.delete()
        } catch (_: Exception) { }
    }

    suspend fun addEntry(
        analysisType: String,
        response: AnalysisResponse,
        rawApiResponse: String?
    ) {
        mutex.withLock {
            val entry = HistoryEntry(
                id = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                analysisType = analysisType,
                responseJson = gson.toJson(response),
                rawApiResponse = rawApiResponse
            )
            val newList = (listOf(entry) + _entries.value).sortedByDescending { it.timestamp }
            _entries.value = newList
            saveEntries(newList)
        }
    }

    private fun saveEntries(list: List<HistoryEntry>) {
        try {
            getHistoryFile().writeText(gson.toJson(list))
        } catch (_: Exception) { }
    }

    fun getEntry(id: String): HistoryEntry? = _entries.value.find { it.id == id }

    suspend fun clearHistory() {
        mutex.withLock {
            _entries.value = emptyList()
            getHistoryFile().delete()
        }
    }

    companion object {
        const val GUEST_ACCOUNT_KEY = "guest"
    }
}
