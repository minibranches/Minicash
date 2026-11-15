package com.uramos.minicash

import com.uramos.minicash.data.local.entities.TransactionEntity
import com.uramos.minicash.data.local.entities.TransactionType
import com.uramos.minicash.data.util.CSV_HEADER
import com.uramos.minicash.data.util.CsvRange
import com.uramos.minicash.data.util.buildTransactionsCsv
import com.uramos.minicash.data.util.csvAmount
import com.uramos.minicash.data.util.csvCell
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class TransactionsCsvTest {
    private val zone = ZoneId.of("America/Mexico_City")
    private fun millis(date: LocalDate, hour: Int = 12) =
        date.atTime(hour, 30).atZone(zone).toInstant().toEpochMilli()

    @Test
    fun cellsEscapeQuotesAndNeutralizeFormulas() {
        assertEquals("\"hola, \"\"mundo\"\"\"", csvCell("hola, \"mundo\""))
        assertEquals("\"'=SUMA(A1)\"", csvCell("=SUMA(A1)"))
        assertEquals("\"'-5\"", csvCell("-5"))
        assertEquals("\"linea1\nlinea2\"", csvCell("linea1\nlinea2"))
    }

    @Test
    fun amountsUseDotAndTwoDecimals() {
        assertEquals("1234.50", csvAmount(123450))
        assertEquals("0.07", csvAmount(7))
        assertEquals("0.00", csvAmount(0))
    }

    @Test
    fun csvHasHeaderRowsInDateOrderAndResolvedNames() {
        val transactions = listOf(
            TransactionEntity(
                id = 2, type = TransactionType.TRANSFER, amount = 20000, date = millis(LocalDate.of(2026, 11, 14)),
                sourceAccountId = 1, destinationAccountId = 2
            ),
            TransactionEntity(
                id = 1, type = TransactionType.EXPENSE, amount = 9400, date = millis(LocalDate.of(2026, 11, 13)),
                note = "Oxxo, leche", sourceAccountId = 1, categoryId = 7
            )
        )
        val csv = buildTransactionsCsv(
            transactions,
            accountNames = mapOf(1L to "Efectivo", 2L to "Nu"),
            categoryNames = mapOf(7L to "Despensa"),
            zone = zone
        )
        val lines = csv.trimEnd('\n').split('\n')

        assertEquals(CSV_HEADER, lines[0])
        assertEquals(
            "\"2026-11-13 12:30\",\"Gasto\",94.00,\"Efectivo\",\"\",\"Despensa\",\"Oxxo, leche\"",
            lines[1]
        )
        assertEquals("\"2026-11-14 12:30\",\"Transferencia\",200.00,\"Efectivo\",\"Nu\",\"\",\"\"", lines[2])
    }

    @Test
    fun rangesCoverTheRightDates() {
        val today = LocalDate.of(2026, 11, 14)
        fun inRange(range: CsvRange, date: LocalDate) = range.contains(millis(date), today, zone)

        assertTrue(inRange(CsvRange.THIS_MONTH, LocalDate.of(2026, 11, 1)))
        assertTrue(inRange(CsvRange.THIS_MONTH, today))
        assertFalse(inRange(CsvRange.THIS_MONTH, LocalDate.of(2026, 10, 31)))

        assertTrue(inRange(CsvRange.LAST_3_MONTHS, LocalDate.of(2026, 9, 1)))
        assertFalse(inRange(CsvRange.LAST_3_MONTHS, LocalDate.of(2026, 8, 31)))

        assertTrue(inRange(CsvRange.THIS_YEAR, LocalDate.of(2026, 1, 1)))
        assertFalse(inRange(CsvRange.THIS_YEAR, LocalDate.of(2025, 12, 31)))

        assertTrue(inRange(CsvRange.ALL, LocalDate.of(2000, 1, 1)))
    }

    @Test
    fun rangesExcludeFutureDatedMovements() {
        val today = LocalDate.of(2026, 11, 14)
        assertFalse(CsvRange.THIS_MONTH.contains(millis(LocalDate.of(2026, 11, 15)), today, zone))
        assertTrue(CsvRange.THIS_MONTH.contains(millis(today, hour = 23), today, zone))
    }
}
