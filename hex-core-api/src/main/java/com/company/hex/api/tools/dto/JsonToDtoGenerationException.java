package com.company.hex.api.tools.dto;

/**
 * Exception thrown when DTO generation fails.
 */
public class JsonToDtoGenerationException extends RuntimeException {
    
    public JsonToDtoGenerationException(String message) {
        super(message);
    }
    
    public JsonToDtoGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}