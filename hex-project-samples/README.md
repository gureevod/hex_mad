# Hex Project Samples

## Описание

Примеры использования Hex Automation Framework. Демонстрирует интеграцию всех модулей фреймворка в реальном проекте.

## 📚 Документация

- **[Page Objects - Подробное руководство](README_PAGE_OBJECTS.md)** - Полное руководство по созданию Page Objects с примерами
- [Архитектурная документация](../docs/architecture/ui-declerative-design.md)
- [Epic 3: UI Wrappers](../docs/prd/epic-3-ui-wrappers-and-composite-component-pattern.md)

## Структура проекта

### API (api)
- **dto/** - Объекты передачи данных для API
- **services/** - Сервисы для работы с API

### Страницы (pages)
- Page Object классы для UI тестирования с декларативным API
- Примеры:
  - [`LoginPage`](src/main/java/com/company/hex/project/pages/LoginPage.java) - простой Page Object с формой логина
  - [`HomePage`](src/main/java/com/company/hex/project/pages/HomePage.java) - домашняя страница с навигацией
  - [`DashboardPage`](src/main/java/com/company/hex/project/pages/DashboardPage.java) - страница с компонентами
  - [`AddOwnerPage`](src/main/java/com/company/hex/project/pages/AddOwnerPage.java) - форма с бизнес-методами
  - [`UserProfilePage`](src/main/java/com/company/hex/project/pages/UserProfilePage.java) - профиль с вкладками
  - [`ForgotPasswordPage`](src/main/java/com/company/hex/project/pages/ForgotPasswordPage.java) - восстановление пароля

### Компоненты (components)
- Переиспользуемые UI компоненты с гибким scoping
- Примеры: [`HeaderComponent`](src/main/java/com/company/hex/project/components/HeaderComponent.java)

### Конфигурация (config)
- Настройки проекта и окружений

### Тесты (tests)
- **api/** - API тесты с декларативным и императивным подходами
- **ui/** - UI тесты с примерами использования Page Objects
  - [`PageObjectExamplesTest`](src/test/java/com/company/hex/project/tests/ui/PageObjectExamplesTest.java) - 10 примеров использования
  - [`ComponentScopingTest`](src/test/java/com/company/hex/project/tests/ui/ComponentScopingTest.java) - примеры с компонентами

## Зависимости

- hex-core (базовые абстракции)
- hex-core-api (API тестирование)
- hex-core-ui (UI тестирование)
- hex-core-testing (утилиты тестирования)

## Использование

```bash
# Запуск всех тестов
mvn test

# Запуск только API тестов
mvn test -Dtest="**/*ApiTest"

# Запуск только UI тестов
mvn test -Dtest="**/*UiTest"
```

## Быстрый старт

### Создание простого Page Object

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

### Использование в тесте

```java
@Test
public void testLogin() {
    LoginPage loginPage = new LoginPage();
    loginPage.open();
    HomePage homePage = loginPage.login("user@example.com", "password123");
    
    homePage.verifyWelcomeMessage("Welcome, User!");
}
```

**Подробнее:** [README_PAGE_OBJECTS.md](README_PAGE_OBJECTS.md)

## Примеры использования

### UI Компоненты с гибким scoping

Hex Framework поддерживает переиспользуемые компоненты с гибким root локатором. Один класс компонента может использоваться на разных страницах с разными локаторами.

#### Создание компонента

```java
public class HeaderComponent extends BaseComponent {
    
    @Element(name = "Home Button", xpath = ".//a[@title='home']")
    Button homeButton;
    
    @Element(name = "Profile Menu", xpath = ".//div[@class='profile-menu']")
    Button profileMenu;
    
    @Step("Перейти на главную из '{this.componentName}'")
    public HomePage goHome() {
        homeButton.click();
        return new HomePage();
    }
    
    @Step("Открыть меню профиля в '{this.componentName}'")
    public HeaderComponent openProfileMenu() {
        profileMenu.click();
        return this;
    }
}
```

**Важно:** Все локаторы внутри компонента должны быть относительными (начинаться с `.//`).

#### Использование компонента в Page Object

```java
@Page(url = "/dashboard", title = "Dashboard")
public class DashboardPage extends BasePage {
    
    // Компонент с указанием root локатора и имени
    @Component(name = "Main Header", root = "//header[@id='main-header']")
    public HeaderComponent header;
    
    // Тот же компонент с другим root локатором
    @Component(name = "Sidebar Navigation", root = "//aside[@id='sidebar']")
    public HeaderComponent sidebar;
    
    @Element(name = "Welcome Message", xpath = "//h1[@class='welcome']")
    public TextElement welcomeMessage;
}
```

#### Использование в тестах

```java
@Test
public void testHeaderComponent() {
    DashboardPage dashboard = new DashboardPage();
    
    dashboard.open();
    
    // Используем компонент
    dashboard.header.verifyLogoDisplayed()
            .openProfileMenu();
    
    // Прямое использование элементов
    dashboard.welcomeMessage.shouldBe(visible);
}
```

### Контекстное логирование

Все действия с элементами автоматически логируются с полным контекстом:

```
INFO - Открытие меню профиля в компоненте 'Main Header' на странице 'Dashboard'
INFO - Клик по 'Profile Menu' в странице 'Dashboard' компоненте 'Main Header'
```

### Автоматические Allure steps

Каждое действие автоматически создает Allure step:

```
└─ Открыть меню профиля в 'Main Header'
   └─ Клик по 'Profile Menu'
```

Для бизнес-логики можно группировать steps:

```java
@Step("Войти как {username}")
public HomePage login(String username, String password) {
    this.username.fill(username);  // вложенный step
    this.password.fill(password);  // вложенный step
    loginButton.click();           // вложенный step
    return new HomePage();
}
```

### Преимущества компонентов

1. **Переиспользование кода** - один компонент на разных страницах
2. **Гибкий scoping** - root локатор указывается при использовании
3. **Контекстное логирование** - автоматически включает имя компонента
4. **Thread-safe** - каждый экземпляр независим
5. **Минимум boilerplate** - декларативный подход с аннотациями

## Отчеты

После выполнения тестов отчеты Allure будут доступны в `target/allure-results/`.

Для генерации HTML отчета:
```bash
allure serve target/allure-results