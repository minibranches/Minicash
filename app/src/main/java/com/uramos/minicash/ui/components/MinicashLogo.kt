package com.uramos.minicash.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private const val LOGO_VIEWPORT = 108f
private const val MONOGRAM_PATH = "M 44,62 V 50 C 44,44.5 54,44.5 54,50 V 62 M 54,50 C 54,44.5 64,44.5 64,50 V 62"

/**
 * Logo de Minicash dibujado con los colores del tema: el anillo y la "m" usan el color primario y el
 * disco interior el contenedor primario, así que cambia junto con el acento o el color dinámico.
 */
@Composable
fun MinicashLogo(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    ringColor: Color = MaterialTheme.colorScheme.primary,
    discColor: Color = MaterialTheme.colorScheme.primaryContainer,
    markColor: Color = MaterialTheme.colorScheme.primary
) {
    val monogram = remember { PathParser().parsePathString(MONOGRAM_PATH).toPath() }

    Canvas(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = "Minicash" }
    ) {
        val scale = this.size.minDimension / LOGO_VIEWPORT
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        drawCircle(color = ringColor, radius = 34f * scale, center = center)
        drawCircle(color = discColor, radius = 25f * scale, center = center)
        withTransform({ scale(scale, scale, pivot = Offset.Zero) }) {
            drawPath(
                path = monogram,
                color = markColor,
                style = Stroke(width = 4.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

@Composable
fun MinicashLogoWithText(
    modifier: Modifier = Modifier,
    logoSize: Dp = 36.dp
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MinicashLogo(size = logoSize)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Minicash",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
