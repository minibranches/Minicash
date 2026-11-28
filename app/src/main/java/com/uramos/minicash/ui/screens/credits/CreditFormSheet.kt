package com.uramos.minicash.ui.screens.credits

import com.uramos.minicash.ui.utils.AppStrings
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.uramos.minicash.R
import com.uramos.minicash.ui.utils.appLocale
import com.uramos.minicash.ui.utils.appDateFormat
import com.uramos.minicash.ui.theme.sheetBackground
import com.uramos.minicash.ui.components.GuardedModalBottomSheet
import com.uramos.minicash.ui.components.MinicashAlertDialog
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import android.os.Build
import android.content.pm.PackageManager
import android.Manifest
import com.uramos.minicash.ui.components.MinicashOutlinedButton
import com.uramos.minicash.ui.components.MinicashTextButton
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
import androidx.compose.material3.Switch
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
    val purchaseDate: Long,
    /** Día del mes (1 a 31) del pago; null si no se indicó. */
    val paymentDay: Int?,
    /** Días de anticipación del aviso (0 = el mismo día); null si no hay aviso. */
    val reminderDaysBefore: Int?,
    /** Verdadero si la compra usa su propio día de pago en vez del de su institución. */
    val paymentOverride: Boolean
)

private const val CUSTOM_MONTHS = -1
private val PRESET_MONTHS = listOf(3, 6, 9, 12)
private const val LAST_STEP = 2

/**
 * Formulario de alta y edición de compras a meses en tres pasos:
 * 1) datos de la compra, 2) plan de pagos y monto mensual, 3) día de pago y aviso. Con [credit] en `null` es un alta.
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
    val initialOverride = credit?.paymentOverride ?: false
    val initialPaymentDay = credit?.paymentDay?.takeIf { initialOverride }?.toString() ?: ""
    val initialReminder = credit?.reminderDaysBefore?.takeIf { initialOverride } ?: NO_REMINDER

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
    var paymentOverride by rememberSaveable { mutableStateOf(initialOverride) }
    var paymentDayStr by rememberSaveable { mutableStateOf(initialPaymentDay) }
    var reminderDays by rememberSaveable { mutableIntStateOf(initialReminder) }
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
        monthlyAmountExpression != initialMonthly ||
        paymentOverride != initialOverride ||
        paymentDayStr != initialPaymentDay ||
        reminderDays != initialReminder

    val guard = rememberDiscardGuard(isDirty = isDirty, onDismiss = onDismiss)
    val haptics = rememberMinicashHaptics()
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val paymentDay = paymentDayStr.toIntOrNull()
    val paymentInvalid = paymentOverride && paymentDayStr.isNotEmpty() && (paymentDay == null || paymentDay !in 1..31)
    // El aviso solo tiene sentido con un día de pago válido
    val effectiveReminder = if (paymentOverride && paymentDay != null && !paymentInvalid) reminderDays else NO_REMINDER

    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val step1Valid = article.isNotBlank() && selectedInstitution.isNotBlank()
    val step2Valid = totalInstallments > 0 && calculatedMonthlyAmount > 0.0 && !paidInvalid
    val canSubmit = step1Valid && step2Valid && !paymentInvalid

    BackHandler(enabled = step > 0) { step -= 1 }

    GuardedModalBottomSheet(
        guard = guard,
        containerColor = MaterialTheme.colorScheme.sheetBackground,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = MinicashShapes.SheetTop
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
                    text = if (isEdit) stringResource(R.string.credit_form_edit) else stringResource(R.string.credit_form_new),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.ob_step, step + 1, LAST_STEP + 1),
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
                } else if (currentStep == 1) {
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
                } else {
                    PaymentStep(
                        institution = institutions.firstOrNull { it.name.equals(selectedInstitution, ignoreCase = true) },
                        institutionName = selectedInstitution,
                        override = paymentOverride,
                        onOverrideChange = { paymentOverride = it },
                        paymentDayStr = paymentDayStr,
                        onPaymentDayChange = { paymentDayStr = it.filter(Char::isDigit).take(2) },
                        paymentInvalid = paymentInvalid,
                        reminderDays = effectiveReminder,
                        onReminderChange = {
                            reminderDays = it
                            if (it != NO_REMINDER) ensureNotificationPermission()
                        }
                    )
                }
            }

            val save: () -> Unit = {
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
                                            purchaseDate = purchaseDate,
                                            paymentDay = paymentDay?.takeIf { paymentOverride && !paymentInvalid },
                                            reminderDaysBefore = effectiveReminder.takeIf { it != NO_REMINDER },
                                            paymentOverride = paymentOverride
                                        )
                                    )
                                    onDismiss()
                                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (step) {
                    0 -> {
                        MinicashTonalButton(
                            text = stringResource(R.string.common_cancel),
                            onClick = guard::requestDismiss,
                            modifier = Modifier.weight(1f)
                        )
                        MinicashButton(
                            text = stringResource(R.string.common_next),
                            onClick = {
                                haptics.tick()
                                keyboard?.hide()
                                focusManager.clearFocus()
                                step = 1
                            },
                            enabled = step1Valid,
                            modifier = Modifier.weight(1f)
                        )
                        if (isEdit) {
                            MinicashButton(
                                text = stringResource(R.string.common_save),
                                onClick = { save() },
                                enabled = canSubmit,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    1 -> {
                        MinicashTonalButton(
                            text = stringResource(R.string.common_previous),
                            onClick = { step = 0 },
                            modifier = Modifier.weight(1f)
                        )
                        MinicashButton(
                            text = stringResource(R.string.common_next),
                            onClick = {
                                haptics.tick()
                                keyboard?.hide()
                                focusManager.clearFocus()
                                step = 2
                            },
                            enabled = step2Valid,
                            modifier = Modifier.weight(1f)
                        )
                        if (isEdit) {
                            MinicashButton(
                                text = stringResource(R.string.common_save),
                                onClick = { save() },
                                enabled = canSubmit,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    else -> {
                        if (isEdit && onDelete != null) {
                            // Solo ícono: deja todo el ancho restante a los botones principales en cualquier densidad/escala de fuente
                            MinicashOutlinedButton(
                                onClick = { showDeleteConfirm = true },
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.credit_delete), modifier = Modifier.size(20.dp))
                            }
                        }
                        MinicashTonalButton(
                            text = stringResource(R.string.common_previous),
                            onClick = { step = 1 },
                            modifier = Modifier.weight(1f)
                        )
                        MinicashButton(
                            text = if (isEdit) stringResource(R.string.common_save) else stringResource(R.string.credit_save_purchase),
                            onClick = {
                                save()
                            },
                            enabled = canSubmit,
                            modifier = Modifier.weight(1.4f)
                        )
                    }
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
                MinicashTextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { purchaseDate = normalizeUtcToLocalMillis(it) }
                        showDatePicker = false
                    }
                ) { Text(stringResource(R.string.common_accept), fontWeight = FontWeight.Bold) }
            },
            dismissButton = { MinicashTextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.common_cancel)) } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showDeleteConfirm && credit != null && onDelete != null) {
        MinicashAlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.credit_delete), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    stringResource(R.string.credit_delete_text, credit.article),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                MinicashTextButton(
                    onClick = {
                        haptics.heavyOrDelete()
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.common_delete), fontWeight = FontWeight.Bold) }
            },
            dismissButton = { MinicashTextButton(onClick = { showDeleteConfirm = false }) { Text(stringResource(R.string.common_cancel)) } },
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
        appDateFormat("d MMM yyyy", "MMM d, yyyy").format(purchaseDate)
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = article,
            onValueChange = onArticleChange,
            label = { Text(stringResource(R.string.credit_article)) },
            placeholder = { Text(stringResource(R.string.credit_article_hint)) },
            singleLine = true,
            shape = MinicashShapes.Input,
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
            text = selectedInstitution.ifBlank { stringResource(R.string.credit_pick_institution) },
            filled = selectedInstitution.isNotBlank()
        )

        OutlinedTextField(
            value = responsible,
            onValueChange = onResponsibleChange,
            label = { Text(stringResource(R.string.budget_responsible)) },
            placeholder = { Text(stringResource(R.string.credit_responsible_hint)) },
            singleLine = true,
            shape = MinicashShapes.Input,
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
            text = stringResource(R.string.credit_purchase_date, dateLabel),
            filled = true
        )

        OutlinedTextField(
            value = originalPriceStr,
            onValueChange = onOriginalPriceChange,
            label = { Text(stringResource(R.string.credit_cash_price)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            shape = MinicashShapes.Input,
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
            label = { if (it == CUSTOM_MONTHS) AppStrings.get(R.string.credit_months_other) else AppStrings.get(R.string.credit_months_short, it) }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = paidInstallmentsStr,
                onValueChange = { onPaidChange(it.filter(Char::isDigit).take(3)) },
                label = { Text(stringResource(R.string.credit_already_paid)) },
                isError = paidInvalid,
                supportingText = if (paidInvalid) {
                    { Text(stringResource(R.string.credit_paid_range, totalInstallments)) }
                } else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = MinicashShapes.Input,
                modifier = Modifier.weight(1f)
            )
            if (isCustomMonths) {
                OutlinedTextField(
                    value = totalInstallmentsStr,
                    onValueChange = { onTotalChange(it.filter(Char::isDigit).take(3)) },
                    label = { Text(stringResource(R.string.credit_total_term)) },
                    suffix = { Text("meses", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = MinicashShapes.Input,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MinicashShapes.Card,
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
                        text = stringResource(R.string.credit_monthly_amount),
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
                    style = MaterialTheme.typography.headlineSmall.copy(fontFeatureSettings = "tnum"),
                    fontWeight = FontWeight.ExtraBold,
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

/** Paso 3: muestra el pago heredado de la institución y permite usar otro día solo para esta compra. */
@Composable
private fun PaymentStep(
    institution: CreditInstitutionEntity?,
    institutionName: String,
    override: Boolean,
    onOverrideChange: (Boolean) -> Unit,
    paymentDayStr: String,
    onPaymentDayChange: (String) -> Unit,
    paymentInvalid: Boolean,
    reminderDays: Int,
    onReminderChange: (Int) -> Unit
) {
    val inheritedDay = institution?.paymentDay
    val inheritedReminder = institution?.reminderDaysBefore
    val name = institutionName.ifBlank { stringResource(R.string.credit_the_institution) }
    val inheritedText = when {
        inheritedDay == null -> stringResource(R.string.credit_inherit_no_day, name)
        inheritedReminder == null -> stringResource(R.string.credit_inherit_no_reminder, name, inheritedDay)
        inheritedReminder == 0 -> stringResource(R.string.credit_inherit_same_day, name, inheritedDay)
        else -> pluralStringResource(R.plurals.credit_inherit_days_before, inheritedReminder, name, inheritedDay, inheritedReminder)
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (!override) {
            Text(
                text = inheritedText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.credit_use_other_day),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Switch(checked = override, onCheckedChange = onOverrideChange)
        }

        AnimatedVisibility(visible = override) {
            PaymentDayFields(
                paymentDayStr = paymentDayStr,
                onPaymentDayChange = onPaymentDayChange,
                paymentInvalid = paymentInvalid,
                reminderDays = reminderDays,
                onReminderChange = onReminderChange
            )
        }
    }
}
