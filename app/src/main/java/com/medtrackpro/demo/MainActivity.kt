package com.medtrackpro.demo

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.medtrackpro.demo.data.local.DatabaseSeeder
import com.medtrackpro.demo.data.repository.PatientRepository
import com.medtrackpro.demo.ui.navigation.MedTrackNavHost
import com.medtrackpro.demo.ui.theme.MedTrackTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val isDatabaseReady = mutableStateOf(false)
        val loggedInPatientId = mutableStateOf<String?>(null)

        setContent {
            MedTrackTheme {
                if (isDatabaseReady.value) {
                    MedTrackNavHost(loggedInPatientId = loggedInPatientId.value)
                } else {
                    DatabaseLoadingScreen()
                }
            }
        }

        lifecycleScope.launch {
            DatabaseSeeder.seedIfNeeded(applicationContext)

            val sharedPreferences = getSharedPreferences("medtrack_prefs", Context.MODE_PRIVATE)
            val savedPatientId = sharedPreferences.getString("logged_in_patient_id", null)
            val patientRepository = PatientRepository(applicationContext)
            val validPatientId = savedPatientId?.takeIf { patientId ->
                patientRepository.getPatientById(patientId) != null
            }

            if (savedPatientId != null && validPatientId == null) {
                sharedPreferences
                    .edit()
                    .remove("logged_in_patient_id")
                    .apply()
            }

            loggedInPatientId.value = validPatientId
            isDatabaseReady.value = true
        }
    }
}

@Composable
private fun DatabaseLoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
