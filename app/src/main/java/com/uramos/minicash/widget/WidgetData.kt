package com.uramos.minicash.widget

import android.content.Context
import com.uramos.minicash.data.local.MinicashDatabase
import com.uramos.minicash.data.local.entities.TransactionEntity
import com.uramos.minicash.data.local.entities.TransactionType
import com.uramos.minicash.data.preferences.FontPreference
import com.uramos.minicash.data.preferences.UserPreferences
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId

/** Datos que muestra el widget; los montos van en centavos. */
data class WidgetSnapshot(
    val totalBalance: Long,
    val monthExpense: Long,
    /** Si es verdadero el widget oculta los montos (misma opción que ocultar en multitarea). */
    val hideAmounts: Boolean,
    /** Fuente elegida en Ajustes › Apariencia. */
    val font: FontPreference = FontPreference.ROUNDED_QUICKSAND
)

/** Gasto del mes: suma de gastos; las transferencias y los ingresos no cuentan, igual que en Inicio. */
fun monthExpenseOf(transactions: List<TransactionEntity>): Long =
    transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

/** Texto del monto para el widget: oculto con puntos si el usuario activó la privacidad. */
fun widgetAmountText(cents: Long, hidden: Boolean, format: (Long) -> String): String =
    if (hidden) "••••" else format(cents)

suspend fun loadWidgetSnapshot(context: Context, today: LocalDate = LocalDate.now()): WidgetSnapshot {
    val database = MinicashDatabase.getDatabase(context.applicationContext)
    val zone = ZoneId.systemDefault()
    val start = today.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
    val end = today.withDayOfMonth(1).plusMonths(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

    val balance = database.accountDao().getTotalBalance().first() ?: 0L
    val expense = monthExpenseOf(database.transactionDao().getTransactionsBetween(start, end).first())
    val hidden = UserPreferences(context.applicationContext).hideInRecents.first()
    val font = loadFontPreference(context)
    return WidgetSnapshot(totalBalance = balance, monthExpense = expense, hideAmounts = hidden, font = font)
}

suspend fun loadFontPreference(context: Context): FontPreference =
    UserPreferences(context.applicationContext).fontPreference.first()
