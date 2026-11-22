package com.uramos.minicash.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Paleta Primaria Azul Eléctrico Light (Google / Pixel)
val PrimaryLight = Color(0xFF1A73E8)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFD2E3FC)
val OnPrimaryContainerLight = Color(0xFF041E49)

val SecondaryLight = Color(0xFF535F70)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFD7E3F7)
val OnSecondaryContainerLight = Color(0xFF101C2B)

val TertiaryLight = Color(0xFF39656D)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFBCEBF5)
val OnTertiaryContainerLight = Color(0xFF001F25)

val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)

val BackgroundLight = Color(0xFFF8FAFC)
val OnBackgroundLight = Color(0xFF191C20)
val SurfaceLight = Color(0xFFF8FAFC)
val OnSurfaceLight = Color(0xFF191C20)
val SurfaceVariantLight = Color(0xFFDFE2EC)
val OnSurfaceVariantLight = Color(0xFF43474E)

val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFF2F4F8)
val SurfaceContainerLight = Color(0xFFECEEF2)
val SurfaceContainerHighLight = Color(0xFFE6E8EE)
val SurfaceContainerHighestLight = Color(0xFFE0E2E8)
val SurfaceBrightLight = Color(0xFFFFFFFF)

// Semántica financiera Light
val IncomeGreenLight = Color(0xFF1B6D2F)
val OnIncomeGreenLight = Color(0xFFFFFFFF)
val IncomeContainerLight = Color(0xFFA5F5A8)
val OnIncomeContainerLight = Color(0xFF002108)

// Paleta Primaria Azul Eléctrico Dark
val PrimaryDark = Color(0xFF8AB4F8)
val OnPrimaryDark = Color(0xFF041E49)
val PrimaryContainerDark = Color(0xFF0842A0)
val OnPrimaryContainerDark = Color(0xFFD2E3FC)

val SecondaryDark = Color(0xFFBBC7DB)
val OnSecondaryDark = Color(0xFF253140)
val SecondaryContainerDark = Color(0xFF3B4858)
val OnSecondaryContainerDark = Color(0xFFD7E3F7)

val TertiaryDark = Color(0xFFA0CFD8)
val OnTertiaryDark = Color(0xFF00363E)
val TertiaryContainerDark = Color(0xFF1F4D55)
val OnTertiaryContainerDark = Color(0xFFBCEBF5)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

// Superficies Neutras Azuladas Frías (Estáticas para tema oscuro)
val BackgroundDark = Color(0xFF0F141C)
val OnBackgroundDark = Color(0xFFE1E6F0)
val SurfaceDark = Color(0xFF0F141C)
val OnSurfaceDark = Color(0xFFE1E6F0)
val SurfaceVariantDark = Color(0xFF2B323E)
val OnSurfaceVariantDark = Color(0xFFC2C7D0)

val SurfaceContainerLowestDark = Color(0xFF0A0E14)
val SurfaceContainerLowDark = Color(0xFF141923)
val SurfaceContainerDark = Color(0xFF181E29)
val SurfaceContainerHighDark = Color(0xFF222936)
val SurfaceContainerHighestDark = Color(0xFF2C3444)
val SurfaceBrightDark = Color(0xFF2E3646)

// Semántica financiera Dark
val IncomeGreenDark = Color(0xFF89D98E)
val OnIncomeGreenDark = Color(0xFF003913)
val IncomeContainerDark = Color(0xFF00531E)
val OnIncomeContainerDark = Color(0xFFA5F5A8)

@Immutable
data class ExtendedFinancialColors(
    val income: Color,
    val onIncome: Color,
    val incomeContainer: Color,
    val onIncomeContainer: Color,
    val expense: Color,
    val onExpense: Color,
    val expenseContainer: Color,
    val onExpenseContainer: Color,
    val neutral: Color,
    val transfer: Color
)
