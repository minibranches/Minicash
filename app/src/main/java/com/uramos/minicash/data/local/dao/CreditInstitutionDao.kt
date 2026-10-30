package com.uramos.minicash.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.uramos.minicash.data.local.entities.CreditInstitutionEntity
import kotlinx.coroutines.flow.Flow

import androidx.room.Update

@Dao
interface CreditInstitutionDao {
    @Query("SELECT * FROM credit_institutions ORDER BY name ASC")
    fun getAllInstitutions(): Flow<List<CreditInstitutionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstitution(institution: CreditInstitutionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstitutions(institutions: List<CreditInstitutionEntity>)

    @Query("SELECT * FROM credit_institutions")
    suspend fun getAllInstitutionsList(): List<CreditInstitutionEntity>

    @Query("SELECT * FROM credit_institutions WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun findByName(name: String): CreditInstitutionEntity?

    @Query("DELETE FROM credit_institutions")
    suspend fun clearAllInstitutions()

    @Update
    suspend fun updateInstitution(institution: CreditInstitutionEntity)

    @Delete
    suspend fun deleteInstitution(institution: CreditInstitutionEntity)
}
