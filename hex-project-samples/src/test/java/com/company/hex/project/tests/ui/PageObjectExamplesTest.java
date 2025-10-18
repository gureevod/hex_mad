package com.company.hex.project.tests.ui;

import com.company.hex.project.pages.*;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Примеры тестов, демонстрирующих использование Page Objects с декларативным API.
 * 
 * <p>Эти тесты показывают:</p>
 * <ul>
 *   <li>Минимальный boilerplate код</li>
 *   <li>Автоматические Allure steps</li>
 *   <li>Fluent API для цепочек вызовов</li>
 *   <li>Композицию Page + Component</li>
 *   <li>Бизнес-методы с группировкой steps</li>
 * </ul>
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
@Epic("UI Testing Examples")
@Feature("Page Objects")
@Tag("ui")
@Tag("examples")
public class PageObjectExamplesTest {
    
    /**
     * Пример 1: Простой тест логина с минимальным кодом.
     * Демонстрирует автоматические Allure steps для каждого действия.
     */
    @Test
    @Story("Login Flow")
    @DisplayName("Успешный вход в систему")
    @Description("Демонстрирует простой flow с автоматическими Allure steps")
    @Severity(SeverityLevel.CRITICAL)
    public void testSuccessfulLogin() {
        // Открываем страницу логина и выполняем вход
        // Каждое действие автоматически создает Allure step
        LoginPage loginPage = new LoginPage();
        loginPage.open();                                  // Step: Открыть страницу 'Login Page'
        HomePage homePage = loginPage.login("user@example.com", "password123"); // Step: Вход как user@example.com
                                                          //   └─ Fill 'Username' with 'user@example.com'
                                                          //   └─ Fill 'Password' with '******'
                                                          //   └─ Click 'Login Button'
        
        // Проверяем что попали на домашнюю страницу
        homePage.verifyWelcomeMessage("Welcome, User!");  // Step: Проверить приветственное сообщение
        homePage.verifyPageLoaded();                      // Step: Проверить отображение элементов
    }
    
    /**
     * Пример 2: Негативный тест с проверкой ошибки.
     * Демонстрирует fluent API и возврат this для цепочек.
     */
    @Test
    @Story("Login Flow")
    @DisplayName("Вход с неверными учетными данными")
    @Description("Демонстрирует негативный сценарий и проверку ошибок")
    @Severity(SeverityLevel.NORMAL)
    public void testFailedLogin() {
        LoginPage loginPage = new LoginPage();
        loginPage.open();
        loginPage.login("invalid@example.com", "wrongpassword");
        loginPage.verifyError("Invalid credentials");  // Возвращает LoginPage для продолжения
    }
    
    /**
     * Пример 3: Работа с формой добавления владельца.
     * Демонстрирует бизнес-методы с группировкой steps.
     */
    @Test
    @Story("Owner Management")
    @DisplayName("Добавление нового владельца")
    @Description("Демонстрирует заполнение формы с группировкой steps")
    @Severity(SeverityLevel.CRITICAL)
    public void testAddOwner() {
        AddOwnerPage addOwnerPage = new AddOwnerPage().open();
        
        // Проверяем что форма загрузилась
        addOwnerPage.verifyFormFields();
        
        // Заполняем форму - все действия группируются в один step
        addOwnerPage.fillOwnerForm(
                "John",           // First Name
                "Doe",            // Last Name
                "123 Main St",    // Address
                "New York",       // City
                "1234567890"      // Telephone
        );
        
        // Отправляем форму
        addOwnerPage.submitForm();
        
        // Проверяем успешное создание
        addOwnerPage.verifySuccessMessage("Owner created successfully");
    }
    
    /**
     * Пример 4: Использование компонентов.
     * Демонстрирует переиспользование компонентов на разных страницах.
     */
    @Test
    @Story("Navigation")
    @DisplayName("Навигация через компонент Header")
    @Description("Демонстрирует использование переиспользуемых компонентов")
    @Severity(SeverityLevel.NORMAL)
    public void testNavigationWithComponent() {
        // Логинимся
        LoginPage loginPage = new LoginPage();
        loginPage.open();
        HomePage homePage = loginPage.login("user@example.com", "password123");
        
        // Переходим на Dashboard
        DashboardPage dashboard = homePage.navigateToDashboard();
        
        // Используем компонент header для навигации
        dashboard.header.verifyLogoDisplayed();
        dashboard.header.openProfileMenu();
        
        // Возвращаемся на главную через header
        HomePage home = dashboard.navigateHome();
        home.verifyPageLoaded();
    }
    
    /**
     * Пример 5: Обновление профиля пользователя.
     * Демонстрирует fluent API с цепочками вызовов.
     */
    @Test
    @Story("User Profile")
    @DisplayName("Обновление профиля пользователя")
    @Description("Демонстрирует fluent API и цепочки вызовов")
    @Severity(SeverityLevel.NORMAL)
    public void testUpdateUserProfile() {
        // Открываем страницу профиля
        UserProfilePage profile = new UserProfilePage().open();
        
        // Проверяем загрузку страницы
        profile.verifyPageLoaded();
        
        // Обновляем данные профиля с использованием fluent API
        profile.updateEmail("newemail@example.com")
               .updatePhone("+1234567890")
               .updateBio("Software Test Engineer")
               .saveChanges()
               .verifySuccessMessage("Profile updated successfully");
        
        // Проверяем что данные сохранились
        profile.verifyEmail("newemail@example.com")
               .verifyPhone("+1234567890");
    }
    
    /**
     * Пример 6: Восстановление пароля.
     * Демонстрирует простой flow с минимальным количеством элементов.
     */
    @Test
    @Story("Password Recovery")
    @DisplayName("Восстановление пароля")
    @Description("Демонстрирует простой Page Object с минимальным boilerplate")
    @Severity(SeverityLevel.NORMAL)
    public void testPasswordRecovery() {
        // Переходим к восстановлению пароля
        LoginPage loginPage = new LoginPage();
        loginPage.open();
        ForgotPasswordPage forgotPassword = loginPage.goToForgotPassword();
        
        // Проверяем загрузку страницы
        forgotPassword.verifyPageLoaded();
        
        // Запрашиваем сброс пароля
        forgotPassword.resetPassword("user@example.com")
                      .verifySuccessMessage("Reset link sent to your email");
        
        // Возвращаемся на страницу логина
        LoginPage backToLoginPage = forgotPassword.backToLogin();
        backToLoginPage.verifyPageElements();
    }
    
    /**
     * Пример 7: Комплексный сценарий с несколькими страницами.
     * Демонстрирует полный user journey.
     */
    @Test
    @Story("Complete User Journey")
    @DisplayName("Полный путь пользователя: логин → профиль → выход")
    @Description("Демонстрирует комплексный сценарий с несколькими страницами")
    @Severity(SeverityLevel.CRITICAL)
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
        
        // 6. Выход из системы через HomePage
        HomePage home = new HomePage();
        home.open();
        LoginPage logoutPage = home.logout();
        logoutPage.verifyPageElements();
    }
    
    /**
     * Пример 8: Работа с вкладками профиля.
     * Демонстрирует навигацию внутри страницы.
     */
    @Test
    @Story("User Profile")
    @DisplayName("Переключение между вкладками профиля")
    @Description("Демонстрирует навигацию внутри одной страницы")
    @Severity(SeverityLevel.MINOR)
    public void testProfileTabs() {
        UserProfilePage profile = new UserProfilePage().open();
        
        // Переключаемся между вкладками
        profile.openPersonalInfoTab()
               .verifyPageLoaded();
        
        profile.openSecurityTab()
               .goToChangePassword();
        
        profile.openPreferencesTab();
    }
    
    /**
     * Пример 9: Отмена действия.
     * Демонстрирует возврат на предыдущую страницу.
     */
    @Test
    @Story("Owner Management")
    @DisplayName("Отмена создания владельца")
    @Description("Демонстрирует отмену действия и возврат на предыдущую страницу")
    @Severity(SeverityLevel.MINOR)
    public void testCancelAddOwner() {
        AddOwnerPage addOwnerPage = new AddOwnerPage().open();
        
        // Начинаем заполнять форму
        addOwnerPage.fillRequiredFields("John", "Doe");
        
        // Отменяем создание
        HomePage homePage = addOwnerPage.cancel();
        
        // Проверяем что вернулись на главную
        homePage.verifyPageLoaded();
    }
    
    /**
     * Пример 10: Очистка формы.
     * Демонстрирует работу с методами очистки.
     */
    @Test
    @Story("Owner Management")
    @DisplayName("Очистка формы добавления владельца")
    @Description("Демонстрирует методы очистки полей формы")
    @Severity(SeverityLevel.TRIVIAL)
    public void testClearOwnerForm() {
        AddOwnerPage addOwnerPage = new AddOwnerPage().open();
        
        // Заполняем форму
        addOwnerPage.fillOwnerForm(
                "John", "Doe", "123 Main St", "New York", "1234567890"
        );
        
        // Очищаем форму
        addOwnerPage.clearForm();
        
        // Заполняем заново
        addOwnerPage.fillOwnerForm(
                "Jane", "Smith", "456 Oak Ave", "Boston", "0987654321"
        );
        
        addOwnerPage.submitForm()
                    .verifySuccessMessage("Owner created successfully");
    }
}