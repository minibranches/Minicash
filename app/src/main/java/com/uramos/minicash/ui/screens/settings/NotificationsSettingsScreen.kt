package com.uramos.minicash.ui.screens.settings

import androidx.compose.ui.res.stringResource
import com.uramos.minicash.R
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.uramos.minicash.ui.components.MinicashSectionLabel
import com.uramos.minicash.ui.components.MinicashSectionNote
import com.uramos.minicash.ui.components.MinicashSubScreenTopBar
import com.uramos.minicash.ui.components.MinicashTimePickerDialog
import com.uramos.minicash.ui.components.plusTop
import com.uramos.minicash.ui.components.underHeaderBlur
import com.uramos.minicash.ui.components.withoutTop
import com.uramos.minicash.ui.theme.MinicashThemeExtensions
import com.uramos.minicash.ui.theme.screenBackground
import com.uramos.minicash.ui.utils.openAppNotificationSettings
import com.uramos.minicash.ui.utils.rememberMinicashHaptics
import com.uramos.minicash.ui.utils.rememberNotificationsAllowed

private fun formatClock(hour: Int, minute: Int): String =
    "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"

/**
 * Ajustes → Notificaciones: la hora de los avisos de pago y el recordatorio diario. Si algo está programado y el
 * sistema no deja notificar, arriba aparece la tarjeta roja que lleva a activarlas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit
) {
    val spacing = MinicashThemeExtensions.spacing
    val state by viewModel.uiState.collectAsState()
    val creditReminderTime by viewModel.creditReminderTime.collectAsState()
    val notificationsNeeded by viewModel.notificationsNeeded.collectAsState()
    val notificationsAllowed = rememberNotificationsAllowed()
    val context = LocalContext.current
    val haptics = rememberMinicashHaptics()
    val listState = rememberLazyListState()
    var showCreditTimePicker by remember { mutableStateOf(false) }
    var showDailyTimePicker by remember { mutableStateOf(false) }

    val postNotificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) viewModel.setDailyReminderEnabled(context, true)
    }

    val setDailyReminder: (Boolean) -> Unit = { checked ->
        haptics.click()
        if (!checked) {
            viewModel.setDailyReminderEnabled(context, false)
        } else if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            postNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.setDailyReminderEnabled(context, true)
        }
    }

    val dailyTime = formatClock(state.dailyReminderHour, state.dailyReminderMinute)
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.screenBackground,
        topBar = { MinicashSubScreenTopBar(title = stringResource(R.string.set_notifications), onBack = onBackClick, scrollBehavior = scrollBehavior) }
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
            if (notificationsNeeded && !notificationsAllowed) {
                item(key = "notifications_off") {
                    NotificationsDisabledCard(onClick = { openAppNotificationSettings(context) })
                }
            }

            item(key = "group_payments") {
                Column {
                    MinicashSectionLabel(stringResource(R.string.set_notif_payments))
                    SettingsGroup {
                        SettingsRowItem(
                            title = stringResource(R.string.set_notif_payment_time),
                            icon = Icons.Default.Payments,
                            value = stringResource(R.string.set_hours, formatClock(creditReminderTime.first, creditReminderTime.second)),
                            onClick = { showCreditTimePicker = true },
                            showChevron = false
                        )
                    }
                    MinicashSectionNote(stringResource(R.string.set_notif_payments_note))
                }
            }

            item(key = "group_daily") {
                val count = if (state.dailyReminderEnabled) 2 else 1
                Column {
                    MinicashSectionLabel(stringResource(R.string.set_notif_reminders))
                    SettingsGroup {
                        SettingsSwitchItem(
                            title = stringResource(R.string.set_notif_daily),
                            subtitle = stringResource(R.string.set_notif_daily_sub),
                            icon = Icons.Default.Notifications,
                            checked = state.dailyReminderEnabled && notificationsAllowed,
                            onCheckedChange = setDailyReminder,
                            index = 0, count = count
                        )
                        if (state.dailyReminderEnabled) {
                            SettingsRowItem(
                                title = stringResource(R.string.set_notif_daily_time),
                                icon = Icons.Default.Schedule,
                                value = stringResource(R.string.set_hours, dailyTime),
                                onClick = { showDailyTimePicker = true },
                                showChevron = false,
                                index = 1, count = count
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDailyTimePicker) {
        MinicashTimePickerDialog(
            title = stringResource(R.string.set_notif_daily_time),
            initialHour = state.dailyReminderHour,
            initialMinute = state.dailyReminderMinute,
            onConfirm = { hour, minute ->
                viewModel.setDailyReminderTime(context, hour, minute)
                showDailyTimePicker = false
            },
            onDismiss = { showDailyTimePicker = false }
        )
    }

    if (showCreditTimePicker) {
        MinicashTimePickerDialog(
            title = stringResource(R.string.set_notif_payments_time_title),
            initialHour = creditReminderTime.first,
            initialMinute = creditReminderTime.second,
            onConfirm = { hour, minute ->
                viewModel.setCreditReminderTime(context, hour, minute)
                showCreditTimePicker = false
            },
            onDismiss = { showCreditTimePicker = false }
        )
    }
}
