package com.medicationreminder.presentation.navigation

sealed class Screen(val route: String) {
    // Parent Route
    data object Main : Screen("main")

    // Bottom nav destinations
    data object Home : Screen("home")
    data object Medications : Screen("medications")
    data object Reminders : Screen("reminders")
    data object History : Screen("history")
    data object Settings : Screen("settings")

    // Stack destinations
    data object AddMedication : Screen("add_medication")
    data object MedicationDetail : Screen("medication_detail/{medicationId}") {
        fun createRoute(medicationId: Long) = "medication_detail/$medicationId"
    }
}
