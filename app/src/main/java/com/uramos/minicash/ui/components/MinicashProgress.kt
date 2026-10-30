package com.uramos.minicash.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.uramos.minicash.ui.theme.LocalAnimationRestriction

/**
 * Título de sección: grande y en negrita, con el peso visual que lo separa de los títulos de fila.
 */
@Composable
fun MinicashSectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    )
}

/**
 * Barra de progreso plana: tramo activo redondeado, hueco, y pista con un punto al final.
 * Con progreso 0 solo se ve la pista; con 1 solo el tramo activo.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MinicashProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    thickness: Dp = 10.dp,
    gap: Dp = 4.dp
) {
    val isRestricted = LocalAnimationRestriction.current
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = if (isRestricted) snap() else MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "minicash_progress"
    )

    Canvas(modifier = modifier.fillMaxWidth().height(thickness)) {
        val h = size.height
        val w = size.width
        val gapPx = gap.toPx()
        val radius = CornerRadius(h / 2f, h / 2f)
        val split = w * animated

        val activeEnd = if (animated >= 1f) w else (split - gapPx / 2f).coerceAtLeast(0f)
        if (activeEnd > 0f) {
            drawRoundRect(color = color, size = Size(activeEnd, h), cornerRadius = radius)
        }

        if (animated < 1f) {
            val trackStart = if (animated <= 0f) 0f else split + gapPx / 2f
            val trackWidth = w - trackStart
            if (trackWidth > 0f) {
                drawRoundRect(
                    color = trackColor,
                    topLeft = Offset(trackStart, 0f),
                    size = Size(trackWidth, h),
                    cornerRadius = radius
                )
                // Punto final, como el remate de la pista en Meiro
                val dot = h * 0.28f
                if (trackWidth > h * 1.5f) {
                    drawCircle(color = color, radius = dot, center = Offset(w - h / 2f, h / 2f))
                }
            }
        }
    }
}