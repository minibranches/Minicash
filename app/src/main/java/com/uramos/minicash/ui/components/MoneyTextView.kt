package com.uramos.minicash.ui.components

import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.uramos.minicash.ui.theme.MinicashThemeExtensions
import com.uramos.minicash.ui.utils.toCurrencyFormat

enum class MoneyDisplayType {
    INCOME,
    EXPENSE,
    TRANSFER,
    NEUTRAL
}

@Composable
fun MoneyTextView(
    amountCents: Long,
    type: MoneyDisplayType,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    /** Cuenta hacia la cifra en lugar de saltar; para totales destacados, no para filas de una lista. */
    animate: Boolean = false,
    /** Los centavos se dibujan más pequeños que el resto de la cifra. */
    smallCents: Boolean = true,
    /** Si se indica, la cifra se encoge hasta este tamaño para caber en una línea (estadios angostos). */
    autoSizeMin: TextUnit? = null
) {
    val financialColors = MinicashThemeExtensions.financialColors
    val amountCents = rememberAnimatedCents(amountCents, enabled = animate)

    val color = if (amountCents == 0L) {
        financialColors.neutral
    } else {
        when (type) {
            MoneyDisplayType.INCOME -> financialColors.income
            MoneyDisplayType.EXPENSE -> financialColors.expense
            MoneyDisplayType.TRANSFER -> financialColors.transfer
            MoneyDisplayType.NEUTRAL -> financialColors.neutral
        }
    }

    val isNegative = amountCents < 0L
    val isZero = amountCents == 0L
    val absoluteCents = remember(amountCents) { kotlin.math.abs(amountCents) }
    val formattedAmount = remember(absoluteCents) { absoluteCents.toCurrencyFormat() }

    val (prefix, effectiveColor) = when {
        isZero -> {
            Pair("", color) // Semántica neutra para $0.00
        }
        type == MoneyDisplayType.INCOME -> {
            Pair("+ ", color)
        }
        type == MoneyDisplayType.EXPENSE -> {
            Pair("- ", color)
        }
        type == MoneyDisplayType.TRANSFER -> {
            Pair("", color)
        }
        type == MoneyDisplayType.NEUTRAL -> {
            // En tipo NEUTRAL (saldos de cuentas y balances patrimoniales):
            // Si el saldo en BD es negativo, mostrar el signo "-" explícito y teñir en color de error/alerta
            if (isNegative) {
                Pair("- ", MaterialTheme.colorScheme.error)
            } else {
                Pair("", color)
            }
        }
        else -> Pair("", color)
    }

    val split = if (smallCents && style.fontSize.isSp) splitCurrencyCents(formattedAmount) else null
    val shown = if (split != null) {
        buildAnnotatedString {
            append(prefix + split.first)
            withStyle(SpanStyle(fontSize = 0.62.em)) { append(split.second) }
        }
    } else {
        AnnotatedString("$prefix$formattedAmount")
    }

    if (autoSizeMin != null && style.fontSize.isSp) {
        Text(
            text = shown,
            color = effectiveColor,
            style = style,
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(minFontSize = autoSizeMin, maxFontSize = style.fontSize),
            modifier = modifier
        )
    } else {
        Text(
            text = shown,
            color = effectiveColor,
            style = style,
            modifier = modifier
        )
    }
}

/** Separa un monto formateado ("$1,234.50") en la parte entera y los centavos (".50"); nulo si no hay dos decimales. */
fun splitCurrencyCents(formatted: String): Pair<String, String>? {
    val dot = formatted.lastIndexOf('.')
    if (dot < 0 || formatted.length - dot != 3 || !formatted.substring(dot + 1).all { it.isDigit() }) return null
    return formatted.substring(0, dot) to formatted.substring(dot)
}
