package com.company.hex.db.builders;

import com.company.hex.db.DbException;
import com.company.hex.db.core.Query;
import com.company.hex.db.core.QueryType;
import com.company.hex.db.executor.SimpleQuery;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Билдер INSERT-запросов.
 *
 * <p><b>НЕ потокобезопасен</b> — создавайте новый экземпляр в каждом потоке.
 *
 * <p>Пример использования:
 * <pre>{@code
 * Query query = SQL.insertInto("users")
 *     .value("name", "John")
 *     .value("email", "john@example.com")
 *     .value("active", true)
 *     .build();
 * }</pre>
 *
 * @author hex-core-db
 * @since 1.0.0
 */
public class InsertBuilder implements QueryBuilder {

    private final String table;
    private final Map<String, Object> values = new LinkedHashMap<>();
    private boolean returnKeys = false;

    /**
     * Создать InsertBuilder для указанной таблицы.
     *
     * @param table имя таблицы
     */
    public InsertBuilder(String table) {
        this.table = table;
    }

    /**
     * Установить значение колонки.
     *
     * @param column имя колонки
     * @param value  значение для вставки
     * @return этот билдер для цепочки вызовов
     */
    public InsertBuilder value(String column, Object value) {
        values.put(column, value);
        return this;
    }

    /**
     * Установить несколько значений из Map.
     *
     * @param columnValues карта имён колонок и значений
     * @return этот билдер для цепочки вызовов
     */
    public InsertBuilder values(Map<String, Object> columnValues) {
        values.putAll(columnValues);
        return this;
    }

    /**
     * Включить возврат сгенерированных ключей (для executeAndGetKey).
     *
     * @return этот билдер для цепочки вызовов
     */
    public InsertBuilder returningKeys() {
        this.returnKeys = true;
        return this;
    }

    /**
     * Получить имя таблицы.
     *
     * @return имя таблицы
     */
    protected String getTable() {
        return table;
    }

    /**
     * Получить карту значений.
     *
     * @return карта значений
     */
    protected Map<String, Object> getValues() {
        return values;
    }

    /**
     * Проверить, включён ли возврат ключей.
     *
     * @return true если возврат ключей включён
     */
    protected boolean isReturnKeys() {
        return returnKeys;
    }

    @Override
    public Query build() {
        if (table == null || table.isBlank()) {
            throw new DbException("Требуется имя таблицы для INSERT-запроса");
        }
        if (values.isEmpty()) {
            throw new DbException("Требуется хотя бы одно значение для INSERT-запроса");
        }

        // Собираем: INSERT INTO table (col1, col2) VALUES (:col1, :col2)
        String columns = String.join(", ", values.keySet());
        String placeholders = values.keySet().stream()
            .map(col -> ":" + col)
            .collect(Collectors.joining(", "));

        String sql = "INSERT INTO " + table + " (" + columns + ") VALUES (" + placeholders + ")";

        return new SimpleQuery(sql, values, QueryType.INSERT, returnKeys);
    }
}
