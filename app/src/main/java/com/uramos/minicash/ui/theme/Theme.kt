package com.uramos.minicash.ui.theme

import androidx.compose.animation.core.spring
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import com.uramos.minicash.data.preferences.ThemeMode

val LocalAnimationRestriction = staticCompositionLocalOf { false }

val LocalFinancialColors = staticCompositionLocalOf<ExtendedFinancialColors> {
    error("ExtendedFinancialColors no provisto")
}

private val DarkFinancialColors = ExtendedFinancialColors(
    income = IncomeGreenDark,
    onIncome = OnIncomeGreenDark,
    incomeContainer = IncomeContainerDark,
    onIncomeContainer = OnIncomeContainerDark,
    expense = ErrorDark,
    onExpense = OnErrorDark,
    expenseContainer = ErrorContainerDark,
    onExpenseContainer = OnErrorContainerDark,
    neutral = OnSurfaceVariantDark,
    transfer = TertiaryDark
)

private val LightFinancialColors = ExtendedFinancialColors(
    income = IncomeGreenLight,
    onIncome = OnIncomeGreenLight,
    incomeContainer = IncomeContainerLight,
    onIncomeContainer = OnIncomeContainerLight,
    expense = ErrorLight,
    onExpense = OnErrorLight,
    expenseContainer = ErrorContainerLight,
    onExpenseContainer = OnErrorContainerLight,
    neutral = OnSurfaceVariantLight,
    transfer = TertiaryLight
)

object MinicashThemeExtensions {
    val financialColors: ExtendedFinancialColors
        @Composable
        @ReadOnlyComposable
        get() = LocalFinancialColors.current

    val spacing: Spacing
        @Composable
        @ReadOnlyComposable
        get() = LocalSpacing.current
}

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
    surfaceBright = SurfaceBrightDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,
    surfaceBright = SurfaceBrightLight
)

/**
 * Las superficies del esquema dinámico oscuro son casi neutras (se ven negras). Se les mezcla el color
 * primario del sistema para que conserven el matiz del fondo de pantalla, como ocurre con el acento propio.
 */
private fun tintDynamicDarkSurfaces(scheme: ColorScheme): ColorScheme {
    fun tint(base: Color, ratio: Float) =
        com.uramos.minicash.ui.utils.MinicashColorUtils.tintedContainerColor(scheme.primary, base, ratio)
    return scheme.copy(
        background = tint(scheme.background, 0.04f),
        surface = tint(scheme.surface, 0.04f),
        surfaceContainerLowest = tint(scheme.surfaceContainerLowest, 0.03f),
        surfaceContainerLow = tint(scheme.surfaceContainerLow, 0.05f),
        surfaceContainer = tint(scheme.surfaceContainer, 0.06f),
        surfaceContainerHigh = tint(scheme.surfaceContainerHigh, 0.08f),
        surfaceContainerHighest = tint(scheme.surfaceContainerHighest, 0.10f),
        surfaceBright = tint(scheme.surfaceBright, 0.12f)
    )
}

@Composable
fun ColorScheme.animateColors(
    animationSpec: AnimationSpec<Color> = spring(dampingRatio = 1f, stiffness = 800f)
): ColorScheme {
    val isRestricted = LocalAnimationRestriction.current
    val effectiveSpec = if (isRestricted) snap() else animationSpec

    @Composable
    fun anim(color: Color, label: String): Color =
        animateColorAsState(targetValue = color, animationSpec = effectiveSpec, label = label).value

    return this.copy(
        primary = anim(primary, "primary"),
        onPrimary = anim(onPrimary, "onPrimary"),
        primaryContainer = anim(primaryContainer, "primaryContainer"),
        onPrimaryContainer = anim(onPrimaryContainer, "onPrimaryContainer"),
        inversePrimary = anim(inversePrimary, "inversePrimary"),
        secondary = anim(secondary, "secondary"),
        onSecondary = anim(onSecondary, "onSecondary"),
        secondaryContainer = anim(secondaryContainer, "secondaryContainer"),
        onSecondaryContainer = anim(onSecondaryContainer, "onSecondaryContainer"),
        tertiary = anim(tertiary, "tertiary"),
        onTertiary = anim(onTertiary, "onTertiary"),
        tertiaryContainer = anim(tertiaryContainer, "tertiaryContainer"),
        onTertiaryContainer = anim(onTertiaryContainer, "onTertiaryContainer"),
        background = anim(background, "background"),
        onBackground = anim(onBackground, "onBackground"),
        surface = anim(surface, "surface"),
        onSurface = anim(onSurface, "onSurface"),
        surfaceVariant = anim(surfaceVariant, "surfaceVariant"),
        onSurfaceVariant = anim(onSurfaceVariant, "onSurfaceVariant"),
        surfaceTint = anim(surfaceTint, "surfaceTint"),
        inverseSurface = anim(inverseSurface, "inverseSurface"),
        inverseOnSurface = anim(inverseOnSurface, "inverseOnSurface"),
        error = anim(error, "error"),
        onError = anim(onError, "onError"),
        errorContainer = anim(errorContainer, "errorContainer"),
        onErrorContainer = anim(onErrorContainer, "onErrorContainer"),
        outline = anim(outline, "outline"),
        outlineVariant = anim(outlineVariant, "outlineVariant"),
        scrim = anim(scrim, "scrim"),
        surfaceBright = anim(surfaceBright, "surfaceBright"),
        surfaceDim = anim(surfaceDim, "surfaceDim"),
        surfaceContainer = anim(surfaceContainer, "surfaceContainer"),
        surfaceContainerHigh = anim(surfaceContainerHigh, "surfaceContainerHigh"),
        surfaceContainerHighest = anim(surfaceContainerHighest, "surfaceContainerHighest"),
        surfaceContainerLow = anim(surfaceContainerLow, "surfaceContainerLow"),
        surfaceContainerLowest = anim(surfaceContainerLowest, "surfaceContainerLowest")
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MinicashTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    isAmoledMode: Boolean = false,
    accentColorHex: String = "#1A73E8",
    fontPreference: com.uramos.minicash.data.preferences.FontPreference = com.uramos.minicash.data.preferences.FontPreference.ROUNDED_QUICKSAND,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val context = LocalContext.current
    val animationScale = remember(context) {
        try {
            android.provider.Settings.Global.getFloat(
                context.contentResolver,
                android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
        } catch (e: Exception) {
            1.0f
        }
    }
    val isAnimationRestricted = animationScale == 0f

    val customAccentColor = remember(accentColorHex) {
        try {
            Color(android.graphics.Color.parseColor(accentColorHex))
        } catch (e: Exception) {
            Color(0xFF1A73E8)
        }
    }

    val baseColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) tintDynamicDarkSurfaces(dynamicDarkColorScheme(context)) else dynamicLightColorScheme(context)
        }
        darkTheme -> {
            val primaryContainer = com.uramos.minicash.ui.utils.MinicashColorUtils.tintedContainerColor(customAccentColor, DarkColorScheme.surface, ratio = 0.28f)
            val secondaryContainer = com.uramos.minicash.ui.utils.MinicashColorUtils.tintedContainerColor(customAccentColor, DarkColorScheme.surface, ratio = 0.22f)
            val surfaceLowest = com.uramos.minicash.ui.utils.MinicashColorUtils.tintedContainerColor(customAccentColor, DarkColorScheme.surfaceContainerLowest, ratio = 0.04f)
            val surfaceLow = com.uramos.minicash.ui.utils.MinicashColorUtils.tintedContainerColor(customAccentColor, DarkColorScheme.surfaceContainerLow, ratio = 0.05f)
            val surfaceNorm = com.uramos.minicash.ui.utils.MinicashColorUtils.tintedContainerColor(customAccentColor, DarkColorScheme.surfaceContainer, ratio = 0.06f)
            val surfaceHigh = com.uramos.minicash.ui.utils.MinicashColorUtils.tintedContainerColor(customAccentColor, DarkColorScheme.surfaceContainerHigh, ratio = 0.08f)
            val surfaceHighest = com.uramos.minicash.ui.utils.MinicashColorUtils.tintedContainerColor(customAccentColor, DarkColorScheme.surfaceContainerHighest, ratio = 0.10f)
            val surfaceBright = com.uramos.minicash.ui.utils.MinicashColorUtils.tintedContainerColor(customAccentColor, DarkColorScheme.surfaceBright, ratio = 0.12f)
            val outlineVar = com.uramos.minicash.ui.utils.MinicashColorUtils.tintedContainerColor(customAccentColor, DarkColorScheme.outlineVariant, ratio = 0.20f)

            DarkColorScheme.copy(
                primary = customAccentColor,
                onPrimary = Color.White,
                primaryContainer = primaryContainer,
                onPrimaryContainer = Color.White,
                inversePrimary = customAccentColor,
                secondary = customAccentColor.copy(alpha = 0.85f),
                onSecondary = Color.White,
                secondaryContainer = secondaryContainer,
                onSecondaryContainer = Color.White,
                surfaceContainerLowest = surfaceLowest,
                surfaceContainerLow = surfaceLow,
                surfaceContainer = surfaceNorm,
                surfaceContainerHigh = surfaceHigh,
                surfaceContainerHighest = surfaceHighest,
                surfaceBright = surfaceBright,
                outlineVariant = outlineVar
            )
        }
        else -> {
            val primaryContainer = com.uramos.minicash.ui.utils.MinicashColorUtils.tintedContainerColor(customAccentColor, Color.White, ratio = 0.18f)
            val secondaryContainer = com.uramos.minicash.ui.utils.MinicashColorUtils.tintedContainerColor(customAccentColor, Color.White, ratio = 0.16f)
            val surfaceLowest = Color.White
            val surfaceLow = com.uramos.minicash.ui.utils.MinicashColorUtils.tintedContainerColor(customAccentColor, Color(0xFFF7F8F9), ratio = 0.08f)
            val surfaceNorm = com.uramos.minicash.ui.utils.MinicashColorUtils.tintedContainerColor(customAccentColor, Color(0xFFF1F3F5), ratio = 0.05f)
            val surfaceHigh = com.uramos.minicash.ui.utils.MinicashColorUtils.tintedContainerColor(customAccentColor, Color(0xFFE9ECF0), ratio = 0.16f)
            val surfaceHighest = com.uramos.minicash.ui.utils.MinicashColorUtils.tintedContainerColor(customAccentColor, Color(0xFFDFE3E8), ratio = 0.20f)
            val surfaceBright = com.uramos.minicash.ui.utils.MinicashColorUtils.tintedContainerColor(customAccentColor, Color.White, ratio = 0.03f)
            val outlineVar = com.uramos.minicash.ui.utils.MinicashColorUtils.tintedContainerColor(customAccentColor, LightColorScheme.outlineVariant, ratio = 0.18f)

            LightColorScheme.copy(
                primary = customAccentColor,
                onPrimary = Color.White,
                primaryContainer = primaryContainer,
                onPrimaryContainer = Color(0xFF191C1E),
                inversePrimary = customAccentColor.copy(alpha = 0.8f),
                secondary = customAccentColor,
                onSecondary = Color.White,
                secondaryContainer = secondaryContainer,
                onSecondaryContainer = Color(0xFF191C1E),
                onSurface = Color(0xFF191C1E),
                onSurfaceVariant = Color(0xFF43474E),
                surfaceContainerLowest = surfaceLowest,
                surfaceContainerLow = surfaceLow,
                surfaceContainer = surfaceNorm,
                surfaceContainerHigh = surfaceHigh,
                surfaceContainerHighest = surfaceHighest,
                surfaceBright = surfaceBright,
                outlineVariant = outlineVar
            )
        }
    }

    val targetColorScheme = baseColorScheme.let { scheme ->
        if (darkTheme && isAmoledMode) {
            scheme.copy(
                surface = Color.Black,
                background = Color.Black,
                surfaceContainerLowest = Color.Black,
                surfaceContainerLow = Color(0xFF111111),
                surfaceContainer = Color(0xFF1C1C1C),
                surfaceContainerHigh = Color(0xFF2A2A2A),
                surfaceContainerHighest = Color(0xFF383838),
                surfaceBright = Color(0xFF303030)
            )
        } else {
            scheme
        }
    }

    val animatedColorScheme = targetColorScheme.animateColors()
    val financialColors = if (darkTheme) DarkFinancialColors else LightFinancialColors

    val view = androidx.compose.ui.platform.LocalView.current
    if (!view.isInEditMode) {
        androidx.compose.runtime.SideEffect {
            val activity = view.context as? android.app.Activity
            activity?.window?.let { window ->
                val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    val currentTypography = remember(fontPreference) { getMinicashTypography(fontPreference) }

    CompositionLocalProvider(
        LocalFinancialColors provides financialColors,
        LocalSpacing provides Spacing(),
        LocalAnimationRestriction provides isAnimationRestricted
    ) {
        MaterialExpressiveTheme(
            colorScheme = animatedColorScheme,
            shapes = Shapes,
            typography = currentTypography,
            motionScheme = MotionScheme.expressive(),
            content = content
        )
    }
}

/**
 * Jerarquía de superficies: el fondo de las pantallas es el gris tonal ([screenBackground]) y las tarjetas, filas y
 * hojas son más claras ([cardSurface], [sheetBackground]); las pistas de pestañas, selectores y chips van un tono
 * por encima del fondo ([trackSurface]).
 */
private val ColorScheme.isLight: Boolean get() = surface.luminance() > 0.5f

/** Fondo de pantalla: gris tonal en claro y la superficie base en oscuro (mucho más oscuro que las tarjetas). */
val ColorScheme.screenBackground: Color get() = if (isLight) surfaceContainer else surface
/** Tarjetas y filas: más claras que el fondo en ambos temas. */
val ColorScheme.cardSurface: Color get() = if (isLight) surfaceBright else surfaceContainer
val ColorScheme.sheetBackground: Color get() = cardSurface
/** Pistas de pestañas, selectores y chips: un nivel por encima de las tarjetas. */
val ColorScheme.trackSurface: Color get() = surfaceContainerHigh

/** Diálogos: claros como las tarjetas en claro y un nivel por encima de ellas en oscuro. */
val ColorScheme.dialogSurface: Color get() = if (isLight) surfaceBright else surfaceContainerHigh
