package com.uramos.minicash.ui.screens.settings

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.uramos.minicash.ui.components.fadingTopEdge
import com.uramos.minicash.ui.components.MinicashSubScreenTopBar
import com.uramos.minicash.ui.components.MinicashSegmentedChoice
import com.uramos.minicash.ui.components.MinicashIconButton
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

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val fadeListState = rememberLazyListState()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        topBar = {
            MinicashSubScreenTopBar(
                title = "Apariencia",
                onBack = onBackClick,
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = fadeListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .fadingTopEdge(fadeListState),
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
                        MinicashSegmentedChoice(
                            options = listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK),
                            isSelected = { state.themeMode == it },
                            onSelect = {
                                haptics.click()
                                viewModel.setThemeMode(it)
                            },
                            label = { it.displayName }
                        )

                        Spacer(modifier = Modifier.height(spacing.small))

                        // Selector de Familia Tipográfica
                        Text(
                            text = "Familia tipográfica",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        MinicashSegmentedChoice(
                            options = listOf(FontPreference.ROUNDED_QUICKSAND, FontPreference.GOOGLE_SANS_FLEX),
                            isSelected = { state.fontPreference == it },
                            onSelect = {
                                haptics.tick()
                                viewModel.setFontPreference(it)
                            },
                            label = { if (it == FontPreference.ROUNDED_QUICKSAND) "Redondeada" else "Google Sans Flex" }
                        )

                        Spacer(modifier = Modifier.height(spacing.small))

                        // Selector de estilo de barra de navegación
                        Text(
                            text = "Barra de navegación",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val navBarStyle by viewModel.navBarStyle.collectAsState()
                        MinicashSegmentedChoice(
                            options = listOf(NavBarStyle.INTEGRATED, NavBarStyle.FLOATING),
                            isSelected = { navBarStyle == it },
                            onSelect = {
                                haptics.tick()
                                viewModel.setNavBarStyle(it)
                            },
                            label = { if (it == NavBarStyle.INTEGRATED) "Integrada" else "Flotante" }
                        )

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
