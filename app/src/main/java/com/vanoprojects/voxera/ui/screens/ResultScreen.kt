package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

@Composable
fun ResultScreen(
  onNewAnalysis: () -> Unit,
  onShare: () -> Unit,
  onHistory: () -> Unit
) {
  // Размеры шрифтов - можно легко менять
  val titleFontSize = 24.sp
  val subtitleFontSize = 16.sp
  
  Box(modifier = Modifier.fillMaxSize()) {
    // Фон bg_reverse_stars для ResultScreen
    Image(
      painter = painterResource(R.drawable.bg_reverse_stars),
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize()
    )
    
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp, vertical = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(34.dp))
      Text(
        text = "Результат",
        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 42.sp),
        color = VoxeraColors.TextPrimary,
        fontWeight = FontWeight.Light,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(48.dp))

      MetricCard(title = "Эмоциональный фон", value = "Напряжённый", progress = 0.72f)
      Spacer(modifier = Modifier.height(12.dp))
      MetricCard(title = "Уровень стресса", value = "Средний", progress = 0.55f)
      Spacer(modifier = Modifier.height(12.dp))
      MetricCard(title = "Внутренняя тревожность", value = "Средняя", progress = 0.58f)
      Spacer(modifier = Modifier.height(12.dp))
      MetricCard(title = "Ресурс", value = "Снижен", progress = 0.40f)

      Spacer(modifier = Modifier.weight(1f))

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(
          onClick = onShare,
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(16.dp),
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color(0xFF1E3A5F) // Темно-синий цвет для видимости на белом фоне
          ),
          border = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            color = Color(0xFF1E3A5F) // Темно-синяя граница
          )
        ) {
          Text("Поделиться", color = Color(0xFF1E3A5F))
        }

        Button(
          onClick = onNewAnalysis,
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(16.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF1E3A5F) // Темно-синий фон для видимости
          )
        ) {
          Text("Новый анализ", color = Color.White) // Белый текст на темном фоне
        }
      }
      Spacer(modifier = Modifier.height(10.dp))
    }
  }
}

@Composable
private fun MetricCard(title: String, value: String, progress: Float) {
  val cardHeight = 140.dp // Фиксированная высота карточки - можно легко менять
  val shape = RoundedCornerShape(16.dp) // Увеличено скругление для более кнопочного вида
  
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
        .clickable { }
        .padding(vertical = 28.dp, horizontal = 24.dp)
    ) {
      Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            title,
            color = VoxeraColors.TextSecondary,
            style = MaterialTheme.typography.bodyMedium
          )
          Text("›", color = VoxeraColors.TextSecondary)
        }
        Text(
          value,
          color = VoxeraColors.TextPrimary,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold
        )
        LinearProgressIndicator(
          progress = { progress },
          color = VoxeraColors.PrimaryGlow.copy(alpha = 0.7f),
          trackColor = VoxeraColors.PrimaryGlow.copy(alpha = 0.12f),
          modifier = Modifier.height(4.dp)
        )
      }
    }
  }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ResultScreenPreview() {
  VoxeraTheme {
    ResultScreen(
      onNewAnalysis = {},
      onShare = {},
      onHistory = {}
    )
  }
}
