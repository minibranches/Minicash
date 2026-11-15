package com.uramos.minicash.ui.components

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Indicador de carga de M3 Expressive, para esperas de entre 200 ms y 5 s (restaurar una copia, cargar una vista
 * previa, abrir la pantalla). El circular clásico queda para esperas largas con progreso conocido.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MinicashLoading(modifier: Modifier = Modifier) {
    LoadingIndicator(modifier = modifier)
}
