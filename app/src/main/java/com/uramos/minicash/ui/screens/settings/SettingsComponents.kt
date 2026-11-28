package com.uramos.minicash.ui.screens.settings

import androidx.compose.ui.res.stringResource
import com.uramos.minicash.R
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
import com.uramos.minicash.ui.components.MinicashListRow
import com.uramos.minicash.ui.components.MinicashListRowDefaults
import com.uramos.minicash.ui.components.MinicashRowGroup
import com.uramos.minicash.ui.components.MinicashRowText
import com.uramos.minicash.ui.components.ScallopedIcon
import com.uramos.minicash.ui.theme.MinicashShapes
import com.uramos.minicash.ui.theme.cardSurface

/** Grupo de filas segmentadas: 4 dp entre filas, como en Inicio y Movimientos. */
@Composable
internal fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    MinicashRowGroup(content = content)
}

/** Contenedor de una fila segmentada de Ajustes (ver [MinicashListRow]). */
@Composable
internal fun SettingsSegmentedItem(
    index: Int,
    count: Int,
    onClick: (() -> Unit)?,
    content: @Composable RowScope.() -> Unit
) = MinicashListRow(index = index, count = count, onClick = onClick, content = content)

/** Título de una línea y, si hace falta, una línea de apoyo. */
@Composable
internal fun RowScope.SettingsItemText(title: String, subtitle: String? = null) = MinicashRowText(title, subtitle)

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
        Spacer(modifier = Modifier.width(MinicashListRowDefaults.IconTextGap))
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
        Spacer(modifier = Modifier.width(MinicashListRowDefaults.IconTextGap))
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

/** Ícono inicial de cada fila de ajustes, en la misma forma expresiva que las categorías y los movimientos. */
@Composable
internal fun SettingsLeadingIcon(icon: ImageVector) = ScallopedIcon(icon = icon, size = MinicashListRowDefaults.IconSize)

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
                    text = stringResource(R.string.set_notif_off_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.set_notif_off_text),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
