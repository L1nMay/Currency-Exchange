package com.example.currencyconverter.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.Date

@Entity(tableName = "exchange_rates")
data class ExchangeRatesResponse(
    @PrimaryKey
    val id: String = "latest",
    @SerializedName("base")
    val baseCurrency: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("rates")
    val rates: Map<String, Double>,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class ApiResponse(
    val success: Boolean,
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)
