package com.uramos.minicash.ui.theme

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> minicashSpatialSpec(): FiniteAnimationSpec<T> =
    if (LocalAnimationRestriction.current) snap() else MaterialTheme.motionScheme.defaultSpatialSpec()

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> minicashEffectsSpec(): FiniteAnimationSpec<T> =
    if (LocalAnimationRestriction.current) snap() else MaterialTheme.motionScheme.defaultEffectsSpec()

/** Resorte espacial rápido: para elementos pequeños (íconos de la barra, interruptores). */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> minicashFastSpatialSpec(): FiniteAnimationSpec<T> =
    if (LocalAnimationRestriction.current) snap() else MaterialTheme.motionScheme.fastSpatialSpec()

/** Resorte de efectos rápido (opacidad y color): amortiguamiento 1.0, sin rebote. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> minicashFastEffectsSpec(): FiniteAnimationSpec<T> =
    if (LocalAnimationRestriction.current) snap() else MaterialTheme.motionScheme.fastEffectsSpec()
