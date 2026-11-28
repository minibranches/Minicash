package com.uramos.minicash.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.uramos.minicash.ui.theme.cardSurface

/**
 * Medidas de las filas de lista de toda la app, tomadas de las filas de Inicio y Movimientos: alto mínimo de 72 dp,
 * 16 dp de relleno lateral, 12 dp entre ícono y texto, 4 dp entre filas y esquinas segmentadas (28 dp en los
 * extremos del grupo, 4 dp entre filas).
 */
object MinicashListRowDefaults {
    val MinHeight = 72.dp
    val HorizontalPadding = 16.dp
    val VerticalPadding = 12.dp
    val IconTextGap = 12.dp
    val RowGap = 4.dp
    val IconSize = 40.dp
    val PressedScale = 0.97f
}

/** Grupo de filas segmentadas: 4 dp visibles entre filas. */
@Composable
fun MinicashRowGroup(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(MinicashListRowDefaults.RowGap), content = content)
}

/**
 * Contenedor de una fila segmentada. [index] y [count] dicen su lugar en el grupo para redondear las esquinas;
 * con [onClick] la fila se encoge un poco al tocarla y vuelve con rebote.
 */
@Composable
fun MinicashListRow(
    index: Int,
    count: Int,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.cardSurface,
    content: @Composable RowScope.() -> Unit
) {
    val shape = rememberSegmentedShape(index = index, count = count)
    val inner: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = MinicashListRowDefaults.MinHeight)
                .padding(
                    horizontal = MinicashListRowDefaults.HorizontalPadding,
                    vertical = MinicashListRowDefaults.VerticalPadding
                ),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
    if (onClick != null) {
        val interactionSource = remember { MutableInteractionSource() }
        Surface(
            onClick = onClick,
            enabled = enabled,
            shape = shape,
            color = color,
            interactionSource = interactionSource,
            modifier = modifier
                .fillMaxWidth()
                .bounceOnPress(interactionSource, MinicashListRowDefaults.PressedScale)
        ) { inner() }
    } else {
        Surface(shape = shape, color = color, modifier = modifier.fillMaxWidth()) { inner() }
    }
}

/** Título de una línea y, si hace falta, una línea de apoyo, pegadas como en las filas de movimientos. */
@Composable
fun RowScope.MinicashRowText(
    title: String,
    subtitle: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(modifier = Modifier.weight(1f)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = titleColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
