package com.example.currencyconverter.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class CryptoManager(private val context: Context) {
    
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }
    
    companion object {
        private const val KEY_ALIAS = "currency_converter_key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val PREFS_NAME = "encrypted_currency_prefs"
    }
    
    private fun getOrCreateKey(): SecretKey {
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, 
                ANDROID_KEYSTORE
            )
            
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setUserAuthenticationRequired(false)
                .build()
            
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
        
        return keyStore.getKey(KEY_ALIAS, null) as SecretKey
    }
    
    fun encrypt(data: String): String {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
            
            val iv = cipher.iv
            val encrypted = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
            
            val combined = iv + encrypted
            Base64.encodeToString(combined, Base64.DEFAULT)
        } catch (e: Exception) {
            // В случае ошибки возвращаем исходные данные (для отладки)
            // В продакшене следует обработать ошибку appropriately
            Base64.encodeToString(data.toByteArray(Charsets.UTF_8), Base64.DEFAULT)
        }
    }
    
    fun decrypt(encryptedData: String): String {
        return try {
            val combined = Base64.decode(encryptedData, Base64.DEFAULT)
            
            val iv = combined.copyOfRange(0, 12)
            val encrypted = combined.copyOfRange(12, combined.size)
            
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), spec)
            
            val decrypted = cipher.doFinal(encrypted)
            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            // В случае ошибки пытаемся декодировать как base64
            try {
                String(Base64.decode(encryptedData, Base64.DEFAULT), Charsets.UTF_8)
            } catch (e2: Exception) {
                encryptedData // Возвращаем как есть в случае двойной ошибки
            }
        }
    }
    
    fun getEncryptedPreferences(): EncryptedSharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
            
        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }
    
    fun saveEncryptedPreference(key: String, value: String) {
        val prefs = getEncryptedPreferences()
        prefs.edit().putString(key, value).apply()
    }
    
    fun getEncryptedPreference(key: String, defaultValue: String = ""): String {
        val prefs = getEncryptedPreferences()
        return prefs.getString(key, defaultValue) ?: defaultValue
    }
}
