package com.uramos.minicash.ui.components

import com.uramos.minicash.ui.theme.minicashSpatialSpec
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.uramos.minicash.ui.theme.LocalAnimationRestriction
import kotlinx.coroutines.delay

/** Recuerda qué elementos de una lista ya hicieron su animación de entrada, para no repetirla al desplazarse. */
class EntranceTracker internal constructor() {
    internal val seen = mutableSetOf<Any>()
}

@Composable
fun rememberEntranceTracker(): EntranceTracker = remember { EntranceTracker() }

private const val MAX_STAGGERED_ITEMS = 10
private const val STAGGER_MILLIS = 40L

/**
 * Entrada escalonada: el elemento aparece con desvanecido y un leve deslizamiento hacia arriba, con un
 * retraso proporcional a su posición. Solo se anima la primera vez que se muestra cada [key] y solo
 * los primeros elementos de la lista, que son los que están a la vista al abrir la pantalla.
 */
@Composable
fun Modifier.listEntrance(tracker: EntranceTracker, key: Any, index: Int): Modifier {
    val restricted = LocalAnimationRestriction.current
    val animate = remember { tracker.seen.add(key) && !restricted && index < MAX_STAGGERED_ITEMS }
    val progress = remember { Animatable(if (animate) 0f else 1f) }
    val spec = minicashSpatialSpec<Float>()
    if (animate) {
        LaunchedEffect(Unit) {
            delay(index * STAGGER_MILLIS)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = spec
            )
        }
    }
    return graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * 28.dp.toPx()
    }
}
