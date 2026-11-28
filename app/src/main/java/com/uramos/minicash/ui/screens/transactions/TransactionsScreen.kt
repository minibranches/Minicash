package com.uramos.minicash.ui.screens.transactions

import com.uramos.minicash.ui.utils.AppStrings
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.uramos.minicash.R
import com.uramos.minicash.ui.utils.rememberTabListBottomPadding
import kotlinx.coroutines.flow.first
import kotlin.math.roundToInt
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.layout.layout
import com.uramos.minicash.ui.theme.minicashAnimateItem
import com.uramos.minicash.ui.theme.minicashSpatialSpec
import com.uramos.minicash.ui.theme.minicashFastEffectsSpec
import com.uramos.minicash.ui.components.ExpandingFilterButton
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.layout.size
import androidx.compose.animation.animateContentSize
import com.uramos.minicash.ui.components.LocalBottomBlurBand
import com.uramos.minicash.data.local.entities.TransactionType
import androidx.compose.material.icons.filled.ArrowDropDown
import com.uramos.minicash.ui.components.MinicashFilterChip
import com.uramos.minicash.ui.components.StatPillMoney
import com.uramos.minicash.ui.theme.screenBackground
import com.uramos.minicash.ui.theme.sheetBackground
import com.uramos.minicash.ui.components.underHeaderBlur
import com.uramos.minicash.ui.components.BlurHeaderScaffold
import com.uramos.minicash.ui.utils.FLOATING_NAV_CLEARANCE
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarHost
import androidx.compose.foundation.layout.WindowInsets
import com.uramos.minicash.ui.components.TabTopBarActions
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.TextButton
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.material.icons.filled.History
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
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
    onOpenReports: () -> Unit = {},
    listState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState(),
    /** Falso cuando la pestaña no está a la vista (otra pestaña o una pantalla encima): el buscador suelta el foco. */
    isActive: Boolean = true
) {
    val spacing = MinicashThemeExtensions.spacing
    val state by viewModel.uiState.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val years by viewModel.availableYears.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val selectedTotalAmount by viewModel.selectedTotalAmount.collectAsState()

    val haptics = rememberMinicashHaptics()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.deletedEvents.collect { deleted ->
            val message = AppStrings.plural(R.plurals.home_tx_deleted, deleted.size, deleted.size)
            val result = snackbarHostState.showSnackbar(message = message, actionLabel = AppStrings.get(R.string.common_undo), duration = SnackbarDuration.Long)
            if (result == SnackbarResult.ActionPerformed) viewModel.restoreTransactions(deleted)
        }
    }

    val entranceTracker = rememberEntranceTracker()
    val isRestricted = LocalAnimationRestriction.current

    var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var searchExpanded by rememberSaveable { mutableStateOf(state.searchQuery.isNotEmpty()) }
    val filterCount = activeFilterCount(state.selectedTypeFilter, state.selectedAccountId, state.selectedCategoryIds)

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



    Scaffold(
        containerColor = MaterialTheme.colorScheme.screenBackground,
        contentWindowInsets = WindowInsets(0.dp),
        snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.padding(bottom = FLOATING_NAV_CLEARANCE)) },
    ) { paddingValues ->
        BlurHeaderScaffold(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            listState = listState,
            header = {
                TopAppBar(
                                windowInsets = WindowInsets(0.dp),
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = Color.Transparent
                                ),
                                title = { Text(stringResource(R.string.nav_transactions), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) },
                                actions = { TabTopBarActions(onOpenReports = onOpenReports, onOpenSettings = onOpenSettings) }
                            )
            },
            pinned = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = spacing.screen),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.small)
                ) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = !searchExpanded,
                            modifier = Modifier.align(Alignment.CenterStart),
                            enter = androidx.compose.animation.fadeIn(minicashEffectsSpec()),
                            exit = androidx.compose.animation.fadeOut(minicashFastEffectsSpec())
                        ) {
                            Text(
                                text = periodLabel(state.selectedMonthIndex, state.selectedYear),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = spacing.small)
                            )
                        }
                        RecentSearchBar(
                            query = state.searchQuery,
                            recents = recentSearches,
                            onQueryChange = { viewModel.onSearchQueryChange(it) },
                            onCommit = { viewModel.commitSearch(it) },
                            onClearRecents = { viewModel.clearRecentSearches() },
                            isActive = isActive,
                            isListScrolling = listState.isScrollInProgress,
                            expanded = searchExpanded,
                            onExpandedChange = { searchExpanded = it }
                        )
                    }
                    ExpandingFilterButton(
                        activeCount = filterCount,
                        onClick = {
                            haptics.tick()
                            showFilterSheet = true
                        },
                        onClear = {
                            haptics.tick()
                            viewModel.clearFilters()
                        },
                        contentDescription = stringResource(R.string.tx_filters_title)
                    )
                }
            }
        ) { headerHeight ->
        Box(modifier = Modifier.fillMaxSize()) {
            val categoryMap = remember(categories) { categories.associateBy({ it.id }, { it.name }) }
            val accountMap = remember(accounts) { accounts.associateBy({ it.id }, { it.name }) }
            val groupedTransactions = remember(state.transactions) {
                com.uramos.minicash.ui.utils.groupTransactionsByDate(state.transactions)
            }
            val hasActiveFilters = state.selectedTypeFilter != null || state.selectedAccountId != null || state.selectedCategoryIds.isNotEmpty()

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().underHeaderBlur(headerHeight, bottomBand = LocalBottomBlurBand.current, listState = listState),
                contentPadding = PaddingValues(
                    top = headerHeight + spacing.standard,
                    start = spacing.screen,
                    end = spacing.screen,
                    bottom = rememberTabListBottomPadding()
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
                // 2. Resumen del mes: el balance primero; ingresos y gastos debajo, y al tocarlos filtran por tipo
                item(key = "header_monthly_summary") {
                    val income = state.summary.totalIncome
                    val expense = state.summary.totalExpense
                    val total = income + expense
                    Row(horizontalArrangement = Arrangement.spacedBy(spacing.small)) {
                        StatPillMoney(
                            label = stringResource(R.string.tx_balance),
                            amountCents = state.summary.netBalance,
                            type = if (state.summary.netBalance >= 0) MoneyDisplayType.INCOME else MoneyDisplayType.EXPENSE,
                            compact = true,
                            modifier = Modifier.weight(1f)
                        )
                        StatPillMoney(
                            label = stringResource(R.string.common_income),
                            amountCents = income,
                            type = MoneyDisplayType.INCOME,
                            progress = if (total > 0) income.toFloat() / total else null,
                            selected = state.selectedTypeFilter == TransactionType.INCOME,
                            onClick = {
                                haptics.tick()
                                viewModel.onTypeFilterSelect(TransactionType.INCOME)
                            },
                            compact = true,
                            modifier = Modifier.weight(1f)
                        )
                        StatPillMoney(
                            label = stringResource(R.string.common_expenses),
                            amountCents = expense,
                            type = MoneyDisplayType.EXPENSE,
                            progress = if (total > 0) expense.toFloat() / total else null,
                            selected = state.selectedTypeFilter == TransactionType.EXPENSE,
                            onClick = {
                                haptics.tick()
                                viewModel.onTypeFilterSelect(TransactionType.EXPENSE)
                            },
                            compact = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 4. Listado de transacciones o Empty State
                if (state.transactions.isEmpty()) {
                    item(key = "empty_state") {
                        MinicashEmptyState(
                            icon = Icons.AutoMirrored.Filled.ReceiptLong,
                            title = stringResource(R.string.tx_empty_title),
                            subtitle = if (state.searchQuery.isNotBlank() || hasActiveFilters) {
                                stringResource(R.string.tx_empty_filtered)
                            } else {
                                stringResource(R.string.tx_empty_month)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = spacing.large)
                        )
                    }
                } else {
                    groupedTransactions.forEachIndexed { groupIndex, group ->
                        item(key = "group_${group.dateKey}") {
                            Box(modifier = minicashAnimateItem().listEntrance(entranceTracker, group.dateKey, groupIndex)) {
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
                                    onTransactionDelete = { viewModel.deleteTransaction(it) }
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
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.common_cancel))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = pluralStringResource(R.plurals.home_selected, selectedIds.size, selectedIds.size),
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
                        MinicashIconButton(onClick = {
                            haptics.heavyOrDelete()
                            viewModel.deleteSelectedTransactions()
                        }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.home_delete_selected),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
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

/**
 * Buscador de M3: el campo es el `InputField` de `SearchBar`; al enfocarlo con el campo vacío, un panel flotante
 * debajo ofrece las búsquedas recientes para repetirlas con un toque. Los resultados son la propia lista de
 * movimientos, que se filtra mientras se escribe. El panel flota (no empuja nada) y no le quita el foco al campo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecentSearchBar(
    query: String,
    recents: List<String>,
    onQueryChange: (String) -> Unit,
    onCommit: (String) -> Unit,
    onClearRecents: () -> Unit,
    isActive: Boolean,
    isListScrolling: Boolean,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var focused by remember { mutableStateOf(false) }
    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    val expandFraction by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = minicashSpatialSpec(),
        label = "search_expand"
    )
    val closeSearch = {
        onQueryChange("")
        onExpandedChange(false)
    }
    val showRecents = isActive && focused && query.isEmpty() && recents.isNotEmpty()
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    // Las pestañas contiguas siguen compuestas: al irse de esta pestaña el panel (otra ventana) debe cerrarse
    // Deslizar la lista (hacia arriba o abajo) también cierra el panel y el teclado
    androidx.compose.runtime.LaunchedEffect(isActive, isListScrolling) {
        if (!isActive || isListScrolling) {
            focusManager.clearFocus()
            focused = false
        }
    }
    val haptics = rememberMinicashHaptics()
    androidx.compose.runtime.LaunchedEffect(expanded) {
        if (expanded) {
            // El campo aparece cuando el contenedor ya tiene ancho; hasta entonces no hay a quién darle el foco
            androidx.compose.runtime.snapshotFlow { expandFraction }.first { it > SEARCH_FIELD_VISIBLE_FRACTION }
            kotlinx.coroutines.delay(30)
            runCatching { focusRequester.requestFocus() }
        } else {
            focused = false
        }
    }
    androidx.activity.compose.BackHandler(enabled = expanded) {
        focusManager.clearFocus()
        closeSearch()
    }
    val density = androidx.compose.ui.platform.LocalDensity.current
    var fieldWidthPx by remember { mutableStateOf(0) }
    val gapPx = with(density) { 6.dp.roundToPx() }

    Box(modifier = modifier.onSizeChanged { fieldWidthPx = it.width }) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.layout { measurable, constraints ->
                // El campo se mide siempre al ancho actual de la animación: si se midiera a todo el ancho y el
                // contenedor lo recortara, Compose intentaría traerlo a la vista y desplazaría el pager de pestañas.
                val minWidth = 48.dp.roundToPx()
                val maxWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else minWidth
                val width = (minWidth + (maxWidth - minWidth) * expandFraction.coerceIn(0f, 1f)).roundToInt()
                val placeable = measurable.measure(
                    Constraints(minWidth = width, maxWidth = width, minHeight = 48.dp.roundToPx(), maxHeight = constraints.maxHeight)
                )
                layout(placeable.width, placeable.height) { placeable.place(0, 0) }
            }
        ) {
            if (expandFraction > SEARCH_FIELD_VISIBLE_FRACTION) {
                SearchBarDefaults.InputField(
                    query = query,
                    onQueryChange = onQueryChange,
                    onSearch = {
                        onCommit(it)
                        focusManager.clearFocus()
                    },
                    expanded = false,
                    onExpandedChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { focused = it.isFocused },
                    placeholder = { Text(stringResource(R.string.tx_search_hint), style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = {
                        MinicashIconButton(onClick = {
                            focusManager.clearFocus()
                            closeSearch()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.tx_search_close))
                        }
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            MinicashIconButton(onClick = { onQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.tx_clear_search))
                            }
                        }
                    },
                    colors = SearchBarDefaults.inputFieldColors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable(role = androidx.compose.ui.semantics.Role.Button) {
                            haptics.tick()
                            onExpandedChange(true)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Search, contentDescription = stringResource(R.string.tx_search_cd))
                }
            }
        }

        if (showRecents) {
            androidx.compose.ui.window.Popup(
                popupPositionProvider = object : androidx.compose.ui.window.PopupPositionProvider {
                    override fun calculatePosition(
                        anchorBounds: androidx.compose.ui.unit.IntRect,
                        windowSize: androidx.compose.ui.unit.IntSize,
                        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
                        popupContentSize: androidx.compose.ui.unit.IntSize
                    ) = androidx.compose.ui.unit.IntOffset(anchorBounds.left, anchorBounds.bottom + gapPx)
                },
                properties = androidx.compose.ui.window.PopupProperties(focusable = false)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shadowElevation = 6.dp,
                    modifier = Modifier.width(with(density) { fieldWidthPx.toDp() })
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        recents.forEach { recent ->
                            ListItem(
                                headlineContent = { Text(recent, maxLines = 1) },
                                leadingContent = { Icon(Icons.Default.History, contentDescription = null) },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                modifier = Modifier.clickable {
                                    haptics.tick()
                                    onQueryChange(recent)
                                    onCommit(recent)
                                    focusManager.clearFocus()
                                }
                            )
                        }
                        TextButton(
                            onClick = onClearRecents,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(stringResource(R.string.tx_clear_history))
                        }
                    }
                }
            }
        }
    }
}

/** Desde qué avance de la animación se muestra el campo de búsqueda; antes es solo el ícono (el campo no cabe). */
private const val SEARCH_FIELD_VISIBLE_FRACTION = 0.4f

/** Nombre completo del mes en el idioma en uso, con mayúscula inicial. */
private fun fullMonth(index: Int): String =
    java.time.Month.of(index.coerceIn(0, 11) + 1)
        .getDisplayName(java.time.format.TextStyle.FULL, com.uramos.minicash.ui.utils.appLocale())
        .replaceFirstChar { it.uppercase() }

/** Periodo que se está viendo, para la fila fija del encabezado («Noviembre 2026»). */
private fun periodLabel(monthIndex: Int, year: Int): String = "${fullMonth(monthIndex)} $year"
