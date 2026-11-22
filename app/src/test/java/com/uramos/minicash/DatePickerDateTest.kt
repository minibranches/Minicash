package com.uramos.minicash

import com.uramos.minicash.ui.utils.normalizeLocalToUtcMillis
import com.uramos.minicash.ui.utils.normalizeUtcToLocalMillis
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.TimeZone
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class DatePickerDateTest {
    private val original = TimeZone.getDefault()

    @After
    fun restoreZone() = TimeZone.setDefault(original)

    private fun local(zone: String, y: Int, m: Int, d: Int, h: Int, min: Int): Long {
        TimeZone.setDefault(TimeZone.getTimeZone(zone))
        return ZonedDateTime.of(y, m, d, h, min, 0, 0, ZoneId.of(zone)).toInstant().toEpochMilli()
    }

    private fun pickerDay(pickerMillis: Long): LocalDate = Instant.ofEpochMilli(pickerMillis).atZone(ZoneOffset.UTC).toLocalDate()

    @Test
    fun deNocheEnMexicoElSelectorMarcaElMismoDia() {
        val tonight = local("America/Mexico_City", 2026, 11, 21, 19, 30)
        assertEquals(LocalDate.of(2026, 11, 21), pickerDay(normalizeLocalToUtcMillis(tonight)))
    }

    @Test
    fun aceptarSinCambiarLaFechaConservaElDia() {
        val tonight = local("America/Mexico_City", 2026, 11, 21, 19, 30)
        val backFromPicker = normalizeUtcToLocalMillis(normalizeLocalToUtcMillis(tonight))
        val expected = LocalDate.of(2026, 11, 21).atStartOfDay(ZoneId.of("America/Mexico_City")).toInstant().toEpochMilli()
        assertEquals(expected, backFromPicker)
    }

    @Test
    fun enZonaAdelantadaDeMananaTambienMarcaElMismoDia() {
        val morning = local("Asia/Tokyo", 2026, 11, 22, 7, 0)
        assertEquals(LocalDate.of(2026, 11, 22), pickerDay(normalizeLocalToUtcMillis(morning)))
    }
}
