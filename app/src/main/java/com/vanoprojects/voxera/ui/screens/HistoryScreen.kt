package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanoprojects.voxera.R
import com.vanoprojects.voxera.data.HistoryEntry
import com.vanoprojects.voxera.data.HistoryRepository
import com.vanoprojects.voxera.ui.strings.LocalStrings
import com.vanoprojects.voxera.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun HistoryScreen(
  historyRepository: HistoryRepository,
  onItemClick: (HistoryEntry) -> Unit
) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val strings = LocalStrings.current
  val entries by historyRepository.entries.collectAsState(initial = emptyList())

  Box(modifier = Modifier.fillMaxSize()) {
    if (theme.type == ThemeType.LIGHT) {
      Image(
        painter = painterResource(R.drawable.bg_light),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )
    } else {
      VoxeraBackground {}
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .windowInsetsPadding(WindowInsets.statusBars),
      contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp, top = 8.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      item {
        Text(
          text = strings.historyTitle,
          style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            shadow = if (theme.type == ThemeType.LIGHT) {
              Shadow(
                color = Color.Black.copy(alpha = 0.4f),
                offset = Offset(0f, 1.5f),
                blurRadius = 5f
              )
            } else {
              Shadow()
            }
          ),
          color = if (theme.type == ThemeType.LIGHT) {
            Color.White
          } else {
            colors.textPrimary
          },
          textAlign = TextAlign.Start,
          modifier = Modifier.fillMaxWidth()
        )
      }
      item {
        HistoryMoodChartCard(entries = entries)
      }
      if (entries.isEmpty()) {
        item {
          TextWithShadow(
            text = strings.historyEmpty,
            color = if (theme.type == ThemeType.LIGHT) {
              colors.backgroundTextSecondary
            } else {
              colors.textSecondary
            },
            style = MaterialTheme.typography.bodyLarge
          )
        }
      } else {
        itemsIndexed(entries) { index, entry ->
          HistoryItem(
            entry = entry,
            gradientIndex = index % 4,
            onClick = { onItemClick(entry) }
          )
        }
      }
    }
  }
}

/** Дата и время анализа в локали пользователя (короткий формат). */
private fun formatHistoryDateTime(timestamp: Long): String {
  val zoned = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault())
  val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT)
  return zoned.format(formatter)
}

private fun formatAnalysisType(analysisType: String, strings: com.vanoprojects.voxera.ui.strings.Strings): String =
  when (analysisType) {
    "psytype" -> strings.historyTypePsytype
    else -> strings.historyTypeEmostate
  }

@Composable
private fun HistoryItem(
  entry: HistoryEntry,
  gradientIndex: Int,
  onClick: () -> Unit
) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val strings = LocalStrings.current
  val dateLabel = formatHistoryDateTime(entry.timestamp)
  val typeLabel = formatAnalysisType(entry.analysisType, strings)

  ThemedCard(gradientIndex = gradientIndex, onClick = onClick) {
    Column {
      TextWithShadow(
        text = dateLabel,
        color = colors.textSecondary,
        style = MaterialTheme.typography.bodyMedium
      )
      Spacer(modifier = Modifier.height(6.dp))
      TextWithShadow(
        text = typeLabel,
        color = colors.textPrimary,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
      )
    }
  }
}
