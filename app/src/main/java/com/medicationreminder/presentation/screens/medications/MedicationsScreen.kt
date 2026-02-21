package com.medicationreminder.presentation.screens.medications

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
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
import com.medicationreminder.domain.model.Medication
import com.medicationreminder.presentation.components.MedSearchBar
import com.medicationreminder.presentation.components.MedicationCard
import com.medicationreminder.presentation.theme.MedicationReminderTheme

@Composable
fun MedicationsScreen(
    onNavigateToAddMedication: () -> Unit,
    onNavigateToMedicationDetail: (Long) -> Unit,
    viewModel: MedicationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filtered by viewModel.filteredMedications.collectAsStateWithLifecycle()
    
    MedicationsScreenContent(
        uiState = uiState,
        filteredMedications = filtered,
        onNavigateToAddMedication = onNavigateToAddMedication,
        onNavigateToMedicationDetail = onNavigateToMedicationDetail,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onClearDeletedMedication = viewModel::clearDeletedMedication
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MedicationsScreenContent(
    uiState: MedicationsUiState,
    filteredMedications: List<Medication>,
    onNavigateToAddMedication: () -> Unit,
    onNavigateToMedicationDetail: (Long) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onClearDeletedMedication: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Background color animation
    var startColorAnimation by remember { mutableStateOf(false) }
    val animatedBgColor by animateColorAsState(
        targetValue = if (startColorAnimation) MaterialTheme.colorScheme.background 
                      else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
        animationSpec = tween(durationMillis = 1000),
        label = "backgroundColorAnimation"
    )

    LaunchedEffect(Unit) {
        startColorAnimation = true
    }

    // Show snackbar when medication deleted
    val deletedMessage = stringResource(R.string.medications_deleted, uiState.deletedMedication?.name ?: "")
    LaunchedEffect(uiState.deletedMedication) {
        uiState.deletedMedication?.let { _ ->
            snackbarHostState.showSnackbar(
                message = deletedMessage,
                duration = SnackbarDuration.Short
            )
            onClearDeletedMedication()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(animatedBgColor)
                .padding(innerPadding)
        ) {
            // Top bar with integrated Add Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.medications_title),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (filteredMedications.size == 1) stringResource(R.string.medications_count_one)
                                   else stringResource(R.string.medications_count_plural, filteredMedications.size),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    FilledIconButton(
                        onClick = onNavigateToAddMedication,
                        shape = RoundedCornerShape(14.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.medications_add))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                MedSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = onSearchQueryChange,
                    onSearch = {},
                    placeholder = stringResource(R.string.medications_search_placeholder)
                )
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (filteredMedications.isEmpty()) {
                EmptyMedicationsList(
                    hasSearchQuery = uiState.searchQuery.isNotBlank(),
                    onAddClick = onNavigateToAddMedication
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 4.dp,
                        bottom = 120.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredMedications, key = { it.id }) { medication ->
                        MedicationCard(
                            medication = medication,
                            onClick = { onNavigateToMedicationDetail(medication.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyMedicationsList(
    hasSearchQuery: Boolean,
    onAddClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (hasSearchQuery) "🔍" else "💊",
                style = MaterialTheme.typography.displaySmall
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (hasSearchQuery) stringResource(R.string.medications_no_results_title)
                       else stringResource(R.string.medications_empty_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (hasSearchQuery) stringResource(R.string.medications_no_results_desc)
                       else stringResource(R.string.medications_empty_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!hasSearchQuery) {
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = onAddClick) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.medications_add))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MedicationsScreenPreview() {
    MedicationReminderTheme {
        MedicationsScreenContent(
            uiState = MedicationsUiState(),
            filteredMedications = listOf(
                Medication(id = 1, name = "Ibuprofen", strength = "400mg", instructions = "Take after meal", color = 0),
                Medication(id = 2, name = "Paracetamol", strength = "500mg", instructions = "Take every 8 hours", color = 2)
            ),
            onNavigateToAddMedication = {},
            onNavigateToMedicationDetail = {},
            onSearchQueryChange = {},
            onClearDeletedMedication = {}
        )
    }
}
