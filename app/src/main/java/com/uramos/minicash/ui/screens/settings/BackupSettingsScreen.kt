package com.uramos.minicash.ui.screens.settings

import androidx.compose.foundation.lazy.rememberLazyListState
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uramos.minicash.ui.components.MinicashAlertDialog
import com.uramos.minicash.ui.components.MinicashFilledButton
import com.uramos.minicash.ui.components.MinicashOptionGroup
import com.uramos.minicash.ui.components.MinicashOptionRow
import com.uramos.minicash.ui.components.MinicashSectionLabel
import com.uramos.minicash.ui.components.MinicashSectionNote
import com.uramos.minicash.ui.components.MinicashSubScreenTopBar
import com.uramos.minicash.ui.components.MinicashTextButton
import com.uramos.minicash.ui.components.fadingTopEdge
import com.uramos.minicash.ui.theme.MinicashShapes
import com.uramos.minicash.ui.theme.MinicashThemeExtensions
import com.uramos.minicash.ui.utils.FLOATING_NAV_CLEARANCE
import com.uramos.minicash.ui.utils.rememberMinicashHaptics
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val WIPE_CONFIRMATION_WORD = "BORRAR"

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
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val snackbarHostState = remember { SnackbarHostState() }
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var showWipeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.messageEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? -> uri?.let { viewModel.exportBackup(context, it) } }

    val createCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? -> uri?.let { viewModel.exportCsv(context, it) } }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? -> uri?.let { pendingRestoreUri = it } }

    val fadeListState = rememberLazyListState()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.padding(bottom = FLOATING_NAV_CLEARANCE)) },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        topBar = {
            MinicashSubScreenTopBar(
                title = "Copias de seguridad",
                onBack = onBackClick,
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = fadeListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .fadingTopEdge(fadeListState),
            contentPadding = PaddingValues(horizontal = spacing.standard, vertical = spacing.small),
            verticalArrangement = Arrangement.spacedBy(spacing.small)
        ) {
            item { MinicashSectionLabel("Copia de seguridad") }
            item {
                MinicashOptionGroup {
                    MinicashOptionRow(
                        title = "Crear copia de seguridad",
                        subtitle = "Cuentas, categorías, movimientos, gastos fijos y créditos",
                        icon = Icons.Default.CloudUpload,
                        iconContainer = MaterialTheme.colorScheme.tertiaryContainer,
                        iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                        onClick = {
                            val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                            createDocumentLauncher.launch("minicash_backup_$dateStr.json")
                        }
                    )
                    MinicashOptionRow(
                        title = "Restaurar copia",
                        subtitle = "Reemplaza los datos actuales con los de un archivo",
                        icon = Icons.Default.CloudDownload,
                        onClick = { openDocumentLauncher.launch(arrayOf("application/json", "*/*")) }
                    )
                }
            }
            item {
                MinicashSectionNote(
                    "La copia es un archivo JSON completo de la app: cuentas, categorías, movimientos, presupuesto, " +
                        "créditos con sus instituciones y tus ajustes. Restaurar reemplaza todo lo que hay ahora; " +
                        "antes se guarda una copia interna de lo actual."
                )
            }

            item { MinicashSectionLabel("Respaldo automático") }
            item {
                val timeText = "${state.autoBackupHour.toString().padStart(2, '0')}:${state.autoBackupMinute.toString().padStart(2, '0')}"
                MinicashOptionGroup {
                    MinicashOptionRow(
                        title = "Respaldo diario",
                        subtitle = if (state.autoBackupEnabled) "Programado todos los días a las $timeText hrs" else "Desactivado",
                        icon = Icons.Default.AutoMode,
                        iconContainer = MaterialTheme.colorScheme.primaryContainer,
                        iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                        onClick = {
                            haptics.click()
                            viewModel.setAutoBackupEnabled(context, !state.autoBackupEnabled)
                        },
                        trailing = {
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
                    )
                    AnimatedVisibility(visible = state.autoBackupEnabled) {
                        MinicashOptionRow(
                            title = "Hora del respaldo",
                            subtitle = "$timeText hrs",
                            icon = Icons.Default.Schedule,
                            onClick = { showTimePickerDialog = true }
                        )
                    }
                }
            }
            item {
                val lastBackupText = remember(state.lastAutoBackupTimestamp) {
                    if (state.lastAutoBackupTimestamp > 0) {
                        val formatted = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(state.lastAutoBackupTimestamp))
                        "Último respaldo completado: $formatted."
                    } else {
                        "Aún no se ha realizado ningún respaldo automático."
                    }
                }
                MinicashSectionNote(
                    "Las copias automáticas se guardan en el almacenamiento interno de la app y se conservan las 3 más recientes. $lastBackupText"
                )
            }

            item { MinicashSectionLabel("Hojas de cálculo") }
            item {
                MinicashOptionGroup {
                    MinicashOptionRow(
                        title = "Exportar a CSV",
                        subtitle = "Guarda cada movimiento en un archivo para hojas de cálculo",
                        icon = Icons.Default.TableChart,
                        onClick = {
                            val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                            createCsvLauncher.launch("minicash_movimientos_$dateStr.csv")
                        }
                    )
                }
            }
            item {
                MinicashSectionNote("El CSV solo lleva movimientos y sirve para analizarlos fuera de la app. Para respaldar todo usa la copia de seguridad.")
            }

            item { MinicashSectionLabel("Zona de peligro", color = MaterialTheme.colorScheme.error) }
            item {
                MinicashOptionGroup {
                    MinicashOptionRow(
                        title = "Borrar todos los datos",
                        subtitle = "Elimina cuentas, movimientos, presupuesto y créditos",
                        icon = Icons.Default.DeleteForever,
                        iconContainer = MaterialTheme.colorScheme.errorContainer,
                        iconTint = MaterialTheme.colorScheme.onErrorContainer,
                        titleColor = MaterialTheme.colorScheme.error,
                        onClick = { showWipeDialog = true }
                    )
                }
            }
            item {
                MinicashSectionNote("Antes de borrar se guarda una copia interna, por si necesitas recuperar los datos.")
                androidx.compose.foundation.layout.Spacer(Modifier.size(96.dp))
            }
        }
    }

    pendingRestoreUri?.let { uri ->
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
                    ) { Text("Restaurar", fontWeight = FontWeight.Bold) }
                }
            },
            dismissButton = {
                if (!isRestoring) {
                    MinicashTextButton(onClick = { pendingRestoreUri = null }) { Text("Cancelar") }
                }
            },
            shape = MinicashShapes.Hero
        )
    }

    if (showWipeDialog) {
        var typed by rememberSaveable { mutableStateOf("") }
        MinicashAlertDialog(
            onDismissRequest = { showWipeDialog = false },
            title = { Text("¿Borrar todos los datos?", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Se eliminarán cuentas, categorías, movimientos, presupuesto y créditos. Se guarda una copia interna antes de borrar.")
                    OutlinedTextField(
                        value = typed,
                        onValueChange = { typed = it },
                        label = { Text("Escribe $WIPE_CONFIRMATION_WORD para confirmar") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions.Default,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                MinicashFilledButton(
                    onClick = {
                        haptics.heavyOrDelete()
                        viewModel.wipeAllData(context)
                        showWipeDialog = false
                    },
                    enabled = typed.trim().equals(WIPE_CONFIRMATION_WORD, ignoreCase = true),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) { Text("Borrar todo", fontWeight = FontWeight.Bold) }
            },
            dismissButton = { MinicashTextButton(onClick = { showWipeDialog = false }) { Text("Cancelar") } },
            shape = MinicashShapes.Hero
        )
    }

    if (showTimePickerDialog) {
        val timePickerState = rememberTimePickerState(
            initialHour = state.autoBackupHour,
            initialMinute = state.autoBackupMinute,
            is24Hour = true
        )
        MinicashAlertDialog(
            onDismissRequest = { showTimePickerDialog = false },
            confirmButton = {
                MinicashFilledButton(
                    onClick = {
                        viewModel.setAutoBackupTime(context, timePickerState.hour, timePickerState.minute)
                        showTimePickerDialog = false
                    },
                ) { Text("Guardar", fontWeight = FontWeight.Bold) }
            },
            dismissButton = { MinicashTextButton(onClick = { showTimePickerDialog = false }) { Text("Cancelar") } },
            title = { Text("Hora del respaldo automático", fontWeight = FontWeight.Bold) },
            text = { TimePicker(state = timePickerState) },
            shape = MinicashShapes.Hero
        )
    }
}
