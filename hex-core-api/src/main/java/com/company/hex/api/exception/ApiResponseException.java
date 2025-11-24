package com.company.hex.api.exception;

import java.util.Collections;
import java.util.Map;

/**
 * Исключение для HTTP-уровня ошибок при выполнении API запросов.
 * Содержит детальную диагностическую информацию о запросе и ответе.
 * 
 * @author Hex Framework
 * @version 1.0
 */
public class ApiResponseException extends RuntimeException {
    
    private final int statusCode;
    private final String responseBody;
    private final String requestMethod;
    private final String requestPath;
    private final Map<String, Object> requestHeaders;
    
    /**
     * Создать исключение с подробной информацией об ошибке HTTP ответа.
     * 
     * @param message описание ошибки
     * @param statusCode HTTP статус код ответа
     * @param responseBody тело ответа
     * @param requestMethod HTTP метод запроса (GET, POST, и т.д.)
     * @param requestPath путь запроса
     * @param requestHeaders заголовки запроса
     */
    public ApiResponseException(String message, int statusCode, String responseBody,
                               String requestMethod, String requestPath, Map<String, Object> requestHeaders) {
        super(formatMessage(message, statusCode, responseBody, requestMethod, requestPath));
        this.statusCode = statusCode;
        this.responseBody = responseBody;
        this.requestMethod = requestMethod;
        this.requestPath = requestPath;
        this.requestHeaders = requestHeaders != null ? Map.copyOf(requestHeaders) : Collections.emptyMap();
    }
    
    /**
     * Создать исключение с подробной информацией и исходной причиной.
     * 
     * @param message описание ошибки
     * @param cause исходное исключение
     * @param statusCode HTTP статус код ответа
     * @param responseBody тело ответа
     * @param requestMethod HTTP метод запроса (GET, POST, и т.д.)
     * @param requestPath путь запроса
     * @param requestHeaders заголовки запроса
     */
    public ApiResponseException(String message, Throwable cause, int statusCode, String responseBody,
                               String requestMethod, String requestPath, Map<String, Object> requestHeaders) {
        super(formatMessage(message, statusCode, responseBody, requestMethod, requestPath), cause);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
        this.requestMethod = requestMethod;
        this.requestPath = requestPath;
        this.requestHeaders = requestHeaders != null ? Map.copyOf(requestHeaders) : Collections.emptyMap();
    }
    
    /**
     * Форматировать детальное сообщение об ошибке.
     */
    private static String formatMessage(String message, int statusCode, String responseBody,
                                       String requestMethod, String requestPath) {
        StringBuilder sb = new StringBuilder();
        sb.append(message);
        sb.append("\n");
        sb.append("HTTP Запрос: ").append(requestMethod).append(" ").append(requestPath);
        sb.append("\n");
        sb.append("HTTP Статус: ").append(statusCode);
        sb.append("\n");
        
        if (responseBody != null && !responseBody.isEmpty()) {
            String truncatedBody = responseBody.length() > 500 
                ? responseBody.substring(0, 500) + "... (усечено)" 
                : responseBody;
            sb.append("Тело ответа: ").append(truncatedBody);
        } else {
            sb.append("Тело ответа: <пусто>");
        }
        
        return sb.toString();
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public String getResponseBody() {
        return responseBody;
    }
    
    public String getRequestMethod() {
        return requestMethod;
    }
    
    public String getRequestPath() {
        return requestPath;
    }
    
    public Map<String, Object> getRequestHeaders() {
        return requestHeaders;
    }
}