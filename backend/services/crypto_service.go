package services

import (
	"crypto/aes"
	"crypto/cipher"
	"crypto/rand"
	"crypto/sha256"
	"encoding/hex"
	"errors"
	"io"

	"golang.org/x/crypto/pbkdf2"
)

type CryptoService struct {
	key []byte
}

func NewCryptoService() *CryptoService {
	// В продакшене ключ должен храниться в безопасном месте (Vault, KMS, etc)
	salt := []byte("currency-converter-salt")
	password := []byte("secure-password-12345")
	key := pbkdf2.Key(password, salt, 4096, 32, sha256.New)
	
	return &CryptoService{key: key}
}

func (cs *CryptoService) Encrypt(plaintext string) (string, error) {
	block, err := aes.NewCipher(cs.key)
	if err != nil {
		return "", err
	}

	gcm, err := cipher.NewGCM(block)
	if err != nil {
		return "", err
	}

	nonce := make([]byte, gcm.NonceSize())
	if _, err = io.ReadFull(rand.Reader, nonce); err != nil {
		return "", err
	}

	ciphertext := gcm.Seal(nonce, nonce, []byte(plaintext), nil)
	return hex.EncodeToString(ciphertext), nil
}

func (cs *CryptoService) Decrypt(encrypted string) (string, error) {
	data, err := hex.DecodeString(encrypted)
	if err != nil {
		return "", err
	}

	block, err := aes.NewCipher(cs.key)
	if err != nil {
		return "", err
	}

	gcm, err := cipher.NewGCM(block)
	if err != nil {
		return "", err
	}

	if len(data) < gcm.NonceSize() {
		return "", errors.New("malformed ciphertext")
	}

	nonce, ciphertext := data[:gcm.NonceSize()], data[gcm.NonceSize():]
	plaintext, err := gcm.Open(nil, nonce, ciphertext, nil)
	if err != nil {
		return "", err
	}

	return string(plaintext), nil
}

func (cs *CryptoService) HashData(data string) string {
	hash := sha256.Sum256([]byte(data))
	return hex.EncodeToString(hash[:])
}

func (cs *CryptoService) ValidateHash(data string, expectedHash string) bool {
	return cs.HashData(data) == expectedHash
}
