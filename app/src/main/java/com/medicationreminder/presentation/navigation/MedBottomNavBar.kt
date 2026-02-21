package com.medicationreminder.presentation.navigation

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.medicationreminder.presentation.theme.MedicationReminderTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeChild

@Composable
fun MedBottomNavBar(
    navController: NavController,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    MedBottomNavBarContent(
        currentRoute = currentRoute,
        hazeState = hazeState,
        modifier = modifier,
        onNavItemClick = { item ->
            navController.navigate(item.screen.route) {
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    )
}

@Composable
private fun MedBottomNavBarContent(
    currentRoute: String?,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    onNavItemClick: (BottomNavItem) -> Unit
) {
    val isDark = isSystemInDarkTheme()

    // Frosted glass style — very high opacity in light mode for maximum contrast
    val hazeStyle = HazeStyle(
        blurRadius = 30.dp,
        noiseFactor = 0.05f,
        backgroundColor = MaterialTheme.colorScheme.background,
        tints = listOf(
            HazeTint(
                if (isDark) Color(0xFF1C1C1E).copy(alpha = 0.70f)
                else Color.White.copy(alpha = 0.92f)
            )
        )
    )

    // Very subtle shimmer
    val shimmer = if (isDark) Color.Transparent
    else Color.White.copy(alpha = 0.15f)

    val shadowColor = if (isDark) Color.Black.copy(alpha = 0.45f)
    else Color.Black.copy(alpha = 0.12f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (isDark) 24.dp else 14.dp,
                    shape = CircleShape,
                    clip = false,
                    ambientColor = shadowColor,
                    spotColor = shadowColor
                )
                .clip(CircleShape)
                .hazeChild(state = hazeState, shape = CircleShape, style = hazeStyle)
        ) {
            // Top shimmer overlay
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to shimmer,
                                0.45f to Color.Transparent
                            )
                        )
                    )
            )

            // Navigation items
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                bottomNavItems.forEach { item ->
                    val isSelected = currentRoute == item.screen.route
                    BottomNavItemView(
                        item = item,
                        isSelected = isSelected,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (!isSelected) {
                                onNavItemClick(item)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavItemView(
    item: BottomNavItem,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val label = stringResource(id = item.labelResId)

    val backgroundAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "backgroundAlpha"
    )
    val backgroundScale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.6f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium),
        label = "backgroundScale"
    )

    val isDark = isSystemInDarkTheme()

    // In light mode the nav bar is near-white (frosted glass at 0.92 opacity).
    // primary (#008080) on white has ~3.7:1 contrast — less than inactive tabs
    // which use onSurface at 40% (~5.5:1). Fix: use onPrimaryContainer (near-black
    // teal) so the active item is always the darkest, most readable element.
    val contentColor by animateColorAsState(
        targetValue = when {
            isSelected -> if (isDark) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onPrimaryContainer
            isDark     -> Color.White.copy(alpha = 0.50f)
            else       -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.40f)
        },
        animationSpec = tween(250),
        label = "contentColor"
    )

    // Pill color: in light mode use the solid primaryContainer surface so the
    // pill is clearly visible instead of a near-invisible 16%-opacity ghost.
    val pillColor = if (isDark)
        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    else
        MaterialTheme.colorScheme.primaryContainer

    Box(
        modifier = modifier
            .height(60.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Pill background: sized consistently regardless of content length
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 4.dp)
                .graphicsLayer {
                    alpha = backgroundAlpha
                    scaleX = backgroundScale
                    scaleY = backgroundScale
                }
                .background(color = pillColor, shape = CircleShape)
        )

        // Content: centered in the item area
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isSelected) item.selectedIcon else item.icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 9.sp
                ),
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MedBottomNavBarPreview() {
    MedicationReminderTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(20.dp)) {
            MedBottomNavBarContent(
                currentRoute = Screen.Home.route,
                hazeState = remember { HazeState() },
                onNavItemClick = {}
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MedBottomNavBarDarkPreview() {
    MedicationReminderTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(20.dp)) {
            MedBottomNavBarContent(
                currentRoute = Screen.Home.route,
                hazeState = remember { HazeState() },
                onNavItemClick = {}
            )
        }
    }
}
