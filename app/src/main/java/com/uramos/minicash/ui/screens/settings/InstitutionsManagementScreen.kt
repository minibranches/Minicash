package com.uramos.minicash.ui.screens.settings

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.uramos.minicash.ui.components.fadingTopEdge
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
    val haptic = LocalHapticFeedback.current
    val haptics = rememberMinicashHaptics()

    var showAddSheet by remember { mutableStateOf(false) }
    var instToDelete by remember { mutableStateOf<CreditInstitutionEntity?>(null) }
    var instToEdit by remember { mutableStateOf<CreditInstitutionEntity?>(null) }

    if (instToDelete != null) {
        val inst = instToDelete!!
        MinicashAlertDialog(
            onDismissRequest = { instToDelete = null },
            title = { Text("¿Eliminar institución?", fontWeight = FontWeight.Bold) },
            text = { Text("¿Deseas eliminar '${inst.name}' de tus instituciones de crédito?") },
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
                    Text("Eliminar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                MinicashTextButton(onClick = { instToDelete = null }) { Text("Cancelar") }
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
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        topBar = {
            MinicashSubScreenTopBar(
                title = "Tarjetas e instituciones",
                onBack = onNavigateBack,
                scrollBehavior = scrollBehavior,
                actions = {
                    MinicashIconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showAddSheet = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Nueva institución"
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
                    title = "Sin instituciones registradas",
                    subtitle = "Agrega tus tarjetas o bancos mediante el botón inferior.",
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
                            Text("Agregar nueva institución")
                        }
                    }
                )
            }
        } else {
            LazyColumn(
                state = fadeListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                .fadingTopEdge(fadeListState),
                contentPadding = PaddingValues(
                    top = spacing.small,
                    start = spacing.standard,
                    end = spacing.standard,
                    bottom = 120.dp
                ),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                item {
                    Text(
                        text = "Instituciones financieras registradas",
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
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            instToEdit = inst
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .animateItem()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spacing.standard, vertical = spacing.medium),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.medium)
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

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = inst.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                inst.paymentDay?.let { day ->
                                    Text(
                                        text = "Pago el día $day" + when (inst.reminderDaysBefore) {
                                            null -> ""
                                            0 -> " • aviso el mismo día"
                                            1 -> " • aviso 1 día antes"
                                            else -> " • aviso ${inst.reminderDaysBefore} días antes"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

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

                item {
                    Spacer(modifier = Modifier.height(spacing.medium))
                    MinicashCard(
                        level = CardTonalLevel.HIGH,
                        shape = CircleShape,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
                                text = "Agregar nueva institución",
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
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
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
                text = if (institution == null) "Nueva institución" else "Editar institución",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre de la institución") },
                placeholder = { Text("Escribe el nombre de la institución") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Color distintivo:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            MinicashColorPicker(
                selectedColorHex = selectedColorHex,
                onColorSelected = { selectedColorHex = it }
            )

            Text("Pago de las compras a meses:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Text(
                text = "Las compras de esta institución heredan este día; cada una puede usar otro.",
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
                MinicashTonalButton(text = "Cancelar", onClick = guard::requestDismiss, modifier = Modifier.weight(1f))
                MinicashButton(
                    text = "Guardar",
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
