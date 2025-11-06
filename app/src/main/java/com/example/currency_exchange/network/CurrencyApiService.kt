package com.example.currencyconverter.network

import com.example.currencyconverter.data.model.ApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyApiService {
    
    @GET("latest")
    suspend fun getLatestRates(
        @Query("base") baseCurrency: String = "USD"
    ): Response<ApiResponse>
    
    @GET("latest")
    suspend fun getLatestRatesWithBase(
        @Query("base") baseCurrency: String
    ): Response<ApiResponse>
}
