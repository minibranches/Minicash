package com.uramos.minicash.widget

import com.uramos.minicash.data.preferences.FontPreference
import androidx.glance.LocalSize
import androidx.glance.layout.fillMaxHeight
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.RowScope
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import com.uramos.minicash.MainActivity
import com.uramos.minicash.R
import com.uramos.minicash.ui.utils.toCurrencyFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private const val ACTION_EXPENSE = "com.uramos.minicash.ACTION_NEW_EXPENSE"
private const val ACTION_INCOME = "com.uramos.minicash.ACTION_NEW_INCOME"
private const val ACTION_TRANSFER = "com.uramos.minicash.ACTION_NEW_TRANSFER"

/** Mismas acciones que los atajos de la app: `MainActivity` las traduce a "nuevo gasto / ingreso / transferencia". */
private fun quickAction(context: Context, action: String, extra: String): Action =
    actionStartActivity(
        Intent(context, MainActivity::class.java).apply {
            this.action = action
            putExtra("shortcut_action", extra)
            // Si la app ya está abierta se reutiliza en lugar de abrir otra copia encima
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    )

private fun openAppAction(context: Context): Action =
    actionStartActivity(
        Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    )

/** Widget principal: patrimonio, gasto del mes y tres botones de registro. */
class MinicashWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = loadWidgetSnapshot(context)
        provideContent {
            GlanceTheme { MinicashWidgetContent(context, snapshot) }
        }
    }
}

/** Widget compacto con solo los tres botones de registro. */
class QuickActionsWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val font = loadFontPreference(context)
        provideContent {
            GlanceTheme {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.widgetBackground)
                        .cornerRadius(28.dp)
                        .padding(12.dp)
                ) { QuickActionsRow(context, font, GlanceModifier.fillMaxSize()) }
            }
        }
    }
}

class MinicashWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MinicashWidget()
}

class QuickActionsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickActionsWidget()
}

private val widgetScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

/** Refresca los widgets; se llama al salir de la app, que es cuando los datos pudieron cambiar. */
fun updateMinicashWidgets(context: Context) {
    val appContext = context.applicationContext
    widgetScope.launch {
        runCatching { MinicashWidget().updateAll(appContext) }
        runCatching { QuickActionsWidget().updateAll(appContext) }
    }
}

@Composable
private fun MinicashWidgetContent(context: Context, snapshot: WidgetSnapshot) {
    val width = LocalSize.current.width
    val textWidth = width - 32.dp
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .cornerRadius(28.dp)
            .padding(16.dp)
    ) {
        Column(modifier = GlanceModifier.fillMaxWidth().clickable(openAppAction(context))) {
            FontText(
                text = "Patrimonio",
                sizeSp = 12f,
                weight = WidgetWeight.SEMIBOLD,
                color = GlanceTheme.colors.onSurfaceVariant,
                font = snapshot.font
            )
            FontText(
                text = widgetAmountText(snapshot.totalBalance, snapshot.hideAmounts) { it.toCurrencyFormat() },
                sizeSp = 28f,
                weight = WidgetWeight.BOLD,
                color = GlanceTheme.colors.onSurface,
                font = snapshot.font,
                tabularFigures = true,
                maxWidth = textWidth
            )
            FontText(
                text = "Gastado este mes: " + widgetAmountText(snapshot.monthExpense, snapshot.hideAmounts) { it.toCurrencyFormat() },
                sizeSp = 12f,
                weight = WidgetWeight.REGULAR,
                color = GlanceTheme.colors.onSurfaceVariant,
                font = snapshot.font,
                tabularFigures = true,
                maxWidth = textWidth
            )
        }
        // Los botones se pegan al borde inferior y el espacio sobrante queda entre las cifras y ellos
        Spacer(GlanceModifier.defaultWeight())
        QuickActionsRow(context, snapshot.font, GlanceModifier.height(44.dp))
    }
}

@Composable
private fun QuickActionsRow(context: Context, font: FontPreference, modifier: GlanceModifier) {
    Row(modifier = modifier.fillMaxWidth()) {
        QuickActionTile(R.drawable.ic_shortcut_expense, "Gasto", font, quickAction(context, ACTION_EXPENSE, "EXPENSE"))
        Spacer(GlanceModifier.width(8.dp))
        QuickActionTile(R.drawable.ic_shortcut_income, "Ingreso", font, quickAction(context, ACTION_INCOME, "INCOME"))
        Spacer(GlanceModifier.width(8.dp))
        QuickActionTile(R.drawable.ic_shortcut_transfer, "Transfer.", font, quickAction(context, ACTION_TRANSFER, "TRANSFER"))
    }
}

@Composable
private fun RowScope.QuickActionTile(iconRes: Int, label: String, font: FontPreference, action: Action) {
    Row(
        modifier = GlanceModifier
            .defaultWeight()
            .fillMaxHeight()
            .background(GlanceTheme.colors.primaryContainer)
            .cornerRadius(20.dp)
            .padding(horizontal = 6.dp)
            .clickable(action),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            provider = ImageProvider(iconRes),
            contentDescription = null,
            colorFilter = ColorFilter.tint(GlanceTheme.colors.onPrimaryContainer),
            modifier = GlanceModifier.size(18.dp)
        )
        Spacer(GlanceModifier.width(4.dp))
        FontText(
            text = label,
            sizeSp = 12f,
            weight = WidgetWeight.SEMIBOLD,
            color = GlanceTheme.colors.onPrimaryContainer,
            font = font
        )
    }
}
