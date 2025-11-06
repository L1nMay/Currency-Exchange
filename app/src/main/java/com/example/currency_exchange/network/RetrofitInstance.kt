package com.example.currencyconverter.network

import com.example.currencyconverter.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    
    private const val FRANKFURTER_BASE_URL = "https://api.frankfurter.app/"
    private const val BACKEND_BASE_URL = "http://10.0.2.2:8080/" // Для эмулятора Android
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .apply {
            if (BuildConfig.DEBUG) {
                val logging = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
                addInterceptor(logging)
            }
            
            // Добавляем интерцептор для логирования
            addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("User-Agent", "CurrencyConverter-Android/1.0.0")
                    .addHeader("Accept", "application/json")
                    .build()
                chain.proceed(request)
            }
        }
        .build()
    
    // API Frankfurter (внешний источник)
    val frankfurterApi: CurrencyApiService by lazy {
        Retrofit.Builder()
            .baseUrl(FRANKFURTER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(CurrencyApiService::class.java)
    }
    
    // Наш собственный бэкенд API
    val backendApi: BackendApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BACKEND_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(BackendApiService::class.java)
    }
}
