package com.uramos.minicash.ui.screens.settings

import com.uramos.minicash.ui.utils.toCents
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.uramos.minicash.data.local.MinicashDatabase
import com.uramos.minicash.data.local.backup.BackupManager
import com.uramos.minicash.data.local.dao.AccountDao
import com.uramos.minicash.data.local.dao.CategoryDao
import com.uramos.minicash.data.local.entities.AccountEntity
import com.uramos.minicash.data.local.entities.AccountType
import com.uramos.minicash.data.local.entities.CategoryEntity
import com.uramos.minicash.data.local.entities.CreditInstitutionEntity
import com.uramos.minicash.data.local.entities.TransactionType
import com.uramos.minicash.data.preferences.ThemeMode
import com.uramos.minicash.data.preferences.UserPreferences
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.uramos.minicash.data.worker.AutoBackupScheduler
import com.uramos.minicash.data.worker.CreditPaymentScheduler
import com.uramos.minicash.data.worker.ReminderScheduler

import com.uramos.minicash.data.preferences.FontPreference
import com.uramos.minicash.data.preferences.NavBarStyle

const val MAX_USER_NAME_LENGTH = 30

private data class ReminderPrefs(
    val lastBackup: Long,
    val enabled: Boolean,
    val hour: Int,
    val minute: Int,
    val autoBackupEnabled: Boolean,
    val autoBackupHour: Int,
    val autoBackupMinute: Int
)

private data class UiPrefs(
    val showBudget: Boolean,
    val showCredits: Boolean,
    val accentHex: String,
    val fontPref: FontPreference
)

private data class SettingsPrefs(
    val userName: String,
    val themeMode: ThemeMode,
    val fontPreference: FontPreference,
    val useDynamicColor: Boolean,
    val isAmoledMode: Boolean,
    val accentColorHex: String,
    val lastAutoBackupTimestamp: Long,
    val dailyReminderEnabled: Boolean,
    val dailyReminderHour: Int,
    val dailyReminderMinute: Int,
    val autoBackupEnabled: Boolean,
    val autoBackupHour: Int,
    val autoBackupMinute: Int,
    val showBudgetTab: Boolean,
    val showCreditsTab: Boolean
)

data class SettingsUiState(
    val userName: String = "",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val fontPreference: FontPreference = FontPreference.ROUNDED_QUICKSAND,
    val useDynamicColor: Boolean = false,
    val isAmoledMode: Boolean = false,
    val accentColorHex: String = "#1A73E8",
    val dailyReminderEnabled: Boolean = false,
    val dailyReminderHour: Int = 21,
    val dailyReminderMinute: Int = 0,
    val autoBackupEnabled: Boolean = true,
    val autoBackupHour: Int = 23,
    val autoBackupMinute: Int = 0,
    val showBudgetTab: Boolean = true,
    val showCreditsTab: Boolean = true,
    val accounts: List<AccountEntity> = emptyList(),
    val expenseCategories: List<CategoryEntity> = emptyList(),
    val incomeCategories: List<CategoryEntity> = emptyList(),
    val creditInstitutions: List<CreditInstitutionEntity> = emptyList(),
    val lastAutoBackupTimestamp: Long = 0L
)

class SettingsViewModel(
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val userPreferences: UserPreferences,
    private val database: MinicashDatabase,
    /** Estilo de barra ya leído al arrancar, para que la primera composición no muestre el predeterminado. */
    initialNavBarStyle: NavBarStyle = NavBarStyle.INTEGRATED
) : ViewModel() {

    private val creditDao = database.creditDao()
    private val backupManager = BackupManager(database, userPreferences)
    private val creditInstitutionDao = database.creditInstitutionDao()

    private val _messageEvent = MutableSharedFlow<String>()
    val messageEvent: SharedFlow<String> = _messageEvent.asSharedFlow()

    private val reminderPrefsFlow = combine(
        userPreferences.lastAutoBackupTimestamp,
        userPreferences.dailyReminderEnabled,
        userPreferences.dailyReminderHour,
        userPreferences.dailyReminderMinute,
        userPreferences.autoBackupEnabled,
        userPreferences.autoBackupHour,
        userPreferences.autoBackupMinute
    ) { args ->
        ReminderPrefs(
            lastBackup = args[0] as Long,
            enabled = args[1] as Boolean,
            hour = args[2] as Int,
            minute = args[3] as Int,
            autoBackupEnabled = args[4] as Boolean,
            autoBackupHour = args[5] as Int,
            autoBackupMinute = args[6] as Int
        )
    }

    private val uiPrefsFlow = combine(
        userPreferences.showBudgetTab,
        userPreferences.showCreditsTab,
        userPreferences.accentColorHex,
        userPreferences.fontPreference
    ) { showBudget, showCredits, accentHex, fontPref ->
        UiPrefs(showBudget, showCredits, accentHex, fontPref)
    }

    private val preferencesFlow = combine(
        userPreferences.userName,
        userPreferences.themeMode,
        userPreferences.useDynamicColor,
        userPreferences.isAmoledMode,
        uiPrefsFlow,
        reminderPrefsFlow
    ) { args: Array<Any> ->
        val name = args[0] as String
        val mode = args[1] as ThemeMode
        val dynamicColor = args[2] as Boolean
        val amoled = args[3] as Boolean
        val uiPrefs = args[4] as UiPrefs
        val rem = args[5] as ReminderPrefs
        SettingsPrefs(
            userName = name,
            themeMode = mode,
            fontPreference = uiPrefs.fontPref,
            useDynamicColor = dynamicColor,
            isAmoledMode = amoled,
            accentColorHex = uiPrefs.accentHex,
            lastAutoBackupTimestamp = rem.lastBackup,
            dailyReminderEnabled = rem.enabled,
            dailyReminderHour = rem.hour,
            dailyReminderMinute = rem.minute,
            autoBackupEnabled = rem.autoBackupEnabled,
            autoBackupHour = rem.autoBackupHour,
            autoBackupMinute = rem.autoBackupMinute,
            showBudgetTab = uiPrefs.showBudget,
            showCreditsTab = uiPrefs.showCredits
        )
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesFlow,
        accountDao.getAllAccounts(),
        categoryDao.getAllActiveCategories(),
        creditInstitutionDao.getAllInstitutions()
    ) { prefs, accounts, categories, institutions ->
        SettingsUiState(
            userName = prefs.userName,
            themeMode = prefs.themeMode,
            fontPreference = prefs.fontPreference,
            useDynamicColor = prefs.useDynamicColor,
            isAmoledMode = prefs.isAmoledMode,
            accentColorHex = prefs.accentColorHex,
            dailyReminderEnabled = prefs.dailyReminderEnabled,
            dailyReminderHour = prefs.dailyReminderHour,
            dailyReminderMinute = prefs.dailyReminderMinute,
            autoBackupEnabled = prefs.autoBackupEnabled,
            autoBackupHour = prefs.autoBackupHour,
            autoBackupMinute = prefs.autoBackupMinute,
            showBudgetTab = prefs.showBudgetTab,
            showCreditsTab = prefs.showCreditsTab,
            lastAutoBackupTimestamp = prefs.lastAutoBackupTimestamp,
            accounts = accounts,
            expenseCategories = categories.filter { it.type == TransactionType.EXPENSE },
            incomeCategories = categories.filter { it.type == TransactionType.INCOME },
            creditInstitutions = institutions
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setDailyReminderEnabled(context: Context, enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setDailyReminderEnabled(enabled)
            if (enabled) {
                val hour = uiState.value.dailyReminderHour
                val minute = uiState.value.dailyReminderMinute
                ReminderScheduler.scheduleDailyReminder(context, hour, minute)
            } else {
                ReminderScheduler.cancelDailyReminder(context)
            }
        }
    }

    fun setDailyReminderTime(context: Context, hour: Int, minute: Int) {
        viewModelScope.launch {
            userPreferences.setDailyReminderTime(hour, minute)
            if (uiState.value.dailyReminderEnabled) {
                ReminderScheduler.scheduleDailyReminder(context, hour, minute)
            }
        }
    }

    fun setAutoBackupEnabled(context: Context, enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setAutoBackupEnabled(enabled)
            if (enabled) {
                val hour = uiState.value.autoBackupHour
                val minute = uiState.value.autoBackupMinute
                AutoBackupScheduler.scheduleAutoBackup(context, hour, minute)
            } else {
                AutoBackupScheduler.cancelAutoBackup(context)
            }
        }
    }

    fun setAutoBackupTime(context: Context, hour: Int, minute: Int) {
        viewModelScope.launch {
            userPreferences.setAutoBackupTime(hour, minute)
            if (uiState.value.autoBackupEnabled) {
                AutoBackupScheduler.scheduleAutoBackup(context, hour, minute)
            }
        }
    }

    fun setUserName(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            userPreferences.setUserName(trimmed.take(MAX_USER_NAME_LENGTH))
            _messageEvent.emit("Nombre actualizado")
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            userPreferences.setThemeMode(mode)
        }
    }

    val hideInRecents: StateFlow<Boolean> = userPreferences.hideInRecents
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val appLockEnabled: StateFlow<Boolean> = userPreferences.appLockEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val appLockTimeoutMinutes: StateFlow<Int> = userPreferences.appLockTimeoutMinutes
        .stateIn(viewModelScope, SharingStarted.Eagerly, 1)

    fun setAppLockEnabled(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setAppLockEnabled(enabled) }
    }

    fun setAppLockTimeoutMinutes(minutes: Int) {
        viewModelScope.launch { userPreferences.setAppLockTimeoutMinutes(minutes) }
    }

    val refreshRateLimit: StateFlow<Int> = userPreferences.refreshRateLimit
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun setRefreshRateLimit(hz: Int) {
        viewModelScope.launch { userPreferences.setRefreshRateLimit(hz) }
    }

    fun setHideInRecents(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setHideInRecents(enabled) }
    }

    val navBarStyle: StateFlow<NavBarStyle> = userPreferences.navBarStyle
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialNavBarStyle)

    val creditReminderTime: StateFlow<Pair<Int, Int>> = combine(
        userPreferences.creditReminderHour,
        userPreferences.creditReminderMinute
    ) { hour, minute -> hour to minute }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 9 to 0)

    fun setCreditReminderTime(context: Context, hour: Int, minute: Int) {
        viewModelScope.launch {
            userPreferences.setCreditReminderTime(hour, minute)
            CreditPaymentScheduler.schedule(context, hour, minute)
        }
    }

    fun setNavBarStyle(style: NavBarStyle) {
        viewModelScope.launch {
            userPreferences.setNavBarStyle(style)
        }
    }

    fun setFontPreference(pref: FontPreference) {
        viewModelScope.launch {
            userPreferences.setFontPreference(pref)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setDynamicColor(enabled)
        }
    }

    fun setAmoledMode(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setAmoledMode(enabled)
        }
    }

    fun setShowBudgetTab(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setShowBudgetTab(enabled)
        }
    }

    fun setShowCreditsTab(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setShowCreditsTab(enabled)
        }
    }

    fun setAccentColor(hex: String) {
        viewModelScope.launch {
            userPreferences.setAccentColor(hex)
        }
    }

    fun addAccount(name: String, type: AccountType, initialBalanceDouble: Double, colorHex: String = "#1A73E8", iconName: String = "account_balance_wallet") {
        val balanceCents = initialBalanceDouble.toCents()
        viewModelScope.launch {
            accountDao.insertAccount(
                AccountEntity(
                    name = name,
                    type = type,
                    balance = balanceCents,
                    colorHex = colorHex,
                    iconName = iconName
                )
            )
        }
    }

    fun updateAccount(account: AccountEntity, newName: String, newType: AccountType, newBalanceCents: Long, newColorHex: String, newIconName: String = account.iconName) {
        viewModelScope.launch {
            accountDao.updateAccount(
                account.copy(
                    name = newName,
                    type = newType,
                    balance = newBalanceCents,
                    colorHex = newColorHex,
                    iconName = newIconName
                )
            )
        }
    }

    fun deleteAccount(account: AccountEntity) {
        viewModelScope.launch {
            accountDao.deleteAccount(account)
        }
    }

    fun toggleAccountVisibility(account: AccountEntity) {
        viewModelScope.launch {
            accountDao.updateAccount(account.copy(isVisibleInHome = !account.isVisibleInHome))
        }
    }

    /** Guarda el orden definitivo de las cuentas al terminar de arrastrar. */
    fun saveAccountOrder(orderedIds: List<Long>) {
        viewModelScope.launch {
            val byId = uiState.value.accounts.associateBy { it.id }
            val updated = orderedIds.mapIndexedNotNull { index, id -> byId[id]?.copy(displayOrder = index) }
            if (updated.isNotEmpty()) accountDao.updateAccounts(updated)
        }
    }

    fun addCategory(name: String, type: TransactionType, colorHex: String = "#1A73E8", iconName: String = "category") {
        viewModelScope.launch {
            categoryDao.insertCategories(
                listOf(CategoryEntity(name = name, type = type, colorHex = colorHex, iconName = iconName))
            )
        }
    }

    fun updateCategory(category: CategoryEntity, newName: String, newColorHex: String = category.colorHex, newIconName: String = category.iconName) {
        viewModelScope.launch {
            categoryDao.updateCategory(category.copy(name = newName, colorHex = newColorHex, iconName = newIconName))
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            categoryDao.deleteCategory(category)
        }
    }

    fun addCreditInstitution(name: String, colorHex: String = "#1A73E8", paymentDay: Int? = null, reminderDaysBefore: Int? = null) {
        if (name.isBlank()) return
        viewModelScope.launch {
            creditInstitutionDao.insertInstitution(
                CreditInstitutionEntity(
                    name = name, colorHex = colorHex,
                    paymentDay = paymentDay, reminderDaysBefore = reminderDaysBefore
                )
            )
        }
    }

    fun updateCreditInstitution(
        institution: CreditInstitutionEntity,
        newName: String,
        newColorHex: String = institution.colorHex,
        newPaymentDay: Int? = institution.paymentDay,
        newReminderDaysBefore: Int? = institution.reminderDaysBefore
    ) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            val trimmed = newName.trim()
            // Nombre en el catálogo y en los créditos ligados cambian juntos.
            database.withTransaction {
                creditInstitutionDao.updateInstitution(institution.copy(name = trimmed, colorHex = newColorHex, paymentDay = newPaymentDay, reminderDaysBefore = newReminderDaysBefore))
                creditDao.renameInstitution(institution.id, trimmed)
            }
        }
    }

    fun deleteCreditInstitution(institution: CreditInstitutionEntity) {
        viewModelScope.launch {
            creditInstitutionDao.deleteInstitution(institution)
        }
    }

    fun exportBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            backupManager.exportBackup(context, uri)
                .onSuccess {
                    _messageEvent.emit("Copia de seguridad exportada con éxito")
                }
                .onFailure {
                    _messageEvent.emit("Error al exportar copia de seguridad: ${it.localizedMessage ?: "Error desconocido"}")
                }
        }
    }

    fun exportCsv(context: Context, uri: Uri, range: com.uramos.minicash.data.util.CsvRange) {
        viewModelScope.launch {
            backupManager.exportCsv(context, uri, range)
                .onSuccess { count -> _messageEvent.emit("Se exportaron $count movimientos") }
                .onFailure { _messageEvent.emit("Error al exportar el CSV: ${it.localizedMessage ?: "Error desconocido"}") }
        }
    }

    fun wipeAllData(context: Context) {
        viewModelScope.launch {
            backupManager.wipeAllData(context)
                .onSuccess { _messageEvent.emit("Se borraron todos los datos. Antes se guardó una copia interna.") }
                .onFailure { _messageEvent.emit("Error al borrar los datos: ${it.localizedMessage ?: "Error desconocido"}") }
        }
    }

    fun importBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            backupManager.importBackup(context, uri)
                .onSuccess {
                    _messageEvent.emit("Copia de seguridad restaurada correctamente")
                }
                .onFailure { err ->
                    val errorMsg = err.localizedMessage ?: "El archivo de respaldo no tiene un formato válido para Minicash."
                    _messageEvent.emit("Error al restaurar respaldo: $errorMsg")
                }
        }
    }
}
