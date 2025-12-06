package com.company.hex.db.builders;

import com.company.hex.db.DbException;
import com.company.hex.db.core.Query;
import com.company.hex.db.core.QueryType;
import com.company.hex.db.executor.SimpleQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Билдер UPDATE-запросов.
 *
 * <p><b>Защита:</b> build() выбрасывает исключение, если не указаны WHERE-условия.
 * Это предотвращает случайное обновление всех строк.
 *
 * <p><b>НЕ потокобезопасен</b> — создавайте новый экземпляр в каждом потоке.
 *
 * <p>Пример использования:
 * <pre>{@code
 * Query query = SQL.update("users")
 *     .set("name", "New Name")
 *     .set("updated_at", LocalDateTime.now())
 *     .where("id", userId)
 *     .build();
 * }</pre>
 *
 * @author hex-core-db
 * @since 1.0.0
 */
public class UpdateBuilder implements QueryBuilder {

    private final String table;
    private final Map<String, Object> setValues = new LinkedHashMap<>();
    private final List<String> whereClauses = new ArrayList<>();
    private final Map<String, Object> whereParams = new LinkedHashMap<>();
    private boolean allowAll = false;
    
    private final AtomicInteger paramCounter = new AtomicInteger(0);

    /**
     * Создать UpdateBuilder для указанной таблицы.
     *
     * @param table имя таблицы
     */
    public UpdateBuilder(String table) {
        this.table = table;
    }

    /**
     * Установить значение колонки.
     *
     * @param column имя колонки
     * @param value  новое значение
     * @return этот билдер для цепочки вызовов
     */
    public UpdateBuilder set(String column, Object value) {
        setValues.put(column, value);
        return this;
    }

    /**
     * Установить несколько значений из Map.
     *
     * @param values карта имён колонок и значений
     * @return этот билдер для цепочки вызовов
     */
    public UpdateBuilder setAll(Map<String, Object> values) {
        setValues.putAll(values);
        return this;
    }

    /**
     * Добавить WHERE условие с оператором равенства.
     *
     * @param column имя колонки
     * @param value  значение для сравнения (null генерирует IS NULL)
     * @return этот билдер для цепочки вызовов
     */
    public UpdateBuilder where(String column, Object value) {
        if (value == null) {
            whereClauses.add(column + " IS NULL");
        } else {
            String paramName = "w_" + column + "_" + paramCounter.incrementAndGet();
            whereClauses.add(column + " = :" + paramName);
            whereParams.put(paramName, value);
        }
        return this;
    }

    /**
     * Добавить WHERE условие с произвольным оператором.
     *
     * @param column   имя колонки
     * @param operator оператор сравнения
     * @param value    значение для сравнения
     * @return этот билдер для цепочки вызовов
     */
    public UpdateBuilder where(String column, String operator, Object value) {
        String paramName = "w_" + column + "_" + paramCounter.incrementAndGet();
        whereClauses.add(column + " " + operator + " :" + paramName);
        whereParams.put(paramName, value);
        return this;
    }

    /**
     * Добавить WHERE IN условие.
     *
     * @param column имя колонки
     * @param values коллекция значений
     * @return этот билдер для цепочки вызовов
     */
    public UpdateBuilder whereIn(String column, Collection<?> values) {
        if (values == null || values.isEmpty()) {
            whereClauses.add("1 = 0");
            return this;
        }
        String paramName = "w_" + column + "_" + paramCounter.incrementAndGet();
        whereClauses.add(column + " IN (:" + paramName + ")");
        whereParams.put(paramName, values);
        return this;
    }

    /**
     * Добавить WHERE IS NULL условие.
     *
     * @param column имя колонки
     * @return этот билдер для цепочки вызовов
     */
    public UpdateBuilder whereNull(String column) {
        whereClauses.add(column + " IS NULL");
        return this;
    }

    /**
     * Добавить WHERE IS NOT NULL условие.
     *
     * @param column имя колонки
     * @return этот билдер для цепочки вызовов
     */
    public UpdateBuilder whereNotNull(String column) {
        whereClauses.add(column + " IS NOT NULL");
        return this;
    }

    /**
     * Разрешить UPDATE без WHERE.
     * <b>Используйте с осторожностью!</b> Это обновит все строки в таблице.
     *
     * @return этот билдер для цепочки вызовов
     */
    public UpdateBuilder all() {
        this.allowAll = true;
        return this;
    }

    /**
     * Получить имя таблицы.
     */
    protected String getTable() {
        return table;
    }

    /**
     * Получить SET-значения.
     */
    protected Map<String, Object> getSetValues() {
        return setValues;
    }

    /**
     * Получить WHERE-условия.
     */
    protected List<String> getWhereClauses() {
        return whereClauses;
    }

    /**
     * Получить WHERE-параметры.
     */
    protected Map<String, Object> getWhereParams() {
        return whereParams;
    }

    @Override
    public Query build() {
        if (table == null || table.isBlank()) {
            throw new DbException("Требуется имя таблицы для UPDATE-запроса");
        }
        if (setValues.isEmpty()) {
            throw new DbException("Требуется хотя бы одно SET-значение для UPDATE-запроса");
        }
        if (whereClauses.isEmpty() && !allowAll) {
            throw new DbException(
                "UPDATE без WHERE запрещён. " 
                + "Вызовите .all() явно, если хотите обновить все строки.");
        }

        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(table).append(" SET ");

        // SET col1 = :col1, col2 = :col2
        String setClause = setValues.keySet().stream()
            .map(col -> col + " = :" + col)
            .collect(Collectors.joining(", "));
        sql.append(setClause);

        // WHERE
        if (!whereClauses.isEmpty()) {
            sql.append(" WHERE ");
            sql.append(String.join(" AND ", whereClauses));
        }

        // Combine parameters
        Map<String, Object> allParams = new LinkedHashMap<>(setValues);
        allParams.putAll(whereParams);

        return new SimpleQuery(sql.toString(), allParams, QueryType.UPDATE);
    }
}
