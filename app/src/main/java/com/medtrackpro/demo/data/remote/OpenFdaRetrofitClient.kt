package com.medtrackpro.demo.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object OpenFdaRetrofitClient {
    val api: OpenFdaApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.fda.gov/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenFdaApiService::class.java)
    }
}
