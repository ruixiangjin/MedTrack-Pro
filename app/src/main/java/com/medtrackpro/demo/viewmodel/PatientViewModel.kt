package com.medtrackpro.demo.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.medtrackpro.demo.data.local.PatientEntity
import com.medtrackpro.demo.data.repository.PatientRepository
import com.medtrackpro.demo.ui.state.PatientUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class PatientViewModel(
    context: Context,
    private val currentPatientId: String? = null
) : ViewModel() {
    private val appContext = context.applicationContext
    private val patientRepository = PatientRepository(appContext)

    val currentPatient: Flow<PatientEntity?> = if (currentPatientId == null) {
        flowOf(null)
    } else {
        patientRepository.getPatientByIdFlow(currentPatientId)
    }

    private val _uiState = MutableStateFlow(PatientUiState())
    val uiState: StateFlow<PatientUiState> = _uiState.asStateFlow()

    fun login(patientId: String, password: String) {
        val trimmedPatientId = patientId.trim()
        val trimmedPassword = password.trim()

        if (trimmedPatientId.isBlank() || trimmedPassword.isBlank()) {
            _uiState.value = _uiState.value.copy(
                loginError = "Patient ID and password cannot be empty",
                loginSucceeded = false
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, loginError = null)

            val patient = patientRepository.getPatientById(trimmedPatientId)

            when {
                patient == null -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        loginError = "No account found with this Patient ID"
                    )
                }

                patient.password.isNullOrBlank() -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        loginError = "This account has not been claimed yet"
                    )
                }

                patient.password != trimmedPassword -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        loginError = "Incorrect password"
                    )
                }

                else -> {
                    appContext
                        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        .edit()
                        .putString(LOGGED_IN_PATIENT_ID_KEY, patient.patientId)
                        .apply()

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        loginError = null,
                        loginSucceeded = true,
                        loggedInPatientId = patient.patientId
                    )
                }
            }
        }
    }

    fun claimAccount(
        patientId: String,
        phoneNumber: String,
        newPassword: String,
        confirmPassword: String
    ) {
        val trimmedPatientId = patientId.trim()
        val trimmedPhoneNumber = phoneNumber.trim()
        val trimmedNewPassword = newPassword.trim()
        val trimmedConfirmPassword = confirmPassword.trim()
        val passwordError = validatePassword(trimmedNewPassword)
        val confirmPasswordError = validateConfirmPassword(trimmedNewPassword, trimmedConfirmPassword)

        when {
            trimmedPatientId.isBlank() -> {
                _uiState.value = _uiState.value.copy(claimError = "Patient ID is required")
                return
            }

            trimmedPhoneNumber.isBlank() -> {
                _uiState.value = _uiState.value.copy(claimError = "Phone number is required")
                return
            }

            passwordError != null -> {
                _uiState.value = _uiState.value.copy(claimError = passwordError)
                return
            }

            confirmPasswordError != null -> {
                _uiState.value = _uiState.value.copy(claimError = confirmPasswordError)
                return
            }
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                claimError = null,
                claimMessage = null
            )

            val patient = patientRepository.getPatientById(trimmedPatientId)

            when {
                patient == null -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        claimError = "No seeded account found with this Patient ID"
                    )
                }

                patient.phoneNumber != trimmedPhoneNumber -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        claimError = "Patient ID and phone number do not match"
                    )
                }

                !patient.password.isNullOrBlank() -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        claimError = "This account has already been claimed"
                    )
                }

                else -> {
                    patientRepository.updatePatientPassword(trimmedPatientId, trimmedNewPassword)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        claimMessage = "Account claimed. Please login."
                    )
                }
            }
        }
    }

    fun signUp(
        fullName: String,
        phoneNumber: String,
        password: String,
        confirmPassword: String
    ) {
        val trimmedFullName = fullName.trim()
        val trimmedPhoneNumber = phoneNumber.trim()
        val trimmedPassword = password.trim()
        val trimmedConfirmPassword = confirmPassword.trim()

        val validationError = validateSignUp(
            fullName = trimmedFullName,
            phoneNumber = trimmedPhoneNumber,
            password = trimmedPassword,
            confirmPassword = trimmedConfirmPassword
        )

        if (validationError != null) {
            _uiState.value = _uiState.value.copy(signUpError = validationError)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, signUpError = null)

            val existingPatientWithPhone = patientRepository.getPatientByPhone(trimmedPhoneNumber)

            if (existingPatientWithPhone != null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    signUpError = "Phone number is already registered"
                )
                return@launch
            }

            val patients = patientRepository.getAllPatients().first()
            val patientId = generateNextPatientId(patients.map { it.patientId })

            patientRepository.insertPatient(
                PatientEntity(
                    patientId = patientId,
                    phoneNumber = trimmedPhoneNumber,
                    name = trimmedFullName,
                    password = trimmedPassword
                )
            )

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                signUpError = null,
                createdPatientId = patientId
            )
        }
    }

    fun logout() {
        appContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(LOGGED_IN_PATIENT_ID_KEY)
            .apply()
    }

    fun clearLoginResult() {
        _uiState.value = _uiState.value.copy(
            loginSucceeded = false,
            loggedInPatientId = null
        )
    }

    fun clearClaimMessage() {
        _uiState.value = _uiState.value.copy(claimMessage = null)
    }

    fun clearSignUpResult() {
        _uiState.value = _uiState.value.copy(createdPatientId = null)
    }

    fun clearErrors() {
        _uiState.value = _uiState.value.copy(
            loginError = null,
            claimError = null,
            signUpError = null
        )
    }

    private fun validateSignUp(
        fullName: String,
        phoneNumber: String,
        password: String,
        confirmPassword: String
    ): String? {
        return when {
            fullName.isBlank() -> "Full name is required"
            phoneNumber.isBlank() -> "Phone number is required"
            !phoneNumber.matches(Regex("^04\\d{8}$")) -> {
                "Phone number must start with 04 and be exactly 10 digits"
            }
            else -> validatePassword(password) ?: validateConfirmPassword(password, confirmPassword)
        }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password is required"
            password.length < 8 -> "Password must be at least 8 characters"
            !password.any { it.isLetter() } -> "Password must contain at least one letter"
            !password.any { it.isDigit() } -> "Password must contain at least one number"
            else -> null
        }
    }

    private fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isBlank() -> "Confirm password is required"
            confirmPassword != password -> "Confirm password must match password"
            else -> null
        }
    }

    private fun generateNextPatientId(patientIds: List<String>): String {
        val nextNumber = (patientIds.maxOfOrNull { extractPatientIdNumber(it) } ?: 0) + 1
        return "P$nextNumber"
    }

    private fun extractPatientIdNumber(patientId: String): Int {
        return patientId.removePrefix("P").toIntOrNull() ?: 0
    }

    class PatientViewModelFactory(
        context: Context,
        private val patientId: String? = null
    ) : ViewModelProvider.Factory {
        private val appContext = context.applicationContext

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PatientViewModel(appContext, patientId) as T
        }
    }

    companion object {
        private const val PREFS_NAME = "medtrack_prefs"
        private const val LOGGED_IN_PATIENT_ID_KEY = "logged_in_patient_id"
    }
}
