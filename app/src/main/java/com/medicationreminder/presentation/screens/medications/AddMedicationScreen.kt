package com.medicationreminder.presentation.screens.medications

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import com.medicationreminder.presentation.theme.ExpressiveShapes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.medicationreminder.R
import com.medicationreminder.domain.model.DrugSearchResult
import com.medicationreminder.domain.model.Schedule
import com.medicationreminder.presentation.components.MedSearchBar
import com.medicationreminder.presentation.components.MedTimePickerDialog
import com.medicationreminder.presentation.components.MedicationColors
import com.medicationreminder.presentation.theme.MedicationReminderTheme
import com.medicationreminder.util.formatTime

@Composable
fun AddMedicationScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddMedicationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    AddMedicationScreenContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onSaveMedication = viewModel::saveMedication,
        onDeleteMedication = viewModel::deleteMedication,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onDrugSelected = viewModel::onDrugSelected,
        onMedicationNameChange = viewModel::onMedicationNameChange,
        onStrengthChange = viewModel::onStrengthChange,
        onDosageFormChange = viewModel::onDosageFormChange,
        onInstructionsChange = viewModel::onInstructionsChange,
        onColorSelected = viewModel::onColorSelected,
        onAddSchedule = viewModel::addSchedule,
        onUpdateSchedule = viewModel::updateSchedule,
        onRemoveSchedule = viewModel::removeSchedule,
        onUpdateScheduleDays = viewModel::updateScheduleDays,
        onClearError = viewModel::clearError
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddMedicationScreenContent(
    uiState: AddMedicationUiState,
    onNavigateBack: () -> Unit,
    onSaveMedication: () -> Unit,
    onDeleteMedication: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onDrugSelected: (DrugSearchResult) -> Unit,
    onMedicationNameChange: (String) -> Unit,
    onStrengthChange: (String) -> Unit,
    onDosageFormChange: (String) -> Unit,
    onInstructionsChange: (String) -> Unit,
    onColorSelected: (Int) -> Unit,
    onAddSchedule: (Int, Int, String, List<Int>) -> Unit,
    onUpdateSchedule: (Int, Int, Int, String, List<Int>) -> Unit,
    onRemoveSchedule: (Int) -> Unit,
    onUpdateScheduleDays: (Int, List<Int>) -> Unit,
    onClearError: () -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }
    var editingScheduleIndex by remember { mutableIntStateOf(-1) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val isEditing = uiState.medicationId != 0L

    // Navigate back on save or delete success
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) onNavigateBack()
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    Icons.Rounded.DeleteForever,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text(stringResource(R.string.medication_delete_dialog_title)) },
            text = {
                Text(
                    stringResource(R.string.medication_delete_dialog_msg, uiState.medicationName),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteMedication()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(stringResource(R.string.medication_delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.medication_delete_cancel))
                }
            }
        )
    }

    // Time picker dialog
    if (showTimePicker) {
        val initial = if (editingScheduleIndex >= 0)
            uiState.schedules.getOrNull(editingScheduleIndex)
        else null

        val morningLabel = stringResource(R.string.period_morning)
        val afternoonLabel = stringResource(R.string.period_afternoon)
        val eveningLabel = stringResource(R.string.period_evening)
        val nightLabel = stringResource(R.string.period_night)

        MedTimePickerDialog(
            onDismiss = { showTimePicker = false; editingScheduleIndex = -1 },
            onConfirm = { hour, minute ->
                val label = when {
                    hour in 5..11 -> morningLabel
                    hour in 12..16 -> afternoonLabel
                    hour in 17..20 -> eveningLabel
                    else -> nightLabel
                }
                if (editingScheduleIndex >= 0) {
                    val existingDays = uiState.schedules.getOrNull(editingScheduleIndex)?.daysOfWeek
                        ?: listOf(1, 2, 3, 4, 5, 6, 7)
                    onUpdateSchedule(editingScheduleIndex, hour, minute, label, existingDays)
                } else {
                    onAddSchedule(hour, minute, label, listOf(1, 2, 3, 4, 5, 6, 7))
                }
                showTimePicker = false
                editingScheduleIndex = -1
            },
            initialHour = initial?.hour ?: 8,
            initialMinute = initial?.minute ?: 0
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditing) stringResource(R.string.medication_edit_title) 
                        else stringResource(R.string.medication_add_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = stringResource(R.string.back_button))
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Rounded.DeleteForever,
                                contentDescription = stringResource(R.string.medication_delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Button(
                    onClick = onSaveMedication,
                    enabled = !uiState.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .height(52.dp),
                    shape = ExpressiveShapes.button        // M3E: expressive button
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Rounded.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (isEditing) stringResource(R.string.medication_update) 
                            else stringResource(R.string.medication_save),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error message
            if (uiState.errorMessage != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = ExpressiveShapes.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.errorMessage!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = onClearError, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Section: Search medication
            item {
                SectionHeader(title = stringResource(R.string.medication_search_section), icon = Icons.Rounded.Search)
                Spacer(modifier = Modifier.height(10.dp))
                MedSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = onSearchQueryChange,
                    onSearch = {},
                    placeholder = stringResource(R.string.medication_search_hint)
                )

                // Search results dropdown
                AnimatedVisibility(
                    visible = uiState.searchResults.isNotEmpty() || uiState.isSearching
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = ExpressiveShapes.card,   // M3E
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        if (uiState.isSearching) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        } else {
                            Column {
                                uiState.searchResults.take(8).forEach { drug ->
                                    ListItem(
                                        headlineContent = {
                                            Text(
                                                drug.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Medium
                                                )
                                            )
                                        },
                                        supportingContent = {
                                            if (drug.tty.isNotBlank()) {
                                                Text(
                                                    drug.tty,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        leadingContent = {
                                            Icon(
                                                Icons.Rounded.Medication,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        modifier = Modifier.clickable { onDrugSelected(drug) }
                                    )
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                        thickness = 0.5.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section: Medication details
            item {
                SectionHeader(title = stringResource(R.string.medication_details_section), icon = Icons.Rounded.Info)
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.medicationName,
                        onValueChange = onMedicationNameChange,
                        label = { Text(stringResource(R.string.medication_name_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = ExpressiveShapes.surface,  // M3E
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = uiState.strength,
                            onValueChange = onStrengthChange,
                            label = { Text(stringResource(R.string.medication_strength_label)) },
                            modifier = Modifier.weight(1f),
                            shape = ExpressiveShapes.surface,  // M3E
                            singleLine = true,
                            placeholder = { Text(stringResource(R.string.medication_strength_hint)) }
                        )
                        OutlinedTextField(
                            value = uiState.dosageForm,
                            onValueChange = onDosageFormChange,
                            label = { Text(stringResource(R.string.medication_form_label)) },
                            modifier = Modifier.weight(1f),
                            shape = ExpressiveShapes.surface,  // M3E
                            singleLine = true,
                            placeholder = { Text(stringResource(R.string.medication_form_hint)) }
                        )
                    }
                    OutlinedTextField(
                        value = uiState.instructions,
                        onValueChange = onInstructionsChange,
                        label = { Text(stringResource(R.string.medication_notes_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = ExpressiveShapes.surface,  // M3E
                        minLines = 2,
                        maxLines = 3
                    )
                }
            }

            // Section: Color picker
            item {
                SectionHeader(title = stringResource(R.string.medication_color_section), icon = Icons.Rounded.Palette)
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(MedicationColors.palette) { index, color ->
                        val isSelected = index == uiState.selectedColorIndex
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    shape = CircleShape
                                )
                                .clickable { onColorSelected(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Section: Reminder schedule
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(title = stringResource(R.string.medication_schedule_section), icon = Icons.Rounded.Alarm)
                    FilledTonalButton(
                        onClick = {
                            editingScheduleIndex = -1
                            showTimePicker = true
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.medication_add_time), style = MaterialTheme.typography.labelMedium)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (uiState.schedules.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = ExpressiveShapes.surface,  // M3E
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Rounded.Alarm,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.medication_no_times),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.schedules.forEachIndexed { index, schedule ->
                            ScheduleItem(
                                schedule = schedule,
                                onEdit = {
                                    editingScheduleIndex = index
                                    showTimePicker = true
                                },
                                onDelete = { onRemoveSchedule(index) },
                                onDaysChanged = { days -> onUpdateScheduleDays(index, days) }
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun ScheduleItem(
    schedule: Schedule,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDaysChanged: (List<Int>) -> Unit
) {
    val dayLabels = listOf(
        stringResource(R.string.day_mon_short),
        stringResource(R.string.day_tue_short),
        stringResource(R.string.day_wed_short),
        stringResource(R.string.day_thu_short),
        stringResource(R.string.day_fri_short),
        stringResource(R.string.day_sat_short),
        stringResource(R.string.day_sun_short)
    )

    val daysDisplayText = when {
        schedule.isEveryDay -> stringResource(R.string.schedule_every_day)
        schedule.isWeekdaysOnly -> stringResource(R.string.schedule_weekdays)
        schedule.isWeekendsOnly -> stringResource(R.string.schedule_weekends)
        else -> {
            val abbrs = listOf(
                stringResource(R.string.day_mon_abbr),
                stringResource(R.string.day_tue_abbr),
                stringResource(R.string.day_wed_abbr),
                stringResource(R.string.day_thu_abbr),
                stringResource(R.string.day_fri_abbr),
                stringResource(R.string.day_sat_abbr),
                stringResource(R.string.day_sun_abbr)
            )
            schedule.daysOfWeek.sorted().map { abbrs[it - 1] }.joinToString(", ")
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ExpressiveShapes.card,              // M3E
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(ExpressiveShapes.iconContainer) // M3E
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Alarm,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formatTime(schedule.hour, schedule.minute),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${schedule.label} · $daysDisplayText",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Rounded.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Rounded.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Day-of-week selector chips
            DayOfWeekSelector(
                selectedDays = schedule.daysOfWeek,
                dayLabels = dayLabels,
                onDaysChanged = onDaysChanged
            )
        }
    }
}

@Composable
private fun DayOfWeekSelector(
    selectedDays: List<Int>,
    dayLabels: List<String>,
    onDaysChanged: (List<Int>) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        (1..7).forEach { day ->
            val isSelected = day in selectedDays
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable {
                        val newDays = if (isSelected) {
                            // Don't allow deselecting all days
                            if (selectedDays.size > 1) selectedDays - day else selectedDays
                        } else {
                            selectedDays + day
                        }
                        onDaysChanged(newDays.sorted())
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dayLabels[day - 1],
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddMedicationScreenPreview() {
    MedicationReminderTheme {
        AddMedicationScreenContent(
            uiState = AddMedicationUiState(
                medicationName = "Advil",
                strength = "200mg",
                dosageForm = "Tablet",
                schedules = listOf(
                    Schedule(id = 1, medicationId = 0, hour = 8, minute = 0, label = "Morning"),
                    Schedule(id = 2, medicationId = 0, hour = 15, minute = 0, label = "Afternoon", daysOfWeek = listOf(1, 3, 5))
                )
            ),
            onNavigateBack = {},
            onSaveMedication = {},
            onDeleteMedication = {},
            onSearchQueryChange = {},
            onDrugSelected = {},
            onMedicationNameChange = {},
            onStrengthChange = {},
            onDosageFormChange = {},
            onInstructionsChange = {},
            onColorSelected = {},
            onAddSchedule = { _, _, _, _ -> },
            onUpdateSchedule = { _, _, _, _, _ -> },
            onRemoveSchedule = {},
            onUpdateScheduleDays = { _, _ -> },
            onClearError = {}
        )
    }
}
