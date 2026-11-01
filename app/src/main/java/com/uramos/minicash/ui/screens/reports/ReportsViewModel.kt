package com.uramos.minicash.ui.screens.reports

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uramos.minicash.data.local.dao.TransactionDao
import com.uramos.minicash.data.local.entities.TransactionType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

@Immutable
data class MonthlyTotals(
    val month: YearMonth,
    val income: Long,
    val expense: Long
) {
    val balance: Long get() = income - expense
}

@Immutable
data class ReportsUiState(
    val monthsRange: Int = 6,
    val months: List<MonthlyTotals> = emptyList()
) {
    val averageIncome: Long get() = if (months.isEmpty()) 0 else months.sumOf { it.income } / months.size
    val averageExpense: Long get() = if (months.isEmpty()) 0 else months.sumOf { it.expense } / months.size
    val averageBalance: Long get() = averageIncome - averageExpense
    val hasData: Boolean get() = months.any { it.income != 0L || it.expense != 0L }
}

/** Ingresos contra gastos por mes. Las transferencias no cuentan, igual que en Inicio. */
@OptIn(ExperimentalCoroutinesApi::class)
class ReportsViewModel(private val transactionDao: TransactionDao) : ViewModel() {

    private val range = MutableStateFlow(6)

    val uiState: StateFlow<ReportsUiState> = range
        .flatMapLatest { count ->
            val zone = ZoneId.systemDefault()
            val first = YearMonth.now().minusMonths((count - 1).toLong())
            val start = first.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
            transactionDao.getTransactionsBetween(start, Long.MAX_VALUE).map { transactions ->
                val income = HashMap<YearMonth, Long>()
                val expense = HashMap<YearMonth, Long>()
                transactions.forEach { tx ->
                    val month = YearMonth.from(Instant.ofEpochMilli(tx.date).atZone(zone))
                    when (tx.type) {
                        TransactionType.INCOME -> income[month] = (income[month] ?: 0L) + tx.amount
                        TransactionType.EXPENSE -> expense[month] = (expense[month] ?: 0L) + tx.amount
                        TransactionType.TRANSFER -> Unit
                    }
                }
                // Serie completa: los meses sin movimientos aparecen en cero
                ReportsUiState(
                    monthsRange = count,
                    months = (0 until count).map { i ->
                        val month = first.plusMonths(i.toLong())
                        MonthlyTotals(month, income[month] ?: 0L, expense[month] ?: 0L)
                    }
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportsUiState())

    val selectedRange: StateFlow<Int> = range.asStateFlow()

    fun selectRange(months: Int) {
        range.value = months
    }
}
