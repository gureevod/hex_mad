package com.company.hex.ui.interceptor;

import com.company.hex.ui.core.BaseElement;
import java.util.function.Supplier;

/**
 * Interceptor для перехвата действий с UI элементами.
 * Использует паттерн Chain of Responsibility для гибкого контроля.
 */
public interface ElementInterceptor {

    /**
     * Перехватывает выполнение действия.
     * Interceptor может:
     * - Выполнить логику до/после действия
     * - Модифицировать результат
     * - Реализовать retry-логику
     * - Пропустить действие полностью
     *
     * @param context контекст действия
     * @param chain цепочка для продолжения выполнения
     * @param <R> тип результата
     * @return результат действия
     */
    <R> R intercept(ActionContext context, ActionChain<R> chain);

    /**
     * Приоритет interceptor'а. Меньше значение = раньше выполняется.
     */
    default int getOrder() {
        return 100;
    }

    /**
     * Проверяет, должен ли interceptor обрабатывать данное действие.
     */
    default boolean supports(String actionName) {
        return true;
    }
}