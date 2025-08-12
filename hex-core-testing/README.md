# Hex Core Testing

## Описание

Модуль с утилитами для тестирования, включающий интеграцию с JUnit 5 и Allure Framework для создания отчетов.

## Функциональность

### Allure (allure)
- Интеграция с Allure Framework
- Утилиты для создания отчетов
- Помощники для аннотаций и степов

### Жизненный цикл (lifecycle)
- BaseTest - базовый класс для тестов
- JUnit 5 расширения
- Утилиты для настройки тестового окружения

## Зависимости

- hex-core (базовый модуль)
- JUnit 5 (5.10.2)
- Allure Framework (2.27.0)
- AssertJ (3.25.3)

## Использование

```xml
<dependency>
    <groupId>com.company.hex</groupId>
    <artifactId>hex-core-testing</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

## Примеры

```java
// Пример будет добавлен после реализации базовых классов
```

Подробные примеры использования находятся в модуле `hex-project-samples`.