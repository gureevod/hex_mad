package com.company.hex.project.pages;

import com.company.hex.ui.config.UiConfig;
import com.company.hex.ui.core.WebDriverFactory;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

/**
 * Пример страницы входа в систему.
 * Демонстрирует использование WebDriverFactory и паттерна Page Object.
 * 
 * @author Hex Framework
 * @version 1.0
 */
public class LoginPage {

    private static final Logger logger = LoggerFactory.getLogger(LoginPage.class);
    
    private final WebDriver driver;
    private final UiConfig config;

    // Локаторы элементов страницы
    private final SelenideElement usernameField = $("#username");
    private final SelenideElement passwordField = $("#password");
    private final SelenideElement loginButton = $("#login-button");
    private final SelenideElement errorMessage = $(".error-message");
    private final SelenideElement forgotPasswordLink = $("#forgot-password");
    private final SelenideElement rememberMeCheckbox = $("#remember-me");
    private final SelenideElement signUpLink = $("#sign-up");

    /**
     * Конструктор с использованием WebDriverFactory.
     * Создает новый экземпляр WebDriver с конфигурацией по умолчанию.
     */
    public LoginPage() {
        this.driver = WebDriverFactory.createDriver();
        this.config = null;
        logger.info("Создана страница входа с WebDriver по умолчанию");
    }

    /**
     * Конструктор с пользовательской конфигурацией.
     * 
     * @param config пользовательская конфигурация UI
     */
    public LoginPage(UiConfig config) {
        this.config = config;
        this.driver = WebDriverFactory.createDriver(config);
        logger.info("Создана страница входа с пользовательской конфигурацией");
    }

    /**
     * Конструктор для использования существующего WebDriver.
     * 
     * @param driver существующий WebDriver
     */
    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.config = null;
        logger.info("Создана страница входа с существующим WebDriver");
    }

    /**
     * Открыть страницу входа.
     * 
     * @return текущий экземпляр LoginPage для цепочки вызовов
     */
    public LoginPage openPage() {
        String url = config != null ? config.baseUrl() + "/login" : "/login";
        logger.info("Открытие страницы входа: {}", url);
        open(url);
        return this;
    }

    /**
     * Открыть страницу входа по указанному URL.
     * 
     * @param url URL страницы входа
     * @return текущий экземпляр LoginPage для цепочки вызовов
     */
    public LoginPage openPage(String url) {
        logger.info("Открытие страницы входа по URL: {}", url);
        open(url);
        return this;
    }

    /**
     * Ввести имя пользователя.
     * 
     * @param username имя пользователя
     * @return текущий экземпляр LoginPage для цепочки вызовов
     */
    public LoginPage enterUsername(String username) {
        logger.debug("Ввод имени пользователя: {}", username);
        usernameField.clear();
        usernameField.setValue(username);
        return this;
    }

    /**
     * Ввести пароль.
     * 
     * @param password пароль
     * @return текущий экземпляр LoginPage для цепочки вызовов
     */
    public LoginPage enterPassword(String password) {
        logger.debug("Ввод пароля");
        passwordField.clear();
        passwordField.setValue(password);
        return this;
    }

    /**
     * Установить или снять флажок "Запомнить меня".
     * 
     * @param remember true для установки флажка, false для снятия
     * @return текущий экземпляр LoginPage для цепочки вызовов
     */
    public LoginPage setRememberMe(boolean remember) {
        logger.debug("Установка флажка 'Запомнить меня': {}", remember);
        if (remember) {
            rememberMeCheckbox.setSelected(true);
        } else {
            rememberMeCheckbox.setSelected(false);
        }
        return this;
    }

    /**
     * Нажать кнопку входа.
     * 
     * @return текущий экземпляр LoginPage для цепочки вызовов
     */
    public LoginPage clickLoginButton() {
        logger.info("Нажатие кнопки входа");
        loginButton.click();
        return this;
    }

    /**
     * Выполнить полный процесс входа в систему.
     * 
     * @param username имя пользователя
     * @param password пароль
     * @return текущий экземпляр LoginPage для цепочки вызовов
     */
    public LoginPage login(String username, String password) {
        logger.info("Выполнение входа в систему для пользователя: {}", username);
        return enterUsername(username)
                .enterPassword(password)
                .clickLoginButton();
    }

    /**
     * Выполнить полный процесс входа в систему с запоминанием.
     * 
     * @param username имя пользователя
     * @param password пароль
     * @param rememberMe запомнить пользователя
     * @return текущий экземпляр LoginPage для цепочки вызовов
     */
    public LoginPage login(String username, String password, boolean rememberMe) {
        logger.info("Выполнение входа в систему для пользователя: {} с запоминанием: {}", username, rememberMe);
        return enterUsername(username)
                .enterPassword(password)
                .setRememberMe(rememberMe)
                .clickLoginButton();
    }

    /**
     * Нажать ссылку "Забыли пароль?".
     * 
     * @return текущий экземпляр LoginPage для цепочки вызовов
     */
    public LoginPage clickForgotPassword() {
        logger.info("Нажатие ссылки 'Забыли пароль?'");
        forgotPasswordLink.click();
        return this;
    }

    /**
     * Нажать ссылку регистрации.
     * 
     * @return текущий экземпляр LoginPage для цепочки вызовов
     */
    public LoginPage clickSignUp() {
        logger.info("Нажатие ссылки регистрации");
        signUpLink.click();
        return this;
    }

    /**
     * Получить текст сообщения об ошибке.
     * 
     * @return текст сообщения об ошибке или пустая строка, если ошибки нет
     */
    public String getErrorMessage() {
        if (errorMessage.exists()) {
            String error = errorMessage.getText();
            logger.debug("Получено сообщение об ошибке: {}", error);
            return error;
        }
        return "";
    }

    /**
     * Проверить, отображается ли сообщение об ошибке.
     * 
     * @return true, если сообщение об ошибке отображается
     */
    public boolean isErrorMessageDisplayed() {
        boolean displayed = errorMessage.exists() && errorMessage.isDisplayed();
        logger.debug("Сообщение об ошибке отображается: {}", displayed);
        return displayed;
    }

    /**
     * Проверить, доступна ли кнопка входа.
     * 
     * @return true, если кнопка входа доступна
     */
    public boolean isLoginButtonEnabled() {
        boolean enabled = loginButton.isEnabled();
        logger.debug("Кнопка входа доступна: {}", enabled);
        return enabled;
    }

    /**
     * Очистить все поля формы.
     * 
     * @return текущий экземпляр LoginPage для цепочки вызовов
     */
    public LoginPage clearForm() {
        logger.debug("Очистка формы входа");
        usernameField.clear();
        passwordField.clear();
        rememberMeCheckbox.setSelected(false);
        return this;
    }

    /**
     * Получить WebDriver, используемый этой страницей.
     * 
     * @return WebDriver
     */
    public WebDriver getDriver() {
        return driver;
    }

    /**
     * Закрыть WebDriver (если он был создан этой страницей).
     */
    public void close() {
        if (driver != null) {
            logger.info("Закрытие WebDriver");
            driver.quit();
        }
    }
}