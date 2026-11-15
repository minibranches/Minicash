package com.uramos.minicash.ui.components

import android.graphics.Matrix
import android.graphics.RectF
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.toPath

/**
 * Forma intermedia de un [Morph] entre dos polígonos de Material ([progress] de 0 a 1), ajustada al tamaño del
 * componente. Con [progress] animado, el contorno se transforma de una forma a la otra.
 */
class MorphShape(private val morph: Morph, private val progress: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = morph.toPath(progress)
        val bounds = RectF()
        path.computeBounds(bounds, true)
        if (bounds.width() > 0f && bounds.height() > 0f) {
            val fit = Matrix()
            fit.setRectToRect(bounds, RectF(0f, 0f, size.width, size.height), Matrix.ScaleToFit.FILL)
            path.transform(fit)
        }
        return Outline.Generic(path.asComposePath())
    }
}
