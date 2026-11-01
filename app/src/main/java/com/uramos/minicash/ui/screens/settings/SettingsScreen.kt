package com.uramos.minicash.ui.screens.settings

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
import com.uramos.minicash.ui.components.MinicashLogoWithText
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
    onNavigateToReports: () -> Unit = {},
    onNavigateToBackups: () -> Unit = {}
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

    val postNotificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.setDailyReminderEnabled(context, true)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.padding(bottom = FLOATING_NAV_CLEARANCE)) },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                ),
                title = { Text("Ajustes", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                top = spacing.small,
                start = spacing.standard,
                end = spacing.standard,
                bottom = 120.dp
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // BLOQUE 1: Tu dinero
            item {
                MinicashCard(
                    level = CardTonalLevel.LOW,
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Tu dinero",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Administra tus cuentas, categorías e instituciones",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        SettingsRowItem(
                            title = "Cuentas",
                            icon = Icons.Default.AccountBalanceWallet,
                            subtitle = "${state.accounts.size} activas configuradas",
                            onClick = onNavigateToAccounts
                        )
                        SettingsRowItem(
                            title = "Categorías",
                            icon = Icons.Default.Category,
                            subtitle = "${state.expenseCategories.size + state.incomeCategories.size} registradas",
                            onClick = onNavigateToCategories
                        )
                        SettingsRowItem(
                            title = "Tarjetas e instituciones",
                            icon = Icons.Default.CreditCard,
                            subtitle = "${state.creditInstitutions.size} instituciones registradas",
                            onClick = onNavigateToInstitutions
                        )
                    }
                }
            }

            // BLOQUE 2: Preferencias
            item {
                MinicashCard(
                    level = CardTonalLevel.LOW,
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Preferencias",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Personaliza la apariencia, módulos y notificaciones",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        SettingsRowItem(
                            title = "Tu nombre",
                            icon = Icons.Default.Person,
                            subtitle = state.userName.ifBlank { "Sin definir" },
                            onClick = { showNameDialog = true }
                        )

                        val fontLabel = if (state.fontPreference == FontPreference.ROUNDED_QUICKSAND) "Quicksand" else "Google Sans Flex"
                        val appearanceSubtitle = "${state.themeMode.displayName} • $fontLabel${if (state.useDynamicColor) " • Color dinámico" else ""}"
                        SettingsRowItem(
                            title = "Apariencia",
                            icon = Icons.Default.Palette,
                            subtitle = appearanceSubtitle,
                            onClick = onNavigateToAppearance
                        )

                        val activeModulesCount = (if (state.showBudgetTab) 1 else 0) + (if (state.showCreditsTab) 1 else 0)
                        val modulesSubtitle = when (activeModulesCount) {
                            2 -> "Presupuesto y Tarjetas activos"
                            1 -> if (state.showBudgetTab) "Presupuesto activo" else "Tarjetas activo"
                            else -> "Sin módulos opcionales"
                        }
                        SettingsRowItem(
                            title = "Módulos de la app",
                            icon = Icons.Default.Widgets,
                            subtitle = modulesSubtitle,
                            onClick = onNavigateToModules
                        )

                        SettingsRowItem(
                            title = "Avisos de pago",
                            icon = Icons.Default.Payments,
                            subtitle = "Cada día a las ${creditReminderTime.first.toString().padStart(2, '0')}:${creditReminderTime.second.toString().padStart(2, '0')} hrs",
                            onClick = { showCreditTimePickerDialog = true }
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SettingsLeadingIcon(Icons.Default.Notifications)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Recordatorio diario",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                val formattedTime = "${state.dailyReminderHour.toString().padStart(2, '0')}:${state.dailyReminderMinute.toString().padStart(2, '0')}"
                                Text(
                                    text = if (state.dailyReminderEnabled) "Activo a las $formattedTime hrs" else "Notificar a las 21:00 si no hay gastos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Switch(
                                checked = state.dailyReminderEnabled,
                                thumbContent = if (state.dailyReminderEnabled) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(SwitchDefaults.IconSize)) }
                                } else null,
                                onCheckedChange = { checked ->
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
                            )
                        }

                        if (state.dailyReminderEnabled) {
                            val formattedTime = "${state.dailyReminderHour.toString().padStart(2, '0')}:${state.dailyReminderMinute.toString().padStart(2, '0')}"
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                onClick = { showTimePickerDialog = true },
                                modifier = Modifier.padding(start = 12.dp, top = 2.dp, bottom = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Hora del recordatorio:",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$formattedTime hrs",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // BLOQUE 3: Datos y sistema
            item {
                MinicashCard(
                    level = CardTonalLevel.LOW,
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDone,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Datos y sistema",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Copias de seguridad e información de la app",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        SettingsRowItem(
                            title = "Reportes",
                            icon = Icons.Default.BarChart,
                            subtitle = "Ingresos y gastos por mes",
                            onClick = onNavigateToReports
                        )
                        val backupSubtitle = if (state.autoBackupEnabled) "Automático activo" else "Manual únicamente"
                        SettingsRowItem(
                            title = "Copias de seguridad",
                            icon = Icons.Default.CloudDone,
                            subtitle = backupSubtitle,
                            onClick = onNavigateToBackups
                        )
                    }
                }
            }

            // Sección Acerca de Minicash
            item {
                MinicashCard(
                    level = CardTonalLevel.LOW,
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.large),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(spacing.small)
                    ) {
                        MinicashLogoWithText(logoSize = 36.dp)
                        Text(
                            text = "Versión 1.0 • Control financiero personal local",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                    shape = RoundedCornerShape(12.dp),
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
        val timePickerState = rememberTimePickerState(
            initialHour = state.dailyReminderHour,
            initialMinute = state.dailyReminderMinute,
            is24Hour = true
        )
        MinicashAlertDialog(
            onDismissRequest = { showTimePickerDialog = false },
            confirmButton = {
                MinicashFilledButton(
                    onClick = {
                        viewModel.setDailyReminderTime(context, timePickerState.hour, timePickerState.minute)
                        showTimePickerDialog = false
                    },
                ) {
                    Text("Guardar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                MinicashTextButton(onClick = { showTimePickerDialog = false }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Hora del recordatorio", fontWeight = FontWeight.Bold) },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            shape = MinicashShapes.Hero
        )
    }

    if (showCreditTimePickerDialog) {
        val timePickerState = rememberTimePickerState(
            initialHour = creditReminderTime.first,
            initialMinute = creditReminderTime.second,
            is24Hour = true
        )
        MinicashAlertDialog(
            onDismissRequest = { showCreditTimePickerDialog = false },
            confirmButton = {
                MinicashFilledButton(
                    onClick = {
                        viewModel.setCreditReminderTime(context, timePickerState.hour, timePickerState.minute)
                        showCreditTimePickerDialog = false
                    },
                ) {
                    Text("Guardar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                MinicashTextButton(onClick = { showCreditTimePickerDialog = false }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Hora de los avisos de pago", fontWeight = FontWeight.Bold) },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            shape = MinicashShapes.Hero
        )
    }

    if (showAutoBackupTimePickerDialog) {
        val timePickerState = rememberTimePickerState(
            initialHour = state.autoBackupHour,
            initialMinute = state.autoBackupMinute,
            is24Hour = true
        )
        MinicashAlertDialog(
            onDismissRequest = { showAutoBackupTimePickerDialog = false },
            confirmButton = {
                MinicashFilledButton(
                    onClick = {
                        viewModel.setAutoBackupTime(context, timePickerState.hour, timePickerState.minute)
                        showAutoBackupTimePickerDialog = false
                    },
                ) {
                    Text("Guardar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                MinicashTextButton(onClick = { showAutoBackupTimePickerDialog = false }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Hora de respaldo", fontWeight = FontWeight.Bold) },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            shape = MinicashShapes.Hero
        )
    }
}

@Composable
private fun SettingsRowItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SettingsLeadingIcon(icon)
        Spacer(modifier = Modifier.width(14.dp))
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
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
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
    val haptic = LocalHapticFeedback.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
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
                                    modifier = Modifier.size(32.dp)
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

                    Text("Color distintivo:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
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
                        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
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
