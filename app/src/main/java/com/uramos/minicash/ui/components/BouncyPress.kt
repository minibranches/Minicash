package com.uramos.minicash.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.uramos.minicash.ui.theme.minicashFastSpatialSpec

/**
 * Rebote al tocar: el elemento se encoge un poco mientras está presionado y vuelve a su tamaño con un resorte que
 * pasa de largo y se asienta (el resorte espacial rápido del esquema expresivo). Es el gesto común de tarjetas,
 * píldoras, chips y filas de toda la app; el modo sin animaciones lo deja sin movimiento.
 */
@Composable
fun Modifier.bounceOnPress(interactionSource: InteractionSource, pressedScale: Float = 0.95f): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = minicashFastSpatialSpec(),
        label = "bounce_on_press"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
