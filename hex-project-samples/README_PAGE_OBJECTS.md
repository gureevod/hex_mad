# Page Objects с декларативным API - Примеры использования

Этот документ содержит примеры использования Page Objects с декларативным API в Hex Framework.

## Содержание

1. [Введение](#введение)
2. [Основные концепции](#основные-концепции)
3. [Создание Page Object](#создание-page-object)
4. [Работа с элементами](#работа-с-элементами)
5. [Использование компонентов](#использование-компонентов)
6. [Бизнес-методы](#бизнес-методы)
7. [Примеры тестов](#примеры-тестов)
8. [Best Practices](#best-practices)

---

## Введение

Hex Framework предоставляет декларативный API для создания Page Objects с минимальным boilerplate кодом. Основные преимущества:

✅ **Минимум кода** - только аннотации для элементов  
✅ **Автоматические Allure steps** - каждое действие логируется  
✅ **Fluent API** - цепочки вызовов для читаемости  
✅ **Type Safety** - конкретные типы элементов (Input, Button, etc.)  
✅ **Lazy инициализация** - элементы создаются при первом обращении  
✅ **Thread-safe** - без shared state, параллельное выполнение  

---

## Основные концепции

### Аннотация @Page

Каждый Page Object должен быть помечен аннотацией `@Page`:

```java
@Page(url = "/login", title = "Login Page")
public class LoginPage extends BasePage {
    // элементы и методы
}
```

**Параметры:**
- `url` - относительный URL страницы (используется в методе `open()`)
- `title` - заголовок страницы для логирования и идентификации

### Аннотация @Element

Для объявления элементов используется аннотация `@Element`:

```java
@Element(name = "Username", xpath = "//input[@id='username']")
Input username;
```

**Параметры:**
- `name` - **обязательный** - имя элемента для Allure steps и логов
- `xpath` - XPath локатор (один из xpath или css обязателен)
- `css` - CSS локатор (альтернатива xpath)

### Типы элементов

Framework предоставляет типобезопасные элементы:

- **Input** - текстовые поля
- **Button** - кнопки и ссылки
- **Checkbox** - чекбоксы
- **Select** - выпадающие списки
- **TextElement** - текстовые элементы (для чтения)

---

## Создание Page Object

### Простой Page Object

```java
@Page(url = "/login", title = "Login Page")
public class LoginPage extends BasePage {
    
    @Element(name = "Username", xpath = "//input[@id='username']")
    Input username;
    
    @Element(name = "Password", xpath = "//input[@id='password']")
    Input password;
    
    @Element(name = "Login Button", xpath = "//button[@type='submit']")
    Button loginButton;
    
    @Step("Вход как {user}")
    public HomePage login(String user, String pass) {
        username.fill(user);
        password.fill(pass);
        loginButton.click();
        return new HomePage();
    }
}
```

### Page Object с компонентами

```java
@Page(url = "/dashboard", title = "Dashboard")
public class DashboardPage extends BasePage {
    
    // Компонент header
    @Component(name = "Main Header", root = "//header[@id='main-header']")
    public HeaderComponent header;
    
    @Element(name = "Welcome Message", xpath = "//h1[@class='welcome']")
    TextElement welcomeMessage;
    
    @Step("Перейти на главную")
    public HomePage navigateHome() {
        return header.goHome();
    }
}
```

---

## Работа с элементами

### Input (текстовые поля)

```java
@Element(name = "Email", xpath = "//input[@type='email']")
Input email;

// Использование
email.fill("user@example.com");           // Заполнить
email.clear();                            // Очистить
email.append(" additional text");        // Добавить текст
String value = email.getValue();          // Получить значение

// Проверки
email.shouldHaveValue("user@example.com");
email.shouldBeEmpty();
email.shouldBe(visible, enabled);
```

### Button (кнопки)

```java
@Element(name = "Submit", xpath = "//button[@type='submit']")
Button submitButton;

// Использование
submitButton.click();                     // Клик
submitButton.doubleClick();               // Двойной клик

// Проверки
submitButton.shouldBe(visible, enabled);
submitButton.shouldHaveText("Submit");
```

### Checkbox (чекбоксы)

```java
@Element(name = "Accept Terms", xpath = "//input[@type='checkbox']")
Checkbox acceptTerms;

// Использование
acceptTerms.check();                      // Установить
acceptTerms.uncheck();                    // Снять
acceptTerms.toggle();                     // Переключить
boolean isChecked = acceptTerms.isChecked();

// Проверки
acceptTerms.shouldBe(checked);
acceptTerms.shouldBe(unchecked);
```

### Select (выпадающие списки)

```java
@Element(name = "Country", xpath = "//select[@id='country']")
Select country;

// Использование
country.selectByText("United States");
country.selectByValue("US");
country.selectByIndex(0);

// Получение данных
String selected = country.getSelectedText();
List<String> allOptions = country.getAllOptions();

// Проверки
country.shouldHave(selectedText("United States"));
```

### TextElement (текстовые элементы)

```java
@Element(name = "Error Message", xpath = "//div[@class='error']")
TextElement errorMessage;

// Использование
String text = errorMessage.getText();

// Проверки
errorMessage.shouldBe(visible);
errorMessage.shouldHaveText("Invalid credentials");
```

---

## Использование компонентов

### Создание компонента

```java
public class HeaderComponent extends BaseComponent {
    
    @Element(name = "Home Button", xpath = ".//a[@title='home']")
    Button homeButton;
    
    @Element(name = "Logo", xpath = ".//img[@class='logo']")
    TextElement logo;
    
    @Step("Перейти на главную из '{this.componentName}'")
    public HomePage goHome() {
        homeButton.click();
        return new HomePage();
    }
    
    @Step("Проверить отображение логотипа в '{this.componentName}'")
    public HeaderComponent verifyLogoDisplayed() {
        logo.shouldBe(visible);
        return this;
    }
}
```

**Важно:** Все локаторы внутри компонента должны быть **относительными** (начинаться с `.//`)

### Использование компонента в Page

```java
@Page(url = "/dashboard", title = "Dashboard")
public class DashboardPage extends BasePage {
    
    // Один компонент с разными root локаторами
    @Component(name = "Main Header", root = "//header[@id='main-header']")
    public HeaderComponent mainHeader;
    
    @Component(name = "Mobile Header", root = "//header[@id='mobile-header']")
    public HeaderComponent mobileHeader;
    
    public HomePage navigateHome() {
        return mainHeader.goHome();
    }
}
```

---

## Бизнес-методы

### Группировка действий

Бизнес-методы группируют несколько действий в один логический step:

```java
@Step("Заполнить форму владельца: {first} {last}")
public AddOwnerPage fillOwnerForm(String first, String last, String addr, String city, String phone) {
    logger.info("Заполнение формы владельца: {} {}", first, last);
    
    firstName.fill(first);      // вложенный step
    lastName.fill(last);        // вложенный step
    address.fill(addr);         // вложенный step
    city.fill(city);            // вложенный step
    telephone.fill(phone);      // вложенный step
    
    return this;
}
```

**В Allure отчете:**
```
└─ Заполнить форму владельца: John Doe
   ├─ Fill 'First Name' with 'John'
   ├─ Fill 'Last Name' with 'Doe'
   ├─ Fill 'Address' with '123 Main St'
   ├─ Fill 'City' with 'New York'
   └─ Fill 'Telephone' with '1234567890'
```

### Fluent API

Возвращайте `this` или следующую страницу для цепочек вызовов:

```java
// Возврат this для продолжения работы с текущей страницей
@Step("Обновить email на: {newEmail}")
public UserProfilePage updateEmail(String newEmail) {
    email.clear();
    email.fill(newEmail);
    return this;  // возвращаем this
}

// Возврат новой страницы при переходе
@Step("Вход как {user}")
public HomePage login(String user, String pass) {
    username.fill(user);
    password.fill(pass);
    loginButton.click();
    return new HomePage();  // возвращаем новую страницу
}

// Использование в тесте
profile.updateEmail("new@example.com")
       .updatePhone("+1234567890")
       .saveChanges()
       .verifySuccessMessage("Profile updated");
```

---

## Примеры тестов

### Пример 1: Простой тест логина

```java
@Test
@DisplayName("Успешный вход в систему")
public void testSuccessfulLogin() {
    LoginPage loginPage = new LoginPage();
    loginPage.open();
    HomePage homePage = loginPage.login("user@example.com", "password123");
    
    homePage.verifyWelcomeMessage("Welcome, User!");
    homePage.verifyPageLoaded();
}
```

### Пример 2: Работа с формой

```java
@Test
@DisplayName("Добавление нового владельца")
public void testAddOwner() {
    AddOwnerPage addOwnerPage = new AddOwnerPage().open();
    
    addOwnerPage.verifyFormFields();
    
    addOwnerPage.fillOwnerForm(
            "John", "Doe", "123 Main St", "New York", "1234567890"
    );
    
    addOwnerPage.submitForm()
                .verifySuccessMessage("Owner created successfully");
}
```

### Пример 3: Использование компонентов

```java
@Test
@DisplayName("Навигация через компонент Header")
public void testNavigationWithComponent() {
    LoginPage loginPage = new LoginPage();
    loginPage.open();
    HomePage homePage = loginPage.login("user@example.com", "password123");
    
    DashboardPage dashboard = homePage.navigateToDashboard();
    
    // Используем компонент header
    dashboard.header.verifyLogoDisplayed();
    dashboard.header.openProfileMenu();
    
    HomePage home = dashboard.navigateHome();
    home.verifyPageLoaded();
}
```

### Пример 4: Fluent API

```java
@Test
@DisplayName("Обновление профиля пользователя")
public void testUpdateUserProfile() {
    UserProfilePage profile = new UserProfilePage().open();
    
    profile.verifyPageLoaded()
           .updateEmail("newemail@example.com")
           .updatePhone("+1234567890")
           .updateBio("Software Test Engineer")
           .saveChanges()
           .verifySuccessMessage("Profile updated successfully")
           .verifyEmail("newemail@example.com")
           .verifyPhone("+1234567890");
}
```

### Пример 5: Комплексный сценарий

```java
@Test
@DisplayName("Полный путь пользователя")
public void testCompleteUserJourney() {
    // 1. Логин
    LoginPage loginPage = new LoginPage();
    loginPage.open();
    loginPage.verifyPageElements();
    HomePage homePage = loginPage.login("user@example.com", "password123");
    
    // 2. Проверка домашней страницы
    homePage.verifyWelcomeMessage("Welcome, User!")
            .verifyPageLoaded();
    
    // 3. Переход в профиль
    UserProfilePage profile = homePage.navigateToProfile();
    
    // 4. Обновление профиля
    profile.verifyPageLoaded()
           .updateEmail("updated@example.com")
           .saveChanges()
           .verifySuccessMessage("Profile updated successfully");
    
    // 5. Переход на Dashboard
    DashboardPage dashboard = new DashboardPage().open();
    dashboard.verifyPageLoaded()
             .verifyWelcomeMessage("Welcome back!");
    
    // 6. Выход
    HomePage home = new HomePage();
    home.open();
    LoginPage logoutPage = home.logout();
    logoutPage.verifyPageElements();
}
```

---

## Best Practices

### 1. Именование элементов

✅ **Правильно:**
```java
@Element(name = "First Name", xpath = "//input[@id='firstName']")
Input firstName;
```

❌ **Неправильно:**
```java
@Element(xpath = "//input[@id='firstName']")  // name обязателен!
Input firstName;
```

### 2. Локаторы в компонентах

✅ **Правильно:**
```java
public class HeaderComponent extends BaseComponent {
    @Element(name = "Home Button", xpath = ".//a[@title='home']")  // относительный
    Button homeButton;
}
```

❌ **Неправильно:**
```java
public class HeaderComponent extends BaseComponent {
    @Element(name = "Home Button", xpath = "//a[@title='home']")  // абсолютный!
    Button homeButton;
}
```

### 3. Возвращаемые типы методов

✅ **Правильно:**
```java
// Возвращаем this для fluent API
public LoginPage verifyError(String error) {
    errorMessage.shouldHaveText(error);
    return this;
}

// Возвращаем новую страницу при переходе
public HomePage login(String user, String pass) {
    username.fill(user);
    password.fill(pass);
    loginButton.click();
    return new HomePage();
}
```

❌ **Неправильно:**
```java
// void - нельзя использовать в цепочках
public void verifyError(String error) {
    errorMessage.shouldHaveText(error);
}
```

### 4. Группировка steps

✅ **Правильно:**
```java
@Step("Заполнить форму владельца: {first} {last}")
public AddOwnerPage fillOwnerForm(String first, String last, ...) {
    firstName.fill(first);   // вложенный step
    lastName.fill(last);     // вложенный step
    return this;
}
```

❌ **Неправильно:**
```java
// Слишком детальные steps - лучше группировать
public AddOwnerPage fillOwnerForm(String first, String last, ...) {
    firstName.fill(first);
    lastName.fill(last);
    return this;
}
```

### 5. Логирование

✅ **Правильно:**
```java
@Step("Вход как {user}")
public HomePage login(String user, String pass) {
    logger.info("Выполнение входа для пользователя: {}", user);
    username.fill(user);
    password.fill(pass);
    loginButton.click();
    logger.info("Вход выполнен");
    return new HomePage();
}
```

### 6. Проверки

✅ **Правильно:**
```java
// Группируем проверки в отдельный метод
@Step("Проверить отображение элементов страницы")
public LoginPage verifyPageElements() {
    username.shouldBe(visible);
    password.shouldBe(visible);
    loginButton.shouldBe(visible);
    return this;
}
```

### 7. Публичные vs приватные элементы

✅ **Правильно:**
```java
// Элементы, используемые в тестах - public
@Element(name = "Welcome Message", xpath = "//h1[@class='welcome']")
public TextElement welcomeMessage;

// Элементы, используемые только внутри Page - private
@Element(name = "Internal Field", xpath = "//input[@id='internal']")
private Input internalField;
```

---

## Структура проекта

```
hex-project-samples/
├── src/main/java/com/company/hex/project/
│   ├── components/          # Переиспользуемые компоненты
│   │   └── HeaderComponent.java
│   └── pages/              # Page Objects
│       ├── LoginPage.java
│       ├── HomePage.java
│       ├── DashboardPage.java
│       ├── AddOwnerPage.java
│       ├── UserProfilePage.java
│       └── ForgotPasswordPage.java
└── src/test/java/com/company/hex/project/tests/ui/
    ├── PageObjectExamplesTest.java  # Примеры использования
    └── ComponentScopingTest.java    # Примеры с компонентами
```

---

## Дополнительные ресурсы

- [Архитектурная документация](../../docs/architecture/ui-declerative-design.md)
- [Epic 3: UI Wrappers](../../docs/prd/epic-3-ui-wrappers-and-composite-component-pattern.md)
- [Примеры тестов](src/test/java/com/company/hex/project/tests/ui/)

---

## Заключение

Декларативный API для Page Objects в Hex Framework позволяет:

✅ Сократить boilerplate код на 40-50%  
✅ Автоматически генерировать Allure steps  
✅ Писать читаемые и поддерживаемые тесты  
✅ Переиспользовать компоненты на разных страницах  
✅ Использовать fluent API для цепочек вызовов  
✅ Обеспечить thread-safety и параллельное выполнение  

**Фокусируйтесь на бизнес-логике, а не на технических деталях!**