package com.vanoprojects.voxera.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.util.Log
import com.vanoprojects.voxera.R
import com.vanoprojects.voxera.audio.AudioUploadHelper
import com.vanoprojects.voxera.data.AnalysisSession
import com.vanoprojects.voxera.data.HistoryRepository
import com.vanoprojects.voxera.data.api.VoxeraApiClient
import com.vanoprojects.voxera.ui.strings.LocalStrings
import com.vanoprojects.voxera.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@Composable
fun ProcessingScreen(
  historyRepository: HistoryRepository,
  onDone: () -> Unit
) {
  val theme = LocalVoxeraTheme.current
  val strings = LocalStrings.current
  
  LaunchedEffect(Unit) {
    val file = AnalysisSession.lastRecordedFile
    val analysisType = AnalysisSession.analysisType

    if (file == null || !file.exists()) {
      AnalysisSession.lastAnalysisResponse = null
      AnalysisSession.lastRawApiResponse = null
      AnalysisSession.lastResultJson = "Ошибка: Нет записанного аудио"
      onDone()
      return@LaunchedEffect
    }

    try {
      val result = withContext(Dispatchers.IO) {
        val mime = AudioUploadHelper.resolveMimeForApi(file, AnalysisSession.lastAudioMimeType)
        Log.d(
          "ProcessingScreen",
          "analyze upload name=${file.name} size=${file.length()} mime=$mime"
        )
        val requestFile = file.asRequestBody(
          mime.toMediaTypeOrNull() ?: "application/octet-stream".toMediaType()
        )
        val audioPart = MultipartBody.Part.createFormData("audio", file.name, requestFile)
        val analysisTypeBody = analysisType.toRequestBody("text/plain".toMediaTypeOrNull())
        VoxeraApiClient.api.analyze(audioPart, analysisTypeBody)
      }

      if (result.isSuccessful) {
        val body = result.body()
        Log.d("ProcessingScreen", "Success: body=${body != null}, lastRawApiResponse=${AnalysisSession.lastRawApiResponse != null}, result.description=${body?.result?.description?.length ?: 0}")
        AnalysisSession.lastAnalysisResponse = body
        AnalysisSession.lastResultJson = body?.let {
          com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(it)
        } ?: AnalysisSession.lastRawApiResponse ?: result.raw().toString()
        if (body != null) {
          historyRepository.addEntry(
            analysisType = AnalysisSession.analysisType,
            response = body,
            rawApiResponse = AnalysisSession.lastRawApiResponse
          )
        }
      } else {
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
      }
    } catch (e: Exception) {
      e.printStackTrace()
      AnalysisSession.lastAnalysisResponse = null
      AnalysisSession.lastRawApiResponse = null
      AnalysisSession.lastResultJson = "Ошибка: ${e.message}\n\n${e.stackTraceToString()}"
    }
    onDone()
  }

  Box(modifier = Modifier.fillMaxSize()) {
    val backgroundRes = when (theme.type) {
      ThemeType.GLASS -> R.drawable.bg_stars
      ThemeType.LIGHT -> R.drawable.bg_light_reverse
    }
    Image(
      painter = painterResource(backgroundRes),
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize()
    )
    
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(24.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Красивая анимация загрузки
      ProcessingAnimation()
      Spacer(modifier = Modifier.height(24.dp))
      val colors = theme.colors
      
      Text(
        text = strings.analyzing,
        style = MaterialTheme.typography.headlineSmall,
        color = colors.backgroundTextPrimary,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = strings.analyzingSubtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = colors.backgroundTextSecondary
      )
    }
  }
}

@Composable
private fun ProcessingAnimation() {
  val infiniteTransition = rememberInfiniteTransition(label = "processing")
  
  val rotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(2000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "rotation"
  )
  
  val scale by infiniteTransition.animateFloat(
    initialValue = 0.95f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(1500, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "scale"
  )

  Box(
    modifier = Modifier.size(80.dp),
    contentAlignment = Alignment.Center
  ) {
    Canvas(
      modifier = Modifier
        .fillMaxSize()
        .graphicsLayer {
          rotationZ = rotation
        }
    ) {
      val center = Offset(size.width / 2, size.height / 2)
      val radius = size.minDimension / 2 * scale
      
      // Внешнее кольцо с градиентом
      drawCircle(
        brush = Brush.sweepGradient(
          colors = listOf(
            VoxeraColors.PrimaryGlow.copy(alpha = 0.0f),
            VoxeraColors.PrimaryGlow.copy(alpha = 0.6f),
            VoxeraColors.PrimaryGlow.copy(alpha = 1.0f),
            VoxeraColors.PrimaryGlow.copy(alpha = 0.6f),
            VoxeraColors.PrimaryGlow.copy(alpha = 0.0f)
          )
        ),
        radius = radius,
        center = center,
        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
      )
      
      // Внутреннее кольцо
      drawCircle(
        color = VoxeraColors.PrimaryGlow.copy(alpha = 0.3f),
        radius = radius * 0.7f,
        center = center,
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
      )
    }
  }
}
