package api

import (
	"currency-converter-backend/models"
	"currency-converter-backend/services"
	"net/http"
	"time"

	"github.com/gin-gonic/gin"
)

type CurrencyHandler struct {
	currencyService *services.CurrencyService
}

func NewCurrencyHandler(currencyService *services.CurrencyService) *CurrencyHandler {
	return &CurrencyHandler{
		currencyService: currencyService,
	}
}

func SetupRoutes(router *gin.Engine, currencyService *services.CurrencyService) {
	handler := NewCurrencyHandler(currencyService)

	// Health check
	router.GET("/api/health", handler.HealthCheck)

	// Currency routes
	api := router.Group("/api")
	{
		api.GET("/rates", handler.GetExchangeRates)
		api.POST("/convert", handler.ConvertCurrency)
		api.GET("/currencies", handler.GetSupportedCurrencies)
		api.GET("/rates/:base", handler.GetRatesByBase)
	}
}

func (h *CurrencyHandler) HealthCheck(c *gin.Context) {
	c.JSON(http.StatusOK, models.HealthResponse{
		Status:    "healthy",
		Timestamp: time.Now().Format(time.RFC3339),
		Version:   "1.0.0",
	})
}

func (h *CurrencyHandler) GetExchangeRates(c *gin.Context) {
	baseCurrency := c.DefaultQuery("base", "USD")
	
	rates, err := h.currencyService.GetExchangeRates(baseCurrency)
	if err != nil {
		c.JSON(http.StatusInternalServerError, models.APIResponse{
			Success: false,
			Error:   "Failed to fetch exchange rates",
		})
		return
	}

	c.JSON(http.StatusOK, models.APIResponse{
		Success: true,
		Data:    rates,
	})
}

func (h *CurrencyHandler) ConvertCurrency(c *gin.Context) {
	var req models.ConversionRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, models.APIResponse{
			Success: false,
			Error:   "Invalid request format",
		})
		return
	}

	result, err := h.currencyService.ConvertCurrency(req.Amount, req.From, req.To)
	if err != nil {
		c.JSON(http.StatusInternalServerError, models.APIResponse{
			Success: false,
			Error:   err.Error(),
		})
		return
	}

	c.JSON(http.StatusOK, models.APIResponse{
		Success: true,
		Data:    result,
	})
}

func (h *CurrencyHandler) GetSupportedCurrencies(c *gin.Context) {
	currencies := []string{
		"USD", "EUR", "GBP", "JPY", "CAD", "AUD", "CHF", "CNY",
		"RUB", "INR", "BRL", "MXN", "KRW", "SGD", "NZD", "TRY",
		"SEK", "NOK", "DKK", "PLN", "HUF", "CZK", "RON", "BGN",
	}

	c.JSON(http.StatusOK, models.APIResponse{
		Success: true,
		Data:    currencies,
	})
}

func (h *CurrencyHandler) GetRatesByBase(c *gin.Context) {
	baseCurrency := c.Param("base")

	rates, err := h.currencyService.GetExchangeRates(baseCurrency)
	if err != nil {
		c.JSON(http.StatusInternalServerError, models.APIResponse{
			Success: false,
			Error:   "Failed to fetch exchange rates",
		})
		return
	}

	c.JSON(http.StatusOK, models.APIResponse{
		Success: true,
		Data:    rates,
	})
}
