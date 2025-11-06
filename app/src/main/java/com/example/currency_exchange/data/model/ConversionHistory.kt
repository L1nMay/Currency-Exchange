package com.example.currencyconverter.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.currencyconverter.data.converters.DateConverter
import java.util.Date
import java.util.UUID

@Entity(tableName = "conversion_history")
@TypeConverters(DateConverter::class)
data class ConversionHistory(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val amountFrom: Double,
    val currencyFrom: String,
    val amountTo: Double,
    val currencyTo: String,
    val rate: Double,
    val timestamp: Date = Date(),
    val isEncrypted: Boolean = false,
    val transactionHash: String? = null
) {
    fun getFormattedAmountFrom(): String {
        return String.format("%.2f %s", amountFrom, currencyFrom)
    }
    
    fun getFormattedAmountTo(): String {
        return String.format("%.2f %s", amountTo, currencyTo)
    }
    
    fun getFormattedRate(): String {
        return String.format("1 %s = %.4f %s", currencyFrom, rate, currencyTo)
    }
}
