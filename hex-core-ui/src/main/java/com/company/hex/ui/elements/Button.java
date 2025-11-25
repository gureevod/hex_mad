package com.company.hex.ui.elements;

import com.codeborne.selenide.SelenideElement;
import com.company.hex.ui.core.BaseElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.text;

/**
 * Класс для работы с кнопками и кликабельными элементами.
 * Предоставляет методы для клика, двойного клика и проверки текста.
 * 
 * <p>Пример использования:</p>
 * <pre>
 * {@code
 * @Element(name = "Submit Button", xpath = "//button[@type='submit']")
 * Button submitButton;
 * 
 * submitButton.click()
 *             .shouldHave(text("Submit"))
 *             .shouldBe(visible, enabled);
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public class Button extends BaseElement {
    
    /**
     * Конструктор для создания Button элемента.
     * 
     * @param name имя элемента для логирования
     * @param element Selenide элемент
     */
    public Button(String name, SelenideElement element) {
        super(name, element);
    }
    
    /**
     * Кликнуть по кнопке.
     * Переопределяет базовый метод для возврата типа Button.
     * 
     * @return this для fluent API
     */
    @Override
    @Step("Клик по кнопке '{this.name}'")
    public Button click() {
        executeWithInterceptors("click", new Object[]{}, () -> {
            logger.info("Клик по кнопке '{}' {}", name, getContext());
            element.click();
            logger.debug("Кнопка '{}' успешно нажата", name);
        });
        return this;
    }
    
    /**
     * Двойной клик по кнопке.
     * Переопределяет базовый метод для возврата типа Button.
     * 
     * @return this для fluent API
     */
    @Override
    @Step("Двойной клик по кнопке '{this.name}'")
    public Button doubleClick() {
        executeWithInterceptors("doubleClick", new Object[]{}, () -> {
            logger.info("Двойной клик по кнопке '{}' {}", name, getContext());
            element.doubleClick();
            logger.debug("Двойной клик по кнопке '{}' выполнен", name);
        });
        return this;
    }
    
    /**
     * Навести курсор на кнопку.
     * Переопределяет базовый метод для возврата типа Button.
     * 
     * @return this для fluent API
     */
    @Override
    @Step("Навести курсор на кнопку '{this.name}'")
    public Button hover() {
        executeWithInterceptors("hover", new Object[]{}, () -> {
            logger.info("Наведение курсора на кнопку '{}' {}", name, getContext());
            element.hover();
            logger.debug("Курсор наведен на кнопку '{}'", name);
        });
        return this;
    }
    
    /**
     * Клик с использованием JavaScript.
     * Полезно для элементов, которые перекрыты другими элементами.
     * 
     * @return this для fluent API
     */
    @Step("Клик по кнопке '{this.name}' через JavaScript")
    public Button clickViaJs() {
        executeWithInterceptors("clickViaJs", new Object[]{}, () -> {
            logger.info("Клик через JavaScript по кнопке '{}' {}", name, getContext());
            element.click(com.codeborne.selenide.ClickOptions.usingJavaScript());
            logger.debug("Клик через JavaScript по кнопке '{}' выполнен", name);
        });
        return this;
    }
    
    /**
     * Клик с смещением от центра элемента.
     *
     * @param offsetX смещение по X
     * @param offsetY смещение по Y
     * @return this для fluent API
     */
    @Step("Клик по кнопке '{this.name}' со смещением ({offsetX}, {offsetY})")
    public Button clickWithOffset(int offsetX, int offsetY) {
        executeWithInterceptors("clickWithOffset", new Object[]{offsetX, offsetY}, () -> {
            logger.info("Клик со смещением ({}, {}) по кнопке '{}' {}", offsetX, offsetY, name, getContext());
            element.click(com.codeborne.selenide.ClickOptions.usingDefaultMethod().offset(offsetX, offsetY));
            logger.debug("Клик со смещением по кнопке '{}' выполнен", name);
        });
        return this;
    }
    
    /**
     * Проверить, что кнопка содержит указанный текст.
     * 
     * @param expectedText ожидаемый текст
     * @return this для fluent API
     */
    @Step("Кнопка '{this.name}' должна содержать текст '{expectedText}'")
    public Button shouldHaveText(String expectedText) {
        logger.info("Проверка кнопки '{}' должна содержать текст '{}' {}", name, expectedText, getContext());
        element.shouldHave(text(expectedText));
        return this;
    }
    
    /**
     * Проверить, что кнопка включена (enabled).
     * 
     * @return true если кнопка включена
     */
    public boolean isEnabled() {
        logger.debug("Проверка доступности кнопки '{}' {}", name, getContext());
        return element.isEnabled();
    }
    
    /**
     * Проверить, что кнопка отключена (disabled).
     * 
     * @return true если кнопка отключена
     */
    public boolean isDisabled() {
        logger.debug("Проверка отключения кнопки '{}' {}", name, getContext());
        return !element.isEnabled();
    }
}