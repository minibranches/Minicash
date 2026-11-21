package com.uramos.minicash.ui.components

import android.os.Build
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

/** Aire entre el borde superior del panel y la barra superior de cada pantalla. */
val TopBarBreathing = 24.dp

/** El desenfoque progresivo nativo (`Modifier.blur` con `BlurRadiusSpec`) existe desde Android 13. */
val nativeProgressiveBlur: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

private val HEADER_BLUR_RADIUS = 18.dp
private val HEADER_BOTTOM_AIR = 8.dp
private val HEADER_FADE = 12.dp

/**
 * Pantalla con encabezado fijo (barra superior y lo que se quiera mantener a la vista) sobre un contenido que
 * se desplaza por debajo. El encabezado se mide primero y su alto se entrega a [content], que lo suma al relleno
 * superior de su lista y aplica [underHeaderBlur] para que lo que pasa por debajo se desenfoque. Así la zona
 * del título no es una franja aparte: es la misma superficie de la pantalla.
 */
@Composable
fun BlurHeaderScaffold(
    header: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (headerHeight: Dp) -> Unit
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val loose = Constraints(maxWidth = constraints.maxWidth, maxHeight = constraints.maxHeight)
        val headerPlaceables = subcompose("header") {
            Box(Modifier.padding(top = TopBarBreathing, bottom = HEADER_BOTTOM_AIR)) { header() }
        }.map { it.measure(loose) }
        val headerHeightPx = headerPlaceables.maxOfOrNull { it.height } ?: 0
        val contentPlaceables = subcompose("content") { content(headerHeightPx.toDp()) }
            .map { it.measure(constraints) }
        layout(constraints.maxWidth, constraints.maxHeight) {
            contentPlaceables.forEach { it.place(0, 0) }
            headerPlaceables.forEach { it.place(0, 0) }
        }
    }
}

/**
 * Para la lista que pasa por debajo del encabezado: desenfoque progresivo fijo, máximo en el borde superior y
 * nulo [HEADER_FADE] después del encabezado. Con [bottomBand] mayor que 0 el mismo desenfoque se aplica también
 * al borde inferior (subpantallas, que no tienen cápsula flotante). Con desenfoque nativo no se pinta ningún
 * velo de color: todo es desenfoque. Antes de Android 13 no hay desenfoque progresivo y se usa un velo casi
 * opaco arriba, como bajo una barra normal.
 */
@Composable
fun Modifier.underHeaderBlur(headerHeight: Dp, bottomBand: Dp = 0.dp): Modifier {
    val native = nativeProgressiveBlur
    val band = headerHeight + HEADER_FADE
    if (!native) {
        val tint = MaterialTheme.colorScheme.surfaceContainerLowest
        return this.drawWithContent {
            drawContent()
            drawRect(
                brush = Brush.verticalGradient(
                    0f to tint.copy(alpha = 0.95f),
                    (headerHeight.toPx() / band.toPx()) to tint.copy(alpha = 0.57f),
                    1f to Color.Transparent,
                    endY = band.toPx()
                ),
                size = Size(size.width, band.toPx())
            )
        }
    }
    return blur {
        val height = size.height
        val stops = if (height > 0.dp) {
            val top = (band / height).coerceIn(0.02f, 0.9f)
            val bottom = if (bottomBand > 0.dp) (1f - bottomBand / height).coerceIn(top + 0.02f, 0.99f) else 1f
            buildList {
                add(BlurStop(0f, HEADER_BLUR_RADIUS))
                add(BlurStop(top, 0.dp))
                if (bottom < 1f) {
                    add(BlurStop(bottom, 0.dp))
                    add(BlurStop(1f, HEADER_BLUR_RADIUS))
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
 * Desenfoque progresivo fijo al pie del contenido, detrás de la cápsula flotante: nulo hasta [band] del borde
 * inferior y máximo en el borde. Sin efecto antes de Android 13 o si [enabled] es falso.
 */
@Composable
fun Modifier.bottomEdgeBlur(band: Dp, enabled: Boolean = true, radius: Dp = 14.dp): Modifier {
    if (!enabled || !nativeProgressiveBlur) return this
    return blur {
        val height = size.height
        val fraction = if (height > 0.dp) (1f - band / height).coerceIn(0.01f, 0.98f) else 0.98f
        blurRadiusSpec = BlurRadiusSpec.verticalGradient(
            listOf(BlurStop(0f, 0.dp), BlurStop(fraction, 0.dp), BlurStop(1f, radius))
        )
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
