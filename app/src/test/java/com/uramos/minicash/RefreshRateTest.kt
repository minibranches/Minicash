package com.uramos.minicash

import com.uramos.minicash.ui.utils.availableRefreshLimits
import com.uramos.minicash.ui.utils.coerceRefreshLimit
import com.uramos.minicash.ui.utils.refreshLimitLabel
import org.junit.Assert.assertEquals
import org.junit.Test

class RefreshRateTest {

    @Test
    fun pantallaDe120HzOfreceTodosLosLimites() {
        assertEquals(listOf(0, 30, 60, 120), availableRefreshLimits(120f))
        assertEquals(listOf(0, 30, 60, 120), availableRefreshLimits(120.0001f))
    }

    @Test
    fun pantallaDe60HzNoOfreceLos120() {
        assertEquals(listOf(0, 30, 60), availableRefreshLimits(60f))
        assertEquals(listOf(0, 30, 60), availableRefreshLimits(90f))
    }

    @Test
    fun unLimiteQueLaPantallaNoTieneVuelveAAutomatico() {
        val sin120 = availableRefreshLimits(60f)
        assertEquals(0, coerceRefreshLimit(120, sin120))
        assertEquals(60, coerceRefreshLimit(60, sin120))
        assertEquals(0, coerceRefreshLimit(0, sin120))
    }

    @Test
    fun etiquetas() {
        assertEquals("Automático", refreshLimitLabel(0))
        assertEquals("30 Hz", refreshLimitLabel(30))
        assertEquals("120 Hz", refreshLimitLabel(120))
    }
}
