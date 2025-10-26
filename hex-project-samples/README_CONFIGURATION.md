# Конфигурация проекта Hex

Этот документ описывает систему конфигурации фреймворка Hex и способы её использования.

## Обзор

Фреймворк Hex использует библиотеку [Owner](http://owner.aeonbits.org/) для управления конфигурацией. Система поддерживает:

- ✅ Множественные профили окружений (local, ci, qa, prod)
- ✅ Приоритетную загрузку из разных источников
- ✅ Переопределение через системные свойства и переменные окружения
- ✅ Типобезопасный доступ к настройкам
- ✅ Библиотечные дефолты для всех модулей

## Архитектура конфигурации

### Интерфейсы конфигурации

Фреймворк предоставляет три основных интерфейса конфигурации:

1. **BaseConfig** (`hex-core`) - базовые настройки (логирование, таймауты, параллелизм)
2. **UiConfig** (`hex-core-ui`) - настройки UI тестирования (браузер, Selenium, скриншоты)
3. **ApiConfig** (`hex-core-api`) - настройки API тестирования (endpoints, аутентификация, retry)

### Приоритет источников

Конфигурация загружается из следующих источников (от низшего к высшему приоритету):

1. `classpath:META-INF/hex-defaults.properties` - библиотечные дефолты
2. `classpath:hex.properties` - дефолты проекта
3. `classpath:hex-${hex.environment}.properties` - профиль окружения
4. `classpath:hex-${env}.properties` - обратная совместимость
5. `system:env` - переменные окружения
6. `system:properties` - системные свойства Java

## Использование

### Прямое использование Owner API

```java
import org.aeonbits.owner.ConfigFactory;
import com.company.hex.ui.config.UiConfig;
import com.company.hex.api.config.ApiConfig;

// Создание конфигурации
UiConfig uiConfig = ConfigFactory.create(UiConfig.class);
ApiConfig apiConfig = ConfigFactory.create(ApiConfig.class);

// Использование
String browser = uiConfig.browser();
String apiUrl = apiConfig.baseUrl();
```

### Использование фасада HexConfigs (рекомендуется)

Проект `hex-project-samples` содержит пример фасада для удобного доступа:

```java
import com.company.hex.project.config.HexConfigs;

// Получение глобальных конфигураций (с кэшированием)
UiConfig ui = HexConfigs.ui();
ApiConfig api = HexConfigs.api();
BaseConfig base = HexConfigs.base();

// Точечные переопределения для конкретного теста
UiConfig headlessUi = HexConfigs.ui(Map.of("hex.ui.headless", "true"));
ApiConfig customApi = HexConfigs.api(Map.of("hex.api.timeout", "60"));

// Установка профиля программно
HexConfigs.useProfile("ci");

// Сброс кэша (для тестов)
HexConfigs.reset();
```

## Файлы конфигурации

### hex.properties (дефолты проекта)

Основной файл конфигурации для локальной разработки:

```properties
# Профиль окружения
hex.environment=local

# UI настройки
hex.ui.browser=chrome
hex.ui.headless=false
hex.ui.base.url=http://localhost:3000

# API настройки
hex.api.base.url=http://localhost:8080
hex.api.timeout=30
```

### hex-ci.properties (CI окружение)

Настройки для CI/CD пайплайна:

```properties
# UI настройки для CI
hex.ui.browser=chrome
hex.ui.headless=true
hex.ui.grid.url=http://selenium-hub:4444/wd/hub

# API настройки для CI
hex.api.base.url=http://test-api:8080
hex.api.timeout=60
```

### Создание дополнительных профилей

Вы можете создать любые профили, например:

- `hex-qa.properties` - для QA окружения
- `hex-staging.properties` - для staging
- `hex-prod.properties` - для production (если нужно)

## Переключение профилей

### Через Maven

```bash
# Локальный запуск
mvn test -Dhex.environment=local

# CI запуск
mvn test -Dhex.environment=ci

# QA окружение
mvn test -Dhex.environment=qa
```

### Через переменные окружения

```bash
# Linux/Mac
export HEX_ENVIRONMENT=ci
mvn test

# Windows
set HEX_ENVIRONMENT=ci
mvn test
```

### Программно в коде

```java
// В @BeforeAll или статическом блоке
@BeforeAll
static void setup() {
    HexConfigs.useProfile("ci");
}
```

## Переопределение отдельных параметров

### Через системные свойства

```bash
mvn test -Dhex.ui.browser=firefox -Dhex.ui.headless=true
```

### Через переменные окружения

```bash
export HEX_UI_BROWSER=firefox
export HEX_UI_HEADLESS=true
mvn test
```

### Программно для конкретного теста

```java
@Test
void testWithCustomConfig() {
    // Создаём конфиг с переопределениями только для этого теста
    UiConfig config = HexConfigs.ui(
        Map.of(
            "hex.ui.headless", "true",
            "hex.ui.browser", "firefox"
        )
    );
    
    // Используем кастомный конфиг
    WebDriver driver = WebDriverFactory.create(config);
    // ...
}
```

## Примеры использования

### Пример 1: Базовое использование в тесте

```java
@Test
void simpleTest() {
    UiConfig config = HexConfigs.ui();
    
    // Открываем браузер с настройками из конфига
    WebDriver driver = WebDriverFactory.create(config);
    driver.get(config.baseUrl());
    
    // Тест...
}
```

### Пример 2: Разные конфиги для разных тестов

```java
@Test
void testInChrome() {
    UiConfig config = HexConfigs.ui(Map.of("hex.ui.browser", "chrome"));
    // Тест в Chrome
}

@Test
void testInFirefox() {
    UiConfig config = HexConfigs.ui(Map.of("hex.ui.browser", "firefox"));
    // Тест в Firefox
}
```

### Пример 3: API тестирование с разными окружениями

```java
@Test
void testAgainstLocalApi() {
    ApiConfig config = HexConfigs.api(Map.of("hex.api.base.url", "http://localhost:8080"));
    // API тест против локального сервера
}

@Test
void testAgainstStagingApi() {
    ApiConfig config = HexConfigs.api(Map.of("hex.api.base.url", "https://api-staging.example.com"));
    // API тест против staging
}
```

## Создание собственного фасада

Вы можете создать свой фасад конфигурации в своём проекте:

```java
package com.mycompany.myproject.config;

import org.aeonbits.owner.ConfigFactory;
import com.company.hex.ui.config.UiConfig;
import com.company.hex.api.config.ApiConfig;

public final class MyProjectConfig {
    
    private static volatile UiConfig UI;
    private static volatile ApiConfig API;
    
    public static UiConfig ui() {
        if (UI == null) {
            synchronized (MyProjectConfig.class) {
                if (UI == null) {
                    UI = ConfigFactory.create(UiConfig.class);
                }
            }
        }
        return UI;
    }
    
    public static ApiConfig api() {
        if (API == null) {
            synchronized (MyProjectConfig.class) {
                if (API == null) {
                    API = ConfigFactory.create(ApiConfig.class);
                }
            }
        }
        return API;
    }
    
    // Добавьте свои методы...
}
```

## Доступные настройки

### Базовые настройки (BaseConfig)

| Ключ | Тип | Дефолт | Описание |
|------|-----|--------|----------|
| `hex.environment` | String | `local` | Профиль окружения |
| `hex.logging.level` | String | `INFO` | Уровень логирования |
| `hex.timeout.default` | int | `30` | Таймаут по умолчанию (сек) |
| `hex.debug.enabled` | boolean | `false` | Режим отладки |
| `hex.retry.count` | int | `3` | Количество повторов |
| `hex.retry.delay` | long | `1000` | Задержка между повторами (мс) |
| `hex.parallel.enabled` | boolean | `true` | Параллельное выполнение |
| `hex.parallel.threads` | int | `4` | Количество потоков |

### UI настройки (UiConfig)

| Ключ | Тип | Дефолт | Описание |
|------|-----|--------|----------|
| `hex.ui.browser` | String | `chrome` | Тип браузера |
| `hex.ui.headless` | boolean | `false` | Headless режим |
| `hex.ui.base.url` | String | `http://localhost:3000` | Базовый URL |
| `hex.ui.timeout` | int | `10` | Таймаут UI элементов (сек) |
| `hex.ui.screenshots.enabled` | boolean | `true` | Включить скриншоты |
| `hex.ui.grid.url` | String | `` | URL Selenium Grid |

Полный список см. в [`UiConfig.java`](../hex-core-ui/src/main/java/com/company/hex/ui/config/UiConfig.java)

### API настройки (ApiConfig)

| Ключ | Тип | Дефолт | Описание |
|------|-----|--------|----------|
| `hex.api.base.url` | String | `http://localhost:8080` | Базовый URL API |
| `hex.api.timeout` | int | `30` | Таймаут запросов (сек) |
| `hex.api.retry.count` | int | `3` | Количество повторов |
| `hex.api.auth.type` | String | `none` | Тип аутентификации |
| `hex.api.logging.enabled` | boolean | `true` | Логирование запросов |

Полный список см. в [`ApiConfig.java`](../hex-core-api/src/main/java/com/company/hex/api/config/ApiConfig.java)

## Best Practices

1. **Используйте профили** для разных окружений вместо хардкода
2. **Не коммитьте секреты** в файлы конфигурации - используйте переменные окружения
3. **Создавайте точечные переопределения** для специфичных тестов
4. **Документируйте** кастомные настройки в своём проекте
5. **Используйте фасад** для упрощения доступа к конфигурации

## Troubleshooting

### Конфигурация не загружается

Проверьте:
- Файлы находятся в `src/test/resources` или `src/main/resources`
- Имена файлов соответствуют паттерну `hex-${hex.environment}.properties`
- Нет опечаток в ключах свойств

### Значения не переопределяются

Помните о приоритете источников:
- Системные свойства (`-D`) имеют наивысший приоритет
- Переменные окружения следующие
- Файлы профилей перекрывают базовый `hex.properties`

### Проблемы с кэшированием

Используйте `HexConfigs.reset()` для сброса кэша между тестами.

## Дополнительные ресурсы

- [Owner Documentation](http://owner.aeonbits.org/)
- [Hex Framework Architecture](../docs/architecture/configuration-with-owner.md)
- [Sample Tests](src/test/java/com/company/hex/project/tests/)