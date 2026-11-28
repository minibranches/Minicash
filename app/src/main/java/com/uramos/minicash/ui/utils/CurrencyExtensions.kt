package com.uramos.minicash.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.uramos.minicash.ui.theme.MinicashThemeExtensions
import java.text.NumberFormat
import java.util.Locale

private val currencyFormatters = ThreadLocal.withInitial { HashMap<Locale, NumberFormat>() }

/** Formato de moneda del idioma en uso (es-MX o en-US); se guarda por idioma para no quedar con uno viejo. */
private fun currencyFormatter(): NumberFormat {
    val locale = appLocale()
    return currencyFormatters.get()!!.getOrPut(locale) { NumberFormat.getCurrencyInstance(locale) }
}

fun Long.toCurrencyFormat(): String {
    return currencyFormatter().format(this / 100.0)
}

fun Double.toCurrencyFormat(): String {
    return currencyFormatter().format(this)
}

/**
 * Monto corto para resúmenes: \$950, \$10.5k, \$1.2M (sin signo; el signo lo pone quien lo muestra).
 * Se usa donde varias cifras comparten una fila angosta.
 */
fun Long.toCompactCurrency(): String {
    val units = kotlin.math.abs(this) / 100.0
    fun trim(value: Double): String {
        val rounded = Math.round(value * 10) / 10.0
        return if (rounded % 1.0 == 0.0) rounded.toLong().toString() else rounded.toString()
    }
    return when {
        units >= 1_000_000 -> "\$" + trim(units / 1_000_000) + "M"
        units >= 1_000 -> "\$" + trim(units / 1_000) + "k"
        else -> "\$" + Math.round(units).toString()
    }
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
