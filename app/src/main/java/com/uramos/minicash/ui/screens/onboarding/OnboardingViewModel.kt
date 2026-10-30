package com.uramos.minicash.ui.screens.onboarding

import com.uramos.minicash.ui.utils.toCents
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uramos.minicash.data.local.dao.AccountDao
import com.uramos.minicash.data.local.dao.CategoryDao
import com.uramos.minicash.data.local.entities.AccountEntity
import com.uramos.minicash.data.local.entities.AccountType
import com.uramos.minicash.data.local.entities.CategoryEntity
import com.uramos.minicash.data.local.entities.TransactionType
import com.uramos.minicash.data.preferences.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingState())
    val uiState: StateFlow<OnboardingState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.update { it.copy(userName = name) }
    }

    fun onAccountNameChange(name: String) {
        _uiState.update { it.copy(accountName = name) }
    }

    fun onAccountTypeChange(type: AccountType) {
        _uiState.update { it.copy(accountType = type) }
    }

    fun onKeypadPress(key: String) {
        _uiState.update { state ->
            val current = state.accountBalance
            val newBalance = when (key) {
                "⌫" -> if (current.isNotEmpty()) current.dropLast(1) else ""
                "C" -> ""
                "." -> {
                    if (current.contains(".")) current
                    else if (current.isEmpty()) "0."
                    else "$current."
                }
                else -> { // Digits "0".."9"
                    val dotIndex = current.indexOf('.')
                    if (dotIndex != -1 && current.length > dotIndex + 2) {
                        current // Máximo 2 decimales
                    } else if (current == "0") {
                        key
                    } else {
                        current + key
                    }
                }
            }
            state.copy(accountBalance = newBalance)
        }
    }

    fun toggleExpenseCategory(category: String) {
        _uiState.update { state ->
            val updated = state.selectedExpenseCategories.toMutableSet()
            if (updated.contains(category)) updated.remove(category) else updated.add(category)
            state.copy(selectedExpenseCategories = updated)
        }
    }

    fun toggleIncomeCategory(category: String) {
        _uiState.update { state ->
            val updated = state.selectedIncomeCategories.toMutableSet()
            if (updated.contains(category)) updated.remove(category) else updated.add(category)
            state.copy(selectedIncomeCategories = updated)
        }
    }

    fun finishOnboarding(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value

            // 1. Guardar la cuenta inicial (convertido a centavos) con propiedades obligatorias
            val balanceDouble = state.accountBalance.toDoubleOrNull() ?: 0.0
            val balanceCents = balanceDouble.toCents()
            accountDao.insertAccount(
                AccountEntity(
                    name = state.accountName.trim().ifBlank { "Efectivo" },
                    type = state.accountType,
                    balance = balanceCents,
                    colorHex = "#1A73E8",
                    iconName = "account_balance_wallet",
                    isVisibleInHome = true,
                    displayOrder = 0
                )
            )

            // 2. Guardar categorías de gasto seleccionadas (exclusivamente si el usuario eligió alguna)
            if (state.selectedExpenseCategories.isNotEmpty()) {
                val expenseEntities = state.selectedExpenseCategories.map { catName ->
                    CategoryEntity(
                        name = catName,
                        type = TransactionType.EXPENSE,
                        colorHex = "#EF5350",
                        iconName = "category"
                    )
                }
                categoryDao.insertCategories(expenseEntities)
            }

            // 3. Guardar categorías de ingreso seleccionadas (exclusivamente si el usuario eligió alguna)
            if (state.selectedIncomeCategories.isNotEmpty()) {
                val incomeEntities = state.selectedIncomeCategories.map { catName ->
                    CategoryEntity(
                        name = catName,
                        type = TransactionType.INCOME,
                        colorHex = "#26A69A",
                        iconName = "category"
                    )
                }
                categoryDao.insertCategories(incomeEntities)
            }

            // 4. Guardar preferencia de sesión terminada
            userPreferences.saveOnboardingState(
                completed = true,
                name = state.userName.trim().ifBlank { "Usuario" }
            )

            onSuccess()
        }
    }
}