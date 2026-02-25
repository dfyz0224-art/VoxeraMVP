package com.vanoprojects.voxera.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.em
import com.vanoprojects.voxera.R

// Inter — современный шрифт с отличной читаемостью
val VoxeraFontFamily = FontFamily(
    Font(R.font.inter, FontWeight.Light),
    Font(R.font.inter, FontWeight.Normal),
    Font(R.font.inter, FontWeight.Medium),
    Font(R.font.inter, FontWeight.SemiBold),
    Font(R.font.inter, FontWeight.Bold)
)

val Typography = Typography(
  // H1: 28/34 Semibold
  headlineLarge = TextStyle(
    fontFamily = VoxeraFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 28.sp,
    lineHeight = 34.sp,
    letterSpacing = (-0.01).em // Более плотное межбуквенное расстояние для современного вида
  ),
  // H2: 20/26 Semibold
  headlineMedium = TextStyle(
    fontFamily = VoxeraFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 20.sp,
    lineHeight = 26.sp,
    letterSpacing = (-0.01).em
  ),
  headlineSmall = TextStyle(
    fontFamily = VoxeraFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 20.sp,
    lineHeight = 26.sp,
    letterSpacing = (-0.01).em
  ),
  // Body: 16/22 Regular
  bodyLarge = TextStyle(
    fontFamily = VoxeraFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 22.sp,
    letterSpacing = 0.em
  ),
  bodyMedium = TextStyle(
    fontFamily = VoxeraFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 22.sp,
    letterSpacing = 0.em
  ),
  // Caption: 12/16 Regular
  bodySmall = TextStyle(
    fontFamily = VoxeraFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.em
  ),
  labelMedium = TextStyle(
    fontFamily = VoxeraFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.em
  ),
  titleLarge = TextStyle(
    fontFamily = VoxeraFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 18.sp,
    lineHeight = 24.sp,
    letterSpacing = (-0.01).em
  ),
  titleMedium = TextStyle(
    fontFamily = VoxeraFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 22.sp,
    letterSpacing = 0.em
  )
)
