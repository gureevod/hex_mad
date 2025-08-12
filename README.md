# Hex Automation Framework

## Описание

Hex Automation Framework - это модульная Java-фреймворк для автоматизации тестирования, построенная на основе Maven и использующая Google Guice для внедрения зависимостей.

## Структура проекта

Проект состоит из следующих модулей:

- **hex-core** - Базовый модуль с общими абстракциями
- **hex-core-api** - Модуль для API тестирования с RestAssured
- **hex-core-ui** - Модуль для UI тестирования с Selenide
- **hex-core-testing** - Модуль с утилитами для тестирования (JUnit 5, Allure)
- **hex-project-samples** - Примеры использования фреймворка

## Требования

- Java 17 (LTS)
- Maven 3.9.6+

## Сборка проекта

```bash
mvn clean install
```

## Использование

Подключите необходимые модули в ваш проект:

```xml
<dependency>
    <groupId>com.company.hex</groupId>
    <artifactId>hex-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Документация

Подробная документация находится в директории `docs/`.

## Лицензия

Внутренний проект компании.