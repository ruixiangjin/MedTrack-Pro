package com.medtrackpro.demo.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medtrackpro.demo.viewmodel.ClinicianViewModel
import java.util.Locale

@Composable
fun ClinicianDashboardScreen(
    innerPadding: PaddingValues,
    onBackToSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val clinicianViewModel: ClinicianViewModel = viewModel(
        factory = ClinicianViewModel.ClinicianViewModelFactory(context)
    )
    val statsUiState by clinicianViewModel.statsUiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(Unit) {
        clinicianViewModel.loadStats()
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
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        HeroIcon(backgroundColor = colorScheme.primaryContainer)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Clinician Dashboard",
                                style = MaterialTheme.typography.headlineMedium,
                                color = colorScheme.onSecondary
                            )
                            Text(
                                text = "Review aggregate statistics across all patients.",
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Dashboard Stats",
                        style = MaterialTheme.typography.titleLarge,
                        color = colorScheme.onSurface
                    )

                    if (statsUiState.isLoading) {
                        CircularProgressIndicator()
                    }

                    statsUiState.errorMessage?.let { error ->
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.error
                        )
                    }

                    ClinicianStatRow(
                        label = "Total Patients",
                        value = statsUiState.totalPatients.toString()
                    )
                    ClinicianStatRow(
                        label = "Average Medications per Patient",
                        value = String.format(
                            Locale.getDefault(),
                            "%.1f",
                            statsUiState.averageMedicationsPerPatient
                        )
                    )
                    ClinicianStatRow(
                        label = "Most Common Symptom",
                        value = if (statsUiState.mostCommonSymptomCount == 0) {
                            "Not available"
                        } else {
                            "${statsUiState.mostCommonSymptomCategory} (${statsUiState.mostCommonSymptomCount} records)"
                        }
                    )
                    ClinicianStatRow(
                        label = "Average Symptom Severity",
                        value = statsUiState.averageSymptomSeverity?.let { severity ->
                            String.format(Locale.getDefault(), "%.1f / 10", severity)
                        } ?: "Not available"
                    )
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
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Patterns",
                        style = MaterialTheme.typography.titleLarge,
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = "Generate three observations from the aggregate dashboard data.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = clinicianViewModel::findPatterns,
                        enabled = !statsUiState.isLoading && !statsUiState.isInsightsLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
                    ) {
                        Text(
                            text = if (statsUiState.isInsightsLoading) {
                                "Finding Patterns..."
                            } else {
                                "Find Patterns"
                            }
                        )
                    }

                    if (statsUiState.isInsightsLoading) {
                        CircularProgressIndicator()
                    }

                    statsUiState.insightsError?.let { error ->
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.error
                        )
                    }

                    if (statsUiState.insights.isNotEmpty()) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            statsUiState.insights.forEachIndexed { index, insight ->
                                ClinicianInsightRow(
                                    number = index + 1,
                                    insight = insight
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = onBackToSettingsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary)
            ) {
                Text(
                    text = "Back to Settings",
                    color = colorScheme.onSecondary
                )
            }
        }
    }
}

@Composable
private fun ClinicianInsightRow(
    number: Int,
    insight: String
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Insight $number",
                style = MaterialTheme.typography.labelLarge,
                color = colorScheme.onSurfaceVariant
            )
            Text(
                text = insight,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ClinicianStatRow(
    label: String,
    value: String
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onSurface
        )
    }
}
