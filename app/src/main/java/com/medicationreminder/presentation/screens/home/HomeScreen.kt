package com.medicationreminder.presentation.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.medicationreminder.R
import com.medicationreminder.data.local.entity.DoseStatus
import com.medicationreminder.domain.model.DoseLog
import com.medicationreminder.presentation.components.DoseCard
import com.medicationreminder.presentation.theme.MedicationReminderTheme
import com.medicationreminder.presentation.theme.Primary
import com.medicationreminder.presentation.theme.PrimaryVariant
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreenContent(
        uiState = uiState,
        onTakeDose = viewModel::markDoseTaken,
        onRefresh = viewModel::refresh
    )
}

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onTakeDose: (DoseLog) -> Unit,
    onRefresh: () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = if (uiState.totalCount > 0)
            uiState.takenCount.toFloat() / uiState.totalCount.toFloat()
        else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "progress"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            HomeHeader(
                greeting = uiState.greeting,
                takenCount = uiState.takenCount,
                totalCount = uiState.totalCount,
                progress = progress,
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
        }
    }
}

@Composable
private fun HomeHeader(
    greeting: String,
    takenCount: Int,
    totalCount: Int,
    progress: Float,
    onRefresh: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
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
                shape = RoundedCornerShape(20.dp),
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
                            Spacer(modifier = Modifier.height(2.dp))
                            val dateStr = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
                                .format(Date())
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

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.25f)
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
        animationSpec = tween(280),
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
            else -> SimpleDateFormat("EEEE, d MMM", Locale.getDefault())
                        .format(Date(dayHistory.dayStartMillis))
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = { expanded = !expanded }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
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
                                color = MaterialTheme.colorScheme.primary
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
            enter = expandVertically(tween(280)) + fadeIn(tween(280)),
            exit = shrinkVertically(tween(280)) + fadeOut(tween(200))
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

@Composable
private fun HistoryChip(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
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
