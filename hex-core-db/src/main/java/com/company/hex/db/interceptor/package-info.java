/**
 * Пакет с системой интерцепторов для перехвата выполнения запросов.
 *
 * <p>Предоставляет механизм для добавления cross-cutting concerns:</p>
 *
 * <ul>
 *     <li>{@code DbInterceptor} — интерфейс интерцептора</li>
 *     <li>{@code DbInterceptorChain} — цепочка интерцепторов</li>
 *     <li>{@code QueryLoggingInterceptor} — логирование SQL и параметров</li>
 *     <li>{@code SlowQueryInterceptor} — предупреждения о медленных запросах</li>
 *     <li>{@code QueryTimeoutInterceptor} — установка таймаутов</li>
 *     <li>{@code ConnectionTrackingInterceptor} — метрики соединений</li>
 * </ul>
 *
 * <h2>Пример создания интерцептора</h2>
 * <pre>{@code
 * public class AuditInterceptor implements DbInterceptor {
 *     @Override
 *     public Object intercept(DbInterceptorChain chain) {
 *         log.info("Executing: {}", chain.query().getSql());
 *         long start = System.currentTimeMillis();
 *         Object result = chain.proceed();
 *         log.info("Completed in {} ms", System.currentTimeMillis() - start);
 *         return result;
 *     }
 *
 *     @Override
 *     public int getOrder() {
 *         return 0; // User interceptors order
 *     }
 * }
 * }</pre>
 *
 * @since 1.0.0
 */
package com.company.hex.db.interceptor;
