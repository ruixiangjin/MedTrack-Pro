package com.medtrackpro.demo.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.medtrackpro.demo.data.local.MedCoachTipEntity
import com.medtrackpro.demo.data.local.MedicationEntity
import com.medtrackpro.demo.data.remote.OpenFdaDrugResult
import com.medtrackpro.demo.data.repository.DrugRepository
import com.medtrackpro.demo.data.repository.MedCoachTipRepository
import com.medtrackpro.demo.data.repository.MedicationRepository
import com.medtrackpro.demo.data.repository.PatientRepository
import com.medtrackpro.demo.data.repository.SymptomRepository
import com.medtrackpro.demo.ui.state.DrugInfoUiState
import com.medtrackpro.demo.ui.state.MedCoachUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class MedCoachViewModel(
    context: Context,
    patientId: String? = null
) : ViewModel() {
    private val appContext = context.applicationContext
    private val patientRepository = PatientRepository(appContext)
    private val medicationRepository = MedicationRepository(appContext)
    private val symptomRepository = SymptomRepository(appContext)
    private val medCoachTipRepository = MedCoachTipRepository(appContext)
    private val drugRepository = DrugRepository()
    private val currentPatientId = patientId ?: appContext
        .getSharedPreferences("medtrack_prefs", Context.MODE_PRIVATE)
        .getString("logged_in_patient_id", null)

    val medications: Flow<List<MedicationEntity>> = if (currentPatientId == null) {
        flowOf(emptyList())
    } else {
        medicationRepository.getMedicationsForPatient(currentPatientId)
    }

    val tipHistory: Flow<List<MedCoachTipEntity>> = if (currentPatientId == null) {
        flowOf(emptyList())
    } else {
        medCoachTipRepository.getTipsForPatient(currentPatientId)
    }

    private val _uiState = MutableStateFlow(MedCoachUiState())
    val uiState: StateFlow<MedCoachUiState> = _uiState.asStateFlow()

    fun updateDrugName(name: String) {
        _uiState.value = _uiState.value.copy(
            drugName = name,
            drugError = null
        )
    }

    fun lookupDrug() {
        val trimmedDrugName = _uiState.value.drugName.trim()

        if (trimmedDrugName.isBlank()) {
            _uiState.value = _uiState.value.copy(
                drugError = "Enter a medication name",
                drugInfo = null
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDrugLoading = true,
                drugError = null,
                drugInfo = null
            )

            try {
                val result = drugRepository.searchDrug(trimmedDrugName)

                _uiState.value = if (result == null) {
                    _uiState.value.copy(
                        isDrugLoading = false,
                        drugError = "Drug not found. Try another medication name.",
                        drugInfo = null
                    )
                } else {
                    _uiState.value.copy(
                        isDrugLoading = false,
                        drugError = null,
                        drugInfo = result.toDrugInfoUiState()
                    )
                }
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDrugLoading = false,
                    drugError = "Unable to load drug information. Check your connection.",
                    drugInfo = null
                )
            }
        }
    }

    fun generateTip() {
        val patientId = currentPatientId

        if (patientId == null) {
            _uiState.value = _uiState.value.copy(
                tipError = "No logged-in patient found"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isTipLoading = true,
                tipError = null
            )

            try {
                val patient = patientRepository.getPatientById(patientId)
                val medications = medicationRepository
                    .getMedicationsForPatientOnce(patientId)
                    .take(5)
                val recentSymptoms = symptomRepository
                    .getRecentSymptomsForPatient(patientId, limit = 5)
                val tip = medCoachTipRepository.generateMedicationTip(
                    patientName = patient?.name,
                    medications = medications,
                    recentSymptoms = recentSymptoms
                )

                _uiState.value = if (tip == null) {
                    _uiState.value.copy(
                        isTipLoading = false,
                        tipError = "Unable to generate tip. Try again later."
                    )
                } else {
                    medCoachTipRepository.saveTip(
                        patientId = patientId,
                        tipText = tip
                    )

                    _uiState.value.copy(
                        latestTip = tip,
                        isTipLoading = false,
                        tipError = null
                    )
                }
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isTipLoading = false,
                    tipError = exception.message ?: "Unable to generate tip. Check your connection."
                )
            }
        }
    }

    fun openTipHistory() {
        _uiState.value = _uiState.value.copy(showTipHistoryDialog = true)
    }

    fun closeTipHistory() {
        _uiState.value = _uiState.value.copy(showTipHistoryDialog = false)
    }

    private fun OpenFdaDrugResult.toDrugInfoUiState(): DrugInfoUiState {
        return DrugInfoUiState(
            purpose = purpose.firstDisplayText(),
            warnings = warnings.firstDisplayText(),
            dosage = dosageAndAdministration.firstDisplayText()
        )
    }

    private fun List<String>?.firstDisplayText(): String {
        return this
            ?.firstOrNull()
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: "Not available"
    }

    class MedCoachViewModelFactory(
        context: Context,
        private val patientId: String? = null
    ) : ViewModelProvider.Factory {
        private val appContext = context.applicationContext

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MedCoachViewModel(appContext, patientId) as T
        }
    }
}
