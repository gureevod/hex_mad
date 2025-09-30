package com.company.hex.api.executor;

import com.company.hex.api.config.ApiConfig;
import com.company.hex.api.model.RequestDefinition;
import com.company.hex.core.config.HexConfigFactory;
import com.company.hex.core.logging.HexLoggerFactory;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;

import java.util.Map;

/**
 * Executes HTTP requests using RestAssured based on RequestDefinition.
 * Handles request building, execution, and basic error handling.
 * 
 * @author Hex Framework
 * @version 1.0
 */
public class RestAssuredExecutor implements RequestExecutor {
    
    private static final Logger logger = HexLoggerFactory.getApiLogger(RestAssuredExecutor.class);
    private final ApiConfig config;
    
    public RestAssuredExecutor() {
        this.config = HexConfigFactory.getConfig(ApiConfig.class);
    }
    
    public RestAssuredExecutor(ApiConfig config) {
        this.config = config;
    }
    
    @Override
    public Response execute(RequestDefinition request) {
        logger.debug("Executing request: {}", request);
        
        RequestSpecification spec = buildRequestSpecification(request);
        
        Response response = executeRequest(spec, request);
        
        logger.info("Request completed: {} {} - Status: {}", 
                   request.getHttpMethod(), 
                   request.getPath(), 
                   response.getStatusCode());
        
        return response;
    }
    
    /**
     * Build RestAssured RequestSpecification from RequestDefinition.
     */
    private RequestSpecification buildRequestSpecification(RequestDefinition request) {
        // Determine base URL
        String baseUrl = request.getBaseUrl() != null && !request.getBaseUrl().isEmpty() 
                        ? request.getBaseUrl() 
                        : config.baseUrl();
        
        RequestSpecification spec = RestAssured.given()
            .baseUri(baseUrl)
            .contentType(config.defaultContentType())
            .accept(config.defaultAccept());
        
        // Add Allure integration
        spec.filter(new AllureRestAssured());
        
        // Add path parameters
        Map<String, Object> pathParams = request.getPathParams();
        if (!pathParams.isEmpty()) {
            spec.pathParams(pathParams);
            logger.debug("Added path params: {}", pathParams);
        }
        
        // Add query parameters
        Map<String, Object> queryParams = request.getQueryParams();
        if (!queryParams.isEmpty()) {
            spec.queryParams(queryParams);
            logger.debug("Added query params: {}", queryParams);
        }
        
        // Add headers
        Map<String, Object> headers = request.getHeaders();
        if (!headers.isEmpty()) {
            headers.forEach((key, value) -> spec.header(key, value));
            logger.debug("Added headers: {}", headers);
        }
        
        // Add form parameters
        Map<String, Object> formParams = request.getFormParams();
        if (!formParams.isEmpty()) {
            formParams.forEach((key, value) -> spec.formParam(key, value));
            logger.debug("Added form params: {}", formParams);
        }
        
        // Add request body
        if (request.getBody() != null) {
            spec.body(request.getBody());
            logger.debug("Added request body: {}", request.getBody().getClass().getSimpleName());
        }
        
        return spec;
    }
    
    /**
     * Execute the HTTP request based on the method type.
     */
    private Response executeRequest(RequestSpecification spec, RequestDefinition request) {
        String method = request.getHttpMethod().toUpperCase();
        String path = request.getPath();
        
        logger.debug("Executing {} request to {}", method, path);
        
        switch (method) {
            case "GET":
                return spec.when().get(path);
            case "POST":
                return spec.when().post(path);
            case "PUT":
                return spec.when().put(path);
            case "DELETE":
                return spec.when().delete(path);
            case "PATCH":
                return spec.when().patch(path);
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }
    }
}