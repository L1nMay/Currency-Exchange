package main

import (
	"currency-converter-backend/api"
	"currency-converter-backend/config"
	"currency-converter-backend/services"
	"log"
)

func main() {
	// Загрузка конфигурации
	cfg := config.LoadConfig()

	// Инициализация сервисов
	cryptoService := services.NewCryptoService()
	currencyService := services.NewCurrencyService(cryptoService)

	// Настройка роутера
	router := api.SetupRouter(currencyService)

	// Запуск сервера
	log.Printf("🚀 Currency Converter API starting on port %s", cfg.Port)
	if err := router.Run(":" + cfg.Port); err != nil {
		log.Fatal("Failed to start server:", err)
	}
}
