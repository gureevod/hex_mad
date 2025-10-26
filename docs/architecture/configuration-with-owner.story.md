# Story: Конфигурация проекта через Owner для Hex

- Цель: унифицировать и упростить настройку UI, API и глобальных параметров в проектах, которые подключают `hex-core-api`, `hex-core-ui` и `hex-core-testing` как зависимости, без правки кода библиотек.
- Результат: понятная слоистая схема разрешения пропертей, единая фасад-обертка для получения конфигов, примеры для конечного проекта и четкий план изменений.

---

## Кратко

- Single-source-of-truth: интерфейсы `BaseConfig`, `UiConfig`, `ApiConfig` остаются источником ключей настройки.
- Слои и приоритеты: системные флаги → переменные окружения → `hex-${hex.environment}.properties` → `hex-${env}.properties` → `hex.properties` → библиотечные дефолты.
- Профили: один явный флаг `hex.environment` для выбора профиля (`local`, `ci`, `qa`, …).
- Фасад для потребителей: класс `HexConfigs` в `hex-core` скрывает Owner и дает точечные override’ы на тест/сьют.

---

## Контекст и проблема

Текущая картина:

- `hex-core-api/src/main/java/com/company/hex/api/config/ApiConfig.java`
- `hex-core-ui/src/main/java/com/company/hex/ui/config/UiConfig.java`
- `hex-core/src/main/java/com/company/hex/core/config/BaseConfig.java`

Они аннотированы `@Config.Sources` и опираются на Owner. Однако конечным проектам не очевидно:

- какие файлы класть и куда (classpath vs системные),
- как переключать окружения (local/ci/qa/…),
- как локально переопределять на уровне тест/сьюта без влияния на весь ран.

Нужно сделать: продуманный, гибкий и ясный способ конфигурирования, который не требует погружения в Owner и работает, когда все модули подключены как зависимости.

---

## Предлагаемый дизайн

### Нейминг ключей

- Глобальные: `hex.*` (например, `hex.environment`, `hex.parallel.*`, `hex.timeout.default`)
- UI: `hex.ui.*` (например, `hex.ui.browser`, `hex.ui.base.url`)
- API: `hex.api.*` (например, `hex.api.base.url`, `hex.api.timeout`)

### Приоритет источников (высший → низший)

1. `system:properties` (например, `-Dhex.ui.browser=edge`)
2. `system:env` (например, `HEX_UI_BROWSER=edge`)
3. `classpath:hex-${hex.environment}.properties` (например, `hex-ci.properties`)
4. `classpath:hex-${env}.properties` (обратная совместимость)
5. `classpath:hex.properties` (дефолты проекта)
6. `classpath:META-INF/hex-defaults.properties` (дефолты библиотек)

Примечание: при `@Config.LoadPolicy(MERGE)` источники, указанные правее, перекрывают значения из источников левее. Поэтому список в аннотации должен идти от дефолтов к самым приоритетным.

### Единые источники для всех конфигов

Обновить аннотации для всех интерфейсов конфигурации до единого вида:

```java
@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources({
    "classpath:META-INF/hex-defaults.properties",
    "classpath:hex.properties",
    "classpath:hex-${hex.environment}.properties",
    "classpath:hex-${env}.properties",
    "system:env",
    "system:properties"
})
```

Сохранить поддержку `${env}` ради обратной совместимости, но предпочитать `${hex.environment}` как основной переключатель профиля.

### Профили окружений

- Основной переключатель: `hex.environment` (дефолт `local`).
- Способы выбора профиля:
  - Maven/Gradle: `-Dhex.environment=ci`
  - Через код (ранняя инициализация): `System.setProperty("hex.environment", "ci");`

### Библиотечные дефолты

- Каждая библиотека может приложить дефолтный набор в `META-INF/hex-defaults.properties` (безопасные значения).
- Эти дефолты всегда ниже по приоритету, чем файлы проекта, системные флаги и переменные окружения.

### Фасад для потребителей (`hex-core`)

Добавить класс `com.company.hex.core.config.HexConfigs`, чтобы скрыть детали Owner и предоставить:

```java
public final class HexConfigs {
  public static UiConfig ui() { /* create & cache */ }
  public static ApiConfig api() { /* create & cache */ }
  public static UiConfig ui(Map<?,?>... overrides) { /* scoped overrides */ }
  public static ApiConfig api(Map<?,?>... overrides) { /* scoped overrides */ }
  public static void useProfile(String profile) { System.setProperty("hex.environment", profile); }
}
```

Назначение: конечный код не зависит от деталей Owner и получает удобные, контролируемые инстансы конфигов; возможны безопасные точечные override’ы на тест/сьют.

---

## Что менять и где

### Базовый конфиг

- Файл: `hex-core/src/main/java/com/company/hex/core/config/BaseConfig.java`
- Действия:
  - Привести `@Config.Sources` к единому списку (см. «Единые источники для всех конфигов»).
  - Сохранить существующие ключи (`hex.environment`, `hex.logging.level`, …).
  - Убедиться, что `@Config.LoadPolicy(MERGE)` присутствует.

### UI и API конфиги

- Файлы:
  - `hex-core-ui/src/main/java/com/company/hex/ui/config/UiConfig.java`
  - `hex-core-api/src/main/java/com/company/hex/api/config/ApiConfig.java`
- Действия:
  - Привести `@Config.Sources` к единому списку (идентично `BaseConfig`).
  - Не менять существующие ключи, чтобы не ломать обратную совместимость.

### Фасад

- Новый файл: `hex-core/src/main/java/com/company/hex/core/config/HexConfigs.java`
- Скелет реализации через `org.aeonbits.owner.ConfigFactory` с легким кэшем (по необходимости):

```java
public final class HexConfigs {
  private static volatile UiConfig UI;
  private static volatile ApiConfig API;

  public static UiConfig ui() {
    if (UI == null) {
      synchronized (HexConfigs.class) {
        if (UI == null) UI = ConfigFactory.create(UiConfig.class);
      }
    }
    return UI;
  }

  public static ApiConfig api() {
    if (API == null) {
      synchronized (HexConfigs.class) {
        if (API == null) API = ConfigFactory.create(ApiConfig.class);
      }
    }
    return API;
  }

  @SafeVarargs
  public static UiConfig ui(Map<?,?>... overrides) {
    return ConfigFactory.create(UiConfig.class, overrides);
  }

  @SafeVarargs
  public static ApiConfig api(Map<?,?>... overrides) {
    return ConfigFactory.create(ApiConfig.class, overrides);
  }

  public static void useProfile(String profile) {
    System.setProperty("hex.environment", profile);
  }
}
```

### Библиотечные дефолты

- Новые файлы в jar-ресурсах библиотек:
  - `hex-core/src/main/resources/META-INF/hex-defaults.properties`
  - `hex-core-ui/src/main/resources/META-INF/hex-defaults.properties`
  - `hex-core-api/src/main/resources/META-INF/hex-defaults.properties`
- Пример содержимого (минимальные безопасные значения):

```properties
# META-INF/hex-defaults.properties (пример)
hex.environment=local
hex.logging.level=INFO
hex.timeout.default=30
hex.parallel.enabled=true
hex.parallel.threads=4

hex.ui.browser=chrome
hex.ui.headless=false
hex.ui.base.url=http://localhost:3000
hex.ui.timeout=10

hex.api.base.url=http://localhost:8080
hex.api.timeout=30
hex.api.retry.count=3
```

---

## Изменения в примере проекта (hex-project-samples)

### Добавить проектные файлы

- `hex-project-samples/src/test/resources/hex.properties`
- `hex-project-samples/src/test/resources/hex-ci.properties` (и/или `hex-qa.properties`)

### Примеры содержимого

```properties
# hex.properties
hex.environment=local
hex.ui.browser=chrome
hex.ui.base.url=http://localhost:3000
hex.api.base.url=http://localhost:8080
hex.timeout.default=20
```

```properties
# hex-ci.properties
hex.ui.browser=edge
hex.ui.headless=true
hex.ui.grid.url=http://selenium:4444/wd/hub
hex.api.base.url=https://api-ci.example.com
hex.api.timeout=60
```

### Использование в тестах

Без override:

```java
UiConfig ui = HexConfigs.ui();
ApiConfig api = HexConfigs.api();
```

Точечный override на тест:

```java
UiConfig ui = HexConfigs.ui(Map.of("hex.ui.headless", "true"));
```

Переключение профиля:

```bash
mvn test -Dhex.environment=ci
```

Или ранняя установка в коде (например, JUnit 5 Extension):

```java
HexConfigs.useProfile("ci");
```

---

## Acceptance Criteria

- Все конфиги (`BaseConfig`, `UiConfig`, `ApiConfig`) читают одинаковый набор источников и уважают приоритеты.
- `hex.environment` выбирает файл `hex-${hex.environment}.properties`.
- Потребитель может:
  - задать дефолты в `hex.properties`,
  - переключать профили через `-Dhex.environment=...`,
  - переопределять отдельные ключи через `-D...` или переменные окружения,
  - делать точечные override’ы на тест через фасад.
- Добавлены библиотечные дефолты `META-INF/hex-defaults.properties` (не ломают проекты).
- Примеры и документация понятны и воспроизводимы.

---

## Out of Scope / Не делаем сейчас

- Изменение ключей или модели конфигов.
- Внедрение DI-контейнера.
- Полная интеграция с внешними секрет-менеджерами (возможна в будущем).

---

## Риски и смягчение

- Риск конфликта ключей: придерживаемся неймспейсов `hex.*`, `hex.ui.*`, `hex.api.*`.
- Переход `${env}` → `${hex.environment}`: сохраняем оба источника; `hex.environment` выше по приоритету.
- Дублирование дефолтов: библиотечные `META-INF/hex-defaults.properties` минимальны; проектные `hex.properties` выигрывают.

---

## План миграции

1. Обновить `@Config.Sources` в трех интерфейсах, не меняя ключей.
2. Добавить фасад `HexConfigs` в `hex-core`.
3. Добавить библиотечные дефолты `META-INF/hex-defaults.properties` в каждом модуле.
4. Добавить файлы примеров в `hex-project-samples`.
5. Обновить документацию и ссылочную навигацию.

---

## Примеры CLI

- Локальный прогон:

```bash
mvn test -Dhex.environment=local
```

- CI прогон:

```bash
mvn test -Dhex.environment=ci -Dhex.ui.headless=true
```

- Точечные overrides:

```bash
mvn test -Dhex.ui.browser=firefox -Dhex.api.timeout=45
```

---

## Связанные файлы

- `hex-core/src/main/java/com/company/hex/core/config/BaseConfig.java`
- `hex-core-ui/src/main/java/com/company/hex/ui/config/UiConfig.java`
- `hex-core-api/src/main/java/com/company/hex/api/config/ApiConfig.java`
- (новый) `hex-core/src/main/java/com/company/hex/core/config/HexConfigs.java`
- (новые) `*/src/main/resources/META-INF/hex-defaults.properties`
- `hex-project-samples/src/test/resources/hex.properties`
- `hex-project-samples/src/test/resources/hex-ci.properties`

---

## Appendix: Пример Owner аннотаций и фасада

```java
// Пример унифицированных источников в конфиг-интерфейсе
@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources({
    "classpath:META-INF/hex-defaults.properties",
    "classpath:hex.properties",
    "classpath:hex-${hex.environment}.properties",
    "classpath:hex-${env}.properties",
    "system:env",
    "system:properties"
})
public interface UiConfig extends BaseConfig {
  @Key("hex.ui.browser") @DefaultValue("chrome") String browser();
  // ... остальные ключи
}

// Пример фасада
public final class HexConfigs {
  private static volatile UiConfig UI;
  private static volatile ApiConfig API;

  public static UiConfig ui() { /* ConfigFactory.create(UiConfig.class) */ }
  public static ApiConfig api() { /* ConfigFactory.create(ApiConfig.class) */ }
  public static UiConfig ui(Map<?,?>... overrides) { /* scoped */ }
  public static ApiConfig api(Map<?,?>... overrides) { /* scoped */ }
  public static void useProfile(String profile) { System.setProperty("hex.environment", profile); }
}
```

