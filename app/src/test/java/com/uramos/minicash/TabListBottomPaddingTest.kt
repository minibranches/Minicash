package com.uramos.minicash

import androidx.compose.ui.unit.dp
import com.uramos.minicash.ui.utils.tabListBottomPadding
import org.junit.Assert.assertEquals
import org.junit.Test

class TabListBottomPaddingTest {

    @Test
    fun conLaCapsulaFlotanteSeReservaSuFranja() {
        assertEquals(140.dp, tabListBottomPadding(floatingBand = 140.dp, imeBottom = 0.dp))
    }

    @Test
    fun conLaBarraIntegradaSoloHayUnMargen() {
        assertEquals(24.dp, tabListBottomPadding(floatingBand = 0.dp, imeBottom = 0.dp))
    }

    @Test
    fun conElTecladoAbiertoSeSumaSuAltoParaPoderLlegarAlFinal() {
        assertEquals(324.dp, tabListBottomPadding(floatingBand = 140.dp, imeBottom = 300.dp))
        assertEquals(324.dp, tabListBottomPadding(floatingBand = 0.dp, imeBottom = 300.dp))
    }
}
