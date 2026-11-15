package com.uramos.minicash.data.util

import com.uramos.minicash.data.local.entities.TransactionEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Periodo de los movimientos que se exportan a CSV. */
enum class CsvRange(val label: String) {
    THIS_MONTH("Este mes"),
    LAST_3_MONTHS("Últimos 3 meses"),
    THIS_YEAR("Este año"),
    ALL("Todo el historial");

    /** Si el movimiento de fecha [dateMillis] cae dentro del periodo, contando hasta el final de hoy. */
    fun contains(dateMillis: Long, today: LocalDate, zone: ZoneId): Boolean {
        val start: LocalDate = when (this) {
            ALL -> return true
            THIS_MONTH -> today.withDayOfMonth(1)
            LAST_3_MONTHS -> today.withDayOfMonth(1).minusMonths(2)
            THIS_YEAR -> today.withDayOfYear(1)
        }
        val from = start.atStartOfDay(zone).toInstant().toEpochMilli()
        val until = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return dateMillis in from until until
    }
}

const val CSV_HEADER = "Fecha,Tipo,Monto,Cuenta,Cuenta destino,Categoría,Nota"

/**
 * Celda de texto entre comillas. Neutraliza lo que empieza con =, +, - o @ (las hojas de cálculo lo ejecutarían como
 * fórmula al abrir el archivo) anteponiendo un apóstrofo, y duplica las comillas internas.
 */
fun csvCell(value: String): String {
    val safe = if (value.firstOrNull() in listOf('=', '+', '-', '@')) "'$value" else value
    return "\"" + safe.replace("\"", "\"\"") + "\""
}

/** Monto en pesos con dos decimales y punto, sin signo ni símbolo (el tipo va en su propia columna). */
fun csvAmount(cents: Long): String = String.format(Locale.US, "%.2f", cents / 100.0)

/**
 * CSV de los movimientos, del más antiguo al más reciente, con encabezado. Una línea por movimiento; los nombres
 * de cuenta y categoría se resuelven con los mapas por id y quedan vacíos si ya no existen.
 */
fun buildTransactionsCsv(
    transactions: List<TransactionEntity>,
    accountNames: Map<Long, String>,
    categoryNames: Map<Long, String>,
    zone: ZoneId = ZoneId.systemDefault()
): String {
    val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.US)
    return buildString {
        append(CSV_HEADER).append('\n')
        transactions.sortedBy { it.date }.forEach { tx ->
            val line = listOf(
                csvCell(dateFormat.format(Instant.ofEpochMilli(tx.date).atZone(zone))),
                csvCell(tx.type.displayName),
                csvAmount(tx.amount),
                csvCell(accountNames[tx.sourceAccountId].orEmpty()),
                csvCell(tx.destinationAccountId?.let { accountNames[it] }.orEmpty()),
                csvCell(tx.categoryId?.let { categoryNames[it] }.orEmpty()),
                csvCell(tx.note.orEmpty())
            )
            append(line.joinToString(",")).append('\n')
        }
    }
}
