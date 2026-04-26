package com.vanoprojects.voxera.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanoprojects.voxera.ui.statistics.MoodChartSeries
import com.vanoprojects.voxera.ui.statistics.buildLinePath
import com.vanoprojects.voxera.ui.statistics.splitSeriesIntoSegments
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MoodLineChart(
  series: MoodChartSeries,
  scaleKeys: List<String>,
  colors: List<Color>,
  /** Пусто или все выбраны — все линии; иначе только выбранные ключи. */
  activeFilter: Set<String> = emptySet(),
  modifier: Modifier = Modifier
) {
  // График только на тёмно-синей карточке: оси и сетка светлые.
  val axis = Color.White.copy(alpha = 0.92f)
  val grid = Color.White.copy(alpha = 0.16f)
  val pointRing = Color.White
  val axisArgb = axis.toArgb()
  val plDp = 36.dp
  val prDp = 12.dp
  val ptDp = 8.dp
  val pbDp = 28.dp
  val density = LocalDensity.current
  val yLabelPaint = remember(density, axisArgb) {
    android.graphics.Paint().apply {
      color = axisArgb
      textSize = with(density) { 10.sp.toPx() }
      isAntiAlias = true
    }
  }
  val xLabelPaint = remember(density, axisArgb) {
    android.graphics.Paint().apply {
      color = axisArgb
      textSize = with(density) { 9.sp.toPx() }
      isAntiAlias = true
      textAlign = android.graphics.Paint.Align.CENTER
    }
  }
  val windowXLabels = remember(series.windowStartMillis, series.windowEndMillis) {
    val start = series.windowStartMillis
    val end = series.windowEndMillis
    if (start == null || end == null || end <= start) return@remember null
    val fmt = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    val z = ZoneId.systemDefault()
    (0..4).map { k ->
      val t = start + (end - start) * k / 4
      fmt.format(Instant.ofEpochMilli(t).atZone(z))
    }
  }
  val allKeysSet = remember(scaleKeys) { scaleKeys.toSet() }
  val keysToDraw = remember(scaleKeys, activeFilter) {
    when {
      activeFilter.isEmpty() -> scaleKeys
      activeFilter == allKeysSet -> scaleKeys
      else -> scaleKeys.filter { it in activeFilter }
    }
  }

  Canvas(modifier = modifier.fillMaxWidth().height(220.dp)) {
    val w = size.width
    val h = size.height
    if (w < 1f || h < 1f) return@Canvas
    val pl = plDp.toPx()
    val pr = prDp.toPx()
    val pt = ptDp.toPx()
    val pb = pbDp.toPx()
    val lineWidthPx = 4.dp.toPx()
    val pointRadiusPx = 3.dp.toPx()
    val pointRingWidthPx = 1.dp.toPx()
    for (t in 0..4) {
      val y = pt + (h - pt - pb) * (t / 4f)
      drawLine(grid, start = Offset(pl, y), end = Offset(w - pr, y), strokeWidth = 1.dp.toPx())
    }
    val yLabels = listOf(100, 75, 50, 25, 0)
    yLabels.forEachIndexed { i, v ->
      val y = pt + (h - pt - pb) * (i / 4f)
      drawIntoCanvas { c ->
        c.nativeCanvas.drawText(
          v.toString(),
          4f,
          y + 3.5f.dp.toPx(),
          yLabelPaint
        )
      }
    }
    if (colors.isEmpty()) return@Canvas
    val n = colors.size
    keysToDraw.forEach { key ->
      val raw = scaleKeys.indexOf(key)
      val colorIdx = (if (raw < 0) 0 else raw) % n
      val c = colors[colorIdx]
      val segs = splitSeriesIntoSegments(series.dayPoints, key)
      segs.forEach { seg ->
        if (seg.isEmpty()) return@forEach
        if (seg.size >= 2) {
          val path = buildLinePath(seg, w, h, pl, pr, pt, pb)
          drawPath(
            path,
            color = c,
            style = Stroke(width = lineWidthPx, cap = StrokeCap.Round, join = StrokeJoin.Round)
          )
        }
        val innerW = (w - pl - pr).coerceAtLeast(1f)
        val innerH = (h - pt - pb).coerceAtLeast(1f)
        fun tx(x: Float) = pl + x * innerW
        fun ty(y: Float) = pt + (1f - y) * innerH
        seg.forEach { (xn, yf) ->
          val cx = tx(xn)
          val cy = ty(yf)
          drawCircle(color = c, radius = pointRadiusPx, center = Offset(cx, cy), style = Fill)
          drawCircle(
            color = pointRing,
            radius = pointRadiusPx,
            center = Offset(cx, cy),
            style = Stroke(width = pointRingWidthPx)
          )
        }
      }
    }
    val pts = series.dayPoints
    if (pts.isNotEmpty() && windowXLabels != null) {
      windowXLabels.forEachIndexed { k, text ->
        val xNorm = k / 4f
        val x = pl + xNorm * (w - pl - pr)
        drawIntoCanvas { c ->
          c.nativeCanvas.drawText(text, x, h - 6f.dp.toPx(), xLabelPaint)
        }
      }
    } else if (pts.isNotEmpty()) {
      val count = pts.size
      val indices = when {
        count == 1 -> listOf(0)
        count <= 3 -> (0 until count).toList()
        count <= 12 -> listOf(0, count / 2, count - 1)
        else -> (0..4).map { idx -> (idx * (count - 1) / 4) }.distinct()
      }
      indices.distinct().forEach { i ->
        val xNorm = pts[i].xInWindow
          ?: if (count == 1) 0.5f else i / (count - 1).coerceAtLeast(1).toFloat()
        val x = pl + xNorm * (w - pl - pr)
        drawIntoCanvas { c ->
          c.nativeCanvas.drawText(pts[i].label, x, h - 6f.dp.toPx(), xLabelPaint)
        }
      }
    }
  }
}
