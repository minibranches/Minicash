package com.uramos.minicash

import com.uramos.minicash.data.local.entities.TransactionEntity
import com.uramos.minicash.data.local.entities.TransactionType
import com.uramos.minicash.ui.screens.account.accountMonthlyNet
import com.uramos.minicash.ui.screens.account.transactionsOfAccount
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class AccountDetailMathTest {
    private val zone = ZoneId.of("America/Mexico_City")
    private fun millis(date: LocalDate) = date.atTime(12, 0).atZone(zone).toInstant().toEpochMilli()

    private fun tx(
        id: Long, type: TransactionType, amount: Long, date: LocalDate, source: Long, destination: Long? = null
    ) = TransactionEntity(
        id = id, type = type, amount = amount, date = millis(date),
        sourceAccountId = source, destinationAccountId = destination
    )

    @Test
    fun accountTransactionsIncludeTransfersReceived() {
        val list = listOf(
            tx(1, TransactionType.EXPENSE, 100, LocalDate.of(2026, 11, 3), source = 1),
            tx(2, TransactionType.TRANSFER, 500, LocalDate.of(2026, 11, 4), source = 2, destination = 1),
            tx(3, TransactionType.INCOME, 900, LocalDate.of(2026, 11, 5), source = 2)
        )
        assertEquals(listOf(1L, 2L), transactionsOfAccount(list, 1).map { it.id })
    }

    @Test
    fun monthlyNetCountsEffectOnTheAccountAndFillsEmptyMonths() {
        val list = listOf(
            tx(1, TransactionType.INCOME, 1000, LocalDate.of(2026, 11, 2), source = 1),
            tx(2, TransactionType.EXPENSE, 300, LocalDate.of(2026, 11, 9), source = 1),
            tx(3, TransactionType.TRANSFER, 200, LocalDate.of(2026, 9, 20), source = 2, destination = 1),
            tx(4, TransactionType.TRANSFER, 50, LocalDate.of(2026, 9, 21), source = 1, destination = 2),
            tx(5, TransactionType.EXPENSE, 999, LocalDate.of(2026, 11, 9), source = 2)
        )
        val net = accountMonthlyNet(list, accountId = 1, months = 3, now = YearMonth.of(2026, 11), zone = zone)

        assertEquals(listOf(YearMonth.of(2026, 9), YearMonth.of(2026, 10), YearMonth.of(2026, 11)), net.map { it.month })
        assertEquals(listOf(150L, 0L, 700L), net.map { it.net })
    }
}
