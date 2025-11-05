.PHONY: build-backend build-android deploy test clean

build-backend:
	@echo "Building backend services..."
	docker-compose build currency-api

build-android:
	@echo "Building Android application..."
	docker-compose run --rm android-builder ./gradlew assembleDebug

deploy:
	@echo "Deploying all services..."
	docker-compose up -d

test:
	@echo "Running tests..."
	docker-compose run --rm android-builder ./gradlew test
	curl http://localhost:8080/api/health

clean:
	@echo "Cleaning up..."
	docker-compose down -v
	docker system prune -f

logs:
	docker-compose logs -f

stop:
	docker-compose down
