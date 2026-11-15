package com.uramos.minicash.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.uramos.minicash.data.util.addRecentSearch
import com.uramos.minicash.data.util.decodeRecentSearches
import com.uramos.minicash.data.util.encodeRecentSearches
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_settings")

enum class ThemeMode(val displayName: String) {
    SYSTEM("Sistema"),
    LIGHT("Claro"),
    DARK("Oscuro")
}

enum class FontPreference {
    ROUNDED_QUICKSAND,
    GOOGLE_SANS_FLEX
}

/** Estilo de la barra de navegación: cápsula flotante o barra integrada al marco de la pantalla. */
enum class NavBarStyle {
    FLOATING,
    INTEGRATED
}

class UserPreferences(private val context: Context) {

    companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val USER_NAME = stringPreferencesKey("user_name")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val FONT_PREFERENCE_KEY = stringPreferencesKey("font_preference")
        val DYNAMIC_COLOR_KEY = booleanPreferencesKey("use_dynamic_color")
        val AMOLED_MODE_KEY = booleanPreferencesKey("is_amoled_mode")
        val HIDE_IN_RECENTS_KEY = booleanPreferencesKey("hide_in_recents")
        val LAST_AUTO_BACKUP_TIMESTAMP = longPreferencesKey("last_auto_backup_timestamp")
        val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
        val DAILY_REMINDER_HOUR = androidx.datastore.preferences.core.intPreferencesKey("daily_reminder_hour")
        val DAILY_REMINDER_MINUTE = androidx.datastore.preferences.core.intPreferencesKey("daily_reminder_minute")
        val AUTO_BACKUP_ENABLED = booleanPreferencesKey("auto_backup_enabled")
        val AUTO_BACKUP_HOUR = androidx.datastore.preferences.core.intPreferencesKey("auto_backup_hour")
        val AUTO_BACKUP_MINUTE = androidx.datastore.preferences.core.intPreferencesKey("auto_backup_minute")
        val SHOW_BUDGET_TAB = booleanPreferencesKey("show_budget_tab")
        val SHOW_CREDITS_TAB = booleanPreferencesKey("show_credits_tab")
        val ACCENT_COLOR_HEX = stringPreferencesKey("accent_color_hex")
        val NAV_BAR_STYLE_KEY = stringPreferencesKey("nav_bar_style")
        val CREDIT_REMINDER_HOUR = androidx.datastore.preferences.core.intPreferencesKey("credit_reminder_hour")
        val CREDIT_REMINDER_MINUTE = androidx.datastore.preferences.core.intPreferencesKey("credit_reminder_minute")
        val RECENT_SEARCHES = stringPreferencesKey("recent_searches")
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val APP_LOCK_TIMEOUT_MINUTES = androidx.datastore.preferences.core.intPreferencesKey("app_lock_timeout_minutes")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[ONBOARDING_COMPLETED] ?: false
    }

    val userName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[USER_NAME] ?: ""
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        val rawName = prefs[THEME_MODE] ?: ThemeMode.SYSTEM.name
        runCatching { ThemeMode.valueOf(rawName) }.getOrDefault(ThemeMode.SYSTEM)
    }

    val useDynamicColor: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[DYNAMIC_COLOR_KEY] ?: false
    }

    /** Oculta la vista previa de la app en multitarea (y bloquea capturas de pantalla). */
    val hideInRecents: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[HIDE_IN_RECENTS_KEY] ?: false
    }

    suspend fun setHideInRecents(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[HIDE_IN_RECENTS_KEY] = enabled }
    }

    val isAmoledMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[AMOLED_MODE_KEY] ?: false
    }

    val lastAutoBackupTimestamp: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[LAST_AUTO_BACKUP_TIMESTAMP] ?: 0L
    }

    val dailyReminderEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[DAILY_REMINDER_ENABLED] ?: false
    }

    val dailyReminderHour: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[DAILY_REMINDER_HOUR] ?: 21
    }

    val dailyReminderMinute: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[DAILY_REMINDER_MINUTE] ?: 0
    }

    val autoBackupEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[AUTO_BACKUP_ENABLED] ?: true
    }

    val autoBackupHour: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[AUTO_BACKUP_HOUR] ?: 23
    }

    val autoBackupMinute: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[AUTO_BACKUP_MINUTE] ?: 0
    }

    val showBudgetTab: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SHOW_BUDGET_TAB] ?: true
    }

    val showCreditsTab: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SHOW_CREDITS_TAB] ?: true
    }

    val accentColorHex: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[ACCENT_COLOR_HEX] ?: "#1A73E8"
    }

    val fontPreference: Flow<FontPreference> = context.dataStore.data.map { prefs ->
        val rawName = prefs[FONT_PREFERENCE_KEY] ?: FontPreference.ROUNDED_QUICKSAND.name
        runCatching { FontPreference.valueOf(rawName) }.getOrDefault(FontPreference.ROUNDED_QUICKSAND)
    }

    val navBarStyle: Flow<NavBarStyle> = context.dataStore.data.map { prefs ->
        val rawName = prefs[NAV_BAR_STYLE_KEY] ?: NavBarStyle.INTEGRATED.name
        runCatching { NavBarStyle.valueOf(rawName) }.getOrDefault(NavBarStyle.INTEGRATED)
    }

    val creditReminderHour: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[CREDIT_REMINDER_HOUR] ?: 9
    }

    val creditReminderMinute: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[CREDIT_REMINDER_MINUTE] ?: 0
    }

    /** Búsquedas recientes de Movimientos, la más nueva primero. */
    val recentSearches: Flow<List<String>> = context.dataStore.data.map { prefs ->
        decodeRecentSearches(prefs[RECENT_SEARCHES])
    }

    suspend fun addRecentSearch(query: String) {
        context.dataStore.edit { prefs ->
            val updated = addRecentSearch(decodeRecentSearches(prefs[RECENT_SEARCHES]), query)
            prefs[RECENT_SEARCHES] = encodeRecentSearches(updated)
        }
    }

    suspend fun clearRecentSearches() {
        context.dataStore.edit { prefs -> prefs.remove(RECENT_SEARCHES) }
    }

    /** Bloqueo de la app con la huella, el rostro o el PIN del dispositivo. */
    val appLockEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[APP_LOCK_ENABLED] ?: false
    }

    /** Minutos en segundo plano tras los cuales se vuelve a pedir el desbloqueo; 0 = cada vez. */
    val appLockTimeoutMinutes: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[APP_LOCK_TIMEOUT_MINUTES] ?: 1
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[APP_LOCK_ENABLED] = enabled }
    }

    suspend fun setAppLockTimeoutMinutes(minutes: Int) {
        context.dataStore.edit { prefs -> prefs[APP_LOCK_TIMEOUT_MINUTES] = minutes }
    }

    suspend fun setCreditReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit { prefs ->
            prefs[CREDIT_REMINDER_HOUR] = hour
            prefs[CREDIT_REMINDER_MINUTE] = minute
        }
    }

    suspend fun saveOnboardingState(completed: Boolean, name: String) {
        context.dataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETED] = completed
            prefs[USER_NAME] = name
        }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_NAME] = name
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[THEME_MODE] = mode.name
        }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DYNAMIC_COLOR_KEY] = enabled
        }
    }

    suspend fun setAmoledMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[AMOLED_MODE_KEY] = enabled
        }
    }

    suspend fun saveLastAutoBackupTimestamp(timestamp: Long) {
        context.dataStore.edit { prefs ->
            prefs[LAST_AUTO_BACKUP_TIMESTAMP] = timestamp
        }
    }

    suspend fun setDailyReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DAILY_REMINDER_ENABLED] = enabled
        }
    }

    suspend fun setDailyReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit { prefs ->
            prefs[DAILY_REMINDER_HOUR] = hour
            prefs[DAILY_REMINDER_MINUTE] = minute
        }
    }

    suspend fun setAutoBackupEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[AUTO_BACKUP_ENABLED] = enabled
        }
    }

    suspend fun setAutoBackupTime(hour: Int, minute: Int) {
        context.dataStore.edit { prefs ->
            prefs[AUTO_BACKUP_HOUR] = hour
            prefs[AUTO_BACKUP_MINUTE] = minute
        }
    }

    suspend fun setShowBudgetTab(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SHOW_BUDGET_TAB] = enabled
        }
    }

    suspend fun setShowCreditsTab(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SHOW_CREDITS_TAB] = enabled
        }
    }

    suspend fun setAccentColor(hex: String) {
        context.dataStore.edit { prefs ->
            prefs[ACCENT_COLOR_HEX] = hex
        }
    }

    suspend fun setFontPreference(pref: FontPreference) {
        context.dataStore.edit { prefs ->
            prefs[FONT_PREFERENCE_KEY] = pref.name
        }
    }

    suspend fun setNavBarStyle(style: NavBarStyle) {
        context.dataStore.edit { prefs ->
            prefs[NAV_BAR_STYLE_KEY] = style.name
        }
    }

    suspend fun exportSnapshot(): PreferencesSnapshot {
        val prefs = context.dataStore.data.first()
        return PreferencesSnapshot(
            userName = prefs[USER_NAME],
            themeMode = prefs[THEME_MODE],
            fontPreference = prefs[FONT_PREFERENCE_KEY],
            useDynamicColor = prefs[DYNAMIC_COLOR_KEY],
            isAmoledMode = prefs[AMOLED_MODE_KEY],
            hideInRecents = prefs[HIDE_IN_RECENTS_KEY],
            accentColorHex = prefs[ACCENT_COLOR_HEX],
            dailyReminderEnabled = prefs[DAILY_REMINDER_ENABLED],
            dailyReminderHour = prefs[DAILY_REMINDER_HOUR],
            dailyReminderMinute = prefs[DAILY_REMINDER_MINUTE],
            autoBackupEnabled = prefs[AUTO_BACKUP_ENABLED],
            autoBackupHour = prefs[AUTO_BACKUP_HOUR],
            autoBackupMinute = prefs[AUTO_BACKUP_MINUTE],
            showBudgetTab = prefs[SHOW_BUDGET_TAB],
            showCreditsTab = prefs[SHOW_CREDITS_TAB],
            navBarStyle = prefs[NAV_BAR_STYLE_KEY],
            creditReminderHour = prefs[CREDIT_REMINDER_HOUR],
            creditReminderMinute = prefs[CREDIT_REMINDER_MINUTE]
        )
    }

    /** Aplica solo los valores presentes en el respaldo; no toca el estado de onboarding ni el último respaldo. */
    suspend fun restoreSnapshot(snapshot: PreferencesSnapshot) {
        context.dataStore.edit { prefs ->
            snapshot.userName?.takeIf { it.isNotBlank() }?.let { prefs[USER_NAME] = it }
            snapshot.themeMode?.takeIf { v -> ThemeMode.entries.any { it.name == v } }?.let { prefs[THEME_MODE] = it }
            snapshot.fontPreference?.takeIf { v -> FontPreference.entries.any { it.name == v } }?.let { prefs[FONT_PREFERENCE_KEY] = it }
            snapshot.useDynamicColor?.let { prefs[DYNAMIC_COLOR_KEY] = it }
            snapshot.isAmoledMode?.let { prefs[AMOLED_MODE_KEY] = it }
            snapshot.hideInRecents?.let { prefs[HIDE_IN_RECENTS_KEY] = it }
            snapshot.accentColorHex?.takeIf { it.isNotBlank() }?.let { prefs[ACCENT_COLOR_HEX] = it }
            snapshot.dailyReminderEnabled?.let { prefs[DAILY_REMINDER_ENABLED] = it }
            snapshot.dailyReminderHour?.takeIf { it in 0..23 }?.let { prefs[DAILY_REMINDER_HOUR] = it }
            snapshot.dailyReminderMinute?.takeIf { it in 0..59 }?.let { prefs[DAILY_REMINDER_MINUTE] = it }
            snapshot.autoBackupEnabled?.let { prefs[AUTO_BACKUP_ENABLED] = it }
            snapshot.autoBackupHour?.takeIf { it in 0..23 }?.let { prefs[AUTO_BACKUP_HOUR] = it }
            snapshot.autoBackupMinute?.takeIf { it in 0..59 }?.let { prefs[AUTO_BACKUP_MINUTE] = it }
            snapshot.showBudgetTab?.let { prefs[SHOW_BUDGET_TAB] = it }
            snapshot.showCreditsTab?.let { prefs[SHOW_CREDITS_TAB] = it }
            snapshot.creditReminderHour?.takeIf { it in 0..23 }?.let { prefs[CREDIT_REMINDER_HOUR] = it }
            snapshot.creditReminderMinute?.takeIf { it in 0..59 }?.let { prefs[CREDIT_REMINDER_MINUTE] = it }
            snapshot.navBarStyle?.takeIf { v -> NavBarStyle.entries.any { it.name == v } }?.let { prefs[NAV_BAR_STYLE_KEY] = it }
        }
    }
}

data class PreferencesSnapshot(
    val userName: String? = null,
    val themeMode: String? = null,
    val fontPreference: String? = null,
    val useDynamicColor: Boolean? = null,
    val isAmoledMode: Boolean? = null,
    val hideInRecents: Boolean? = null,
    val accentColorHex: String? = null,
    val dailyReminderEnabled: Boolean? = null,
    val dailyReminderHour: Int? = null,
    val dailyReminderMinute: Int? = null,
    val autoBackupEnabled: Boolean? = null,
    val autoBackupHour: Int? = null,
    val autoBackupMinute: Int? = null,
    val showBudgetTab: Boolean? = null,
    val showCreditsTab: Boolean? = null,
    val navBarStyle: String? = null,
    val creditReminderHour: Int? = null,
    val creditReminderMinute: Int? = null
)
