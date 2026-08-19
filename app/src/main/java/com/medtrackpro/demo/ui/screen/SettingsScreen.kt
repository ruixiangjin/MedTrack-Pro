package com.medtrackpro.demo.ui.screen

import android.content.Intent
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medtrackpro.demo.viewmodel.HealthReportViewModel
import com.medtrackpro.demo.viewmodel.PatientViewModel

@Composable
fun SettingsScreen(
    innerPadding: PaddingValues,
    patientId: String?,
    onLogoutClick: () -> Unit,
    onClinicianLoginClick: () -> Unit
) {
    val context = LocalContext.current
    val patientViewModel: PatientViewModel = viewModel(
        key = "settings-$patientId",
        factory = PatientViewModel.PatientViewModelFactory(context, patientId)
    )
    val healthReportViewModel: HealthReportViewModel = viewModel(
        key = "health-report-$patientId",
        factory = HealthReportViewModel.HealthReportViewModelFactory(context, patientId)
    )
    val currentPatient by patientViewModel.currentPatient.collectAsState(initial = null)
    val healthReportUiState by healthReportViewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(healthReportUiState.reportText) {
        val reportText = healthReportUiState.reportText
        if (reportText != null) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "MedTrack Health Report")
                putExtra(Intent.EXTRA_TEXT, reportText)
            }

            context.startActivity(
                Intent.createChooser(shareIntent, "Share Health Report")
            )
            healthReportViewModel.clearReportText()
        }
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
                                text = "Settings",
                                style = MaterialTheme.typography.headlineMedium,
                                color = colorScheme.onSecondary
                            )
                            Text(
                                text = "Review your account details and app access.",
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
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Account",
                        style = MaterialTheme.typography.titleLarge,
                        color = colorScheme.onSurface
                    )
                    SettingsInfoRow(label = "Name", value = currentPatient?.name ?: "Loading")
                    SettingsInfoRow(label = "Phone", value = currentPatient?.phoneNumber ?: "Loading")
                    SettingsInfoRow(label = "Patient ID", value = currentPatient?.patientId ?: "Loading")
                }
            }

            Button(
                onClick = {
                    patientViewModel.logout()
                    onLogoutClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
            ) {
                Text("Logout")
            }

            Button(
                onClick = healthReportViewModel::generateHealthReport,
                enabled = !healthReportUiState.isReportLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary)
            ) {
                if (healthReportUiState.isReportLoading) {
                    Text(
                        text = "Preparing report...",
                        color = colorScheme.onSecondary
                    )
                } else {
                    Text(
                        text = "Share Health Report",
                        color = colorScheme.onSecondary
                    )
                }
            }

            InlineErrorText(healthReportUiState.reportError)

            Button(
                onClick = onClinicianLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary)
            ) {
                Text(
                    text = "Clinician Login",
                    color = colorScheme.onSecondary
                )
            }
        }
    }
}

@Composable
private fun SettingsInfoRow(
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
            color = colorScheme.onSurface
        )
    }
}
