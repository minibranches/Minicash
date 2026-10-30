package com.uramos.minicash.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.uramos.minicash.ui.theme.MinicashThemeExtensions
import java.text.NumberFormat
import java.util.Locale

private val currencyFormatter = ThreadLocal.withInitial {
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-MX"))
}

fun Long.toCurrencyFormat(): String {
    return currencyFormatter.get()?.format(this / 100.0) ?: ""
}

fun Double.toCurrencyFormat(): String {
    return currencyFormatter.get()?.format(this) ?: ""
}

/** Convierte un monto en unidades a centavos redondeando (evita que 19.99 * 100 = 1998.99… se trunque a 1998). */
fun Double.toCents(): Long = Math.round(this * 100.0)

@Composable
fun getAmountColor(amount: Long): Color {
    val financialColors = MinicashThemeExtensions.financialColors
    return when {
        amount > 0 -> financialColors.income
        amount < 0 -> financialColors.expense
        else -> financialColors.neutral
    }
}
