package com.medtrackpro.demo.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.medtrackpro.demo.data.local.MedicationEntity
import com.medtrackpro.demo.data.local.TakenStatusEntity
import com.medtrackpro.demo.data.repository.MedicationRepository
import com.medtrackpro.demo.ui.state.MedicationUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class MedicationViewModel(
    context: Context,
    private val currentPatientId: String?
) : ViewModel() {
    private val appContext = context.applicationContext
    private val medicationRepository = MedicationRepository(appContext)
    private val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    val medications: Flow<List<MedicationEntity>> = if (currentPatientId == null) {
        flowOf(emptyList())
    } else {
        medicationRepository.getMedicationsForPatient(currentPatientId)
    }

    val takenStatuses: Flow<List<TakenStatusEntity>> = if (currentPatientId == null) {
        flowOf(emptyList())
    } else {
        medicationRepository.getTakenStatusesForDate(currentPatientId, todayDate)
    }

    private val _uiState = MutableStateFlow(MedicationUiState())
    val uiState: StateFlow<MedicationUiState> = _uiState.asStateFlow()

    val frequencyOptions = listOf("Once daily", "Twice daily", "Three times daily", "As needed")
    val medicationTypeOptions = listOf("Tablet", "Capsule", "Liquid", "Injection", "Topical", "Other")

    fun onMedicationNameChange(value: String) {
        _uiState.value = _uiState.value.copy(
            medicationName = value,
            medicationNameError = validateMedicationName(value.trim()),
            addMedicationError = null
        )
    }

    fun onDosageChange(value: String) {
        _uiState.value = _uiState.value.copy(
            dosage = value,
            dosageError = validateDosage(value.trim()),
            addMedicationError = null
        )
    }

    fun onFrequencySelected(value: String) {
        _uiState.value = _uiState.value.copy(
            frequency = value,
            frequencyError = null,
            addMedicationError = null
        )
    }

    fun onMedicationTypeSelected(value: String) {
        _uiState.value = _uiState.value.copy(
            medicationType = value,
            medicationTypeError = null,
            addMedicationError = null
        )
    }

    fun onNotesChange(value: String) {
        _uiState.value = _uiState.value.copy(notes = value)
    }

    fun showScheduledTimePicker() {
        val selectedHourMinute = parsePickerTime(_uiState.value.scheduledTime)
        _uiState.value = _uiState.value.copy(
            showScheduledTimePicker = true,
            scheduledTimePickerHour = selectedHourMinute?.first ?: 8,
            scheduledTimePickerMinute = selectedHourMinute?.second ?: 0
        )
    }

    fun dismissScheduledTimePicker() {
        _uiState.value = _uiState.value.copy(showScheduledTimePicker = false)
    }

    fun confirmScheduledTime(hour: Int, minute: Int) {
        _uiState.value = _uiState.value.copy(
            scheduledTime = formatPickerTime(hour, minute),
            scheduledTimeError = null,
            showScheduledTimePicker = false,
            addMedicationError = null
        )
    }

    fun saveMedication() {
        val state = _uiState.value
        val trimmedName = state.medicationName.trim()
        val trimmedDosage = state.dosage.trim()
        val trimmedNotes = state.notes.trim()
        val medicationNameError = validateMedicationName(trimmedName)
        val dosageError = validateDosage(trimmedDosage)
        val frequencyError = validateRequiredSelection(state.frequency, "Frequency")
        val scheduledTimeError = validateScheduledTime(state.scheduledTime)
        val medicationTypeError = validateRequiredSelection(state.medicationType, "Medication type")

        _uiState.value = state.copy(
            medicationNameError = medicationNameError,
            dosageError = dosageError,
            frequencyError = frequencyError,
            scheduledTimeError = scheduledTimeError,
            medicationTypeError = medicationTypeError,
            addMedicationError = null
        )

        if (
            medicationNameError != null ||
            dosageError != null ||
            frequencyError != null ||
            scheduledTimeError != null ||
            medicationTypeError != null
        ) {
            return
        }

        val patientId = currentPatientId
        if (patientId == null) {
            _uiState.value = _uiState.value.copy(addMedicationError = "No logged-in patient found")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSavingMedication = true,
                addMedicationError = null,
                medicationSaved = false
            )

            medicationRepository.insertMedication(
                MedicationEntity(
                    patientId = patientId,
                    name = trimmedName,
                    dosage = trimmedDosage,
                    frequency = state.frequency,
                    scheduledTime = state.scheduledTime,
                    medicationType = state.medicationType,
                    notes = trimmedNotes
                )
            )

            _uiState.value = _uiState.value.copy(
                isSavingMedication = false,
                medicationSaved = true
            )
        }
    }

    fun clearMedicationForm() {
        _uiState.value = _uiState.value.copy(
            medicationName = "",
            dosage = "",
            frequency = "",
            scheduledTime = "",
            medicationType = "",
            notes = "",
            medicationNameError = null,
            dosageError = null,
            frequencyError = null,
            scheduledTimeError = null,
            medicationTypeError = null,
            addMedicationError = null,
            showScheduledTimePicker = false
        )
    }

    fun clearMedicationResult() {
        _uiState.value = _uiState.value.copy(medicationSaved = false)
    }

    fun setTaken(
        medicationId: Int,
        isTaken: Boolean
    ) {
        val patientId = currentPatientId
        if (patientId == null) {
            return
        }

        viewModelScope.launch {
            medicationRepository.setMedicationTaken(
                patientId = patientId,
                medicationId = medicationId,
                date = todayDate,
                isTaken = isTaken
            )
        }
    }

    private fun validateMedicationName(medicationName: String): String? {
        return if (medicationName.isBlank()) {
            "Medication name is required"
        } else {
            null
        }
    }

    private fun validateDosage(dosage: String): String? {
        if (dosage.isBlank()) {
            return "Dosage is required"
        }

        val match = Regex("^(\\d+(?:\\.\\d+)?)(mg|ml|g)$").find(dosage)
            ?: return "Dosage must be a number followed by mg, ml, or g"
        val value = match.groupValues[1].toDouble()

        return if (value <= 0.0) {
            "Dosage must be greater than 0"
        } else {
            null
        }
    }

    private fun validateRequiredSelection(value: String, fieldName: String): String? {
        return if (value.isBlank()) {
            "$fieldName is required"
        } else {
            null
        }
    }

    private fun validateScheduledTime(scheduledTime: String): String? {
        return if (scheduledTime.isBlank()) {
            "Scheduled time is required"
        } else {
            null
        }
    }

    private fun parsePickerTime(time: String): Pair<Int, Int>? {
        val parts = time.split(":")
        if (parts.size != 2) {
            return null
        }

        val hour = parts[0].toIntOrNull()
        val minute = parts[1].toIntOrNull()
        return if (
            hour != null &&
            minute != null &&
            hour in 0..23 &&
            minute in 0..59
        ) {
            hour to minute
        } else {
            null
        }
    }

    private fun formatPickerTime(hour: Int, minute: Int): String {
        return String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
    }

    class MedicationViewModelFactory(
        context: Context,
        private val patientId: String?
    ) : ViewModelProvider.Factory {
        private val appContext = context.applicationContext

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MedicationViewModel(appContext, patientId) as T
        }
    }
}
