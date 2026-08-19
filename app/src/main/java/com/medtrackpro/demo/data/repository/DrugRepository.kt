package com.medtrackpro.demo.data.repository

import com.medtrackpro.demo.data.remote.OpenFdaDrugResult
import com.medtrackpro.demo.data.remote.OpenFdaRetrofitClient

class DrugRepository {
    private val openFdaApi = OpenFdaRetrofitClient.api

    suspend fun searchDrug(drugName: String): OpenFdaDrugResult? {
        val trimmedDrugName = drugName.trim()

        if (trimmedDrugName.isBlank()) {
            return null
        }

        return searchByOpenFdaField("brand_name", trimmedDrugName)
            ?: searchByOpenFdaField("generic_name", trimmedDrugName)
    }

    private suspend fun searchByOpenFdaField(
        fieldName: String,
        drugName: String
    ): OpenFdaDrugResult? {
        val searchQuery = "openfda.$fieldName:\"$drugName\""
        return openFdaApi
            .searchDrugLabel(search = searchQuery, limit = 1)
            .results
            ?.firstOrNull()
    }
}
