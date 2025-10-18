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

/**
 * Page Object для страницы добавления владельца (Add Owner).
 * Демонстрирует использование декларативного API с минимальным boilerplate кодом.
 * 
 * <p>Пример использования:</p>
 * <pre>
 * {@code
 * AddOwnerPage page = new AddOwnerPage().open();
 * 
 * // Заполнение формы с автоматическими Allure steps
 * page.fillOwnerForm("John", "Doe", "123 Main St", "New York", "1234567890");
 * page.submitForm();
 * 
 * // Проверка успешного создания
 * page.verifySuccessMessage("Owner created successfully");
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
@Page(url = "/owners/new", title = "Add Owner Page")
public class AddOwnerPage extends BasePage {
    
    // Компонент header для навигации
    @Component(name = "Header", root = "//nav[@id='header']")
    public HeaderComponent header;
    
    // Поля формы владельца
    @Element(name = "First Name", xpath = "//input[@id='firstName']")
    Input firstName;
    
    @Element(name = "Last Name", xpath = "//input[@id='lastName']")
    Input lastName;
    
    @Element(name = "Address", xpath = "//input[@id='address']")
    Input address;
    
    @Element(name = "City", xpath = "//input[@id='city']")
    Input city;
    
    @Element(name = "Telephone", xpath = "//input[@id='telephone']")
    Input telephone;
    
    // Кнопки
    @Element(name = "Submit Button", xpath = "//button[@type='submit']")
    Button submitButton;
    
    @Element(name = "Cancel Button", xpath = "//button[contains(text(), 'Cancel')]")
    Button cancelButton;
    
    // Сообщения
    @Element(name = "Success Message", xpath = "//div[@class='alert-success']")
    TextElement successMessage;
    
    @Element(name = "Error Message", xpath = "//div[@class='alert-error']")
    TextElement errorMessage;
    
    /**
     * Заполнить форму владельца всеми данными.
     * Каждое действие автоматически создает Allure step.
     * 
     * @param first имя
     * @param last фамилия
     * @param addr адрес
     * @param cityName город
     * @param phone телефон
     * @return this для fluent API
     */
    @Step("Заполнить форму владельца: {first} {last}")
    public AddOwnerPage fillOwnerForm(String first, String last, String addr, String cityName, String phone) {
        logger.info("Заполнение формы владельца: {} {}", first, last);
        
        firstName.fill(first);      // автоматический step: Fill 'First Name' with 'John'
        lastName.fill(last);        // автоматический step: Fill 'Last Name' with 'Doe'
        address.fill(addr);         // автоматический step: Fill 'Address' with '123 Main St'
        city.fill(cityName);        // автоматический step: Fill 'City' with 'New York'
        telephone.fill(phone);      // автоматический step: Fill 'Telephone' with '1234567890'
        
        logger.info("Форма владельца заполнена");
        return this;
    }
    
    /**
     * Заполнить только обязательные поля.
     * 
     * @param first имя
     * @param last фамилия
     * @return this для fluent API
     */
    @Step("Заполнить обязательные поля: {first} {last}")
    public AddOwnerPage fillRequiredFields(String first, String last) {
        logger.info("Заполнение обязательных полей: {} {}", first, last);
        
        firstName.fill(first);
        lastName.fill(last);
        
        logger.info("Обязательные поля заполнены");
        return this;
    }
    
    /**
     * Отправить форму.
     * 
     * @return this для fluent API
     */
    @Step("Отправить форму добавления владельца")
    public AddOwnerPage submitForm() {
        logger.info("Отправка формы добавления владельца");
        submitButton.click();
        logger.info("Форма отправлена");
        return this;
    }
    
    /**
     * Отменить создание владельца.
     * 
     * @return HomePage после отмены
     */
    @Step("Отменить создание владельца")
    public HomePage cancel() {
        logger.info("Отмена создания владельца");
        cancelButton.click();
        return new HomePage();
    }
    
    /**
     * Проверить сообщение об успехе.
     * 
     * @param expectedMessage ожидаемое сообщение
     * @return this для fluent API
     */
    @Step("Проверить сообщение об успехе: {expectedMessage}")
    public AddOwnerPage verifySuccessMessage(String expectedMessage) {
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
    public AddOwnerPage verifyErrorMessage(String expectedError) {
        logger.info("Проверка сообщения об ошибке: {}", expectedError);
        
        errorMessage.shouldBe(visible);
        errorMessage.shouldHaveText(expectedError);
        
        logger.info("Сообщение об ошибке отображается корректно");
        return this;
    }
    
    /**
     * Проверить, что все поля формы отображаются.
     * 
     * @return this для fluent API
     */
    @Step("Проверить отображение всех полей формы")
    public AddOwnerPage verifyFormFields() {
        logger.info("Проверка отображения полей формы");
        
        firstName.shouldBe(visible);
        lastName.shouldBe(visible);
        address.shouldBe(visible);
        city.shouldBe(visible);
        telephone.shouldBe(visible);
        submitButton.shouldBe(visible);
        cancelButton.shouldBe(visible);
        
        logger.info("Все поля формы отображаются корректно");
        return this;
    }
    
    /**
     * Очистить все поля формы.
     * 
     * @return this для fluent API
     */
    @Step("Очистить все поля формы")
    public AddOwnerPage clearForm() {
        logger.info("Очистка всех полей формы");
        
        firstName.clear();
        lastName.clear();
        address.clear();
        city.clear();
        telephone.clear();
        
        logger.info("Все поля формы очищены");
        return this;
    }
}