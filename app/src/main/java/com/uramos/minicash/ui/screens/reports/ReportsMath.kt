package com.uramos.minicash.ui.screens.reports

import androidx.compose.runtime.Immutable
import com.uramos.minicash.data.local.entities.CategoryEntity
import com.uramos.minicash.data.local.entities.TransactionEntity
import com.uramos.minicash.data.local.entities.TransactionType

/** Gasto del periodo en una categoría; [colorHex] es null para "Otros" y "Sin categoría". */
@Immutable
data class CategorySpend(
    val name: String,
    val colorHex: String?,
    val total: Long,
    /** Parte del gasto total del periodo, de 0 a 1. */
    val share: Float
)

const val OTHER_CATEGORIES_LABEL = "Otros"
const val UNCATEGORIZED_LABEL = "Sin categoría"

/**
 * Gastos del periodo agrupados por categoría, de mayor a menor. Se muestran las [top] más grandes y el resto
 * se pliega en "Otros", para que la lista nunca crezca sin límite.
 */
fun buildCategorySpend(
    transactions: List<TransactionEntity>,
    categories: Map<Long, CategoryEntity>,
    top: Int = 6
): List<CategorySpend> {
    val expenses = transactions.filter { it.type == TransactionType.EXPENSE && it.amount > 0 }
    val grandTotal = expenses.sumOf { it.amount }
    if (grandTotal <= 0L) return emptyList()

    val grouped = expenses
        .groupBy { tx -> tx.categoryId?.takeIf { it in categories } }
        .map { (id, list) ->
            val category = id?.let { categories[it] }
            CategorySpend(
                name = category?.name ?: UNCATEGORIZED_LABEL,
                colorHex = category?.colorHex,
                total = list.sumOf { it.amount },
                share = 0f
            )
        }
        .sortedByDescending { it.total }

    val shown = grouped.take(top)
    val rest = grouped.drop(top)
    val withOthers = if (rest.isEmpty()) shown else shown + CategorySpend(
        name = OTHER_CATEGORIES_LABEL,
        colorHex = null,
        total = rest.sumOf { it.total },
        share = 0f
    )
    return withOthers.map { it.copy(share = it.total.toFloat() / grandTotal.toFloat()) }
}

/** Balance acumulado mes a mes: cada valor suma el balance de ese mes y de los anteriores del periodo. */
fun cumulativeBalance(months: List<MonthlyTotals>): List<Long> {
    var running = 0L
    return months.map { month ->
        running += month.balance
        running
    }
}

/** Parte de los ingresos que no se gastó, de -∞ a 1; null si no hubo ingresos en el periodo. */
fun savingsRate(months: List<MonthlyTotals>): Float? {
    val income = months.sumOf { it.income }
    if (income <= 0L) return null
    val expense = months.sumOf { it.expense }
    return (income - expense).toFloat() / income.toFloat()
}

/** Mes con mejor balance entre los que tuvieron movimientos; null si no hay ninguno. */
fun bestMonth(months: List<MonthlyTotals>): MonthlyTotals? =
    months.filter { it.income != 0L || it.expense != 0L }.maxByOrNull { it.balance }
