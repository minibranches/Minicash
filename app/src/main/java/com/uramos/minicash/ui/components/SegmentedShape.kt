package com.uramos.minicash.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.uramos.minicash.ui.theme.LocalAnimationRestriction

/**
 * Devuelve una forma redondeada dinámica gobernada por el esquema de movimiento de M3E.
 * Modela con resortes las esquinas de listas segmentadas según el índice y el tamaño del grupo:
 * - count == 1: redondeo completo en las 4 esquinas (cornerLarge).
 * - index == 0 (primero): superiores cornerLarge, inferiores cornerSmall.
 * - index == count - 1 (último): superiores cornerSmall, inferiores cornerLarge.
 * - intermedios: 4 esquinas con cornerSmall.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun rememberSegmentedShape(
    index: Int,
    count: Int,
    cornerLarge: Dp = 20.dp,
    cornerSmall: Dp = 4.dp
): Shape {
    val isAnimationRestricted = LocalAnimationRestriction.current
    val spatialSpec = if (isAnimationRestricted) {
        snap()
    } else {
        MaterialTheme.motionScheme.defaultSpatialSpec<Dp>()
    }

    val targetTop = when {
        count <= 1 -> cornerLarge
        index == 0 -> cornerLarge
        else -> cornerSmall
    }

    val targetBottom = when {
        count <= 1 -> cornerLarge
        index == count - 1 -> cornerLarge
        else -> cornerSmall
    }

    val animatedTopStart by animateDpAsState(
        targetValue = targetTop,
        animationSpec = spatialSpec,
        label = "segmented_top_start"
    )
    val animatedTopEnd by animateDpAsState(
        targetValue = targetTop,
        animationSpec = spatialSpec,
        label = "segmented_top_end"
    )
    val animatedBottomStart by animateDpAsState(
        targetValue = targetBottom,
        animationSpec = spatialSpec,
        label = "segmented_bottom_start"
    )
    val animatedBottomEnd by animateDpAsState(
        targetValue = targetBottom,
        animationSpec = spatialSpec,
        label = "segmented_bottom_end"
    )

    return RoundedCornerShape(
        topStart = animatedTopStart,
        topEnd = animatedTopEnd,
        bottomStart = animatedBottomStart,
        bottomEnd = animatedBottomEnd
    )
}
