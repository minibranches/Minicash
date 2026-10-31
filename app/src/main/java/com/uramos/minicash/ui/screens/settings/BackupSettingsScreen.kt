package com.uramos.minicash.ui.screens.settings

import com.uramos.minicash.ui.components.MinicashTextButton
import com.uramos.minicash.ui.components.MinicashFilledButton
import com.uramos.minicash.ui.components.MinicashIconButton
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Check
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.uramos.minicash.ui.utils.FLOATING_NAV_CLEARANCE
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uramos.minicash.ui.components.CardTonalLevel
import com.uramos.minicash.ui.components.MinicashCard
import com.uramos.minicash.ui.theme.MinicashShapes
import com.uramos.minicash.ui.theme.MinicashThemeExtensions
import com.uramos.minicash.ui.utils.rememberMinicashHaptics
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupSettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit
) {
    val spacing = MinicashThemeExtensions.spacing
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val haptics = rememberMinicashHaptics()

    val snackbarHostState = remember { SnackbarHostState() }
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }
    var showTimePickerDialog by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.messageEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { viewModel.exportBackup(context, it) }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { pendingRestoreUri = it }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.padding(bottom = FLOATING_NAV_CLEARANCE)) },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                ),
                navigationIcon = {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .padding(start = spacing.small)
                            .size(40.dp)
                    ) {
                        MinicashIconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                title = {
                    Text(
                        text = "Copias de seguridad",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                horizontal = spacing.standard,
                vertical = spacing.small
            ),
            verticalArrangement = Arrangement.spacedBy(spacing.medium)
        ) {
            // Tarjeta 1: Gestión de datos
            item {
                Column {
                    Text(
                        text = "Gestión de datos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(spacing.medium))

                    MinicashCard(
                        level = CardTonalLevel.LOW,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.standard),
                            verticalArrangement = Arrangement.spacedBy(spacing.medium)
                        ) {
                            // Crear copia
                            Surface(
                                onClick = {
                                    val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                                    createDocumentLauncher.launch("minicash_backup_$dateStr.json")
                                },
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                SettingsGroupRow(
                                    title = "Crear copia de seguridad",
                                    subtitle = "Guarda una copia de seguridad local en JSON",
                                    icon = Icons.Default.CloudUpload,
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                    onClick = null
                                )
                            }

                            Spacer(modifier = Modifier.height(spacing.small))

                            // Restaurar copia
                            Surface(
                                onClick = {
                                    openDocumentLauncher.launch(arrayOf("application/json", "*/*"))
                                },
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                SettingsGroupRow(
                                    title = "Restaurar copia de seguridad",
                                    subtitle = "Importa una copia de seguridad desde un archivo JSON",
                                    icon = Icons.Default.CloudDownload,
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    onClick = null
                                )
                            }
                        }
                    }
                }
            }

            // Tarjeta 2: Respaldo automático local
            item {
                Column {
                    Text(
                        text = "Respaldo automático local",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(spacing.medium))

                    MinicashCard(
                        level = CardTonalLevel.LOW,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val autoBackupTimeText = "${state.autoBackupHour.toString().padStart(2, '0')}:${state.autoBackupMinute.toString().padStart(2, '0')}"

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.standard),
                            verticalArrangement = Arrangement.spacedBy(spacing.medium)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.AutoMode,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(spacing.medium))
                                    Column {
                                        Text(
                                            text = "Respaldo automático diario",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (state.autoBackupEnabled) "Programado todos los días a las $autoBackupTimeText hrs" else "Desactivado",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Switch(
                                    checked = state.autoBackupEnabled,
                                    thumbContent = if (state.autoBackupEnabled) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(SwitchDefaults.IconSize)) }
                                    } else null,
                                    onCheckedChange = {
                                        haptics.click()
                                        viewModel.setAutoBackupEnabled(context, it)
                                    }
                                )
                            }

                            AnimatedVisibility(visible = state.autoBackupEnabled) {
                                Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        onClick = { showTimePickerDialog = true },
                                        modifier = Modifier.padding(top = spacing.extraSmall)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "Hora de respaldo:",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "$autoBackupTimeText hrs",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Las copias automáticas se resguardan de forma segura en el almacenamiento interno de la app, manteniendo rotación de los 3 respaldos más recientes.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    val lastBackupText = remember(state.lastAutoBackupTimestamp) {
                                        if (state.lastAutoBackupTimestamp > 0) {
                                            val formatted = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(state.lastAutoBackupTimestamp))
                                            "Último respaldo completado: $formatted"
                                        } else {
                                            "Aún no se ha realizado ningún respaldo automático"
                                        }
                                    }

                                    Text(
                                        text = lastBackupText,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (pendingRestoreUri != null) {
        val uri = pendingRestoreUri!!
        var isRestoring by remember { mutableStateOf(false) }

        AlertDialog(
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
                        CircularProgressIndicator()
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
            initialHour = state.autoBackupHour,
            initialMinute = state.autoBackupMinute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePickerDialog = false },
            confirmButton = {
                MinicashFilledButton(
                    onClick = {
                        viewModel.setAutoBackupTime(context, timePickerState.hour, timePickerState.minute)
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
            title = { Text("Hora del respaldo automático", fontWeight = FontWeight.Bold) },
            text = { TimePicker(state = timePickerState) },
            shape = MinicashShapes.Hero
        )
    }
}
