package com.uramos.minicash

import com.uramos.minicash.data.local.entities.TransactionEntity
import com.uramos.minicash.data.local.entities.TransactionType
import com.uramos.minicash.widget.monthExpenseOf
import com.uramos.minicash.widget.widgetAmountText
import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetDataTest {

    private fun tx(type: TransactionType, amount: Long) =
        TransactionEntity(type = type, amount = amount, date = 0L, sourceAccountId = 1L)

    @Test
    fun monthExpenseIgnoresIncomeAndTransfers() {
        val list = listOf(
            tx(TransactionType.EXPENSE, 1_500),
            tx(TransactionType.EXPENSE, 2_500),
            tx(TransactionType.INCOME, 90_000),
            tx(TransactionType.TRANSFER, 40_000)
        )
        assertEquals(4_000L, monthExpenseOf(list))
    }

    @Test
    fun noTransactionsMeansZeroExpense() {
        assertEquals(0L, monthExpenseOf(emptyList()))
    }

    @Test
    fun hiddenAmountsAreMasked() {
        assertEquals("••••", widgetAmountText(12_345, hidden = true) { "$123.45" })
    }

    @Test
    fun visibleAmountsUseTheFormatter() {
        assertEquals("$123.45", widgetAmountText(12_345, hidden = false) { "$123.45" })
    }
}
