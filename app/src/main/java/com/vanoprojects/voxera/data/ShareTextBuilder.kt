package com.vanoprojects.voxera.data

import androidx.core.text.HtmlCompat
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.vanoprojects.voxera.data.model.AnalysisResponse
import com.vanoprojects.voxera.data.model.EmoScale
import com.vanoprojects.voxera.data.model.PsyType
import com.vanoprojects.voxera.ui.strings.Strings

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
  "stress_tolerance" to "Стрессоустойчивость"
)

private fun translateEmoScaleName(name: String): String {
  if (name.any { it in '\u0400'..'\u04FF' }) return name
  return EMO_SCALE_NAME_RU[name.lowercase()] ?: name
}

private fun formatPsyTypeName(name: String): String =
  name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

private fun stripHtml(html: String): String =
  HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim()

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
  val plain = stripHtml(rawHtml)
  if (!brief || plain.length <= BRIEF_DESC_MAX) return plain
  return plain.take(BRIEF_DESC_MAX).trimEnd() + "…"
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
          append("${strings.leadingType}: ${formatPsyTypeName(lead.name)} (${"%.2f".format(lead.value)}%)\n")
          val active = sorted.getOrNull(1)
          if (active != null) {
            append("${strings.activeType}: ${formatPsyTypeName(active.name)} (${"%.2f".format(active.value)}%)\n")
          }
          if (desc.isNotEmpty()) {
            append("\n")
            append(desc)
          }
        } else {
          sorted.forEach { pt ->
            append("${formatPsyTypeName(pt.name)}: ${"%.2f".format(pt.value)}%\n")
          }
          if (descRaw.isNotEmpty()) {
            append("\n")
            append(stripHtml(descRaw))
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
            append("${translateEmoScaleName(s.name)}: ${s.value}\n")
          }
          if (desc.isNotEmpty()) {
            append("\n")
            append(desc)
          }
        } else {
          sorted.forEach { s ->
            append("${translateEmoScaleName(s.name)}: ${s.value}\n")
          }
          if (descRaw.isNotEmpty()) {
            append("\n")
            append(stripHtml(descRaw))
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
        "${strings.leadingType}: ${formatPsyTypeName(lead.name)} (${"%.2f".format(lead.value)}%)"
    }
    else -> {
      val scales = result.emoScales.orEmpty()
      if (scales.isEmpty()) return strings.shareNoData to ""
      val top3 = scales.sortedByDescending { it.value }.take(3)
      val subtitle = top3.joinToString("\n") { s ->
        "${translateEmoScaleName(s.name)}: ${s.value}"
      }
      strings.emostateResultTitle to subtitle
    }
  }
}
