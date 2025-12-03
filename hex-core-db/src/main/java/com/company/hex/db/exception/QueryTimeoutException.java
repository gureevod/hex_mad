package com.company.hex.db.exception;

import java.util.Map;

/**
 * Исключение, возникающее при превышении таймаута выполнения запроса.
 *
 * <p>Выбрасывается когда запрос не завершился в отведённое время.
 * Таймаут задаётся через {@code @Timeout} аннотацию или в конфигурации.</p>
 *
 * <h2>Пример</h2>
 * <pre>{@code
 * @Select("SELECT * FROM large_table")
 * @Timeout(5)  // 5 секунд
 * List<Entity> findAll();
 *
 * // При превышении таймаута:
 * // QueryTimeoutException: Query timeout after 5 seconds
 * //   Method: findAll
 * //   SQL: SELECT * FROM large_table
 * }</pre>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see QueryExecutionException
 */
public class QueryTimeoutException extends QueryExecutionException {

    private static final long serialVersionUID = 1L;

    /**
     * Таймаут в секундах.
     */
    private final int timeoutSeconds;

    /**
     * Создаёт исключение с таймаутом.
     *
     * @param timeoutSeconds таймаут в секундах
     */
    public QueryTimeoutException(int timeoutSeconds) {
        super("Query timeout after " + timeoutSeconds + " seconds");
        this.timeoutSeconds = timeoutSeconds;
    }

    /**
     * Создаёт исключение с полным контекстом.
     *
     * @param timeoutSeconds таймаут в секундах
     * @param methodName имя метода
     * @param sql SQL запрос
     * @param parameters параметры запроса
     * @param cause причина ошибки
     */
    public QueryTimeoutException(int timeoutSeconds, String methodName, String sql,
                                  Map<String, Object> parameters, Throwable cause) {
        super("Query timeout after " + timeoutSeconds + " seconds",
            methodName, sql, parameters, cause);
        this.timeoutSeconds = timeoutSeconds;
    }

    /**
     * Возвращает таймаут в секундах.
     *
     * @return таймаут
     */
    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }
}
