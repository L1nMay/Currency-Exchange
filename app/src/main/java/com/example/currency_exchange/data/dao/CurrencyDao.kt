package com.example.currencyconverter.data.dao

import androidx.room.*
import com.example.currencyconverter.data.model.ConversionHistory
import com.example.currencyconverter.data.model.ExchangeRatesResponse
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrencyDao {
    
    // Exchange Rates operations
    @Query("SELECT * FROM exchange_rates WHERE id = 'latest'")
    fun getLatestRates(): Flow<ExchangeRatesResponse?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRates(rates: ExchangeRatesResponse)
    
    @Query("DELETE FROM exchange_rates WHERE id = 'latest'")
    suspend fun deleteOldRates()
    
    @Query("SELECT COUNT(*) FROM exchange_rates WHERE id = 'latest'")
    suspend fun hasCachedRates(): Int
    
    // Conversion History operations
    @Query("SELECT * FROM conversion_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<ConversionHistory>>
    
    @Query("SELECT * FROM conversion_history WHERE currencyFrom = :from AND currencyTo = :to ORDER BY timestamp DESC")
    fun getHistoryByCurrencies(from: String, to: String): Flow<List<ConversionHistory>>
    
    @Query("SELECT * FROM conversion_history WHERE id = :id")
    suspend fun getHistoryById(id: String): ConversionHistory?
    
    @Insert
    suspend fun insertHistory(history: ConversionHistory): Long
    
    @Update
    suspend fun updateHistory(history: ConversionHistory)
    
    @Delete
    suspend fun deleteHistory(history: ConversionHistory)
    
    @Query("DELETE FROM conversion_history WHERE id = :id")
    suspend fun deleteHistoryById(id: String)
    
    @Query("DELETE FROM conversion_history")
    suspend fun clearAllHistory()
    
    @Query("SELECT COUNT(*) FROM conversion_history")
    suspend fun getHistoryCount(): Int
}
