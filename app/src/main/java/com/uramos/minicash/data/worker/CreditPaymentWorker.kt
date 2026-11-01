package com.uramos.minicash.data.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.uramos.minicash.MainActivity
import com.uramos.minicash.R
import com.uramos.minicash.data.local.MinicashDatabase
import com.uramos.minicash.data.local.entities.CreditEntity
import com.uramos.minicash.data.util.PaymentSchedule
import com.uramos.minicash.ui.utils.toCurrencyFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Revisa una vez al día las compras a meses con día de pago y aviso configurados, y notifica las
 * que vencen exactamente dentro de los días de anticipación elegidos.
 */
class CreditPaymentWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val database = MinicashDatabase.getDatabase(applicationContext)
        val credits = database.creditDao().getAllCreditsList()
        val institutions = database.creditInstitutionDao().getAllInstitutionsList().associateBy { it.id }
        val today = LocalDate.now()
        val due = credits.mapNotNull { credit ->
            val (day, daysBefore) = PaymentSchedule.effectivePayment(credit, institutions[credit.institutionId])
            if (day == null || daysBefore == null) return@mapNotNull null
            if (credit.paidInstallments >= credit.totalInstallments) return@mapNotNull null
            if (!PaymentSchedule.shouldNotify(day, daysBefore, today)) return@mapNotNull null
            credit to day
        }
        if (due.isNotEmpty()) sendNotification(due, today)
        return Result.success()
    }

    private fun sendNotification(due: List<Pair<CreditEntity, Int>>, today: LocalDate) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Pagos de compras a meses", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Avisos antes de la fecha de pago de tus compras a meses"
                }
            )
        }

        val lines = due.map { (credit, day) ->
            val days = PaymentSchedule.daysUntilNextPayment(day, today)
            val whenText = when (days) {
                0L -> "hoy"
                1L -> "mañana"
                else -> "en $days días"
            }
            "${credit.article}: ${credit.monthlyAmount.toCurrencyFormat()} ($whenText)"
        }
        val title = if (due.size == 1) "Pago próximo" else "${due.size} pagos próximos"

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_coin)
            .setColor(0xFF1A73E8.toInt())
            .setContentTitle(title)
            .setContentText(lines.first())
            .setStyle(NotificationCompat.BigTextStyle().bigText(lines.joinToString("\n")))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val CHANNEL_ID = "credit_payment_channel"
        private const val NOTIFICATION_ID = 1002
        const val WORK_NAME = "credit_payment_work"
    }
}

object CreditPaymentScheduler {
    /** Programa la revisión diaria a la hora indicada; reemplaza la programación anterior. */
    fun schedule(context: Context, hourOfDay: Int = 9, minute: Int = 0) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        val request = PeriodicWorkRequestBuilder<CreditPaymentWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(target.timeInMillis - now.timeInMillis, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
            CreditPaymentWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}