package com.company.hex.ui.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LoggingInterceptor implements ElementInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public <R> R intercept(ActionContext context, ActionChain<R> chain) {
        logger.info("▶ {} на элементе '{}'",
                context.getActionName(),
                context.getElement().getName());

        try {
            R result = chain.proceed();

            logger.info("✓ {} завершён за {}ms",
                    context.getActionName(),
                    context.getElapsedMillis());

            return result;
        } catch (Exception e) {
            logger.error("✗ {} провалился: {}",
                    context.getActionName(),
                    e.getMessage());
            throw e;
        }
    }

    @Override
    public int getOrder() {
        return 10; // Выполняется первым
    }
}