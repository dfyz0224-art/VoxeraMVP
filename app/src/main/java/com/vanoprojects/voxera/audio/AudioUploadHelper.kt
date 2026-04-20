package com.vanoprojects.voxera.audio

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream

/**
 * Копирует выбранный пользователем аудиофайл в cache.
 * MIME приводится к тем, что ожидает API (см. документацию Voxera).
 */
object AudioUploadHelper {

    /**
     * Канонический MIME по расширению — совпадает с поддерживаемыми форматами API.
     */
    fun mimeFromExtension(fileName: String): String? {
        val lower = fileName.lowercase()
        return when {
            lower.endsWith(".wav") -> "audio/wav"
            lower.endsWith(".mp3") -> "audio/mpeg"
            lower.endsWith(".ogg") || lower.endsWith(".oga") -> "audio/ogg"
            lower.endsWith(".webm") -> "audio/webm"
            lower.endsWith(".m4a") -> "audio/mp4"
            lower.endsWith(".aac") -> "audio/aac"
            lower.endsWith(".flac") -> "audio/flac"
            lower.endsWith(".opus") -> "audio/opus"
            else -> null
        }
    }

    /**
     * Нормализует MIME от ContentResolver / системы к виду, который принимает сервер.
     */
    fun normalizeMimeType(mime: String, fileName: String): String {
        val trimmed = mime.trim().lowercase()
        val byExt = mimeFromExtension(fileName)
        if (byExt != null) {
            // Расширение надёжнее «угадываний» контент-провайдера (часто дают application/octet-stream).
            return byExt
        }
        return when (trimmed) {
            "audio/x-wav", "audio/wave", "audio/vnd.wave" -> "audio/wav"
            "audio/mp3" -> "audio/mpeg"
            "application/ogg", "audio/vorbis" -> "audio/ogg"
            "audio/x-m4a", "audio/m4a" -> "audio/mp4"
            "audio/x-flac" -> "audio/flac"
            "audio/opus" -> "audio/opus"
            "" -> "application/octet-stream"
            else -> trimmed
        }
    }

    fun guessMimeFromFileName(fileName: String): String {
        return mimeFromExtension(fileName) ?: "application/octet-stream"
    }

    /**
     * Итоговый MIME для multipart перед отправкой на API.
     */
    fun resolveMimeForApi(file: File, sessionMime: String?): String {
        val extMime = mimeFromExtension(file.name)
        if (extMime != null) return extMime
        val candidate = sessionMime?.takeIf { it.isNotBlank() } ?: guessMimeFromFileName(file.name)
        return normalizeMimeType(candidate, file.name)
    }

    fun copyToCache(context: Context, uri: Uri): Pair<File, String>? {
        val resolver = context.contentResolver
        var displayName = ""
        try {
            resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                if (c.moveToFirst()) {
                    val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) displayName = c.getString(idx).orEmpty()
                }
            }
        } catch (_: Exception) { }

        val fromDisplay = displayName.substringAfterLast('.', "").lowercase().takeIf { it.length in 1..6 } ?: ""
        val pathSeg = uri.lastPathSegment.orEmpty().substringAfterLast('/')
        val fromPath = pathSeg.substringAfterLast('.', "").lowercase().takeIf { it.length in 1..6 } ?: ""
        val ext = fromDisplay.ifBlank { fromPath }
        val extPart = if (ext.isNotBlank()) ".$ext" else ""
        val dest = File(context.cacheDir, "upload_${System.currentTimeMillis()}$extPart")

        return try {
            resolver.openInputStream(uri)?.use { input ->
                FileOutputStream(dest).use { output -> input.copyTo(output) }
            } ?: return null
            val resolverMime = resolver.getType(uri).orEmpty()
            val mime = resolveMimeForApi(dest, resolverMime)
            Pair(dest, mime)
        } catch (_: Exception) {
            if (dest.exists()) dest.delete()
            null
        }
    }

}
