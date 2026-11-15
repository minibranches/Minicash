package com.uramos.minicash.ui.transactions

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.uramos.minicash.ui.components.GuardedModalBottomSheet
import com.uramos.minicash.ui.components.MinicashFilledButton
import com.uramos.minicash.ui.components.MinicashTextButton
import com.uramos.minicash.ui.components.DiscardChangesDialog
import com.uramos.minicash.ui.components.rememberDiscardGuard
import androidx.compose.animation.AnimatedContent
import com.uramos.minicash.ui.utils.asIconTint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import com.uramos.minicash.ui.utils.normalizeUtcToLocalMillis
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.uramos.minicash.ui.utils.rememberMinicashHaptics
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.uramos.minicash.data.local.entities.AccountEntity
import com.uramos.minicash.data.local.entities.CategoryEntity
import com.uramos.minicash.data.local.entities.TransactionType
import com.uramos.minicash.ui.components.CardTonalLevel
import com.uramos.minicash.ui.components.MinicashCard
import com.uramos.minicash.ui.components.MoneyDisplayType
import com.uramos.minicash.ui.components.rememberSegmentedShape
import com.uramos.minicash.ui.theme.LocalAnimationRestriction
import com.uramos.minicash.ui.theme.MinicashShapes
import com.uramos.minicash.ui.theme.MinicashThemeExtensions
import com.uramos.minicash.ui.utils.getCategoryIcon
import com.uramos.minicash.ui.utils.getIconVectorByName
import com.uramos.minicash.ui.utils.toCurrencyFormat
import kotlinx.coroutines.delay
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AddTransactionSheet(
    viewModel: AddTransactionViewModel,
    initialType: TransactionType? = null,
    onDismiss: () -> Unit
) {
    val spacing = MinicashThemeExtensions.spacing
    val state by viewModel.uiState.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val haptics = rememberMinicashHaptics()
    val view = androidx.compose.ui.platform.LocalView.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    

    LaunchedEffect(initialType) {
        if (initialType != null) {
            viewModel.onTypeChange(initialType)
        }
    }

    val isPrefilled = remember {
        state.concept.isNotBlank() || state.amountExpression.isNotBlank() || state.calculatedAmount > 0.0
    }
    var step by remember(initialType) {
        mutableIntStateOf(if (initialType != null || isPrefilled) 1 else 0)
    }

    val handleDismiss = {
        viewModel.resetForm()
        onDismiss()
    }

    var showCategorySheet by remember { mutableStateOf(false) }
    var showAccountSheet by remember { mutableStateOf(false) }
    var pickingAccountForDestination by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showNumpad by remember { mutableStateOf(true) }

    val conceptFocusRequester = remember { FocusRequester() }
    val noteFocusRequester = remember { FocusRequester() }
    var isSuppressingKeyboard by remember { mutableStateOf(false) }

    LaunchedEffect(step, state.type) {
        if (step == 1 && state.type != TransactionType.TRANSFER && state.concept.isBlank()) {
            delay(120)
            conceptFocusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    LaunchedEffect(isSuppressingKeyboard) {
        if (isSuppressingKeyboard) {
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
            val dialogWindow = (view.parent as? DialogWindowProvider)?.window
            if (dialogWindow != null) {
                WindowCompat.getInsetsController(dialogWindow, view).hide(WindowInsetsCompat.Type.ime())
            }
            val imm = view.context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)

            delay(250)
            isSuppressingKeyboard = false
        }
    }

    val isRestricted = LocalAnimationRestriction.current
    val motionScheme = MaterialTheme.motionScheme

    val selectedAccount = remember(accounts, state.selectedAccountId, state.type) {
        accounts.firstOrNull { it.id == state.selectedAccountId } ?: defaultAccountFor(state.type, accounts)
    }
    val selectedDestAccount = remember(accounts, state.selectedDestinationAccountId, state.selectedAccountId) {
        accounts.firstOrNull { it.id == state.selectedDestinationAccountId && it.id != selectedAccount?.id }
            ?: accounts.firstOrNull { it.id != selectedAccount?.id }
    }
    val selectedCategory = remember(categories, state.selectedCategoryId) {
        categories.firstOrNull { it.id == state.selectedCategoryId }
    }

    val isTransfer = state.type == TransactionType.TRANSFER
    val filteredCategories = remember(categories, state.type) {
        categories.filter { it.type == state.type }
    }
    val calculatedCents = (state.calculatedAmount * 100.0).roundToLong()
    val rawAmount = if (state.calculatedAmount > 0.0) calculatedCents else state.amountExpression.toDoubleOrNull()?.let { (it * 100.0).roundToLong() } ?: 0L

    val canSave = if (isTransfer) {
        rawAmount > 0L &&
        selectedAccount != null &&
        selectedDestAccount != null &&
        selectedAccount.id != selectedDestAccount.id
    } else {
        rawAmount > 0L &&
        selectedAccount != null &&
        selectedCategory != null &&
        state.concept.isNotBlank()
    }

    val initialFormSnapshot = remember { Triple(state.concept, state.amountExpression, state.note) }
    val guard = rememberDiscardGuard(isDirty = Triple(state.concept, state.amountExpression, state.note) != initialFormSnapshot, onDismiss = handleDismiss)
    DiscardChangesDialog(guard)
        GuardedModalBottomSheet(
        guard = guard,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        contentWindowInsets = { BottomSheetDefaults.windowInsets },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                if (isRestricted) {
                    fadeIn(animationSpec = snap()) togetherWith fadeOut(animationSpec = snap())
                } else {
                    fadeIn(animationSpec = motionScheme.defaultEffectsSpec()) togetherWith fadeOut(animationSpec = motionScheme.defaultEffectsSpec())
                }
            },
            label = "step_transition"
        ) { currentStep ->
            if (currentStep == 0) {
                // PASO 1: Selector modal de tipo de movimiento (Gasto | Ingreso | Transferencia)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.standard)
                        .padding(bottom = spacing.large),
                    verticalArrangement = Arrangement.spacedBy(spacing.medium),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Selecciona el tipo de movimiento",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(spacing.extraSmall))

                    val financialColors = MinicashThemeExtensions.financialColors

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Tarjeta Gasto
                        Surface(
                            onClick = {
                                haptics.click()
                                viewModel.onTypeChange(TransactionType.EXPENSE)
                                step = 1
                            },
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(spacing.standard),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(spacing.medium)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = financialColors.expenseContainer,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                            contentDescription = null,
                                            tint = financialColors.expense,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "Gasto",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Registrar un egreso o compra",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Tarjeta Ingreso
                        Surface(
                            onClick = {
                                haptics.click()
                                viewModel.onTypeChange(TransactionType.INCOME)
                                step = 1
                            },
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(spacing.standard),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(spacing.medium)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = financialColors.incomeContainer,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                            contentDescription = null,
                                            tint = financialColors.income,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "Ingreso",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Registrar depósito o cobro",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Tarjeta Transferencia
                        Surface(
                            onClick = {
                                haptics.click()
                                viewModel.onTypeChange(TransactionType.TRANSFER)
                                step = 1
                            },
                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(spacing.standard),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(spacing.medium)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = financialColors.transfer.copy(alpha = 0.2f),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.SwapHoriz,
                                            contentDescription = null,
                                            tint = financialColors.transfer,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "Transferencia",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Traspaso entre tus cuentas",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // PASO 2: Formulario de Registro
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding()
                        .padding(horizontal = spacing.standard)
                        .padding(bottom = spacing.standard),
                    verticalArrangement = Arrangement.spacedBy(spacing.medium)
                ) {
                    // Cabecera: Título contextual y Moneda
                    val contextualTitle = when (state.type) {
                        TransactionType.INCOME -> "Nuevo ingreso"
                        TransactionType.TRANSFER -> "Nueva transferencia"
                        else -> "Nuevo gasto"
                    }

                    val dateText = remember(state.selectedDate) {
                        com.uramos.minicash.ui.utils.formatTransactionDate(state.selectedDate)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = contextualTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
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
                    }

                    // Cifra del Importe Destacada
                    val amountColor = when (state.type) {
                        TransactionType.INCOME -> MinicashThemeExtensions.financialColors.income
                        TransactionType.EXPENSE -> MinicashThemeExtensions.financialColors.expense
                        TransactionType.TRANSFER -> MinicashThemeExtensions.financialColors.transfer
                    }

                    val amountPrefix = when (state.type) {
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
                                text = if (state.amountExpression.isNotBlank()) state.amountExpression else "Toca para ingresar monto",
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

                    // Campos de Texto para Concepto y Notas (Ocultos en Transferencia)
                    AnimatedVisibility(visible = !isTransfer) {
                        Column(verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
                            OutlinedTextField(
                                value = state.concept,
                                onValueChange = { viewModel.setConcept(it) },
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
                                    // Cierra el teclado con el mismo mecanismo que las sugerencias: evita que la hoja le devuelva el foco
                                    onDone = { isSuppressingKeyboard = true }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusProperties { canFocus = !isSuppressingKeyboard }
                                    .focusRequester(conceptFocusRequester)
                            )

                            AnimatedVisibility(
                                visible = state.autocompleteSuggestions.isNotEmpty(),
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(vertical = 4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(state.autocompleteSuggestions) { suggestion ->
                                        Surface(
                                            onClick = {
                                                haptics.click()
                                                isSuppressingKeyboard = true
                                                focusManager.clearFocus(force = true)
                                                keyboardController?.hide()
                                                viewModel.applySuggestion(suggestion)
                                            },
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = suggestion.concept,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = " • ${suggestion.categoryName ?: "Sin categoría"} • ${suggestion.accountName}",
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = state.note,
                                onValueChange = { viewModel.setNotes(it) },
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
                                    .focusProperties { canFocus = !isSuppressingKeyboard }
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
                                    haptics.tick()
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
                                    haptics.tick()
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
                            val catIcon = remember(selectedCategory, state.type) {
                                if (selectedCategory != null) {
                                    if (selectedCategory.iconName != "category") getIconVectorByName(selectedCategory.iconName)
                                    else getCategoryIcon(selectedCategory.name, state.type)
                                } else Icons.Default.Category
                            }

                            Card(
                                onClick = {
                                    haptics.tick()
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
                                    haptics.tick()
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
                                // Bloque Numérico 3x4 (Siempre visible)
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
                                                        viewModel.onKeypadPress(key)
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

                                // Bloque de Operadores (4ª Columna Deslizable a la derecha)
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
                                                    viewModel.onKeypadPress(op)
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

                            // Botón Añadir al Pie Ancho Completo
                            MinicashFilledButton(
                                onClick = {
                                    haptics.confirm()
                                    viewModel.saveTransaction(onSuccess = handleDismiss)
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
                                    text = "Añadir",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCategorySheet) {
        CategorySelectionSheet(
            categories = filteredCategories,
            selectedCategoryId = state.selectedCategoryId,
            onCategorySelected = { catId ->
                viewModel.onCategorySelect(catId)
                showCategorySheet = false
            },
            onCreateCategory = { name ->
                viewModel.addCategory(name, state.type)
                showCategorySheet = false
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
        val targetId = if (pickingAccountForDestination) state.selectedDestinationAccountId else state.selectedAccountId

        AccountSelectionSheet(
            accounts = candidates,
            selectedAccountId = targetId,
            onAccountSelected = { accId ->
                if (pickingAccountForDestination) {
                    viewModel.setDestinationAccount(accId)
                } else {
                    viewModel.setSourceAccount(accId)
                }
                showAccountSheet = false
            },
            onDismiss = { showAccountSheet = false }
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.selectedDate
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                MinicashTextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            viewModel.onDateChange(normalizeUtcToLocalMillis(millis))
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
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun KeypadButton(
    text: String = "",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    shape: Shape = RoundedCornerShape(16.dp),
    /** Si se indica, la tecla cambia de esquina [cornerRadius] a [pressedCornerRadius] con resorte al presionarse. */
    cornerRadius: androidx.compose.ui.unit.Dp? = null,
    pressedCornerRadius: androidx.compose.ui.unit.Dp = 28.dp
) {
    val scale = remember { Animatable(1f) }
    val isRestricted = LocalAnimationRestriction.current
    var isPressed by remember { mutableStateOf(false) }
    val animatedRadius by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isPressed) pressedCornerRadius else (cornerRadius ?: 0.dp),
        animationSpec = if (isRestricted) androidx.compose.animation.core.snap() else spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "keypad_corner_morph"
    )
    val keyShape: Shape = if (cornerRadius != null) RoundedCornerShape(animatedRadius) else shape
    val coroutineScope = rememberCoroutineScope()
    val haptics = rememberMinicashHaptics()
    // pointerInput(Unit) no se reinicia: sin esto el clic usaría la expresión de la primera composición.
    val currentOnClick by rememberUpdatedState(onClick)

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        if (!isRestricted) {
                            coroutineScope.launch {
                                scale.animateTo(0.90f, tween(durationMillis = 80))
                            }
                        }
                        tryAwaitRelease()
                        isPressed = false
                        if (!isRestricted) {
                            coroutineScope.launch {
                                scale.animateTo(
                                    1f,
                                    spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                            }
                        }
                    },
                    onTap = {
                        haptics.tick()
                        currentOnClick()
                    }
                )
            }
    ) {
        Surface(
            shape = keyShape,
            color = containerColor,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (icon != null || text == "⌫") {
                    Icon(
                        imageVector = icon ?: Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Borrar",
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFeatureSettings = "tnum"
                        ),
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectionSheet(
    categories: List<CategoryEntity>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long) -> Unit,
    onCreateCategory: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val spacing = MinicashThemeExtensions.spacing

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.standard)
                .padding(bottom = spacing.large),
            verticalArrangement = Arrangement.spacedBy(spacing.standard)
        ) {
            Text(
                text = "Categoría",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (categories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.large),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sin categorías disponibles",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                    modifier = Modifier.heightIn(max = 340.dp)
                ) {
                    itemsIndexed(categories, key = { _, cat -> cat.id }) { index, category ->
                        val itemShape = rememberSegmentedShape(index = index, count = categories.size)
                        val isSelected = selectedCategoryId == category.id
                        val catColor = remember(category.colorHex) {
                            try {
                                Color(android.graphics.Color.parseColor(category.colorHex))
                            } catch (e: Exception) {
                                null
                            }
                        }
                        val catIcon = remember(category) {
                            if (category.iconName != "category") getIconVectorByName(category.iconName)
                            else getCategoryIcon(category.name, category.type)
                        }

                        Surface(
                            onClick = { onCategorySelected(category.id) },
                            shape = itemShape,
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = spacing.standard, vertical = spacing.medium),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(spacing.medium)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = catColor?.copy(alpha = 0.2f) ?: MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = catIcon,
                                                contentDescription = null,
                                                tint = catColor?.asIconTint() ?: MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = category.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }

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

            NewCategoryRow(onCreate = onCreateCategory)
        }
    }
}

/** "+ Nueva categoría": se despliega en un campo con botón "Crear"; el tipo lo define el movimiento en curso. */
@Composable
private fun NewCategoryRow(onCreate: (String) -> Unit) {
    var creating by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    val canCreate = name.isNotBlank()

    if (!creating) {
        Surface(
            onClick = { creating = true },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Nueva categoría",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    } else {
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
        OutlinedTextField(
            value = name,
            onValueChange = { name = it.take(30) },
            label = { Text("Nombre de la categoría") },
            singleLine = true,
            shape = CircleShape,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, capitalization = KeyboardCapitalization.Sentences),
            keyboardActions = KeyboardActions(onDone = { if (canCreate) onCreate(name.trim()) }),
            trailingIcon = {
                MinicashTextButton(onClick = { onCreate(name.trim()) }, enabled = canCreate) {
                    Text("Crear", fontWeight = FontWeight.Bold)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSelectionSheet(
    accounts: List<AccountEntity>,
    selectedAccountId: Long? = null,
    selectedSourceAccountId: Long? = selectedAccountId,
    selectedDestAccountId: Long? = null,
    isTransfer: Boolean = false,
    onAccountSelected: ((Long) -> Unit)? = null,
    onSelectSourceAccount: ((Long) -> Unit)? = onAccountSelected,
    onSelectDestAccount: ((Long) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val spacing = MinicashThemeExtensions.spacing
    val currentSelectedId = selectedSourceAccountId ?: selectedAccountId

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.standard)
                .padding(bottom = spacing.large),
            verticalArrangement = Arrangement.spacedBy(spacing.standard)
        ) {
            Text(
                text = "Cuenta",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (accounts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.large),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sin cuentas disponibles",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                    modifier = Modifier.heightIn(max = 340.dp)
                ) {
                    itemsIndexed(accounts, key = { _, acc -> acc.id }) { index, account ->
                        val itemShape = rememberSegmentedShape(index = index, count = accounts.size)
                        val isSelected = selectedAccountId == account.id
                        val accColor = remember(account.colorHex) {
                            try {
                                Color(android.graphics.Color.parseColor(account.colorHex))
                            } catch (e: Exception) {
                                null
                            }
                        }
                        val accIcon = getIconVectorByName(account.iconName)

                        Surface(
                            onClick = {
                                onSelectSourceAccount?.invoke(account.id)
                                onAccountSelected?.invoke(account.id)
                            },
                            shape = itemShape,
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = spacing.standard, vertical = spacing.medium),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(spacing.medium)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = accColor?.copy(alpha = 0.2f) ?: MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = accIcon,
                                                contentDescription = null,
                                                tint = accColor?.asIconTint() ?: MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = account.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = account.type.displayName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

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
        }
    }
}
