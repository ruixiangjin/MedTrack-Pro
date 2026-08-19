package com.medtrackpro.demo.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.medtrackpro.demo.data.local.MedCoachTipEntity
import com.medtrackpro.demo.data.local.MedicationEntity
import com.medtrackpro.demo.data.local.PatientEntity
import com.medtrackpro.demo.data.local.SymptomEntity
import com.medtrackpro.demo.data.repository.MedCoachTipRepository
import com.medtrackpro.demo.data.repository.MedicationRepository
import com.medtrackpro.demo.data.repository.PatientRepository
import com.medtrackpro.demo.data.repository.SymptomRepository
import com.medtrackpro.demo.ui.state.HealthReportUiState
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HealthReportViewModel(
    context: Context,
    private val currentPatientId: String?
) : ViewModel() {
    private val appContext = context.applicationContext
    private val patientRepository = PatientRepository(appContext)
    private val medicationRepository = MedicationRepository(appContext)
    private val symptomRepository = SymptomRepository(appContext)
    private val medCoachTipRepository = MedCoachTipRepository(appContext)
    private val reportTimestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    private val tipTimestampFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    private val _uiState = MutableStateFlow(HealthReportUiState())
    val uiState: StateFlow<HealthReportUiState> = _uiState.asStateFlow()

    fun generateHealthReport() {
        val patientId = currentPatientId

        if (patientId == null) {
            _uiState.value = _uiState.value.copy(
                isReportLoading = false,
                reportError = "Unable to generate report because no patient was found."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = HealthReportUiState(isReportLoading = true)

            try {
                val patient = patientRepository.getPatientById(patientId)

                if (patient == null) {
                    _uiState.value = HealthReportUiState(
                        reportError = "Unable to generate report because no patient was found."
                    )
                    return@launch
                }

                val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                val medications = medicationRepository.getMedicationsForPatientOnce(patientId)
                val takenStatuses = medicationRepository.getTakenStatusesForDateOnce(
                    patientId = patientId,
                    date = todayDate
                )
                val symptoms = symptomRepository.getRecentSymptomsForPatient(
                    patientId = patientId,
                    limit = 10
                )
                val tips = medCoachTipRepository.getRecentTipsForPatient(
                    patientId = patientId,
                    limit = 5
                )

                _uiState.value = HealthReportUiState(
                    reportText = buildHealthReport(
                        patient = patient,
                        medications = medications,
                        takenStatusesByMedicationId = takenStatuses.associateBy { it.medicationId },
                        symptoms = symptoms,
                        tips = tips,
                        todayDate = todayDate
                    )
                )
            } catch (_: Exception) {
                _uiState.value = HealthReportUiState(
                    reportError = "Unable to prepare report."
                )
            }
        }
    }

    fun clearReportText() {
        _uiState.value = _uiState.value.copy(reportText = null)
    }

    private fun buildHealthReport(
        patient: PatientEntity,
        medications: List<MedicationEntity>,
        takenStatusesByMedicationId: Map<Int, com.medtrackpro.demo.data.local.TakenStatusEntity>,
        symptoms: List<SymptomEntity>,
        tips: List<MedCoachTipEntity>,
        todayDate: String
    ): String {
        val generatedAt = LocalDateTime.now().format(reportTimestampFormatter)

        return buildString {
            appendLine("MedTrack Health Report")
            appendLine("Generated: $generatedAt")
            appendLine()
            appendPatientSection(patient)
            appendLine()
            appendMedicationSection(medications)
            appendLine()
            appendTakenStatusSection(
                medications = medications,
                takenStatusesByMedicationId = takenStatusesByMedicationId,
                todayDate = todayDate
            )
            appendLine()
            appendSymptomsSection(symptoms)
            appendLine()
            appendTipsSection(tips)
            appendLine()
            appendLine("Disclaimer")
            appendLine(
                "This report is generated from user-entered MedTrack records. " +
                    "It is for tracking and discussion purposes only and does not replace professional medical advice."
            )
        }
    }

    private fun StringBuilder.appendPatientSection(patient: PatientEntity) {
        appendLine("Patient")
        appendLine("Patient ID: ${patient.patientId.ifBlank { "Not available" }}")
        appendLine("Name: ${patient.name.ifBlank { "Not available" }}")
        appendLine("Phone: ${patient.phoneNumber.ifBlank { "Not available" }}")
    }

    private fun StringBuilder.appendMedicationSection(medications: List<MedicationEntity>) {
        appendLine("Medications")

        if (medications.isEmpty()) {
            appendLine("No medications recorded.")
            return
        }

        medications.forEachIndexed { index, medication ->
            appendLine("${index + 1}. ${medication.name.ifBlank { "Not available" }}")
            appendLine("   Dosage: ${medication.dosage.ifBlank { "Not available" }}")
            appendLine("   Frequency: ${medication.frequency.ifBlank { "Not available" }}")
            appendLine("   Scheduled Time: ${medication.scheduledTime.ifBlank { "Not available" }}")
            appendLine("   Type: ${medication.medicationType.ifBlank { "Not available" }}")
            appendLine("   Notes: ${medication.notes.ifBlank { "None" }}")
            if (index != medications.lastIndex) {
                appendLine()
            }
        }
    }

    private fun StringBuilder.appendTakenStatusSection(
        medications: List<MedicationEntity>,
        takenStatusesByMedicationId: Map<Int, com.medtrackpro.demo.data.local.TakenStatusEntity>,
        todayDate: String
    ) {
        appendLine("Today's Taken Status")
        appendLine("Date: $todayDate")

        if (medications.isEmpty()) {
            appendLine("No medications recorded.")
            return
        }

        medications.forEachIndexed { index, medication ->
            val status = if (takenStatusesByMedicationId[medication.id]?.isTaken == true) {
                "Taken"
            } else {
                "Not taken"
            }
            appendLine("${index + 1}. ${medication.name.ifBlank { "Not available" }}: $status")
        }
    }

    private fun StringBuilder.appendSymptomsSection(symptoms: List<SymptomEntity>) {
        appendLine("Symptom History")

        if (symptoms.isEmpty()) {
            appendLine("No symptoms recorded.")
            return
        }

        symptoms.forEachIndexed { index, symptom ->
            appendLine("${index + 1}. ${symptom.dateTime.ifBlank { "Not available" }}")
            appendLine("   Category: ${symptom.category.ifBlank { "Not available" }}")
            appendLine("   Severity: ${symptom.severity}/10")
            appendLine("   Notes: ${symptom.notes.ifBlank { "None" }}")
            if (index != symptoms.lastIndex) {
                appendLine()
            }
        }
    }

    private fun StringBuilder.appendTipsSection(tips: List<MedCoachTipEntity>) {
        appendLine("MedCoach Tips")

        if (tips.isEmpty()) {
            appendLine("No MedCoach tips generated yet.")
            return
        }

        tips.forEachIndexed { index, tip ->
            appendLine("${index + 1}. ${tipTimestampFormatter.format(Date(tip.timestamp))}")
            appendLine("   ${tip.tipText.ifBlank { "Not available" }}")
            if (index != tips.lastIndex) {
                appendLine()
            }
        }
    }

    class HealthReportViewModelFactory(
        context: Context,
        private val patientId: String?
    ) : ViewModelProvider.Factory {
        private val appContext = context.applicationContext

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HealthReportViewModel(appContext, patientId) as T
        }
    }
}
