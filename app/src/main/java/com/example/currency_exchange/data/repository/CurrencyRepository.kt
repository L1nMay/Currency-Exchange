package com.example.currencyconverter.data.repository

import com.example.currencyconverter.data.dao.CurrencyDao
import com.example.currencyconverter.data.model.ConversionHistory
import com.example.currencyconverter.data.model.ExchangeRatesResponse
import com.example.currencyconverter.network.RetrofitInstance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class CurrencyRepository @Inject constructor(
    private val currencyDao: CurrencyDao
) {
    
    // Local database operations
    fun getLatestRates(): Flow<ExchangeRatesResponse?> = currencyDao.getLatestRates()
    
    fun getConversionHistory(): Flow<List<ConversionHistory>> = currencyDao.getAllHistory()
    
    suspend fun saveConversion(history: ConversionHistory) = currencyDao.insertHistory(history)
    
    suspend fun deleteHistory(history: ConversionHistory) = currencyDao.deleteHistory(history)
    
    suspend fun deleteHistoryById(id: String) = currencyDao.deleteHistoryById(id)
    
    suspend fun clearAllHistory() = currencyDao.clearAllHistory()
    
    suspend fun getHistoryCount(): Int = currencyDao.getHistoryCount()
    
    suspend fun hasCachedRates(): Boolean = currencyDao.hasCachedRates() > 0

    // Network operations with multiple fallback APIs
    suspend fun fetchLatestRates(baseCurrency: String = "USD"): Flow<Result<ExchangeRatesResponse>> = flow {
        emit(Result.loading())
        
        try {
            // Try our backend API first
            val backendResponse = RetrofitInstance.backendApi.getExchangeRates(baseCurrency)
            if (backendResponse.isSuccessful && backendResponse.body()?.success == true) {
                val backendData = backendResponse.body()!!.data!!
                val ratesResponse = ExchangeRatesResponse(
                    baseCurrency = backendData.base,
                    date = backendData.date,
                    rates = backendData.rates
                )
                
                // Save to local database
                currencyDao.insertRates(ratesResponse)
                emit(Result.success(ratesResponse))
            } else {
                // Fallback to Frankfurter API
                try {
                    val frankfurterResponse = RetrofitInstance.frankfurterApi.getLatestRates(baseCurrency)
                    if (frankfurterResponse.isSuccessful && frankfurterResponse.body() != null) {
                        val apiResponse = frankfurterResponse.body()!!
                        val ratesResponse = ExchangeRatesResponse(
                            baseCurrency = apiResponse.base,
                            date = apiResponse.date,
                            rates = apiResponse.rates
                        )
                        
                        currencyDao.insertRates(ratesResponse)
                        emit(Result.success(ratesResponse))
                    } else {
                        emit(Result.error("Both APIs failed to respond"))
                    }
                } catch (e: Exception) {
                    emit(Result.error("Frankfurter API failed: ${e.message}"))
                }
            }
        } catch (e: HttpException) {
            emit(Result.error("HTTP error: ${e.message}"))
        } catch (e: IOException) {
            emit(Result.error("Network error: ${e.message}"))
        } catch (e: Exception) {
            emit(Result.error("Unexpected error: ${e.message}"))
        }
    }
    
    suspend fun convertWithBackend(
        amount: Double,
        fromCurrency: String,
        toCurrency: String
    ): Flow<Result<Double>> = flow {
        emit(Result.loading())
        
        try {
            val request = com.example.currencyconverter.data.model.BackendConversionRequest(
                amount = amount,
                from = fromCurrency,
                to = toCurrency
            )
            
            val response = RetrofitInstance.backendApi.convertCurrency(request)
            if (response.isSuccessful && response.body()?.success == true) {
                val result = response.body()!!.data!!
                emit(Result.success(result.converted))
            } else {
                emit(Result.error("Backend conversion failed"))
            }
        } catch (e: Exception) {
            emit(Result.error("Conversion error: ${e.message}"))
        }
    }
    
    suspend fun convertCurrency(
        amount: Double,
        fromCurrency: String,
        toCurrency: String,
        rates: Map<String, Double>
    ): Double? {
        return try {
            if (fromCurrency == toCurrency) return amount
            
            val fromRate = rates[fromCurrency] ?: 1.0
            val toRate = rates[toCurrency] ?: return null
            
            // Convert to base currency first, then to target currency
            val amountInBase = amount / fromRate
            amountInBase * toRate
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun getSupportedCurrencies(): List<String> {
        return try {
            val response = RetrofitInstance.backendApi.getSupportedCurrencies()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()!!.data!!
            } else {
                // Fallback currencies
                listOf("USD", "EUR", "GBP", "JPY", "CAD", "AUD", "CHF", "CNY")
            }
        } catch (e: Exception) {
            listOf("USD", "EUR", "GBP", "JPY", "CAD", "AUD", "CHF", "CNY")
        }
    }
}

// Result wrapper for network operations
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
    
    companion object {
        fun <T> success(data: T): Result<T> = Success(data)
        fun error(message: String): Result<Nothing> = Error(message)
        fun loading(): Result<Nothing> = Loading
    }
}
