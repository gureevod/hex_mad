package com.company.hex.ui.core;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebElementCondition;
import com.company.hex.core.logging.HexLoggerFactory;
import io.qameta.allure.Step;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;

/**
 * Базовый абстрактный класс для всех UI элементов в Hex Framework.
 * Предоставляет общую функциональность для работы с элементами, включая:
 * - Контекстное логирование (page + component + element)
 * - Автоматические Allure steps
 * - Fluent API для цепочек вызовов
 * - Интеграцию с Selenide
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public abstract class BaseElement {
    
    protected final String name;
    protected final SelenideElement element;
    protected final Logger logger;
    
    // Контекст для логирования
    protected String pageName;
    protected String componentName;
    
    // Настройки элемента (timeout, polling interval)
    protected ElementSettings settings;
    
    /**
     * Конструктор базового элемента.
     * 
     * @param name имя элемента для логирования и Allure steps
     * @param element Selenide элемент
     */
    protected BaseElement(String name, SelenideElement element) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя элемента не может быть null или пустым");
        }
        if (element == null) {
            throw new IllegalArgumentException("SelenideElement не может быть null");
        }
        
        this.name = name;
        this.element = element;
        this.logger = HexLoggerFactory.getUiLogger(getClass());
    }
    
    /**
     * Получить имя элемента.
     * 
     * @return имя элемента
     */
    public String getName() {
        return name;
    }
    
    /**
     * Получить базовый Selenide элемент.
     * 
     * @return Selenide элемент
     */
    public SelenideElement getElement() {
        return element;
    }
    
    /**
     * Получить базовый WebElement.
     * 
     * @return WebElement
     */
    public WebElement getWebElement() {
        return element.toWebElement();
    }
    
    /**
     * Установить имя страницы для контекстного логирования.
     * 
     * @param pageName имя страницы
     */
    public void setPageName(String pageName) {
        this.pageName = pageName;
    }
    
    /**
     * Установить имя компонента для контекстного логирования.
     *
     * @param componentName имя компонента
     */
    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }
    
    /**
     * Установить настройки элемента.
     *
     * @param settings настройки элемента
     */
    public void setSettings(ElementSettings settings) {
        this.settings = settings;
    }
    
    /**
     * Получить настройки элемента.
     *
     * @return настройки или null если не установлены
     */
    public ElementSettings getSettings() {
        return settings;
    }
    
    /**
     * Получить имя страницы.
     * 
     * @return имя страницы или "Unknown Page"
     */
    protected String getPageName() {
        return pageName != null ? pageName : "Unknown Page";
    }
    
    /**
     * Получить имя компонента.
     * 
     * @return имя компонента или "Root"
     */
    protected String getComponentName() {
        return componentName != null ? componentName : "Root";
    }
    
    /**
     * Получить полный контекст для логирования.
     *
     * @return строка с контекстом
     */
    protected String getContext() {
        return String.format("в странице '%s' компоненте '%s'", getPageName(), getComponentName());
    }
    
    /**
     * Получить timeout для ожидания.
     *
     * @return timeout из настроек или Duration.ofSeconds(10) по умолчанию
     */
    protected java.time.Duration getTimeout() {
        return settings != null ? settings.getTimeout() : java.time.Duration.ofSeconds(10);
    }
    
    /**
     * Получить polling interval для ожидания.
     *
     * @return polling interval из настроек или Duration.ofMillis(200) по умолчанию
     */
    protected java.time.Duration getPollingInterval() {
        return settings != null ? settings.getPollingInterval() : java.time.Duration.ofMillis(200);
    }
    
    /**
     * Временно изменить timeout для цепочки вызовов.
     * Создает новые настройки с измененным timeout.
     *
     * @param timeout новый timeout
     * @return this для fluent API
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseElement> T withTimeout(java.time.Duration timeout) {
        if (this.settings != null) {
            this.settings = this.settings.withTimeout(timeout);
        } else {
            this.settings = new ElementSettings(timeout, java.time.Duration.ofMillis(200));
        }
        return (T) this;
    }
    
    /**
     * Кликнуть по элементу.
     * 
     * @return this для fluent API
     */
    @Step("Клик по '{this.name}'")
    public BaseElement click() {
        logger.info("Клик по '{}' {}", name, getContext());
        element.click();
        return this;
    }
    
    /**
     * Двойной клик по элементу.
     * 
     * @return this для fluent API
     */
    @Step("Двойной клик по '{this.name}'")
    public BaseElement doubleClick() {
        logger.info("Двойной клик по '{}' {}", name, getContext());
        element.doubleClick();
        return this;
    }
    
    /**
     * Навести курсор на элемент.
     * 
     * @return this для fluent API
     */
    @Step("Навести курсор на '{this.name}'")
    public BaseElement hover() {
        logger.info("Наведение курсора на '{}' {}", name, getContext());
        element.hover();
        return this;
    }
    
    /**
     * Получить текст элемента.
     * 
     * @return текст элемента
     */
    @Step("Получить текст из '{this.name}'")
    public String getText() {
        logger.debug("Получение текста из '{}' {}", name, getContext());
        String text = element.getText();
        logger.debug("Текст из '{}': '{}'", name, text);
        return text;
    }
    
    /**
     * Получить значение атрибута элемента.
     * 
     * @param attributeName имя атрибута
     * @return значение атрибута
     */
    @Step("Получить атрибут '{attributeName}' из '{this.name}'")
    public String getAttribute(String attributeName) {
        logger.debug("Получение атрибута '{}' из '{}' {}", attributeName, name, getContext());
        String value = element.getAttribute(attributeName);
        logger.debug("Атрибут '{}' из '{}': '{}'", attributeName, name, value);
        return value;
    }
    
    /**
     * Проверить, что элемент соответствует условию.
     *
     * @param condition условие для проверки
     * @return this для fluent API
     */
    @Step("'{this.name}' должен быть {condition}")
    public BaseElement shouldBe(WebElementCondition condition) {
        logger.info("Проверка '{}' должен быть {} {}", name, condition, getContext());
        element.shouldBe(condition, getTimeout());
        return this;
    }
    
    /**
     * Проверить, что элемент соответствует нескольким условиям.
     * 
     * @param conditions условия для проверки
     * @return this для fluent API
     */
    public BaseElement shouldBe(WebElementCondition... conditions) {
        for (WebElementCondition condition : conditions) {
            shouldBe(condition);
        }
        return this;
    }
    
    /**
     * Проверить, что элемент имеет условие.
     *
     * @param condition условие для проверки
     * @return this для fluent API
     */
    @Step("'{this.name}' должен иметь {condition}")
    public BaseElement shouldHave(WebElementCondition condition) {
        logger.info("Проверка '{}' должен иметь {} {}", name, condition, getContext());
        element.shouldHave(condition, getTimeout());
        return this;
    }
    
    /**
     * Проверить, что элемент имеет несколько условий.
     * 
     * @param conditions условия для проверки
     * @return this для fluent API
     */
    public BaseElement shouldHave(WebElementCondition... conditions) {
        for (WebElementCondition condition : conditions) {
            shouldHave(condition);
        }
        return this;
    }
    
    /**
     * Проверить, что элемент НЕ соответствует условию.
     *
     * @param condition условие для проверки
     * @return this для fluent API
     */
    @Step("'{this.name}' НЕ должен быть {condition}")
    public BaseElement shouldNotBe(WebElementCondition condition) {
        logger.info("Проверка '{}' НЕ должен быть {} {}", name, condition, getContext());
        element.shouldNotBe(condition, getTimeout());
        return this;
    }
    
    /**
     * Проверить, что элемент НЕ имеет условие.
     *
     * @param condition условие для проверки
     * @return this для fluent API
     */
    @Step("'{this.name}' НЕ должен иметь {condition}")
    public BaseElement shouldNotHave(WebElementCondition condition) {
        logger.info("Проверка '{}' НЕ должен иметь {} {}", name, condition, getContext());
        element.shouldNotHave(condition, getTimeout());
        return this;
    }
    
    /**
     * Проверить, что элемент существует в DOM.
     * 
     * @return true если элемент существует
     */
    public boolean exists() {
        logger.debug("Проверка существования '{}' {}", name, getContext());
        return element.exists();
    }
    
    /**
     * Проверить, что элемент отображается.
     * 
     * @return true если элемент отображается
     */
    public boolean isDisplayed() {
        logger.debug("Проверка отображения '{}' {}", name, getContext());
        return element.isDisplayed();
    }
    
    /**
     * Прокрутить страницу к элементу.
     * 
     * @return this для fluent API
     */
    @Step("Прокрутить к '{this.name}'")
    public BaseElement scrollTo() {
        logger.info("Прокрутка к '{}' {}", name, getContext());
        element.scrollTo();
        return this;
    }
    
    /**
     * Прокрутить элемент в видимую область.
     * 
     * @return this для fluent API
     */
    @Step("Прокрутить '{this.name}' в видимую область")
    public BaseElement scrollIntoView() {
        logger.info("Прокрутка '{}' в видимую область {}", name, getContext());
        element.scrollIntoView(true);
        return this;
    }
    
    @Override
    public String toString() {
        return String.format("%s[name='%s', page='%s', component='%s']",
                getClass().getSimpleName(), name, getPageName(), getComponentName());
    }
}