# Builder API для создания UI элементов

Это руководство описывает использование Builder API для создания UI элементов с динамическими и составными локаторами в Hex Framework.

## Содержание

1. [Обзор](#обзор)
2. [Простые локаторы](#простые-локаторы)
3. [Составные локаторы](#составные-локаторы)
4. [Параметризованные локаторы](#параметризованные-локаторы)
5. [Многоуровневые параметризованные локаторы](#многоуровневые-параметризованные-локаторы)
6. [Кастомные элементы](#кастомные-элементы)
7. [Лучшие практики](#лучшие-практики)

---

## Обзор

Builder API предоставляет fluent интерфейс для создания UI элементов с поддержкой:
- ✅ Простых локаторов
- ✅ Составных локаторов (из нескольких частей)
- ✅ Параметризованных локаторов (с плейсхолдерами)
- ✅ Многоуровневых параметризованных локаторов
- ✅ Кастомных реализаций элементов

### Когда использовать Builder API?

**Используйте аннотации `@Element`** для:
- Статических элементов с фиксированными локаторами
- Простых случаев без параметризации

**Используйте Builder API** для:
- Динамических элементов с параметрами
- Составных локаторов из нескольких частей
- Элементов с кастомной реализацией
- Сложных многоуровневых структур

---

## Простые локаторы

### Короткий синтаксис

Для простых локаторов используйте короткий синтаксис:

```java
import static com.company.hex.ui.builder.Elements.*;

public class LoginPage extends BasePage {
    
    // Простой input
    private final Input email = input("//input[@type='email']")
        .withName("Email Field")
        .build();
    
    // Простая кнопка
    private final Button submit = button("//button[@type='submit']")
        .withName("Submit Button")
        .build();
    
    // Checkbox
    private final Checkbox terms = checkbox("//input[@type='checkbox'][@id='terms']")
        .withName("Accept Terms")
        .build();
    
    // Select
    private final Select country = select("//select[@id='country']")
        .withName("Country Selector")
        .build();
}
```

### Использование

```java
@Test
public void testSimpleElements() {
    LoginPage page = new LoginPage();
    
    page.email.fill("user@example.com");
    page.submit.click();
    page.terms.check();
    page.country.selectByText("Russia");
}
```

---

## Составные локаторы

Составные локаторы позволяют строить XPath из нескольких частей.

### Пример

```java
public class FormPage extends BasePage {
    
    // Составной локатор из 3 частей
    private final Input compositeField = input()
        .withName("Composite Field")
        .locator()
            .base("//div[@class='form-container']")
            .append("//section[@id='personal-info']")
            .append("//input[@data-field='email']")
            .build()
        .build();
    
    // Результирующий XPath:
    // //div[@class='form-container']//section[@id='personal-info']//input[@data-field='email']
}
```

### Преимущества

- **Читаемость**: Локатор разбит на логические части
- **Переиспользование**: Базовую часть можно переиспользовать
- **Поддержка**: Легче найти и исправить проблемы

---

## Параметризованные локаторы

Параметризованные локаторы используют плейсхолдеры `{paramName}` для динамической подстановки значений.

### Пример с одним параметром

```java
public class UserPage extends BasePage {
    
    // Динамическое поле с параметром {field}
    private final InputBuilder dynamicFieldBuilder = input()
        .withName("Dynamic Field")
        .locator()
            .base("//div[@class='form-section']")
            .append("//input[@data-field='{field}']")
            .build();
    
    @Step("Заполнить поле {fieldName} значением {value}")
    public UserPage fillField(String fieldName, String value) {
        dynamicFieldBuilder.resolve("field", fieldName).fill(value);
        return this;
    }
}
```

### Использование

```java
@Test
public void testDynamicFields() {
    UserPage page = new UserPage();
    
    // Один элемент используется для разных полей
    page.fillField("firstName", "Иван");
    page.fillField("lastName", "Петров");
    page.fillField("email", "ivan@example.com");
    page.fillField("phone", "+7 999 123-45-67");
}
```

### Важно

- **Храните builder, а не элемент**: `InputBuilder`, а не `Input`
- **Вызывайте resolve() при использовании**: Создает новый элемент с подставленными параметрами
- **Параметры передаются парами**: `"key", "value", "key2", "value2"`

---

## Многоуровневые параметризованные локаторы

Для сложных вложенных структур используйте несколько параметров.

### Пример с двумя параметрами

```java
public class SettingsPage extends BasePage {
    
    // Локатор с 2 параметрами: {section} и {field}
    private final InputBuilder multiLevelFieldBuilder = input()
        .withName("Multi-Level Field")
        .locator()
            .base("//div[@class='settings-container']")
            .append("//section[@data-section='{section}']")
            .append("//div[@class='field-group']")
            .append("//input[@data-field='{field}']")
            .build();
    
    @Step("Заполнить поле {field} в секции {section}")
    public SettingsPage fillSectionField(String section, String field, String value) {
        multiLevelFieldBuilder.resolve(
            "section", section,
            "field", field
        ).fill(value);
        return this;
    }
}
```

### Использование

```java
@Test
public void testMultiLevelLocators() {
    SettingsPage page = new SettingsPage();
    
    // Заполнение полей в разных секциях
    page.fillSectionField("profile", "email", "user@example.com");
    page.fillSectionField("profile", "phone", "+7 999 123-45-67");
    page.fillSectionField("address", "city", "Москва");
    page.fillSectionField("address", "street", "Тверская");
}
```

### Пример с таблицей (3 параметра)

```java
public class DataTablePage extends BasePage {
    
    // Кнопка в ячейке таблицы с 3 параметрами
    private final ButtonBuilder tableCellButtonBuilder = button()
        .withName("Table Cell Button")
        .locator()
            .base("//table[@id='data-table']")
            .append("//tr[@data-row-id='{rowId}']")
            .append("//td[@data-column='{column}']")
            .append("//button[@data-action='{action}']")
            .build();
    
    @Step("Кликнуть кнопку {action} в строке {rowId}, колонке {column}")
    public DataTablePage clickTableButton(String rowId, String column, String action) {
        tableCellButtonBuilder.resolve(
            "rowId", rowId,
            "column", column,
            "action", action
        ).click();
        return this;
    }
}
```

### Использование

```java
@Test
public void testTableOperations() {
    DataTablePage page = new DataTablePage();
    
    // Клики по разным кнопкам в таблице
    page.clickTableButton("user-123", "actions", "edit");
    page.clickTableButton("user-123", "actions", "delete");
    page.clickTableButton("user-456", "status", "activate");
    page.clickTableButton("user-789", "actions", "view");
}
```

### Очень сложные структуры (4+ параметров)

```java
public class DashboardPage extends BasePage {
    
    // Глубоко вложенная структура с 4 параметрами
    private final InputBuilder complexFieldBuilder = input()
        .withName("Complex Nested Field")
        .locator()
            .base("//div[@class='dashboard']")
            .append("//section[@data-section='{section}']")
            .append("//div[@class='widget'][@data-widget='{widget}']")
            .append("//form[@data-form='{form}']")
            .append("//input[@data-field='{field}']")
            .build();
    
    @Step("Заполнить поле в dashboard")
    public DashboardPage fillDashboardField(
            String section, String widget, String form, String field, String value) {
        complexFieldBuilder.resolve(
            "section", section,
            "widget", widget,
            "form", form,
            "field", field
        ).fill(value);
        return this;
    }
}
```

---

## Кастомные элементы

Builder API поддерживает использование кастомных реализаций элементов.

### Создание кастомного элемента

```java
public class RichTextEditor extends BaseElement {
    
    public RichTextEditor(String name, SelenideElement element) {
        super(name, element);
    }
    
    @Step("Установить HTML контент в '{this.name}'")
    public RichTextEditor setHtmlContent(String html) {
        logger.info("Установка HTML в '{}' {}", name, getContext());
        executeScript("arguments[0].innerHTML = arguments[1]", element, html);
        return this;
    }
    
    @Step("Форматировать текст как жирный в '{this.name}'")
    public RichTextEditor formatBold() {
        element.$("button[data-action='bold']").click();
        return this;
    }
}
```

### Регистрация в ElementFactory

```java
// В статическом блоке или при инициализации приложения
ElementFactory.register(RichTextEditor.class, RichTextEditor::new);
```

### Использование с Builder API

```java
public class ArticlePage extends BasePage {
    
    // Способ 1: Через withCustomImplementation()
    private final RichTextEditor editor1 = input("//div[@class='rich-editor']")
        .withName("Article Content")
        .withCustomImplementation(RichTextEditor.class)
        .build();
    
    // Способ 2: Через as() (короче)
    private final RichTextEditor editor2 = element("//div[@class='rich-editor']")
        .withName("Article Content")
        .as(RichTextEditor.class);
    
    // С параметризованным локатором
    private final ElementBuilder<?, ?> editorBuilder = element()
        .withName("Section Editor")
        .locator()
            .base("//section[@id='{section}']")
            .append("//div[@class='rich-editor']")
            .build();
    
    @Step("Редактировать секцию {section}")
    public ArticlePage editSection(String section, String content) {
        RichTextEditor editor = editorBuilder
            .resolve("section", section)
            .as(RichTextEditor.class);
        
        editor.setHtmlContent(content);
        editor.formatBold();
        return this;
    }
}
```

---

## Лучшие практики

### 1. Именование

```java
// ✅ Хорошо: Понятные имена для Allure steps
private final Input email = input("//input[@type='email']")
    .withName("Email Field")
    .build();

// ❌ Плохо: Непонятное имя
private final Input email = input("//input[@type='email']")
    .withName("field1")
    .build();
```

### 2. Хранение builders для параметризованных локаторов

```java
// ✅ Хорошо: Храним builder для resolve()
private final InputBuilder dynamicFieldBuilder = input()
    .withName("Dynamic Field")
    .locator()
        .base("//div[@class='form']")
        .append("//input[@data-field='{field}']")
        .build();

public void fillField(String field, String value) {
    dynamicFieldBuilder.resolve("field", field).fill(value);
}

// ❌ Плохо: Вызываем build() сразу - нельзя использовать resolve()
private final Input dynamicField = input()
    .withName("Dynamic Field")
    .locator()
        .base("//div[@class='form']")
        .append("//input[@data-field='{field}']")
        .build()
    .build(); // ❌ Теперь это Input, а не InputBuilder!
```

### 3. Группировка в бизнес-методы

```java
// ✅ Хорошо: Группируем действия в бизнес-методы
@Step("Заполнить форму пользователя")
public UserPage fillUserForm(String firstName, String lastName, String email) {
    fillField("firstName", firstName);
    fillField("lastName", lastName);
    fillField("email", email);
    return this;
}

// Использование
page.fillUserForm("Иван", "Петров", "ivan@example.com");
```

### 4. Комбинирование с аннотациями

```java
public class MixedPage extends BasePage {
    
    // Статические элементы через аннотации
    @Element(name = "First Name", xpath = "//input[@id='firstName']")
    Input firstName;
    
    @Element(name = "Last Name", xpath = "//input[@id='lastName']")
    Input lastName;
    
    // Динамические элементы через Builder API
    private final InputBuilder dynamicFieldBuilder = input()
        .withName("Dynamic Field")
        .locator()
            .base("//div[@class='form']")
            .append("//input[@data-field='{field}']")
            .build();
}
```

### 5. Читаемость составных локаторов

```java
// ✅ Хорошо: Каждая часть на новой строке
private final Input field = input()
    .withName("User Field")
    .locator()
        .base("//div[@class='container']")
        .append("//section[@id='user-info']")
        .append("//div[@class='field-group']")
        .append("//input[@data-field='email']")
        .build()
    .build();

// ❌ Плохо: Все в одну строку
private final Input field = input().withName("User Field").locator().base("//div[@class='container']").append("//section[@id='user-info']").append("//div[@class='field-group']").append("//input[@data-field='email']").build().build();
```

---

## Примеры из проекта

Полные рабочие примеры смотрите в:

- **Page Object**: [`AdvancedFormPage.java`](src/main/java/com/company/hex/project/pages/AdvancedFormPage.java)
- **Тесты**: [`BuilderApiExamplesTest.java`](src/test/java/com/company/hex/project/tests/ui/BuilderApiExamplesTest.java)

---

## Заключение

Builder API предоставляет мощный и гибкий способ создания UI элементов для сложных сценариев:

✅ **Простота**: Короткий синтаксис для простых случаев  
✅ **Гибкость**: Составные и параметризованные локаторы  
✅ **Читаемость**: Fluent API и понятная структура  
✅ **Переиспользование**: Один builder для множества элементов  
✅ **Расширяемость**: Поддержка кастомных элементов  

Используйте Builder API когда аннотаций `@Element` недостаточно для ваших задач!