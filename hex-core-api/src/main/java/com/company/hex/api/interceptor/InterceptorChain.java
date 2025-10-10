package com.company.hex.api.interceptor;

import com.company.hex.api.executor.RequestExecutor;
import com.company.hex.api.model.RequestDefinition;
import com.company.hex.core.logging.HexLoggerFactory;
import io.restassured.response.Response;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages and executes a chain of interceptors.
 * Interceptors are executed in order, with each interceptor having the opportunity
 * to modify the request or response.
 * 
 * @author Hex Framework
 * @version 1.0
 */
public class InterceptorChain {
    
    private static final Logger logger = HexLoggerFactory.getApiLogger(InterceptorChain.class);
    
    private final List<Interceptor> interceptors;
    private final RequestExecutor requestExecutor;
    
    public InterceptorChain(List<Interceptor> interceptors, RequestExecutor requestExecutor) {
        this.interceptors = new ArrayList<>(interceptors);
        this.requestExecutor = requestExecutor;
    }
    
    /**
     * Execute the interceptor chain with the given request.
     * 
     * @param request the initial request definition
     * @return the response after all interceptors and execution
     */
    public Response proceed(RequestDefinition request) {
        logger.debug("Starting interceptor chain with {} interceptors", interceptors.size());
        return new RealChain(0, request).proceed(request);
    }
    
    /**
     * Internal chain implementation that tracks position in the interceptor list.
     */
    private class RealChain implements Interceptor.Chain {
        private final int index;
        private final RequestDefinition request;
        
        RealChain(int index, RequestDefinition request) {
            this.index = index;
            this.request = request;
        }
        
        @Override
        public RequestDefinition request() {
            return request;
        }
        
        @Override
        public Response proceed(RequestDefinition request) {
            if (index >= interceptors.size()) {
                // End of chain - execute the actual request
                logger.debug("End of interceptor chain, executing request");
                return requestExecutor.execute(request);
            }
            
            // Get next interceptor and proceed
            Interceptor interceptor = interceptors.get(index);
            logger.debug("Executing interceptor {}: {}", index, interceptor.getClass().getSimpleName());
            
            RealChain nextChain = new RealChain(index + 1, request);
            return interceptor.intercept(nextChain);
        }
    }
    
    /**
     * Builder for creating InterceptorChain instances.
     */
    public static class Builder {
        private final List<Interceptor> interceptors = new ArrayList<>();
        private RequestExecutor requestExecutor;
        
        public Builder addInterceptor(Interceptor interceptor) {
            this.interceptors.add(interceptor);
            return this;
        }
        
        public Builder addInterceptors(List<Interceptor> interceptors) {
            this.interceptors.addAll(interceptors);
            return this;
        }
        
        public Builder requestExecutor(RequestExecutor requestExecutor) {
            this.requestExecutor = requestExecutor;
            return this;
        }
        
        public InterceptorChain build() {
            if (requestExecutor == null) {
                throw new IllegalStateException("RequestExecutor is required");
            }
            return new InterceptorChain(interceptors, requestExecutor);
        }
    }
}