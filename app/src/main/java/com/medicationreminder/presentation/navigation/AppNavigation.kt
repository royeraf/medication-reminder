package com.medicationreminder.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.medicationreminder.presentation.screens.home.HomeScreen
import com.medicationreminder.presentation.screens.medications.AddMedicationScreen
import com.medicationreminder.presentation.screens.medications.MedicationsScreen
import com.medicationreminder.presentation.screens.reminders.RemindersScreen
import com.medicationreminder.presentation.screens.settings.SettingsScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    Scaffold(
        bottomBar = {
            // Hide bottom nav on stack screens
            val currentRoute = navController
                .currentBackStackEntry?.destination?.route
            val bottomNavRoutes = bottomNavItems.map { it.screen.route }
            if (currentRoute in bottomNavRoutes) {
                MedBottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(280)
                ) + fadeIn(tween(280))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it / 3 },
                    animationSpec = tween(280)
                ) + fadeOut(tween(280))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it / 3 },
                    animationSpec = tween(280)
                ) + fadeIn(tween(280))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(280)
                ) + fadeOut(tween(280))
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
                // Placeholder — can be expanded to a full detail/edit screen
                MedicationsScreen(
                    onNavigateToAddMedication = {
                        navController.navigate(Screen.AddMedication.route)
                    },
                    onNavigateToMedicationDetail = {}
                )
            }
        }
    }
}
