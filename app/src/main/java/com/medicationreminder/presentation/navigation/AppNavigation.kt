package com.medicationreminder.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
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

// Routes that live at the same level in the bottom nav — they cross-fade instead of sliding.
private val tabRoutes = setOf(
    Screen.Home.route,
    Screen.Medications.route,
    Screen.Reminders.route,
    Screen.Settings.route,
)

@Composable
fun AppNavigation(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val backdrop = rememberLayerBackdrop()

    Scaffold(
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .layerBackdrop(backdrop)
                    .padding(top = innerPadding.calculateTopPadding())
            ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize(),
                enterTransition = {
                    val isTabSwitch = initialState.destination.route in tabRoutes &&
                            targetState.destination.route in tabRoutes
                    if (isTabSwitch) {
                        fadeIn(tween(200, easing = LinearEasing))
                    } else {
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(340, easing = FastOutSlowInEasing)
                        ) + fadeIn(tween(240, easing = LinearEasing))
                    }
                },
                exitTransition = {
                    val isTabSwitch = initialState.destination.route in tabRoutes &&
                            targetState.destination.route in tabRoutes
                    if (isTabSwitch) {
                        fadeOut(tween(200, easing = LinearEasing))
                    } else {
                        slideOutHorizontally(
                            targetOffsetX = { -it / 4 },
                            animationSpec = tween(340, easing = FastOutLinearInEasing)
                        ) + fadeOut(tween(200, easing = LinearEasing))
                    }
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -it / 4 },
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    ) + fadeIn(tween(220, easing = LinearEasing))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300, easing = FastOutLinearInEasing)
                    ) + fadeOut(tween(200, easing = LinearEasing))
                }
            ) {
            composable(Screen.Home.route) {
                HomeScreen()
            }

            composable(Screen.Medications.route) {
                MedicationsScreen(
                    onNavigateToAddMedication = {
                        navController.navigate(Screen.AddMedication.route)
                    },
                    onNavigateToMedicationDetail = { id ->
                        navController.navigate(Screen.MedicationDetail.createRoute(id))
                    }
                )
            }

            composable(Screen.Reminders.route) {
                RemindersScreen(
                    onNavigateToAddMedication = {
                        navController.navigate(Screen.AddMedication.route)
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen()
            }

            composable(Screen.AddMedication.route) {
                AddMedicationScreen(
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
            }

            val bottomNavRoutes = bottomNavItems.map { it.screen.route }
            if (currentRoute in bottomNavRoutes) {
                MedBottomNavBar(
                    navController = navController,
                    backdrop = backdrop,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
fun AppNavigationContent(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val backdrop = rememberLayerBackdrop()

    Scaffold(
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .layerBackdrop(backdrop)
                    .padding(top = innerPadding.calculateTopPadding())
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(Screen.Home.route) {
                        HomeScreenContent(
                            uiState = HomeUiState(greeting = "Hola"),
                            onTakeDose = {},
                            onRefresh = {}
                        )
                    }

                    composable(Screen.Medications.route) {
                        MedicationsScreenContent(
                            uiState = MedicationsUiState(),
                            filteredMedications = emptyList(),
                            onNavigateToAddMedication = {},
                            onNavigateToMedicationDetail = {},
                            onSearchQueryChange = {},
                            onClearDeletedMedication = {}
                        )
                    }

                    composable(Screen.Reminders.route) {
                        RemindersScreenContent(
                            uiState = RemindersUiState(),
                            onNavigateToAddMedication = {},
                            onToggleSchedule = { _, _ -> }
                        )
                    }

                    composable(Screen.Settings.route) {
                        SettingsScreenContent(
                            themeSetting = ThemeSetting.SYSTEM,
                            languageSetting = LanguageSetting.SYSTEM,
                            onThemeChanged = {},
                            onLanguageChanged = {}
                        )
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
                            onAddSchedule = { _, _, _ -> },
                            onUpdateSchedule = { _, _, _, _ -> },
                            onRemoveSchedule = {},
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
                            onAddSchedule = { _, _, _ -> },
                            onUpdateSchedule = { _, _, _, _ -> },
                            onRemoveSchedule = {},
                            onClearError = {}
                        )
                    }
                }
            }

            val bottomNavRoutes = bottomNavItems.map { it.screen.route }
            if (currentRoute in bottomNavRoutes) {
                MedBottomNavBar(
                    navController = navController,
                    backdrop = backdrop,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
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
