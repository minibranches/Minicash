package com.uramos.minicash.ui.screens.credits

import com.uramos.minicash.ui.utils.rememberMinicashHaptics
import com.uramos.minicash.ui.components.MinicashTextButton
import com.uramos.minicash.ui.components.MinicashFilledButton
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uramos.minicash.ui.theme.MinicashThemeExtensions
import com.uramos.minicash.ui.utils.monthKeyLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditsFilterSheet(
    selectedResponsible: String?,
    responsibles: List<String>,
    selectedInstitution: String?,
    institutions: List<String>,
    selectedMonth: String?,
    months: List<String>,
    onSelectMonth: (String?) -> Unit,
    onSelectResponsible: (String?) -> Unit,
    onSelectInstitution: (String?) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val spacing = MinicashThemeExtensions.spacing
    val haptics = rememberMinicashHaptics()
    val hasActiveFilters = selectedResponsible != null || selectedInstitution != null || selectedMonth != null

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
                    text = "Filtros de créditos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (hasActiveFilters) {
                    MinicashTextButton(
                        onClick = {
                            haptics.tick()
                            onClearFilters()
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

            // 1. Filtro por Responsable
            Text(
                text = "Responsable",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.small)
            ) {
                item {
                    val isSelected = selectedResponsible == null
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            haptics.tick()
                            onSelectResponsible(null)
                        },
                        label = { Text("Todos") },
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
                items(responsibles) { resp ->
                    val isSelected = selectedResponsible == resp
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            haptics.tick()
                            onSelectResponsible(resp)
                        },
                        label = { Text(resp) },
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
            }

            Spacer(modifier = Modifier.height(spacing.extraSmall))

            // 2. Filtro por Entidad Bancaria / Institución
            Text(
                text = "Entidad bancaria / Institución",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.small)
            ) {
                item {
                    val isSelected = selectedInstitution == null
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            haptics.tick()
                            onSelectInstitution(null)
                        },
                        label = { Text("Todas") },
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
                items(institutions) { inst ->
                    val isSelected = selectedInstitution == inst
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            haptics.tick()
                            onSelectInstitution(inst)
                        },
                        label = { Text(inst) },
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
            }

            Spacer(modifier = Modifier.height(spacing.extraSmall))

            // 3. Filtro por mes de compra
            Text(
                text = "Mes de compra",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.small)
            ) {
                item {
                    val isSelected = selectedMonth == null
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            haptics.tick()
                            onSelectMonth(null)
                        },
                        label = { Text("Todos") },
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
                items(months) { month ->
                    val isSelected = selectedMonth == month
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            haptics.tick()
                            onSelectMonth(month)
                        },
                        label = { Text(monthKeyLabel(month)) },
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
}
