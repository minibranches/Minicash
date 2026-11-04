package com.uramos.minicash.ui.screens.settings

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.uramos.minicash.ui.components.fadingTopEdge
import com.uramos.minicash.ui.components.MinicashSubScreenTopBar
import com.uramos.minicash.ui.components.MinicashAlertDialog
import com.uramos.minicash.ui.components.MinicashPillTabs
import com.uramos.minicash.ui.components.MinicashTextButton
import com.uramos.minicash.ui.components.MinicashFilledButton
import com.uramos.minicash.ui.components.MinicashIconButton
import androidx.compose.foundation.layout.*


import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import com.uramos.minicash.ui.utils.asIconTint
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.uramos.minicash.ui.utils.rememberMinicashHaptics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uramos.minicash.data.local.entities.CategoryEntity
import com.uramos.minicash.data.local.entities.TransactionType
import androidx.compose.foundation.lazy.itemsIndexed
import com.uramos.minicash.ui.components.CardTonalLevel
import com.uramos.minicash.ui.components.MinicashCard
import com.uramos.minicash.ui.components.MinicashEmptyState
import com.uramos.minicash.ui.components.rememberSegmentedShape
import com.uramos.minicash.ui.theme.MinicashThemeExtensions
import com.uramos.minicash.ui.utils.getCategoryIcon
import com.uramos.minicash.ui.utils.getIconVectorByName

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CategoriesManagementScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val spacing = MinicashThemeExtensions.spacing
    val state by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    val haptics = rememberMinicashHaptics()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Gastos, 1: Ingresos
    var searchQuery by remember { mutableStateOf("") }

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var categoryToEditName by remember { mutableStateOf<CategoryEntity?>(null) }
    var categoryToDelete by remember { mutableStateOf<CategoryEntity?>(null) }

    val currentCategories = if (selectedTab == 0) state.expenseCategories else state.incomeCategories
    val filteredCategories = remember(currentCategories, searchQuery) {
        if (searchQuery.isBlank()) {
            currentCategories
        } else {
            currentCategories.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val fadeListState = rememberLazyListState()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        topBar = {
            MinicashSubScreenTopBar(
                title = "Categorías",
                onBack = onNavigateBack,
                scrollBehavior = scrollBehavior,
                actions = {
                    MinicashIconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showAddCategoryDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Nueva categoría"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Barra de Búsqueda Cápsula
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.standard, vertical = spacing.extraSmall),
                placeholder = { Text("Buscar categorías...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        MinicashIconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                        }
                    }
                },
                singleLine = true,
                shape = CircleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            )

            // Selector Segmentado de Gastos vs Ingresos
            MinicashPillTabs(
                labels = listOf("Gastos (${state.expenseCategories.size})", "Ingresos (${state.incomeCategories.size})"),
                selectedIndex = selectedTab,
                onSelect = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    selectedTab = it
                },
                modifier = Modifier.padding(horizontal = spacing.standard, vertical = spacing.small)
            )

            // Lista de Categorías
            if (filteredCategories.isEmpty()) {
                MinicashEmptyState(
                    icon = Icons.Default.Category,
                    title = "Sin categorías",
                    subtitle = if (searchQuery.isNotBlank()) "No se encontraron categorías con '$searchQuery'." else "Organiza tus movimientos creando una nueva categoría.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = spacing.hero),
                    actionButton = {
                        MinicashFilledButton(
                            onClick = { showAddCategoryDialog = true },
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .heightIn(min = 48.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Nueva categoría")
                        }
                    }
                )
            } else {
                LazyColumn(
                    state = fadeListState,
                    modifier = Modifier.fillMaxSize().fadingTopEdge(fadeListState),
                    contentPadding = PaddingValues(
                        top = spacing.small,
                        start = spacing.standard,
                        end = spacing.standard,
                        bottom = 120.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    itemsIndexed(filteredCategories, key = { _, cat -> cat.id }) { index, category ->
                        val itemShape = rememberSegmentedShape(index = index, count = filteredCategories.size)
                        val icon = if (category.iconName != "category") getIconVectorByName(category.iconName) else getCategoryIcon(category.name, category.type)
                        val isExpense = category.type == TransactionType.EXPENSE
                        val categoryIconShape = MaterialShapes.Cookie9Sided.toShape()
                        val customColor = try { Color(android.graphics.Color.parseColor(category.colorHex)) } catch (e: Exception) { null }
                        val containerColor = customColor?.copy(alpha = 0.2f)
                            ?: if (isExpense) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                        val contentColor = customColor?.asIconTint()
                            ?: if (isExpense) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer

                        MinicashCard(
                            level = CardTonalLevel.LOW,
                            shape = itemShape,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                categoryToEditName = category
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(spacing.standard),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = categoryIconShape,
                                        color = containerColor,
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = contentColor,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(spacing.medium))
                                    Column(verticalArrangement = Arrangement.spacedBy(spacing.extraSmall)) {
                                        Text(
                                            text = category.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isExpense) "Categoría de gasto" else "Categoría de ingreso",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    MinicashIconButton(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            categoryToDelete = category
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar categoría",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(14.dp))
                        MinicashCard(
                            level = CardTonalLevel.HIGH,
                            shape = CircleShape,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showAddCategoryDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(spacing.standard),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(spacing.small))
                                Text(
                                    text = "Nueva categoría",
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
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            initialType = if (selectedTab == 0) TransactionType.EXPENSE else TransactionType.INCOME,
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { name, type, colorHex, iconName ->
                viewModel.addCategory(name, type, colorHex, iconName)
                showAddCategoryDialog = false
            }
        )
    }

    if (categoryToEditName != null) {
        val cat = categoryToEditName!!
        EditCategoryDialog(
            category = cat,
            onDismiss = { categoryToEditName = null },
            onConfirm = { newName, colorHex, iconName ->
                viewModel.updateCategory(cat, newName, colorHex, iconName)
                categoryToEditName = null
            }
        )
    }

    if (categoryToDelete != null) {
        val cat = categoryToDelete!!
        MinicashAlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("¿Eliminar categoría?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Esta acción eliminará la categoría '${cat.name}'. Los movimientos asociados no se borrarán y pasarán a estar sin categoría asignada.")
            },
            confirmButton = {
                MinicashFilledButton(
                    onClick = {
                        haptics.heavyOrDelete()
                        viewModel.deleteCategory(cat)
                        categoryToDelete = null
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
                MinicashTextButton(onClick = { categoryToDelete = null }) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }
}
