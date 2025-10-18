package com.company.hex.ui.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для декларативного определения Page Object.
 * Содержит метаданные о странице: URL, заголовок.
 * 
 * <p>Пример использования:</p>
 * <pre>
 * {@code
 * @Page(url = "/login", title = "Login Page")
 * public class LoginPage extends BasePage {
 *     // элементы и методы страницы
 * }
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Page {
    
    /**
     * Относительный URL страницы.
     * Используется для метода open().
     * 
     * @return URL страницы
     */
    String url() default "";
    
    /**
     * Заголовок страницы для логирования и идентификации.
     * 
     * @return заголовок страницы
     */
    String title();
}