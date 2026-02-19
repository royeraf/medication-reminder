package com.medicationreminder.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
)

val bottomNavItems = listOf(
    BottomNavItem(
        screen = Screen.Home,
        label = "Home",
        icon = Icons.Rounded.Home,
        selectedIcon = Icons.Rounded.Home
    ),
    BottomNavItem(
        screen = Screen.Medications,
        label = "Medications",
        icon = Icons.Rounded.Medication,
        selectedIcon = Icons.Rounded.Medication
    ),
    BottomNavItem(
        screen = Screen.Reminders,
        label = "Reminders",
        icon = Icons.Rounded.Alarm,
        selectedIcon = Icons.Rounded.Alarm
    ),
    BottomNavItem(
        screen = Screen.Settings,
        label = "Settings",
        icon = Icons.Rounded.Settings,
        selectedIcon = Icons.Rounded.Settings
    )
)
