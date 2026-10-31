package com.uramos.minicash.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.getValue
import com.uramos.minicash.ui.theme.minicashSpatialSpec
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.uramos.minicash.ui.utils.MinicashColorUtils

@Composable
fun MinicashColorPicker(
    selectedColorHex: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    val vibrantColors = listOf(
        "#1A73E8", "#00838F", "#2E7D32", "#689F38", "#F57C00", "#D32F2F", "#C2185B", "#7B1FA2",
        "#795548", "#607D8B", "#00BFA5", "#FF7043"
    )

    val pastelColors = listOf(
        "#D2E3FC", "#B2EBF2", "#C8E6C9", "#DCEDC8", "#FFECB3", "#FFCDD2", "#F8BBD0", "#E1BEE7",
        "#D7CCC8", "#CFD8DC", "#A7FFEB", "#FFCCBC"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Fila 1: Tonos vivos
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                vibrantColors.forEach { hex ->
                    ColorSwatch(
                        hexColor = hex,
                        isSelected = selectedColorHex.equals(hex, ignoreCase = true),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            onColorSelected(hex)
                        }
                    )
                }
            }

            // Fila 2: Tonos pastel sincronizados
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                pastelColors.forEach { hex ->
                    ColorSwatch(
                        hexColor = hex,
                        isSelected = selectedColorHex.equals(hex, ignoreCase = true),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            onColorSelected(hex)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ColorSwatch(
    hexColor: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    val parsedColor = MinicashColorUtils.parseHexColor(hexColor, MaterialTheme.colorScheme.primary)
    val contentColor = MinicashColorUtils.getContrastingContentColor(parsedColor, isDarkTheme)

    val size by animateDpAsState(
        targetValue = if (isSelected) 44.dp else 38.dp,
        animationSpec = minicashSpatialSpec(),
        label = "swatch_size"
    )

    Surface(
        onClick = onClick,
        shape = if (isSelected) MaterialShapes.Cookie9Sided.toShape() else CircleShape,
        color = parsedColor,
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary)
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.12f))
        },
        modifier = Modifier.size(size)
    ) {
        Box(contentAlignment = Alignment.Center) {
            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
