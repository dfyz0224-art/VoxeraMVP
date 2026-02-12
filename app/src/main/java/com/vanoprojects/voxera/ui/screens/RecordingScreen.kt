package com.vanoprojects.voxera.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.vanoprojects.voxera.R
import com.vanoprojects.voxera.ui.theme.VoxeraTheme
import com.vanoprojects.voxera.ui.theme.VoxeraColors
import com.vanoprojects.voxera.ui.theme.LocalVoxeraTheme
import com.vanoprojects.voxera.ui.theme.ThemeType
import io.github.fletchmckee.liquid.liquid
import io.github.fletchmckee.liquid.liquefiable
import io.github.fletchmckee.liquid.rememberLiquidState
import kotlinx.coroutines.delay

@Composable
fun RecordingScreen(
    onGoProcessing: () -> Unit, // переход на processing должен быть ПОСЛЕ отпускания
) {
    val liquidState = rememberLiquidState()

    var isHolding by remember { mutableStateOf(false) }
    var holdStartTime by remember { mutableStateOf(0L) }
    var isTransitioning by remember { mutableStateOf(false) } // Флаг перехода на Processing
    val HOLD_THRESHOLD_MS = 300L // 0.3 секунды

    // Breathing эффект - сильнее при удержании
    val breathingIdle by rememberInfiniteTransition(label = "breathingIdle").animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingIdle"
    )
    
    val breathingHold by rememberInfiniteTransition(label = "breathingHold").animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingHold"
    )
    
    val breathing = if (isHolding) breathingHold else breathingIdle
    val theme = LocalVoxeraTheme.current

    Box(modifier = Modifier.fillMaxSize()) {
        // Фон должен быть liquefiable, чтобы liquid мог "сэмплить" пиксели
        // Для стеклянной темы используем bg_stars, для светлой - bg_light_reverse, для темной - bg_clean
        val backgroundRes = when (theme.type) {
            ThemeType.GLASS -> R.drawable.bg_stars
            ThemeType.LIGHT -> R.drawable.bg_light_reverse
            ThemeType.DARK -> R.drawable.bg_clean
        }
        Image(
            painter = painterResource(backgroundRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .liquefiable(liquidState)
        )


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            // Центр экрана — кнопка (на том же месте)
            Spacer(Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(), // Полная высота для колец, которые уходят за края
                contentAlignment = Alignment.Center
            ) {
                // Кольца-волны только во время удержания (за кнопкой)
                // Синхронизированы с дыханием кнопки
                if (isHolding) {
                    WaterRings(baseSize = 200.dp, breathing = breathing)
                }

                // Кнопка на своем месте (центр экрана)
                LiquidRecordButtonHold(
                    liquidState = liquidState,
                    isHolding = isHolding,
                    breathing = breathing,
                    onHoldStart = {
                        holdStartTime = System.currentTimeMillis()
                        isHolding = true
                    },
                    onHoldEnd = {
                        val holdDuration = System.currentTimeMillis() - holdStartTime
                        isHolding = false
                        // Если удержание >= 0.3 сек, переходим на processing
                        if (holdDuration >= HOLD_THRESHOLD_MS) {
                            isTransitioning = true // Устанавливаем флаг перехода
                            onGoProcessing()
                        }
                    }
                )
                
                // Заголовок и текст над кнопкой - абсолютное позиционирование
                // Текст исчезает при удержании кнопки и не появляется при переходе
                val textAlpha by animateFloatAsState(
                    targetValue = if (isHolding || isTransitioning) 0f else 1f,
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                    label = "textAlpha"
                )
                
                if (textAlpha > 0.01f) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = (-200).dp), // Смещаем выше, чтобы не перекрывать кнопку
                            horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Заголовок по центру - цвет зависит от темы
                        val titleColor = when (theme.type) {
                            ThemeType.LIGHT -> Color(0xFF0D1B3A) // Темно-синий для светлой темы
                            ThemeType.DARK -> Color(0xFF0D1B3A) // Темно-синий для темной темы
                            else -> Color.White
                        }
                        Text(
                            text = "Запись голоса",
                            color = titleColor.copy(alpha = textAlpha),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Normal,
                                letterSpacing = 0.04.em,
                                lineHeight = 42.sp,
                            ),
                            modifier = Modifier.padding(bottom = 20.dp),
                            textAlign = TextAlign.Center
                        )

                        // Текст над кнопкой - цвет зависит от темы
                        val descriptionColor = when (theme.type) {
                            ThemeType.LIGHT -> Color(0xFF0A1628) // Темный для светлой темы
                            ThemeType.DARK -> Color(0xFF0A1628) // Темный для темной темы
                            else -> Color.White
                        }
                        Text(
                            text = "Скажите 2–3 предложения о том, как прошёл ваш день.",
                            color = descriptionColor.copy(alpha = textAlpha),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Normal,
                                lineHeight = 24.sp
                            ),
                            modifier = Modifier.padding(bottom = 100.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // Цвет текста зависит от темы
            val timeTextColor = when (theme.type) {
                ThemeType.LIGHT -> Color(0xFF1A2F4A) // Темно-синий для светлой темы
                ThemeType.DARK -> Color(0xFF1A2F4A) // Темно-синий для темной темы
                else -> Color.White
            }
            Text(
                text = "15–30 секунд",
                color = timeTextColor,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(10.dp))
        }
    }
}


@Composable
private fun WaterRings(baseSize: Dp, breathing: Float) {
    // 3 кольца появляются одновременно при каждом "вдохе"
    val ringCount = 3
    // Период breathing кнопки при удержании = 1200ms (от 0.94 до 1.06 и обратно)
    val breathingCycle = 1200
    // Длительность расширения кольца - разумная длительность для видимости на экране
    val ringDuration = 3500 // Кольца видны ~2-3 секунды, этого достаточно
    
    // Отслеживаем циклы breathing - запускаем кольца при каждом дыхании
    var previousBreathing by remember { mutableStateOf(breathing) }
    var cycleKey by remember { mutableStateOf(0) }
    
    // Определяем момент "вдоха" - когда breathing достигает минимума (0.94) и начинает расти
    // Используем более точную логику для синхронизации с каждым дыханием
    LaunchedEffect(breathing) {
        val minThreshold = 0.945f // Близко к минимуму 0.94
        val wasBelow = previousBreathing < minThreshold
        val nowAbove = breathing >= minThreshold
        val isGrowing = breathing > previousBreathing
        
        // Если breathing перешел снизу вверх через threshold - это "вдох"
        // Запускаем кольца при каждом дыхании без ограничений по времени
        if (wasBelow && nowAbove && isGrowing) {
            cycleKey++ // Увеличиваем ключ для перезапуска анимаций
        }
        previousBreathing = breathing
    }
    
    // Создаём анимации для каждого кольца - перезапускаются при каждом цикле (cycleKey)
    // Добавляем задержку между кольцами, чтобы они были видны отдельно
    val ringGap = 200 // мс между кольцами для визуального зазора
    val transition0 = rememberInfiniteTransition(label = "waterRing_0_$cycleKey")
    val transition1 = rememberInfiniteTransition(label = "waterRing_1_$cycleKey")
    val transition2 = rememberInfiniteTransition(label = "waterRing_2_$cycleKey")
    
    val radiusProgress0 by transition0.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(ringDuration, delayMillis = 0, easing = FastOutSlowInEasing), // Плавный easing для плавности
            repeatMode = RepeatMode.Restart
        ),
        label = "radius_0"
    )
    val radiusProgress1 by transition1.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(ringDuration, delayMillis = ringGap, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radius_1"
    )
    val radiusProgress2 by transition2.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(ringDuration, delayMillis = ringGap * 2, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radius_2"
    )
    
    val radiusProgresses = listOf(radiusProgress0, radiusProgress1, radiusProgress2)
    
    // Используем один Canvas на весь экран - кольца рисуются относительно центра экрана
    // и могут выходить за пределы видимой области без ограничений
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val center = Offset(centerX, centerY)
        
        // Начальный радиус - от края кнопки (кнопка имеет размер baseSize = 200.dp)
        // Кольцо начинается точно от края кнопки
        val buttonRadius = baseSize.toPx() / 2 // Радиус кнопки
        val initialRadius = buttonRadius // Кольцо начинается от края кнопки
        
        // Максимальный радиус - очень большой, чтобы кольца уходили далеко за экран
        val maxRadius = size.maxDimension * 1.5f // В 3 раза больше максимального размера экрана
        
        repeat(ringCount) { index ->
            val radiusProgress = radiusProgresses[index]
            
            // Текущий радиус кольца - от начального до максимального
            val currentRadius = initialRadius + (maxRadius - initialRadius) * radiusProgress
            
            // Толщина кольца уменьшается по мере расширения - немного толще для неонового эффекта
            val strokeWidth = (3.0f - 2.0f * radiusProgress).coerceAtLeast(0.5f)

            // Альфа вычисляется на основе radiusProgress - быстро появляется, затем плавно исчезает
            // Усилена для более яркого неонового эффекта
            val finalAlpha = when {
                radiusProgress < 0.05f -> {
                    // В самом начале быстро появляется от 0
                    1.0f * (radiusProgress / 0.05f)
                }
                radiusProgress < 0.3f -> {
                    // На максимуме - яркий неоновый эффект
                    1.0f
                }
                else -> {
                    // Плавно исчезает
                    1.0f * (1f - (radiusProgress - 0.3f) / 0.7f)
                }
            }

            // Рисуем кольцо только если оно видимо - яркий неоновый голубой цвет с эффектом свечения
            if (finalAlpha > 0.01f) {
                // Создаем эффект свечения - рисуем несколько концентрических кругов
                // Внешние слои для свечения (более прозрачные)
                for (i in 3 downTo 1) {
                    val glowRadius = currentRadius + (i * 3.dp.toPx())
                    val glowStrokeWidth = (strokeWidth + i * 1.5f).dp.toPx()
                    val glowAlpha = finalAlpha * (0.15f / i) // Уменьшаем альфу для внешних слоев
                    
                    if (glowAlpha > 0.01f) {
                        drawCircle(
                            color = VoxeraColors.PrimaryGlow.copy(alpha = glowAlpha),
                            radius = glowRadius,
                            center = center,
                            style = Stroke(
                                width = glowStrokeWidth,
                                cap = StrokeCap.Round
                            )
                        )
                    }
                }
                
                // Основное яркое кольцо
                drawCircle(
                    color = VoxeraColors.PrimaryGlow.copy(alpha = finalAlpha),
                    radius = currentRadius,
                    center = center,
                    style = Stroke(
                        width = strokeWidth.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }
        }
    }
}

@Composable
private fun LiquidRecordButtonHold(
    liquidState: io.github.fletchmckee.liquid.LiquidState,
    isHolding: Boolean,
    breathing: Float = 1f,
    onHoldStart: () -> Unit,
    onHoldEnd: () -> Unit
) {
    val tintIdle = Color.White.copy(alpha = 0.0f)
    val tintHold = VoxeraColors.PrimaryGlow.copy(alpha = 0.02f) // Почти прозрачный

    Box(
        modifier = Modifier
            .size(200.dp)
            .graphicsLayer {
                scaleX = breathing
                scaleY = breathing
            }
            // Тень для glow эффекта - усилена для неонового эффекта
            .shadow(
                elevation = if (isHolding) 32.dp else 20.dp,
                shape = CircleShape,
                clip = false,
                ambientColor = VoxeraColors.PrimaryGlow.copy(alpha = if (isHolding) 0.3f else 0.15f),
                spotColor = VoxeraColors.PrimaryGlow.copy(alpha = if (isHolding) 0.4f else 0.2f)
            )
            .liquid(liquidState) {
                shape = CircleShape

                // Liquid glass эффект - кнопка практически прозрачная
                frost = if (isHolding) 1.dp else 0.dp // Минимальная мутность
                refraction = if (isHolding) 0.25f else 0.20f // Меньше рефракции
                curve = 0.20f
                edge = 0.1f

                tint = if (isHolding) tintHold else tintIdle
                saturation = if (isHolding) 1.05f else 1.0f // Почти без насыщенности
                dispersion = if (isHolding) 0.7f else 0.5f // Меньше дисперсии
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onHoldStart()
                        try {
                            awaitRelease()
                        } finally {
                            onHoldEnd()
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Лёгкий блик поверх
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        0f to VoxeraColors.PrimaryGlow.copy(alpha = if (isHolding) 0.06f else 0.03f),
                        0.3f to Color.Transparent,
                        1f to Color.Transparent
                    )
                )
        )

        // Иконка микрофона без тени (свечение уже есть от liquid эффекта кнопки)
        Image(
            painter = painterResource(R.drawable.ic_mic_2),
            contentDescription = "Mic",
            modifier = Modifier.size(140.dp),
            colorFilter = ColorFilter.tint(
                VoxeraColors.TextPrimary.copy(alpha = if (isHolding) 1f else 0.9f)
            )
        )
        
        // Неоновая голубая окантовка вокруг кнопки с эффектом свечения - поверх всего
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val baseRadius = size.minDimension / 2 - 2.dp.toPx()
            val glowAlpha = if (isHolding) 1.0f else 0.8f
            
            // Создаем эффект свечения - рисуем несколько концентрических кругов
            // Внешние слои для свечения (более прозрачные)
            for (i in 3 downTo 1) {
                val glowRadius = baseRadius + (i * 2.dp.toPx())
                val glowStrokeWidth = (4 + i * 2).dp.toPx()
                val alpha = glowAlpha * (0.2f / i) // Уменьшаем альфу для внешних слоев
                
                drawCircle(
                    color = VoxeraColors.PrimaryGlow.copy(alpha = alpha),
                    radius = glowRadius,
                    center = center,
                    style = Stroke(width = glowStrokeWidth, cap = StrokeCap.Round)
                )
            }
            
            // Основная яркая обводка
            val strokeWidth = if (isHolding) 4.dp.toPx() else 2.5.dp.toPx()
            drawCircle(
                color = VoxeraColors.PrimaryGlow.copy(alpha = glowAlpha),
                radius = baseRadius,
                center = center,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RecordingScreenPreview() {
  VoxeraTheme {
    RecordingScreen(
      onGoProcessing = {}
    )
  }
}


