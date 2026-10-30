package com.uramos.minicash.ui.components

import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.Dp

/**
 * Los componentes tocables de Material 3 reservan 48 dp de alto mínimo, aunque se dibujen más pequeños,
 * y esa reserva invisible hace que el margen vertical de píldoras dentro de una cápsula sea mayor que el
 * lateral. Dentro de este bloque se usa el tamaño dibujado, para que los márgenes sean idénticos.
 */
@Composable
fun WithoutMinTouchTarget(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified, content = content)
}