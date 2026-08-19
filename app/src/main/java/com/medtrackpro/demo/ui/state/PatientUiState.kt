package com.medtrackpro.demo.ui.state

data class PatientUiState(
    val isLoading: Boolean = false,
    val loginError: String? = null,
    val claimError: String? = null,
    val claimMessage: String? = null,
    val signUpError: String? = null,
    val loginSucceeded: Boolean = false,
    val loggedInPatientId: String? = null,
    val createdPatientId: String? = null
)
