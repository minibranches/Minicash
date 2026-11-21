package com.uramos.minicash

import com.uramos.minicash.ui.navigation.Screen
import com.uramos.minicash.ui.navigation.navScreens
import org.junit.Assert.assertEquals
import org.junit.Test

class NavScreensTest {

    @Test
    fun conTodosLosModulos_hayCuatroDestinosEnOrden() {
        assertEquals(
            listOf(Screen.Home, Screen.Transactions, Screen.Budget, Screen.Credits),
            navScreens(showBudget = true, showCredits = true)
        )
    }

    @Test
    fun sinModulos_quedanInicioYMovimientos() {
        assertEquals(listOf(Screen.Home, Screen.Transactions), navScreens(showBudget = false, showCredits = false))
    }

    @Test
    fun unSoloModulo_apareceSinHuecos() {
        assertEquals(listOf(Screen.Home, Screen.Transactions, Screen.Budget), navScreens(true, false))
        assertEquals(listOf(Screen.Home, Screen.Transactions, Screen.Credits), navScreens(false, true))
    }

    @Test
    fun reportesYAjustesNuncaEstanEnLaBarra() {
        val all = navScreens(true, true)
        assertEquals(false, Screen.Settings in all)
    }
}
