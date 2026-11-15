package com.uramos.minicash.ui.transactions

import com.uramos.minicash.ui.components.GuardedModalBottomSheet
import com.uramos.minicash.ui.components.MinicashAlertDialog
import com.uramos.minicash.ui.components.MinicashSegmentedChoice
import com.uramos.minicash.ui.components.MinicashTextButton
import com.uramos.minicash.ui.components.MinicashFilledButton
import com.uramos.minicash.ui.components.MinicashIconButton
import com.uramos.minicash.ui.components.MinicashOutlinedButton
import com.uramos.minicash.ui.components.DiscardChangesDialog
import com.uramos.minicash.ui.components.rememberDiscardGuard
import com.uramos.minicash.ui.utils.normalizeUtcToLocalMillis

import com.uramos.minicash.ui.utils.asIconTint

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.snap
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.uramos.minicash.ui.utils.rememberMinicashHaptics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import com.uramos.minicash.ui.utils.hideKeyboardFully
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.uramos.minicash.data.local.entities.AccountEntity
import com.uramos.minicash.data.local.entities.CategoryEntity
import com.uramos.minicash.data.local.entities.TransactionEntity
import com.uramos.minicash.data.local.entities.TransactionType
import com.uramos.minicash.ui.theme.LocalAnimationRestriction
import com.uramos.minicash.ui.theme.MinicashShapes
import com.uramos.minicash.ui.theme.MinicashThemeExtensions
import com.uramos.minicash.ui.transactions.AccountSelectionSheet
import com.uramos.minicash.ui.transactions.CategorySelectionSheet
import com.uramos.minicash.ui.transactions.KeypadButton
import com.uramos.minicash.ui.utils.getCategoryIcon
import com.uramos.minicash.ui.utils.getIconVectorByName
import com.uramos.minicash.ui.utils.toCurrencyFormat
import java.util.Locale
import kotlin.math.roundToLong

private fun evaluateMathExpression(expression: String): Double {
    if (expression.isBlank()) return 0.0
    val clean = expression.replace('×', '*').replace('÷', '/').replace(',', '.').trim()
    return runCatching {
        val tokenRegex = Regex("(\\d+(?:\\.\\d+)?|[+\\-*\\/])")
        val tokens = tokenRegex.findAll(clean).map { it.value }.toList()
        if (tokens.isEmpty()) return@runCatching 0.0

        var total = tokens[0].toDoubleOrNull() ?: 0.0
        var i = 1
        while (i < tokens.size - 1) {
            val op = tokens[i]
            val nextVal = tokens[i + 1].toDoubleOrNull()
            if (nextVal != null) {
                when (op) {
                    "+" -> total += nextVal
                    "-" -> total -= nextVal
                    "*" -> total *= nextVal
                    "/" -> if (nextVal != 0.0) total /= nextVal
                }
                i += 2
            } else {
                i += 1
            }
        }
        if (total.isNaN() || total.isInfinite()) 0.0 else total
    }.getOrDefault(0.0)
}

private fun onKeypadPressHandler(currentExpr: String, key: String): Pair<String, Double> {
    val newExpr = when (key) {
        "⌫" -> {
            if (currentExpr.endsWith(" ")) {
                currentExpr.trimEnd().dropLast(1).trimEnd()
            } else if (currentExpr.isNotEmpty()) {
                currentExpr.dropLast(1)
            } else {
                ""
            }
        }
        "C" -> ""
        "+", "-", "×", "÷" -> {
            val trimmed = currentExpr.trim()
            if (trimmed.isEmpty()) {
                ""
            } else {
                val lastChar = trimmed.last().toString()
                if (lastChar in listOf("+", "-", "×", "÷")) {
                    trimmed.dropLast(1).trim() + " $key "
                } else {
                    "$trimmed $key "
                }
            }
        }
        "." -> {
            val tokens = currentExpr.split(" ")
            val lastToken = tokens.lastOrNull() ?: ""
            if (lastToken.contains(".")) {
                currentExpr
            } else if (lastToken.isEmpty() || lastToken in listOf("+", "-", "×", "÷")) {
                if (currentExpr.endsWith(" ")) "${currentExpr}0." else "${currentExpr} 0."
            } else {
                "$currentExpr."
            }
        }
        else -> {
            if (currentExpr == "0" && key != "0") {
                key
            } else if (currentExpr == "0" && key == "0") {
                "0"
            } else {
                currentExpr + key
            }
        }
    }
    val calc = evaluateMathExpression(newExpr)
    return Pair(newExpr, calc)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EditTransactionSheet(
    transaction: TransactionEntity,
    accounts: List<AccountEntity>,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (original: TransactionEntity, updated: TransactionEntity) -> Unit,
    onCreateCategory: (name: String, type: TransactionType, onCreated: (Long) -> Unit) -> Unit = { _, _, _ -> },
    onDeleteTransaction: ((TransactionEntity) -> Unit)? = null
) {
    val spacing = MinicashThemeExtensions.spacing
    val haptics = rememberMinicashHaptics()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val view = androidx.compose.ui.platform.LocalView.current
    var showCategorySheet by remember { mutableStateOf(false) }
    var showAccountSheet by remember { mutableStateOf(false) }
    var pickingAccountForDestination by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showNumpad by remember { mutableStateOf(true) }

    val conceptFocusRequester = remember { FocusRequester() }
    val noteFocusRequester = remember { FocusRequester() }

    val isRestricted = LocalAnimationRestriction.current
    val motionScheme = MaterialTheme.motionScheme

    var type by remember(transaction) { mutableStateOf(transaction.type) }

    val initialAmountDouble = remember(transaction) { transaction.amount / 100.0 }
    var amountExpression by remember(transaction) {
        mutableStateOf(if (transaction.amount % 100 == 0L) (transaction.amount / 100).toString() else String.format(Locale.US, "%.2f", initialAmountDouble))
    }
    var calculatedAmount by remember(transaction) { mutableDoubleStateOf(initialAmountDouble) }

    val parts = remember(transaction.note) {
        val fullNote = transaction.note ?: ""
        if (fullNote.contains(" • ")) {
            val p = fullNote.split(" • ", limit = 2)
            Pair(p[0], p[1])
        } else {
            Pair(fullNote, "")
        }
    }

    var conceptText by remember(transaction) { mutableStateOf(parts.first) }
    var noteText by remember(transaction) { mutableStateOf(parts.second) }
    var selectedAccountId by remember(transaction) { mutableStateOf<Long?>(transaction.sourceAccountId) }
    var selectedDestinationAccountId by remember(transaction) { mutableStateOf<Long?>(transaction.destinationAccountId) }
    var selectedCategoryId by remember(transaction) { mutableStateOf<Long?>(transaction.categoryId) }
    var selectedDateMillis by remember(transaction) { mutableLongStateOf(transaction.date) }

    val selectedAccount = remember(accounts, selectedAccountId) {
        accounts.firstOrNull { it.id == selectedAccountId } ?: accounts.firstOrNull()
    }
    val selectedDestAccount = remember(accounts, selectedDestinationAccountId, selectedAccountId) {
        accounts.firstOrNull { it.id == selectedDestinationAccountId && it.id != selectedAccount?.id }
            ?: accounts.firstOrNull { it.id != selectedAccount?.id }
    }
    val selectedCategory = remember(categories, selectedCategoryId) {
        categories.firstOrNull { it.id == selectedCategoryId }
    }

    val isTransfer = type == TransactionType.TRANSFER
    val filteredCategories = remember(categories, type) {
        categories.filter { it.type == type }
    }

    val calculatedCents = (calculatedAmount * 100.0).roundToLong()
    val rawAmount = if (calculatedAmount > 0.0) calculatedCents else amountExpression.toDoubleOrNull()?.let { (it * 100.0).roundToLong() } ?: 0L

    val canSave = if (isTransfer) {
        rawAmount > 0L &&
        selectedAccount != null &&
        selectedDestAccount != null &&
        selectedAccount.id != selectedDestAccount.id
    } else {
        rawAmount > 0L &&
        selectedAccount != null &&
        selectedCategory != null &&
        conceptText.isNotBlank()
    }

    val initialAmountExpression = remember(transaction) { if (transaction.amount % 100 == 0L) (transaction.amount / 100).toString() else String.format(Locale.US, "%.2f", transaction.amount / 100.0) }
    val guard = rememberDiscardGuard(isDirty = type != transaction.type || amountExpression != initialAmountExpression || conceptText != parts.first || noteText != parts.second || selectedAccountId != transaction.sourceAccountId || selectedDestinationAccountId != transaction.destinationAccountId || selectedCategoryId != transaction.categoryId || selectedDateMillis != transaction.date, onDismiss = onDismiss)
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
                .padding(horizontal = spacing.standard)
                .padding(bottom = spacing.standard),
            verticalArrangement = Arrangement.spacedBy(spacing.medium)
        ) {
            // Cabecera: Título, Selector de Fecha en píldora e Ícono de Eliminar
            val dateText = remember(selectedDateMillis) {
                com.uramos.minicash.ui.utils.formatTransactionDate(selectedDateMillis)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Editar movimiento",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        onClick = { showDatePicker = true },
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
                                text = dateText,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    if (onDeleteTransaction != null) {
                        MinicashIconButton(onClick = {
                            haptics.heavyOrDelete()
                            onDeleteTransaction(transaction)
                            onDismiss()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar movimiento",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Cifra del Importe Destacada
            val amountColor = when (type) {
                TransactionType.INCOME -> MinicashThemeExtensions.financialColors.income
                TransactionType.EXPENSE -> MinicashThemeExtensions.financialColors.expense
                TransactionType.TRANSFER -> MinicashThemeExtensions.financialColors.transfer
            }

            val amountPrefix = when (type) {
                TransactionType.INCOME -> "+"
                TransactionType.EXPENSE -> "-"
                TransactionType.TRANSFER -> ""
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        showNumpad = true
                    },
                shape = MinicashShapes.ContainerLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = spacing.medium, horizontal = spacing.standard),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = if (amountExpression.isNotBlank()) amountExpression else "Toca para ingresar monto",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (rawAmount == 0L) rawAmount.toCurrencyFormat() else "$amountPrefix${rawAmount.toCurrencyFormat()}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (rawAmount == 0L) MinicashThemeExtensions.financialColors.neutral else amountColor
                    )
                }
            }

            // Selector Segmentado: Gasto, Ingreso, Transferencia
            MinicashSegmentedChoice(
                options = listOf(TransactionType.EXPENSE, TransactionType.INCOME, TransactionType.TRANSFER),
                isSelected = { type == it },
                onSelect = { itemType ->
                    haptics.click()
                    type = itemType
                    if (itemType == TransactionType.TRANSFER) {
                        selectedCategoryId = null
                        if (selectedDestinationAccountId == null) {
                            selectedDestinationAccountId = accounts.firstOrNull { it.id != selectedAccountId }?.id
                        }
                    } else {
                        selectedDestinationAccountId = null
                        if (selectedCategoryId == null || categories.none { it.id == selectedCategoryId && it.type == itemType }) {
                            selectedCategoryId = categories.firstOrNull { it.type == itemType }?.id
                        }
                    }
                },
                label = { it.displayName }
            )

            // Campos de Texto para Concepto y Notas (Ocultos en Transferencia)
            AnimatedVisibility(visible = !isTransfer) {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
                    OutlinedTextField(
                        value = conceptText,
                        onValueChange = { conceptText = it },
                        label = { Text("Concepto") },
                        placeholder = { Text("Nombre o concepto del movimiento") },
                        singleLine = true,
                        shape = MinicashShapes.Input,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { hideKeyboardFully(view, focusManager, keyboardController) }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(conceptFocusRequester)
                    )

                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Notas") },
                        placeholder = { Text("Escribe una nota") },
                        singleLine = false,
                        minLines = 2,
                        maxLines = 4,
                        shape = MinicashShapes.Input,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Default
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .imePadding()
                            .focusRequester(noteFocusRequester)
                    )
                }
            }

            // FILA DE SELECTORES (Categoría / Cuenta Destino, Cuenta Origen)
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isTransfer) {
                    // Selector Cuenta Origen ("De: ...")
                    val srcAccColor = remember(selectedAccount) {
                        try {
                            selectedAccount?.colorHex?.let { Color(android.graphics.Color.parseColor(it)) }
                        } catch (e: Exception) {
                            null
                        }
                    }
                    val srcAccIcon = getIconVectorByName(selectedAccount?.iconName ?: "account_balance")

                    Card(
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            pickingAccountForDestination = false
                            showAccountSheet = true
                        },
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = spacing.small),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = srcAccColor?.copy(alpha = 0.2f) ?: MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = srcAccIcon,
                                        contentDescription = null,
                                        tint = srcAccColor ?: MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = "De: ${selectedAccount?.name ?: "Origen"}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Selector Cuenta Destino ("A: ...")
                    val destAccColor = remember(selectedDestAccount) {
                        try {
                            selectedDestAccount?.colorHex?.let { Color(android.graphics.Color.parseColor(it)) }
                        } catch (e: Exception) {
                            null
                        }
                    }
                    val destAccIcon = getIconVectorByName(selectedDestAccount?.iconName ?: "account_balance")

                    Card(
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            pickingAccountForDestination = true
                            showAccountSheet = true
                        },
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = spacing.small),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = destAccColor?.copy(alpha = 0.2f) ?: MaterialTheme.colorScheme.tertiaryContainer,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = destAccIcon,
                                        contentDescription = null,
                                        tint = destAccColor ?: MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = "A: ${selectedDestAccount?.name ?: "Destino"}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    val catColor = remember(selectedCategory) {
                        try {
                            selectedCategory?.colorHex?.let { Color(android.graphics.Color.parseColor(it)) }
                        } catch (e: Exception) {
                            null
                        }
                    }
                    val catIcon = remember(selectedCategory, type) {
                        if (selectedCategory != null) {
                            if (selectedCategory.iconName != "category") getIconVectorByName(selectedCategory.iconName)
                            else getCategoryIcon(selectedCategory.name, type)
                        } else Icons.Default.Category
                    }

                    Card(
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            showCategorySheet = true
                        },
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = spacing.small),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = catColor?.copy(alpha = 0.2f) ?: MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = catIcon,
                                        contentDescription = null,
                                        tint = catColor?.asIconTint() ?: MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = selectedCategory?.name ?: "Categoría",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    val accColor = remember(selectedAccount) {
                        try {
                            selectedAccount?.colorHex?.let { Color(android.graphics.Color.parseColor(it)) }
                        } catch (e: Exception) {
                            null
                        }
                    }
                    val accIcon = getIconVectorByName(selectedAccount?.iconName ?: "account_balance")

                    Card(
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            showAccountSheet = true
                        },
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = spacing.small),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = accColor?.copy(alpha = 0.2f) ?: MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = accIcon,
                                        contentDescription = null,
                                        tint = accColor?.asIconTint() ?: MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = selectedAccount?.name ?: "Cuenta",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // TECLADO NUMÉRICO HOMOLOGADO PBT (3x4 + 4ª Columna de Operadores Deslizable)
            AnimatedVisibility(
                visible = showNumpad,
                enter = if (isRestricted) {
                    fadeIn(animationSpec = snap()) + expandVertically(animationSpec = snap())
                } else {
                    fadeIn(animationSpec = motionScheme.defaultEffectsSpec()) + expandVertically(animationSpec = motionScheme.defaultSpatialSpec())
                },
                exit = if (isRestricted) {
                    fadeOut(animationSpec = snap()) + shrinkVertically(animationSpec = snap())
                } else {
                    fadeOut(animationSpec = motionScheme.defaultEffectsSpec()) + shrinkVertically(animationSpec = motionScheme.defaultSpatialSpec())
                }
            ) {
                var showOperators by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = spacing.extraSmall, bottom = spacing.standard)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { _, dragAmount ->
                                if (dragAmount < -20) showOperators = true
                                else if (dragAmount > 20) showOperators = false
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spacing.small)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.small)
                    ) {
                        val keypadLayout = listOf(
                            listOf("1", "2", "3"),
                            listOf("4", "5", "6"),
                            listOf("7", "8", "9"),
                            listOf(".", "0", "⌫")
                        )

                        Column(
                            modifier = Modifier.weight(3f),
                            verticalArrangement = Arrangement.spacedBy(spacing.small)
                        ) {
                            keypadLayout.forEach { row ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(spacing.small),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    row.forEach { key ->
                                        val isBackspace = key == "⌫"

                                        KeypadButton(
                                            text = key,
                                            onClick = {
                                                haptics.click()
                                                val (newExpr, calc) = onKeypadPressHandler(amountExpression, key)
                                                amountExpression = newExpr
                                                calculatedAmount = calc
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            containerColor = if (isBackspace) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                                            contentColor = if (isBackspace) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                        )
                                    }
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = showOperators,
                            enter = expandHorizontally() + fadeIn(),
                            exit = shrinkHorizontally() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier.width(56.dp),
                                verticalArrangement = Arrangement.spacedBy(spacing.small)
                            ) {
                                listOf("+", "-", "×", "÷").forEach { op ->
                                    KeypadButton(
                                        text = op,
                                        onClick = {
                                            haptics.click()
                                            val (newExpr, calc) = onKeypadPressHandler(amountExpression, op)
                                            amountExpression = newExpr
                                            calculatedAmount = calc
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(spacing.extraSmall))

                    // Botones Guardar Cambios y Eliminar Movimiento
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MinicashFilledButton(
                            onClick = {
                                haptics.confirm()
                                val finalNote = when {
                                    conceptText.isNotBlank() && noteText.isNotBlank() -> "$conceptText • $noteText"
                                    conceptText.isNotBlank() -> conceptText
                                    noteText.isNotBlank() -> noteText
                                    else -> if (type == TransactionType.TRANSFER) "Transferencia entre cuentas" else null
                                }

                                val sourceId = selectedAccountId ?: accounts.firstOrNull()?.id ?: return@MinicashFilledButton

                                val updatedTx = transaction.copy(
                                    type = type,
                                    amount = rawAmount,
                                    note = finalNote,
                                    sourceAccountId = sourceId,
                                    destinationAccountId = if (type == TransactionType.TRANSFER) selectedDestinationAccountId else null,
                                    categoryId = if (type != TransactionType.TRANSFER) selectedCategoryId else null,
                                    date = selectedDateMillis
                                )

                                onSave(transaction, updatedTx)
                                onDismiss()
                            },
                            enabled = canSave,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                        ) {
                            Text(
                                text = "Guardar cambios",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (onDeleteTransaction != null) {
                            MinicashOutlinedButton(
                                onClick = {
                                    haptics.heavyOrDelete()
                                    onDeleteTransaction(transaction)
                                    onDismiss()
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Eliminar movimiento",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                MinicashTextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDateMillis = normalizeUtcToLocalMillis(millis)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                MinicashTextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showCategorySheet) {
        CategorySelectionSheet(
            categories = filteredCategories,
            selectedCategoryId = selectedCategoryId,
            onCategorySelected = { catId ->
                selectedCategoryId = catId
                showCategorySheet = false
            },
            onCreateCategory = { name ->
                onCreateCategory(name, type) { newId ->
                    selectedCategoryId = newId
                    showCategorySheet = false
                }
            },
            onDismiss = { showCategorySheet = false }
        )
    }

    if (showAccountSheet) {
        val candidates = if (pickingAccountForDestination) {
            accounts.filter { it.id != selectedAccount?.id }
        } else {
            accounts
        }
        val targetSelectedId = if (pickingAccountForDestination) selectedDestinationAccountId else selectedAccountId

        AccountSelectionSheet(
            accounts = candidates,
            selectedAccountId = targetSelectedId,
            onAccountSelected = { accId ->
                if (pickingAccountForDestination) {
                    selectedDestinationAccountId = accId
                } else {
                    selectedAccountId = accId
                }
                showAccountSheet = false
            },
            onDismiss = { showAccountSheet = false }
        )
    }

}
