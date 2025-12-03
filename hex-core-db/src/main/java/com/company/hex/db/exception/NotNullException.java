package com.company.hex.db.exception;

import java.util.Map;

/**
 * Исключение, возникающее при попытке вставить NULL в NOT NULL колонку.
 *
 * <h2>Пример</h2>
 * <pre>{@code
 * // При попытке создать пользователя без email:
 * NotNullException: Column 'email' cannot be null
 *   [Table: users]
 *   [Column: email]
 * }</pre>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see ConstraintViolationException
 */
public class NotNullException extends ConstraintViolationException {

    private static final long serialVersionUID = 1L;

    /**
     * Имя колонки.
     */
    private final String columnName;

    /**
     * Создаёт исключение с сообщением.
     *
     * @param message сообщение об ошибке
     */
    public NotNullException(String message) {
        super(message);
        this.columnName = null;
    }

    /**
     * Создаёт исключение с информацией о колонке.
     *
     * @param message сообщение об ошибке
     * @param tableName имя таблицы
     * @param columnName имя колонки
     */
    public NotNullException(String message, String tableName, String columnName) {
        super(formatMessage(message, columnName), null, tableName);
        this.columnName = columnName;
    }

    /**
     * Создаёт исключение с полным контекстом.
     *
     * @param message сообщение об ошибке
     * @param tableName имя таблицы
     * @param columnName имя колонки
     * @param methodName имя метода
     * @param sql SQL запрос
     * @param parameters параметры запроса
     * @param cause причина ошибки
     */
    public NotNullException(String message, String tableName, String columnName,
                             String methodName, String sql,
                             Map<String, Object> parameters, Throwable cause) {
        super(formatMessage(message, columnName), null, tableName,
            methodName, sql, parameters, cause);
        this.columnName = columnName;
    }

    /**
     * Форматирует сообщение.
     */
    private static String formatMessage(String message, String columnName) {
        if (columnName != null) {
            return message + " [Column: " + columnName + "]";
        }
        return message;
    }

    /**
     * Возвращает имя колонки.
     *
     * @return имя колонки или null
     */
    public String getColumnName() {
        return columnName;
    }
}
