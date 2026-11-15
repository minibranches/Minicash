package com.uramos.minicash.ui.screens.transactions

import com.uramos.minicash.data.util.removeTransactions
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import com.uramos.minicash.data.local.dao.findOrCreateByName
import kotlinx.coroutines.flow.map
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Immutable
data class MonthSummary(
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val netBalance: Long = 0L
)

@Immutable
data class TransactionsUiState(
    val currentMonthLabel: String = "",
    val selectedMonthIndex: Int = Calendar.getInstance().get(Calendar.MONTH),
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val summary: MonthSummary = MonthSummary(),
    val transactions: List<TransactionEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedTypeFilter: TransactionType? = null,
    val accounts: List<AccountEntity> = emptyList(),
    val selectedAccountId: Long? = null,
    val selectedCategoryIds: Set<Long> = emptySet(),
    val isLoading: Boolean = false
)

private data class TransactionFilters(
    val query: String,
    val type: TransactionType?,
    val accountId: Long?,
    val categoryIds: Set<Long>
)

class TransactionsViewModel(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao
) : ViewModel() {

    val accounts: StateFlow<List<AccountEntity>> = accountDao.getAllAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Alta rápida desde el selector de categorías de la edición; entrega el id para dejarla seleccionada. */
    fun createCategory(name: String, type: TransactionType, onCreated: (Long) -> Unit) {
        if (name.isBlank()) return
        viewModelScope.launch { onCreated(categoryDao.findOrCreateByName(name, type)) }
    }

    val categories: StateFlow<List<CategoryEntity>> = categoryDao.getAllActiveCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val currentCalendar = MutableStateFlow(Calendar.getInstance())
    private val searchQuery = MutableStateFlow("")
    private val selectedTypeFilter = MutableStateFlow<TransactionType?>(null)
    private val selectedAccountId = MutableStateFlow<Long?>(null)
    private val selectedCategoryIds = MutableStateFlow<Set<Long>>(emptySet())

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

    private val filters = combine(searchQuery, selectedTypeFilter, selectedAccountId, selectedCategoryIds) { query, type, account, categories ->
        TransactionFilters(query, type, account, categories)
    }

    val uiState: StateFlow<TransactionsUiState> = currentCalendar.flatMapLatest { cal ->
        val startOfMonth = (cal.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfMonth = (cal.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val monthLabelFormat = SimpleDateFormat("MMMM yyyy", Locale("es", "MX"))
        val monthLabel = monthLabelFormat.format(cal.time).replaceFirstChar { it.uppercase() }

        combine(
            transactionDao.getTransactionsBetween(startOfMonth, endOfMonth),
            filters,
            accounts
        ) { txList, activeFilters, allAccounts ->
            val (query, typeFilter, accountIdFilter, categoryIdFilter) = activeFilters
            val income = txList.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val expense = txList.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val net = income - expense

            val filteredList = txList.filter { tx ->
                val matchesQuery = query.isBlank() ||
                        (tx.note?.contains(query, ignoreCase = true) == true) ||
                        tx.type.displayName.contains(query, ignoreCase = true)
                val matchesType = typeFilter == null || tx.type == typeFilter
                val matchesAccount = accountIdFilter == null ||
                        tx.sourceAccountId == accountIdFilter ||
                        tx.destinationAccountId == accountIdFilter
                val matchesCategory = categoryIdFilter.isEmpty() || tx.categoryId in categoryIdFilter
                matchesQuery && matchesType && matchesAccount && matchesCategory
            }

            TransactionsUiState(
                currentMonthLabel = monthLabel,
                selectedMonthIndex = cal.get(Calendar.MONTH),
                selectedYear = cal.get(Calendar.YEAR),
                summary = MonthSummary(totalIncome = income, totalExpense = expense, netBalance = net),
                transactions = filteredList,
                searchQuery = query,
                selectedTypeFilter = typeFilter,
                accounts = allAccounts,
                selectedAccountId = accountIdFilter,
                selectedCategoryIds = categoryIdFilter,
                isLoading = false
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TransactionsUiState(isLoading = true))

    val selectedTotalAmount: StateFlow<Long> = combine(
        _selectedIds,
        uiState
    ) { ids, state ->
        state.transactions.filter { it.id in ids }.sumOf { tx ->
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
            val selectedTxs = uiState.value.transactions.filter { it.id in _selectedIds.value }
            if (selectedTxs.isNotEmpty()) {
                removeTransactions(accountDao, transactionDao, selectedTxs)
                _deletedEvents.emit(selectedTxs)
            }
            clearSelection()
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun onTypeFilterSelect(type: TransactionType?) {
        selectedTypeFilter.value = if (selectedTypeFilter.value == type) null else type
        selectedCategoryIds.value = emptySet() // las categorías pertenecen a un tipo; al cambiarlo dejan de aplicar
    }

    fun onSelectAccountFilter(accountId: Long?) {
        selectedAccountId.value = if (selectedAccountId.value == accountId) null else accountId
    }

    fun toggleCategoryFilter(categoryId: Long) {
        selectedCategoryIds.update { if (categoryId in it) it - categoryId else it + categoryId }
    }

    fun clearCategoryFilter() {
        selectedCategoryIds.value = emptySet()
    }

    /** Años que se pueden elegir: desde el del primer movimiento hasta el próximo año. */
    val availableYears: StateFlow<List<Int>> = transactionDao.getEarliestTransactionDate()
        .map { earliest ->
            val current = Calendar.getInstance().get(Calendar.YEAR)
            val first = earliest?.let { Calendar.getInstance().apply { timeInMillis = it }.get(Calendar.YEAR) } ?: current
            (minOf(first, current)..current + 1).toList().reversed()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(Calendar.getInstance().get(Calendar.YEAR)))

    fun selectYear(year: Int) {
        val next = (currentCalendar.value.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.YEAR, year)
        }
        currentCalendar.value = next
    }

    /** Mueve el periodo [delta] meses, cruzando de año si hace falta. */
    fun shiftMonths(delta: Int) {
        val next = (currentCalendar.value.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, delta)
        }
        currentCalendar.value = next
    }

    /** Se fija el día 1 antes de cambiar mes o año para que un día 31 no salte al mes siguiente. */
    fun selectMonth(monthIndex: Int) {
        val next = (currentCalendar.value.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.MONTH, monthIndex)
        }
        currentCalendar.value = next
    }

    fun shiftYear(delta: Int) {
        val next = (currentCalendar.value.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.YEAR, delta)
        }
        currentCalendar.value = next
    }

    fun nextMonth() {
        val next = (currentCalendar.value.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
        }
        currentCalendar.value = next
    }

    fun previousMonth() {
        val prev = (currentCalendar.value.clone() as Calendar).apply {
            add(Calendar.MONTH, -1)
        }
        currentCalendar.value = prev
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
