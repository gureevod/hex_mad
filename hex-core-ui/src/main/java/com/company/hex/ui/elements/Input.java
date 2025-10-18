package com.company.hex.ui.elements;

import com.codeborne.selenide.SelenideElement;
import com.company.hex.ui.core.BaseElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.empty;
import static com.codeborne.selenide.Condition.value;

/**
 * Класс для работы с текстовыми полями ввода (input, textarea).
 * Предоставляет методы для заполнения, очистки и проверки значений.
 * 
 * <p>Пример использования:</p>
 * <pre>
 * {@code
 * @Element(name = "Username", xpath = "//input[@id='username']")
 * Input username;
 * 
 * username.fill("john@example.com")
 *         .shouldHaveValue("john@example.com")
 *         .shouldBe(visible, enabled);
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public class Input extends BaseElement {
    
    /**
     * Конструктор для создания Input элемента.
     * 
     * @param name имя элемента для логирования
     * @param element Selenide элемент
     */
    public Input(String name, SelenideElement element) {
        super(name, element);
    }
    
    /**
     * Заполнить поле значением.
     * Очищает поле перед вводом.
     * 
     * @param text текст для ввода
     * @return this для fluent API
     */
    @Step("Заполнить '{this.name}' значением '{text}'")
    public Input fill(String text) {
        logger.info("Заполнение '{}' значением '{}' {}", name, text, getContext());
        element.clear();
        element.setValue(text);
        logger.debug("Поле '{}' успешно заполнено", name);
        return this;
    }
    
    /**
     * Очистить поле.
     * 
     * @return this для fluent API
     */
    @Step("Очистить '{this.name}'")
    public Input clear() {
        logger.info("Очистка '{}' {}", name, getContext());
        element.clear();
        logger.debug("Поле '{}' успешно очищено", name);
        return this;
    }
    
    /**
     * Добавить текст к существующему значению.
     * 
     * @param text текст для добавления
     * @return this для fluent API
     */
    @Step("Добавить '{text}' к '{this.name}'")
    public Input append(String text) {
        logger.info("Добавление '{}' к '{}' {}", text, name, getContext());
        element.append(text);
        logger.debug("Текст успешно добавлен к '{}'", name);
        return this;
    }
    
    /**
     * Получить текущее значение поля.
     * 
     * @return значение поля
     */
    @Step("Получить значение из '{this.name}'")
    public String getValue() {
        logger.debug("Получение значения из '{}' {}", name, getContext());
        String value = element.getValue();
        logger.debug("Значение из '{}': '{}'", name, value);
        return value;
    }
    
    /**
     * Ввести текст посимвольно (медленный ввод).
     * Полезно для полей с автодополнением.
     * 
     * @param text текст для ввода
     * @return this для fluent API
     */
    @Step("Ввести посимвольно '{text}' в '{this.name}'")
    public Input type(String text) {
        logger.info("Посимвольный ввод '{}' в '{}' {}", text, name, getContext());
        element.sendKeys(text);
        logger.debug("Текст успешно введен в '{}'", name);
        return this;
    }
    
    /**
     * Нажать Enter в поле.
     * 
     * @return this для fluent API
     */
    @Step("Нажать Enter в '{this.name}'")
    public Input pressEnter() {
        logger.info("Нажатие Enter в '{}' {}", name, getContext());
        element.pressEnter();
        return this;
    }
    
    /**
     * Нажать Tab в поле.
     * 
     * @return this для fluent API
     */
    @Step("Нажать Tab в '{this.name}'")
    public Input pressTab() {
        logger.info("Нажатие Tab в '{}' {}", name, getContext());
        element.pressTab();
        return this;
    }
    
    /**
     * Нажать Escape в поле.
     * 
     * @return this для fluent API
     */
    @Step("Нажать Escape в '{this.name}'")
    public Input pressEscape() {
        logger.info("Нажатие Escape в '{}' {}", name, getContext());
        element.pressEscape();
        return this;
    }
    
    /**
     * Проверить, что поле имеет указанное значение.
     * 
     * @param expectedValue ожидаемое значение
     * @return this для fluent API
     */
    @Step("'{this.name}' должно иметь значение '{expectedValue}'")
    public Input shouldHaveValue(String expectedValue) {
        logger.info("Проверка '{}' должно иметь значение '{}' {}", name, expectedValue, getContext());
        element.shouldHave(value(expectedValue));
        return this;
    }
    
    /**
     * Проверить, что поле пустое.
     * 
     * @return this для fluent API
     */
    @Step("'{this.name}' должно быть пустым")
    public Input shouldBeEmpty() {
        logger.info("Проверка '{}' должно быть пустым {}", name, getContext());
        element.shouldBe(empty);
        return this;
    }
    
    /**
     * Установить фокус на поле.
     * 
     * @return this для fluent API
     */
    @Step("Установить фокус на '{this.name}'")
    public Input focus() {
        logger.info("Установка фокуса на '{}' {}", name, getContext());
        element.click();
        return this;
    }
}