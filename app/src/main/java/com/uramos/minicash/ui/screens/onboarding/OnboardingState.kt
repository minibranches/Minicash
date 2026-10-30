package com.uramos.minicash.ui.screens.onboarding

import com.uramos.minicash.data.local.entities.AccountType

data class OnboardingState(
    val userName: String = "",
    val accountName: String = "",
    val accountType: AccountType = AccountType.CASH,
    val accountBalance: String = "",
    val selectedExpenseCategories: Set<String> = setOf("Comestibles", "Transporte", "Comer fuera", "Servicios"),
    val selectedIncomeCategories: Set<String> = setOf("Nómina/Sueldo", "Reembolsos")
)