package com.uramos.minicash.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Forma de galleta festoneada (9 lóbulos) de Material Expressive, la que usan los íconos de la app. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun rememberScallopedShape(): Shape = MaterialShapes.Cookie9Sided.toShape()

/** Ícono sobre un contenedor festoneado y tonal: filas de opciones, estados vacíos y avatares de movimientos. */
@Composable
fun ScallopedIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    container: Color = MaterialTheme.colorScheme.secondaryContainer,
    tint: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    contentDescription: String? = null
) {
    Surface(shape = rememberScallopedShape(), color = container, modifier = modifier.size(size)) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(size / 2))
        }
    }
}
