package com.medicationreminder.presentation.navigation

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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

@Composable
fun MedBottomNavBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    MedBottomNavBarContent(
        currentRoute = currentRoute,
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
    modifier: Modifier = Modifier,
    onNavItemClick: (BottomNavItem) -> Unit
) {
    val navBarColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val shadowColor = Color.Black.copy(alpha = 0.18f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 50.dp, vertical = 10.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 15.dp,
                    shape = CircleShape,
                    clip = false,
                    ambientColor = shadowColor,
                    spotColor = shadowColor
                )
                .clip(CircleShape)
                .background(color = navBarColor, shape = CircleShape)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 4.dp),
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

    // Animatable garantiza que .value se lea en el draw layer → se redibuja en cada frame
    val pillWidthAnim = remember { Animatable(if (isSelected) 1f else 0f) }
    val pillHeightAnim = remember { Animatable(if (isSelected) 1f else 0.7f) }
    LaunchedEffect(isSelected) {
        if (isSelected) {
            pillWidthAnim.animateTo(1f, tween(350, easing = FastOutSlowInEasing))
        } else {
            pillWidthAnim.animateTo(0f, tween(200, easing = FastOutSlowInEasing))
        }
    }
    LaunchedEffect(isSelected) {
        if (isSelected) {
            pillHeightAnim.animateTo(1f, tween(250, easing = FastOutSlowInEasing))
        } else {
            pillHeightAnim.animateTo(0.7f, tween(150, easing = FastOutSlowInEasing))
        }
    }
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow),
        label = "iconScale"
    )
    val iconOffsetY by animateFloatAsState(
        targetValue = if (isSelected) -2f else 0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium),
        label = "iconOffsetY"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                      else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(250),
        label = "contentColor"
    )

    // Wobble rotation on selection
    val iconRotation = remember { Animatable(0f) }
    LaunchedEffect(isSelected) {
        if (isSelected) {
            iconRotation.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    -14f at 80
                    10f at 180
                    -5f at 270
                    0f at 400
                }
            )
        }
    }

    val pillColor = MaterialTheme.colorScheme.primaryContainer

    Box(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .drawBehind {
                // Leer .value aquí suscribe el draw layer directamente al Animatable
                val pillW = size.width * pillWidthAnim.value
                val pillH = size.height * pillHeightAnim.value
                val left = (size.width - pillW) / 2f
                val top = (size.height - pillH) / 2f
                drawRoundRect(
                    color = pillColor,
                    topLeft = Offset(left, top),
                    size = Size(pillW, pillH),
                    cornerRadius = CornerRadius(pillH / 2f)
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Content: icon + label with symmetric padding
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedContent(
                targetState = isSelected,
                transitionSpec = {
                    (scaleIn(spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium)) +
                     fadeIn(tween(180))).togetherWith(
                        scaleOut(tween(120)) + fadeOut(tween(100))
                    )
                },
                label = "iconTransition"
            ) { selected ->
                Icon(
                    imageVector = if (selected) item.selectedIcon else item.icon,
                    contentDescription = label,
                    tint = contentColor,
                    modifier = Modifier
                        .size(22.dp)
                        .graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                            translationY = iconOffsetY
                            rotationZ = iconRotation.value
                        }
                )
            }

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
                onNavItemClick = {}
            )
        }
    }
}
