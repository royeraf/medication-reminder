package com.medicationreminder.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.medicationreminder.data.local.entity.DoseStatus
import com.medicationreminder.domain.model.DoseLog
import com.medicationreminder.presentation.theme.Success
import com.medicationreminder.presentation.theme.SuccessContainer
import com.medicationreminder.util.toFormattedTime

@Composable
fun DoseCard(
    doseLog: DoseLog,
    onTakeDose: (DoseLog) -> Unit,
    modifier: Modifier = Modifier
) {
    val isTaken = doseLog.status == DoseStatus.TAKEN
    val isMissed = doseLog.status == DoseStatus.MISSED

    val containerColor = when {
        isTaken -> SuccessContainer
        isMissed -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isTaken) 0.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(52.dp)
            ) {
                Text(
                    text = doseLog.scheduledTime.toFormattedTime(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = when {
                        isTaken -> Success
                        isMissed -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(24.dp)
                        .background(
                            when {
                                isTaken -> Success.copy(alpha = 0.3f)
                                isMissed -> MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                                else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            }
                        )
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Medication icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isTaken) Success.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.primaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isTaken) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Taken",
                        tint = Success,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Medication,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = doseLog.medicationName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (doseLog.scheduleLabel.isNotBlank()) {
                    Text(
                        text = doseLog.scheduleLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (isTaken && doseLog.takenAt != null) {
                    Text(
                        text = "Taken at ${doseLog.takenAt.toFormattedTime()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Success
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
                        text = "Take",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}
