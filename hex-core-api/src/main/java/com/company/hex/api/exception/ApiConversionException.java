package com.company.hex.api.exception;

import java.lang.reflect.Type;

/**
 * Исключение для ошибок десериализации ответов API.
 * Содержит детальную информацию для диагностики проблем маппинга DTO.
 * 
 * @author Hex Framework
 * @version 1.0
 */
public class ApiConversionException extends RuntimeException {
    
    private final Type expectedType;
    private final String responseBody;
    private final int statusCode;
    
    /**
     * Создать исключение с детальной информацией о проблеме конвертации.
     * 
     * @param message описание ошибки
     * @param expectedType ожидаемый тип для десериализации
     * @param responseBody тело ответа, которое не удалось десериализовать
     * @param statusCode HTTP статус код ответа
     */
    public ApiConversionException(String message, Type expectedType, String responseBody, int statusCode) {
        super(formatMessage(message, expectedType, responseBody, statusCode));
        this.expectedType = expectedType;
        this.responseBody = responseBody;
        this.statusCode = statusCode;
    }
    
    /**
     * Создать исключение с детальной информацией и исходной причиной.
     * 
     * @param message описание ошибки
     * @param cause исходное исключение (обычно от Jackson)
     * @param expectedType ожидаемый тип для десериализации
     * @param responseBody тело ответа, которое не удалось десериализовать
     * @param statusCode HTTP статус код ответа
     */
    public ApiConversionException(String message, Throwable cause, Type expectedType, 
                                 String responseBody, int statusCode) {
        super(formatMessage(message, expectedType, responseBody, statusCode), cause);
        this.expectedType = expectedType;
        this.responseBody = responseBody;
        this.statusCode = statusCode;
    }
    
    /**
     * Форматировать детальное сообщение об ошибке с подсказками для разработчика.
     */
    private static String formatMessage(String message, Type expectedType, String responseBody, int statusCode) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ошибка десериализации ответа API: ").append(message);
        sb.append("\n");
        sb.append("Ожидаемый тип: ").append(expectedType.getTypeName());
        sb.append("\n");
        sb.append("HTTP Статус: ").append(statusCode);
        sb.append("\n");
        
        if (responseBody != null && !responseBody.isEmpty()) {
            String truncatedBody = responseBody.length() > 500 
                ? responseBody.substring(0, 500) + "... (усечено)" 
                : responseBody;
            sb.append("Тело ответа: ").append(truncatedBody);
            sb.append("\n");
        } else {
            sb.append("Тело ответа: <пусто>");
            sb.append("\n");
        }
        
        sb.append("\n");
        sb.append("Возможные решения:");
        sb.append("\n");
        sb.append("  1. Проверьте соответствие полей DTO и JSON структуры");
        sb.append("\n");
        sb.append("  2. Убедитесь, что все обязательные поля присутствуют в ответе");
        sb.append("\n");
        sb.append("  3. Проверьте аннотации Jackson (@JsonProperty, @JsonIgnore и т.д.)");
        sb.append("\n");
        sb.append("  4. Убедитесь, что типы данных совпадают (например, String вместо Integer)");
        
        return sb.toString();
    }
    
    public Type getExpectedType() {
        return expectedType;
    }
    
    public String getResponseBody() {
        return responseBody;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
}