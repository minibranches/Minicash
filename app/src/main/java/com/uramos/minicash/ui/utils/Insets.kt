package com.uramos.minicash.ui.utils

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.uramos.minicash.ui.components.LocalBottomBlurBand

/** Alto que ocupa la cápsula flotante de pestañas (incluye márgenes); los mensajes deben quedar por encima. */
val FLOATING_NAV_CLEARANCE = 88.dp

/** Aire bajo el último elemento de una lista cuando nada más lo tapa (barra integrada o teclado abierto). */
private val LIST_END_GAP = 24.dp

/**
 * Relleno inferior de las listas de las pestañas, para poder llegar siempre al último elemento:
 * - con el teclado del sistema abierto, el alto del teclado más un margen (la ventana es de borde a borde y la lista
 *   mide también la parte que el teclado tapa);
 * - con la cápsula flotante, la franja que ocupa ([floatingBand], ya incluye el inset de navegación);
 * - con la barra integrada, solo un margen, porque la barra no se superpone a la lista.
 */
fun tabListBottomPadding(floatingBand: Dp, imeBottom: Dp): Dp = when {
    imeBottom > 0.dp -> imeBottom + LIST_END_GAP
    floatingBand > 0.dp -> floatingBand
    else -> LIST_END_GAP
}

@Composable
fun rememberTabListBottomPadding(): Dp =
    tabListBottomPadding(LocalBottomBlurBand.current, WindowInsets.ime.asPaddingValues().calculateBottomPadding())
