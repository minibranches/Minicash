package com.uramos.minicash.ui.screens.settings

import androidx.compose.ui.res.stringResource
import com.uramos.minicash.R
import com.uramos.minicash.ui.theme.minicashAnimateItem
import com.uramos.minicash.ui.theme.screenBackground
import com.uramos.minicash.ui.theme.MinicashShapes
import com.uramos.minicash.ui.theme.sheetBackground
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
import androidx.compose.ui.text.style.TextOverflow
import com.uramos.minicash.ui.components.ScallopedIcon
import com.uramos.minicash.ui.components.MinicashListRowDefaults


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
        containerColor = MaterialTheme.colorScheme.screenBackground,
        topBar = {
            MinicashSubScreenTopBar(
                title = stringResource(R.string.set_categories),
                onBack = onNavigateBack,
                scrollBehavior = scrollBehavior,
                actions = {
                    MinicashIconButton(
                        onClick = {
                            haptics.tick()
                            showAddCategoryDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.tx_new_category)
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
                    .padding(horizontal = spacing.screen, vertical = spacing.extraSmall),
                placeholder = { Text(stringResource(R.string.cat_search)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        MinicashIconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.tx_clear_search))
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
                labels = listOf(stringResource(R.string.ob_tab_expenses, state.expenseCategories.size), stringResource(R.string.ob_tab_income, state.incomeCategories.size)),
                selectedIndex = selectedTab,
                onSelect = {
                    haptics.tick()
                    selectedTab = it
                },
                modifier = Modifier.padding(horizontal = spacing.screen, vertical = spacing.small)
            )

            // Lista de Categorías
            if (filteredCategories.isEmpty()) {
                MinicashEmptyState(
                    icon = Icons.Default.Category,
                    title = stringResource(R.string.cat_empty_title),
                    subtitle = if (searchQuery.isNotBlank()) stringResource(R.string.cat_empty_search, searchQuery) else stringResource(R.string.cat_empty_sub),
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
                            Text(stringResource(R.string.tx_new_category))
                        }
                    }
                )
            } else {
                LazyColumn(
                    state = fadeListState,
                    modifier = Modifier.fillMaxSize().fadingTopEdge(fadeListState),
                    contentPadding = PaddingValues(
                        top = spacing.small,
                        start = spacing.screen,
                        end = spacing.screen,
                        bottom = 120.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(MinicashListRowDefaults.RowGap)
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
                                haptics.tick()
                                categoryToEditName = category
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(minicashAnimateItem())
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = MinicashListRowDefaults.MinHeight)
                                    .padding(horizontal = MinicashListRowDefaults.HorizontalPadding, vertical = MinicashListRowDefaults.VerticalPadding),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    ScallopedIcon(
                                        icon = icon,
                                        size = MinicashListRowDefaults.IconSize,
                                        container = containerColor,
                                        tint = contentColor
                                    )
                                    Spacer(modifier = Modifier.width(spacing.medium))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = category.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (isExpense) stringResource(R.string.cat_expense_kind) else stringResource(R.string.cat_income_kind),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    MinicashIconButton(
                                        onClick = {
                                            haptics.heavyOrDelete()
                                            categoryToDelete = category
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.cat_delete_cd),
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
                                haptics.tick()
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
                                    text = stringResource(R.string.tx_new_category),
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
            title = { Text(stringResource(R.string.cat_delete_title), fontWeight = FontWeight.Bold) },
            text = {
                Text(stringResource(R.string.cat_delete_text, cat.name))
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
                    Text(stringResource(R.string.common_delete), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                MinicashTextButton(onClick = { categoryToDelete = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
            shape = MinicashShapes.Card
        )
    }
}
