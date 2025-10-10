package com.company.hex.api.interceptor;

import io.restassured.response.Response;

/**
 * Interface for request/response interceptors.
 * Interceptors can modify requests before execution and responses after execution.
 * They follow the chain of responsibility pattern.
 * 
 * <p>Example usage:
 * <pre>
 * {@code
 * public class AuthInterceptor implements Interceptor {
 *     @Override
 *     public Response intercept(Chain chain) {
 *         RequestDefinition request = chain.request();
 *         // Add authentication header
 *         request.getHeaders().put("Authorization", "Bearer " + getToken());
 *         return chain.proceed(request);
 *     }
 * }
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 */
public interface Interceptor {
    
    /**
     * Intercept the request/response flow.
     * Implementations can modify the request before proceeding or the response after.
     * 
     * @param chain the interceptor chain
     * @return the response after processing
     */
    Response intercept(Chain chain);
    
    /**
     * Chain interface for passing control to the next interceptor or executor.
     */
    interface Chain {
        /**
         * Get the current request definition.
         * 
         * @return the request definition
         */
        com.company.hex.api.model.RequestDefinition request();
        
        /**
         * Proceed with the request, passing control to the next interceptor or executor.
         * 
         * @param request the potentially modified request
         * @return the response
         */
        Response proceed(com.company.hex.api.model.RequestDefinition request);
    }
}