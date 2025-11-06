package com.example.currencyconverter.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MapConverter {
    
    private val gson = Gson()
    
    @TypeConverter
    fun fromString(value: String): Map<String, Double> {
        val mapType = object : TypeToken<Map<String, Double>>() {}.type
        return gson.fromJson(value, mapType)
    }

    @TypeConverter
    fun fromMap(map: Map<String, Double>): String {
        return gson.toJson(map)
    }
    
    @TypeConverter
    fun fromStringToStringMap(value: String): Map<String, String> {
        val mapType = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(value, mapType)
    }

    @TypeConverter
    fun fromStringMap(map: Map<String, String>): String {
        return gson.toJson(map)
    }
}
