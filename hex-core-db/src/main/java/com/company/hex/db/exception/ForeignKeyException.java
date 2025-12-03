package com.company.hex.db.exception;

import java.util.Map;

/**
 * Исключение, возникающее при нарушении ограничения внешнего ключа.
 *
 * <p>Выбрасывается при попытке:</p>
 * <ul>
 *     <li>Вставить запись с несуществующим foreign key</li>
 *     <li>Удалить запись, на которую ссылаются другие записи</li>
 * </ul>
 *
 * <h2>Пример</h2>
 * <pre>{@code
 * // При попытке создать заказ для несуществующего пользователя:
 * ForeignKeyException: Foreign key violation
 *   [Constraint: orders_user_id_fkey]
 *   [Table: orders]
 *   [ReferencedTable: users]
 *   [ReferencedColumn: id]
 * }</pre>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see ConstraintViolationException
 */
public class ForeignKeyException extends ConstraintViolationException {

    private static final long serialVersionUID = 1L;

    /**
     * Имя ссылающейся таблицы.
     */
    private final String referencedTable;

    /**
     * Имя ссылающейся колонки.
     */
    private final String referencedColumn;

    /**
     * Создаёт исключение с сообщением.
     *
     * @param message сообщение об ошибке
     */
    public ForeignKeyException(String message) {
        super(message);
        this.referencedTable = null;
        this.referencedColumn = null;
    }

    /**
     * Создаёт исключение с информацией о foreign key.
     *
     * @param message сообщение об ошибке
     * @param constraintName имя ограничения
     * @param tableName имя таблицы
     * @param referencedTable ссылающаяся таблица
     * @param referencedColumn ссылающаяся колонка
     */
    public ForeignKeyException(String message, String constraintName,
                                String tableName, String referencedTable,
                                String referencedColumn) {
        super(formatMessage(message, referencedTable, referencedColumn),
            constraintName, tableName);
        this.referencedTable = referencedTable;
        this.referencedColumn = referencedColumn;
    }

    /**
     * Создаёт исключение с полным контекстом.
     *
     * @param message сообщение об ошибке
     * @param constraintName имя ограничения
     * @param tableName имя таблицы
     * @param referencedTable ссылающаяся таблица
     * @param referencedColumn ссылающаяся колонка
     * @param methodName имя метода
     * @param sql SQL запрос
     * @param parameters параметры запроса
     * @param cause причина ошибки
     */
    public ForeignKeyException(String message, String constraintName,
                                String tableName, String referencedTable,
                                String referencedColumn, String methodName,
                                String sql, Map<String, Object> parameters,
                                Throwable cause) {
        super(formatMessage(message, referencedTable, referencedColumn),
            constraintName, tableName, methodName, sql, parameters, cause);
        this.referencedTable = referencedTable;
        this.referencedColumn = referencedColumn;
    }

    /**
     * Форматирует сообщение с информацией о foreign key.
     */
    private static String formatMessage(String message, String referencedTable,
                                         String referencedColumn) {
        StringBuilder sb = new StringBuilder(message);
        if (referencedTable != null) {
            sb.append(" [ReferencedTable: ").append(referencedTable).append("]");
        }
        if (referencedColumn != null) {
            sb.append(" [ReferencedColumn: ").append(referencedColumn).append("]");
        }
        return sb.toString();
    }

    /**
     * Возвращает имя ссылающейся таблицы.
     *
     * @return имя таблицы или null
     */
    public String getReferencedTable() {
        return referencedTable;
    }

    /**
     * Возвращает имя ссылающейся колонки.
     *
     * @return имя колонки или null
     */
    public String getReferencedColumn() {
        return referencedColumn;
    }
}
