package com.uramos.minicash.data.local.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.uramos.minicash.data.local.MinicashDatabase
import com.uramos.minicash.data.local.entities.CreditInstitutionEntity
import com.uramos.minicash.data.local.entities.TransactionType
import com.uramos.minicash.data.preferences.UserPreferences
import com.uramos.minicash.data.worker.AutoBackupScheduler
import com.uramos.minicash.data.worker.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class BackupManager(
    private val database: MinicashDatabase,
    private val userPreferences: UserPreferences
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    fun validateBackupStructure(backup: MinicashBackup?): Boolean {
        if (backup == null) return false
        if (backup.version !in 1..BACKUP_FORMAT_VERSION) return false
        val invalidTx = backup.transactions.any { it.amount <= 0L && it.type != TransactionType.TRANSFER }
        return !invalidTx
    }

    suspend fun exportBackup(context: Context, uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val jsonString = gson.toJson(buildBackup(database, userPreferences))
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
                outputStream.flush()
            } ?: throw IOException("No se pudo abrir el archivo para escritura.")
        }
    }

    suspend fun importBackup(context: Context, uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            } ?: throw IOException("No se pudo abrir el archivo para lectura.")

            val backup = try {
                gson.fromJson(jsonString, MinicashBackup::class.java)
            } catch (_: Exception) {
                throw IllegalArgumentException("El archivo de respaldo no tiene un formato válido para Minicash.")
            }

            if (!validateBackupStructure(backup)) {
                throw IllegalArgumentException("El archivo de respaldo no tiene un formato válido para Minicash.")
            }

            // Red de seguridad: copia de los datos actuales antes de reemplazarlos.
            writePreRestoreCopy(context)

            database.withTransaction {
                // 1. Limpiar registros existentes respetando restricciones de llave foránea
                database.transactionDao().clearAllTransactions()
                database.budgetDao().clearAllBudgetItems()
                database.creditDao().clearAllCredits()
                database.accountDao().clearAllAccounts()
                database.categoryDao().clearAllCategories()

                // 2. Instituciones primero (los créditos las referencian por llave foránea).
                //    Solo existen en respaldos v2+; un v1 no debe borrar el catálogo actual.
                val institutionDao = database.creditInstitutionDao()
                backup.creditInstitutions?.let { institutions ->
                    institutionDao.clearAllInstitutions()
                    if (institutions.isNotEmpty()) {
                        institutionDao.insertInstitutions(institutions)
                    }
                }

                // 3. Insertar registros del archivo de respaldo
                if (backup.accounts.isNotEmpty()) {
                    database.accountDao().insertAccounts(backup.accounts)
                }
                if (backup.categories.isNotEmpty()) {
                    database.categoryDao().insertCategories(backup.categories)
                }
                if (backup.transactions.isNotEmpty()) {
                    database.transactionDao().insertTransactions(backup.transactions)
                }
                if (backup.budgetItems.isNotEmpty()) {
                    database.budgetDao().insertItems(backup.budgetItems)
                }
                if (backup.credits.isNotEmpty()) {
                    // Respaldos v1/v2 traen solo el nombre: se enlaza con el catálogo y, si falta, se crea.
                    val catalog = institutionDao.getAllInstitutionsList().toMutableList()
                    val linked = backup.credits.map { credit ->
                        val name = credit.institution.trim()
                        val match = catalog.firstOrNull { it.id == credit.institutionId }
                            ?: catalog.firstOrNull { it.name.equals(name, ignoreCase = true) }
                            ?: if (name.isNotEmpty()) {
                                val created = CreditInstitutionEntity(name = name)
                                created.copy(id = institutionDao.insertInstitution(created)).also { catalog += it }
                            } else null
                        credit.copy(
                            institution = match?.name ?: credit.institution,
                            institutionId = match?.id
                        )
                    }
                    database.creditDao().insertCredits(linked)
                }
            }

            // 4. Ajustes (fuera de la transacción de Room) y reprogramación de tareas
            backup.preferences?.let { snapshot ->
                userPreferences.restoreSnapshot(snapshot)
                rescheduleWorkers(context)
            }
            Unit
        }
    }

    private suspend fun writePreRestoreCopy(context: Context) {
        val dir = File(context.filesDir, "backups").apply { mkdirs() }
        val file = File(dir, "$PRE_RESTORE_PREFIX${System.currentTimeMillis()}.json")
        file.writeText(gson.toJson(buildBackup(database, userPreferences)), Charsets.UTF_8)
        dir.listFiles { _, name -> name.startsWith(PRE_RESTORE_PREFIX) && name.endsWith(".json") }
            ?.sortedByDescending { it.lastModified() }
            ?.drop(3)
            ?.forEach { it.delete() }
    }

    private suspend fun rescheduleWorkers(context: Context) {
        if (userPreferences.dailyReminderEnabled.first()) {
            ReminderScheduler.scheduleDailyReminder(
                context,
                userPreferences.dailyReminderHour.first(),
                userPreferences.dailyReminderMinute.first()
            )
        } else {
            ReminderScheduler.cancelDailyReminder(context)
        }
        if (userPreferences.autoBackupEnabled.first()) {
            AutoBackupScheduler.scheduleAutoBackup(
                context,
                userPreferences.autoBackupHour.first(),
                userPreferences.autoBackupMinute.first()
            )
        } else {
            AutoBackupScheduler.cancelAutoBackup(context)
        }
    }

    companion object {
        const val PRE_RESTORE_PREFIX = "minicash_pre_restore_"

        /** Única fuente del contenido del respaldo, compartida por exportación manual y automática. */
        suspend fun buildBackup(database: MinicashDatabase, userPreferences: UserPreferences): MinicashBackup {
            return MinicashBackup(
                version = BACKUP_FORMAT_VERSION,
                exportDate = System.currentTimeMillis(),
                accounts = database.accountDao().getAllAccountsList(),
                categories = database.categoryDao().getAllCategoriesList(),
                transactions = database.transactionDao().getAllTransactionsList(),
                budgetItems = database.budgetDao().getAllBudgetItemsList(),
                credits = database.creditDao().getAllCreditsList(),
                creditInstitutions = database.creditInstitutionDao().getAllInstitutionsList(),
                preferences = userPreferences.exportSnapshot()
            )
        }
    }
}
