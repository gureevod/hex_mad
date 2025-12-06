package com.company.hex.db.core;

/**
 * Тип SQL-запроса.
 * 
 * <p>Используется для определения стратегии выполнения запроса
 * и формирования правильного типа результата.
 *
 * @author hex-core-db
 * @since 1.0.0
 */
public enum QueryType {

    /**
     * SELECT запрос — возвращает набор строк.
     */
    SELECT,

    /**
     * INSERT запрос — вставка данных.
     */
    INSERT,

    /**
     * UPDATE запрос — обновление данных.
     */
    UPDATE,

    /**
     * DELETE запрос — удаление данных.
     */
    DELETE,

    /**
     * SCRIPT — произвольный SQL-скрипт (DDL, хранимые процедуры и т.д.).
     */
    SCRIPT
}
