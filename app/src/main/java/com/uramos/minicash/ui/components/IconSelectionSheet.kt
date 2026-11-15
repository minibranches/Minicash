package com.uramos.minicash.ui.components

import com.uramos.minicash.ui.utils.rememberMinicashHaptics
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uramos.minicash.ui.theme.MinicashThemeExtensions

data class IconCategoryGroup(
    val title: String,
    val icons: List<Pair<String, ImageVector>>
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun IconSelectionSheet(
    selectedIconName: String,
    onIconSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val spacing = MinicashThemeExtensions.spacing
    val haptics = rememberMinicashHaptics()

    val groups = listOf(
        IconCategoryGroup(
            title = "Finanzas",
            icons = listOf(
                "account_balance_wallet" to Icons.Default.AccountBalanceWallet,
                "payments" to Icons.Default.Payments,
                "savings" to Icons.Default.Savings,
                "currency_exchange" to Icons.Default.CurrencyExchange,
                "point_of_sale" to Icons.Default.PointOfSale,
                "receipt_long" to Icons.AutoMirrored.Filled.ReceiptLong,
                "credit_card" to Icons.Default.CreditCard,
                "account_balance" to Icons.Default.AccountBalance,
                "trending_up" to Icons.AutoMirrored.Filled.TrendingUp
            )
        ),
        IconCategoryGroup(
            title = "Hogar y servicios",
            icons = listOf(
                "home" to Icons.Default.Home,
                "tv" to Icons.Default.Tv,
                "kitchen" to Icons.Default.Kitchen,
                "chair" to Icons.Default.Chair,
                "bed" to Icons.Default.Bed,
                "lightbulb" to Icons.Default.Lightbulb,
                "bolt" to Icons.Default.Bolt,
                "water_drop" to Icons.Default.WaterDrop,
                "wifi" to Icons.Default.Wifi,
                "build" to Icons.Default.Build,
                "local_laundry_service" to Icons.Default.LocalLaundryService
            )
        ),
        IconCategoryGroup(
            title = "Alimentación",
            icons = listOf(
                "restaurant" to Icons.Default.Restaurant,
                "local_cafe" to Icons.Default.LocalCafe,
                "fastfood" to Icons.Default.Fastfood,
                "cake" to Icons.Default.Cake,
                "bakery_dining" to Icons.Default.BakeryDining,
                "icecream" to Icons.Default.Icecream,
                "cookie" to Icons.Default.Cookie,
                "liquor" to Icons.Default.Liquor,
                "lunch_dining" to Icons.Default.LunchDining,
                "brunch_dining" to Icons.Default.BrunchDining,
                "local_bar" to Icons.Default.LocalBar
            )
        ),
        IconCategoryGroup(
            title = "Transporte",
            icons = listOf(
                "directions_car" to Icons.Default.DirectionsCar,
                "airport_shuttle" to Icons.Default.AirportShuttle,
                "ev_station" to Icons.Default.EvStation,
                "directions_subway" to Icons.Default.DirectionsSubway,
                "local_taxi" to Icons.Default.LocalTaxi,
                "local_gas_station" to Icons.Default.LocalGasStation,
                "directions_bus" to Icons.Default.DirectionsBus,
                "commute" to Icons.Default.Commute,
                "flight" to Icons.Default.Flight,
                "two_wheeler" to Icons.Default.TwoWheeler
            )
        ),
        IconCategoryGroup(
            title = "Ocio y compras",
            icons = listOf(
                "shopping_cart" to Icons.Default.ShoppingCart,
                "shopping_bag" to Icons.Default.ShoppingBag,
                "card_giftcard" to Icons.Default.CardGiftcard,
                "gamepad" to Icons.Default.Gamepad,
                "theater_comedy" to Icons.Default.TheaterComedy,
                "camera_alt" to Icons.Default.CameraAlt,
                "checkroom" to Icons.Default.Checkroom,
                "movie" to Icons.Default.Movie,
                "sports_esports" to Icons.Default.SportsEsports
            )
        ),
        IconCategoryGroup(
            title = "Salud y medicina",
            icons = listOf(
                "medication" to Icons.Default.Medication,
                "healing" to Icons.Default.Healing,
                "fitness_center" to Icons.Default.FitnessCenter,
                "medical_services" to Icons.Default.MedicalServices
            )
        ),
        IconCategoryGroup(
            title = "Belleza y cuidado personal",
            icons = listOf(
                "face_retouching_natural" to Icons.Default.FaceRetouchingNatural,
                "brush" to Icons.Default.Brush,
                "content_cut" to Icons.Default.ContentCut,
                "spa" to Icons.Default.Spa,
                "clean_hands" to Icons.Default.CleanHands,
                "self_improvement" to Icons.Default.SelfImprovement
            )
        ),
        IconCategoryGroup(
            title = "Educación y trabajo",
            icons = listOf(
                "school" to Icons.Default.School,
                "menu_book" to Icons.AutoMirrored.Filled.MenuBook,
                "work" to Icons.Default.Work,
                "laptop_mac" to Icons.Default.LaptopMac,
                "attach_money" to Icons.Default.AttachMoney
            )
        ),
        IconCategoryGroup(
            title = "Mascotas y otros",
            icons = listOf(
                "pets" to Icons.Default.Pets,
                "flight_takeoff" to Icons.Default.FlightTakeoff,
                "child_care" to Icons.Default.ChildCare,
                "volunteer_activism" to Icons.Default.VolunteerActivism
            )
        )
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Seleccionar icono",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                groups.forEach { group ->
                    item(key = group.title) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = group.title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                group.icons.forEach { (iconName, vector) ->
                                    val isSelected = iconName == selectedIconName

                                    Surface(
                                        onClick = {
                                            haptics.click()
                                            onIconSelected(iconName)
                                            onDismiss()
                                        },
                                        shape = if (isSelected) MaterialShapes.Cookie9Sided.toShape() else CircleShape,
                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = vector,
                                                contentDescription = null,
                                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(24.dp)
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
    }
}
