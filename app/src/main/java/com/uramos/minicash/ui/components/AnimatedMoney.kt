package com.uramos.minicash.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.uramos.minicash.ui.theme.LocalAnimationRestriction
import com.uramos.minicash.ui.theme.minicashEffectsSpec
import kotlin.math.roundToLong

/**
 * Cifra que "cuenta" hacia [target] en lugar de saltar: la primera vez sube desde cero y, cuando cambia, va del valor
 * que se veía al nuevo. Usa el resorte de efectos (sin rebote) para que la cifra no pase de largo, y con las
 * animaciones restringidas devuelve el valor al instante. Solo para cifras destacadas, no para filas de una lista.
 */
@Composable
fun rememberAnimatedCents(target: Long, enabled: Boolean = true): Long {
    if (!enabled || LocalAnimationRestriction.current) return target
    var shown by remember { mutableLongStateOf(0L) }
    val spec = minicashEffectsSpec<Float>()
    LaunchedEffect(target) {
        val start = shown
        if (start == target) return@LaunchedEffect
        Animatable(0f).animateTo(1f, spec) {
            shown = start + ((target - start) * value).roundToLong()
        }
        shown = target
    }
    return shown
}
