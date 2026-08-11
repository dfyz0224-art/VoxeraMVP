package com.vanoprojects.voxera.data

import android.util.Log
import com.google.gson.GsonBuilder
import com.vanoprojects.voxera.audio.AudioUploadHelper
import com.vanoprojects.voxera.data.api.VoxeraApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.SocketException

/**
 * Загрузка анализа вне UI: переживает уход с экрана / свертывание Activity
 * (пока жив процесс) и умеет повторять сетевые сбои.
 */
object AnalysisUploadCoordinator {
  private const val TAG = "AnalysisUpload"
  private const val MAX_ATTEMPTS = 3

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
  private var job: Job? = null

  sealed class State {
    data object Idle : State()
    data object Running : State()
    data object Success : State()
    data class Failed(val userMessage: String, val canRetry: Boolean) : State()
  }

  private val _state = MutableStateFlow<State>(State.Idle)
  val state: StateFlow<State> = _state.asStateFlow()

  fun reset() {
    job?.cancel()
    job = null
    _state.value = State.Idle
  }

  /** Запускает анализ, если ещё не идёт и не завершён успешно. */
  fun ensureStarted(historyRepository: HistoryRepository) {
    when (_state.value) {
      is State.Running, is State.Success -> return
      else -> start(historyRepository)
    }
  }

  fun retry(historyRepository: HistoryRepository) {
    if (_state.value is State.Running) return
    start(historyRepository)
  }

  private fun start(historyRepository: HistoryRepository) {
    job?.cancel()
    _state.value = State.Running
    job = scope.launch {
      val file = AnalysisSession.lastRecordedFile
      val analysisType = AnalysisSession.analysisType
      if (file == null || !file.exists()) {
        AnalysisSession.lastAnalysisResponse = null
        AnalysisSession.lastRawApiResponse = null
        AnalysisSession.lastResultJson = "Ошибка: Нет записанного аудио"
        _state.value = State.Failed("Нет записанного аудио", canRetry = false)
        return@launch
      }

      var lastError: Exception? = null
      repeat(MAX_ATTEMPTS) { attemptIndex ->
        val attempt = attemptIndex + 1
        try {
          val result = withContext(Dispatchers.IO) {
            val mime = AudioUploadHelper.resolveMimeForApi(file, AnalysisSession.lastAudioMimeType)
            Log.d(TAG, "analyze attempt=$attempt name=${file.name} size=${file.length()} mime=$mime")
            val requestFile = file.asRequestBody(
              mime.toMediaTypeOrNull() ?: "application/octet-stream".toMediaType()
            )
            val audioPart = MultipartBody.Part.createFormData("audio", file.name, requestFile)
            val analysisTypeBody = analysisType.toRequestBody("text/plain".toMediaTypeOrNull())
            VoxeraApiClient.api.analyze(audioPart, analysisTypeBody)
          }

          if (result.isSuccessful) {
            val body = result.body()
            AnalysisSession.lastAnalysisResponse = body
            AnalysisSession.lastResultJson = body?.let {
              GsonBuilder().setPrettyPrinting().create().toJson(it)
            } ?: AnalysisSession.lastRawApiResponse ?: result.raw().toString()
            if (body != null) {
              historyRepository.addEntry(
                analysisType = AnalysisSession.analysisType,
                response = body,
                rawApiResponse = AnalysisSession.lastRawApiResponse
              )
            }
            _state.value = State.Success
            return@launch
          }

          AnalysisSession.lastAnalysisResponse = null
          AnalysisSession.lastRawApiResponse = null
          val errBody = try {
            result.errorBody()?.string()?.takeIf { it.isNotBlank() }
          } catch (_: Exception) {
            null
          }
          val detail = errBody?.let { "\n\n$it" } ?: ""
          AnalysisSession.lastResultJson =
            "Ошибка API: ${result.code()} ${result.message()}$detail"
          // HTTP 5xx — можно повторить; 4xx — обычно нет
          if (result.code() in 500..599 && attempt < MAX_ATTEMPTS) {
            delay(1500L * attempt)
            return@repeat
          }
          _state.value = State.Failed(
            userMessage = "Сервер вернул ошибку (${result.code()}). Попробуйте ещё раз.",
            canRetry = true
          )
          return@launch
        } catch (e: Exception) {
          lastError = e
          Log.w(TAG, "analyze attempt=$attempt failed: ${e.message}", e)
          if (isTransientNetworkError(e) && attempt < MAX_ATTEMPTS) {
            delay(1500L * attempt)
          } else if (!isTransientNetworkError(e)) {
            AnalysisSession.lastAnalysisResponse = null
            AnalysisSession.lastRawApiResponse = null
            AnalysisSession.lastResultJson = "Ошибка: ${e.message ?: e.javaClass.simpleName}"
            _state.value = State.Failed(
              userMessage = "Не удалось выполнить анализ. Попробуйте ещё раз.",
              canRetry = true
            )
            return@launch
          }
        }
      }

      AnalysisSession.lastAnalysisResponse = null
      AnalysisSession.lastRawApiResponse = null
      AnalysisSession.lastResultJson =
        "Ошибка: ${lastError?.message ?: "нет связи с сервером"}"
      _state.value = State.Failed(
        userMessage = "Соединение прервалось (часто при сворачивании приложения). Нажмите «Повторить».",
        canRetry = true
      )
    }
  }

  private fun isTransientNetworkError(e: Exception): Boolean {
    if (e is SocketException || e is IOException) return true
    var c: Throwable? = e.cause
    while (c != null) {
      if (c is SocketException || c is IOException) return true
      c = c.cause
    }
    val msg = e.message?.lowercase().orEmpty()
    return msg.contains("connection abort") ||
      msg.contains("timeout") ||
      msg.contains("failed to connect") ||
      msg.contains("connection reset") ||
      msg.contains("broken pipe")
  }
}
