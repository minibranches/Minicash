package com.uramos.minicash.ui.components

import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.uramos.minicash.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uramos.minicash.ui.theme.minicashEffectsSpec
import com.uramos.minicash.ui.theme.minicashFastEffectsSpec
import com.uramos.minicash.ui.theme.minicashSpatialSpec

/**
 * Botón de filtros que cambia de forma: sin filtros es un ícono redondo de 48 dp; con filtros aplicados crece con
 * un resorte hasta una píldora «N filtros ×». Tocar el ícono o el texto abre la hoja de filtros ([onClick]); la
 * × los limpia todos ([onClear]).
 */
@Composable
fun ExpandingFilterButton(
    activeCount: Int,
    onClick: () -> Unit,
    onClear: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val active = activeCount > 0
    val container = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
    val content = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        shape = CircleShape,
        color = container,
        contentColor = content,
        modifier = modifier
            .animateContentSize(minicashSpatialSpec())
            .height(48.dp)
            .defaultMinSize(minWidth = 48.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(48.dp)
                    .clickable(role = Role.Button, onClick = onClick)
            ) {
                Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.FilterList, contentDescription = contentDescription)
                }
                AnimatedVisibility(
                    visible = active,
                    enter = fadeIn(minicashEffectsSpec()) + expandHorizontally(minicashSpatialSpec()),
                    exit = fadeOut(minicashFastEffectsSpec()) + shrinkHorizontally(minicashSpatialSpec())
                ) {
                    Text(
                        text = pluralStringResource(R.plurals.filter_count, activeCount, activeCount),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }
            AnimatedVisibility(
                visible = active,
                enter = fadeIn(minicashEffectsSpec()) + expandHorizontally(minicashSpatialSpec()),
                exit = fadeOut(minicashFastEffectsSpec()) + shrinkHorizontally(minicashSpatialSpec())
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable(role = Role.Button, onClick = onClear),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.filters_clear), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
