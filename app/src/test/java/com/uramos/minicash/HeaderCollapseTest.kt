package com.uramos.minicash

import com.uramos.minicash.data.local.entities.TransactionType
import com.uramos.minicash.ui.components.filterCountLabel
import com.uramos.minicash.ui.components.headerOffsetAfterScroll
import com.uramos.minicash.ui.components.headerSettleTarget
import com.uramos.minicash.ui.screens.transactions.activeFilterCount
import org.junit.Assert.assertEquals
import org.junit.Test

class HeaderCollapseTest {

    @Test
    fun alBajarPorLaListaElEncabezadoSeEscondeSinPasarseDelAlto() {
        assertEquals(-30f, headerOffsetAfterScroll(0f, -30f, 100f), 0.001f)
        assertEquals(-100f, headerOffsetAfterScroll(-90f, -40f, 100f), 0.001f)
    }

    @Test
    fun alSubirPorLaListaElEncabezadoReapareceDeInmediatoSinPasarDeCero() {
        assertEquals(-40f, headerOffsetAfterScroll(-100f, 60f, 100f), 0.001f)
        assertEquals(0f, headerOffsetAfterScroll(-10f, 50f, 100f), 0.001f)
    }

    @Test
    fun sinParteplegableNoSeMueve() {
        assertEquals(0f, headerOffsetAfterScroll(0f, -50f, 0f), 0.001f)
    }

    @Test
    fun alSoltarSeAcomodaAlExtremoMasCercano() {
        assertEquals(0f, headerSettleTarget(-40f, 100f), 0.001f)
        assertEquals(-100f, headerSettleTarget(-60f, 100f), 0.001f)
        assertEquals(0f, headerSettleTarget(0f, 100f), 0.001f)
        assertEquals(-100f, headerSettleTarget(-100f, 100f), 0.001f)
    }

    @Test
    fun cuentaLosFiltrosDeLaHoja() {
        assertEquals(0, activeFilterCount(null, null, emptySet()))
        assertEquals(1, activeFilterCount(TransactionType.EXPENSE, null, emptySet()))
        assertEquals(2, activeFilterCount(null, 3L, setOf(1L, 2L, 5L)))
        assertEquals(3, activeFilterCount(TransactionType.INCOME, 3L, setOf(9L)))
    }

    @Test
    fun textoDelBotonDeFiltros() {
        assertEquals("1 filtro", filterCountLabel(1))
        assertEquals("2 filtros", filterCountLabel(2))
    }
}
