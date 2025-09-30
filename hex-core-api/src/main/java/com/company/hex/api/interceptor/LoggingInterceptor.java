package com.company.hex.api.interceptor;

import com.company.hex.api.model.RequestDefinition;
import com.company.hex.core.logging.HexLoggerFactory;
import io.restassured.response.Response;
import org.slf4j.Logger;

/**
 * Interceptor that logs request and response details.
 * This is enabled by default to provide visibility into API calls.
 * 
 * @author Hex Framework
 * @version 1.0
 */
public class LoggingInterceptor implements Interceptor {
    
    private static final Logger logger = HexLoggerFactory.getApiLogger(LoggingInterceptor.class);
    
    @Override
    public Response intercept(Chain chain) {
        RequestDefinition request = chain.request();
        
        // Log request details
        logRequest(request);
        
        long startTime = System.currentTimeMillis();
        
        // Proceed with the request
        Response response = chain.proceed(request);
        
        long duration = System.currentTimeMillis() - startTime;
        
        // Log response details
        logResponse(request, response, duration);
        
        return response;
    }
    
    private void logRequest(RequestDefinition request) {
        logger.info("→ {} {}", request.getHttpMethod(), request.getPath());
        
        if (!request.getPathParams().isEmpty()) {
            logger.debug("  Path params: {}", request.getPathParams());
        }
        
        if (!request.getQueryParams().isEmpty()) {
            logger.debug("  Query params: {}", request.getQueryParams());
        }
        
        if (!request.getHeaders().isEmpty()) {
            logger.debug("  Headers: {}", request.getHeaders());
        }
        
        if (request.getBody() != null) {
            logger.debug("  Body: {}", request.getBody().getClass().getSimpleName());
        }
    }
    
    private void logResponse(RequestDefinition request, Response response, long duration) {
        int statusCode = response.getStatusCode();
        String statusText = response.getStatusLine();
        
        if (statusCode >= 200 && statusCode < 300) {
            logger.info("← {} {} - {} ({}ms)", 
                request.getHttpMethod(), 
                request.getPath(), 
                statusText, 
                duration);
        } else if (statusCode >= 400) {
            logger.warn("← {} {} - {} ({}ms)", 
                request.getHttpMethod(), 
                request.getPath(), 
                statusText, 
                duration);
        } else {
            logger.info("← {} {} - {} ({}ms)", 
                request.getHttpMethod(), 
                request.getPath(), 
                statusText, 
                duration);
        }
        
        logger.debug("  Response time: {}ms", duration);
        logger.debug("  Content-Type: {}", response.getContentType());
    }
}