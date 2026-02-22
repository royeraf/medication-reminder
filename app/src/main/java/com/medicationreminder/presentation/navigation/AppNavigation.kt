package com.medicationreminder.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.medicationreminder.presentation.screens.home.HomeScreen
import com.medicationreminder.presentation.screens.medications.AddMedicationScreen
import com.medicationreminder.presentation.screens.medications.MedicationsScreen
import com.medicationreminder.presentation.screens.reminders.RemindersScreen
import com.medicationreminder.presentation.screens.history.HistoryScreen
import com.medicationreminder.presentation.screens.settings.SettingsScreen
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.medicationreminder.presentation.theme.MedicationReminderTheme
import com.medicationreminder.presentation.screens.home.HomeScreenContent
import com.medicationreminder.presentation.screens.home.HomeUiState
import com.medicationreminder.presentation.screens.medications.MedicationsScreenContent
import com.medicationreminder.presentation.screens.medications.MedicationsUiState
import com.medicationreminder.presentation.screens.reminders.RemindersScreenContent
import com.medicationreminder.presentation.screens.reminders.RemindersUiState
import com.medicationreminder.presentation.screens.settings.SettingsScreenContent
import com.medicationreminder.data.ThemeSetting
import com.medicationreminder.data.LanguageSetting
import com.medicationreminder.presentation.screens.medications.AddMedicationScreenContent
import com.medicationreminder.presentation.screens.medications.AddMedicationUiState

@Composable
fun AppNavigation(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Screen.Main.route,
            modifier = Modifier.fillMaxSize(),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(200, easing = FastOutSlowInEasing)
                ) + fadeIn(tween(150))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it / 3 },
                    animationSpec = tween(200, easing = FastOutLinearInEasing)
                ) + fadeOut(tween(150))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it / 3 },
                    animationSpec = tween(200, easing = FastOutSlowInEasing)
                ) + fadeIn(tween(150))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(200, easing = FastOutLinearInEasing)
                ) + fadeOut(tween(150))
            }
        ) {
            composable(Screen.Main.route) {
                val homeViewModel: com.medicationreminder.presentation.screens.home.HomeViewModel = androidx.hilt.navigation.compose.hiltViewModel()
                val medicationsViewModel: com.medicationreminder.presentation.screens.medications.MedicationsViewModel = androidx.hilt.navigation.compose.hiltViewModel()
                val remindersViewModel: com.medicationreminder.presentation.screens.reminders.RemindersViewModel = androidx.hilt.navigation.compose.hiltViewModel()
                val settingsViewModel: com.medicationreminder.presentation.screens.settings.SettingsViewModel = androidx.hilt.navigation.compose.hiltViewModel()

                androidx.compose.foundation.pager.HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 1,
                    key = { it }
                ) { page ->
                    when (page) {
                        0 -> HomeScreen(
                            onNavigateToHistory = { navController.navigate(Screen.History.route) },
                            viewModel = homeViewModel
                        )
                        1 -> MedicationsScreen(
                            onNavigateToAddMedication = { navController.navigate(Screen.AddMedication.route) },
                            onNavigateToMedicationDetail = { id -> navController.navigate(Screen.MedicationDetail.createRoute(id)) },
                            viewModel = medicationsViewModel
                        )
                        2 -> RemindersScreen(
                            onNavigateToAddMedication = { navController.navigate(Screen.AddMedication.route) },
                            viewModel = remindersViewModel
                        )
                        3 -> SettingsScreen(
                            viewModel = settingsViewModel
                        )
                    }
                }
            }

            composable(Screen.AddMedication.route) {
                AddMedicationScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.MedicationDetail.route,
                arguments = listOf(navArgument("medicationId") { type = NavType.LongType })
            ) {
                AddMedicationScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        val hideNavBarRoutes = setOf(Screen.AddMedication.route, Screen.MedicationDetail.route, Screen.History.route)
        if (currentRoute !in hideNavBarRoutes) {
            MedBottomNavBar(
                selectedIndex = pagerState.settledPage,
                modifier = Modifier.align(Alignment.BottomCenter),
                onNavItemClick = { index ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                    if (currentRoute != Screen.Main.route) {
                        navController.popBackStack(Screen.Main.route, false)
                    }
                }
            )
        }
    }
    }
}

@Composable
fun AppNavigationContent(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Main.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Screen.Main.route) {
                androidx.compose.foundation.pager.HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 1
                ) { page ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (page) {
                            0 -> HomeScreenContent(
                                uiState = HomeUiState(greeting = "Hola"),
                                onTakeDose = {},
                                onRefresh = {}
                            )
                            1 -> MedicationsScreenContent(
                                uiState = MedicationsUiState(),
                                filteredMedications = emptyList(),
                                onNavigateToAddMedication = {},
                                onNavigateToMedicationDetail = {},
                                onSearchQueryChange = {},
                                onClearDeletedMedication = {}
                            )
                            2 -> RemindersScreenContent(
                                uiState = RemindersUiState(),
                                onNavigateToAddMedication = {},
                                onToggleSchedule = { _, _ -> }
                            )
                            3 -> SettingsScreenContent(
                                themeSetting = ThemeSetting.SYSTEM,
                                languageSetting = LanguageSetting.SYSTEM,
                                onThemeChanged = {},
                                onLanguageChanged = {}
                            )
                        }
                    }
                }
            }

            composable(Screen.AddMedication.route) {
                AddMedicationScreenContent(
                    uiState = AddMedicationUiState(),
                    onNavigateBack = {},
                    onSaveMedication = {},
                    onDeleteMedication = {},
                    onSearchQueryChange = {},
                    onDrugSelected = {},
                    onMedicationNameChange = {},
                    onStrengthChange = {},
                    onDosageFormChange = {},
                    onInstructionsChange = {},
                    onColorSelected = {},
                    onAddSchedule = { _, _, _, _ -> },
                    onUpdateSchedule = { _, _, _, _, _ -> },
                    onRemoveSchedule = {},
                    onUpdateScheduleDays = { _, _ -> },
                    onClearError = {}
                )
            }

            composable(
                route = Screen.MedicationDetail.route,
                arguments = listOf(navArgument("medicationId") { type = NavType.LongType })
            ) {
                AddMedicationScreenContent(
                    uiState = AddMedicationUiState(),
                    onNavigateBack = {},
                    onSaveMedication = {},
                    onDeleteMedication = {},
                    onSearchQueryChange = {},
                    onDrugSelected = {},
                    onMedicationNameChange = {},
                    onStrengthChange = {},
                    onDosageFormChange = {},
                    onInstructionsChange = {},
                    onColorSelected = {},
                    onAddSchedule = { _, _, _, _ -> },
                    onUpdateSchedule = { _, _, _, _, _ -> },
                    onRemoveSchedule = {},
                    onUpdateScheduleDays = { _, _ -> },
                    onClearError = {}
                )
            }
        }

        val hideNavBarRoutes = setOf(Screen.AddMedication.route, Screen.MedicationDetail.route)
        if (currentRoute !in hideNavBarRoutes) {
            MedBottomNavBar(
                selectedIndex = pagerState.settledPage,
                modifier = Modifier.align(Alignment.BottomCenter),
                onNavItemClick = { index ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppNavigationPreview() {
    MedicationReminderTheme {
        AppNavigationContent(navController = rememberNavController())
    }
}
