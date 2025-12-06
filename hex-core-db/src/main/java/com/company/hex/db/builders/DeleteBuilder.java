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

/**
 * Билдер DELETE-запросов.
 *
 * <p><b>Защита:</b> build() выбрасывает исключение, если не указаны WHERE-условия.
 * Вызовите {@link #all()} явно, если хотите удалить все строки.
 *
 * <p><b>НЕ потокобезопасен</b> — создавайте новый экземпляр в каждом потоке.
 *
 * <p>Пример использования:
 * <pre>{@code
 * Query query = SQL.deleteFrom("users")
 *     .where("id", userId)
 *     .build();
 * }</pre>
 *
 * @author hex-core-db
 * @since 1.0.0
 */
public class DeleteBuilder implements QueryBuilder {

    private final String table;
    private final List<String> whereClauses = new ArrayList<>();
    private final Map<String, Object> whereParams = new LinkedHashMap<>();
    private boolean allowAll = false;
    
    private final AtomicInteger paramCounter = new AtomicInteger(0);

    /**
     * Создать DeleteBuilder для указанной таблицы.
     *
     * @param table имя таблицы
     */
    public DeleteBuilder(String table) {
        this.table = table;
    }

    /**
     * Добавить WHERE условие с оператором равенства.
     *
     * @param column имя колонки
     * @param value  значение для сравнения (null генерирует IS NULL)
     * @return этот билдер для цепочки вызовов
     */
    public DeleteBuilder where(String column, Object value) {
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
    public DeleteBuilder where(String column, String operator, Object value) {
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
    public DeleteBuilder whereIn(String column, Collection<?> values) {
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
    public DeleteBuilder whereNull(String column) {
        whereClauses.add(column + " IS NULL");
        return this;
    }

    /**
     * Добавить WHERE IS NOT NULL условие.
     *
     * @param column имя колонки
     * @return этот билдер для цепочки вызовов
     */
    public DeleteBuilder whereNotNull(String column) {
        whereClauses.add(column + " IS NOT NULL");
        return this;
    }

    /**
     * Разрешить DELETE без WHERE.
     * <b>Используйте с осторожностью!</b> Это удалит все строки из таблицы.
     *
     * @return этот билдер для цепочки вызовов
     */
    public DeleteBuilder all() {
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
            throw new DbException("Требуется имя таблицы для DELETE-запроса");
        }
        if (whereClauses.isEmpty() && !allowAll) {
            throw new DbException(
                "DELETE без WHERE запрещён. " 
                + "Вызовите .all() явно, если хотите удалить все строки.");
        }

        StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM ").append(table);

        // WHERE
        if (!whereClauses.isEmpty()) {
            sql.append(" WHERE ");
            sql.append(String.join(" AND ", whereClauses));
        }

        return new SimpleQuery(sql.toString(), whereParams, QueryType.DELETE);
    }
}
