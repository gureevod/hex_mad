# Hex UI Framework: Полное руководство

## Содержание

1. [Введение](#введение)
2. [Быстрый старт](#быстрый-старт)
3. [Архитектура фреймворка](#архитектура-фреймворка)
4. [Page Objects](#page-objects)
5. [Элементы](#элементы)
6. [Компоненты](#компоненты)
7. [Коллекции элементов](#коллекции-элементов)
8. [Условия проверки](#условия-проверки)
9. [Interceptors](#interceptors)
10. [Builder API](#builder-api)
11. [Конфигурация](#конфигурация)
12. [Создание кастомных элементов](#создание-кастомных-элементов)
13. [Лучшие практики](#лучшие-практики)
14. [Решение типичных задач](#решение-типичных-задач)

---

## Введение

Hex UI Framework — это декларативный фреймворк для UI-тестирования, построенный на базе Selenide. Он предоставляет:

- **Декларативный подход** — определение элементов через аннотации
- **Типобезопасность** — строго типизированные элементы и коллекции
- **Автоматическое логирование** — контекстные логи с привязкой к странице и компоненту
- **Интеграция с Allure** — автоматические steps для всех действий
- **Thread-safe** — готовность к параллельному запуску тестов
- **Расширяемость** — легкое создание кастомных элементов, условий и interceptors

### Основные преимущества

| Возможность | Описание |
|-------------|----------|
| Сокращение boilerplate | Уменьшение дублирующегося кода на ≥40% |
| Быстрый старт | Время до первого теста ≤60 минут |
| Стабильность | Flake rate ≤2% благодаря умным ожиданиям |
| Стандартизация | Единые паттерны для Page Objects и Components |

---

## Быстрый старт

### Шаг 1: Создание Page Object

```java
import com.company.hex.ui.annotations.Element;
import com.company.hex.ui.annotations.Page;
import com.company.hex.ui.core.BasePage;
import com.company.hex.ui.elements.Button;
import com.company.hex.ui.elements.Input;

@Page(url = "/login", title = "Страница входа")
public class LoginPage extends BasePage {
    
    @Element(name = "Логин", xpath = "//input[@id='username']")
    public Input username;
    
    @Element(name = "Пароль", xpath = "//input[@id='password']")
    public Input password;
    
    @Element(name = "Кнопка Войти", xpath = "//button[@type='submit']")
    public Button loginButton;
    
    public HomePage login(String user, String pass) {
        username.fill(user);
        password.fill(pass);
        loginButton.click();
        return new HomePage();
    }
}
```

### Шаг 2: Написание теста

```java
@Test
public void testSuccessfulLogin() {
    LoginPage loginPage = new LoginPage();
    loginPage.open();
    
    HomePage homePage = loginPage.login("admin", "password");
    
    homePage.welcomeMessage.shouldBe(visible);
}
```

### Шаг 3: Конфигурация (hex.properties)

```properties
hex.ui.base.url=http://localhost:3000
hex.ui.browser=chrome
hex.ui.headless=false
hex.ui.timeout=10
hex.ui.window.width=1920
hex.ui.window.height=1080
```

---

## Архитектура фреймворка

### Диаграмма компонентов

```mermaid
graph TB
    subgraph Core Layer
        BasePage[BasePage]
        BaseComponent[BaseComponent]
        BaseElement[BaseElement]
    end
    
    subgraph Elements Layer
        Input[Input]
        Button[Button]
        Select[Select]
        Checkbox[Checkbox]
        Table[Table]
        DatePicker[DatePicker]
        TextElement[TextElement]
    end
    
    subgraph Annotations
        PageAnn[@Page]
        ElementAnn[@Element]
        ElementsAnn[@Elements]
        ComponentAnn[@Component]
    end
    
    subgraph Factory
        ElementFactory[ElementFactory]
        FieldInitializer[FieldInitializer]
    end
    
    subgraph Collections
        ElementList[ElementList]
    end
    
    subgraph Interceptors
        InterceptorRegistry[InterceptorRegistry]
        ScreenshotInterceptor[ScreenshotOnError]
        MetricsInterceptor[Metrics]
    end
    
    BasePage --> BaseElement
    BaseComponent --> BaseElement
    BaseElement --> Elements Layer
    
    PageAnn --> BasePage
    ElementAnn --> FieldInitializer
    ElementsAnn --> FieldInitializer
    ComponentAnn --> FieldInitializer
    
    FieldInitializer --> ElementFactory
    ElementFactory --> Elements Layer
    
    BaseElement --> InterceptorRegistry
```

### Ключевые концепции

1. **BasePage** — базовый класс для всех страниц
2. **BaseComponent** — базовый класс для переиспользуемых компонентов
3. **BaseElement** — базовый класс для всех UI элементов
4. **ElementList** — типизированная коллекция элементов
5. **ElementInterceptor** — перехватчик действий над элементами

---

## Page Objects

### Аннотация @Page

Аннотация `@Page` определяет метаданные страницы:

```java
@Page(url = "/users", title = "Список пользователей")
public class UsersPage extends BasePage {
    // ...
}
```

| Параметр | Описание | Обязательный |
|----------|----------|--------------|
| `url` | Относительный URL страницы | Нет |
| `title` | Заголовок страницы для логирования | Да |

### Методы BasePage

| Метод | Описание |
|-------|----------|
| `open()` | Открыть страницу по URL из аннотации |
| `open(String url)` | Открыть страницу по указанному URL |
| `isOpened()` | Проверить, что страница открыта (по URL) |
| `isTitleCorrect()` | Проверить заголовок страницы |
| `refresh()` | Обновить страницу |
| `back()` | Вернуться назад |
| `forward()` | Перейти вперед |
| `getCurrentUrl()` | Получить текущий URL |
| `getCurrentTitle()` | Получить текущий заголовок |

### Пример расширенного Page Object

```java
@Page(url = "/users", title = "Пользователи")
public class UsersPage extends BasePage {
    
    @Element(name = "Поиск", xpath = "//input[@placeholder='Поиск']")
    public Input searchField;
    
    @Element(name = "Кнопка поиска", css = "button.search-btn")
    public Button searchButton;
    
    @Element(name = "Таблица пользователей", xpath = "//table[@id='users-table']")
    public Table usersTable;
    
    @Elements(name = "Карточки пользователей", xpath = "//div[@class='user-card']")
    public ElementList<TextElement> userCards;
    
    @Component(name = "Навигация", root = "//nav[@class='main-nav']")
    public NavigationComponent navigation;
    
    @Step("Найти пользователя по имени")
    public UsersPage searchUser(String name) {
        searchField.fill(name);
        searchButton.click();
        return this;
    }
    
    @Step("Получить количество найденных пользователей")
    public int getFoundUsersCount() {
        return userCards.size();
    }
}
```

---

## Элементы

### Аннотация @Element

```java
@Element(
    name = "Email",           // Обязательно: имя для логов и Allure
    xpath = "//input[@type='email']",  // XPath локатор
    timeout = 15,             // Опционально: таймаут в секундах
    pollingInterval = 500     // Опционально: интервал polling в мс
)
public Input emailField;

// Или с CSS селектором
@Element(name = "Submit", css = "button[type='submit']")
public Button submitButton;
```

### Встроенные типы элементов

#### Input — текстовое поле ввода

```java
@Element(name = "Email", xpath = "//input[@id='email']")
public Input email;

// Использование
email.fill("user@example.com");      // Заполнить (с очисткой)
email.clear();                        // Очистить
email.append(" additional");          // Добавить текст
email.type("slow input");             // Посимвольный ввод
email.getValue();                     // Получить значение
email.pressEnter();                   // Нажать Enter
email.pressTab();                     // Нажать Tab
email.shouldHaveValue("expected");    // Проверить значение
email.shouldBeEmpty();                // Проверить пустоту
```

#### Button — кнопка

```java
@Element(name = "Submit", xpath = "//button[@type='submit']")
public Button submitButton;

// Использование
submitButton.click();                 // Клик
submitButton.doubleClick();           // Двойной клик
submitButton.hover();                 // Навести курсор
submitButton.clickViaJs();            // Клик через JavaScript
submitButton.clickWithOffset(10, 5);  // Клик со смещением
submitButton.shouldHaveText("Save");  // Проверить текст
submitButton.isEnabled();             // Проверить активность
submitButton.isDisabled();            // Проверить отключение
```

#### Select — выпадающий список

```java
@Element(name = "Страна", xpath = "//select[@id='country']")
public Select country;

// Использование
country.selectByText("Россия");           // Выбрать по тексту
country.selectByValue("RU");              // Выбрать по значению
country.selectByIndex(0);                 // Выбрать по индексу
country.getSelectedText();                // Получить выбранный текст
country.getSelectedValue();               // Получить выбранное значение
country.getAllOptions();                  // Получить все опции
country.getAllOptionValues();             // Получить все значения
country.shouldHaveSelectedText("Россия"); // Проверить выбранный текст
country.shouldHaveOption("США");          // Проверить наличие опции
country.shouldHaveOptionsCount(10);       // Проверить количество опций
```

#### Checkbox — чекбокс

```java
@Element(name = "Согласие", xpath = "//input[@type='checkbox']")
public Checkbox agreement;

// Использование
agreement.check();                // Установить галочку
agreement.uncheck();              // Снять галочку
agreement.toggle();               // Переключить
agreement.isChecked();            // Проверить состояние
agreement.setChecked(true);       // Установить в состояние
agreement.shouldBeChecked();      // Проверить, что отмечен
agreement.shouldBeUnchecked();    // Проверить, что не отмечен
```

#### TextElement — текстовый элемент

```java
@Element(name = "Заголовок", xpath = "//h1")
public TextElement title;

// Использование
title.getText();                          // Получить текст
title.getInnerText();                     // Получить innerText
title.getOwnText();                       // Получить собственный текст
title.shouldHaveText("Добро пожаловать"); // Проверить текст
title.shouldHaveExactText("Welcome");     // Точное совпадение
title.shouldNotHaveText("Error");         // Текст не содержит
title.shouldBeEmpty();                    // Пустой
title.shouldNotBeEmpty();                 // Не пустой
title.shouldMatchText("\\d{4}");          // Regex проверка
title.shouldHaveTextLength(10);           // Проверить длину
title.shouldStartWith("Hello");           // Начинается с
title.shouldEndWith("!");                 // Заканчивается на
```

#### Table — таблица (Ant Design)

```java
@Element(name = "Таблица пользователей", xpath = "//table[@class='users-table']")
public Table usersTable;

// Использование
usersTable.getHeaders();                  // Получить заголовки
usersTable.getRowCount();                 // Количество строк
usersTable.getRow(0);                     // Получить строку по индексу
usersTable.getRow("user@example.com");    // Найти строку по тексту
usersTable.getRowsAsMaps();               // Все данные как List<Map>

// Работа со строкой
Table.Row row = usersTable.getRow(0);
row.getValue("Email");                    // Получить значение ячейки
row.click();                              // Клик по строке
row.doubleClick();                        // Двойной клик
row.fillInputs(Map.of("Name", "Иван"));   // Заполнить инпуты в строке
row.clickAction("Редактировать");         // Клик по кнопке действия
row.shouldHaveValues(Map.of("Status", "Active"));  // Проверить значения

// Проверки таблицы
usersTable.shouldHaveRow(Map.of("Email", "user@example.com"));
usersTable.shouldNotHaveRow(Map.of("Email", "deleted@example.com"));
usersTable.shouldBeEmpty();
usersTable.shouldHaveHeaders("ID", "Name", "Email");

// Обновление до появления данных
usersTable.refreshPageUntilDataAppears(
    Map.of("Status", "Processed"), 
    5  // максимум попыток
);
```

#### DatePicker — календарь (Ant Design)

```java
@Element(name = "Дата рождения", xpath = "//div[@class='ant-picker']")
public DatePicker birthDate;

// Использование
birthDate.setDate(LocalDate.of(1990, 5, 15));  // Выбор через UI
birthDate.typeDate(LocalDate.now());            // Ручной ввод
birthDate.clear();                              // Очистить
birthDate.isDateDisabled(LocalDate.now());      // Проверить доступность даты
```

### Общие методы BaseElement

Все элементы наследуют от `BaseElement` следующие методы:

```java
// Основные действия
element.click();
element.doubleClick();
element.hover();
element.scrollTo();
element.scrollIntoView();

// Получение информации
element.getText();
element.getAttribute("class");
element.exists();
element.isDisplayed();

// Проверки
element.shouldBe(visible);
element.shouldBe(visible, enabled);
element.shouldHave(text("Hello"));
element.shouldNotBe(disabled);
element.shouldNotHave(cssClass("error"));

// Настройки
element.withTimeout(Duration.ofSeconds(30));  // Временный таймаут

// Доступ к Selenide
element.getElement();     // SelenideElement
element.getWebElement();  // WebElement
```

---

## Компоненты

### Что такое компонент?

Компонент — это переиспользуемая группа элементов с общим корневым локатором. Используется для:

- Шапки (Header)
- Подвала (Footer)
- Боковой навигации (Sidebar)
- Модальных окон (Modal)
- Форм (Form)
- Любых повторяющихся блоков

### Создание компонента

```java
public class HeaderComponent extends BaseComponent {
    
    @Element(name = "Логотип", xpath = ".//img[@class='logo']")
    public Button logo;
    
    @Element(name = "Поиск", xpath = ".//input[@type='search']")
    public Input searchField;
    
    @Element(name = "Меню пользователя", xpath = ".//div[@class='user-menu']")
    public Button userMenu;
    
    @Elements(name = "Пункты меню", xpath = ".//nav//a")
    public ElementList<Button> menuItems;
    
    @Step("Выполнить поиск")
    public void search(String query) {
        searchField.fill(query);
        searchField.pressEnter();
    }
    
    @Step("Открыть меню пользователя")
    public void openUserMenu() {
        userMenu.click();
    }
}
```

**Важно:** Локаторы внутри компонента должны начинаться с `.//` (относительный XPath), чтобы искать элементы внутри корня компонента.

### Использование компонента в Page Object

```java
@Page(url = "/dashboard", title = "Dashboard")
public class DashboardPage extends BasePage {
    
    @Component(name = "Шапка", root = "//header[@id='main-header']")
    public HeaderComponent header;
    
    @Component(name = "Боковое меню", root = "//aside[@id='sidebar']")
    public SidebarComponent sidebar;
    
    @Element(name = "Заголовок", xpath = "//h1")
    public TextElement pageTitle;
    
    @Step("Найти на сайте")
    public SearchResultsPage searchOnSite(String query) {
        header.search(query);
        return new SearchResultsPage();
    }
}
```

### Вложенные компоненты

Компоненты могут содержать другие компоненты:

```java
public class SidebarComponent extends BaseComponent {
    
    @Component(name = "Навигация", root = ".//nav[@class='main-nav']")
    public NavigationComponent navigation;
    
    @Component(name = "Виджеты", root = ".//div[@class='widgets']")
    public WidgetsComponent widgets;
}
```

---

## Коллекции элементов

### Аннотация @Elements

```java
@Elements(
    name = "Карточки продуктов",
    xpath = "//div[@class='product-card']",
    timeout = 15
)
public ElementList<TextElement> productCards;
```

### Методы ElementList

#### Доступ к элементам

```java
productCards.size();           // Количество элементов
productCards.isEmpty();        // Пуста ли коллекция
productCards.get(0);           // Элемент по индексу (0-based)
productCards.nth(2);           // Синоним get()
productCards.first();          // Первый элемент
productCards.last();           // Последний элемент
productCards.single();         // Единственный элемент (выбросит исключение если != 1)
```

#### Фильтрация

```java
// По Selenide условию (с ожиданием)
ElementList<Button> activeButtons = buttons.filterBy(visible);
ElementList<Button> enabledButtons = buttons.filterBy(enabled);

// По Java предикату (по снэпшоту)
ElementList<TextElement> expensiveProducts = products.filter(
    product -> {
        String priceText = product.getText();
        int price = Integer.parseInt(priceText.replaceAll("\\D", ""));
        return price > 1000;
    }
);
```

#### Поиск

```java
// По Selenide условию
Optional<Button> activeButton = buttons.findBy(visible);

// По Java предикату
Optional<TextElement> product = products.findFirst(
    p -> p.getText().contains("iPhone")
);
```

#### Трансформации

```java
// Stream API
Stream<TextElement> stream = products.stream();

// Map
Stream<String> names = products.map(p -> p.getText());

// Тексты всех элементов
List<String> texts = products.texts();

// Значения атрибута
List<String> ids = products.attributes("data-id");

// Значения value
List<String> values = inputs.values();

// ForEach
products.forEachElement(product -> {
    logger.info("Product: {}", product.getText());
});
```

#### Проверки

```java
// Selenide условия
products.shouldHave(size(10));
products.shouldHave(sizeGreaterThan(5));
products.shouldHave(exactTexts("Product 1", "Product 2"));

// Hex условия
products.shouldHaveSize(5);
products.shouldBeEmpty();
products.shouldNotBeEmpty();
```

#### Материализация

```java
// Создать снэпшот (фиксированный список)
List<TextElement> snapshot = products.snapshot();

// Получить Selenide коллекцию
ElementsCollection selenideCollection = products.asSelenide();
```

### Параметризованные коллекции

ElementList поддерживает параметризованные локаторы через метод `resolve()`:

```java
@Elements(
    name = "Строки таблицы по статусу",
    xpath = "//tr[@data-status='{status}']"
)
public ElementList<TextElement> rowsByStatus;

// Использование
rowsByStatus.resolve("status", "active").shouldHaveSize(5);
rowsByStatus.resolve("status", "pending").shouldNotBeEmpty();

// С несколькими параметрами
@Elements(
    name = "Ячейки",
    xpath = "//tr[@data-id='{rowId}']//td[@data-col='{colName}']"
)
public ElementList<TextElement> cells;

cells.resolve("rowId", "user-123", "colName", "email").first().shouldHaveText("user@example.com");

// Или через Map
cells.resolve(Map.of("rowId", "user-123", "colName", "email")).first().getText();
```

---

## Условия проверки

### Стандартные Selenide Conditions

```java
import static com.codeborne.selenide.Condition.*;

element.shouldBe(visible);
element.shouldBe(enabled);
element.shouldBe(disabled);
element.shouldBe(focused);
element.shouldBe(hidden);
element.shouldBe(checked);
element.shouldBe(selected);
element.shouldBe(empty);
element.shouldBe(readonly);

element.shouldHave(text("Hello"));
element.shouldHave(exactText("Hello World"));
element.shouldHave(value("input value"));
element.shouldHave(attribute("class", "active"));
element.shouldHave(cssClass("highlighted"));
element.shouldHave(cssValue("color", "rgb(0, 0, 0)"));
```

### HexConditions — расширенные условия для элементов

```java
import static com.company.hex.ui.conditions.HexConditions.*;

// CSS классы
element.shouldBe(hasClass("active"));           // Имеет класс
element.shouldBe(notHasClass("disabled"));      // Не имеет класс

// Data-атрибуты
element.shouldBe(hasDataAttribute("testid"));                  // Имеет атрибут
element.shouldBe(hasDataAttribute("status", "active"));        // Атрибут со значением

// Regex для атрибутов
element.shouldBe(attributeMatches("id", "user-\\d+"));

// CSS свойства
element.shouldBe(hasCssValue("display", "block"));
element.shouldBe(cssValueMatches("font-size", "\\d+px"));

// Состояния
element.shouldBe(beDisabled());     // Отключен
element.shouldBe(beEnabled());      // Активен
element.shouldBe(beReadonly());     // Только для чтения
element.shouldBe(notBeReadonly());  // Редактируемый
element.shouldBe(beRequired());     // Обязательное поле
element.shouldBe(beChecked());      // Отмечен (checkbox/radio)
element.shouldBe(beUnchecked());    // Не отмечен

// Пустота
element.shouldBe(beEmpty());        // Пустой (нет текста и value)
element.shouldBe(notBeEmpty());     // Не пустой

// Placeholder
element.shouldBe(hasPlaceholder("Введите email"));
element.shouldBe(placeholderContains("email"));

// Тип input
element.shouldBe(hasType("email"));

// Viewport
element.shouldBe(inViewport());     // Элемент в видимой области
```

### HexCollectionConditions — условия для коллекций

```java
import static com.company.hex.ui.conditions.HexCollectionConditions.*;

// Все/любой/ни один по предикату
elements.shouldHave(allMatch(el -> el.isEnabled(), "все активны"));
elements.shouldHave(anyMatch(el -> el.getText().contains("New"), "есть новые"));
elements.shouldHave(noneMatch(el -> el.getText().isEmpty(), "нет пустых"));

// Классы
elements.shouldHave(allHaveClass("item"));
elements.shouldHave(noneHaveClass("hidden"));

// Текст
elements.shouldHave(allContainText("Product"));
elements.shouldHave(anyContainsText("Sale"));
elements.shouldHave(allHaveExactText("Active"));

// Размер
elements.shouldHave(sizeInRange(5, 10));      // От 5 до 10
elements.shouldHave(sizeGreaterThan(0));      // Больше 0
elements.shouldHave(sizeLessThan(100));       // Меньше 100

// Состояния
elements.shouldHave(allVisible());
elements.shouldHave(allHidden());
elements.shouldHave(allEnabled());
elements.shouldHave(allDisabled());

// Атрибуты
elements.shouldHave(allHaveAttribute("data-loaded", "true"));
elements.shouldHave(allHaveAttribute("data-id"));
```

### Создание кастомных условий

#### Для одиночного элемента

```java
public static WebElementCondition hasMinLength(int minLength) {
    return new WebElementCondition("имеет минимальную длину " + minLength) {
        @Override
        public CheckResult check(Driver driver, WebElement element) {
            String value = element.getAttribute("value");
            if (value != null && value.length() >= minLength) {
                return CheckResult.accepted();
            }
            return CheckResult.rejected(
                "Длина = " + (value != null ? value.length() : 0), 
                value
            );
        }
    };
}

// Использование
input.shouldBe(hasMinLength(8));
```

#### Для коллекции

```java
public static WebElementsCondition sortedAscending() {
    return new WebElementsCondition() {
        @Override
        @Nonnull
        public CheckResult check(Driver driver, List<WebElement> elements) {
            List<String> texts = elements.stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
            
            List<String> sorted = new ArrayList<>(texts);
            Collections.sort(sorted);
            
            if (texts.equals(sorted)) {
                return CheckResult.accepted();
            }
            return CheckResult.rejected(
                "Элементы не отсортированы по возрастанию",
                elements
            );
        }
        
        @Override
        public String toString() {
            return "отсортированы по возрастанию";
        }
    };
}

// Использование
elements.shouldHave(sortedAscending());
```

---

## Interceptors

### Что такое Interceptor?

Interceptor — это перехватчик действий с элементами. Позволяет выполнять код:
- **До** действия (`beforeAction`)
- **После** успешного действия (`afterAction`)
- **При ошибке** (`onError`)

### Интерфейс ElementInterceptor

```java
public interface ElementInterceptor {
    
    // Вызывается до действия
    default void beforeAction(BaseElement element, String actionName, Object[] args) {}
    
    // Вызывается после успешного действия
    default void afterAction(BaseElement element, String actionName, Object result) {}
    
    // Вызывается при ошибке
    default void onError(BaseElement element, String actionName, Exception exception) {}
    
    // Приоритет (меньше = раньше выполняется)
    default int getOrder() { return 100; }
}
```

### Встроенные Interceptors

#### ScreenshotOnErrorInterceptor

Автоматически создает скриншот при ошибке и прикрепляет его к Allure отчету.

```java
@BeforeAll
static void setUp() {
    ElementInterceptorRegistry.register(new ScreenshotOnErrorInterceptor());
}

@AfterAll
static void tearDown() {
    ElementInterceptorRegistry.clear();
}
```

#### MetricsInterceptor

Собирает статистику по действиям: количество вызовов, ошибки, время выполнения.

```java
MetricsInterceptor metrics = new MetricsInterceptor();
ElementInterceptorRegistry.register(metrics);

// После тестов
Map<String, Long> actionCounts = metrics.getActionCounts();
Map<String, Long> errorCounts = metrics.getErrorCounts();

logger.info("Всего кликов: {}", actionCounts.get("click"));
logger.info("Всего ошибок: {}", metrics.getTotalErrors());

// Сброс метрик
metrics.reset();
```

### Создание кастомного Interceptor

#### Пример: Логирование времени выполнения

```java
public class TimingInterceptor implements ElementInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(TimingInterceptor.class);
    private final ThreadLocal<Long> startTime = new ThreadLocal<>();
    
    @Override
    public void beforeAction(BaseElement element, String actionName, Object[] args) {
        startTime.set(System.currentTimeMillis());
    }
    
    @Override
    public void afterAction(BaseElement element, String actionName, Object result) {
        long duration = System.currentTimeMillis() - startTime.get();
        startTime.remove();
        
        if (duration > 1000) {
            logger.warn("Медленное действие '{}' на '{}': {}ms", 
                actionName, element.getName(), duration);
        }
    }
    
    @Override
    public int getOrder() {
        return 50; // Выполняется перед стандартными interceptors
    }
}
```

#### Пример: Retry при стейл-элементах

```java
public class StaleElementRetryInterceptor implements ElementInterceptor {
    
    private static final int MAX_RETRIES = 3;
    
    @Override
    public void onError(BaseElement element, String actionName, Exception exception) {
        if (exception instanceof StaleElementReferenceException) {
            logger.info("Stale element detected, retry will be attempted");
            // Логика retry обычно реализуется на уровне action
        }
    }
}
```

### Регистрация и управление Interceptors

```java
// Регистрация
ElementInterceptorRegistry.register(new ScreenshotOnErrorInterceptor());
ElementInterceptorRegistry.register(new MetricsInterceptor());

// Удаление конкретного
ElementInterceptorRegistry.unregister(myInterceptor);

// Удаление по типу
ElementInterceptorRegistry.unregisterByType(MetricsInterceptor.class);

// Очистка всех
ElementInterceptorRegistry.clear();

// Проверки
boolean hasAny = ElementInterceptorRegistry.hasInterceptors();
int count = ElementInterceptorRegistry.count();
List<ElementInterceptor> all = ElementInterceptorRegistry.getAll();
```

---

## Builder API

Builder API позволяет создавать элементы программно без аннотаций. Полезен для:
- Динамических элементов с параметризованными локаторами
- Создания элементов в runtime
- Сложных составных локаторов

### Простой Builder

```java
import com.company.hex.ui.builder.InputBuilder;
import com.company.hex.ui.builder.ButtonBuilder;

// Создание Input
Input email = InputBuilder.create("//input[@type='email']")
    .withName("Email Field")
    .build();

// Создание Button
Button submit = ButtonBuilder.create("//button[@type='submit']")
    .withName("Submit Button")
    .build();
```

### Составной локатор

```java
Input fieldInForm = InputBuilder.create()
    .withName("Email в форме регистрации")
    .locator()
        .base("//form[@id='registration']")
        .append("//div[@class='field-group']")
        .append("//input[@name='email']")
        .build()
    .build();
```

### Параметризованный локатор

```java
// Определение builder с параметром
InputBuilder dynamicField = InputBuilder.create()
    .withName("Динамическое поле")
    .locator()
        .base("//div[@data-field='{fieldName}']")
        .append("//input")
        .build();

// Использование с разными параметрами
dynamicField.resolve("fieldName", "email").fill("user@example.com");
dynamicField.resolve("fieldName", "phone").fill("+7 999 123-45-67");

// Несколько параметров
ButtonBuilder tableCellButton = ButtonBuilder.create()
    .withName("Кнопка в ячейке")
    .locator()
        .base("//tr[@data-id='{rowId}']")
        .append("//td[@data-col='{colName}']")
        .append("//button[@data-action='{action}']")
        .build();

// Подстановка всех параметров
tableCellButton.resolve("rowId", "user-123", "colName", "actions", "action", "edit").click();

// Или через Map
tableCellButton.resolve(Map.of(
    "rowId", "user-456",
    "colName", "status",
    "action", "activate"
)).click();
```

### Кастомная реализация элемента

```java
// Использование кастомного типа элемента
CustomButton customBtn = InputBuilder.create("//button[@class='custom']")
    .withName("Custom Button")
    .as(CustomButton.class);

// Или через withCustomImplementation
Button btn = ButtonBuilder.create("//button")
    .withName("Button")
    .withCustomImplementation(CustomButton.class)
    .build();
```

### Настройка таймаутов

```java
Input slowField = InputBuilder.create("//input[@id='slow']")
    .withName("Slow Input")
    .withTimeout(Duration.ofSeconds(30))
    .withPollingInterval(Duration.ofMillis(500))
    .build();
```

---

## Конфигурация

### UiConfig — интерфейс конфигурации

Конфигурация управляется через библиотеку Owner. Файлы загружаются в порядке приоритета:

1. `META-INF/hex-defaults.properties` (базовые значения)
2. `hex.properties` (основные настройки проекта)
3. `hex-${hex.environment}.properties` (настройки окружения)
4. Системные переменные окружения
5. Системные свойства Java

### Все доступные параметры

```properties
# ===== Браузер =====
hex.ui.browser=chrome          # chrome, firefox, edge, safari
hex.ui.browser.version=        # Конкретная версия (опционально)
hex.ui.headless=false          # Запуск без UI
hex.ui.grid.url=               # URL Selenium Grid (опционально)

# ===== Размер окна =====
hex.ui.window.width=1920
hex.ui.window.height=1080

# ===== URL =====
hex.ui.base.url=http://localhost:3000

# ===== Таймауты =====
hex.ui.timeout=10              # Таймаут ожидания элементов (секунды)
hex.ui.polling.interval=200    # Интервал опроса (миллисекунды)
hex.ui.page.load.timeout=30    # Таймаут загрузки страницы
hex.ui.implicit.timeout=5      # Неявный таймаут

# ===== Скриншоты и видео =====
hex.ui.screenshots.enabled=true
hex.ui.screenshots.folder=target/screenshots
hex.ui.video.enabled=false
hex.ui.video.folder=target/videos

# ===== Логирование =====
hex.ui.logging.enabled=true
hex.ui.logging.level=INFO      # OFF, SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST, ALL

# ===== Дополнительно =====
hex.ui.highlight.enabled=false # Подсветка элементов при взаимодействии
hex.ui.action.delay=0          # Задержка между действиями (мс)
hex.ui.page.load.strategy=normal  # none, normal, eager
```

### Использование конфигурации в коде

```java
import com.company.hex.ui.config.UiConfig;
import com.company.hex.core.config.HexConfigFactory;

// Получение конфигурации
UiConfig config = HexConfigFactory.getConfig(UiConfig.class);

// Использование параметров
String browser = config.browser();
boolean headless = config.headless();
int timeout = config.uiTimeout();
String baseUrl = config.baseUrl();
```

### Переопределение через переменные окружения

```bash
# Linux/Mac
export HEX_UI_BROWSER=firefox
export HEX_UI_HEADLESS=true

# Windows
set HEX_UI_BROWSER=firefox
set HEX_UI_HEADLESS=true
```

### Переопределение через системные свойства

```bash
mvn test -Dhex.ui.browser=firefox -Dhex.ui.headless=true
```

---

## Создание кастомных элементов

### Когда нужен кастомный элемент?

- Специфичный UI компонент (карусель, слайдер, редактор)
- Дополнительная бизнес-логика
- Специальные методы проверки
- Интеграция с конкретным UI фреймворком (Ant Design, Material UI)

### Шаг 1: Создание класса

```java
package com.company.hex.project.elements;

import com.codeborne.selenide.SelenideElement;
import com.company.hex.ui.core.BaseElement;
import io.qameta.allure.Step;

/**
 * Кастомный слайдер для выбора значения в диапазоне.
 */
public class Slider extends BaseElement {
    
    // Обязательный конструктор!
    public Slider(String name, SelenideElement element) {
        super(name, element);
    }
    
    @Step("Установить значение слайдера '{this.name}' на {value}")
    public Slider setValue(int value) {
        executeWithInterceptors("setValue", new Object[]{value}, () -> {
            logger.info("Установка значения {} для слайдера '{}' {}", value, name, getContext());
            
            // Логика установки значения
            SelenideElement handle = element.$(".slider-handle");
            SelenideElement track = element.$(".slider-track");
            
            // Вычисление позиции и клик
            int trackWidth = track.getRect().getWidth();
            int minValue = getMinValue();
            int maxValue = getMaxValue();
            int position = (value - minValue) * trackWidth / (maxValue - minValue);
            
            track.click(com.codeborne.selenide.ClickOptions
                .usingDefaultMethod()
                .offset(position - trackWidth/2, 0));
        });
        return this;
    }
    
    @Step("Получить текущее значение слайдера '{this.name}'")
    public int getValue() {
        logger.debug("Получение значения слайдера '{}' {}", name, getContext());
        String value = element.$("input").getValue();
        return Integer.parseInt(value);
    }
    
    public int getMinValue() {
        return Integer.parseInt(element.getAttribute("data-min"));
    }
    
    public int getMaxValue() {
        return Integer.parseInt(element.getAttribute("data-max"));
    }
    
    @Step("Слайдер '{this.name}' должен иметь значение {expected}")
    public Slider shouldHaveValue(int expected) {
        int actual = getValue();
        if (actual != expected) {
            throw new AssertionError(String.format(
                "Слайдер '%s' должен иметь значение %d, но имеет %d",
                name, expected, actual));
        }
        return this;
    }
}
```

### Шаг 2: Использование кастомного элемента

```java
@Page(url = "/settings", title = "Настройки")
public class SettingsPage extends BasePage {
    
    @Element(name = "Громкость", xpath = "//div[@class='volume-slider']")
    public Slider volumeSlider;
    
    @Step("Установить громкость")
    public SettingsPage setVolume(int level) {
        volumeSlider.setValue(level);
        return this;
    }
}
```

### Шаг 3: JIT-регистрация

Фреймворк автоматически регистрирует кастомные элементы при первом использовании (JIT — Just-In-Time). Никакой дополнительной настройки не требуется, если класс имеет правильный конструктор `(String, SelenideElement)`.

### Расширение существующего элемента

```java
public class HighlightedButton extends Button {
    
    public HighlightedButton(String name, SelenideElement element) {
        super(name, element);
    }
    
    @Override
    @Step("Клик по кнопке '{this.name}' с подсветкой")
    public Button click() {
        // Добавляем подсветку перед кликом
        executeWithInterceptors("click", new Object[]{}, () -> {
            highlightElement();
            element.click();
        });
        return this;
    }
    
    private void highlightElement() {
        com.codeborne.selenide.Selenide.executeJavaScript(
            "arguments[0].style.boxShadow = '0 0 10px red'",
            element.toWebElement()
        );
        try {
            Thread.sleep(200);
        } catch (InterruptedException ignored) {}
    }
}
```

---

## Лучшие практики

### Организация кода

```
src/main/java/com/company/project/
├── pages/                    # Page Objects
│   ├── LoginPage.java
│   ├── HomePage.java
│   └── users/
│       ├── UsersListPage.java
│       └── UserProfilePage.java
├── components/               # Переиспользуемые компоненты
│   ├── HeaderComponent.java
│   ├── FooterComponent.java
│   └── modals/
│       ├── ConfirmModal.java
│       └── ErrorModal.java
├── elements/                 # Кастомные элементы
│   ├── Slider.java
│   └── RichTextEditor.java
├── conditions/               # Кастомные условия
│   └── ProjectConditions.java
└── config/                   # Конфигурация
    └── HexConfigs.java
```

### Правила именования

```java
// Страницы: существительное + Page
LoginPage, UserProfilePage, OrderDetailsPage

// Компоненты: существительное + Component
HeaderComponent, NavigationComponent, FilterPanelComponent

// Элементы: описательное имя в аннотации
@Element(name = "Кнопка отправки формы", ...)
@Element(name = "Поле email", ...)
@Element(name = "Выбор страны", ...)

// Методы: глагол + объект + опционально where/how
login(user, password)
searchByName(name)
filterByStatus(status)
verifyUserIsDisplayed(userId)
```

### Рекомендации по локаторам

```java
// ✅ Хорошо: стабильные селекторы
@Element(name = "Login", xpath = "//button[@data-testid='login-btn']")
@Element(name = "Email", css = "input[name='email']")
@Element(name = "Username", xpath = "//input[@id='username']")

// ❌ Плохо: хрупкие селекторы
@Element(name = "Login", xpath = "//div[3]/button")
@Element(name = "Email", css = "div > div > input:nth-child(2)")
@Element(name = "Username", xpath = "//input[contains(@class, 'sc-1234')]")

// ✅ Относительные локаторы в компонентах
@Element(name = "Logo", xpath = ".//img[@class='logo']")

// ❌ Абсолютные локаторы в компонентах
@Element(name = "Logo", xpath = "//header//img[@class='logo']")
```

### Fluent API и цепочки вызовов

```java
// ✅ Хорошо: цепочки методов
loginPage
    .open()
    .verifyPageElements()
    .login("admin", "password");

userProfile
    .updatePersonalInfo()
    .fillFirstName("Иван")
    .fillLastName("Петров")
    .selectCountry("Россия")
    .save();

// ✅ Хорошо: цепочки проверок
email
    .shouldBe(visible)
    .shouldBe(enabled)
    .shouldHave(attribute("placeholder", "Email"));

// ❌ Плохо: отдельные вызовы для простых операций
loginPage.open();
loginPage.verifyPageElements();
loginPage.login("admin", "password");
```

### Переиспользование компонентов

```java
// ✅ Хорошо: общие компоненты вынесены
public abstract class AuthenticatedPage extends BasePage {
    
    @Component(name = "Header", root = "//header")
    public HeaderComponent header;
    
    @Component(name = "Sidebar", root = "//aside")
    public SidebarComponent sidebar;
    
    public void logout() {
        header.openUserMenu();
        header.clickLogout();
    }
}

@Page(url = "/dashboard", title = "Dashboard")
public class DashboardPage extends AuthenticatedPage {
    // Специфичные элементы dashboard
}

@Page(url = "/settings", title = "Settings")
public class SettingsPage extends AuthenticatedPage {
    // Специфичные элементы settings
}
```

---

## Решение типичных задач

### Работа с модальными окнами

```java
public class ConfirmModal extends BaseComponent {
    
    @Element(name = "Заголовок", xpath = ".//h2")
    public TextElement title;
    
    @Element(name = "Сообщение", xpath = ".//p[@class='message']")
    public TextElement message;
    
    @Element(name = "Подтвердить", xpath = ".//button[@data-action='confirm']")
    public Button confirmButton;
    
    @Element(name = "Отмена", xpath = ".//button[@data-action='cancel']")
    public Button cancelButton;
    
    @Step("Подтвердить действие")
    public void confirm() {
        confirmButton.shouldBe(visible);
        confirmButton.click();
    }
    
    @Step("Отменить действие")
    public void cancel() {
        cancelButton.click();
    }
    
    @Step("Проверить текст сообщения")
    public ConfirmModal verifyMessage(String expected) {
        message.shouldHaveText(expected);
        return this;
    }
}

// Использование на странице
@Component(name = "Модальное окно подтверждения", root = "//div[@class='modal-confirm']")
public ConfirmModal confirmModal;

public void deleteUser(String userId) {
    usersTable.getRow(userId).clickAction("Удалить");
    confirmModal.verifyMessage("Вы уверены?");
    confirmModal.confirm();
}
```

### Работа с вложенными фреймами

```java
@Step("Переключиться на фрейм и заполнить данные")
public void fillIframeForm(String name, String email) {
    // Переключение на фрейм
    Selenide.switchTo().frame("payment-frame");
    
    // Работа с элементами внутри фрейма
    $x("//input[@name='name']").setValue(name);
    $x("//input[@name='email']").setValue(email);
    
    // Возврат к основному контенту
    Selenide.switchTo().defaultContent();
}
```

### Ожидание AJAX-загрузки

```java
@Step("Дождаться загрузки данных")
public void waitForDataLoad() {
    // Ждем исчезновения спиннера
    $x("//div[@class='loading-spinner']").shouldBe(hidden);
    
    // Или ждем появления данных
    $x("//table[@data-loaded='true']").shouldBe(visible);
}

// Условие для проверки отсутствия активных запросов
public static WebElementCondition noActiveRequests() {
    return new WebElementCondition("нет активных AJAX запросов") {
        @Override
        public CheckResult check(Driver driver, WebElement element) {
            Long activeRequests = (Long) Selenide.executeJavaScript(
                "return window.jQuery ? jQuery.active : 0"
            );
            if (activeRequests == 0) {
                return CheckResult.accepted();
            }
            return CheckResult.rejected("Активных запросов: " + activeRequests, activeRequests);
        }
    };
}
```

### Работа с файлами

```java
// Загрузка файла
@Element(name = "Загрузка файла", xpath = "//input[@type='file']")
public Input fileUpload;

@Step("Загрузить файл")
public void uploadFile(File file) {
    fileUpload.getElement().uploadFile(file);
}

// Скачивание файла
@Element(name = "Кнопка скачивания", xpath = "//button[@id='download']")
public Button downloadButton;

@Step("Скачать отчет")
public File downloadReport() {
    return downloadButton.getElement().download();
}
```

### Drag and Drop

```java
@Step("Переместить элемент")
public void dragAndDrop(String sourceId, String targetId) {
    SelenideElement source = $x("//div[@data-id='" + sourceId + "']");
    SelenideElement target = $x("//div[@data-id='" + targetId + "']");
    
    Selenide.actions()
        .dragAndDrop(source, target)
        .perform();
}
```

### Скриншоты вручную

```java
@Step("Сделать скриншот текущего состояния")
public void takeScreenshot(String name) {
    byte[] screenshot = Selenide.screenshot(OutputType.BYTES);
    Allure.addAttachment(name, "image/png", 
        new ByteArrayInputStream(screenshot), ".png");
}
```

### Выполнение JavaScript

```java
@Step("Прокрутить страницу вниз")
public void scrollToBottom() {
    Selenide.executeJavaScript("window.scrollTo(0, document.body.scrollHeight)");
}

@Step("Получить значение из localStorage")
public String getLocalStorageValue(String key) {
    return Selenide.executeJavaScript(
        "return localStorage.getItem(arguments[0])", key
    );
}

@Step("Установить значение в localStorage")
public void setLocalStorageValue(String key, String value) {
    Selenide.executeJavaScript(
        "localStorage.setItem(arguments[0], arguments[1])", key, value
    );
}
```

### Работа с cookies

```java
@Step("Добавить cookie авторизации")
public void addAuthCookie(String token) {
    Cookie cookie = new Cookie("auth_token", token, "/");
    WebDriverRunner.getWebDriver().manage().addCookie(cookie);
}

@Step("Получить все cookies")
public Set<Cookie> getAllCookies() {
    return WebDriverRunner.getWebDriver().manage().getCookies();
}

@Step("Очистить cookies")
public void clearCookies() {
    WebDriverRunner.getWebDriver().manage().deleteAllCookies();
}
```

---

## Заключение

Hex UI Framework предоставляет мощный и гибкий инструментарий для UI-тестирования:

- **Декларативный подход** сокращает количество кода и улучшает читаемость
- **Типобезопасность** помогает находить ошибки на этапе компиляции
- **Расширяемость** позволяет адаптировать фреймворк под любые требования
- **Интеграция с Allure** обеспечивает детальное документирование тестов
- **Thread-safety** позволяет безопасно запускать тесты параллельно

При возникновении вопросов обращайтесь к примерам в модуле `hex-project-samples` или к документации в папке `docs/`.