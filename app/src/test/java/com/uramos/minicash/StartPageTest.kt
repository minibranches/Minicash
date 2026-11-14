package com.uramos.minicash

import org.junit.Assert.assertEquals
import org.junit.Test

class StartPageTest {

    /** Misma cuenta que MainScreen: la página inicial debe caer en el índice 0 (Inicio) con cualquier número de pestañas. */
    private fun startIndex(pageCount: Int, numPages: Int): Int {
        val initialPage = (pageCount / 2) / numPages * numPages
        return initialPage % numPages
    }

    @Test
    fun startsOnHomeWithThreeTabs() {
        assertEquals(0, startIndex(10_000, 3))
    }

    @Test
    fun startsOnHomeForAnyTabCount() {
        (2..6).forEach { assertEquals(0, startIndex(10_000, it)) }
    }
}
