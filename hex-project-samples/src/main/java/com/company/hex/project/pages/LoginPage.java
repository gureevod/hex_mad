package com.company.hex.project.pages;

import com.company.hex.ui.annotations.Element;
import com.company.hex.ui.annotations.Page;
import com.company.hex.ui.core.BasePage;
import com.company.hex.ui.elements.Button;
import com.company.hex.ui.elements.Input;
import com.company.hex.ui.elements.TextElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.visible;

/**
 * Page Object для страницы логина.
 * Демонстрирует использование декларативного API с аннотациями.
 * 
 * @author Hex Framework
 * @version 1.0
 */
@Page(url = "/login", title = "Login Page")
public class LoginPage extends BasePage {
    
    @Element(name = "Username", xpath = "//input[@id='username']")
    Input username;
    
    @Element(name = "Password", xpath = "//input[@id='password']")
    Input password;
    
    @Element(name = "Login Button", xpath = "//button[@type='submit']")
    Button loginButton;
    
    @Element(name = "Error Message", xpath = "//div[@class='error-message']")
    TextElement errorMessage;
    
    @Element(name = "Forgot Password Link", xpath = "//a[contains(text(), 'Forgot')]")
    Button forgotPasswordLink;
    
    /**
     * Выполнить вход с указанными учетными данными.
     * 
     * @param user имя пользователя
     * @param pass пароль
     * @return HomePage после успешного входа
     */
    @Step("Вход как {user}")
    public HomePage login(String user, String pass) {
        logger.info("Выполнение входа для пользователя: {}", user);
        
        username.fill(user);
        password.fill(pass);
        loginButton.click();
        
        logger.info("Вход выполнен для пользователя: {}", user);
        return new HomePage();
    }
    
    /**
     * Выполнить вход с пустыми полями (для негативного тестирования).
     * 
     * @return this для fluent API
     */
    @Step("Попытка входа с пустыми полями")
    public LoginPage loginWithEmptyFields() {
        logger.info("Попытка входа с пустыми полями");
        
        loginButton.click();
        
        return this;
    }
    
    /**
     * Проверить, что отображается сообщение об ошибке.
     * 
     * @param expectedError ожидаемый текст ошибки
     * @return this для fluent API
     */
    @Step("Проверить сообщение об ошибке: {expectedError}")
    public LoginPage verifyError(String expectedError) {
        logger.info("Проверка сообщения об ошибке: {}", expectedError);
        
        errorMessage.shouldBe(visible);
        errorMessage.shouldHaveText(expectedError);
        
        return this;
    }
    
    /**
     * Кликнуть по ссылке "Forgot Password".
     * 
     * @return ForgotPasswordPage
     */
    @Step("Перейти к восстановлению пароля")
    public ForgotPasswordPage goToForgotPassword() {
        logger.info("Переход к восстановлению пароля");
        
        forgotPasswordLink.click();
        
        return new ForgotPasswordPage();
    }
    
    /**
     * Проверить, что все элементы страницы отображаются.
     * 
     * @return this для fluent API
     */
    @Step("Проверить отображение всех элементов страницы логина")
    public LoginPage verifyPageElements() {
        logger.info("Проверка отображения элементов страницы логина");
        
        username.shouldBe(visible);
        password.shouldBe(visible);
        loginButton.shouldBe(visible);
        forgotPasswordLink.shouldBe(visible);
        
        logger.info("Все элементы страницы логина отображаются корректно");
        return this;
    }
}