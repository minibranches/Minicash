package com.uramos.minicash.ui.components

import androidx.compose.runtime.compositionLocalOf
import com.uramos.minicash.ui.theme.screenBackground
import android.os.Build
import kotlin.math.roundToInt
import com.uramos.minicash.ui.theme.minicashSpatialSpec
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.animation.core.animate
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope
import com.uramos.minicash.ui.theme.minicashFastEffectsSpec
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.blur.BlurRadiusSpec
import androidx.compose.ui.graphics.blur.BlurStop
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Alto de la franja inferior que desenfocan las listas de las pestañas (la cápsula flotante pasa por encima).
 * Es 0 con la barra integrada. Como el desenfoque va en la lista y no en la página, los avisos («Deshacer») y la
 * barra de selección múltiple quedan por encima, sin desenfocar.
 */
val LocalBottomBlurBand = compositionLocalOf { 0.dp }

/** Aire entre el borde superior del panel y la barra superior de cada pantalla. */
val TopBarBreathing = 24.dp

/**
 * Desenfoque progresivo nativo (`Modifier.blur` con `BlurRadiusSpec`, Android 13+) apagado a propósito: medido en un
 * Nothing Phone a 120 Hz (build perf, Movimientos, desplazamiento continuo) deja la mediana de cuadro en ~16 ms
 * (≈ 60 fps) frente a ~9 ms sin él, y bajar el radio de 18 a 10 dp no cambia nada: el costo es desenfocar una capa
 * a pantalla completa, no el radio. Se usa el velo de color (el mismo de Android 12). El código nativo se conserva
 * para volver a probarlo en hardware o versiones de Compose más rápidos.
 */
private const val PROGRESSIVE_BLUR_ENABLED = false

val nativeProgressiveBlur: Boolean get() = PROGRESSIVE_BLUR_ENABLED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

private val HEADER_BLUR_RADIUS = 10.dp
private val HEADER_BOTTOM_AIR = 8.dp
private val HEADER_PINNED_TOP_AIR = 8.dp
private val HEADER_FADE = 12.dp

/** Alto visible del encabezado (en px), para que el velo de [underHeaderBlur] siga al encabezado mientras se pliega. */
internal val LocalHeaderVisiblePx = compositionLocalOf<(() -> Float)?> { null }

/**
 * Pantalla con encabezado sobre un contenido que se desplaza por debajo. El encabezado tiene dos partes:
 * [header] (la barra con el título), que se esconde al bajar por la lista y reaparece al subir, y [pinned]
 * (opcional: buscador, resumen, selector de periodo), que se queda a la vista y sube a ocupar su lugar.
 * El alto completo (barra y fila fija, sin plegar) se entrega a [content], que lo suma al relleno superior de su
 * lista y aplica [underHeaderBlur]. Así la zona del título no es una franja aparte: es la misma superficie.
 * Si se indica [listState], al volver al principio de la lista el encabezado vuelve a desplegarse.
 */
@Composable
fun BlurHeaderScaffold(
    header: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    pinned: (@Composable () -> Unit)? = null,
    listState: ScrollableState? = null,
    content: @Composable (headerHeight: Dp) -> Unit
) {
    val state = remember { HeaderCollapseState() }
    val spec = minicashSpatialSpec<Float>()
    val connection = remember(state, spec) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (state.collapsiblePx > 0f && available.y != 0f) {
                    state.offsetPx = headerOffsetAfterScroll(state.offsetPx, available.y, state.collapsiblePx)
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                val target = headerSettleTarget(state.offsetPx, state.collapsiblePx)
                if (state.offsetPx != target) {
                    animate(state.offsetPx, target, animationSpec = spec) { value, _ -> state.offsetPx = value }
                }
                return Velocity.Zero
            }
        }
    }
    if (listState != null) {
        LaunchedEffect(listState) {
            snapshotFlow { listState.canScrollBackward }.collect { canScroll ->
                if (!canScroll && state.offsetPx != 0f) {
                    animate(state.offsetPx, 0f, animationSpec = spec) { value, _ -> state.offsetPx = value }
                }
            }
        }
    }
    val hasPinned = pinned != null
    SubcomposeLayout(modifier = modifier.nestedScroll(connection)) { constraints ->
        val loose = Constraints(maxWidth = constraints.maxWidth, maxHeight = constraints.maxHeight)
        val headerPlaceables = subcompose("header") {
            Box(Modifier.padding(start = 4.dp, end = 4.dp, top = TopBarBreathing, bottom = if (hasPinned) 0.dp else HEADER_BOTTOM_AIR)) { header() }
        }.map { it.measure(loose) }
        val pinnedPlaceables = if (pinned != null) {
            subcompose("pinned") {
                Box(Modifier.padding(top = HEADER_PINNED_TOP_AIR, bottom = HEADER_BOTTOM_AIR)) { pinned() }
            }.map { it.measure(loose) }
        } else emptyList()
        val headerHeightPx = headerPlaceables.maxOfOrNull { it.height } ?: 0
        val pinnedHeightPx = pinnedPlaceables.maxOfOrNull { it.height } ?: 0
        state.collapsiblePx = headerHeightPx.toFloat()
        state.pinnedPx = pinnedHeightPx.toFloat()
        val contentPlaceables = subcompose("content") {
            CompositionLocalProvider(LocalHeaderVisiblePx provides { state.visiblePx }) {
                content((headerHeightPx + pinnedHeightPx).toDp())
            }
        }.map { it.measure(constraints) }
        layout(constraints.maxWidth, constraints.maxHeight) {
            contentPlaceables.forEach { it.place(0, 0) }
            val offset = state.offsetPx
            val fade = if (headerHeightPx > 0) (1f + offset / headerHeightPx).coerceIn(0f, 1f) else 1f
            headerPlaceables.forEach {
                it.placeWithLayer(0, 0) {
                    translationY = offset
                    alpha = fade
                }
            }
            pinnedPlaceables.forEach { it.place(0, (headerHeightPx + offset).roundToInt()) }
        }
    }
}

/**
 * Para la lista que pasa por debajo del encabezado: desenfoque progresivo, máximo en el borde superior y
 * nulo [HEADER_FADE] después del encabezado. Con [bottomBand] mayor que 0 el mismo desenfoque se aplica también
 * al borde inferior (subpantallas, que no tienen cápsula flotante). Hoy se usa el velo de color (ver
 * [PROGRESSIVE_BLUR_ENABLED]): casi opaco arriba, como bajo una barra normal, y en la franja inferior, si la hay.
 *
 * El desenfoque es costoso, así que solo existe mientras hay contenido bajo cada borde: arriba, si la lista se
 * desplazó ([ScrollableState.canScrollBackward]); abajo, si queda más por ver ([ScrollableState.canScrollForward]).
 * El radio entra y sale con un resorte y, cuando ningún borde lo necesita, el modificador se quita por completo.
 */
@Composable
fun Modifier.underHeaderBlur(headerHeight: Dp, bottomBand: Dp = 0.dp, listState: ScrollableState): Modifier {
    val native = nativeProgressiveBlur
    val band = headerHeight + HEADER_FADE
    if (!native) {
        val tint = MaterialTheme.colorScheme.screenBackground
        val visiblePx = LocalHeaderVisiblePx.current
        return this.drawWithContent {
            drawContent()
            val headerPx = visiblePx?.invoke()?.coerceAtLeast(0f) ?: headerHeight.toPx()
            val bandPx = headerPx + HEADER_FADE.toPx()
            drawRect(
                brush = Brush.verticalGradient(
                    0f to tint.copy(alpha = 0.98f),
                    (headerPx / bandPx) to tint.copy(alpha = 0.92f),
                    1f to Color.Transparent,
                    endY = bandPx
                ),
                size = Size(size.width, bandPx)
            )
            if (bottomBand > 0.dp) {
                val bottomPx = bottomBand.toPx().coerceAtMost(size.height)
                drawRect(
                    brush = Brush.verticalGradient(
                        0f to Color.Transparent,
                        1f to tint.copy(alpha = 0.9f),
                        startY = size.height - bottomPx,
                        endY = size.height
                    ),
                    topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - bottomPx),
                    size = Size(size.width, bottomPx)
                )
            }
        }
    }
    val topActive by remember(listState) { derivedStateOf { listState.canScrollBackward } }
    val bottomActive by remember(listState) { derivedStateOf { bottomBand > 0.dp && listState.canScrollForward } }
    val topFactor = remember { Animatable(0f) }
    val bottomFactor = remember { Animatable(0f) }
    var applied by remember { mutableStateOf(false) }
    val spec = minicashFastEffectsSpec<Float>()
    LaunchedEffect(topActive, bottomActive) {
        if (topActive || bottomActive) applied = true
        coroutineScope {
            launch { topFactor.animateTo(if (topActive) 1f else 0f, spec) }
            launch { bottomFactor.animateTo(if (bottomActive) 1f else 0f, spec) }
        }
        if (!topActive && !bottomActive) applied = false
    }
    if (!applied) return this
    return blur {
        val height = size.height
        val stops = if (height > 0.dp) {
            val top = (band / height).coerceIn(0.02f, 0.9f)
            val bottom = if (bottomBand > 0.dp) (1f - bottomBand / height).coerceIn(top + 0.02f, 0.99f) else 1f
            buildList {
                add(BlurStop(0f, HEADER_BLUR_RADIUS * topFactor.value))
                add(BlurStop(top, 0.dp))
                if (bottom < 1f) {
                    add(BlurStop(bottom, 0.dp))
                    add(BlurStop(1f, HEADER_BLUR_RADIUS * bottomFactor.value))
                } else {
                    add(BlurStop(1f, 0.dp))
                }
            }
        } else {
            listOf(BlurStop(0f, 0.dp), BlurStop(1f, 0.dp))
        }
        blurRadiusSpec = BlurRadiusSpec.verticalGradient(stops)
        edgeTreatment = BlurredEdgeTreatment.Rectangle
    }
}

/**
 * Para las subpantallas: el relleno que entrega Scaffold trae arriba el alto de la barra; la lista lo quita de
 * su `padding` (para poder pasar por debajo de la barra) y lo suma a su `contentPadding` con [plusTop].
 */
@Composable
fun PaddingValues.withoutTop(): PaddingValues {
    val direction = LocalLayoutDirection.current
    return PaddingValues(start = calculateLeftPadding(direction), end = calculateRightPadding(direction), bottom = calculateBottomPadding())
}

@Composable
fun PaddingValues.plusTop(extra: Dp): PaddingValues {
    val direction = LocalLayoutDirection.current
    return PaddingValues(
        start = calculateLeftPadding(direction),
        top = calculateTopPadding() + extra,
        end = calculateRightPadding(direction),
        bottom = calculateBottomPadding()
    )
}
