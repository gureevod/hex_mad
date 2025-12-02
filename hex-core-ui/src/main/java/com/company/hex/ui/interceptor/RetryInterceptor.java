package com.company.hex.ui.interceptor;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.codeborne.selenide.Selenide.sleep;

public class RetryInterceptor implements ElementInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RetryInterceptor.class);

    private final int maxRetries;
    private final Set<String> retryableActions;

    public RetryInterceptor(int maxRetries) {
        this.maxRetries = maxRetries;
        this.retryableActions = Set.of("click", "fill", "select");
    }

    @Override
    public <R> R intercept(ActionContext context, ActionChain<R> chain) {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return chain.proceed();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxRetries) {
                    logger.warn("Попытка {} не удалась для {}, повтор...",
                            attempt, context.getActionName());
                    sleep(500 * attempt); // Exponential backoff
                }
            }
        }

        throw new RuntimeException("Не удалось выполнить " + context.getActionName() +
                " после " + maxRetries + " попыток", lastException);
    }

    @Override
    public boolean supports(String actionName) {
        return retryableActions.contains(actionName);
    }

    @Override
    public int getOrder() {
        return 50; // Между логированием и действием
    }
}
