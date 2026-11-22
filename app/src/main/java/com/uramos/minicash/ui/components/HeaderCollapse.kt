package com.uramos.minicash.ui.components

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue

/**
 * Desplazamiento (en px, de 0 a `-max`) de la parte plegable del encabezado tras un desplazamiento de la lista de
 * [delta] px. Al bajar por la lista ([delta] negativo) el encabezado se esconde; al subir ([delta] positivo) vuelve
 * a aparecer de inmediato, sin esperar a llegar al principio de la lista.
 */
internal fun headerOffsetAfterScroll(current: Float, delta: Float, max: Float): Float =
    (current + delta).coerceIn(-max.coerceAtLeast(0f), 0f)

/** A dónde acomoda el encabezado al soltar: completamente visible o completamente escondido, el más cercano. */
internal fun headerSettleTarget(offset: Float, max: Float): Float = if (offset < -max / 2f) -max else 0f

/** Estado del encabezado plegable: cuánto mide la parte plegable, la fija, y cuánto se ha escondido. */
@Stable
internal class HeaderCollapseState {
    var collapsiblePx by mutableFloatStateOf(0f)
    var pinnedPx by mutableFloatStateOf(0f)
    var offsetPx by mutableFloatStateOf(0f)

    /** Alto que se ve ahora del encabezado completo (lo que tapa a la lista). */
    val visiblePx: Float get() = collapsiblePx + pinnedPx + offsetPx
}
