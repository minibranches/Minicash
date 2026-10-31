package com.uramos.minicash.ui.screens.onboarding

import com.uramos.minicash.ui.components.MinicashTextButton
import com.uramos.minicash.ui.components.MinicashFilledButton
import com.uramos.minicash.ui.components.MinicashIconButton
import com.uramos.minicash.ui.utils.toCents
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uramos.minicash.R
import com.uramos.minicash.data.local.entities.AccountType
import com.uramos.minicash.ui.transactions.KeypadButton
import com.uramos.minicash.ui.utils.toCurrencyFormat
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onFinished: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Botón Atrás
                if (pagerState.currentPage > 0) {
                    MinicashIconButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Página anterior"
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }

                // Indicador de progreso segmentado
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        val isSelected = pagerState.currentPage == index
                        val width by animateDpAsState(
                            targetValue = if (isSelected) 32.dp else 12.dp,
                            label = "segment_width"
                        )
                        val color by animateColorAsState(
                            targetValue = when {
                                isSelected -> MaterialTheme.colorScheme.primary
                                index < pagerState.currentPage -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
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

                // Botón Omitir
                if (pagerState.currentPage < 2) {
                    MinicashTextButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(2)
                            }
                        }
                    ) {
                        Text("Omitir", fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
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
                    onNameChange = { viewModel.onNameChange(it) },
                    onNext = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        scope.launch { pagerState.animateScrollToPage(1) }
                    }
                )
                1 -> AccountPage(
                    state = state,
                    onAccountNameChange = { viewModel.onAccountNameChange(it) },
                    onAccountTypeChange = { viewModel.onAccountTypeChange(it) },
                    onKeypadPress = { viewModel.onKeypadPress(it) },
                    onNext = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        scope.launch { pagerState.animateScrollToPage(2) }
                    }
                )
                2 -> CategoriesPage(
                    state = state,
                    onToggleExpenseCategory = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.toggleExpenseCategory(it)
                    },
                    onToggleIncomeCategory = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.toggleIncomeCategory(it)
                    },
                    onFinish = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.finishOnboarding(onFinished)
                    }
                )
            }
        }
    }
}

@Composable
private fun WelcomePage(
    state: OnboardingState,
    onNameChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_minicash_coin),
                contentDescription = "Moneda Minicash",
                modifier = Modifier.size(80.dp)
            )

            Text(
                text = "Te damos la bienvenida a Minicash",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Comencemos por personalizar tu espacio financiero local.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.userName,
                onValueChange = onNameChange,
                label = { Text("Tu nombre") },
                placeholder = { Text("Escribe tu nombre") },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 32.dp, top = 24.dp)
        ) {
            MinicashFilledButton(
                onClick = onNext,
                enabled = state.userName.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
            ) {
                Text("Continuar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    text = "Todo permanece en este teléfono. Sin cuenta, sin registro, nada se envía a ningún lado.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun AccountPage(
    state: OnboardingState,
    onAccountNameChange: (String) -> Unit,
    onAccountTypeChange: (AccountType) -> Unit,
    onKeypadPress: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = "Dónde vive tu dinero",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Nombra la cuenta desde la que gastas y di qué hay en ella ahora.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = state.accountName,
                onValueChange = onAccountNameChange,
                label = { Text("Nombre de la cuenta") },
                placeholder = { Text("Escribe el nombre de la cuenta") },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Tipo de cuenta:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(AccountType.entries) { type ->
                    FilterChip(
                        selected = state.accountType == type,
                        onClick = { onAccountTypeChange(type) },
                        shape = CircleShape,
                        label = { Text(type.displayName) }
                    )
                }
            }

            // Tarjeta destacada de Saldo Inicial con Teclado Embebido
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Qué hay en ella ahora",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val balanceDouble = state.accountBalance.toDoubleOrNull() ?: 0.0
                    val balanceCents = balanceDouble.toCents()

                    Text(
                        text = balanceCents.toCurrencyFormat(),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Déjalo en cero si prefieres no decirlo.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Teclado Numérico Embebido
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
                                        containerColor = if (isBackspace) {
                                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                                        } else {
                                            MaterialTheme.colorScheme.surfaceContainerHigh
                                        },
                                        contentColor = if (isBackspace) {
                                            MaterialTheme.colorScheme.onErrorContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(50.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Box(modifier = Modifier.padding(bottom = 32.dp, top = 20.dp)) {
            MinicashFilledButton(
                onClick = onNext,
                enabled = state.accountName.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
            ) {
                Text("Continuar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

data class CategoryItem(val name: String, val icon: ImageVector)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoriesPage(
    state: OnboardingState,
    onToggleExpenseCategory: (String) -> Unit,
    onToggleIncomeCategory: (String) -> Unit,
    onFinish: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Gastos, 1: Ingresos

    val expenseItems = listOf(
        CategoryItem("Comestibles", Icons.Default.ShoppingCart),
        CategoryItem("Transporte", Icons.Default.DirectionsCar),
        CategoryItem("Comer fuera", Icons.Default.Fastfood),
        CategoryItem("Casa/Renta", Icons.Default.Home),
        CategoryItem("Servicios", Icons.AutoMirrored.Filled.ReceiptLong),
        CategoryItem("Compras", Icons.Default.ShoppingBag),
        CategoryItem("Salud", Icons.Default.MedicalServices),
        CategoryItem("Diversión", Icons.Default.SportsEsports)
    )

    val incomeItems = listOf(
        CategoryItem("Nómina/Sueldo", Icons.Default.Work),
        CategoryItem("Rendimientos/Inversiones", Icons.AutoMirrored.Filled.TrendingUp),
        CategoryItem("Reembolsos", Icons.Default.AttachMoney),
        CategoryItem("Venta/Comisiones", Icons.Default.Payments)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = "¿En qué gastas e ingresas?",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Elige tus categorías iniciales para configurar tus catálogos.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text("Gastos (${state.selectedExpenseCategories.size})", fontWeight = FontWeight.Bold)
                }
                SegmentedButton(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text("Ingresos (${state.selectedIncomeCategories.size})", fontWeight = FontWeight.Bold)
                }
            }

            if (selectedTab == 0) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    expenseItems.forEach { item ->
                        val isSelected = state.selectedExpenseCategories.contains(item.name)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onToggleExpenseCategory(item.name) },
                            label = { Text(item.name) },
                            leadingIcon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            shape = CircleShape
                        )
                    }
                }
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    incomeItems.forEach { item ->
                        val isSelected = state.selectedIncomeCategories.contains(item.name)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onToggleIncomeCategory(item.name) },
                            label = { Text(item.name) },
                            leadingIcon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            shape = CircleShape
                        )
                    }
                }
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                val totalSelected = state.selectedExpenseCategories.size + state.selectedIncomeCategories.size
                Text(
                    text = "$totalSelected categorías seleccionadas",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Box(modifier = Modifier.padding(bottom = 32.dp, top = 24.dp)) {
            MinicashFilledButton(
                onClick = onFinish,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
            ) {
                Text("Comenzar a usar Minicash", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}