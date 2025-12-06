package com.company.hex.db.core;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Результат выполнения запроса.
 * 
 * <p>Все данные читаются в память при создании — никаких ленивых вычислений,
 * никаких утечек ресурсов. После создания QueryResult все JDBC-ресурсы
 * (Connection, Statement, ResultSet) уже закрыты.
 *
 * <p>Интерфейс предоставляет методы для:
 * <ul>
 *   <li>SELECT-запросов: {@link #toList()}, {@link #firstRow()}, {@link #scalar(Class)}</li>
 *   <li>Модифицирующих запросов: {@link #affectedRows()}, {@link #generatedKey(Class)}</li>
 * </ul>
 *
 * @author hex-core-db
 * @since 1.0.0
 */
public interface QueryResult {

    // === SELECT результаты ===

    /**
     * Получить все строки как список Map.
     * 
     * <p>Самый простой случай — подходит для быстрых проверок в тестах.
     *
     * @return список строк, каждая строка — Map с именами колонок в нижнем регистре
     */
    List<Map<String, Object>> toList();

    /**
     * Получить все строки с применением маппера.
     *
     * @param mapper функция преобразования строки в объект
     * @param <T>    тип результата маппинга
     * @return список преобразованных объектов
     */
    <T> List<T> toList(RowMapper<T> mapper);

    /**
     * Получить первую строку результата.
     * 
     * <p>Рекомендуется использовать для запросов с LIMIT 1.
     *
     * @return первая строка как Map или пустой Optional
     */
    Optional<Map<String, Object>> firstRow();

    /**
     * Получить первую строку с применением маппера.
     *
     * @param mapper функция преобразования строки в объект
     * @param <T>    тип результата маппинга
     * @return преобразованный объект или пустой Optional
     */
    <T> Optional<T> firstRow(RowMapper<T> mapper);

    /**
     * Получить единственное скалярное значение.
     * 
     * <p>Подходит для запросов вида {@code SELECT COUNT(*) FROM ...}
     * или {@code SELECT MAX(id) FROM ...}.
     *
     * @param type целевой тип значения
     * @param <T>  тип возвращаемого значения
     * @return скалярное значение из первой колонки первой строки
     * @throws com.company.hex.db.DbException если результат пуст
     */
    <T> T scalar(Class<T> type);

    // === INSERT/UPDATE/DELETE результаты ===

    /**
     * Получить количество затронутых строк.
     * 
     * <p>Для INSERT/UPDATE/DELETE запросов.
     *
     * @return количество вставленных, обновлённых или удалённых строк
     */
    int affectedRows();

    /**
     * Получить сгенерированный ключ после INSERT.
     * 
     * <p>Работает только если запрос был выполнен с флагом returnGeneratedKeys.
     *
     * @param type целевой тип ключа (обычно Long или Integer)
     * @param <T>  тип возвращаемого значения
     * @return сгенерированный ключ
     * @throws com.company.hex.db.DbException если ключ не был сгенерирован
     */
    <T> T generatedKey(Class<T> type);
}
