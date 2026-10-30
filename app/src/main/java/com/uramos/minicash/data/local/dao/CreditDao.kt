package com.uramos.minicash.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.uramos.minicash.data.local.entities.CreditEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditDao {
    @Query("SELECT * FROM credits ORDER BY purchaseDate ASC, id ASC")
    fun getAllCredits(): Flow<List<CreditEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCredit(credit: CreditEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCredits(credits: List<CreditEntity>)

    @Query("SELECT * FROM credits")
    suspend fun getAllCreditsList(): List<CreditEntity>

    @Query("UPDATE credits SET institution = :name WHERE institutionId = :institutionId")
    suspend fun renameInstitution(institutionId: Long, name: String)

    @Query("DELETE FROM credits")
    suspend fun clearAllCredits()

    @Update
    suspend fun updateCredit(credit: CreditEntity)

    @Delete
    suspend fun deleteCredit(credit: CreditEntity)
}