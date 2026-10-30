package com.uramos.minicash.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.uramos.minicash.data.local.MinicashDatabase
import com.uramos.minicash.data.local.backup.AutoBackupManager
import com.uramos.minicash.data.preferences.UserPreferences
import java.util.Calendar
import java.util.concurrent.TimeUnit

class AutoBackupWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val database = MinicashDatabase.getDatabase(applicationContext)
        val userPreferences = UserPreferences(applicationContext)
        return AutoBackupManager.performAutoBackup(applicationContext, database, userPreferences)
            .fold(onSuccess = { Result.success() }, onFailure = { Result.retry() })
    }
}

object AutoBackupScheduler {
    const val WORK_TAG = "auto_backup_work"

    fun scheduleAutoBackup(context: Context, hourOfDay: Int = 23, minute: Int = 0) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val initialDelay = target.timeInMillis - now.timeInMillis

        val backupRequest = PeriodicWorkRequestBuilder<AutoBackupWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag(WORK_TAG)
            .build()

        WorkManager.getInstance(context.getApplicatonContextOrSelf()).enqueueUniquePeriodicWork(
            WORK_TAG,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            backupRequest
        )
    }

    fun cancelAutoBackup(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_TAG)
    }

    private fun Context.getApplicatonContextOrSelf(): Context {
        return applicationContext ?: this
    }
}
