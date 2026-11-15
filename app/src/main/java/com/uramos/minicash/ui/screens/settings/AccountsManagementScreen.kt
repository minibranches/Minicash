package com.uramos.minicash.ui.screens.settings

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.uramos.minicash.ui.components.fadingTopEdge
import com.uramos.minicash.ui.components.MinicashSubScreenTopBar
import com.uramos.minicash.ui.components.MinicashAlertDialog
import com.uramos.minicash.ui.components.MinicashTextButton
import com.uramos.minicash.ui.components.MinicashFilledButton
import com.uramos.minicash.ui.components.MinicashIconButton
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.foundation.lazy.rememberLazyListState
import sh.calvin.reorderable.rememberReorderableLazyListState
import sh.calvin.reorderable.ReorderableItem
import androidx.compose.foundation.background

import com.uramos.minicash.ui.utils.asIconTint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.uramos.minicash.ui.utils.rememberMinicashHaptics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.uramos.minicash.data.local.entities.AccountEntity
import com.uramos.minicash.ui.components.CardTonalLevel
import com.uramos.minicash.ui.components.MinicashCard
import com.uramos.minicash.ui.components.MinicashEmptyState
import com.uramos.minicash.ui.components.MoneyDisplayType
import com.uramos.minicash.ui.components.MoneyTextView
import com.uramos.minicash.ui.components.rememberSegmentedShape
import com.uramos.minicash.ui.theme.MinicashShapes
import com.uramos.minicash.ui.theme.MinicashThemeExtensions
import com.uramos.minicash.ui.utils.MinicashColorUtils
import com.uramos.minicash.ui.utils.getIconVectorByName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsManagementScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val spacing = MinicashThemeExtensions.spacing
    val state by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    val haptics = rememberMinicashHaptics()

    var showAddAccountDialog by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<AccountEntity?>(null) }
    var accountToDelete by remember { mutableStateOf<AccountEntity?>(null) }

    // Orden local mientras se arrastra; se guarda en la base al soltar
    var orderedAccounts by remember(state.accounts) { mutableStateOf(state.accounts) }
    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val list = orderedAccounts.toMutableList()
        val fromIdx = list.indexOfFirst { it.id == from.key }
        val toIdx = list.indexOfFirst { it.id == to.key }
        if (fromIdx >= 0 && toIdx >= 0) {
            list.add(toIdx, list.removeAt(fromIdx))
            orderedAccounts = list
            haptics.tick()
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        topBar = {
            MinicashSubScreenTopBar(
                title = "Cuentas",
                onBack = onNavigateBack,
                scrollBehavior = scrollBehavior,
                actions = {
                    MinicashIconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showAddAccountDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Nueva cuenta"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (state.accounts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                MinicashEmptyState(
                    icon = Icons.Default.AccountBalanceWallet,
                    title = "Sin cuentas registradas",
                    subtitle = "Agrega tus cuentas de efectivo, débito o banco para comenzar.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = spacing.hero),
                    actionButton = {
                        MinicashFilledButton(
                            onClick = { showAddAccountDialog = true },
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .heightIn(min = 48.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Agregar nueva cuenta")
                        }
                    }
                )
            }
        } else {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                .fadingTopEdge(lazyListState),
                contentPadding = PaddingValues(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                item {
                    Text(
                        text = "Gestión de cuentas y saldos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )
                }

                itemsIndexed(orderedAccounts, key = { _, acc -> acc.id }) { index, account ->
                    ReorderableItem(reorderState, key = account.id) { isDragging ->
                        val itemShape = rememberSegmentedShape(index = index, count = state.accounts.size)
                        val cardAccentColor = MinicashColorUtils.parseHexColor(
                            account.colorHex,
                            MaterialTheme.colorScheme.primary
                        )
                        val accountIcon = getIconVectorByName(account.iconName)

                        MinicashCard(
                            level = CardTonalLevel.LOW,
                            shape = itemShape,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                accountToEdit = account
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 72.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = cardAccentColor.copy(alpha = 0.18f),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = accountIcon,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = cardAccentColor.asIconTint()
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = account.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = account.type.displayName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                MoneyTextView(
                                    amountCents = account.balance,
                                    type = MoneyDisplayType.NEUTRAL,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DragHandle,
                                        contentDescription = "Mantén presionado y arrastra para reordenar",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .padding(6.dp)
                                            .draggableHandle(
                                                onDragStopped = { viewModel.saveAccountOrder(orderedAccounts.map { it.id }) }
                                            )
                                    )

                                    MinicashIconButton(
                                        onClick = { viewModel.toggleAccountVisibility(account) },
                                    ) {
                                        Icon(
                                            imageVector = if (account.isVisibleInHome) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = "Visibilidad",
                                            tint = if (account.isVisibleInHome) cardAccentColor.asIconTint() else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    MinicashCard(
                        level = CardTonalLevel.HIGH,
                        shape = MinicashShapes.ContainerLarge,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showAddAccountDialog = true
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
                                text = "Agregar nueva cuenta",
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

    if (showAddAccountDialog) {
        AddAccountDialog(
            onDismiss = { showAddAccountDialog = false },
            onConfirm = { name, type, balance, colorHex, iconName ->
                viewModel.addAccount(name, type, balance, colorHex, iconName)
                showAddAccountDialog = false
            }
        )
    }

    if (accountToEdit != null) {
        val acc = accountToEdit!!
        EditAccountDialog(
            account = acc,
            onDismiss = { accountToEdit = null },
            onConfirm = { name, type, balanceCents, colorHex, iconName ->
                viewModel.updateAccount(acc, name, type, balanceCents, colorHex, iconName)
                accountToEdit = null
            },
            onDelete = {
                accountToDelete = acc
                accountToEdit = null
            }
        )
    }

    if (accountToDelete != null) {
        val acc = accountToDelete!!
        MinicashAlertDialog(
            onDismissRequest = { accountToDelete = null },
            title = { Text("¿Eliminar cuenta?", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro de eliminar la cuenta '${acc.name}'? Esta acción eliminará permanentemente la cuenta.") },
            confirmButton = {
                MinicashFilledButton(
                    onClick = {
                        haptics.heavyOrDelete()
                        viewModel.deleteAccount(acc)
                        accountToDelete = null
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
                MinicashTextButton(onClick = { accountToDelete = null }) { Text("Cancelar") }
            },
            shape = MinicashShapes.Hero
        )
    }
}
