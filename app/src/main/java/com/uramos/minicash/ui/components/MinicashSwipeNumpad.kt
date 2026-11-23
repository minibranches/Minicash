package com.uramos.minicash.ui.components

import com.uramos.minicash.ui.utils.rememberMinicashHaptics
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.uramos.minicash.ui.theme.MinicashThemeExtensions
import com.uramos.minicash.ui.transactions.KeypadButton

private val KEYPAD_OPERATORS = listOf("+", "-", "×", "÷")
private const val MAX_DECIMALS = 2

/**
 * Función pura: dada la expresión actual y la tecla pulsada devuelve la nueva expresión.
 * Invariantes: sin espacios iniciales, sin ceros a la izquierda en un número y máximo
 * [MAX_DECIMALS] decimales por número.
 */
fun processKeypadPress(currentExpr: String, key: String): String {
    val expr = currentExpr.trimStart()
    return when (key) {
        "⌫" -> {
            if (expr.endsWith(" ")) {
                expr.trimEnd().dropLast(1).trimEnd()
            } else {
                expr.dropLast(1)
            }
        }
        "C" -> ""
        in KEYPAD_OPERATORS -> {
            val trimmed = expr.trim()
            when {
                trimmed.isEmpty() -> ""
                trimmed.last().toString() in KEYPAD_OPERATORS -> trimmed.dropLast(1).trim() + " $key "
                else -> trimmed.trimEnd('.') + " $key "
            }
        }
        "." -> {
            val lastToken = expr.substringAfterLast(' ')
            when {
                '.' in lastToken -> expr
                lastToken.isEmpty() -> expr + "0."
                else -> "$expr."
            }
        }
        else -> { // Dígitos "0".."9"
            val head = expr.substringBeforeLast(' ', missingDelimiterValue = "")
            val lastToken = expr.substringAfterLast(' ')
            val prefix = if (expr.contains(' ')) "$head " else ""
            when {
                lastToken.contains('.') && lastToken.substringAfter('.').length >= MAX_DECIMALS -> expr
                lastToken == "0" -> prefix + key
                else -> expr + key
            }
        }
    }
}

fun evaluateMathExpression(expression: String): Double {
    if (expression.isBlank()) return 0.0
    val clean = expression.replace('×', '*').replace('÷', '/').replace(',', '.').trim()
    return runCatching {
        val tokenRegex = Regex("(\\d+(?:\\.\\d+)?|[+\\-*\\/])")
        val tokens = tokenRegex.findAll(clean).map { it.value }.toList()
        if (tokens.isEmpty()) return@runCatching 0.0

        var total = tokens[0].toDoubleOrNull() ?: 0.0
        var i = 1
        while (i < tokens.size - 1) {
            val op = tokens[i]
            val nextVal = tokens[i + 1].toDoubleOrNull()
            if (nextVal != null) {
                when (op) {
                    "+" -> total += nextVal
                    "-" -> total -= nextVal
                    "*" -> total *= nextVal
                    "/" -> if (nextVal != 0.0) total /= nextVal
                }
                i += 2
            } else {
                i += 1
            }
        }
        if (total.isNaN() || total.isInfinite()) 0.0 else total
    }.getOrDefault(0.0)
}

@Composable
fun MinicashSwipeNumpad(
    amountExpression: String,
    onExpressionChange: (newExpression: String, calculatedAmount: Double) -> Unit,
    modifier: Modifier = Modifier,
    /** Si se indica, cada tecla se entrega tal cual (con su propia lógica de edición) en lugar de usar [onExpressionChange]. */
    onKeyPress: ((String) -> Unit)? = null
) {
    val haptics = rememberMinicashHaptics()
    val spacing = MinicashThemeExtensions.spacing
    var showOperators by remember { mutableStateOf(false) }

    val keypadLayout = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "⌫")
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount < -20) showOperators = true
                    else if (dragAmount > 20) showOperators = false
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.small)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.small)
        ) {
            // Bloque Numérico 3x4 (Siempre visible)
            Column(
                modifier = Modifier.weight(3f),
                verticalArrangement = Arrangement.spacedBy(spacing.small)
            ) {
                keypadLayout.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(spacing.small),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        row.forEach { key ->
                            val isBackspace = key == "⌫"
                            KeypadButton(
                                text = key,
                                onClick = {
                                    haptics.click()
                                    if (onKeyPress != null) {
                                        onKeyPress(key)
                                    } else {
                                        val newExpr = processKeypadPress(amountExpression, key)
                                        onExpressionChange(newExpr, evaluateMathExpression(newExpr))
                                    }
                                },
                                cornerRadius = 12.dp,
                                containerColor = if (isBackspace) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = if (isBackspace) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            )
                        }
                    }
                }
            }

            // Bloque de Operadores (4ª Columna Deslizable a la derecha)
            AnimatedVisibility(
                visible = showOperators,
                enter = expandHorizontally() + fadeIn(),
                exit = shrinkHorizontally() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.width(56.dp),
                    verticalArrangement = Arrangement.spacedBy(spacing.small)
                ) {
                    listOf("+", "-", "×", "÷").forEach { op ->
                        KeypadButton(
                            text = op,
                            onClick = {
                                haptics.click()
                                if (onKeyPress != null) {
                                    onKeyPress(op)
                                } else {
                                    val newExpr = processKeypadPress(amountExpression, op)
                                    onExpressionChange(newExpr, evaluateMathExpression(newExpr))
                                }
                            },
                            cornerRadius = 12.dp,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        )
                    }
                }
            }
        }
    }
}
