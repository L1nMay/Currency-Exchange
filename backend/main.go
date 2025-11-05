package main

import (
	"currency-converter-backend/api"
	"currency-converter-backend/config"
	"currency-converter-backend/services"
	"log"
	"os"

	"github.com/gin-gonic/gin"
)

func main() {
	// Загрузка конфигурации
	cfg := config.LoadConfig()

	// Инициализация сервисов
	cryptoService := services.NewCryptoService()
	currencyService := services.NewCurrencyService(cryptoService)

	// Настройка Gin
	if os.Getenv("GIN_MODE") == "release" {
		gin.SetMode(gin.ReleaseMode)
	}

	router := gin.Default()

	// Middleware
	router.Use(api.CORSMiddleware())
	router.Use(api.SecurityMiddleware())

	// API routes
	api.SetupRoutes(router, currencyService)

	// Запуск сервера
	log.Printf("🚀 Currency Converter API starting on port %s", cfg.Port)
	if err := router.Run(":" + cfg.Port); err != nil {
		log.Fatal("Failed to start server:", err)
	}
}
