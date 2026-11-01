package com.uramos.minicash.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "budget_items")
data class BudgetItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: BudgetItemType,
    val concept: String,
    val amount: Long,
    val dueDate: Long? = null,
    val responsible: String,
    val fortnightKey: String,
    val isPaid: Boolean = false,
    val displayOrder: Int = 0
)

/**
 * [institutionId] es la referencia real al catálogo (SET_NULL si se borra la institución).
 * [institution] es el nombre en caché, para mostrar el crédito aun sin institución; se mantiene
 * sincronizado al renombrar la institución.
 */
@Entity(
    tableName = "credits",
    foreignKeys = [
        ForeignKey(
            entity = CreditInstitutionEntity::class,
            parentColumns = ["id"],
            childColumns = ["institutionId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("institutionId")]
)
data class CreditEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val article: String,
    val institution: String,
    val responsible: String,
    val purchaseDate: Long,
    val totalInstallments: Int,
    val paidInstallments: Int = 0,
    val monthlyAmount: Long,
    val originalPrice: Long,
    val totalAmount: Long,
    val institutionId: Long? = null,
    /** Día del mes (1 a 31) en que se paga la mensualidad; solo cuenta si [paymentOverride] está activo. */
    val paymentDay: Int? = null,
    /** Días de anticipación del aviso de pago (0 = el mismo día); solo cuenta si [paymentOverride] está activo. */
    val reminderDaysBefore: Int? = null,
    /** Si es falso la compra hereda el día de pago y el aviso de su institución. */
    val paymentOverride: Boolean = false
)
