package com.uramos.minicash

import com.uramos.minicash.data.preferences.DistributionStyle
import com.uramos.minicash.data.preferences.HomeLayout
import com.uramos.minicash.data.preferences.HomeWidget
import com.uramos.minicash.data.preferences.PatrimonyStyle
import com.uramos.minicash.data.preferences.distributionStyleOf
import com.uramos.minicash.data.preferences.encodeHomeHidden
import com.uramos.minicash.data.preferences.encodeHomeOrder
import com.uramos.minicash.data.preferences.moveHomeWidget
import com.uramos.minicash.data.preferences.parseHomeHidden
import com.uramos.minicash.data.preferences.parseHomeOrder
import com.uramos.minicash.data.preferences.patrimonyStyleOf
import com.uramos.minicash.ui.navigation.routeDepth
import com.uramos.minicash.ui.screens.home.CategoryExpenseStat
import com.uramos.minicash.ui.screens.home.changeLabel
import com.uramos.minicash.ui.screens.home.groupTopStats
import com.uramos.minicash.ui.screens.home.percentChange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HomeLayoutTest {

    @Test
    fun `sin dato guardado el orden es el de fabrica`() {
        assertEquals(HomeWidget.entries.toList(), parseHomeOrder(null))
        assertEquals(HomeWidget.entries.toList(), parseHomeOrder(""))
    }

    @Test
    fun `el orden guardado se respeta y lo que falta se agrega al final`() {
        val parsed = parseHomeOrder("recents,accounts")
        assertEquals(listOf(HomeWidget.RECENTS, HomeWidget.ACCOUNTS, HomeWidget.PATRIMONY, HomeWidget.DISTRIBUTION), parsed)
    }

    @Test
    fun `ids desconocidos o repetidos se ignoran`() {
        val parsed = parseHomeOrder("accounts,fantasma,accounts,patrimony")
        assertEquals(HomeWidget.ACCOUNTS, parsed[0])
        assertEquals(HomeWidget.PATRIMONY, parsed[1])
        assertEquals(HomeWidget.entries.size, parsed.size)
    }

    @Test
    fun `codificar y leer el orden devuelve lo mismo`() {
        val order = listOf(HomeWidget.DISTRIBUTION, HomeWidget.RECENTS, HomeWidget.PATRIMONY, HomeWidget.ACCOUNTS)
        assertEquals(order, parseHomeOrder(encodeHomeOrder(order)))
    }

    @Test
    fun `las secciones ocultas se guardan y se leen`() {
        val hidden = setOf(HomeWidget.ACCOUNTS, HomeWidget.RECENTS)
        assertEquals(hidden, parseHomeHidden(encodeHomeHidden(hidden)))
        assertEquals(emptySet<HomeWidget>(), parseHomeHidden(null))
        assertEquals(setOf(HomeWidget.PATRIMONY), parseHomeHidden("patrimony,xx"))
    }

    @Test
    fun `visible respeta el orden y quita las ocultas`() {
        val layout = HomeLayout(
            order = listOf(HomeWidget.RECENTS, HomeWidget.PATRIMONY, HomeWidget.ACCOUNTS, HomeWidget.DISTRIBUTION),
            hidden = setOf(HomeWidget.PATRIMONY)
        )
        assertEquals(listOf(HomeWidget.RECENTS, HomeWidget.ACCOUNTS, HomeWidget.DISTRIBUTION), layout.visible)
    }

    @Test
    fun `mover una seccion la lleva a su nueva posicion`() {
        val order = HomeWidget.entries.toList()
        assertEquals(
            listOf(HomeWidget.ACCOUNTS, HomeWidget.DISTRIBUTION, HomeWidget.PATRIMONY, HomeWidget.RECENTS),
            moveHomeWidget(order, 0, 2)
        )
        assertEquals(order, moveHomeWidget(order, 1, 1))
        assertEquals(order, moveHomeWidget(order, 9, 0))
        assertEquals(HomeWidget.PATRIMONY, moveHomeWidget(order, 0, 99).last())
    }

    @Test
    fun `los estilos desconocidos caen al predeterminado`() {
        assertEquals(DistributionStyle.STACKED, distributionStyleOf(null))
        assertEquals(DistributionStyle.STACKED, distributionStyleOf("nada"))
        assertEquals(DistributionStyle.DONUT, distributionStyleOf("DONUT"))
        assertEquals(PatrimonyStyle.STRIP, patrimonyStyleOf("nada"))
        assertEquals(PatrimonyStyle.CARD, patrimonyStyleOf("CARD"))
    }

    @Test
    fun `las pantallas nuevas de Ajustes tienen la profundidad de una subpantalla`() {
        assertEquals(2, routeDepth("settings/home"))
        assertEquals(2, routeDepth("settings/notifications"))
    }

    private fun stat(name: String, amount: Long, pct: Float) = CategoryExpenseStat(name, amount, pct)

    @Test
    fun `groupTopStats junta lo que sobra en Otros`() {
        val stats = listOf(stat("A", 50, 50f), stat("B", 20, 20f), stat("C", 15, 15f), stat("D", 10, 10f), stat("E", 5, 5f))
        val grouped = groupTopStats(stats, 3)
        assertEquals(listOf("A", "B", "Otros"), grouped.map { it.categoryName })
        assertEquals(30L, grouped.last().totalAmount)
        assertEquals(30f, grouped.last().percentage, 0.001f)
    }

    @Test
    fun `groupTopStats no toca listas que ya caben`() {
        val stats = listOf(stat("A", 50, 50f), stat("B", 50, 50f))
        assertEquals(stats, groupTopStats(stats, 5))
    }

    @Test
    fun `percentChange compara contra el mes anterior`() {
        assertEquals(50, percentChange(150, 100))
        assertEquals(-25, percentChange(75, 100))
        assertEquals(0, percentChange(100, 100))
        assertNull(percentChange(100, 0))
    }

    @Test
    fun `changeLabel muestra la direccion del cambio`() {
        assertEquals("▲ 12 %", changeLabel(12))
        assertEquals("▼ 5 %", changeLabel(-5))
        assertEquals("sin cambio", changeLabel(0))
    }
}
