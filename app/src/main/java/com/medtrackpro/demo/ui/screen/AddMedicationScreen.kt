package com.medtrackpro.demo.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medtrackpro.demo.viewmodel.MedicationViewModel

@Composable
fun AddMedicationScreen(
    innerPadding: PaddingValues,
    patientId: String?,
    onMedicationSaved: () -> Unit
) {
    val context = LocalContext.current
    val medicationViewModel: MedicationViewModel = viewModel(
        key = "add-medication-$patientId",
        factory = MedicationViewModel.MedicationViewModelFactory(context, patientId)
    )
    val medicationUiState by medicationViewModel.uiState.collectAsState()

    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(medicationUiState.medicationSaved) {
        if (medicationUiState.medicationSaved) {
            medicationViewModel.clearMedicationResult()
            medicationViewModel.clearMedicationForm()
            onMedicationSaved()
        }
    }

    if (medicationUiState.showScheduledTimePicker) {
        AddMedicationTimePickerDialog(
            initialHour = medicationUiState.scheduledTimePickerHour,
            initialMinute = medicationUiState.scheduledTimePickerMinute,
            onDismiss = medicationViewModel::dismissScheduledTimePicker,
            onConfirm = medicationViewModel::confirmScheduledTime
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        color = colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
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
                                text = "Add Medication",
                                style = MaterialTheme.typography.headlineMedium,
                                color = colorScheme.onSecondary
                            )
                            Text(
                                text = "Record a medication schedule for the day.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
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
                        text = "Medication Details",
                        style = MaterialTheme.typography.titleLarge,
                        color = colorScheme.onSurface
                    )
                    OutlinedTextField(
                        value = medicationUiState.medicationName,
                        onValueChange = medicationViewModel::onMedicationNameChange,
                        label = {
                            Text("Medication Name")
                        },
                        singleLine = true,
                        isError = medicationUiState.medicationNameError != null,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colorScheme.surface,
                            unfocusedContainerColor = colorScheme.surface,
                            focusedBorderColor = colorScheme.primary,
                            unfocusedBorderColor = colorScheme.outline
                        )
                    )
                    InlineErrorText(medicationUiState.medicationNameError)
                    OutlinedTextField(
                        value = medicationUiState.dosage,
                        onValueChange = medicationViewModel::onDosageChange,
                        label = {
                            Text("Dosage")
                        },
                        singleLine = true,
                        isError = medicationUiState.dosageError != null,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colorScheme.surface,
                            unfocusedContainerColor = colorScheme.surface,
                            focusedBorderColor = colorScheme.primary,
                            unfocusedBorderColor = colorScheme.outline
                        )
                    )
                    InlineErrorText(medicationUiState.dosageError)
                    MedicationDropdown(
                        label = "Frequency",
                        selectedValue = medicationUiState.frequency,
                        placeholder = "Select Frequency",
                        options = medicationViewModel.frequencyOptions,
                        onSelected = medicationViewModel::onFrequencySelected
                    )
                    InlineErrorText(medicationUiState.frequencyError)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Scheduled Time",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        Button(
                            onClick = medicationViewModel::showScheduledTimePicker,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.surfaceVariant)
                        ) {
                            Text(
                                text = medicationUiState.scheduledTime.ifBlank { "Select Time" },
                                color = colorScheme.onSurface
                            )
                        }
                    }
                    InlineErrorText(medicationUiState.scheduledTimeError)
                    MedicationDropdown(
                        label = "Medication Type",
                        selectedValue = medicationUiState.medicationType,
                        placeholder = "Select Medication Type",
                        options = medicationViewModel.medicationTypeOptions,
                        onSelected = medicationViewModel::onMedicationTypeSelected
                    )
                    InlineErrorText(medicationUiState.medicationTypeError)
                    InlineErrorText(medicationUiState.addMedicationError)

                    OutlinedTextField(
                        value = medicationUiState.notes,
                        onValueChange = medicationViewModel::onNotesChange,
                        label = {
                            Text("Notes")
                        },
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = medicationViewModel::saveMedication,
                            enabled = !medicationUiState.isSavingMedication,
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
                        ) {
                            Text(if (medicationUiState.isSavingMedication) "Saving..." else "Save")
                        }
                        Button(
                            onClick = medicationViewModel::clearMedicationForm,
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary)
                        ) {
                            Text(
                                text = "Clear",
                                color = colorScheme.onSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMedicationTimePickerDialog(
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
fun MedicationDropdown(
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
