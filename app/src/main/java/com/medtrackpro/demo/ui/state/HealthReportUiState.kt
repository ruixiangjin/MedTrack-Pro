package com.medtrackpro.demo.ui.state

data class HealthReportUiState(
    val isReportLoading: Boolean = false,
    val reportText: String? = null,
    val reportError: String? = null
)
