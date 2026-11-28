package com.uramos.minicash.ui.screens.onboarding

import androidx.annotation.StringRes
import com.uramos.minicash.R
import com.uramos.minicash.data.local.entities.AccountType

/**
 * Categorías que se ofrecen al empezar. Se eligen por clave estable y su nombre se toma del idioma al guardarlas, así
 * cambiar de idioma en el onboarding no pierde la selección.
 */
object StarterCategories {
    val expense: List<Pair<String, Int>> = listOf(
        "groceries" to R.string.starter_groceries,
        "transport" to R.string.starter_transport,
        "dining" to R.string.starter_dining,
        "rent" to R.string.starter_rent,
        "utilities" to R.string.starter_utilities,
        "shopping" to R.string.starter_shopping,
        "health" to R.string.starter_health,
        "fun" to R.string.starter_fun
    )
    val income: List<Pair<String, Int>> = listOf(
        "salary" to R.string.starter_salary,
        "investments" to R.string.starter_investments,
        "refunds" to R.string.starter_refunds,
        "sales" to R.string.starter_sales
    )

    @StringRes
    fun nameRes(key: String): Int? = (expense + income).firstOrNull { it.first == key }?.second
}

data class OnboardingState(
    val userName: String = "",
    val accountName: String = "",
    val accountType: AccountType = AccountType.CASH,
    val accountBalance: String = "",
    val selectedExpenseCategories: Set<String> = setOf("groceries", "transport", "dining", "utilities"),
    val selectedIncomeCategories: Set<String> = setOf("salary", "refunds")
)
