@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vanoprojects.voxera.data.HistoryEntry
import com.vanoprojects.voxera.ui.statistics.MoodScaleLabels
import com.vanoprojects.voxera.ui.statistics.MoodStatsPeriod
import com.vanoprojects.voxera.ui.statistics.MoodTimeSeries
import com.vanoprojects.voxera.ui.strings.LocalStrings
import com.vanoprojects.voxera.ui.theme.LocalVoxeraTheme
import com.vanoprojects.voxera.ui.theme.MoodChartColors
import com.vanoprojects.voxera.ui.theme.ThemeColors
import com.vanoprojects.voxera.ui.theme.ThemeType
import com.vanoprojects.voxera.ui.theme.TextWithShadow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private enum class PeriodKind { H24, WEEK, MONTH, CUSTOM }

@Composable
fun HistoryMoodChartCard(
  entries: List<HistoryEntry>,
  modifier: Modifier = Modifier
) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val strings = LocalStrings.current
  var periodKind by remember { mutableStateOf(PeriodKind.WEEK) }
  var customFrom by remember { mutableStateOf(LocalDate.now().minusDays(6)) }
  var customTo by remember { mutableStateOf(LocalDate.now()) }
  var showFromPicker by remember { mutableStateOf(false) }
  var showToPicker by remember { mutableStateOf(false) }
  var activeFilter by remember { mutableStateOf<Set<String>>(emptySet()) }

  val period: MoodStatsPeriod = when (periodKind) {
    PeriodKind.H24 -> MoodStatsPeriod.Last24h
    PeriodKind.WEEK -> MoodStatsPeriod.Week
    PeriodKind.MONTH -> MoodStatsPeriod.Month
    PeriodKind.CUSTOM -> MoodStatsPeriod.Custom(customFrom, customTo)
  }
  val scaleKeys = MoodScaleLabels.ORDER
  val chartSeries = remember(period, entries) {
    MoodTimeSeries.fromEntries(entries, period, scaleKeys)
  }
  // Карточка всегда тёмно-синяя: линии как в glass, чтобы хорошо читались.
  val palette = MoodChartColors.glass
  val hasData = remember(entries, period, chartSeries) {
    chartSeries.dayPoints.any { p -> p.values.values.any { it != null } }
  }
  val onChartCard = colors.textPrimary
  val periodBg = if (theme.type == ThemeType.LIGHT) {
    Color.Black.copy(alpha = 0.06f)
  } else {
    Color.White.copy(alpha = 0.1f)
  }
  val periodSelectedBg = if (theme.type == ThemeType.LIGHT) {
    Color(0xFF2E5F9E).copy(alpha = 0.25f)
  } else {
    Color.White.copy(alpha = 0.2f)
  }
  val dateLabelFmt = remember {
    DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
      .withLocale(java.util.Locale.getDefault())
  }
  if (showFromPicker) {
    val zone = remember { ZoneId.systemDefault() }
    val fromMillis = remember(customFrom) {
      customFrom.atStartOfDay(zone).toInstant().toEpochMilli()
    }
    val fromState = rememberDatePickerState(initialSelectedDateMillis = fromMillis)
    DatePickerDialog(
      onDismissRequest = { showFromPicker = false },
      confirmButton = {
        TextButton(
          onClick = {
            fromState.selectedDateMillis?.let { ms ->
              customFrom = Instant.ofEpochMilli(ms).atZone(zone).toLocalDate()
            }
            showFromPicker = false
          }
        ) { Text(stringResource(id = android.R.string.ok)) }
      },
      dismissButton = {
        TextButton(onClick = { showFromPicker = false }) {
          Text(stringResource(id = android.R.string.cancel))
        }
      }
    ) {
      DatePicker(state = fromState)
    }
  }
  if (showToPicker) {
    val zone = remember { ZoneId.systemDefault() }
    val toMillis = remember(customTo) {
      customTo.atStartOfDay(zone).toInstant().toEpochMilli()
    }
    val toState = rememberDatePickerState(initialSelectedDateMillis = toMillis)
    DatePickerDialog(
      onDismissRequest = { showToPicker = false },
      confirmButton = {
        TextButton(
          onClick = {
            toState.selectedDateMillis?.let { ms ->
              customTo = Instant.ofEpochMilli(ms).atZone(zone).toLocalDate()
            }
            showToPicker = false
          }
        ) { Text(stringResource(id = android.R.string.ok)) }
      },
      dismissButton = {
        TextButton(onClick = { showToPicker = false }) {
          Text(stringResource(id = android.R.string.cancel))
        }
      }
    ) {
      DatePicker(state = toState)
    }
  }
  HistoryMoodPanelCard(modifier = modifier) {
    Column(Modifier.fillMaxWidth()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        SmallPeriodSegment(
          label = strings.statisticsPeriod24h,
          selected = periodKind == PeriodKind.H24,
          onClick = { periodKind = PeriodKind.H24 },
          textColor = onChartCard,
          bg = periodBg,
          selectedBg = periodSelectedBg
        )
        SmallPeriodSegment(
          label = strings.statisticsPeriodWeek,
          selected = periodKind == PeriodKind.WEEK,
          onClick = { periodKind = PeriodKind.WEEK },
          textColor = onChartCard,
          bg = periodBg,
          selectedBg = periodSelectedBg
        )
        SmallPeriodSegment(
          label = strings.statisticsPeriodMonth,
          selected = periodKind == PeriodKind.MONTH,
          onClick = { periodKind = PeriodKind.MONTH },
          textColor = onChartCard,
          bg = periodBg,
          selectedBg = periodSelectedBg
        )
        SmallPeriodSegment(
          label = strings.statisticsPeriodCustom,
          selected = periodKind == PeriodKind.CUSTOM,
          onClick = { periodKind = PeriodKind.CUSTOM },
          textColor = onChartCard,
          bg = periodBg,
          selectedBg = periodSelectedBg
        )
      }
      if (periodKind == PeriodKind.CUSTOM) {
        Spacer(Modifier.height(10.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          TextButton(
            onClick = { showFromPicker = true },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.textButtonColors(contentColor = onChartCard)
          ) {
            Text(
              text = "${strings.statisticsDateFrom}: ${customFrom.format(dateLabelFmt)}",
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
          TextButton(
            onClick = { showToPicker = true },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.textButtonColors(contentColor = onChartCard)
          ) {
            Text(
              text = "${strings.statisticsDateTo}: ${customTo.format(dateLabelFmt)}",
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }
      Spacer(Modifier.height(12.dp))
      if (!hasData) {
        TextWithShadow(
          text = strings.statisticsNoData,
          color = onChartCard.copy(alpha = 0.88f),
          style = MaterialTheme.typography.bodyMedium
        )
      } else {
        MoodLineChart(
          series = chartSeries,
          scaleKeys = scaleKeys,
          colors = palette,
          activeFilter = activeFilter
        )
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          scaleKeys.chunked(2).forEach { rowKeys ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              rowKeys.forEach { key ->
                val i = scaleKeys.indexOf(key)
                val lineColor = palette[i.coerceIn(0, palette.lastIndex)]
                StatisticParamChip(
                  label = MoodScaleLabels.label(key, strings),
                  lineColor = lineColor,
                  selected = activeFilter.isNotEmpty() && key in activeFilter,
                  onClick = {
                    val next = activeFilter.toMutableSet()
                    if (next.contains(key)) next.remove(key) else next.add(key)
                    activeFilter = next
                  },
                  cardColors = colors
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun HistoryMoodPanelCard(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val cardShape = RoundedCornerShape(16.dp)
  val cardGradient = if (theme.type == ThemeType.LIGHT) {
    listOf(
      Color(0xFF0F3278),
      Color(0xFF1B74E2)
    )
  } else {
    listOf(
      Color.White.copy(alpha = 0.22f),
      Color.White.copy(alpha = 0.10f)
    )
  }
  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(cardShape)
      .background(Brush.linearGradient(cardGradient), cardShape)
      .border(
        width = if (theme.type == ThemeType.LIGHT) 0.5.dp else 1.dp,
        color = if (theme.type == ThemeType.LIGHT) Color.Black.copy(alpha = 0.06f) else colors.buttonBorder,
        shape = cardShape
      )
      .padding(vertical = 16.dp, horizontal = 16.dp)
  ) {
    content()
  }
}

@Composable
private fun RowScope.SmallPeriodSegment(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  textColor: Color,
  bg: Color,
  selectedBg: Color
) {
  val shape = RoundedCornerShape(8.dp)
  val interaction = remember { MutableInteractionSource() }
  Box(
    modifier = Modifier
      .widthIn(min = 56.dp)
      .clip(shape)
      .background(if (selected) selectedBg else bg, shape)
      .clickable(
        interactionSource = interaction,
        indication = ripple(bounded = true),
        onClick = onClick
      )
      .padding(vertical = 8.dp, horizontal = 8.dp)
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelLarge,
      color = textColor,
      textAlign = TextAlign.Center
    )
  }
}

@Composable
private fun RowScope.StatisticParamChip(
  label: String,
  lineColor: Color,
  selected: Boolean,
  onClick: () -> Unit,
  cardColors: ThemeColors
) {
  val shape = RoundedCornerShape(10.dp)
  val interaction = remember { MutableInteractionSource() }
  val bg = if (selected) lineColor.copy(alpha = 0.42f) else Color.White.copy(alpha = 0.12f)
  Row(
    modifier = Modifier
      .weight(1f)
      .height(44.dp)
      .clip(shape)
      .border(
        width = if (selected) 2.dp else 1.5.dp,
        color = if (selected) lineColor else Color.White.copy(alpha = 0.32f),
        shape = shape
      )
      .background(bg, shape)
      .clickable(
        interactionSource = interaction,
        indication = ripple(bounded = true),
        onClick = onClick
      )
      .padding(horizontal = 6.dp, vertical = 5.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(width = 6.dp, height = 18.dp)
        .clip(RoundedCornerShape(3.dp))
        .background(lineColor)
        .border(
          width = 1.dp,
          color = Color.White.copy(alpha = 0.6f),
          shape = RoundedCornerShape(3.dp)
        )
    )
    Spacer(Modifier.size(4.dp))
    Text(
      text = label,
      color = cardColors.textPrimary,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      style = MaterialTheme.typography.labelMedium
    )
  }
}
