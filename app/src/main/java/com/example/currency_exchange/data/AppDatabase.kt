package com.example.currencyconverter.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.currencyconverter.data.converters.DateConverter
import com.example.currencyconverter.data.converters.MapConverter
import com.example.currencyconverter.data.dao.CurrencyDao
import com.example.currencyconverter.data.model.ConversionHistory
import com.example.currencyconverter.data.model.ExchangeRatesResponse

@Database(
    entities = [ExchangeRatesResponse::class, ConversionHistory::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class, MapConverter::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun currencyDao(): CurrencyDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "currency_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
