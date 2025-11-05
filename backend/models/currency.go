package models

import (
	"time"
)

type ExchangeRates struct {
	Base          string             `json:"base"`
	Date          string             `json:"date"`
	Rates         map[string]float64 `json:"rates"`
	EncryptedRates map[string]string `json:"encrypted_rates,omitempty"`
	LastUpdated   time.Time          `json:"last_updated"`
}

type ConversionRequest struct {
	Amount float64 `json:"amount" binding:"required,gt=0"`
	From   string  `json:"from" binding:"required,len=3"`
	To     string  `json:"to" binding:"required,len=3"`
}

type ConversionResult struct {
	From      string    `json:"from"`
	To        string    `json:"to"`
	Amount    float64   `json:"amount"`
	Converted float64   `json:"converted"`
	Rate      float64   `json:"rate"`
	Timestamp time.Time `json:"timestamp"`
	Hash      string    `json:"hash,omitempty"`
}

type APIResponse struct {
	Success bool        `json:"success"`
	Data    interface{} `json:"data,omitempty"`
	Error   string      `json:"error,omitempty"`
	Message string      `json:"message,omitempty"`
}

type Currency struct {
	Code  string `json:"code" gorm:"primaryKey"`
	Name  string `json:"name"`
	Symbol string `json:"symbol"`
}
