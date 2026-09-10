package com.vanoprojects.voxera.data

import androidx.core.text.HtmlCompat
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.vanoprojects.voxera.data.model.AnalysisResponse
import com.vanoprojects.voxera.data.model.EmoScale
import com.vanoprojects.voxera.data.model.PsyType
import com.vanoprojects.voxera.ui.strings.EmoScaleNames
import com.vanoprojects.voxera.ui.strings.Strings
import kotlin.math.roundToInt

private fun formatPsyTypeName(name: String): String =
  name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

private fun extractDescriptionFromJsonString(jsonStr: String): String = try {
  val json = Gson().fromJson(jsonStr, JsonObject::class.java) ?: return ""
  val fromResult = json.getAsJsonObject("result")?.let { resultObj ->
    (resultObj.get("description") as? JsonPrimitive)?.asString
  }
  val fromTop = (json.get("description") as? JsonPrimitive)?.asString
  fromResult ?: fromTop ?: ""
} catch (_: Exception) {
  ""
}

private fun fullDescription(response: AnalysisResponse): String {
  val fromModel = response.result?.description.orEmpty()
  if (fromModel.isNotEmpty()) return fromModel
  val raw = AnalysisSession.lastRawApiResponse
  if (!raw.isNullOrEmpty()) {
    val fromRaw = extractDescriptionFromJsonString(raw)
    if (fromRaw.isNotEmpty()) return fromRaw
  }
  val resultJson = AnalysisSession.lastResultJson
  if (!resultJson.isNullOrEmpty() && resultJson.startsWith("{")) {
    val fromResult = extractDescriptionFromJsonString(resultJson)
    if (fromResult.isNotEmpty()) return fromResult
  }
  return ""
}

private const val BRIEF_DESC_MAX = 450

private fun truncateDescription(rawHtml: String, brief: Boolean): String {
  val plain = formatEmostateDescriptionForShare(rawHtml)
  if (!brief || plain.length <= BRIEF_DESC_MAX) return plain
  return plain.take(BRIEF_DESC_MAX).trimEnd() + "…"
}

/** Paragraph breaks before "2. …"; title on its own line, then body. */
private fun formatEmostateDescriptionForShare(raw: String): String {
  if (raw.isBlank()) return raw
  val plain = HtmlCompat.fromHtml(raw, HtmlCompat.FROM_HTML_MODE_LEGACY)
    .toString()
    .replace("\r\n", "\n")
    .replace('\r', '\n')
    .trim()
  val withParagraphs = plain
    .replace(Regex("""(?<!^)\s+(?=\d+\.\s*\S)"""), "\n\n")
    .replace(Regex("""\n{3,}"""), "\n\n")
  return withParagraphs.split("\n\n").joinToString("\n\n") { paragraph ->
    val trimmed = paragraph.trim()
    val match = Regex("""^(\d+\.\s*)(\S+)\s*(.*)$""", RegexOption.DOT_MATCHES_ALL)
      .matchEntire(trimmed)
    if (match != null) {
      val prefix = match.groupValues[1]
      val title = match.groupValues[2]
      val body = match.groupValues[3].trim()
      if (body.isEmpty()) "$prefix$title" else "$prefix$title\n$body"
    } else {
      trimmed
    }
  }.trim()
}

/**
 * Текст для ACTION_SEND. [null] — нечего публиковать.
 */
fun buildSharePlainText(
  response: AnalysisResponse?,
  analysisType: String,
  briefOnly: Boolean,
  strings: Strings
): String? {
  if (response == null || !response.success) return null
  val result = response.result ?: return null

  val header = "Voxera\n"

  return when (analysisType) {
    "psytype" -> {
      val types = result.psyTypes.orEmpty()
      if (types.isEmpty()) return null
      val sorted = types.sortedByDescending { it.value }
      val descRaw = fullDescription(response)
      val desc = truncateDescription(descRaw, briefOnly)

      buildString {
        append(header)
        append("\n")
        append(strings.psytypeResultTitle)
        append("\n\n")
        if (briefOnly) {
          val lead = sorted.first()
          append("${strings.leadingType}: ${formatPsyTypeName(lead.name)} (${lead.value.roundToInt()}%)\n")
          val active = sorted.getOrNull(1)
          if (active != null) {
            append("${strings.activeType}: ${formatPsyTypeName(active.name)} (${active.value.roundToInt()}%)\n")
          }
          if (desc.isNotEmpty()) {
            append("\n")
            append(desc)
          }
        } else {
          sorted.forEach { pt ->
            append("${formatPsyTypeName(pt.name)}: ${pt.value.roundToInt()}%\n")
          }
          if (descRaw.isNotEmpty()) {
            append("\n")
            append(formatEmostateDescriptionForShare(descRaw))
          }
        }
      }
    }
    else -> {
      val scales = result.emoScales.orEmpty()
      if (scales.isEmpty()) return null
      val descRaw = fullDescription(response)
      val desc = truncateDescription(descRaw, briefOnly)

      buildString {
        append(header)
        append("\n")
        append(strings.emostateResultTitle)
        append("\n\n")
        val sorted = scales.sortedByDescending { it.value }
        if (briefOnly) {
          sorted.take(3).forEach { s ->
            append("${EmoScaleNames.translate(s.name, strings)}: ${s.value}\n")
          }
          if (desc.isNotEmpty()) {
            append("\n")
            append(desc)
          }
        } else {
          sorted.forEach { s ->
            append("${EmoScaleNames.translate(s.name, strings)}: ${s.value}\n")
          }
          if (descRaw.isNotEmpty()) {
            append("\n")
            append(formatEmostateDescriptionForShare(descRaw))
          }
        }
      }
    }
  }
}

/** Первая строка превью и подзаголовок для карточки на экране «Поделиться». */
fun sharePreviewLines(
  response: AnalysisResponse?,
  analysisType: String,
  strings: Strings
): Pair<String, String> {
  if (response == null || !response.success || response.result == null) {
    return strings.shareNoData to ""
  }
  val result = response.result
  return when (analysisType) {
    "psytype" -> {
      val types = result.psyTypes.orEmpty()
      if (types.isEmpty()) return strings.shareNoData to ""
      val lead = types.maxByOrNull { it.value }!!
      strings.psytypeResultTitle to
        "${strings.leadingType}: ${formatPsyTypeName(lead.name)} (${lead.value.roundToInt()}%)"
    }
    else -> {
      val scales = result.emoScales.orEmpty()
      if (scales.isEmpty()) return strings.shareNoData to ""
      val top3 = scales.sortedByDescending { it.value }.take(3)
      val subtitle = top3.joinToString("\n") { s ->
        "${EmoScaleNames.translate(s.name, strings)}: ${s.value}"
      }
      strings.emostateResultTitle to subtitle
    }
  }
}
