package services

import (
	"currency-converter-backend/models"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"time"
)

type CurrencyService struct {
	cryptoService *CryptoService
	cache         map[string]*models.ExchangeRates
}

func NewCurrencyService(cryptoService *CryptoService) *CurrencyService {
	return &CurrencyService{
		cryptoService: cryptoService,
		cache:         make(map[string]*models.ExchangeRates),
	}
}

func (cs *CurrencyService) GetExchangeRates(baseCurrency string) (*models.ExchangeRates, error) {
	// Проверка кэша
	if cached, exists := cs.cache[baseCurrency]; exists {
		if time.Since(cached.LastUpdated).Minutes() < 5 {
			return cached, nil
		}
	}

	// Получение данных из внешнего API
	rates, err := cs.fetchFromExternalAPI(baseCurrency)
	if err != nil {
		return nil, err
	}

	// Шифрование чувствительных данных
	encryptedRates := make(map[string]string)
	for currency, rate := range rates.Rates {
		encryptedRate, err := cs.cryptoService.Encrypt(fmt.Sprintf("%f", rate))
		if err == nil {
			encryptedRates[currency] = encryptedRate
		}
	}

	response := &models.ExchangeRates{
		Base:          baseCurrency,
		Date:          time.Now().Format("2006-01-02"),
		Rates:         rates.Rates,
		EncryptedRates: encryptedRates,
		LastUpdated:   time.Now(),
	}

	// Сохранение в кэш
	cs.cache[baseCurrency] = response

	return response, nil
}

func (cs *CurrencyService) fetchFromExternalAPI(baseCurrency string) (*models.ExchangeRates, error) {
	// Использование Frankfurter API как основного источника
	url := fmt.Sprintf("https://api.frankfurter.app/latest?from=%s", baseCurrency)
	
	client := &http.Client{Timeout: 10 * time.Second}
	resp, err := client.Get(url)
	if err != nil {
		return nil, fmt.Errorf("failed to fetch from Frankfurter API: %v", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("API returned status: %d", resp.StatusCode)
	}

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response body: %v", err)
	}

	var apiResponse struct {
		Base  string             `json:"base"`
		Date  string             `json:"date"`
		Rates map[string]float64 `json:"rates"`
	}

	if err := json.Unmarshal(body, &apiResponse); err != nil {
		return nil, fmt.Errorf("failed to parse JSON response: %v", err)
	}

	return &models.ExchangeRates{
		Base:        apiResponse.Base,
		Date:        apiResponse.Date,
		Rates:       apiResponse.Rates,
		LastUpdated: time.Now(),
	}, nil
}

func (cs *CurrencyService) ConvertCurrency(amount float64, from string, to string) (*models.ConversionResult, error) {
	rates, err := cs.GetExchangeRates(from)
	if err != nil {
		return nil, err
	}

	rate, exists := rates.Rates[to]
	if !exists {
		return nil, fmt.Errorf("currency not supported: %s", to)
	}

	converted := amount * rate

	// Создание хэша для верификации
	conversionHash := cs.cryptoService.HashData(
		fmt.Sprintf("%.2f%s%s%.2f%d", amount, from, to, converted, time.Now().Unix()),
	)

	return &models.ConversionResult{
		From:      from,
		To:        to,
		Amount:    amount,
		Converted: converted,
		Rate:      rate,
		Timestamp: time.Now(),
		Hash:      conversionHash,
	}, nil
}

func (cs *CurrencyService) GetCacheStats() map[string]interface{} {
	stats := make(map[string]interface{})
	stats["cache_size"] = len(cs.cache)

	var keys []string
	for k := range cs.cache {
		keys = append(keys, k)
	}
	stats["cached_currencies"] = keys

	return stats
}
