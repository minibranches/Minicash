package com.uramos.minicash.ui.screens.settings

import com.uramos.minicash.ui.components.MinicashOutlinedButton
import com.uramos.minicash.ui.components.MinicashTonalButton
import com.uramos.minicash.ui.components.MinicashButton
import com.uramos.minicash.ui.components.DiscardChangesDialog
import com.uramos.minicash.ui.components.rememberDiscardGuard
import com.uramos.minicash.ui.utils.asIconTint
import com.uramos.minicash.ui.utils.toCents
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.uramos.minicash.data.local.entities.AccountEntity
import com.uramos.minicash.data.local.entities.AccountType
import com.uramos.minicash.data.local.entities.CategoryEntity
import com.uramos.minicash.data.local.entities.TransactionType
import com.uramos.minicash.ui.components.IconSelectionSheet
import com.uramos.minicash.ui.components.MinicashColorPicker
import com.uramos.minicash.ui.theme.MinicashShapes
import com.uramos.minicash.ui.utils.MinicashColorUtils
import com.uramos.minicash.ui.utils.getIconVectorByName
import java.util.Locale

@Composable
fun ColorAndIconSelector(
    selectedColorHex: String,
    onColorSelected: (String) -> Unit,
    selectedIconName: String,
    onIconSelected: (String) -> Unit
) {
    var showIconSheet by remember { mutableStateOf(false) }
    val iconVector = getIconVectorByName(selectedIconName)
    val color = MinicashColorUtils.parseHexColor(selectedColorHex, MaterialTheme.colorScheme.primary)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Color distintivo:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        MinicashColorPicker(
            selectedColorHex = selectedColorHex,
            onColorSelected = onColorSelected
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text("Icono:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        Surface(
            onClick = { showIconSheet = true },
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
                Surface(
                    shape = CircleShape,
                    color = color.copy(alpha = 0.2f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = color.asIconTint(),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Seleccionar icono",
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
    }

    if (showIconSheet) {
        IconSelectionSheet(
            selectedIconName = selectedIconName,
            onIconSelected = onIconSelected,
            onDismiss = { showIconSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAccountSheet(
    account: AccountEntity,
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: AccountType, balanceCents: Long, colorHex: String, iconName: String) -> Unit,
    onDelete: () -> Unit
) {
    

    var name by remember(account) { mutableStateOf(account.name) }
    var balanceText by remember(account) { mutableStateOf(initialAmountStr(account.balance)) }
    var selectedType by remember(account) { mutableStateOf(account.type) }
    var selectedColorHex by remember(account) { mutableStateOf(account.colorHex) }
    var selectedIconName by remember(account) { mutableStateOf(account.iconName) }

    val guard = rememberDiscardGuard(isDirty = name != account.name || balanceText != initialAmountStr(account.balance) || selectedType != account.type || selectedColorHex != account.colorHex || selectedIconName != account.iconName, onDismiss = onDismiss)
    DiscardChangesDialog(guard)
        ModalBottomSheet(
        onDismissRequest = guard::requestDismiss,
        sheetState = guard.sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Editar cuenta",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre de la cuenta") },
                placeholder = { Text("Escribe el nombre de la cuenta") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = balanceText,
                onValueChange = { balanceText = it },
                label = { Text("Saldo actual") },
                placeholder = { Text("Ingresa el saldo actual") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Tipo de cuenta:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(AccountType.entries) { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type.displayName) },
                        shape = CircleShape
                    )
                }
            }

            ColorAndIconSelector(
                selectedColorHex = selectedColorHex,
                onColorSelected = { selectedColorHex = it },
                selectedIconName = selectedIconName,
                onIconSelected = { selectedIconName = it }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            MinicashOutlinedButton(
                onClick = onDelete,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Eliminar cuenta")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MinicashTonalButton(text = "Cancelar", onClick = guard::requestDismiss, modifier = Modifier.weight(1f))
                MinicashButton(
                    text = "Guardar",
                    onClick = {
                        val parsedDouble = balanceText.toDoubleOrNull() ?: 0.0
                        val balanceCents = parsedDouble.toCents()
                        onConfirm(name.trim(), selectedType, balanceCents, selectedColorHex, selectedIconName)
                    },
                    enabled = name.isNotBlank() && balanceText.toDoubleOrNull() != null,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun EditAccountDialog(
    account: AccountEntity,
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: AccountType, balanceCents: Long, colorHex: String, iconName: String) -> Unit,
    onDelete: () -> Unit
) = EditAccountSheet(
    account = account,
    onDismiss = onDismiss,
    onConfirm = onConfirm,
    onDelete = onDelete
)

private fun initialAmountStr(cents: Long): String {
    return if (cents % 100 == 0L) {
        (cents / 100).toString()
    } else {
        String.format(Locale.US, "%.2f", cents / 100.0)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCategorySheet(
    category: CategoryEntity,
    onDismiss: () -> Unit,
    onConfirm: (newName: String, colorHex: String, iconName: String) -> Unit
) {
    

    var name by remember(category) { mutableStateOf(category.name) }
    var selectedColorHex by remember(category) { mutableStateOf(category.colorHex) }
    var selectedIconName by remember(category) { mutableStateOf(category.iconName) }

    val guard = rememberDiscardGuard(isDirty = name != category.name || selectedColorHex != category.colorHex || selectedIconName != category.iconName, onDismiss = onDismiss)
    DiscardChangesDialog(guard)
        ModalBottomSheet(
        onDismissRequest = guard::requestDismiss,
        sheetState = guard.sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Editar categoría",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre de la categoría") },
                placeholder = { Text("Escribe el nombre de la categoría") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            ColorAndIconSelector(
                selectedColorHex = selectedColorHex,
                onColorSelected = { selectedColorHex = it },
                selectedIconName = selectedIconName,
                onIconSelected = { selectedIconName = it }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MinicashTonalButton(text = "Cancelar", onClick = guard::requestDismiss, modifier = Modifier.weight(1f))
                MinicashButton(
                    text = "Guardar",
                    onClick = { onConfirm(name.trim(), selectedColorHex, selectedIconName) },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun EditCategoryDialog(
    category: CategoryEntity,
    onDismiss: () -> Unit,
    onConfirm: (newName: String, colorHex: String, iconName: String) -> Unit
) = EditCategorySheet(
    category = category,
    onDismiss = onDismiss,
    onConfirm = onConfirm
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountSheet(
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: AccountType, initialBalance: Double, colorHex: String, iconName: String) -> Unit
) {
    

    var name by remember { mutableStateOf("") }
    var balanceText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(AccountType.CASH) }
    var selectedColorHex by remember { mutableStateOf("#1A73E8") }
    var selectedIconName by remember { mutableStateOf("account_balance_wallet") }

    val guard = rememberDiscardGuard(isDirty = name.isNotBlank() || balanceText.isNotBlank() || selectedType != AccountType.CASH || selectedColorHex != "#1A73E8" || selectedIconName != "account_balance_wallet", onDismiss = onDismiss)
    DiscardChangesDialog(guard)
        ModalBottomSheet(
        onDismissRequest = guard::requestDismiss,
        sheetState = guard.sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Nueva cuenta",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre de la cuenta") },
                placeholder = { Text("Escribe el nombre de la cuenta") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = balanceText,
                onValueChange = { balanceText = it },
                label = { Text("Saldo inicial") },
                placeholder = { Text("Ingresa el saldo inicial") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Tipo de cuenta:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(AccountType.entries) { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type.displayName) },
                        shape = CircleShape
                    )
                }
            }

            ColorAndIconSelector(
                selectedColorHex = selectedColorHex,
                onColorSelected = { selectedColorHex = it },
                selectedIconName = selectedIconName,
                onIconSelected = { selectedIconName = it }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MinicashTonalButton(text = "Cancelar", onClick = guard::requestDismiss, modifier = Modifier.weight(1f))
                MinicashButton(
                    text = "Crear",
                    onClick = {
                        val bal = balanceText.toDoubleOrNull() ?: 0.0
                        onConfirm(name.trim(), selectedType, bal, selectedColorHex, selectedIconName)
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun AddAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: AccountType, initialBalance: Double, colorHex: String, iconName: String) -> Unit
) = AddAccountSheet(
    onDismiss = onDismiss,
    onConfirm = onConfirm
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategorySheet(
    initialType: TransactionType = TransactionType.EXPENSE,
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: TransactionType, colorHex: String, iconName: String) -> Unit
) {
    

    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(initialType) }
    var selectedColorHex by remember { mutableStateOf("#1A73E8") }
    var selectedIconName by remember { mutableStateOf("category") }

    val guard = rememberDiscardGuard(isDirty = name.isNotBlank() || selectedType != initialType || selectedColorHex != "#1A73E8" || selectedIconName != "category", onDismiss = onDismiss)
    DiscardChangesDialog(guard)
        ModalBottomSheet(
        onDismissRequest = guard::requestDismiss,
        sheetState = guard.sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Nueva categoría",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre de categoría") },
                placeholder = { Text("Escribe el nombre de la categoría") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Aplica para:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedType == TransactionType.EXPENSE,
                    onClick = { selectedType = TransactionType.EXPENSE },
                    label = { Text("Gasto") },
                    shape = CircleShape
                )
                FilterChip(
                    selected = selectedType == TransactionType.INCOME,
                    onClick = { selectedType = TransactionType.INCOME },
                    label = { Text("Ingreso") },
                    shape = CircleShape
                )
            }

            ColorAndIconSelector(
                selectedColorHex = selectedColorHex,
                onColorSelected = { selectedColorHex = it },
                selectedIconName = selectedIconName,
                onIconSelected = { selectedIconName = it }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MinicashTonalButton(text = "Cancelar", onClick = guard::requestDismiss, modifier = Modifier.weight(1f))
                MinicashButton(
                    text = "Guardar",
                    onClick = { onConfirm(name.trim(), selectedType, selectedColorHex, selectedIconName) },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun AddCategoryDialog(
    initialType: TransactionType = TransactionType.EXPENSE,
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: TransactionType, colorHex: String, iconName: String) -> Unit
) = AddCategorySheet(
    initialType = initialType,
    onDismiss = onDismiss,
    onConfirm = onConfirm
)
