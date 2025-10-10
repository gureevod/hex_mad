package com.company.hex.api.interceptor;

import com.company.hex.api.annotations.config.ExpectedStatus;
import com.company.hex.api.model.RequestDefinition;
import com.company.hex.core.logging.HexLoggerFactory;
import io.restassured.response.Response;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Interceptor that validates response status codes against @ExpectedStatus annotation.
 * Throws an exception if the actual status code doesn't match the expected value(s).
 * 
 * @author Hex Framework
 * @version 1.0
 */
public class StatusValidationInterceptor implements Interceptor {
    
    private static final Logger logger = HexLoggerFactory.getApiLogger(StatusValidationInterceptor.class);
    
    @Override
    public Response intercept(Chain chain) {
        RequestDefinition request = chain.request();
        Method method = request.getMethod();
        
        // Proceed with the request
        Response response = chain.proceed(request);
        
        // Check if method has @ExpectedStatus annotation
        if (method != null && method.isAnnotationPresent(ExpectedStatus.class)) {
            ExpectedStatus expectedStatus = method.getAnnotation(ExpectedStatus.class);
            int[] expectedCodes = expectedStatus.value();
            int actualCode = response.getStatusCode();
            
            boolean matches = Arrays.stream(expectedCodes).anyMatch(code -> code == actualCode);
            
            if (!matches) {
                String expectedCodesStr = Arrays.toString(expectedCodes);
                String errorMessage = String.format(
                    "Status code validation failed for %s %s. Expected: %s, Actual: %d",
                    request.getHttpMethod(),
                    request.getPath(),
                    expectedCodesStr,
                    actualCode
                );
                logger.error(errorMessage);
                throw new AssertionError(errorMessage);
            }
            
            logger.debug("Status code validation passed: {} matches expected {}", 
                actualCode, expectedCodes);
        }
        
        return response;
    }
}