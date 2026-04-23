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
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanoprojects.voxera.ui.statistics.MoodChartSeries
import com.vanoprojects.voxera.ui.statistics.buildSmoothPath
import com.vanoprojects.voxera.ui.statistics.splitSeriesIntoSegments
import com.vanoprojects.voxera.ui.theme.ThemeType

@Composable
fun MoodLineChart(
  series: MoodChartSeries,
  scaleKeys: List<String>,
  colors: List<Color>,
  themeType: ThemeType,
  selectedOnlyKey: String?,
  modifier: Modifier = Modifier
) {
  val axis = if (themeType == ThemeType.LIGHT) Color(0xFF1A1A1A) else Color.White.copy(alpha = 0.88f)
  val grid = if (themeType == ThemeType.LIGHT) Color(0x1F000000) else Color.White.copy(alpha = 0.12f)
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

  Canvas(modifier = modifier.fillMaxWidth().height(220.dp)) {
    val w = size.width
    val h = size.height
    if (w < 1f || h < 1f) return@Canvas
    val pl = plDp.toPx()
    val pr = prDp.toPx()
    val pt = ptDp.toPx()
    val pb = pbDp.toPx()
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
    val keysToDraw = if (selectedOnlyKey != null) listOf(selectedOnlyKey) else scaleKeys
    val n = colors.size
    keysToDraw.forEach { key ->
      val raw = scaleKeys.indexOf(key)
      val colorIdx = (if (raw < 0) 0 else raw) % n
      val c = colors[colorIdx]
      val segs = splitSeriesIntoSegments(series.dayPoints, key)
      segs.forEach { seg ->
        if (seg.size < 2) return@forEach
        val path = buildSmoothPath(seg, w, h, pl, pr, pt, pb)
        drawPath(
          path,
          color = c,
          style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )
      }
    }
    val pts = series.dayPoints
    if (pts.isNotEmpty()) {
      val count = pts.size
      val indices = when {
        count == 1 -> listOf(0)
        count <= 3 -> (0 until count).toList()
        else -> listOf(0, count / 2, count - 1)
      }
      indices.distinct().forEach { i ->
        val x = if (count == 1) (pl + w - pr) / 2f
        else pl + (i / (count - 1).coerceAtLeast(1).toFloat()) * (w - pl - pr)
        drawIntoCanvas { c ->
          c.nativeCanvas.drawText(pts[i].label, x, h - 6f.dp.toPx(), xLabelPaint)
        }
      }
    }
  }
}
