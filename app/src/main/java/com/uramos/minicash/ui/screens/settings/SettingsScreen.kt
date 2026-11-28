package com.uramos.minicash.ui.screens.settings

import com.uramos.minicash.ui.components.MinicashRowText
import com.uramos.minicash.ui.components.MinicashRowGroup
import com.uramos.minicash.ui.components.MinicashListRow
import com.uramos.minicash.ui.utils.setAppLanguage
import com.uramos.minicash.ui.utils.AppLanguage
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import com.uramos.minicash.ui.utils.AppStrings
import androidx.compose.ui.res.stringResource
import com.uramos.minicash.R
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uramos.minicash.data.util.appLockTimeoutLabel
import com.uramos.minicash.data.util.nextAppLockTimeout
import com.uramos.minicash.ui.components.MinicashAlertDialog
import com.uramos.minicash.ui.components.MinicashFilledButton
import com.uramos.minicash.ui.components.MinicashSectionLabel
import com.uramos.minicash.ui.components.MinicashSubScreenTopBar
import com.uramos.minicash.ui.components.MinicashTextButton
import com.uramos.minicash.ui.components.plusTop
import com.uramos.minicash.ui.components.underHeaderBlur
import com.uramos.minicash.ui.components.withoutTop
import com.uramos.minicash.ui.security.canUseAppLock
import com.uramos.minicash.ui.security.findFragmentActivity
import com.uramos.minicash.ui.security.showAppLockPrompt
import com.uramos.minicash.ui.theme.MinicashShapes
import com.uramos.minicash.ui.theme.MinicashThemeExtensions
import com.uramos.minicash.ui.theme.screenBackground
import com.uramos.minicash.ui.utils.FLOATING_NAV_CLEARANCE
import com.uramos.minicash.ui.utils.openAppNotificationSettings
import com.uramos.minicash.ui.utils.rememberMinicashHaptics
import com.uramos.minicash.ui.utils.rememberNotificationsAllowed

/**
 * Ajustes en cuatro grupos con etiqueta pequeña: Tu dinero, Preferencias, Privacidad y datos y Acerca de. Cada fila es
 * de una sola línea con su valor actual a la derecha; lo que necesita más espacio vive en su propia subpantalla.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    listState: LazyListState = rememberLazyListState(),
    onNavigateToAccounts: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    onNavigateToInstitutions: () -> Unit = {},
    onNavigateToAppearance: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToModules: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToBackups: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val spacing = MinicashThemeExtensions.spacing
    val state by viewModel.uiState.collectAsState()
    val hideInRecents by viewModel.hideInRecents.collectAsState()
    val appLockEnabled by viewModel.appLockEnabled.collectAsState()
    val appLockTimeout by viewModel.appLockTimeoutMinutes.collectAsState()
    val notificationsNeeded by viewModel.notificationsNeeded.collectAsState()
    val notificationsAllowed = rememberNotificationsAllowed()
    val context = LocalContext.current
    val haptics = rememberMinicashHaptics()
    val snackbarHostState = remember { SnackbarHostState() }
    var showNameDialog by rememberSaveable { mutableStateOf(false) }
    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.messageEvent.collect { message -> snackbarHostState.showSnackbar(message) }
    }


    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.padding(bottom = FLOATING_NAV_CLEARANCE)) },
        containerColor = MaterialTheme.colorScheme.screenBackground,
        topBar = { MinicashSubScreenTopBar(title = stringResource(R.string.nav_settings), onBack = onBackClick, scrollBehavior = scrollBehavior) }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues.withoutTop())
                .underHeaderBlur(paddingValues.calculateTopPadding(), bottomBand = 64.dp, listState = listState),
            contentPadding = PaddingValues(
                top = spacing.small,
                start = spacing.screen,
                end = spacing.screen,
                bottom = 120.dp
            ).plusTop(paddingValues.calculateTopPadding()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Aviso cuando hay algo programado y el sistema no deja notificar
            if (notificationsNeeded && !notificationsAllowed) {
                item(key = "notifications_off") {
                    NotificationsDisabledCard(onClick = { openAppNotificationSettings(context) })
                }
            }

            item(key = "group_money") {
                Column {
                    MinicashSectionLabel(stringResource(R.string.set_group_money))
                    SettingsGroup {
                        SettingsRowItem(
                            title = stringResource(R.string.set_accounts),
                            icon = Icons.Default.AccountBalanceWallet,
                            onClick = onNavigateToAccounts,
                            index = 0, count = 3
                        )
                        SettingsRowItem(
                            title = stringResource(R.string.set_categories),
                            icon = Icons.Default.Category,
                            onClick = onNavigateToCategories,
                            index = 1, count = 3
                        )
                        SettingsRowItem(
                            title = stringResource(R.string.set_institutions),
                            icon = Icons.Default.CreditCard,
                            onClick = onNavigateToInstitutions,
                            index = 2, count = 3
                        )
                    }
                }
            }

            item(key = "group_preferences") {
                Column {
                    MinicashSectionLabel(stringResource(R.string.set_group_preferences))
                    SettingsGroup {
                        SettingsRowItem(
                            title = stringResource(R.string.set_your_name),
                            icon = Icons.Default.Person,
                            value = state.userName.ifBlank { stringResource(R.string.set_name_undefined) },
                            onClick = { showNameDialog = true },
                            showChevron = false,
                            index = 0, count = 6
                        )
                        SettingsRowItem(
                            title = stringResource(R.string.set_language),
                            icon = Icons.Default.Language,
                            value = AppLanguage.current().nativeName,
                            onClick = { showLanguageDialog = true },
                            index = 1, count = 6
                        )
                        SettingsRowItem(
                            title = stringResource(R.string.set_appearance),
                            icon = Icons.Default.Palette,
                            onClick = onNavigateToAppearance,
                            index = 2, count = 6
                        )
                        SettingsRowItem(
                            title = stringResource(R.string.nav_home),
                            icon = Icons.Default.Home,
                            onClick = onNavigateToHome,
                            index = 3, count = 6
                        )
                        SettingsRowItem(
                            title = stringResource(R.string.set_modules),
                            icon = Icons.Default.Widgets,
                            onClick = onNavigateToModules,
                            index = 4, count = 6
                        )
                        SettingsRowItem(
                            title = stringResource(R.string.set_notifications),
                            icon = Icons.Default.Notifications,
                            onClick = onNavigateToNotifications,
                            index = 5, count = 6
                        )
                    }
                }
            }

            item(key = "group_privacy") {
                val count = if (appLockEnabled) 4 else 3
                Column {
                    MinicashSectionLabel(stringResource(R.string.set_group_privacy))
                    SettingsGroup {
                        SettingsSwitchItem(
                            title = stringResource(R.string.set_app_lock),
                            subtitle = if (appLockEnabled) appLockTimeoutLabel(appLockTimeout) + stringResource(R.string.set_app_lock_on)
                            else stringResource(R.string.set_app_lock_off),
                            icon = Icons.Default.Lock,
                            checked = appLockEnabled,
                            onCheckedChange = { wantsOn ->
                                haptics.click()
                                val activity = context.findFragmentActivity()
                                when {
                                    !wantsOn -> activity?.let {
                                        // Para quitar el bloqueo también se pide verificarse
                                        showAppLockPrompt(it, AppStrings.get(R.string.set_lock_remove_title), AppStrings.get(R.string.set_lock_verify_you), onSuccess = { viewModel.setAppLockEnabled(false) })
                                    } ?: viewModel.setAppLockEnabled(false)
                                    activity == null || !canUseAppLock(context) ->
                                        Toast.makeText(context, AppStrings.get(R.string.set_lock_setup_needed), Toast.LENGTH_LONG).show()
                                    else -> showAppLockPrompt(activity, AppStrings.get(R.string.set_lock_enable_title), AppStrings.get(R.string.set_lock_verify_unlock), onSuccess = { viewModel.setAppLockEnabled(true) })
                                }
                            },
                            index = 0, count = count
                        )
                        if (appLockEnabled) {
                            SettingsRowItem(
                                title = stringResource(R.string.set_lock_ask_again),
                                icon = Icons.Default.Timer,
                                value = appLockTimeoutLabel(appLockTimeout),
                                onClick = {
                                    haptics.click()
                                    viewModel.setAppLockTimeoutMinutes(nextAppLockTimeout(appLockTimeout))
                                },
                                showChevron = false,
                                index = 1, count = count
                            )
                        }
                        SettingsSwitchItem(
                            title = stringResource(R.string.set_hide_recents),
                            subtitle = if (hideInRecents) stringResource(R.string.set_hide_recents_on)
                            else stringResource(R.string.set_hide_recents_off),
                            icon = Icons.Default.VisibilityOff,
                            checked = hideInRecents,
                            onCheckedChange = {
                                haptics.click()
                                viewModel.setHideInRecents(it)
                            },
                            index = count - 2, count = count
                        )
                        SettingsRowItem(
                            title = stringResource(R.string.set_backups),
                            icon = Icons.Default.CloudDone,
                            onClick = onNavigateToBackups,
                            index = count - 1, count = count
                        )
                    }
                }
            }

            item(key = "group_about") {
                Column {
                    MinicashSectionLabel(stringResource(R.string.set_group_about))
                    SettingsGroup {
                        SettingsRowItem(
                            title = "Minicash",
                            icon = Icons.Default.Info,
                            onClick = onNavigateToAbout
                        )
                    }
                }
            }
        }
    }

    if (showLanguageDialog) {
        LanguageDialog(onDismiss = { showLanguageDialog = false })
    }

    if (showNameDialog) {
        var draft by rememberSaveable { mutableStateOf(state.userName) }
        val canSave = draft.isNotBlank() && draft.trim() != state.userName
        MinicashAlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text(stringResource(R.string.set_your_name), fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it.take(MAX_USER_NAME_LENGTH) },
                    label = { Text(stringResource(R.string.set_name_label)) },
                    supportingText = { Text(stringResource(R.string.set_name_support)) },
                    singleLine = true,
                    shape = MinicashShapes.Input,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                MinicashFilledButton(
                    onClick = {
                        viewModel.setUserName(draft)
                        showNameDialog = false
                    },
                    enabled = canSave,
                ) {
                    Text(stringResource(R.string.common_save), fontWeight = FontWeight.Bold, maxLines = 1)
                }
            },
            dismissButton = {
                MinicashTextButton(onClick = { showNameDialog = false }) { Text(stringResource(R.string.common_cancel)) }
            },
            shape = MinicashShapes.Hero
        )
    }
}

/** Elige el idioma de la app: español (predeterminado) o inglés. El cambio vuelve a crear la pantalla. */
@Composable
private fun LanguageDialog(onDismiss: () -> Unit) {
    val current = AppLanguage.current()
    MinicashAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.set_language), fontWeight = FontWeight.Bold) },
        text = {
            MinicashRowGroup {
                AppLanguage.entries.forEachIndexed { index, language ->
                    MinicashListRow(
                        index = index,
                        count = AppLanguage.entries.size,
                        onClick = {
                            onDismiss()
                            if (language != current) setAppLanguage(language)
                        }
                    ) {
                        MinicashRowText(title = language.nativeName)
                        if (language == current) {
                            Icon(Icons.Default.Check, contentDescription = stringResource(R.string.set_chosen))
                        }
                    }
                }
            }
        },
        confirmButton = {
            MinicashTextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        },
        shape = MinicashShapes.Hero
    )
}
