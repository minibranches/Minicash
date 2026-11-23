package com.uramos.minicash.data.util

import com.uramos.minicash.data.local.entities.CreditEntity
import com.uramos.minicash.data.local.entities.CreditInstitutionEntity

/** Decide si la app tiene algo programado que necesita el permiso de notificaciones. */
object NotificationNeeds {

    /**
     * Hay avisos de pago si alguna compra a meses sin liquidar tiene día de pago y días de anticipación,
     * propios o heredados de su institución ([PaymentSchedule.effectivePayment]).
     */
    fun hasPaymentReminders(credits: List<CreditEntity>, institutions: List<CreditInstitutionEntity>): Boolean {
        val byId = institutions.associateBy { it.id }
        return credits.any { credit ->
            if (credit.paidInstallments >= credit.totalInstallments) return@any false
            val (day, daysBefore) = PaymentSchedule.effectivePayment(credit, byId[credit.institutionId])
            day != null && daysBefore != null
        }
    }

    /** El permiso se necesita si hay recordatorio diario activo o avisos de pago. */
    fun needsPermission(dailyReminderEnabled: Boolean, hasPaymentReminders: Boolean): Boolean =
        dailyReminderEnabled || hasPaymentReminders

    /**
     * Si hay que pedir el permiso ahora: algo lo necesita, nunca se ha pedido (para no insistir tras un rechazo),
     * el sistema pide permiso en tiempo de ejecución (Android 13 o más) y aún no está concedido.
     */
    fun shouldRequestNow(needed: Boolean, alreadyAsked: Boolean, sdkInt: Int, granted: Boolean): Boolean =
        needed && !alreadyAsked && sdkInt >= 33 && !granted
}
