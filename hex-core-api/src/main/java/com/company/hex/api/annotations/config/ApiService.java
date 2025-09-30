package com.company.hex.api.annotations.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an interface as an API service and provides service-level configuration.
 * This annotation is used to configure base URL and base path for all methods in the interface.
 * 
 * <p>Example usage:
 * <pre>
 * {@code
 * @ApiService(baseUrl = "https://api.example.com", basePath = "/v1")
 * public interface UserApi {
 *     @GET("/users/{id}")
 *     UserDto getUser(@Path("id") int userId);
 * }
 * }
 * </pre>
 * 
 * <p>The baseUrl can reference properties using ${property.name} syntax:
 * <pre>
 * {@code
 * @ApiService(baseUrl = "${api.base.url}")
 * public interface UserApi { ... }
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiService {
    /**
     * The base URL for all API calls in this service.
     * Can be a literal URL or a property reference like "${api.base.url}".
     * If not specified, the default configuration base URL will be used.
     * 
     * @return the base URL
     */
    String baseUrl() default "";
    
    /**
     * The base path to prepend to all method paths in this service.
     * For example, if basePath = "/api/v1" and a method has @GET("/users"),
     * the final path will be "/api/v1/users".
     * 
     * @return the base path
     */
    String basePath() default "";
}