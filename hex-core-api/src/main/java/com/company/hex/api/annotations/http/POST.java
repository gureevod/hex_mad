package com.company.hex.api.annotations.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated method performs an HTTP POST request.
 * The value specifies the relative path for the request.
 * 
 * <p>Example usage:
 * <pre>
 * {@code
 * @POST("/users")
 * UserDto createUser(@Body CreateUserRequest request);
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface POST {
    /**
     * The relative path for the POST request.
     * Can include path parameters in curly braces (e.g., "/users/{id}/posts").
     * 
     * @return the relative path
     */
    String value() default "";
}