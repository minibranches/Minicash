package com.uramos.minicash

import com.uramos.minicash.data.local.entities.CategoryEntity
import com.uramos.minicash.data.local.entities.TransactionEntity
import com.uramos.minicash.data.local.entities.TransactionType
import com.uramos.minicash.ui.screens.reports.MonthlyTotals
import com.uramos.minicash.ui.screens.reports.OTHER_CATEGORIES_LABEL
import com.uramos.minicash.ui.screens.reports.UNCATEGORIZED_LABEL
import com.uramos.minicash.ui.screens.reports.bestMonth
import com.uramos.minicash.ui.screens.reports.buildCategorySpend
import com.uramos.minicash.ui.screens.reports.cumulativeBalance
import com.uramos.minicash.ui.screens.reports.savingsRate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReportsMathTest {

    private fun category(id: Long, name: String) =
        CategoryEntity(id = id, name = name, type = TransactionType.EXPENSE, colorHex = "#112233")

    private fun expense(amount: Long, categoryId: Long?, type: TransactionType = TransactionType.EXPENSE) =
        TransactionEntity(type = type, amount = amount, date = 0L, sourceAccountId = 1L, categoryId = categoryId)

    private fun month(m: Int, income: Long, expense: Long) = MonthlyTotals(YearMonth.of(2026, m), income, expense)

    @Test
    fun categoriesAreSortedAndIgnoreIncomeAndTransfers() {
        val categories = mapOf(1L to category(1, "Súper"), 2L to category(2, "Renta"))
        val result = buildCategorySpend(
            listOf(
                expense(3_000, 1), expense(7_000, 2), expense(1_000, 1),
                expense(99_000, 1, TransactionType.INCOME), expense(50_000, 2, TransactionType.TRANSFER)
            ),
            categories
        )
        assertEquals(listOf("Renta", "Súper"), result.map { it.name })
        assertEquals(7_000L, result.first().total)
        assertEquals(0.636f, result.first().share, 0.001f)
    }

    @Test
    fun smallCategoriesFoldIntoOthers() {
        val categories = (1L..8L).associateWith { category(it, "Cat $it") }
        val txs = (1L..8L).map { expense(it * 100, it) }
        val result = buildCategorySpend(txs, categories, top = 3)
        assertEquals(4, result.size)
        assertEquals(OTHER_CATEGORIES_LABEL, result.last().name)
        assertEquals(1.0f, result.sumOf { it.share.toDouble() }.toFloat(), 0.001f)
    }

    @Test
    fun missingCategoryIsLabelledUncategorized() {
        val result = buildCategorySpend(listOf(expense(500, null), expense(500, 99)), emptyMap())
        assertEquals(1, result.size)
        assertEquals(UNCATEGORIZED_LABEL, result.single().name)
        assertEquals(1_000L, result.single().total)
    }

    @Test
    fun noExpensesGivesEmptyList() {
        assertEquals(emptyList<Any>(), buildCategorySpend(emptyList(), emptyMap()))
    }

    @Test
    fun cumulativeBalanceRunsAcrossMonths() {
        val months = listOf(month(1, 1_000, 400), month(2, 200, 900), month(3, 0, 0))
        assertEquals(listOf(600L, -100L, -100L), cumulativeBalance(months))
    }

    @Test
    fun savingsRateIsShareOfIncomeNotSpent() {
        assertEquals(0.25f, savingsRate(listOf(month(1, 1_000, 500), month(2, 1_000, 1_000)))!!, 0.0001f)
        assertEquals(-0.5f, savingsRate(listOf(month(1, 1_000, 1_500)))!!, 0.0001f)
        assertNull(savingsRate(listOf(month(1, 0, 300))))
    }

    @Test
    fun bestMonthSkipsEmptyMonths() {
        val months = listOf(month(1, 0, 0), month(2, 500, 700), month(3, 900, 100), month(4, 0, 0))
        assertEquals(YearMonth.of(2026, 3), bestMonth(months)?.month)
        assertNull(bestMonth(listOf(month(1, 0, 0))))
    }
}
