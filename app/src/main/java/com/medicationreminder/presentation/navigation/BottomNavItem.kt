package com.medicationreminder.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.medicationreminder.R

data class BottomNavItem(
    val screen: Screen,
    val labelResId: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
)

val bottomNavItems = listOf(
    BottomNavItem(
        screen = Screen.Home,
        labelResId = R.string.nav_home,
        icon = Icons.Rounded.Home,
        selectedIcon = Icons.Rounded.Home
    ),
    BottomNavItem(
        screen = Screen.Medications,
        labelResId = R.string.nav_medications,
        icon = Icons.Rounded.Medication,
        selectedIcon = Icons.Rounded.Medication
    ),
    BottomNavItem(
        screen = Screen.Reminders,
        labelResId = R.string.nav_reminders,
        icon = Icons.Rounded.Alarm,
        selectedIcon = Icons.Rounded.Alarm
    ),
    BottomNavItem(
        screen = Screen.Settings,
        labelResId = R.string.nav_settings,
        icon = Icons.Rounded.Settings,
        selectedIcon = Icons.Rounded.Settings
    )
)
