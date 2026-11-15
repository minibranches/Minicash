package com.uramos.minicash.ui.screens.extras

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uramos.minicash.ui.components.CardTonalLevel
import com.uramos.minicash.ui.components.MinicashCard
import com.uramos.minicash.ui.components.MoneyDisplayType
import com.uramos.minicash.ui.components.MoneyTextView
import com.uramos.minicash.ui.components.SettingsGearButton
import com.uramos.minicash.ui.components.WavyProgressIndicator
import com.uramos.minicash.ui.components.fadingTopEdge
import com.uramos.minicash.ui.screens.budget.BudgetViewModel
import com.uramos.minicash.ui.screens.credits.CreditsViewModel
import com.uramos.minicash.ui.screens.reports.ReportsViewModel
import com.uramos.minicash.ui.theme.MinicashThemeExtensions
import com.uramos.minicash.ui.utils.toCompactCurrency
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

/**
 * Centro de Extras: tarjetas hacia Gastos fijos, Créditos y Reportes, cada una con un mini resumen.
 * Los módulos que el usuario oculta en Ajustes no aparecen aquí.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtrasScreen(
    budgetViewModel: BudgetViewModel,
    creditsViewModel: CreditsViewModel,
    reportsViewModel: ReportsViewModel,
    showBudget: Boolean,
    showCredits: Boolean,
    listState: LazyListState,
    onOpenBudget: () -> Unit,
    onOpenCredits: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val spacing = MinicashThemeExtensions.spacing
    val budgetState by budgetViewModel.uiState.collectAsState()
    val creditsState by creditsViewModel.uiState.collectAsState()
    val reportsState by reportsViewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0.dp),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                title = { Text("Extras", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) },
                actions = { SettingsGearButton(onClick = onOpenSettings) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .fadingTopEdge(listState),
            contentPadding = PaddingValues(
                top = spacing.small,
                start = spacing.standard,
                end = spacing.standard,
                bottom = 120.dp
            ),
            verticalArrangement = Arrangement.spacedBy(spacing.medium)
        ) {
            if (showBudget) {
                item {
                    val summary = budgetState.summary
                    val progress = if (summary.expectedExpense > 0) {
                        (summary.paidExpense.toFloat() / summary.expectedExpense.toFloat()).coerceIn(0f, 1f)
                    } else 0f
                    ExtrasCard(
                        icon = Icons.Default.PieChart,
                        title = "Gastos fijos",
                        label = "Disponible",
                        amountCents = summary.actualFreeAmount,
                        amountType = if (summary.actualFreeAmount >= 0) MoneyDisplayType.INCOME else MoneyDisplayType.EXPENSE,
                        caption = "${(progress * 100).toInt()}% pagado de la quincena",
                        progress = progress,
                        onClick = onOpenBudget
                    )
                }
            }
            if (showCredits) {
                item {
                    val summary = creditsState.summary
                    ExtrasCard(
                        icon = Icons.Default.CreditCard,
                        title = "Créditos",
                        label = "Deuda pendiente",
                        amountCents = summary.totalPendingDebt,
                        amountType = MoneyDisplayType.NEUTRAL,
                        caption = "Mensual ${summary.monthlyPaymentCommitment.toCompactCurrency()} · ${summary.activeCreditsCount} activos",
                        onClick = onOpenCredits
                    )
                }
            }
            item {
                val month = reportsState.months.lastOrNull()
                val monthName = month?.month?.month?.getDisplayName(JavaTextStyle.FULL, Locale.forLanguageTag("es-MX"))
                ExtrasCard(
                    icon = Icons.Default.BarChart,
                    title = "Reportes",
                    label = "Balance de ${monthName ?: "este mes"}",
                    amountCents = month?.balance ?: 0L,
                    amountType = if ((month?.balance ?: 0L) >= 0) MoneyDisplayType.INCOME else MoneyDisplayType.EXPENSE,
                    caption = if (month != null) {
                        "Ingresos ${month.income.toCompactCurrency()} · Gastos ${month.expense.toCompactCurrency()}"
                    } else "Ingresos contra gastos por mes",
                    onClick = onOpenReports
                )
            }
        }
    }
}

@Composable
private fun ExtrasCard(
    icon: ImageVector,
    title: String,
    label: String,
    amountCents: Long,
    amountType: MoneyDisplayType,
    caption: String,
    onClick: () -> Unit,
    progress: Float? = null
) {
    MinicashCard(
        level = CardTonalLevel.LOW,
        shape = RoundedCornerShape(28.dp),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(44.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(22.dp))
                    }
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column {
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                MoneyTextView(
                    amountCents = amountCents,
                    type = amountType,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold, fontFeatureSettings = "tnum")
                )
            }
            if (progress != null) {
                WavyProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth().height(20.dp))
            }
            Text(caption, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
