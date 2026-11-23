package com.uramos.minicash.ui.theme

import androidx.compose.animation.scaleIn
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.snap
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> minicashSpatialSpec(): FiniteAnimationSpec<T> =
    if (LocalAnimationRestriction.current) snap() else MaterialTheme.motionScheme.defaultSpatialSpec()

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> minicashEffectsSpec(): FiniteAnimationSpec<T> =
    if (LocalAnimationRestriction.current) snap() else MaterialTheme.motionScheme.defaultEffectsSpec()

/** Resorte espacial rápido: para elementos pequeños (íconos de la barra, interruptores). */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> minicashFastSpatialSpec(): FiniteAnimationSpec<T> =
    if (LocalAnimationRestriction.current) snap() else MaterialTheme.motionScheme.fastSpatialSpec()

/** Resorte de efectos rápido (opacidad y color): amortiguamiento 1.0, sin rebote. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> minicashFastEffectsSpec(): FiniteAnimationSpec<T> =
    if (LocalAnimationRestriction.current) snap() else MaterialTheme.motionScheme.fastEffectsSpec()

/** Resorte de efectos lento (opacidad, color, conteos): amortiguamiento 1.0, nunca pasa del valor final. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> minicashSlowEffectsSpec(): FiniteAnimationSpec<T> =
    if (LocalAnimationRestriction.current) snap() else MaterialTheme.motionScheme.slowEffectsSpec()

/**
 * Entradas y salidas únicas de la app. Todo `AnimatedVisibility`/`AnimatedContent` nuevo debe usar estas en lugar de
 * armar su propio fundido y deslizamiento: así todas las pantallas se mueven igual. Salen del esquema de movimiento
 * de Material (espacial para tamaño y desplazamiento, efectos para opacidad) y respetan el modo sin animaciones.
 */
/** Tamaño desde el que «saltan» las entradas: el resorte espacial lo pasa de largo y lo asienta (rebote). */
private const val POP_IN_SCALE = 0.9f

object MinicashTransitions {
    /** Aparece: fundido y despliegue vertical. */
    @Composable
    fun enterVisible(): EnterTransition =
        fadeIn(minicashEffectsSpec()) + expandVertically(minicashSpatialSpec()) +
            scaleIn(minicashSpatialSpec(), initialScale = POP_IN_SCALE)

    /** Desaparece: fundido y pliegue vertical. */
    @Composable
    fun exitVisible(): ExitTransition =
        fadeOut(minicashEffectsSpec()) + shrinkVertically(minicashSpatialSpec())

    /** Aparece sin cambiar de tamaño: fundido y un leve ascenso (para elementos que entran sobre otros). */
    @Composable
    fun enterFloat(): EnterTransition =
        fadeIn(minicashEffectsSpec()) + slideInVertically(minicashSpatialSpec()) { it / 8 } +
            scaleIn(minicashSpatialSpec(), initialScale = POP_IN_SCALE)

    @Composable
    fun exitFloat(): ExitTransition =
        fadeOut(minicashFastEffectsSpec()) + slideOutVertically(minicashSpatialSpec()) { it / 8 }

    /** Cambio de contenido en el mismo lugar: el nuevo entra con fundido y el anterior sale más rápido. */
    @Composable
    fun contentSwap(): ContentTransform =
        (fadeIn(minicashEffectsSpec()) + slideInVertically(minicashSpatialSpec()) { it / 10 }) togetherWith
            fadeOut(minicashFastEffectsSpec())
}

/**
 * `animateItem` de las listas con el movimiento de la app: el elemento nuevo entra con fundido, el que cambia de
 * lugar se acomoda con resorte espacial y el que sale se desvanece rápido.
 */
@Composable
fun LazyItemScope.minicashAnimateItem(): Modifier =
    Modifier.animateItem(
        fadeInSpec = minicashEffectsSpec(),
        placementSpec = minicashSpatialSpec(),
        fadeOutSpec = minicashFastEffectsSpec()
    )
