package com.company.hex.api.annotations.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated method performs an HTTP GET request.
 * The value specifies the relative path for the request.
 * 
 * <p>Example usage:
 * <pre>
 * {@code
 * @GET("/users/{id}")
 * UserDto getUser(@Path("id") int userId);
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GET {
    /**
     * The relative path for the GET request.
     * Can include path parameters in curly braces (e.g., "/users/{id}").
     * 
     * @return the relative path
     */
    String value() default "";
}