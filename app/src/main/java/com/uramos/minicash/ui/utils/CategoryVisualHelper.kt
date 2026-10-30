package com.uramos.minicash.ui.utils

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.uramos.minicash.data.local.entities.TransactionType
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class MaterialIconOption(
    val label: String,
    val iconName: String,
    val vector: ImageVector
)

val availableIcons = listOf(
    MaterialIconOption("Billetera", "account_balance_wallet", Icons.Default.AccountBalanceWallet),
    MaterialIconOption("Tarjeta", "credit_card", Icons.Default.CreditCard),
    MaterialIconOption("Ahorros", "savings", Icons.Default.Savings),
    MaterialIconOption("Pagos", "payments", Icons.Default.Payments),
    MaterialIconOption("Carrito", "shopping_cart", Icons.Default.ShoppingCart),
    MaterialIconOption("Comida", "fastfood", Icons.Default.Fastfood),
    MaterialIconOption("Auto", "directions_car", Icons.Default.DirectionsCar),
    MaterialIconOption("Hogar", "home", Icons.Default.Home),
    MaterialIconOption("Servicios", "bolt", Icons.Default.Bolt),
    MaterialIconOption("Gimnasio", "fitness_center", Icons.Default.FitnessCenter),
    MaterialIconOption("Salud", "medical_services", Icons.Default.MedicalServices),
    MaterialIconOption("Cine", "movie", Icons.Default.Movie),
    MaterialIconOption("Trabajo", "work", Icons.Default.Work),
    MaterialIconOption("Rendimiento", "trending_up", Icons.AutoMirrored.Filled.TrendingUp),
    MaterialIconOption("Mascotas", "pets", Icons.Default.Pets),
    MaterialIconOption("Educación", "school", Icons.Default.School)
)

fun getIconVectorByName(iconName: String?): ImageVector {
    val key = iconName?.trim()?.lowercase() ?: return Icons.Default.AccountBalanceWallet
    return when (key) {
        // Finanzas
        "account_balance_wallet", "wallet", "accountbalancewallet" -> Icons.Default.AccountBalanceWallet
        "payments" -> Icons.Default.Payments
        "savings" -> Icons.Default.Savings
        "currency_exchange", "currencyexchange" -> Icons.Default.CurrencyExchange
        "point_of_sale", "pointofsale" -> Icons.Default.PointOfSale
        "receipt_long", "receiptlong" -> Icons.AutoMirrored.Filled.ReceiptLong
        "credit_card", "creditcard" -> Icons.Default.CreditCard
        "account_balance", "accountbalance" -> Icons.Default.AccountBalance
        "trending_up", "trendingup" -> Icons.AutoMirrored.Filled.TrendingUp

        // Hogar y servicios
        "home" -> Icons.Default.Home
        "tv" -> Icons.Default.Tv
        "kitchen" -> Icons.Default.Kitchen
        "chair" -> Icons.Default.Chair
        "bed" -> Icons.Default.Bed
        "lightbulb" -> Icons.Default.Lightbulb
        "bolt" -> Icons.Default.Bolt
        "water_drop", "waterdrop" -> Icons.Default.WaterDrop
        "wifi" -> Icons.Default.Wifi
        "build" -> Icons.Default.Build
        "local_laundry_service", "locallaundryservice" -> Icons.Default.LocalLaundryService

        // Alimentación, Postres y Repostería
        "restaurant" -> Icons.Default.Restaurant
        "local_cafe", "localcafe" -> Icons.Default.LocalCafe
        "fastfood" -> Icons.Default.Fastfood
        "icecream" -> Icons.Default.Icecream
        "liquor" -> Icons.Default.Liquor
        "lunch_dining", "lunchdining" -> Icons.Default.LunchDining
        "brunch_dining", "brunchdining" -> Icons.Default.BrunchDining
        "local_bar", "localbar" -> Icons.Default.LocalBar
        "cake" -> Icons.Default.Cake
        "bakery_dining", "bakerydining" -> Icons.Default.BakeryDining
        "cookie" -> Icons.Default.Cookie

        // Transporte
        "directions_car", "directionscar" -> Icons.Default.DirectionsCar
        "airport_shuttle", "airportshuttle" -> Icons.Default.AirportShuttle
        "ev_station", "evstation" -> Icons.Default.EvStation
        "directions_subway", "directionssubway" -> Icons.Default.DirectionsSubway
        "local_taxi", "localtaxi" -> Icons.Default.LocalTaxi
        "local_gas_station", "localgasstation" -> Icons.Default.LocalGasStation
        "directions_bus", "directionsbus" -> Icons.Default.DirectionsBus
        "commute" -> Icons.Default.Commute
        "flight" -> Icons.Default.Flight
        "two_wheeler", "twowheeler" -> Icons.Default.TwoWheeler

        // Ocio y compras
        "shopping_cart", "shoppingcart" -> Icons.Default.ShoppingCart
        "shopping_bag", "shoppingbag" -> Icons.Default.ShoppingBag
        "card_giftcard", "cardgiftcard" -> Icons.Default.CardGiftcard
        "gamepad" -> Icons.Default.Gamepad
        "theater_comedy", "theatercomedy" -> Icons.Default.TheaterComedy
        "camera_alt", "cameraalt" -> Icons.Default.CameraAlt
        "checkroom" -> Icons.Default.Checkroom
        "movie" -> Icons.Default.Movie
        "sports_esports", "sportsesports" -> Icons.Default.SportsEsports

        // Salud y medicina
        "medication" -> Icons.Default.Medication
        "healing" -> Icons.Default.Healing
        "fitness_center", "fitnesscenter" -> Icons.Default.FitnessCenter
        "medical_services", "medicalservices" -> Icons.Default.MedicalServices

        // Belleza y cuidado personal
        "face", "face_retouching_natural", "faceretouchingnatural" -> Icons.Default.FaceRetouchingNatural
        "brush" -> Icons.Default.Brush
        "content_cut", "contentcut" -> Icons.Default.ContentCut
        "spa" -> Icons.Default.Spa
        "clean_hands", "cleanhands" -> Icons.Default.CleanHands
        "self_improvement", "selfimprovement" -> Icons.Default.SelfImprovement

        // Educación y trabajo
        "school" -> Icons.Default.School
        "menu_book", "menubook" -> Icons.AutoMirrored.Filled.MenuBook
        "work" -> Icons.Default.Work
        "laptop_mac", "laptopmac" -> Icons.Default.LaptopMac
        "attach_money", "attachmoney" -> Icons.Default.AttachMoney

        // Mascotas y otros
        "pets" -> Icons.Default.Pets
        "flight_takeoff", "flighttakeoff" -> Icons.Default.FlightTakeoff
        "child_care", "childcare" -> Icons.Default.ChildCare
        "volunteer_activism", "volunteeractivism" -> Icons.Default.VolunteerActivism

        else -> Icons.Default.AccountBalanceWallet
    }
}

@Composable
fun AnimatedCounterText(
    targetValue: Long,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    isCurrency: Boolean = true
) {
    val animatable = remember { Animatable(0f) }

    LaunchedEffect(targetValue) {
        animatable.snapTo(0f)
        animatable.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    val currentCents = (targetValue * animatable.value).toLong()
    val textToDisplay = if (isCurrency) currentCents.toCurrencyFormat() else currentCents.toString()

    Text(
        text = textToDisplay,
        modifier = modifier,
        style = style,
        color = color,
        fontWeight = fontWeight
    )
}

fun normalizeText(input: String?): String {
    if (input.isNullOrBlank()) return ""
    val normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
    return normalized.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        .lowercase(Locale.getDefault())
}

fun getCategoryIcon(categoryName: String?, type: TransactionType): ImageVector {
    val norm = normalizeText(categoryName)

    return when {
        // Alimentos, Postres y Repostería
        norm.contains("pastel") || norm.contains("postre") || norm.contains("panaderia") || norm.contains("reposteria") || norm.contains("galleta") || norm.contains("helado") -> Icons.Default.Cake
        norm.contains("despensa") || norm.contains("super") || norm.contains("mercado") || norm.contains("tiendita") || norm.contains("oxxo") -> Icons.Default.ShoppingCart
        norm.contains("comida") || norm.contains("restaurante") || norm.contains("cafe") || norm.contains("cena") || norm.contains("desayuno") || norm.contains("tacos") || norm.contains("pizza") || norm.contains("chelas") || norm.contains("bar") -> Icons.Default.Fastfood

        // Belleza, Estética y Cuidado Personal
        norm.contains("maquillaje") || norm.contains("belleza") || norm.contains("estetica") || norm.contains("corte") || norm.contains("peluqueria") || norm.contains("barberia") || norm.contains("cabello") || norm.contains("unas") || norm.contains("skincare") || norm.contains("spa") || norm.contains("facial") || norm.contains("cosmetico") -> Icons.Default.FaceRetouchingNatural

        // Transporte
        norm.contains("gasolina") || norm.contains("gasolinera") -> Icons.Default.LocalGasStation
        norm.contains("transporte") || norm.contains("uber") || norm.contains("didi") || norm.contains("taxi") || norm.contains("camion") || norm.contains("metro") || norm.contains("pasaje") || norm.contains("caseta") || norm.contains("estacionamiento") || norm.contains("auto") || norm.contains("coche") -> Icons.Default.DirectionsCar

        // Vivienda y Servicios
        norm.contains("luz") || norm.contains("cfe") || norm.contains("electricidad") -> Icons.Default.Bolt
        norm.contains("renta") || norm.contains("casa") -> Icons.Default.Home
        norm.contains("agua") || norm.contains("gas") || norm.contains("internet") || norm.contains("telmex") || norm.contains("izzi") || norm.contains("telefonia") || norm.contains("plan") || norm.contains("servicio") -> Icons.Default.Receipt

        // Entretenimiento y Suscripciones
        norm.contains("netfl") || norm.contains("cine") || norm.contains("movie") || norm.contains("streaming") -> Icons.Default.Movie
        norm.contains("spoti") || norm.contains("musica") -> Icons.Default.MusicNote
        norm.contains("juego") || norm.contains("videojuego") || norm.contains("gamer") || norm.contains("sports") -> Icons.Default.SportsEsports

        // Salud y Cuidado
        norm.contains("gym") || norm.contains("gimnasio") || norm.contains("deporte") || norm.contains("ejercicio") -> Icons.Default.FitnessCenter
        norm.contains("salud") || norm.contains("farmacia") || norm.contains("doctor") || norm.contains("medico") || norm.contains("dentista") || norm.contains("consulta") || norm.contains("medicina") -> Icons.Default.MedicalServices

        // Compras y Personales
        norm.contains("ropa") || norm.contains("calzado") || norm.contains("vestido") -> Icons.Default.Checkroom
        norm.contains("compras") || norm.contains("tienda") || norm.contains("amazon") || norm.contains("mercadolibre") || norm.contains("electronica") || norm.contains("regalo") || norm.contains("tecnologia") -> Icons.Default.ShoppingBag

        // Mascotas
        norm.contains("mascota") || norm.contains("veterinario") || norm.contains("perro") || norm.contains("gato") || norm.contains("croqueta") -> Icons.Default.Pets

        // Ingresos
        norm.contains("sueldo") || norm.contains("nomina") || norm.contains("quincena") || norm.contains("salario") || norm.contains("honorarios") || norm.contains("trabajo") -> Icons.Default.Work
        norm.contains("pago") || norm.contains("bono") || norm.contains("venta") || norm.contains("rendimiento") || norm.contains("intereses") || norm.contains("ingreso") -> Icons.AutoMirrored.Filled.TrendingUp

        // Transferencias
        norm.contains("transferencia") || norm.contains("traspaso") || norm.contains("retiro") || norm.contains("abono") || type == TransactionType.TRANSFER -> Icons.Default.SwapHoriz

        type == TransactionType.INCOME -> Icons.Default.AttachMoney
        type == TransactionType.EXPENSE -> Icons.Default.Payments
        else -> Icons.Default.Category
    }
}

private val dateFormatShortSameYear = ThreadLocal.withInitial {
    SimpleDateFormat("d 'de' MMM", Locale.forLanguageTag("es-MX"))
}
private val dateFormatShortOtherYear = ThreadLocal.withInitial {
    SimpleDateFormat("d MMM yyyy", Locale.forLanguageTag("es-MX"))
}
private val dayKeyFormatThreadLocal = ThreadLocal.withInitial {
    SimpleDateFormat("yyyy-MM-dd", Locale.US)
}
private val dayLabelFullFormatThreadLocal = ThreadLocal.withInitial {
    SimpleDateFormat("EEEE, d 'de' MMMM", Locale.forLanguageTag("es-MX"))
}
private val yearLabelFormatThreadLocal = ThreadLocal.withInitial {
    SimpleDateFormat("d 'de' MMMM yyyy", Locale.forLanguageTag("es-MX"))
}
private val monthDayLabelFormatThreadLocal = ThreadLocal.withInitial {
    SimpleDateFormat("d 'de' MMMM", Locale.forLanguageTag("es-MX"))
}

fun formatTransactionDate(dateMillis: Long): String {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply { timeInMillis = dateMillis }

    val isSameYear = now.get(Calendar.YEAR) == target.get(Calendar.YEAR)
    val isSameDay = isSameYear && now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

    val yesterday = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
    val isYesterday = yesterday.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
            yesterday.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

    return when {
        isSameDay -> "Hoy"
        isYesterday -> "Ayer"
        isSameYear -> dateFormatShortSameYear.get()?.format(Date(dateMillis)) ?: ""
        else -> dateFormatShortOtherYear.get()?.format(Date(dateMillis)) ?: ""
    }
}

data class TransactionDateGroup(
    val dateKey: String,
    val dateLabel: String,
    val dayNetTotal: Long,
    val transactions: List<com.uramos.minicash.data.local.entities.TransactionEntity>
)

fun groupTransactionsByDate(transactions: List<com.uramos.minicash.data.local.entities.TransactionEntity>): List<TransactionDateGroup> {
    if (transactions.isEmpty()) return emptyList()

    val dayKeyFmt = dayKeyFormatThreadLocal.get()
    val dayLabelFullFmt = dayLabelFullFormatThreadLocal.get()
    val yearLabelFmt = yearLabelFormatThreadLocal.get()
    val monthDayFmt = monthDayLabelFormatThreadLocal.get()

    val now = Calendar.getInstance()
    val yesterday = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }

    val map = LinkedHashMap<String, MutableList<com.uramos.minicash.data.local.entities.TransactionEntity>>()
    for (tx in transactions) {
        val key = dayKeyFmt?.format(Date(tx.date)) ?: ""
        map.getOrPut(key) { mutableListOf() }.add(tx)
    }

    return map.map { (key, txList) ->
        val firstMillis = txList.first().date
        val target = Calendar.getInstance().apply { timeInMillis = firstMillis }

        val isSameYear = now.get(Calendar.YEAR) == target.get(Calendar.YEAR)
        val isSameDay = isSameYear && now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
        val isYesterday = yesterday.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

        val dateLabel = when {
            isSameDay -> "Hoy"
            isYesterday -> "Ayer, " + (monthDayFmt?.format(target.time) ?: "")
            isSameYear -> (dayLabelFullFmt?.format(target.time) ?: "").replaceFirstChar { it.uppercase() }
            else -> (yearLabelFmt?.format(target.time) ?: "").replaceFirstChar { it.uppercase() }
        }

        val dayNetTotal = txList.sumOf { tx ->
            when (tx.type) {
                TransactionType.INCOME -> tx.amount
                TransactionType.EXPENSE -> -tx.amount
                TransactionType.TRANSFER -> 0L
            }
        }

        TransactionDateGroup(
            dateKey = key,
            dateLabel = dateLabel,
            dayNetTotal = dayNetTotal,
            transactions = txList
        )
    }
}
