package com.company.hex.ui.core;

import com.company.hex.core.logging.HexLoggerFactory;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.slf4j.Logger;

import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;

/**
 * Базовый класс для всех UI элементов с интегрированным логированием и Allure шагами.
 * Предоставляет обертки над Selenide элементами с автоматическим логированием действий.
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public class BaseElement {
    
    protected final Logger logger = HexLoggerFactory.getUiLogger(this.getClass());
    protected final SelenideElement element;
    protected final String elementName;
    
    /**
     * Конструктор базового элемента.
     * 
     * @param element Selenide элемент
     * @param elementName имя элемента для логирования
     */
    public BaseElement(SelenideElement element, String elementName) {
        this.element = element;
        this.elementName = elementName != null ? elementName : "Element";
        logger.debug("🖥️ Инициализирован элемент: {}", this.elementName);
    }
    
    /**
     * Конструктор с CSS селектором.
     * 
     * @param cssSelector CSS селектор элемента
     * @param elementName имя элемента для логирования
     */
    public BaseElement(String cssSelector, String elementName) {
        this($(cssSelector), elementName);
    }
    
    /**
     * Кликает по элементу с логированием.
     * 
     * @return этот элемент для цепочки вызовов
     */
    @Step("🖱️ Клик по элементу: {this.elementName}")
    public BaseElement click() {
        logger.info("🖱️ Клик по элементу: {}", elementName);
        try {
            element.click();
            logger.debug("✅ Успешно кликнули по элементу: {}", elementName);
            return this;
        } catch (Exception e) {
            logger.error("❌ Ошибка при клике по элементу {}: {}", elementName, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Вводит текст в элемент с логированием.
     * 
     * @param text текст для ввода
     * @return этот элемент для цепочки вызовов
     */
    @Step("⌨️ Ввод текста в {this.elementName}: '{text}'")
    public BaseElement sendKeys(String text) {
        logger.info("⌨️ Ввод текста в {}: '{}'", elementName, text);
        try {
            element.sendKeys(text);
            logger.debug("✅ Успешно ввели текст в элемент: {}", elementName);
            return this;
        } catch (Exception e) {
            logger.error("❌ Ошибка при вводе текста в элемент {}: {}", elementName, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Очищает поле и вводит новый текст.
     * 
     * @param text текст для ввода
     * @return этот элемент для цепочки вызовов
     */
    @Step("🗑️ Очистка и ввод текста в {this.elementName}: '{text}'")
    public BaseElement setValue(String text) {
        logger.info("🗑️ Очистка и ввод текста в {}: '{}'", elementName, text);
        try {
            element.setValue(text);
            logger.debug("✅ Успешно установили значение в элемент: {}", elementName);
            return this;
        } catch (Exception e) {
            logger.error("❌ Ошибка при установке значения в элемент {}: {}", elementName, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Получает текст элемента с логированием.
     * 
     * @return текст элемента
     */
    @Step("📋 Получение текста элемента: {this.elementName}")
    public String getText() {
        logger.debug("📋 Получение текста элемента: {}", elementName);
        try {
            String text = element.getText();
            logger.debug("✅ Получен текст элемента {}: '{}'", elementName, text);
            return text;
        } catch (Exception e) {
            logger.error("❌ Ошибка при получении текста элемента {}: {}", elementName, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Получает значение атрибута элемента.
     * 
     * @param attributeName имя атрибута
     * @return значение атрибута
     */
    @Step("📋 Получение атрибута '{attributeName}' элемента: {this.elementName}")
    public String getAttribute(String attributeName) {
        logger.debug("📋 Получение атрибута '{}' элемента: {}", attributeName, elementName);
        try {
            String value = element.getAttribute(attributeName);
            logger.debug("✅ Получен атрибут '{}' элемента {}: '{}'", attributeName, elementName, value);
            return value;
        } catch (Exception e) {
            logger.error("❌ Ошибка при получении атрибута '{}' элемента {}: {}", attributeName, elementName, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Проверяет видимость элемента.
     * 
     * @return этот элемент для цепочки вызовов
     */
    @Step("👁️ Проверка видимости элемента: {this.elementName}")
    public BaseElement shouldBeVisible() {
        logger.info("👁️ Проверка видимости элемента: {}", elementName);
        try {
            element.shouldBe(visible);
            logger.debug("✅ Элемент {} видим", elementName);
            return this;
        } catch (Exception e) {
            logger.error("❌ Элемент {} не видим: {}", elementName, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Проверяет, что элемент скрыт.
     * 
     * @return этот элемент для цепочки вызовов
     */
    @Step("🙈 Проверка скрытости элемента: {this.elementName}")
    public BaseElement shouldBeHidden() {
        logger.info("🙈 Проверка скрытости элемента: {}", elementName);
        try {
            element.shouldBe(hidden);
            logger.debug("✅ Элемент {} скрыт", elementName);
            return this;
        } catch (Exception e) {
            logger.error("❌ Элемент {} не скрыт: {}", elementName, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Проверяет, что элемент содержит указанный текст.
     * 
     * @param expectedText ожидаемый текст
     * @return этот элемент для цепочки вызовов
     */
    @Step("📝 Проверка текста элемента {this.elementName}: должен содержать '{expectedText}'")
    public BaseElement shouldHaveText(String expectedText) {
        logger.info("📝 Проверка текста элемента {}: должен содержать '{}'", elementName, expectedText);
        try {
            element.shouldHave(text(expectedText));
            logger.debug("✅ Элемент {} содержит ожидаемый текст", elementName);
            return this;
        } catch (Exception e) {
            logger.error("❌ Элемент {} не содержит ожидаемый текст '{}': {}", elementName, expectedText, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Проверяет точное соответствие текста элемента.
     * 
     * @param exactText точный текст
     * @return этот элемент для цепочки вызовов
     */
    @Step("📝 Проверка точного текста элемента {this.elementName}: '{exactText}'")
    public BaseElement shouldHaveExactText(String exactText) {
        logger.info("📝 Проверка точного текста элемента {}: '{}'", elementName, exactText);
        try {
            element.shouldHave(exactText(exactText));
            logger.debug("✅ Элемент {} имеет точный текст", elementName);
            return this;
        } catch (Exception e) {
            logger.error("❌ Элемент {} не имеет точный текст '{}': {}", elementName, exactText, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Проверяет, что элемент включен (enabled).
     * 
     * @return этот элемент для цепочки вызовов
     */
    @Step("✅ Проверка доступности элемента: {this.elementName}")
    public BaseElement shouldBeEnabled() {
        logger.info("✅ Проверка доступности элемента: {}", elementName);
        try {
            element.shouldBe(enabled);
            logger.debug("✅ Элемент {} доступен", elementName);
            return this;
        } catch (Exception e) {
            logger.error("❌ Элемент {} недоступен: {}", elementName, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Проверяет, что элемент отключен (disabled).
     * 
     * @return этот элемент для цепочки вызовов
     */
    @Step("❌ Проверка недоступности элемента: {this.elementName}")
    public BaseElement shouldBeDisabled() {
        logger.info("❌ Проверка недоступности элемента: {}", elementName);
        try {
            element.shouldBe(disabled);
            logger.debug("✅ Элемент {} отключен", elementName);
            return this;
        } catch (Exception e) {
            logger.error("❌ Элемент {} не отключен: {}", elementName, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Ожидает появления элемента.
     * 
     * @param timeoutMs таймаут в миллисекундах
     * @return этот элемент для цепочки вызовов
     */
    @Step("⏳ Ожидание появления элемента {this.elementName} ({timeoutMs} мс)")
    public BaseElement waitForVisible(long timeoutMs) {
        logger.info("⏳ Ожидание появления элемента {} ({} мс)", elementName, timeoutMs);
        try {
            element.shouldBe(visible, Duration.ofMillis(timeoutMs));
            logger.debug("✅ Элемент {} появился", elementName);
            return this;
        } catch (Exception e) {
            logger.error("❌ Элемент {} не появился за {} мс: {}", elementName, timeoutMs, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Проверяет, существует ли элемент в DOM.
     * 
     * @return true если элемент существует
     */
    @Step("🔍 Проверка существования элемента: {this.elementName}")
    public boolean exists() {
        logger.debug("🔍 Проверка существования элемента: {}", elementName);
        try {
            boolean exists = element.exists();
            logger.debug("✅ Элемент {} {}", elementName, exists ? "существует" : "не существует");
            return exists;
        } catch (Exception e) {
            logger.error("❌ Ошибка при проверке существования элемента {}: {}", elementName, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Получает базовый Selenide элемент.
     * 
     * @return Selenide элемент
     */
    public SelenideElement getElement() {
        return element;
    }
    
    /**
     * Получает имя элемента.
     * 
     * @return имя элемента
     */
    public String getElementName() {
        return elementName;
    }
    
    @Override
    public String toString() {
        return String.format("BaseElement{name='%s'}", elementName);
    }
}