package com.uramos.minicash

import com.uramos.minicash.data.local.entities.BudgetItemEntity
import com.uramos.minicash.data.local.entities.BudgetItemType
import com.uramos.minicash.ui.screens.budget.isDuplicateInTarget
import com.uramos.minicash.ui.screens.budget.normalizeConcept
import com.uramos.minicash.ui.screens.budget.shiftYearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FortnightCopyTest {

    private fun item(concept: String, type: BudgetItemType = BudgetItemType.EXPENSE) = BudgetItemEntity(
        type = type, concept = concept, amount = 100, responsible = "Yo", fortnightKey = "2026-11-Q1"
    )

    @Test
    fun conceptIgnoresCaseAndExtraSpaces() {
        assertEquals("renta casa", normalizeConcept("  Renta   CASA "))
    }

    @Test
    fun sameConceptAndTypeIsDuplicate() {
        assertTrue(isDuplicateInTarget(item("Renta"), listOf(item("  renta "))))
    }

    @Test
    fun sameConceptWithDifferentTypeIsNotDuplicate() {
        assertFalse(isDuplicateInTarget(item("Bono"), listOf(item("Bono", BudgetItemType.INCOME))))
    }

    @Test
    fun differentConceptIsNotDuplicate() {
        assertFalse(isDuplicateInTarget(item("Luz"), listOf(item("Agua"))))
    }

    @Test
    fun shiftCrossesYearBoundaries() {
        assertEquals(2025 to 12, shiftYearMonth(2026, 1, -1))
        assertEquals(2027 to 1, shiftYearMonth(2026, 12, 1))
        assertEquals(2026 to 6, shiftYearMonth(2026, 6, 0))
    }
}
