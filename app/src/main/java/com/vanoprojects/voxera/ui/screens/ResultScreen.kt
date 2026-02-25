package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.util.Log
import androidx.core.text.HtmlCompat
import com.vanoprojects.voxera.R
import com.vanoprojects.voxera.data.AnalysisSession
import com.vanoprojects.voxera.data.model.AnalysisResponse
import com.vanoprojects.voxera.data.model.EmoScale
import com.vanoprojects.voxera.data.model.PsyType
import com.vanoprojects.voxera.ui.strings.LocalStrings
import com.vanoprojects.voxera.ui.theme.*

private val EMO_SCALE_NAME_RU = mapOf(
    "ability_to_attract" to "Притягательность",
    "expressivity" to "Экспрессивность",
    "authority" to "Властность",
    "person_manifestation" to "Демонстративность",
    "kindness" to "Дружелюбие",
    "self_control" to "Самоконтроль",
    "openness_to_new" to "Открытость к опыту",
    "energy_level" to "Жизнерадостность",
    "emo_engage" to "Вдохновенность",
    "ability_to_set_goals" to "Реализованность",
    "ability_to_assert" to "Независимость",
    "person_harmonicity" to "Уравновешенность",
    "emotional_confidence" to "Эмоциональность",
    "stress_tolerance" to "Стрессоустойчивость",
)

private fun translateEmoScaleName(name: String): String {
    if (name.any { it in '\u0400'..'\u04FF' }) return name // уже на русском
    return EMO_SCALE_NAME_RU[name.lowercase()] ?: name
}

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

@Composable
fun ResultScreen(
  onNewAnalysis: () -> Unit,
  onShare: () -> Unit,
  onHistory: () -> Unit
) {
  val theme = LocalVoxeraTheme.current
  val colors = theme.colors
  val strings = LocalStrings.current

  val response = AnalysisSession.lastAnalysisResponse
  val analysisType = AnalysisSession.analysisType
  val resultJson = AnalysisSession.lastResultJson ?: "Нет данных"

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
      ThemeType.DARK -> {
        Image(
          painter = painterResource(R.drawable.bg_reverse_stars),
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
            style = MaterialTheme.typography.bodyMedium,
            color = titleColor,
            modifier = Modifier
              .weight(1f)
              .fillMaxWidth()
              .verticalScroll(rememberScrollState())
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ThemedOutlinedButton(
          text = strings.share,
          onClick = onShare,
          modifier = Modifier.weight(1f)
        )
        ThemedFilledButton(
          text = strings.newAnalysis,
          onClick = onNewAnalysis,
          modifier = Modifier.weight(1f)
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
  val scrollState = rememberScrollState()
  val emoScales = response.result?.emoScales ?: emptyList()
  val fromModel = response.result?.description.orEmpty()
  val description = fromModel.ifEmpty { extractDescriptionFromRawJson() }
  Log.d("DescriptionExtract", "Emostate: fromModel=${fromModel.length}, final description=${description.length}, showCard=${description.isNotEmpty()}")

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
        name = translateEmoScaleName(scale.name),
        value = scale.value,
        textColor = cardTextColor,
        secondaryColor = secondaryColor,
        theme = theme
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
        theme = theme
      )

      Spacer(modifier = Modifier.height(24.dp))
    }
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
  val scrollState = rememberScrollState()
  val psyTypes = response.result?.psyTypes ?: emptyList()
  val fromModel = response.result?.description.orEmpty()
  val description = fromModel.ifEmpty { extractDescriptionFromRawJson() }
  Log.d("DescriptionExtract", "Psytype: fromModel=${fromModel.length}, final description=${description.length}, showCard=${description.isNotEmpty()}")
  val sorted = psyTypes.sortedByDescending { it.value }
  val leading = sorted.getOrNull(0)
  val active = sorted.getOrNull(1)

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
        theme = theme
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
  theme: com.vanoprojects.voxera.ui.theme.VoxeraTheme
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
    Column(modifier = Modifier.fillMaxWidth()) {
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
  }
}

@Composable
private fun EmoScaleCard(
  name: String,
  value: Int,
  textColor: Color,
  secondaryColor: Color,
  theme: com.vanoprojects.voxera.ui.theme.VoxeraTheme
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
      .then(
        if (theme.type == ThemeType.LIGHT) {
          Modifier
        } else {
          Modifier
        }
      )
      .padding(16.dp)
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
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
          text = value.toString(),
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
  }
}

@Composable
private fun DescriptionCard(
  description: String,
  textColor: Color,
  secondaryColor: Color,
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
      .wrapContentHeight()
      .clip(cardShape)
      .background(brush = Brush.linearGradient(cardGradient))
      .padding(16.dp)
  ) {
    AndroidView(
      modifier = Modifier.fillMaxWidth(),
      factory = { ctx ->
        android.widget.TextView(ctx).apply {
          setTextColor(textColor.toArgb())
          textSize = 16f
          setLineSpacing(6f, 1.2f)
        }
      },
      update = { textView ->
        textView.text = HtmlCompat.fromHtml(description, HtmlCompat.FROM_HTML_MODE_LEGACY)
        textView.setTextColor(textColor.toArgb())
      }
    )
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
          EmoScale(2, "expressivity", 68),
          EmoScale(3, "authority", 65),
          EmoScale(4, "kindness", 47),
          EmoScale(5, "stress_tolerance", 20)
        ),
        description = "Данный отчет представляет результаты анализа психоэмоционального состояния."
      )
    )
    AnalysisSession.analysisType = "emostate"
    ResultScreen(onNewAnalysis = {}, onShare = {}, onHistory = {})
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
    ResultScreen(onNewAnalysis = {}, onShare = {}, onHistory = {})
  }
}
