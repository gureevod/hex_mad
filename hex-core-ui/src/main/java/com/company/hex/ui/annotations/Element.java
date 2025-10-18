package com.company.hex.ui.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для декларативного определения UI элемента в Page Object или Component.
 * Используется для автоматической инициализации элементов с lazy loading.
 * 
 * <p>Пример использования:</p>
 * <pre>
 * {@code
 * @Element(name = "Username", xpath = "//input[@id='username']")
 * Input username;
 * 
 * @Element(name = "Submit Button", css = "button[type='submit']")
 * Button submitButton;
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Element {
    
    /**
     * Имя элемента для логирования и Allure steps.
     * Обязательный параметр.
     * 
     * @return имя элемента
     */
    String name();
    
    /**
     * XPath локатор элемента.
     * Один из локаторов (xpath или css) обязателен.
     * 
     * @return XPath локатор
     */
    String xpath() default "";
    
    /**
     * CSS селектор элемента.
     * Один из локаторов (xpath или css) обязателен.
     * 
     * @return CSS селектор
     */
    String css() default "";
    
    /**
     * Таймаут ожидания элемента в секундах.
     * Если не указан, используется значение по умолчанию из конфигурации.
     * 
     * @return таймаут в секундах
     */
    int timeout() default -1;
    
    /**
     * Интервал polling при ожидании элемента в миллисекундах.
     * Если не указан, используется значение по умолчанию из конфигурации.
     * 
     * @return интервал polling в миллисекундах
     */
    int pollingInterval() default -1;
}