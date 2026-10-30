package com.uramos.minicash.ui.screens.credits

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import com.uramos.minicash.data.local.entities.CreditEntity
import com.uramos.minicash.data.local.entities.CreditInstitutionEntity
import com.uramos.minicash.ui.components.DiscardChangesDialog
import com.uramos.minicash.ui.components.MinicashButton
import com.uramos.minicash.ui.components.MinicashSegmentedChoice
import com.uramos.minicash.ui.components.MinicashSwipeNumpad
import com.uramos.minicash.ui.components.MinicashTonalButton
import com.uramos.minicash.ui.components.evaluateMathExpression
import com.uramos.minicash.ui.components.rememberDiscardGuard
import com.uramos.minicash.ui.theme.MinicashShapes
import com.uramos.minicash.ui.utils.MinicashColorUtils
import com.uramos.minicash.ui.utils.asIconTint
import com.uramos.minicash.ui.utils.normalizeLocalToUtcMillis
import com.uramos.minicash.ui.utils.normalizeUtcToLocalMillis
import com.uramos.minicash.ui.utils.rememberMinicashHaptics
import com.uramos.minicash.ui.utils.toCurrencyFormat
import java.text.SimpleDateFormat
import java.util.Locale

/** Datos capturados por el formulario de compras a meses. */
data class CreditFormValues(
    val article: String,
    val institution: String,
    val responsible: String,
    val totalInstallments: Int,
    val paidInstallments: Int,
    val monthlyAmount: Double,
    /** 0.0 si el campo "Precio de contado" quedó vacío. */
    val originalPrice: Double,
    val purchaseDate: Long
)

private const val CUSTOM_MONTHS = -1
private val PRESET_MONTHS = listOf(3, 6, 9, 12)
private const val LAST_STEP = 1

/**
 * Formulario de alta y edición de compras a meses en dos pasos:
 * 1) datos de la compra, 2) plan de pagos y monto mensual. Con [credit] en `null` es un alta.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditFormSheet(
    credit: CreditEntity?,
    institutions: List<CreditInstitutionEntity>,
    onAddInstitution: (String, String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: (CreditFormValues) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val isEdit = credit != null

    val initialMonthly = remember(credit) {
        credit?.monthlyAmount?.let { cents ->
            val amount = cents / 100.0
            if (amount % 1 == 0.0) amount.toLong().toString() else String.format(Locale.US, "%.2f", amount)
        } ?: ""
    }
    val initialPrice = remember(credit) {
        credit?.originalPrice?.takeIf { it > 0 }?.let { String.format(Locale.US, "%.2f", it / 100.0) } ?: ""
    }
    val initialDate = remember(credit) {
        normalizeUtcToLocalMillis(normalizeLocalToUtcMillis(credit?.purchaseDate ?: System.currentTimeMillis()))
    }
    val initialTotal = credit?.totalInstallments?.toString() ?: "12"
    val initialPaid = credit?.paidInstallments?.toString() ?: "0"
    val initialCustom = credit != null && credit.totalInstallments !in PRESET_MONTHS

    var step by rememberSaveable { mutableIntStateOf(0) }
    var article by rememberSaveable { mutableStateOf(credit?.article ?: "") }
    var pickedInstitution by rememberSaveable { mutableStateOf(credit?.institution ?: "") }
    var responsible by rememberSaveable { mutableStateOf(credit?.responsible ?: "") }
    var purchaseDate by rememberSaveable { mutableLongStateOf(initialDate) }
    var originalPriceStr by rememberSaveable { mutableStateOf(initialPrice) }
    var totalInstallmentsStr by rememberSaveable { mutableStateOf(initialTotal) }
    var paidInstallmentsStr by rememberSaveable { mutableStateOf(initialPaid) }
    var isCustomMonths by rememberSaveable { mutableStateOf(initialCustom) }
    var monthlyAmountExpression by rememberSaveable { mutableStateOf(initialMonthly) }
    var showInstitutionSheet by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }

    val selectedInstitution = pickedInstitution.ifBlank { if (isEdit) "" else institutions.firstOrNull()?.name ?: "" }
    val calculatedMonthlyAmount = remember(monthlyAmountExpression) { evaluateMathExpression(monthlyAmountExpression) }
    val totalInstallments = totalInstallmentsStr.toIntOrNull() ?: 0
    val paidInstallments = paidInstallmentsStr.toIntOrNull()
    val paidInvalid = paidInstallments == null || paidInstallments > totalInstallments
    val monthlyCents = calculatedMonthlyAmount.toCentsSafe()

    val isDirty = article != (credit?.article ?: "") ||
        pickedInstitution != (credit?.institution ?: "") ||
        responsible != (credit?.responsible ?: "") ||
        purchaseDate != initialDate ||
        originalPriceStr != initialPrice ||
        totalInstallmentsStr != initialTotal ||
        paidInstallmentsStr != initialPaid ||
        monthlyAmountExpression != initialMonthly

    val guard = rememberDiscardGuard(isDirty = isDirty, onDismiss = onDismiss)
    val haptics = rememberMinicashHaptics()
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val step1Valid = article.isNotBlank() && selectedInstitution.isNotBlank()
    val canSubmit = step1Valid && totalInstallments > 0 && calculatedMonthlyAmount > 0.0 && !paidInvalid

    BackHandler(enabled = step > 0) { step = 0 }

    ModalBottomSheet(
        onDismissRequest = guard::requestDismiss,
        sheetState = guard.sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEdit) "Editar compra a meses" else "Nueva compra a meses",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Paso ${step + 1} de ${LAST_STEP + 1}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            StepIndicator(step = step)

            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { it / 4 } + fadeIn()) togetherWith (slideOutHorizontally { -it / 4 } + fadeOut())
                    } else {
                        (slideInHorizontally { -it / 4 } + fadeIn()) togetherWith (slideOutHorizontally { it / 4 } + fadeOut())
                    }
                },
                label = "credit_form_step"
            ) { currentStep ->
                if (currentStep == 0) {
                    PurchaseStep(
                        article = article,
                        onArticleChange = { article = it },
                        institutions = institutions,
                        selectedInstitution = selectedInstitution,
                        onPickInstitution = { showInstitutionSheet = true },
                        responsible = responsible,
                        onResponsibleChange = { responsible = it },
                        purchaseDate = purchaseDate,
                        onPickDate = { showDatePicker = true },
                        originalPriceStr = originalPriceStr,
                        onOriginalPriceChange = { input ->
                            val normalized = input.replace(',', '.').filter { it.isDigit() || it == '.' }
                            val parts = normalized.split('.')
                            if (parts.size <= 2) {
                                originalPriceStr = if (parts.size == 2) parts[0] + "." + parts[1].take(2) else normalized
                            }
                        }
                    )
                } else {
                    PlanStep(
                        isCustomMonths = isCustomMonths,
                        onCustomMonthsChange = { isCustomMonths = it },
                        totalInstallmentsStr = totalInstallmentsStr,
                        onTotalChange = { totalInstallmentsStr = it },
                        paidInstallmentsStr = paidInstallmentsStr,
                        onPaidChange = { paidInstallmentsStr = it },
                        paidInvalid = paidInvalid,
                        totalInstallments = totalInstallments,
                        expression = monthlyAmountExpression,
                        onExpressionChange = { monthlyAmountExpression = it },
                        monthlyCents = monthlyCents
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step == 0) {
                    MinicashTonalButton(
                        text = "Cancelar",
                        onClick = guard::requestDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    MinicashButton(
                        text = "Siguiente",
                        onClick = {
                            haptics.tick()
                            keyboard?.hide()
                            focusManager.clearFocus()
                            step = 1
                        },
                        enabled = step1Valid,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    if (isEdit && onDelete != null) {
                        // Solo ícono: deja todo el ancho restante a los botones principales en cualquier densidad/escala de fuente
                        OutlinedButton(
                            onClick = { showDeleteConfirm = true },
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar crédito", modifier = Modifier.size(20.dp))
                        }
                    }
                    MinicashTonalButton(
                        text = "Atrás",
                        onClick = { step = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    MinicashButton(
                        text = if (isEdit) "Guardar" else "Guardar compra",
                        onClick = {
                            if (canSubmit) {
                                haptics.confirm()
                                keyboard?.hide()
                                focusManager.clearFocus()
                                onSubmit(
                                    CreditFormValues(
                                        article = article.trim(),
                                        institution = selectedInstitution.trim(),
                                        responsible = responsible.trim(),
                                        totalInstallments = totalInstallments,
                                        paidInstallments = paidInstallments ?: 0,
                                        monthlyAmount = calculatedMonthlyAmount,
                                        originalPrice = originalPriceStr.toDoubleOrNull() ?: 0.0,
                                        purchaseDate = purchaseDate
                                    )
                                )
                                onDismiss()
                            }
                        },
                        enabled = canSubmit,
                        modifier = Modifier.weight(1.4f)
                    )
                }
            }
        }
    }

    DiscardChangesDialog(guard)

    if (showInstitutionSheet) {
        InstitutionSelectionSheet(
            institutions = institutions,
            selectedInstitution = selectedInstitution,
            onInstitutionSelected = { pickedInstitution = it },
            onAddInstitution = onAddInstitution,
            onDismiss = { showInstitutionSheet = false }
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = normalizeLocalToUtcMillis(purchaseDate))
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { purchaseDate = normalizeUtcToLocalMillis(it) }
                        showDatePicker = false
                    }
                ) { Text("Aceptar", fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showDeleteConfirm && credit != null && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Eliminar crédito", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "¿Deseas eliminar el registro de \"${credit.article}\"? Esta acción no se puede deshacer.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptics.heavyOrDelete()
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar", fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancelar") } },
            shape = MinicashShapes.Hero,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }
}

private fun Double.toCentsSafe(): Long = Math.round(this * 100.0)

@Composable
private fun StepIndicator(step: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        repeat(LAST_STEP + 1) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(
                        if (index <= step) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceContainerHighest,
                        CircleShape
                    )
            )
        }
    }
}

@Composable
private fun PurchaseStep(
    article: String,
    onArticleChange: (String) -> Unit,
    institutions: List<CreditInstitutionEntity>,
    selectedInstitution: String,
    onPickInstitution: () -> Unit,
    responsible: String,
    onResponsibleChange: (String) -> Unit,
    purchaseDate: Long,
    onPickDate: () -> Unit,
    originalPriceStr: String,
    onOriginalPriceChange: (String) -> Unit
) {
    val instColor = institutions.firstOrNull { it.name.equals(selectedInstitution, ignoreCase = true) }
        ?.colorHex?.let { MinicashColorUtils.parseHexColor(it, MaterialTheme.colorScheme.primary) }
        ?: MaterialTheme.colorScheme.primary
    val dateLabel = remember(purchaseDate) {
        SimpleDateFormat("d MMM yyyy", Locale.forLanguageTag("es-MX")).format(purchaseDate)
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = article,
            onValueChange = onArticleChange,
            label = { Text("Artículo o servicio") },
            placeholder = { Text("Nombre del producto o servicio") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        PickerPill(
            onClick = onPickInstitution,
            container = instColor.copy(alpha = 0.15f),
            leading = {
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = null,
                    tint = instColor.asIconTint(),
                    modifier = Modifier.size(20.dp)
                )
            },
            text = selectedInstitution.ifBlank { "Selecciona una institución" },
            filled = selectedInstitution.isNotBlank()
        )

        OutlinedTextField(
            value = responsible,
            onValueChange = onResponsibleChange,
            label = { Text("Responsable") },
            placeholder = { Text("Nombre del responsable (por defecto, Yo)") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        PickerPill(
            onClick = onPickDate,
            container = MaterialTheme.colorScheme.surfaceContainerHigh,
            leading = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            },
            text = "Fecha de compra: $dateLabel",
            filled = true
        )

        OutlinedTextField(
            value = originalPriceStr,
            onValueChange = onOriginalPriceChange,
            label = { Text("Precio de contado (opcional)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PickerPill(
    onClick: () -> Unit,
    container: androidx.compose.ui.graphics.Color,
    leading: @Composable () -> Unit,
    text: String,
    filled: Boolean
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = container,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leading()
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (filled) FontWeight.SemiBold else FontWeight.Normal,
                color = if (filled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PlanStep(
    isCustomMonths: Boolean,
    onCustomMonthsChange: (Boolean) -> Unit,
    totalInstallmentsStr: String,
    onTotalChange: (String) -> Unit,
    paidInstallmentsStr: String,
    onPaidChange: (String) -> Unit,
    paidInvalid: Boolean,
    totalInstallments: Int,
    expression: String,
    onExpressionChange: (String) -> Unit,
    monthlyCents: Long
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MinicashSegmentedChoice(
            options = PRESET_MONTHS + CUSTOM_MONTHS,
            isSelected = { if (it == CUSTOM_MONTHS) isCustomMonths else !isCustomMonths && totalInstallmentsStr == it.toString() },
            onSelect = {
                if (it == CUSTOM_MONTHS) {
                    onCustomMonthsChange(true)
                } else {
                    onCustomMonthsChange(false)
                    onTotalChange(it.toString())
                }
            },
            label = { if (it == CUSTOM_MONTHS) "Otro" else "$it m" }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = paidInstallmentsStr,
                onValueChange = { onPaidChange(it.filter(Char::isDigit).take(3)) },
                label = { Text("Ya pagadas") },
                isError = paidInvalid,
                supportingText = if (paidInvalid) {
                    { Text("0 a $totalInstallments") }
                } else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )
            if (isCustomMonths) {
                OutlinedTextField(
                    value = totalInstallmentsStr,
                    onValueChange = { onTotalChange(it.filter(Char::isDigit).take(3)) },
                    label = { Text("Plazo total") },
                    suffix = { Text("meses", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Monto mensual",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = expression.ifBlank { "0" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                Text(
                    text = monthlyCents.toCurrencyFormat(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        MinicashSwipeNumpad(
            amountExpression = expression,
            onExpressionChange = { expr, _ -> onExpressionChange(expr) }
        )
    }
}
