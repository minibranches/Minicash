package com.uramos.minicash.ui.screens.settings

import com.uramos.minicash.ui.utils.AppStrings
import androidx.compose.ui.res.stringResource
import com.uramos.minicash.R
import com.uramos.minicash.ui.components.MinicashTimePickerDialog
import com.uramos.minicash.ui.theme.screenBackground
import com.uramos.minicash.ui.theme.sheetBackground
import com.uramos.minicash.ui.components.plusTop
import com.uramos.minicash.ui.components.withoutTop
import com.uramos.minicash.ui.components.underHeaderBlur
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import com.uramos.minicash.ui.components.rememberSegmentedShape
import com.uramos.minicash.data.util.CsvRange
import com.uramos.minicash.ui.components.MinicashLoading
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
import com.uramos.minicash.ui.components.MinicashRowGroup
import com.uramos.minicash.ui.components.MinicashOptionRow
import com.uramos.minicash.ui.components.MinicashSectionLabel
import com.uramos.minicash.ui.components.MinicashSectionNote
import com.uramos.minicash.ui.components.MinicashSubScreenTopBar
import com.uramos.minicash.ui.components.MinicashTextButton
import com.uramos.minicash.ui.theme.MinicashShapes
import com.uramos.minicash.ui.theme.MinicashThemeExtensions
import com.uramos.minicash.ui.utils.FLOATING_NAV_CLEARANCE
import com.uramos.minicash.ui.utils.rememberMinicashHaptics
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val WIPE_CONFIRMATION_WORD: String get() = AppStrings.get(R.string.backup_wipe_word)

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
    var showCsvRangeDialog by remember { mutableStateOf(false) }
    var pendingCsvRange by remember { mutableStateOf(CsvRange.ALL) }

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
    ) { uri: Uri? -> uri?.let { viewModel.exportCsv(context, it, pendingCsvRange) } }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? -> uri?.let { pendingRestoreUri = it } }

    if (showCsvRangeDialog) {
        CsvRangeDialog(
            onDismiss = { showCsvRangeDialog = false },
            onPick = { range ->
                showCsvRangeDialog = false
                pendingCsvRange = range
                val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                createCsvLauncher.launch("minicash_movimientos_$dateStr.csv")
            }
        )
    }

    val fadeListState = rememberLazyListState()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.padding(bottom = FLOATING_NAV_CLEARANCE)) },
        containerColor = MaterialTheme.colorScheme.screenBackground,
        topBar = {
            MinicashSubScreenTopBar(
                title = stringResource(R.string.set_backups),
                onBack = onBackClick,
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = fadeListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues.withoutTop())
                .underHeaderBlur(paddingValues.calculateTopPadding(), bottomBand = 64.dp, listState = fadeListState),
            contentPadding = PaddingValues(horizontal = spacing.screen, vertical = spacing.small).plusTop(paddingValues.calculateTopPadding()),
            verticalArrangement = Arrangement.spacedBy(spacing.small)
        ) {
            item { MinicashSectionLabel(stringResource(R.string.backup_section_backup)) }
            item {
                MinicashRowGroup {
                    MinicashOptionRow(
                        title = stringResource(R.string.backup_create),
                        subtitle = stringResource(R.string.backup_create_sub),
                        icon = Icons.Default.CloudUpload,
                        index = 0, count = 2,
                        onClick = {
                            val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                            createDocumentLauncher.launch("minicash_backup_$dateStr.json")
                        }
                    )
                    MinicashOptionRow(
                        title = stringResource(R.string.backup_restore),
                        subtitle = stringResource(R.string.backup_restore_sub),
                        icon = Icons.Default.CloudDownload,
                        index = 1, count = 2,
                        onClick = { openDocumentLauncher.launch(arrayOf("application/json", "*/*")) }
                    )
                }
            }
            item {
                MinicashSectionNote(
                    stringResource(R.string.backup_note)
                )
            }

            item { MinicashSectionLabel(stringResource(R.string.backup_section_auto)) }
            item {
                val timeText = "${state.autoBackupHour.toString().padStart(2, '0')}:${state.autoBackupMinute.toString().padStart(2, '0')}"
                MinicashRowGroup {
                    MinicashOptionRow(
                        title = stringResource(R.string.backup_daily),
                        subtitle = if (state.autoBackupEnabled) stringResource(R.string.backup_daily_on, timeText) else stringResource(R.string.backup_daily_off),
                        icon = Icons.Default.AutoMode,
                        index = 0, count = if (state.autoBackupEnabled) 2 else 1,
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
                            title = stringResource(R.string.backup_time),
                            subtitle = stringResource(R.string.set_hours, timeText),
                            icon = Icons.Default.Schedule,
                            index = 1, count = 2,
                            onClick = { showTimePickerDialog = true }
                        )
                    }
                }
            }
            item {
                val lastBackupText = remember(state.lastAutoBackupTimestamp) {
                    if (state.lastAutoBackupTimestamp > 0) {
                        val formatted = com.uramos.minicash.ui.utils.appDateFormat("dd/MM/yyyy HH:mm", "MM/dd/yyyy HH:mm").format(Date(state.lastAutoBackupTimestamp))
                        AppStrings.get(R.string.backup_last, formatted)
                    } else {
                        AppStrings.get(R.string.backup_never)
                    }
                }
                MinicashSectionNote(
                    stringResource(R.string.backup_auto_note, lastBackupText)
                )
            }

            item { MinicashSectionLabel(stringResource(R.string.backup_section_sheets)) }
            item {
                MinicashRowGroup {
                    MinicashOptionRow(
                        title = stringResource(R.string.backup_csv),
                        subtitle = stringResource(R.string.backup_csv_sub),
                        icon = Icons.Default.TableChart,
                        onClick = { showCsvRangeDialog = true }
                    )
                }
            }
            item {
                MinicashSectionNote(stringResource(R.string.backup_csv_note))
            }

            item { MinicashSectionLabel(stringResource(R.string.backup_section_danger), color = MaterialTheme.colorScheme.error) }
            item {
                MinicashRowGroup {
                    MinicashOptionRow(
                        title = stringResource(R.string.backup_wipe),
                        subtitle = stringResource(R.string.backup_wipe_sub),
                        icon = Icons.Default.DeleteForever,
                        iconContainer = MaterialTheme.colorScheme.errorContainer,
                        iconTint = MaterialTheme.colorScheme.onErrorContainer,
                        titleColor = MaterialTheme.colorScheme.error,
                        onClick = { showWipeDialog = true }
                    )
                }
            }
            item {
                MinicashSectionNote(stringResource(R.string.backup_wipe_note))
                androidx.compose.foundation.layout.Spacer(Modifier.size(96.dp))
            }
        }
    }

    pendingRestoreUri?.let { uri ->
        var isRestoring by remember { mutableStateOf(false) }

        MinicashAlertDialog(
            onDismissRequest = { if (!isRestoring) pendingRestoreUri = null },
            title = { Text(stringResource(R.string.backup_restore_title), fontWeight = FontWeight.Bold) },
            text = {
                if (isRestoring) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MinicashLoading()
                        Text(stringResource(R.string.backup_restoring), style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    Text(stringResource(R.string.backup_restore_text))
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
                    ) { Text(stringResource(R.string.backup_restore_btn), fontWeight = FontWeight.Bold) }
                }
            },
            dismissButton = {
                if (!isRestoring) {
                    MinicashTextButton(onClick = { pendingRestoreUri = null }) { Text(stringResource(R.string.common_cancel)) }
                }
            },
            shape = MinicashShapes.Hero
        )
    }

    if (showWipeDialog) {
        var typed by rememberSaveable { mutableStateOf("") }
        MinicashAlertDialog(
            onDismissRequest = { showWipeDialog = false },
            title = { Text(stringResource(R.string.backup_wipe_title), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.backup_wipe_text))
                    OutlinedTextField(
                        value = typed,
                        onValueChange = { typed = it },
                        label = { Text(stringResource(R.string.backup_wipe_confirm, WIPE_CONFIRMATION_WORD)) },
                        singleLine = true,
                        shape = MinicashShapes.Input,
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
                ) { Text(stringResource(R.string.backup_wipe_btn), fontWeight = FontWeight.Bold) }
            },
            dismissButton = { MinicashTextButton(onClick = { showWipeDialog = false }) { Text(stringResource(R.string.common_cancel)) } },
            shape = MinicashShapes.Hero
        )
    }

    if (showTimePickerDialog) {
        MinicashTimePickerDialog(
            title = stringResource(R.string.backup_auto_time_title),
            initialHour = state.autoBackupHour,
            initialMinute = state.autoBackupMinute,
            onConfirm = { hour, minute ->
                viewModel.setAutoBackupTime(context, hour, minute)
                showTimePickerDialog = false
            },
            onDismiss = { showTimePickerDialog = false }
        )
    }
}

/** Elige qué periodo de movimientos se exporta; al tocar una opción se abre el selector de archivos. */
@Composable
private fun CsvRangeDialog(onDismiss: () -> Unit, onPick: (CsvRange) -> Unit) {
    MinicashAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.backup_csv_which), fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                CsvRange.entries.forEachIndexed { index, range ->
                    Surface(
                        onClick = { onPick(range) },
                        shape = rememberSegmentedShape(index = index, count = CsvRange.entries.size),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = range.label,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } }
    )
}
