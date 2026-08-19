package com.medtrackpro.demo.ui.state

import kotlin.math.roundToInt

data class SymptomUiState(
    val isSavingSymptom: Boolean = false,
    val symptomError: String? = null,
    val symptomSaved: Boolean = false,
    val selectedCategory: String = "",
    val severity: Float = 1f,
    val isSeveritySelected: Boolean = false,
    val notes: String = "",
    val selectedDate: String = "",
    val selectedTime: String = "",
    val categoryError: String? = null,
    val severityError: String? = null,
    val notesError: String? = null,
    val dateTimeError: String? = null,
    val showDatePicker: Boolean = false,
    val datePickerInitialMillis: Long? = null,
    val showTimePicker: Boolean = false,
    val timePickerInitialHour: Int = 0,
    val timePickerInitialMinute: Int = 0
) {
    val severityValue: Int
        get() = severity.roundToInt()
}
