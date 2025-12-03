package com.company.hex.db.exception;

import java.util.Map;

/**
 * Исключение, возникающее при нарушении ограничений базы данных.
 *
 * <p>Базовый класс для исключений, связанных с нарушением
 * constraint'ов: уникальность, внешние ключи, NOT NULL и т.д.</p>
 *
 * <h2>Подклассы</h2>
 * <ul>
 *     <li>{@link UniqueConstraintException} — нарушение уникальности</li>
 *     <li>{@link ForeignKeyException} — нарушение внешнего ключа</li>
 *     <li>{@link NotNullException} — NULL в NOT NULL колонке</li>
 * </ul>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see QueryExecutionException
 */
public class ConstraintViolationException extends QueryExecutionException {

    private static final long serialVersionUID = 1L;

    /**
     * Имя нарушенного ограничения.
     */
    private final String constraintName;

    /**
     * Имя таблицы.
     */
    private final String tableName;

    /**
     * Создаёт исключение с сообщением.
     *
     * @param message сообщение об ошибке
     */
    public ConstraintViolationException(String message) {
        super(message);
        this.constraintName = null;
        this.tableName = null;
    }

    /**
     * Создаёт исключение с информацией об ограничении.
     *
     * @param message сообщение об ошибке
     * @param constraintName имя ограничения
     * @param tableName имя таблицы
     */
    public ConstraintViolationException(String message, String constraintName, String tableName) {
        super(formatMessage(message, constraintName, tableName));
        this.constraintName = constraintName;
        this.tableName = tableName;
    }

    /**
     * Создаёт исключение с полным контекстом.
     *
     * @param message сообщение об ошибке
     * @param constraintName имя ограничения
     * @param tableName имя таблицы
     * @param methodName имя метода
     * @param sql SQL запрос
     * @param parameters параметры запроса
     * @param cause причина ошибки
     */
    public ConstraintViolationException(String message, String constraintName, String tableName,
                                         String methodName, String sql,
                                         Map<String, Object> parameters, Throwable cause) {
        super(formatMessage(message, constraintName, tableName),
            methodName, sql, parameters, cause);
        this.constraintName = constraintName;
        this.tableName = tableName;
    }

    /**
     * Форматирует сообщение с информацией об ограничении.
     */
    private static String formatMessage(String message, String constraintName, String tableName) {
        StringBuilder sb = new StringBuilder(message);
        if (constraintName != null) {
            sb.append(" [Constraint: ").append(constraintName).append("]");
        }
        if (tableName != null) {
            sb.append(" [Table: ").append(tableName).append("]");
        }
        return sb.toString();
    }

    /**
     * Возвращает имя нарушенного ограничения.
     *
     * @return имя ограничения или null
     */
    public String getConstraintName() {
        return constraintName;
    }

    /**
     * Возвращает имя таблицы.
     *
     * @return имя таблицы или null
     */
    public String getTableName() {
        return tableName;
    }
}
