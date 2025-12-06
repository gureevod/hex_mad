package com.company.hex.db.core;

import java.util.Map;

/**
 * Описание запроса для выполнения.
 * 
 * <p>Инкапсулирует SQL-запрос с именованными параметрами и метаинформацией.
 * Именованные параметры в SQL указываются в формате {@code :paramName}.
 *
 * <p>Пример:
 * <pre>{@code
 * Query query = new SimpleQuery(
 *     "SELECT * FROM users WHERE id = :id AND active = :active",
 *     Map.of("id", 1L, "active", true),
 *     QueryType.SELECT,
 *     false
 * );
 * }</pre>
 *
 * @author hex-core-db
 * @since 1.0.0
 */
public interface Query {

    /**
     * SQL-запрос с именованными параметрами в формате {@code :name}.
     *
     * @return SQL-строка запроса
     */
    String sql();

    /**
     * Значения именованных параметров.
     *
     * @return неизменяемая карта параметров (имя → значение)
     */
    Map<String, Object> parameters();

    /**
     * Тип запроса.
     *
     * @return тип запроса (SELECT, INSERT, UPDATE, DELETE, SCRIPT)
     */
    QueryType type();

    /**
     * Флаг необходимости возврата сгенерированных ключей.
     * 
     * <p>Используется для INSERT-запросов с автоинкрементными полями.
     *
     * @return true, если нужно вернуть сгенерированные ключи
     */
    boolean returnGeneratedKeys();
}
