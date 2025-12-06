package com.company.hex.db.core;

/**
 * Интерцептор для перехвата и модификации запросов перед выполнением.
 * 
 * <p>Используется для:
 * <ul>
 *   <li>Логирования SQL-запросов</li>
 *   <li>Добавления метрик</li>
 *   <li>Модификации запросов (например, добавление tenant_id)</li>
 * </ul>
 *
 * <p>Пример логирующего интерцептора:
 * <pre>{@code
 * QueryInterceptor loggingInterceptor = query -> {
 *     log.info("Executing: {} with params: {}", query.sql(), query.parameters());
 *     return query;
 * };
 * }</pre>
 *
 * @author hex-core-db
 * @since 1.0.0
 */
@FunctionalInterface
public interface QueryInterceptor {

    /**
     * Обработать запрос перед выполнением.
     * 
     * <p>Может вернуть тот же запрос (для логирования) или новый
     * модифицированный запрос.
     *
     * @param query исходный запрос
     * @return запрос для выполнения (может быть тем же или модифицированным)
     */
    Query beforeExecute(Query query);

    /**
     * Создать композитный интерцептор, выполняющий оба интерцептора последовательно.
     *
     * @param other следующий интерцептор
     * @return композитный интерцептор
     */
    default QueryInterceptor andThen(QueryInterceptor other) {
        return query -> other.beforeExecute(this.beforeExecute(query));
    }

    /**
     * Интерцептор-заглушка, не выполняющий никаких действий.
     *
     * @return интерцептор, возвращающий запрос без изменений
     */
    static QueryInterceptor noOp() {
        return query -> query;
    }
}
