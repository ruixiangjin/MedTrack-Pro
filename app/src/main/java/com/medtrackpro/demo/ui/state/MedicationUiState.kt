package com.medtrackpro.demo.ui.state

data class MedicationUiState(
    val isSavingMedication: Boolean = false,
    val addMedicationError: String? = null,
    val medicationSaved: Boolean = false,
    val medicationName: String = "",
    val dosage: String = "",
    val frequency: String = "",
    val scheduledTime: String = "",
    val medicationType: String = "",
    val notes: String = "",
    val medicationNameError: String? = null,
    val dosageError: String? = null,
    val frequencyError: String? = null,
    val scheduledTimeError: String? = null,
    val medicationTypeError: String? = null,
    val showScheduledTimePicker: Boolean = false,
    val scheduledTimePickerHour: Int = 8,
    val scheduledTimePickerMinute: Int = 0
)
