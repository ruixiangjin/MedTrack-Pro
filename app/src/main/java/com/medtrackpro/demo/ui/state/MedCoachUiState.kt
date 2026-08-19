package com.medtrackpro.demo.ui.state

data class DrugInfoUiState(
    val purpose: String,
    val warnings: String,
    val dosage: String
)

data class MedCoachUiState(
    val drugName: String = "",
    val isDrugLoading: Boolean = false,
    val drugError: String? = null,
    val drugInfo: DrugInfoUiState? = null,
    val latestTip: String? = null,
    val isTipLoading: Boolean = false,
    val tipError: String? = null,
    val showTipHistoryDialog: Boolean = false
)
