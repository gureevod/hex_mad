package com.company.hex.db.builders;

import com.company.hex.db.core.Query;

/**
 * Базовый интерфейс для всех SQL-билдеров.
 * 
 * <p>Предоставляет метод {@link #build()} для создания итогового объекта {@link Query}.
 *
 * @author hex-core-db
 * @since 1.0.0
 */
public interface QueryBuilder {

    /**
     * Построить объект Query из текущего состояния билдера.
     *
     * @return сконструированный Query
     */
    Query build();
}
