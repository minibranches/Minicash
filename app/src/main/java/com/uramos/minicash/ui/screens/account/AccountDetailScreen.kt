package com.uramos.minicash.ui.screens.account

import androidx.compose.ui.res.stringResource
import com.uramos.minicash.R
import com.uramos.minicash.ui.utils.appLocale
import com.uramos.minicash.ui.theme.minicashAnimateItem
import com.uramos.minicash.ui.theme.screenBackground
import com.uramos.minicash.ui.theme.MinicashShapes
import com.uramos.minicash.ui.theme.cardSurface
import com.uramos.minicash.ui.theme.sheetBackground
import com.uramos.minicash.ui.components.plusTop
import com.uramos.minicash.ui.components.withoutTop
import com.uramos.minicash.ui.components.underHeaderBlur
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.uramos.minicash.data.local.entities.AccountEntity
import com.uramos.minicash.data.local.entities.TransactionEntity
import com.uramos.minicash.ui.components.CardTonalLevel
import com.uramos.minicash.ui.components.MinicashCard
import com.uramos.minicash.ui.components.MinicashEmptyState
import com.uramos.minicash.ui.components.MinicashSubScreenTopBar
import com.uramos.minicash.ui.components.rememberAnimatedCents
import com.uramos.minicash.ui.screens.home.GroupedTransactionCard
import com.uramos.minicash.ui.screens.home.HomeViewModel
import com.uramos.minicash.ui.theme.LocalAnimationRestriction
import com.uramos.minicash.ui.theme.MinicashThemeExtensions
import com.uramos.minicash.ui.theme.minicashSpatialSpec
import com.uramos.minicash.ui.transactions.EditTransactionSheet
import com.uramos.minicash.ui.utils.MinicashColorUtils
import com.uramos.minicash.ui.utils.getIconVectorByName
import com.uramos.minicash.ui.utils.groupTransactionsByDate
import com.uramos.minicash.ui.utils.toCurrencyFormat
import java.time.YearMonth
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale
import kotlin.math.abs

private const val CHART_MONTHS = 6

/**
 * Detalle de una cuenta: su saldo, cuánto cambió en los últimos meses y sus movimientos (como origen o destino).
 * La edición abre la misma hoja que Movimientos; borrar se hace desde esa lista, no desde aquí.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AccountDetailScreen(
    accountId: Long,
    viewModel: HomeViewModel,
    onBack: () -> Unit
) {
    val spacing = MinicashThemeExtensions.spacing
    val homeState by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val transactions by remember(accountId) { viewModel.accountTransactions(accountId) }
        .collectAsState(initial = emptyList())
    val account = homeState.accounts.firstOrNull { it.id == accountId }

    val categoryMap = remember(categories) { categories.associateBy({ it.id }, { it.name }) }
    val accountMap = remember(homeState.accounts) { homeState.accounts.associateBy({ it.id }, { it.name }) }
    val grouped = remember(transactions) { groupTransactionsByDate(transactions) }
    val monthlyNet = remember(transactions, accountId) {
        accountMonthlyNet(transactions, accountId, CHART_MONTHS, YearMonth.now())
    }
    var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }

    transactionToEdit?.let { editing ->
        EditTransactionSheet(
            transaction = editing,
            accounts = homeState.accounts,
            categories = categories,
            onDismiss = { transactionToEdit = null },
            onSave = { original, updated ->
                viewModel.updateTransaction(original, updated)
                transactionToEdit = null
            },
            onCreateCategory = viewModel::createCategory
        )
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val listState = rememberLazyListState()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.screenBackground,
        topBar = {
            MinicashSubScreenTopBar(
                title = account?.name ?: stringResource(R.string.tx_account),
                onBack = onBack,
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        if (account == null) {
            Box(Modifier.fillMaxSize().padding(paddingValues))
            return@Scaffold
        }
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
            verticalArrangement = Arrangement.spacedBy(spacing.medium)
        ) {
            item(key = "hero") { AccountHeroCard(account) }

            item(key = "chart") {
                MinicashCard(level = CardTonalLevel.LOW, modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(spacing.standard),
                        verticalArrangement = Arrangement.spacedBy(spacing.small)
                    ) {
                        Text(
                            text = stringResource(R.string.account_balance_change),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        MonthlyNetChart(monthlyNet)
                    }
                }
            }

            item(key = "title") {
                Text(
                    text = stringResource(R.string.nav_transactions),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, top = spacing.small)
                )
            }

            if (grouped.isEmpty()) {
                item(key = "empty") {
                    MinicashEmptyState(
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        title = stringResource(R.string.account_empty_title),
                        subtitle = stringResource(R.string.account_empty_sub),
                        modifier = Modifier.fillMaxWidth().padding(top = spacing.large)
                    )
                }
            } else {
                items(items = grouped, key = { "group_${it.dateKey}" }) { group ->
                    GroupedTransactionCard(
                        group = group,
                        categoryMap = categoryMap,
                        accountMap = accountMap,
                        onTransactionClick = { transactionToEdit = it },
                        modifier = minicashAnimateItem()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AccountHeroCard(account: AccountEntity) {
    val accountColor = MinicashColorUtils.parseHexColor(account.colorHex, MaterialTheme.colorScheme.primary)
    val containerColor = MinicashColorUtils.tintedContainerColor(
        baseColor = accountColor,
        surfaceColor = MaterialTheme.colorScheme.cardSurface,
        ratio = 0.18f
    )
    MinicashCard(
        level = CardTonalLevel.LOW,
        containerColor = containerColor,
        shape = MinicashShapes.Card,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = MaterialShapes.Cookie9Sided.toShape(),
                color = accountColor.copy(alpha = 0.28f),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getIconVectorByName(account.iconName),
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = account.type.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val animatedBalance = rememberAnimatedCents(account.balance)
                Text(
                    text = (if (animatedBalance < 0) "- " else "") + abs(animatedBalance).toCurrencyFormat(),
                    style = MaterialTheme.typography.headlineMedium.copy(fontFeatureSettings = "tnum"),
                    fontWeight = FontWeight.ExtraBold,
                    color = if (account.balance < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }
        }
    }
}

/** Barras del cambio neto por mes: verdes sobre la línea base si entró más de lo que salió y rojas debajo si no. */
@Composable
private fun MonthlyNetChart(months: List<MonthlyNet>) {
    val financial = MinicashThemeExtensions.financialColors
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val baselineColor = MaterialTheme.colorScheme.outlineVariant
    val restricted = LocalAnimationRestriction.current
    val progress = remember { Animatable(if (restricted) 1f else 0f) }
    val spatial = minicashSpatialSpec<Float>()
    LaunchedEffect(months) { progress.animateTo(1f, spatial) }

    val maxUp = months.maxOf { maxOf(it.net, 0L) }
    val maxDown = months.maxOf { maxOf(-it.net, 0L) }
    val range = (maxUp + maxDown).coerceAtLeast(1L).toFloat()
    val hasData = months.any { it.net != 0L }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
            val slot = size.width / months.size
            val barWidth = slot * 0.5f
            val baseline = if (hasData) size.height * (maxUp / range) else size.height / 2f
            months.forEachIndexed { index, month ->
                val barHeight = (abs(month.net) / range) * size.height * progress.value
                if (barHeight > 0f) {
                    val left = index * slot + (slot - barWidth) / 2f
                    val top = if (month.net >= 0) baseline - barHeight else baseline
                    drawRoundRect(
                        color = if (month.net >= 0) financial.income else financial.expense,
                        topLeft = Offset(left, top),
                        size = Size(barWidth, barHeight.coerceAtLeast(2f)),
                        cornerRadius = CornerRadius(6.dp.toPx())
                    )
                }
            }
            drawLine(
                color = baselineColor,
                start = Offset(0f, baseline),
                end = Offset(size.width, baseline),
                strokeWidth = 1.dp.toPx()
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            months.forEach { month ->
                Text(
                    text = month.month.month.getDisplayName(JavaTextStyle.SHORT, appLocale()).trimEnd('.'),
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (!hasData) {
            Text(
                text = stringResource(R.string.account_empty_months),
                style = MaterialTheme.typography.bodySmall,
                color = labelColor
            )
        }
    }
}
