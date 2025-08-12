# Hex Project Samples

## Описание

Примеры использования Hex Automation Framework. Демонстрирует интеграцию всех модулей фреймворка в реальном проекте.

## Структура проекта

### API (api)
- **dto/** - Объекты передачи данных для API
- **services/** - Сервисы для работы с API

### Страницы (pages)
- Page Object классы для UI тестирования

### Компоненты (components)
- Переиспользуемые UI компоненты

### Конфигурация (config)
- Настройки проекта и окружений

### Тесты (tests)
- **api/** - API тесты
- **ui/** - UI тесты

## Зависимости

- hex-core (базовые абстракции)
- hex-core-api (API тестирование)
- hex-core-ui (UI тестирование)
- hex-core-testing (утилиты тестирования)

## Использование

```bash
# Запуск всех тестов
mvn test

# Запуск только API тестов
mvn test -Dtest="**/*ApiTest"

# Запуск только UI тестов
mvn test -Dtest="**/*UiTest"
```

## Отчеты

После выполнения тестов отчеты Allure будут доступны в `target/allure-results/`.

Для генерации HTML отчета:
```bash
allure serve target/allure-results