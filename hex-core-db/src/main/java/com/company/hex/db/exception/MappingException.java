package com.company.hex.db.exception;

import java.util.Map;

/**
 * Исключение, возникающее при ошибке маппинга результатов запроса.
 *
 * <p>Выбрасывается когда не удаётся преобразовать ResultSet
 * в Java объект.</p>
 *
 * <h2>Возможные причины</h2>
 * <ul>
 *     <li>Несоответствие типов колонок и полей класса</li>
 *     <li>Отсутствие необходимых колонок в ResultSet</li>
 *     <li>Ошибка при вызове конструктора или сеттера</li>
 *     <li>Невозможность создать экземпляр класса</li>
 * </ul>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see DbException
 */
public class MappingException extends DbException {

    private static final long serialVersionUID = 1L;

    /**
     * Целевой класс маппинга.
     */
    private final Class<?> targetClass;

    /**
     * Имя проблемной колонки.
     */
    private final String columnName;

    /**
     * Создаёт исключение с сообщением.
     *
     * @param message сообщение об ошибке
     */
    public MappingException(String message) {
        super(message);
        this.targetClass = null;
        this.columnName = null;
    }

    /**
     * Создаёт исключение с сообщением и причиной.
     *
     * @param message сообщение об ошибке
     * @param cause причина ошибки
     */
    public MappingException(String message, Throwable cause) {
        super(message, cause);
        this.targetClass = null;
        this.columnName = null;
    }

    /**
     * Создаёт исключение с информацией о классе.
     *
     * @param message сообщение об ошибке
     * @param targetClass целевой класс
     * @param cause причина ошибки
     */
    public MappingException(String message, Class<?> targetClass, Throwable cause) {
        super(formatMessage(message, targetClass, null), cause);
        this.targetClass = targetClass;
        this.columnName = null;
    }

    /**
     * Создаёт исключение с полным контекстом.
     *
     * @param message сообщение об ошибке
     * @param targetClass целевой класс
     * @param columnName имя колонки
     * @param methodName имя метода
     * @param sql SQL запрос
     * @param parameters параметры запроса
     * @param cause причина ошибки
     */
    public MappingException(String message, Class<?> targetClass, String columnName,
                             String methodName, String sql,
                             Map<String, Object> parameters, Throwable cause) {
        super(formatMessage(message, targetClass, columnName),
            methodName, sql, parameters, cause);
        this.targetClass = targetClass;
        this.columnName = columnName;
    }

    /**
     * Форматирует сообщение.
     */
    private static String formatMessage(String message, Class<?> targetClass, String columnName) {
        StringBuilder sb = new StringBuilder(message);
        if (targetClass != null) {
            sb.append(" [TargetClass: ").append(targetClass.getName()).append("]");
        }
        if (columnName != null) {
            sb.append(" [Column: ").append(columnName).append("]");
        }
        return sb.toString();
    }

    /**
     * Возвращает целевой класс маппинга.
     *
     * @return класс или null
     */
    public Class<?> getTargetClass() {
        return targetClass;
    }

    /**
     * Возвращает имя проблемной колонки.
     *
     * @return имя колонки или null
     */
    public String getColumnName() {
        return columnName;
    }
}
