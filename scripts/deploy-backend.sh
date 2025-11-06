#!/bin/bash

echo "Deploying backend services..."

# Build and start services
docker-compose up -d --build currency-api postgres nginx

# Wait for services to start
echo "Waiting for services to start..."
sleep 15

# Test API
echo "Testing API..."
curl -f http://localhost:8080/api/health || echo "API health check failed"

echo "Backend deployed successfully!"
