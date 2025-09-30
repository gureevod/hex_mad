package com.company.hex.api.converter;

import com.company.hex.core.logging.HexLoggerFactory;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.slf4j.Logger;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Jackson-based implementation of ResponseConverter.
 * Converts HTTP responses to Java objects using Jackson ObjectMapper.
 * 
 * @author Hex Framework
 * @version 1.0
 */
public class JacksonResponseConverter implements ResponseConverter {
    
    private static final Logger logger = HexLoggerFactory.getApiLogger(JacksonResponseConverter.class);
    private final ObjectMapper objectMapper;
    
    public JacksonResponseConverter() {
        this.objectMapper = new ObjectMapper();
    }
    
    public JacksonResponseConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @Override
    public <T> T convert(Response response, Type returnType) {
        logger.debug("Converting response to type: {}", returnType);
        
        // Handle Response type - return as-is
        if (returnType.equals(Response.class)) {
            logger.debug("Return type is Response, returning as-is");
            return (T) response;
        }
        
        // Handle void return type
        if (returnType.equals(Void.TYPE) || returnType.equals(Void.class)) {
            logger.debug("Return type is void, returning null");
            return null;
        }
        
        String responseBody = response.getBody().asString();
        logger.debug("Response body: {}", responseBody);
        
        try {
            // Handle parameterized types (e.g., List<UserDto>)
            if (returnType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) returnType;
                JavaType javaType = objectMapper.getTypeFactory()
                    .constructType(parameterizedType);
                T result = objectMapper.readValue(responseBody, javaType);
                logger.debug("Successfully converted response to parameterized type: {}", returnType);
                return result;
            }
            
            // Handle simple types
            if (returnType instanceof Class) {
                Class<T> clazz = (Class<T>) returnType;
                T result = objectMapper.readValue(responseBody, clazz);
                logger.debug("Successfully converted response to class: {}", clazz.getSimpleName());
                return result;
            }
            
            throw new IllegalArgumentException("Unsupported return type: " + returnType);
            
        } catch (Exception e) {
            logger.error("Failed to convert response to type {}: {}", returnType, e.getMessage(), e);
            throw new RuntimeException("Failed to convert response to " + returnType, e);
        }
    }
}