package com.uramos.minicash.ui.screens.transactions

import com.uramos.minicash.ui.components.SettingsGearButton
import com.uramos.minicash.ui.components.fadingTopEdge
import com.uramos.minicash.ui.components.MinicashAlertDialog
import com.uramos.minicash.ui.components.MinicashTextButton
import com.uramos.minicash.ui.components.MinicashTonalIconButton
import com.uramos.minicash.ui.components.MinicashIconButton
import com.uramos.minicash.ui.components.MinicashFilledButton
import com.uramos.minicash.ui.components.MinicashPeriodSelector
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import com.uramos.minicash.ui.components.listEntrance
import com.uramos.minicash.ui.components.rememberEntranceTracker
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.graphics.Color
import com.uramos.minicash.ui.theme.minicashEffectsSpec
import androidx.activity.compose.BackHandler
import com.uramos.minicash.ui.transactions.EditTransactionSheet
import com.uramos.minicash.ui.utils.toCurrencyFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.uramos.minicash.ui.utils.rememberMinicashHaptics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uramos.minicash.data.local.entities.TransactionEntity
import com.uramos.minicash.ui.components.CardTonalLevel
import com.uramos.minicash.ui.components.MinicashCard
import com.uramos.minicash.ui.components.MinicashEmptyState
import com.uramos.minicash.ui.components.MoneyDisplayType
import com.uramos.minicash.ui.components.MoneyTextView
import com.uramos.minicash.ui.screens.home.GroupedTransactionCard
import com.uramos.minicash.ui.theme.LocalAnimationRestriction
import com.uramos.minicash.ui.theme.MinicashShapes
import com.uramos.minicash.ui.theme.MinicashThemeExtensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: TransactionsViewModel,
    onOpenSettings: () -> Unit = {},
    listState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState()
) {
    val spacing = MinicashThemeExtensions.spacing
    val state by viewModel.uiState.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val years by viewModel.availableYears.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val selectedTotalAmount by viewModel.selectedTotalAmount.collectAsState()

    val haptic = LocalHapticFeedback.current
    val haptics = rememberMinicashHaptics()

    val entranceTracker = rememberEntranceTracker()
    val isRestricted = LocalAnimationRestriction.current

    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showBatchDeleteDialog by remember { mutableStateOf(false) }

    if (selectedIds.isNotEmpty()) {
        BackHandler {
            viewModel.clearSelection()
        }
    }

    if (transactionToEdit != null) {
        EditTransactionSheet(
            transaction = transactionToEdit!!,
            accounts = accounts,
            categories = categories,
            onDismiss = { transactionToEdit = null },
            onSave = { original, updated ->
                viewModel.updateTransaction(original, updated)
                transactionToEdit = null
            },
            onCreateCategory = viewModel::createCategory,
            onDeleteTransaction = { tx ->
                viewModel.deleteTransaction(tx)
                transactionToEdit = null
            }
        )
    }

    if (transactionToDelete != null) {
        val tx = transactionToDelete!!
        MinicashAlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = {
                Text(
                    text = "¿Eliminar movimiento?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Esta acción no se puede deshacer. Se ajustará automáticamente el saldo de la cuenta correspondiente.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                MinicashTextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                        viewModel.deleteTransaction(tx)
                        transactionToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                MinicashTextButton(
                    onClick = { transactionToDelete = null }
                ) {
                    Text("Cancelar")
                }
            },
            shape = MinicashShapes.Hero,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }

    if (showBatchDeleteDialog) {
        MinicashAlertDialog(
            onDismissRequest = { showBatchDeleteDialog = false },
            title = { Text("¿Eliminar ${selectedIds.size} movimientos?", fontWeight = FontWeight.Bold) },
            text = { Text("Esta acción eliminará las transacciones seleccionadas y reajustará automáticamente los saldos correspondientes de las cuentas. ¿Proceder?") },
            confirmButton = {
                MinicashFilledButton(
                    onClick = {
                        haptics.heavyOrDelete()
                        viewModel.deleteSelectedTransactions()
                        showBatchDeleteDialog = false
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
                MinicashTextButton(onClick = { showBatchDeleteDialog = false }) { Text("Cancelar") }
            },
            shape = MinicashShapes.Hero
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                ),
                title = { Text("Movimientos", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) },
                actions = { SettingsGearButton(onClick = onOpenSettings) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val categoryMap = remember(categories) { categories.associateBy({ it.id }, { it.name }) }
            val accountMap = remember(accounts) { accounts.associateBy({ it.id }, { it.name }) }
            val groupedTransactions = remember(state.transactions) {
                com.uramos.minicash.ui.utils.groupTransactionsByDate(state.transactions)
            }
            val hasActiveFilters = state.selectedTypeFilter != null || state.selectedAccountId != null || state.selectedCategoryIds.isNotEmpty()

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().fadingTopEdge(listState),
                contentPadding = PaddingValues(
                    top = spacing.small,
                    start = spacing.standard,
                    end = spacing.standard,
                    bottom = 120.dp
                ),
                verticalArrangement = Arrangement.spacedBy(spacing.medium)
            ) {
                // 1. Selector de periodo: año (menú) y meses anterior, actual y siguiente
                item(key = "header_period_selector") {
                    MinicashPeriodSelector(
                        year = state.selectedYear,
                        monthIndex = state.selectedMonthIndex,
                        years = years,
                        onYearSelect = { year ->
                            haptics.tick()
                            viewModel.selectYear(year)
                        },
                        onShiftMonths = { delta ->
                            haptics.tick()
                            viewModel.shiftMonths(delta)
                        }
                    )
                }
                // 2. Tarjeta de Balance Mensual
                item(key = "header_monthly_summary") {
                    MinicashCard(
                        level = CardTonalLevel.LOW,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.standard),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Ingresos",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                MoneyTextView(
                                    amountCents = state.summary.totalIncome,
                                    type = MoneyDisplayType.INCOME,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Column {
                                Text(
                                    text = "Gastos",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                MoneyTextView(
                                    amountCents = state.summary.totalExpense,
                                    type = MoneyDisplayType.EXPENSE,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Balance",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                MoneyTextView(
                                    amountCents = state.summary.netBalance,
                                    type = if (state.summary.netBalance >= 0) MoneyDisplayType.INCOME else MoneyDisplayType.EXPENSE,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }

                // 3. Barra de Búsqueda con Botón de Filtro
                item(key = "header_filters") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacing.small)
                    ) {
                        TextField(
                            value = state.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Buscar...", style = MaterialTheme.typography.bodyMedium) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (state.searchQuery.isNotEmpty()) {
                                    MinicashIconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                        Icon(Icons.Default.Close, contentDescription = "Limpiar búsqueda")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = CircleShape,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            )
                        )

                        Box {
                            MinicashTonalIconButton(
                                onClick = {
                                    haptics.tick()
                                    showFilterSheet = true
                                },
                                modifier = Modifier.size(48.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = "Filtros de movimientos"
                                )
                            }

                            if (hasActiveFilters) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .align(Alignment.TopEnd)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                )
                            }
                        }
                    }
                }

                // 4. Listado de transacciones o Empty State
                if (state.transactions.isEmpty()) {
                    item(key = "empty_state") {
                        MinicashEmptyState(
                            icon = Icons.AutoMirrored.Filled.ReceiptLong,
                            title = "Sin movimientos registrados",
                            subtitle = if (state.searchQuery.isNotBlank() || hasActiveFilters) {
                                "No se encontraron transacciones con los filtros seleccionados."
                            } else {
                                "No hay transacciones registradas en este mes."
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = spacing.large)
                        )
                    }
                } else {
                    groupedTransactions.forEachIndexed { groupIndex, group ->
                        item(key = "group_${group.dateKey}") {
                            Box(modifier = Modifier.animateItem().listEntrance(entranceTracker, group.dateKey, groupIndex)) {
                                GroupedTransactionCard(
                                    group = group,
                                    categoryMap = categoryMap,
                                    accountMap = accountMap,
                                    selectedIds = selectedIds,
                                    onToggleSelection = { id ->
                                        haptics.tick()
                                        viewModel.toggleSelection(id)
                                    },
                                    onTransactionClick = { transactionToEdit = it },
                                    onTransactionDelete = { transactionToDelete = it }
                                )
                            }
                        }
                    }
                }
            }

            // CÁPSULA FLOTANTE DE SELECCIÓN Y SUMA EN VIVO
            AnimatedVisibility(
                visible = selectedIds.isNotEmpty(),
                enter = if (isRestricted) fadeIn(animationSpec = snap()) else slideInVertically { it } + fadeIn(),
                exit = if (isRestricted) fadeOut(animationSpec = snap()) else slideOutVertically { it } + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 96.dp, start = 16.dp, end = 16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    tonalElevation = 8.dp,
                    shadowElevation = 6.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MinicashIconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancelar")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "${selectedIds.size} seleccionados",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val (displayColor, displaySign) = when {
                                selectedTotalAmount > 0 -> MinicashThemeExtensions.financialColors.income to "+"
                                selectedTotalAmount < 0 -> MinicashThemeExtensions.financialColors.expense to "-"
                                else -> MaterialTheme.colorScheme.onSurfaceVariant to ""
                            }
                            val formattedAmount = "$displaySign${kotlin.math.abs(selectedTotalAmount).toCurrencyFormat()}"
                            Text(
                                text = formattedAmount,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = displayColor
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        MinicashIconButton(onClick = { showBatchDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar seleccionados",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        TransactionsFilterSheet(
            selectedTypeFilter = state.selectedTypeFilter,
            accounts = accounts,
            selectedAccountId = state.selectedAccountId,
            categories = categories,
            selectedCategoryIds = state.selectedCategoryIds,
            onTypeFilterSelect = { type -> viewModel.onTypeFilterSelect(type) },
            onSelectAccountFilter = { accId -> viewModel.onSelectAccountFilter(accId) },
            onToggleCategory = { catId -> viewModel.toggleCategoryFilter(catId) },
            onClearCategories = { viewModel.clearCategoryFilter() },
            onDismiss = { showFilterSheet = false }
        )
    }
}
