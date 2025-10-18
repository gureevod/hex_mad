package com.company.hex.project.pages;

import com.company.hex.ui.annotations.Component;
import com.company.hex.ui.annotations.Element;
import com.company.hex.ui.annotations.Page;
import com.company.hex.ui.core.BasePage;
import com.company.hex.ui.elements.Button;
import com.company.hex.ui.elements.Input;
import com.company.hex.ui.elements.TextElement;
import com.company.hex.project.components.HeaderComponent;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Condition.enabled;

/**
 * Page Object для страницы профиля пользователя.
 * Демонстрирует использование декларативного API и композиции элементов.
 * 
 * <p>Пример использования:</p>
 * <pre>
 * {@code
 * UserProfilePage profile = new UserProfilePage().open();
 * 
 * // Обновление профиля
 * profile.updateEmail("newemail@example.com")
 *        .updatePhone("+1234567890")
 *        .saveChanges();
 * 
 * // Проверка изменений
 * profile.verifyEmail("newemail@example.com");
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
@Page(url = "/profile", title = "User Profile Page")
public class UserProfilePage extends BasePage {
    
    // Компонент header
    @Component(name = "Header", root = "//nav[@id='header']")
    public HeaderComponent header;
    
    // Информация о пользователе
    @Element(name = "User Name", xpath = "//h1[@class='user-name']")
    TextElement userName;
    
    @Element(name = "User Avatar", xpath = "//img[@class='user-avatar']")
    TextElement userAvatar;
    
    // Поля редактирования профиля
    @Element(name = "Email", xpath = "//input[@id='email']")
    Input email;
    
    @Element(name = "Phone", xpath = "//input[@id='phone']")
    Input phone;
    
    @Element(name = "Bio", xpath = "//textarea[@id='bio']")
    Input bio;
    
    @Element(name = "First Name", xpath = "//input[@id='firstName']")
    Input firstName;
    
    @Element(name = "Last Name", xpath = "//input[@id='lastName']")
    Input lastName;
    
    // Кнопки действий
    @Element(name = "Save Changes Button", xpath = "//button[@id='save']")
    Button saveButton;
    
    @Element(name = "Cancel Button", xpath = "//button[@id='cancel']")
    Button cancelButton;
    
    @Element(name = "Change Password Button", xpath = "//button[@id='change-password']")
    Button changePasswordButton;
    
    @Element(name = "Upload Avatar Button", xpath = "//button[@id='upload-avatar']")
    Button uploadAvatarButton;
    
    // Сообщения
    @Element(name = "Success Message", xpath = "//div[@class='alert-success']")
    TextElement successMessage;
    
    @Element(name = "Error Message", xpath = "//div[@class='alert-error']")
    TextElement errorMessage;
    
    // Вкладки профиля
    @Element(name = "Personal Info Tab", xpath = "//a[@data-tab='personal']")
    Button personalInfoTab;
    
    @Element(name = "Security Tab", xpath = "//a[@data-tab='security']")
    Button securityTab;
    
    @Element(name = "Preferences Tab", xpath = "//a[@data-tab='preferences']")
    Button preferencesTab;
    
    /**
     * Обновить email пользователя.
     * 
     * @param newEmail новый email
     * @return this для fluent API
     */
    @Step("Обновить email на: {newEmail}")
    public UserProfilePage updateEmail(String newEmail) {
        logger.info("Обновление email на: {}", newEmail);
        email.clear();
        email.fill(newEmail);
        logger.info("Email обновлен");
        return this;
    }
    
    /**
     * Обновить телефон пользователя.
     * 
     * @param newPhone новый телефон
     * @return this для fluent API
     */
    @Step("Обновить телефон на: {newPhone}")
    public UserProfilePage updatePhone(String newPhone) {
        logger.info("Обновление телефона на: {}", newPhone);
        phone.clear();
        phone.fill(newPhone);
        logger.info("Телефон обновлен");
        return this;
    }
    
    /**
     * Обновить биографию пользователя.
     * 
     * @param newBio новая биография
     * @return this для fluent API
     */
    @Step("Обновить биографию")
    public UserProfilePage updateBio(String newBio) {
        logger.info("Обновление биографии");
        bio.clear();
        bio.fill(newBio);
        logger.info("Биография обновлена");
        return this;
    }
    
    /**
     * Обновить имя и фамилию.
     * 
     * @param first имя
     * @param last фамилия
     * @return this для fluent API
     */
    @Step("Обновить имя на: {first} {last}")
    public UserProfilePage updateName(String first, String last) {
        logger.info("Обновление имени на: {} {}", first, last);
        firstName.clear();
        firstName.fill(first);
        lastName.clear();
        lastName.fill(last);
        logger.info("Имя обновлено");
        return this;
    }
    
    /**
     * Сохранить изменения профиля.
     * 
     * @return this для fluent API
     */
    @Step("Сохранить изменения профиля")
    public UserProfilePage saveChanges() {
        logger.info("Сохранение изменений профиля");
        saveButton.click();
        logger.info("Изменения сохранены");
        return this;
    }
    
    /**
     * Отменить изменения.
     * 
     * @return this для fluent API
     */
    @Step("Отменить изменения профиля")
    public UserProfilePage cancelChanges() {
        logger.info("Отмена изменений профиля");
        cancelButton.click();
        logger.info("Изменения отменены");
        return this;
    }
    
    /**
     * Перейти к смене пароля.
     * 
     * @return this для fluent API
     */
    @Step("Перейти к смене пароля")
    public UserProfilePage goToChangePassword() {
        logger.info("Переход к смене пароля");
        changePasswordButton.click();
        return this;
    }
    
    /**
     * Переключиться на вкладку Personal Info.
     * 
     * @return this для fluent API
     */
    @Step("Открыть вкладку Personal Info")
    public UserProfilePage openPersonalInfoTab() {
        logger.info("Открытие вкладки Personal Info");
        personalInfoTab.click();
        return this;
    }
    
    /**
     * Переключиться на вкладку Security.
     * 
     * @return this для fluent API
     */
    @Step("Открыть вкладку Security")
    public UserProfilePage openSecurityTab() {
        logger.info("Открытие вкладки Security");
        securityTab.click();
        return this;
    }
    
    /**
     * Переключиться на вкладку Preferences.
     * 
     * @return this для fluent API
     */
    @Step("Открыть вкладку Preferences")
    public UserProfilePage openPreferencesTab() {
        logger.info("Открытие вкладки Preferences");
        preferencesTab.click();
        return this;
    }
    
    /**
     * Проверить email пользователя.
     * 
     * @param expectedEmail ожидаемый email
     * @return this для fluent API
     */
    @Step("Проверить email: {expectedEmail}")
    public UserProfilePage verifyEmail(String expectedEmail) {
        logger.info("Проверка email: {}", expectedEmail);
        email.shouldHaveValue(expectedEmail);
        logger.info("Email корректен");
        return this;
    }
    
    /**
     * Проверить телефон пользователя.
     * 
     * @param expectedPhone ожидаемый телефон
     * @return this для fluent API
     */
    @Step("Проверить телефон: {expectedPhone}")
    public UserProfilePage verifyPhone(String expectedPhone) {
        logger.info("Проверка телефона: {}", expectedPhone);
        phone.shouldHaveValue(expectedPhone);
        logger.info("Телефон корректен");
        return this;
    }
    
    /**
     * Проверить имя пользователя в заголовке.
     * 
     * @param expectedName ожидаемое имя
     * @return this для fluent API
     */
    @Step("Проверить имя пользователя: {expectedName}")
    public UserProfilePage verifyUserName(String expectedName) {
        logger.info("Проверка имени пользователя: {}", expectedName);
        userName.shouldHaveText(expectedName);
        logger.info("Имя пользователя корректно");
        return this;
    }
    
    /**
     * Проверить сообщение об успехе.
     * 
     * @param expectedMessage ожидаемое сообщение
     * @return this для fluent API
     */
    @Step("Проверить сообщение об успехе: {expectedMessage}")
    public UserProfilePage verifySuccessMessage(String expectedMessage) {
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
    public UserProfilePage verifyErrorMessage(String expectedError) {
        logger.info("Проверка сообщения об ошибке: {}", expectedError);
        errorMessage.shouldBe(visible);
        errorMessage.shouldHaveText(expectedError);
        logger.info("Сообщение об ошибке отображается корректно");
        return this;
    }
    
    /**
     * Проверить, что все основные элементы профиля отображаются.
     * 
     * @return this для fluent API
     */
    @Step("Проверить отображение элементов профиля")
    public UserProfilePage verifyPageLoaded() {
        logger.info("Проверка загрузки страницы профиля");
        
        userName.shouldBe(visible);
        userAvatar.shouldBe(visible);
        email.shouldBe(visible);
        phone.shouldBe(visible);
        saveButton.shouldBe(visible, enabled);
        cancelButton.shouldBe(visible);
        
        logger.info("Все элементы профиля отображаются корректно");
        return this;
    }
}