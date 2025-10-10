package com.company.hex.api.annotations.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configures automatic retry behavior for failed requests.
 * Useful for handling transient network failures or service unavailability.
 * 
 * <p>Example usage:
 * <pre>
 * {@code
 * @PUT("/users/{id}")
 * @Retry(count = 3, delay = 1000)
 * UserDto updateUser(@Path("id") int userId, @Body UpdateUserRequest request);
 * 
 * @GET("/users/{id}")
 * @Retry(count = 5, delay = 2000)
 * UserDto getUserById(@Path("id") int userId);
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Retry {
    /**
     * Number of retry attempts.
     * Default is 3 retries.
     * 
     * @return the number of retry attempts
     */
    int count() default 3;
    
    /**
     * Delay between retry attempts in milliseconds.
     * Default is 1000ms (1 second).
     * 
     * @return the delay in milliseconds
     */
    long delay() default 1000;
}