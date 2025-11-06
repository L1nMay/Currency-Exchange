#!/bin/bash

echo "Deploying to production environment..."

# Build production Docker images
echo "1. Building production images..."
docker-compose -f docker-compose.prod.yml build

# Run database migrations
echo "2. Running database migrations..."
docker-compose -f docker-compose.prod.yml run --rm backend ./migrate

# Deploy services
echo "3. Deploying services..."
docker-compose -f docker-compose.prod.yml up -d

# Health check
echo "4. Performing health check..."
sleep 10
curl -f https://your-domain.com/api/health || echo "Health check failed"

echo "Production deployment completed!"
