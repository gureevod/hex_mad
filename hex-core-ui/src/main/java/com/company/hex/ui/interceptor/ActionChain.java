package com.company.hex.ui.interceptor;

import java.util.List;
import java.util.function.Supplier;

/**
 * Цепочка выполнения действия через interceptors.
 * Реализует паттерн Chain of Responsibility.
 */
public final class ActionChain<R> {

    private final List<ElementInterceptor> interceptors;
    private final int currentIndex;
    private final ActionContext context;
    private final Supplier<R> finalAction;

    private ActionChain(List<ElementInterceptor> interceptors,
                        int currentIndex,
                        ActionContext context,
                        Supplier<R> finalAction) {
        this.interceptors = interceptors;
        this.currentIndex = currentIndex;
        this.context = context;
        this.finalAction = finalAction;
    }

    /**
     * Создает начальную цепочку.
     */
    public static <R> ActionChain<R> create(List<ElementInterceptor> interceptors,
                                            ActionContext context,
                                            Supplier<R> action) {
        return new ActionChain<>(interceptors, 0, context, action);
    }

    /**
     * Продолжить выполнение цепочки.
     * Вызывает следующий interceptor или финальное действие.
     *
     * @return результат действия
     */
    public R proceed() {
        // Ищем следующий подходящий interceptor
        int nextIndex = findNextSupportingInterceptor(currentIndex);

        if (nextIndex < interceptors.size()) {
            // Есть ещё interceptors - вызываем следующий
            ElementInterceptor next = interceptors.get(nextIndex);
            ActionChain<R> nextChain = new ActionChain<>(
                    interceptors, nextIndex + 1, context, finalAction
            );
            return next.intercept(context, nextChain);
        } else {
            // Все interceptors отработали - выполняем действие
            return finalAction.get();
        }
    }

    /**
     * Продолжить выполнение с модифицированным контекстом.
     */
    public R proceed(ActionContext modifiedContext) {
        int nextIndex = findNextSupportingInterceptor(currentIndex);

        if (nextIndex < interceptors.size()) {
            ElementInterceptor next = interceptors.get(nextIndex);
            ActionChain<R> nextChain = new ActionChain<>(
                    interceptors, nextIndex + 1, modifiedContext, finalAction
            );
            return next.intercept(modifiedContext, nextChain);
        } else {
            return finalAction.get();
        }
    }

    private int findNextSupportingInterceptor(int startIndex) {
        for (int i = startIndex; i < interceptors.size(); i++) {
            if (interceptors.get(i).supports(context.getActionName())) {
                return i;
            }
        }
        return interceptors.size();
    }

    /**
     * Получить текущий контекст.
     */
    public ActionContext getContext() {
        return context;
    }
}