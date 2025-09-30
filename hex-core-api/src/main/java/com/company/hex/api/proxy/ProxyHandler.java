package com.company.hex.api.proxy;

import com.company.hex.api.converter.ResponseConverter;
import com.company.hex.api.executor.RequestExecutor;
import com.company.hex.api.model.RequestDefinition;
import com.company.hex.api.processor.AnnotationProcessor;
import com.company.hex.core.logging.HexLoggerFactory;
import io.restassured.response.Response;
import org.slf4j.Logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Dynamic proxy handler that intercepts method calls on API service interfaces.
 * Orchestrates the flow: annotation processing → request execution → response conversion.
 * 
 * @author Hex Framework
 * @version 1.0
 */
public class ProxyHandler implements InvocationHandler {
    
    private static final Logger logger = HexLoggerFactory.getApiLogger(ProxyHandler.class);
    
    private final Class<?> serviceInterface;
    private final AnnotationProcessor annotationProcessor;
    private final RequestExecutor requestExecutor;
    private final ResponseConverter responseConverter;
    
    public ProxyHandler(Class<?> serviceInterface,
                       AnnotationProcessor annotationProcessor,
                       RequestExecutor requestExecutor,
                       ResponseConverter responseConverter) {
        this.serviceInterface = serviceInterface;
        this.annotationProcessor = annotationProcessor;
        this.requestExecutor = requestExecutor;
        this.responseConverter = responseConverter;
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Handle Object methods (toString, equals, hashCode)
        if (method.getDeclaringClass() == Object.class) {
            return handleObjectMethod(proxy, method, args);
        }
        
        logger.debug("Intercepted method call: {}.{}", serviceInterface.getSimpleName(), method.getName());
        
        try {
            // Step 1: Process annotations to create RequestDefinition
            RequestDefinition requestDefinition = annotationProcessor.process(method, args, serviceInterface);
            
            // Step 2: Execute the HTTP request
            Response response = requestExecutor.execute(requestDefinition);
            
            // Step 3: Convert response to method return type
            Object result = responseConverter.convert(response, method.getGenericReturnType());
            
            logger.debug("Method invocation completed successfully: {}.{}", 
                        serviceInterface.getSimpleName(), method.getName());
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error invoking method {}.{}: {}", 
                        serviceInterface.getSimpleName(), method.getName(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Handle Object methods (toString, equals, hashCode).
     */
    private Object handleObjectMethod(Object proxy, Method method, Object[] args) {
        String methodName = method.getName();
        
        switch (methodName) {
            case "toString":
                return "ApiServiceProxy[" + serviceInterface.getSimpleName() + "]";
            case "equals":
                return proxy == args[0];
            case "hashCode":
                return System.identityHashCode(proxy);
            default:
                throw new UnsupportedOperationException("Method " + methodName + " is not supported");
        }
    }
}