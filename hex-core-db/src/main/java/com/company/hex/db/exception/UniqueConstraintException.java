package com.company.hex.db.exception;

import java.util.Map;

/**
 * Исключение, возникающее при нарушении уникального ограничения.
 *
 * <p>Выбрасывается при попытке вставить или обновить запись
 * с значением, нарушающим UNIQUE constraint.</p>
 *
 * <h2>Пример</h2>
 * <pre>{@code
 * // При попытке создать пользователя с существующим email:
 * UniqueConstraintException: Duplicate entry 'test@email.com'
 *   [Constraint: users_email_unique]
 *   [Table: users]
 *   [Column: email]
 *   [Value: test@email.com]
 * }</pre>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see ConstraintViolationException
 */
public class UniqueConstraintException extends ConstraintViolationException {

    private static final long serialVersionUID = 1L;

    /**
     * Имя колонки с дублирующимся значением.
     */
    private final String columnName;

    /**
     * Дублирующееся значение.
     */
    private final Object duplicateValue;

    /**
     * Создаёт исключение с сообщением.
     *
     * @param message сообщение об ошибке
     */
    public UniqueConstraintException(String message) {
        super(message);
        this.columnName = null;
        this.duplicateValue = null;
    }

    /**
     * Создаёт исключение с информацией о дубликате.
     *
     * @param message сообщение об ошибке
     * @param constraintName имя ограничения
     * @param tableName имя таблицы
     * @param columnName имя колонки
     * @param duplicateValue дублирующееся значение
     */
    public UniqueConstraintException(String message, String constraintName,
                                      String tableName, String columnName,
                                      Object duplicateValue) {
        super(formatMessage(message, columnName, duplicateValue), constraintName, tableName);
        this.columnName = columnName;
        this.duplicateValue = duplicateValue;
    }

    /**
     * Создаёт исключение с полным контекстом.
     *
     * @param message сообщение об ошибке
     * @param constraintName имя ограничения
     * @param tableName имя таблицы
     * @param columnName имя колонки
     * @param duplicateValue дублирующееся значение
     * @param methodName имя метода
     * @param sql SQL запрос
     * @param parameters параметры запроса
     * @param cause причина ошибки
     */
    public UniqueConstraintException(String message, String constraintName,
                                      String tableName, String columnName,
                                      Object duplicateValue, String methodName,
                                      String sql, Map<String, Object> parameters,
                                      Throwable cause) {
        super(formatMessage(message, columnName, duplicateValue),
            constraintName, tableName, methodName, sql, parameters, cause);
        this.columnName = columnName;
        this.duplicateValue = duplicateValue;
    }

    /**
     * Форматирует сообщение с информацией о дубликате.
     */
    private static String formatMessage(String message, String columnName, Object duplicateValue) {
        StringBuilder sb = new StringBuilder(message);
        if (columnName != null) {
            sb.append(" [Column: ").append(columnName).append("]");
        }
        if (duplicateValue != null) {
            sb.append(" [Value: ").append(duplicateValue).append("]");
        }
        return sb.toString();
    }

    /**
     * Возвращает имя колонки.
     *
     * @return имя колонки или null
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * Возвращает дублирующееся значение.
     *
     * @return значение или null
     */
    public Object getDuplicateValue() {
        return duplicateValue;
    }
}
