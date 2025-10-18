package com.company.hex.project.pages;

import com.company.hex.ui.annotations.Page;
import com.company.hex.ui.builder.ButtonBuilder;
import com.company.hex.ui.builder.InputBuilder;
import com.company.hex.ui.core.BasePage;
import com.company.hex.ui.elements.Button;
import com.company.hex.ui.elements.Input;
import io.qameta.allure.Step;

import static com.company.hex.ui.builder.Elements.*;

/**
 * Пример страницы с использованием Builder API для сложных локаторов.
 * Демонстрирует различные способы создания элементов через Builder API.
 * 
 * @author Hex Framework
 * @version 1.0
 */
@Page(url = "/advanced-form", title = "Advanced Form Page")
public class AdvancedFormPage extends BasePage {
    
    // ========== Простые локаторы через Builder API ==========
    
    /**
     * Простой input с коротким синтаксисом.
     */
    private final Input simpleEmail = input("//input[@type='email']")
        .withName("Simple Email Field")
        .build();
    
    /**
     * Простая кнопка с коротким синтаксисом.
     */
    private final Button simpleSubmit = button("//button[@type='submit']")
        .withName("Simple Submit Button")
        .build();
    
    // ========== Составные локаторы ==========
    
    /**
     * Поле с составным локатором.
     * Демонстрирует построение локатора из нескольких частей.
     */
    private final Input compositeField = input()
        .withName("Composite Field")
        .locator()
            .base("//div[@class='form-container']")
            .append("//section[@id='personal-info']")
            .append("//input[@data-field='email']")
            .build()
        .build();
    
    /**
     * Кнопка с составным локатором.
     */
    private final Button compositeButton = button()
        .withName("Composite Action Button")
        .locator()
            .base("//div[@class='form-actions']")
            .append("//div[@class='primary-actions']")
            .append("//button[@type='submit']")
            .build()
        .build();
    
    // ========== Параметризованные локаторы (хранятся как builders) ==========
    
    /**
     * Динамическое поле с одним параметром.
     * Используется для доступа к полям по имени.
     */
    private final InputBuilder dynamicFieldBuilder = input()
        .withName("Dynamic Field")
        .locator()
            .base("//div[@class='form-section']")
            .append("//input[@data-field='{field}']")
            .build();
    
    /**
     * Динамическая кнопка с одним параметром.
     */
    private final ButtonBuilder dynamicActionBuilder = button()
        .withName("Dynamic Action Button")
        .locator()
            .base("//div[@class='actions-panel']")
            .append("//button[@data-action='{action}']")
            .build();
    
    // ========== Многоуровневые параметризованные локаторы ==========
    
    /**
     * Поле с несколькими параметрами.
     * Демонстрирует сложную параметризацию для доступа к вложенным элементам.
     */
    private final InputBuilder multiLevelFieldBuilder = input()
        .withName("Multi-Level Field")
        .locator()
            .base("//div[@class='form-container']")
            .append("//section[@data-section='{section}']")
            .append("//div[@class='field-group']")
            .append("//input[@data-field='{field}']")
            .build();
    
    /**
     * Кнопка в таблице с множественными параметрами.
     * Используется для доступа к кнопкам в ячейках таблицы.
     */
    private final ButtonBuilder tableCellButtonBuilder = button()
        .withName("Table Cell Button")
        .locator()
            .base("//table[@id='data-table']")
            .append("//tr[@data-row-id='{rowId}']")
            .append("//td[@data-column='{column}']")
            .append("//button[@data-action='{action}']")
            .build();
    
    /**
     * Сложный многоуровневый локатор для вложенных структур.
     */
    private final InputBuilder complexNestedFieldBuilder = input()
        .withName("Complex Nested Field")
        .locator()
            .base("//div[@class='dashboard']")
            .append("//section[@data-section='{section}']")
            .append("//div[@class='widget'][@data-widget='{widget}']")
            .append("//form[@data-form='{form}']")
            .append("//input[@data-field='{field}']")
            .build();
    
    // ========== Методы для работы с простыми элементами ==========
    
    @Step("Заполнить простое email поле: {email}")
    public AdvancedFormPage fillSimpleEmail(String email) {
        simpleEmail.fill(email);
        return this;
    }
    
    @Step("Нажать простую кнопку Submit")
    public AdvancedFormPage clickSimpleSubmit() {
        simpleSubmit.click();
        return this;
    }
    
    // ========== Методы для работы с составными локаторами ==========
    
    @Step("Заполнить составное поле: {value}")
    public AdvancedFormPage fillCompositeField(String value) {
        compositeField.fill(value);
        return this;
    }
    
    @Step("Нажать составную кнопку")
    public AdvancedFormPage clickCompositeButton() {
        compositeButton.click();
        return this;
    }
    
    // ========== Методы для работы с параметризованными локаторами ==========
    
    @Step("Заполнить динамическое поле '{fieldName}' значением '{value}'")
    public AdvancedFormPage fillDynamicField(String fieldName, String value) {
        dynamicFieldBuilder.resolve("field", fieldName).fill(value);
        return this;
    }
    
    @Step("Выполнить динамическое действие: {action}")
    public AdvancedFormPage performDynamicAction(String action) {
        dynamicActionBuilder.resolve("action", action).click();
        return this;
    }
    
    // ========== Методы для работы с многоуровневыми локаторами ==========
    
    @Step("Заполнить многоуровневое поле: секция '{section}', поле '{field}', значение '{value}'")
    public AdvancedFormPage fillMultiLevelField(String section, String field, String value) {
        multiLevelFieldBuilder.resolve("section", section, "field", field).fill(value);
        return this;
    }
    
    @Step("Кликнуть кнопку в таблице: строка '{rowId}', колонка '{column}', действие '{action}'")
    public AdvancedFormPage clickTableCellButton(String rowId, String column, String action) {
        tableCellButtonBuilder.resolve(
            "rowId", rowId,
            "column", column,
            "action", action
        ).click();
        return this;
    }
    
    @Step("Заполнить сложное вложенное поле")
    public AdvancedFormPage fillComplexNestedField(
            String section, String widget, String form, String field, String value) {
        complexNestedFieldBuilder.resolve(
            "section", section,
            "widget", widget,
            "form", form,
            "field", field
        ).fill(value);
        return this;
    }
    
    // ========== Примеры комплексных сценариев ==========
    
    @Step("Заполнить форму пользователя")
    public AdvancedFormPage fillUserForm(String firstName, String lastName, String email) {
        fillDynamicField("firstName", firstName);
        fillDynamicField("lastName", lastName);
        fillDynamicField("email", email);
        return this;
    }
    
    @Step("Заполнить секцию профиля")
    public AdvancedFormPage fillProfileSection(String email, String phone) {
        fillMultiLevelField("profile", "email", email);
        fillMultiLevelField("profile", "phone", phone);
        return this;
    }
    
    @Step("Заполнить секцию адреса")
    public AdvancedFormPage fillAddressSection(String city, String street) {
        fillMultiLevelField("address", "city", city);
        fillMultiLevelField("address", "street", street);
        return this;
    }
    
    @Step("Выполнить действия с таблицей пользователей")
    public AdvancedFormPage performTableActions() {
        clickTableCellButton("user-123", "actions", "edit");
        clickTableCellButton("user-456", "status", "activate");
        clickTableCellButton("user-789", "actions", "delete");
        return this;
    }
}