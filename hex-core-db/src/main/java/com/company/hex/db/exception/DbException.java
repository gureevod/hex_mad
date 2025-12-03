package com.company.hex.db.exception;

import java.util.Collections;
import java.util.Map;

/**
 * Базовое исключение для всех ошибок DB модуля.
 *
 * <p>Содержит контекстную информацию о запросе, который вызвал ошибку:
 * имя метода, SQL запрос и параметры. Эта информация помогает
 * в диагностике проблем.</p>
 *
 * <h2>Иерархия исключений</h2>
 * <pre>
 * DbException (базовый класс)
 * ├── QueryValidationException     - Ошибка валидации SQL перед выполнением
 * ├── ConnectionException          - Ошибки подключения к БД
 * ├── QueryExecutionException      - Ошибки выполнения запроса
 * │   ├── QueryTimeoutException    - Таймаут запроса
 * │   ├── ConstraintViolationException - Нарушение ограничений
 * │   │   ├── UniqueConstraintException
 * │   │   ├── ForeignKeyException
 * │   │   └── NotNullException
 * │   └── DataAccessException      - Ошибки доступа к данным
 * ├── MappingException             - Ошибки маппинга результатов
 * └── TransactionException         - Ошибки транзакций
 * </pre>
 *
 * <h2>Пример вывода</h2>
 * <pre>
 * DbException: Parameter mismatch in findById()
 * Method: findById
 * SQL: SELECT * FROM users WHERE id = :userId
 * Parameters: {id=42}
 * </pre>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 */
public class DbException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Имя метода репозитория, вызвавшего ошибку.
     */
    private final String methodName;

    /**
     * SQL запрос, вызвавший ошибку.
     */
    private final String sql;

    /**
     * Параметры запроса.
     */
    private final Map<String, Object> parameters;

    /**
     * Создаёт исключение только с сообщением.
     *
     * @param message сообщение об ошибке
     */
    public DbException(String message) {
        super(message);
        this.methodName = null;
        this.sql = null;
        this.parameters = null;
    }

    /**
     * Создаёт исключение с сообщением и причиной.
     *
     * @param message сообщение об ошибке
     * @param cause причина ошибки
     */
    public DbException(String message, Throwable cause) {
        super(message, cause);
        this.methodName = null;
        this.sql = null;
        this.parameters = null;
    }

    /**
     * Создаёт исключение с полным контекстом.
     *
     * @param message сообщение об ошибке
     * @param methodName имя метода
     * @param sql SQL запрос
     * @param parameters параметры запроса
     * @param cause причина ошибки (может быть null)
     */
    public DbException(String message, String methodName, String sql,
                       Map<String, Object> parameters, Throwable cause) {
        super(formatMessage(message, methodName, sql, parameters), cause);
        this.methodName = methodName;
        this.sql = sql;
        this.parameters = parameters != null
            ? Collections.unmodifiableMap(parameters)
            : null;
    }

    /**
     * Форматирует сообщение об ошибке с контекстом.
     */
    private static String formatMessage(String message, String methodName,
                                         String sql, Map<String, Object> parameters) {
        StringBuilder sb = new StringBuilder();
        sb.append(message);

        if (methodName != null) {
            sb.append("\nMethod: ").append(methodName);
        }

        if (sql != null) {
            sb.append("\nSQL: ").append(sql);
        }

        if (parameters != null && !parameters.isEmpty()) {
            sb.append("\nParameters: ").append(parameters);
        }

        return sb.toString();
    }

    /**
     * Возвращает имя метода, вызвавшего ошибку.
     *
     * @return имя метода или null
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Возвращает SQL запрос, вызвавший ошибку.
     *
     * @return SQL запрос или null
     */
    public String getSql() {
        return sql;
    }

    /**
     * Возвращает параметры запроса.
     *
     * @return неизменяемая карта параметров или null
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }
}
