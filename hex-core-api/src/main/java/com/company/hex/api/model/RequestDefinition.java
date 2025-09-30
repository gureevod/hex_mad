package com.company.hex.api.model;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a complete HTTP request definition extracted from annotated interface methods.
 * This immutable class holds all the metadata needed to execute an HTTP request.
 * 
 * @author Hex Framework
 * @version 1.0
 */
public final class RequestDefinition {
    
    private final String httpMethod;
    private final String path;
    private final Map<String, Object> pathParams;
    private final Map<String, Object> queryParams;
    private final Map<String, Object> headers;
    private final Map<String, Object> formParams;
    private final Object body;
    private final String baseUrl;
    private final Method method;
    
    private RequestDefinition(Builder builder) {
        this.httpMethod = builder.httpMethod;
        this.path = builder.path;
        this.pathParams = new HashMap<>(builder.pathParams);
        this.queryParams = new HashMap<>(builder.queryParams);
        this.headers = new HashMap<>(builder.headers);
        this.formParams = new HashMap<>(builder.formParams);
        this.body = builder.body;
        this.baseUrl = builder.baseUrl;
        this.method = builder.method;
    }
    
    public String getHttpMethod() {
        return httpMethod;
    }
    
    public String getPath() {
        return path;
    }
    
    public Map<String, Object> getPathParams() {
        return new HashMap<>(pathParams);
    }
    
    public Map<String, Object> getQueryParams() {
        return new HashMap<>(queryParams);
    }
    
    public Map<String, Object> getHeaders() {
        return new HashMap<>(headers);
    }
    
    public Map<String, Object> getFormParams() {
        return new HashMap<>(formParams);
    }
    
    public Object getBody() {
        return body;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public Method getMethod() {
        return method;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String httpMethod;
        private String path;
        private Map<String, Object> pathParams = new HashMap<>();
        private Map<String, Object> queryParams = new HashMap<>();
        private Map<String, Object> headers = new HashMap<>();
        private Map<String, Object> formParams = new HashMap<>();
        private Object body;
        private String baseUrl;
        private Method method;
        
        public Builder httpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }
        
        public Builder path(String path) {
            this.path = path;
            return this;
        }
        
        public Builder addPathParam(String name, Object value) {
            this.pathParams.put(name, value);
            return this;
        }
        
        public Builder addQueryParam(String name, Object value) {
            if (value != null) {
                this.queryParams.put(name, value);
            }
            return this;
        }
        
        public Builder addHeader(String name, Object value) {
            if (value != null) {
                this.headers.put(name, value);
            }
            return this;
        }
        
        public Builder addFormParam(String name, Object value) {
            if (value != null) {
                this.formParams.put(name, value);
            }
            return this;
        }
        
        public Builder body(Object body) {
            this.body = body;
            return this;
        }
        
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }
        
        public Builder method(Method method) {
            this.method = method;
            return this;
        }
        
        public RequestDefinition build() {
            if (httpMethod == null || httpMethod.isEmpty()) {
                throw new IllegalStateException("HTTP method is required");
            }
            if (path == null) {
                throw new IllegalStateException("Path is required");
            }
            return new RequestDefinition(this);
        }
    }
    
    @Override
    public String toString() {
        return "RequestDefinition{" +
                "httpMethod='" + httpMethod + '\'' +
                ", path='" + path + '\'' +
                ", pathParams=" + pathParams +
                ", queryParams=" + queryParams +
                ", headers=" + headers +
                ", formParams=" + formParams +
                ", hasBody=" + (body != null) +
                ", baseUrl='" + baseUrl + '\'' +
                '}';
    }
}