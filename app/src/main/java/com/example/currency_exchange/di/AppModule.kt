package com.example.currencyconverter.di

import android.content.Context
import com.example.currencyconverter.data.AppDatabase
import com.example.currencyconverter.data.repository.CurrencyRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }
    
    @Provides
    @Singleton
    fun provideCurrencyDao(database: AppDatabase) = database.currencyDao()
    
    @Provides
    @Singleton
    fun provideCurrencyRepository(currencyDao: com.example.currencyconverter.data.dao.CurrencyDao): CurrencyRepository {
        return CurrencyRepository(currencyDao)
    }
}
