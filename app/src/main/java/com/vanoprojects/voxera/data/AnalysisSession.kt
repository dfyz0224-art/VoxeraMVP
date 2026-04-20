package com.vanoprojects.voxera.data

import com.vanoprojects.voxera.data.model.AnalysisResponse

/**
 * Хранит состояние текущей сессии анализа между экранами.
 * analysisType: "emostate" или "psytype" в зависимости от выбранного режима.
 */
object AnalysisSession {
    var analysisType: String = "emostate"
    var lastRecordedFile: java.io.File? = null
    /** MIME тип загруженного файла (для multipart); для записи с микрофона — null */
    var lastAudioMimeType: String? = null
    var lastResultJson: String? = null
    var lastAnalysisResponse: AnalysisResponse? = null
    /** Сырой JSON ответ API — для извлечения description (Gson может не парсить) */
    @Volatile
    var lastRawApiResponse: String? = null
}
