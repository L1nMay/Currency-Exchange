#!/bin/bash

echo "Testing Currency Converter API..."

# Test health endpoint
echo "1. Testing health endpoint:"
curl -s http://localhost:8080/api/health | jq .

# Test rates endpoint
echo "2. Testing rates endpoint:"
curl -s "http://localhost:8080/api/rates?base=USD" | jq .

# Test conversion endpoint
echo "3. Testing conversion endpoint:"
curl -s -X POST http://localhost:8080/api/convert \
  -H "Content-Type: application/json" \
  -d '{"amount": 100, "from": "USD", "to": "EUR"}' | jq .

echo "API tests completed!"
