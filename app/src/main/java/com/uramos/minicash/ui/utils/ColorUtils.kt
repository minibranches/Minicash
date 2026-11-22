package com.uramos.minicash.ui.utils

import com.uramos.minicash.ui.theme.cardSurface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import kotlin.math.pow

/**
 * Color de un ícono que se dibuja sobre un contenedor teñido con el mismo color (p. ej. 20 % de alpha).
 * Con tonos pastel en modo claro el ícono se pierde; esto lo oscurece (o aclara en tema oscuro)
 * conservando el tono hasta alcanzar al menos 3:1 de contraste (WCAG para componentes gráficos).
 */
@Composable
fun Color.asIconTint(containerAlpha: Float = 0.2f): Color {
    val surface = MaterialTheme.colorScheme.cardSurface
    return remember(this, surface, containerAlpha) {
        Color(AccessibleColor.iconTint(this.toArgb(), surface.toArgb(), containerAlpha))
    }
}

/** Aritmética de color pura (sin dependencias de Android) para poder probarla en JVM. */
object AccessibleColor {
    const val MIN_ICON_CONTRAST = 3.0

    private fun channel(argb: Int, shift: Int) = (argb shr shift) and 0xFF

    fun blend(from: Int, to: Int, ratio: Float): Int {
        fun mix(shift: Int): Int =
            (channel(from, shift) + (channel(to, shift) - channel(from, shift)) * ratio).toInt().coerceIn(0, 255)
        return (0xFF shl 24) or (mix(16) shl 16) or (mix(8) shl 8) or mix(0)
    }

    fun luminance(argb: Int): Double {
        fun lin(shift: Int): Double {
            val c = channel(argb, shift) / 255.0
            return if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * lin(16) + 0.7152 * lin(8) + 0.0722 * lin(0)
    }

    fun contrast(a: Int, b: Int): Double {
        val la = luminance(a)
        val lb = luminance(b)
        return (maxOf(la, lb) + 0.05) / (minOf(la, lb) + 0.05)
    }

    fun iconTint(base: Int, surface: Int, containerAlpha: Float, minContrast: Double = MIN_ICON_CONTRAST): Int {
        val opaqueBase = base or (0xFF shl 24)
        val container = blend(surface, opaqueBase, containerAlpha)
        val towards = if (luminance(surface) > 0.5) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
        for (step in 0..10) {
            val candidate = if (step == 0) opaqueBase else blend(opaqueBase, towards, step / 10f)
            if (contrast(candidate, container) >= minContrast) return candidate
        }
        return towards
    }
}

object MinicashColorUtils {

    /**
     * Parsea un string hex de forma segura, retornando un color de fallback si es inválido.
     */
    fun parseHexColor(hex: String?, fallback: Color): Color {
        if (hex.isNullOrBlank()) return fallback
        return try {
            val cleanHex = if (hex.startsWith("#")) hex else "#$hex"
            Color(android.graphics.Color.parseColor(cleanHex))
        } catch (e: Exception) {
            fallback
        }
    }

    /**
     * Genera un color de contenedor teñido sutilmente mezclando el color base con la superficie actual.
     */
    fun tintedContainerColor(baseColor: Color, surfaceColor: Color, ratio: Float = 0.16f): Color {
        val blendedArgb = ColorUtils.blendARGB(surfaceColor.toArgb(), baseColor.toArgb(), ratio)
        return Color(blendedArgb)
    }

    /**
     * Determina el color de texto/icono óptimo (onSurface o derivado de alto contraste)
     * para garantizar un ratio mínimo de 4.5:1 frente al fondo proporcionado.
     */
    fun getContrastingContentColor(backgroundColor: Color, isDarkTheme: Boolean): Color {
        val bgArgb = backgroundColor.toArgb()
        val whiteArgb = android.graphics.Color.WHITE
        val darkArgb = android.graphics.Color.rgb(24, 29, 24) // Neutro teñido oscuro OnBackground

        val contrastWithWhite = ColorUtils.calculateContrast(whiteArgb, bgArgb)
        val contrastWithDark = ColorUtils.calculateContrast(darkArgb, bgArgb)

        return if (isDarkTheme) {
            if (contrastWithWhite >= 4.5) Color.White else Color(0xFFE0E4DC)
        } else {
            if (contrastWithDark >= 4.5) Color(0xFF181D18) else Color.Black
        }
    }
}
