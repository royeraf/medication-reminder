package com.medicationreminder.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.medicationreminder.R

data class BottomNavItem(
    val screen: Screen,
    val labelResId: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        screen = Screen.Home,
        labelResId = R.string.nav_home,
        icon = Icons.Outlined.Home,
        selectedIcon = Icons.Rounded.Home
    ),
    BottomNavItem(
        screen = Screen.Medications,
        labelResId = R.string.nav_medications,
        icon = Icons.Outlined.Medication,
        selectedIcon = Icons.Rounded.Medication
    ),
    BottomNavItem(
        screen = Screen.Reminders,
        labelResId = R.string.nav_reminders,
        icon = Icons.Outlined.Notifications,
        selectedIcon = Icons.Rounded.Notifications
    ),
    BottomNavItem(
        screen = Screen.Settings,
        labelResId = R.string.nav_settings,
        icon = Icons.Outlined.Settings,
        selectedIcon = Icons.Rounded.Settings
    )
)
