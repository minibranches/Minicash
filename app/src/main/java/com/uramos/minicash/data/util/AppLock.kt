package com.uramos.minicash.data.util

/** Minutos fuera de la app tras los que se vuelve a pedir el desbloqueo; 0 = cada vez que se abre. */
val APP_LOCK_TIMEOUT_OPTIONS = listOf(0, 1, 5, 15)

/** Texto de una opción de espera, para mostrarlo en ajustes. */
fun appLockTimeoutLabel(minutes: Int): String =
    if (minutes <= 0) com.uramos.minicash.ui.utils.AppStrings.get(com.uramos.minicash.R.string.lock_timeout_always)
    else com.uramos.minicash.ui.utils.AppStrings.get(com.uramos.minicash.R.string.lock_timeout_after, minutes)

/** Siguiente opción de espera al tocar el renglón de ajustes; da la vuelta al llegar al final. */
fun nextAppLockTimeout(current: Int): Int {
    val index = APP_LOCK_TIMEOUT_OPTIONS.indexOf(current)
    return APP_LOCK_TIMEOUT_OPTIONS[(index + 1) % APP_LOCK_TIMEOUT_OPTIONS.size]
}

/**
 * Si al volver a la app hay que pedir el desbloqueo. Sin bloqueo activo nunca; en un arranque en frío
 * ([backgroundedAtMillis] nulo) siempre; y si ya se había abierto, solo cuando pasó al menos [timeoutMinutes] fuera.
 * Los tiempos son del reloj monotónico (no cambian si el usuario mueve la hora del teléfono).
 */
fun shouldLock(enabled: Boolean, timeoutMinutes: Int, backgroundedAtMillis: Long?, nowMillis: Long): Boolean {
    if (!enabled) return false
    val backgroundedAt = backgroundedAtMillis ?: return true
    return nowMillis - backgroundedAt >= timeoutMinutes * 60_000L
}
