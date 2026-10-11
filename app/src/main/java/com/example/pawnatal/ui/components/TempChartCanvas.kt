package com.example.pawnatal.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pawnatal.data.model.TemperatureRecord
import com.example.pawnatal.theme.PawAlertRed
import com.example.pawnatal.theme.PawPrimary
import com.example.pawnatal.theme.PawSuccess

@Composable
fun TempChartCanvas(
    records: List<TemperatureRecord>,
    tempUnit: String = "FAHRENHEIT",
    modifier: Modifier = Modifier
) {
    if (records.isEmpty()) return

    val isCelsius = tempUnit == com.example.pawnatal.data.model.TempUnit.CELSIUS.name
    val thresholdLabel = if (isCelsius) "Labor Drop (37.2°C)" else "Labor Drop (99.0°F)"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Last ${records.takeLast(6).size} Readings Trend",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(PawAlertRed, RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = thresholdLabel,
                    fontSize = 11.sp,
                    color = PawAlertRed,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val displayRecords = records.takeLast(6)
        val minTemp = 98.0
        val maxTemp = 101.5

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            val width = size.width
            val height = size.height
            val paddingStart = 30f
            val paddingEnd = 30f
            val chartWidth = width - paddingStart - paddingEnd

            // Draw Threshold Line at 99.0°F
            val thresholdY = height - ((99.0 - minTemp) / (maxTemp - minTemp) * height).toFloat()
            drawLine(
                color = PawAlertRed,
                start = Offset(0f, thresholdY),
                end = Offset(width, thresholdY),
                strokeWidth = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
            )

            // Draw Safe baseline band (100.0°F to 101.2°F)
            val safeBottomY = height - ((100.0 - minTemp) / (maxTemp - minTemp) * height).toFloat()
            val safeTopY = height - ((101.2 - minTemp) / (maxTemp - minTemp) * height).toFloat()
            drawRect(
                color = PawSuccess.copy(alpha = 0.08f),
                topLeft = Offset(0f, safeTopY),
                size = androidx.compose.ui.geometry.Size(width, safeBottomY - safeTopY)
            )

            // Calculate points
            val points = displayRecords.mapIndexed { index, record ->
                val x = paddingStart + if (displayRecords.size > 1) {
                    (index.toFloat() / (displayRecords.size - 1)) * chartWidth
                } else {
                    chartWidth / 2
                }
                val clamped = record.tempFahrenheit.coerceIn(minTemp, maxTemp)
                val y = height - ((clamped - minTemp) / (maxTemp - minTemp) * height).toFloat()
                Offset(x, y)
            }

            // Draw line
            if (points.size > 1) {
                val path = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        val prev = points[i - 1]
                        val current = points[i]
                        val midX = (prev.x + current.x) / 2f
                        cubicTo(midX, prev.y, midX, current.y, current.x, current.y)
                    }
                }
                drawPath(
                    path = path,
                    color = PawPrimary,
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            // Draw data points
            points.forEachIndexed { idx, point ->
                val record = displayRecords[idx]
                val dotColor = if (record.isHypothermiaDrop) PawAlertRed else PawPrimary
                drawCircle(
                    color = Color.White,
                    radius = 6.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = dotColor,
                    radius = 4.dp.toPx(),
                    center = point
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Labels below chart
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            displayRecords.forEach { record ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = record.displayLabel.replace(" (Latest)", ""),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val formatted = com.example.pawnatal.data.util.UnitFormatter.formatTemperature(record.tempFahrenheit, tempUnit)
                    Text(
                        text = formatted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (record.isHypothermiaDrop) PawAlertRed else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
