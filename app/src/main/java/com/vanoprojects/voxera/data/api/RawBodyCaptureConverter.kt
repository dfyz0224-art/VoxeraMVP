package com.vanoprojects.voxera.data.api

import android.util.Log
import com.vanoprojects.voxera.data.AnalysisSession
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * Обёртка над GsonConverter: сохраняет сырой JSON в AnalysisSession.lastRawApiResponse
 * перед парсингом, чтобы description всегда был доступен.
 */
class RawBodyCaptureConverter(
    private val delegate: Converter.Factory
) : Converter.Factory() {

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        val delegateConverter = delegate.responseBodyConverter(type, annotations, retrofit)
            ?: return null
        return Converter<ResponseBody, Any> { body ->
            val rawString = body.string()
            AnalysisSession.lastRawApiResponse = rawString
            Log.d("RawBodyCapture", "Saved raw JSON, length=${rawString.length}, hasDescription=${rawString.contains("\"description\"")}")
            @Suppress("UNCHECKED_CAST")
            (delegateConverter as Converter<ResponseBody, Any>).convert(
                @Suppress("DEPRECATION")
                ResponseBody.create(body.contentType(), rawString)
            )
        }
    }
}
