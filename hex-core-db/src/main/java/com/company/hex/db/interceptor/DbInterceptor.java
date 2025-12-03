package com.company.hex.db.interceptor;

import com.company.hex.db.service.QueryDefinition;

/**
 * Интерфейс интерцептора для перехвата выполнения запросов к БД.
 *
 * <p>Интерцепторы позволяют добавлять cross-cutting concerns к выполнению
 * запросов: логирование, метрики, таймауты, аудит и т.д.</p>
 *
 * <p>Реализации должны быть потокобезопасными, так как один экземпляр
 * интерцептора может использоваться несколькими потоками одновременно.</p>
 *
 * <h2>Пример реализации</h2>
 * <pre>{@code
 * public class LoggingInterceptor implements DbInterceptor {
 *     @Override
 *     public Object intercept(DbInterceptorChain chain) {
 *         QueryDefinition query = chain.query();
 *         log.info("Executing: {}", query.getSql());
 *
 *         long start = System.currentTimeMillis();
 *         try {
 *             Object result = chain.proceed();
 *             log.info("Completed in {} ms", System.currentTimeMillis() - start);
 *             return result;
 *         } catch (Exception e) {
 *             log.error("Failed: {}", e.getMessage());
 *             throw e;
 *         }
 *     }
 *
 *     @Override
 *     public int getOrder() {
 *         return -1000; // Execute first
 *     }
 * }
 * }</pre>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see DbInterceptorChain
 */
public interface DbInterceptor {

    /**
     * Перехватывает выполнение запроса.
     *
     * <p>Метод должен вызвать {@code chain.proceed()} для продолжения
     * выполнения цепочки. Может модифицировать запрос перед передачей
     * дальше или обработать результат после выполнения.</p>
     *
     * @param chain цепочка выполнения
     * @return результат выполнения запроса
     */
    Object intercept(DbInterceptorChain chain);

    /**
     * Порядок выполнения интерцептора в цепочке.
     *
     * <p>Меньшее значение означает более раннее выполнение.
     * Рекомендуемые диапазоны:</p>
     * <ul>
     *     <li>{@code -1000} — системные (логирование)</li>
     *     <li>{@code -500} — мониторинг (slow query)</li>
     *     <li>{@code 0} — пользовательские</li>
     *     <li>{@code 100} — таймауты</li>
     *     <li>{@code 500} — транзакции</li>
     *     <li>{@code 1000} — финальное выполнение</li>
     * </ul>
     *
     * @return порядок выполнения (меньше = раньше)
     */
    default int getOrder() {
        return 0;
    }
}
