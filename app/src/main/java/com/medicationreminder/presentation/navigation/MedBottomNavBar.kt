package com.medicationreminder.presentation.navigation

import android.content.res.Configuration
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medicationreminder.presentation.theme.MedicationReminderTheme

@Composable
fun MedBottomNavBar(
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    onNavItemClick: (Int) -> Unit
) {
    MedBottomNavBarContent(
        selectedIndex = selectedIndex,
        modifier = modifier,
        onNavItemClick = { itemIndex ->
            onNavItemClick(itemIndex)
        }
    )
}

@Composable
private fun MedBottomNavBarContent(
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    onNavItemClick: (Int) -> Unit
) {
    val bgColor = MaterialTheme.colorScheme.background
    val isDark = remember(bgColor) { bgColor.luminance() < 0.5f }

    // Semi-transparent background for blur to show through
    val surfaceColor = if (isDark) Color.Black.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.70f)
    // Fallback opaque color for pre-Android 12
    val solidColor = if (isDark) Color(0xE6121212) else Color(0xE6F5F5F5)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 60.dp, vertical = 10.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // LAYER 1: Background with blur (only this gets blurred)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
                    .then(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            Modifier.background(surfaceColor)
                        } else {
                            Modifier.background(solidColor)
                        }
                    )
            )

            // LAYER 2: Content on top (icons + labels, always crisp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                bottomNavItems.forEachIndexed { index, item ->
                    val isSelected = selectedIndex == index
                    BottomNavItemView(
                        item = item,
                        isSelected = isSelected,
                        isDark = isDark,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (!isSelected) {
                                onNavItemClick(index)
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
    isDark: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val label = stringResource(id = item.labelResId)

    // Single Animatable for pill reveal (drives both width & height in drawBehind)
    val pillAnim = remember { Animatable(if (isSelected) 1f else 0f) }
    LaunchedEffect(isSelected) {
        pillAnim.animateTo(
            targetValue = if (isSelected) 1f else 0f,
            animationSpec = if (isSelected)
                spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)
            else tween(180, easing = FastOutSlowInEasing)
        )
    }
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
        label = "iconScale"
    )
    val iconOffsetY by animateFloatAsState(
        targetValue = if (isSelected) -3f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
        label = "iconOffsetY"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            if (isDark) Color(0xFF4DB6AC) else Color(0xFF00897B)
        } else {
            if (isDark) Color(0xFF9E9E9E) else Color.Black.copy(alpha = 0.6f)
        },
        animationSpec = tween(200),
        label = "contentColor"
    )

    val pillColor = if (isDark) Color(0xFF004D40) else Color(0xFFE0F2F1)

    Box(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .drawBehind {
                val progress = pillAnim.value
                val pillW = size.width * progress
                val pillH = size.height * progress
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
            Icon(
                imageVector = if (isSelected) item.selectedIcon else item.icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                        translationY = iconOffsetY
                    }
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
        MedBottomNavBarContent(
            selectedIndex = 0,
            onNavItemClick = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MedBottomNavBarDarkPreview() {
    MedicationReminderTheme {
        MedBottomNavBarContent(
            selectedIndex = 0,
            onNavItemClick = {}
        )
    }
}
