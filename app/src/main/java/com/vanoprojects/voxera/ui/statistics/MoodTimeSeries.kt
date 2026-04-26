package com.vanoprojects.voxera.ui.statistics

import com.vanoprojects.voxera.data.HistoryEntry
import com.vanoprojects.voxera.data.model.EmoScale
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import androidx.compose.ui.geometry.Offset as ComposeOffset
import androidx.compose.ui.graphics.Path
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

sealed class MoodStatsPeriod {
  data object Last24h : MoodStatsPeriod()
  data object Week : MoodStatsPeriod()
  data object Month : MoodStatsPeriod()
  data class Custom(val from: LocalDate, val to: LocalDate) : MoodStatsPeriod()
}

data class MoodDayPoint(
  val dayIndex: Int,
  val label: String,
  /** scaleKey -> value 0..100 or null if no measurement in that bucket */
  val values: Map<String, Int?>,
  /**
   * 0f..1f — позиция по оси X внутри окна «сутки»; для недели/месяца/своего — null (индекс
   * точек равномерно по [0,1]).
   */
  val xInWindow: Float? = null
)

data class MoodChartSeries(
  val dayPoints: List<MoodDayPoint>,
  val yMax: Int = 100,
  /**
   * Границы 24-часового окна (мс, UTC) для подписи оси X; только для суток.
   * Подписи равномерно: начало, +6ч, +12ч, +18ч, конец.
   */
  val windowStartMillis: Long? = null,
  val windowEndMillis: Long? = null
)

private data class EmoSample(val atMillis: Long, val values: Map<String, Int>)

object MoodTimeSeries {
  private const val EMO = "emostate"
  private val zone: ZoneId get() = ZoneId.systemDefault()

  fun fromEntries(
    entries: List<HistoryEntry>,
    period: MoodStatsPeriod,
    scaleKeys: List<String>,
    now: Instant = Instant.now()
  ): MoodChartSeries {
    val samples = collectEmoSamples(entries, scaleKeys)
    return when (period) {
      is MoodStatsPeriod.Last24h -> buildHourlySeries(samples, now, scaleKeys)
      is MoodStatsPeriod.Week -> buildDayWindowSeries(samples, 7, scaleKeys)
      is MoodStatsPeriod.Month -> buildDayWindowSeries(samples, 30, scaleKeys)
      is MoodStatsPeriod.Custom -> {
        val a = if (period.from <= period.to) period.from else period.to
        val b = if (period.from <= period.to) period.to else period.from
        buildCustomDaySeries(samples, a, b, scaleKeys)
      }
    }
  }

  private fun collectEmoSamples(
    entries: List<HistoryEntry>,
    scaleKeys: List<String>
  ): List<EmoSample> = entries
    .asSequence()
    .filter { it.analysisType == EMO }
    .mapNotNull { e ->
      val r = e.toAnalysisResponse() ?: return@mapNotNull null
      val m = emoValueMap(r.result?.emoScales.orEmpty(), scaleKeys)
      if (m.isEmpty()) null else EmoSample(e.timestamp, m)
    }
    .sortedBy { it.atMillis }
    .toList()

  /**
   * Имена в ответе API (латиница/кириллица) -> канонический ключ [MoodScaleLabels.ORDER].
   * Шкала «жизнерадостность» в API — обычно [energy_level], иногда имя на русском.
   */
  private val emoNameAliases: Map<String, String> = mapOf(
    "жизнерадостность" to "energy_level",
    "cheerfulness" to "energy_level",
    "vitality" to "energy_level",
    "energy level" to "energy_level"
  )

  private fun emoValueMap(scales: List<EmoScale>, keys: List<String>): Map<String, Int> {
    val byKey = buildMap<String, Int> {
      for (s in scales) {
        val raw = s.name.lowercase().trim()
        put(raw, s.value)
        emoNameAliases[raw]?.let { canonical -> putIfAbsent(canonical, s.value) }
      }
    }
    return keys.mapNotNull { k ->
      val v = byKey[k] ?: byKey[k.replace("_", " ")]
      v?.let { k to it }
    }.toMap()
  }

  private fun averageNumericMaps(
    list: List<Map<String, Int>>,
    scaleKeys: List<String>
  ): Map<String, Int?> {
    if (list.isEmpty()) return scaleKeys.associateWith { null }
    return scaleKeys.associateWith { k ->
      val vals = list.mapNotNull { m -> m[k] }
      if (vals.isEmpty()) null else
        (vals.average() + 1e-6).roundToInt().coerceIn(0, 100)
    }
  }

  private val shortDateFmt: DateTimeFormatter
    get() = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(Locale.getDefault())

  private fun buildDayWindowSeries(
    samples: List<EmoSample>,
    nDays: Int,
    scaleKeys: List<String>
  ): MoodChartSeries {
    if (samples.isEmpty()) {
      val endD = LocalDate.now(zone)
      val startD = endD.minusDays((nDays - 1).toLong())
      return emptyDayGrid(startD, nDays, scaleKeys, shortDateFmt)
    }
    val firstD = samples.minOf { LocalDate.ofInstant(Instant.ofEpochMilli(it.atMillis), zone) }
    val lastD = samples.maxOf { LocalDate.ofInstant(Instant.ofEpochMilli(it.atMillis), zone) }
    val span = ChronoUnit.DAYS.between(firstD, lastD) + 1
    val startD = if (span >= nDays) {
      lastD.minusDays((nDays - 1).toLong())
    } else {
      firstD
    }
    val byDay = samples.groupBy { Instant.ofEpochMilli(it.atMillis).atZone(zone).toLocalDate() }
    val points = (0 until nDays).map { i ->
      val d = startD.plusDays(i.toLong())
      val daySamples = byDay[d].orEmpty()
      val maps = daySamples.map { it.values }
      val av = if (maps.isEmpty()) {
        scaleKeys.associateWith { null }
      } else {
        val avg = averageNumericMaps(maps, scaleKeys)
        scaleKeys.associateWith { k -> avg[k] }
      }
      MoodDayPoint(
        dayIndex = i,
        label = d.format(shortDateFmt),
        values = av
      )
    }
    return MoodChartSeries(dayPoints = points, yMax = 100)
  }

  private fun buildCustomDaySeries(
    samples: List<EmoSample>,
    from: LocalDate,
    to: LocalDate,
    scaleKeys: List<String>
  ): MoodChartSeries {
    val a = if (from <= to) from else to
    val b = if (from <= to) to else from
    val n = (ChronoUnit.DAYS.between(a, b) + 1).toInt().coerceAtLeast(1)
    val byDay = samples.groupBy { Instant.ofEpochMilli(it.atMillis).atZone(zone).toLocalDate() }
    val points = (0 until n).map { i ->
      val d = a.plusDays(i.toLong())
      val daySamples = byDay[d].orEmpty()
      val maps = daySamples.map { it.values }
      val av = if (maps.isEmpty()) {
        scaleKeys.associateWith { null }
      } else {
        val avg = averageNumericMaps(maps, scaleKeys)
        scaleKeys.associateWith { k -> avg[k] }
      }
      MoodDayPoint(i, d.format(shortDateFmt), av)
    }
    return MoodChartSeries(dayPoints = points, yMax = 100)
  }

  private fun emptyDayGrid(
    start: LocalDate,
    count: Int,
    scaleKeys: List<String>,
    fmt: DateTimeFormatter
  ): MoodChartSeries = MoodChartSeries(
    (0 until count).map { i ->
      val d = start.plusDays(i.toLong())
      MoodDayPoint(
        i,
        d.format(fmt),
        scaleKeys.associateWith { null }
      )
    },
    yMax = 100
  )

  /**
   * 24ч: точка на каждый анализ, X = (t − tStart) / 24ч. Граница слева — не «last−24h», если
   * в последние 24ч к lastA есть съёмы: левый край = **самый ранний** из этого интервала
   * (2:23 и 2:27 с длинной историей — оба слева, не в конце полосы).
   */
  private fun buildHourlySeries(
    samples: List<EmoSample>,
    now: Instant,
    scaleKeys: List<String>
  ): MoodChartSeries {
    val timeFmt = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    if (samples.isEmpty()) {
      return MoodChartSeries(dayPoints = emptyList(), yMax = 100)
    }
    val s = samples.sortedBy { it.atMillis }
    val lastA = s.last().atMillis
    val h24ms = 24L * 60 * 60 * 1000
    val prevBeforeLast = if (s.size >= 2) s[s.size - 2].atMillis else null
    val gapOver24h = prevBeforeLast != null && (lastA - prevBeforeLast > h24ms)
    val (tStart, tEnd) = when {
      s.size == 1 || gapOver24h ->
        lastA to (lastA + h24ms)
      else -> {
        val inLastDayToLast = s.filter { it.atMillis >= lastA - h24ms && it.atMillis <= lastA }
        val start = inLastDayToLast.minOf { it.atMillis }
        start to (start + h24ms)
      }
    }
    val inWindow = s.filter { it.atMillis >= tStart && it.atMillis < tEnd }
    val points = inWindow.mapIndexed { idx, sp ->
      val x = ((sp.atMillis - tStart).toFloat() / h24ms.toFloat()).coerceIn(0f, 1f)
      val z = Instant.ofEpochMilli(sp.atMillis).atZone(zone)
      MoodDayPoint(
        dayIndex = idx,
        label = timeFmt.format(z),
        values = scaleKeys.associateWith { k -> sp.values[k] },
        xInWindow = x
      )
    }
    return MoodChartSeries(
      dayPoints = points,
      yMax = 100,
      windowStartMillis = tStart,
      windowEndMillis = tEnd
    )
  }
}

/**
 * x нормирован [0,1] по индексу; пустые бакеты пропускаются, ненулевые точки идут **одной**
 * линией через весь график (соседние по времени с данными соединяются даже через дни/часы без
 * анализов).
 */
fun splitSeriesIntoSegments(
  points: List<MoodDayPoint>,
  scaleKey: String
): List<List<Pair<Float, Float>>> {
  val w = (points.size - 1).coerceAtLeast(1).toFloat()
  val cur = mutableListOf<Pair<Float, Float>>()
  for (i in points.indices) {
    val p = points[i]
    val yv = p.values[scaleKey] ?: continue
    val xn = p.xInWindow
      ?: (if (points.size == 1) 0.5f else i / w)
    val yf = (yv.coerceIn(0, 100)) / 100f
    cur.add(Pair(xn, yf))
  }
  val segs = mutableListOf<MutableList<Pair<Float, Float>>>()
  if (cur.isNotEmpty()) {
    if (cur.size == 1) {
      val p = cur[0]
      segs.add(mutableListOf(p, p))
    } else {
      segs.add(cur)
    }
  }
  return segs
}

/** Прямая ломаная по точкам в нормированных [0,1] x и [0,1] y. */
fun buildLinePath(
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
    path.lineTo(x, y)
    return path
  }
  val p = points.map { ComposeOffset(tx(it.first), ty(it.second)) }
  path.moveTo(p[0].x, p[0].y)
  for (i in 1 until p.size) {
    path.lineTo(p[i].x, p[i].y)
  }
  return path
}
