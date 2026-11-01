package com.uramos.minicash.ui.screens.home

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uramos.minicash.data.local.dao.AccountDao
import com.uramos.minicash.data.local.dao.CategoryDao
import com.uramos.minicash.data.local.dao.TransactionDao
import com.uramos.minicash.data.local.entities.AccountEntity
import com.uramos.minicash.data.local.entities.CategoryEntity
import com.uramos.minicash.data.local.entities.TransactionEntity
import com.uramos.minicash.data.local.entities.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.ZoneId
import java.util.Calendar

@Immutable
data class CategoryExpenseStat(
    val categoryName: String,
    val totalAmount: Long,
    val percentage: Float,
    val colorHex: String? = null,
    val iconName: String? = null
)

@Immutable
data class HomeUiState(
    val accounts: List<AccountEntity> = emptyList(),
    val totalBalance: Long = 0L,
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val monthlyExpenseStats: List<CategoryExpenseStat> = emptyList(),
    val totalMonthlyExpense: Long = 0L,
    val monthlyIncomeStats: List<CategoryExpenseStat> = emptyList(),
    val totalMonthlyIncome: Long = 0L,
    val isLoading: Boolean = true
)

class HomeViewModel(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao
) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> = categoryDao.getAllActiveCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

    private fun getStartOfMonth(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getEndOfMonth(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    /** Mes elegido a mano para las gráficas de distribución; null sigue al mes en curso. */
    private val _chartMonth = MutableStateFlow<YearMonth?>(null)
    val chartMonth: StateFlow<YearMonth?> = _chartMonth.asStateFlow()

    fun shiftChartMonth(delta: Int) {
        _chartMonth.update { current ->
            val target = (current ?: YearMonth.now()).plusMonths(delta.toLong())
            // Sin meses futuros; llegar al mes en curso vuelve al modo automático
            if (!target.isBefore(YearMonth.now())) null else target
        }
    }

    fun resetChartMonth() {
        _chartMonth.value = null
    }

    private fun rangeOf(month: YearMonth): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val start = month.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = month.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
        return start to end
    }

    /** Se recalcula a medianoche, de modo que el mes mostrado no se queda viejo si la app sigue abierta. */
    private val currentMonthRange: Flow<Pair<Long, Long>> = flow {
        while (true) {
            emit(getStartOfMonth() to getEndOfMonth())
            val now = Calendar.getInstance()
            val nextMidnight = (now.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            delay((nextMidnight - now.timeInMillis).coerceAtLeast(1_000L))
        }
    }.distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val monthRange: Flow<Pair<Long, Long>> = _chartMonth.flatMapLatest { month ->
        if (month == null) currentMonthRange else flowOf(rangeOf(month))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HomeUiState> = monthRange.flatMapLatest { (monthStart, monthEnd) -> combine(
        accountDao.getAllActiveAccounts(),
        accountDao.getTotalBalance(),
        transactionDao.getRecentTransactions(),
        transactionDao.getTransactionsBetween(monthStart, monthEnd),
        categoryDao.getAllActiveCategories()
    ) { accounts, totalBalance, recentTransactions, monthTransactions, categories ->
        val categoriesMap = categories.associateBy { it.id }

        // Gastos
        val expenseTransactions = monthTransactions.filter { it.type == TransactionType.EXPENSE }
        val totalMonthlyExpense = expenseTransactions.sumOf { it.amount }
        val monthlyExpenseStats = if (totalMonthlyExpense > 0L) {
            expenseTransactions
                .groupBy { tx ->
                    tx.categoryId?.let { categoriesMap[it] }
                }
                .map { (catEntity, txList) ->
                    val catTotal = txList.sumOf { it.amount }
                    CategoryExpenseStat(
                        categoryName = catEntity?.name ?: "Otros",
                        totalAmount = catTotal,
                        percentage = catTotal.toFloat() / totalMonthlyExpense.toFloat(),
                        colorHex = catEntity?.colorHex,
                        iconName = catEntity?.iconName
                    )
                }
                .sortedByDescending { it.totalAmount }
        } else {
            emptyList()
        }

        // Ingresos
        val incomeTransactions = monthTransactions.filter { it.type == TransactionType.INCOME }
        val totalMonthlyIncome = incomeTransactions.sumOf { it.amount }
        val monthlyIncomeStats = if (totalMonthlyIncome > 0L) {
            incomeTransactions
                .groupBy { tx ->
                    tx.categoryId?.let { categoriesMap[it] }
                }
                .map { (catEntity, txList) ->
                    val catTotal = txList.sumOf { it.amount }
                    CategoryExpenseStat(
                        categoryName = catEntity?.name ?: "Otros",
                        totalAmount = catTotal,
                        percentage = catTotal.toFloat() / totalMonthlyIncome.toFloat(),
                        colorHex = catEntity?.colorHex,
                        iconName = catEntity?.iconName
                    )
                }
                .sortedByDescending { it.totalAmount }
        } else {
            emptyList()
        }

        HomeUiState(
            accounts = accounts,
            totalBalance = totalBalance ?: 0L,
            recentTransactions = recentTransactions,
            monthlyExpenseStats = monthlyExpenseStats,
            totalMonthlyExpense = totalMonthlyExpense,
            monthlyIncomeStats = monthlyIncomeStats,
            totalMonthlyIncome = totalMonthlyIncome,
            isLoading = false
        )
    } }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    val selectedTotalAmount: StateFlow<Long> = combine(
        _selectedIds,
        uiState
    ) { ids, state ->
        state.recentTransactions.filter { it.id in ids }.sumOf { tx ->
            when (tx.type) {
                TransactionType.INCOME -> tx.amount
                TransactionType.EXPENSE -> -tx.amount
                TransactionType.TRANSFER -> 0L
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun toggleSelection(id: Long) {
        _selectedIds.update { current ->
            if (id in current) current - id else current + id
        }
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun deleteSelectedTransactions() {
        viewModelScope.launch {
            val selectedTxs = uiState.value.recentTransactions.filter { it.id in _selectedIds.value }
            selectedTxs.forEach { transaction ->
                when (transaction.type) {
                    TransactionType.INCOME -> accountDao.updateBalance(transaction.sourceAccountId, -transaction.amount)
                    TransactionType.EXPENSE -> accountDao.updateBalance(transaction.sourceAccountId, transaction.amount)
                    TransactionType.TRANSFER -> {
                        accountDao.updateBalance(transaction.sourceAccountId, transaction.amount)
                        transaction.destinationAccountId?.let { destId -> accountDao.updateBalance(destId, -transaction.amount) }
                    }
                }
                transactionDao.deleteTransaction(transaction)
            }
            clearSelection()
        }
    }

    fun updateTransaction(original: TransactionEntity, updated: TransactionEntity, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            when (original.type) {
                TransactionType.INCOME -> accountDao.updateBalance(original.sourceAccountId, -original.amount)
                TransactionType.EXPENSE -> accountDao.updateBalance(original.sourceAccountId, original.amount)
                TransactionType.TRANSFER -> {
                    accountDao.updateBalance(original.sourceAccountId, original.amount)
                    original.destinationAccountId?.let { destId -> accountDao.updateBalance(destId, -original.amount) }
                }
            }

            when (updated.type) {
                TransactionType.INCOME -> accountDao.updateBalance(updated.sourceAccountId, updated.amount)
                TransactionType.EXPENSE -> accountDao.updateBalance(updated.sourceAccountId, -updated.amount)
                TransactionType.TRANSFER -> {
                    accountDao.updateBalance(updated.sourceAccountId, -updated.amount)
                    updated.destinationAccountId?.let { destId -> accountDao.updateBalance(destId, updated.amount) }
                }
            }

            transactionDao.updateTransaction(updated)
            onSuccess()
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            when (transaction.type) {
                TransactionType.INCOME -> accountDao.updateBalance(transaction.sourceAccountId, -transaction.amount)
                TransactionType.EXPENSE -> accountDao.updateBalance(transaction.sourceAccountId, transaction.amount)
                TransactionType.TRANSFER -> {
                    accountDao.updateBalance(transaction.sourceAccountId, transaction.amount)
                    transaction.destinationAccountId?.let { destId -> accountDao.updateBalance(destId, -transaction.amount) }
                }
            }
            transactionDao.deleteTransaction(transaction)
        }
    }
}
