package com.company.hex.api.annotations.param;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated parameter should be sent as an HTTP header.
 * The value specifies the header name.
 * 
 * <p>Example usage:
 * <pre>
 * {@code
 * @GET("/users/{id}")
 * UserDto getUser(@Path("id") int userId, @Header("Authorization") String token);
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Header {
    /**
     * The name of the HTTP header.
     * 
     * @return the header name
     */
    String value();
}