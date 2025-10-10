package com.company.hex.api.interceptor;

import com.company.hex.api.annotations.config.Retry;
import com.company.hex.api.model.RequestDefinition;
import com.company.hex.core.logging.HexLoggerFactory;
import io.restassured.response.Response;
import org.slf4j.Logger;

import java.lang.reflect.Method;

/**
 * Interceptor that implements automatic retry logic based on @Retry annotation.
 * Retries failed requests with configurable count and delay.
 * 
 * @author Hex Framework
 * @version 1.0
 */
public class RetryInterceptor implements Interceptor {
    
    private static final Logger logger = HexLoggerFactory.getApiLogger(RetryInterceptor.class);
    
    @Override
    public Response intercept(Chain chain) {
        RequestDefinition request = chain.request();
        Method method = request.getMethod();
        
        // Check if method has @Retry annotation
        if (method != null && method.isAnnotationPresent(Retry.class)) {
            Retry retry = method.getAnnotation(Retry.class);
            return executeWithRetry(chain, request, retry.count(), retry.delay());
        }
        
        // No retry annotation, proceed normally
        return chain.proceed(request);
    }
    
    private Response executeWithRetry(Chain chain, RequestDefinition request, int maxAttempts, long delay) {
        int attempt = 1;
        Exception lastException = null;
        
        while (attempt <= maxAttempts) {
            try {
                logger.debug("Attempt {}/{} for {} {}", 
                    attempt, maxAttempts, request.getHttpMethod(), request.getPath());
                
                Response response = chain.proceed(request);
                
                // Consider 5xx errors as failures that should be retried
                int statusCode = response.getStatusCode();
                if (statusCode >= 500 && statusCode < 600 && attempt < maxAttempts) {
                    logger.warn("Received {} status code, retrying... (attempt {}/{})", 
                        statusCode, attempt, maxAttempts);
                    attempt++;
                    sleep(delay);
                    continue;
                }
                
                // Success or non-retryable error
                if (attempt > 1) {
                    logger.info("Request succeeded on attempt {}/{}", attempt, maxAttempts);
                }
                return response;
                
            } catch (Exception e) {
                lastException = e;
                logger.warn("Request failed on attempt {}/{}: {}", 
                    attempt, maxAttempts, e.getMessage());
                
                if (attempt >= maxAttempts) {
                    logger.error("All {} retry attempts exhausted for {} {}", 
                        maxAttempts, request.getHttpMethod(), request.getPath());
                    throw new RuntimeException("Request failed after " + maxAttempts + " attempts", e);
                }
                
                attempt++;
                sleep(delay);
            }
        }
        
        // Should not reach here, but just in case
        throw new RuntimeException("Request failed after " + maxAttempts + " attempts", lastException);
    }
    
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Retry delay interrupted", e);
        }
    }
}