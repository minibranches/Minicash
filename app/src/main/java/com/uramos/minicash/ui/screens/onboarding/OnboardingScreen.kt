package com.uramos.minicash.ui.screens.onboarding

import com.uramos.minicash.ui.components.MinicashSegmentedChoice
import com.uramos.minicash.ui.utils.setAppLanguage
import com.uramos.minicash.ui.utils.AppLanguage
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.delay
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.layout.imePadding
import com.uramos.minicash.ui.components.MinicashFilterChip
import com.uramos.minicash.ui.theme.cardSurface
import com.uramos.minicash.ui.theme.screenBackground
import com.uramos.minicash.ui.theme.sheetBackground
import com.uramos.minicash.ui.utils.rememberMinicashHaptics
import com.uramos.minicash.ui.components.MinicashLogo
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.uramos.minicash.R
import com.uramos.minicash.data.local.entities.AccountType
import com.uramos.minicash.ui.components.MinicashIconButton
import com.uramos.minicash.ui.components.MinicashPillTabs
import com.uramos.minicash.ui.components.MinicashTextButton
import com.uramos.minicash.ui.theme.LocalAnimationRestriction
import com.uramos.minicash.ui.theme.minicashSpatialSpec
import com.uramos.minicash.ui.transactions.KeypadButton
import com.uramos.minicash.ui.utils.toCents
import com.uramos.minicash.ui.utils.toCurrencyFormat
import kotlin.math.ceil
import kotlinx.coroutines.launch

private const val PAGE_COUNT = 3

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onFinished: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
    val scope = rememberCoroutineScope()
    val haptics = rememberMinicashHaptics()

    val currentPage = pagerState.currentPage
    val isLastPage = currentPage == PAGE_COUNT - 1
    val canContinue = when (currentPage) {
        0 -> state.userName.isNotBlank()
        1 -> state.accountName.isNotBlank()
        else -> true
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        containerColor = MaterialTheme.colorScheme.screenBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentPage > 0) {
                    MinicashIconButton(
                        onClick = { scope.launch { pagerState.animateScrollToPage(currentPage - 1) } }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.ob_previous_page))
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }

                StepDots(currentPage = currentPage)

                if (currentPage < PAGE_COUNT - 1 && canContinue) {
                    MinicashTextButton(
                        onClick = { scope.launch { pagerState.animateScrollToPage(PAGE_COUNT - 1) } }
                    ) {
                        Text(stringResource(R.string.ob_skip), fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.ob_step, currentPage + 1, PAGE_COUNT),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isLastPage) stringResource(R.string.ob_hint_last) else if (canContinue) stringResource(R.string.ob_hint_continue) else stringResource(R.string.ob_hint_incomplete),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                MorphingNextButton(
                    enabled = canContinue,
                    isLast = isLastPage,
                    onClick = {
                        haptics.tick()
                        if (isLastPage) {
                            haptics.heavyOrDelete()
                            viewModel.finishOnboarding(onFinished)
                        } else {
                            scope.launch { pagerState.animateScrollToPage(currentPage + 1) }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { page ->
            when (page) {
                0 -> WelcomePage(
                    state = state,
                    onNameChange = { viewModel.onNameChange(it) }
                )
                1 -> AccountPage(
                    state = state,
                    onAccountNameChange = { viewModel.onAccountNameChange(it) },
                    onAccountTypeChange = { viewModel.onAccountTypeChange(it) },
                    onKeypadPress = { viewModel.onKeypadPress(it) }
                )
                else -> CategoriesPage(
                    state = state,
                    onToggleExpenseCategory = {
                        haptics.tick()
                        viewModel.toggleExpenseCategory(it)
                    },
                    onToggleIncomeCategory = {
                        haptics.tick()
                        viewModel.toggleIncomeCategory(it)
                    }
                )
            }
        }
    }
}

/** Indicador de pasos: el actual se alarga, los anteriores quedan teñidos. */
@Composable
private fun StepDots(currentPage: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(PAGE_COUNT) { index ->
            val isSelected = currentPage == index
            val width by animateDpAsState(
                targetValue = if (isSelected) 32.dp else 12.dp,
                animationSpec = minicashSpatialSpec(),
                label = "segment_width"
            )
            val color by animateColorAsState(
                targetValue = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    index < currentPage -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    else -> MaterialTheme.colorScheme.surfaceContainerHighest
                },
                label = "segment_color"
            )
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(width)
                    .background(color = color, shape = CircleShape)
            )
        }
    }
}

/**
 * Botón de avance que cambia de forma: mientras el paso está incompleto es un cuadrado redondeado que
 * gira despacio; al completarse se asienta en círculo y se tiñe con el color primario.
 */
@Composable
private fun MorphingNextButton(enabled: Boolean, isLast: Boolean, onClick: () -> Unit) {
    val isRestricted = LocalAnimationRestriction.current
    val radius by animateDpAsState(
        targetValue = if (enabled) 30.dp else 16.dp,
        animationSpec = minicashSpatialSpec(),
        label = "next_radius"
    )
    val container by animateColorAsState(
        targetValue = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
        label = "next_container"
    )
    val content by animateColorAsState(
        targetValue = if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "next_content"
    )

    val rotation = remember { Animatable(0f) }
    LaunchedEffect(enabled, isRestricted) {
        if (enabled || isRestricted) {
            // Termina la vuelta en curso hacia la posición recta
            val target = ceil(rotation.value / 90f) * 90f
            rotation.animateTo(target, spring(dampingRatio = 0.6f, stiffness = 200f))
            rotation.snapTo(((target % 360f) + 360f) % 360f)
            if (rotation.value == 0f || rotation.value == 360f) rotation.snapTo(0f)
        } else {
            while (true) {
                rotation.animateTo(rotation.value + 360f, tween(durationMillis = 7000, easing = LinearEasing))
            }
        }
    }

    Box(
        modifier = Modifier.size(72.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .graphicsLayer { rotationZ = rotation.value }
                .background(container, RoundedCornerShape(radius))
        )
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isLast) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = if (isLast) stringResource(R.string.ob_start_cd) else stringResource(R.string.ob_continue_cd),
                tint = content,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun WelcomePage(
    state: OnboardingState,
    onNameChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Ilustración hecha con formas expresivas alrededor de la moneda
        Box(modifier = Modifier.size(220.dp), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, MaterialShapes.SoftBurst.toShape())
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 8.dp)
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.tertiaryContainer, MaterialShapes.Cookie9Sided.toShape())
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = 4.dp, y = (-6).dp)
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, MaterialShapes.Clover4Leaf.toShape())
            )
            MinicashLogo(
                size = 104.dp,
                discColor = MaterialTheme.colorScheme.surface
            )
        }

        Text(
            text = stringResource(R.string.ob_welcome_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = stringResource(R.string.ob_welcome_sub),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Idioma de la app: español de fábrica; el cambio vuelve a crear la pantalla con los textos nuevos
        Text(
            text = "Idioma · Language",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        MinicashSegmentedChoice(
            options = AppLanguage.entries.toList(),
            isSelected = { it == AppLanguage.current() },
            onSelect = { setAppLanguage(it) },
            label = { it.nativeName }
        )

        PillTextField(
            value = state.userName,
            onValueChange = onNameChange,
            placeholder = stringResource(R.string.ob_name_hint),
            modifier = Modifier.fillMaxWidth()
        )

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.cardSurface
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.ob_privacy),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

/** Campo de texto relleno y en píldora, sin borde, igual que los buscadores de la app. */
@Composable
private fun PillTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val requester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        shape = CircleShape,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        modifier = modifier
            .bringIntoViewRequester(requester)
            .onFocusEvent { focus ->
                // Con el teclado abierto el campo quedaba tapado: se espera a que termine de subir y se trae a la vista
                if (focus.isFocused) scope.launch {
                    delay(300)
                    requester.bringIntoView()
                }
            }
    )
}

@Composable
private fun AccountPage(
    state: OnboardingState,
    onAccountNameChange: (String) -> Unit,
    onAccountTypeChange: (AccountType) -> Unit,
    onKeypadPress: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.ob_account_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = stringResource(R.string.ob_account_sub),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        PillTextField(
            value = state.accountName,
            onValueChange = onAccountNameChange,
            placeholder = stringResource(R.string.ob_account_name_hint),
            modifier = Modifier.fillMaxWidth()
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(AccountType.entries) { type ->
                MinicashFilterChip(
                    selected = state.accountType == type,
                    onClick = { onAccountTypeChange(type) },
                    label = { Text(type.displayName) }
                )
            }
        }

        // Saldo inicial con teclado embebido
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.ob_balance_label),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                val balanceCents = (state.accountBalance.toDoubleOrNull() ?: 0.0).toCents()
                Text(
                    text = balanceCents.toCurrencyFormat(),
                    style = MaterialTheme.typography.displaySmall.copy(fontFeatureSettings = "tnum"),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    text = stringResource(R.string.ob_balance_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f)
                )

                val keypadRows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf(".", "0", "⌫")
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    keypadRows.forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row.forEach { key ->
                                val isBackspace = key == "⌫"
                                KeypadButton(
                                    text = key,
                                    onClick = { onKeypadPress(key) },
                                    cornerRadius = 16.dp,
                                    containerColor = if (isBackspace) {
                                        MaterialTheme.colorScheme.errorContainer
                                    } else {
                                        MaterialTheme.colorScheme.screenBackground.copy(alpha = 0.7f)
                                    },
                                    contentColor = if (isBackspace) {
                                        MaterialTheme.colorScheme.onErrorContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

data class CategoryItem(val key: String, @androidx.annotation.StringRes val nameRes: Int, val icon: ImageVector)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoriesPage(
    state: OnboardingState,
    onToggleExpenseCategory: (String) -> Unit,
    onToggleIncomeCategory: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Gastos, 1: Ingresos

    val expenseItems = listOf(
        CategoryItem("groceries", R.string.starter_groceries, Icons.Default.ShoppingCart),
        CategoryItem("transport", R.string.starter_transport, Icons.Default.DirectionsCar),
        CategoryItem("dining", R.string.starter_dining, Icons.Default.Fastfood),
        CategoryItem("rent", R.string.starter_rent, Icons.Default.Home),
        CategoryItem("utilities", R.string.starter_utilities, Icons.AutoMirrored.Filled.ReceiptLong),
        CategoryItem("shopping", R.string.starter_shopping, Icons.Default.ShoppingBag),
        CategoryItem("health", R.string.starter_health, Icons.Default.MedicalServices),
        CategoryItem("fun", R.string.starter_fun, Icons.Default.SportsEsports)
    )
    val incomeItems = listOf(
        CategoryItem("salary", R.string.starter_salary, Icons.Default.Work),
        CategoryItem("investments", R.string.starter_investments, Icons.AutoMirrored.Filled.TrendingUp),
        CategoryItem("refunds", R.string.starter_refunds, Icons.Default.AttachMoney),
        CategoryItem("sales", R.string.starter_sales, Icons.Default.Payments)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.ob_categories_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = stringResource(R.string.ob_categories_sub),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        MinicashPillTabs(
            labels = listOf(
                stringResource(R.string.ob_tab_expenses, state.selectedExpenseCategories.size),
                stringResource(R.string.ob_tab_income, state.selectedIncomeCategories.size)
            ),
            selectedIndex = selectedTab,
            onSelect = { selectedTab = it }
        )

        val items = if (selectedTab == 0) expenseItems else incomeItems
        val selected = if (selectedTab == 0) state.selectedExpenseCategories else state.selectedIncomeCategories
        val onToggle = if (selectedTab == 0) onToggleExpenseCategory else onToggleIncomeCategory

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    pair.forEach { item ->
                        CategoryTile(
                            item = item,
                            selected = item.key in selected,
                            onClick = { onToggle(item.key) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            val totalSelected = state.selectedExpenseCategories.size + state.selectedIncomeCategories.size
            Text(
                text = pluralStringResource(R.plurals.ob_categories_selected, totalSelected, totalSelected),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

/** Mosaico seleccionable: el ícono va en una forma expresiva y la selección tiñe todo el mosaico. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CategoryTile(
    item: CategoryItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val container by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        label = "tile_container"
    )
    val radius by animateDpAsState(
        targetValue = if (selected) 28.dp else 20.dp,
        animationSpec = minicashSpatialSpec(),
        label = "tile_radius"
    )
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(radius),
        color = container,
        modifier = modifier.height(76.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = MaterialShapes.Cookie9Sided.toShape(),
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (selected) Icons.Default.Check else item.icon,
                        contentDescription = null,
                        tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Text(
                text = stringResource(item.nameRes),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
