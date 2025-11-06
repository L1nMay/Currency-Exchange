package com.example.currencyconverter.network

import com.example.currencyconverter.data.model.BackendApiResponse
import com.example.currencyconverter.data.model.BackendConversionRequest
import com.example.currencyconverter.data.model.BackendConversionResponse
import com.example.currencyconverter.data.model.BackendExchangeRates
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface BackendApiService {
    @GET("/api/rates")
    suspend fun getExchangeRates(
        @Query("base") baseCurrency: String = "USD"
    ): Response<BackendApiResponse<BackendExchangeRates>>

    @POST("/api/convert")
    suspend fun convertCurrency(
        @Body request: BackendConversionRequest
    ): Response<BackendApiResponse<BackendConversionResponse>>

    @GET("/api/currencies")
    suspend fun getSupportedCurrencies(): Response<BackendApiResponse<List<String>>>

    @GET("/api/health")
    suspend fun healthCheck(): Response<BackendApiResponse<Any>>
}
