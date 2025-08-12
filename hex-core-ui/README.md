# Hex Core UI

## Описание

Модуль для UI тестирования, построенный на основе Selenide. Содержит базовые классы и обертки для работы с веб-элементами.

## Функциональность

### Ядро (core)
- BaseElement - базовый класс для веб-элементов
- BaseComponent - базовый класс для компонентов страниц
- Утилиты для работы с Selenide

### Обертки (wrappers)
- Button - обертка для кнопок
- Input - обертка для полей ввода
- Select - обертка для выпадающих списков
- Другие UI элементы

## Зависимости

- hex-core (базовый модуль)
- Selenide 7.3.2
- Selenium WebDriver

## Использование

```xml
<dependency>
    <groupId>com.company.hex</groupId>
    <artifactId>hex-core-ui</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Примеры

```java
// Пример будет добавлен после реализации базовых классов
```

Подробные примеры использования находятся в модуле `hex-project-samples`.