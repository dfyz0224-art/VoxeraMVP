package com.vanoprojects.voxera.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.vanoprojects.voxera.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

/** Параметры анимации — меняй здесь (AppSplashScreen.kt) */
private object AppSplashConfig {
  const val LOGO_SIZE_DP = 160          // размер лого
  const val GLOW_SIZE_FACTOR = 1.45f     // размер свечения относительно лого (1.4 = на 40% больше)
  const val GLOW_BLUR_DP = 18          // размытие свечения (радиус blur)
  const val INITIAL_DELAY_MS = 100L     // пауза перед вспышкой
  const val FLASH_DURATION_MS = 700     // длительность вспышки
  const val FADEOUT_DURATION_MS = 400   // длительность исчезновения лого
}

/**
 * Внутриприложенный сплеш: лого на чёрном фоне, визуально продолжение системного.
 * Лого вспыхивает и исчезает, затем вызывается onComplete.
 */
@Composable
fun AppSplashScreen(
  onComplete: () -> Unit
) {
  val logoAlpha = remember { Animatable(1f) }
  val flashAlpha = remember { Animatable(0f) }

  LaunchedEffect(Unit) {
    try {
      delay(AppSplashConfig.INITIAL_DELAY_MS)
      // Вспышка: яркий белый оверлей на лого
      flashAlpha.animateTo(1f, animationSpec = tween(AppSplashConfig.FLASH_DURATION_MS / 2))
      flashAlpha.animateTo(0f, animationSpec = tween(AppSplashConfig.FLASH_DURATION_MS / 2))
      // Исчезновение лого
      logoAlpha.animateTo(0f, animationSpec = tween(AppSplashConfig.FADEOUT_DURATION_MS))
      onComplete()
    } catch (e: CancellationException) {
      throw e
    } catch (_: Exception) {
      onComplete()
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black),
    contentAlignment = Alignment.Center
  ) {
    Box(contentAlignment = Alignment.Center) {
      // Вспышка — свечение по форме X (то же изображение, крупнее, с blur, под лого)
      if (flashAlpha.value > 0.001f) {
        Image(
          painter = painterResource(R.drawable.ic_x_white),
          contentDescription = null,
          colorFilter = ColorFilter.tint(Color.White),
          modifier = Modifier
            .size((AppSplashConfig.LOGO_SIZE_DP * AppSplashConfig.GLOW_SIZE_FACTOR).toInt().dp)
            .blur(AppSplashConfig.GLOW_BLUR_DP.dp)
            .graphicsLayer { alpha = flashAlpha.value * 0.9f }
        )
      }
      // Лого — ic_x_white (PNG)
      Image(
        painter = painterResource(R.drawable.ic_x_white),
        contentDescription = null,
        modifier = Modifier
          .size(AppSplashConfig.LOGO_SIZE_DP.dp)
          .graphicsLayer { alpha = logoAlpha.value }
      )
    }
  }
}
