package com.uramos.minicash.ui.screens.home

import androidx.compose.foundation.layout.WindowInsets
import com.uramos.minicash.ui.components.SettingsGearButton
import com.uramos.minicash.ui.components.fadingTopEdge
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
    listState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState(),
    onAddTransactionClick: () -> Unit,
    onSeeMoreTransactionsClick: () -> Unit = {}
) {
    val spacing = MinicashThemeExtensions.spacing
    val state by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val selectedTotalAmount by viewModel.selectedTotalAmount.collectAsState()

    val categoryMap = remember(categories) { categories.associateBy({ it.id }, { it.name }) }
    var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }
    var showBatchDeleteDialog by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current
    val haptics = rememberMinicashHaptics()

    val entranceTracker = rememberEntranceTracker()
    val isRestricted = LocalAnimationRestriction.current

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

    if (showBatchDeleteDialog) {
        MinicashAlertDialog(
            onDismissRequest = { showBatchDeleteDialog = false },
            title = { Text("¿Eliminar ${selectedIds.size} movimientos?", fontWeight = FontWeight.Bold) },
            text = { Text("Esta acción eliminará los movimientos seleccionados y reajustará automáticamente los saldos correspondientes. ¿Proceder?") },
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
                windowInsets = WindowInsets(0.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
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
                        }
                    }
                },
                actions = { SettingsGearButton(onClick = onOpenSettings) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val groupedRecent = remember(state.recentTransactions) {
                    com.uramos.minicash.ui.utils.groupTransactionsByDate(state.recentTransactions)
                }
                val accountMap = remember(state.accounts) { state.accounts.associateBy({ it.id }, { it.name }) }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().fadingTopEdge(listState),
                    contentPadding = PaddingValues(
                        top = spacing.standard,
                        start = spacing.standard,
                        end = spacing.standard,
                        bottom = 120.dp
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
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = spacing.none),
                                horizontalArrangement = Arrangement.spacedBy(spacing.medium)
                            ) {
                                items(items = state.accounts, key = { it.id }) { account ->
                                    AccountCard(account = account)
                                }
                            }
                        }
                    }

                    // Módulo de Análisis Estadístico: Widget de 2 Caras con Swipe (Gastos e Ingresos)
                    item {
                        Column(modifier = Modifier.listEntrance(entranceTracker, "home_stats", 2)) {
                            MinicashSectionTitle("Distribución del mes")
                            Spacer(modifier = Modifier.height(spacing.small))
                            val chartMonthOverride by viewModel.chartMonth.collectAsState()
                            ChartMonthSelector(
                                month = chartMonthOverride ?: YearMonth.now(),
                                isCurrent = chartMonthOverride == null,
                                onPrevious = { viewModel.shiftChartMonth(-1) },
                                onNext = { viewModel.shiftChartMonth(1) },
                                onReset = viewModel::resetChartMonth
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
                                Box(modifier = Modifier.animateItem()) {
                                    GroupedTransactionCard(
                                        group = group,
                                        categoryMap = categoryMap,
                                        accountMap = accountMap,
                                        selectedIds = selectedIds,
                                        onToggleSelection = { id ->
                                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
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
            val itemShape = rememberSegmentedShape(
                index = index,
                count = group.transactions.size
            )
            val accountName = accountMap[tx.sourceAccountId] ?: "Cuenta"
            val categoryName = tx.categoryId?.let { categoryMap[it] }
            val isSelectionMode = selectedIds.isNotEmpty()

            val rowColor by animateColorAsState(
                targetValue = if (tx.id in selectedIds) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                animationSpec = minicashEffectsSpec(),
                label = "transaction_row_color"
            )
            MinicashCard(
                shape = itemShape,
                containerColor = rowColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .combinedClickable(
                        onClick = {
                            if (isSelectionMode) {
                                haptics.tick()
                                onToggleSelection?.invoke(tx.id)
                            } else {
                                onTransactionClick(tx)
                            }
                        },
                        onLongClick = {
                            haptics.heavyOrDelete()
                            onToggleSelection?.invoke(tx.id)
                        }
                    )
            ) {
                CompactTransactionRow(
                    transaction = tx,
                    accountName = accountName,
                    categoryName = categoryName,
                    isSelected = tx.id in selectedIds,
                    isSelectionActive = isSelectionMode,
                    onClick = null,
                    onDeleteClick = if (onTransactionDelete != null) { { onTransactionDelete(tx) } } else null
                )
            }
        }
    }
}

@Composable
fun CompactTransactionRow(
    transaction: TransactionEntity,
    accountName: String,
    categoryName: String? = null,
    isSelected: Boolean = false,
    isSelectionActive: Boolean = false,
    onClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null
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

    val haptic = LocalHapticFeedback.current
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
            shape = CircleShape,
            color = containerColor,
            modifier = Modifier.size(36.dp)
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

        if (onDeleteClick != null && !isSelectionActive) {
            MinicashIconButton(
                onClick = onDeleteClick,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar movimiento",
                    tint = colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
