package com.uramos.minicash.data.local.backup

import com.uramos.minicash.data.local.entities.AccountEntity
import com.uramos.minicash.data.local.entities.BudgetItemEntity
import com.uramos.minicash.data.local.entities.CategoryEntity
import com.uramos.minicash.data.local.entities.CreditEntity
import com.uramos.minicash.data.local.entities.CreditInstitutionEntity
import com.uramos.minicash.data.local.entities.TransactionEntity
import com.uramos.minicash.data.preferences.PreferencesSnapshot

const val BACKUP_FORMAT_VERSION = 4

/**
 * v1: cuentas, categorías, transacciones, presupuesto y créditos.
 * v2: agrega catálogo de instituciones y ajustes de la app.
 * v3: los créditos referencian la institución por id (institutionId); v1/v2 se enlazan por nombre al restaurar.
 * Los campos nuevos son opcionales para poder leer archivos v1.
 */
data class MinicashBackup(
    val version: Int = BACKUP_FORMAT_VERSION,
    val exportDate: Long = System.currentTimeMillis(),
    val accounts: List<AccountEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList(),
    val budgetItems: List<BudgetItemEntity> = emptyList(),
    val credits: List<CreditEntity> = emptyList(),
    val creditInstitutions: List<CreditInstitutionEntity>? = null,
    val preferences: PreferencesSnapshot? = null
)
