package com.vanoprojects.voxera.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vanoprojects.voxera.R
import com.vanoprojects.voxera.data.AnalysisUploadCoordinator
import com.vanoprojects.voxera.data.HistoryRepository
import com.vanoprojects.voxera.ui.strings.LocalStrings
import com.vanoprojects.voxera.ui.theme.*

@Composable
fun ProcessingScreen(
  historyRepository: HistoryRepository,
  onDone: () -> Unit
) {
  val theme = LocalVoxeraTheme.current
  val strings = LocalStrings.current
  val colors = theme.colors
  val uploadState by AnalysisUploadCoordinator.state.collectAsState()
  var didNavigate by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    AnalysisUploadCoordinator.ensureStarted(historyRepository)
  }

  LaunchedEffect(uploadState) {
    if (!didNavigate && uploadState is AnalysisUploadCoordinator.State.Success) {
      didNavigate = true
      onDone()
    }
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
      when (val s = uploadState) {
        is AnalysisUploadCoordinator.State.Failed -> {
          Text(
            text = strings.analyzing,
            style = MaterialTheme.typography.headlineSmall,
            color = colors.backgroundTextPrimary,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = s.userMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.backgroundTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
          )
          if (s.canRetry) {
            Spacer(modifier = Modifier.height(24.dp))
            ThemedFilledButton(
              text = strings.analyzeRetry,
              onClick = { AnalysisUploadCoordinator.retry(historyRepository) },
              modifier = Modifier.fillMaxWidth(0.7f)
            )
          }
        }
        else -> {
          ProcessingAnimation()
          Spacer(modifier = Modifier.height(24.dp))
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
            color = colors.backgroundTextSecondary,
            textAlign = TextAlign.Center
          )
        }
      }
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

      drawCircle(
        color = VoxeraColors.PrimaryGlow.copy(alpha = 0.3f),
        radius = radius * 0.7f,
        center = center,
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
      )
    }
  }
}
