package com.uramos.minicash.ui.screens.account

import androidx.compose.runtime.Immutable
import com.uramos.minicash.data.local.entities.TransactionEntity
import com.uramos.minicash.data.util.transactionEffect
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

/** Cambio neto del saldo de una cuenta en un mes (centavos; negativo si salió más de lo que entró). */
@Immutable
data class MonthlyNet(val month: YearMonth, val net: Long)

/** Movimientos que tocan la cuenta: como origen o, en una transferencia, como destino. */
fun transactionsOfAccount(transactions: List<TransactionEntity>, accountId: Long): List<TransactionEntity> =
    transactions.filter { it.sourceAccountId == accountId || it.destinationAccountId == accountId }

/**
 * Cambio neto del saldo de la cuenta en cada uno de los últimos [months] meses (el último es [now]), con ceros en
 * los meses sin movimientos. Una transferencia resta en el origen y suma en el destino, como en los saldos reales.
 */
fun accountMonthlyNet(
    transactions: List<TransactionEntity>,
    accountId: Long,
    months: Int,
    now: YearMonth,
    zone: ZoneId = ZoneId.systemDefault()
): List<MonthlyNet> {
    val totals = HashMap<YearMonth, Long>()
    transactions.forEach { tx ->
        val delta = transactionEffect(tx).filter { it.first == accountId }.sumOf { it.second }
        if (delta != 0L) {
            val month = YearMonth.from(Instant.ofEpochMilli(tx.date).atZone(zone))
            totals[month] = (totals[month] ?: 0L) + delta
        }
    }
    return (months - 1 downTo 0).map { back ->
        val month = now.minusMonths(back.toLong())
        MonthlyNet(month, totals[month] ?: 0L)
    }
}
