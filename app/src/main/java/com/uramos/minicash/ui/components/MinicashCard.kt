package com.uramos.minicash.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.uramos.minicash.ui.theme.MinicashShapes

enum class CardTonalLevel {
    LOW,
    DEFAULT,
    HIGH
}

@Composable
fun MinicashCard(
    modifier: Modifier = Modifier,
    level: CardTonalLevel = CardTonalLevel.DEFAULT,
    onClick: (() -> Unit)? = null,
    shape: Shape? = null,
    containerColor: Color? = null,
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val resolvedShape = shape ?: when (level) {
        CardTonalLevel.LOW -> MinicashShapes.ContainerLarge
        CardTonalLevel.DEFAULT -> MinicashShapes.Card
        CardTonalLevel.HIGH -> MinicashShapes.Hero
    }

    val resolvedColor = containerColor ?: when (level) {
        CardTonalLevel.LOW -> MaterialTheme.colorScheme.surfaceContainerLow
        CardTonalLevel.DEFAULT -> MaterialTheme.colorScheme.surfaceContainer
        CardTonalLevel.HIGH -> MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val colors = CardDefaults.cardColors(containerColor = resolvedColor)

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = resolvedShape,
            colors = colors,
            border = border,
            content = content
        )
    } else {
        Card(
            modifier = modifier,
            shape = resolvedShape,
            colors = colors,
            border = border,
            content = content
        )
    }
}
