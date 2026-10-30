package com.uramos.minicash.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.uramos.minicash.data.local.entities.BudgetItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budget_items WHERE fortnightKey = :fortnightKey ORDER BY displayOrder ASC, id ASC")
    fun getItemsByFortnight(fortnightKey: String): Flow<List<BudgetItemEntity>>

    @Query("SELECT * FROM budget_items WHERE fortnightKey = :fortnightKey ORDER BY displayOrder ASC, id ASC")
    suspend fun getItemsByFortnightList(fortnightKey: String): List<BudgetItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: BudgetItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<BudgetItemEntity>)

    @Query("SELECT * FROM budget_items")
    suspend fun getAllBudgetItemsList(): List<BudgetItemEntity>

    @Query("DELETE FROM budget_items")
    suspend fun clearAllBudgetItems()

    @Update
    suspend fun updateItem(item: BudgetItemEntity)

    @Update
    suspend fun updateItems(items: List<BudgetItemEntity>)

    @Delete
    suspend fun deleteItem(item: BudgetItemEntity)
}