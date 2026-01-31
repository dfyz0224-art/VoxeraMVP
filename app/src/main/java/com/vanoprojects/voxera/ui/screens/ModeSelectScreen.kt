package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vanoprojects.voxera.R
import com.vanoprojects.voxera.ui.theme.VoxeraTheme
import com.vanoprojects.voxera.ui.theme.VoxeraColors

@Composable
fun ModeSelectScreen(
  onBack: () -> Unit,
  onModeChosen: (String) -> Unit
) {
  // Фон bg_reverse для ModeSelect
  Box(modifier = Modifier.fillMaxSize()) {
    Image(
      painter = painterResource(R.drawable.bg_reverse_stars),
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize()
    )
    
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
      Spacer(modifier = Modifier.height(48.dp))
      
      // Логотип ic_voxera_logo_text - увеличенный размер, по центру
      Box(
        modifier = Modifier
          .height(70.dp)
          .wrapContentHeight(),
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = painterResource(R.drawable.ic_voxera_logo_text),
          contentDescription = null,
          modifier = Modifier
            .fillMaxWidth(),
          contentScale = ContentScale.Fit
        )
      }
      
      Spacer(modifier = Modifier.height(28.dp))
      
      // Заголовок "Выберите режим" под логотипом, перед карточками - по центру, ближе к карточкам
      Text(
        text = "Выберите режим",
        style = MaterialTheme.typography.headlineSmall,
        color = VoxeraColors.TextPrimary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
      )
      
      Spacer(modifier = Modifier.height(12.dp))

      // Карточки с фиксированной высотой
      ModeCard(
        iconRes = R.drawable.ic_mother,
        title = "Я — родитель",
        subtitle = "Поддержка и оценка перегруза, усталости, тревожности",
        onClick = { onModeChosen("mom") }
      )
      Spacer(modifier = Modifier.height(16.dp))
      ModeCard(
        iconRes = R.drawable.ic_teen,
        title = "Я — тинейджер",
        subtitle = "Короткая оценка настроения и внутреннего напряжения",
        onClick = { onModeChosen("teen") }
      )
      Spacer(modifier = Modifier.height(16.dp))
      ModeCard(
        iconRes = R.drawable.ic_quick,
        title = "Быстрый анализ",
        subtitle = "Универсальная оценка за 15–30 секунд",
        onClick = { onModeChosen("quick") }
      )

      Spacer(modifier = Modifier.weight(1f))
      OutlinedButton(
        onClick = { onModeChosen("history") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
          contentColor = Color.Black
        ),
        border = androidx.compose.foundation.BorderStroke(
          width = 1.5.dp,
          color = Color.Black.copy(alpha = 0.3f)
        )
      ) {
        Text("История", color = Color.Black)
      }
      Spacer(modifier = Modifier.height(10.dp))
    }
  }
}

@Composable
private fun ModeCard(
  iconRes: Int,
  title: String,
  subtitle: String,
  onClick: () -> Unit
) {
  val cardHeight = 140.dp // Фиксированная высота карточки - можно легко менять
  val shape = RoundedCornerShape(16.dp) // Увеличено скругление для более кнопочного вида
  
  // Box для позиционирования карточки и тени
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(cardHeight)
  ) {
    // Мягкая тень со всех сторон через Canvas
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
          cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius + spread)
        )
      }
    }
    
    // Сама карточка поверх тени - без shadow модификатора
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
        .clickable(onClick = onClick)
        .padding(vertical = 28.dp, horizontal = 24.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Иконка слева - еще больше
        Image(
          painter = painterResource(iconRes),
          contentDescription = null,
          modifier = Modifier.size(88.dp), // Увеличено до 88.dp
          colorFilter = ColorFilter.tint(VoxeraColors.PrimaryGlow.copy(alpha = 0.9f))
        )
        Spacer(modifier = Modifier.width(24.dp))
        
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
        Text(
          text = "›",
          style = MaterialTheme.typography.titleLarge,
          color = VoxeraColors.PrimaryGlow.copy(alpha = 0.7f),
          modifier = Modifier.padding(start = 8.dp)
        )
      }
    }
  }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ModeSelectScreenPreview() {
  VoxeraTheme {
    ModeSelectScreen(
      onBack = {},
      onModeChosen = {}
    )
  }
}
