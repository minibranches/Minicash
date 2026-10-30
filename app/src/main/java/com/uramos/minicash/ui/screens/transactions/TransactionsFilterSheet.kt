package com.uramos.minicash.ui.screens.transactions

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
import com.uramos.minicash.data.local.entities.TransactionType
import com.uramos.minicash.ui.theme.MinicashShapes
import com.uramos.minicash.ui.theme.MinicashThemeExtensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsFilterSheet(
    selectedTypeFilter: TransactionType?,
    accounts: List<AccountEntity>,
    selectedAccountId: Long?,
    onTypeFilterSelect: (TransactionType?) -> Unit,
    onSelectAccountFilter: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val spacing = MinicashThemeExtensions.spacing
    val haptic = LocalHapticFeedback.current
    val hasActiveFilters = selectedTypeFilter != null || selectedAccountId != null

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
                    TextButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onTypeFilterSelect(null)
                            onSelectAccountFilter(null)
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

            Spacer(modifier = Modifier.height(spacing.small))

            // Botón Cerrar / Aplicar
            Button(
                onClick = onDismiss,
                shape = CircleShape,
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
}
