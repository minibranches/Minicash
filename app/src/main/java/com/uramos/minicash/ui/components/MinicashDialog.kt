package com.uramos.minicash.ui.components

import androidx.compose.ui.unit.IntSize
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.uramos.minicash.ui.theme.MinicashShapes
import com.uramos.minicash.ui.theme.minicashEffectsSpec
import com.uramos.minicash.ui.theme.minicashSpatialSpec

/**
 * Diálogo de alerta con la misma estructura que el de Material 3, pero que aparece con un fundido corto y se
 * expande en vertical. M3 evita la escala en Android porque sugiere un cambio de elevación.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinicashAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    shape: Shape = MinicashShapes.Hero,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh
) {
    val visibleState = remember { MutableTransitionState(false).apply { targetState = true } }
    val fadeSpec = minicashEffectsSpec<Float>()
    val expandSpec = minicashSpatialSpec<IntSize>()

    BasicAlertDialog(onDismissRequest = onDismissRequest, modifier = modifier) {
        AnimatedVisibility(
            visibleState = visibleState,
            enter = fadeIn(animationSpec = fadeSpec) + expandVertically(animationSpec = expandSpec, expandFrom = Alignment.CenterVertically)
        ) {
            Surface(
                shape = shape,
                color = containerColor,
                modifier = Modifier.widthIn(min = 280.dp, max = 560.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    if (title != null) {
                        ProvideTextStyle(MaterialTheme.typography.headlineSmall) {
                            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                                Box(modifier = Modifier.padding(bottom = 16.dp)) { title() }
                            }
                        }
                    }
                    if (text != null) {
                        ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
                                Box(modifier = Modifier.padding(bottom = 24.dp)) { text() }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        dismissButton?.invoke()
                        confirmButton()
                    }
                }
            }
        }
    }
}