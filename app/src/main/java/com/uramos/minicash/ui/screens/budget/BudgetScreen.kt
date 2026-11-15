package com.uramos.minicash.ui.screens.budget

import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarDuration
import com.uramos.minicash.ui.utils.normalizeLocalToUtcMillis
import com.uramos.minicash.ui.components.MinicashLoading
import androidx.compose.material3.ripple
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.compositeOver
import com.uramos.minicash.ui.components.MinicashBackButton
import androidx.compose.material3.Checkbox
import com.uramos.minicash.ui.components.fadingTopEdge
import com.uramos.minicash.ui.theme.minicashSpatialSpec
import com.uramos.minicash.ui.theme.minicashFastSpatialSpec
import androidx.compose.material3.MaterialShapes
import androidx.graphics.shapes.Morph
import com.uramos.minicash.ui.components.MorphShape
import com.uramos.minicash.ui.theme.minicashEffectsSpec
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState
import com.uramos.minicash.ui.utils.toCompactCurrency
import com.uramos.minicash.ui.components.WavyProgressIndicator
import sh.calvin.reorderable.rememberReorderableLazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.GridCells
import com.uramos.minicash.ui.components.GuardedModalBottomSheet
import com.uramos.minicash.ui.components.MinicashAlertDialog
import com.uramos.minicash.ui.components.MinicashSegmentedChoice
import com.uramos.minicash.ui.components.MinicashTextButton
import com.uramos.minicash.ui.components.MinicashFilledButton
import com.uramos.minicash.ui.components.MinicashIconButton
import com.uramos.minicash.ui.components.MinicashOutlinedButton
import com.uramos.minicash.ui.components.MinicashTonalIconButton
import androidx.compose.material.icons.filled.DragHandle
import sh.calvin.reorderable.ReorderableItem
import com.uramos.minicash.ui.components.WithoutMinTouchTarget
import com.uramos.minicash.ui.components.listEntrance
import com.uramos.minicash.ui.components.rememberEntranceTracker
import com.uramos.minicash.ui.components.DiscardChangesDialog
import com.uramos.minicash.ui.components.rememberDiscardGuard
import com.uramos.minicash.ui.utils.toCents
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
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
    listState: LazyGridState = rememberLazyGridState(),
    showAddDialogExternal: Boolean = false,
    onAddDialogExternalHandled: () -> Unit = {},
    onOpenAddTransactionForBudgetItem: ((BudgetItemEntity) -> Unit)? = null,
    onBack: (() -> Unit)? = null
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
    var itemToEdit by remember { mutableStateOf<BudgetItemEntity?>(null) }

    // Orden local mientras se arrastra; se guarda en la base al soltar
    var orderedItems by remember(state.items) { mutableStateOf(state.items) }
    val reorderState = rememberReorderableLazyGridState(listState) { from, to ->
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
                windowInsets = WindowInsets(0.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                ),
                navigationIcon = { onBack?.let { MinicashBackButton(onClick = it) } },
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
            // Una sola fila: mes y quincena
            BudgetPeriodBar(
                monthLabel = state.currentMonthLabel,
                fortnight = state.selectedFortnight,
                onPreviousMonth = {
                    haptics.tick()
                    viewModel.previousMonth()
                },
                onNextMonth = {
                    haptics.tick()
                    viewModel.nextMonth()
                },
                onSelectFortnight = {
                    haptics.tick()
                    viewModel.selectFortnight(it)
                },
                modifier = Modifier.padding(horizontal = spacing.standard, vertical = spacing.extraSmall)
            )

            // El resumen va dentro de la cuadrícula para que se desplace junto con las partidas
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = listState,
                modifier = Modifier.fillMaxSize().fadingTopEdge(listState),
                contentPadding = PaddingValues(
                    top = spacing.small,
                    start = spacing.standard,
                    end = spacing.standard,
                    bottom = 120.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item(key = "budget_summary", span = { GridItemSpan(maxLineSpan) }) {
                    BudgetSummaryCard(summary = state.summary)
                }

                if (orderedItems.isEmpty()) {
                    item(key = "budget_empty", span = { GridItemSpan(maxLineSpan) }) {
                        MinicashEmptyState(
                            icon = Icons.Default.Savings,
                            title = "Sin partidas presupuestadas",
                            subtitle = "No has agregado ingresos ni gastos para esta quincena.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = spacing.large)
                        )
                    }
                } else {
                    itemsIndexed(orderedItems, key = { _, item -> item.id }) { index, item ->
                        ReorderableItem(reorderState, key = item.id) { isDragging ->
                            BudgetItemCard(
                                item = item,
                                isDragging = isDragging,
                                onClick = { itemToEdit = item },
                                onToggleExecuted = { itemToPay = item },
                                modifier = Modifier
                                    .longPressDraggableHandle(
                                        onDragStarted = { haptics.click() },
                                        onDragStopped = { viewModel.saveItemOrder(orderedItems.map { it.id }) }
                                    )
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
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = "Partida eliminada",
                        actionLabel = "Deshacer",
                        duration = SnackbarDuration.Long
                    )
                    if (result == SnackbarResult.ActionPerformed) viewModel.restoreItem(target)
                }
                itemToEdit = null
            }
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

/** Selector de periodo en una sola fila: mes con flechas y las dos quincenas. */
@Composable
internal fun BudgetPeriodBar(
    monthLabel: String,
    fortnight: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectFortnight: (Int) -> Unit,
    modifier: Modifier = Modifier,
    leadingLabel: String? = null
) {
    val isRestricted = LocalAnimationRestriction.current
    val motionScheme = MaterialTheme.motionScheme

    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier.fillMaxWidth()
    ) {
        WithoutMinTouchTarget {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (leadingLabel != null) {
                    Text(
                        text = leadingLabel,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                    )
                }
                MinicashIconButton(onClick = onPreviousMonth, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mes anterior")
                }
                AnimatedContent(
                    targetState = monthLabel,
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
                    label = "budget_month_transition",
                    modifier = Modifier.weight(1f)
                ) { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                MinicashIconButton(onClick = onNextMonth, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Mes siguiente")
                }

                listOf(1, 2).forEach { q ->
                    val selected = q == fortnight
                    val pillColor by animateColorAsState(
                        targetValue = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                        animationSpec = minicashEffectsSpec(),
                        label = "fortnight_pill"
                    )
                    Surface(
                        onClick = { onSelectFortnight(q) },
                        shape = CircleShape,
                        color = pillColor,
                        modifier = Modifier
                            .height(40.dp)
                            .widthIn(min = 48.dp)
                            .semantics { contentDescription = "Quincena $q" }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                            Text(
                                text = "${q}ª",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Resumen de la quincena en tres líneas: disponible, cifras ejecutadas y avance ondulado. */
@Composable
private fun BudgetSummaryCard(summary: BudgetSummary) {
    val financialColors = MinicashThemeExtensions.financialColors
    val progress = if (summary.expectedExpense > 0) {
        (summary.paidExpense.toFloat() / summary.expectedExpense.toFloat()).coerceIn(0f, 1f)
    } else 0f

    MinicashCard(
        level = CardTonalLevel.LOW,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Disponible",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MoneyTextView(
                        amountCents = summary.actualFreeAmount,
                        type = if (summary.actualFreeAmount >= 0) MoneyDisplayType.INCOME else MoneyDisplayType.EXPENSE,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
                    )
                }
                Text(
                    text = "${(progress * 100).toInt()}% pagado",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryFigure("Ingresos", "+" + summary.executedIncome.toCompactCurrency(), financialColors.income)
                SummaryFigure("Reembolsos", "+" + summary.executedReimbursement.toCompactCurrency(), financialColors.income)
                SummaryFigure("Gastos", "-" + summary.paidExpense.toCompactCurrency(), financialColors.expense)
            }

            WavyProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
            )
        }
    }
}

@Composable
private fun SummaryFigure(label: String, value: String, color: Color) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"),
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/**
 * Partida como tarjeta de la cuadrícula. Tocar abre la edición, el círculo marca ejecutado y
 * mantener presionado la arrastra para reordenar.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BudgetItemCard(
    item: BudgetItemEntity,
    isDragging: Boolean,
    onClick: () -> Unit,
    onToggleExecuted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    // Pendiente: tarjeta más clara y elevada. Pagada: tinte del color primario y más recesiva; además lleva la palomita
    val containerColor = if (item.isPaid) {
        colorScheme.primaryContainer.copy(alpha = 0.30f).compositeOver(colorScheme.surfaceContainerLow)
    } else {
        colorScheme.surfaceContainerHigh
    }
    val displayType = when (item.type) {
        BudgetItemType.EXPENSE -> MoneyDisplayType.EXPENSE
        BudgetItemType.INCOME, BudgetItemType.REIMBURSEMENT -> MoneyDisplayType.INCOME
    }
    val scale by animateFloatAsState(
        targetValue = if (isDragging) 1.04f else 1f,
        animationSpec = minicashSpatialSpec(),
        label = "budget_card_drag"
    )
    val dateFormat = remember { SimpleDateFormat("dd/MM", Locale.forLanguageTag("es-MX")) }
    val subtitle = buildString {
        append(item.type.displayName)
        item.dueDate?.let { append(" • ").append(dateFormat.format(Date(it))) }
    }

    MinicashCard(
        level = CardTonalLevel.LOW,
        shape = RoundedCornerShape(20.dp),
        containerColor = containerColor,
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(116.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.concept,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isPaid) colorScheme.onSurfaceVariant else colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        // Sin recorte: el círculo visible va en la esquina y un clip circular de 48 dp lo cortaría;
                        // la onda de toque no acotada se dibuja alrededor sin tocar su forma
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = false, radius = 24.dp),
                            onClick = onToggleExecuted
                        ),
                    contentAlignment = Alignment.TopEnd
                ) {
                    // Al marcarla pagada el círculo se transforma en una galleta y la palomita "salta"
                    val checkMorph = remember { Morph(MaterialShapes.Circle, MaterialShapes.Cookie6Sided) }
                    val morphProgress by animateFloatAsState(
                        targetValue = if (item.isPaid) 1f else 0f,
                        animationSpec = minicashFastSpatialSpec(),
                        label = "check_morph"
                    )
                    val checkPop by animateFloatAsState(
                        targetValue = if (item.isPaid) 1f else 0.85f,
                        animationSpec = minicashFastSpatialSpec(),
                        label = "check_pop"
                    )
                    val checkColor by animateColorAsState(
                        targetValue = if (item.isPaid) colorScheme.primary else colorScheme.surfaceContainerHighest,
                        animationSpec = minicashEffectsSpec(),
                        label = "check_color"
                    )
                    Surface(
                        shape = MorphShape(checkMorph, morphProgress.coerceIn(0f, 1f)),
                        color = checkColor,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = if (item.isPaid) "Marcar pendiente" else "Marcar ejecutado",
                                tint = if (item.isPaid) colorScheme.onPrimary else colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(18.dp)
                                    .graphicsLayer {
                                        scaleX = checkPop
                                        scaleY = checkPop
                                    }
                            )
                        }
                    }
                }
            }
            MoneyTextView(
                amountCents = item.amount,
                type = displayType,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontFeatureSettings = "tnum"),
                modifier = Modifier.alpha(if (item.isPaid) 0.7f else 1f)
            )
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
        title = { Text("¿Reactivar partida?", fontWeight = FontWeight.SemiBold) },
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
        title = { Text("Confirmar ${item.concept}", fontWeight = FontWeight.SemiBold) },
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
                        text = "Crear movimiento",
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
                    fontWeight = FontWeight.Bold
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
                        fontWeight = FontWeight.ExtraBold,
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
                shape = MinicashShapes.Input,
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
                shape = MinicashShapes.Input,
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
            initialSelectedDateMillis = normalizeLocalToUtcMillis(selectedDateMillis ?: System.currentTimeMillis())
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
                    fontWeight = FontWeight.Bold
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
                        fontWeight = FontWeight.ExtraBold,
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
                shape = MinicashShapes.Input,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = responsible,
                onValueChange = { responsible = it },
                label = { Text("Responsable") },
                placeholder = { Text("Nombre del responsable") },
                singleLine = true,
                shape = MinicashShapes.Input,
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
            initialSelectedDateMillis = normalizeLocalToUtcMillis(selectedDateMillis ?: System.currentTimeMillis())
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

    // Origen sugerido: la quincena inmediata anterior a la de destino
    val (suggestedYear, suggestedMonth) = if (defaultDestQ == 1) shiftYearMonth(defaultDestYear, defaultDestMonth, -1) else defaultDestYear to defaultDestMonth
    var sourceYear by remember { mutableIntStateOf(suggestedYear) }
    var sourceMonth by remember { mutableIntStateOf(suggestedMonth) }
    var sourceFortnight by remember { mutableIntStateOf(if (defaultDestQ == 1) 2 else 1) }

    val monthNames = remember {
        listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
    }
    fun label(year: Int, month: Int) = "${monthNames[month - 1].take(3)} $year"

    val sourceKey = remember(sourceYear, sourceMonth, sourceFortnight) {
        String.format(Locale.US, "%04d-%02d-Q%d", sourceYear, sourceMonth, sourceFortnight)
    }
    val destKey = remember(destYear, destMonth, destFortnight) {
        String.format(Locale.US, "%04d-%02d-Q%d", destYear, destMonth, destFortnight)
    }

    var sourceItems by remember { mutableStateOf<List<BudgetItemEntity>>(emptyList()) }
    var duplicateIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var selectedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var isLoading by remember { mutableStateOf(false) }
    val isSameKey = sourceKey == destKey

    // Al cambiar origen o destino se recalcula qué partidas ya existen; las repetidas quedan desmarcadas
    LaunchedEffect(sourceKey, destKey) {
        isLoading = true
        val source = viewModel.getItemsForFortnightList(sourceKey)
        val target = if (isSameKey) emptyList() else viewModel.getItemsForFortnightList(destKey)
        val duplicates = source.filter { isDuplicateInTarget(it, target) }.map { it.id }.toSet()
        sourceItems = source
        duplicateIds = duplicates
        selectedIds = source.map { it.id }.toSet() - duplicates
        isLoading = false
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Copiar quincena",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            BudgetPeriodBar(
                leadingLabel = "Desde",
                monthLabel = label(sourceYear, sourceMonth),
                fortnight = sourceFortnight,
                onPreviousMonth = { shiftYearMonth(sourceYear, sourceMonth, -1).let { sourceYear = it.first; sourceMonth = it.second } },
                onNextMonth = { shiftYearMonth(sourceYear, sourceMonth, 1).let { sourceYear = it.first; sourceMonth = it.second } },
                onSelectFortnight = { sourceFortnight = it }
            )
            BudgetPeriodBar(
                leadingLabel = "Hacia",
                monthLabel = label(destYear, destMonth),
                fortnight = destFortnight,
                onPreviousMonth = { shiftYearMonth(destYear, destMonth, -1).let { destYear = it.first; destMonth = it.second } },
                onNextMonth = { shiftYearMonth(destYear, destMonth, 1).let { destYear = it.first; destMonth = it.second } },
                onSelectFortnight = { destFortnight = it }
            )

            if (isSameKey) {
                Text(
                    text = "El origen y el destino no pueden ser el mismo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Partidas (${selectedIds.size} de ${sourceItems.size})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (sourceItems.isNotEmpty()) {
                    val allSelected = selectedIds.size == sourceItems.size
                    MinicashTextButton(
                        onClick = { selectedIds = if (allSelected) emptySet() else sourceItems.map { it.id }.toSet() }
                    ) {
                        Text(if (allSelected) "Ninguna" else "Todas", fontWeight = FontWeight.Bold)
                    }
                }
            }

            when {
                isLoading -> MinicashLoading(modifier = Modifier.size(40.dp).align(Alignment.CenterHorizontally))
                sourceItems.isEmpty() -> Text(
                    text = "No hay partidas registradas en la quincena origen seleccionada.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(sourceItems, key = { it.id }) { item ->
                        val checked = item.id in selectedIds
                        Surface(
                            onClick = {
                                haptics.tick()
                                selectedIds = if (checked) selectedIds - item.id else selectedIds + item.id
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = if (checked) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Checkbox(checked = checked, onCheckedChange = null)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.concept,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = item.type.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (item.id in duplicateIds) {
                                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.tertiaryContainer) {
                                        Text(
                                            text = "Ya existe",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = item.amount.toCurrencyFormat(),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            MinicashFilledButton(
                onClick = {
                    haptics.confirm()
                    viewModel.copyFortnight(
                        sourceKey = sourceKey,
                        targetKey = destKey,
                        targetYear = destYear,
                        targetMonth = destMonth,
                        itemIds = selectedIds
                    ) { count ->
                        onConfirmed(count)
                        onDismiss()
                    }
                },
                enabled = selectedIds.isNotEmpty() && !isSameKey,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Copiar ${selectedIds.size} partidas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
