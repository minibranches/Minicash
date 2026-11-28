package com.uramos.minicash.ui.screens.home

import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Deja a lo más [maxVisible] categorías: las mayores y, si sobran, una sola «Otros» con la suma del resto. Se espera
 * la lista ordenada de mayor a menor (como la entrega el ViewModel).
 */
internal fun groupTopStats(stats: List<CategoryExpenseStat>, maxVisible: Int, otherLabel: String = "Otros"): List<CategoryExpenseStat> {
    if (maxVisible < 2 || stats.size <= maxVisible) return stats
    val head = stats.take(maxVisible - 1)
    val rest = stats.drop(maxVisible - 1)
    return head + CategoryExpenseStat(
        categoryName = otherLabel,
        totalAmount = rest.sumOf { it.totalAmount },
        percentage = rest.sumOf { it.percentage.toDouble() }.toFloat()
    )
}

/**
 * Cambio porcentual entero de [current] contra [previous] (positivo: subió). Nulo si no hay mes anterior con qué
 * comparar ([previous] en cero o negativo).
 */
internal fun percentChange(current: Long, previous: Long): Int? {
    if (previous <= 0L) return null
    return (((current - previous).toDouble() / previous.toDouble()) * 100.0).roundToInt()
}

/** Texto del cambio: «▲ 12 %», «▼ 5 %» o «sin cambio» cuando el redondeo da cero. */
internal fun changeLabel(change: Int, noChangeLabel: String = "sin cambio"): String = when {
    change > 0 -> "▲ $change %"
    change < 0 -> "▼ ${abs(change)} %"
    else -> noChangeLabel
}
