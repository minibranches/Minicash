package com.uramos.minicash.ui.navigation

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
import com.uramos.minicash.ui.theme.minicashFrameColor
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

    val visibleScreens = remember(showBudgetTab, showCreditsTab) {
        buildList {
            add(Screen.Home)
            add(Screen.Transactions)
            if (showBudgetTab) add(Screen.Budget)
            if (showCreditsTab) add(Screen.Credits)
            add(Screen.Settings)
        }
    }
    val numPages = visibleScreens.size

    val pageCount = 10_000
    val initialPage = pageCount / 2
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
            val newIndex = visibleScreens.indexOf(staying).takeIf { it >= 0 } ?: visibleScreens.lastIndex
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
    var activeSettingsSubScreen by remember { mutableStateOf<String?>(null) }

    // La barra flotante se oculta al bajar por la página y reaparece al subir, al cambiar de pestaña y en el tope
    var navBarScrollVisible by remember { mutableStateOf(true) }
    val navBarScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (consumed.y < -NAV_SCROLL_THRESHOLD) navBarScrollVisible = false
                else if (consumed.y > NAV_SCROLL_THRESHOLD) navBarScrollVisible = true
                return Offset.Zero
            }
        }
    }
    LaunchedEffect(currentScreenIndex) { navBarScrollVisible = true }
    // El submenú solo oculta la barra mientras se está en Ajustes
    val inSubScreen = activeSettingsSubScreen != null && currentScreen is Screen.Settings

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

    // Subpantallas de Ajustes: eje X compartido; el gesto atrás predictivo arrastra la transición con el dedo
    val subScreenTransitionState = remember { SeekableTransitionState<String?>(null) }
    val subScreenTransition = rememberTransition(subScreenTransitionState, label = "settings_navigation_transition")
    LaunchedEffect(activeSettingsSubScreen) {
        if (subScreenTransitionState.currentState != activeSettingsSubScreen ||
            subScreenTransitionState.targetState != activeSettingsSubScreen
        ) {
            subScreenTransitionState.animateTo(activeSettingsSubScreen)
        }
    }
    PredictiveBackHandler(enabled = activeSettingsSubScreen != null) { progress ->
        val leaving = activeSettingsSubScreen
        try {
            progress.collect { event -> subScreenTransitionState.seekTo(event.progress, targetState = null) }
            activeSettingsSubScreen = null
        } catch (e: CancellationException) {
            subScreenTransitionState.animateTo(leaving)
            throw e
        }
    }

    // Cambio de pestaña desde la barra: fundido con leve escala, como un "fade through"; deslizar sigue funcionando
    val animationsRestricted = LocalAnimationRestriction.current
    val tabFade = remember { Animatable(1f) }
    val fadeToPage: (Int) -> Unit = { targetPage ->
        coroutineScope.launch {
            if (animationsRestricted) {
                pagerState.scrollToPage(targetPage)
            } else {
                tabFade.animateTo(0f, tween(TAB_FADE_OUT_MS, easing = FastOutLinearInEasing))
                pagerState.scrollToPage(targetPage)
                tabFade.animateTo(1f, tween(TAB_FADE_IN_MS, easing = LinearOutSlowInEasing))
            }
        }
    }

    // Atrás desde cualquier pestaña que no sea Inicio regresa a Inicio; en Inicio sale de la app
    BackHandler(enabled = activeSettingsSubScreen == null && currentScreen !is Screen.Home) {
        val homeIndex = visibleScreens.indexOf(Screen.Home).coerceAtLeast(0)
        fadeToPage(getShortestTargetPage(pagerState.currentPage, homeIndex, numPages))
    }

    val onNavClick: (Int, Screen) -> Unit = { index, screen ->
        if (activeSettingsSubScreen != null) {
            activeSettingsSubScreen = null
        }
        haptics.tick()
        if (currentScreenIndex == index) {
            coroutineScope.launch {
                when (screen) {
                    is Screen.Home -> homeListState.animateScrollToItem(0)
                    is Screen.Transactions -> transactionsListState.animateScrollToItem(0)
                    is Screen.Budget -> budgetListState.animateScrollToItem(0)
                    is Screen.Credits -> creditsListState.animateScrollToItem(0)
                    is Screen.Settings -> settingsListState.animateScrollToItem(0)
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
            is Screen.Home, is Screen.Transactions -> showAddSheet = true
            is Screen.Budget -> showAddBudgetItemFromMain = true
            is Screen.Credits -> showAddCreditFromMain = true
            else -> {}
        }
    }

    Scaffold(
        containerColor = minicashFrameColor()
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Panel de contenido: esquinas superiores redondeadas sobre el marco oscuro
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(top = innerPadding.calculateTopPadding())
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                        .nestedScroll(navBarScrollConnection)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        beyondViewportPageCount = 1,
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
                                    onAddTransactionClick = { showAddSheet = true },
                                    onSeeMoreTransactionsClick = {
                                        val txIdx = visibleScreens.indexOf(Screen.Transactions).coerceAtLeast(0)
                                        fadeToPage(getShortestTargetPage(pagerState.currentPage, txIdx, numPages))
                                    }
                                )
                                is Screen.Transactions -> TransactionsScreen(
                                    viewModel = transactionsViewModel,
                                    listState = transactionsListState
                                )
                                is Screen.Budget -> BudgetScreen(
                                    viewModel = budgetViewModel,
                                    listState = budgetListState,
                                    showAddDialogExternal = showAddBudgetItemFromMain,
                                    onAddDialogExternalHandled = { showAddBudgetItemFromMain = false },
                                    onOpenAddTransactionForBudgetItem = { item ->
                                        addTransactionViewModel.prefillForBudgetItem(item)
                                        showAddSheet = true
                                    }
                                )
                                is Screen.Credits -> CreditsScreen(
                                    viewModel = creditsViewModel,
                                    listState = creditsListState,
                                    showAddDialogExternal = showAddCreditFromMain,
                                    onAddDialogExternalHandled = { showAddCreditFromMain = false },
                                    onOpenAddTransactionForCredit = { amountCents, conceptStr, notesStr ->
                                        addTransactionViewModel.prefillForCredit(amountCents, conceptStr, notesStr)
                                        showAddSheet = true
                                    }
                                )
                                else -> {
                                    subScreenTransition.AnimatedContent(
                                        transitionSpec = {
                                            if (animationsRestricted) {
                                                fadeIn(animationSpec = snap()) togetherWith fadeOut(animationSpec = snap())
                                            } else {
                                                sharedAxisX(forward = targetState != null)
                                            }
                                        }
                                    ) { subScreen ->
                                        when (subScreen) {
                                            "accounts" -> AccountsManagementScreen(
                                                viewModel = settingsViewModel,
                                                onNavigateBack = { activeSettingsSubScreen = null }
                                            )
                                            "categories" -> CategoriesManagementScreen(
                                                viewModel = settingsViewModel,
                                                onNavigateBack = { activeSettingsSubScreen = null }
                                            )
                                            "institutions" -> InstitutionsManagementScreen(
                                                viewModel = settingsViewModel,
                                                onNavigateBack = { activeSettingsSubScreen = null }
                                            )
                                            "appearance" -> AppearanceSettingsScreen(
                                                viewModel = settingsViewModel,
                                                onBackClick = { activeSettingsSubScreen = null }
                                            )
                                            "modules" -> ModulesSettingsScreen(
                                                viewModel = settingsViewModel,
                                                onBackClick = { activeSettingsSubScreen = null }
                                            )
                                            "reports" -> ReportsScreen(
                                                viewModel = reportsViewModel,
                                                onBackClick = { activeSettingsSubScreen = null }
                                            )
                                            "about" -> AboutScreen(
                                                onBackClick = { activeSettingsSubScreen = null }
                                            )
                                            "backups" -> BackupSettingsScreen(
                                                viewModel = settingsViewModel,
                                                onBackClick = { activeSettingsSubScreen = null }
                                            )
                                            else -> SettingsScreen(
                                                viewModel = settingsViewModel,
                                                listState = settingsListState,
                                                onNavigateToAccounts = { activeSettingsSubScreen = "accounts" },
                                                onNavigateToCategories = { activeSettingsSubScreen = "categories" },
                                                onNavigateToInstitutions = { activeSettingsSubScreen = "institutions" },
                                                onNavigateToAppearance = { activeSettingsSubScreen = "appearance" },
                                                onNavigateToModules = { activeSettingsSubScreen = "modules" },
                                                onNavigateToReports = { activeSettingsSubScreen = "reports" },
                                                onNavigateToBackups = { activeSettingsSubScreen = "backups" },
                                                onNavigateToAbout = { activeSettingsSubScreen = "about" }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (navBarStyle == NavBarStyle.FLOATING) {
                        // Capa Overlaid Flotante Desacoplada (Alineación simétrica de insets y ancho optimizado)
                        SlideVisibility(
                            visible = navBarScrollVisible && !inSubScreen,
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
                                // Cápsula Flotante de Pestañas Animadas PBT
                                Surface(
                                    shape = RoundedCornerShape(28.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    tonalElevation = 4.dp,
                                    shadowElevation = 0.dp,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                                    modifier = Modifier.animateContentSize(
                                        animationSpec = spatialSize
                                    )
                                ) {
                                    WithoutMinTouchTarget {
                                        Row(
                                            modifier = Modifier.padding(6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            visibleScreens.forEachIndexed { index, screen ->
                                                val isSelected = currentScreenIndex == index
                                                // El ícono "rebota" al seleccionarse y la píldora cambia de color y de ancho con suavidad
                                                val iconScale by animateFloatAsState(
                                                    targetValue = if (isSelected) 1.12f else 1.0f,
                                                    animationSpec = spring(dampingRatio = 0.45f, stiffness = 380f),
                                                    label = "icon_scale"
                                                )
                                                val pillColor by animateColorAsState(
                                                    targetValue = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                                                    animationSpec = effectsColor,
                                                    label = "pill_color"
                                                )
                                                val iconTint by animateColorAsState(
                                                    targetValue = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                                    animationSpec = effectsColor,
                                                    label = "icon_tint"
                                                )
                                                val sidePadding by animateDpAsState(
                                                    targetValue = if (isSelected) 14.dp else 9.dp,
                                                    animationSpec = spatialDp,
                                                    label = "pill_padding"
                                                )

                                                Surface(
                                                    onClick = { onNavClick(index, screen) },
                                                    shape = MinicashShapes.Pill,
                                                    color = pillColor,
                                                    modifier = Modifier.animateContentSize(animationSpec = spatialSize)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.height(40.dp).padding(horizontal = sidePadding),
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
                                                        AnimatedVisibility(
                                                            visible = isSelected,
                                                            enter = expandHorizontally(animationSpec = spatialSize) + fadeIn(animationSpec = effectsFloat),
                                                            exit = shrinkHorizontally(animationSpec = spatialSize) + fadeOut(animationSpec = effectsFloat)
                                                        ) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Spacer(modifier = Modifier.width(6.dp))
                                                                Text(
                                                                    text = screen.title,
                                                                    style = MaterialTheme.typography.labelMedium,
                                                                    fontWeight = FontWeight.SemiBold,
                                                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                                    maxLines = 1,
                                                                    softWrap = false
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                ContextualFab(currentScreen = currentScreen, leadingSpacer = true, onClick = onFabClick)
                            }
                        }
                        }
                    }

                    if (navBarStyle == NavBarStyle.INTEGRATED) {
                        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
                            ContextualFab(currentScreen = currentScreen, leadingSpacer = false, onClick = onFabClick)
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun IntegratedNavBar(
    screens: List<Screen>,
    selectedIndex: Int,
    onItemClick: (Int, Screen) -> Unit
) {
    // El marco oscuro queda solo arriba: la barra es una hoja con esquinas superiores curvas sobre el fondo de la página
    Box(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLowest)) {
    ShortNavigationBar(
        modifier = Modifier.clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
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

/** Botón de agregar contextual: aparece en todas las pestañas excepto Ajustes. */
@Composable
private fun ContextualFab(
    currentScreen: Screen,
    leadingSpacer: Boolean,
    onClick: () -> Unit
) {
    val spatialFloat = minicashSpatialSpec<Float>()
    val effectsFloat = minicashEffectsSpec<Float>()

    AnimatedVisibility(
        visible = currentScreen !is Screen.Settings,
        enter = scaleIn(animationSpec = spatialFloat) + fadeIn(animationSpec = effectsFloat),
        exit = scaleOut(animationSpec = spatialFloat) + fadeOut(animationSpec = effectsFloat)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingSpacer) Spacer(modifier = Modifier.width(8.dp))
            val fabInteractionSource = remember { MutableInteractionSource() }
            val isFabPressed by fabInteractionSource.collectIsPressedAsState()
            val isAnimationRestricted = LocalAnimationRestriction.current
            val fabRadius by animateDpAsState(
                targetValue = if (isFabPressed) 26.dp else 18.dp,
                animationSpec = if (isAnimationRestricted) snap() else MaterialTheme.motionScheme.defaultSpatialSpec(),
                label = "fab_corner_radius"
            )

            FloatingActionButton(
                onClick = onClick,
                shape = RoundedCornerShape(fabRadius),
                interactionSource = fabInteractionSource,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 6.dp
                ),
                modifier = Modifier.size(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private const val TAB_FADE_OUT_MS = 90
private const val TAB_FADE_IN_MS = 210
private const val TAB_FADE_MIN_SCALE = 0.96f
private const val SHARED_AXIS_MS = 300
private const val SHARED_AXIS_FADE_OUT_MS = 90
private const val SHARED_AXIS_OFFSET = 0.3f

/** Eje X compartido de Material: la pantalla nueva entra desde el lado con fundido y la anterior sale al lado opuesto. */
private fun sharedAxisX(forward: Boolean): ContentTransform {
    val direction = if (forward) 1 else -1
    val slide = tween<IntOffset>(SHARED_AXIS_MS, easing = FastOutSlowInEasing)
    return (slideInHorizontally(slide) { (it * SHARED_AXIS_OFFSET).toInt() * direction } +
        fadeIn(tween(SHARED_AXIS_MS - SHARED_AXIS_FADE_OUT_MS, delayMillis = SHARED_AXIS_FADE_OUT_MS, easing = LinearOutSlowInEasing))) togetherWith
        (slideOutHorizontally(slide) { -(it * SHARED_AXIS_OFFSET).toInt() * direction } +
            fadeOut(tween(SHARED_AXIS_FADE_OUT_MS, easing = FastOutLinearInEasing)))
}
