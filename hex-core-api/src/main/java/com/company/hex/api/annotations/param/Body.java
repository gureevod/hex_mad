package com.company.hex.api.annotations.param;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated parameter should be used as the request body.
 * The parameter will be serialized to JSON and sent in the request body.
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
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Body {
    // No value needed - the entire parameter becomes the request body
}