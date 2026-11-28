package com.uramos.minicash.data.local.entities

import androidx.annotation.StringRes
import com.uramos.minicash.R
import com.uramos.minicash.ui.utils.AppStrings

enum class TransactionType(@StringRes val labelRes: Int) {
    EXPENSE(R.string.tx_type_expense),
    INCOME(R.string.tx_type_income),
    TRANSFER(R.string.tx_type_transfer);

    /** Nombre en el idioma de la app. */
    val displayName: String get() = AppStrings.get(labelRes)
}

enum class AccountType(@StringRes val labelRes: Int) {
    CASH(R.string.account_type_cash),
    DEBIT(R.string.account_type_debit),
    CREDIT_CARD(R.string.account_type_credit),
    SAVINGS(R.string.account_type_savings);

    val displayName: String get() = AppStrings.get(labelRes)
}

enum class BudgetItemType(@StringRes val labelRes: Int) {
    EXPENSE(R.string.budget_type_expense),
    INCOME(R.string.budget_type_income),
    REIMBURSEMENT(R.string.budget_type_reimbursement);

    val displayName: String get() = AppStrings.get(labelRes)
}
