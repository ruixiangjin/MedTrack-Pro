package com.medtrackpro.demo.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenFdaApiService {
    @GET("drug/label.json")
    suspend fun searchDrugLabel(
        @Query("search") search: String,
        @Query("limit") limit: Int = 1
    ): OpenFdaResponse
}
