package com.uramos.minicash.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.uramos.minicash.ui.utils.rememberMinicashHaptics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.uramos.minicash.data.preferences.FontPreference
import com.uramos.minicash.data.preferences.NavBarStyle
import com.uramos.minicash.data.preferences.ThemeMode
import com.uramos.minicash.ui.components.CardTonalLevel
import com.uramos.minicash.ui.components.MinicashCard
import com.uramos.minicash.ui.theme.MinicashThemeExtensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit
) {
    val spacing = MinicashThemeExtensions.spacing
    val state by viewModel.uiState.collectAsState()
    val haptics = rememberMinicashHaptics()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                ),
                navigationIcon = {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .padding(start = spacing.small)
                            .size(40.dp)
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                title = {
                    Text(
                        text = "Apariencia",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                horizontal = spacing.standard,
                vertical = spacing.small
            ),
            verticalArrangement = Arrangement.spacedBy(spacing.medium)
        ) {
            item {
                MinicashCard(
                    level = CardTonalLevel.LOW,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.standard),
                        verticalArrangement = Arrangement.spacedBy(spacing.medium)
                    ) {
                        Text(
                            text = "Tema de la aplicación",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // Selector de Tema de la Aplicación
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val modes = listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK)
                            modes.forEachIndexed { index, mode ->
                                SegmentedButton(
                                    selected = state.themeMode == mode,
                                    onClick = {
                                        haptics.click()
                                        viewModel.setThemeMode(mode)
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
                                    label = {
                                        Text(
                                            text = mode.displayName,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(spacing.small))

                        // Selector de Familia Tipográfica
                        Text(
                            text = "Familia tipográfica",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val fonts = listOf(
                                FontPreference.ROUNDED_QUICKSAND to "Redondeada",
                                FontPreference.GOOGLE_SANS_FLEX to "Google Sans Flex"
                            )
                            fonts.forEachIndexed { index, (pref, label) ->
                                SegmentedButton(
                                    selected = state.fontPreference == pref,
                                    onClick = {
                                        haptics.tick()
                                        viewModel.setFontPreference(pref)
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = fonts.size),
                                    label = {
                                        Text(
                                            text = label,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(spacing.small))

                        // Selector de estilo de barra de navegación
                        Text(
                            text = "Barra de navegación",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val navBarStyle by viewModel.navBarStyle.collectAsState()
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            val styles = listOf(NavBarStyle.INTEGRATED to "Integrada", NavBarStyle.FLOATING to "Flotante")
                            styles.forEachIndexed { index, (style, label) ->
                                SegmentedButton(
                                    selected = navBarStyle == style,
                                    onClick = {
                                        haptics.tick()
                                        viewModel.setNavBarStyle(style)
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = styles.size),
                                    label = { Text(label, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(spacing.small))

                        // Fila: Color dinámico
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Color dinámico",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Adaptar al fondo de pantalla del sistema",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = state.useDynamicColor,
                                thumbContent = if (state.useDynamicColor) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(SwitchDefaults.IconSize)) }
                                } else null,
                                onCheckedChange = {
                                    haptics.click()
                                    viewModel.setDynamicColor(it)
                                }
                            )
                        }

                        // Sección: Color de acento de la app (cuando no es dinámico)
                        AnimatedVisibility(visible = !state.useDynamicColor) {
                            Column {
                                Spacer(modifier = Modifier.height(spacing.small))

                                Text(
                                    text = "Color de acento de la app",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(spacing.small))

                                val presetColors = remember {
                                    listOf(
                                        "#1A73E8" to "Azul Google",
                                        "#4FC3F7" to "Celeste Cielo Pastel",
                                        "#2E7D32" to "Verde Esmeralda",
                                        "#80CBC4" to "Menta Suave",
                                        "#3F51B5" to "Índigo",
                                        "#BA68C8" to "Lila Suave Pastel",
                                        "#F06292" to "Rosa Pastel",
                                        "#C2185B" to "Rosa Frambuesa",
                                        "#F57C00" to "Naranja Cálido",
                                        "#FFB74D" to "Ámbar Melocotón",
                                        "#7B1FA2" to "Violeta Profundo",
                                        "#00838F" to "Teal"
                                    )
                                }

                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    items(presetColors, key = { it.first }) { (hex, name) ->
                                        val color = remember(hex) { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(hex)) }
                                        val isSelected = state.accentColorHex.equals(hex, ignoreCase = true)
                                        val contentColor = remember(color) {
                                            com.uramos.minicash.ui.utils.MinicashColorUtils.getContrastingContentColor(color, isDarkTheme = false)
                                        }

                                        Surface(
                                            onClick = {
                                                haptics.tick()
                                                viewModel.setAccentColor(hex)
                                            },
                                            shape = CircleShape,
                                            color = color,
                                            border = if (isSelected) androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface) else null,
                                            modifier = Modifier.size(44.dp)
                                        ) {
                                            if (isSelected) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = name,
                                                        tint = contentColor,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Fila: Modo negro puro (AMOLED)
                        AnimatedVisibility(visible = state.themeMode != ThemeMode.LIGHT) {
                            Column {
                                Spacer(modifier = Modifier.height(spacing.small))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Negro puro",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "Fondo negro absoluto para pantallas OLED en tema oscuro",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = state.isAmoledMode,
                                        thumbContent = if (state.isAmoledMode) {
                                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(SwitchDefaults.IconSize)) }
                                        } else null,
                                        onCheckedChange = {
                                            haptics.click()
                                            viewModel.setAmoledMode(it)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
