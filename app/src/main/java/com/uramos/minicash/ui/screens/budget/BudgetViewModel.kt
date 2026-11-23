package com.uramos.minicash.ui.screens.budget

import com.uramos.minicash.ui.utils.toCents
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uramos.minicash.data.local.dao.AccountDao
import com.uramos.minicash.data.local.dao.BudgetDao
import com.uramos.minicash.data.local.dao.CategoryDao
import com.uramos.minicash.data.local.dao.TransactionDao
import com.uramos.minicash.data.local.entities.AccountEntity
import com.uramos.minicash.data.local.entities.BudgetItemEntity
import com.uramos.minicash.data.local.entities.BudgetItemType
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
data class BudgetSummary(
    val executedIncome: Long = 0L,
    val executedReimbursement: Long = 0L,
    val paidExpense: Long = 0L,
    val actualFreeAmount: Long = 0L,
    val expectedIncome: Long = 0L,
    val expectedReimbursement: Long = 0L,
    val expectedExpense: Long = 0L,
    val projectedFreeAmount: Long = 0L
)

@Immutable
data class BudgetUiState(
    val currentMonthLabel: String = "",
    val selectedFortnight: Int = 1,
    val fortnightKey: String = "",
    val summary: BudgetSummary = BudgetSummary(),
    val items: List<BudgetItemEntity> = emptyList(),
    val isLoading: Boolean = false
)

class BudgetViewModel(
    private val budgetDao: BudgetDao,
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao? = null
) : ViewModel() {

    val accounts: StateFlow<List<AccountEntity>> = accountDao.getAllActiveAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = (categoryDao?.getAllActiveCategories() ?: kotlinx.coroutines.flow.flowOf(emptyList()))
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val initialCalendar = Calendar.getInstance().apply {
        if (get(Calendar.DAY_OF_MONTH) <= 15) {
            add(Calendar.MONTH, -1)
        }
    }
    
    private val currentCalendar = MutableStateFlow(initialCalendar)
    private val selectedFortnight = MutableStateFlow(
        if (Calendar.getInstance().get(Calendar.DAY_OF_MONTH) <= 15) 2 else 1
    )

    val uiState: StateFlow<BudgetUiState> = combine(
        currentCalendar,
        selectedFortnight
    ) { cal, q ->
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val key = String.format(Locale.US, "%04d-%02d-Q%d", year, month, q)

        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("es", "MX"))
        val label = monthFormat.format(cal.time).replaceFirstChar { it.uppercase() }

        Triple(key, label, q)
    }.flatMapLatest { (key, label, q) ->
        budgetDao.getItemsByFortnight(key).combine(
            MutableStateFlow(Pair(key, label))
        ) { items, pair ->
            val executedIncome = items.filter { it.type == BudgetItemType.INCOME && it.isPaid }.sumOf { it.amount }
            val executedReimbursement = items.filter { it.type == BudgetItemType.REIMBURSEMENT && it.isPaid }.sumOf { it.amount }
            val paidExpense = items.filter { it.type == BudgetItemType.EXPENSE && it.isPaid }.sumOf { it.amount }
            val actualFree = (executedIncome + executedReimbursement) - paidExpense

            val expectedIncome = items.filter { it.type == BudgetItemType.INCOME }.sumOf { it.amount }
            val expectedReimbursement = items.filter { it.type == BudgetItemType.REIMBURSEMENT }.sumOf { it.amount }
            val expectedExpense = items.filter { it.type == BudgetItemType.EXPENSE }.sumOf { it.amount }
            val projectedFree = (expectedIncome + expectedReimbursement) - expectedExpense

            BudgetUiState(
                currentMonthLabel = pair.second,
                selectedFortnight = q,
                fortnightKey = pair.first,
                summary = BudgetSummary(
                    executedIncome = executedIncome,
                    executedReimbursement = executedReimbursement,
                    paidExpense = paidExpense,
                    actualFreeAmount = actualFree,
                    expectedIncome = expectedIncome,
                    expectedReimbursement = expectedReimbursement,
                    expectedExpense = expectedExpense,
                    projectedFreeAmount = projectedFree
                ),
                items = items,
                isLoading = false
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BudgetUiState(isLoading = true))

    fun selectFortnight(fortnight: Int) {
        selectedFortnight.value = fortnight
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

    suspend fun getItemsForFortnightList(sourceKey: String): List<BudgetItemEntity> {
        return budgetDao.getItemsByFortnightList(sourceKey)
    }

    fun copyFortnight(
        sourceKey: String,
        targetKey: String,
        targetYear: Int,
        targetMonth: Int,
        itemIds: Set<Long>? = null,
        onSuccess: (Int) -> Unit
    ) {
        if (sourceKey.isBlank() || targetKey.isBlank()) return

        viewModelScope.launch {
            val sourceItems = budgetDao.getItemsByFortnightList(sourceKey)
                .filter { itemIds == null || it.id in itemIds }
            if (sourceItems.isNotEmpty()) {
                val clonedItems = sourceItems.map { item ->
                    val newDueDate = item.dueDate?.let { origDueDate ->
                        val origCal = Calendar.getInstance().apply { timeInMillis = origDueDate }
                        val dayOfMonth = origCal.get(Calendar.DAY_OF_MONTH)
                        val hour = origCal.get(Calendar.HOUR_OF_DAY)
                        val minute = origCal.get(Calendar.MINUTE)

                        val targetCal = Calendar.getInstance().apply {
                            clear()
                            set(Calendar.YEAR, targetYear)
                            set(Calendar.MONTH, targetMonth - 1)
                            val maxDay = getActualMaximum(Calendar.DAY_OF_MONTH)
                            set(Calendar.DAY_OF_MONTH, dayOfMonth.coerceAtMost(maxDay))
                            set(Calendar.HOUR_OF_DAY, hour)
                            set(Calendar.MINUTE, minute)
                        }
                        targetCal.timeInMillis
                    }

                    item.copy(
                        id = 0,
                        fortnightKey = targetKey,
                        isPaid = false,
                        dueDate = newDueDate,
                        displayOrder = item.displayOrder
                    )
                }
                budgetDao.insertItems(clonedItems)
                onSuccess(clonedItems.size)
            } else {
                onSuccess(0)
            }
        }
    }

    fun cloneItemsFromFortnight(sourceKey: String, onSuccess: (Int) -> Unit) {
        val currentKey = uiState.value.fortnightKey
        if (currentKey.isBlank()) return
        val parts = currentKey.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
        val month = parts.getOrNull(1)?.toIntOrNull() ?: (Calendar.getInstance().get(Calendar.MONTH) + 1)
        copyFortnight(sourceKey, currentKey, year, month, null, onSuccess)
    }

    fun markItemAsPaidWithTransaction(
        item: BudgetItemEntity,
        accountId: Long,
        categoryId: Long? = null,
        createTransaction: Boolean
    ) {
        viewModelScope.launch {
            if (createTransaction) {
                when (item.type) {
                    BudgetItemType.EXPENSE -> {
                        transactionDao.insertTransaction(
                            TransactionEntity(
                                sourceAccountId = accountId,
                                categoryId = categoryId,
                                amount = item.amount,
                                type = TransactionType.EXPENSE,
                                date = item.dueDate ?: System.currentTimeMillis(),
                                note = "Presupuesto: ${item.concept}"
                            )
                        )
                        accountDao.updateBalance(accountId, -item.amount)
                    }
                    BudgetItemType.REIMBURSEMENT, BudgetItemType.INCOME -> {
                        transactionDao.insertTransaction(
                            TransactionEntity(
                                sourceAccountId = accountId,
                                categoryId = categoryId,
                                amount = item.amount,
                                type = TransactionType.INCOME,
                                date = item.dueDate ?: System.currentTimeMillis(),
                                note = "Presupuesto (${item.type.displayName}): ${item.concept}"
                            )
                        )
                        accountDao.updateBalance(accountId, item.amount)
                    }
                }
            }
            budgetDao.updateItem(item.copy(isPaid = true))
        }
    }

    fun toggleItemPaid(item: BudgetItemEntity) {
        viewModelScope.launch {
            budgetDao.updateItem(item.copy(isPaid = !item.isPaid))
        }
    }

    /** Guarda el orden definitivo de las partidas al terminar de arrastrar. */
    fun saveItemOrder(orderedIds: List<Long>) {
        viewModelScope.launch {
            val byId = uiState.value.items.associateBy { it.id }
            val reindexed = orderedIds.mapIndexedNotNull { index, id -> byId[id]?.copy(displayOrder = index) }
            if (reindexed.isNotEmpty()) budgetDao.updateItems(reindexed)
        }
    }

    fun addItem(concept: String, amountDouble: Double, type: BudgetItemType, responsible: String, dueDate: Long? = null) {
        val key = uiState.value.fortnightKey
        if (key.isBlank() || concept.isBlank()) return

        viewModelScope.launch {
            budgetDao.insertItem(
                BudgetItemEntity(
                    concept = concept,
                    amount = amountDouble.toCents(),
                    type = type,
                    responsible = responsible,
                    dueDate = dueDate,
                    fortnightKey = key,
                    isPaid = false,
                    displayOrder = uiState.value.items.size
                )
            )
        }
    }

    fun updateBudgetItem(item: BudgetItemEntity, concept: String, amountDouble: Double, type: BudgetItemType, responsible: String, dueDate: Long?) {
        viewModelScope.launch {
            budgetDao.updateItem(
                item.copy(
                    concept = concept,
                    amount = amountDouble.toCents(),
                    type = type,
                    responsible = responsible,
                    dueDate = dueDate
                )
            )
        }
    }

    fun deleteItem(item: BudgetItemEntity) {
        viewModelScope.launch {
            budgetDao.deleteItem(item)
        }
    }

    /** Deshace el borrado de una partida: se vuelve a insertar con su mismo id, orden y estado. */
    fun restoreItem(item: BudgetItemEntity) {
        viewModelScope.launch {
            budgetDao.insertItems(listOf(item))
        }
    }
}
/** Partidas del tipo elegido con las píldoras del resumen; sin filtro (null) se devuelven todas, en el mismo orden. */
internal fun filterBudgetItems(items: List<BudgetItemEntity>, filter: BudgetItemType?): List<BudgetItemEntity> =
    if (filter == null) items else items.filter { it.type == filter }
