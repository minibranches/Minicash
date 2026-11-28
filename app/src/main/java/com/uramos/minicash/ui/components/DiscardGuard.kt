package com.uramos.minicash.ui.components

import androidx.compose.ui.res.stringResource
import com.uramos.minicash.R
import com.uramos.minicash.ui.theme.dialogSurface
import com.uramos.minicash.ui.theme.MinicashShapes
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ColumnScope
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Protege un formulario en hoja inferior contra descartes accidentales: si hay cambios sin guardar,
 * el gesto de deslizar hacia abajo, el fondo, el botón atrás y [requestDismiss] piden confirmación
 * en lugar de cerrar. Cerrar tras guardar no pasa por aquí (se llama a `onDismiss` directo).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Stable
class DiscardGuard internal constructor(
    val sheetState: SheetState,
    private val isDirty: () -> Boolean,
    private val onDiscard: () -> Unit
) {
    internal var showConfirm by mutableStateOf(false)

    /** Verdadero si hay cambios sin guardar. Se lee en composición, así que la hoja reacciona al instante. */
    val hasChanges: Boolean get() = isDirty()

    /** Úsalo para `onDismissRequest` y para los botones "Cancelar". */
    fun requestDismiss() {
        if (isDirty()) showConfirm = true else onDiscard()
    }

    internal fun vetoSwipe() {
        showConfirm = true
    }

    internal fun discard() {
        showConfirm = false
        onDiscard()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberDiscardGuard(isDirty: Boolean, onDismiss: () -> Unit): DiscardGuard {
    val dirtyState = rememberUpdatedState(isDirty)
    val dismissState = rememberUpdatedState(onDismiss)
    val holder = remember { arrayOfNulls<DiscardGuard>(1) }
    // La lambda se crea una sola vez: rememberModalBottomSheetState reinicia el estado si cambia.
    val confirmValueChange = remember {
        { target: SheetValue ->
            val blocked = target == SheetValue.Hidden && dirtyState.value
            if (blocked) holder[0]?.vetoSwipe()
            !blocked
        }
    }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = confirmValueChange
    )
    return remember(sheetState) {
        DiscardGuard(sheetState, { dirtyState.value }, { dismissState.value() }).also { holder[0] = it }
    }
}

/** Diálogo de confirmación; colócalo junto al `ModalBottomSheet` del formulario. */
@Composable
fun DiscardChangesDialog(guard: DiscardGuard) {
    if (!guard.showConfirm) return
    MinicashAlertDialog(
        onDismissRequest = { guard.showConfirm = false },
        title = { Text(stringResource(R.string.discard_title), fontWeight = FontWeight.Bold) },
        text = { Text(stringResource(R.string.discard_text)) },
        confirmButton = {
            MinicashTextButton(onClick = { guard.discard() }) {
                Text(stringResource(R.string.discard_confirm), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            MinicashTextButton(onClick = { guard.showConfirm = false }) { Text(stringResource(R.string.discard_keep_editing)) }
        },
        shape = MinicashShapes.Hero,
        containerColor = MaterialTheme.colorScheme.dialogSurface
    )
}

/**
 * `ModalBottomSheet` para formularios con [DiscardGuard]. Con cambios sin guardar, el botón o gesto atrás
 * se intercepta aquí en lugar de cerrar la hoja: dejar que la hoja lo gestione hacía que intentara cerrarse
 * una y otra vez mientras el diálogo de confirmación estaba abierto (la hoja subía y bajaba).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuardedModalBottomSheet(
    guard: DiscardGuard,
    modifier: Modifier = Modifier,
    shape: Shape = BottomSheetDefaults.ExpandedShape,
    containerColor: Color = BottomSheetDefaults.ContainerColor,
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    contentWindowInsets: @Composable () -> WindowInsets = { BottomSheetDefaults.modalWindowInsets },
    content: @Composable ColumnScope.() -> Unit
) {
    val hasChanges = guard.hasChanges
    ModalBottomSheet(
        onDismissRequest = guard::requestDismiss,
        modifier = modifier,
        sheetState = guard.sheetState,
        shape = shape,
        containerColor = containerColor,
        dragHandle = dragHandle,
        contentWindowInsets = contentWindowInsets,
        properties = ModalBottomSheetProperties(shouldDismissOnBackPress = !hasChanges)
    ) {
        BackHandler(enabled = hasChanges) { guard.requestDismiss() }
        content()
    }
}
