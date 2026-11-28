package com.uramos.minicash.ui.screens.home

import com.uramos.minicash.ui.utils.AppStrings
import androidx.compose.ui.res.stringResource
import com.uramos.minicash.R
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.uramos.minicash.data.preferences.DistributionStyle
import com.uramos.minicash.ui.components.CardTonalLevel
import com.uramos.minicash.ui.components.MinicashCard
import com.uramos.minicash.ui.components.MinicashSegmentedChoice
import com.uramos.minicash.ui.theme.MinicashShapes
import com.uramos.minicash.ui.theme.minicashSpatialSpec
import com.uramos.minicash.ui.theme.trackSurface
import com.uramos.minicash.ui.utils.MinicashColorUtils
import com.uramos.minicash.ui.utils.toCurrencyFormat

/**
 * «Distribución del mes» en el estilo que se eligió en Ajustes → Inicio: dona (las dos caras Gastos e Ingresos),
 * barras por categoría, barra apilada o Top 5 contra el mes anterior.
 */
@Composable
fun DistributionWidget(
    style: DistributionStyle,
    expenseStats: List<CategoryExpenseStat>,
    totalExpense: Long,
    incomeStats: List<CategoryExpenseStat>,
    totalIncome: Long,
    previousExpense: PreviousMonthExpense,
    modifier: Modifier = Modifier
) {
    if (style == DistributionStyle.DONUT) {
        TwoFacedStatsChartWidget(
            monthlyExpenseStats = expenseStats,
            totalMonthlyExpense = totalExpense,
            monthlyIncomeStats = incomeStats,
            totalMonthlyIncome = totalIncome,
            modifier = modifier
        )
        return
    }

    var showIncome by rememberSaveable { mutableStateOf(false) }
    val stats = if (showIncome) incomeStats else expenseStats
    val total = if (showIncome) totalIncome else totalExpense
    MinicashCard(
        level = CardTonalLevel.LOW,
        shape = MinicashShapes.Card,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MinicashSegmentedChoice(
                options = listOf(false, true),
                isSelected = { it == showIncome },
                onSelect = { showIncome = it },
                label = { if (it) AppStrings.get(R.string.common_income) else AppStrings.get(R.string.common_expenses) }
            )
            if (stats.isEmpty()) {
                Text(
                    text = if (showIncome) stringResource(R.string.home_no_income) else stringResource(R.string.home_no_expenses),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            } else {
                when (style) {
                    DistributionStyle.BARS -> BarsBody(stats)
                    DistributionStyle.STACKED -> StackedBody(stats, total, if (showIncome) stringResource(R.string.home_total_income) else stringResource(R.string.home_total_expense))
                    DistributionStyle.TOP5 -> Top5Body(stats, total, if (showIncome) null else previousExpense, showIncome)
                    DistributionStyle.DONUT -> Unit
                }
            }
        }
    }
}

@Composable
private fun categoryColor(stat: CategoryExpenseStat, index: Int): Color {
    val scheme = MaterialTheme.colorScheme
    val palette = listOf(scheme.primary, scheme.tertiary, scheme.secondary, scheme.error, scheme.inversePrimary)
    return stat.colorHex?.let { MinicashColorUtils.parseHexColor(it, palette[index % palette.size]) } ?: palette[index % palette.size]
}

private fun percentText(fraction: Float): String = "${(fraction * 100).toInt().coerceAtLeast(if (fraction > 0f) 1 else 0)} %"

/** Una barra por categoría, con el monto y el porcentaje; la mayor llena todo el ancho. */
@Composable
private fun BarsBody(stats: List<CategoryExpenseStat>) {
    val visible = remember(stats) { groupTopStats(stats, 6, AppStrings.get(R.string.cat_others)) }
    val max = visible.maxOfOrNull { it.totalAmount }?.coerceAtLeast(1L) ?: 1L
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        visible.forEachIndexed { index, stat ->
            val color = categoryColor(stat, index)
            val fill by animateFloatAsState(
                targetValue = stat.totalAmount.toFloat() / max.toFloat(),
                animationSpec = minicashSpatialSpec(),
                label = "distribution_bar"
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
                    Text(
                        text = stat.categoryName,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = stat.totalAmount.toCurrencyFormat(),
                        style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = percentText(stat.percentage),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(40.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .background(MaterialTheme.colorScheme.trackSurface, CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fill.coerceIn(0.02f, 1f))
                            .height(10.dp)
                            .background(color, CircleShape)
                    )
                }
            }
        }
    }
}

/** El total en grande, una sola barra dividida en segmentos y la leyenda en dos columnas. */
@Composable
private fun StackedBody(stats: List<CategoryExpenseStat>, total: Long, label: String) {
    val visible = remember(stats) { groupTopStats(stats, 5, AppStrings.get(R.string.cat_others)) }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = total.toCurrencyFormat(),
                style = MaterialTheme.typography.headlineLarge.copy(fontFeatureSettings = "tnum"),
                fontWeight = FontWeight.ExtraBold
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            visible.forEachIndexed { index, stat ->
                val shape = when {
                    visible.size == 1 -> RoundedCornerShape(17.dp)
                    index == 0 -> RoundedCornerShape(topStart = 17.dp, bottomStart = 17.dp, topEnd = 6.dp, bottomEnd = 6.dp)
                    index == visible.lastIndex -> RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp, topEnd = 17.dp, bottomEnd = 17.dp)
                    else -> RoundedCornerShape(6.dp)
                }
                Box(
                    modifier = Modifier
                        .weight(stat.percentage.coerceAtLeast(0.03f))
                        .height(34.dp)
                        .background(categoryColor(stat, index), shape)
                )
            }
        }
        // Leyenda en filas de dos
        visible.chunked(2).forEachIndexed { rowIndex, pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                pair.forEachIndexed { col, stat ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(modifier = Modifier.size(12.dp).background(categoryColor(stat, rowIndex * 2 + col), CircleShape))
                        Column {
                            Text(
                                text = stat.categoryName,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${stat.totalAmount.toCurrencyFormat()} · ${percentText(stat.percentage)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
                if (pair.size == 1) Box(modifier = Modifier.weight(1f))
            }
        }
    }
}

/** Las cinco categorías mayores; en gastos, cada una con su cambio contra el mes anterior. */
@Composable
private fun Top5Body(stats: List<CategoryExpenseStat>, total: Long, previous: PreviousMonthExpense?, isIncome: Boolean) {
    val visible = remember(stats) { groupTopStats(stats, 5, AppStrings.get(R.string.cat_others)) }
    val totalChange = previous?.let { percentChange(total, it.total) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = if (isIncome) stringResource(R.string.home_income_of_month) else stringResource(R.string.home_expenses_of_month),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = total.toCurrencyFormat(),
                    style = MaterialTheme.typography.headlineMedium.copy(fontFeatureSettings = "tnum"),
                    fontWeight = FontWeight.ExtraBold
                )
            }
            if (totalChange != null && previous.monthLabel.isNotBlank()) {
                ChangeChip(change = totalChange, suffix = " " + stringResource(R.string.home_vs_month, previous.monthLabel.take(3).lowercase()))
            }
        }
        visible.forEachIndexed { index, stat ->
            val change = previous?.totalByCategory?.get(stat.categoryName)?.let { percentChange(stat.totalAmount, it) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.trackSurface, modifier = Modifier.size(26.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("${index + 1}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    text = stat.categoryName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stat.totalAmount.toCurrencyFormat(),
                    style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
                    fontWeight = FontWeight.Bold
                )
                if (previous != null) {
                    if (change != null) ChangeChip(change = change, suffix = "")
                    else Spacer56()
                }
            }
        }
    }
}

@Composable
private fun Spacer56() {
    Box(modifier = Modifier.width(58.dp))
}

/** Insignia de cambio: en gastos, bajar es bueno (verde) y subir es malo (rojo). */
@Composable
private fun ChangeChip(change: Int, suffix: String) {
    val scheme = MaterialTheme.colorScheme
    val (container, content) = when {
        change < 0 -> scheme.primaryContainer to scheme.onPrimaryContainer
        change > 0 -> scheme.errorContainer to scheme.onErrorContainer
        else -> scheme.surfaceContainerHighest to scheme.onSurfaceVariant
    }
    Surface(shape = CircleShape, color = container, contentColor = content) {
        Text(
            text = changeLabel(change, stringResource(R.string.change_none)) + suffix,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}
