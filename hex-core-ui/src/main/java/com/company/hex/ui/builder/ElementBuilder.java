package com.company.hex.ui.builder;

import com.company.hex.ui.core.BaseElement;
import com.company.hex.ui.factory.ElementCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;

/**
 * Базовый builder для создания UI элементов с fluent API.
 * Поддерживает составные локаторы, параметризацию и кастомизацию.
 * 
 * <p>Пример использования:</p>
 * <pre>
 * {@code
 * Input emailField = ElementBuilder.input()
 *     .withName("Email Field")
 *     .locator()
 *         .base("//div[@class='form']")
 *         .append("//input[@type='email']")
 *         .build();
 * }
 * </pre>
 * 
 * @param <T> тип элемента
 * @param <B> тип builder'а (для fluent API)
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public abstract class ElementBuilder<T extends BaseElement, B extends ElementBuilder<T, B>> {
    
    protected static final Logger logger = LoggerFactory.getLogger(ElementBuilder.class);
    
    protected String name;
    protected CompositeLocatorBuilder locatorBuilder;
    protected String simpleLocator;
    protected Duration timeout;
    protected Duration pollingInterval;
    protected Class<? extends BaseElement> customImplementation;
    
    /**
     * Создать новый builder.
     */
    protected ElementBuilder() {
        this.locatorBuilder = new CompositeLocatorBuilder();
    }
    
    /**
     * Установить имя элемента (обязательно).
     * 
     * @param name имя элемента для логирования и Allure steps
     * @return this для fluent API
     */
    @SuppressWarnings("unchecked")
    public B withName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя элемента не может быть null или пустым");
        }
        this.name = name;
        return (B) this;
    }
    
    /**
     * Начать построение составного локатора.
     * 
     * @return builder локатора
     */
    public LocatorBuilderContext locator() {
        return new LocatorBuilderContext();
    }
    
    /**
     * Установить timeout для ожидания элемента.
     * 
     * @param timeout timeout
     * @return this для fluent API
     */
    @SuppressWarnings("unchecked")
    public B withTimeout(Duration timeout) {
        this.timeout = timeout;
        return (B) this;
    }
    
    /**
     * Установить интервал polling для ожидания элемента.
     * 
     * @param pollingInterval интервал polling
     * @return this для fluent API
     */
    @SuppressWarnings("unchecked")
    public B withPollingInterval(Duration pollingInterval) {
        this.pollingInterval = pollingInterval;
        return (B) this;
    }
    
    /**
     * Использовать кастомную реализацию элемента.
     * 
     * @param implClass класс кастомной реализации
     * @return this для fluent API
     */
    @SuppressWarnings("unchecked")
    public B withCustomImplementation(Class<? extends BaseElement> implClass) {
        this.customImplementation = implClass;
        return (B) this;
    }
    
    /**
     * Короткий синтаксис для кастомной реализации.
     * 
     * @param <E> тип элемента
     * @param elementClass класс элемента
     * @return элемент указанного типа
     */
    @SuppressWarnings("unchecked")
    public <E extends BaseElement> E as(Class<E> elementClass) {
        this.customImplementation = elementClass;
        return (E) build();
    }
    
    /**
     * Построить элемент.
     *
     * @return созданный элемент
     */
    public T build() {
        validateBeforeBuild();
        
        String locator = getLocator();
        
        Class<? extends BaseElement> elementClass = customImplementation != null
            ? customImplementation
            : getDefaultElementClass();
        
        // Используем ElementCreator для создания элемента
        ElementCreator.CreateElementRequest<?> request =
            ElementCreator.CreateElementRequest.builder(elementClass)
                .withName(name)
                .withLocator(locator)
                .withXpath(true)  // Builder использует XPath по умолчанию
                .build();
        
        @SuppressWarnings("unchecked")
        T element = (T) ElementCreator.create(request);
        
        logger.debug("Создан элемент '{}' типа {} с локатором: {}",
            name, elementClass.getSimpleName(), locator);
        
        return element;
    }
    
    /**
     * Построить элемент с подстановкой параметров.
     *
     * @param params параметры для подстановки (пары ключ-значение)
     * @return элемент с разрешенным локатором
     */
    public T resolve(String... params) {
        validateBeforeBuild();
        
        String locator;
        if (simpleLocator != null) {
            // Для простых локаторов создаем временный builder
            CompositeLocatorBuilder tempBuilder = new CompositeLocatorBuilder().base(simpleLocator);
            locator = tempBuilder.resolve(params);
        } else {
            locator = locatorBuilder.resolve(params);
        }
        
        Class<? extends BaseElement> elementClass = customImplementation != null
            ? customImplementation
            : getDefaultElementClass();
        
        // Используем ElementCreator для создания элемента
        ElementCreator.CreateElementRequest<?> request =
            ElementCreator.CreateElementRequest.builder(elementClass)
                .withName(name)
                .withLocator(locator)
                .withXpath(true)
                .build();
        
        @SuppressWarnings("unchecked")
        T element = (T) ElementCreator.create(request);
        
        logger.debug("Создан параметризованный элемент '{}' типа {} с локатором: {}",
            name, elementClass.getSimpleName(), locator);
        
        return element;
    }
    
    /**
     * Построить элемент с подстановкой параметров из Map.
     *
     * @param params map параметров
     * @return элемент с разрешенным локатором
     */
    public T resolve(Map<String, String> params) {
        validateBeforeBuild();
        
        String locator;
        if (simpleLocator != null) {
            CompositeLocatorBuilder tempBuilder = new CompositeLocatorBuilder().base(simpleLocator);
            locator = tempBuilder.resolve(params);
        } else {
            locator = locatorBuilder.resolve(params);
        }
        
        Class<? extends BaseElement> elementClass = customImplementation != null
            ? customImplementation
            : getDefaultElementClass();
        
        // Используем ElementCreator для создания элемента
        ElementCreator.CreateElementRequest<?> request =
            ElementCreator.CreateElementRequest.builder(elementClass)
                .withName(name)
                .withLocator(locator)
                .withXpath(true)
                .build();
        
        @SuppressWarnings("unchecked")
        T element = (T) ElementCreator.create(request);
        
        return element;
    }
    
    /**
     * Получить класс элемента по умолчанию.
     * Должен быть переопределен в наследниках.
     * 
     * @return класс элемента
     */
    protected abstract Class<? extends BaseElement> getDefaultElementClass();
    
    /**
     * Валидация перед построением.
     */
    protected void validateBeforeBuild() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalStateException(
                "Имя элемента должно быть установлено через withName()");
        }
        
        if (simpleLocator == null && locatorBuilder.build().isEmpty()) {
            throw new IllegalStateException(
                "Локатор должен быть установлен через locator() или конструктор");
        }
    }
    
    /**
     * Получить финальный локатор.
     *
     * @return локатор
     */
    protected String getLocator() {
        return simpleLocator != null ? simpleLocator : locatorBuilder.build();
    }
    
    /**
     * Контекст для построения составного локатора.
     * Позволяет использовать fluent API для локаторов.
     */
    public class LocatorBuilderContext {
        
        /**
         * Установить базовую часть локатора.
         * 
         * @param xpath базовый XPath
         * @return this для fluent API
         */
        public LocatorBuilderContext base(String xpath) {
            locatorBuilder.base(xpath);
            return this;
        }
        
        /**
         * Добавить часть локатора.
         * 
         * @param xpath XPath для добавления
         * @return this для fluent API
         */
        public LocatorBuilderContext append(String xpath) {
            locatorBuilder.append(xpath);
            return this;
        }
        
        /**
         * Завершить построение локатора и вернуться к builder'у элемента.
         * 
         * @return builder элемента
         */
        @SuppressWarnings("unchecked")
        public B build() {
            return (B) ElementBuilder.this;
        }
    }
}