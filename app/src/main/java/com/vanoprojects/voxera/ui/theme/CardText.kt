package com.vanoprojects.voxera.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/** Вертикальные отступы между абзацами и блоками на карточках с длинным текстом. */
object CardTextSpacing {
    val BetweenParagraphs = 16.dp
    val AfterSectionTitle = 12.dp
    val BetweenSections = 24.dp
}

/**
 * Многострочный текст на карточках: выравнивание по левому краю (без «лесенки» пробелов от justify),
 * увеличенный интерлиньяж и чуть крупнее основной кегль.
 */
@Composable
fun cardParagraphTextStyle(): TextStyle =
    MaterialTheme.typography.bodyMedium.copy(
        fontSize = 17.sp,
        lineHeight = 27.sp,
        letterSpacing = 0.em,
        textAlign = TextAlign.Start,
        hyphens = Hyphens.None
    )

/** Заголовки секций на карточках — крупнее и жирнее основного текста. */
@Composable
fun cardSectionTitleOnCardStyle(): TextStyle =
    MaterialTheme.typography.titleLarge.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.01).em,
        textAlign = TextAlign.Start,
        hyphens = Hyphens.None
    )

/** Второстепенный мелкий текст на карточках (подписи тарифов и т.п.). */
@Composable
fun cardParagraphSmallTextStyle(): TextStyle =
    MaterialTheme.typography.bodySmall.copy(
        fontSize = 13.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.em,
        textAlign = TextAlign.Start,
        hyphens = Hyphens.None
    )
