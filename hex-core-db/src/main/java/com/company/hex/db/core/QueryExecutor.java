package com.company.hex.db.core;

import java.util.List;
import java.util.Map;

/**
 * Выполняет запросы к базе данных.
 * 
 * <p><b>Потокобезопасность:</b> реализации этого интерфейса должны быть потокобезопасными.
 * Каждый запрос получает собственное соединение из пула и возвращает его после выполнения.
 *
 * <p>Основные методы:
 * <ul>
 *   <li>{@link #execute(Query)} — выполнить подготовленный Query-объект</li>
 *   <li>{@link #execute(String, Map)} — выполнить SQL с параметрами</li>
 *   <li>{@link #execute(String)} — выполнить SQL без параметров</li>
 *   <li>{@link #executeBatch(String, List)} — batch-выполнение для множества строк</li>
 * </ul>
 *
 * @author hex-core-db
 * @since 1.0.0
 * @see Query
 * @see QueryResult
 */
public interface QueryExecutor {

    /**
     * Выполнить Query-объект.
     *
     * @param query объект запроса с SQL и параметрами
     * @return результат выполнения запроса
     * @throws com.company.hex.db.DbException при ошибке выполнения
     */
    QueryResult execute(Query query);

    /**
     * Выполнить SQL-запрос с именованными параметрами.
     * 
     * <p>Тип запроса определяется автоматически по первому ключевому слову SQL.
     *
     * @param sql    SQL-запрос с параметрами в формате {@code :paramName}
     * @param params карта параметров (имя → значение)
     * @return результат выполнения запроса
     * @throws com.company.hex.db.DbException при ошибке выполнения
     */
    QueryResult execute(String sql, Map<String, Object> params);

    /**
     * Выполнить SQL-запрос без параметров.
     *
     * @param sql SQL-запрос
     * @return результат выполнения запроса
     * @throws com.company.hex.db.DbException при ошибке выполнения
     */
    QueryResult execute(String sql);

    /**
     * Batch-выполнение SQL для множества наборов параметров.
     * 
     * <p>Используется для эффективной вставки/обновления большого количества строк.
     * Все операции выполняются в одной транзакции.
     *
     * @param sql        SQL-запрос с параметрами в формате {@code :paramName}
     * @param paramsList список карт параметров для каждой строки
     * @return массив с количеством затронутых строк для каждой операции
     * @throws com.company.hex.db.DbException при ошибке выполнения
     */
    int[] executeBatch(String sql, List<Map<String, Object>> paramsList);
}
