package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanoprojects.voxera.R
import com.vanoprojects.voxera.ui.theme.VoxeraTheme
import com.vanoprojects.voxera.ui.theme.VoxeraColors
import io.github.fletchmckee.liquid.liquid
import io.github.fletchmckee.liquid.liquefiable
import io.github.fletchmckee.liquid.rememberLiquidState

@Composable
fun ShareScreen(onBack: () -> Unit) {
  val liquidState = rememberLiquidState()
  
  Box(modifier = Modifier.fillMaxSize()) {
    // Фон bg_clean - должен быть liquefiable для liquid эффекта
    Image(
      painter = painterResource(R.drawable.bg_clean),
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier = Modifier
        .fillMaxSize()
        .liquefiable(liquidState)
    )
    
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
      Spacer(modifier = Modifier.height(18.dp))
      // Кастомный заголовок - больше и темно-синего цвета, по центру
      Text(
        text = "Поделиться результатом",
        color = Color(0xFF1E3A5F), // Темно-синий цвет
        style = MaterialTheme.typography.headlineMedium.copy(
          fontSize = 42.sp, // Увеличенный размер
          fontWeight = FontWeight.Light,
          lineHeight = 40.sp // Увеличенное расстояние между строками
        ),
        modifier = Modifier.height(95.dp),
        textAlign = TextAlign.Center
      )
      Spacer(modifier = Modifier.height(22.dp))
      Text(
        text = "Публикуется только краткая карточка без деталей",
        color = Color(0xFF1E3A5F), // Темно-синий цвет
        style = MaterialTheme.typography.bodyLarge.copy(
          fontSize = 18.sp, // Увеличенный размер
          fontWeight = FontWeight.Normal
        ),
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
      )
      Spacer(modifier = Modifier.height(32.dp))

      // Preview card в стиле ModeSelect - большая карточка
      SharePreviewCard()

      Spacer(modifier = Modifier.weight(1f))

      // Быстрые кнопки
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        ShareButton(
          label = "TikTok",
          iconRes = R.drawable.ic_tiktok,
          liquidState = liquidState,
          onClick = { },
          modifier = Modifier.weight(1f)
        )
        ShareButton(
          label = "Instagram",
          iconRes = R.drawable.ic_instagram,
          liquidState = liquidState,
          onClick = { },
          modifier = Modifier.weight(1f)
        )
        ShareButton(
          label = "Telegram",
          iconRes = R.drawable.ic_telegram,
          liquidState = liquidState,
          onClick = { },
          modifier = Modifier.weight(1f)
        )
        ShareButton(
          label = "Ссылка",
          iconRes = R.drawable.ic_link,
          liquidState = liquidState,
          onClick = { },
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(18.dp))
      Button(
        onClick = { },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
          containerColor = Color.White // Белый фон
        ),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(vertical = 14.dp)
      ) {
        Text("Ещё…", color = Color(0xFF071223)) // Темно-синий текст
      }
      Spacer(modifier = Modifier.height(10.dp))
    }
  }
}

@Composable
private fun SharePreviewCard() {
  val cardHeight = 200.dp // Увеличенная высота карточки
  val shape = RoundedCornerShape(16.dp)
  
  // Box для позиционирования карточки и тени
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(cardHeight)
  ) {
    // Мягкая тень со всех сторон через Canvas (как в ModeSelect)
    Canvas(
      modifier = Modifier.fillMaxSize()
    ) {
      val shadowSpread = 12.dp.toPx()
      val cardWidth = size.width
      val cardHeight = size.height
      val cornerRadius = 19.dp.toPx()
      
      // Рисуем несколько слоев тени для создания мягкого эффекта
      for (i in 1..8) {
        val spread = shadowSpread * (i / 8f)
        val alpha = (0.25f / i).coerceAtMost(0.15f)
        
        // Рисуем скругленный прямоугольник с расширением во все стороны
        drawRoundRect(
          color = Color.Black.copy(alpha = alpha),
          topLeft = Offset(-spread, -spread + spread * 0.2f), // Небольшое смещение вниз
          size = Size(cardWidth + spread * 2, cardHeight + spread * 2),
          cornerRadius = CornerRadius(cornerRadius + spread)
        )
      }
    }
    
    // Сама карточка поверх тени - без shadow модификатора (как в ModeSelect)
    Box(
      modifier = Modifier
        .fillMaxSize()
        .clip(shape)
        .background(
          // Градиентный фон для более выразительного вида
          brush = Brush.linearGradient(
            colors = listOf(
              VoxeraColors.CardBackground.copy(alpha = 0.95f),
              VoxeraColors.CardBackground.copy(alpha = 0.85f)
            )
          ),
          shape = shape
        )
        .border(
          width = 1.3.dp,
          color = VoxeraColors.PrimaryGlow.copy(alpha = 0.53f),
          shape = shape
        )
        .padding(vertical = 28.dp, horizontal = 24.dp)
    ) {
      Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
      ) {
        Image(
          painter = painterResource(R.drawable.ic_voxera_logo_text),
          contentDescription = null,
          modifier = Modifier.height(44.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          "Состояние",
          color = VoxeraColors.TextSecondary,
          style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          "Стабильнее, лёгкое напряжение",
          color = VoxeraColors.TextPrimary,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Normal // Убрали SemiBold
        )
      }
    }
  }
}

@Composable
private fun ShareButton(
  label: String,
  iconRes: Int,
  liquidState: io.github.fletchmckee.liquid.LiquidState,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val buttonShape = RoundedCornerShape(14.dp)
  
  Box(
    modifier = modifier
      .height(100.dp) // Увеличена высота кнопок
      .clip(buttonShape)
      .clickable(onClick = onClick)
      .liquid(liquidState) {
        shape = buttonShape
        // Liquid glass эффект - прозрачное стекло с искажением фона
        frost = 1.5.dp // Легкая мутность для эффекта стекла
        refraction = 0.8f // Искажение фона
        curve = 0.4f
        edge = 0.05f
        tint = Color.White.copy(alpha = 0.0f) // Без оттенка
        saturation = 1.0f
        dispersion = 0.6f // Искажение фона
      },
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
    ) {
      Image(
        painter = painterResource(iconRes),
        contentDescription = null,
        modifier = Modifier.size(50.dp), // Увеличен размер иконок
        colorFilter = ColorFilter.tint(Color.White) // Белые иконки
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.bodySmall,
        fontSize = 11.sp, // Немного увеличен размер текста
        color = Color.White // Белый текст
      )
    }
  }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ShareScreenPreview() {
  VoxeraTheme {
    ShareScreen(onBack = {})
  }
}
