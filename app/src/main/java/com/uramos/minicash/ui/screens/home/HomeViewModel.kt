package com.uramos.minicash.ui.screens.home

import com.uramos.minicash.data.util.removeTransactions
import com.uramos.minicash.ui.screens.account.transactionsOfAccount
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import com.uramos.minicash.data.local.dao.findOrCreateByName
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

/** Gastos del mes anterior al que se muestra, por nombre de categoría, para comparar en el estilo «Top 5». */
@Immutable
data class PreviousMonthExpense(
    val totalByCategory: Map<String, Long> = emptyMap(),
    val total: Long = 0L,
    val monthLabel: String = ""
)

class HomeViewModel(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    userPreferences: com.uramos.minicash.data.preferences.UserPreferences? = null
) : ViewModel() {

    /** Qué secciones de Inicio se ven, en qué orden y con qué estilo (se cambia en Ajustes → Inicio). */
    val homeLayout: StateFlow<com.uramos.minicash.data.preferences.HomeLayout> =
        (userPreferences?.homeLayout ?: flowOf(com.uramos.minicash.data.preferences.HomeLayout()))
            .stateIn(viewModelScope, SharingStarted.Eagerly, com.uramos.minicash.data.preferences.HomeLayout())

    /** Alta rápida desde el selector de categorías de la edición; entrega el id para dejarla seleccionada. */
    fun createCategory(name: String, type: TransactionType, onCreated: (Long) -> Unit) {
        if (name.isBlank()) return
        viewModelScope.launch { onCreated(categoryDao.findOrCreateByName(name, type)) }
    }

    /** Movimientos que tocan una cuenta (como origen o destino), del más reciente al más antiguo. */
    fun accountTransactions(accountId: Long): Flow<List<TransactionEntity>> =
        transactionDao.getAllTransactions().map { transactionsOfAccount(it, accountId) }

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
        _chartMonth.update { current -> shiftedChartMonth(current, delta, YearMonth.now()) }
    }

    fun resetChartMonth() {
        _chartMonth.value = null
    }

    /** Años que se pueden elegir en la distribución: del primer movimiento al año en curso (sin años futuros). */
    val chartYears: StateFlow<List<Int>> = transactionDao.getEarliestTransactionDate()
        .map { earliest ->
            val current = YearMonth.now().year
            val first = earliest?.let { java.time.Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).year } ?: current
            (current downTo minOf(first, current)).toList()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(YearMonth.now().year))

    /** Cambia de año conservando el mes; si el resultado cae en el futuro vuelve al mes en curso. */
    fun selectChartYear(year: Int) {
        _chartMonth.update { current -> chartMonthForYear(current, year, YearMonth.now()) }
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

    @OptIn(ExperimentalCoroutinesApi::class)
    val previousMonthExpense: StateFlow<PreviousMonthExpense> = monthRange.flatMapLatest { (monthStart, _) ->
        val shown = java.time.Instant.ofEpochMilli(monthStart).atZone(ZoneId.systemDefault()).toLocalDate()
        val previous = YearMonth.from(shown).minusMonths(1)
        val (previousStart, previousEnd) = rangeOf(previous)
        combine(
            transactionDao.getTransactionsBetween(previousStart, previousEnd),
            categoryDao.getAllActiveCategories()
        ) { transactions, categories ->
            val names = categories.associate { it.id to it.name }
            val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
            PreviousMonthExpense(
                totalByCategory = expenses
                    .groupBy { tx -> tx.categoryId?.let { names[it] } ?: "Otros" }
                    .mapValues { (_, list) -> list.sumOf { it.amount } },
                total = expenses.sumOf { it.amount },
                monthLabel = previous.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.forLanguageTag("es-MX"))
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PreviousMonthExpense())

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

    private val _deletedEvents = MutableSharedFlow<List<TransactionEntity>>(extraBufferCapacity = 1)

    /** Movimientos recién borrados, para mostrar el aviso con "Deshacer". */
    val deletedEvents: SharedFlow<List<TransactionEntity>> = _deletedEvents.asSharedFlow()

    fun deleteSelectedTransactions() {
        viewModelScope.launch {
            val selectedTxs = uiState.value.recentTransactions.filter { it.id in _selectedIds.value }
            if (selectedTxs.isNotEmpty()) {
                removeTransactions(accountDao, transactionDao, selectedTxs)
                _deletedEvents.emit(selectedTxs)
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
            removeTransactions(accountDao, transactionDao, listOf(transaction))
            _deletedEvents.emit(listOf(transaction))
        }
    }

    /** Deshace un borrado: la pantalla lo pide desde el aviso con "Deshacer". */
    fun restoreTransactions(transactions: List<TransactionEntity>) {
        viewModelScope.launch { com.uramos.minicash.data.util.restoreTransactions(accountDao, transactionDao, transactions) }
    }
}

/** Mes de la distribución tras moverse [delta] meses: sin meses futuros; llegar al mes en curso vuelve a «automático» (null). */
internal fun shiftedChartMonth(current: YearMonth?, delta: Int, now: YearMonth): YearMonth? {
    val target = (current ?: now).plusMonths(delta.toLong())
    return if (!target.isBefore(now)) null else target
}

/** Mes de la distribución al elegir [year]: conserva el mes; si cae en el futuro vuelve a «automático» (null). */
internal fun chartMonthForYear(current: YearMonth?, year: Int, now: YearMonth): YearMonth? {
    val target = YearMonth.of(year, (current ?: now).month)
    return if (!target.isBefore(now)) null else target
}
