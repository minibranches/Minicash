package com.uramos.minicash.ui.screens.settings

import androidx.compose.material.icons.filled.NotificationsOff
import com.uramos.minicash.ui.utils.openAppNotificationSettings
import com.uramos.minicash.ui.utils.rememberNotificationsAllowed
import com.uramos.minicash.ui.components.MinicashTimePickerDialog
import com.uramos.minicash.ui.theme.screenBackground
import com.uramos.minicash.ui.theme.cardSurface
import com.uramos.minicash.ui.theme.sheetBackground
import com.uramos.minicash.ui.components.plusTop
import com.uramos.minicash.ui.components.withoutTop
import com.uramos.minicash.ui.components.underHeaderBlur
import com.uramos.minicash.ui.security.showAppLockPrompt
import com.uramos.minicash.ui.security.findFragmentActivity
import com.uramos.minicash.ui.security.canUseAppLock
import android.widget.Toast
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.uramos.minicash.ui.components.MinicashSubScreenTopBar
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Switch
import androidx.compose.material.icons.filled.VisibilityOff
import com.uramos.minicash.ui.components.MinicashAlertDialog
import androidx.compose.material.icons.filled.Payments
import com.uramos.minicash.ui.components.MinicashTextButton
import com.uramos.minicash.ui.components.MinicashFilledButton
import com.uramos.minicash.ui.components.MinicashIconButton
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.Person
import android.Manifest
import com.uramos.minicash.ui.utils.asIconTint
import androidx.compose.runtime.saveable.rememberSaveable
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.core.content.ContextCompat
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.uramos.minicash.ui.utils.FLOATING_NAV_CLEARANCE
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import com.uramos.minicash.ui.utils.rememberMinicashHaptics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uramos.minicash.data.local.entities.CreditInstitutionEntity
import com.uramos.minicash.data.preferences.FontPreference
import com.uramos.minicash.data.preferences.ThemeMode
import com.uramos.minicash.ui.components.CardTonalLevel
import com.uramos.minicash.ui.components.MinicashCard
import com.uramos.minicash.ui.components.MinicashColorPicker
import com.uramos.minicash.ui.components.MinicashEmptyState
import com.uramos.minicash.ui.components.rememberSegmentedShape
import com.uramos.minicash.ui.utils.MinicashColorUtils
import com.uramos.minicash.ui.theme.MinicashShapes
import com.uramos.minicash.ui.theme.MinicashThemeExtensions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    listState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState(),
    onNavigateToAccounts: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    onNavigateToInstitutions: () -> Unit = {},
    onNavigateToAppearance: () -> Unit = {},
    onNavigateToModules: () -> Unit = {},
    onNavigateToBackups: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val spacing = MinicashThemeExtensions.spacing
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val haptics = rememberMinicashHaptics()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.messageEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }
    var showNameDialog by rememberSaveable { mutableStateOf(false) }
    var showInstitutionsDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var showAutoBackupTimePickerDialog by remember { mutableStateOf(false) }
    var showCreditTimePickerDialog by remember { mutableStateOf(false) }
    val creditReminderTime by viewModel.creditReminderTime.collectAsState()
    val notificationsNeeded by viewModel.notificationsNeeded.collectAsState()
    val notificationsAllowed = rememberNotificationsAllowed()

    val postNotificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.setDailyReminderEnabled(context, true)
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.padding(bottom = FLOATING_NAV_CLEARANCE)) },
        containerColor = MaterialTheme.colorScheme.screenBackground,
        topBar = {
            MinicashSubScreenTopBar(title = "Ajustes", onBack = onBackClick, scrollBehavior = scrollBehavior)
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues.withoutTop())
                .underHeaderBlur(paddingValues.calculateTopPadding(), bottomBand = 64.dp, listState = listState),
            contentPadding = PaddingValues(
                top = spacing.small,
                start = spacing.screen,
                end = spacing.screen,
                bottom = 120.dp
            ).plusTop(paddingValues.calculateTopPadding()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Aviso cuando hay algo programado y el sistema no deja notificar
            if (notificationsNeeded && !notificationsAllowed) {
                item {
                    NotificationsDisabledCard(onClick = { openAppNotificationSettings(context) })
                }
            }

            // BLOQUE 1: Tu dinero
            item {
                SettingsSection(
                    icon = Icons.Default.AccountBalanceWallet,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "Tu dinero",
                    subtitle = "Administra tus cuentas, categorías e instituciones"
                ) {
                    SettingsRowItem(
                        title = "Cuentas",
                        icon = Icons.Default.AccountBalanceWallet,
                        subtitle = state.accounts.size.let { if (it == 1) "1 activa configurada" else "$it activas configuradas" },
                        onClick = onNavigateToAccounts,
                        index = 0, count = 3
                    )
                    SettingsRowItem(
                        title = "Categorías",
                        icon = Icons.Default.Category,
                        subtitle = (state.expenseCategories.size + state.incomeCategories.size).let { if (it == 1) "1 registrada" else "$it registradas" },
                        onClick = onNavigateToCategories,
                        index = 1, count = 3
                    )
                    SettingsRowItem(
                        title = "Tarjetas e instituciones",
                        icon = Icons.Default.CreditCard,
                        subtitle = state.creditInstitutions.size.let { if (it == 1) "1 institución registrada" else "$it instituciones registradas" },
                        onClick = onNavigateToInstitutions,
                        index = 2, count = 3
                    )
                }
            }

            // BLOQUE 2: Preferencias
            item {
                val fontLabel = if (state.fontPreference == FontPreference.ROUNDED_QUICKSAND) "Quicksand" else "Google Sans Flex"
                val appearanceSubtitle = "${state.themeMode.displayName} • $fontLabel${if (state.useDynamicColor) " • Color dinámico" else ""}"
                val activeModulesCount = (if (state.showBudgetTab) 1 else 0) + (if (state.showCreditsTab) 1 else 0)
                val modulesSubtitle = when (activeModulesCount) {
                    2 -> "Gastos fijos y Créditos activos"
                    1 -> if (state.showBudgetTab) "Gastos fijos activo" else "Créditos activo"
                    else -> "Sin módulos opcionales"
                }
                val reminderCount = if (state.dailyReminderEnabled) 6 else 5
                val formattedTime = "${state.dailyReminderHour.toString().padStart(2, '0')}:${state.dailyReminderMinute.toString().padStart(2, '0')}"
                val setDailyReminder: (Boolean) -> Unit = { checked ->
                    haptics.click()
                    if (checked) {
                        if (Build.VERSION.SDK_INT >= 33) {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasPermission) {
                                viewModel.setDailyReminderEnabled(context, true)
                            } else {
                                postNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        } else {
                            viewModel.setDailyReminderEnabled(context, true)
                        }
                    } else {
                        viewModel.setDailyReminderEnabled(context, false)
                    }
                }

                SettingsSection(
                    icon = Icons.Default.Tune,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "Preferencias",
                    subtitle = "Personaliza la apariencia, módulos y notificaciones"
                ) {
                    SettingsRowItem(
                        title = "Tu nombre",
                        icon = Icons.Default.Person,
                        subtitle = state.userName.ifBlank { "Sin definir" },
                        onClick = { showNameDialog = true },
                        index = 0, count = reminderCount
                    )
                    SettingsRowItem(
                        title = "Apariencia",
                        icon = Icons.Default.Palette,
                        subtitle = appearanceSubtitle,
                        onClick = onNavigateToAppearance,
                        index = 1, count = reminderCount
                    )
                    SettingsRowItem(
                        title = "Módulos de la app",
                        icon = Icons.Default.Widgets,
                        subtitle = modulesSubtitle,
                        onClick = onNavigateToModules,
                        index = 2, count = reminderCount
                    )
                    SettingsRowItem(
                        title = "Avisos de pago",
                        icon = Icons.Default.Payments,
                        subtitle = "Cada día a las ${creditReminderTime.first.toString().padStart(2, '0')}:${creditReminderTime.second.toString().padStart(2, '0')} hrs",
                        onClick = { showCreditTimePickerDialog = true },
                        index = 3, count = reminderCount
                    )
                    SettingsSwitchItem(
                        title = "Recordatorio diario",
                        subtitle = if (state.dailyReminderEnabled) "Activo a las $formattedTime hrs" else "Notificar a las 21:00 si no hay gastos",
                        icon = Icons.Default.Notifications,
                        checked = state.dailyReminderEnabled && notificationsAllowed,
                        onCheckedChange = setDailyReminder,
                        index = 4, count = reminderCount
                    )
                    if (state.dailyReminderEnabled) {
                        SettingsRowItem(
                            title = "Hora del recordatorio",
                            icon = Icons.Default.Schedule,
                            subtitle = "$formattedTime hrs",
                            onClick = { showTimePickerDialog = true },
                            index = 5, count = reminderCount
                        )
                    }
                }
            }

            // BLOQUE 3: Datos y sistema
            item {
                val backupSubtitle = if (state.autoBackupEnabled) "Automático activo" else "Manual únicamente"
                val hideInRecents by viewModel.hideInRecents.collectAsState()
                val appLockEnabled by viewModel.appLockEnabled.collectAsState()
                val appLockTimeout by viewModel.appLockTimeoutMinutes.collectAsState()
                val context = androidx.compose.ui.platform.LocalContext.current
                val dataCount = if (appLockEnabled) 5 else 4
                SettingsSection(
                    icon = Icons.Default.CloudDone,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "Datos y sistema",
                    subtitle = "Copias de seguridad e información de la app"
                ) {
                    SettingsRowItem(
                        title = "Copias de seguridad",
                        icon = Icons.Default.CloudDone,
                        subtitle = backupSubtitle,
                        onClick = onNavigateToBackups,
                        index = 0, count = dataCount
                    )
                    SettingsSwitchItem(
                        title = "Ocultar en multitarea",
                        subtitle = if (hideInRecents) "Vista previa oculta; también se bloquean las capturas"
                        else "Oculta la vista previa al ver las apps abiertas",
                        icon = Icons.Default.VisibilityOff,
                        checked = hideInRecents,
                        onCheckedChange = {
                            haptics.click()
                            viewModel.setHideInRecents(it)
                        },
                        index = 1, count = dataCount
                    )
                    SettingsSwitchItem(
                        title = "Bloqueo de la app",
                        subtitle = if (appLockEnabled) com.uramos.minicash.data.util.appLockTimeoutLabel(appLockTimeout) + "; pide huella, rostro o PIN"
                        else "Pide huella, rostro o el PIN del teléfono al abrir Minicash",
                        icon = Icons.Default.Lock,
                        checked = appLockEnabled,
                        onCheckedChange = { wantsOn ->
                            haptics.click()
                            val activity = context.findFragmentActivity()
                            when {
                                !wantsOn -> activity?.let {
                                    // Para quitar el bloqueo también se pide verificarse
                                    showAppLockPrompt(it, "Quitar el bloqueo", "Verifica que eres tú", onSuccess = { viewModel.setAppLockEnabled(false) })
                                } ?: viewModel.setAppLockEnabled(false)
                                activity == null || !canUseAppLock(context) ->
                                    Toast.makeText(context, "Configura una huella, un rostro o un PIN en los ajustes del teléfono", Toast.LENGTH_LONG).show()
                                else -> showAppLockPrompt(activity, "Activar el bloqueo", "Verifica que puedes desbloquear", onSuccess = { viewModel.setAppLockEnabled(true) })
                            }
                        },
                        index = 2, count = dataCount
                    )
                    if (appLockEnabled) {
                        SettingsRowItem(
                            title = "Volver a pedir",
                            icon = Icons.Default.Timer,
                            subtitle = com.uramos.minicash.data.util.appLockTimeoutLabel(appLockTimeout),
                            onClick = {
                                haptics.click()
                                viewModel.setAppLockTimeoutMinutes(com.uramos.minicash.data.util.nextAppLockTimeout(appLockTimeout))
                            },
                            index = 3, count = dataCount
                        )
                    }
                    SettingsRowItem(
                        title = "Acerca de Minicash",
                        icon = Icons.Default.Info,
                        subtitle = "Versión, enlaces y compartir",
                        onClick = onNavigateToAbout,
                        index = dataCount - 1, count = dataCount
                    )
                }
            }
        }
    }

    if (showNameDialog) {
        var draft by rememberSaveable { mutableStateOf(state.userName) }
        val canSave = draft.isNotBlank() && draft.trim() != state.userName
        MinicashAlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text("Tu nombre", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it.take(MAX_USER_NAME_LENGTH) },
                    label = { Text("Nombre") },
                    supportingText = { Text("Se muestra en el saludo de Inicio") },
                    singleLine = true,
                    shape = MinicashShapes.Input,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                MinicashFilledButton(
                    onClick = {
                        viewModel.setUserName(draft)
                        showNameDialog = false
                    },
                    enabled = canSave,
                ) {
                    Text("Guardar", fontWeight = FontWeight.Bold, maxLines = 1)
                }
            },
            dismissButton = {
                MinicashTextButton(onClick = { showNameDialog = false }) { Text("Cancelar") }
            },
            shape = MinicashShapes.Hero
        )
    }

    if (showInstitutionsDialog) {
        InstitutionsManagementSheet(
            institutions = state.creditInstitutions,
            onDismiss = { showInstitutionsDialog = false },
            onAdd = { name, colorHex -> viewModel.addCreditInstitution(name, colorHex) },
            onDelete = { inst -> viewModel.deleteCreditInstitution(inst) }
        )
    }

    if (pendingRestoreUri != null) {
        val uri = pendingRestoreUri!!
        var isRestoring by remember { mutableStateOf(false) }

        MinicashAlertDialog(
            onDismissRequest = { if (!isRestoring) pendingRestoreUri = null },
            title = { Text("¿Restaurar copia de seguridad?", fontWeight = FontWeight.Bold) },
            text = {
                if (isRestoring) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LoadingIndicator()
                        Text("Restaurando datos...", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    Text("Esta acción reemplazará los datos actuales de la aplicación con la información del archivo de respaldo. Antes de restaurar se guarda automáticamente una copia de los datos actuales. ¿Deseas proceder?")
                }
            },
            confirmButton = {
                if (!isRestoring) {
                    MinicashFilledButton(
                        onClick = {
                            isRestoring = true
                            viewModel.importBackup(context, uri)
                            pendingRestoreUri = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                    ) {
                        Text("Restaurar", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                if (!isRestoring) {
                    MinicashTextButton(onClick = { pendingRestoreUri = null }) {
                        Text("Cancelar")
                    }
                }
            },
            shape = MinicashShapes.Hero
        )
    }

    if (showTimePickerDialog) {
        MinicashTimePickerDialog(
            title = "Hora del recordatorio",
            initialHour = state.dailyReminderHour,
            initialMinute = state.dailyReminderMinute,
            onConfirm = { hour, minute ->
                viewModel.setDailyReminderTime(context, hour, minute)
                showTimePickerDialog = false
            },
            onDismiss = { showTimePickerDialog = false }
        )
    }

    if (showCreditTimePickerDialog) {
        MinicashTimePickerDialog(
            title = "Hora de los avisos de pago",
            initialHour = creditReminderTime.first,
            initialMinute = creditReminderTime.second,
            onConfirm = { hour, minute ->
                viewModel.setCreditReminderTime(context, hour, minute)
                showCreditTimePickerDialog = false
            },
            onDismiss = { showCreditTimePickerDialog = false }
        )
    }

    if (showAutoBackupTimePickerDialog) {
        MinicashTimePickerDialog(
            title = "Hora de respaldo",
            initialHour = state.autoBackupHour,
            initialMinute = state.autoBackupMinute,
            onConfirm = { hour, minute ->
                viewModel.setAutoBackupTime(context, hour, minute)
                showAutoBackupTimePickerDialog = false
            },
            onDismiss = { showAutoBackupTimePickerDialog = false }
        )
    }
}

/** Sección de Ajustes: encabezado con ícono y, debajo, filas segmentadas separadas por 2 dp (lista expresiva de M3E). */
@Composable
private fun SettingsSection(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.padding(start = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
                }
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        content()
    }
}

/** Contenedor de una fila segmentada: las esquinas exteriores del grupo son grandes y las interiores pequeñas. */
@Composable
private fun SettingsSegmentedItem(
    index: Int,
    count: Int,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        onClick = onClick,
        shape = rememberSegmentedShape(index = index, count = count),
        color = MaterialTheme.colorScheme.cardSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
private fun RowScope.SettingsItemText(title: String, subtitle: String) {
    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsRowItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    index: Int = 0,
    count: Int = 1
) {
    SettingsSegmentedItem(index = index, count = count, onClick = onClick) {
        SettingsLeadingIcon(icon)
        Spacer(modifier = Modifier.width(14.dp))
        SettingsItemText(title, subtitle)
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

/** Fila con interruptor: tocar cualquier parte de la fila lo cambia, igual que en los ajustes del sistema. */
@Composable
private fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    index: Int,
    count: Int
) {
    SettingsSegmentedItem(index = index, count = count, onClick = { onCheckedChange(!checked) }) {
        SettingsLeadingIcon(icon)
        Spacer(modifier = Modifier.width(14.dp))
        SettingsItemText(title, subtitle)
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            thumbContent = if (checked) {
                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(SwitchDefaults.IconSize)) }
            } else null,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingsGroupRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: (() -> Unit)? = null
) {
    val spacing = MinicashThemeExtensions.spacing

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(spacing.standard),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                shape = CircleShape,
                color = containerColor,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(spacing.medium))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstitutionsManagementSheet(
    institutions: List<CreditInstitutionEntity>,
    onDismiss: () -> Unit,
    onAdd: (name: String, colorHex: String) -> Unit,
    onDelete: (CreditInstitutionEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showAddDialog by remember { mutableStateOf(false) }
    var instToDelete by remember { mutableStateOf<CreditInstitutionEntity?>(null) }
    val haptics = rememberMinicashHaptics()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.sheetBackground,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = MinicashShapes.SheetTop
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Tarjetas e instituciones",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (institutions.isEmpty()) {
                MinicashEmptyState(
                    icon = Icons.Default.CreditCard,
                    title = "Sin instituciones registradas",
                    subtitle = "Agrega tus bancos o tarjetas mediante el botón inferior.",
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    itemsIndexed(institutions, key = { _, inst -> inst.id }) { index, inst ->
                        val itemShape = rememberSegmentedShape(index = index, count = institutions.size)
                        val instColor = MinicashColorUtils.parseHexColor(inst.colorHex, MaterialTheme.colorScheme.primary)

                        Surface(
                            shape = itemShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = instColor.copy(alpha = 0.25f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.CreditCard,
                                            contentDescription = null,
                                            tint = instColor.asIconTint(),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = inst.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )

                                MinicashIconButton(
                                    onClick = { instToDelete = inst },
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            MinicashFilledButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Text("+ Nueva institución", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var selectedColorHex by remember { mutableStateOf("#1A73E8") }

        MinicashAlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Nueva institución", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre de la institución") },
                        placeholder = { Text("Escribe el nombre de la institución") },
                        singleLine = true,
                        shape = CircleShape,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Color distintivo", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    MinicashColorPicker(
                        selectedColorHex = selectedColorHex,
                        onColorSelected = { selectedColorHex = it }
                    )
                }
            },
            confirmButton = {
                MinicashFilledButton(
                    onClick = {
                        if (name.isNotBlank()) {
                            onAdd(name.trim(), selectedColorHex)
                            showAddDialog = false
                        }
                    },
                ) {
                    Text("Guardar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                MinicashTextButton(onClick = { showAddDialog = false }) { Text("Cancelar") }
            },
            shape = MinicashShapes.Hero
        )
    }

    if (instToDelete != null) {
        val inst = instToDelete!!
        MinicashAlertDialog(
            onDismissRequest = { instToDelete = null },
            title = { Text("¿Eliminar institución?", fontWeight = FontWeight.Bold) },
            text = { Text("¿Deseas eliminar '${inst.name}' de tus instituciones de crédito?") },
            confirmButton = {
                MinicashFilledButton(
                    onClick = {
                        haptics.confirm()
                        onDelete(inst)
                        instToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                ) {
                    Text("Eliminar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                MinicashTextButton(onClick = { instToDelete = null }) { Text("Cancelar") }
            },
            shape = MinicashShapes.Hero
        )
    }
}

/** Ícono inicial de cada fila de ajustes, en la misma forma expresiva que las categorías. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SettingsLeadingIcon(icon: ImageVector) {
    Surface(
        shape = MaterialShapes.Cookie9Sided.toShape(),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.size(44.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}


/** Tarjeta roja que avisa que las notificaciones están bloqueadas en el sistema y lleva a activarlas. */
@Composable
private fun NotificationsDisabledCard(onClick: () -> Unit) {
    androidx.compose.material3.Surface(
        onClick = onClick,
        shape = MinicashShapes.Card,
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Default.NotificationsOff, contentDescription = null)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Notificaciones desactivadas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Toca para activarlas en la configuración del sistema. Hasta entonces, ningún aviso programado te llegará.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
