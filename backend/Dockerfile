FROM golang:1.21-alpine

# Установка зависимостей для сборки
RUN apk add --no-cache git

WORKDIR /app

# Копирование файлов зависимостей
COPY go.mod go.sum ./
RUN go mod download

# Копирование исходного кода
COPY . .

# Сборка приложения
RUN go build -o main .

EXPOSE 8080

CMD ["./main"]
