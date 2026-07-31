package com.vanoprojects.voxera.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.liquid

/**
 * Чекбоксы с [CheckboxDefaults.colors] и только [checkedColor] = белый дают невидимую галочку
 * (checkmark того же цвета). Здесь заливка и галочка контрастируют на любых фонах.
 */
@Composable
fun voxeraCheckboxColors(): CheckboxColors {
    val colors = LocalVoxeraTheme.current.colors
    return CheckboxDefaults.colors(
        checkedColor = colors.buttonFilledBackground,
        uncheckedColor = colors.backgroundTextSecondary.copy(alpha = 0.45f),
        checkmarkColor = Color.White
    )
}

/** В GLASS [ThemeColors.buttonFilledBackground] почти прозрачный — трек Switch сливается с карточкой. */
@Composable
fun voxeraSwitchColors(): SwitchColors {
    val theme = LocalVoxeraTheme.current
    val colors = theme.colors
    val checkedTrack = when (theme.type) {
        ThemeType.GLASS -> Color(0xFF3D6CAD)
        else -> colors.buttonFilledBackground
    }
    val uncheckedTrack = when (theme.type) {
        ThemeType.GLASS -> Color(0xFF5A6578).copy(alpha = 0.75f)
        else -> colors.backgroundTextSecondary.copy(alpha = 0.38f)
    }
    return SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = checkedTrack,
        uncheckedThumbColor = Color.White,
        uncheckedTrackColor = uncheckedTrack,
        disabledCheckedThumbColor = Color.White.copy(alpha = 0.65f),
        disabledCheckedTrackColor = checkedTrack.copy(alpha = 0.45f),
        disabledUncheckedThumbColor = Color.White.copy(alpha = 0.45f),
        disabledUncheckedTrackColor = uncheckedTrack.copy(alpha = 0.55f)
    )
}

@Composable
fun ThemedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    liquidState: io.github.fletchmckee.liquid.LiquidState? = null,
    gradientIndex: Int? = null, // Индекс карточки для единого градиента в светлой теме
    height: Dp? = null, // Переопределение высоты (все карточки одного размера)
    contentPadding: PaddingValues? = null, // Переопределение отступов контента (null = стандартные)
    content: @Composable () -> Unit
) {
    val theme = LocalVoxeraTheme.current
    val colors = theme.colors
    val cardShape = RoundedCornerShape(16.dp)
    val cardHeight = height ?: 140.dp
    
    // Для светлой темы вычисляем градиент на основе позиции карточки
    val cardGradient = if (theme.type == ThemeType.LIGHT && gradientIndex != null) {
        // Единый градиент от темного к светлому - контрастные синие цвета
        // Верхняя карточка (0) - самая темная, нижняя - самая светлая
        val baseDark = Color(0xFF001F5C)    // Глубокий темно-синий
        val dark1 = Color(0xFF003A8C)       // Темно-синий
        val dark2 = Color(0xFF0055BD)       // Насыщенный синий
        val mid1 = Color(0xFF0070EE)        // Яркий синий
        val mid2 = Color(0xFF1E88FF)         // Светло-синий
        val light1 = Color(0xFF4DA3FF)      // Светлый синий
        val light2 = Color(0xFF7CBEFF)      // Очень светлый синий
        val baseLight = Color(0xFFABD9FF)   // Самый светлый синий
        
        when (gradientIndex) {
            0 -> listOf(baseDark, dark2)      // Первая карточка - самая темная
            1 -> listOf(dark1, mid1)          // Вторая карточка
            2 -> listOf(dark2, light1)           // Третья карточка
            3 -> listOf(mid2, light2)         // Четвертая карточка
            else -> listOf(light1, baseLight) // Пятая и далее - самые светлые
        }
    } else {
        colors.cardBackgroundGradient
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(if (modifier == Modifier) Modifier.height(cardHeight) else Modifier)
    ) {
        // Тень: в светлой теме — одна мягкая матовая, в темной — многослойная
        if (theme.type != ThemeType.GLASS) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val cardWidth = size.width
                val cardHeight = size.height
                val cornerRadius = 19.dp.toPx()
                if (theme.type == ThemeType.LIGHT) {
                    // Лёгкая тень для светлой темы — минимальный spread
                    val spread = 2.dp.toPx()
                    drawRoundRect(
                        color = colors.shadowColor.copy(alpha = 0.04f),
                        topLeft = Offset(spread * 0.5f, spread),
                        size = Size(cardWidth + spread, cardHeight + spread),
                        cornerRadius = CornerRadius(cornerRadius + spread * 0.5f)
                    )
                } else {
                    val shadowSpread = 12.dp.toPx()
                    for (i in 1..8) {
                        val spread = shadowSpread * (i / 8f)
                        val alpha = (0.25f / i).coerceAtMost(0.15f)
                        drawRoundRect(
                            color = colors.shadowColor.copy(alpha = alpha),
                            topLeft = Offset(-spread, -spread + spread * 0.2f),
                            size = Size(cardWidth + spread * 2, cardHeight + spread * 2),
                            cornerRadius = CornerRadius(cornerRadius + spread)
                        )
                    }
                }
            }
        }
        
        // Сама карточка
        val cardModifier = Modifier
            .fillMaxSize()
            .clip(cardShape)
            .then(
                if (theme.type == ThemeType.GLASS && liquidState != null) {
                    // Liquid эффект для стеклянной темы
                    Modifier.liquid(liquidState) {
                        shape = cardShape
                        frost = 0.12.dp
                        refraction = 3.5f
                        curve = 2.0f
                        edge = 0.05f
                        tint = Color.White.copy(alpha = 0.0f)
                        saturation = 1.0f
                        dispersion = 0.6f
                    }
                } else {
                    // Обычный градиентный фон
                    Modifier.background(
                        brush = Brush.linearGradient(cardGradient),
                        shape = cardShape
                    )
                }
            )
            .then(
                // В светлой теме — очень тонкая нейтральная граница для матового вида
                if (theme.type == ThemeType.LIGHT) {
                    Modifier.border(
                        width = 0.5.dp,
                        color = Color.Black.copy(alpha = 0.06f),
                        shape = cardShape
                    )
                } else {
                    Modifier.border(
                        width = if (theme.type == ThemeType.GLASS) 1.dp else 1.3.dp,
                        color = colors.buttonBorder,
                        shape = cardShape
                    )
                }
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .then(
                if (contentPadding != null) Modifier.padding(contentPadding)
                else Modifier.padding(vertical = 28.dp, horizontal = 30.dp)
            )
        
        Box(modifier = cardModifier) {
            content()
        }
    }
}

@Composable
fun ThemedOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val theme = LocalVoxeraTheme.current
    val colors = theme.colors
    
    // Ширина границы и стиль зависят от темы
    val borderWidth = when (theme.type) {
        ThemeType.LIGHT -> 1.dp
        ThemeType.GLASS -> 1.dp
    }
    
    // Фон кнопки: непрозрачный для светлой и темной, прозрачный для стеклянной
    val buttonBackground = when (theme.type) {
        ThemeType.LIGHT -> colors.buttonBackground
        ThemeType.GLASS -> Color.Transparent
    }
    
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = colors.buttonText,
            containerColor = buttonBackground
        ),
        border = BorderStroke(
            width = borderWidth,
            color = if (theme.type == ThemeType.LIGHT) Color.Black.copy(alpha = 0.08f)
            else colors.buttonBorder
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.buttonText,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ThemedFilledButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val theme = LocalVoxeraTheme.current
    val colors = theme.colors
    
    // Фон кнопки: непрозрачный для светлой и темной, прозрачный для стеклянной
    val buttonBackground = when (theme.type) {
        ThemeType.LIGHT -> colors.buttonFilledBackground
        ThemeType.GLASS -> colors.buttonFilledBackground
    }
    
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonBackground
        )
    ) {
        val textColor = when (theme.type) {
            ThemeType.LIGHT -> Color.White
            ThemeType.GLASS -> colors.buttonText
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
