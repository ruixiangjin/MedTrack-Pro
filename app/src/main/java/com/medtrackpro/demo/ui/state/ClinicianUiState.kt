package com.medtrackpro.demo.ui.state

data class ClinicianUiState(
    val accessKey: String = "",
    val loginError: String? = null
)

data class ClinicianStatsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val totalPatients: Int = 0,
    val totalMedications: Int = 0,
    val averageMedicationsPerPatient: Double = 0.0,
    val mostCommonSymptomCategory: String = "Not available",
    val mostCommonSymptomCount: Int = 0,
    val averageSymptomSeverity: Double? = null,
    val isInsightsLoading: Boolean = false,
    val insightsError: String? = null,
    val insights: List<String> = emptyList()
)
