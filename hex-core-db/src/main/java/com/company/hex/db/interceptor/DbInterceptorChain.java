package com.company.hex.db.interceptor;

import com.company.hex.db.service.QueryDefinition;

/**
 * Цепочка интерцепторов для последовательного выполнения перехвата запросов.
 *
 * <p>Предоставляет доступ к текущему запросу и контексту выполнения,
 * а также метод для продолжения выполнения цепочки.</p>
 *
 * <h2>Использование в интерцепторе</h2>
 * <pre>{@code
 * public class MyInterceptor implements DbInterceptor {
 *     @Override
 *     public Object intercept(DbInterceptorChain chain) {
 *         // Получаем информацию о запросе
 *         QueryDefinition query = chain.query();
 *         log.info("SQL: {}", query.getSql());
 *
 *         // Продолжаем выполнение
 *         Object result = chain.proceed();
 *
 *         // Обрабатываем результат
 *         return result;
 *     }
 * }
 * }</pre>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see DbInterceptor
 */
public interface DbInterceptorChain {

    /**
     * Возвращает текущий запрос.
     *
     * @return определение запроса
     */
    QueryDefinition query();

    /**
     * Возвращает контекст выполнения.
     *
     * <p>Контекст содержит информацию о соединении, транзакции
     * и других параметрах выполнения.</p>
     *
     * @return контекст выполнения
     */
    ExecutionContext context();

    /**
     * Продолжает выполнение цепочки.
     *
     * <p>Вызывает следующий интерцептор в цепочке или выполняет
     * запрос, если интерцепторы закончились.</p>
     *
     * @return результат выполнения
     */
    Object proceed();

    /**
     * Продолжает выполнение с модифицированным запросом.
     *
     * <p>Позволяет интерцептору изменить запрос перед передачей
     * следующему интерцептору.</p>
     *
     * @param query модифицированный запрос
     * @return результат выполнения
     */
    Object proceed(QueryDefinition query);
}
