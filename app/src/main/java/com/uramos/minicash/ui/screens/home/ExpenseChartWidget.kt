package com.uramos.minicash.ui.screens.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.uramos.minicash.ui.utils.toCurrencyFormat
import kotlin.math.roundToInt

@Composable
fun TwoFacedStatsChartWidget(
    monthlyExpenseStats: List<CategoryExpenseStat>,
    totalMonthlyExpense: Long,
    monthlyIncomeStats: List<CategoryExpenseStat>,
    totalMonthlyIncome: Long,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { 2 })

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            if (page == 0) {
                SingleDonutPage(
                    title = "Distribución de gastos del mes",
                    emptyMessage = "Sin gastos registrados este mes",
                    totalLabel = "Total gastado",
                    stats = monthlyExpenseStats,
                    totalAmount = totalMonthlyExpense
                )
            } else {
                SingleDonutPage(
                    title = "Distribución de ingresos del mes",
                    emptyMessage = "Sin ingresos registrados este mes",
                    totalLabel = "Total ingresado",
                    stats = monthlyIncomeStats,
                    totalAmount = totalMonthlyIncome
                )
            }
        }
    }
}

@Composable
private fun SingleDonutPage(
    title: String,
    emptyMessage: String,
    totalLabel: String,
    stats: List<CategoryExpenseStat>,
    totalAmount: Long
) {
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(stats) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (stats.isEmpty() || totalAmount == 0L) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emptyMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val colorScheme = MaterialTheme.colorScheme
            val categoryColors = remember(colorScheme) {
                listOf(
                    colorScheme.primary,
                    colorScheme.tertiary,
                    colorScheme.secondary,
                    colorScheme.error,
                    colorScheme.inversePrimary,
                    colorScheme.primaryContainer,
                    colorScheme.tertiaryContainer,
                    colorScheme.secondaryContainer
                )
            }

            val displayStats = remember(stats) {
                if (stats.size <= 4) {
                    stats
                } else {
                    val top3 = stats.take(3)
                    val rest = stats.drop(3)
                    val otherPercentage = rest.fold(0f) { acc, item -> acc + item.percentage }
                    val otherAmount = rest.fold(0L) { acc, item -> acc + item.totalAmount }
                    top3 + CategoryExpenseStat(
                        categoryName = "Otras",
                        totalAmount = otherAmount,
                        percentage = otherPercentage,
                        colorHex = null
                    )
                }
            }

            val parsedColors = remember(displayStats, colorScheme) {
                displayStats.mapIndexed { index, stat ->
                    if (!stat.colorHex.isNullOrBlank()) {
                        try {
                            androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(stat.colorHex))
                        } catch (e: Exception) {
                            categoryColors[index % categoryColors.size]
                        }
                    } else {
                        categoryColors[index % categoryColors.size]
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 130.dp, max = 160.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Lado Izquierdo (45% del ancho): Donut Canvas + Total centrado
                Box(
                    modifier = Modifier
                        .weight(0.45f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    val chartSize = 110.dp
                    val strokeWidth = 12.dp
                    val strokeWidthPx = with(LocalDensity.current) { strokeWidth.toPx() }

                    Canvas(modifier = Modifier.size(chartSize)) {
                        var currentStartAngle = -90f
                        val numStats = displayStats.size
                        val gapAngle = if (numStats > 1) 3f else 0f
                        val availableAngle = 360f - (numStats * gapAngle)

                        displayStats.forEachIndexed { index, stat ->
                            val sweepAngle = stat.percentage * availableAngle * animationProgress.value
                            val color = parsedColors.getOrElse(index) { categoryColors[index % categoryColors.size] }

                            if (sweepAngle > 0f) {
                                drawArc(
                                    color = color,
                                    startAngle = currentStartAngle + (gapAngle / 2f),
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    style = Stroke(
                                        width = strokeWidthPx,
                                        cap = if (numStats > 1) StrokeCap.Round else StrokeCap.Butt
                                    )
                                )
                            }
                            currentStartAngle += (stat.percentage * availableAngle) + gapAngle
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        Text(
                            text = totalLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = totalAmount.toCurrencyFormat(),
                            style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Lado Derecho (55% del ancho): Lista vertical de hasta 4 categorías
                Column(
                    modifier = Modifier.weight(0.55f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    displayStats.forEachIndexed { index, stat ->
                        val color = parsedColors.getOrElse(index) { categoryColors[index % categoryColors.size] }
                        val percentageText = (stat.percentage * 100).roundToInt()

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(color = color, shape = CircleShape)
                            )
                            Text(
                                text = stat.categoryName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "$percentageText%",
                                style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
