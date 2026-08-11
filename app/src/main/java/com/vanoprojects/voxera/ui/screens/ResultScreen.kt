package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.content.Intent
import android.os.Build
import android.text.Layout
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.text.HtmlCompat
import com.vanoprojects.voxera.R
import com.vanoprojects.voxera.data.AnalysisSession
import com.vanoprojects.voxera.data.buildSharePlainText
import com.vanoprojects.voxera.data.model.AnalysisResponse
import com.vanoprojects.voxera.data.model.EmoScale
import com.vanoprojects.voxera.data.model.PsyType
import com.vanoprojects.voxera.ui.strings.EmoScaleNames
import com.vanoprojects.voxera.ui.strings.LocalStrings
import com.vanoprojects.voxera.ui.strings.Strings
import com.vanoprojects.voxera.ui.theme.*

private fun extractDescriptionFromRawJson(): String {
    val raw = AnalysisSession.lastRawApiResponse
    val resultJson = AnalysisSession.lastResultJson
    Log.d("DescriptionExtract", "lastRawApiResponse=${if (raw != null) "length=${raw.length}" else "null"}, lastResultJson=${if (resultJson != null) "length=${resultJson.length}" else "null"}")
    if (!raw.isNullOrEmpty()) {
        val fromRaw = extractDescriptionFromJsonString(raw)
        Log.d("DescriptionExtract", "From raw: extracted length=${fromRaw.length}, preview=${fromRaw.take(50)}...")
        if (fromRaw.isNotEmpty()) return fromRaw
    }
    if (!resultJson.isNullOrEmpty() && resultJson.startsWith("{")) {
        val fromResult = extractDescriptionFromJsonString(resultJson)
        Log.d("DescriptionExtract", "From lastResultJson: extracted length=${fromResult.length}, preview=${fromResult.take(50)}...")
        if (fromResult.isNotEmpty()) return fromResult
    }
    Log.d("DescriptionExtract", "No description found")
    return ""
}

private fun extractDescriptionFromJsonString(jsonStr: String): String = try {
    val json = com.google.gson.Gson().fromJson(jsonStr, com.google.gson.JsonObject::class.java)
        ?: return "".also { Log.d("DescriptionExtract", "extractFromJson: json parse returned null") }
    val topLevelKeys = json.keySet().joinToString(",")
    Log.d("DescriptionExtract", "extractFromJson: topLevelKeys=$topLevelKeys, hasTopLevelDesc=${json.has("description")}")
    // description может быть в result или на верхнем уровне
    val fromResult = json.getAsJsonObject("result")?.let { resultObj ->
        val resultKeys = resultObj.keySet().joinToString(",")
        Log.d("DescriptionExtract", "extractFromJson: resultKeys=$resultKeys")
        (resultObj.get("description") as? com.google.gson.JsonPrimitive)?.asString
    }
    val fromTop = (json.get("description") as? com.google.gson.JsonPrimitive)?.asString
    (fromResult ?: fromTop) ?: "".also { Log.d("DescriptionExtract", "extractFromJson: no description found") }
} catch (e: Exception) {
    Log.d("DescriptionExtract", "extractFromJson exception: ${e.message}", e)
    ""
}

/**
 * API often returns "1. Состояние … 2. Риски … 3. Рекомендация …" as one line.
 * Split into paragraphs and bold the section titles for HtmlCompat display.
 */
private fun formatEmostateDescriptionHtml(raw: String): String {
  if (raw.isBlank()) return raw
  val plain = HtmlCompat.fromHtml(raw, HtmlCompat.FROM_HTML_MODE_LEGACY)
    .toString()
    .replace("\r\n", "\n")
    .replace('\r', '\n')
    .trim()
  val withParagraphs = plain
    .replace(Regex("""(?<!^)\s+(?=\d+\.\s+)"""), "\n\n")
    .replace(Regex("""\n{3,}"""), "\n\n")
  val withBoldTitles = withParagraphs.replace(
    Regex("""(?m)^(\d+\.\s*)(\S+)""")
  ) { match ->
    "${match.groupValues[1]}<b>${match.groupValues[2]}</b>"
  }
  return withBoldTitles
    .split("\n\n")
    .joinToString("<br/><br/>") { paragraph ->
      paragraph.trim().replace("\n", "<br/>")
    }
}

@Composable
fun ResultScreen(
  onHistory: () -> Unit,
  onGoHome: () -> Unit
) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val strings = LocalStrings.current
  val context = LocalContext.current

  val response = AnalysisSession.lastAnalysisResponse
  val analysisType = AnalysisSession.analysisType
  val resultJson = AnalysisSession.lastResultJson ?: "Нет данных"

  fun launchShare() {
    val text = buildSharePlainText(
      response = response,
      analysisType = analysisType,
      briefOnly = false,
      strings = strings
    )
    if (text.isNullOrBlank()) {
      Toast.makeText(context, strings.shareNoData, Toast.LENGTH_SHORT).show()
    } else {
      val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        putExtra(Intent.EXTRA_SUBJECT, strings.shareSubject)
      }
      context.startActivity(Intent.createChooser(send, strings.share))
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
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
    }

    val titleColor = if (theme.type == ThemeType.LIGHT) Color.White else colors.backgroundTextPrimary
    val cardTextColor = if (theme.type == ThemeType.LIGHT) Color.White else colors.backgroundTextPrimary
    val secondaryColor = if (theme.type == ThemeType.LIGHT) Color.White.copy(alpha = 0.85f) else colors.backgroundTextSecondary

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp, vertical = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(24.dp))

      when {
        analysisType == "emostate" && response?.success == true && !response.result?.emoScales.isNullOrEmpty() -> {
          Box(modifier = Modifier.weight(1f)) {
            EmostateResultContent(
              response = response,
              titleColor = titleColor,
              cardTextColor = cardTextColor,
              secondaryColor = secondaryColor,
              theme = theme,
              strings = strings
            )
          }
        }
        analysisType == "psytype" && response?.success == true && !response.result?.psyTypes.isNullOrEmpty() -> {
          Box(modifier = Modifier.weight(1f)) {
            PsytypeResultContent(
              response = response,
              titleColor = titleColor,
              cardTextColor = cardTextColor,
              secondaryColor = secondaryColor,
              theme = theme,
              strings = strings
            )
          }
        }
        else -> {
          Text(
            text = strings.result,
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 42.sp),
            color = titleColor,
            fontWeight = FontWeight.Light,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(24.dp))
          Text(
            text = resultJson,
            style = cardParagraphTextStyle(),
            color = titleColor,
            modifier = Modifier
              .weight(1f)
              .fillMaxWidth()
              .verticalScroll(rememberScrollState())
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          ThemedOutlinedButton(
            text = strings.share,
            onClick = { launchShare() },
            modifier = Modifier.weight(1f)
          )
          ThemedFilledButton(
            text = strings.goHome,
            onClick = onGoHome,
            modifier = Modifier.weight(1f)
          )
        }
        ThemedFilledButton(
          text = strings.statesChart,
          onClick = onHistory,
          modifier = Modifier.fillMaxWidth()
        )
      }
      Spacer(modifier = Modifier.height(10.dp))
    }
  }
}

@Composable
private fun EmostateResultContent(
  response: AnalysisResponse,
  titleColor: Color,
  cardTextColor: Color,
  secondaryColor: Color,
  theme: com.vanoprojects.voxera.ui.theme.VoxeraTheme,
  strings: com.vanoprojects.voxera.ui.strings.Strings
) {
  var hintSheet by remember { mutableStateOf<ResultHintUi?>(null) }
  val scrollState = rememberScrollState()
  val emoScales = (response.result?.emoScales ?: emptyList())
    .sortedByDescending { it.value }
  val fromModel = response.result?.description.orEmpty()
  val descriptionRaw = fromModel.ifEmpty { extractDescriptionFromRawJson() }
  val description = formatEmostateDescriptionHtml(descriptionRaw)
  Log.d("DescriptionExtract", "Emostate: fromModel=${fromModel.length}, final description=${description.length}, showCard=${description.isNotEmpty()}")

  Box(modifier = Modifier.fillMaxSize()) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
    ) {
      Text(
        text = strings.emostateResultTitle,
        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp),
        color = titleColor,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = "${strings.emostateParameters}:",
        style = MaterialTheme.typography.titleMedium,
        color = titleColor,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(12.dp))

      emoScales.forEach { scale ->
        EmoScaleCard(
          name = EmoScaleNames.translate(scale.name, strings),
          value = scale.value,
          textColor = cardTextColor,
          theme = theme,
          onInfoClick = {
            val (title, body) = emostateMetricHintTitleBody(scale.name, strings)
            hintSheet = ResultHintUi(title = title, body = body)
          }
        )
        Spacer(modifier = Modifier.height(10.dp))
      }

      Spacer(modifier = Modifier.height(24.dp))

      if (description.isNotEmpty()) {
        Text(
          text = strings.emostateReportTitle,
          style = MaterialTheme.typography.titleMedium,
          color = titleColor,
          fontWeight = FontWeight.Medium,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        DescriptionCard(
          description = description,
          textColor = cardTextColor,
          secondaryColor = secondaryColor,
          theme = theme,
          onInfoClick = {
            hintSheet = ResultHintUi(
              title = strings.emostateReportInfoSheetTitle,
              body = emostateDescriptionInterpretationBody(strings)
            )
          }
        )

        Spacer(modifier = Modifier.height(24.dp))
      }
    }

    ResultHintBottomSheet(
      data = hintSheet,
      theme = theme,
      cardTextColor = cardTextColor,
      onDismiss = { hintSheet = null }
    )
  }
}

private fun formatPsyTypeName(name: String): String =
  name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

@Composable
private fun PsytypeResultContent(
  response: AnalysisResponse,
  titleColor: Color,
  cardTextColor: Color,
  secondaryColor: Color,
  theme: com.vanoprojects.voxera.ui.theme.VoxeraTheme,
  strings: com.vanoprojects.voxera.ui.strings.Strings
) {
  var hintSheet by remember { mutableStateOf<ResultHintUi?>(null) }
  val scrollState = rememberScrollState()
  val psyTypes = response.result?.psyTypes ?: emptyList()
  val fromModel = response.result?.description.orEmpty()
  val description = fromModel.ifEmpty { extractDescriptionFromRawJson() }
  Log.d("DescriptionExtract", "Psytype: fromModel=${fromModel.length}, final description=${description.length}, showCard=${description.isNotEmpty()}")
  val sorted = psyTypes.sortedByDescending { it.value }
  val leading = sorted.getOrNull(0)
  val active = sorted.getOrNull(1)

  Box(modifier = Modifier.fillMaxSize()) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
    ) {
      Text(
        text = strings.psytypeResultTitle,
        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp),
        color = titleColor,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(20.dp))

      LeadingActiveCard(
        leadingType = leading?.let { formatPsyTypeName(it.name) to it.value } ?: ("—" to 0.0),
        activeType = active?.let { formatPsyTypeName(it.name) to it.value } ?: ("—" to 0.0),
        leadingLabel = strings.leadingType,
        activeLabel = strings.activeType,
        textColor = cardTextColor,
        theme = theme
      )

      Spacer(modifier = Modifier.height(24.dp))

      Text(
        text = "${strings.psytypeAllTypes}:",
        style = MaterialTheme.typography.titleMedium,
        color = titleColor,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(12.dp))

      sorted.forEach { psyType ->
        PsyTypeCard(
          name = formatPsyTypeName(psyType.name),
          value = psyType.value,
          textColor = cardTextColor,
          theme = theme,
          onInfoClick = {
            val (title, body) = psytypeHintTitleBody(psyType.name, strings)
            hintSheet = ResultHintUi(
              title = title,
              body = body,
              psytypeStyle = true,
              personalityTypeLabel = strings.psytypePersonalityTypeLabel,
              sectionTitle = strings.psytypeReportTitle
            )
          }
        )
        Spacer(modifier = Modifier.height(10.dp))
      }

      Spacer(modifier = Modifier.height(24.dp))

      if (description.isNotEmpty()) {
        Text(
          text = strings.psytypeReportTitle,
          style = MaterialTheme.typography.titleMedium,
          color = titleColor,
          fontWeight = FontWeight.Medium,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        DescriptionCard(
          description = description,
          textColor = cardTextColor,
          secondaryColor = secondaryColor,
          theme = theme
        )

        Spacer(modifier = Modifier.height(24.dp))
      }
    }

    ResultHintBottomSheet(
      data = hintSheet,
      theme = theme,
      cardTextColor = cardTextColor,
      onDismiss = { hintSheet = null }
    )
  }
}

@Composable
private fun LeadingActiveCard(
  leadingType: Pair<String, Double>,
  activeType: Pair<String, Double>,
  leadingLabel: String,
  activeLabel: String,
  textColor: Color,
  theme: com.vanoprojects.voxera.ui.theme.VoxeraTheme
) {
  val cardShape = RoundedCornerShape(12.dp)
  val cardGradient = when (theme.type) {
    ThemeType.LIGHT -> listOf(
      Color(0xFF003A8C).copy(alpha = 0.9f),
      Color(0xFF0055BD).copy(alpha = 0.9f)
    )
    else -> theme.colors.cardBackgroundGradient
  }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(cardShape)
      .background(brush = Brush.linearGradient(cardGradient))
      .padding(16.dp)
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      Text(
        text = "$leadingLabel: ${leadingType.first} (${"%.2f".format(leadingType.second)}%)",
        style = MaterialTheme.typography.titleMedium,
        color = textColor,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "$activeLabel: ${activeType.first} (${"%.2f".format(activeType.second)}%)",
        style = MaterialTheme.typography.titleMedium,
        color = textColor,
        fontWeight = FontWeight.Medium
      )
    }
  }
}

@Composable
private fun PsyTypeCard(
  name: String,
  value: Double,
  textColor: Color,
  theme: com.vanoprojects.voxera.ui.theme.VoxeraTheme,
  onInfoClick: () -> Unit
) {
  val cardShape = RoundedCornerShape(12.dp)
  val progress = (value / 100.0).toFloat().coerceIn(0f, 1f)
  val percentStr = "%.2f".format(value)

  val cardGradient = when (theme.type) {
    ThemeType.LIGHT -> listOf(
      Color(0xFF003A8C).copy(alpha = 0.9f),
      Color(0xFF0055BD).copy(alpha = 0.9f)
    )
    else -> theme.colors.cardBackgroundGradient
  }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(cardShape)
      .background(brush = Brush.linearGradient(cardGradient))
      .padding(16.dp)
  ) {
    val endClearanceForInfo = 52.dp
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(end = endClearanceForInfo)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "$name:",
          style = MaterialTheme.typography.bodyLarge,
          color = textColor,
          fontWeight = FontWeight.Medium
        )
        Text(
          text = "$percentStr%",
          style = MaterialTheme.typography.titleMedium,
          color = textColor,
          fontWeight = FontWeight.SemiBold
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .clip(RoundedCornerShape(3.dp))
          .background(Color.White.copy(alpha = 0.25f))
      ) {
        Box(
          modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(progress)
            .clip(RoundedCornerShape(3.dp))
            .background(VoxeraColors.PrimaryGlow)
        )
      }
    }
    MetricInfoButton(
      onClick = onInfoClick,
      contentColor = textColor,
      modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(top = 2.dp, end = 2.dp)
    )
  }
}

@Composable
private fun EmoScaleCard(
  name: String,
  value: Int,
  textColor: Color,
  theme: com.vanoprojects.voxera.ui.theme.VoxeraTheme,
  onInfoClick: () -> Unit
) {
  val cardShape = RoundedCornerShape(12.dp)
  val progress = (value / 100f).coerceIn(0f, 1f)

  val cardGradient = when (theme.type) {
    ThemeType.LIGHT -> listOf(
      Color(0xFF003A8C).copy(alpha = 0.9f),
      Color(0xFF0055BD).copy(alpha = 0.9f)
    )
    else -> theme.colors.cardBackgroundGradient
  }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(cardShape)
      .background(brush = Brush.linearGradient(cardGradient))
      .padding(16.dp)
  ) {
    // Запас справа под кнопку «i» (36dp круг + отступы), чтобы цифры и шкала не прижимались
    val endClearanceForInfo = 52.dp
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(end = endClearanceForInfo)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "$name:",
          style = MaterialTheme.typography.bodyLarge,
          color = textColor,
          fontWeight = FontWeight.Medium,
          modifier = Modifier.weight(1f, fill = false)
        )
        Text(
          text = value.toString(),
          style = MaterialTheme.typography.titleMedium,
          color = textColor,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.padding(end = 4.dp)
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .clip(RoundedCornerShape(3.dp))
          .background(Color.White.copy(alpha = 0.25f))
      ) {
        Box(
          modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(progress)
            .clip(RoundedCornerShape(3.dp))
            .background(VoxeraColors.PrimaryGlow)
        )
      }
    }
    MetricInfoButton(
      onClick = onInfoClick,
      contentColor = textColor,
      modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(top = 2.dp, end = 2.dp)
    )
  }
}

@Composable
private fun MetricInfoButton(
  onClick: () -> Unit,
  contentColor: Color,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .padding(2.dp)
      .size(36.dp)
      .clip(CircleShape)
      .background(contentColor.copy(alpha = 0.2f))
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = "i",
      style = MaterialTheme.typography.titleSmall.copy(
        fontSize = 17.sp,
        lineHeight = 18.sp,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Bold
      ),
      color = contentColor
    )
  }
}

private data class ResultHintUi(
  val title: String,
  val body: String,
  val psytypeStyle: Boolean = false,
  val personalityTypeLabel: String = "",
  val sectionTitle: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResultHintBottomSheet(
  data: ResultHintUi?,
  theme: com.vanoprojects.voxera.ui.theme.VoxeraTheme,
  cardTextColor: Color,
  onDismiss: () -> Unit
) {
  if (data == null) return
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val scroll = rememberScrollState()
  val cardShape = RoundedCornerShape(16.dp)
  val cardGradient = when (theme.type) {
    ThemeType.LIGHT -> listOf(
      Color(0xFF003A8C).copy(alpha = 0.94f),
      Color(0xFF0055BD).copy(alpha = 0.94f)
    )
    ThemeType.GLASS -> listOf(
      Color(0xFF1A2436),
      Color(0xFF243149)
    )
  }
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = Color.Transparent,
    tonalElevation = 0.dp,
    dragHandle = {
      Box(
        modifier = Modifier
          .padding(vertical = 10.dp)
          .width(40.dp)
          .height(4.dp)
          .clip(RoundedCornerShape(2.dp))
          .background(cardTextColor.copy(alpha = 0.35f))
      )
    }
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .padding(bottom = 32.dp)
    ) {
      if (data.psytypeStyle && data.sectionTitle.isNotBlank()) {
        Text(
          text = data.sectionTitle,
          style = MaterialTheme.typography.titleMedium,
          color = cardTextColor,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.padding(bottom = 10.dp)
        )
      }
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(cardShape)
          .background(brush = Brush.linearGradient(cardGradient))
          .border(1.dp, cardTextColor.copy(alpha = 0.22f), cardShape)
      ) {
        Column(
          modifier = Modifier
            .padding(20.dp)
            .verticalScroll(scroll)
        ) {
          if (data.psytypeStyle) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "★",
                color = Color(0xFFFFD54F),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = data.title,
                style = MaterialTheme.typography.titleMedium,
                color = cardTextColor,
                fontWeight = FontWeight.Bold
              )
              if (data.personalityTypeLabel.isNotBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "${data.personalityTypeLabel} ",
                  style = MaterialTheme.typography.bodyMedium,
                  color = cardTextColor.copy(alpha = 0.9f)
                )
                Text(
                  text = data.title,
                  style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                  color = cardTextColor.copy(alpha = 0.9f)
                )
              }
            }
            Spacer(modifier = Modifier.height(12.dp))
          } else if (data.title.isNotBlank()) {
            Text(
              text = data.title,
              style = MaterialTheme.typography.titleMedium,
              color = cardTextColor,
              fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
          }
          Text(
            text = data.body,
            style = MaterialTheme.typography.bodyMedium,
            color = cardTextColor.copy(alpha = 0.94f),
            lineHeight = 22.sp
          )
        }
      }
    }
  }
}

@Composable
private fun DescriptionCard(
  description: String,
  textColor: Color,
  secondaryColor: Color,
  theme: com.vanoprojects.voxera.ui.theme.VoxeraTheme,
  onInfoClick: (() -> Unit)? = null
) {
  val cardShape = RoundedCornerShape(12.dp)

  val cardGradient = when (theme.type) {
    ThemeType.LIGHT -> listOf(
      Color(0xFF003A8C).copy(alpha = 0.9f),
      Color(0xFF0055BD).copy(alpha = 0.9f)
    )
    else -> theme.colors.cardBackgroundGradient
  }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .wrapContentHeight()
      .clip(cardShape)
      .background(brush = Brush.linearGradient(cardGradient))
  ) {
    AndroidView(
      modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .padding(horizontal = 20.dp, vertical = 18.dp),
      factory = { ctx ->
        android.widget.TextView(ctx).apply {
          layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
          )
          setTextColor(textColor.toArgb())
          textSize = 17f
          setLineSpacing(10f, 1.25f)
          letterSpacing = 0f
          isSingleLine = false
          movementMethod = LinkMovementMethod.getInstance()
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            justificationMode = Layout.JUSTIFICATION_MODE_NONE
            hyphenationFrequency = Layout.HYPHENATION_FREQUENCY_NONE
          }
        }
      },
      update = { textView ->
        textView.text = HtmlCompat.fromHtml(description, HtmlCompat.FROM_HTML_MODE_LEGACY)
        textView.setTextColor(textColor.toArgb())
        textView.requestLayout()
      }
    )
    onInfoClick?.let { click ->
      MetricInfoButton(
        onClick = click,
        contentColor = textColor,
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(top = 8.dp, end = 8.dp)
      )
    }
  }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ResultScreenEmostatePreview() {
  VoxeraTheme {
    AnalysisSession.lastAnalysisResponse = AnalysisResponse(
      success = true,
      analysisType = "emostate",
      result = com.vanoprojects.voxera.data.model.AnalysisResult(
        psyTypes = null,
        emoScales = listOf(
          EmoScale(1, "ability_to_attract", 87),
          EmoScale(2, "energy_level", 68),
          EmoScale(3, "authority", 65),
          EmoScale(4, "kindness", 47),
          EmoScale(5, "stress_tolerance", 20)
        ),
        description = "Данный отчет представляет результаты анализа психоэмоционального состояния."
      )
    )
    AnalysisSession.analysisType = "emostate"
    ResultScreen(onHistory = {}, onGoHome = {})
  }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ResultScreenPsytypePreview() {
  VoxeraTheme {
    AnalysisSession.lastAnalysisResponse = AnalysisResponse(
      success = true,
      analysisType = "psytype",
      result = com.vanoprojects.voxera.data.model.AnalysisResult(
        psyTypes = listOf(
          PsyType(6, "expert", 77.80),
          PsyType(8, "stabilizer", 45.0),
          PsyType(5, "communicator", 17.22),
          PsyType(1, "manager", 14.82),
          PsyType(4, "creator", 13.38),
          PsyType(7, "realizer", 11.16),
          PsyType(2, "visionary", 9.49),
          PsyType(3, "leader", 8.31)
        ),
        emoScales = null,
        description = "Тип личности <b>Expert</b> характеризуется высоким уровнем аналитических способностей."
      )
    )
    AnalysisSession.analysisType = "psytype"
    ResultScreen(onHistory = {}, onGoHome = {})
  }
}
