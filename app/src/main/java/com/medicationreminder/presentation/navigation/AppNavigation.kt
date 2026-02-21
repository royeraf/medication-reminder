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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
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
    val hazeState = remember { HazeState() }

    Scaffold(
        containerColor = Color.Transparent
    ) { innerPadding ->
        // Outer Box fills the full screen — nav bar is an overlay so it never
        // changes the content area size (eliminates the startup resize caused by
        // the Scaffold re-measuring the bottomBar when window insets resolve).
        Box(modifier = Modifier.fillMaxSize()) {
            // haze() marks this area as the source to blur — the nav bar reads from it
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
                    .haze(hazeState)
            ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize(),
                // ── Tab switch ────────────────────────────────────────────────
                // Tabs are peers; a horizontal slide implies hierarchy that
                // doesn't exist.  A quick cross-fade feels natural and avoids
                // the disorientation of sliding in the "wrong" direction.
                //
                // ── Stack push/pop ────────────────────────────────────────────
                // The incoming screen slides the full width while the outgoing
                // screen moves only ¼ of the way — classic parallax depth cue.
                // FastOutSlowInEasing (decelerate) on enter and
                // FastOutLinearInEasing (accelerate) on exit match the Material
                // motion spec and feel significantly more natural than linear.
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
                // Pop: user is going back, so reverse the direction.
                // Slightly shorter duration feels more responsive.
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
            } // haze Box

            // Nav bar as full-screen overlay — positioned at the bottom, never
            // affects the content area size regardless of window inset changes.
            val bottomNavRoutes = bottomNavItems.map { it.screen.route }
            if (currentRoute in bottomNavRoutes) {
                MedBottomNavBar(
                    navController = navController,
                    hazeState = hazeState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        } // outer Box
    }
}

@Preview
@Composable
fun AppNavigationPreview() {
    MedicationReminderTheme {
        AppNavigation(navController = rememberNavController())
    }
}
