# Предложения по улучшению Hex UI Framework

## Обзор

После анализа трёх модулей фреймворка (API, DB, UI) выявлены несоответствия в архитектурном подходе UI модуля относительно API и DB модулей. Данный документ содержит предложения по унификации и улучшению UI части.

---

## 1. Главная проблема: Несогласованность парадигмы

### Текущая ситуация

| Модуль | Парадигма | Создание сервиса |
|--------|-----------|------------------|
| **API** | Интерфейс + Proxy | `ApiServiceFactory.create(UserApi.class)` |
| **DB** | Интерфейс + Proxy | `DbServiceFactory.create(UserRepository.class)` |
| **UI** | Наследование классов | `new LoginPage()`, `extends BasePage` |

API и DB используют **декларативные интерфейсы** с динамическими прокси. UI использует **классическое наследование**, что создаёт:

- Разный опыт разработки между модулями
- Больше boilerplate кода в UI (обязательный конструктор `super()`)
- Сложнее мокировать в тестах (классы vs интерфейсы)
- Нет единой фабрики с Builder API

### Предложение

Рассмотреть возможность декларативного подхода для UI:

```java
// Вместо наследования
@Page(url = "/login", title = "Login Page")
public class LoginPage extends BasePage {
    @Element(name = "Username", xpath = "//input[@id='username']")
    public Input username;
}

// Декларативный интерфейс (как в API/DB)
@UiService(url = "/login", title = "Login Page")
public interface LoginPage {
    
    @Element(name = "Username", xpath = "//input[@id='username']")
    Input username();
    
    @Element(name = "Password", xpath = "//input[@id='password']")
    Input password();
    
    @Action("Войти в систему")
    HomePage login(@Param("user") String user, @Param("pass") String pass);
}

// Использование
LoginPage loginPage = UiServiceFactory.create(LoginPage.class);
loginPage.open();
loginPage.username().fill("admin");
```

**Почему это важно:**
- Единообразие API во всём фреймворке
- Интерфейсы легче мокировать
- Возможность добавить Builder API с интерцепторами
- Compile-time проверка контракта

---

## 2. Интерцепторы: Разный подход

### Текущая ситуация

| Модуль | Интерфейс | Chain API | Метод перехвата |
|--------|-----------|-----------|-----------------|
| **API** | `Interceptor.intercept(Chain)` | ✅ | Request → Response |
| **DB** | `DbInterceptor.intercept(DbInterceptorChain)` | ✅ | Query → Result |
| **UI** | `ElementInterceptor` | ❌ | `beforeAction`, `afterAction`, `onError` |

API и DB используют паттерн **Chain of Responsibility** с методом `proceed()`. UI использует callback-подход без цепочки.

### Предложение

Унифицировать интерцепторы UI с API/DB:

```java
// Текущий подход (callback)
public interface ElementInterceptor {
    default void beforeAction(BaseElement element, String action, Object[] args) {}
    default void afterAction(BaseElement element, String action, Object result) {}
    default void onError(BaseElement element, String action, Exception e) {}
}

// Предлагаемый подход (chain)
public interface UiInterceptor {
    
    ActionResult intercept(UiInterceptorChain chain);
    
    default int getOrder() { return 0; }
}

public interface UiInterceptorChain {
    
    ActionDefinition action();      // Информация о действии (click, fill, etc.)
    ElementContext context();        // Контекст элемента (page, component, locator)
    ActionResult proceed();          // Продолжить выполнение
}

// Пример использования
public class ScreenshotOnErrorInterceptor implements UiInterceptor {
    
    @Override
    public ActionResult intercept(UiInterceptorChain chain) {
        try {
            return chain.proceed();
        } catch (Exception e) {
            Allure.addAttachment("failure", takeScreenshot());
            throw e;
        }
    }
}
```

**Преимущества:**
- Единый паттерн во всём фреймворке
- Возможность модифицировать action до выполнения
- Возможность подменить результат
- Явный контроль порядка выполнения

---

## 3. Отсутствие валидаторов

### Текущая ситуация

| Модуль | Валидация |
|--------|-----------|
| **API** | `InterfaceValidator` проверяет аннотации при создании |
| **DB** | `QueryValidator` проверяет SQL до выполнения |
| **UI** | ❌ Нет валидации |

### Предложение

Добавить `PageValidator` / `ElementValidator`:

```java
public interface PageValidator {
    
    void validate(PageDefinition page);
}

// Встроенные проверки:
// ✅ Все @Element имеют либо xpath, либо css
// ✅ Локаторы в компонентах начинаются с "./"
// ✅ Нет дублирующихся имён элементов
// ✅ @Page имеет обязательный title
// ⚠️ Предупреждение о хрупких локаторах (//div[3]/button)

// Пример валидации
public class StrictPageValidator implements PageValidator {
    
    @Override
    public void validate(PageDefinition page) {
        for (ElementDefinition element : page.getElements()) {
            // Проверка стабильности локатора
            if (isFragileLocator(element.getLocator())) {
                throw new PageValidationException(
                    "Fragile locator detected in " + element.getName() + 
                    ": " + element.getLocator() + 
                    ". Use data-testid or stable attributes."
                );
            }
        }
    }
    
    private boolean isFragileLocator(String locator) {
        return locator.matches(".*\\[\\d+\\].*") ||  // Индексы [1], [2]
               locator.contains("nth-child");
    }
}
```

---

## 4. Нет единой фабрики с Builder API

### Текущая ситуация

| Модуль | Фабрика | Builder API |
|--------|---------|-------------|
| **API** | `ApiServiceFactory.create()` | `ApiServiceFactory.builder()` |
| **DB** | `DbServiceFactory.create()` | `DbServiceFactory.builder()` |
| **UI** | `new LoginPage()` | ❌ |

### Предложение

Добавить `UiServiceFactory`:

```java
// Простой подход
LoginPage page = UiServiceFactory.create(LoginPage.class);

// Builder API
LoginPage page = UiServiceFactory.builder()
    .withConfig(customConfig)
    .addInterceptor(new ScreenshotOnErrorInterceptor())
    .addInterceptor(new HighlightInterceptor())
    .addInterceptor(new SlowActionWarningInterceptor(Duration.ofSeconds(5)))
    .withValidator(new StrictPageValidator())
    .withElementFactory(customElementFactory)
    .create(LoginPage.class);
```

**Преимущества:**
- Унификация с API/DB модулями
- Централизованная конфигурация
- Расширяемость через Builder

---

## 5. Escape Hatch: Неконсистентность

### Текущая ситуация

| Модуль | Escape Hatch |
|--------|--------------|
| **API** | `ApiServiceFactory.getRequestSpecification()`, Raw Response |
| **DB** | `DbServiceFactory.getConnection()`, `@RawQuery` |
| **UI** | `element.getElement()` (Selenide), `element.getWebElement()` |

UI предоставляет доступ к низкоуровневым объектам, но нет документированного "escape hatch" API.

### Предложение

Формализовать escape hatch:

```java
public interface UiEscapeHatch {
    
    // Доступ к Selenide напрямую
    SelenideElement selenide(String locator);
    ElementsCollection selenideAll(String locator);
    
    // Выполнить JavaScript
    <T> T executeJs(String script, Object... args);
    
    // Получить WebDriver
    WebDriver getWebDriver();
    
    // Получить базовую спецификацию
    Configuration getConfiguration();
}

// Использование
UiEscapeHatch escape = UiServiceFactory.getEscapeHatch();
escape.executeJs("window.scrollTo(0, document.body.scrollHeight)");
```

---

## 6. Конфигурация: Различия в именовании

### Текущая ситуация

```properties
# API
hex.api.base.url=...
hex.api.timeout=30

# DB
hex.db.primary.url=...
hex.db.query.timeout=30

# UI
hex.ui.base.url=...
hex.ui.timeout=10
```

Неконсистентность: `hex.api.timeout` vs `hex.ui.timeout` vs `hex.db.query.timeout`.

### Предложение

Унифицировать naming convention:

```properties
# Общий паттерн: hex.<module>.<component>.<property>

# API
hex.api.http.timeout=30
hex.api.http.retry.count=3

# DB
hex.db.query.timeout=30
hex.db.connection.timeout=30

# UI
hex.ui.action.timeout=10
hex.ui.page-load.timeout=30
```

---

## 7. Обработка ошибок: Нет иерархии исключений

### Текущая ситуация

| Модуль | Иерархия исключений |
|--------|---------------------|
| **API** | `ApiConversionException`, `ApiResponseException` |
| **DB** | Полная иерархия: `DbException` → `QueryValidationException`, `ConstraintViolationException`, ... |
| **UI** | Использует Selenide exceptions напрямую |

### Предложение

Добавить иерархию UI исключений:

```
UiException (unchecked, base)
├── PageValidationException      - Ошибка валидации страницы/элемента
├── ElementNotFoundException     - Элемент не найден
├── ElementStateException        - Неверное состояние элемента
│   ├── ElementNotVisibleException
│   ├── ElementNotClickableException
│   └── ElementDisabledException
├── ActionException              - Ошибка выполнения действия
│   ├── ActionTimeoutException
│   └── StaleElementException
└── PageLoadException            - Ошибка загрузки страницы
```

```java
public class ElementNotFoundException extends UiException {
    
    private final String elementName;
    private final String locator;
    private final Duration waitTime;
    
    @Override
    public String getMessage() {
        return String.format("""
            Element not found
            Name: %s
            Locator: %s
            Wait time: %s
            
            Possible solutions:
              1. Check that the locator is correct
              2. Ensure element is rendered before interaction
              3. Increase timeout if element loads slowly
              4. Verify element is not inside an iframe
            """, elementName, locator, waitTime);
    }
}
```

---

## 8. Компоненты: Нет @Component аннотации уровня интерфейса

### Текущая ситуация

Компоненты создаются через наследование `BaseComponent`, но нет декларативного способа определить компонент.

### Предложение

Добавить декларативные компоненты (аналог `@DbService`, `@ApiService`):

```java
@UiComponent(name = "Header")
public interface HeaderComponent {
    
    @Element(name = "Logo", xpath = ".//img[@class='logo']")
    Button logo();
    
    @Element(name = "Search", xpath = ".//input[@type='search']")
    Input searchField();
    
    @Action("Выполнить поиск")
    void search(@Param("query") String query);
}

// Использование на странице
@UiService(url = "/dashboard", title = "Dashboard")
public interface DashboardPage {
    
    @Component(root = "//header[@id='main-header']")
    HeaderComponent header();
    
    @Component(root = "//aside[@id='sidebar']")
    SidebarComponent sidebar();
}
```

---

## 9. Типизация ElementList

### Текущая ситуация

`ElementList<T>` хорошо спроектирован, но методы `filter()`, `findBy()` не всегда type-safe.

### Предложение

Усилить типизацию через кастомные предикаты:

```java
// Текущий подход
ElementList<Button> activeButtons = buttons.filterBy(visible);

// Предлагаемый подход с типизированными предикатами
public interface TypedElementPredicate<T extends BaseElement> {
    boolean test(T element);
}

public interface ElementList<T extends BaseElement> {
    
    ElementList<T> filterBy(TypedElementPredicate<T> predicate);
    
    Optional<T> findFirst(TypedElementPredicate<T> predicate);
}

// Использование
ElementList<Input> emptyInputs = inputs.filterBy(input -> input.getValue().isEmpty());
```

---

## 10. Резюме приоритетов

| # | Изменение | Приоритет | Сложность | Влияние |
|---|-----------|-----------|-----------|---------|
| 1 | UiServiceFactory + Builder API | 🔴 Высокий | Средняя | Унификация API |
| 2 | Унификация интерцепторов (Chain) | 🔴 Высокий | Средняя | Консистентность |
| 3 | Иерархия UiException | 🟡 Средний | Низкая | Удобство отладки |
| 4 | PageValidator | 🟡 Средний | Низкая | Качество кода |
| 5 | Декларативные интерфейсы страниц | 🟢 Низкий | Высокая | Breaking change |
| 6 | Формализованный Escape Hatch | 🟢 Низкий | Низкая | Документация |
| 7 | Унификация конфигурации | 🟢 Низкий | Низкая | Удобство |

---

## Заключение

Главные направления улучшения:

1. **Унификация архитектуры** — привести UI к единому паттерну с API/DB (фабрики, интерцепторы, валидаторы)
2. **Улучшение DX** — иерархия исключений, понятные сообщения об ошибках
3. **Расширяемость** — Builder API с возможностью кастомизации

Рекомендуется начать с **добавления UiServiceFactory и Builder API** — это не breaking change, но значительно улучшит консистентность фреймворка.

---

*Документ подготовлен на основе анализа hex-ui-framework-guide.md, api-client-guide.md, hex-db-framework-guide.md*
