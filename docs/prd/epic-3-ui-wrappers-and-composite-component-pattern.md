# Epic 3: Декларативный UI Framework с минимальным boilerplate

**Epic Goal:** Создать декларативный UI framework с гибридным подходом (аннотации + builder API), который минимизирует boilerplate код и обеспечивает автоматическое логирование и Allure steps с полным контекстом.

---

## Story 3.1: Базовые элементы и аннотации

**As a** UI test developer,  
**I want** типобезопасные элементы с декларативным API,  
**So that** я могу описывать Page Objects с минимумом кода.

### Acceptance Criteria:

1. **Базовый класс `BaseElement`**
   - Содержит имя элемента, SelenideElement, logger
   - Хранит контекст: pageName, componentName для логирования
   - Предоставляет базовые методы: click(), getText(), getAttribute()
   - Все методы автоматически создают Allure steps
   - Логирование через SLF4J с полным контекстом: "Action on 'Element' in page 'Page' component 'Component'"

2. **Конкретные типы элементов**
   - `Input`: fill(), clear(), append(), getValue(), shouldHaveValue(), shouldBeEmpty()
   - `Button`: click(), doubleClick(), hover()
   - `Checkbox`: check(), uncheck(), toggle(), isChecked(), shouldBe(checked/unchecked)
   - `Select`: selectByText(), selectByValue(), selectByIndex(), getSelectedText(), getAllOptions()
   - `TextElement`: getText(), shouldHave(text())
   - Все элементы поддерживают fluent API для цепочек вызовов

3. **Аннотация `@Element`**
   - Обязательные параметры: `name` (для Allure steps и логов)
   - Локатор: `xpath` или `css` (один обязателен)
   - Опциональные: `timeout`, `pollingInterval`
   - Пример: `@Element(name = "Username", xpath = "//input[@id='username']")`

4. **Аннотация `@Elements` для коллекций**
   - Те же параметры что и `@Element`
   - Возвращает `ElementList<T>` с методами filter(), find(), map()
   - Пример: `@Elements(name = "Menu Items", xpath = "//li[@class='menu-item']")`

5. **Fluent assertions**
   - Все элементы поддерживают: shouldBe(), shouldHave(), shouldNotHave()
   - Можно передавать несколько условий: `element.shouldBe(visible, enabled)`
   - Возвращают this для цепочек вызовов

6. **Логирование**
   - Каждое действие логируется через SLF4J
   - Формат: `"Action on 'ElementName' in page 'PageName' component 'ComponentName'"`
   - Скриншоты делаются ТОЛЬКО при ошибках через JUnit 5 lifecycle listener
   - НЕТ скриншотов на каждом действии

### Technical Notes:
- Lazy инициализация через Java Proxy
- Thread-safe без shared state
- Интеграция с Selenide для всех операций
- Автоматическая генерация Allure steps через `@Step` аннотацию

---

## Story 3.2: Компоненты с гибким scoping

**As a** framework user,  
**I want** переиспользуемые компоненты с гибким root локатором,  
**So that** я могу использовать один компонент на разных страницах с разными локаторами.

### Acceptance Criteria:

1. **Базовый класс `BaseComponent`**
   - Абстрактный класс для всех компонентов
   - Содержит имя компонента (для логирования)
   - Поддерживает вложенные элементы через `@Element` и `@Elements`
   - Все элементы внутри компонента используют относительные локаторы (начинаются с `.//`)

2. **Аннотация `@Component`**
   - Обязательные параметры: `name` (для логирования и Allure steps), `root` (локатор корневого элемента)
   - Пример: `@Component(name = "Main Header", root = "//header[@id='main-header']")`
   - Root локатор указывается в Page, а не в самом компоненте

3. **Контекстное логирование в компонентах**
   - Все действия в компоненте логируются с именем компонента
   - Формат: `"Action on 'Element' in page 'Page' component 'ComponentName'"`
   - Allure steps включают имя компонента: `"Navigate to home from 'Main Header'"`

4. **Переиспользование компонентов**
   - Один класс компонента можно использовать на разных страницах
   - Root локатор и имя указываются в аннотации `@Component` на странице
   - Пример:
     ```java
     @Component(name = "Main Header", root = "//header[@id='main']")
     HeaderComponent mainHeader;
     
     @Component(name = "Mobile Header", root = "//header[@id='mobile']")
     HeaderComponent mobileHeader;
     ```

5. **Thread-safe дизайн**
   - Нет shared state между компонентами
   - Каждый экземпляр компонента независим
   - Lazy инициализация элементов внутри компонента

6. **Вложенные компоненты**
   - Компоненты могут содержать другие компоненты
   - Root локаторы вложенных компонентов относительны к родительскому

### Technical Notes:
- Компоненты инициализируются через тот же механизм что и Page Objects
- Root локатор передается в контекст при инициализации элементов
- Все локаторы внутри компонента должны быть относительными (`.//`)

---

## Story 3.3: Page Objects с декларативным API

**As a** developer,  
**I want** простой способ создания Page Objects,  
**So that** я могу фокусироваться на бизнес-логике, а не на технических деталях.

### Acceptance Criteria:

1. **Аннотация `@Page`**
   - Параметры: `url` (относительный путь), `title` (для логирования)
   - Пример: `@Page(url = "/login", title = "Login Page")`
   - URL используется для метода `open()`

2. **Базовый класс `BasePage`**
   - Абстрактный класс для всех страниц
   - Автоматическая инициализация всех полей с аннотациями в конструкторе
   - Метод `open()` для открытия страницы
   - Метод `isOpened()` для проверки что страница открыта (опционально)

3. **Минимальный boilerplate**
   - НЕТ необходимости в конструкторах
   - НЕТ необходимости в factory методах
   - НЕТ необходимости в методах-обертках для каждого элемента
   - Прямое использование элементов: `page.username.fill("john")`

4. **Композиция из элементов и компонентов**
   - Page может содержать `@Element`, `@Elements`, `@Component`
   - Все инициализируется автоматически
   - Пример:
     ```java
     @Page(url = "/dashboard", title = "Dashboard")
     public class DashboardPage extends BasePage {
         @Element(name = "Welcome Message", xpath = "//h1[@class='welcome']")
         TextElement welcomeMessage;
         
         @Component(name = "Header", root = "//header")
         HeaderComponent header;
         
         @Elements(name = "Cards", xpath = "//div[@class='card']")
         ElementList<Button> cards;
     }
     ```

5. **Бизнес-методы с явными steps**
   - Для сложной бизнес-логики создаются методы с `@Step`
   - Внутри методов используются элементы напрямую
   - Каждое действие с элементом создает вложенный step
   - Пример:
     ```java
     @Step("Login as {username}")
     public HomePage login(String username, String password) {
         this.username.fill(username);  // вложенный step
         this.password.fill(password);  // вложенный step
         loginButton.click();           // вложенный step
         return new HomePage();
     }
     ```

6. **Примеры в `hex-project-samples`**
   - LoginPage, HomePage, DashboardPage
   - Демонстрация всех возможностей
   - README на русском языке с примерами использования

### Technical Notes:
- Инициализация через `FieldInitializer` в конструкторе `BasePage`
- Поддержка наследования - инициализируются поля всей иерархии классов
- Page name передается в контекст всех элементов для логирования

---

## Story 3.4: Builder API для сложных случаев

**As a** tester,
**I want** fluent builder API для составных локаторов и кастомных элементов,
**So that** я могу элегантно обрабатывать сложные UI структуры на уровне PageObject.

### Acceptance Criteria:

1. **Статические методы-билдеры**
   - `input()` → `InputBuilder`
   - `button()` → `ButtonBuilder`
   - `checkbox()` → `CheckboxBuilder`
   - `select()` → `SelectBuilder`
   - `element()` → `ElementBuilder` (generic)
   - Импортируются статически для удобства

2. **Обязательные методы Builder API**
   - `.withName(String name)` - **обязательно** для Allure steps и логирования
   - Пример:
     ```java
     Input emailField = input()
         .withName("Email Field")
         .locator()
             .base("//div[@class='form-container']")
             .append("//input[@type='email']")
             .build();
     ```

3. **Composite Locator Builder (составные локаторы)**
   - `.locator()` - начало построения составного локатора
   - `.base(String xpath)` - базовая часть локатора
   - `.append(String xpath)` - добавление части локатора
   - `.append(String xpath, String placeholder)` - добавление с плейсхолдером для параметризации
   - `.build()` - завершение построения локатора
   - Пример простого составного локатора:
     ```java
     Button saveButton = button()
         .withName("Save Button")
         .locator()
             .base("//div[@class='form-actions']")
             .append("//button[@type='submit']")
             .build();
     ```
   - Пример с параметризацией:
     ```java
     Input userField = input()
         .withName("User Field")
         .locator()
             .base("//div[@class='user-container']")
             .append("//section[@id='{section}']")
             .append("//input[@data-field='{field}']")
             .build();
     
     // Использование в тесте
     userField.resolve("section", "profile", "field", "email")
         .fill("user@example.com");
     
     userField.resolve("section", "settings", "field", "phone")
         .fill("+1234567890");
     ```
   - Пример сложного многоуровневого локатора:
     ```java
     Button actionButton = button()
         .withName("Dynamic Action Button")
         .locator()
             .base("//div[@class='dashboard']")
             .append("//section[@data-section='{section}']")
             .append("//div[@class='actions-panel']")
             .append("//button[@data-action='{action}']")
             .build();
     
     // Использование
     actionButton.resolve("section", "users", "action", "delete").click();
     actionButton.resolve("section", "reports", "action", "export").click();
     ```

4. **Альтернативный синтаксис для простых случаев**
   - Для простых локаторов без параметризации можно использовать короткий синтаксис:
     ```java
     Input email = input("//input[@type='email']")
         .withName("Email Field");
     
     // Эквивалентно:
     Input email = input()
         .withName("Email Field")
         .locator()
             .base("//input[@type='email']")
             .build();
     ```

5. **Кастомная имплементация элемента**
   - `.withCustomImplementation(Class<? extends BaseElement> implClass)` - использование кастомного класса
   - `.as(Class<T> elementClass)` - более короткий синтаксис
   - Позволяет использовать специализированные элементы (RichTextEditor, DatePicker, etc.)
   - Пример:
     ```java
     RichTextEditor editor = input("//div[@class='rich-editor']")
         .withName("Article Content")
         .withCustomImplementation(RichTextEditor.class);
     
     // Или короче
     DatePicker birthDate = element("//div[@class='datepicker']")
         .withName("Birth Date")
         .as(DatePicker.class);
     
     // С составным локатором
     RichTextEditor dynamicEditor = input()
         .withName("Section Editor")
         .locator()
             .base("//section[@id='{section}']")
             .append("//div[@class='rich-editor']")
             .build()
         .as(RichTextEditor.class);
     
     dynamicEditor.resolve("section", "article").setHtmlContent("<p>Content</p>");
     ```

6. **Комбинирование с аннотациями**
   - В одном Page можно использовать и аннотации, и builder API
   - Builder API для сложных случаев, аннотации для простых
   - Пример:
     ```java
     @Page(url = "/user/profile", title = "User Profile")
     public class UserProfilePage extends BasePage {
         
         // Простые статические элементы через аннотации
         @Element(name = "First Name", xpath = "//input[@id='firstName']")
         Input firstName;
         
         @Element(name = "Last Name", xpath = "//input[@id='lastName']")
         Input lastName;
         
         // Сложные составные локаторы через Builder API
         private final Input dynamicField = input()
             .withName("Dynamic User Field")
             .locator()
                 .base("//div[@class='profile-form']")
                 .append("//div[@data-section='{section}']")
                 .append("//input[@data-field='{field}']")
                 .build();
         
         private final Button actionButton = button()
             .withName("Profile Action")
             .locator()
                 .base("//div[@class='profile-actions']")
                 .append("//button[@data-action='{action}']")
                 .build();
         
         @Step("Update user field: {section}.{fieldName}")
         public UserProfilePage updateField(String section, String fieldName, String value) {
             dynamicField.resolve("section", section, "field", fieldName).fill(value);
             return this;
         }
         
         @Step("Perform action: {action}")
         public UserProfilePage performAction(String action) {
             actionButton.resolve("action", action).click();
             return this;
         }
     }
     
     // Использование в тесте
     userProfilePage
         .updateField("personal", "email", "new@example.com")
         .updateField("personal", "phone", "+1234567890")
         .updateField("address", "city", "New York")
         .performAction("save");
     ```

7. **Примеры использования**
   - Динамические таблицы с многоуровневыми локаторами
   - Параметризованные формы с повторяющимися полями
   - Сложные UI компоненты с вложенной структурой
   - Кастомные элементы (DatePicker, RichTextEditor, AutoComplete)
   - Пример сложной таблицы:
     ```java
     Button cellButton = button()
         .withName("Table Cell Button")
         .locator()
             .base("//table[@id='users']")
             .append("//tr[@data-user-id='{userId}']")
             .append("//td[@data-column='{column}']")
             .append("//button[@data-action='{action}']")
             .build();
     
     // Использование
     cellButton.resolve("userId", "123", "column", "actions", "action", "edit").click();
     cellButton.resolve("userId", "456", "column", "status", "action", "activate").click();
     ```

### Technical Notes:
- Builder возвращает тот же тип элемента ([`Input`](hex-core-ui/src/main/java/com/company/hex/ui/elements/Input.java), [`Button`](hex-core-ui/src/main/java/com/company/hex/ui/elements/Button.java), etc.)
- Метод `.resolve()` создает новый экземпляр элемента с подставленными параметрами
- Thread-safe - каждый вызов `.resolve()` создает независимый контекст
- Плейсхолдеры в локаторах используют формат `{paramName}` для читаемости

---

## Story 3.5: ElementList для работы с коллекциями

As a UI developer,
I want удобный, типобезопасный и «живой» API для коллекций элементов,
So that я могу фильтровать, искать, проверять и обрабатывать списки элементов с минимальным boilerplate, полным контекстом в логах и аккуратными Allure steps.

Acceptance Criteria

1) Класс и типизация
- Реализовать `ElementList<T extends BaseElement>` как тонкую обёртку над Selenide `ElementsCollection`.
- Поддержка «живой» коллекции (ленивая резолвация) и потокобезопасная иммутабельность.
- Реализовать `Iterable<T>` и `Spliterator<T>`.
- Каждый элемент внутри списка лениво оборачивается через `ElementFactory` с именем `"{collectionName}[{index}]"` (индексация 0‑based).
- Хранить контекст `UiContext` (pageName, componentName) для логов и Allure.

2) Методы доступа
- `int size()`
- `T get(int index)` и синоним `nth(int index)`
- `T first()`, `T last()`
- `boolean isEmpty()`
- `T single()` — ожидает ровно один элемент (иначе кидает информативное исключение).

3) Функциональные методы
- «Ожидающие» (через Selenide):
  - `ElementList<T> filterBy(Condition condition)`
  - `Optional<T> findBy(Condition condition)` — использует `elements.findBy()`
- «Гибкие» (через Java-предикаты; работают по снэпшоту):
  - `ElementList<T> filter(Predicate<T> predicate)` — возвращает новый список на основе `snapshot()`
  - `Optional<T> findFirst(Predicate<T> predicate)`
- Потоки и трансформации:
  - `Stream<T> stream()`
  - `Stream<R> map(Function<T, R> mapper)`
  - `void forEach(Consumer<T> action)`
- Утилиты для быстрых извлечений:
  - `List<String> texts()` — прокладка к `elements.texts()`
  - `List<String> attributes(String name)`
  - `List<String> values()` — сахар для `attributes("value")`
- «Эвакуационный люк» и материализация:
  - `ElementsCollection asSelenide()`
  - `List<T> snapshot()` — материализовать текущую выборку в список элементов (фиксирует состав коллекции на момент вызова).

4) Assertions для коллекций
- Мост к `CollectionCondition`:
  - `ElementList<T> shouldHave(CollectionCondition... conditions)`
- Часто используемые шорткаты:
  - `ElementList<T> shouldHaveSize(int expected)`
  - `ElementList<T> shouldBeEmpty()`
  - `ElementList<T> shouldNotBeEmpty()`
- Дополнительные коллекционные условия (минимальный набор):
  - `CollectionConditions.anyMatch(Condition condition)`
  - `CollectionConditions.allMatch(Condition condition)`
  - `CollectionConditions.textsContain(String... parts)` — проверка, что хотя бы один элемент содержит соответствующий текст (или документировать точную семантику).

5) Allure и логирование
- Все публичные операции `ElementList` создают один агрегированный Allure step уровня коллекции (без спама по каждому элементу).
  - Примеры названий шагов: 
    - "Filter 'Product Cards' by condition 'matchText: Premium'"
    - "Get size of 'Product Cards'"
    - "Map over 'Product Cards' → texts()"
- Логи через SLF4J:
  - `INFO` — агрегированные сообщения в духе Allure шага.
  - `DEBUG` — детализированный перебор: индексы, тексты, атрибуты, результаты фильтров (перечисление всех затронутых элементов).
- Скриншоты делаются только при ошибках глобальным JUnit 5 listener (как в проекте), без снимков в успешных шагах.

6) Builder для коллекций (симметрия с 3.4)
- Статические фабрики (импортируются статически):
  - `elements(Class<T> type)` → `ElementListBuilder<T>`
  - Сугары для популярных типов: `buttons()`, `inputs()`, `checkboxes()`, `selects()`
  - Короткий синтаксис: `elements(Class<T> type, String xpathOrCss)`
- Обязательные шаги билдера:
  - `.withName(String name)` — обязательно для контекста Allure/логов
  - `.locator()` → композитный билдер локатора с тем же DSL, что и для одиночных элементов:
    - `.base(String xpathOrCss)`
    - `.append(String xpathOrCss)` 
    - `.append(String xpathOrCss, String placeholder)`
    - `.buildList()` — завершение и возврат `ElementList<T>`
- Плейсхолдеры в локаторе формата `{param}`:
  - `ElementList<T> resolve(Object... nameValuePairs)` — возвращает новый `ElementList<T>` с подставленными значениями.
- Симметрия с `@Elements`:
  - Аннотация `@Elements(name, xpath|css)` создаёт тот же `ElementList<T>` через `ElementFactory`, тип `T` берётся из дженерика поля.

7) Lazy-инициализация и «живость»
- Внутри `ElementList` хранится `Supplier<ElementsCollection>` или ленивый `By` + скопированный root компонента (scope), чтобы коллекция всегда отражала текущее состояние DOM.
- Методы с предикатами Java (`filter`, `findFirst`) работают по `snapshot()` для предсказуемости и производительности, о чём указано в JavaDoc.

8) Именование и индексация
- Индексация 0‑based во всех именах и сообщениях.
- Формат имени элемента в коллекции: `"{collectionName}[{index}]"`.
- Имя коллекции берётся из `@Elements(name = "...")` или `.withName(...)` в билдере.

9) Сообщения об ошибках
- `get(index)` вне диапазона: "Index 5 out of bounds for 'Product Cards' (size=3) in page 'Dashboard' component 'Catalog'".
- `single()` при `size != 1`: 
  - size=0 — "Expected exactly one element in 'X', but found none".
  - size>1 — "Expected exactly one element in 'X', but found N".
- `findBy(Condition)` ничего не нашёл: возвращается `Optional.empty()`; для ассерт‑варианта можно добавить `singleBy(Condition)` (опционально) с аналогичными сообщениями.

10) Потокобезопасность и (не)кэширование
- `ElementList` — неизменяемый объект; любые трансформации возвращают новый инстанс со своим `resolver`.
- По умолчанию — без кэширования `SelenideElement`/`ElementsCollection`; материализация — только через `snapshot()`.

11) Производительность
- Внутренние обходы коллекции (например, `map`, `texts`, `attributes`) выполняются максимально близко к `ElementsCollection` API (использовать `texts()` при возможности).
- Для долгих операций (например, `forEach` с пользовательским действием) — один агрегированный Allure step; внутри дебаг‑лог по каждому элементу.

12) Совместимость с 3.1–3.4
- Работает внутри `@Page`, `@Component`, учитывает относительные локаторы компонентов (начиная с `.//`) и прокидывает `UiContext`.
- Билдер коллекций использует тот же `CompositeLocatorBuilder`, что и одиночные элементы.
- Метод `.resolve()` у `ElementList` ведёт себя так же, как у одиночных элементов: возвращает новый инстанс с подставленными плейсхолдерами.

API эскизы

Интерфейс `ElementList`
```java
public final class ElementList<T extends BaseElement> implements Iterable<T> {
  private final String name;
  private final Supplier<ElementsCollection> resolver;
  private final Class<T> type;
  private final UiContext context;            // pageName, componentName
  private final ElementFactory factory;       // create(Class<T>, String, SelenideElement, UiContext)

  // Доступ
  public int size();
  public boolean isEmpty();
  public T get(int index);
  public T nth(int index);
  public T first();
  public T last();
  public T single();

  // Фильтрация/поиск
  public ElementList<T> filterBy(Condition condition);
  public Optional<T> findBy(Condition condition);
  public ElementList<T> filter(Predicate<T> predicate);     // по snapshot()
  public Optional<T> findFirst(Predicate<T> predicate);     // по snapshot()

  // Потоки и маппинги
  public Stream<T> stream();
  public <R> Stream<R> map(Function<T, R> mapper);
  public void forEach(Consumer<T> action);

  // Утилиты
  public List<String> texts();
  public List<String> attributes(String name);
  public List<String> values();

  // Assertions
  public ElementList<T> shouldHave(CollectionCondition... conditions);
  public ElementList<T> shouldHaveSize(int expected);
  public ElementList<T> shouldBeEmpty();
  public ElementList<T> shouldNotBeEmpty();

  // «Люки»
  public ElementsCollection asSelenide();
  public List<T> snapshot();

  // Параметризация
  public ElementList<T> resolve(Object... nameValuePairs);

  @Override public Iterator<T> iterator();
}
```

Builder коллекций
```java
// Импортируются статически аналогично builder’ам одиночных элементов
public final class ElementListBuilders {
  public static <T extends BaseElement> ElementListBuilder<T> elements(Class<T> type) { ... }
  public static ElementListUntypedBuilder elements(String xpathOrCss) { ... } // затем .as(Class<T>)
  public static ElementListBuilder<Button> buttons() { ... }
  public static ElementListBuilder<Input> inputs() { ... }
  public static ElementListBuilder<Checkbox> checkboxes() { ... }
  public static ElementListBuilder<Select> selects() { ... }
}

public interface ElementListBuilder<T extends BaseElement> {
  ElementListBuilder<T> withName(String name);
  CompositeLocatorBuilder locator(); // тот же интерфейс, что в 3.4
  ElementList<T> buildList();

  // Короткий путь без locator():
  ElementListBuilder<T> at(String xpathOrCss); // эквивалент locator().base(...).buildList()
}

public interface ElementListUntypedBuilder {
  ElementListUntypedBuilder withName(String name);
  CompositeLocatorBuilder locator();
  <T extends BaseElement> ElementList<T> as(Class<T> type); // завершение сборки
  ElementListUntypedBuilder at(String xpathOrCss);
}
```

Примеры использования

1) Через аннотацию
```java
@Elements(name = "Product Cards", xpath = "//div[@class='product']")
ElementList<Button> products;

products.shouldNotBeEmpty()
        .filterBy(visible)
        .shouldHaveSize(10);

Button premium = products.findBy(matchText("Premium"))
                         .orElseThrow();

List<String> names = products.texts();
```

2) Через билдер (простой локатор)
```java
ElementList<Button> products = elements(Button.class, "//div[@class='product']")
    .withName("Product Cards")
    .buildList();

products.filterBy(visible).nth(2).click();
```

3) Через композитный локатор и плейсхолдеры
```java
ElementList<Button> tableActions = buttons()
    .withName("Users Table Actions")
    .locator()
      .base("//table[@id='{tableId}']")
      .append(".//tr[@data-user-id='{userId}']")
      .append(".//button[@data-action='{action}']")
      .buildList();

tableActions.resolve("tableId", "users", "userId", "123", "action", "edit")
            .single()
            .click();
```

4) «Гибкая» фильтрация по снэпшоту с аггрегированными шагами
```java
List<Button> visiblePremium = products.snapshot().stream()
    .filter(b -> b.shouldHave(visible).getText().contains("Premium"))
    .toList();

visiblePremium.forEach(Button::click);
```

Логирование и шаги (семантика)
- Пример INFO шага при `filterBy(matchText("Premium"))`:
  - Allure: "Filter 'Product Cards' by condition 'matchText: Premium'"
  - Лог INFO: "Filter 'Product Cards' by condition 'matchText: Premium' in page 'Dashboard' component 'Catalog'"
  - Лог DEBUG:
    - "Before filter: size=15; items=[Product Cards[0]='Basic' ... Product Cards[14]='Premium XL']"
    - "After filter: size=2; items=[Product Cards[3]='Premium', Product Cards[14]='Premium XL']"

Технические заметки (реализация)
- Внутри `ElementList`:
  - `resolver: Supplier<ElementsCollection>`; для `@Elements` и билдера — создаётся из `By`/`SelenideElement` root компонента.
  - Оборачивание элемента: `factory.create(type, itemName(index), selenideElement, context)`.
  - `filterBy` возвращает новый `ElementList` c `resolver = () -> resolver.get().filter(condition)`.
  - `findBy` — получает `SelenideElement el = resolver.get().findBy(condition)`; при существовании оборачивает; имя можно проставить как первый подходящий индекс, либо `"{name}[?]"` (рекомендуется вычислить индекс через `indexOf(el)` если это не будет дорого).
- Исключения:
  - Свои доменные исключения `UiCollectionException` с полным контекстом.
- JavaDoc:
  - Подчеркнуть разницу между «ожидающими» методами (через Selenide) и «снэпшотными».
- Не дублировать Selenide‑конфигурацию: таймауты/поллинг берутся из глобальных настроек (как зафиксировано в Epics).

Out of Scope
- Любые собственные таймауты/ретраи/ожидания на уровне `ElementList`.
- `ComponentList` (согласовано, не нужен в 3.5).
- Скриншоты на успешных операциях.

Итого
- `ElementList<T>` — типобезопасный, «живой» слой над `ElementsCollection` с аккуратными Allure steps (агрегация) и подробными DEBUG‑логами.
- Симметричный билдер для коллекций (3.4/3.5) c тем же DSL композитных локаторов и `.resolve(...)`.
- Минимальный, но полезный расширенный API: `filterBy/findBy`, `texts/attributes/values`, `single`, `snapshot`, `asSelenide`, шорткаты assertions.
- Полная интеграция с контекстом страницы/компонента и существующей фабрикой элементов.

---

## Story 3.6: Расширяемость и кастомные элементы

**As a** framework user,  
**I want** возможность создавать кастомные элементы,  
**So that** я могу добавлять специфичные для проекта UI компоненты.

### Acceptance Criteria:

1. **Создание кастомных элементов**
   - Наследование от `BaseElement`
   - Добавление специфичных методов
   - Автоматическое логирование и Allure steps
   - Пример:
     ```java
     public class RichTextEditor extends BaseElement {
         public RichTextEditor(String name, SelenideElement element) {
             super(name, element);
         }
         
         @Step("Set HTML content in '{this.name}'")
         public RichTextEditor setHtmlContent(String html) {
             logger.info("Setting HTML in '{}' in page '{}' component '{}'",
                 name, getPageName(), getComponentName());
             executeScript("arguments[0].innerHTML = arguments[1]", element, html);
             return this;
         }
     }
     ```

2. **Регистрация в ElementFactory**
   - Метод `ElementFactory.register(Class, BiFunction)`
   - Позволяет использовать кастомные элементы с аннотациями
   - Пример: `ElementFactory.register(RichTextEditor.class, RichTextEditor::new)`

3. **Кастомные Conditions**
   - Создание специфичных условий для assertions
   - Наследование от `WebElementCondition`
   - Пример:
     ```java
     public static WebElementCondition hasClass(String className) {
         return new WebElementCondition("has class '" + className + "'") {
             @Override
             public boolean test(WebElement element) {
                 return element.getAttribute("class").contains(className);
             }
         };
     }
     ```

4. **Interceptors для расширения функциональности**
   - Interface `ElementInterceptor` с методами: beforeAction, afterAction, onError
   - Регистрация через `ElementInterceptorRegistry.register()`
   - Использование для логирования, метрик, дополнительных проверок

5. **Документация по расширению**
   - Руководство по созданию кастомных элементов
   - Примеры кастомных Conditions
   - Best practices для interceptors

### Technical Notes:
- Кастомные элементы должны следовать тем же принципам что и базовые
- Обязательное логирование с контекстом
- Автоматические Allure steps через `@Step`

---

## Definition of Done

- [ ] Все классы реализованы в `hex-core-ui`
- [ ] Unit тесты покрывают ≥80% кода
- [ ] Примеры в `hex-project-samples` работают
- [ ] README на русском языке с примерами
- [ ] JavaDoc для всех публичных API
- [ ] Интеграция с Allure работает корректно
- [ ] Логирование через SLF4J с полным контекстом
- [ ] Скриншоты только при ошибках через JUnit 5 lifecycle
- [ ] CI pipeline запускает примеры headless
- [ ] Архитектурная документация обновлена

---

## Technical Dependencies

- Java 17+
- Selenide 7.x
- JUnit 5
- Allure 2.x
- SLF4J + Logback
- Maven

---

## Non-Goals (Out of Scope)

- ❌ Миграция со старых проектов (это новый проект с нуля)
- ❌ Поддержка других UI фреймворков кроме Selenide
- ❌ Конкретные компоненты в `hex-core-ui` (только в samples)
- ❌ Скриншоты на каждом действии (только при ошибках)
- ❌ Поддержка старых версий Java (<17)

---

## Success Metrics

- ✅ Сокращение boilerplate кода на ≥40%
- ✅ Время создания нового Page Object ≤5 минут
- ✅ Автоматические Allure steps для всех действий
- ✅ Контекстное логирование (page + component + element)
- ✅ Thread-safe выполнение тестов
- ✅ Положительные отзывы от команд-пользователей

TODO: динамические url-ы
TODO: RU плагин для автокомплита