package com.uramos.minicash.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.uramos.minicash.data.local.entities.AccountEntity
import kotlinx.coroutines.flow.Flow

import androidx.room.Delete

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE isVisibleInHome = 1 ORDER BY displayOrder ASC, id ASC")
    fun getAllActiveAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts ORDER BY displayOrder ASC, id ASC")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Long): AccountEntity?

    @Query("SELECT SUM(balance) FROM accounts")
    fun getTotalBalance(): Flow<Long?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>)

    @Query("SELECT * FROM accounts")
    suspend fun getAllAccountsList(): List<AccountEntity>

    @Query("DELETE FROM accounts")
    suspend fun clearAllAccounts()

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Update
    suspend fun updateAccounts(accounts: List<AccountEntity>)

    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    @Query("UPDATE accounts SET balance = balance + :amount WHERE id = :accountId")
    suspend fun updateBalance(accountId: Long, amount: Long): Int
}