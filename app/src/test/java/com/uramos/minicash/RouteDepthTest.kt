package com.uramos.minicash

import com.uramos.minicash.ui.navigation.routeDepth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteDepthTest {

    @Test
    fun noRouteIsDepthZero() {
        assertEquals(0, routeDepth(null))
    }

    @Test
    fun firstLevelScreensAreDepthOne() {
        assertEquals(1, routeDepth("settings"))
        assertEquals(1, routeDepth("reports"))
        assertEquals(1, routeDepth("settings"))
    }

    @Test
    fun settingsSubScreensAreDepthTwo() {
        assertEquals(2, routeDepth("settings/accounts"))
        assertEquals(2, routeDepth("settings/about"))
    }

    @Test
    fun pushingGoesForwardAndPoppingGoesBack() {
        assertTrue(routeDepth("settings/backups") > routeDepth("settings"))
        assertTrue(routeDepth(null) < routeDepth("reports"))
    }
}
