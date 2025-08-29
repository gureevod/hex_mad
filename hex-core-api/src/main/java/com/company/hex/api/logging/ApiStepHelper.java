package com.company.hex.api.logging;

import com.company.hex.core.logging.HexLoggerFactory;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Утилитарный класс для создания API шагов с автоматическим логированием и Allure интеграцией.
 * Предоставляет методы для выполнения HTTP запросов с детальным логированием.
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public class ApiStepHelper {
    
    private static final Logger logger = HexLoggerFactory.getApiLogger(ApiStepHelper.class);
    
    /**
     * Приватный конструктор для предотвращения создания экземпляров.
     */
    private ApiStepHelper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Выполняет GET запрос с автоматическим логированием и Allure шагом.
     * 
     * @param endpoint конечная точка API
     * @param requestSpec спецификация запроса
     * @return ответ сервера
     */
    @Step("🌐 GET запрос к {endpoint}")
    public static Response get(String endpoint, RequestSpecification requestSpec) {
        logger.info("📤 Выполняется GET запрос к: {}", endpoint);
        
        try {
            Response response = requestSpec.get(endpoint);
            logResponse("GET", endpoint, response);
            return response;
        } catch (Exception e) {
            logger.error("❌ GET запрос к {} завершился ошибкой: {}", endpoint, e.getMessage(), e);
            Allure.addAttachment("Error Details", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Выполняет POST запрос с автоматическим логированием и Allure шагом.
     * 
     * @param endpoint конечная точка API
     * @param requestSpec спецификация запроса
     * @return ответ сервера
     */
    @Step("🌐 POST запрос к {endpoint}")
    public static Response post(String endpoint, RequestSpecification requestSpec) {
        logger.info("📤 Выполняется POST запрос к: {}", endpoint);
        
        try {
            Response response = requestSpec.post(endpoint);
            logResponse("POST", endpoint, response);
            return response;
        } catch (Exception e) {
            logger.error("❌ POST запрос к {} завершился ошибкой: {}", endpoint, e.getMessage(), e);
            Allure.addAttachment("Error Details", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Выполняет PUT запрос с автоматическим логированием и Allure шагом.
     * 
     * @param endpoint конечная точка API
     * @param requestSpec спецификация запроса
     * @return ответ сервера
     */
    @Step("🌐 PUT запрос к {endpoint}")
    public static Response put(String endpoint, RequestSpecification requestSpec) {
        logger.info("📤 Выполняется PUT запрос к: {}", endpoint);
        
        try {
            Response response = requestSpec.put(endpoint);
            logResponse("PUT", endpoint, response);
            return response;
        } catch (Exception e) {
            logger.error("❌ PUT запрос к {} завершился ошибкой: {}", endpoint, e.getMessage(), e);
            Allure.addAttachment("Error Details", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Выполняет PATCH запрос с автоматическим логированием и Allure шагом.
     * 
     * @param endpoint конечная точка API
     * @param requestSpec спецификация запроса
     * @return ответ сервера
     */
    @Step("🌐 PATCH запрос к {endpoint}")
    public static Response patch(String endpoint, RequestSpecification requestSpec) {
        logger.info("📤 Выполняется PATCH запрос к: {}", endpoint);
        
        try {
            Response response = requestSpec.patch(endpoint);
            logResponse("PATCH", endpoint, response);
            return response;
        } catch (Exception e) {
            logger.error("❌ PATCH запрос к {} завершился ошибкой: {}", endpoint, e.getMessage(), e);
            Allure.addAttachment("Error Details", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Выполняет DELETE запрос с автоматическим логированием и Allure шагом.
     * 
     * @param endpoint конечная точка API
     * @param requestSpec спецификация запроса
     * @return ответ сервера
     */
    @Step("🌐 DELETE запрос к {endpoint}")
    public static Response delete(String endpoint, RequestSpecification requestSpec) {
        logger.info("📤 Выполняется DELETE запрос к: {}", endpoint);
        
        try {
            Response response = requestSpec.delete(endpoint);
            logResponse("DELETE", endpoint, response);
            return response;
        } catch (Exception e) {
            logger.error("❌ DELETE запрос к {} завершился ошибкой: {}", endpoint, e.getMessage(), e);
            Allure.addAttachment("Error Details", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Выполняет запрос с телом в формате JSON.
     * 
     * @param method HTTP метод
     * @param endpoint конечная точка API
     * @param requestSpec спецификация запроса
     * @param body тело запроса
     * @return ответ сервера
     */
    @Step("🌐 {method} запрос к {endpoint} с JSON телом")
    public static Response requestWithJsonBody(String method, String endpoint, RequestSpecification requestSpec, Object body) {
        logger.info("📤 Выполняется {} запрос к: {} с JSON телом", method, endpoint);
        logger.debug("📋 Тело запроса: {}", body);
        
        try {
            requestSpec.body(body);
            Response response;
            
            switch (method.toUpperCase()) {
                case "POST":
                    response = requestSpec.post(endpoint);
                    break;
                case "PUT":
                    response = requestSpec.put(endpoint);
                    break;
                case "PATCH":
                    response = requestSpec.patch(endpoint);
                    break;
                default:
                    throw new IllegalArgumentException("Неподдерживаемый HTTP метод: " + method);
            }
            
            logResponse(method, endpoint, response);
            return response;
        } catch (Exception e) {
            logger.error("❌ {} запрос к {} с JSON телом завершился ошибкой: {}", method, endpoint, e.getMessage(), e);
            Allure.addAttachment("Error Details", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Выполняет запрос с параметрами запроса.
     * 
     * @param method HTTP метод
     * @param endpoint конечная точка API
     * @param requestSpec спецификация запроса
     * @param queryParams параметры запроса
     * @return ответ сервера
     */
    @Step("🌐 {method} запрос к {endpoint} с параметрами")
    public static Response requestWithQueryParams(String method, String endpoint, RequestSpecification requestSpec, Map<String, Object> queryParams) {
        logger.info("📤 Выполняется {} запрос к: {} с параметрами: {}", method, endpoint, queryParams);
        
        try {
            requestSpec.queryParams(queryParams);
            Response response;
            
            switch (method.toUpperCase()) {
                case "GET":
                    response = requestSpec.get(endpoint);
                    break;
                case "POST":
                    response = requestSpec.post(endpoint);
                    break;
                case "PUT":
                    response = requestSpec.put(endpoint);
                    break;
                case "DELETE":
                    response = requestSpec.delete(endpoint);
                    break;
                default:
                    throw new IllegalArgumentException("Неподдерживаемый HTTP метод: " + method);
            }
            
            logResponse(method, endpoint, response);
            return response;
        } catch (Exception e) {
            logger.error("❌ {} запрос к {} с параметрами завершился ошибкой: {}", method, endpoint, e.getMessage(), e);
            Allure.addAttachment("Error Details", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Проверяет статус код ответа.
     * 
     * @param response ответ сервера
     * @param expectedStatusCode ожидаемый статус код
     * @return ответ сервера для цепочки вызовов
     */
    @Step("✅ Проверка статус кода: ожидается {expectedStatusCode}")
    public static Response verifyStatusCode(Response response, int expectedStatusCode) {
        int actualStatusCode = response.getStatusCode();
        logger.info("🔍 Проверка статус кода: ожидается {}, получен {}", expectedStatusCode, actualStatusCode);
        
        if (actualStatusCode == expectedStatusCode) {
            logger.info("✅ Статус код корректный: {}", actualStatusCode);
            Allure.addAttachment("Status Code Verification", "Expected: " + expectedStatusCode + ", Actual: " + actualStatusCode + " ✅");
        } else {
            logger.error("❌ Неожиданный статус код: ожидался {}, получен {}", expectedStatusCode, actualStatusCode);
            Allure.addAttachment("Status Code Verification", "Expected: " + expectedStatusCode + ", Actual: " + actualStatusCode + " ❌");
            throw new AssertionError(String.format("Неожиданный статус код: ожидался %d, получен %d", expectedStatusCode, actualStatusCode));
        }
        
        return response;
    }
    
    /**
     * Проверяет время ответа.
     * 
     * @param response ответ сервера
     * @param maxResponseTimeMs максимальное время ответа в миллисекундах
     * @return ответ сервера для цепочки вызовов
     */
    @Step("⏱️ Проверка времени ответа: максимум {maxResponseTimeMs} мс")
    public static Response verifyResponseTime(Response response, long maxResponseTimeMs) {
        long actualResponseTime = response.getTime();
        logger.info("🔍 Проверка времени ответа: максимум {} мс, фактическое {} мс", maxResponseTimeMs, actualResponseTime);
        
        if (actualResponseTime <= maxResponseTimeMs) {
            logger.info("✅ Время ответа в пределах нормы: {} мс", actualResponseTime);
            Allure.addAttachment("Response Time Verification", "Max: " + maxResponseTimeMs + " ms, Actual: " + actualResponseTime + " ms ✅");
        } else {
            logger.warn("⚠️ Время ответа превышает лимит: {} мс (лимит: {} мс)", actualResponseTime, maxResponseTimeMs);
            Allure.addAttachment("Response Time Verification", "Max: " + maxResponseTimeMs + " ms, Actual: " + actualResponseTime + " ms ⚠️");
        }
        
        return response;
    }
    
    /**
     * Извлекает значение из JSON ответа по JSONPath.
     * 
     * @param response ответ сервера
     * @param jsonPath JSONPath выражение
     * @param <T> тип возвращаемого значения
     * @return извлеченное значение
     */
    @Step("📋 Извлечение значения по JSONPath: {jsonPath}")
    public static <T> T extractFromResponse(Response response, String jsonPath) {
        logger.info("📋 Извлечение значения по JSONPath: {}", jsonPath);
        
        try {
            T value = response.jsonPath().get(jsonPath);
            logger.debug("✅ Извлечено значение: {} = {}", jsonPath, value);
            Allure.addAttachment("Extracted Value", jsonPath + " = " + value);
            return value;
        } catch (Exception e) {
            logger.error("❌ Ошибка при извлечении значения по JSONPath {}: {}", jsonPath, e.getMessage(), e);
            Allure.addAttachment("Extraction Error", "JSONPath: " + jsonPath + ", Error: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Логирует информацию об ответе.
     * 
     * @param method HTTP метод
     * @param endpoint конечная точка
     * @param response ответ сервера
     */
    private static void logResponse(String method, String endpoint, Response response) {
        int statusCode = response.getStatusCode();
        long responseTime = response.getTime();
        String contentType = response.getContentType();
        
        if (statusCode >= 200 && statusCode < 300) {
            logger.info("📥 {} {} - ✅ {} ({} мс) [{}]", method, endpoint, statusCode, responseTime, contentType);
        } else if (statusCode >= 400) {
            logger.warn("📥 {} {} - ❌ {} ({} мс) [{}]", method, endpoint, statusCode, responseTime, contentType);
        } else {
            logger.info("📥 {} {} - ℹ️ {} ({} мс) [{}]", method, endpoint, statusCode, responseTime, contentType);
        }
        
        logger.debug("📋 Заголовки ответа: {}", response.getHeaders());
        
        // Логируем тело ответа только для отладки
        if (logger.isTraceEnabled()) {
            logger.trace("📋 Тело ответа: {}", response.getBody().asString());
        }
    }
}