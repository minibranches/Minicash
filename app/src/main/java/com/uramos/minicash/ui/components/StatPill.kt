package com.uramos.minicash.ui.components

import androidx.compose.runtime.remember
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.uramos.minicash.ui.theme.trackSurface

/**
 * Estadio con una cifra grande y su etiqueta. Con [progress] (0 a 1) el avance se pinta como relleno del propio
 * estadio. La cifra la dibuja [value], que recibe el estilo y el color para que montos y porcentajes se vean igual.
 */
@Composable
fun StatPill(
    label: String,
    modifier: Modifier = Modifier,
    progress: Float? = null,
    valueColor: Color = MaterialTheme.colorScheme.primary,
    /** Si se indica, el estadio es un botón (p. ej. filtro rápido). */
    onClick: (() -> Unit)? = null,
    selected: Boolean = false,
    /** Versión angosta para tres estadios en una fila: menos relleno y cifra más pequeña. */
    compact: Boolean = false,
    value: @Composable (style: TextStyle, color: Color) -> Unit
) {
    val ring = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.trackSurface
    val fill = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .then(if (onClick != null) Modifier.bounceOnPress(interactionSource) else Modifier)
            .clip(CircleShape)
            .then(
                if (onClick != null) Modifier
                    .semantics { stateDescription = if (selected) "Filtro activo" else "Filtro inactivo" }
                    .clickable(
                        interactionSource = interactionSource,
                        indication = LocalIndication.current,
                        role = Role.Button,
                        onClick = onClick
                    )
                else Modifier
            )
            .background(track)
            .drawBehind {
                if (progress != null) {
                    drawRect(color = fill, size = Size(size.width * progress.coerceIn(0f, 1f), size.height))
                }
            }
            .then(if (selected) Modifier.border(2.dp, ring, CircleShape) else Modifier)
            .padding(horizontal = if (compact) 8.dp else 12.dp, vertical = if (compact) 10.dp else 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            value(
                (if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge)
                    .copy(fontWeight = FontWeight.Bold, fontFeatureSettings = "tnum"),
                valueColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** [StatPill] para un monto en centavos; el signo y el color los pone [MoneyTextView] según [type]. */
@Composable
fun StatPillMoney(
    label: String,
    amountCents: Long,
    type: MoneyDisplayType,
    modifier: Modifier = Modifier,
    progress: Float? = null,
    onClick: (() -> Unit)? = null,
    selected: Boolean = false,
    compact: Boolean = false
) {
    StatPill(label = label, modifier = modifier, progress = progress, onClick = onClick, selected = selected, compact = compact) { style, _ ->
        MoneyTextView(amountCents = amountCents, type = type, style = style, autoSizeMin = if (compact) 11.sp else null)
    }
}
