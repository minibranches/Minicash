package com.uramos.minicash

import com.uramos.minicash.data.local.entities.AccountEntity
import com.uramos.minicash.data.local.entities.AccountType
import com.uramos.minicash.data.local.entities.TransactionType
import com.uramos.minicash.ui.transactions.defaultAccountFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DefaultAccountTest {

    private fun account(id: Long, balance: Long) =
        AccountEntity(id = id, name = "Cuenta $id", type = AccountType.DEBIT, balance = balance)

    private val accounts = listOf(account(1, 5_000), account(2, 90_000), account(3, 90_000), account(4, -2_000))

    @Test
    fun transferPicksHighestBalance() {
        assertEquals(2L, defaultAccountFor(TransactionType.TRANSFER, accounts)?.id)
    }

    @Test
    fun tiedBalancesKeepTheFirstOne() {
        assertEquals(2L, defaultAccountFor(TransactionType.TRANSFER, listOf(account(2, 100), account(3, 100)))?.id)
    }

    @Test
    fun expenseAndIncomeKeepTheFirstAccount() {
        assertEquals(1L, defaultAccountFor(TransactionType.EXPENSE, accounts)?.id)
        assertEquals(1L, defaultAccountFor(TransactionType.INCOME, accounts)?.id)
    }

    @Test
    fun noAccountsGivesNull() {
        assertNull(defaultAccountFor(TransactionType.TRANSFER, emptyList()))
    }
}
