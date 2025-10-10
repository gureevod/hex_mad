package com.company.hex.ui.core;

import com.company.hex.core.logging.HexLoggerFactory;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.List;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

/**
 * Базовый класс для UI компонентов с интегрированным логированием и Allure шагами.
 * Представляет составные элементы интерфейса, состоящие из нескольких базовых элементов.
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public abstract class BaseComponent {
    
    protected final Logger logger = HexLoggerFactory.getUiLogger(this.getClass());
    protected final SelenideElement rootElement;
    protected final String componentName;
    
    /**
     * Конструктор базового компонента.
     * 
     * @param rootElement корневой элемент компонента
     * @param componentName имя компонента для логирования
     */
    public BaseComponent(SelenideElement rootElement, String componentName) {
        this.rootElement = rootElement;
        this.componentName = componentName != null ? componentName : this.getClass().getSimpleName();
        logger.debug("🖥️ Инициализирован компонент: {}", this.componentName);
    }
    
    /**
     * Конструктор с CSS селектором корневого элемента.
     * 
     * @param rootSelector CSS селектор корневого элемента
     * @param componentName имя компонента для логирования
     */
    public BaseComponent(String rootSelector, String componentName) {
        this($(rootSelector), componentName);
    }
    
    /**
     * Проверяет, что компонент загружен и видим.
     * 
     * @return этот компонент для цепочки вызовов
     */
    @Step("🔄 Проверка загрузки компонента: {this.componentName}")
    public BaseComponent shouldBeLoaded() {
        logger.info("🔄 Проверка загрузки компонента: {}", componentName);
        try {
            rootElement.shouldBe(visible);
            logger.debug("✅ Компонент {} успешно загружен", componentName);
            return this;
        } catch (Exception e) {
            logger.error("❌ Компонент {} не загружен: {}", componentName, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Проверяет, что компонент загружен с таймаутом.
     * 
     * @param timeoutMs таймаут в миллисекундах
     * @return этот компонент для цепочки вызовов
     */
    @Step("🔄 Ожидание загрузки компонента {this.componentName} ({timeoutMs} мс)")
    public BaseComponent shouldBeLoaded(long timeoutMs) {
        logger.info("🔄 Ожидание загрузки компонента {} ({} мс)", componentName, timeoutMs);
        try {
            rootElement.shouldBe(visible, Duration.ofMillis(timeoutMs));
            logger.debug("✅ Компонент {} успешно загружен за {} мс", componentName, timeoutMs);
            return this;
        } catch (Exception e) {
            logger.error("❌ Компонент {} не загружен за {} мс: {}", componentName, timeoutMs, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Проверяет, что компонент скрыт.
     * 
     * @return этот компонент для цепочки вызовов
     */
    @Step("🙈 Проверка скрытости компонента: {this.componentName}")
    public BaseComponent shouldBeHidden() {
        logger.info("🙈 Проверка скрытости компонента: {}", componentName);
        try {
            rootElement.shouldBe(hidden);
            logger.debug("✅ Компонент {} скрыт", componentName);
            return this;
        } catch (Exception e) {
            logger.error("❌ Компонент {} не скрыт: {}", componentName, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Создает элемент внутри компонента.
     * 
     * @param selector CSS селектор относительно корневого элемента
     * @param elementName имя элемента
     * @return базовый элемент
     */
    protected BaseElement element(String selector, String elementName) {
        SelenideElement element = rootElement.$(selector);
        logger.debug("🔍 Создан элемент '{}' в компоненте '{}' с селектором: {}", elementName, componentName, selector);
        return new BaseElement(element, elementName);
    }
    
    /**
     * Создает коллекцию элементов внутри компонента.
     * 
     * @param selector CSS селектор относительно корневого элемента
     * @param elementsName имя коллекции элементов
     * @return коллекция элементов
     */
    protected ElementsCollection elements(String selector, String elementsName) {
        ElementsCollection elements = rootElement.$$(selector);
        logger.debug("🔍 Создана коллекция элементов '{}' в компоненте '{}' с селектором: {}", elementsName, componentName, selector);
        return elements;
    }
    
    /**
     * Находит элемент по тексту внутри компонента.
     * 
     * @param text текст для поиска
     * @param elementName имя элемента
     * @return базовый элемент
     */
    @Step("🔍 Поиск элемента по тексту '{text}' в компоненте {this.componentName}")
    protected BaseElement elementByText(String text, String elementName) {
        logger.debug("🔍 Поиск элемента по тексту '{}' в компоненте '{}'", text, componentName);
        try {
            SelenideElement element = rootElement.$x(String.format(".//*[contains(text(), '%s')]", text));
            return new BaseElement(element, elementName);
        } catch (Exception e) {
            logger.error("❌ Не удалось найти элемент по тексту '{}' в компоненте '{}': {}", text, componentName, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Находит элемент по частичному тексту внутри компонента.
     * 
     * @param partialText частичный текст для поиска
     * @param elementName имя элемента
     * @return базовый элемент
     */
    @Step("🔍 Поиск элемента по частичному тексту '{partialText}' в компоненте {this.componentName}")
    protected BaseElement elementByPartialText(String partialText, String elementName) {
        logger.debug("🔍 Поиск элемента по частичному тексту '{}' в компоненте '{}'", partialText, componentName);
        try {
            SelenideElement element = rootElement.$x(String.format(".//*[contains(text(), '%s')]", partialText));
            return new BaseElement(element, elementName);
        } catch (Exception e) {
            logger.error("❌ Не удалось найти элемент по частичному тексту '{}' в компоненте '{}': {}", partialText, componentName, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Находит элемент по атрибуту внутри компонента.
     * 
     * @param attributeName имя атрибута
     * @param attributeValue значение атрибута
     * @param elementName имя элемента
     * @return базовый элемент
     */
    @Step("🔍 Поиск элемента по атрибуту {attributeName}='{attributeValue}' в компоненте {this.componentName}")
    protected BaseElement elementByAttribute(String attributeName, String attributeValue, String elementName) {
        logger.debug("🔍 Поиск элемента по атрибуту {}='{}' в компоненте '{}'", attributeName, attributeValue, componentName);
        try {
            SelenideElement element = rootElement.$x(String.format(".//*[@%s='%s']", attributeName, attributeValue));
            return new BaseElement(element, elementName);
        } catch (Exception e) {
            logger.error("❌ Не удалось найти элемент по атрибуту {}='{}' в компоненте '{}': {}", attributeName, attributeValue, componentName, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Получает количество элементов в коллекции.
     * 
     * @param selector CSS селектор коллекции
     * @return количество элементов
     */
    @Step("🔢 Подсчет элементов по селектору '{selector}' в компоненте {this.componentName}")
    protected int getElementsCount(String selector) {
        logger.debug("🔢 Подсчет элементов по селектору '{}' в компоненте '{}'", selector, componentName);
        try {
            int count = rootElement.$$(selector).size();
            logger.debug("✅ Найдено {} элементов по селектору '{}' в компоненте '{}'", count, selector, componentName);
            return count;
        } catch (Exception e) {
            logger.error("❌ Ошибка при подсчете элементов по селектору '{}' в компоненте '{}': {}", selector, componentName, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Проверяет, содержит ли компонент текст.
     * 
     * @param expectedText ожидаемый текст
     * @return этот компонент для цепочки вызовов
     */
    @Step("📝 Проверка наличия текста '{expectedText}' в компоненте {this.componentName}")
    public BaseComponent shouldContainText(String expectedText) {
        logger.info("📝 Проверка наличия текста '{}' в компоненте '{}'", expectedText, componentName);
        try {
            rootElement.shouldHave(text(expectedText));
            logger.debug("✅ Компонент '{}' содержит ожидаемый текст", componentName);
            return this;
        } catch (Exception e) {
            logger.error("❌ Компонент '{}' не содержит ожидаемый текст '{}': {}", componentName, expectedText, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Проверяет, не содержит ли компонент текст.
     * 
     * @param unexpectedText нежелательный текст
     * @return этот компонент для цепочки вызовов
     */
    @Step("📝 Проверка отсутствия текста '{unexpectedText}' в компоненте {this.componentName}")
    public BaseComponent shouldNotContainText(String unexpectedText) {
        logger.info("📝 Проверка отсутствия текста '{}' в компоненте '{}'", unexpectedText, componentName);
        try {
            rootElement.shouldNotHave(text(unexpectedText));
            logger.debug("✅ Компонент '{}' не содержит нежелательный текст", componentName);
            return this;
        } catch (Exception e) {
            logger.error("❌ Компонент '{}' содержит нежелательный текст '{}': {}", componentName, unexpectedText, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Прокручивает к компоненту.
     * 
     * @return этот компонент для цепочки вызовов
     */
    @Step("📜 Прокрутка к компоненту: {this.componentName}")
    public BaseComponent scrollTo() {
        logger.info("📜 Прокрутка к компоненту: {}", componentName);
        try {
            rootElement.scrollTo();
            logger.debug("✅ Прокрутили к компоненту: {}", componentName);
            return this;
        } catch (Exception e) {
            logger.error("❌ Ошибка при прокрутке к компоненту '{}': {}", componentName, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Получает корневой элемент компонента.
     * 
     * @return корневой Selenide элемент
     */
    public SelenideElement getRootElement() {
        return rootElement;
    }
    
    /**
     * Получает имя компонента.
     * 
     * @return имя компонента
     */
    public String getComponentName() {
        return componentName;
    }
    
    /**
     * Проверяет, существует ли компонент в DOM.
     * 
     * @return true если компонент существует
     */
    @Step("🔍 Проверка существования компонента: {this.componentName}")
    public boolean exists() {
        logger.debug("🔍 Проверка существования компонента: {}", componentName);
        try {
            boolean exists = rootElement.exists();
            logger.debug("✅ Компонент '{}' {}", componentName, exists ? "существует" : "не существует");
            return exists;
        } catch (Exception e) {
            logger.error("❌ Ошибка при проверке существования компонента '{}': {}", componentName, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public String toString() {
        return String.format("BaseComponent{name='%s'}", componentName);
    }
}