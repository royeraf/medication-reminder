package com.medicationreminder.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.medicationreminder.R
import com.medicationreminder.data.local.entity.DoseStatus
import com.medicationreminder.domain.model.DoseLog
import com.medicationreminder.util.toFormattedTime

@Composable
fun DoseCard(
    doseLog: DoseLog,
    onTakeDose: (DoseLog) -> Unit,
    modifier: Modifier = Modifier
) {
    val isTaken = doseLog.status == DoseStatus.TAKEN
    val isMissed = doseLog.status == DoseStatus.MISSED
    val isDark = isSystemInDarkTheme()

    val containerColor = when {
        isTaken -> if (isDark) Color(0xFF14532D) else Color(0xFFBBF7D0)
        isMissed -> MaterialTheme.colorScheme.errorContainer
        else -> if (isDark) MaterialTheme.colorScheme.surface else Color(0xFFF0FDF4)
    }

    val contentColor = when {
        isTaken -> if (isDark) Color(0xFF86EFAC) else Color(0xFF166534)
        isMissed -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = if (isTaken || isMissed) contentColor else MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isTaken) 0.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Medication icon with color badge
            Box(modifier = Modifier.size(48.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(14.dp))
                        .background(contentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isTaken) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = stringResource(R.string.home_all_taken),
                            tint = contentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Medication,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                // Label color badge
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.BottomEnd)
                        .border(1.5.dp, containerColor, CircleShape)
                        .background(MedicationColors.getColor(doseLog.color), CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name + time + label
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = doseLog.medicationName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = if (isTaken || isMissed) contentColor else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = doseLog.scheduledTime.toFormattedTime(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = contentColor.copy(alpha = if (isTaken || isMissed) 0.85f else 0.9f)
                    )
                    if (doseLog.scheduleLabel.isNotBlank()) {
                        Text(
                            text = " · ${doseLog.scheduleLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isTaken || isMissed) contentColor.copy(alpha = 0.7f)
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (isTaken && doseLog.takenAt != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    val takenAtText = stringResource(R.string.home_taken_at, doseLog.takenAt.toFormattedTime())
                    Text(
                        text = takenAtText,
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.75f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (!isTaken && !isMissed) {
                FilledTonalButton(
                    onClick = { onTakeDose(doseLog) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = stringResource(R.string.home_take_dose),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}
