package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

// Calming tones
val ColorExpense = Color(0xFFC97C78)    // Soft Calming Red
val ColorIncome = Color(0xFF7CA685)     // Soft Calming Sage Green
val ColorSaving = Color(0xFF7FA2BE)     // Soft Calming Slate Blue
val ColorInvestment = Color(0xFFA58CB6) // Soft Calming Lavender

@Composable
fun FinanceDonutChart(
    income: Double,
    expense: Double,
    saving: Double,
    investment: Double,
    modifier: Modifier = Modifier
) {
    val total = income + expense + saving + investment
    
    // Percentages
    val pctIncome = if (total > 0) (income / total).toFloat() else 0f
    val pctExpense = if (total > 0) (expense / total).toFloat() else 0f
    val pctSaving = if (total > 0) (saving / total).toFloat() else 0f
    val pctInvestment = if (total > 0) (investment / total).toFloat() else 0f

    // Animations
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(income, expense, saving, investment) {
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 1000))
    }

    var selectedSection by remember { mutableStateOf("TAP CHART") }
    var selectedValue by remember { mutableStateOf(total) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Circle Chart
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .pointerInput(income, expense, saving, investment) {
                        detectTapGestures { offset ->
                            val center = size.width / 2f
                            val dx = offset.x - center
                            val dy = offset.y - center
                            val angleRad = kotlin.math.atan2(dy, dx)
                            var angleDeg = Math.toDegrees(angleRad.toDouble()) + 90.0
                            if (angleDeg < 0) angleDeg += 360.0

                            val angle1 = pctIncome * 360f
                            val angle2 = angle1 + pctExpense * 360f
                            val angle3 = angle2 + pctSaving * 360f

                            when {
                                angleDeg < angle1 -> {
                                    selectedSection = "INCOME"
                                    selectedValue = income
                                }
                                angleDeg < angle2 -> {
                                    selectedSection = "EXPENSES"
                                    selectedValue = expense
                                }
                                angleDeg < angle3 -> {
                                    selectedSection = "SAVINGS"
                                    selectedValue = saving
                                }
                                else -> {
                                    selectedSection = "INVESTMENTS"
                                    selectedValue = investment
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 32.dp.toPx()
                    val chartSize = size.minDimension - strokeWidth
                    val rect = Size(chartSize, chartSize)
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                    var startAngle = -90f

                    // Income arc
                    if (pctIncome > 0) {
                        drawArc(
                            color = ColorIncome,
                            startAngle = startAngle,
                            sweepAngle = pctIncome * 360f * animationProgress.value,
                            useCenter = false,
                            topLeft = topLeft,
                            size = rect,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        startAngle += pctIncome * 360f
                    }

                    // Expense arc
                    if (pctExpense > 0) {
                        drawArc(
                            color = ColorExpense,
                            startAngle = startAngle,
                            sweepAngle = pctExpense * 360f * animationProgress.value,
                            useCenter = false,
                            topLeft = topLeft,
                            size = rect,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        startAngle += pctExpense * 360f
                    }

                    // Saving arc
                    if (pctSaving > 0) {
                        drawArc(
                            color = ColorSaving,
                            startAngle = startAngle,
                            sweepAngle = pctSaving * 360f * animationProgress.value,
                            useCenter = false,
                            topLeft = topLeft,
                            size = rect,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        startAngle += pctSaving * 360f
                    }

                    // Investment arc
                    if (pctInvestment > 0) {
                        drawArc(
                            color = ColorInvestment,
                            startAngle = startAngle,
                            sweepAngle = pctInvestment * 360f * animationProgress.value,
                            useCenter = false,
                            topLeft = topLeft,
                            size = rect,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                // Center readout text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = selectedSection,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "$${selectedValue.roundToInt()}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Legend
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(start = 12.dp)
            ) {
                LegendItem(color = ColorIncome, label = "Income", value = income)
                LegendItem(color = ColorExpense, label = "Expenses", value = expense)
                LegendItem(color = ColorSaving, label = "Savings", value = saving)
                LegendItem(color = ColorInvestment, label = "Investments", value = investment)
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String, value: Double) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$${value.roundToInt()}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun InteractiveTrendLineChart(
    dataPoints: List<Double>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    if (dataPoints.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Tracking recent spending trends...",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
        return
    }

    val maxVal = (dataPoints.maxOrNull() ?: 1.0).toFloat().coerceAtLeast(1f)
    val minVal = (dataPoints.minOrNull() ?: 0.0).toFloat()
    val valueRange = (maxVal - minVal).coerceAtLeast(1f)

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(dataPoints) {
        animProgress.animateTo(1f, animationSpec = tween(durationMillis = 1000))
    }

    var selectedIndex by remember { mutableStateOf(-1) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cash Flow Trend",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (selectedIndex in dataPoints.indices) {
                Text(
                    text = "${labels[selectedIndex]}: $${dataPoints[selectedIndex].roundToInt()}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    text = "Tap graph to query points",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .pointerInput(dataPoints) {
                    detectTapGestures { offset ->
                        val itemWidth = size.width / (dataPoints.size - 1).coerceAtLeast(1)
                        val rawIndex = (offset.x / itemWidth).roundToInt()
                        selectedIndex = rawIndex.coerceIn(0, dataPoints.lastIndex)
                    }
                }
        ) {
            val primaryColor = MaterialTheme.colorScheme.primary
            val topColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
            val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)

            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                // Draw horizontal grid lines
                val gridLines = 4
                for (i in 0..gridLines) {
                    val y = height * (i / gridLines.toFloat())
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                val points = dataPoints.mapIndexed { idx, valItem ->
                    val x = width * (idx / (dataPoints.size - 1).toFloat())
                    val normalizedY = (valItem.toFloat() - minVal) / valueRange
                    val y = height - (normalizedY * height * animProgress.value)
                    Offset(x, y)
                }

                // Draw filled curve under path
                if (points.size >= 2) {
                    val fillPath = Path().apply {
                        moveTo(points.first().x, height)
                        points.forEach { lineTo(it.x, it.y) }
                        lineTo(points.last().x, height)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(topColor, Color.Transparent),
                            startY = 0f,
                            endY = height
                        )
                    )

                    // Draw line curve
                    val strokePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            val pPrev = points[i - 1]
                            val pCur = points[i]
                            // Simple cubic connection for smooth curve
                            cubicTo(
                                x1 = (pPrev.x + pCur.x) / 2f, y1 = pPrev.y,
                                x2 = (pPrev.x + pCur.x) / 2f, y2 = pCur.y,
                                x3 = pCur.x, y3 = pCur.y
                            )
                        }
                    }
                    drawPath(
                        path = strokePath,
                        color = primaryColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Highlight tapped item
                if (selectedIndex in points.indices) {
                    val activePoint = points[selectedIndex]
                    drawCircle(
                        color = primaryColor,
                        radius = 8.dp.toPx(),
                        center = activePoint
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = activePoint
                    )
                    // Draw indicator line
                    drawLine(
                        color = primaryColor.copy(alpha = 0.5f),
                        start = Offset(activePoint.x, 0f),
                        end = Offset(activePoint.x, height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // X-Axis designations
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEachIndexed { idx, label ->
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = if (idx == selectedIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (idx == selectedIndex) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
