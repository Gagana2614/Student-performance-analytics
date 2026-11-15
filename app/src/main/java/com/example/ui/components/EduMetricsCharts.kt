package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.theme.Blue500
import com.example.ui.theme.Teal500
import com.example.ui.theme.Violet500
import kotlin.math.max

data class BarDatum(val label: String, val value: Float, val color: Color, val valueLabel: String)

data class GroupedBarDatum(
    val label: String,
    val primaryValue: Float,
    val secondaryValue: Float
)

data class ScatterDatum(
    val label: String,
    val xPercent: Float,
    val yPercent: Float,
    val color: Color
)

@Composable
fun ChartLegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DualTrendAreaChart(
    labels: List<String>,
    seriesA: List<Float>,
    seriesB: List<Float>,
    seriesALabel: String,
    seriesBLabel: String,
    seriesAColor: Color = Violet500,
    seriesBColor: Color = Teal500,
    modifier: Modifier = Modifier
) {
    if (labels.isEmpty()) {
        EmptyStateView(
            title = "No trend records yet",
            description = "Record semester marks and attendance to generate progression curves."
        )
        return
    }
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChartLegendItem(seriesALabel, seriesAColor)
            ChartLegendItem(seriesBLabel, seriesBColor)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            val w = size.width
            val h = size.height

            // Horizontal percentage grid lines (25, 50, 75, 100)
            listOf(0.25f, 0.5f, 0.75f, 1f).forEach { frac ->
                val y = h - frac * h
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                )
            }

            fun buildPoints(values: List<Float>): List<Offset> {
                if (values.size == 1) {
                    val y = h - (values[0].coerceIn(0f, 100f) / 100f) * h
                    return listOf(Offset(w * 0.2f, y), Offset(w * 0.8f, y))
                }
                val step = w / max(1, values.size - 1)
                return values.mapIndexed { idx, v ->
                    Offset(idx * step, h - (v.coerceIn(0f, 100f) / 100f) * h)
                }
            }

            fun drawSeries(values: List<Float>, color: Color) {
                val pts = buildPoints(values)
                if (pts.isEmpty()) return

                val linePath = Path().apply {
                    moveTo(pts.first().x, pts.first().y)
                    for (i in 1 until pts.size) {
                        val prev = pts[i - 1]
                        val curr = pts[i]
                        val midX = (prev.x + curr.x) / 2f
                        cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                    }
                }
                val fillPath = Path().apply {
                    addPath(linePath)
                    lineTo(pts.last().x, h)
                    lineTo(pts.first().x, h)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(color.copy(alpha = 0.28f), color.copy(alpha = 0.0f))
                    )
                )
                drawPath(
                    path = linePath,
                    color = color,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
                pts.forEach { pt ->
                    drawCircle(color = Color.White, radius = 5.dp.toPx(), center = pt)
                    drawCircle(color = color, radius = 3.5.dp.toPx(), center = pt)
                }
            }

            drawSeries(seriesB, seriesBColor)
            drawSeries(seriesA, seriesAColor)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEachIndexed { idx, label ->
                val valA = seriesA.getOrNull(idx)?.toInt() ?: 0
                val valB = seriesB.getOrNull(idx)?.toInt() ?: 0
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${valA}% / ${valB}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun VerticalBarChart(
    data: List<BarDatum>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        EmptyStateView(title = "No distribution data", description = "Add records to populate this chart.")
        return
    }
    val maxVal = max(1f, data.maxOf { it.value })
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { item ->
                val ratio = (item.value / maxVal).coerceIn(0.04f, 1f)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = item.valueLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .height(115.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(trackColor),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((115f * ratio).dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(item.color, item.color.copy(alpha = 0.72f))
                                    )
                                )
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun GroupedBenchmarkBarChart(
    data: List<GroupedBarDatum>,
    primaryLabel: String,
    secondaryLabel: String,
    primaryColor: Color = Blue500,
    secondaryColor: Color = Teal500,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        EmptyStateView(title = "No benchmark data", description = "Add subjects and evaluations to compare cohorts.")
        return
    }
    Column(modifier = modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ChartLegendItem(primaryLabel, primaryColor)
            ChartLegendItem(secondaryLabel, secondaryColor)
        }
        Spacer(modifier = Modifier.height(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            data.forEach { item ->
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${item.primaryValue.toInt()}% · ${item.secondaryValue.toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalTrackBar(ratio = item.primaryValue / 100f, color = primaryColor)
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalTrackBar(ratio = item.secondaryValue / 100f, color = secondaryColor)
                }
            }
        }
    }
}

@Composable
private fun HorizontalTrackBar(ratio: Float, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(ratio.coerceIn(0.02f, 1f))
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(color)
        )
    }
}

@Composable
fun AttendanceMarksScatterChart(
    points: List<ScatterDatum>,
    attendanceThreshold: Int,
    passThreshold: Int,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) {
        EmptyStateView(title = "Insufficient scatter data", description = "Evaluate students to plot correlation.")
        return
    }
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val thresholdColor = MaterialTheme.colorScheme.error.copy(alpha = 0.45f)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .padding(8.dp)
        ) {
            val w = size.width
            val h = size.height

            // Subtle frame and quartiles
            drawRoundRect(
                color = gridColor,
                topLeft = Offset.Zero,
                size = Size(w, h),
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                style = Stroke(width = 1.dp.toPx())
            )
            listOf(0.25f, 0.5f, 0.75f).forEach { f ->
                drawLine(gridColor, Offset(0f, h * f), Offset(w, h * f), strokeWidth = 1.dp.toPx())
                drawLine(gridColor, Offset(w * f, 0f), Offset(w * f, h), strokeWidth = 1.dp.toPx())
            }

            // Threshold lines (Attendance X, Pass Y)
            val minAxis = 35f
            val span = 65f
            val attX = ((attendanceThreshold - minAxis) / span).coerceIn(0f, 1f) * w
            val passY = h - ((passThreshold - minAxis) / span).coerceIn(0f, 1f) * h
            drawLine(
                color = thresholdColor,
                start = Offset(attX, 0f),
                end = Offset(attX, h),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f))
            )
            drawLine(
                color = thresholdColor,
                start = Offset(0f, passY),
                end = Offset(w, passY),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f))
            )

            points.forEach { pt ->
                val cx = ((pt.xPercent - minAxis) / span).coerceIn(0.03f, 0.97f) * w
                val cy = h - ((pt.yPercent - minAxis) / span).coerceIn(0.03f, 0.97f) * h
                drawCircle(color = pt.color.copy(alpha = 0.25f), radius = 10.dp.toPx(), center = Offset(cx, cy))
                drawCircle(color = pt.color, radius = 5.5.dp.toPx(), center = Offset(cx, cy))
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "X-Axis: Attendance (35%–100%)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Y-Axis: Mean Score (35%–100%)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
