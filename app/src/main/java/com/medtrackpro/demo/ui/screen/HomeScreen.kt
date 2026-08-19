package com.medtrackpro.demo.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medtrackpro.demo.data.local.MedicationEntity
import com.medtrackpro.demo.viewmodel.MedicationViewModel
import com.medtrackpro.demo.viewmodel.PatientViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    innerPadding: PaddingValues,
    patientId: String?,
    onAddMedicationClick: () -> Unit
) {
    val context = LocalContext.current
    val patientViewModel: PatientViewModel = viewModel(
        key = "home-patient-$patientId",
        factory = PatientViewModel.PatientViewModelFactory(context, patientId)
    )
    val medicationViewModel: MedicationViewModel = viewModel(
        key = "home-medication-$patientId",
        factory = MedicationViewModel.MedicationViewModelFactory(context, patientId)
    )
    val currentPatient by patientViewModel.currentPatient.collectAsState(initial = null)
    val medications by medicationViewModel.medications.collectAsState(initial = emptyList())
    val takenStatuses by medicationViewModel.takenStatuses.collectAsState(initial = emptyList())
    val colorScheme = MaterialTheme.colorScheme
    val patientName = currentPatient?.name ?: "Patient"

    val medicationIds = remember(medications) {
        medications.map { it.id }.toSet()
    }
    val takenStatusMap = remember(takenStatuses) {
        takenStatuses.associateBy { it.medicationId }
    }
    val takenCount = takenStatuses.count { status ->
        status.isTaken && status.medicationId in medicationIds
    }

    // Header date
    val todayDate = remember {
        LocalDate.now().format(
            DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH)
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        color = colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp),
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
                                    text = "Hello, $patientName",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = colorScheme.onSecondary
                                )

                                Text(
                                    text = todayDate,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = "Review today's medications and keep your health records up to date.",
                                    style = MaterialTheme.typography.bodyMedium,
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
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Today's Overview",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = colorScheme.onSurface
                                )

                                Text(
                                    text = "$takenCount of ${medications.size} medications taken today",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Button(
                            onClick = onAddMedicationClick,
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
                        ) {
                            Text("Add Medication")
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Today's Medications",
                    style = MaterialTheme.typography.titleLarge,
                    color = colorScheme.onBackground
                )
            }

            if (medications.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
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
                                text = "No medications scheduled.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Once medications are available, they will appear here for today.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(medications) { _, medication ->
                    val isTaken = takenStatusMap[medication.id]?.isTaken == true

                    MedicationCard(
                        medication = medication,
                        isTaken = isTaken,
                        onTakenChange = { isChecked ->
                            medicationViewModel.setTaken(
                                medicationId = medication.id,
                                isTaken = isChecked
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MedicationCard(
    medication: MedicationEntity,
    isTaken: Boolean,
    onTakenChange: (Boolean) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val takenAccentColor = Color(0xFF2F9E5C)
    val takenContainerColor = Color(0xFFEAF8EF)
    val takenDetailColor = Color(0xFFD8F1E2)
    val cardContainerColor = if (isTaken) {
        takenContainerColor
    } else {
        colorScheme.surface
    }
    val primaryTextColor = if (isTaken) {
        Color(0xFF1F5D39)
    } else {
        colorScheme.onSurface
    }
    val secondaryTextColor = colorScheme.onSurfaceVariant
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainerColor),
        border = if (isTaken) {
            BorderStroke(2.dp, takenAccentColor)
        } else {
            null
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = medication.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryTextColor,
                        textDecoration = if (isTaken) TextDecoration.LineThrough else TextDecoration.None
                    )
                }
                Card(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isTaken) takenAccentColor else colorScheme.secondary
                    )
                ) {
                    Text(
                        text = if (isTaken) "Taken" else medication.dosage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isTaken) colorScheme.onPrimary else colorScheme.onSecondary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isTaken) takenDetailColor else colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "Frequency",
                            style = MaterialTheme.typography.labelMedium,
                            color = secondaryTextColor
                        )

                        Text(
                            text = medication.frequency,
                            style = MaterialTheme.typography.bodyMedium,
                            color = primaryTextColor,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "Schedule",
                            style = MaterialTheme.typography.labelMedium,
                            color = secondaryTextColor
                        )

                        Text(
                            text = medication.scheduledTime,
                            style = MaterialTheme.typography.bodyMedium,
                            color = primaryTextColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Taken",
                        style = MaterialTheme.typography.bodyLarge,
                        color = primaryTextColor
                    )

                    Text(
                        text = if (isTaken) "Marked as taken" else "Not marked yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isTaken) takenAccentColor else secondaryTextColor,
                        fontWeight = if (isTaken) FontWeight.SemiBold else FontWeight.Normal
                    )
                }

                Switch(
                    checked = isTaken,
                    onCheckedChange = onTakenChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = takenAccentColor,
                        checkedBorderColor = takenAccentColor,
                        uncheckedThumbColor = colorScheme.outline,
                        uncheckedTrackColor = colorScheme.surfaceVariant,
                        uncheckedBorderColor = colorScheme.outline
                    )
                )
            }
        }
    }
}
