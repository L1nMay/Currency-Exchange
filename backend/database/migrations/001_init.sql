-- Инициализация базы данных для Currency Converter

CREATE TABLE IF NOT EXISTS currencies (
    code VARCHAR(3) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    symbol VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Вставка основных валют
INSERT INTO currencies (code, name, symbol) VALUES
('USD', 'US Dollar', '$'),
('EUR', 'Euro', '€'),
('GBP', 'British Pound', '£'),
('JPY', 'Japanese Yen', '¥'),
('CAD', 'Canadian Dollar', 'C$'),
('AUD', 'Australian Dollar', 'A$'),
('CHF', 'Swiss Franc', 'CHF'),
('CNY', 'Chinese Yuan', '¥'),
('RUB', 'Russian Ruble', '₽'),
('INR', 'Indian Rupee', '₹'),
('BRL', 'Brazilian Real', 'R$'),
('MXN', 'Mexican Peso', '$'),
('KRW', 'South Korean Won', '₩'),
('SGD', 'Singapore Dollar', 'S$'),
('NZD', 'New Zealand Dollar', 'NZ$'),
('TRY', 'Turkish Lira', '₺')
ON CONFLICT (code) DO NOTHING;

-- Создание индексов
CREATE INDEX IF NOT EXISTS idx_currencies_code ON currencies(code);
