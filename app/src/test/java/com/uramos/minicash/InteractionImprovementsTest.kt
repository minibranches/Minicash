package com.uramos.minicash

import com.uramos.minicash.data.local.entities.BudgetItemEntity
import com.uramos.minicash.data.local.entities.BudgetItemType
import com.uramos.minicash.ui.screens.budget.filterBudgetItems
import com.uramos.minicash.ui.screens.home.chartMonthForYear
import com.uramos.minicash.ui.screens.home.shiftedChartMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.YearMonth

class InteractionImprovementsTest {

    private val now = YearMonth.of(2026, 11)

    @Test
    fun alRetrocederSeVeElMesAnterior() {
        assertEquals(YearMonth.of(2026, 10), shiftedChartMonth(null, -1, now))
        assertEquals(YearMonth.of(2026, 8), shiftedChartMonth(YearMonth.of(2026, 9), -1, now))
        assertEquals(YearMonth.of(2025, 12), shiftedChartMonth(YearMonth.of(2026, 1), -1, now))
    }

    @Test
    fun noSeVaAlFuturoYLlegarAlMesEnCursoVuelveAAutomatico() {
        assertNull(shiftedChartMonth(YearMonth.of(2026, 10), 1, now))
        assertNull(shiftedChartMonth(null, 1, now))
        assertNull(shiftedChartMonth(YearMonth.of(2026, 11), 3, now))
    }

    @Test
    fun alElegirAnioSeConservaElMes() {
        assertEquals(YearMonth.of(2025, 11), chartMonthForYear(null, 2025, now))
        assertEquals(YearMonth.of(2024, 3), chartMonthForYear(YearMonth.of(2026, 3), 2024, now))
    }

    @Test
    fun elegirElAnioEnCursoConUnMesFuturoVuelveAAutomatico() {
        assertNull(chartMonthForYear(YearMonth.of(2025, 12), 2026, now))
        assertEquals(YearMonth.of(2026, 5), chartMonthForYear(YearMonth.of(2025, 5), 2026, now))
    }

    private fun item(id: Long, type: BudgetItemType) =
        BudgetItemEntity(id = id, concept = "Partida $id", amount = 1000L, type = type, responsible = "", fortnightKey = "2026-11-Q1")

    @Test
    fun filtrarPorTipoConservaElOrdenYSinFiltroDevuelveTodo() {
        val items = listOf(
            item(1, BudgetItemType.EXPENSE), item(2, BudgetItemType.INCOME),
            item(3, BudgetItemType.EXPENSE), item(4, BudgetItemType.REIMBURSEMENT)
        )
        assertEquals(listOf(1L, 3L), filterBudgetItems(items, BudgetItemType.EXPENSE).map { it.id })
        assertEquals(listOf(2L), filterBudgetItems(items, BudgetItemType.INCOME).map { it.id })
        assertEquals(listOf(4L), filterBudgetItems(items, BudgetItemType.REIMBURSEMENT).map { it.id })
        assertEquals(listOf(1L, 2L, 3L, 4L), filterBudgetItems(items, null).map { it.id })
    }
}
