package com.uramos.minicash

import com.uramos.minicash.widget.updateMinicashWidgets
import androidx.compose.runtime.LaunchedEffect
import android.view.WindowManager
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import android.os.SystemClock
import com.uramos.minicash.data.util.shouldLock
import com.uramos.minicash.ui.security.LockScreen
import com.uramos.minicash.ui.security.canUseAppLock
import com.uramos.minicash.ui.security.showAppLockPrompt
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.uramos.minicash.data.local.MinicashDatabase
import com.uramos.minicash.data.local.backup.AutoBackupManager
import com.uramos.minicash.data.preferences.UserPreferences
import com.uramos.minicash.ui.navigation.MainScreen
import com.uramos.minicash.ui.screens.budget.BudgetViewModel
import com.uramos.minicash.ui.screens.credits.CreditsViewModel
import com.uramos.minicash.ui.screens.home.HomeViewModel
import com.uramos.minicash.ui.screens.onboarding.OnboardingScreen
import com.uramos.minicash.ui.screens.onboarding.OnboardingViewModel
import com.uramos.minicash.ui.screens.reports.ReportsViewModel
import com.uramos.minicash.ui.screens.settings.SettingsViewModel
import com.uramos.minicash.ui.screens.transactions.TransactionsViewModel
import com.uramos.minicash.ui.theme.MinicashTheme
import com.uramos.minicash.ui.transactions.AddTransactionViewModel
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Estado del bloqueo de la app: CHECKING solo dura el instante en que se lee si el bloqueo está activo. */
private enum class LockStatus { CHECKING, LOCKED, UNLOCKED }

// FragmentActivity (que ya es una ComponentActivity) es lo que necesita el diálogo biométrico del sistema
class MainActivity : FragmentActivity() {

    private var shortcutActionState by mutableStateOf<String?>(null)
    private var lockStatus by mutableStateOf(LockStatus.CHECKING)
    private var promptShowing = false

    /** Momento (reloj monotónico) en que la app pasó a segundo plano; nulo mientras no se haya abierto. */
    private var backgroundedAt: Long? = null
    private lateinit var db: MinicashDatabase
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        // El splash se queda hasta saber si la app arranca bloqueada, para no mostrar un destello de otra pantalla
        installSplashScreen().setKeepOnScreenCondition { lockStatus == LockStatus.CHECKING }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        shortcutActionState = intent.extractShortcutAction()

        db = MinicashDatabase.getDatabase(applicationContext)

        userPreferences = UserPreferences(applicationContext)

        lifecycleScope.launch(Dispatchers.IO) {
            com.uramos.minicash.data.worker.CreditPaymentScheduler.schedule(
                applicationContext,
                userPreferences.creditReminderHour.first(),
                userPreferences.creditReminderMinute.first()
            )
            val enabled = userPreferences.dailyReminderEnabled.first()
            if (enabled) {
                val hour = userPreferences.dailyReminderHour.first()
                val minute = userPreferences.dailyReminderMinute.first()
                com.uramos.minicash.data.worker.ReminderScheduler.scheduleDailyReminder(applicationContext, hour, minute)
            }
        }

        val onboardingViewModel = OnboardingViewModel(
            accountDao = db.accountDao(),
            categoryDao = db.categoryDao(),
            userPreferences = userPreferences
        )

        val homeViewModel = HomeViewModel(
            accountDao = db.accountDao(),
            transactionDao = db.transactionDao(),
            categoryDao = db.categoryDao()
        )

        val addTransactionViewModel = AddTransactionViewModel(
            accountDao = db.accountDao(),
            categoryDao = db.categoryDao(),
            transactionDao = db.transactionDao()
        )

        val transactionsViewModel = TransactionsViewModel(
            transactionDao = db.transactionDao(),
            accountDao = db.accountDao(),
            categoryDao = db.categoryDao(),
            userPreferences = userPreferences
        )

        val budgetViewModel = BudgetViewModel(
            budgetDao = db.budgetDao(),
            accountDao = db.accountDao(),
            transactionDao = db.transactionDao(),
            categoryDao = db.categoryDao()
        )

        val creditsViewModel = CreditsViewModel(
            creditDao = db.creditDao(),
            accountDao = db.accountDao(),
            transactionDao = db.transactionDao(),
            creditInstitutionDao = db.creditInstitutionDao(),
            database = db
        )

        val reportsViewModel = ReportsViewModel(transactionDao = db.transactionDao(), categoryDao = db.categoryDao())

        val settingsViewModel = SettingsViewModel(
            accountDao = db.accountDao(),
            categoryDao = db.categoryDao(),
            userPreferences = userPreferences,
            database = db
        )

        setContent {
            val themeMode by userPreferences.themeMode.collectAsState(initial = com.uramos.minicash.data.preferences.ThemeMode.SYSTEM)
            val fontPreference by userPreferences.fontPreference.collectAsState(initial = com.uramos.minicash.data.preferences.FontPreference.ROUNDED_QUICKSAND)
            val useDynamicColor by userPreferences.useDynamicColor.collectAsState(initial = false)
            val isAmoledMode by userPreferences.isAmoledMode.collectAsState(initial = false)
            val hideInRecents by userPreferences.hideInRecents.collectAsState(initial = false)
            // FLAG_SECURE oculta la vista previa en multitarea; como efecto secundario también bloquea las capturas
            LaunchedEffect(hideInRecents) {
                if (hideInRecents) window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                else window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
            val accentColorHex by userPreferences.accentColorHex.collectAsState(initial = "#1A73E8")

            MinicashTheme(
                themeMode = themeMode,
                dynamicColor = useDynamicColor,
                isAmoledMode = isAmoledMode,
                accentColorHex = accentColorHex,
                fontPreference = fontPreference
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Con el bloqueo activo la app no se compone hasta desbloquear: no queda ningún dato en pantalla
                    if (lockStatus != LockStatus.UNLOCKED) {
                        LockScreen(
                            showUnlock = lockStatus == LockStatus.LOCKED,
                            onUnlock = { requestUnlock() }
                        )
                    } else AppNavigation(
                        shortcutAction = shortcutActionState,
                        onShortcutActionHandled = { shortcutActionState = null },
                        userPreferences = userPreferences,
                        onboardingViewModel = onboardingViewModel,
                        homeViewModel = homeViewModel,
                        addTransactionViewModel = addTransactionViewModel,
                        transactionsViewModel = transactionsViewModel,
                        budgetViewModel = budgetViewModel,
                        creditsViewModel = creditsViewModel,
                        reportsViewModel = reportsViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        checkAutoBackup()
        evaluateLock()
    }

    /** Decide si hace falta bloquear al volver a la app; si ya está bloqueada se queda así hasta desbloquear. */
    private fun evaluateLock() {
        if (lockStatus == LockStatus.LOCKED) {
            requestUnlock()
            return
        }
        lifecycleScope.launch {
            val enabled = userPreferences.appLockEnabled.first()
            val timeout = userPreferences.appLockTimeoutMinutes.first()
            // Si el teléfono ya no tiene bloqueo de pantalla configurado no se puede pedir nada: se deja entrar
            val lock = enabled && canUseAppLock(this@MainActivity) &&
                shouldLock(enabled, timeout, backgroundedAt, SystemClock.elapsedRealtime())
            if (lock) {
                lockStatus = LockStatus.LOCKED
                requestUnlock()
            } else {
                lockStatus = LockStatus.UNLOCKED
            }
        }
    }

    private fun requestUnlock() {
        if (promptShowing) return
        promptShowing = true
        showAppLockPrompt(
            activity = this,
            title = "Minicash",
            subtitle = "Desbloquea para continuar",
            onSuccess = {
                promptShowing = false
                lockStatus = LockStatus.UNLOCKED
            },
            onFailure = { promptShowing = false }
        )
    }

    override fun onStop() {
        super.onStop()
        // Mientras el diálogo del sistema está abierto no cuenta como salir de la app
        if (lockStatus == LockStatus.UNLOCKED) backgroundedAt = SystemClock.elapsedRealtime()
        checkAutoBackup()
        // Al salir de la app los datos pudieron cambiar: se refrescan los widgets de la pantalla de inicio
        updateMinicashWidgets(applicationContext)
    }

    private fun checkAutoBackup() {
        if (::db.isInitialized && ::userPreferences.isInitialized) {
            lifecycleScope.launch(Dispatchers.IO) {
                AutoBackupManager.checkAndTriggerAutoBackup(
                    context = applicationContext,
                    database = db,
                    userPreferences = userPreferences
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        shortcutActionState = intent.extractShortcutAction()
    }

    private fun Intent?.extractShortcutAction(): String? {
        if (this == null) return null
        return when (action) {
            "com.uramos.minicash.ACTION_NEW_EXPENSE" -> "EXPENSE"
            "com.uramos.minicash.ACTION_NEW_INCOME" -> "INCOME"
            "com.uramos.minicash.ACTION_NEW_TRANSFER" -> "TRANSFER"
            else -> getStringExtra("shortcut_action")
        }
    }
}

@Composable
fun AppNavigation(
    shortcutAction: String? = null,
    onShortcutActionHandled: () -> Unit = {},
    userPreferences: UserPreferences,
    onboardingViewModel: OnboardingViewModel,
    homeViewModel: HomeViewModel,
    addTransactionViewModel: AddTransactionViewModel,
    transactionsViewModel: TransactionsViewModel,
    budgetViewModel: BudgetViewModel,
    creditsViewModel: CreditsViewModel,
    reportsViewModel: ReportsViewModel,
    settingsViewModel: SettingsViewModel
) {
    val isOnboardingCompleted by remember { userPreferences.isOnboardingCompleted }
        .collectAsState(initial = null)
    val userName by remember { userPreferences.userName }
        .collectAsState(initial = "")

    when (isOnboardingCompleted) {
        null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        false -> {
            OnboardingScreen(
                viewModel = onboardingViewModel,
                onFinished = {}
            )
        }
        true -> {
            MainScreen(
                userName = userName,
                shortcutAction = shortcutAction,
                onShortcutActionHandled = onShortcutActionHandled,
                homeViewModel = homeViewModel,
                addTransactionViewModel = addTransactionViewModel,
                transactionsViewModel = transactionsViewModel,
                budgetViewModel = budgetViewModel,
                creditsViewModel = creditsViewModel,
                reportsViewModel = reportsViewModel,
                settingsViewModel = settingsViewModel
            )
        }
    }
}
