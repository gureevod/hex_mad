# Hex Core

## Описание

Базовый модуль Hex Automation Framework, содержащий общие абстракции и утилиты для всех остальных модулей.

## Функциональность

### Конфигурация (config)
- Интерфейсы для работы с конфигурацией через Owner
- Фабрика конфигураций

### Утилиты (utils)
- Общие утилиты для работы со строками
- Утилиты для рефлексии
- Вспомогательные методы

## Зависимости
- Owner 1.0.12
- SLF4J API 2.0.13
- Logback Classic 1.5.6

## Использование

```xml
<dependency>
    <groupId>com.company.hex</groupId>
    <artifactId>hex-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Примеры

Подробные примеры использования находятся в модуле `hex-project-samples`.