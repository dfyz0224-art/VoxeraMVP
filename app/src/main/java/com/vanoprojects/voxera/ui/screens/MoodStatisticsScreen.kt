package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ripple
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanoprojects.voxera.R
import com.vanoprojects.voxera.data.HistoryRepository
import com.vanoprojects.voxera.ui.strings.LocalStrings
import com.vanoprojects.voxera.ui.statistics.MoodScaleLabels
import com.vanoprojects.voxera.ui.statistics.MoodStatsPeriod
import com.vanoprojects.voxera.ui.statistics.MoodTimeSeries
import com.vanoprojects.voxera.ui.theme.LocalVoxeraTheme
import com.vanoprojects.voxera.ui.theme.MoodChartColors
import com.vanoprojects.voxera.ui.theme.ThemeColors
import com.vanoprojects.voxera.ui.theme.ThemeType
import com.vanoprojects.voxera.ui.theme.TextWithShadow

@Composable
fun MoodStatisticsScreen(
  historyRepository: HistoryRepository,
  onBack: () -> Unit
) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val strings = LocalStrings.current
  var period by remember { mutableStateOf(MoodStatsPeriod.WEEK) }
  var selectedFilterKey by remember { mutableStateOf<String?>(null) }
  val entries by historyRepository.entries.collectAsState(initial = emptyList())
  val scaleKeys = MoodScaleLabels.ORDER
  val chartSeries = remember(period, entries) {
    MoodTimeSeries.fromEntries(entries, period, scaleKeys)
  }
  val palette = if (theme.type == ThemeType.LIGHT) MoodChartColors.light else MoodChartColors.glass
  val hasData = remember(entries, period, chartSeries) {
    chartSeries.dayPoints.any { p -> p.values.values.any { it != null } }
  }
  val headingColor = if (theme.type == ThemeType.LIGHT) colors.backgroundTextPrimary else colors.textPrimary
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

  Box(modifier = Modifier.fillMaxSize()) {
    if (theme.type == ThemeType.LIGHT) {
      Image(
        painter = painterResource(R.drawable.bg_light),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )
    } else {
      VoxeraBackground { }
    }
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(20.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        TextButton(onClick = onBack) {
          Text(
            text = "‹  ${strings.back}",
            color = headingColor
          )
        }
      }
      Spacer(Modifier.height(4.dp))
      Text(
        text = strings.statisticsTitle,
        style = MaterialTheme.typography.headlineSmall.copy(
          fontWeight = FontWeight.SemiBold,
          fontSize = 24.sp
        ),
        color = headingColor
      )
      Spacer(Modifier.height(16.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        PeriodSegment(
          label = strings.statisticsPeriodWeek,
          selected = period == MoodStatsPeriod.WEEK,
          onClick = { period = MoodStatsPeriod.WEEK },
          textColor = headingColor,
          bg = periodBg,
          selectedBg = periodSelectedBg
        )
        PeriodSegment(
          label = strings.statisticsPeriodMonth,
          selected = period == MoodStatsPeriod.MONTH,
          onClick = { period = MoodStatsPeriod.MONTH },
          textColor = headingColor,
          bg = periodBg,
          selectedBg = periodSelectedBg
        )
      }
      Spacer(Modifier.height(20.dp))
      if (!hasData) {
        TextWithShadow(
          text = strings.statisticsNoData,
          color = colors.textSecondary,
          style = MaterialTheme.typography.bodyLarge
        )
      } else {
        StatisticsMoodPanel {
          Column(Modifier.fillMaxWidth()) {
            MoodLineChart(
              series = chartSeries,
              scaleKeys = scaleKeys,
              colors = palette,
              themeType = theme.type,
              selectedOnlyKey = selectedFilterKey
            )
            Spacer(Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              scaleKeys.chunked(2).forEach { rowKeys ->
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  rowKeys.forEach { key ->
                    val i = scaleKeys.indexOf(key)
                    val lineColor = palette[i.coerceIn(0, palette.lastIndex)]
                    StatisticParamChip(
                      label = MoodScaleLabels.label(key, strings),
                      lineColor = lineColor,
                      selected = selectedFilterKey == key,
                      onClick = {
                        selectedFilterKey = if (selectedFilterKey == key) null else key
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
  }
}

@Composable
private fun RowScope.PeriodSegment(
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
      .weight(1f)
      .clip(shape)
      .background(if (selected) selectedBg else bg, shape)
      .clickable(
        interactionSource = interaction,
        indication = ripple(bounded = true),
        onClick = onClick
      )
      .padding(vertical = 10.dp, horizontal = 8.dp)
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelLarge,
      color = textColor,
      modifier = Modifier.fillMaxWidth(),
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
  val bg = if (selected) lineColor.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.08f)
  Row(
    modifier = Modifier
      .weight(1f)
      .height(40.dp)
      .clip(shape)
      .border(
        width = 1.dp,
        color = if (selected) lineColor else Color.White.copy(alpha = 0.15f),
        shape = shape
      )
      .background(bg, shape)
      .clickable(
        interactionSource = interaction,
        indication = ripple(bounded = true),
        onClick = onClick
      )
      .padding(horizontal = 6.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(width = 3.dp, height = 12.dp)
        .background(lineColor, RoundedCornerShape(2.dp))
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

/** Без M3 FilterChip/FlowRow — внутри scroll это часто вело к падениям. */
@Composable
private fun StatisticsMoodPanel(content: @Composable () -> Unit) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val cardShape = RoundedCornerShape(16.dp)
  val cardGradient = if (theme.type == ThemeType.LIGHT) {
    listOf(
      Color(0xFF001F5C),
      Color(0xFF0055BD)
    )
  } else {
    colors.cardBackgroundGradient
  }
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(cardShape)
      .background(Brush.linearGradient(cardGradient), cardShape)
      .border(
        width = if (theme.type == ThemeType.LIGHT) 0.5.dp else 1.dp,
        color = if (theme.type == ThemeType.LIGHT) Color.Black.copy(alpha = 0.06f) else colors.buttonBorder,
        shape = cardShape
      )
      .padding(vertical = 24.dp, horizontal = 24.dp)
  ) {
    content()
  }
}
