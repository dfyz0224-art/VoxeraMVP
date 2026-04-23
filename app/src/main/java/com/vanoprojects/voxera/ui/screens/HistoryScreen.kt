package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
  onOpenStatistics: () -> Unit,
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

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(20.dp)
    ) {
      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = strings.historyTitle,
        style = MaterialTheme.typography.headlineSmall.copy(
          fontWeight = FontWeight.SemiBold,
          fontSize = 24.sp
        ),
        color = if (theme.type == ThemeType.LIGHT) {
          colors.backgroundTextPrimary
        } else {
          colors.textPrimary
        },
        textAlign = TextAlign.Start,
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(10.dp))
      ThemedOutlinedButton(
        text = strings.statisticsButton,
        onClick = onOpenStatistics,
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(8.dp))

      if (entries.isEmpty()) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f, fill = true)
        ) {
          TextWithShadow(
            text = strings.historyEmpty,
            color = colors.textSecondary,
            style = MaterialTheme.typography.bodyLarge
          )
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
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
