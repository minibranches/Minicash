package com.uramos.minicash.ui.screens.settings

import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.uramos.minicash.R
import com.uramos.minicash.ui.theme.minicashAnimateItem
import com.uramos.minicash.ui.theme.screenBackground
import com.uramos.minicash.ui.theme.sheetBackground
import com.uramos.minicash.ui.components.plusTop
import com.uramos.minicash.ui.components.withoutTop
import com.uramos.minicash.ui.components.underHeaderBlur
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.uramos.minicash.ui.components.MinicashSubScreenTopBar
import com.uramos.minicash.ui.components.GuardedModalBottomSheet
import com.uramos.minicash.ui.components.MinicashAlertDialog
import com.uramos.minicash.ui.components.MinicashTextButton
import com.uramos.minicash.ui.components.MinicashFilledButton
import com.uramos.minicash.ui.components.MinicashIconButton
import com.uramos.minicash.ui.components.MinicashTonalButton
import com.uramos.minicash.ui.components.MinicashButton
import com.uramos.minicash.ui.components.DiscardChangesDialog
import com.uramos.minicash.ui.components.rememberDiscardGuard
import androidx.compose.foundation.layout.*
import com.uramos.minicash.ui.components.ScallopedIcon
import com.uramos.minicash.ui.components.MinicashListRowDefaults
import com.uramos.minicash.ui.utils.asIconTint
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.uramos.minicash.ui.utils.rememberMinicashHaptics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uramos.minicash.data.local.entities.CreditInstitutionEntity
import com.uramos.minicash.ui.components.CardTonalLevel
import com.uramos.minicash.ui.components.MinicashCard
import com.uramos.minicash.ui.components.MinicashColorPicker
import com.uramos.minicash.ui.components.MinicashEmptyState
import com.uramos.minicash.ui.components.rememberSegmentedShape
import com.uramos.minicash.ui.theme.MinicashShapes
import com.uramos.minicash.ui.theme.MinicashThemeExtensions
import com.uramos.minicash.ui.utils.MinicashColorUtils
import com.uramos.minicash.ui.screens.credits.NO_REMINDER
import com.uramos.minicash.ui.screens.credits.PaymentDayFields
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstitutionsManagementScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val spacing = MinicashThemeExtensions.spacing
    val state by viewModel.uiState.collectAsState()
    val haptics = rememberMinicashHaptics()

    var showAddSheet by remember { mutableStateOf(false) }
    var instToDelete by remember { mutableStateOf<CreditInstitutionEntity?>(null) }
    var instToEdit by remember { mutableStateOf<CreditInstitutionEntity?>(null) }

    if (instToDelete != null) {
        val inst = instToDelete!!
        MinicashAlertDialog(
            onDismissRequest = { instToDelete = null },
            title = { Text(stringResource(R.string.inst_delete_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.inst_delete_text, inst.name)) },
            confirmButton = {
                MinicashFilledButton(
                    onClick = {
                        haptics.heavyOrDelete()
                        viewModel.deleteCreditInstitution(inst)
                        instToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                ) {
                    Text(stringResource(R.string.common_delete), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                MinicashTextButton(onClick = { instToDelete = null }) { Text(stringResource(R.string.common_cancel)) }
            },
            shape = MinicashShapes.Hero
        )
    }

    if (showAddSheet) {
        AddInstitutionSheet(
            onDismiss = { showAddSheet = false },
            onConfirm = { name, colorHex, paymentDay, reminder ->
                viewModel.addCreditInstitution(name, colorHex, paymentDay, reminder)
                showAddSheet = false
            }
        )
    }

    instToEdit?.let { editing ->
        AddInstitutionSheet(
            institution = editing,
            onDismiss = { instToEdit = null },
            onConfirm = { name, colorHex, paymentDay, reminder ->
                viewModel.updateCreditInstitution(editing, name, colorHex, paymentDay, reminder)
                instToEdit = null
            }
        )
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val fadeListState = rememberLazyListState()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.screenBackground,
        topBar = {
            MinicashSubScreenTopBar(
                title = stringResource(R.string.set_institutions),
                onBack = onNavigateBack,
                scrollBehavior = scrollBehavior,
                actions = {
                    MinicashIconButton(
                        onClick = {
                            haptics.tick()
                            showAddSheet = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.credit_new_institution)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (state.creditInstitutions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                MinicashEmptyState(
                    icon = Icons.Default.CreditCard,
                    title = stringResource(R.string.credit_inst_empty_title),
                    subtitle = stringResource(R.string.credit_inst_empty_sub),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = spacing.hero),
                    actionButton = {
                        MinicashFilledButton(
                            onClick = { showAddSheet = true },
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .heightIn(min = 48.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.inst_add))
                        }
                    }
                )
            }
        } else {
            LazyColumn(
                state = fadeListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues.withoutTop())
                .underHeaderBlur(paddingValues.calculateTopPadding(), bottomBand = 64.dp, listState = fadeListState),
                contentPadding = PaddingValues(
                    top = spacing.small,
                    start = spacing.screen,
                    end = spacing.screen,
                    bottom = 120.dp
                ).plusTop(paddingValues.calculateTopPadding()),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                item {
                    Text(
                        text = stringResource(R.string.inst_registered),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(spacing.medium))
                }

                itemsIndexed(state.creditInstitutions, key = { _, inst -> inst.id }) { index, inst ->
                    val itemShape = rememberSegmentedShape(index = index, count = state.creditInstitutions.size)
                    val instColor = MinicashColorUtils.parseHexColor(inst.colorHex, MaterialTheme.colorScheme.primary)

                    MinicashCard(
                        level = CardTonalLevel.LOW,
                        shape = itemShape,
                        onClick = {
                            haptics.tick()
                            instToEdit = inst
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .then(minicashAnimateItem())
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = MinicashListRowDefaults.MinHeight)
                                .padding(horizontal = MinicashListRowDefaults.HorizontalPadding, vertical = MinicashListRowDefaults.VerticalPadding),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(MinicashListRowDefaults.IconTextGap)
                        ) {
                            ScallopedIcon(
                                icon = Icons.Default.CreditCard,
                                size = MinicashListRowDefaults.IconSize,
                                container = instColor.copy(alpha = 0.25f),
                                tint = instColor.asIconTint()
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = inst.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                inst.paymentDay?.let { day ->
                                    Text(
                                        text = stringResource(R.string.inst_payment_day, day) + when (inst.reminderDaysBefore) {
                                            null -> ""
                                            0 -> " • " + stringResource(R.string.inst_reminder_same)
                                            else -> " • " + pluralStringResource(R.plurals.inst_reminder_before, inst.reminderDaysBefore, inst.reminderDaysBefore)
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }

                            MinicashIconButton(
                                onClick = { instToDelete = inst },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.common_delete),
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(spacing.medium))
                    MinicashCard(
                        level = CardTonalLevel.HIGH,
                        shape = CircleShape,
                        onClick = {
                            haptics.tick()
                            showAddSheet = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.standard),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(spacing.small))
                            Text(
                                text = stringResource(R.string.inst_add),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInstitutionSheet(
    onDismiss: () -> Unit,
    onConfirm: (name: String, colorHex: String, paymentDay: Int?, reminderDaysBefore: Int?) -> Unit,
    /** Si se indica, la hoja edita esa institución en lugar de crear una nueva. */
    institution: CreditInstitutionEntity? = null
) {
    val initialName = institution?.name ?: ""
    val initialColor = institution?.colorHex ?: "#1A73E8"
    var name by remember(institution) { mutableStateOf(initialName) }
    var selectedColorHex by remember(institution) { mutableStateOf(initialColor) }
    val initialDay = institution?.paymentDay?.toString() ?: ""
    val initialReminder = institution?.reminderDaysBefore ?: NO_REMINDER
    var paymentDayStr by remember(institution) { mutableStateOf(initialDay) }
    var reminderDays by remember(institution) { mutableStateOf(initialReminder) }

    val paymentDay = paymentDayStr.toIntOrNull()
    val paymentInvalid = paymentDayStr.isNotEmpty() && (paymentDay == null || paymentDay !in 1..31)
    // El aviso solo tiene sentido con un día de pago válido
    val effectiveReminder = if (paymentDay != null && !paymentInvalid) reminderDays else NO_REMINDER
    val isDirty = name != initialName || selectedColorHex != initialColor ||
        paymentDayStr != initialDay || reminderDays != initialReminder

    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    val guard = rememberDiscardGuard(isDirty = isDirty, onDismiss = onDismiss)
    DiscardChangesDialog(guard)
    GuardedModalBottomSheet(
        guard = guard,
        containerColor = MaterialTheme.colorScheme.sheetBackground,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = MinicashShapes.SheetTop
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (institution == null) stringResource(R.string.credit_new_institution) else stringResource(R.string.inst_edit),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.credit_institution_name)) },
                placeholder = { Text(stringResource(R.string.credit_institution_name_hint)) },
                singleLine = true,
                shape = MinicashShapes.Input,
                modifier = Modifier.fillMaxWidth()
            )

            Text(stringResource(R.string.credit_color), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            MinicashColorPicker(
                selectedColorHex = selectedColorHex,
                onColorSelected = { selectedColorHex = it }
            )

            Text(stringResource(R.string.inst_payment_title), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Text(
                text = stringResource(R.string.inst_payment_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            PaymentDayFields(
                paymentDayStr = paymentDayStr,
                onPaymentDayChange = { paymentDayStr = it.filter(Char::isDigit).take(2) },
                paymentInvalid = paymentInvalid,
                reminderDays = effectiveReminder,
                onReminderChange = {
                    reminderDays = it
                    if (it != NO_REMINDER && Build.VERSION.SDK_INT >= 33 &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MinicashTonalButton(text = stringResource(R.string.common_cancel), onClick = guard::requestDismiss, modifier = Modifier.weight(1f))
                MinicashButton(
                    text = stringResource(R.string.common_save),
                    onClick = {
                        if (name.isNotBlank() && !paymentInvalid) {
                            onConfirm(
                                name.trim(), selectedColorHex,
                                paymentDay?.takeIf { !paymentInvalid },
                                effectiveReminder.takeIf { it != NO_REMINDER }
                            )
                        }
                    },
                    enabled = name.isNotBlank() && !paymentInvalid && isDirty,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
