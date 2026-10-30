package com.uramos.minicash.ui.navigation

import androidx.activity.compose.BackHandler
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

    val homeListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val transactionsListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val budgetListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val creditsListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val settingsListState = androidx.compose.foundation.lazy.rememberLazyListState()

    var showAddSheet by remember { mutableStateOf(false) }
    var shortcutType by remember { mutableStateOf<TransactionType?>(null) }
    var showAddBudgetItemFromMain by remember { mutableStateOf(false) }
    var showAddCreditFromMain by remember { mutableStateOf(false) }
    var activeSettingsSubScreen by remember { mutableStateOf<String?>(null) }

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

    if (activeSettingsSubScreen != null) {
        BackHandler {
            activeSettingsSubScreen = null
        }
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
            val targetPage = getShortestTargetPage(pagerState.currentPage, index, numPages)
            coroutineScope.launch {
                pagerState.animateScrollToPage(targetPage)
            }
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
                ) {
                    HorizontalPager(
                        state = pagerState,
                        beyondViewportPageCount = 1,
                        modifier = Modifier
                            .fillMaxSize()
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
                                        coroutineScope.launch {
                                            val txIdx = visibleScreens.indexOf(Screen.Transactions).coerceAtLeast(0)
                                            val targetPage = getShortestTargetPage(pagerState.currentPage, txIdx, numPages)
                                            pagerState.animateScrollToPage(targetPage)
                                        }
                                    },
                                    onNavigateToTransactionsWithFilter = { filterType ->
                                        transactionsViewModel.onTypeFilterSelect(filterType)
                                        coroutineScope.launch {
                                            val txIdx = visibleScreens.indexOf(Screen.Transactions).coerceAtLeast(0)
                                            val targetPage = getShortestTargetPage(pagerState.currentPage, txIdx, numPages)
                                            pagerState.animateScrollToPage(targetPage)
                                        }
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
                                    val isRestricted = LocalAnimationRestriction.current
                                    AnimatedContent(
                                        targetState = activeSettingsSubScreen,
                                        transitionSpec = {
                                            if (isRestricted) {
                                                fadeIn(animationSpec = snap()) togetherWith fadeOut(animationSpec = snap())
                                            } else if (targetState != null) {
                                                (slideInHorizontally(initialOffsetX = { it }) + fadeIn()) togetherWith
                                                        (slideOutHorizontally(targetOffsetX = { -it / 3 }) + fadeOut())
                                            } else {
                                                (slideInHorizontally(initialOffsetX = { -it / 3 }) + fadeIn()) togetherWith
                                                        (slideOutHorizontally(targetOffsetX = { it }) + fadeOut())
                                            }
                                        },
                                        label = "settings_navigation_transition"
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
                                                onNavigateToBackups = { activeSettingsSubScreen = "backups" }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (navBarStyle == NavBarStyle.FLOATING) {
                        // Capa Overlaid Flotante Desacoplada (Alineación simétrica de insets y ancho optimizado)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                                .padding(horizontal = 12.dp, vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val spatialFloat = minicashSpatialSpec<Float>()
                            val spatialSize = minicashSpatialSpec<IntSize>()
                            val effectsFloat = minicashEffectsSpec<Float>()

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
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        visibleScreens.forEachIndexed { index, screen ->
                                            val isSelected = currentScreenIndex == index
                                            val iconScale by animateFloatAsState(
                                                targetValue = if (isSelected) 1.05f else 1.0f,
                                                animationSpec = spatialFloat,
                                                label = "icon_scale"
                                            )

                                            Surface(
                                                onClick = { onNavClick(index, screen) },
                                                shape = MinicashShapes.Pill,
                                                color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                                                modifier = Modifier.animateContentSize(
                                                    animationSpec = spatialSize
                                                )
                                            ) {
                                                if (isSelected) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = screen.selectedIcon,
                                                            contentDescription = screen.title,
                                                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                            modifier = Modifier
                                                                .size(22.dp)
                                                                .graphicsLayer {
                                                                    scaleX = iconScale
                                                                    scaleY = iconScale
                                                                }
                                                        )
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
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(40.dp)
                                                            .padding(8.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = screen.unselectedIcon,
                                                            contentDescription = screen.title,
                                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                                            modifier = Modifier
                                                                .size(22.dp)
                                                                .graphicsLayer {
                                                                    scaleX = iconScale
                                                                    scaleY = iconScale
                                                                }
                                                        )
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

                    if (navBarStyle == NavBarStyle.INTEGRATED) {
                        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
                            ContextualFab(currentScreen = currentScreen, leadingSpacer = false, onClick = onFabClick)
                        }
                    }

                    // Capa Modal Superpuesta para Subpantallas de Configuración (dentro del panel, con el mismo fondo)
                    SettingsSubScreenOverlay(
                        activeSubScreen = activeSettingsSubScreen,
                        settingsViewModel = settingsViewModel,
                        onClose = { activeSettingsSubScreen = null }
                    )
                }

                if (navBarStyle == NavBarStyle.INTEGRATED) {
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

@Composable
private fun SettingsSubScreenOverlay(
    activeSubScreen: String?,
    settingsViewModel: SettingsViewModel,
    onClose: () -> Unit
) {
    AnimatedVisibility(
        visible = activeSubScreen != null,
        enter = slideInHorizontally { width -> width } + fadeIn(),
        exit = slideOutHorizontally { width -> width } + fadeOut(),
        modifier = Modifier.fillMaxSize()
    ) {
        when (activeSubScreen) {
            "accounts" -> AccountsManagementScreen(viewModel = settingsViewModel, onNavigateBack = onClose)
            "categories" -> CategoriesManagementScreen(viewModel = settingsViewModel, onNavigateBack = onClose)
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
    ShortNavigationBar(
        containerColor = minicashFrameColor(),
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
