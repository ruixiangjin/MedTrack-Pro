package com.medtrackpro.demo.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.medtrackpro.demo.data.local.SymptomEntity
import com.medtrackpro.demo.data.repository.SymptomRepository
import com.medtrackpro.demo.ui.state.SymptomUiState
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class SymptomViewModel(
    context: Context,
    private val currentPatientId: String?
) : ViewModel() {
    private val appContext = context.applicationContext
    private val symptomRepository = SymptomRepository(appContext)

    val symptoms: Flow<List<SymptomEntity>> = if (currentPatientId == null) {
        flowOf(emptyList())
    } else {
        symptomRepository.getSymptomsForPatient(currentPatientId)
    }

    private val _uiState = MutableStateFlow(SymptomUiState())
    val uiState: StateFlow<SymptomUiState> = _uiState.asStateFlow()

    val symptomCategories = listOf(
        "Pain",
        "Nausea",
        "Dizziness",
        "Fatigue",
        "Headache",
        "Skin Reaction",
        "Other"
    )

    fun onCategorySelected(value: String) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = value,
            categoryError = null,
            symptomError = null
        )
    }

    fun onSeverityChanged(value: Float) {
        val updatedSeverity = value.coerceIn(1f, 10f)
        _uiState.value = _uiState.value.copy(
            severity = updatedSeverity,
            isSeveritySelected = true,
            severityError = validateSymptomSeverity(updatedSeverity.roundToInt()),
            symptomError = null
        )
    }

    fun onNotesChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            notes = value,
            notesError = validateSymptomNotes(value),
            symptomError = null
        )
    }

    fun showDatePicker() {
        _uiState.value = _uiState.value.copy(
            showDatePicker = true,
            datePickerInitialMillis = parseDateToUtcMillis(_uiState.value.selectedDate)
        )
    }

    fun dismissDatePicker() {
        _uiState.value = _uiState.value.copy(showDatePicker = false)
    }

    fun confirmDate(dateMillis: Long?) {
        if (dateMillis == null) {
            _uiState.value = _uiState.value.copy(showDatePicker = false)
            return
        }

        val selectedDate = formatDateFromUtcMillis(dateMillis)
        _uiState.value = _uiState.value.copy(
            selectedDate = selectedDate,
            dateTimeError = validateSymptomDateTime(
                selectedDate = selectedDate,
                selectedTime = _uiState.value.selectedTime
            ),
            showDatePicker = false,
            symptomError = null
        )
    }

    fun showTimePicker() {
        val selectedHourMinute = parseSymptomPickerTime(_uiState.value.selectedTime)
        val currentTime = LocalTime.now()
        _uiState.value = _uiState.value.copy(
            showTimePicker = true,
            timePickerInitialHour = selectedHourMinute?.first ?: currentTime.hour,
            timePickerInitialMinute = selectedHourMinute?.second ?: currentTime.minute
        )
    }

    fun dismissTimePicker() {
        _uiState.value = _uiState.value.copy(showTimePicker = false)
    }

    fun confirmTime(hour: Int, minute: Int) {
        val selectedTime = formatSymptomPickerTime(hour, minute)
        _uiState.value = _uiState.value.copy(
            selectedTime = selectedTime,
            dateTimeError = validateSymptomDateTime(
                selectedDate = _uiState.value.selectedDate,
                selectedTime = selectedTime
            ),
            showTimePicker = false,
            symptomError = null
        )
    }

    fun saveSymptom() {
        val state = _uiState.value
        val severityValue = if (state.isSeveritySelected) state.severityValue else null
        val categoryError = validateSymptomCategory(state.selectedCategory)
        val severityError = validateSymptomSeverity(severityValue)
        val notesError = validateSymptomNotes(state.notes)
        val dateTimeError = validateSymptomDateTime(
            selectedDate = state.selectedDate,
            selectedTime = state.selectedTime
        )

        _uiState.value = state.copy(
            categoryError = categoryError,
            severityError = severityError,
            notesError = notesError,
            dateTimeError = dateTimeError,
            symptomError = null
        )

        if (
            categoryError != null ||
            severityError != null ||
            notesError != null ||
            dateTimeError != null ||
            severityValue == null
        ) {
            return
        }

        val patientId = currentPatientId
        if (patientId == null) {
            _uiState.value = _uiState.value.copy(symptomError = "No logged-in patient found")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSavingSymptom = true,
                symptomError = null,
                symptomSaved = false
            )

            symptomRepository.insertSymptom(
                SymptomEntity(
                    patientId = patientId,
                    category = state.selectedCategory,
                    severity = severityValue,
                    notes = state.notes.trim(),
                    dateTime = "${state.selectedDate} ${state.selectedTime}"
                )
            )

            _uiState.value = _uiState.value.copy(
                isSavingSymptom = false,
                symptomSaved = true
            )
        }
    }

    fun clearSymptomForm() {
        _uiState.value = _uiState.value.copy(
            selectedCategory = "",
            severity = 1f,
            isSeveritySelected = false,
            notes = "",
            selectedDate = "",
            selectedTime = "",
            categoryError = null,
            severityError = null,
            notesError = null,
            dateTimeError = null,
            symptomError = null,
            showDatePicker = false,
            showTimePicker = false
        )
    }

    fun clearSymptomResult() {
        _uiState.value = _uiState.value.copy(symptomSaved = false)
    }

    private fun validateSymptomCategory(category: String): String? {
        return if (category.isBlank()) {
            "Symptom category is required"
        } else {
            null
        }
    }

    private fun validateSymptomSeverity(severity: Int?): String? {
        return when {
            severity == null -> "Severity is required"
            severity !in 1..10 -> "Severity must be between 1 and 10"
            else -> null
        }
    }

    private fun validateSymptomNotes(notes: String): String? {
        return if (notes.length > 200) {
            "Notes must be 200 characters or fewer"
        } else {
            null
        }
    }

    private fun validateSymptomDateTime(
        selectedDate: String,
        selectedTime: String
    ): String? {
        if (selectedDate.isBlank()) {
            return "Date is required"
        }

        if (selectedTime.isBlank()) {
            return "Time is required"
        }

        val selectedDateTime = "$selectedDate $selectedTime"
        val currentDateTime = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) +
            " " +
            LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))

        return when {
            selectedDateTime > currentDateTime -> "Date and time cannot be in the future"
            else -> null
        }
    }

    private fun parseSymptomPickerTime(time: String): Pair<Int, Int>? {
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

    private fun formatSymptomPickerTime(hour: Int, minute: Int): String {
        return String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
    }

    private fun parseDateToUtcMillis(date: String): Long? {
        return try {
            LocalDate
                .parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        } catch (_: Exception) {
            null
        }
    }

    private fun formatDateFromUtcMillis(dateMillis: Long): String {
        return Instant
            .ofEpochMilli(dateMillis)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
            .format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    class SymptomViewModelFactory(
        context: Context,
        private val patientId: String?
    ) : ViewModelProvider.Factory {
        private val appContext = context.applicationContext

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SymptomViewModel(appContext, patientId) as T
        }
    }
}
