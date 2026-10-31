package com.uramos.minicash.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonShapes
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import com.uramos.minicash.ui.theme.minicashSpatialSpec
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

/**
 * Botones M3 Expressive del proyecto: forma que se transforma al presionar, altura mediana y texto
 * de una sola línea (no se parte en pantallas angostas ni con fuente grande).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MinicashButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shapes = rememberMorphingShapes(enabled),
        contentPadding = ButtonDefaults.contentPaddingFor(ButtonDefaults.MediumContainerHeight),
        modifier = modifier.heightIn(min = ButtonDefaults.MediumContainerHeight)
    ) {
        Text(text, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, autoSize = buttonTextAutoSize)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MinicashTonalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        shapes = rememberMorphingShapes(enabled),
        contentPadding = ButtonDefaults.contentPaddingFor(ButtonDefaults.MediumContainerHeight),
        modifier = modifier.heightIn(min = ButtonDefaults.MediumContainerHeight)
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, autoSize = buttonTextAutoSize)
    }
}

/** Selector de una opción entre varias, como grupo conectado de botones de alternancia. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> MinicashSegmentedChoice(
    options: List<T>,
    isSelected: (T) -> Boolean,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
    ) {
        options.forEachIndexed { index, option ->
            ChoiceButton(
                checked = isSelected(option),
                onCheckedChange = { onSelect(option) },
                text = label(option),
                first = index == 0,
                last = index == options.lastIndex
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RowScope.ChoiceButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    text: String,
    first: Boolean,
    last: Boolean
) {
    ToggleButton(
        checked = checked,
        onCheckedChange = onCheckedChange,
        shapes = when {
            first -> ButtonGroupDefaults.connectedLeadingButtonShapes()
            last -> ButtonGroupDefaults.connectedTrailingButtonShapes()
            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
        },
        modifier = Modifier.weight(1f)
    ) {
        Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/**
 * Forma del botón: cuadrado redondeado mientras está deshabilitado y píldora al habilitarse,
 * con resorte expresivo. Al presionar, [ButtonDefaults.shapes] aplica su propia forma de pulsación.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun rememberMorphingShapes(enabled: Boolean): ButtonShapes {
    val radius by animateDpAsState(
        targetValue = if (enabled) 28.dp else 14.dp,
        animationSpec = minicashSpatialSpec(),
        label = "button_corner_morph"
    )
    return ButtonDefaults.shapes(shape = RoundedCornerShape(radius))
}

/** El texto se reduce (hasta 11 sp) antes de truncarse, para pantallas angostas o con fuente grande. */
private val buttonTextAutoSize = TextAutoSize.StepBased(minFontSize = 11.sp, maxFontSize = 14.sp)
