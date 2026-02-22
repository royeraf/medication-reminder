package com.medicationreminder.presentation.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.medicationreminder.R
import com.medicationreminder.data.local.entity.DoseStatus
import com.medicationreminder.domain.model.DoseLog
import com.medicationreminder.presentation.components.DoseCard
import com.medicationreminder.presentation.theme.ExpressiveMotion
import com.medicationreminder.presentation.theme.ExpressiveShapes
import com.medicationreminder.presentation.theme.MedicationReminderTheme
import com.medicationreminder.presentation.theme.Primary
import com.medicationreminder.presentation.theme.PrimaryVariant
import com.medicationreminder.util.toFormattedTime
import com.medicationreminder.util.toFormattedHeaderDate
import com.medicationreminder.util.toFormattedAccordionDate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.*

@Composable
fun HomeScreen(
    onNavigateToHistory: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreenContent(
        uiState = uiState,
        onTakeDose = viewModel::markDoseTaken,
        onRefresh = viewModel::refresh,
        onNavigateToHistory = onNavigateToHistory
    )
}

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onTakeDose: (DoseLog) -> Unit,
    onRefresh: () -> Unit,
    onNavigateToHistory: () -> Unit = {}
) {
    val targetProgress = if (uiState.totalCount > 0)
        uiState.takenCount.toFloat() / uiState.totalCount.toFloat()
    else 0f
    // M3E: spring animation for progress bar–feels alive, not mechanical
    val progressAnim = remember { Animatable(0f) }
    LaunchedEffect(targetProgress) {
        progressAnim.animateTo(
            targetValue = targetProgress,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            HomeHeader(
                greeting = uiState.greeting,
                takenCount = uiState.takenCount,
                totalCount = uiState.totalCount,
                progressProvider = { progressAnim.value },
                onRefresh = onRefresh
            )
        }

        val hasAny = uiState.upcomingDoses.isNotEmpty() || uiState.historyByDay.isNotEmpty()

        if (!hasAny && !uiState.isLoading) {
            item { EmptyTodayDoses() }
        } else {
            if (uiState.upcomingDoses.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = stringResource(R.string.home_section_upcoming),
                        topPadding = 16.dp
                    )
                }
                items(uiState.upcomingDoses, key = { "up_${it.id}" }) { doseLog ->
                    DoseCard(
                        doseLog = doseLog,
                        onTakeDose = onTakeDose,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            uiState.historyByDay.forEach { dayHistory ->
                item(key = "hist_${dayHistory.dayStartMillis}") {
                    HistoryAccordion(
                        dayHistory = dayHistory,
                        onTakeDose = onTakeDose
                    )
                }
            }

            // 7-Day History navigation button
            item {
                ViewHistoryButton(onClick = onNavigateToHistory)
            }
        }
    }
}

@Composable
private fun ViewHistoryButton(onClick: () -> Unit) {
    val isDark = isSystemInDarkTheme()

    val cardGradient = if (isDark) {
        Brush.linearGradient(listOf(Color(0xFF0F2027), Color(0xFF1A3A4A)))
    } else {
        Brush.linearGradient(listOf(Color(0xFFE0F7FA), Color(0xFFB2EBF2)))
    }

    val iconGradient = if (isDark) {
        Brush.linearGradient(listOf(Color(0xFF00BCD4), Color(0xFF26C6DA)))
    } else {
        Brush.linearGradient(listOf(Primary, PrimaryVariant))
    }

    val titleColor = if (isDark) Color(0xFFE0F2F1) else Color(0xFF004D40)
    val subtitleColor = if (isDark) Color(0xFF80CBC4) else Color(0xFF00796B)
    val chevronColor = if (isDark) Color(0xFF4DD0E1) else Color(0xFF00897B)

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = ExpressiveShapes.card,              // M3E
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 4.dp else 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardGradient, ExpressiveShapes.card)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(ExpressiveShapes.iconContainer)    // M3E
                    .background(iconGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.CalendarMonth,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.history_title),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = titleColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.history_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = subtitleColor
                )
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(ExpressiveShapes.iconContainer)    // M3E
                    .background(chevronColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = chevronColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun HomeHeader(
    greeting: String,
    takenCount: Int,
    totalCount: Int,
    progressProvider: () -> Float,
    onRefresh: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ExpressiveShapes.header)           // M3E: expressive header shape
            .background(
                Brush.linearGradient(listOf(Primary, PrimaryVariant))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = stringResource(R.string.home_title),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                }
                Row {
                    IconButton(
                        onClick = onRefresh,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.2f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = stringResource(R.string.home_refresh_desc),
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {},
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.2f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Notifications,
                            contentDescription = stringResource(R.string.home_notifications_desc),
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = ExpressiveShapes.cardLarge,  // M3E: larger card shape
                color = Color.White.copy(alpha = 0.18f)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.home_today_progress),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            val dateStr = remember {
                                System.currentTimeMillis().toFormattedHeaderDate()
                            }
                            Text(
                                text = dateStr,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                        Text(
                            text = "$takenCount / $totalCount",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // M3E Expressive Progress Bar
                    M3ExpressiveProgressBar(
                        progress = progressProvider(),
                        totalCount = totalCount,
                        takenCount = takenCount,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = when {
                            totalCount == 0 -> stringResource(R.string.home_no_medications_today)
                            takenCount == totalCount -> stringResource(R.string.home_all_taken)
                            else -> stringResource(R.string.home_doses_remaining, totalCount - takenCount)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun EmptyTodayDoses() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.home_empty_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.home_empty_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, topPadding: androidx.compose.ui.unit.Dp = 12.dp) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = androidx.compose.ui.unit.TextUnit(0.8f, androidx.compose.ui.unit.TextUnitType.Sp)
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = topPadding, bottom = 4.dp)
    )
}

@Composable
private fun HistoryAccordion(
    dayHistory: DayHistory,
    onTakeDose: (DoseLog) -> Unit
) {
    var expanded by remember { mutableStateOf(dayHistory.isToday) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = ExpressiveMotion.chevronRotation, // M3E: spring rotation
        label = "chevron"
    )

    val pastDoses = dayHistory.doses
    val takenCount = pastDoses.count { it.status == DoseStatus.TAKEN }
    val missedCount = pastDoses.count { it.status == DoseStatus.MISSED }

    val todayLabel = stringResource(R.string.home_today_label)
    val yesterdayLabel = stringResource(R.string.home_yesterday_label)
    val dayLabel = remember(dayHistory.dayStartMillis) {
        val cal = java.util.Calendar.getInstance()
        val yesterdayCal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -1) }
        cal.timeInMillis = dayHistory.dayStartMillis
        when {
            dayHistory.isToday -> todayLabel
            cal.get(java.util.Calendar.YEAR) == yesterdayCal.get(java.util.Calendar.YEAR) &&
            cal.get(java.util.Calendar.DAY_OF_YEAR) == yesterdayCal.get(java.util.Calendar.DAY_OF_YEAR) ->
                yesterdayLabel
            else -> dayHistory.dayStartMillis.toFormattedAccordionDate()
        }
    }

    val isDark = isSystemInDarkTheme()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = ExpressiveShapes.card,               // M3E
        colors = CardDefaults.cardColors(containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(ExpressiveShapes.iconContainer) // M3E
                    .background(if (isDark) Color(0xFF134E4A) else Color(0xFFCCFBF1)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.History,
                    contentDescription = null,
                    tint = if (isDark) Color(0xFF5EEAD4) else Color(0xFF0F766E),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dayLabel,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (takenCount > 0 || missedCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (takenCount > 0) {
                            HistoryChip(
                                text = stringResource(R.string.home_history_taken, takenCount),
                                color = if (isDark) Color(0xFF5EEAD4) else Color(0xFF0F766E)
                            )
                        }
                        if (missedCount > 0) {
                            HistoryChip(
                                text = stringResource(R.string.home_history_missed, missedCount),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Icon(
                imageVector = Icons.Rounded.ExpandMore,
                contentDescription = null,
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer { rotationZ = rotation },
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = ExpressiveMotion.accordionEnter,  // M3E: bouncy spring
            exit = ExpressiveMotion.accordionExit
        ) {
            Column(modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp)) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                pastDoses.forEach { doseLog ->
                    DoseCard(
                        doseLog = doseLog,
                        onTakeDose = onTakeDose,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun HistoryChip(text: String, color: Color) {
    Surface(
        shape = ExpressiveShapes.chip,              // M3E: pill chip
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = color,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}


// ─── M3 Expressive Progress Bar ──────────────────────────────────────────────
/**
 * M3 Expressive progress bar for the Home screen header.
 *
 * Features:
 *  - Rounded pill track with layered gradient fill (white → white-alpha)
 *  - Glow halo pulse on the progress tip
 *  - Dose-tick markers (one per medication dose in totalCount)
 *  - Spring-physics fill animation (via [progress] already animated upstream)
 *  - "100% ✓" celebration text when fully complete
 *  - Percentage label at the leading end of the fill
 */
@Composable
private fun M3ExpressiveProgressBar(
    progress: Float,                 // 0f … 1f (already spring-animated)
    totalCount: Int,
    takenCount: Int,
    modifier: Modifier = Modifier,
) {
    val isComplete = takenCount > 0 && takenCount == totalCount

    // Celebration pulse when finished
    val glowAlpha by animateFloatAsState(
        targetValue = if (isComplete) 0.55f else 0.25f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "glow"
    )

    Column(modifier = modifier) {
        // ── Main track ────────────────────────────────────────────────────────
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
        ) {
            val trackH = size.height
            val trackW = size.width
            val radius = trackH / 2f
            val strokeWidth = trackH

            // 1. Track background (frosted dark pill)
            drawRoundRect(
                color = Color.White.copy(alpha = 0.20f),
                size = size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius)
            )

            // 2. Filled segment (white with gradient opacity)
            val fillW = (trackW * progress.coerceIn(0f, 1f)).coerceAtLeast(radius * 2)
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.White,
                        Color.White.copy(alpha = 0.90f)
                    ),
                    startX = 0f,
                    endX = fillW
                ),
                size = androidx.compose.ui.geometry.Size(fillW, trackH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius)
            )

            // 3. Glow halo at the fill tip (M3E: "alive" leading edge)
            if (progress > 0.02f) {
                val tipX = fillW - radius
                drawCircle(
                    color = Color.White.copy(alpha = glowAlpha),
                    radius = radius * 2.2f,
                    center = androidx.compose.ui.geometry.Offset(tipX, size.height / 2f)
                )
            }

            // 4. Dose-tick markers (thin vertical lines per dose slot)
            if (totalCount > 1) {
                val slotW = trackW / totalCount
                for (i in 1 until totalCount) {
                    val tickX = slotW * i
                    drawLine(
                        color = Color.Black.copy(alpha = 0.18f),
                        start = androidx.compose.ui.geometry.Offset(tickX, trackH * 0.2f),
                        end = androidx.compose.ui.geometry.Offset(tickX, trackH * 0.8f),
                        strokeWidth = 1.5.dp.toPx()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // ── Status row below the track ─────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Percentage label (M3E: dynamic typography)
            Text(
                text = if (isComplete) "✓ 100%" else "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = if (isComplete)
                    Color.White
                else
                    Color.White.copy(alpha = 0.85f),
                fontWeight = if (isComplete) FontWeight.Bold else FontWeight.Medium
            )

            // Taken / Total fraction chips
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "$takenCount / $totalCount",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MedicationReminderTheme {
        HomeScreenContent(
            uiState = HomeUiState(
                greeting = "Good morning",
                takenCount = 1,
                totalCount = 3,
                upcomingDoses = listOf(
                    DoseLog(
                        id = 1,
                        medicationId = 1,
                        scheduleId = 1,
                        scheduledTime = System.currentTimeMillis(),
                        status = DoseStatus.TAKEN,
                        medicationName = "Aspirin",
                        scheduleLabel = "8:00 AM"
                    )
                ),
                historyByDay = listOf(
                    DayHistory(
                        dayStartMillis = System.currentTimeMillis(),
                        isToday = true,
                        doses = listOf(
                             DoseLog(
                                id = 2,
                                medicationId = 2,
                                scheduleId = 2,
                                scheduledTime = System.currentTimeMillis() + 100000,
                                status = DoseStatus.PENDING,
                                medicationName = "Vitamin C",
                                scheduleLabel = "12:00 PM"
                            ),
                            DoseLog(
                                id = 3,
                                medicationId = 3,
                                scheduleId = 3,
                                scheduledTime = System.currentTimeMillis() + 200000,
                                status = DoseStatus.PENDING,
                                medicationName = "Omega 3",
                                scheduleLabel = "6:00 PM"
                            )
                        )
                    )
                )
            ),
            onTakeDose = {},
            onRefresh = {}
        )
    }
}
