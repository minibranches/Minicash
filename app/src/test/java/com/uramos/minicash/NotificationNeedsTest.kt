package com.uramos.minicash

import com.uramos.minicash.data.local.entities.CreditEntity
import com.uramos.minicash.data.local.entities.CreditInstitutionEntity
import com.uramos.minicash.data.util.NotificationNeeds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationNeedsTest {

    private fun credit(
        id: Long = 1,
        institutionId: Long? = null,
        paid: Int = 0,
        total: Int = 6,
        override: Boolean = false,
        day: Int? = null,
        before: Int? = null
    ) = CreditEntity(
        id = id, article = "Silla", institution = "Nu", institutionId = institutionId, responsible = "KU",
        purchaseDate = 0L, totalInstallments = total, paidInstallments = paid, monthlyAmount = 10000L,
        originalPrice = 60000L, totalAmount = 60000L, paymentDay = day, reminderDaysBefore = before,
        paymentOverride = override
    )

    private fun institution(id: Long = 1, day: Int? = null, before: Int? = null) =
        CreditInstitutionEntity(id = id, name = "Nu", paymentDay = day, reminderDaysBefore = before)

    @Test
    fun sinCreditosNoHayAvisosDePago() {
        assertFalse(NotificationNeeds.hasPaymentReminders(emptyList(), emptyList()))
    }

    @Test
    fun avisoPropioDeLaCompraCuenta() {
        val c = credit(override = true, day = 10, before = 2)
        assertTrue(NotificationNeeds.hasPaymentReminders(listOf(c), emptyList()))
    }

    @Test
    fun avisoHeredadoDeLaInstitucionCuenta() {
        val c = credit(institutionId = 1)
        assertTrue(NotificationNeeds.hasPaymentReminders(listOf(c), listOf(institution(1, day = 15, before = 3))))
    }

    @Test
    fun sinDiasDeAnticipacionNoHayAviso() {
        val c = credit(institutionId = 1)
        assertFalse(NotificationNeeds.hasPaymentReminders(listOf(c), listOf(institution(1, day = 15, before = null))))
    }

    @Test
    fun unaCompraLiquidadaNoCuenta() {
        val c = credit(override = true, day = 10, before = 2, paid = 6, total = 6)
        assertFalse(NotificationNeeds.hasPaymentReminders(listOf(c), emptyList()))
    }

    @Test
    fun elPermisoSeNecesitaConRecordatorioDiarioOAvisosDePago() {
        assertFalse(NotificationNeeds.needsPermission(dailyReminderEnabled = false, hasPaymentReminders = false))
        assertTrue(NotificationNeeds.needsPermission(dailyReminderEnabled = true, hasPaymentReminders = false))
        assertTrue(NotificationNeeds.needsPermission(dailyReminderEnabled = false, hasPaymentReminders = true))
    }

    @Test
    fun soloSePideUnaVezEnAndroid13OMasYSiFalta() {
        assertTrue(NotificationNeeds.shouldRequestNow(needed = true, alreadyAsked = false, sdkInt = 34, granted = false))
        assertFalse(NotificationNeeds.shouldRequestNow(needed = false, alreadyAsked = false, sdkInt = 34, granted = false))
        assertFalse(NotificationNeeds.shouldRequestNow(needed = true, alreadyAsked = true, sdkInt = 34, granted = false))
        assertFalse(NotificationNeeds.shouldRequestNow(needed = true, alreadyAsked = false, sdkInt = 32, granted = false))
        assertFalse(NotificationNeeds.shouldRequestNow(needed = true, alreadyAsked = false, sdkInt = 34, granted = true))
        assertEquals(false, NotificationNeeds.shouldRequestNow(needed = true, alreadyAsked = false, sdkInt = 26, granted = false))
    }
}
