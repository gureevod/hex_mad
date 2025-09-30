package com.company.hex.api.processor;

import com.company.hex.api.annotations.config.ApiService;
import com.company.hex.api.annotations.http.GET;
import com.company.hex.api.annotations.http.POST;
import com.company.hex.api.annotations.param.Body;
import com.company.hex.api.annotations.param.Path;
import com.company.hex.api.annotations.param.Query;
import com.company.hex.api.model.RequestDefinition;
import com.company.hex.core.logging.HexLoggerFactory;
import org.slf4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Processes method annotations to extract HTTP request metadata.
 * Converts annotated interface methods into RequestDefinition objects.
 * 
 * @author Hex Framework
 * @version 1.0
 */
public class AnnotationProcessor {
    
    private static final Logger logger = HexLoggerFactory.getApiLogger(AnnotationProcessor.class);
    
    /**
     * Process a method and its arguments to create a RequestDefinition.
     * 
     * @param method the interface method being invoked
     * @param args the arguments passed to the method
     * @param serviceClass the service interface class
     * @return a RequestDefinition containing all request metadata
     */
    public RequestDefinition process(Method method, Object[] args, Class<?> serviceClass) {
        logger.debug("Processing method: {}.{}", serviceClass.getSimpleName(), method.getName());
        
        RequestDefinition.Builder builder = RequestDefinition.builder();
        
        // Extract service-level configuration
        processServiceAnnotation(serviceClass, builder);
        
        // Extract HTTP method and path
        processHttpMethodAnnotation(method, builder);
        
        // Extract parameter annotations
        processParameters(method, args, builder);
        
        RequestDefinition definition = builder.build();
        logger.debug("Created RequestDefinition: {}", definition);
        
        return definition;
    }
    
    /**
     * Process the @ApiService annotation on the interface.
     */
    private void processServiceAnnotation(Class<?> serviceClass, RequestDefinition.Builder builder) {
        ApiService apiService = serviceClass.getAnnotation(ApiService.class);
        if (apiService != null) {
            String baseUrl = apiService.baseUrl();
            if (!baseUrl.isEmpty()) {
                // Resolve property placeholders if needed
                baseUrl = resolvePropertyPlaceholder(baseUrl);
                builder.baseUrl(baseUrl);
                logger.debug("Set base URL from @ApiService: {}", baseUrl);
            }
            
            String basePath = apiService.basePath();
            if (!basePath.isEmpty()) {
                logger.debug("Base path from @ApiService: {}", basePath);
                // Base path will be prepended to method path
            }
        }
    }
    
    /**
     * Process HTTP method annotations (@GET, @POST, etc.).
     */
    private void processHttpMethodAnnotation(Method method, RequestDefinition.Builder builder) {
        if (method.isAnnotationPresent(GET.class)) {
            GET get = method.getAnnotation(GET.class);
            builder.httpMethod("GET");
            builder.path(get.value());
            logger.debug("HTTP Method: GET, Path: {}", get.value());
        } else if (method.isAnnotationPresent(POST.class)) {
            POST post = method.getAnnotation(POST.class);
            builder.httpMethod("POST");
            builder.path(post.value());
            logger.debug("HTTP Method: POST, Path: {}", post.value());
        } else {
            throw new IllegalStateException("Method " + method.getName() + " must have an HTTP method annotation (@GET, @POST, etc.)");
        }
    }
    
    /**
     * Process parameter annotations (@Path, @Query, @Body).
     */
    private void processParameters(Method method, Object[] args, RequestDefinition.Builder builder) {
        Parameter[] parameters = method.getParameters();
        
        if (args == null || args.length == 0) {
            return;
        }
        
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Object arg = args[i];
            
            if (parameter.isAnnotationPresent(Path.class)) {
                Path path = parameter.getAnnotation(Path.class);
                builder.addPathParam(path.value(), arg);
                logger.debug("Path param: {} = {}", path.value(), arg);
            } else if (parameter.isAnnotationPresent(Query.class)) {
                Query query = parameter.getAnnotation(Query.class);
                builder.addQueryParam(query.value(), arg);
                logger.debug("Query param: {} = {}", query.value(), arg);
            } else if (parameter.isAnnotationPresent(Body.class)) {
                builder.body(arg);
                logger.debug("Request body set: {}", arg != null ? arg.getClass().getSimpleName() : "null");
            }
        }
    }
    
    /**
     * Resolve property placeholders like ${property.name}.
     * For MVP, this is a simple implementation. Can be enhanced later.
     */
    private String resolvePropertyPlaceholder(String value) {
        if (value.startsWith("${") && value.endsWith("}")) {
            String propertyName = value.substring(2, value.length() - 1);
            String resolved = System.getProperty(propertyName);
            if (resolved != null) {
                logger.debug("Resolved property {} to {}", propertyName, resolved);
                return resolved;
            }
            logger.warn("Property {} not found, using original value", propertyName);
        }
        return value;
    }
}