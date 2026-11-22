package com.uramos.minicash

import com.uramos.minicash.ui.components.splitCurrencyCents
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyCentsTest {

    @Test
    fun separaEnteroYCentavos() {
        assertEquals("$1,234" to ".50", splitCurrencyCents("$1,234.50"))
        assertEquals("$0" to ".05", splitCurrencyCents("$0.05"))
    }

    @Test
    fun sinDosDecimalesNoSeSepara() {
        assertNull(splitCurrencyCents("$1,234"))
        assertNull(splitCurrencyCents("$1,234.5"))
        assertNull(splitCurrencyCents("$1.234.567"))
        assertNull(splitCurrencyCents(""))
    }
}
