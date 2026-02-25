package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vanoprojects.voxera.R
import com.vanoprojects.voxera.data.HistoryEntry
import com.vanoprojects.voxera.data.HistoryRepository
import com.vanoprojects.voxera.ui.strings.LocalStrings
import com.vanoprojects.voxera.ui.theme.*
import java.util.concurrent.TimeUnit

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

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(20.dp)
    ) {
      Spacer(modifier = Modifier.height(10.dp))

      if (entries.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          contentAlignment = Alignment.Center
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

private fun formatDateLabel(timestamp: Long, strings: com.vanoprojects.voxera.ui.strings.Strings): String {
  val now = System.currentTimeMillis()
  val diffMs = now - timestamp
  val diffDays = TimeUnit.MILLISECONDS.toDays(diffMs)
  return when {
    diffDays == 0L -> strings.today
    diffDays == 1L -> strings.yesterday
    diffDays == 2L -> strings.twoDaysAgo
    else -> "$diffDays ${strings.daysAgo}"
  }
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
  val dateLabel = formatDateLabel(entry.timestamp, strings)
  val typeLabel = formatAnalysisType(entry.analysisType, strings)

  ThemedCard(gradientIndex = gradientIndex, onClick = onClick) {
    Column {
      TextWithShadow(
        text = dateLabel,
        color = colors.textSecondary,
        style = MaterialTheme.typography.bodySmall
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
