package com.company.hex.api.annotations.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для определения статических HTTP заголовков на уровне метода.
 * Заголовки задаются в формате "Header-Name: Header-Value".
 * 
 * Пример использования:
 * <pre>
 * {@code
 * @POST("/api/users")
 * @Headers({
 *     "Content-Type: application/json",
 *     "X-API-Version: 2",
 *     "Accept-Language: ru-RU"
 * })
 * UserDto createUser(@Body CreateUserRequest request);
 * }
 * </pre>
 * 
 * Статические заголовки объединяются с динамическими заголовками,
 * переданными через параметры с аннотацией @Header.
 * 
 * @author Hex Framework
 * @version 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Headers {
    
    /**
     * Массив заголовков в формате "Header-Name: Header-Value".
     * Поддерживаются плейсхолдеры свойств в формате ${property.name}.
     * 
     * @return массив строк заголовков
     */
    String[] value();
}