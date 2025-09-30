package com.company.hex.api.annotations.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated method performs an HTTP PATCH request.
 * The value specifies the relative path for the request.
 * 
 * <p>Example usage:
 * <pre>
 * {@code
 * @PATCH("/users/{id}")
 * UserDto patchUser(@Path("id") int userId, @Body Map<String, Object> updates);
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PATCH {
    /**
     * The relative path for the PATCH request.
     * Can include path parameters in curly braces (e.g., "/users/{id}").
     * 
     * @return the relative path
     */
    String value() default "";
}