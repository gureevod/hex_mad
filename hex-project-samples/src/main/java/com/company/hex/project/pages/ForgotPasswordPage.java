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
 * Page Object для страницы восстановления пароля.
 * Демонстрирует простой Page Object с минимальным количеством элементов.
 *
 * <p>Пример использования:</p>
 * <pre>
 * {@code
 * ForgotPasswordPage page = new ForgotPasswordPage().open();
 * page.resetPassword("user@example.com")
 *     .verifySuccessMessage("Reset link sent");
 * }
 * </pre>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
@Page(url = "/forgot-password", title = "Forgot Password Page")
public class ForgotPasswordPage extends BasePage {
    
    @Element(name = "Email", xpath = "//input[@id='email']")
    Input email;
    
    @Element(name = "Reset Button", xpath = "//button[@type='submit']")
    Button resetButton;
    
    @Element(name = "Back to Login Link", xpath = "//a[contains(text(), 'Back to Login')]")
    Button backToLoginLink;
    
    @Element(name = "Success Message", xpath = "//div[@class='alert-success']")
    TextElement successMessage;
    
    @Element(name = "Error Message", xpath = "//div[@class='alert-error']")
    TextElement errorMessage;
    
    /**
     * Запросить сброс пароля для указанного email.
     *
     * @param userEmail email пользователя
     * @return this для fluent API
     */
    @Step("Запросить сброс пароля для: {userEmail}")
    public ForgotPasswordPage resetPassword(String userEmail) {
        logger.info("Запрос сброса пароля для: {}", userEmail);
        
        email.fill(userEmail);
        resetButton.click();
        
        logger.info("Запрос на сброс пароля отправлен");
        return this;
    }
    
    /**
     * Вернуться на страницу логина.
     *
     * @return LoginPage
     */
    @Step("Вернуться на страницу логина")
    public LoginPage backToLogin() {
        logger.info("Возврат на страницу логина");
        backToLoginLink.click();
        return new LoginPage();
    }
    
    /**
     * Проверить сообщение об успехе.
     *
     * @param expectedMessage ожидаемое сообщение
     * @return this для fluent API
     */
    @Step("Проверить сообщение об успехе: {expectedMessage}")
    public ForgotPasswordPage verifySuccessMessage(String expectedMessage) {
        logger.info("Проверка сообщения об успехе: {}", expectedMessage);
        
        successMessage.shouldBe(visible);
        successMessage.shouldHaveText(expectedMessage);
        
        logger.info("Сообщение об успехе отображается корректно");
        return this;
    }
    
    /**
     * Проверить сообщение об ошибке.
     *
     * @param expectedError ожидаемая ошибка
     * @return this для fluent API
     */
    @Step("Проверить сообщение об ошибке: {expectedError}")
    public ForgotPasswordPage verifyErrorMessage(String expectedError) {
        logger.info("Проверка сообщения об ошибке: {}", expectedError);
        
        errorMessage.shouldBe(visible);
        errorMessage.shouldHaveText(expectedError);
        
        logger.info("Сообщение об ошибке отображается корректно");
        return this;
    }
    
    /**
     * Проверить, что все элементы страницы отображаются.
     *
     * @return this для fluent API
     */
    @Step("Проверить отображение элементов страницы")
    public ForgotPasswordPage verifyPageLoaded() {
        logger.info("Проверка загрузки страницы восстановления пароля");
        
        email.shouldBe(visible);
        resetButton.shouldBe(visible);
        backToLoginLink.shouldBe(visible);
        
        logger.info("Все элементы страницы отображаются корректно");
        return this;
    }
}