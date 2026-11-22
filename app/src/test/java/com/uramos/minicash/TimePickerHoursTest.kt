package com.uramos.minicash

import com.uramos.minicash.ui.components.hour12Of
import com.uramos.minicash.ui.components.hour24Of
import org.junit.Assert.assertEquals
import org.junit.Test

class TimePickerHoursTest {

    @Test
    fun deVeinticuatroHorasAHoraDeReloj() {
        assertEquals(12, hour12Of(0))
        assertEquals(1, hour12Of(1))
        assertEquals(11, hour12Of(11))
        assertEquals(12, hour12Of(12))
        assertEquals(1, hour12Of(13))
        assertEquals(9, hour12Of(21))
        assertEquals(11, hour12Of(23))
    }

    @Test
    fun deHoraDeRelojAVeinticuatroHoras() {
        assertEquals(0, hour24Of(12, pm = false))
        assertEquals(9, hour24Of(9, pm = false))
        assertEquals(12, hour24Of(12, pm = true))
        assertEquals(13, hour24Of(1, pm = true))
        assertEquals(21, hour24Of(9, pm = true))
        assertEquals(23, hour24Of(11, pm = true))
    }

    @Test
    fun ida_y_vuelta_conserva_la_hora() {
        for (hour in 0..23) {
            assertEquals(hour, hour24Of(hour12Of(hour), pm = hour >= 12))
        }
    }
}
