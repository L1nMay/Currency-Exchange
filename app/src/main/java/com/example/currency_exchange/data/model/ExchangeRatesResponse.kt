package com.example.currencyconverter.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

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
) {
    fun getRate(currency: String): Double? {
        return rates[currency]
    }
    
    fun isExpired(): Boolean {
        return System.currentTimeMillis() - lastUpdated > 5 * 60 * 1000 // 5 минут
    }
}
