package com.medtrackpro.demo.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.medtrackpro.demo.data.repository.ClinicianInsightStats
import com.medtrackpro.demo.data.repository.ClinicianRepository
import com.medtrackpro.demo.data.repository.MedicationRepository
import com.medtrackpro.demo.data.repository.PatientRepository
import com.medtrackpro.demo.data.repository.SymptomRepository
import com.medtrackpro.demo.ui.state.ClinicianStatsUiState
import com.medtrackpro.demo.ui.state.ClinicianUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClinicianViewModel(context: Context) : ViewModel() {
    private val appContext = context.applicationContext
    private val patientRepository = PatientRepository(appContext)
    private val medicationRepository = MedicationRepository(appContext)
    private val symptomRepository = SymptomRepository(appContext)
    private val clinicianRepository = ClinicianRepository()

    private val _uiState = MutableStateFlow(ClinicianUiState())
    val uiState: StateFlow<ClinicianUiState> = _uiState.asStateFlow()

    private val _statsUiState = MutableStateFlow(ClinicianStatsUiState())
    val statsUiState: StateFlow<ClinicianStatsUiState> = _statsUiState.asStateFlow()

    fun updateAccessKey(accessKey: String) {
        _uiState.value = _uiState.value.copy(
            accessKey = accessKey,
            loginError = null
        )
    }

    fun validateAccessKey(): Boolean {
        val trimmedAccessKey = _uiState.value.accessKey.trim()

        return if (trimmedAccessKey == DEMO_ACCESS_CODE) {
            _uiState.value = _uiState.value.copy(loginError = null)
            true
        } else {
            _uiState.value = _uiState.value.copy(loginError = "Invalid access key")
            false
        }
    }

    fun loadStats() {
        viewModelScope.launch {
            _statsUiState.value = _statsUiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val totalPatients = patientRepository.getPatientCount()
                val totalMedications = medicationRepository.getMedicationCount()
                val averageMedicationsPerPatient = if (totalPatients == 0) {
                    0.0
                } else {
                    totalMedications.toDouble() / totalPatients.toDouble()
                }
                val mostCommonSymptom = symptomRepository.getMostCommonSymptomCategory()
                val averageSymptomSeverity = symptomRepository.getAverageSymptomSeverity()

                _statsUiState.value = ClinicianStatsUiState(
                    isLoading = false,
                    errorMessage = null,
                    totalPatients = totalPatients,
                    totalMedications = totalMedications,
                    averageMedicationsPerPatient = averageMedicationsPerPatient,
                    mostCommonSymptomCategory = mostCommonSymptom?.category ?: "Not available",
                    mostCommonSymptomCount = mostCommonSymptom?.count ?: 0,
                    averageSymptomSeverity = averageSymptomSeverity
                )
            } catch (_: Exception) {
                _statsUiState.value = _statsUiState.value.copy(
                    isLoading = false,
                    errorMessage = "Unable to load dashboard statistics."
                )
            }
        }
    }

    fun findPatterns() {
        val currentStats = _statsUiState.value

        viewModelScope.launch {
            _statsUiState.value = currentStats.copy(
                isInsightsLoading = true,
                insightsError = null
            )

            try {
                val insights = clinicianRepository.generateInsights(
                    ClinicianInsightStats(
                        totalPatients = currentStats.totalPatients,
                        totalMedications = currentStats.totalMedications,
                        averageMedicationsPerPatient = currentStats.averageMedicationsPerPatient,
                        mostCommonSymptomCategory = currentStats.mostCommonSymptomCategory,
                        mostCommonSymptomCount = currentStats.mostCommonSymptomCount,
                        averageSymptomSeverity = currentStats.averageSymptomSeverity
                    )
                )

                _statsUiState.value = if (insights == null || insights.size < 3) {
                    _statsUiState.value.copy(
                        isInsightsLoading = false,
                        insightsError = "Unable to generate insights. Try again later."
                    )
                } else {
                    _statsUiState.value.copy(
                        isInsightsLoading = false,
                        insightsError = null,
                        insights = insights.take(3)
                    )
                }
            } catch (exception: Exception) {
                _statsUiState.value = _statsUiState.value.copy(
                    isInsightsLoading = false,
                    insightsError = exception.message ?: "Unable to generate insights. Try again later."
                )
            }
        }
    }

    class ClinicianViewModelFactory(context: Context) : ViewModelProvider.Factory {
        private val appContext = context.applicationContext

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ClinicianViewModel(appContext) as T
        }
    }

    companion object {
        private const val DEMO_ACCESS_CODE = "medtrack-demo"
    }
}
