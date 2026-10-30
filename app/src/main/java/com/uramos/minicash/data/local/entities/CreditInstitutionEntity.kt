package com.uramos.minicash.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credit_institutions")
data class CreditInstitutionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorHex: String = "#1A73E8"
)
