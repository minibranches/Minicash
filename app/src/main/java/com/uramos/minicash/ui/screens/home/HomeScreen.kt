package com.uramos.minicash.ui.screens.home

import androidx.compose.runtime.rememberUpdatedState
import com.uramos.minicash.ui.components.bounceOnPress
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.uramos.minicash.ui.components.MinicashPeriodSelector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.runtime.key
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.SwipeToDismissBox
import com.uramos.minicash.ui.utils.rememberTabListBottomPadding
import com.uramos.minicash.ui.theme.minicashAnimateItem
import com.uramos.minicash.ui.components.LocalBottomBlurBand
import com.uramos.minicash.ui.theme.screenBackground
import com.uramos.minicash.ui.theme.cardSurface
import com.uramos.minicash.ui.theme.sheetBackground
import com.uramos.minicash.ui.components.underHeaderBlur
import com.uramos.minicash.ui.components.BlurHeaderScaffold
import com.uramos.minicash.ui.utils.FLOATING_NAV_CLEARANCE
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarHost
import com.uramos.minicash.ui.components.MinicashLoading
import androidx.compose.foundation.layout.WindowInsets
import com.uramos.minicash.ui.components.TabTopBarActions
import com.uramos.minicash.ui.components.MinicashAlertDialog
import com.uramos.minicash.ui.components.MinicashTextButton
import com.uramos.minicash.ui.components.MinicashFilledButton
import com.uramos.minicash.ui.components.MinicashIconButton
import com.uramos.minicash.ui.components.listEntrance
import com.uramos.minicash.ui.components.rememberEntranceTracker
import java.time.YearMonth
import androidx.compose.animation.animateColorAsState
import com.uramos.minicash.ui.theme.minicashEffectsSpec
import androidx.activity.compose.BackHandler
import com.uramos.minicash.ui.utils.asIconTint
import com.uramos.minicash.ui.transactions.EditTransactionSheet
import com.uramos.minicash.ui.utils.toCurrencyFormat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import com.uramos.minicash.ui.theme.minicashSpatialSpec
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import com.uramos.minicash.ui.components.TransactionNoteDialog
import com.uramos.minicash.ui.utils.rememberMinicashHaptics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.uramos.minicash.data.local.entities.AccountEntity
import com.uramos.minicash.data.local.entities.TransactionEntity
import com.uramos.minicash.data.local.entities.TransactionType
import com.uramos.minicash.ui.components.CardTonalLevel
import com.uramos.minicash.ui.components.MinicashCard
import com.uramos.minicash.ui.components.MinicashSectionTitle
import com.uramos.minicash.ui.components.MinicashEmptyState
import com.uramos.minicash.ui.components.MoneyDisplayType
import com.uramos.minicash.ui.components.MoneyTextView
import com.uramos.minicash.ui.components.rememberSegmentedShape
import com.uramos.minicash.ui.transactions.EditTransactionSheet
import com.uramos.minicash.ui.theme.LocalAnimationRestriction
import com.uramos.minicash.ui.theme.MinicashShapes
import com.uramos.minicash.ui.theme.MinicashThemeExtensions
import com.uramos.minicash.ui.utils.MinicashColorUtils
import com.uramos.minicash.ui.utils.getIconVectorByName
import com.uramos.minicash.ui.utils.toCurrencyFormat

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    userName: String,
    viewModel: HomeViewModel,
    onOpenSettings: () -> Unit = {},
    onOpenReports: () -> Unit = {},
    listState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState(),
    onAddTransactionClick: () -> Unit,
    onOpenAccount: (Long) -> Unit = {},
    onSeeMoreTransactionsClick: () -> Unit = {}
) {
    val spacing = MinicashThemeExtensions.spacing
    val state by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val selectedTotalAmount by viewModel.selectedTotalAmount.collectAsState()

    val categoryMap = remember(categories) { categories.associateBy({ it.id }, { it.name }) }
    var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }

    val haptics = rememberMinicashHaptics()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.deletedEvents.collect { deleted ->
            val message = if (deleted.size == 1) "Movimiento eliminado" else "${deleted.size} movimientos eliminados"
            val result = snackbarHostState.showSnackbar(message = message, actionLabel = "Deshacer", duration = SnackbarDuration.Long)
            if (result == SnackbarResult.ActionPerformed) viewModel.restoreTransactions(deleted)
        }
    }

    val entranceTracker = rememberEntranceTracker()
    val isRestricted = LocalAnimationRestriction.current
    // Fecha de hoy bajo el saludo, p. ej. Sábado 21 de noviembre
    val todayLabel = remember {
        java.text.SimpleDateFormat("EEEE d 'de' MMMM", java.util.Locale("es", "MX")).format(java.util.Date())
            .replaceFirstChar { it.uppercase() }
    }

    if (selectedIds.isNotEmpty()) {
        BackHandler {
            viewModel.clearSelection()
        }
    }

    val greetingPattern = remember {
        listOf(
            "Hola, %s",
            "Qué tal, %s",
            "Buen día, %s",
            "Todo listo, %s",
            "Qué hay de nuevo, %s"
        ).random()
    }
    val greetingText = remember(userName) { greetingPattern.format(userName) }

    if (transactionToEdit != null) {
        EditTransactionSheet(
            transaction = transactionToEdit!!,
            accounts = state.accounts,
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
                                title = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = greetingText,
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = todayLabel,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                actions = { TabTopBarActions(onOpenReports = onOpenReports, onOpenSettings = onOpenSettings) }
                            )
            }
        ) { headerHeight ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    MinicashLoading()
                }
            } else {
                val groupedRecent = remember(state.recentTransactions) {
                    com.uramos.minicash.ui.utils.groupTransactionsByDate(state.recentTransactions)
                }
                val accountMap = remember(state.accounts) { state.accounts.associateBy({ it.id }, { it.name }) }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().underHeaderBlur(headerHeight, bottomBand = LocalBottomBlurBand.current, listState = listState),
                    contentPadding = PaddingValues(
                        top = headerHeight + spacing.standard,
                        start = spacing.screen,
                        end = spacing.screen,
                        bottom = rememberTabListBottomPadding()
                    ),
                    verticalArrangement = Arrangement.spacedBy(spacing.hero)
                ) {
                    // Patrimonio: suma de los saldos de todas las cuentas
                    item {
                        PatrimonyCard(
                            totalBalance = state.totalBalance,
                            accountCount = state.accounts.size,
                            modifier = Modifier.listEntrance(entranceTracker, "home_hero", 0)
                        )
                    }

                    // Sección 1: Cuentas
                    item {
                        Column(modifier = Modifier.listEntrance(entranceTracker, "home_accounts", 1)) {
                            MinicashSectionTitle("Mis cuentas")
                            Spacer(modifier = Modifier.height(spacing.medium))
                            // Carrusel: las tarjetas de los bordes se recortan con máscara y se acomodan al desplazar
                            val accountsCarouselState = rememberCarouselState { state.accounts.size }
                            HorizontalUncontainedCarousel(
                                state = accountsCarouselState,
                                itemWidth = 156.dp,
                                itemSpacing = spacing.medium,
                                modifier = Modifier.fillMaxWidth().height(148.dp)
                            ) { index ->
                                val account = state.accounts[index]
                                AccountCard(
                                    account = account,
                                    modifier = Modifier.fillMaxSize().maskClip(MinicashShapes.Card),
                                    onClick = { onOpenAccount(account.id) }
                                )
                            }
                        }
                    }

                    // Módulo de Análisis Estadístico: Widget de 2 Caras con Swipe (Gastos e Ingresos)
                    item {
                        Column(modifier = Modifier.listEntrance(entranceTracker, "home_stats", 2)) {
                            MinicashSectionTitle("Distribución del mes")
                            Spacer(modifier = Modifier.height(spacing.small))
                            val chartMonthOverride by viewModel.chartMonth.collectAsState()
                            val chartYears by viewModel.chartYears.collectAsState()
                            val chartMonth = chartMonthOverride ?: YearMonth.now()
                            MinicashPeriodSelector(
                                year = chartMonth.year,
                                monthIndex = chartMonth.monthValue - 1,
                                years = chartYears,
                                onYearSelect = {
                                    haptics.tick()
                                    viewModel.selectChartYear(it)
                                },
                                onShiftMonths = {
                                    haptics.tick()
                                    viewModel.shiftChartMonth(it)
                                }
                            )
                            Spacer(modifier = Modifier.height(spacing.small))
                            TwoFacedStatsChartWidget(
                                monthlyExpenseStats = state.monthlyExpenseStats,
                                totalMonthlyExpense = state.totalMonthlyExpense,
                                monthlyIncomeStats = state.monthlyIncomeStats,
                                totalMonthlyIncome = state.totalMonthlyIncome
                            )
                        }
                    }

                    // Sección 2: Transacciones recientes (Últimos 10 movimientos)
                    item {
                        Column(modifier = Modifier.listEntrance(entranceTracker, "home_recent", 3)) {
                            MinicashSectionTitle("Movimientos recientes")
                            Spacer(modifier = Modifier.height(spacing.medium))
                        }
                    }

                    if (state.recentTransactions.isEmpty()) {
                        item {
                            MinicashEmptyState(
                                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                                title = "Sin movimientos recientes",
                                subtitle = "Tus movimientos de los últimos días aparecerán aquí.",
                                modifier = Modifier.padding(vertical = spacing.large)
                            )
                        }
                    } else {
                        groupedRecent.forEach { group ->
                            item(key = "group_${group.dateKey}") {
                                Box(modifier = minicashAnimateItem()) {
                                    GroupedTransactionCard(
                                        group = group,
                                        categoryMap = categoryMap,
                                        accountMap = accountMap,
                                        selectedIds = selectedIds,
                                        onToggleSelection = { id ->
                                            haptics.click()
                                            viewModel.toggleSelection(id)
                                        },
                                        onTransactionClick = { transactionToEdit = it }
                                    )
                                }
                            }
                        }

                        // Botón en píldora "Ver más movimientos"
                        item {
                            MinicashCard(
                                onClick = onSeeMoreTransactionsClick,
                                level = CardTonalLevel.HIGH,
                                shape = CircleShape,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = spacing.extraSmall)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = spacing.medium),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Ver más movimientos",
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

            // CÁPSULA FLOTANTE DE SELECCIÓN Y SUMA EN VIVO (ELEVADA A 96dp)
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
                                text = if (selectedIds.size == 1) "1 seleccionado" else "${selectedIds.size} seleccionados",
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
                                contentDescription = "Eliminar seleccionados",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
                }
    }
    }
}



@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GroupedTransactionCard(
    group: com.uramos.minicash.ui.utils.TransactionDateGroup,
    categoryMap: Map<Long, String>,
    accountMap: Map<Long, String>,
    selectedIds: Set<Long> = emptySet(),
    onToggleSelection: ((Long) -> Unit)? = null,
    onTransactionClick: (TransactionEntity) -> Unit,
    onTransactionDelete: ((TransactionEntity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val spacing = MinicashThemeExtensions.spacing
    val haptics = rememberMinicashHaptics()

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.small, vertical = spacing.extraSmall),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = group.dateLabel,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            MoneyTextView(
                amountCents = group.dayNetTotal,
                type = when {
                    group.dayNetTotal > 0 -> MoneyDisplayType.INCOME
                    group.dayNetTotal < 0 -> MoneyDisplayType.EXPENSE
                    else -> MoneyDisplayType.NEUTRAL
                },
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFeatureSettings = "tnum"
                )
            )
        }

        Spacer(modifier = Modifier.height(spacing.extraSmall))

        group.transactions.forEachIndexed { index, tx ->
            // Todo el estado de la fila (forma, color, deslizamiento) vive dentro de la llave del movimiento:
            // al insertar o quitar filas cada una conserva el suyo y no hereda el de la que ocupaba su lugar.
            key(tx.id) {
                TransactionListRow(
                    transaction = tx,
                    index = index,
                    count = group.transactions.size,
                    accountName = accountMap[tx.sourceAccountId] ?: "Cuenta",
                    categoryName = tx.categoryId?.let { categoryMap[it] },
                    selectedIds = selectedIds,
                    onToggleSelection = onToggleSelection,
                    onTransactionClick = onTransactionClick,
                    onTransactionDelete = onTransactionDelete
                )
            }
        }
    }
}

/** Fila de un movimiento dentro de su grupo de día: tarjeta segmentada, selección y, si se puede, deslizar para eliminar. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TransactionListRow(
    transaction: TransactionEntity,
    index: Int,
    count: Int,
    accountName: String,
    categoryName: String?,
    selectedIds: Set<Long>,
    onToggleSelection: ((Long) -> Unit)?,
    onTransactionClick: (TransactionEntity) -> Unit,
    onTransactionDelete: ((TransactionEntity) -> Unit)?
) {
    val haptics = rememberMinicashHaptics()
    val itemShape = rememberSegmentedShape(index = index, count = count)
    val isSelectionMode = selectedIds.isNotEmpty()
    val isSelected = transaction.id in selectedIds
    val rowColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.cardSurface,
        animationSpec = minicashEffectsSpec(),
        label = "transaction_row_color"
    )
    val interactionSource = remember { MutableInteractionSource() }

    @Composable
    fun Card() {
        MinicashCard(
            shape = itemShape,
            containerColor = rowColor,
            modifier = Modifier
                .fillMaxWidth()
                .bounceOnPress(interactionSource, pressedScale = 0.97f)
                .semantics {
                    // Quien no pueda deslizar (TalkBack) elimina desde las acciones del elemento
                    if (onTransactionDelete != null) {
                        customActions = listOf(
                            CustomAccessibilityAction("Eliminar movimiento") {
                                onTransactionDelete(transaction)
                                true
                            }
                        )
                    }
                }
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = {
                        if (isSelectionMode) {
                            haptics.tick()
                            onToggleSelection?.invoke(transaction.id)
                        } else {
                            onTransactionClick(transaction)
                        }
                    },
                    onLongClick = {
                        haptics.heavyOrDelete()
                        onToggleSelection?.invoke(transaction.id)
                    }
                )
        ) {
            CompactTransactionRow(
                transaction = transaction,
                accountName = accountName,
                categoryName = categoryName,
                isSelected = isSelected,
                isSelectionActive = isSelectionMode,
                onClick = null
            )
        }
    }

    if (onTransactionDelete != null && !isSelectionMode) {
        SwipeToDeleteRow(
            shape = itemShape,
            onDelete = { onTransactionDelete(transaction) },
            modifier = Modifier.padding(vertical = 2.dp)
        ) { Card() }
    } else {
        Box(modifier = Modifier.padding(vertical = 2.dp)) { Card() }
    }
}

@Composable
fun CompactTransactionRow(
    transaction: TransactionEntity,
    accountName: String,
    categoryName: String? = null,
    isSelected: Boolean = false,
    isSelectionActive: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val spacing = MinicashThemeExtensions.spacing
    val isIncome = transaction.type == TransactionType.INCOME
    val isTransfer = transaction.type == TransactionType.TRANSFER

    val colorScheme = MaterialTheme.colorScheme
    val (containerColor, contentColor, arrowIcon) = remember(transaction.type, colorScheme) {
        when {
            isIncome -> Triple(colorScheme.primaryContainer, colorScheme.onPrimaryContainer, Icons.AutoMirrored.Filled.TrendingUp)
            isTransfer -> Triple(colorScheme.tertiaryContainer, colorScheme.onTertiaryContainer, Icons.Default.SwapHoriz)
            else -> Triple(colorScheme.errorContainer, colorScheme.onErrorContainer, Icons.AutoMirrored.Filled.TrendingDown)
        }
    }

    val displayType = when (transaction.type) {
        TransactionType.INCOME -> MoneyDisplayType.INCOME
        TransactionType.EXPENSE -> MoneyDisplayType.EXPENSE
        TransactionType.TRANSFER -> MoneyDisplayType.TRANSFER
    }

    val rowModifier = Modifier
        .fillMaxWidth()
        .height(72.dp)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(horizontal = 16.dp, vertical = 12.dp)

    val haptics = rememberMinicashHaptics()
    var showNoteDialog by remember { mutableStateOf(false) }

    val fullNote = transaction.note ?: ""
    val (rawConcept, extraNote) = remember(fullNote) {
        if (fullNote.contains(" • ")) {
            val p = fullNote.split(" • ", limit = 2)
            Pair(p[0], p[1])
        } else if (fullNote.contains(" - ")) {
            val p = fullNote.split(" - ", limit = 2)
            Pair(p[0], p[1])
        } else {
            Pair(fullNote, "")
        }
    }

    val pureConcept = remember(rawConcept, categoryName, transaction.type) {
        when {
            rawConcept.isNotBlank() -> rawConcept
            !categoryName.isNullOrBlank() -> categoryName
            else -> transaction.type.displayName
        }
    }

    val subtitleText = remember(accountName, categoryName) {
        if (!categoryName.isNullOrBlank()) {
            "$accountName • $categoryName"
        } else {
            accountName
        }
    }

    if (showNoteDialog) {
        TransactionNoteDialog(
            concept = pureConcept,
            note = extraNote,
            onDismiss = { showNoteDialog = false }
        )
    }

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.medium)
    ) {
        AnimatedVisibility(visible = isSelectionActive) {
            Surface(
                shape = CircleShape,
                color = if (isSelected) colorScheme.primary else colorScheme.surfaceContainerHighest,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 4.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        Surface(
            shape = com.uramos.minicash.ui.components.rememberScallopedShape(),
            color = containerColor,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = arrowIcon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(spacing.none)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = pureConcept,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (extraNote.isNotBlank()) {
                    MinicashIconButton(
                        onClick = {
                            haptics.tick()
                            showNoteDialog = true
                        },
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Notes,
                            contentDescription = "Ver notas",
                            tint = colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Text(
                text = subtitleText,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }

        MoneyTextView(
            amountCents = transaction.amount,
            type = displayType,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

/**
 * Fila que se elimina deslizándola de derecha a izquierda: el fondo rojo con el bote de basura aparece mientras se
 * arrastra y el ícono hace un pequeño rebote al pasar el umbral (~40 %). Al soltar pasado el umbral se llama a
 * [onDelete], que borra con el aviso «Deshacer» de la pantalla. Solo se desliza en ese sentido.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteRow(
    shape: androidx.compose.ui.graphics.Shape,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val haptics = rememberMinicashHaptics()
    // El estado del deslizamiento se crea una sola vez: siempre debe llamar al borrado vigente, no al de la primera composición
    val currentOnDelete by rememberUpdatedState(onDelete)
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                haptics.heavyOrDelete()
                currentOnDelete()
                true
            } else {
                false
            }
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.4f }
    )
    val passedThreshold = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart
    LaunchedEffect(passedThreshold) { if (passedThreshold) haptics.tick() }
    val iconScale by animateFloatAsState(
        targetValue = if (passedThreshold) 1.3f else 0.9f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
        ),
        label = "swipe_delete_icon"
    )
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val showing = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .background(if (showing) MaterialTheme.colorScheme.errorContainer else Color.Transparent)
                    .padding(end = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (showing) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(24.dp).graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                        }
                    )
                }
            }
        }
    ) { content() }
}
