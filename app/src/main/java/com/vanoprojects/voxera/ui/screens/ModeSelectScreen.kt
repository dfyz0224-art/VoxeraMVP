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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.sp
import com.vanoprojects.voxera.R
import com.vanoprojects.voxera.ui.theme.*
import com.vanoprojects.voxera.ui.theme.TextWithShadow
import io.github.fletchmckee.liquid.liquid
import io.github.fletchmckee.liquid.liquefiable
import io.github.fletchmckee.liquid.rememberLiquidState

private const val ONBOARDING_TEXT_1 = "Voxera — это интеллектуальная система анализа голоса, построенная на технологиях искусственного интеллекта, цифровой обработки звука и машинного обучения. Она считывает краткие фрагменты вашей речи и оценивает состояние человека по многим параметрам, связанным с эмоциями, физиологией и поведением."
private const val ONBOARDING_TEXT_2 = "Голос — это не просто слова: он отражает работу дыхания, мышц, нервной системы и эмоций. Именно эти признаки Voxera анализирует, чтобы понять ваш уровень стресса, эмоциональный фон, когнитивную нагрузку, энергетическое состояние и стабильность поведения."

@Composable
fun ModeSelectScreen(
  onBack: () -> Unit,
  onModeChosen: (String) -> Unit,
  onOpenSettings: () -> Unit = {},
  onboardingCompleted: Boolean = true,
  onOnboardingComplete: () -> Unit = {}
) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val liquidState = rememberLiquidState()
  var showOnboardingOverlay by remember { mutableStateOf(true) } // Локальное состояние: при "Начать" скрываем карточку

  // Всё содержимое под карточкой должно быть liquefiable для корректного стеклянного эффекта
  Box(modifier = Modifier.fillMaxSize()) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .then(if (showOnboardingOverlay) Modifier.liquefiable(liquidState) else Modifier)
    ) {
      when (theme.type) {
        ThemeType.LIGHT -> {
          Image(
            painter = painterResource(R.drawable.bg_light),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        }
        ThemeType.GLASS -> {
          Image(
            painter = painterResource(R.drawable.bg_stars),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        }
        ThemeType.DARK -> {
          Image(
            painter = painterResource(R.drawable.bg_reverse_stars),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        }
      }

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
          contentScale = ContentScale.Fit,

        )
      }
      
      Spacer(modifier = Modifier.height(28.dp))
      
      // Заголовок "Выберите режим" под логотипом, перед карточками - по центру, ближе к карточкам
      // В светлой теме - белый, в остальных - backgroundTextPrimary
      val titleColor = if (theme.type == ThemeType.LIGHT) {
        Color.White
      } else {
        colors.backgroundTextPrimary
      }
      Text(
        text = "Выберите режим",
        style = MaterialTheme.typography.headlineSmall,
        color = titleColor,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
      )
      
      Spacer(modifier = Modifier.height(12.dp))

      // Карточки с фиксированной высотой
      ModeCard(
        iconRes = R.drawable.ic_mother,
        title = "Родительский режим",
        subtitle = "Поддержка и оценка перегруза, усталости, тревожности",
        onClick = { onModeChosen("mom") },
        gradientIndex = 0
      )
      Spacer(modifier = Modifier.height(16.dp))
      ModeCard(
        iconRes = R.drawable.ic_teen,
        title = "Универсальный режим",
        subtitle = "Общая оценка состояния",
        onClick = { onModeChosen("teen") },
        gradientIndex = 1
      )
      Spacer(modifier = Modifier.height(16.dp))
      ModeCard(
        iconRes = R.drawable.ic_quick,
        title = "Глубокий анализ",
        subtitle = "Подробная оценка и психоэмоциональный портрет",
        onClick = { onModeChosen("quick") },
        gradientIndex = 2
      )

      Spacer(modifier = Modifier.weight(1f))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        ThemedOutlinedButton(
          text = "История",
        onClick = { onModeChosen("history") },
          modifier = Modifier.weight(1f)
        )
        ThemedFilledButton(
          text = "Настройки",
          onClick = onOpenSettings,
          modifier = Modifier.weight(1f)
        )
      }
      Spacer(modifier = Modifier.height(10.dp))
    }
    }

    // Онбординг: затемнение фона + стеклянная карточка поверх экрана
    if (showOnboardingOverlay) {
      // Затемнение фона
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.8f))
      )
      var onboardingStep by remember { mutableStateOf(0) }
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(24.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.fillMaxWidth()
        ) {
          // Стеклянная карточка: прозрачная, frost, искажение фона
          val cardShape = RoundedCornerShape(24.dp)
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(400.dp)
              .clip(cardShape)
              .liquid(liquidState) {
                shape = cardShape
                frost = 1.2.dp
                refraction = 0.1f
                curve = 0.1f
                edge = 0.08f
                tint = Color.Black.copy(alpha = 0.52f)
                saturation = 0.55f
                dispersion = 0.05f
              }
              .padding(32.dp)
          ) {
            Text(
              text = if (onboardingStep == 0) ONBOARDING_TEXT_1 else ONBOARDING_TEXT_2,
              style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                lineHeight = 24.sp
              ),
              color = Color.White,
              modifier = Modifier.fillMaxSize()
            )
          }
          Spacer(modifier = Modifier.height(24.dp))
          // Кнопка: "Далее" или "Начать"
          val buttonBg = if (theme.type == ThemeType.LIGHT) {
            Color(0xFF2E5F9E).copy(alpha = 0.9f)
          } else {
            Color.White.copy(alpha = 0.25f)
          }
          val buttonTextColor = Color.White
          if (onboardingStep == 0) {
            Button(
              onClick = { onboardingStep = 1 },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = buttonBg)
            ) {
              Text("Далее", color = buttonTextColor)
            }
          } else {
            Button(
              onClick = {
                showOnboardingOverlay = false
                onOnboardingComplete()
              },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = buttonBg)
            ) {
              Text("Начать", color = buttonTextColor)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ModeCard(
  iconRes: Int,
  title: String,
  subtitle: String,
  onClick: () -> Unit,
  gradientIndex: Int
) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  
  ThemedCard(onClick = onClick, gradientIndex = gradientIndex) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Иконка слева - еще больше
      Image(
        painter = painterResource(iconRes),
        contentDescription = null,
        modifier = Modifier.size(88.dp),
        colorFilter = ColorFilter.tint(colors.primaryGlow.copy(alpha = 0.9f))
        )
      Spacer(modifier = Modifier.width(24.dp))
      
      Column(modifier = Modifier.weight(1f)) {
        TextWithShadow(
        text = title,
        style = MaterialTheme.typography.titleMedium,
          color = colors.textPrimary,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.height(6.dp))
        TextWithShadow(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium,
          color = colors.textSecondary
        )
      }
      TextWithShadow(
        text = "›",
        style = MaterialTheme.typography.titleLarge,
        color = colors.primaryGlow.copy(alpha = 0.7f),
        modifier = Modifier.padding(start = 8.dp)
      )
    }
  }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ModeSelectScreenPreview() {
  VoxeraTheme {
    ModeSelectScreen(
      onBack = {},
      onModeChosen = {},
      onboardingCompleted = true
    )
  }
}
