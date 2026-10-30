package com.uramos.minicash.data.local.entities

enum class TransactionType(val displayName: String) {
    EXPENSE("Gasto"),
    INCOME("Ingreso"),
    TRANSFER("Transferencia")
}

enum class AccountType(val displayName: String) {
    CASH("Efectivo"),
    DEBIT("Tarjeta de débito"),
    CREDIT_CARD("Tarjeta de crédito"),
    SAVINGS("Ahorro / Inversión")
}

enum class BudgetItemType(val displayName: String) {
    EXPENSE("Gasto"),
    INCOME("Ingreso"),
    REIMBURSEMENT("Reembolso")
}