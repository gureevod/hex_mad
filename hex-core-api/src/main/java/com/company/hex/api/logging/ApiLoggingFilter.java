package com.company.hex.api.logging;

import com.company.hex.core.logging.CorrelationIdManager;
import com.company.hex.core.logging.HexLoggerFactory;
import io.qameta.allure.Allure;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Кастомный фильтр для RestAssured с интеграцией логирования и Allure отчетов.
 * Автоматически логирует запросы и ответы, прикрепляет их к Allure отчетам.
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public class ApiLoggingFilter implements Filter {
    
    private static final Logger logger = HexLoggerFactory.getApiLogger(ApiLoggingFilter.class);
    private final boolean attachToAllure;
    private final boolean logRequestBody;
    private final boolean logResponseBody;
    
    /**
     * Конструктор с настройками по умолчанию.
     */
    public ApiLoggingFilter() {
        this(true, true, true);
    }
    
    /**
     * Конструктор с кастомными настройками.
     * 
     * @param attachToAllure прикреплять ли запросы/ответы к Allure
     * @param logRequestBody логировать ли тело запроса
     * @param logResponseBody логировать ли тело ответа
     */
    public ApiLoggingFilter(boolean attachToAllure, boolean logRequestBody, boolean logResponseBody) {
        this.attachToAllure = attachToAllure;
        this.logRequestBody = logRequestBody;
        this.logResponseBody = logResponseBody;
    }
    
    @Override
    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
        String correlationId = CorrelationIdManager.getCorrelationId();
        long startTime = System.currentTimeMillis();
        
        // Логируем запрос
        logRequest(requestSpec, correlationId);
        
        // Выполняем запрос
        Response response = ctx.next(requestSpec, responseSpec);
        
        // Вычисляем время выполнения
        long executionTime = System.currentTimeMillis() - startTime;
        
        // Логируем ответ
        logResponse(response, executionTime, correlationId);
        
        // Прикрепляем к Allure если включено
        if (attachToAllure) {
            attachToAllure(requestSpec, response, executionTime);
        }
        
        return response;
    }
    
    /**
     * Логирует информацию о запросе.
     * 
     * @param requestSpec спецификация запроса
     * @param correlationId correlation ID
     */
    private void logRequest(FilterableRequestSpecification requestSpec, String correlationId) {
        String method = requestSpec.getMethod();
        String uri = requestSpec.getURI();
        
        logger.info("📤 [{}] {} {} - Отправка запроса", correlationId, method, uri);
        
        // Логируем заголовки
        if (logger.isDebugEnabled()) {
            logger.debug("📋 [{}] Заголовки запроса: {}", correlationId, requestSpec.getHeaders());
            
            // Логируем параметры запроса
            if (!requestSpec.getQueryParams().isEmpty()) {
                logger.debug("📋 [{}] Параметры запроса: {}", correlationId, requestSpec.getQueryParams());
            }
            
            // Логируем параметры пути
            if (!requestSpec.getPathParams().isEmpty()) {
                logger.debug("📋 [{}] Параметры пути: {}", correlationId, requestSpec.getPathParams());
            }
        }
        
        // Логируем тело запроса
        if (logRequestBody && requestSpec.getBody() != null && logger.isTraceEnabled()) {
            logger.trace("📋 [{}] Тело запроса: {}", correlationId, requestSpec.getBody());
        }
    }
    
    /**
     * Логирует информацию об ответе.
     * 
     * @param response ответ сервера
     * @param executionTime время выполнения запроса
     * @param correlationId correlation ID
     */
    private void logResponse(Response response, long executionTime, String correlationId) {
        int statusCode = response.getStatusCode();
        String statusLine = response.getStatusLine();
        String contentType = response.getContentType();
        
        // Определяем уровень логирования на основе статус кода
        if (statusCode >= 200 && statusCode < 300) {
            logger.info("📥 [{}] ✅ {} ({} мс) [{}]", correlationId, statusLine, executionTime, contentType);
        } else if (statusCode >= 400 && statusCode < 500) {
            logger.warn("📥 [{}] ⚠️ {} ({} мс) [{}]", correlationId, statusLine, executionTime, contentType);
        } else if (statusCode >= 500) {
            logger.error("📥 [{}] ❌ {} ({} мс) [{}]", correlationId, statusLine, executionTime, contentType);
        } else {
            logger.info("📥 [{}] ℹ️ {} ({} мс) [{}]", correlationId, statusLine, executionTime, contentType);
        }
        
        // Логируем заголовки ответа
        if (logger.isDebugEnabled()) {
            logger.debug("📋 [{}] Заголовки ответа: {}", correlationId, response.getHeaders());
        }
        
        // Логируем тело ответа
        if (logResponseBody && logger.isTraceEnabled()) {
            String responseBody = response.getBody().asString();
            if (responseBody != null && !responseBody.trim().isEmpty()) {
                logger.trace("📋 [{}] Тело ответа: {}", correlationId, responseBody);
            }
        }
    }
    
    /**
     * Прикрепляет информацию о запросе и ответе к Allure отчету.
     * 
     * @param requestSpec спецификация запроса
     * @param response ответ сервера
     * @param executionTime время выполнения
     */
    private void attachToAllure(FilterableRequestSpecification requestSpec, Response response, long executionTime) {
        try {
            // Формируем информацию о запросе
            StringBuilder requestInfo = new StringBuilder();
            requestInfo.append("Method: ").append(requestSpec.getMethod()).append("\n");
            requestInfo.append("URI: ").append(requestSpec.getURI()).append("\n");
            requestInfo.append("Headers: ").append(formatHeaders(requestSpec.getHeaders())).append("\n");
            
            if (!requestSpec.getQueryParams().isEmpty()) {
                requestInfo.append("Query Params: ").append(requestSpec.getQueryParams()).append("\n");
            }
            
            if (!requestSpec.getPathParams().isEmpty()) {
                requestInfo.append("Path Params: ").append(requestSpec.getPathParams()).append("\n");
            }
            
            if (requestSpec.getBody() != null) {
                requestInfo.append("Body: ").append(String.valueOf(requestSpec.getBody())).append("\n");
            }
            
            // Формируем информацию об ответе
            StringBuilder responseInfo = new StringBuilder();
            responseInfo.append("Status: ").append(response.getStatusLine()).append("\n");
            responseInfo.append("Time: ").append(executionTime).append(" ms\n");
            responseInfo.append("Headers: ").append(formatHeaders(response.getHeaders())).append("\n");
            
            String responseBody = response.getBody().asString();
            if (responseBody != null && !responseBody.trim().isEmpty()) {
                responseInfo.append("Body: ").append(responseBody);
            }
            
            // Прикрепляем к Allure
            Allure.addAttachment("HTTP Request", "text/plain", requestInfo.toString());
            Allure.addAttachment("HTTP Response", "text/plain", responseInfo.toString());
            
            // Прикрепляем JSON ответ отдельно если это JSON
            String contentType = response.getContentType();
            if (contentType != null && contentType.contains("application/json") && responseBody != null && !responseBody.trim().isEmpty()) {
                Allure.addAttachment("Response JSON", "application/json", responseBody);
            }
            
        } catch (Exception e) {
            logger.warn("⚠️ Не удалось прикрепить данные к Allure: {}", e.getMessage());
        }
    }
    
    /**
     * Форматирует заголовки для читаемого вывода.
     * 
     * @param headers заголовки
     * @return отформатированная строка заголовков
     */
    private String formatHeaders(io.restassured.http.Headers headers) {
        if (headers == null || headers.size() == 0) {
            return "None";
        }
        
        StringBuilder formatted = new StringBuilder();
        headers.forEach(header -> {
            String name = header.getName();
            String value = header.getValue();
            
            // Маскируем чувствительные заголовки
            if (isSensitiveHeader(name)) {
                value = "***REDACTED***";
            }
            
            formatted.append(name).append(": ").append(value).append("; ");
        });
        
        return formatted.toString();
    }
    
    /**
     * Проверяет, является ли заголовок чувствительным.
     * 
     * @param headerName имя заголовка
     * @return true если заголовок чувствительный
     */
    private boolean isSensitiveHeader(String headerName) {
        if (headerName == null) {
            return false;
        }
        
        String lowerName = headerName.toLowerCase();
        return lowerName.contains("authorization") ||
               lowerName.contains("cookie") ||
               lowerName.contains("token") ||
               lowerName.contains("key") ||
               lowerName.contains("secret") ||
               lowerName.contains("password");
    }
    
    /**
     * Создает экземпляр фильтра с настройками для продакшена.
     * Отключает логирование тел запросов/ответов для безопасности.
     * 
     * @return настроенный фильтр
     */
    public static ApiLoggingFilter forProduction() {
        return new ApiLoggingFilter(true, false, false);
    }
    
    /**
     * Создает экземпляр фильтра с настройками для разработки.
     * Включает полное логирование для отладки.
     * 
     * @return настроенный фильтр
     */
    public static ApiLoggingFilter forDevelopment() {
        return new ApiLoggingFilter(true, true, true);
    }
    
    /**
     * Создает экземпляр фильтра только для Allure без логирования.
     * 
     * @return настроенный фильтр
     */
    public static ApiLoggingFilter allureOnly() {
        return new ApiLoggingFilter(true, false, false);
    }
}