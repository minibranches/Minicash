package com.uramos.minicash.data.util

import com.uramos.minicash.data.local.entities.CreditEntity
import com.uramos.minicash.data.local.entities.CreditInstitutionEntity
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/** Cálculo de fechas de pago de compras a meses con un día fijo del mes. */
object PaymentSchedule {

    /**
     * Día de pago y aviso que aplican a [credit]: los propios si los sobrescribe, y si no los de su
     * [institution] (null si la compra no tiene institución).
     */
    fun effectivePayment(credit: CreditEntity, institution: CreditInstitutionEntity?): Pair<Int?, Int?> =
        if (credit.paymentOverride) credit.paymentDay to credit.reminderDaysBefore
        else institution?.let { it.paymentDay to it.reminderDaysBefore } ?: (null to null)

    /** Fecha del pago en [month]; si el mes no tiene ese día (29, 30, 31) se usa el último día del mes. */
    fun dateIn(month: YearMonth, paymentDay: Int): LocalDate =
        month.atDay(paymentDay.coerceIn(1, month.lengthOfMonth()))

    /** Próximo pago a partir de [today] (hoy cuenta si es el día de pago). */
    fun nextPayment(paymentDay: Int, today: LocalDate): LocalDate {
        val thisMonth = dateIn(YearMonth.from(today), paymentDay)
        return if (!thisMonth.isBefore(today)) thisMonth else dateIn(YearMonth.from(today).plusMonths(1), paymentDay)
    }

    fun daysUntilNextPayment(paymentDay: Int, today: LocalDate): Long =
        ChronoUnit.DAYS.between(today, nextPayment(paymentDay, today))

    /** `true` si hoy toca avisar: faltan exactamente [daysBefore] días para el próximo pago (0 = el mismo día). */
    fun shouldNotify(paymentDay: Int, daysBefore: Int, today: LocalDate): Boolean =
        daysUntilNextPayment(paymentDay, today) == daysBefore.toLong()
}