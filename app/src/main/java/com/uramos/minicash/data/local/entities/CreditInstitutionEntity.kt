package com.uramos.minicash.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credit_institutions")
data class CreditInstitutionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorHex: String = "#1A73E8",
    /** Día del mes (1 a 31) de pago que heredan sus compras a meses; null si no se registró. */
    val paymentDay: Int? = null,
    /** Días de anticipación del aviso de pago (0 = el mismo día); null si no hay aviso. */
    val reminderDaysBefore: Int? = null
)
