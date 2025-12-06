package com.company.hex.db.builders;

import com.company.hex.db.core.Query;
import com.company.hex.db.core.QueryExecutor;
import com.company.hex.db.core.QueryResult;
import com.company.hex.db.core.QueryType;
import com.company.hex.db.core.RowMapper;
import com.company.hex.db.executor.SimpleQuery;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Билдер сырых SQL-запросов.
 *
 * <p>Используйте для сложных запросов, которые нельзя выразить другими билдерами:
 * CTE, сложные подзапросы, вызовы хранимых процедур и т.д.
 *
 * <p><b>НЕ потокобезопасен</b> — создавайте новый экземпляр в каждом потоке.
 *
 * <p>Пример использования:
 * <pre>{@code
 * Query query = SQL.raw("""
 *         WITH active_users AS (
 *             SELECT * FROM users WHERE active = :active
 *         )
 *         SELECT u.*, COUNT(o.id) as order_count
 *         FROM active_users u
 *         LEFT JOIN orders o ON o.user_id = u.id
 *         WHERE o.created_at > :since
 *         GROUP BY u.id
 *         """)
 *     .param("active", true)
 *     .param("since", LocalDate.now().minusDays(30))
 *     .build();
 * }</pre>
 *
 * @author hex-core-db
 * @since 1.0.0
 */
public class RawQueryBuilder implements QueryBuilder {

    private final String sql;
    private final Map<String, Object> parameters = new LinkedHashMap<>();
    private boolean returnKeys = false;
    private QueryType queryType;

    /**
     * Создать RawQueryBuilder с указанным SQL.
     *
     * @param sql сырая SQL-строка с именованными параметрами в формате :name
     */
    public RawQueryBuilder(String sql) {
        this.sql = sql;
        this.queryType = SimpleQuery.detectType(sql);
    }

    /**
     * Добавить именованный параметр.
     *
     * @param name  имя параметра (без двоеточия)
     * @param value значение параметра
     * @return этот билдер для цепочки вызовов
     */
    public RawQueryBuilder param(String name, Object value) {
        parameters.put(name, value);
        return this;
    }

    /**
     * Добавить несколько именованных параметров.
     *
     * @param params карта имён параметров и значений
     * @return этот билдер для цепочки вызовов
     */
    public RawQueryBuilder params(Map<String, Object> params) {
        parameters.putAll(params);
        return this;
    }

    /**
     * Включить возврат сгенерированных ключей.
     *
     * @return этот билдер для цепочки вызовов
     */
    public RawQueryBuilder returningKeys() {
        this.returnKeys = true;
        return this;
    }

    /**
     * Переопределить автоматически определённый тип запроса.
     *
     * @param type тип запроса
     * @return этот билдер для цепочки вызовов
     */
    public RawQueryBuilder type(QueryType type) {
        this.queryType = type;
        return this;
    }

    @Override
    public Query build() {
        return new SimpleQuery(sql, parameters, queryType, returnKeys);
    }

    /**
     * Построить и выполнить запрос, вернуть результат.
     *
     * @param executor исполнитель запросов
     * @return результат запроса
     */
    public QueryResult execute(QueryExecutor executor) {
        return executor.execute(build());
    }

    /**
     * Выполнить и вернуть все строки как List из Map.
     *
     * @param executor исполнитель запросов
     * @return список строк
     */
    public List<Map<String, Object>> toList(QueryExecutor executor) {
        return executor.execute(build()).toList();
    }

    /**
     * Выполнить и вернуть все строки, преобразованные в объекты.
     *
     * @param executor исполнитель запросов
     * @param mapper   функция преобразования строки
     * @param <T>      тип результата
     * @return список преобразованных объектов
     */
    public <T> List<T> toList(QueryExecutor executor, RowMapper<T> mapper) {
        return executor.execute(build()).toList(mapper);
    }

    /**
     * Выполнить и вернуть первую строку.
     *
     * @param executor исполнитель запросов
     * @return первая строка или пустой Optional
     */
    public Optional<Map<String, Object>> first(QueryExecutor executor) {
        return executor.execute(build()).firstRow();
    }

    /**
     * Выполнить и вернуть скалярное значение.
     *
     * @param executor исполнитель запросов
     * @param type     ожидаемый тип
     * @param <T>      тип результата
     * @return скалярное значение
     */
    public <T> T scalar(QueryExecutor executor, Class<T> type) {
        return executor.execute(build()).scalar(type);
    }

    /**
     * Выполнить и вернуть количество затронутых строк.
     *
     * @param executor исполнитель запросов
     * @return количество затронутых строк
     */
    public int affectedRows(QueryExecutor executor) {
        return executor.execute(build()).affectedRows();
    }

    /**
     * Выполнить и вернуть сгенерированный ключ.
     *
     * @param executor исполнитель запросов
     * @param keyType  тип ключа
     * @param <T>      тип ключа
     * @return сгенерированный ключ
     */
    public <T> T generatedKey(QueryExecutor executor, Class<T> keyType) {
        returningKeys();
        return executor.execute(build()).generatedKey(keyType);
    }
}
