package com.uramos.minicash

import com.uramos.minicash.ui.components.evaluateMathExpression
import com.uramos.minicash.ui.components.processKeypadPress
import com.uramos.minicash.ui.utils.toCents
import com.uramos.minicash.ui.utils.toCompactCurrency
import org.junit.Assert.assertEquals
import org.junit.Test

class KeypadTest {

    private fun press(vararg keys: String): String =
        keys.fold("") { expr, key -> processKeypadPress(expr, key) }

    @Test
    fun decimalOnEmptyStartsWithZero_withoutLeadingSpace() {
        assertEquals("0.", press("."))
    }

    @Test
    fun backspaceAfterPointClearsEverything() {
        assertEquals("0", press(".", "⌫"))
        assertEquals("", press(".", "⌫", "⌫"))
    }

    @Test
    fun leadingZeroIsReplaced() {
        assertEquals("5", press("0", "5"))
        assertEquals("12 + 3", press("1", "2", "+", "0", "3"))
    }

    @Test
    fun onlyOnePointPerNumber() {
        assertEquals("1.5", press("1", ".", "5", "."))
    }

    @Test
    fun limitsTwoDecimals() {
        assertEquals("1.25", press("1", ".", "2", "5", "9"))
    }

    @Test
    fun operatorDropsTrailingPoint() {
        assertEquals("5 + ", press("5", ".", "+"))
    }

    @Test
    fun backspaceOverOperatorRemovesIt() {
        assertEquals("5", press("5", "+", "⌫"))
    }

    @Test
    fun decimalAfterOperator() {
        assertEquals("5 + 0.", press("5", "+", "."))
    }

    @Test
    fun evaluates() {
        assertEquals(7.5, evaluateMathExpression("5 + 2.5"), 0.0001)
        assertEquals(5.0, evaluateMathExpression("5."), 0.0001)
    }

    @Test
    fun centsAreRounded() {
        assertEquals(1999L, 19.99.toCents())
        assertEquals(1005L, 10.05.toCents())
    }

    @Test
    fun compactCurrency() {
        assertEquals("\$950", 95000L.toCompactCurrency())
        assertEquals("\$10.5k", 1050000L.toCompactCurrency())
        assertEquals("\$10k", 1000000L.toCompactCurrency())
        assertEquals("\$1.2M", 120000000L.toCompactCurrency())
        assertEquals("\$5.6k", (-560900L).toCompactCurrency())
    }
}
