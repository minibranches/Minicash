package com.uramos.minicash.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: AccountType,
    val balance: Long, // Guardado en centavos ($100.50 -> 10050)
    val colorHex: String = "#1A73E8",
    val iconName: String = "account_balance_wallet",
    val isVisibleInHome: Boolean = true,
    val displayOrder: Int = 0
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: TransactionType, // EXPENSE o INCOME
    val icon: String = "default",
    val iconName: String = "category",
    val colorHex: String = "#1A73E8",
    val isActive: Boolean = true
)