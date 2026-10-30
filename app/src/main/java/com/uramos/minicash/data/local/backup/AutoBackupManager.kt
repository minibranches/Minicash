package com.uramos.minicash.data.local.backup

import android.content.Context
import com.google.gson.GsonBuilder
import com.uramos.minicash.data.local.MinicashDatabase
import com.uramos.minicash.data.preferences.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File

object AutoBackupManager {

    suspend fun checkAndTriggerAutoBackup(
        context: Context,
        database: MinicashDatabase,
        userPreferences: UserPreferences
    ) {
        val lastTimestamp = userPreferences.lastAutoBackupTimestamp.first()
        val now = System.currentTimeMillis()
        val twentyFourHours = 24 * 60 * 60 * 1000L
        if (now - lastTimestamp >= twentyFourHours) {
            performAutoBackup(context, database, userPreferences)
        }
    }

    suspend fun performAutoBackup(
        context: Context,
        database: MinicashDatabase,
        userPreferences: UserPreferences
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val backup = BackupManager.buildBackup(database, userPreferences)

            val gson = GsonBuilder().setPrettyPrinting().create()
            val jsonString = gson.toJson(backup)

            val backupDir = File(context.filesDir, "backups")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            val timestamp = System.currentTimeMillis()
            val backupFile = File(backupDir, "minicash_auto_backup_$timestamp.json")
            backupFile.writeText(jsonString, Charsets.UTF_8)

            // Rotación: Conservar únicamente las 3 copias más recientes
            val autoBackups = backupDir.listFiles { _, name ->
                name.startsWith("minicash_auto_backup_") && name.endsWith(".json")
            }?.sortedByDescending { it.lastModified() } ?: emptyList()

            if (autoBackups.size > 3) {
                autoBackups.drop(3).forEach { oldFile ->
                    oldFile.delete()
                }
            }

            userPreferences.saveLastAutoBackupTimestamp(timestamp)
            backupFile
        }
    }
}
