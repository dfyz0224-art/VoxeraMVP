package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
fun SettingsScreen(
  onBack: () -> Unit,
  onAbout: () -> Unit,
  onHelp: () -> Unit
) {
  var keepHistory by remember { mutableStateOf(true) }
  var shareMinimal by remember { mutableStateOf(true) }

  VoxeraBackground {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(20.dp)
    ) {
      Spacer(modifier = Modifier.height(10.dp))
      TopBar(title = "Настройки", onBack = onBack)
      Spacer(modifier = Modifier.height(16.dp))

      SettingCard(
        title = "Сохранять историю",
        subtitle = "Хранить результаты локально на устройстве",
        checked = keepHistory,
        onChecked = { keepHistory = it }
      )
      Spacer(modifier = Modifier.height(16.dp))
      SettingCard(
        title = "Делиться только кратко",
        subtitle = "Без деталей и чувствительных данных",
        checked = shareMinimal,
        onChecked = { shareMinimal = it }
      )

      Spacer(modifier = Modifier.height(18.dp))
      OutlinedButton(
        onClick = onAbout,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E3A5F))
      ) { Text("О приложении", color = Color(0xFF1E3A5F)) }

      Spacer(modifier = Modifier.height(8.dp))
      OutlinedButton(
        onClick = onHelp,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E3A5F))
      ) { Text("Помощь", color = Color(0xFF1E3A5F)) }
    }
  }
}

@Composable
private fun SettingCard(
  title: String,
  subtitle: String,
  checked: Boolean,
  onChecked: (Boolean) -> Unit
) {
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
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = VoxeraColors.TextPrimary,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = VoxeraColors.TextSecondary
          )
        }
        Switch(
          checked = checked,
          onCheckedChange = onChecked
        )
      }
    }
  }
}
