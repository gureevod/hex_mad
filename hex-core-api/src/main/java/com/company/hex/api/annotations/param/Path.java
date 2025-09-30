package com.company.hex.api.annotations.param;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated parameter should be substituted into a path variable.
 * The value specifies the name of the path parameter in the URL template.
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
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Path {
    /**
     * The name of the path parameter to substitute.
     * Must match a parameter name in the URL template (e.g., "id" for "/users/{id}").
     * 
     * @return the path parameter name
     */
    String value();
}