package com.uramos.minicash.ui.components

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
    animate: Boolean = false
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

    Text(
        text = "$prefix$formattedAmount",
        color = effectiveColor,
        style = style,
        modifier = modifier
    )
}
