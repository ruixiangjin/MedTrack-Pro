package com.medtrackpro.demo.data.remote

import com.google.gson.annotations.SerializedName

data class OpenFdaResponse(
    val results: List<OpenFdaDrugResult>? = null
)

data class OpenFdaDrugResult(
    val purpose: List<String>? = null,
    val warnings: List<String>? = null,
    @SerializedName("dosage_and_administration")
    val dosageAndAdministration: List<String>? = null,
    @SerializedName("active_ingredient")
    val activeIngredient: List<String>? = null,
    @SerializedName("indications_and_usage")
    val indicationsAndUsage: List<String>? = null,
    val openfda: OpenFdaInfo? = null
)

data class OpenFdaInfo(
    @SerializedName("brand_name")
    val brandName: List<String>? = null,
    @SerializedName("generic_name")
    val genericName: List<String>? = null
)
