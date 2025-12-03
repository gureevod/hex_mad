package com.company.hex.db.exception;

import java.util.Map;

/**
 * Исключение, возникающее при ошибке выполнения SQL запроса.
 *
 * <p>Это общий класс для ошибок, происходящих во время выполнения
 * запроса через JDBC. Содержит полную информацию о контексте ошибки.</p>
 *
 * <h2>Подклассы</h2>
 * <ul>
 *     <li>{@link QueryTimeoutException} — таймаут запроса</li>
 *     <li>{@link ConstraintViolationException} — нарушение ограничений БД</li>
 * </ul>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see DbException
 */
public class QueryExecutionException extends DbException {

    private static final long serialVersionUID = 1L;

    /**
     * SQL State код ошибки.
     */
    private final String sqlState;

    /**
     * Vendor-specific error code.
     */
    private final int errorCode;

    /**
     * Создаёт исключение только с сообщением.
     *
     * @param message сообщение об ошибке
     */
    public QueryExecutionException(String message) {
        super(message);
        this.sqlState = null;
        this.errorCode = 0;
    }

    /**
     * Создаёт исключение с сообщением и причиной.
     *
     * @param message сообщение об ошибке
     * @param cause причина ошибки
     */
    public QueryExecutionException(String message, Throwable cause) {
        super(message, cause);
        this.sqlState = null;
        this.errorCode = 0;
    }

    /**
     * Создаёт исключение с полным контекстом.
     *
     * @param message сообщение об ошибке
     * @param methodName имя метода
     * @param sql SQL запрос
     * @param parameters параметры запроса
     * @param cause причина ошибки
     */
    public QueryExecutionException(String message, String methodName, String sql,
                                    Map<String, Object> parameters, Throwable cause) {
        super(message, methodName, sql, parameters, cause);
        this.sqlState = null;
        this.errorCode = 0;
    }

    /**
     * Создаёт исключение с SQL State и error code.
     *
     * @param message сообщение об ошибке
     * @param methodName имя метода
     * @param sql SQL запрос
     * @param parameters параметры запроса
     * @param sqlState SQL State код
     * @param errorCode vendor error code
     * @param cause причина ошибки
     */
    public QueryExecutionException(String message, String methodName, String sql,
                                    Map<String, Object> parameters,
                                    String sqlState, int errorCode, Throwable cause) {
        super(formatMessageWithCodes(message, sqlState, errorCode),
            methodName, sql, parameters, cause);
        this.sqlState = sqlState;
        this.errorCode = errorCode;
    }

    /**
     * Форматирует сообщение с кодами ошибок.
     */
    private static String formatMessageWithCodes(String message, String sqlState, int errorCode) {
        StringBuilder sb = new StringBuilder(message);
        if (sqlState != null) {
            sb.append(" [SQLState: ").append(sqlState).append("]");
        }
        if (errorCode != 0) {
            sb.append(" [ErrorCode: ").append(errorCode).append("]");
        }
        return sb.toString();
    }

    /**
     * Возвращает SQL State код ошибки.
     *
     * <p>SQL State — стандартизированный код ошибки ANSI SQL.</p>
     *
     * @return SQL State или null
     */
    public String getSqlState() {
        return sqlState;
    }

    /**
     * Возвращает vendor-specific error code.
     *
     * @return код ошибки
     */
    public int getErrorCode() {
        return errorCode;
    }
}
