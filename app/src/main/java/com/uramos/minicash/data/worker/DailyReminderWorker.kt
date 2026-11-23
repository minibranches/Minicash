package com.uramos.minicash.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.uramos.minicash.MainActivity
import com.uramos.minicash.R
import com.uramos.minicash.data.local.MinicashDatabase
import java.util.Calendar

class DailyReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val database = MinicashDatabase.getDatabase(applicationContext)
        val transactionDao = database.transactionDao()

        val calendar = Calendar.getInstance()
        val startOfDay = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfDay = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val todayTransactionsCount = transactionDao.getTransactionsCountBetween(startOfDay, endOfDay)
        if (todayTransactionsCount == 0) {
            sendReminderNotification()
        }
        return Result.success()
    }

    private fun sendReminderNotification() {
        // Sin permiso (Android 13 o más) el aviso se perdería sin ningún error; no se intenta
        if (!androidx.core.app.NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) return
        val channelId = "daily_reminder_channel"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Recordatorios diarios",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Recordatorio para registrar transacciones diarias"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val largeIconBitmap = try {
            val drawable = androidx.core.content.ContextCompat.getDrawable(applicationContext, R.drawable.ic_minicash_coin)
            val bitmap = android.graphics.Bitmap.createBitmap(
                drawable?.intrinsicWidth?.takeIf { it > 0 } ?: 96,
                drawable?.intrinsicHeight?.takeIf { it > 0 } ?: 96,
                android.graphics.Bitmap.Config.ARGB_8888
            )
            val canvas = android.graphics.Canvas(bitmap)
            drawable?.setBounds(0, 0, canvas.width, canvas.height)
            drawable?.draw(canvas)
            bitmap
        } catch (e: Exception) {
            null
        }

        val notificationBuilder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification_coin)
            .setColor(0xFF1A73E8.toInt())
            .setContentTitle("Minicash")
            .setContentText("¿Registraste tus gastos de hoy?")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (largeIconBitmap != null) {
            notificationBuilder.setLargeIcon(largeIconBitmap)
        }

        notificationManager.notify(1001, notificationBuilder.build())
    }
}
