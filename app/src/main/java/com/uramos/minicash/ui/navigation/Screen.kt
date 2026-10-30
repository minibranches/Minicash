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
    object Transactions : Screen("transactions", "Historial", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong)
    object Budget : Screen("budget", "Fijos", Icons.Filled.PieChart, Icons.Outlined.PieChart)
    object Credits : Screen("credits", "Créditos", Icons.Filled.CreditCard, Icons.Outlined.CreditCard)
    object Settings : Screen("settings", "Ajustes", Icons.Filled.Settings, Icons.Outlined.Settings)
    object AccountsManagement : Screen("accounts_management", "Gestión de cuentas", Icons.Filled.Settings, Icons.Outlined.Settings)
    object CategoriesManagement : Screen("categories_management", "Gestión de categorías", Icons.Filled.Settings, Icons.Outlined.Settings)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Transactions,
    Screen.Budget,
    Screen.Credits,
    Screen.Settings
)