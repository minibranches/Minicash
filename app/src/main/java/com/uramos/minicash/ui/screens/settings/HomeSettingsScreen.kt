package com.uramos.minicash.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uramos.minicash.data.preferences.DistributionStyle
import com.uramos.minicash.data.preferences.HomeWidget
import com.uramos.minicash.data.preferences.PatrimonyStyle
import com.uramos.minicash.data.preferences.moveHomeWidget
import com.uramos.minicash.ui.components.MinicashSectionLabel
import com.uramos.minicash.ui.components.MinicashSectionNote
import com.uramos.minicash.ui.components.MinicashSegmentedChoice
import com.uramos.minicash.ui.components.MinicashSubScreenTopBar
import com.uramos.minicash.ui.components.plusTop
import com.uramos.minicash.ui.components.underHeaderBlur
import com.uramos.minicash.ui.components.withoutTop
import com.uramos.minicash.ui.theme.MinicashShapes
import com.uramos.minicash.ui.theme.MinicashThemeExtensions
import com.uramos.minicash.ui.theme.cardSurface
import com.uramos.minicash.ui.theme.screenBackground
import com.uramos.minicash.ui.utils.rememberMinicashHaptics
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

/**
 * Ajustes → Inicio: qué secciones se muestran y en qué orden (se arrastran desde el asa), cómo se dibuja
 * «Distribución del mes» y cómo se dibuja el Patrimonio.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit
) {
    val spacing = MinicashThemeExtensions.spacing
    val layout by viewModel.homeLayout.collectAsState()
    val haptics = rememberMinicashHaptics()
    val listState = rememberLazyListState()
    // Copia local del orden para que el arrastre se vea al instante; se guarda al soltar
    var order by remember(layout.order) { mutableStateOf(layout.order) }
    val reorderState = rememberReorderableLazyListState(listState) { from, to ->
        val fromIndex = order.indexOfFirst { it.id == from.key }
        val toIndex = order.indexOfFirst { it.id == to.key }
        if (fromIndex >= 0 && toIndex >= 0) {
            order = moveHomeWidget(order, fromIndex, toIndex)
            haptics.tick()
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.screenBackground,
        topBar = { MinicashSubScreenTopBar(title = "Inicio", onBack = onBackClick, scrollBehavior = scrollBehavior) }
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
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            item(key = "label_widgets") {
                MinicashSectionLabel("Qué se muestra")
            }
            order.forEachIndexed { index, widget ->
                item(key = widget.id) {
                    ReorderableItem(reorderState, key = widget.id) { _ ->
                        Box(modifier = Modifier.fillMaxWidth()) {
                            SettingsSegmentedItem(index = index, count = order.size, onClick = null) {
                                Icon(
                                    imageVector = Icons.Default.DragIndicator,
                                    contentDescription = "Arrastrar para reordenar",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .draggableHandle(
                                            onDragStarted = { haptics.click() },
                                            onDragStopped = { viewModel.setHomeOrder(order) }
                                        )
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                SettingsItemText(widget.title)
                                Switch(
                                    checked = widget !in layout.hidden,
                                    thumbContent = if (widget !in layout.hidden) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(SwitchDefaults.IconSize)) }
                                    } else null,
                                    onCheckedChange = {
                                        haptics.click()
                                        viewModel.setHomeWidgetHidden(widget, hidden = !it)
                                    }
                                )
                            }
                        }
                    }
                }
            }
            item(key = "note_widgets") {
                MinicashSectionNote("Mantén pulsados los puntos y arrastra para cambiar el orden.")
            }

            item(key = "label_distribution") {
                MinicashSectionLabel("Distribución del mes", modifier = Modifier.padding(top = 14.dp))
            }
            item(key = "distribution_options") {
                val styles = DistributionStyle.entries
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    styles.chunked(2).forEach { pair ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            pair.forEach { style ->
                                DistributionOptionCard(
                                    style = style,
                                    selected = layout.distributionStyle == style,
                                    onClick = {
                                        haptics.click()
                                        viewModel.setDistributionStyle(style)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            item(key = "label_patrimony") {
                MinicashSectionLabel("Patrimonio", modifier = Modifier.padding(top = 14.dp))
            }
            item(key = "patrimony_choice") {
                MinicashSegmentedChoice(
                    options = PatrimonyStyle.entries.toList(),
                    isSelected = { it == layout.patrimonyStyle },
                    onSelect = {
                        haptics.click()
                        viewModel.setPatrimonyStyle(it)
                    },
                    label = { it.displayName }
                )
            }
            item(key = "note_patrimony") {
                MinicashSectionNote("La franja ocupa una línea; la tarjeta muestra el total en grande.")
            }
        }
    }
}

@Composable
private fun DistributionOptionCard(
    style: DistributionStyle,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = MaterialTheme.colorScheme.primary
    val soft = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    val softer = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    Surface(
        onClick = onClick,
        shape = MinicashShapes.Card,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.cardSurface,
        border = if (selected) BorderStroke(2.dp, accent) else null,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .drawBehind { drawDistributionThumbnail(style, accent, soft, softer) }
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = style.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (selected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Elegido",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/** Miniatura esquemática de cada estilo, dibujada con los colores del tema. */
private fun DrawScope.drawDistributionThumbnail(style: DistributionStyle, accent: Color, soft: Color, softer: Color) {
    val w = size.width
    val h = size.height
    when (style) {
        DistributionStyle.DONUT -> {
            val d = h * 0.92f
            val topLeft = Offset((w - d) / 2f, (h - d) / 2f)
            val stroke = Stroke(width = d * 0.2f, cap = StrokeCap.Butt)
            val box = Size(d - stroke.width, d - stroke.width)
            val tl = Offset(topLeft.x + stroke.width / 2f, topLeft.y + stroke.width / 2f)
            drawArc(softer, 0f, 360f, false, tl, box, style = stroke)
            drawArc(accent, -90f, 160f, false, tl, box, style = stroke)
            drawArc(soft, 74f, 110f, false, tl, box, style = stroke)
        }
        DistributionStyle.BARS -> {
            val fractions = listOf(0.9f, 0.62f, 0.4f, 0.22f)
            val rowH = h / (fractions.size * 2f - 1f)
            fractions.forEachIndexed { i, f ->
                val y = i * rowH * 2f
                drawRoundRect(softer, Offset(0f, y), Size(w, rowH), CornerRadius(rowH / 2f))
                drawRoundRect(if (i == 0) accent else soft, Offset(0f, y), Size(w * f, rowH), CornerRadius(rowH / 2f))
            }
        }
        DistributionStyle.STACKED -> {
            val barH = h * 0.42f
            val y = (h - barH) / 2f
            val parts = listOf(0.42f to accent, 0.26f to soft, 0.18f to softer, 0.14f to soft.copy(alpha = 0.6f))
            var x = 0f
            parts.forEach { (f, c) ->
                drawRect(c, Offset(x, y), Size(w * f - 2f, barH))
                x += w * f
            }
        }
        DistributionStyle.TOP5 -> {
            val rows = 3
            val rowH = h / (rows * 2f - 1f)
            repeat(rows) { i ->
                val y = i * rowH * 2f
                drawCircle(if (i == 0) accent else soft, radius = rowH / 2f, center = Offset(rowH / 2f, y + rowH / 2f))
                drawRoundRect(softer, Offset(rowH * 1.6f, y + rowH * 0.2f), Size(w * 0.5f, rowH * 0.6f), CornerRadius(rowH))
                drawRoundRect(if (i == 0) accent else soft, Offset(w - w * 0.22f, y + rowH * 0.1f), Size(w * 0.22f, rowH * 0.8f), CornerRadius(rowH))
            }
        }
    }
}
