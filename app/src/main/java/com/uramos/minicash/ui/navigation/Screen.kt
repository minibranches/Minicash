package com.uramos.minicash.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : Screen("home", "Inicio", Icons.Filled.Home, Icons.Outlined.Home)
    object Transactions : Screen("transactions", "Movimientos", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong)
    object Budget : Screen("budget", "Gastos fijos", Icons.Filled.PieChart, Icons.Outlined.PieChart)
    object Credits : Screen("credits", "Créditos", Icons.Filled.CreditCard, Icons.Outlined.CreditCard)
    object Settings : Screen("settings", "Ajustes", Icons.Filled.Settings, Icons.Outlined.Settings)
    object AccountsManagement : Screen("accounts_management", "Gestión de cuentas", Icons.Filled.Settings, Icons.Outlined.Settings)
    object CategoriesManagement : Screen("categories_management", "Gestión de categorías", Icons.Filled.Settings, Icons.Outlined.Settings)
}

/**
 * Destinos de la barra de navegación, en orden. Inicio y Movimientos son fijos; Gastos fijos y Créditos
 * aparecen solo si su módulo está activo. Reportes y Ajustes no están en la barra: se abren desde los
 * íconos de arriba a la derecha de cada pestaña.
 */
fun navScreens(showBudget: Boolean, showCredits: Boolean): List<Screen> = buildList {
    add(Screen.Home)
    add(Screen.Transactions)
    if (showBudget) add(Screen.Budget)
    if (showCredits) add(Screen.Credits)
}
