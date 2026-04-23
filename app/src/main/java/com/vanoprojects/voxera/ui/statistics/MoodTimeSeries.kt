package com.vanoprojects.voxera.ui.statistics

import com.vanoprojects.voxera.data.HistoryEntry
import com.vanoprojects.voxera.data.model.EmoScale
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import androidx.compose.ui.geometry.Offset as ComposeOffset
import androidx.compose.ui.graphics.Path
import kotlin.math.max
import kotlin.math.min

enum class MoodStatsPeriod { WEEK, MONTH }

data class MoodDayPoint(
  val dayIndex: Int,
  val label: String,
  /** scaleKey -> value 0..100 or null if no measurement that day */
  val values: Map<String, Int?>
)

data class MoodChartSeries(
  val dayPoints: List<MoodDayPoint>,
  val yMax: Int = 100
)

object MoodTimeSeries {
  private const val EMO = "emostate"
  private val zone: ZoneId get() = ZoneId.systemDefault()

  fun fromEntries(
    entries: List<HistoryEntry>,
    period: MoodStatsPeriod,
    scaleKeys: List<String>
  ): MoodChartSeries {
    val today = LocalDate.now(zone)
    val daysCount = if (period == MoodStatsPeriod.WEEK) 7 else 30
    val startDay = today.minusDays((daysCount - 1).toLong())

    val emoEntries = entries
      .asSequence()
      .filter { it.analysisType == EMO }
      .mapNotNull { e -> e.toAnalysisResponse()?.let { e.timestamp to it } }
      .toList()
      .filter { (ts, resp) -> resp.result?.emoScales?.isNotEmpty() == true }
      .sortedBy { it.first }

    val byDay = mutableMapOf<LocalDate, Map<String, Int>>()
    for ((ts, resp) in emoEntries) {
      val day = Instant.ofEpochMilli(ts).atZone(zone).toLocalDate()
      if (day.isBefore(startDay) || day.isAfter(today)) continue
      val map = emoValueMap(resp.result?.emoScales.orEmpty(), scaleKeys)
      if (map.isNotEmpty()) {
        byDay[day] = map
      }
    }

    val labelFmt = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(Locale.getDefault())
    val points = (0 until daysCount).map { i ->
      val d = startDay.plusDays(i.toLong())
      val v = byDay[d]
      MoodDayPoint(
        dayIndex = i,
        label = d.format(labelFmt),
        values = scaleKeys.associateWith { k -> v?.get(k) }
      )
    }
    return MoodChartSeries(dayPoints = points, yMax = 100)
  }

  private fun emoValueMap(scales: List<EmoScale>, keys: List<String>): Map<String, Int> {
    val byKey = scales.associate { it.name.lowercase() to it.value }
    return keys.mapNotNull { k ->
      val v = byKey[k] ?: byKey[k.replace("_", " ")]
      v?.let { k to it }
    }.toMap()
  }
}

/** Точки для шкалы: x нормирован [0,1] по дням, y = value/100. Разрывы по null. */
fun splitSeriesIntoSegments(
  points: List<MoodDayPoint>,
  scaleKey: String
): List<List<Pair<Float, Float>>> {
  val w = (points.size - 1).coerceAtLeast(1).toFloat()
  val segs = mutableListOf<MutableList<Pair<Float, Float>>>()
  var cur: MutableList<Pair<Float, Float>>? = null
  for (i in points.indices) {
    val yv = points[i].values[scaleKey]
    if (yv == null) {
      flushSegment(cur, segs)
      cur = null
      continue
    }
    if (cur == null) cur = mutableListOf()
    val xn = if (points.size == 1) 0.5f else i / w
    val yf = (yv.coerceIn(0, 100)) / 100f
    cur.add(Pair(xn, yf))
  }
  flushSegment(cur, segs)
  return segs
}

private fun flushSegment(
  cur: MutableList<Pair<Float, Float>>?,
  segs: MutableList<MutableList<Pair<Float, Float>>>
) {
  if (cur == null || cur.isEmpty()) return
  if (cur.size == 1) {
    val p = cur[0]
    segs.add(mutableListOf(p, p))
  } else {
    segs.add(cur)
  }
}

/** Catmull–Rom style cubic through points in normalized [0,1] x and [0,1] y. */
fun buildSmoothPath(
  points: List<Pair<Float, Float>>,
  widthPx: Float,
  heightPx: Float,
  paddingL: Float,
  paddingR: Float,
  paddingT: Float,
  paddingB: Float
): Path {
  val path = Path()
  if (points.isEmpty()) return path
  val innerW = (widthPx - paddingL - paddingR).coerceAtLeast(1f)
  val innerH = (heightPx - paddingT - paddingB).coerceAtLeast(1f)
  fun tx(x: Float) = paddingL + x * innerW
  fun ty(y: Float) = paddingT + (1f - y) * innerH
  if (points.size == 1) {
    val x = tx(points[0].first)
    val y = ty(points[0].second)
    path.moveTo(x, y)
    path.lineTo(x + 0.5f, y)
    return path
  }
  val p = points.map { ComposeOffset(tx(it.first), ty(it.second)) }
  path.moveTo(p[0].x, p[0].y)
  for (i in 0 until p.size - 1) {
    val p0 = p[max(0, i - 1)]
    val p1 = p[i]
    val p2 = p[i + 1]
    val p3 = p[min(p.size - 1, i + 2)]
    val c1x = p1.x + (p2.x - p0.x) / 6f
    val c1y = p1.y + (p2.y - p0.y) / 6f
    val c2x = p2.x - (p3.x - p1.x) / 6f
    val c2y = p2.y - (p3.y - p1.y) / 6f
    if (
      c1x.isFinite() && c1y.isFinite() && c2x.isFinite() && c2y.isFinite() &&
      p2.x.isFinite() && p2.y.isFinite()
    ) {
      path.cubicTo(c1x, c1y, c2x, c2y, p2.x, p2.y)
    } else {
      path.lineTo(p2.x, p2.y)
    }
  }
  return path
}
