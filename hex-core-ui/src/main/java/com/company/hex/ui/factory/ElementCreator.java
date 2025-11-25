package com.company.hex.ui.factory;

import com.codeborne.selenide.SelenideElement;
import com.company.hex.ui.core.BaseElement;
import com.company.hex.ui.core.UiContext;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codeborne.selenide.Selenide.$;

/**
 * Централизованное создание UI элементов.
 * Используется как FieldInitializer, так и ElementBuilder для устранения дублирования.
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public final class ElementCreator {
    
    private static final Logger logger = LoggerFactory.getLogger(ElementCreator.class);
    
    /**
     * Приватный конструктор для утилитного класса.
     */
    private ElementCreator() {
        throw new UnsupportedOperationException("Утилитный класс не может быть инстанцирован");
    }
    
    /**
     * Создать элемент по запросу.
     * 
     * @param request запрос на создание элемента
     * @param <T> тип элемента
     * @return созданный элемент
     */
    public static <T extends BaseElement> T create(CreateElementRequest<T> request) {
        if (request == null) {
            throw new IllegalArgumentException("Запрос на создание элемента не может быть null");
        }
        
        // Строим полный локатор
        String fullLocator = buildFullLocator(
            request.getLocator(),
            request.isXpath(),
            request.getComponentRoot()
        );
        
        logger.trace("Создание элемента '{}' с локатором: {}", request.getName(), fullLocator);
        
        // Создаем SelenideElement
        SelenideElement selenideElement = request.isXpath()
            ? $(By.xpath(fullLocator))
            : $(By.cssSelector(fullLocator));
        
        // Создаем элемент через фабрику
        T element = ElementFactory.create(
            request.getElementType(),
            request.getName(),
            selenideElement
        );
        
        // Устанавливаем контекст если предоставлен
        if (request.getContext() != null) {
            element.setPageName(request.getContext().getPageName());
            element.setComponentName(request.getContext().getComponentName());
        }
        
        logger.debug("Создан элемент '{}' типа {} с локатором '{}'",
            request.getName(), request.getElementType().getSimpleName(), fullLocator);
        
        return element;
    }
    
    /**
     * Построить полный локатор с учетом componentRoot.
     * 
     * @param locator базовый локатор
     * @param isXpath true если это XPath локатор
     * @param componentRoot корневой локатор компонента (может быть null)
     * @return полный локатор
     */
    static String buildFullLocator(String locator, boolean isXpath, String componentRoot) {
        if (locator == null || locator.trim().isEmpty()) {
            throw new IllegalArgumentException("Локатор не может быть null или пустым");
        }
        
        // Если нет root компонента, возвращаем локатор как есть
        if (componentRoot == null || componentRoot.trim().isEmpty()) {
            return locator;
        }
        
        // Комбинируем локаторы в зависимости от типа
        if (isXpath) {
            return buildXpathLocator(locator, componentRoot);
        } else {
            return buildCssLocator(locator, componentRoot);
        }
    }
    
    /**
     * Построить XPath локатор с учетом root.
     * 
     * @param locator базовый локатор
     * @param componentRoot корневой локатор
     * @return полный XPath локатор
     */
    private static String buildXpathLocator(String locator, String componentRoot) {
        if (locator.startsWith(".//")) {
            // Относительный локатор - добавляем к root
            return componentRoot + "/" + locator.substring(3);
        } else if (locator.startsWith("//")) {
            // Абсолютный локатор - используем как есть
            return locator;
        } else {
            // Локатор без префикса - делаем относительным
            return componentRoot + "//" + locator;
        }
    }
    
    /**
     * Построить CSS локатор с учетом root.
     * 
     * @param locator базовый локатор
     * @param componentRoot корневой локатор
     * @return полный CSS локатор
     */
    private static String buildCssLocator(String locator, String componentRoot) {
        // Для CSS используем вложенность через пробел
        return componentRoot + " " + locator;
    }
    
    /**
     * Запрос на создание элемента с параметрами.
     * Использует Builder паттерн для удобства.
     * 
     * @param <T> тип создаваемого элемента
     */
    public static class CreateElementRequest<T extends BaseElement> {
        
        private final Class<T> elementType;
        private final String name;
        private final String locator;
        private final boolean xpath;
        private final String componentRoot;
        private final UiContext context;
        
        /**
         * Конструктор (использовать через Builder).
         */
        private CreateElementRequest(Builder<T> builder) {
            this.elementType = builder.elementType;
            this.name = builder.name;
            this.locator = builder.locator;
            this.xpath = builder.xpath;
            this.componentRoot = builder.componentRoot;
            this.context = builder.context;
        }
        
        /**
         * Получить тип элемента.
         * 
         * @return класс элемента
         */
        public Class<T> getElementType() {
            return elementType;
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
         * Получить локатор.
         * 
         * @return локатор
         */
        public String getLocator() {
            return locator;
        }
        
        /**
         * Проверить, является ли локатор XPath.
         * 
         * @return true если XPath
         */
        public boolean isXpath() {
            return xpath;
        }
        
        /**
         * Получить корневой локатор компонента.
         * 
         * @return корневой локатор или null
         */
        public String getComponentRoot() {
            return componentRoot;
        }
        
        /**
         * Получить контекст UI.
         * 
         * @return контекст или null
         */
        public UiContext getContext() {
            return context;
        }
        
        /**
         * Создать новый builder.
         * 
         * @param <T> тип элемента
         * @param elementType класс элемента
         * @return builder
         */
        public static <T extends BaseElement> Builder<T> builder(Class<T> elementType) {
            return new Builder<>(elementType);
        }
        
        /**
         * Builder для CreateElementRequest.
         * 
         * @param <T> тип элемента
         */
        public static class Builder<T extends BaseElement> {
            
            private final Class<T> elementType;
            private String name;
            private String locator;
            private boolean xpath = true; // По умолчанию XPath
            private String componentRoot;
            private UiContext context;
            
            private Builder(Class<T> elementType) {
                if (elementType == null) {
                    throw new IllegalArgumentException("Тип элемента не может быть null");
                }
                this.elementType = elementType;
            }
            
            /**
             * Установить имя элемента.
             * 
             * @param name имя элемента
             * @return this для fluent API
             */
            public Builder<T> withName(String name) {
                this.name = name;
                return this;
            }
            
            /**
             * Установить локатор.
             * 
             * @param locator локатор
             * @return this для fluent API
             */
            public Builder<T> withLocator(String locator) {
                this.locator = locator;
                return this;
            }
            
            /**
             * Установить тип локатора.
             * 
             * @param xpath true если XPath, false если CSS
             * @return this для fluent API
             */
            public Builder<T> withXpath(boolean xpath) {
                this.xpath = xpath;
                return this;
            }
            
            /**
             * Установить корневой локатор компонента.
             * 
             * @param componentRoot корневой локатор
             * @return this для fluent API
             */
            public Builder<T> withComponentRoot(String componentRoot) {
                this.componentRoot = componentRoot;
                return this;
            }
            
            /**
             * Установить контекст UI.
             * 
             * @param context контекст
             * @return this для fluent API
             */
            public Builder<T> withContext(UiContext context) {
                this.context = context;
                return this;
            }
            
            /**
             * Построить запрос.
             * 
             * @return запрос на создание элемента
             */
            public CreateElementRequest<T> build() {
                if (name == null || name.trim().isEmpty()) {
                    throw new IllegalStateException("Имя элемента должно быть установлено");
                }
                if (locator == null || locator.trim().isEmpty()) {
                    throw new IllegalStateException("Локатор должен быть установлен");
                }
                
                return new CreateElementRequest<>(this);
            }
        }
    }
}