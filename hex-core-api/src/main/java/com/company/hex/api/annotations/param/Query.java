package com.company.hex.api.annotations.param;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated parameter should be added as a query parameter.
 * The value specifies the name of the query parameter in the URL.
 * 
 * <p>Example usage:
 * <pre>
 * {@code
 * @GET("/users/search")
 * List<UserDto> searchUsers(@Query("name") String name, @Query("age") Integer age);
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Query {
    /**
     * The name of the query parameter.
     * Will be appended to the URL as ?name=value or &name=value.
     * 
     * @return the query parameter name
     */
    String value();
}