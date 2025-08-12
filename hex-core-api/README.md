# Hex Core API

## Описание

Модуль для API тестирования, построенный на основе RestAssured. Содержит базовые классы и утилиты для работы с REST API.

## Функциональность

### DTO (dto)
- Базовые интерфейсы для объектов передачи данных
- Общие абстракции для API моделей

### Сервисы (service)
- BaseApiService для работы с API
- Утилиты RestAssured
- Помощники для HTTP запросов

## Зависимости

- hex-core (базовый модуль)
- RestAssured 5.4.0
- Jackson для JSON обработки

## Использование

```xml
<dependency>
    <groupId>com.company.hex</groupId>
    <artifactId>hex-core-api</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Примеры

```java
// Пример будет добавлен после реализации базовых классов
```

Подробные примеры использования находятся в модуле `hex-project-samples`.