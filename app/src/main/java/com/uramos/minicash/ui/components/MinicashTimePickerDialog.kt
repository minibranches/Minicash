package com.uramos.minicash.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private val WHEEL_ITEM_HEIGHT = 52.dp
private const val WHEEL_VISIBLE_ITEMS = 3

/**
 * Selector de hora con dos ruedas (hora y minutos) y botones AM/PM, como el de las referencias de diseño.
 * Trabaja con hora de 24 h hacia afuera: [initialHour] y el resultado de [onConfirm] van de 0 a 23.
 */
@Composable
fun MinicashTimePickerDialog(
    title: String,
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val hours = remember { (1..12).map { it.toString().padStart(2, '0') } }
    val minutes = remember { (0..59).map { it.toString().padStart(2, '0') } }
    val hourState = rememberLazyListState(initialFirstVisibleItemIndex = hour12Of(initialHour) - 1)
    val minuteState = rememberLazyListState(initialFirstVisibleItemIndex = initialMinute.coerceIn(0, 59))
    var isPm by remember { mutableStateOf(initialHour >= 12) }

    val selectedHour12 by remember { derivedStateOf { centeredIndex(hourState) + 1 } }
    val selectedMinute by remember { derivedStateOf { centeredIndex(minuteState) } }

    MinicashAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimeWheel(
                        items = hours,
                        state = hourState,
                        description = "Hora",
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = ":",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TimeWheel(
                        items = minutes,
                        state = minuteState,
                        description = "Minutos",
                        modifier = Modifier.weight(1f)
                    )
                }
                MinicashSegmentedChoice(
                    options = listOf(false, true),
                    isSelected = { it == isPm },
                    onSelect = { isPm = it },
                    label = { if (it) "PM" else "AM" }
                )
            }
        },
        confirmButton = {
            MinicashFilledButton(
                onClick = { onConfirm(hour24Of(selectedHour12, isPm), selectedMinute) }
            ) { Text("Guardar", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            MinicashTextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

/** Rueda vertical con ajuste automático: el elemento del centro es el elegido y se muestra más grande. */
@Composable
private fun TimeWheel(
    items: List<String>,
    state: androidx.compose.foundation.lazy.LazyListState,
    description: String,
    modifier: Modifier = Modifier
) {
    val centered by remember { derivedStateOf { centeredIndex(state) } }
    Box(
        modifier = modifier
            .height(WHEEL_ITEM_HEIGHT * WHEEL_VISIBLE_ITEMS)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = state,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = state),
            contentPadding = PaddingValues(vertical = WHEEL_ITEM_HEIGHT),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(items) { index, label ->
                val selected = index == centered
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(WHEEL_ITEM_HEIGHT),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = if (selected) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** Índice del elemento que está en el centro de la rueda (el primero visible, o el siguiente si ya pasó la mitad). */
private fun centeredIndex(state: androidx.compose.foundation.lazy.LazyListState): Int {
    val half = state.layoutInfo.visibleItemsInfo.firstOrNull()?.size?.div(2) ?: 0
    return state.firstVisibleItemIndex + if (state.firstVisibleItemScrollOffset > half) 1 else 0
}

/** Hora de 24 h (0 a 23) a la hora de 1 a 12 que se muestra. */
internal fun hour12Of(hour24: Int): Int = ((hour24 % 12).let { if (it == 0) 12 else it })

/** Hora de 1 a 12 con AM o PM a hora de 24 h. */
internal fun hour24Of(hour12: Int, pm: Boolean): Int = (hour12 % 12) + if (pm) 12 else 0
