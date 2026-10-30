package com.uramos.minicash.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.uramos.minicash.ui.theme.minicashEffectsSpec

private val SHORT_MONTHS = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
private const val JUMP_MONTHS = 1

/**
 * Selector de periodo en una sola fila: año (menú), flecha, mes anterior, mes actual, mes siguiente y flecha.
 * Tocar el mes anterior o siguiente, o las flechas, mueve el periodo un mes (cruzando de año si hace falta).
 */
@Composable
fun MinicashPeriodSelector(
    year: Int,
    monthIndex: Int,
    years: List<Int>,
    onYearSelect: (Int) -> Unit,
    onShiftMonths: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier.fillMaxWidth()
    ) {
        WithoutMinTouchTarget {
            Row(
                modifier = Modifier.padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                YearSelect(year = year, years = years, onYearSelect = onYearSelect)

                PeriodArrow(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    description = "Mes anterior",
                    onClick = { onShiftMonths(-JUMP_MONTHS) }
                )

                MonthPill(
                    label = SHORT_MONTHS[(monthIndex + 11) % 12],
                    selected = false,
                    onClick = { onShiftMonths(-1) },
                    modifier = Modifier.weight(1f)
                )
                MonthPill(
                    label = SHORT_MONTHS[monthIndex],
                    selected = true,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                MonthPill(
                    label = SHORT_MONTHS[(monthIndex + 1) % 12],
                    selected = false,
                    onClick = { onShiftMonths(1) },
                    modifier = Modifier.weight(1f)
                )

                PeriodArrow(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    description = "Mes siguiente",
                    onClick = { onShiftMonths(JUMP_MONTHS) }
                )
            }
        }
    }
}

@Composable
private fun YearSelect(year: Int, years: List<Int>, onYearSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Surface(
            onClick = { expanded = true },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.height(40.dp)
        ) {
            Row(
                modifier = Modifier.padding(start = 14.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = year.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Elegir año",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            years.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option.toString(),
                            fontWeight = if (option == year) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        expanded = false
                        onYearSelect(option)
                    }
                )
            }
        }
    }
}

@Composable
private fun PeriodArrow(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
        Icon(icon, contentDescription = description, modifier = Modifier.size(22.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MonthPill(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val color by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
        animationSpec = minicashEffectsSpec(),
        label = "period_month_color"
    )
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = color,
        modifier = modifier.height(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
