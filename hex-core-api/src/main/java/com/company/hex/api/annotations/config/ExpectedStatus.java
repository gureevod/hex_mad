package com.company.hex.api.annotations.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the expected HTTP status code(s) for a request.
 * If the actual status code doesn't match, an exception will be thrown.
 * 
 * <p>Example usage:
 * <pre>
 * {@code
 * @DELETE("/users/{id}")
 * @ExpectedStatus(204)
 * void deleteUser(@Path("id") int userId);
 * 
 * @GET("/users/{id}")
 * @ExpectedStatus({200, 304})
 * UserDto getUser(@Path("id") int userId);
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExpectedStatus {
    /**
     * The expected HTTP status code(s).
     * Multiple values can be specified for cases where different status codes are acceptable.
     * 
     * @return array of expected status codes
     */
    int[] value();
}