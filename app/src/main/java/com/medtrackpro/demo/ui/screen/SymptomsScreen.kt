package com.medtrackpro.demo.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medtrackpro.demo.data.local.SymptomEntity
import com.medtrackpro.demo.viewmodel.SymptomViewModel

@Composable
fun SymptomsScreen(
    innerPadding: PaddingValues,
    patientId: String?,
    messageHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val symptomViewModel: SymptomViewModel = viewModel(
        key = "symptoms-$patientId",
        factory = SymptomViewModel.SymptomViewModelFactory(context, patientId)
    )
    val symptoms by symptomViewModel.symptoms.collectAsState(initial = emptyList())
    val symptomUiState by symptomViewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    val currentSeverityIndicator = getSeverityIndicator(symptomUiState.severityValue)

    LaunchedEffect(symptomUiState.symptomSaved) {
        if (symptomUiState.symptomSaved) {
            symptomViewModel.clearSymptomResult()
            symptomViewModel.clearSymptomForm()
            messageHostState.showSnackbar("Symptom saved")
        }
    }

    if (symptomUiState.showDatePicker) {
        SymptomDatePickerDialog(
            initialDateMillis = symptomUiState.datePickerInitialMillis,
            onDismiss = symptomViewModel::dismissDatePicker,
            onConfirm = symptomViewModel::confirmDate
        )
    }

    if (symptomUiState.showTimePicker) {
        SymptomTimePickerDialog(
            initialHour = symptomUiState.timePickerInitialHour,
            initialMinute = symptomUiState.timePickerInitialMinute,
            onDismiss = symptomViewModel::dismissTimePicker,
            onConfirm = symptomViewModel::confirmTime
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        color = colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        colorScheme.secondary,
                                        colorScheme.secondaryContainer
                                    )
                                )
                            )
                            .padding(horizontal = 24.dp, vertical = 28.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            HeroIcon(
                                backgroundColor = colorScheme.primaryContainer
                            )
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Symptoms",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = colorScheme.onSecondary
                                )

                                Text(
                                    text = "Log symptoms and review previous records.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Log Symptom",
                            style = MaterialTheme.typography.titleLarge,
                            color = colorScheme.onSurface
                        )

                        SymptomDropdown(
                            label = "Symptom Category",
                            selectedValue = symptomUiState.selectedCategory,
                            placeholder = "Select a category",
                            options = symptomViewModel.symptomCategories,
                            onSelected = symptomViewModel::onCategorySelected
                        )
                        InlineErrorText(symptomUiState.categoryError)

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (symptomUiState.isSeveritySelected) {
                                        "Severity: ${symptomUiState.severityValue}"
                                    } else {
                                        "Select severity from 1 to 10"
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = colorScheme.onSurface
                                )

                                if (symptomUiState.isSeveritySelected) {
                                    Card(
                                        shape = RoundedCornerShape(18.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = currentSeverityIndicator.backgroundColor
                                        )
                                    ) {
                                        Text(
                                            text = currentSeverityIndicator.label,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = currentSeverityIndicator.contentColor,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }

                            Slider(
                                value = symptomUiState.severity,
                                onValueChange = symptomViewModel::onSeverityChanged,
                                valueRange = 1f..10f,
                                steps = 8,
                                colors = SliderDefaults.colors(
                                    thumbColor = if (symptomUiState.isSeveritySelected) {
                                        currentSeverityIndicator.accentColor
                                    } else {
                                        colorScheme.outline
                                    },
                                    activeTrackColor = if (symptomUiState.isSeveritySelected) {
                                        currentSeverityIndicator.accentColor
                                    } else {
                                        colorScheme.outline
                                    },
                                    inactiveTrackColor = if (symptomUiState.isSeveritySelected) {
                                        currentSeverityIndicator.backgroundColor
                                    } else {
                                        colorScheme.surfaceVariant
                                    }
                                )
                            )
                            InlineErrorText(symptomUiState.severityError)
                        }

                        OutlinedTextField(
                            value = symptomUiState.notes,
                            onValueChange = symptomViewModel::onNotesChanged,
                            label = {
                                Text("Additional Notes")
                            },
                            isError = symptomUiState.notesError != null,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colorScheme.surface,
                                unfocusedContainerColor = colorScheme.surface,
                                focusedBorderColor = colorScheme.primary,
                                unfocusedBorderColor = colorScheme.outline
                            )
                        )
                        Text(
                            text = "${symptomUiState.notes.length}/200",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (symptomUiState.notesError == null) {
                                colorScheme.onSurfaceVariant
                            } else {
                                colorScheme.error
                            },
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        InlineErrorText(symptomUiState.notesError)
                        InlineErrorText(symptomUiState.symptomError)

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Date and Time",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = symptomViewModel::showDatePicker,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.surfaceVariant)
                                ) {
                                    Text(
                                        text = symptomUiState.selectedDate.ifBlank { "Select Date" },
                                        color = colorScheme.onSurface
                                    )
                                }

                                Button(
                                    onClick = symptomViewModel::showTimePicker,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.surfaceVariant)
                                ) {
                                    Text(
                                        text = symptomUiState.selectedTime.ifBlank { "Select Time" },
                                        color = colorScheme.onSurface
                                    )
                                }
                            }
                        }
                        InlineErrorText(symptomUiState.dateTimeError)
                        Button(
                            onClick = symptomViewModel::saveSymptom,
                            enabled = !symptomUiState.isSavingSymptom,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
                        ) {
                            Text(if (symptomUiState.isSavingSymptom) "Saving..." else "Save Symptom")
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Symptom History",
                    style = MaterialTheme.typography.titleLarge,
                    color = colorScheme.onBackground
                )
            }
            if (symptoms.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "No symptoms logged yet.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(symptoms) { symptom ->
                    SymptomHistoryCard(symptom = symptom)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SymptomDatePickerDialog(
    initialDateMillis: Long?,
    onDismiss: () -> Unit,
    onConfirm: (Long?) -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(datePickerState.selectedDateMillis)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SymptomTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Select Time")
        },
        text = {
            TimePicker(state = timePickerState)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(timePickerState.hour, timePickerState.minute)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SymptomHistoryCard(symptom: SymptomEntity) {
    val colorScheme = MaterialTheme.colorScheme
    val severityIndicator = getSeverityIndicator(symptom.severity)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(86.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(severityIndicator.barColor)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = symptom.category,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurface
                        )

                        Text(
                            text = symptom.dateTime,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                    SeverityBadge(
                        severity = symptom.severity,
                        severityIndicator = severityIndicator
                    )
                }
                Text(
                    text = symptom.notes.ifBlank { "No notes provided." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SeverityBadge(
    severity: Int,
    severityIndicator: SeverityIndicator
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = severityIndicator.backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = "${severityIndicator.label} ($severity)",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = severityIndicator.contentColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun SymptomDropdown(
    label: String,
    selectedValue: String,
    placeholder: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    expanded = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.surfaceVariant)
            ) {
                Text(
                    text = selectedValue.ifBlank { placeholder },
                    color = colorScheme.onSurface
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(option)
                        },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

data class SeverityIndicator(
    val label: String,
    val backgroundColor: Color,
    val contentColor: Color,
    val accentColor: Color,
    val barColor: Color
)

fun getSeverityIndicator(severity: Int): SeverityIndicator {
    return when (severity) {
        in 1..3 -> SeverityIndicator(
            label = "Mild",
            backgroundColor = Color(0xFFDCEFE4),
            contentColor = Color(0xFF28623D),
            accentColor = Color(0xFF44B978),
            barColor = Color(0xFFA9DCC0)
        )

        in 4..6 -> SeverityIndicator(
            label = "Moderate",
            backgroundColor = Color(0xFFFFF1C7),
            contentColor = Color(0xFF6B4A00),
            accentColor = Color(0xFFFFC233),
            barColor = Color(0xFFFFC233)
        )

        else -> SeverityIndicator(
            label = "Severe",
            backgroundColor = Color(0xFFFFE1E1),
            contentColor = Color(0xFF8B2E2E),
            accentColor = Color(0xFFFF6B6B),
            barColor = Color(0xFFFF6B6B)
        )
    }
}
