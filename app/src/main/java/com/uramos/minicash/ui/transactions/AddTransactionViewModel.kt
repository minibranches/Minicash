package com.uramos.minicash.ui.transactions

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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

data class AutocompleteSuggestion(
    val concept: String,
    val categoryId: Long?,
    val categoryName: String?,
    val sourceAccountId: Long,
    val accountName: String
)

data class AddTransactionUiState(
    val type: TransactionType = TransactionType.EXPENSE,
    val concept: String = "",
    val note: String = "",
    val amountExpression: String = "",
    val calculatedAmount: Double = 0.0,
    val selectedAccountId: Long? = null,
    val selectedDestinationAccountId: Long? = null,
    val selectedCategoryId: Long? = null,
    val selectedDate: Long = System.currentTimeMillis(),
    val autocompleteSuggestions: List<AutocompleteSuggestion> = emptyList()
)

class AddTransactionViewModel(
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao
) : ViewModel() {

    val accounts: StateFlow<List<AccountEntity>> = accountDao.getAllAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = categoryDao.getAllActiveCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    fun onTypeChange(type: TransactionType) {
        _uiState.update { it.copy(type = type, selectedCategoryId = null, autocompleteSuggestions = emptyList()) }
    }

    private fun normalizeText(input: String): String {
        val normalized = java.text.Normalizer.normalize(input.trim(), java.text.Normalizer.Form.NFD)
        return normalized.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "").lowercase()
    }

    fun onConceptChange(concept: String) {
        _uiState.update { it.copy(concept = concept) }

        val normalizedQuery = normalizeText(concept)
        val currentType = _uiState.value.type

        if (normalizedQuery.length >= 2 && currentType != TransactionType.TRANSFER) {
            viewModelScope.launch {
                val allTx = transactionDao.getAllTransactionsList()
                val sortedTx = allTx.sortedByDescending { it.date }

                val matchingTxList = sortedTx.filter { tx ->
                    if (tx.type != currentType) return@filter false
                    val txConcept = tx.note?.substringBefore(" • ")?.trim().orEmpty()
                    txConcept.isNotEmpty() && normalizeText(txConcept).contains(normalizedQuery)
                }

                val uniqueMatches = matchingTxList
                    .distinctBy { tx ->
                        val txConcept = tx.note?.substringBefore(" • ")?.trim().orEmpty()
                        Triple(
                            normalizeText(txConcept),
                            tx.categoryId,
                            tx.sourceAccountId
                        )
                    }
                    .take(5)

                if (uniqueMatches.size == 1) {
                    val singleMatch = uniqueMatches.first()
                    val singleConcept = singleMatch.note?.substringBefore(" • ")?.trim().orEmpty()
                    val currentState = _uiState.value
                    if (currentState.concept.trim() == singleConcept &&
                        currentState.selectedCategoryId == singleMatch.categoryId &&
                        currentState.selectedAccountId == singleMatch.sourceAccountId) {
                        _uiState.update { it.copy(autocompleteSuggestions = emptyList()) }
                        return@launch
                    }
                }

                if (uniqueMatches.isNotEmpty()) {
                    val allCategories = categoryDao.getAllCategoriesList()
                    val allAccounts = accountDao.getAllAccountsList()

                    val suggestions = uniqueMatches.map { match ->
                        val conceptStr = match.note?.substringBefore(" • ")?.trim() ?: concept.trim()
                        val catName = match.categoryId?.let { catId ->
                            allCategories.firstOrNull { it.id == catId }?.name
                        }
                        val accName = allAccounts.firstOrNull { it.id == match.sourceAccountId }?.name ?: "Cuenta"

                        AutocompleteSuggestion(
                            concept = conceptStr,
                            categoryId = match.categoryId,
                            categoryName = catName,
                            sourceAccountId = match.sourceAccountId,
                            accountName = accName
                        )
                    }
                    _uiState.update { it.copy(autocompleteSuggestions = suggestions) }
                } else {
                    _uiState.update { it.copy(autocompleteSuggestions = emptyList()) }
                }
            }
        } else {
            _uiState.update { it.copy(autocompleteSuggestions = emptyList()) }
        }
    }

    fun applySuggestion(suggestion: AutocompleteSuggestion) {
        _uiState.update { state ->
            state.copy(
                concept = suggestion.concept,
                selectedCategoryId = suggestion.categoryId ?: state.selectedCategoryId,
                selectedAccountId = suggestion.sourceAccountId,
                autocompleteSuggestions = emptyList()
            )
        }
    }

    fun onKeypadPress(key: String) {
        _uiState.update { state ->
            val currentExpr = state.amountExpression
            val newExpr = when (key) {
                "⌫" -> {
                    if (currentExpr.endsWith(" ")) {
                        currentExpr.trimEnd().dropLast(1).trimEnd()
                    } else if (currentExpr.isNotEmpty()) {
                        currentExpr.dropLast(1)
                    } else {
                        ""
                    }
                }
                "C" -> ""
                "+", "-", "×", "÷" -> {
                    val trimmed = currentExpr.trim()
                    if (trimmed.isEmpty()) {
                        ""
                    } else {
                        val lastChar = trimmed.last().toString()
                        if (lastChar in listOf("+", "-", "×", "÷")) {
                            trimmed.dropLast(1).trim() + " $key "
                        } else {
                            "$trimmed $key "
                        }
                    }
                }
                "." -> {
                    val tokens = currentExpr.split(" ")
                    val lastToken = tokens.lastOrNull() ?: ""
                    if (lastToken.contains(".")) {
                        currentExpr
                    } else if (lastToken.isEmpty() || lastToken in listOf("+", "-", "×", "÷")) {
                        if (currentExpr.endsWith(" ")) "${currentExpr}0." else "${currentExpr} 0."
                    } else {
                        "$currentExpr."
                    }
                }
                else -> { // Digits "0".."9", "00"
                    if (currentExpr == "0" && key != "0") {
                        key
                    } else if (currentExpr == "0" && key == "0") {
                        "0"
                    } else {
                        currentExpr + key
                    }
                }
            }

            val calcResult = evaluateMathExpression(newExpr)
            state.copy(amountExpression = newExpr, calculatedAmount = calcResult)
        }
    }

    private fun evaluateMathExpression(expression: String): Double {
        if (expression.isBlank()) return 0.0
        val clean = expression.replace('×', '*').replace('÷', '/').replace(',', '.').trim()
        return runCatching {
            val tokenRegex = Regex("(\\d+(?:\\.\\d+)?|[+\\-*\\/])")
            val tokens = tokenRegex.findAll(clean).map { it.value }.toList()
            if (tokens.isEmpty()) return@runCatching 0.0

            var total = tokens[0].toDoubleOrNull() ?: 0.0
            var i = 1
            while (i < tokens.size - 1) {
                val op = tokens[i]
                val nextVal = tokens[i + 1].toDoubleOrNull()
                if (nextVal != null) {
                    when (op) {
                        "+" -> total += nextVal
                        "-" -> total -= nextVal
                        "*" -> total *= nextVal
                        "/" -> if (nextVal != 0.0) total /= nextVal
                    }
                    i += 2
                } else {
                    i += 1
                }
            }
            if (total.isNaN() || total.isInfinite()) 0.0 else total
        }.getOrDefault(0.0)
    }

    fun resetState() {
        _uiState.value = AddTransactionUiState()
    }

    fun resetForm() {
        resetState()
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun setNotes(notes: String) {
        onNoteChange(notes)
    }

    fun setConcept(concept: String) {
        onConceptChange(concept)
    }

    fun onAccountSelect(accountId: Long) {
        _uiState.update { it.copy(selectedAccountId = accountId) }
    }

    fun setSourceAccount(accountId: Long) {
        onAccountSelect(accountId)
    }

    fun onDestinationAccountSelect(accountId: Long) {
        _uiState.update { it.copy(selectedDestinationAccountId = accountId) }
    }

    fun setDestinationAccount(accountId: Long) {
        onDestinationAccountSelect(accountId)
    }

    fun onCategorySelect(categoryId: Long) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun prefillForBudgetItem(item: com.uramos.minicash.data.local.entities.BudgetItemEntity) {
        val txType = when (item.type) {
            com.uramos.minicash.data.local.entities.BudgetItemType.EXPENSE -> TransactionType.EXPENSE
            com.uramos.minicash.data.local.entities.BudgetItemType.REIMBURSEMENT, com.uramos.minicash.data.local.entities.BudgetItemType.INCOME -> TransactionType.INCOME
        }
        val amountCents = item.amount
        val amountDouble = amountCents / 100.0

        _uiState.update { state ->
            state.copy(
                type = txType,
                concept = item.concept.trim(),
                note = "",
                amountExpression = if (amountDouble > 0) amountDouble.toString() else "",
                calculatedAmount = amountDouble,
                selectedDate = System.currentTimeMillis()
            )
        }
    }

    fun prefillForCredit(amountCents: Long, conceptStr: String, notesStr: String) {
        val amountDouble = amountCents / 100.0
        _uiState.update { state ->
            state.copy(
                type = TransactionType.EXPENSE,
                concept = conceptStr.trim(),
                note = "",
                amountExpression = if (amountDouble > 0) amountDouble.toString() else "",
                calculatedAmount = amountDouble,
                selectedDate = System.currentTimeMillis()
            )
        }
    }

    fun addCategory(name: String, type: TransactionType, colorHex: String, iconName: String) {
        viewModelScope.launch {
            val newCat = com.uramos.minicash.data.local.entities.CategoryEntity(
                name = name,
                type = type,
                colorHex = colorHex,
                iconName = iconName
            )
            val id = categoryDao.insertCategory(newCat)
            _uiState.update { it.copy(selectedCategoryId = id) }
        }
    }

    fun onDateChange(dateMillis: Long) {
        _uiState.update { it.copy(selectedDate = dateMillis) }
    }

    fun saveTransaction(onSuccess: () -> Unit) {
        val state = _uiState.value
        val amountDouble = if (state.calculatedAmount > 0.0) {
            state.calculatedAmount
        } else {
            evaluateMathExpression(state.amountExpression).takeIf { it > 0.0 }
                ?: state.amountExpression.toDoubleOrNull()
                ?: return
        }
        val amountCents = (amountDouble * 100.0).roundToLong()
        if (amountCents <= 0L) return

        viewModelScope.launch {
            if (state.type == TransactionType.TRANSFER) {
                val sourceId = state.selectedAccountId ?: accounts.value.firstOrNull()?.id ?: return@launch
                val destId = state.selectedDestinationAccountId ?: return@launch
                if (sourceId == destId) return@launch

                val conceptStr = state.concept.ifBlank { "Transferencia" }
                val finalNote = if (state.note.isNotBlank()) "$conceptStr • ${state.note}" else conceptStr

                transactionDao.insertTransaction(
                    TransactionEntity(
                        sourceAccountId = sourceId,
                        destinationAccountId = destId,
                        categoryId = null,
                        amount = amountCents,
                        type = TransactionType.TRANSFER,
                        date = state.selectedDate,
                        note = finalNote
                    )
                )
                accountDao.updateBalance(sourceId, -amountCents)
                accountDao.updateBalance(destId, amountCents)
                resetForm()
                onSuccess()
            } else {
                val sourceId = state.selectedAccountId ?: accounts.value.firstOrNull()?.id ?: return@launch
                val categoryId = state.selectedCategoryId ?: return@launch
                if (state.concept.isBlank()) return@launch

                val finalNote = if (state.note.isNotBlank()) "${state.concept.trim()} • ${state.note.trim()}" else state.concept.trim()

                transactionDao.insertTransaction(
                    TransactionEntity(
                        sourceAccountId = sourceId,
                        categoryId = categoryId,
                        amount = amountCents,
                        type = state.type,
                        date = state.selectedDate,
                        note = finalNote
                    )
                )

                val delta = if (state.type == TransactionType.INCOME) amountCents else -amountCents
                accountDao.updateBalance(sourceId, delta)
                resetForm()
                onSuccess()
            }
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
}
