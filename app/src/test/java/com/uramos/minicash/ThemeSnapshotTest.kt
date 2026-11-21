package com.uramos.minicash

import androidx.datastore.preferences.core.preferencesOf
import com.uramos.minicash.data.preferences.FontPreference
import com.uramos.minicash.data.preferences.NavBarStyle
import com.uramos.minicash.data.preferences.ThemeMode
import com.uramos.minicash.data.preferences.ThemeSnapshot
import com.uramos.minicash.data.preferences.UserPreferences
import com.uramos.minicash.data.preferences.toThemeSnapshot
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeSnapshotTest {

    @Test
    fun sinPreferenciasGuardadas_usaLosPredeterminados() {
        assertEquals(ThemeSnapshot(), preferencesOf().toThemeSnapshot())
    }

    @Test
    fun conPreferenciasGuardadas_lasRespeta() {
        val snapshot = preferencesOf(
            UserPreferences.THEME_MODE to ThemeMode.DARK.name,
            UserPreferences.DYNAMIC_COLOR_KEY to true,
            UserPreferences.AMOLED_MODE_KEY to true,
            UserPreferences.ACCENT_COLOR_HEX to "#00897B",
            UserPreferences.NAV_BAR_STYLE_KEY to NavBarStyle.FLOATING.name
        ).toThemeSnapshot()

        assertEquals(ThemeMode.DARK, snapshot.themeMode)
        assertEquals(true, snapshot.useDynamicColor)
        assertEquals(true, snapshot.isAmoledMode)
        assertEquals("#00897B", snapshot.accentColorHex)
        assertEquals(NavBarStyle.FLOATING, snapshot.navBarStyle)
    }

    @Test
    fun valoresInvalidos_caenAlPredeterminado() {
        val snapshot = preferencesOf(
            UserPreferences.THEME_MODE to "NO_EXISTE",
            UserPreferences.FONT_PREFERENCE_KEY to "NO_EXISTE",
            UserPreferences.NAV_BAR_STYLE_KEY to "NO_EXISTE"
        ).toThemeSnapshot()

        assertEquals(ThemeMode.SYSTEM, snapshot.themeMode)
        assertEquals(FontPreference.ROUNDED_QUICKSAND, snapshot.fontPreference)
        assertEquals(NavBarStyle.INTEGRATED, snapshot.navBarStyle)
    }
}
