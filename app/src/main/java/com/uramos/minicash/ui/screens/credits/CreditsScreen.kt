package com.uramos.minicash.ui.screens.credits

import com.uramos.minicash.ui.components.fadingTopEdge
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
    listState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState(),
    showAddDialogExternal: Boolean = false,
    onAddDialogExternalHandled: () -> Unit = {},
    onOpenAddTransactionForCredit: (amountCents: Long, conceptStr: String, notesStr: String) -> Unit = { _, _, _ -> }
) {
    val spacing = MinicashThemeExtensions.spacing
    val state by viewModel.uiState.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val institutions by viewModel.institutions.collectAsState()
    val haptic = LocalHapticFeedback.current
    val haptics = rememberMinicashHaptics()
    val entranceTracker = rememberEntranceTracker()

    var showAddDialog by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
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
                    text = "Eliminar crédito",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "¿Deseas eliminar el registro de \"${targetCredit.article}\"? Esta acción no se puede deshacer.",
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
                    Text("Eliminar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                MinicashTextButton(onClick = { creditToDelete = null }) {
                    Text("Cancelar")
                }
            },
            shape = MinicashShapes.Hero,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                ),
                title = { Text("Créditos", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Resumen compacto de deuda
            Surface(
                shape = MinicashShapes.ContainerLarge,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.standard, vertical = spacing.small)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.standard, vertical = spacing.medium),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Deuda pendiente",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        MoneyTextView(
                            amountCents = state.summary.totalPendingDebt,
                            type = MoneyDisplayType.NEUTRAL,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Mensual · ${state.summary.activeCreditsCount} activos",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        MoneyTextView(
                            amountCents = state.summary.monthlyPaymentCommitment,
                            type = MoneyDisplayType.NEUTRAL,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // Selector de Pestañas con Botón de Filtro
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.standard, vertical = spacing.extraSmall),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.small)
            ) {
                MinicashPillTabs(
                    labels = listOf("Activos (${state.activeCount})", "Liquidados (${state.settledCount})"),
                    selectedIndex = if (state.selectedTab == CreditTab.ACTIVE) 0 else 1,
                    onSelect = { index ->
                        haptics.tick()
                        viewModel.onSelectTab(if (index == 0) CreditTab.ACTIVE else CreditTab.SETTLED)
                    },
                    modifier = Modifier.weight(1f)
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
                            contentDescription = "Filtros de créditos"
                        )
                    }

                    if (state.hasActiveFilters) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .align(Alignment.TopEnd)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                    }
                }
            }

            // Lista de Créditos
            if (state.credits.isEmpty()) {
                val emptyTitle = if (state.selectedTab == CreditTab.ACTIVE) "Sin créditos activos" else "Sin compras liquidadas"
                val emptySubtitle = if (state.selectedTab == CreditTab.ACTIVE) {
                    "No tienes compras a meses pendientes de pago."
                } else {
                    "Las compras a meses que vayas completando aparecerán aquí."
                }
                MinicashEmptyState(
                    icon = Icons.Default.CreditCard,
                    title = emptyTitle,
                    subtitle = emptySubtitle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = spacing.hero)
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().fadingTopEdge(listState),
                    contentPadding = PaddingValues(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    itemsIndexed(state.credits, key = { _, item -> item.id }) { index, credit ->
                        val itemShape = rememberSegmentedShape(index = index, count = state.credits.size)
                        Box(modifier = Modifier.animateItem().listEntrance(entranceTracker, credit.id, index)) {
                            CreditItemCard(
                                credit = credit,
                                institutions = institutions,
                                shape = itemShape,
                                onPayClick = { creditToPay = credit },
                                onEdit = { creditToEdit = credit },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
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
                        responsible = v.responsible.ifBlank { "Yo" },
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
        title = { Text("Abonar a ${credit.article}", fontWeight = FontWeight.Bold) },
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
                            text = "${credit.institution} • Mensualidad $nextInstallment de ${credit.totalInstallments}",
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
                        text = "Crear transacción de gasto",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            MinicashFilledButton(
                onClick = { onConfirm(createExpenseTransaction) },
            ) {
                Text("Confirmar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            MinicashTextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        shape = MinicashShapes.Hero
    )
}

@Composable
fun CreditItemCard(
    credit: CreditEntity,
    institutions: List<CreditInstitutionEntity> = emptyList(),
    onPayClick: () -> Unit,
    onEdit: () -> Unit = {},
    modifier: Modifier = Modifier,
    shape: Shape? = null
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
        match?.colorHex?.let {
            MinicashColorUtils.parseHexColor(it, defaultPrimary)
        } ?: defaultPrimary
    }

    val purchaseLabel = remember(credit.purchaseDate) {
        SimpleDateFormat("d MMM yyyy", Locale.forLanguageTag("es-MX")).format(credit.purchaseDate)
    }

    val institution = institutions.firstOrNull { it.id == credit.institutionId }
    val (paymentDay, reminderDays) = com.uramos.minicash.data.util.PaymentSchedule.effectivePayment(credit, institution)
    val nextPaymentLabel = remember(paymentDay, isCompleted) {
        paymentDay?.takeIf { !isCompleted }?.let { day ->
            val next = com.uramos.minicash.data.util.PaymentSchedule.nextPayment(day, java.time.LocalDate.now())
            SimpleDateFormat("d MMM", Locale.forLanguageTag("es-MX"))
                .format(java.util.Date.from(next.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()))
        }
    }

    val remainingMonths = (credit.totalInstallments - credit.paidInstallments).coerceAtLeast(0)
    val estimatedTermLabel = remember(credit.totalInstallments, credit.paidInstallments, isCompleted, remainingMonths) {
        if (!isCompleted && remainingMonths > 0) {
            val termCal = Calendar.getInstance().apply {
                add(Calendar.MONTH, remainingMonths)
            }
            val format = SimpleDateFormat("MMM yyyy", Locale.forLanguageTag("es-MX"))
            format.format(termCal.time).replaceFirstChar { it.uppercase() }
        } else null
    }

    MinicashCard(
        level = CardTonalLevel.LOW,
        shape = shape ?: RoundedCornerShape(20.dp),
        onClick = onEdit,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.standard),
            verticalArrangement = Arrangement.spacedBy(spacing.small)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = instColor.copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.CreditCard,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = instColor.asIconTint()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(spacing.small))
                    Column {
                        Text(
                            text = credit.article,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${credit.institution} • ${credit.responsible} • ${purchaseLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (nextPaymentLabel != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (reminderDays != null) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Con aviso de pago",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                Text(
                                    text = "Próximo pago: $nextPaymentLabel",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            if (!isCompleted) {
                // Barra de progreso de amortización
                Column(verticalArrangement = Arrangement.spacedBy(spacing.extraSmall)) {
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
                            text = "Mensualidad ${credit.paidInstallments} de ${credit.totalInstallments}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )
                        if (estimatedTermLabel != null) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHighest
                            ) {
                                Text(
                                    text = "Término est.: $estimatedTermLabel",
                                    modifier = Modifier.padding(horizontal = spacing.small, vertical = spacing.none),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Detalle económico y botón de registrar mensualidad
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Mensualidad:", style = MaterialTheme.typography.labelSmall)
                        MoneyTextView(
                            amountCents = credit.monthlyAmount,
                            type = MoneyDisplayType.NEUTRAL,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    MinicashFilledButton(
                        onClick = onPayClick,
                        contentPadding = PaddingValues(horizontal = spacing.medium, vertical = spacing.extraSmall)
                    ) {
                        Text("Pagar mes")
                    }
                }
            } else {
                // Tarjeta Liquidada Refinada
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total pagado",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        MoneyTextView(
                            amountCents = credit.totalAmount,
                            type = MoneyDisplayType.NEUTRAL,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Text(
                            text = "✓ Liquidado",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = spacing.medium, vertical = spacing.extraSmall)
                        )
                    }
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
    val haptic = LocalHapticFeedback.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Instituciones de crédito",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (institutions.isEmpty()) {
                MinicashEmptyState(
                    icon = Icons.Default.CreditCard,
                    title = "Sin instituciones registradas",
                    subtitle = "Agrega tus tarjetas o bancos mediante el botón inferior.",
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
                                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
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
                Text("+ Nueva institución", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showAddDialog) {
        var newInstName by remember { mutableStateOf("") }
        var selectedColorHex by remember { mutableStateOf("#1A73E8") }

        MinicashAlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Nueva institución", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newInstName,
                        onValueChange = { newInstName = it },
                        label = { Text("Nombre de la institución") },
                        placeholder = { Text("Escribe el nombre de la institución") },
                        singleLine = true,
                        shape = CircleShape,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Color distintivo:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
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
                    Text("Guardar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                MinicashTextButton(onClick = { showAddDialog = false }) { Text("Cancelar") }
            },
            shape = MinicashShapes.Hero
        )
    }
}

