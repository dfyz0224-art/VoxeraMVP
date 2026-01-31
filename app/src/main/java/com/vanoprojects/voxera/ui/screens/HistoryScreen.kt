package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vanoprojects.voxera.ui.theme.VoxeraColors

@Composable
fun HistoryScreen(
  onBack: () -> Unit,
  onOpenSettings: () -> Unit
) {
  VoxeraBackground {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(20.dp)
    ) {
      Spacer(modifier = Modifier.height(10.dp))
      TopBar(title = "История", onBack = onBack)
      Spacer(modifier = Modifier.height(14.dp))

      HistoryItem(date = "Сегодня", state = "Лёгкое напряжение", stress = "Средний")
      Spacer(modifier = Modifier.height(10.dp))
      HistoryItem(date = "Вчера", state = "Стабильное", stress = "Низкий")
      Spacer(modifier = Modifier.height(10.dp))
      HistoryItem(date = "2 дня назад", state = "Перегруз", stress = "Высокий")

      Spacer(modifier = Modifier.weight(1f))
      OutlinedButton(
        onClick = onOpenSettings,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
      ) {
        Text("Настройки")
      }
    }
  }
}

@Composable
private fun HistoryItem(date: String, state: String, stress: String) {
  val cardHeight = 140.dp // Фиксированная высота карточки как в ModeSelect
  val shape = RoundedCornerShape(16.dp) // Скругление как в ModeSelect
  
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
    
    // Сама карточка поверх тени - стиль как в ModeSelect
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
      Column {
        Text(
          text = date,
          color = VoxeraColors.TextSecondary,
          style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = state,
          color = VoxeraColors.TextPrimary,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Стресс: $stress",
          color = VoxeraColors.TextSecondary,
          style = MaterialTheme.typography.bodyMedium
        )
      }
    }
  }
}
