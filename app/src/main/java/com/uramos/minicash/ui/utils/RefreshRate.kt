package com.uramos.minicash.ui.utils

import android.content.Context
import android.hardware.display.DisplayManager
import android.view.Display
import android.view.Window

/** Valor guardado cuando la app no limita la frecuencia y la decide el sistema. */
const val REFRESH_RATE_AUTO = 0

/**
 * Límites que se ofrecen según la pantalla: siempre «Automático», 30 y 60 Hz; 120 Hz solo si la pantalla llega
 * a ~120 Hz. (Una pantalla de 60 Hz no ofrece 120.)
 */
fun availableRefreshLimits(deviceMaxHz: Float): List<Int> = buildList {
    add(REFRESH_RATE_AUTO)
    add(30)
    add(60)
    if (deviceMaxHz >= 119f) add(120)
}

/** Si el valor guardado ya no es válido para esta pantalla (por ejemplo, un respaldo de otro teléfono), vuelve a automático. */
fun coerceRefreshLimit(saved: Int, available: List<Int>): Int = if (saved in available) saved else REFRESH_RATE_AUTO

fun refreshLimitLabel(limit: Int, autoLabel: String = "Automático"): String = if (limit == REFRESH_RATE_AUTO) autoLabel else "$limit Hz"

/** Frecuencia máxima que admite la pantalla principal, en Hz (60 si no se puede saber). */
fun deviceMaxRefreshRate(context: Context): Float {
    val display = runCatching {
        (context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager).getDisplay(Display.DEFAULT_DISPLAY)
    }.getOrNull() ?: return 60f
    return display.supportedModes.maxOfOrNull { it.refreshRate } ?: display.refreshRate
}

/** Pide al sistema que la ventana corra a [limitHz]; 0 quita la preferencia. Es una petición: la pantalla decide. */
fun applyRefreshRateLimit(window: Window, limitHz: Int) {
    val params = window.attributes
    val requested = if (limitHz > 0) limitHz.toFloat() else 0f
    if (params.preferredRefreshRate != requested) {
        params.preferredRefreshRate = requested
        window.attributes = params
    }
}
