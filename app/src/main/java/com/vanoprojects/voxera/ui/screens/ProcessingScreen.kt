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
import com.vanoprojects.voxera.R
import com.vanoprojects.voxera.ui.theme.VoxeraTheme
import com.vanoprojects.voxera.ui.theme.VoxeraColors
import kotlinx.coroutines.delay

@Composable
fun ProcessingScreen(onDone: () -> Unit) {
  LaunchedEffect(Unit) {
    delay(2000) // 1-2 секунды
    onDone()
  }

  Box(modifier = Modifier.fillMaxSize()) {
    // Фон bg_clean для ProcessingScreen
    Image(
      painter = painterResource(R.drawable.bg_clean),
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
      Text(
        text = "Анализируем",
        style = MaterialTheme.typography.headlineSmall,
        color = VoxeraColors.TextPrimary,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Это займёт несколько секунд",
        style = MaterialTheme.typography.bodyMedium,
        color = VoxeraColors.TextSecondary
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
