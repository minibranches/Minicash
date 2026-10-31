package com.uramos.minicash.ui.screens.transactions

import com.uramos.minicash.ui.components.MinicashTextButton
import com.uramos.minicash.ui.components.MinicashFilledButton
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Category
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uramos.minicash.data.local.entities.AccountEntity
import com.uramos.minicash.data.local.entities.CategoryEntity
import com.uramos.minicash.data.local.entities.TransactionType
import com.uramos.minicash.ui.theme.MinicashShapes
import com.uramos.minicash.ui.theme.MinicashThemeExtensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsFilterSheet(
    selectedTypeFilter: TransactionType?,
    accounts: List<AccountEntity>,
    selectedAccountId: Long?,
    categories: List<CategoryEntity>,
    selectedCategoryIds: Set<Long>,
    onTypeFilterSelect: (TransactionType?) -> Unit,
    onSelectAccountFilter: (Long?) -> Unit,
    onToggleCategory: (Long) -> Unit,
    onClearCategories: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val spacing = MinicashThemeExtensions.spacing
    val haptic = LocalHapticFeedback.current
    var showCategoryPicker by remember { mutableStateOf(false) }
    val hasActiveFilters = selectedTypeFilter != null || selectedAccountId != null || selectedCategoryIds.isNotEmpty()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.standard)
                .padding(bottom = spacing.large),
            verticalArrangement = Arrangement.spacedBy(spacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filtros de movimientos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (hasActiveFilters) {
                    MinicashTextButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onTypeFilterSelect(null)
                            onSelectAccountFilter(null)
                            onClearCategories()
                        }
                    ) {
                        Text(
                            text = "Limpiar filtros",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 1. Filtro por Tipo de Movimiento
            Text(
                text = "Tipo de movimiento",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                val types = listOf(
                    null to "Todos",
                    TransactionType.EXPENSE to "Gasto",
                    TransactionType.INCOME to "Ingreso",
                    TransactionType.TRANSFER to "Transferencia"
                )
                types.forEachIndexed { index, (type, label) ->
                    val isSelected = selectedTypeFilter == type
                    val shape = when (index) {
                        0 -> RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp, topEnd = 4.dp, bottomEnd = 4.dp)
                        types.size - 1 -> RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 20.dp, bottomEnd = 20.dp)
                        else -> RoundedCornerShape(4.dp)
                    }
                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            onTypeFilterSelect(type)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        shape = shape,
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.extraSmall))

            // 2. Filtro por Cuenta
            if (accounts.isNotEmpty()) {
                Text(
                    text = "Cuenta",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.small)
                ) {
                    item {
                        val isSelected = selectedAccountId == null
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSelectAccountFilter(null)
                            },
                            label = { Text("Todas las cuentas") },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            shape = CircleShape,
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                    }
                    items(accounts, key = { it.id }) { account ->
                        val isSelected = selectedAccountId == account.id
                        val accountColor = remember(account.colorHex) {
                            try {
                                Color(android.graphics.Color.parseColor(account.colorHex))
                            } catch (e: Exception) {
                                null
                            }
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSelectAccountFilter(account.id)
                            },
                            label = { Text(account.name) },
                            leadingIcon = {
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                color = accountColor ?: MaterialTheme.colorScheme.primary,
                                                shape = CircleShape
                                            )
                                    )
                                }
                            },
                            shape = CircleShape,
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                    }
                }
            }

            // 3. Filtro por Categoría (varias a la vez; solo las del tipo elegido, las transferencias no tienen categoría)
            val visibleCategories = categories.filter { selectedTypeFilter == null || it.type == selectedTypeFilter }
            if (visibleCategories.isNotEmpty()) {
                Text(
                    text = "Categoría",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    onClick = { showCategoryPicker = true },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = when (selectedCategoryIds.size) {
                                0 -> "Todas las categorías"
                                1 -> "1 categoría seleccionada"
                                else -> "${selectedCategoryIds.size} categorías seleccionadas"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                val selectedCategories = categories.filter { it.id in selectedCategoryIds }
                if (selectedCategories.isNotEmpty()) {
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.small),
                        verticalArrangement = Arrangement.spacedBy(spacing.small)
                    ) {
                        selectedCategories.forEach { category ->
                            InputChip(
                                selected = true,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onToggleCategory(category.id)
                                },
                                label = { Text(category.name) },
                                trailingIcon = {
                                    Icon(Icons.Default.Close, contentDescription = "Quitar", modifier = Modifier.size(16.dp))
                                },
                                shape = CircleShape
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(spacing.small))

            // Botón Cerrar / Aplicar
            MinicashFilledButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Text(
                    text = "Aplicar filtros",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showCategoryPicker) {
        CategoryPickerSheet(
            categories = categories.filter { selectedTypeFilter == null || it.type == selectedTypeFilter },
            selectedIds = selectedCategoryIds,
            onToggle = onToggleCategory,
            onClear = onClearCategories,
            onDismiss = { showCategoryPicker = false }
        )
    }
}
