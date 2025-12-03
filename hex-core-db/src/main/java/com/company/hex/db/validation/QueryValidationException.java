package com.company.hex.db.validation;

import com.company.hex.db.exception.DbException;
import com.company.hex.db.service.QueryDefinition;

/**
 * Исключение, выбрасываемое при ошибке валидации SQL запроса.
 *
 * <p>Содержит информацию о запросе, вызвавшем ошибку валидации,
 * и список конкретных ошибок или предупреждений.</p>
 *
 * <h2>Примеры ошибок</h2>
 * <ul>
 *     <li>Несоответствие параметров: {@code :userId} в SQL, но {@code @Param("id")}</li>
 *     <li>UPDATE/DELETE без WHERE clause</li>
 *     <li>Опасные операции без {@code @DangerousQuery}</li>
 * </ul>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see QueryValidator
 */
public class QueryValidationException extends DbException {

    private static final long serialVersionUID = 1L;

    /**
     * Определение запроса, вызвавшего ошибку.
     */
    private final transient QueryDefinition queryDefinition;

    /**
     * Является ли это ошибкой (true) или предупреждением (false).
     */
    private final boolean error;

    /**
     * Создаёт исключение с сообщением и запросом.
     *
     * @param message сообщение об ошибке
     * @param queryDefinition определение запроса
     */
    public QueryValidationException(String message, QueryDefinition queryDefinition) {
        super(message,
            queryDefinition != null ? queryDefinition.getMethodName() : null,
            queryDefinition != null ? queryDefinition.getSql() : null,
            queryDefinition != null ? queryDefinition.getParameters() : null,
            null);
        this.queryDefinition = queryDefinition;
        this.error = true;
    }

    /**
     * Создаёт исключение с указанием типа (ошибка/предупреждение).
     *
     * @param message сообщение
     * @param queryDefinition определение запроса
     * @param isError true если это ошибка, false если предупреждение
     */
    public QueryValidationException(String message, QueryDefinition queryDefinition, boolean isError) {
        super(message,
            queryDefinition != null ? queryDefinition.getMethodName() : null,
            queryDefinition != null ? queryDefinition.getSql() : null,
            queryDefinition != null ? queryDefinition.getParameters() : null,
            null);
        this.queryDefinition = queryDefinition;
        this.error = isError;
    }

    /**
     * Создаёт исключение с причиной.
     *
     * @param message сообщение об ошибке
     * @param queryDefinition определение запроса
     * @param cause причина ошибки
     */
    public QueryValidationException(String message, QueryDefinition queryDefinition, Throwable cause) {
        super(message,
            queryDefinition != null ? queryDefinition.getMethodName() : null,
            queryDefinition != null ? queryDefinition.getSql() : null,
            queryDefinition != null ? queryDefinition.getParameters() : null,
            cause);
        this.queryDefinition = queryDefinition;
        this.error = true;
    }

    /**
     * Возвращает определение запроса, вызвавшего ошибку.
     *
     * @return QueryDefinition или null
     */
    public QueryDefinition getQueryDefinition() {
        return queryDefinition;
    }

    /**
     * Проверяет, является ли это ошибкой или предупреждением.
     *
     * @return true если это ошибка, false если предупреждение
     */
    public boolean isError() {
        return error;
    }
}
