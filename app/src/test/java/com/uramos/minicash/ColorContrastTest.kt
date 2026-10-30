package com.uramos.minicash

import com.uramos.minicash.ui.utils.AccessibleColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorContrastTest {
    private val lightSurface = 0xFFF7F2FA.toInt()
    private val darkSurface = 0xFF1D1B20.toInt()

    private val pastels = listOf(0xFFFFB3BA, 0xFFFFDFBA, 0xFFFFFFBA, 0xFFBAFFC9, 0xFFBAE1FF, 0xFFE0BBE4)
        .map { it.toInt() }

    @Test
    fun pastelsOnLightGetReadableIcons() {
        pastels.forEach { base ->
            val tint = AccessibleColor.iconTint(base, lightSurface, 0.2f)
            val container = AccessibleColor.blend(lightSurface, base, 0.2f)
            assertTrue("base=${base.toString(16)}", AccessibleColor.contrast(tint, container) >= 3.0)
        }
    }

    @Test
    fun rawPastelFailsContrastOnLight() {
        val base = 0xFFFFFFBA.toInt()
        val container = AccessibleColor.blend(lightSurface, base, 0.2f)
        assertTrue(AccessibleColor.contrast(base, container) < 3.0)
    }

    @Test
    fun darkColorsOnDarkAreLightened() {
        val base = 0xFF1A237E.toInt()
        val tint = AccessibleColor.iconTint(base, darkSurface, 0.2f)
        val container = AccessibleColor.blend(darkSurface, base, 0.2f)
        assertTrue(AccessibleColor.contrast(tint, container) >= 3.0)
    }

    @Test
    fun alreadyContrastedColorIsUnchanged() {
        val base = 0xFF1A73E8.toInt()
        assertEquals(base, AccessibleColor.iconTint(base, lightSurface, 0.2f))
    }
}
