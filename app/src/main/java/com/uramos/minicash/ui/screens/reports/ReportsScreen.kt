package com.uramos.minicash.ui.screens.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uramos.minicash.ui.components.CardTonalLevel
import com.uramos.minicash.ui.components.MinicashCard
import com.uramos.minicash.ui.components.MinicashEmptyState
import com.uramos.minicash.ui.components.MinicashIconButton
import com.uramos.minicash.ui.components.MinicashSegmentedChoice
import com.uramos.minicash.ui.components.MoneyDisplayType
import com.uramos.minicash.ui.components.MoneyTextView
import com.uramos.minicash.ui.theme.MinicashThemeExtensions
import com.uramos.minicash.ui.utils.toCompactCurrency
import java.time.YearMonth
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

private val esMx: Locale = Locale.forLanguageTag("es-MX")

private fun YearMonth.shortLabel(): String =
    month.getDisplayName(JavaTextStyle.SHORT, esMx).trimEnd('.')

private fun YearMonth.fullLabel(): String =
    month.getDisplayName(JavaTextStyle.FULL, esMx).replaceFirstChar { it.uppercase() } + " $year"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    onBackClick: () -> Unit
) {
    val spacing = MinicashThemeExtensions.spacing
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                ),
                navigationIcon = {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .padding(start = spacing.small)
                            .size(40.dp)
                    ) {
                        MinicashIconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                title = {
                    Text("Reportes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                top = spacing.small,
                start = spacing.standard,
                end = spacing.standard,
                bottom = 120.dp
            ),
            verticalArrangement = Arrangement.spacedBy(spacing.medium)
        ) {
            item {
                MinicashSegmentedChoice(
                    options = listOf(6, 12),
                    isSelected = { it == state.monthsRange },
                    onSelect = viewModel::selectRange,
                    label = { "$it meses" }
                )
            }

            if (!state.hasData) {
                item {
                    MinicashEmptyState(
                        icon = Icons.Default.BarChart,
                        title = "Sin movimientos",
                        subtitle = "Aún no hay ingresos ni gastos en estos meses.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = spacing.large)
                    )
                }
            } else {
                item {
                    MinicashCard(
                        level = CardTonalLevel.LOW,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(spacing.standard),
                            verticalArrangement = Arrangement.spacedBy(spacing.small)
                        ) {
                            Text(
                                text = "Ingresos vs gastos por mes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            ChartLegend()
                            IncomeExpenseChart(months = state.months)
                        }
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AverageTile("Ingreso prom.", state.averageIncome, MoneyDisplayType.INCOME, Modifier.weight(1f))
                        AverageTile("Gasto prom.", state.averageExpense, MoneyDisplayType.EXPENSE, Modifier.weight(1f))
                        AverageTile(
                            "Balance prom.",
                            state.averageBalance,
                            if (state.averageBalance >= 0) MoneyDisplayType.INCOME else MoneyDisplayType.EXPENSE,
                            Modifier.weight(1f)
                        )
                    }
                }

                items(state.months.reversed(), key = { it.month.toString() }) { month ->
                    MonthRow(month)
                }
            }
        }
    }
}

@Composable
private fun ChartLegend() {
    val colors = MinicashThemeExtensions.financialColors
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        LegendItem("Ingresos", colors.income)
        LegendItem("Gastos", colors.expense)
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(10.dp).background(color, CircleShape))
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Barras agrupadas (ingreso y gasto) por mes, con cuadrícula tenue y etiquetas en la tinta de texto. */
@Composable
private fun IncomeExpenseChart(months: List<MonthlyTotals>) {
    val colors = MinicashThemeExtensions.financialColors
    val inkMuted = MaterialTheme.colorScheme.onSurfaceVariant
    val grid = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val measurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 10.sp, color = inkMuted, textAlign = TextAlign.Center)

    val maxValue = (months.maxOfOrNull { maxOf(it.income, it.expense) } ?: 0L).coerceAtLeast(100L)
    val ticks = listOf(0f, 0.5f, 1f)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        val leftPad = 40.dp.toPx()
        val bottomPad = 22.dp.toPx()
        val chartW = size.width - leftPad
        val chartH = size.height - bottomPad
        val slot = chartW / months.size
        val barW = (slot * 0.3f).coerceAtMost(14.dp.toPx())
        val gap = 2.dp.toPx()
        val radius = CornerRadius(4.dp.toPx())

        ticks.forEach { t ->
            val y = chartH - chartH * t
            drawLine(grid, Offset(leftPad, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
            val text = measurer.measure((maxValue * t).toLong().toCompactCurrency(), labelStyle)
            drawText(text, topLeft = Offset(leftPad - text.size.width - 6.dp.toPx(), y - text.size.height / 2f))
        }

        months.forEachIndexed { i, m ->
            val center = leftPad + slot * i + slot / 2f
            fun bar(value: Long, left: Float, color: Color) {
                val h = chartH * value / maxValue
                if (h <= 0f) return
                drawRoundRect(
                    color = color,
                    topLeft = Offset(left, chartH - h),
                    size = Size(barW, h),
                    cornerRadius = radius
                )
                // Base recta: solo el extremo de datos va redondeado
                drawRect(color, Offset(left, chartH - radius.x.coerceAtMost(h)), Size(barW, radius.x.coerceAtMost(h)))
            }
            bar(m.income, center - barW - gap / 2f, colors.income)
            bar(m.expense, center + gap / 2f, colors.expense)

            val label = measurer.measure(m.month.shortLabel(), labelStyle)
            if (months.size <= 6 || i % 2 == 0 || label.size.width < slot) {
                drawText(label, topLeft = Offset(center - label.size.width / 2f, chartH + 6.dp.toPx()))
            }
        }
    }
}

@Composable
private fun AverageTile(label: String, amount: Long, type: MoneyDisplayType, modifier: Modifier = Modifier) {
    MinicashCard(
        level = CardTonalLevel.LOW,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = amount.toCompactCurrency().let { if (amount < 0) "-$it" else it },
                style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                fontWeight = FontWeight.ExtraBold,
                color = when (type) {
                    MoneyDisplayType.INCOME -> MinicashThemeExtensions.financialColors.income
                    MoneyDisplayType.EXPENSE -> MinicashThemeExtensions.financialColors.expense
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@Composable
private fun MonthRow(month: MonthlyTotals) {
    MinicashCard(
        level = CardTonalLevel.LOW,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(month.month.fullLabel(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                MoneyTextView(
                    amountCents = month.balance,
                    type = if (month.balance >= 0) MoneyDisplayType.INCOME else MoneyDisplayType.EXPENSE,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "Ingresos ${month.income.toCompactCurrency()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Gastos ${month.expense.toCompactCurrency()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
