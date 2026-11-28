package com.uramos.minicash.ui.screens.settings

import androidx.compose.ui.res.stringResource
import com.uramos.minicash.R
import com.uramos.minicash.ui.theme.screenBackground
import com.uramos.minicash.ui.theme.MinicashShapes
import com.uramos.minicash.ui.theme.sheetBackground
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.uramos.minicash.ui.components.fadingTopEdge
import com.uramos.minicash.ui.components.MinicashSubScreenTopBar
import com.uramos.minicash.ui.components.MinicashIconButton
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Check
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uramos.minicash.ui.components.CardTonalLevel
import com.uramos.minicash.ui.components.MinicashCard
import com.uramos.minicash.ui.theme.MinicashThemeExtensions
import com.uramos.minicash.ui.utils.rememberMinicashHaptics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModulesSettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit
) {
    val spacing = MinicashThemeExtensions.spacing
    val state by viewModel.uiState.collectAsState()
    val haptics = rememberMinicashHaptics()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.screenBackground,
        topBar = {
            MinicashSubScreenTopBar(
                title = stringResource(R.string.set_modules_title),
                onBack = onBackClick,
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = spacing.screen, vertical = spacing.small),
            verticalArrangement = Arrangement.spacedBy(spacing.medium)
        ) {
            SettingsGroup {
                SettingsSwitchItem(
                    title = stringResource(R.string.nav_budget),
                    subtitle = stringResource(R.string.set_module_budget_sub),
                    icon = Icons.Default.AccountBalanceWallet,
                    checked = state.showBudgetTab,
                    onCheckedChange = { checked ->
                        haptics.click()
                        viewModel.setShowBudgetTab(checked)
                    },
                    index = 0, count = 2
                )
                SettingsSwitchItem(
                    title = stringResource(R.string.nav_credits),
                    subtitle = stringResource(R.string.set_module_credits_sub),
                    icon = Icons.Default.CreditCard,
                    checked = state.showCreditsTab,
                    onCheckedChange = { checked ->
                        haptics.click()
                        viewModel.setShowCreditsTab(checked)
                    },
                    index = 1, count = 2
                )
            }
        }
    }
}
