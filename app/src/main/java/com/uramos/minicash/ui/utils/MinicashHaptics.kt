package com.uramos.minicash.ui.utils

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView

class MinicashHapticHelper(
    private val haptic: HapticFeedback,
    private val view: View
) {
    /** Toque ligero para teclas del numpad, pestañas, chips, filtros y selectores */
    fun tick() {
        val performed = view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        if (!performed) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    /** Click medio para conmutadores (Switch), checkboxes de presupuesto y selección de sugerencias */
    fun click() {
        val performed = view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        if (!performed) {
            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
        }
    }

    /** Confirmación firme al guardar transacciones, créditos, cuentas, presupuestos o pagar mes */
    fun confirm() {
        val performed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            false
        }
        if (!performed) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    /** Pulsación profunda para Long-Press (selección múltiple) y acciones destructivas (eliminar) */
    fun heavyOrDelete() {
        val performed = view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        if (!performed) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
}

@Composable
fun rememberMinicashHaptics(): MinicashHapticHelper {
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    return remember(haptic, view) { MinicashHapticHelper(haptic, view) }
}
