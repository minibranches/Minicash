package com.uramos.minicash.ui.screens.credits

import androidx.compose.ui.res.pluralStringResource
import com.uramos.minicash.ui.utils.AppStrings
import androidx.compose.ui.res.stringResource
import com.uramos.minicash.R
import com.uramos.minicash.ui.utils.appLocale
import com.uramos.minicash.ui.utils.appDateFormat
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import com.uramos.minicash.ui.utils.rememberTabListBottomPadding
import com.uramos.minicash.ui.theme.minicashAnimateItem
import com.uramos.minicash.ui.components.ExpandingFilterButton
import com.uramos.minicash.ui.components.LocalBottomBlurBand
import com.uramos.minicash.ui.components.CollapsibleSectionHeader
import com.uramos.minicash.ui.components.StatPillMoney
import com.uramos.minicash.ui.theme.screenBackground
import com.uramos.minicash.ui.theme.cardSurface
import com.uramos.minicash.ui.theme.sheetBackground
import com.uramos.minicash.ui.components.underHeaderBlur
import com.uramos.minicash.ui.components.BlurHeaderScaffold
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.WindowInsets
import com.uramos.minicash.ui.components.MinicashBackButton
import com.uramos.minicash.ui.components.TabTopBarActions
import com.uramos.minicash.ui.components.MinicashAlertDialog
import androidx.compose.material.icons.filled.Notifications
import com.uramos.minicash.ui.components.MinicashTextButton
import com.uramos.minicash.ui.components.MinicashFilledButton
import com.uramos.minicash.ui.components.MinicashTonalIconButton
import com.uramos.minicash.ui.components.MinicashIconButton
import com.uramos.minicash.ui.components.listEntrance
import com.uramos.minicash.ui.components.rememberEntranceTracker
import com.uramos.minicash.ui.utils.toCents
import com.uramos.minicash.ui.utils.asIconTint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import com.uramos.minicash.ui.utils.rememberMinicashHaptics
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.uramos.minicash.data.local.entities.AccountEntity
import com.uramos.minicash.data.local.entities.CreditEntity
import com.uramos.minicash.data.local.entities.CreditInstitutionEntity
import com.uramos.minicash.ui.components.CardTonalLevel
import com.uramos.minicash.ui.components.MinicashSwipeNumpad
import com.uramos.minicash.ui.components.MinicashButton
import com.uramos.minicash.ui.components.MinicashTonalButton
import com.uramos.minicash.ui.components.MinicashSegmentedChoice
import com.uramos.minicash.ui.components.evaluateMathExpression
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.saveable.rememberSaveable
import com.uramos.minicash.ui.components.MinicashCard
import com.uramos.minicash.ui.components.MinicashColorPicker
import com.uramos.minicash.ui.components.MinicashEmptyState
import com.uramos.minicash.ui.components.MoneyDisplayType
import com.uramos.minicash.ui.components.MoneyTextView
import com.uramos.minicash.ui.components.MinicashPillTabs
import com.uramos.minicash.ui.components.WavyProgressIndicator
import com.uramos.minicash.ui.components.rememberSegmentedShape
import com.uramos.minicash.ui.theme.MinicashShapes
import com.uramos.minicash.ui.theme.MinicashThemeExtensions
import com.uramos.minicash.ui.utils.MinicashColorUtils
import com.uramos.minicash.ui.utils.formatTransactionDate
import com.uramos.minicash.ui.utils.toCurrencyFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditsScreen(
    viewModel: CreditsViewModel,
    listState: androidx.compose.foundation.lazy.grid.LazyGridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState(),
    showAddDialogExternal: Boolean = false,
    onAddDialogExternalHandled: () -> Unit = {},
    onOpenAddTransactionForCredit: (amountCents: Long, conceptStr: String, notesStr: String) -> Unit = { _, _, _ -> },
    onBack: (() -> Unit)? = null,
    onOpenReports: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    /** Verdadero cuando Créditos es la pestaña a la vista (no solo compuesta junto a la visible). */
    isActive: Boolean = true
) {
    val spacing = MinicashThemeExtensions.spacing
    val state by viewModel.uiState.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val institutions by viewModel.institutions.collectAsState()
    val haptics = rememberMinicashHaptics()
    val entranceTracker = rememberEntranceTracker()

    // Si la pestaña se compone junto a la visible (pager) y luego se muestra, la lista puede quedarse sin
    // redibujar hasta que algo la toque. Pedir el mismo desplazamiento fuerza un nuevo medido al mostrarla.
    LaunchedEffect(isActive) {
        if (isActive) listState.requestScrollToItem(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset)
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var settledExpanded by rememberSaveable { mutableStateOf(false) }
    var creditToPay by remember { mutableStateOf<CreditEntity?>(null) }
    var creditToEdit by remember { mutableStateOf<CreditEntity?>(null) }
    var creditToDelete by remember { mutableStateOf<CreditEntity?>(null) }

    LaunchedEffect(showAddDialogExternal) {
        if (showAddDialogExternal) {
            showAddDialog = true
            onAddDialogExternalHandled()
        }
    }

    if (creditToDelete != null) {
        val targetCredit = creditToDelete!!
        MinicashAlertDialog(
            onDismissRequest = { creditToDelete = null },
            title = {
                Text(
                    text = stringResource(R.string.credit_delete),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.credit_delete_text, targetCredit.article),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                MinicashTextButton(
                    onClick = {
                        haptics.heavyOrDelete()
                        viewModel.deleteCredit(targetCredit)
                        creditToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.common_delete), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                MinicashTextButton(onClick = { creditToDelete = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
            shape = MinicashShapes.Hero,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.screenBackground,
        contentWindowInsets = WindowInsets(0.dp),
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
                                    navigationIcon = { onBack?.let { MinicashBackButton(onClick = it) } },
                                    title = { Text(stringResource(R.string.nav_credits), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) },
                                    actions = { TabTopBarActions(onOpenReports = onOpenReports, onOpenSettings = onOpenSettings) }
                                )
            },
            pinned = {
                Column {
            // Resumen en estadios y botón de filtros, en la misma fila
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.screen, vertical = spacing.small),
                        horizontalArrangement = Arrangement.spacedBy(spacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatPillMoney(
                            label = stringResource(R.string.credit_debt_pending),
                            amountCents = state.summary.totalPendingDebt,
                            type = MoneyDisplayType.NEUTRAL,
                            modifier = Modifier.weight(1f)
                        )
                        StatPillMoney(
                            label = pluralStringResource(R.plurals.credit_monthly_active, state.summary.activeCreditsCount, state.summary.activeCreditsCount),
                            amountCents = state.summary.monthlyPaymentCommitment,
                            type = MoneyDisplayType.NEUTRAL,
                            modifier = Modifier.weight(1f)
                        )
                        ExpandingFilterButton(
                            activeCount = listOfNotNull(
                                state.selectedResponsible,
                                state.selectedInstitution,
                                state.selectedMonth
                            ).size,
                            onClick = {
                                haptics.tick()
                                showFilterSheet = true
                            },
                            onClear = {
                                haptics.tick()
                                viewModel.clearFilters()
                            },
                            contentDescription = stringResource(R.string.credit_filters_title)
                        )
                    }
                }
            }
        ) { headerHeight ->
            // Una sola cuadrícula de 2 columnas: activos arriba y, plegados al final, los liquidados
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = listState,
                modifier = Modifier.fillMaxSize().underHeaderBlur(headerHeight, bottomBand = LocalBottomBlurBand.current, listState = listState),
                contentPadding = PaddingValues(top = headerHeight + 16.dp, start = 20.dp, end = 20.dp, bottom = rememberTabListBottomPadding()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (state.activeCredits.isEmpty()) {
                    item(key = "credits_empty", span = { GridItemSpan(maxLineSpan) }) {
                        MinicashEmptyState(
                            icon = Icons.Default.CreditCard,
                            title = stringResource(R.string.credit_empty_title),
                            subtitle = stringResource(R.string.credit_empty_sub),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = spacing.large)
                        )
                    }
                }
                itemsIndexed(state.activeCredits, key = { _, item -> item.id }) { index, credit ->
                    CreditItemCard(
                        credit = credit,
                        institutions = institutions,
                        onPayClick = { creditToPay = credit },
                        onEdit = { creditToEdit = credit },
                        modifier = minicashAnimateItem().listEntrance(entranceTracker, credit.id, index)
                    )
                }
                if (state.settledCredits.isNotEmpty()) {
                    item(key = "credits_settled_header", span = { GridItemSpan(maxLineSpan) }) {
                        CollapsibleSectionHeader(
                            title = stringResource(R.string.credit_settled),
                            count = state.settledCredits.size,
                            expanded = settledExpanded,
                            onToggle = {
                                haptics.tick()
                                settledExpanded = !settledExpanded
                            },
                            modifier = Modifier.padding(top = spacing.medium)
                        )
                    }
                    if (settledExpanded) {
                        itemsIndexed(state.settledCredits, key = { _, item -> item.id }) { _, credit ->
                            CreditItemCard(
                                credit = credit,
                                institutions = institutions,
                                onPayClick = { creditToPay = credit },
                                onEdit = { creditToEdit = credit },
                                modifier = minicashAnimateItem()
                            )
                        }
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        CreditsFilterSheet(
            selectedResponsible = state.selectedResponsible,
            responsibles = state.availableResponsibles,
            selectedInstitution = state.selectedInstitution,
            institutions = state.availableInstitutions,
            selectedMonth = state.selectedMonth,
            months = state.availableMonths,
            onSelectMonth = { month -> viewModel.onSelectMonthFilter(month) },
            onSelectResponsible = { resp -> viewModel.onSelectResponsibleFilter(resp) },
            onSelectInstitution = { inst -> viewModel.onSelectInstitutionFilter(inst) },
            onClearFilters = { viewModel.clearFilters() },
            onDismiss = { showFilterSheet = false }
        )
    }

    if (showAddDialog) {
        CreditFormSheet(
            credit = null,
            institutions = institutions,
            onAddInstitution = { name, colorHex -> viewModel.addInstitution(name, colorHex) },
            onDismiss = { showAddDialog = false },
            onSubmit = { v ->
                val price = if (v.originalPrice > 0.0) v.originalPrice else v.monthlyAmount * v.totalInstallments
                viewModel.addCredit(
                    v.article, v.institution, v.responsible, v.totalInstallments,
                    v.monthlyAmount, price, v.purchaseDate, v.paidInstallments,
                    v.paymentDay, v.reminderDaysBefore, v.paymentOverride
                )
            }
        )
    }

    creditToEdit?.let { editing ->
        CreditFormSheet(
            credit = editing,
            institutions = institutions,
            onAddInstitution = { name, colorHex -> viewModel.addInstitution(name, colorHex) },
            onDismiss = { creditToEdit = null },
            onSubmit = { v ->
                val monthlyCents = v.monthlyAmount.toCents()
                viewModel.updateCredit(
                    editing.copy(
                        article = v.article,
                        institution = v.institution,
                        responsible = v.responsible.ifBlank { AppStrings.get(R.string.credit_default_responsible) },
                        purchaseDate = v.purchaseDate,
                        paidInstallments = v.paidInstallments.coerceIn(0, v.totalInstallments),
                        totalInstallments = v.totalInstallments,
                        monthlyAmount = monthlyCents,
                        totalAmount = monthlyCents * v.totalInstallments,
                        originalPrice = if (v.originalPrice > 0.0) v.originalPrice.toCents() else editing.originalPrice,
                        paymentDay = v.paymentDay,
                        reminderDaysBefore = v.reminderDaysBefore,
                        paymentOverride = v.paymentOverride
                    )
                )
            },
            onDelete = {
                creditToDelete = editing
                creditToEdit = null
            }
        )
    }

    if (creditToPay != null) {
        ConfirmCreditPaymentDialog(
            credit = creditToPay!!,
            onDismiss = { creditToPay = null },
            onConfirm = { createExpenseTransaction ->
                val targetCredit = creditToPay!!
                haptics.confirm()
                viewModel.incrementInstallment(targetCredit)

                if (createExpenseTransaction) {
                    val conceptStr = targetCredit.article.trim()
                    val notesStr = ""
                    onOpenAddTransactionForCredit(targetCredit.monthlyAmount, conceptStr, notesStr)
                }

                creditToPay = null
            }
        )
    }
}

@Composable
fun ConfirmCreditPaymentDialog(
    credit: CreditEntity,
    onDismiss: () -> Unit,
    onConfirm: (createExpenseTransaction: Boolean) -> Unit
) {
    var createExpenseTransaction by remember { mutableStateOf(true) }
    val nextInstallment = credit.paidInstallments + 1

    MinicashAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.credit_pay_title, credit.article), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.credit_installment_line, credit.institution, nextInstallment, credit.totalInstallments),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        MoneyTextView(
                            amountCents = credit.monthlyAmount,
                            type = MoneyDisplayType.NEUTRAL,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Checkbox(
                        checked = createExpenseTransaction,
                        onCheckedChange = { createExpenseTransaction = it }
                    )
                    Text(
                        text = stringResource(R.string.credit_create_expense),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            MinicashFilledButton(
                onClick = { onConfirm(createExpenseTransaction) },
            ) {
                Text(stringResource(R.string.common_confirm), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            MinicashTextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        },
        shape = MinicashShapes.Hero
    )
}

/**
 * Crédito como tarjeta de la cuadrícula de 2 columnas: ícono con el color de su institución, avance (con la barra
 * ondulada expresiva), mensualidad y «Pagar mes». Los liquidados muestran el total pagado y una insignia.
 */
@Composable
fun CreditItemCard(
    credit: CreditEntity,
    institutions: List<CreditInstitutionEntity> = emptyList(),
    onPayClick: () -> Unit,
    onEdit: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val spacing = MinicashThemeExtensions.spacing
    val progress = if (credit.totalInstallments > 0) {
        credit.paidInstallments.toFloat() / credit.totalInstallments.toFloat()
    } else 0f
    val isCompleted = credit.paidInstallments >= credit.totalInstallments

    val defaultPrimary = MaterialTheme.colorScheme.primary
    val instColor = remember(institutions, credit.institution, credit.institutionId, defaultPrimary) {
        val match = institutions.firstOrNull { it.id == credit.institutionId }
            ?: institutions.firstOrNull { it.name.equals(credit.institution, ignoreCase = true) }
        match?.colorHex?.let { MinicashColorUtils.parseHexColor(it, defaultPrimary) } ?: defaultPrimary
    }

    val institution = institutions.firstOrNull { it.id == credit.institutionId }
    val (paymentDay, reminderDays) = com.uramos.minicash.data.util.PaymentSchedule.effectivePayment(credit, institution)
    val nextPaymentLabel = remember(paymentDay, isCompleted) {
        paymentDay?.takeIf { !isCompleted }?.let { day ->
            val next = com.uramos.minicash.data.util.PaymentSchedule.nextPayment(day, java.time.LocalDate.now())
            appDateFormat("d MMM", "MMM d")
                .format(Date.from(next.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()))
        }
    }
    val purchaseLabel = remember(credit.purchaseDate) {
        appDateFormat("d MMM yyyy", "MMM d, yyyy").format(credit.purchaseDate)
    }
    val subtitle = if (isCompleted) {
        "${credit.institution} • $purchaseLabel"
    } else {
        buildString {
            append(credit.institution)
            nextPaymentLabel?.let { append(" • ").append(AppStrings.get(R.string.credit_next_payment, it)) }
        }
    }

    MinicashCard(
        level = CardTonalLevel.LOW,
        shape = MinicashShapes.Card,
        onClick = onEdit,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = instColor.copy(alpha = 0.2f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.CreditCard,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = instColor.asIconTint()
                        )
                    }
                }
                if (!isCompleted) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHighest) {
                        Text(
                            text = stringResource(R.string.credit_settled_chip),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = spacing.small, vertical = 2.dp)
                        )
                    }
                }
            }
            Text(
                text = credit.article,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (reminderDays != null && nextPaymentLabel != null) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = stringResource(R.string.credit_with_reminder),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (!isCompleted) {
                WavyProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth().height(20.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.credit_progress, credit.paidInstallments, credit.totalInstallments),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MoneyTextView(
                        amountCents = credit.monthlyAmount,
                        type = MoneyDisplayType.NEUTRAL,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        autoSizeMin = 11.sp
                    )
                }
                MinicashFilledButton(
                    onClick = onPayClick,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = spacing.medium, vertical = spacing.extraSmall)
                ) {
                    Text(stringResource(R.string.credit_pay_month))
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.credit_total_paid),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MoneyTextView(
                        amountCents = credit.totalAmount,
                        type = MoneyDisplayType.NEUTRAL,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        autoSizeMin = 11.sp
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstitutionSelectionSheet(
    institutions: List<CreditInstitutionEntity>,
    selectedInstitution: String,
    onInstitutionSelected: (String) -> Unit,
    onAddInstitution: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showAddDialog by remember { mutableStateOf(false) }
    val haptics = rememberMinicashHaptics()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.sheetBackground,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = MinicashShapes.SheetTop
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.credit_institutions),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (institutions.isEmpty()) {
                MinicashEmptyState(
                    icon = Icons.Default.CreditCard,
                    title = stringResource(R.string.credit_inst_empty_title),
                    subtitle = stringResource(R.string.credit_inst_empty_sub),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    itemsIndexed(institutions, key = { _, inst -> inst.id }) { index, inst ->
                        val isSelected = inst.name == selectedInstitution
                        val itemShape = rememberSegmentedShape(index = index, count = institutions.size)
                        val instColor = MinicashColorUtils.parseHexColor(inst.colorHex, MaterialTheme.colorScheme.primary)

                        Surface(
                            onClick = {
                                haptics.click()
                                onInstitutionSelected(inst.name)
                                onDismiss()
                            },
                            shape = itemShape,
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
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

                                Text(
                                    text = inst.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            MinicashFilledButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Text(stringResource(R.string.credit_new_institution_btn), fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showAddDialog) {
        var newInstName by remember { mutableStateOf("") }
        var selectedColorHex by remember { mutableStateOf("#1A73E8") }

        MinicashAlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(stringResource(R.string.credit_new_institution), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newInstName,
                        onValueChange = { newInstName = it },
                        label = { Text(stringResource(R.string.credit_institution_name)) },
                        placeholder = { Text(stringResource(R.string.credit_institution_name_hint)) },
                        singleLine = true,
                        shape = CircleShape,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(stringResource(R.string.credit_color), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    MinicashColorPicker(
                        selectedColorHex = selectedColorHex,
                        onColorSelected = { selectedColorHex = it }
                    )
                }
            },
            confirmButton = {
                MinicashFilledButton(
                    onClick = {
                        if (newInstName.isNotBlank()) {
                            val trimmed = newInstName.trim()
                            onAddInstitution(trimmed, selectedColorHex)
                            onInstitutionSelected(trimmed)
                            newInstName = ""
                            showAddDialog = false
                            onDismiss()
                        }
                    },
                ) {
                    Text(stringResource(R.string.common_save), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                MinicashTextButton(onClick = { showAddDialog = false }) { Text(stringResource(R.string.common_cancel)) }
            },
            shape = MinicashShapes.Hero
        )
    }
}

