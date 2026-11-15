package com.uramos.minicash

import com.uramos.minicash.data.local.entities.TransactionEntity
import com.uramos.minicash.data.local.entities.TransactionType
import com.uramos.minicash.data.util.transactionEffect
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionBalanceTest {

    private fun tx(type: TransactionType, amount: Long, source: Long = 1L, dest: Long? = null) =
        TransactionEntity(type = type, amount = amount, date = 0L, sourceAccountId = source, destinationAccountId = dest)

    @Test
    fun incomeAddsToTheSourceAccount() {
        assertEquals(listOf(1L to 5_000L), transactionEffect(tx(TransactionType.INCOME, 5_000)))
    }

    @Test
    fun expenseSubtractsFromTheSourceAccount() {
        assertEquals(listOf(2L to -1_250L), transactionEffect(tx(TransactionType.EXPENSE, 1_250, source = 2L)))
    }

    @Test
    fun transferMovesMoneyBetweenAccounts() {
        assertEquals(
            listOf(1L to -3_000L, 4L to 3_000L),
            transactionEffect(tx(TransactionType.TRANSFER, 3_000, source = 1L, dest = 4L))
        )
    }

    @Test
    fun transferWithoutDestinationOnlyTouchesTheSource() {
        assertEquals(listOf(1L to -3_000L), transactionEffect(tx(TransactionType.TRANSFER, 3_000)))
    }

    @Test
    fun removingAndRestoringCancelOut() {
        val effect = transactionEffect(tx(TransactionType.EXPENSE, 700))
        val netPerAccount = (effect.map { (id, delta) -> id to -delta } + effect).groupBy({ it.first }, { it.second }).mapValues { it.value.sum() }
        assertEquals(mapOf(1L to 0L), netPerAccount)
    }
}
