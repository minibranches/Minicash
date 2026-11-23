package com.uramos.minicash.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.uramos.minicash.ui.components.rememberSegmentedShape
import com.uramos.minicash.ui.theme.MinicashShapes
import com.uramos.minicash.ui.theme.cardSurface

/** Grupo de filas segmentadas separadas por 2 dp: las esquinas exteriores son grandes y las interiores pequeñas. */
@Composable
internal fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp), content = content)
}

/** Contenedor de una fila segmentada. */
@Composable
internal fun SettingsSegmentedItem(
    index: Int,
    count: Int,
    onClick: (() -> Unit)?,
    content: @Composable RowScope.() -> Unit
) {
    val shape = rememberSegmentedShape(index = index, count = count)
    val inner: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
    if (onClick != null) {
        Surface(onClick = onClick, shape = shape, color = MaterialTheme.colorScheme.cardSurface, modifier = Modifier.fillMaxWidth()) { inner() }
    } else {
        Surface(shape = shape, color = MaterialTheme.colorScheme.cardSurface, modifier = Modifier.fillMaxWidth()) { inner() }
    }
}

/** Título de una línea y, si hace falta, una línea de apoyo. */
@Composable
internal fun RowScope.SettingsItemText(title: String, subtitle: String? = null) {
    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Fila que lleva a otra pantalla o ejecuta una acción: ícono, título de una línea (con el dato actual debajo, si lo hay) y flecha alineada a la derecha. */
@Composable
internal fun SettingsRowItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    index: Int = 0,
    count: Int = 1,
    value: String? = null,
    subtitle: String? = null,
    showChevron: Boolean = true
) {
    SettingsSegmentedItem(index = index, count = count, onClick = onClick) {
        SettingsLeadingIcon(icon)
        Spacer(modifier = Modifier.width(14.dp))
        SettingsItemText(title, subtitle ?: value)
        if (showChevron) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/** Fila con interruptor: tocar cualquier parte de la fila lo cambia, igual que en los ajustes del sistema. */
@Composable
internal fun SettingsSwitchItem(
    title: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    index: Int,
    count: Int,
    subtitle: String? = null
) {
    SettingsSegmentedItem(index = index, count = count, onClick = { onCheckedChange(!checked) }) {
        SettingsLeadingIcon(icon)
        Spacer(modifier = Modifier.width(14.dp))
        SettingsItemText(title, subtitle)
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            thumbContent = if (checked) {
                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(SwitchDefaults.IconSize)) }
            } else null,
            onCheckedChange = onCheckedChange
        )
    }
}

/** Ícono inicial de cada fila de ajustes, en la misma forma expresiva que las categorías. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SettingsLeadingIcon(icon: ImageVector) {
    Surface(
        shape = MaterialShapes.Cookie9Sided.toShape(),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/** Tarjeta roja que avisa que las notificaciones están bloqueadas en el sistema y lleva a activarlas. */
@Composable
internal fun NotificationsDisabledCard(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MinicashShapes.Card,
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Default.NotificationsOff, contentDescription = null)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Notificaciones desactivadas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Toca para activarlas en la configuración del sistema. Hasta entonces, ningún aviso programado te llegará.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
