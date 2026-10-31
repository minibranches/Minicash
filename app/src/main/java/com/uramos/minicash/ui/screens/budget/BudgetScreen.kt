package com.uramos.minicash.ui.screens.budget

import com.uramos.minicash.ui.components.GuardedModalBottomSheet
import com.uramos.minicash.ui.components.MinicashAlertDialog
import com.uramos.minicash.ui.components.MinicashSegmentedChoice
import com.uramos.minicash.ui.components.MinicashTextButton
import com.uramos.minicash.ui.components.MinicashFilledButton
import com.uramos.minicash.ui.components.MinicashIconButton
import com.uramos.minicash.ui.components.MinicashOutlinedButton
import com.uramos.minicash.ui.components.MinicashTonalIconButton
import androidx.compose.material.icons.filled.DragHandle
import sh.calvin.reorderable.rememberReorderableLazyListState
import sh.calvin.reorderable.ReorderableItem
import com.uramos.minicash.ui.components.WithoutMinTouchTarget
import com.uramos.minicash.ui.components.listEntrance
import com.uramos.minicash.ui.components.rememberEntranceTracker
import com.uramos.minicash.ui.components.DiscardChangesDialog
import com.uramos.minicash.ui.components.rememberDiscardGuard
import com.uramos.minicash.ui.utils.toCents
import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import com.uramos.minicash.ui.theme.MinicashShapes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import com.uramos.minicash.ui.components.MinicashSwipeNumpad
import com.uramos.minicash.ui.transactions.KeypadButton
import com.uramos.minicash.ui.utils.normalizeUtcToLocalMillis
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.uramos.minicash.ui.utils.FLOATING_NAV_CLEARANCE
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import com.uramos.minicash.ui.utils.rememberMinicashHaptics
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.uramos.minicash.data.local.entities.BudgetItemEntity
import com.uramos.minicash.data.local.entities.BudgetItemType
import com.uramos.minicash.ui.components.CardTonalLevel
import androidx.compose.animation.core.snap
import com.uramos.minicash.ui.components.MinicashCard
import com.uramos.minicash.ui.components.MinicashEmptyState
import com.uramos.minicash.ui.components.MoneyDisplayType
import com.uramos.minicash.ui.components.MoneyTextView
import com.uramos.minicash.ui.components.MinicashButton
import com.uramos.minicash.ui.components.MinicashPillTabs
import com.uramos.minicash.ui.components.MinicashProgressBar
import com.uramos.minicash.ui.components.rememberSegmentedShape
import com.uramos.minicash.ui.theme.LocalAnimationRestriction
import com.uramos.minicash.ui.theme.MinicashThemeExtensions
import com.uramos.minicash.ui.utils.formatTransactionDate
import com.uramos.minicash.ui.utils.toCurrencyFormat
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel,
    listState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState(),
    showAddDialogExternal: Boolean = false,
    onAddDialogExternalHandled: () -> Unit = {},
    onOpenAddTransactionForBudgetItem: ((BudgetItemEntity) -> Unit)? = null
) {
    val spacing = MinicashThemeExtensions.spacing
    val state by viewModel.uiState.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val haptics = rememberMinicashHaptics()
    val entranceTracker = rememberEntranceTracker()

    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(showAddDialogExternal) {
        if (showAddDialogExternal) {
            showAddDialog = true
            onAddDialogExternalHandled()
        }
    }
    var showSelectiveCloneDialog by remember { mutableStateOf(false) }
    var itemToPay by remember { mutableStateOf<BudgetItemEntity?>(null) }
    var itemToDelete by remember { mutableStateOf<BudgetItemEntity?>(null) }
    var itemToEdit by remember { mutableStateOf<BudgetItemEntity?>(null) }

    // Orden local mientras se arrastra; se guarda en la base al soltar
    var orderedItems by remember(state.items) { mutableStateOf(state.items) }
    val reorderState = rememberReorderableLazyListState(listState) { from, to ->
        val list = orderedItems.toMutableList()
        val fromIdx = list.indexOfFirst { it.id == from.key }
        val toIdx = list.indexOfFirst { it.id == to.key }
        if (fromIdx >= 0 && toIdx >= 0) {
            list.add(toIdx, list.removeAt(fromIdx))
            orderedItems = list
            haptics.tick()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.padding(bottom = FLOATING_NAV_CLEARANCE)) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                ),
                title = {
                    Text(
                        "Gastos fijos",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    MinicashIconButton(
                        onClick = {
                            showSelectiveCloneDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copiar partidas de quincena"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Selector de Mes estilo Cápsula Expressive con animación
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.standard, vertical = spacing.extraSmall),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                WithoutMinTouchTarget {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MinicashTonalIconButton(
                            onClick = { viewModel.previousMonth() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Mes anterior",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        val isRestricted = LocalAnimationRestriction.current
                        val motionScheme = MaterialTheme.motionScheme

                        AnimatedContent(
                            targetState = state.currentMonthLabel,
                            transitionSpec = {
                                if (isRestricted) {
                                    fadeIn(animationSpec = snap()) togetherWith fadeOut(animationSpec = snap())
                                } else {
                                    val spatialSpec = motionScheme.defaultSpatialSpec<androidx.compose.ui.unit.IntOffset>()
                                    val effectsSpec = motionScheme.defaultEffectsSpec<Float>()
                                    (slideInHorizontally(animationSpec = spatialSpec) { width -> width / 2 } + fadeIn(animationSpec = effectsSpec)) togetherWith
                                    (slideOutHorizontally(animationSpec = spatialSpec) { width -> -width / 2 } + fadeOut(animationSpec = effectsSpec))
                                }
                            },
                            label = "budget_month_transition"
                        ) { monthLabel ->
                            Text(
                                text = monthLabel,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        MinicashTonalIconButton(
                            onClick = { viewModel.nextMonth() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Mes siguiente",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Pestañas de Quincena (Segmented Controls Expressive)
            MinicashPillTabs(
                labels = listOf("1ª Quincena", "2ª Quincena"),
                selectedIndex = state.selectedFortnight - 1,
                onSelect = { index ->
                    haptics.tick()
                    viewModel.selectFortnight(index + 1)
                },
                modifier = Modifier.padding(horizontal = spacing.standard, vertical = spacing.small)
            )

            // Dashboard Compacto de Presupuesto
            MinicashCard(
                level = CardTonalLevel.LOW,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.standard, vertical = spacing.extraSmall)
            ) {
                Column(
                    modifier = Modifier.padding(spacing.standard),
                    verticalArrangement = Arrangement.spacedBy(spacing.small)
                ) {
                    // Fila 1: Disponible libre actual
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Disponible libre actual",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val freeAmountType = if (state.summary.actualFreeAmount >= 0) MoneyDisplayType.INCOME else MoneyDisplayType.EXPENSE
                            MoneyTextView(
                                amountCents = state.summary.actualFreeAmount,
                                type = freeAmountType,
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Fila 2: 3 columnas distribuidas equitativamente
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Columna 1: Ingresos
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Ingresos",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            MoneyTextView(
                                amountCents = state.summary.executedIncome,
                                type = MoneyDisplayType.INCOME,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "de ${state.summary.expectedIncome.toCurrencyFormat()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Columna 2: Reembolsos
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Reembolsos",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            MoneyTextView(
                                amountCents = state.summary.executedReimbursement,
                                type = MoneyDisplayType.INCOME,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "de ${state.summary.expectedReimbursement.toCurrencyFormat()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Columna 3: Gastos
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "Gastos",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            MoneyTextView(
                                amountCents = state.summary.paidExpense,
                                type = MoneyDisplayType.EXPENSE,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "de ${state.summary.expectedExpense.toCurrencyFormat()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Barra expresiva ondulada de ejecución de gastos
                    val expenseProgress = if (state.summary.expectedExpense > 0) {
                        (state.summary.paidExpense.toFloat() / state.summary.expectedExpense.toFloat()).coerceIn(0f, 1f)
                    } else 0f

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Avance de ejecución quincenal",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${(expenseProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        MinicashProgressBar(progress = expenseProgress)
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.small))

            // Lista de partidas del presupuesto
            if (state.items.isEmpty()) {
                MinicashEmptyState(
                    icon = Icons.Default.Savings,
                    title = "Sin partidas presupuestadas",
                    subtitle = "No has agregado ingresos ni gastos para esta quincena.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = spacing.hero)
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = spacing.small,
                        start = spacing.standard,
                        end = spacing.standard,
                        bottom = 120.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    itemsIndexed(orderedItems, key = { _, item -> item.id }) { index, item ->
                        ReorderableItem(reorderState, key = item.id) { _ ->
                            val itemShape = rememberSegmentedShape(index = index, count = orderedItems.size)

                            BudgetItemRow(
                                item = item,
                                shape = itemShape,
                                dragModifier = Modifier.draggableHandle(
                                    onDragStopped = { viewModel.saveItemOrder(orderedItems.map { it.id }) }
                                ),
                                onClick = { itemToEdit = item },
                                onToggleExecuted = { itemToPay = item },
                                onDelete = { itemToDelete = item },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .listEntrance(entranceTracker, item.id, index)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddBudgetItemDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { concept, amountDouble, itemType, responsible, dueDate ->
                viewModel.addItem(concept, amountDouble, itemType, responsible, dueDate)
                showAddDialog = false
            }
        )
    }

    if (itemToEdit != null) {
        val item = itemToEdit!!
        EditBudgetItemSheet(
            item = item,
            onDismiss = { itemToEdit = null },
            onConfirm = { concept, amountDouble, itemType, responsible, dueDate ->
                viewModel.updateBudgetItem(item, concept, amountDouble, itemType, responsible, dueDate)
                itemToEdit = null
            },
            onDelete = { target ->
                viewModel.deleteItem(target)
                itemToEdit = null
            }
        )
    }

    if (itemToDelete != null) {
        val item = itemToDelete!!
        MinicashAlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("¿Eliminar partida?", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro de que deseas eliminar la partida '${item.concept}' de ${item.amount.toCurrencyFormat()}?") },
            confirmButton = {
                MinicashFilledButton(
                    onClick = {
                        haptics.heavyOrDelete()
                        viewModel.deleteItem(item)
                        itemToDelete = null
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
                MinicashTextButton(onClick = { itemToDelete = null }) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (showSelectiveCloneDialog) {
        CopyFortnightSheet(
            viewModel = viewModel,
            onDismiss = { showSelectiveCloneDialog = false },
            onConfirmed = { count ->
                scope.launch {
                    if (count > 0) {
                        snackbarHostState.showSnackbar("Se copiaron $count partidas con éxito")
                    } else {
                        snackbarHostState.showSnackbar("No se encontraron partidas para copiar en la quincena seleccionada")
                    }
                }
                showSelectiveCloneDialog = false
            }
        )
    }

    if (itemToPay != null) {
        val item = itemToPay!!
        if (item.isPaid) {
            ReactivateBudgetItemDialog(
                item = item,
                onDismiss = { itemToPay = null },
                onConfirm = {
                    viewModel.toggleItemPaid(item)
                    itemToPay = null
                }
            )
        } else {
            ConfirmPaymentDialog(
                item = item,
                onDismiss = { itemToPay = null },
                onConfirm = { createTx ->
                    haptics.confirm()
                    viewModel.toggleItemPaid(item)
                    itemToPay = null
                    if (createTx && onOpenAddTransactionForBudgetItem != null) {
                        onOpenAddTransactionForBudgetItem(item)
                    }
                }
            )
        }
    }
}

@Composable
fun BudgetItemRow(
    item: BudgetItemEntity,
    onClick: () -> Unit,
    onToggleExecuted: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape? = null,
    /** Modificador de arrastre (draggableHandle) que se aplica al asa de reordenamiento. */
    dragModifier: Modifier = Modifier
) {
    val spacing = MinicashThemeExtensions.spacing
    val colorScheme = MaterialTheme.colorScheme

    val containerColor = remember(item.isPaid, colorScheme) {
        when {
            item.isPaid -> colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
            else -> colorScheme.surfaceContainerLow
        }
    }

    val displayType = when (item.type) {
        BudgetItemType.EXPENSE -> MoneyDisplayType.EXPENSE
        BudgetItemType.INCOME, BudgetItemType.REIMBURSEMENT -> MoneyDisplayType.INCOME
    }

    MinicashCard(
        level = CardTonalLevel.LOW,
        shape = shape ?: RoundedCornerShape(20.dp),
        containerColor = containerColor,
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.standard, vertical = spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.medium),
                modifier = Modifier.weight(1f)
            ) {
                MinicashIconButton(
                    onClick = onToggleExecuted,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (item.isPaid) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = if (item.isPaid) "Marcar pendiente" else "Marcar ejecutado",
                        tint = if (item.isPaid) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column {
                    Text(
                        text = item.concept,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isPaid) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(spacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.type.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (item.dueDate != null) {
                            val dateFormat = remember { SimpleDateFormat("dd/MM", Locale.forLanguageTag("es-MX")) }
                            Text(
                                text = "•  ${dateFormat.format(Date(item.dueDate))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
            ) {
                MoneyTextView(
                    amountCents = item.amount,
                    type = displayType,
                    style = MaterialTheme.typography.titleMedium
                )

                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Mantén presionado y arrastra para reordenar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = dragModifier
                        .size(36.dp)
                        .padding(6.dp)
                )

                MinicashIconButton(onClick = onClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar partida",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                MinicashIconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar partida",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ReactivateBudgetItemDialog(
    item: BudgetItemEntity,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    MinicashAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Reactivar partida?", fontWeight = FontWeight.Bold) },
        text = {
            Text(
                "¿Deseas marcar esta partida como pendiente nuevamente?",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            MinicashFilledButton(
                onClick = onConfirm,
            ) {
                Text("Reactivar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            MinicashTextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun ConfirmPaymentDialog(
    item: BudgetItemEntity,
    onDismiss: () -> Unit,
    onConfirm: (createTransaction: Boolean) -> Unit
) {
    var createTransaction by remember { mutableStateOf(true) }

    MinicashAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar ${item.concept}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "¿Deseas registrar '${item.concept}' (${item.amount.toCurrencyFormat()}) como ejecutado?",
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = createTransaction,
                        onCheckedChange = { createTransaction = it }
                    )
                    Text(
                        text = "Crear transacción",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            MinicashFilledButton(
                onClick = { onConfirm(createTransaction) },
            ) {
                Text("Confirmar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            MinicashTextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (concept: String, amountDouble: Double, itemType: BudgetItemType, responsible: String, dueDate: Long?) -> Unit
) {
    var concept by remember { mutableStateOf("") }
    var amountExpression by remember { mutableStateOf("") }
    var calculatedAmount by remember { mutableDoubleStateOf(0.0) }
    var responsible by remember { mutableStateOf("") }
    var itemType by remember { mutableStateOf(BudgetItemType.EXPENSE) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current
    val haptics = rememberMinicashHaptics()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val conceptFocusRequester = remember { FocusRequester() }

    val canConfirm = concept.isNotBlank() && calculatedAmount > 0.0

    

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(200)
        conceptFocusRequester.requestFocus()
    }

    val guard = rememberDiscardGuard(isDirty = concept.isNotBlank() || amountExpression.isNotBlank() || responsible.isNotBlank(), onDismiss = onDismiss)
    DiscardChangesDialog(guard)
        GuardedModalBottomSheet(
        guard = guard,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nueva partida de presupuesto",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                val formattedDateLabel = selectedDateMillis?.let { formatTransactionDate(it) } ?: "Sin fecha"
                Surface(
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        showDatePicker = true
                    },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.height(32.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formattedDateLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Selector superior en píldora: [ Gasto | Ingreso | Reembolso ]
            MinicashSegmentedChoice(
                options = BudgetItemType.entries,
                isSelected = { itemType == it },
                onSelect = { itemType = it },
                label = { it.displayName }
            )

            // Importe destacado
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = if (amountExpression.isNotBlank()) amountExpression else "0",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val cents = calculatedAmount.toCents()
                    Text(
                        text = cents.toCurrencyFormat(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            OutlinedTextField(
                value = concept,
                onValueChange = { concept = it },
                label = { Text("Concepto") },
                placeholder = { Text("Nombre de la partida") },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(conceptFocusRequester)
            )

            OutlinedTextField(
                value = responsible,
                onValueChange = { responsible = it },
                label = { Text("Responsable") },
                placeholder = { Text("Nombre del responsable") },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Teclado Numérico Embebido con Swipe a Operadores
            MinicashSwipeNumpad(
                amountExpression = amountExpression,
                onExpressionChange = { expr, calcVal ->
                    amountExpression = expr
                    calculatedAmount = calcVal
                }
            )

            // Botón de acción principal (Píldora full width independiente)
            MinicashButton(
                text = "Agregar partida",
                onClick = {
                    haptics.confirm()
                    if (calculatedAmount > 0.0) {
                        onConfirm(concept.trim(), calculatedAmount, itemType, responsible.trim(), selectedDateMillis)
                    }
                
                },
                enabled = canConfirm,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                MinicashTextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDateMillis = normalizeUtcToLocalMillis(it)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                MinicashTextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBudgetItemSheet(
    item: BudgetItemEntity,
    onDismiss: () -> Unit,
    onConfirm: (concept: String, amountDouble: Double, itemType: BudgetItemType, responsible: String, dueDate: Long?) -> Unit,
    onDelete: (item: BudgetItemEntity) -> Unit
) {
    var concept by remember { mutableStateOf(item.concept) }
    val initialVal = remember(item) { (item.amount / 100.0).let { if (it % 1 == 0.0) it.toLong().toString() else it.toString() } }
    var amountExpression by remember { mutableStateOf(initialVal) }
    var calculatedAmount by remember { mutableDoubleStateOf(item.amount / 100.0) }
    var responsible by remember { mutableStateOf(item.responsible) }
    var itemType by remember { mutableStateOf(item.type) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(item.dueDate ?: System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current
    val haptics = rememberMinicashHaptics()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val canConfirm = concept.isNotBlank() && calculatedAmount > 0.0

    

    if (showDeleteConfirmDialog) {
        MinicashAlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("¿Eliminar partida?", fontWeight = FontWeight.Bold) },
            text = { Text("¿Deseas eliminar '${item.concept}' de tus partidas de presupuesto?") },
            confirmButton = {
                MinicashFilledButton(
                    onClick = {
                        haptics.heavyOrDelete()
                        onDelete(item)
                        showDeleteConfirmDialog = false
                        onDismiss()
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
                MinicashTextButton(onClick = { showDeleteConfirmDialog = false }) { Text("Cancelar") }
            },
            shape = MinicashShapes.Hero
        )
    }

    val guard = rememberDiscardGuard(isDirty = concept != item.concept || amountExpression != initialVal || responsible != item.responsible || itemType != item.type || (item.dueDate != null && selectedDateMillis != item.dueDate), onDismiss = onDismiss)
    DiscardChangesDialog(guard)
        GuardedModalBottomSheet(
        guard = guard,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Editar partida",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                val formattedDateLabel = selectedDateMillis?.let { formatTransactionDate(it) } ?: "Sin fecha"
                Surface(
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        showDatePicker = true
                    },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.height(32.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formattedDateLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Selector superior en píldora: [ Gasto | Ingreso | Reembolso ]
            MinicashSegmentedChoice(
                options = BudgetItemType.entries,
                isSelected = { itemType == it },
                onSelect = { itemType = it },
                label = { it.displayName }
            )

            // Importe destacado
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = if (amountExpression.isNotBlank()) amountExpression else "0",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val cents = calculatedAmount.toCents()
                    Text(
                        text = cents.toCurrencyFormat(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            OutlinedTextField(
                value = concept,
                onValueChange = { concept = it },
                label = { Text("Concepto") },
                placeholder = { Text("Nombre de la partida") },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = responsible,
                onValueChange = { responsible = it },
                label = { Text("Responsable") },
                placeholder = { Text("Nombre del responsable") },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Teclado Numérico Embebido con Swipe a Operadores
            MinicashSwipeNumpad(
                amountExpression = amountExpression,
                onExpressionChange = { expr, calcVal ->
                    amountExpression = expr
                    calculatedAmount = calcVal
                }
            )

            // Botón de acción principal (Píldora full width independiente)
            MinicashButton(
                text = "Guardar cambios",
                onClick = {
                    haptics.confirm()
                    if (calculatedAmount > 0.0) {
                        onConfirm(concept.trim(), calculatedAmount, itemType, responsible.trim(), selectedDateMillis)
                    }
                
                },
                enabled = canConfirm,
                modifier = Modifier.fillMaxWidth()
            )

            MinicashOutlinedButton(
                onClick = { showDeleteConfirmDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Eliminar partida", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                MinicashTextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDateMillis = normalizeUtcToLocalMillis(it)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                MinicashTextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            )
        }
    }
}

private fun isSameDay(cal1Millis: Long, cal2Millis: Long): Boolean {
    val c1 = Calendar.getInstance().apply { timeInMillis = cal1Millis }
    val c2 = Calendar.getInstance().apply { timeInMillis = cal2Millis }
    return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
}

@Composable
fun FortnightSelectorRow(
    label: String,
    year: Int,
    month: Int,
    fortnight: Int,
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onFortnightChange: (Int) -> Unit,
    monthNames: List<String>
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Month Dropdown
            var monthExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1.2f)) {
                MinicashOutlinedButton(
                    onClick = { monthExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(monthNames[month - 1], style = MaterialTheme.typography.labelMedium)
                }
                DropdownMenu(
                    expanded = monthExpanded,
                    onDismissRequest = { monthExpanded = false }
                ) {
                    monthNames.forEachIndexed { idx, m ->
                        DropdownMenuItem(
                            text = { Text(m) },
                            onClick = {
                                onMonthChange(idx + 1)
                                monthExpanded = false
                            }
                        )
                    }
                }
            }

            // Year Dropdown
            var yearExpanded by remember { mutableStateOf(false) }
            val yearOptions = remember(year) {
                val cur = Calendar.getInstance().get(Calendar.YEAR)
                ((cur - 2)..(cur + 2)).toList()
            }
            Box(modifier = Modifier.weight(0.8f)) {
                MinicashOutlinedButton(
                    onClick = { yearExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(year.toString(), style = MaterialTheme.typography.labelMedium)
                }
                DropdownMenu(
                    expanded = yearExpanded,
                    onDismissRequest = { yearExpanded = false }
                ) {
                    yearOptions.forEach { y ->
                        DropdownMenuItem(
                            text = { Text(y.toString()) },
                            onClick = {
                                onYearChange(y)
                                yearExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Fortnight Segmented Selector: [ 1ª Quincena | 2ª Quincena ]
        MinicashSegmentedChoice(
            options = listOf(1, 2),
            isSelected = { fortnight == it },
            onSelect = onFortnightChange,
            label = { "${it}ª Quincena" }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CopyFortnightSheet(
    viewModel: BudgetViewModel,
    onDismiss: () -> Unit,
    onConfirmed: (Int) -> Unit
) {
    val haptics = rememberMinicashHaptics()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val uiState by viewModel.uiState.collectAsState()

    val currentKey = uiState.fortnightKey
    val parts = remember(currentKey) { currentKey.split("-") }
    val defaultDestYear = parts.getOrNull(0)?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
    val defaultDestMonth = parts.getOrNull(1)?.toIntOrNull() ?: (Calendar.getInstance().get(Calendar.MONTH) + 1)
    val defaultDestQ = parts.getOrNull(2)?.replace("Q", "")?.toIntOrNull() ?: 1

    var destYear by remember { mutableIntStateOf(defaultDestYear) }
    var destMonth by remember { mutableIntStateOf(defaultDestMonth) }
    var destFortnight by remember { mutableIntStateOf(defaultDestQ) }

    var sourceYear by remember { mutableIntStateOf(if (defaultDestQ == 1) if (defaultDestMonth == 1) defaultDestYear - 1 else defaultDestYear else defaultDestYear) }
    var sourceMonth by remember { mutableIntStateOf(if (defaultDestQ == 1) if (defaultDestMonth == 1) 12 else defaultDestMonth - 1 else defaultDestMonth) }
    var sourceFortnight by remember { mutableIntStateOf(if (defaultDestQ == 1) 2 else 1) }

    val monthNames = remember {
        listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
    }

    val sourceKey = remember(sourceYear, sourceMonth, sourceFortnight) {
        String.format(Locale.US, "%04d-%02d-Q%d", sourceYear, sourceMonth, sourceFortnight)
    }

    val destKey = remember(destYear, destMonth, destFortnight) {
        String.format(Locale.US, "%04d-%02d-Q%d", destYear, destMonth, destFortnight)
    }

    var previewItems by remember { mutableStateOf<List<BudgetItemEntity>>(emptyList()) }
    var isLoadingPreview by remember { mutableStateOf(false) }

    LaunchedEffect(sourceKey) {
        isLoadingPreview = true
        previewItems = viewModel.getItemsForFortnightList(sourceKey)
        isLoadingPreview = false
    }

    val isSameKey = sourceKey == destKey

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Copiar quincena",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Sección 1: Origen (Copiar desde)
            FortnightSelectorRow(
                label = "Origen (Copiar desde)",
                year = sourceYear,
                month = sourceMonth,
                fortnight = sourceFortnight,
                onYearChange = { sourceYear = it },
                onMonthChange = { sourceMonth = it },
                onFortnightChange = { sourceFortnight = it },
                monthNames = monthNames
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Sección 2: Destino (Copiar hacia)
            FortnightSelectorRow(
                label = "Destino (Copiar hacia)",
                year = destYear,
                month = destMonth,
                fortnight = destFortnight,
                onYearChange = { destYear = it },
                onMonthChange = { destMonth = it },
                onFortnightChange = { destFortnight = it },
                monthNames = monthNames
            )

            if (isSameKey) {
                Text(
                    text = "El origen y el destino no pueden ser el mismo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Text(
                text = "Vista previa (${previewItems.size} partidas)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (isLoadingPreview) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterHorizontally)
                )
            } else if (previewItems.isEmpty()) {
                Text(
                    text = "No hay partidas registradas en la quincena origen seleccionada.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    previewItems.take(5).forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = item.concept,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = item.amount.toCurrencyFormat(),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (previewItems.size > 5) {
                        Text(
                            text = "+ ${previewItems.size - 5} partidas más",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            MinicashFilledButton(
                onClick = {
                    haptics.confirm()
                    viewModel.copyFortnight(
                        sourceKey = sourceKey,
                        targetKey = destKey,
                        targetYear = destYear,
                        targetMonth = destMonth
                    ) { count ->
                        onConfirmed(count)
                        onDismiss()
                    }
                },
                enabled = previewItems.isNotEmpty() && !isSameKey,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Copiar ${previewItems.size} partidas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
