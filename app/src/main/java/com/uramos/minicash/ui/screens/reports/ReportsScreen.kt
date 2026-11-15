package com.uramos.minicash.ui.screens.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uramos.minicash.ui.components.CardTonalLevel
import com.uramos.minicash.ui.components.MinicashCard
import com.uramos.minicash.ui.components.MinicashEmptyState
import com.uramos.minicash.ui.components.MinicashSegmentedChoice
import com.uramos.minicash.ui.components.MinicashSubScreenTopBar
import com.uramos.minicash.ui.components.MoneyDisplayType
import com.uramos.minicash.ui.components.MoneyTextView
import com.uramos.minicash.ui.components.fadingTopEdge
import com.uramos.minicash.ui.theme.MinicashThemeExtensions
import com.uramos.minicash.ui.utils.MinicashColorUtils
import com.uramos.minicash.ui.utils.toCompactCurrency
import com.uramos.minicash.ui.utils.toCurrencyFormat
import java.time.YearMonth
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private val esMx: Locale = Locale.forLanguageTag("es-MX")

private fun YearMonth.shortLabel(): String =
    month.getDisplayName(JavaTextStyle.SHORT, esMx).trimEnd('.')

private fun YearMonth.fullLabel(): String =
    month.getDisplayName(JavaTextStyle.FULL, esMx).replaceFirstChar { it.uppercase() } + " $year"

/**
 * Dashboard de reportes: balance del periodo, cifras clave, ingresos contra gastos por mes, en qué se gasta
 * y balance acumulado, con el desglose mensual al final como vista de tabla.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    onBackClick: () -> Unit
) {
    val spacing = MinicashThemeExtensions.spacing
    val state by viewModel.uiState.collectAsState()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val fadeListState = rememberLazyListState()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        topBar = {
            MinicashSubScreenTopBar(
                title = "Reportes",
                onBack = onBackClick,
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = fadeListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .fadingTopEdge(fadeListState),
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
                item { HeroBalanceCard(state) }

                item { StatTiles(state) }

                item {
                    ChartCard(title = "Ingresos vs gastos por mes") {
                        ChartLegend()
                        IncomeExpenseChart(months = state.months)
                    }
                }

                if (state.categorySpend.isNotEmpty()) {
                    item {
                        ChartCard(title = "En qué gastas") {
                            CategoryBars(state.categorySpend)
                        }
                    }
                }

                item {
                    ChartCard(title = "Balance acumulado") {
                        CumulativeLine(months = state.months, values = state.cumulative)
                    }
                }

                item {
                    Text(
                        text = "Mes por mes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp, top = spacing.small)
                    )
                }
                items(state.months.reversed(), key = { it.month.toString() }) { month ->
                    MonthRow(month)
                }
            }
        }
    }
}

/** Único momento "hero" del dashboard: el balance del periodo sobre un contenedor de acento con una forma decorativa. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HeroBalanceCard(state: ReportsUiState) {
    val container = MaterialTheme.colorScheme.primaryContainer
    val onContainer = MaterialTheme.colorScheme.onPrimaryContainer
    val rate = state.savingsRate
    val rateText = when {
        rate == null -> "Sin ingresos en el periodo"
        rate >= 0f -> "Ahorraste el ${(rate * 100).roundToInt()} % de tus ingresos"
        else -> "Gastaste ${(abs(rate) * 100).roundToInt()} % más de lo que ingresó"
    }

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = container,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box {
            // Forma decorativa (no lleva información): sirve de fondo, nunca de contenedor de texto
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 48.dp, y = (-40).dp)
                    .size(180.dp)
                    .background(onContainer.copy(alpha = 0.08f), MaterialShapes.SoftBurst.toShape())
            )
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Balance de los últimos ${state.monthsRange} meses",
                    style = MaterialTheme.typography.labelLarge,
                    color = onContainer
                )
                Text(
                    text = state.periodBalance.toCurrencyFormat(),
                    style = MaterialTheme.typography.displaySmall.copy(fontFeatureSettings = "tnum"),
                    fontWeight = FontWeight.ExtraBold,
                    color = onContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Surface(shape = CircleShape, color = onContainer.copy(alpha = 0.12f)) {
                    Text(
                        text = rateText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = onContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

/** Cuatro cifras en cuadrícula; las esquinas alternan 28 y 8 dp en tablero para dar tensión entre formas. */
@Composable
private fun StatTiles(state: ReportsUiState) {
    val colors = MinicashThemeExtensions.financialColors
    val best = state.bestMonth
    val big = 28.dp
    val small = 8.dp
    val shapeA = RoundedCornerShape(topStart = big, topEnd = small, bottomEnd = big, bottomStart = small)
    val shapeB = RoundedCornerShape(topStart = small, topEnd = big, bottomEnd = small, bottomStart = big)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            StatTile("Ingreso promedio", state.averageIncome.toCompactCurrency(), colors.income, shapeA, Modifier.weight(1f))
            StatTile("Gasto promedio", state.averageExpense.toCompactCurrency(), colors.expense, shapeB, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            StatTile(
                label = "Ahorro mensual",
                value = state.averageBalance.toCompactCurrency().let { if (state.averageBalance < 0) "-$it" else it },
                dot = MaterialTheme.colorScheme.primary,
                shape = shapeB,
                modifier = Modifier.weight(1f)
            )
            StatTile(
                label = "Mejor mes",
                value = best?.month?.month?.getDisplayName(JavaTextStyle.FULL, esMx)?.replaceFirstChar { it.uppercase() } ?: "—",
                dot = MaterialTheme.colorScheme.tertiary,
                shape = shapeA,
                modifier = Modifier.weight(1f),
                caption = best?.balance?.toCompactCurrency()
            )
        }
    }
}

/** Cifra con un punto de color que identifica la serie; el valor va en la tinta de texto, no en el color de la serie. */
@Composable
private fun StatTile(
    label: String,
    value: String,
    dot: Color,
    shape: androidx.compose.ui.graphics.Shape,
    modifier: Modifier = Modifier,
    caption: String? = null
) {
    MinicashCard(level = CardTonalLevel.LOW, shape = shape, modifier = modifier) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.size(8.dp).background(dot, CircleShape))
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (caption != null) {
                Text(caption, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ChartCard(title: String, content: @Composable () -> Unit) {
    MinicashCard(
        level = CardTonalLevel.LOW,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(MinicashThemeExtensions.spacing.standard),
            verticalArrangement = Arrangement.spacedBy(MinicashThemeExtensions.spacing.small)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            content()
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
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = inkMuted, textAlign = TextAlign.Center)

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

/**
 * Gasto por categoría en barras horizontales ordenadas: el nombre y la cifra van en tinta de texto y el color
 * de cada categoría solo identifica la barra, así que nada depende del color.
 */
@Composable
private fun CategoryBars(spend: List<CategorySpend>) {
    val maxTotal = spend.maxOf { it.total }.coerceAtLeast(1L)
    val fallback = MaterialTheme.colorScheme.outline
    val primary = MaterialTheme.colorScheme.primary
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        spend.forEach { item ->
            val barColor = item.colorHex?.let { MinicashColorUtils.parseHexColor(it, primary) } ?: fallback
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${item.total.toCompactCurrency()} · ${(item.share * 100).roundToInt()} %",
                        style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = (item.total.toFloat() / maxTotal.toFloat()).coerceIn(0.02f, 1f))
                            .clip(RoundedCornerShape(5.dp))
                            .background(barColor)
                    )
                }
            }
        }
    }
}

/** Línea del balance acumulado: 2 dp, extremos redondos, puntos de 8 dp con un anillo del color de la superficie. */
@Composable
private fun CumulativeLine(months: List<MonthlyTotals>, values: List<Long>) {
    if (values.size < 2) return
    val line = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surfaceContainerLow
    val ink = MaterialTheme.colorScheme.onSurface
    val inkMuted = MaterialTheme.colorScheme.onSurfaceVariant
    val grid = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val measurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = inkMuted, textAlign = TextAlign.Center)
    val valueStyle = MaterialTheme.typography.labelMedium.copy(color = ink, fontWeight = FontWeight.Bold)

    // El eje siempre incluye el cero para que se vea cuándo el acumulado pasa a negativo
    val minValue = minOf(0L, values.min())
    val maxValue = maxOf(0L, values.max()).coerceAtLeast(minValue + 100L)
    val span = (maxValue - minValue).toFloat()

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val leftPad = 44.dp.toPx()
        val rightPad = 12.dp.toPx()
        val topPad = 22.dp.toPx()
        val bottomPad = 22.dp.toPx()
        val chartW = size.width - leftPad - rightPad
        val chartH = size.height - topPad - bottomPad
        fun x(i: Int) = leftPad + chartW * i / (values.size - 1)
        fun y(v: Long) = topPad + chartH * (1f - (v - minValue) / span)

        listOf(minValue, 0L, maxValue).distinct().forEach { tick ->
            val ty = y(tick)
            drawLine(grid, Offset(leftPad, ty), Offset(size.width - rightPad, ty), strokeWidth = 1.dp.toPx())
            val text = measurer.measure(tick.toCompactCurrency().let { if (tick < 0) "-$it" else it }, labelStyle)
            drawText(text, topLeft = Offset(leftPad - text.size.width - 6.dp.toPx(), ty - text.size.height / 2f))
        }

        val path = Path().apply {
            values.forEachIndexed { i, v -> if (i == 0) moveTo(x(i), y(v)) else lineTo(x(i), y(v)) }
        }
        drawPath(path, line, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

        values.forEachIndexed { i, v ->
            drawCircle(surface, radius = 6.dp.toPx(), center = Offset(x(i), y(v)))
            drawCircle(line, radius = 4.dp.toPx(), center = Offset(x(i), y(v)))
            if (months.size <= 6 || i % 2 == 0 || i == values.lastIndex) {
                val label = measurer.measure(months[i].month.shortLabel(), labelStyle)
                drawText(label, topLeft = Offset(x(i) - label.size.width / 2f, size.height - bottomPad + 6.dp.toPx()))
            }
        }

        // Etiqueta directa solo en el último punto
        val lastText = measurer.measure(values.last().toCompactCurrency().let { if (values.last() < 0) "-$it" else it }, valueStyle)
        drawText(
            lastText,
            topLeft = Offset(
                (x(values.lastIndex) - lastText.size.width / 2f).coerceIn(leftPad, size.width - lastText.size.width.toFloat()),
                y(values.last()) - lastText.size.height - 8.dp.toPx()
            )
        )
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
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
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
