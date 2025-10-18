package com.company.hex.ui.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для декларативного определения коллекции UI элементов в Page Object или Component.
 * Используется для автоматической инициализации списков элементов с lazy loading.
 * Возвращает ElementList<T> с методами filter(), find(), map().
 * 
 * <p>Пример использования:</p>
 * <pre>
 * {@code
 * @Elements(name = "Menu Items", xpath = "//li[@class='menu-item']")
 * ElementList<Button> menuItems;
 * 
 * @Elements(name = "Product Cards", css = "div.product-card")
 * ElementList<TextElement> productCards;
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Elements {
    
    /**
     * Имя коллекции элементов для логирования и Allure steps.
     * Обязательный параметр.
     * 
     * @return имя коллекции
     */
    String name();
    
    /**
     * XPath локатор для поиска элементов.
     * Один из локаторов (xpath или css) обязателен.
     * 
     * @return XPath локатор
     */
    String xpath() default "";
    
    /**
     * CSS селектор для поиска элементов.
     * Один из локаторов (xpath или css) обязателен.
     * 
     * @return CSS селектор
     */
    String css() default "";
    
    /**
     * Таймаут ожидания элементов в секундах.
     * Если не указан, используется значение по умолчанию из конфигурации.
     * 
     * @return таймаут в секундах
     */
    int timeout() default -1;
    
    /**
     * Интервал polling при ожидании элементов в миллисекундах.
     * Если не указан, используется значение по умолчанию из конфигурации.
     * 
     * @return интервал polling в миллисекундах
     */
    int pollingInterval() default -1;
}