package com.uramos.minicash.ui.navigation

import com.uramos.minicash.ui.components.bounceOnPress
import com.uramos.minicash.ui.components.LocalBottomBlurBand
import androidx.compose.runtime.CompositionLocalProvider
import com.uramos.minicash.ui.theme.screenBackground
import com.uramos.minicash.ui.theme.minicashFastEffectsSpec
import com.uramos.minicash.ui.theme.minicashFastSpatialSpec
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import android.view.accessibility.AccessibilityManager
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.animation.core.spring
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.Crossfade
import com.uramos.minicash.ui.screens.settings.AboutScreen
import kotlin.coroutines.cancellation.CancellationException
import androidx.compose.ui.unit.IntOffset
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.expandVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import com.uramos.minicash.ui.components.WithoutMinTouchTarget
import androidx.compose.foundation.layout.height
import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.draw.clip
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import com.uramos.minicash.data.preferences.NavBarStyle
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.uramos.minicash.ui.utils.rememberMinicashHaptics
import kotlin.math.absoluteValue
import com.uramos.minicash.data.local.entities.TransactionType
import com.uramos.minicash.ui.screens.budget.BudgetScreen
import com.uramos.minicash.ui.screens.budget.BudgetViewModel
import com.uramos.minicash.ui.screens.credits.CreditsScreen
import com.uramos.minicash.ui.screens.credits.CreditsViewModel
import com.uramos.minicash.ui.screens.reports.ReportsScreen
import com.uramos.minicash.ui.screens.reports.ReportsViewModel
import com.uramos.minicash.ui.screens.home.HomeScreen
import com.uramos.minicash.ui.screens.home.HomeViewModel
import com.uramos.minicash.ui.screens.settings.AccountsManagementScreen
import com.uramos.minicash.ui.screens.settings.AppearanceSettingsScreen
import com.uramos.minicash.ui.screens.settings.BackupSettingsScreen
import com.uramos.minicash.ui.screens.settings.CategoriesManagementScreen
import com.uramos.minicash.ui.screens.settings.InstitutionsManagementScreen
import com.uramos.minicash.ui.screens.settings.ModulesSettingsScreen
import com.uramos.minicash.ui.screens.settings.SettingsScreen
import com.uramos.minicash.ui.screens.settings.SettingsViewModel
import com.uramos.minicash.ui.screens.transactions.TransactionsScreen
import com.uramos.minicash.ui.screens.transactions.TransactionsViewModel
import com.uramos.minicash.ui.theme.LocalAnimationRestriction
import com.uramos.minicash.ui.theme.MinicashShapes
import com.uramos.minicash.ui.theme.minicashEffectsSpec
import com.uramos.minicash.ui.theme.minicashSpatialSpec
import com.uramos.minicash.ui.transactions.AddTransactionSheet
import com.uramos.minicash.ui.screens.account.AccountDetailScreen
import com.uramos.minicash.ui.transactions.AddTransactionViewModel
import kotlinx.coroutines.launch

private fun getShortestTargetPage(currentPage: Int, targetIndex: Int, numPages: Int): Int {
    if (numPages <= 0) return currentPage
    val currentIndex = currentPage % numPages
    var diff = targetIndex - currentIndex
    val half = numPages / 2
    if (diff > half) {
        diff -= numPages
    } else if (diff < -half) {
        diff += numPages
    }
    return currentPage + diff
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    userName: String,
    shortcutAction: String? = null,
    onShortcutActionHandled: () -> Unit = {},
    homeViewModel: HomeViewModel,
    addTransactionViewModel: AddTransactionViewModel,
    transactionsViewModel: TransactionsViewModel,
    budgetViewModel: BudgetViewModel,
    creditsViewModel: CreditsViewModel,
    reportsViewModel: ReportsViewModel,
    settingsViewModel: SettingsViewModel
) {
    val settingsState by settingsViewModel.uiState.collectAsState()
    val showBudgetTab = settingsState.showBudgetTab
    val showCreditsTab = settingsState.showCreditsTab
    val navBarStyle by settingsViewModel.navBarStyle.collectAsState()

    // Inicio y Movimientos son fijos; Gastos fijos y Créditos solo si su módulo está activo.
    // Reportes y Ajustes se abren desde los íconos de arriba a la derecha de cada pestaña.
    val visibleScreens = remember(showBudgetTab, showCreditsTab) { navScreens(showBudgetTab, showCreditsTab) }
    val numPages = visibleScreens.size

    val pageCount = 10_000
    // Página central que cae en un múltiplo del número de pestañas, para que la primera siempre sea Inicio
    val initialPage = (pageCount / 2) / numPages * numPages
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { pageCount }
    )
    val coroutineScope = rememberCoroutineScope()
    val haptics = rememberMinicashHaptics()
    val currentScreenIndex = pagerState.currentPage % numPages
    val currentScreen = visibleScreens[currentScreenIndex]

    // Al mostrar u ocultar módulos cambia el número de páginas: se reubica el pager para seguir en la misma pestaña
    val previousScreens = remember { arrayOf(visibleScreens) }
    LaunchedEffect(visibleScreens) {
        val previous = previousScreens[0]
        if (previous != visibleScreens) {
            val page = pagerState.currentPage
            val staying = previous[page % previous.size]
            val newIndex = visibleScreens.indexOf(staying).takeIf { it >= 0 } ?: 0
            previousScreens[0] = visibleScreens
            pagerState.scrollToPage((page / visibleScreens.size) * visibleScreens.size + newIndex)
        }
    }

    val homeListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val transactionsListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val budgetListState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
    val creditsListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val settingsListState = androidx.compose.foundation.lazy.rememberLazyListState()

    var showAddSheet by remember { mutableStateOf(false) }
    var shortcutType by remember { mutableStateOf<TransactionType?>(null) }
    var showAddBudgetItemFromMain by remember { mutableStateOf(false) }
    var showAddCreditFromMain by remember { mutableStateOf(false) }
    // Pila de pantallas superpuestas (Reportes, Ajustes y sus subpantallas); vacía = se ven las pestañas
    val routeStack = remember { mutableStateListOf<String>() }
    val activeRoute = routeStack.lastOrNull()

    // La barra flotante se oculta al bajar por la página y reaparece al subir, al cambiar de pestaña y en el tope
    var navBarScrollVisible by remember { mutableStateOf(true) }
    // Con un lector de pantalla activo la barra no se oculta al desplazar (pauta de accesibilidad de M3)
    val screenReaderOn = rememberTouchExplorationEnabled()
    val navBarScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (consumed.y < -NAV_SCROLL_THRESHOLD && !screenReaderOn.value) navBarScrollVisible = false
                else if (consumed.y > NAV_SCROLL_THRESHOLD) navBarScrollVisible = true
                return Offset.Zero
            }
        }
    }
    LaunchedEffect(currentScreenIndex) { navBarScrollVisible = true }
    // Con cualquier pantalla superpuesta abierta la barra se oculta
    val inSubScreen = routeStack.isNotEmpty()
    // Desplazar una pantalla superpuesta (Ajustes, Reportes…) también pasa por este panel y esconde la barra;
    // al volver a las pestañas debe reaparecer
    LaunchedEffect(inSubScreen) { if (!inSubScreen) navBarScrollVisible = true }

    LaunchedEffect(shortcutAction) {
        if (!shortcutAction.isNullOrBlank()) {
            val type = when (shortcutAction) {
                "INCOME" -> TransactionType.INCOME
                "TRANSFER" -> TransactionType.TRANSFER
                else -> TransactionType.EXPENSE
            }
            shortcutType = type
            showAddSheet = true
            onShortcutActionHandled()
        }
    }

    if (showAddSheet) {
        AddTransactionSheet(
            viewModel = addTransactionViewModel,
            initialType = shortcutType,
            onDismiss = {
                showAddSheet = false
                shortcutType = null
            }
        )
    }

    // Pantallas superpuestas: eje X compartido; el gesto atrás predictivo arrastra la transición con el dedo
    val routeTransitionState = remember { SeekableTransitionState<String?>(null) }
    val routeTransition = rememberTransition(routeTransitionState, label = "route_navigation_transition")
    LaunchedEffect(activeRoute) {
        if (routeTransitionState.currentState != activeRoute || routeTransitionState.targetState != activeRoute) {
            routeTransitionState.animateTo(activeRoute)
        }
    }
    PredictiveBackHandler(enabled = routeStack.isNotEmpty()) { progress ->
        val leaving = activeRoute
        val previous = routeStack.getOrNull(routeStack.lastIndex - 1)
        try {
            progress.collect { event -> routeTransitionState.seekTo(event.progress, targetState = previous) }
            routeStack.removeLastOrNull()
        } catch (e: CancellationException) {
            routeTransitionState.animateTo(leaving)
            throw e
        }
    }

    // Cambio de pestaña desde la barra: fundido con leve escala, como un "fade through"; deslizar sigue funcionando
    val animationsRestricted = LocalAnimationRestriction.current
    val tabFade = remember { Animatable(1f) }
    val fadeOutSpec = minicashFastEffectsSpec<Float>()
    val fadeInSpec = minicashEffectsSpec<Float>()
    val fadeToPage: (Int) -> Unit = { targetPage ->
        coroutineScope.launch {
            if (animationsRestricted) {
                pagerState.scrollToPage(targetPage)
            } else {
                tabFade.animateTo(0f, fadeOutSpec)
                pagerState.scrollToPage(targetPage)
                tabFade.animateTo(1f, fadeInSpec)
            }
        }
    }

    // Atrás desde cualquier pestaña que no sea Inicio regresa a Inicio; en Inicio sale de la app
    BackHandler(enabled = routeStack.isEmpty() && currentScreen !is Screen.Home) {
        val homeIndex = visibleScreens.indexOf(Screen.Home).coerceAtLeast(0)
        fadeToPage(getShortestTargetPage(pagerState.currentPage, homeIndex, numPages))
    }

    val onNavClick: (Int, Screen) -> Unit = { index, screen ->
        routeStack.clear()
        haptics.tick()
        if (currentScreenIndex == index) {
            coroutineScope.launch {
                when (screen) {
                    is Screen.Home -> homeListState.animateScrollToItem(0)
                    is Screen.Transactions -> transactionsListState.animateScrollToItem(0)
                    is Screen.Budget -> budgetListState.animateScrollToItem(0)
                    is Screen.Credits -> creditsListState.animateScrollToItem(0)
                    else -> {}
                }
            }
        } else {
            fadeToPage(getShortestTargetPage(pagerState.currentPage, index, numPages))
        }
    }

    val onFabClick: () -> Unit = {
        haptics.click()
        when (currentScreen) {
            is Screen.Budget -> showAddBudgetItemFromMain = true
            is Screen.Credits -> showAddCreditFromMain = true
            else -> showAddSheet = true
        }
    }
    // El botón de agregar está en las cuatro pestañas; en Inicio y Movimientos despliega el menú de tipos
    val tabFabVisible = !inSubScreen

    // Menú del botón de agregar: el primero de la lista queda más cerca del botón (el gasto es lo más frecuente)
    var fabMenuExpanded by remember { mutableStateOf(false) }
    LaunchedEffect(tabFabVisible, currentScreenIndex) { fabMenuExpanded = false }
    BackHandler(enabled = fabMenuExpanded) { fabMenuExpanded = false }
    val openAddSheetAs: (TransactionType) -> Unit = { type ->
        haptics.click()
        fabMenuExpanded = false
        shortcutType = type
        showAddSheet = true
    }
    val fabMenu = listOf(
        FabMenuAction("Gasto", Icons.AutoMirrored.Filled.TrendingDown) { openAddSheetAs(TransactionType.EXPENSE) },
        FabMenuAction("Ingreso", Icons.AutoMirrored.Filled.TrendingUp) { openAddSheetAs(TransactionType.INCOME) },
        FabMenuAction("Transferencia", Icons.Default.SwapHoriz) { openAddSheetAs(TransactionType.TRANSFER) }
    )
    val tabFabMenu = if (currentScreen is Screen.Home || currentScreen is Screen.Transactions) fabMenu else null

    val bottomBlurBand = if (navBarStyle == NavBarStyle.FLOATING) FLOATING_BAR_BLUR_HEIGHT else 0.dp
    CompositionLocalProvider(LocalBottomBlurBand provides bottomBlurBand) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.screenBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Panel de contenido: fondo plano de borde a borde, sin marco ni esquinas redondeadas arriba
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(top = innerPadding.calculateTopPadding())
                        .background(MaterialTheme.colorScheme.screenBackground)
                        .nestedScroll(navBarScrollConnection)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        beyondViewportPageCount = 1,
                        // Las pestañas se cambian solo desde la barra: así el deslizamiento horizontal queda libre
                        // para deslizar-y-eliminar en las filas de movimientos.
                        userScrollEnabled = false,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                val fade = tabFade.value
                                alpha = fade
                                scaleX = TAB_FADE_MIN_SCALE + (1f - TAB_FADE_MIN_SCALE) * fade
                                scaleY = TAB_FADE_MIN_SCALE + (1f - TAB_FADE_MIN_SCALE) * fade
                            }
                    ) { page ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                                    val coercedOffset = pageOffset.coerceIn(0f, 1f)
                                    scaleX = 1f - (coercedOffset * 0.05f)
                                    scaleY = 1f - (coercedOffset * 0.05f)
                                    alpha = 1f - (coercedOffset * 0.35f)
                                }
                        ) {
                            val pageScreen = visibleScreens[page % numPages]
                            when (pageScreen) {
                                is Screen.Home -> HomeScreen(
                                    userName = userName,
                                    viewModel = homeViewModel,
                                    listState = homeListState,
                                    onOpenSettings = { routeStack.add(ROUTE_SETTINGS) },
                                    onOpenReports = { routeStack.add(ROUTE_REPORTS) },
                                    onAddTransactionClick = { showAddSheet = true },
                                    onOpenAccount = { id -> routeStack.add("$ROUTE_ACCOUNT_PREFIX$id") },
                                    onSeeMoreTransactionsClick = {
                                        val txIdx = visibleScreens.indexOf(Screen.Transactions).coerceAtLeast(0)
                                        fadeToPage(getShortestTargetPage(pagerState.currentPage, txIdx, numPages))
                                    }
                                )
                                is Screen.Transactions -> TransactionsScreen(
                                    viewModel = transactionsViewModel,
                                    listState = transactionsListState,
                                    onOpenSettings = { routeStack.add(ROUTE_SETTINGS) },
                                    onOpenReports = { routeStack.add(ROUTE_REPORTS) },
                                    isActive = currentScreen is Screen.Transactions && !inSubScreen
                                )
                                is Screen.Budget -> BudgetScreen(
                                    viewModel = budgetViewModel,
                                    listState = budgetListState,
                                    showAddDialogExternal = showAddBudgetItemFromMain,
                                    onAddDialogExternalHandled = { showAddBudgetItemFromMain = false },
                                    onOpenAddTransactionForBudgetItem = { item ->
                                        addTransactionViewModel.prefillForBudgetItem(item)
                                        showAddSheet = true
                                    },
                                    onOpenReports = { routeStack.add(ROUTE_REPORTS) },
                                    onOpenSettings = { routeStack.add(ROUTE_SETTINGS) }
                                )
                                is Screen.Credits -> CreditsScreen(
                                    viewModel = creditsViewModel,
                                    listState = creditsListState,
                                    showAddDialogExternal = showAddCreditFromMain,
                                    onAddDialogExternalHandled = { showAddCreditFromMain = false },
                                    onOpenAddTransactionForCredit = { amountCents, conceptStr, notesStr ->
                                        addTransactionViewModel.prefillForCredit(amountCents, conceptStr, notesStr)
                                        showAddSheet = true
                                    },
                                    onOpenReports = { routeStack.add(ROUTE_REPORTS) },
                                    onOpenSettings = { routeStack.add(ROUTE_SETTINGS) },
                                    isActive = currentScreen is Screen.Credits && !inSubScreen
                                )
                                else -> Unit
                            }
                        }
                    }

                    // Pantallas superpuestas con eje X compartido; con la pila vacía no se dibuja nada y se ven las pestañas
                    // Entre dos pantallas superpuestas el fundido las deja semitransparentes un instante y dejaría ver la
                    // pestaña de abajo (un destello de Inicio); con un fondo opaco ese cruce no se nota. Desde y hacia las
                    // pestañas no se pone, porque ahí sí debe verse lo que hay detrás.
                    val betweenRoutes = routeTransition.currentState != null && routeTransition.targetState != null
                    routeTransition.AnimatedContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (betweenRoutes) Modifier.background(MaterialTheme.colorScheme.screenBackground) else Modifier),
                        transitionSpec = {
                            if (animationsRestricted) {
                                fadeIn(animationSpec = snap()) togetherWith fadeOut(animationSpec = snap())
                            } else {
                                sharedAxisX(forward = routeDepth(targetState) > routeDepth(initialState))
                            }
                        }
                    ) { route ->
                        val back: () -> Unit = { routeStack.removeLastOrNull() }
                        when (route) {
                            null -> Box(Modifier.fillMaxSize())
                            ROUTE_REPORTS -> ReportsScreen(viewModel = reportsViewModel, onBackClick = back)
                            ROUTE_SETTINGS -> SettingsScreen(
                                viewModel = settingsViewModel,
                                listState = settingsListState,
                                onNavigateToAccounts = { routeStack.add(ROUTE_ACCOUNTS) },
                                onNavigateToCategories = { routeStack.add(ROUTE_CATEGORIES) },
                                onNavigateToInstitutions = { routeStack.add(ROUTE_INSTITUTIONS) },
                                onNavigateToAppearance = { routeStack.add(ROUTE_APPEARANCE) },
                                onNavigateToModules = { routeStack.add(ROUTE_MODULES) },
                                onNavigateToBackups = { routeStack.add(ROUTE_BACKUPS) },
                                onNavigateToAbout = { routeStack.add(ROUTE_ABOUT) },
                                onBackClick = back
                            )
                            ROUTE_ACCOUNTS -> AccountsManagementScreen(viewModel = settingsViewModel, onNavigateBack = back)
                            ROUTE_CATEGORIES -> CategoriesManagementScreen(viewModel = settingsViewModel, onNavigateBack = back)
                            ROUTE_INSTITUTIONS -> InstitutionsManagementScreen(viewModel = settingsViewModel, onNavigateBack = back)
                            ROUTE_APPEARANCE -> AppearanceSettingsScreen(viewModel = settingsViewModel, onBackClick = back)
                            ROUTE_MODULES -> ModulesSettingsScreen(viewModel = settingsViewModel, onBackClick = back)
                            ROUTE_BACKUPS -> BackupSettingsScreen(viewModel = settingsViewModel, onBackClick = back)
                            ROUTE_ABOUT -> AboutScreen(onBackClick = back)
                            else -> {
                                val accountId = route?.removePrefix(ROUTE_ACCOUNT_PREFIX)?.toLongOrNull()
                                if (route?.startsWith(ROUTE_ACCOUNT_PREFIX) == true && accountId != null) {
                                    AccountDetailScreen(accountId = accountId, viewModel = homeViewModel, onBack = back)
                                } else {
                                    Box(Modifier.fillMaxSize())
                                }
                            }
                        }
                    }

                    // Velo detrás del menú del botón de agregar; tocarlo lo cierra
                    FabScrim(visible = fabMenuExpanded, onDismiss = { fabMenuExpanded = false })

                    if (navBarStyle == NavBarStyle.FLOATING) {
                        // Capa Overlaid Flotante Desacoplada (Alineación simétrica de insets y ancho optimizado)
                        SlideVisibility(
                            visible = (navBarScrollVisible || screenReaderOn.value) && !inSubScreen,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                        ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 12.dp, vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val spatialFloat = minicashSpatialSpec<Float>()
                            val spatialSize = minicashSpatialSpec<IntSize>()
                            val effectsFloat = minicashEffectsSpec<Float>()
                            val effectsColor = minicashEffectsSpec<androidx.compose.ui.graphics.Color>()
                            val spatialDp = minicashSpatialSpec<androidx.compose.ui.unit.Dp>()

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.wrapContentWidth()
                            ) {
                                // Cápsula flotante de pestañas. Su ancho es fijo (el del destino activo más largo): lo que
                                // cambia al elegir otra pestaña es cómo se reparte entre las píldoras, así la cápsula no
                                // se redimensiona ni mueve el botón de agregar, y las formas nunca se recortan en seco.
                                val density = LocalDensity.current
                                val textMeasurer = rememberTextMeasurer()
                                val labelStyle = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                                val labelWidths = visibleScreens.map { screen ->
                                    with(density) {
                                        textMeasurer.measure(screen.title, labelStyle, softWrap = false, maxLines = 1).size.width.toDp()
                                    }
                                }
                                val selectedPillWidth = (14 + 22 + 6 + 14).dp + (labelWidths.maxOrNull() ?: 0.dp)
                                // Con cuatro destinos el ancho puede no alcanzar junto al botón de agregar en pantallas
                                // angostas: las píldoras cerradas se encogen (hasta 40 dp) en lugar de desbordar
                                val screenWidth = LocalConfiguration.current.screenWidthDp.dp
                                val collapsedCount = visibleScreens.size - 1
                                val fixedWidth = 6.dp * 2 + 2.dp * collapsedCount + selectedPillWidth
                                val fabWidth = if (tabFabVisible) FLOATING_BAR_HEIGHT + 8.dp else 0.dp
                                val collapsedWidth = ((screenWidth - 24.dp - fabWidth - fixedWidth) / collapsedCount.coerceAtLeast(1))
                                    .coerceIn(FLOATING_PILL_MIN_WIDTH, FLOATING_PILL_COLLAPSED_WIDTH)
                                val capsuleWidth = fixedWidth + collapsedWidth * collapsedCount

                                Surface(
                                    shape = MinicashShapes.Card,
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    tonalElevation = 4.dp,
                                    shadowElevation = 0.dp,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                                    modifier = Modifier.width(capsuleWidth)
                                ) {
                                    WithoutMinTouchTarget {
                                        Row(
                                            modifier = Modifier.padding(6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            visibleScreens.forEachIndexed { index, screen ->
                                                val isSelected = currentScreenIndex == index
                                                // El ícono "rebota" al seleccionarse; la píldora cambia de color y de ancho con resortes
                                                val iconScale by animateFloatAsState(
                                                    targetValue = if (isSelected) 1.12f else 1.0f,
                                                    animationSpec = minicashFastSpatialSpec(),
                                                    label = "icon_scale"
                                                )
                                                val pillColor by animateColorAsState(
                                                    targetValue = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                                                    animationSpec = effectsColor,
                                                    label = "pill_color"
                                                )
                                                val iconTint by animateColorAsState(
                                                    targetValue = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    animationSpec = effectsColor,
                                                    label = "icon_tint"
                                                )
                                                val pillWidth by animateDpAsState(
                                                    targetValue = if (isSelected) selectedPillWidth else collapsedWidth,
                                                    animationSpec = spatialDp,
                                                    label = "pill_width"
                                                )
                                                // Espacio que ocupa la etiqueta junto al ícono; el texto aparece con un fundido
                                                val labelSlot by animateDpAsState(
                                                    targetValue = if (isSelected) labelWidths[index] + 6.dp else 0.dp,
                                                    animationSpec = spatialDp,
                                                    label = "label_slot"
                                                )
                                                val labelAlpha by animateFloatAsState(
                                                    targetValue = if (isSelected) 1f else 0f,
                                                    animationSpec = effectsFloat,
                                                    label = "label_alpha"
                                                )

                                                val navInteraction = remember { MutableInteractionSource() }
                                                Surface(
                                                    onClick = { onNavClick(index, screen) },
                                                    shape = MinicashShapes.Pill,
                                                    color = pillColor,
                                                    interactionSource = navInteraction,
                                                    modifier = Modifier.width(pillWidth).bounceOnPress(navInteraction)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.height(40.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.Center
                                                    ) {
                                                        Crossfade(
                                                            targetState = isSelected,
                                                            animationSpec = effectsFloat,
                                                            label = "icon_swap"
                                                        ) { selected ->
                                                            Icon(
                                                                imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                                                contentDescription = screen.title,
                                                                tint = iconTint,
                                                                modifier = Modifier
                                                                    .size(22.dp)
                                                                    .graphicsLayer {
                                                                        scaleX = iconScale
                                                                        scaleY = iconScale
                                                                    }
                                                            )
                                                        }
                                                        Box(
                                                            modifier = Modifier.width(labelSlot),
                                                            contentAlignment = Alignment.CenterStart
                                                        ) {
                                                            Text(
                                                                text = screen.title,
                                                                style = labelStyle,
                                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                                maxLines = 1,
                                                                softWrap = false,
                                                                modifier = Modifier
                                                                    .wrapContentWidth(align = Alignment.Start, unbounded = true)
                                                                    .padding(start = 6.dp)
                                                                    .graphicsLayer { alpha = labelAlpha }
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                ContextualFab(
                                    visible = tabFabVisible, leadingSpacer = true, onClick = onFabClick,
                                    menu = tabFabMenu, expanded = fabMenuExpanded, onExpandedChange = { fabMenuExpanded = it }
                                )
                            }
                        }
                        }
                    }

                    if (navBarStyle == NavBarStyle.INTEGRATED) {
                        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
                            ContextualFab(
                                visible = tabFabVisible, leadingSpacer = false, onClick = onFabClick,
                                menu = tabFabMenu, expanded = fabMenuExpanded, onExpandedChange = { fabMenuExpanded = it }
                            )
                        }
                    }
                }

                if (navBarStyle == NavBarStyle.INTEGRATED) {
                    AnimatedVisibility(
                        visible = !inSubScreen,
                        enter = expandVertically(animationSpec = minicashSpatialSpec()) + fadeIn(animationSpec = minicashEffectsSpec()),
                        exit = shrinkVertically(animationSpec = minicashSpatialSpec()) + fadeOut(animationSpec = minicashEffectsSpec())
                    ) {
                        IntegratedNavBar(
                            screens = visibleScreens,
                            selectedIndex = currentScreenIndex,
                            onItemClick = onNavClick
                        )
                    }
                }
            }
        }
    }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun IntegratedNavBar(
    screens: List<Screen>,
    selectedIndex: Int,
    onItemClick: (Int, Screen) -> Unit
) {
    // El marco oscuro queda solo arriba: la barra es una hoja con esquinas superiores curvas sobre el fondo de la página
    Box(modifier = Modifier.background(MaterialTheme.colorScheme.screenBackground)) {
    ShortNavigationBar(
        modifier = Modifier.clip(MinicashShapes.SheetTop),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        screens.forEachIndexed { index, screen ->
            val selected = index == selectedIndex
            ShortNavigationBarItem(
                selected = selected,
                onClick = { onItemClick(index, screen) },
                icon = {
                    Icon(
                        imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                        contentDescription = screen.title
                    )
                },
                label = {
                    Text(
                        text = screen.title,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            )
        }
    }
    }
}

private const val NAV_SCROLL_THRESHOLD = 2f

/** Alto de la cápsula flotante de pestañas: 6 dp de relleno, el destino de 40 dp y otros 6 dp. */
private val FLOATING_BAR_HEIGHT = 52.dp

/** Ancho de una píldora de la cápsula flotante cuando no está seleccionada: solo el ícono, 22 dp y 13 dp por lado. */
/** Alto de la franja desenfocada al pie: la cápsula (52 dp), su margen, el borde del sistema y un degradado. */
private val FLOATING_BAR_BLUR_HEIGHT = 140.dp
private val FLOATING_PILL_MIN_WIDTH = 40.dp
private val FLOATING_PILL_COLLAPSED_WIDTH = 48.dp

/** Entra y sale deslizándose desde el borde inferior; vive fuera del Column para evitar la ambigüedad de receptores. */
@Composable
private fun SlideVisibility(visible: Boolean, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(animationSpec = minicashSpatialSpec()) { it } + fadeIn(animationSpec = minicashEffectsSpec()),
        exit = slideOutVertically(animationSpec = minicashSpatialSpec()) { it } + fadeOut(animationSpec = minicashEffectsSpec()),
        modifier = modifier
    ) { content() }
}

private class FabMenuAction(val label: String, val icon: ImageVector, val onClick: () -> Unit)

/** Coloca el menú con su borde derecho y su base justo encima del botón. */
private class FabMenuPositionProvider(private val gapPx: Int) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ) = IntOffset(
        x = anchorBounds.right - popupContentSize.width,
        y = anchorBounds.top - popupContentSize.height - gapPx
    )
}

/**
 * Botón de agregar contextual; [visible] lo decide quien lo coloca. Con [menu] el botón despliega las acciones
 * (el primero queda más cerca del botón) en lugar de llamar a [onClick].
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ContextualFab(
    visible: Boolean,
    leadingSpacer: Boolean,
    onClick: () -> Unit,
    menu: List<FabMenuAction>? = null,
    expanded: Boolean = false,
    onExpandedChange: (Boolean) -> Unit = {}
) {
    val spatialFloat = minicashSpatialSpec<Float>()
    val effectsFloat = minicashEffectsSpec<Float>()

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(animationSpec = spatialFloat) + fadeIn(animationSpec = effectsFloat),
        exit = scaleOut(animationSpec = spatialFloat) + fadeOut(animationSpec = effectsFloat)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingSpacer) Spacer(modifier = Modifier.width(8.dp))
            val fabInteractionSource = remember { MutableInteractionSource() }
            val isFabPressed by fabInteractionSource.collectIsPressedAsState()
            val isAnimationRestricted = LocalAnimationRestriction.current
            // Círculo que se encoge un poco al presionarse; en la barra flotante mide lo mismo que la cápsula
            val pressScale by animateFloatAsState(
                targetValue = if (isFabPressed) 0.92f else 1f,
                animationSpec = if (isAnimationRestricted) snap() else minicashFastSpatialSpec(),
                label = "fab_press_scale"
            )
            val fabSize = if (leadingSpacer) FLOATING_BAR_HEIGHT else 56.dp

            val iconRotation by animateFloatAsState(
                targetValue = if (expanded) 135f else 0f,
                animationSpec = if (isAnimationRestricted) snap() else minicashSpatialSpec(),
                label = "fab_icon_rotation"
            )
            val containerColor by animateColorAsState(
                targetValue = if (expanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                animationSpec = minicashEffectsSpec(),
                label = "fab_container"
            )
            val contentColor by animateColorAsState(
                targetValue = if (expanded) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
                animationSpec = minicashEffectsSpec(),
                label = "fab_content"
            )

            FloatingActionButton(
                onClick = { if (menu != null) onExpandedChange(!expanded) else onClick() },
                shape = MinicashShapes.FABShape,
                interactionSource = fabInteractionSource,
                containerColor = containerColor,
                contentColor = contentColor,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 6.dp
                ),
                modifier = Modifier
                    .size(fabSize)
                    .graphicsLayer {
                        scaleX = pressScale
                        scaleY = pressScale
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (menu != null && expanded) "Cerrar menú" else "Añadir",
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer { rotationZ = iconRotation }
                )
            }

            if (menu != null) {
                // El menú vive en su propia ventana para poder crecer hacia arriba sin mover la barra; se conserva
                // mientras corre la animación de salida
                val menuTransition = updateTransition(targetState = expanded, label = "fab_menu")
                if (menuTransition.currentState || menuTransition.targetState) {
                    val gapPx = with(LocalDensity.current) { 12.dp.roundToPx() }
                    Popup(
                        popupPositionProvider = remember(gapPx) { FabMenuPositionProvider(gapPx) },
                        properties = PopupProperties(focusable = false, clippingEnabled = false)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            menu.asReversed().forEach { action ->
                                menuTransition.AnimatedVisibility(
                                    visible = { it },
                                    enter = fadeIn(minicashEffectsSpec()) +
                                        scaleIn(minicashSpatialSpec(), transformOrigin = TransformOrigin(1f, 1f)) +
                                        slideInVertically(minicashSpatialSpec()) { it / 2 },
                                    exit = fadeOut(minicashEffectsSpec()) +
                                        scaleOut(minicashSpatialSpec(), transformOrigin = TransformOrigin(1f, 1f))
                                ) {
                                    FabMenuItem(action)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Velo detrás del menú del botón de agregar; tocarlo lo cierra. Vive fuera del Column por la ambigüedad de receptores. */
@Composable
private fun FabScrim(visible: Boolean, onDismiss: () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = minicashEffectsSpec()),
        exit = fadeOut(animationSpec = minicashEffectsSpec())
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f))
                .pointerInput(Unit) { detectTapGestures { onDismiss() } }
                .clearAndSetSemantics { }
        )
    }
}

/** Acción del menú del botón de agregar: píldora de 56 dp con ícono y texto, en el color del botón. */
@Composable
private fun FabMenuItem(action: FabMenuAction) {
    val interactionSource = remember { MutableInteractionSource() }
    Surface(
        onClick = action.onClick,
        interactionSource = interactionSource,
        modifier = Modifier.bounceOnPress(interactionSource),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.height(56.dp).padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(imageVector = action.icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Text(
                text = action.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

private const val TAB_FADE_MIN_SCALE = 0.96f
private const val SHARED_AXIS_OFFSET = 0.3f

/**
 * Eje X compartido de Material: la pantalla nueva entra desde el lado con fundido y la anterior sale al lado opuesto.
 * Resortes críticamente amortiguados (sin rebote) con la rigidez del esquema de movimiento: espacial para el
 * desplazamiento y de efectos para el fundido, más rápido al salir para que no se encimen las dos pantallas.
 */
private fun sharedAxisX(forward: Boolean): ContentTransform {
    val direction = if (forward) 1 else -1
    val slide = spring<IntOffset>(dampingRatio = 1f, stiffness = 500f, visibilityThreshold = androidx.compose.ui.unit.IntOffset(1, 1))
    return (slideInHorizontally(slide) { (it * SHARED_AXIS_OFFSET).toInt() * direction } +
        fadeIn(spring(dampingRatio = 1f, stiffness = 800f))) togetherWith
        (slideOutHorizontally(slide) { -(it * SHARED_AXIS_OFFSET).toInt() * direction } +
            fadeOut(spring(dampingRatio = 1f, stiffness = 1600f)))
}

private const val ROUTE_SETTINGS = "settings"
private const val ROUTE_ACCOUNTS = "settings/accounts"
private const val ROUTE_CATEGORIES = "settings/categories"
private const val ROUTE_INSTITUTIONS = "settings/institutions"
private const val ROUTE_APPEARANCE = "settings/appearance"
private const val ROUTE_MODULES = "settings/modules"
private const val ROUTE_BACKUPS = "settings/backups"
private const val ROUTE_ABOUT = "settings/about"
private const val ROUTE_REPORTS = "reports"
private const val ROUTE_ACCOUNT_PREFIX = "account/"

/** Profundidad de una ruta: 0 sin pantalla superpuesta, 1 pantallas de primer nivel, 2 subpantallas de Ajustes. */
internal fun routeDepth(route: String?): Int = when {
    route == null -> 0
    route.startsWith("settings/") -> 2
    else -> 1
}

/** Estado del lector de pantalla (exploración táctil); se actualiza si el usuario lo activa o desactiva. */
@Composable
private fun rememberTouchExplorationEnabled(): State<Boolean> {
    val context = LocalContext.current
    val manager = remember { context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager }
    val enabled = remember { mutableStateOf(manager.isTouchExplorationEnabled) }
    DisposableEffect(manager) {
        val listener = AccessibilityManager.TouchExplorationStateChangeListener { enabled.value = it }
        manager.addTouchExplorationStateChangeListener(listener)
        onDispose { manager.removeTouchExplorationStateChangeListener(listener) }
    }
    return enabled
}
