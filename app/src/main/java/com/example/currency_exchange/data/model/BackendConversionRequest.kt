package com.example.currencyconverter.data.model

import com.google.gson.annotations.SerializedName

data class BackendConversionRequest(
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("from")
    val from: String,
    @SerializedName("to")
    val to: String
)

data class BackendConversionResponse(
    @SerializedName("from")
    val from: String,
    @SerializedName("to")
    val to: String,
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("converted")
    val converted: Double,
    @SerializedName("rate")
    val rate: Double,
    @SerializedName("timestamp")
    val timestamp: String,
    @SerializedName("hash")
    val hash: String?
)

data class BackendExchangeRates(
    @SerializedName("base")
    val base: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("rates")
    val rates: Map<String, Double>,
    @SerializedName("encrypted_rates")
    val encryptedRates: Map<String, String>?,
    @SerializedName("last_updated")
    val lastUpdated: String
)
