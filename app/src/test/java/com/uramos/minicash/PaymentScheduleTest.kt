package com.uramos.minicash

import com.uramos.minicash.data.util.PaymentSchedule
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentScheduleTest {

    @Test
    fun nextPaymentLaterThisMonth() {
        assertEquals(LocalDate.of(2026, 11, 20), PaymentSchedule.nextPayment(20, LocalDate.of(2026, 11, 5)))
    }

    @Test
    fun nextPaymentTodayCounts() {
        assertEquals(LocalDate.of(2026, 11, 5), PaymentSchedule.nextPayment(5, LocalDate.of(2026, 11, 5)))
    }

    @Test
    fun nextPaymentAlreadyPassedMovesToNextMonth() {
        assertEquals(LocalDate.of(2026, 12, 5), PaymentSchedule.nextPayment(5, LocalDate.of(2026, 11, 6)))
    }

    @Test
    fun crossesTheYear() {
        assertEquals(LocalDate.of(2027, 1, 10), PaymentSchedule.nextPayment(10, LocalDate.of(2026, 12, 20)))
    }

    @Test
    fun day31UsesLastDayOfShortMonths() {
        assertEquals(LocalDate.of(2027, 2, 28), PaymentSchedule.nextPayment(31, LocalDate.of(2027, 2, 1)))
        assertEquals(LocalDate.of(2028, 2, 29), PaymentSchedule.nextPayment(31, LocalDate.of(2028, 2, 1)))
        assertEquals(LocalDate.of(2026, 11, 30), PaymentSchedule.nextPayment(31, LocalDate.of(2026, 11, 1)))
    }

    @Test
    fun notifiesExactlyOnTheConfiguredDay() {
        val payment = 15
        assertTrue(PaymentSchedule.shouldNotify(payment, 3, LocalDate.of(2026, 11, 12)))
        assertFalse(PaymentSchedule.shouldNotify(payment, 3, LocalDate.of(2026, 11, 11)))
        assertFalse(PaymentSchedule.shouldNotify(payment, 3, LocalDate.of(2026, 11, 13)))
    }

    @Test
    fun zeroDaysBeforeNotifiesOnPaymentDay() {
        assertTrue(PaymentSchedule.shouldNotify(15, 0, LocalDate.of(2026, 11, 15)))
        assertFalse(PaymentSchedule.shouldNotify(15, 0, LocalDate.of(2026, 11, 14)))
    }

    @Test
    fun reminderAcrossMonthBoundary() {
        // Pago el día 2; aviso 3 días antes cae el 30 del mes anterior
        assertTrue(PaymentSchedule.shouldNotify(2, 3, LocalDate.of(2026, 11, 29)))
        assertTrue(PaymentSchedule.shouldNotify(2, 3, LocalDate.of(2026, 12, 30)))
    }
}