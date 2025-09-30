package com.company.hex.api.converter;

import io.restassured.response.Response;
import java.lang.reflect.Type;

/**
 * Interface for converting HTTP responses to Java types.
 * Implementations can use different serialization libraries (Jackson, Gson, etc.).
 * 
 * @author Hex Framework
 * @version 1.0
 */
public interface ResponseConverter {
    
    /**
     * Convert an HTTP response to the specified Java type.
     * 
     * @param response the HTTP response to convert
     * @param returnType the target Java type
     * @param <T> the type parameter
     * @return the converted object
     */
    <T> T convert(Response response, Type returnType);
}