package com.uramos.minicash.ui.utils

import java.time.Instant
import java.time.ZoneId

/**
 * Normaliza un timestamp en milisegundos recibido desde DatePickerState (medianoche UTC)
 * a la medianoche de la fecha correspondiente en la zona horaria local del dispositivo.
 */
fun normalizeUtcToLocalMillis(utcMillis: Long): Long {
    val localDate = Instant.ofEpochMilli(utcMillis)
        .atZone(ZoneId.of("UTC"))
        .toLocalDate()
    return localDate.atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}

/** Inverso de [normalizeUtcToLocalMillis]: la fecha local pasa a medianoche UTC para DatePickerState. */
fun normalizeLocalToUtcMillis(localMillis: Long): Long {
    val localDate = Instant.ofEpochMilli(localMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    return localDate.atStartOfDay(ZoneId.of("UTC"))
        .toInstant()
        .toEpochMilli()
}

/** Clave "aaaa-mm" del mes (zona local) al que pertenece el timestamp; sirve para agrupar y filtrar. */
fun monthKey(millis: Long): String {
    val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
    return "%04d-%02d".format(date.year, date.monthValue)
}

/** Etiqueta legible ("Mar 2026") de una clave producida por [monthKey]. */
fun monthKeyLabel(key: String): String {
    val (year, month) = key.split('-').map { it.toInt() }
    val name = java.time.Month.of(month)
        .getDisplayName(java.time.format.TextStyle.SHORT, appLocale())
        .replace(".", "")
    return "${name.replaceFirstChar { it.uppercase() }} $year"
}
