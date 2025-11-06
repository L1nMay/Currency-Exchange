package com.example.currencyconverter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.currencyconverter.data.model.ConversionHistory
import com.example.currencyconverter.data.repository.CurrencyRepository
import com.example.currencyconverter.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CurrencyConverterViewModel @Inject constructor(
    private val repository: CurrencyRepository
) : ViewModel() {
    
    private val _amount = MutableStateFlow("1.0")
    private val _fromCurrency = MutableStateFlow("USD")
    private val _toCurrency = MutableStateFlow("EUR")
    private val _convertedAmount = MutableStateFlow(0.0)
    private val _conversionRate = MutableStateFlow(0.0)
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _lastUpdate = MutableStateFlow("")
    private val _supportedCurrencies = MutableStateFlow<List<String>>(emptyList())
    
    val amount: StateFlow<String> = _amount.asStateFlow()
    val fromCurrency: StateFlow<String> = _fromCurrency.asStateFlow()
    val toCurrency: StateFlow<String> = _toCurrency.asStateFlow()
    val convertedAmount: StateFlow<Double> = _convertedAmount.asStateFlow()
    val conversionRate: StateFlow<Double> = _conversionRate.asStateFlow()
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    val lastUpdate: StateFlow<String> = _lastUpdate.asStateFlow()
    val supportedCurrencies: StateFlow<List<String>> = _supportedCurrencies.asStateFlow()
    
    // Combine flows for rates and history
    val exchangeRates = repository.getLatestRates()
    val conversionHistory = repository.getConversionHistory()
    
    init {
        loadExchangeRates()
        loadSupportedCurrencies()
        setupConversionListener()
    }
    
    private fun setupConversionListener() {
        viewModelScope.launch {
            combine(
                _amount,
                _fromCurrency,
                _toCurrency,
                exchangeRates
            ) { amount, from, to, ratesResponse ->
                convertAmount(amount, from, to, ratesResponse?.rates ?: emptyMap())
            }.collect { }
        }
    }
    
    fun loadExchangeRates(baseCurrency: String = "USD") {
        viewModelScope.launch {
            repository.fetchLatestRates(baseCurrency).collect { result ->
                when (result) {
                    is Result.Loading -> _isLoading.value = true
                    is Result.Success -> {
                        _isLoading.value = false
                        _errorMessage.value = null
                        _lastUpdate.value = "Last update: ${result.data.date}"
                        // Trigger conversion with new rates
                        convertAmount(_amount.value, _fromCurrency.value, _toCurrency.value, result.data.rates)
                        Timber.d("Rates loaded successfully for ${result.data.baseCurrency}")
                    }
                    is Result.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = result.message
                        Timber.e("Failed to load rates: ${result.message}")
                    }
                }
            }
        }
    }
    
    private fun loadSupportedCurrencies() {
        viewModelScope.launch {
            try {
                val currencies = repository.getSupportedCurrencies()
                _supportedCurrencies.value = currencies
                Timber.d("Loaded ${currencies.size} supported currencies")
            } catch (e: Exception) {
                Timber.e("Failed to load supported currencies: ${e.message}")
                // Use default currencies as fallback
                _supportedCurrencies.value = listOf("USD", "EUR", "GBP", "JPY", "CAD", "AUD", "CHF", "CNY")
            }
        }
    }
    
    private fun convertAmount(
        amountStr: String,
        fromCurrency: String,
        toCurrency: String,
        rates: Map<String, Double>
    ) {
        viewModelScope.launch {
            try {
                val amountValue = amountStr.toDoubleOrNull() ?: 0.0
                val converted = repository.convertCurrency(amountValue, fromCurrency, toCurrency, rates)
                
                _convertedAmount.value = converted ?: 0.0
                
                // Calculate conversion rate
                val fromRate = rates[fromCurrency] ?: 1.0
                val toRate = rates[toCurrency] ?: 1.0
                _conversionRate.value = toRate / fromRate
                
                Timber.d("Converted $amountValue $fromCurrency to $converted $toCurrency")
                
            } catch (e: Exception) {
                _errorMessage.value = "Conversion error: ${e.message}"
                Timber.e("Conversion error: ${e.message}")
            }
        }
    }
    
    fun setAmount(amount: String) {
        _amount.value = amount
    }
    
    fun setFromCurrency(currency: String) {
        _fromCurrency.value = currency
    }
    
    fun setToCurrency(currency: String) {
        _toCurrency.value = currency
    }
    
    fun swapCurrencies() {
        val currentFrom = _fromCurrency.value
        _fromCurrency.value = _toCurrency.value
        _toCurrency.value = currentFrom
    }
    
    fun saveConversionToHistory() {
        viewModelScope.launch {
            try {
                val amountValue = _amount.value.toDoubleOrNull() ?: return@launch
                if (amountValue <= 0) return@launch
                
                val history = ConversionHistory(
                    amountFrom = amountValue,
                    currencyFrom = _fromCurrency.value,
                    amountTo = _convertedAmount.value,
                    currencyTo = _toCurrency.value,
                    rate = _conversionRate.value
                )
                repository.saveConversion(history)
                Timber.d("Saved conversion to history: $history")
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save history: ${e.message}"
                Timber.e("Failed to save history: ${e.message}")
            }
        }
    }
    
    fun deleteHistoryItem(history: ConversionHistory) {
        viewModelScope.launch {
            try {
                repository.deleteHistory(history)
                Timber.d("Deleted history item: ${history.id}")
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete history: ${e.message}"
                Timber.e("Failed to delete history: ${e.message}")
            }
        }
    }
    
    fun clearAllHistory() {
        viewModelScope.launch {
            try {
                repository.clearAllHistory()
                Timber.d("Cleared all history")
            } catch (e: Exception) {
                _errorMessage.value = "Failed to clear history: ${e.message}"
                Timber.e("Failed to clear history: ${e.message}")
            }
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    fun refreshData() {
        loadExchangeRates(_fromCurrency.value)
        loadSupportedCurrencies()
    }
}
