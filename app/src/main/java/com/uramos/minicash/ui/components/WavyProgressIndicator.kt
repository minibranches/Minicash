package com.uramos.minicash.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.uramos.minicash.ui.theme.LocalAnimationRestriction
import kotlin.math.PI
import kotlin.math.sin

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WavyProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    strokeWidth: Dp = 3.5.dp,
    waveAmplitude: Dp = 2.8.dp,
    wavelength: Dp = 36.dp
) {
    val isRestricted = LocalAnimationRestriction.current
    val motionScheme = MaterialTheme.motionScheme

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = if (isRestricted) snap() else motionScheme.defaultSpatialSpec(),
        label = "wavy_progress_animation"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "wavy_phase_loop")
    val phaseOffset by if (isRestricted) {
        remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    } else {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "wavy_phase"
        )
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val centerY = size.height / 2f
        val strokeWidthPx = strokeWidth.toPx()
        val amplitudePx = waveAmplitude.toPx()
        val wavelengthPx = wavelength.toPx()
        val stepPx = 1.5.dp.toPx()
        val phaseShiftPx = phaseOffset * wavelengthPx
        val twoPi = (2.0 * PI).toFloat()

        fun buildSinePath(endX: Float): Path {
            val path = Path()
            var currentX = 0f
            val initialAngle = ((currentX - phaseShiftPx) / wavelengthPx) * twoPi
            path.moveTo(currentX, centerY + sin(initialAngle) * amplitudePx)

            while (currentX < endX) {
                currentX = (currentX + stepPx).coerceAtMost(endX)
                val angle = ((currentX - phaseShiftPx) / wavelengthPx) * twoPi
                val currentY = centerY + sin(angle) * amplitudePx
                path.lineTo(currentX, currentY)
            }
            return path
        }

        // 1. Pista inactiva: Onda senoidal completa de punta a punta (0 a width)
        drawPath(
            path = buildSinePath(width),
            color = trackColor,
            style = Stroke(
                width = strokeWidthPx,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // 2. Tramo activo: Sobre la misma onda senoidal, traza en color primario hasta activeWidth
        val activeWidth = (width * animatedProgress).coerceIn(0f, width)
        if (activeWidth > 2.dp.toPx()) {
            drawPath(
                path = buildSinePath(activeWidth),
                color = color,
                style = Stroke(
                    width = strokeWidthPx,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}
