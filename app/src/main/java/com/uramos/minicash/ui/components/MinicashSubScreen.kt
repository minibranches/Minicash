package com.uramos.minicash.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import com.uramos.minicash.ui.theme.MinicashShapes
import com.uramos.minicash.ui.theme.cardSurface
import com.uramos.minicash.ui.theme.screenBackground
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Barra superior de las subpantallas: botón atrás en círculo y título grande que se colapsa al
 * desplazar la página. Requiere `Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)` en el Scaffold.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MinicashSubScreenTopBar(
    title: String,
    onBack: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    actions: @Composable RowScope.() -> Unit = {}
) {
    LargeFlexibleTopAppBar(
        windowInsets = WindowInsets(top = TopBarBreathing),
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = { MinicashBackButton(onClick = onBack) },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent
        ),
        scrollBehavior = scrollBehavior
    )
}

/** Encabezado de sección de las subpantallas, en el color primario como en los ajustes del sistema. */
@Composable
fun MinicashSectionLabel(text: String, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = modifier.padding(start = 8.dp, top = 8.dp, bottom = 4.dp)
    )
}

/** Texto explicativo bajo un grupo de opciones. */
@Composable
fun MinicashSectionNote(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

/** Grupo de filas con esquinas redondeadas y separación mínima entre filas. */
@Composable
fun MinicashOptionGroup(content: @Composable () -> Unit) {
    Surface(
        shape = MinicashShapes.Card,
        color = MaterialTheme.colorScheme.cardSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) { content() }
    }
}

/** Fila de opción: ícono en círculo tonal, título y subtítulo; `trailing` para interruptores o insignias. */
@Composable
fun MinicashOptionRow(
    title: String,
    subtitle: String?,
    icon: ImageVector,
    onClick: (() -> Unit)?,
    iconContainer: Color = MaterialTheme.colorScheme.secondaryContainer,
    iconTint: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    enabled: Boolean = true,
    trailing: (@Composable () -> Unit)? = null
) {
    val row: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ScallopedIcon(icon = icon, size = 40.dp, container = iconContainer, tint = iconTint)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (enabled) titleColor else titleColor.copy(alpha = 0.5f)
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            trailing?.invoke()
        }
    }
    if (onClick != null) {
        val interactionSource = remember { MutableInteractionSource() }
        Surface(
            onClick = onClick,
            enabled = enabled,
            color = Color.Transparent,
            interactionSource = interactionSource,
            modifier = Modifier.bounceOnPress(interactionSource, pressedScale = 0.97f)
        ) { row() }
    } else {
        row()
    }
}

/**
 * Velo de encabezado: cuando el contenido se desplaza, los primeros [height] bajo el título se tiñen del color
 * de fondo en degradado (como en Pixel Budget Tracker). En reposo no se ve nada; el título nunca desaparece.
 */
@Composable
fun Modifier.fadingTopEdge(state: LazyListState, height: Dp = SCRIM_HEIGHT): Modifier {
    val progress = remember(state) {
        derivedStateOf {
            if (state.firstVisibleItemIndex > 0) 1f
            else (state.firstVisibleItemScrollOffset / SCRIM_RAMP_PX).coerceIn(0f, 1f)
        }
    }
    return headerScrim(height) { progress.value }
}

@Composable
fun Modifier.fadingTopEdge(state: LazyGridState, height: Dp = SCRIM_HEIGHT): Modifier {
    val progress = remember(state) {
        derivedStateOf {
            if (state.firstVisibleItemIndex > 0) 1f
            else (state.firstVisibleItemScrollOffset / SCRIM_RAMP_PX).coerceIn(0f, 1f)
        }
    }
    return headerScrim(height) { progress.value }
}

@Composable
private fun Modifier.headerScrim(height: Dp, progress: () -> Float): Modifier {
    val color = MaterialTheme.colorScheme.screenBackground
    return drawWithContent {
        drawContent()
        val strength = progress()
        if (strength > 0f) {
            val h = height.toPx()
            drawRect(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0f to color.copy(alpha = SCRIM_MAX_ALPHA * strength),
                        0.45f to color.copy(alpha = SCRIM_MAX_ALPHA * strength * 0.5f),
                        1f to Color.Transparent
                    ),
                    startY = 0f,
                    endY = h
                ),
                size = Size(size.width, h)
            )
        }
    }
}

private val SCRIM_HEIGHT = 36.dp
private const val SCRIM_RAMP_PX = 60f
private const val SCRIM_MAX_ALPHA = 0.8f

/** Botón de atrás: zona táctil de 48 dp (M3) con el círculo tonal visible de 40 dp. */
@Composable
fun MinicashBackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    MinicashIconButton(onClick = onClick, modifier = modifier.padding(start = 4.dp).size(48.dp)) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.size(40.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/** Engrane fijo de las barras superiores de las pestañas: abre Ajustes. Zona táctil de 48 dp, círculo de 40 dp. */
@Composable
fun SettingsGearButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    MinicashIconButton(onClick = onClick, modifier = modifier.padding(end = 4.dp).size(48.dp)) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.size(40.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Ajustes",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/** Botón de acción de las barras superiores de las pestañas: círculo tonal de 40 dp con zona táctil de 48 dp. */
@Composable
fun TabActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    MinicashIconButton(onClick = onClick, modifier = modifier.padding(end = 4.dp).size(48.dp)) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.size(40.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = contentDescription, tint = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

/** Botón de Reportes de las barras superiores de las pestañas. */
@Composable
fun ReportsButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    TabActionButton(Icons.Outlined.BarChart, "Reportes", onClick, modifier)
}

/** Reportes y Ajustes, juntos arriba a la derecha de todas las pestañas. */
@Composable
fun RowScope.TabTopBarActions(onOpenReports: () -> Unit, onOpenSettings: () -> Unit) {
    ReportsButton(onClick = onOpenReports)
    SettingsGearButton(onClick = onOpenSettings)
}
