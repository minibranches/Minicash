package com.uramos.minicash

import com.uramos.minicash.data.util.nextAppLockTimeout
import com.uramos.minicash.data.util.shouldLock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppLockTest {
    private val minute = 60_000L

    @Test
    fun neverLocksWhenDisabled() {
        assertFalse(shouldLock(enabled = false, timeoutMinutes = 0, backgroundedAtMillis = null, nowMillis = 10 * minute))
        assertFalse(shouldLock(enabled = false, timeoutMinutes = 1, backgroundedAtMillis = 0L, nowMillis = 99 * minute))
    }

    @Test
    fun coldStartAlwaysLocksWhenEnabled() {
        assertTrue(shouldLock(enabled = true, timeoutMinutes = 15, backgroundedAtMillis = null, nowMillis = 0L))
    }

    @Test
    fun locksOnlyAfterTheTimeoutOutsideTheApp() {
        assertFalse(shouldLock(true, 1, backgroundedAtMillis = 10 * minute, nowMillis = 10 * minute + 59_000L))
        assertTrue(shouldLock(true, 1, backgroundedAtMillis = 10 * minute, nowMillis = 11 * minute))
        assertTrue(shouldLock(true, 5, backgroundedAtMillis = 0L, nowMillis = 5 * minute))
    }

    @Test
    fun zeroTimeoutLocksEveryTime() {
        assertTrue(shouldLock(true, 0, backgroundedAtMillis = 1_000L, nowMillis = 1_000L))
    }

    @Test
    fun timeoutOptionsCycleAndWrapAround() {
        assertEquals(1, nextAppLockTimeout(0))
        assertEquals(5, nextAppLockTimeout(1))
        assertEquals(15, nextAppLockTimeout(5))
        assertEquals(0, nextAppLockTimeout(15))
        assertEquals(0, nextAppLockTimeout(99)) // valor desconocido: vuelve a empezar por la primera opción
    }
}
