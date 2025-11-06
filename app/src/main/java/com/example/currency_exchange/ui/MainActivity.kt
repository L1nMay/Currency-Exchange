package com.example.currencyconverter.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.example.currencyconverter.databinding.ActivityMainBinding
import com.example.currencyconverter.ui.viewmodel.CurrencyConverterViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val viewModel: CurrencyConverterViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        setupObservers()
        
        Timber.d("MainActivity created")
    }
    
    private fun setupUI() {
        // Setup currency spinners with initial data
        setupCurrencySpinners()
        
        // Setup amount input
        binding.etAmount.addTextChangedListener { editable ->
            viewModel.setAmount(editable?.toString() ?: "0.0")
        }
        
        // Setup swap button
        binding.btnSwap.setOnClickListener {
            viewModel.swapCurrencies()
            updateCurrencySelections()
        }
        
        // Setup save button
        binding.btnSave.setOnClickListener {
            viewModel.saveConversionToHistory()
        }
        
        // Setup refresh button
        binding.btnRefresh.setOnClickListener {
            viewModel.refreshData()
        }
        
        // Setup clear error button
        binding.btnClearError.setOnClickListener {
            viewModel.clearError()
        }
    }
    
    private fun setupCurrencySpinners() {
        val initialCurrencies = listOf("USD", "EUR", "GBP", "JPY", "CAD", "AUD", "CHF", "CNY")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, initialCurrencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        
        binding.spinnerFromCurrency.adapter = adapter
        binding.spinnerToCurrency.adapter = adapter
        
        // Set default currencies
        binding.spinnerFromCurrency.setSelection(initialCurrencies.indexOf("USD"))
        binding.spinnerToCurrency.setSelection(initialCurrencies.indexOf("EUR"))
        
        // Setup listeners
        binding.spinnerFromCurrency.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selected = parent?.getItemAtPosition(position) as? String
                selected?.let {
                    viewModel.setFromCurrency(it)
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
        
        binding.spinnerToCurrency.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selected = parent?.getItemAtPosition(position) as? String
                selected?.let {
                    viewModel.setToCurrency(it)
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }
    
    private fun updateCurrencySelections() {
        val fromCurrency = viewModel.fromCurrency.value
        val toCurrency = viewModel.toCurrency.value
        
        val fromPosition = (binding.spinnerFromCurrency.adapter as? ArrayAdapter<*>)?.getPosition(fromCurrency) ?: 0
        val toPosition = (binding.spinnerToCurrency.adapter as? ArrayAdapter<*>)?.getPosition(toCurrency) ?: 0
        
        binding.spinnerFromCurrency.setSelection(fromPosition)
        binding.spinnerToCurrency.setSelection(toPosition)
    }
    
    private fun setupObservers() {
        // Observe converted amount
        viewModel.convertedAmount.observe(this) { amount ->
            binding.tvConvertedAmount.text = String.format("%.2f", amount)
        }
        
        // Observe conversion rate
        viewModel.conversionRate.observe(this) { rate ->
            binding.tvConversionRate.text = String.format(
                "1 %s = %.4f %s", 
                viewModel.fromCurrency.value, 
                rate, 
                viewModel.toCurrency.value
            )
        }
        
        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.isVisible = isLoading
            binding.btnRefresh.isEnabled = !isLoading
            binding.btnRefresh.text = if (isLoading) "Refreshing..." else "Refresh Rates"
        }
        
        // Observe error messages
        viewModel.errorMessage.observe(this) { error ->
            binding.tvError.isVisible = error != null
            binding.tvError.text = error
            binding.btnClearError.isVisible = error != null
        }
        
        // Observe last update
        viewModel.lastUpdate.observe(this) { update ->
            binding.tvLastUpdate.text = update
        }
        
        // Observe supported currencies and update spinners
        viewModel.supportedCurrencies.observe(this) { currencies ->
            if (currencies.isNotEmpty()) {
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                
                binding.spinnerFromCurrency.adapter = adapter
                binding.spinnerToCurrency.adapter = adapter
                
                updateCurrencySelections()
            }
        }
        
        // Observe exchange rates for debugging
        viewModel.exchangeRates.observe(this) { rates ->
            rates?.let {
                Timber.d("Rates updated: ${it.baseCurrency}, ${it.rates.size} currencies")
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh data when activity resumes
        viewModel.refreshData()
    }
}
