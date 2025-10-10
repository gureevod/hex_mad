package com.company.hex.api.executor;

import com.company.hex.api.model.RequestDefinition;
import io.restassured.response.Response;

/**
 * Interface for executing HTTP requests based on RequestDefinition.
 * Implementations can use different HTTP clients (RestAssured, OkHttp, etc.).
 * 
 * @author Hex Framework
 * @version 1.0
 */
public interface RequestExecutor {
    
    /**
     * Execute an HTTP request based on the provided RequestDefinition.
     * 
     * @param request the request definition containing all request metadata
     * @return the HTTP response
     */
    Response execute(RequestDefinition request);
}