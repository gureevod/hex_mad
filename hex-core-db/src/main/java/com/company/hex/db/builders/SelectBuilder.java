package com.company.hex.db.builders;

import com.company.hex.db.DbException;
import com.company.hex.db.core.Query;
import com.company.hex.db.core.QueryType;
import com.company.hex.db.executor.SimpleQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * Билдер SELECT-запросов.
 * 
 * <p><b>НЕ потокобезопасен</b> — создавайте новый экземпляр в каждом потоке.
 *
 * <p>Пример использования:
 * <pre>{@code
 * Query query = SQL.select("id", "name", "email")
 *     .from("users")
 *     .where("active", true)
 *     .where("age", ">=", 18)
 *     .orderBy("name")
 *     .limit(10)
 *     .build();
 * }</pre>
 *
 * @author hex-core-db
 * @since 1.0.0
 */
public class SelectBuilder implements QueryBuilder {

    private final List<String> columns;
    private String table;
    private final List<String> joins = new ArrayList<>();
    private final List<WhereClause> whereClauses = new ArrayList<>();
    private final Map<String, Object> parameters = new LinkedHashMap<>();
    private String groupBy;
    private String having;
    private String orderBy;
    private Integer limit;
    private Integer offset;
    
    private final AtomicInteger paramCounter = new AtomicInteger(0);

    /**
     * Создать SelectBuilder с указанными колонками.
     *
     * @param columns колонки для выборки
     */
    public SelectBuilder(String... columns) {
        this.columns = new ArrayList<>(Arrays.asList(columns));
    }

    // === FROM ===

    /**
     * Указать таблицу для выборки.
     *
     * @param table имя таблицы
     * @return этот билдер для цепочки вызовов
     */
    public SelectBuilder from(String table) {
        this.table = table;
        return this;
    }

    // === JOINs ===

    /**
     * Добавить INNER JOIN.
     *
     * @param joinTable таблица для соединения
     * @param on        условие соединения
     * @return этот билдер для цепочки вызовов
     */
    public SelectBuilder join(String joinTable, String on) {
        joins.add("JOIN " + joinTable + " ON " + on);
        return this;
    }

    /**
     * Добавить LEFT JOIN.
     *
     * @param joinTable таблица для соединения
     * @param on        условие соединения
     * @return этот билдер для цепочки вызовов
     */
    public SelectBuilder leftJoin(String joinTable, String on) {
        joins.add("LEFT JOIN " + joinTable + " ON " + on);
        return this;
    }

    /**
     * Добавить RIGHT JOIN.
     *
     * @param joinTable таблица для соединения
     * @param on        условие соединения
     * @return этот билдер для цепочки вызовов
     */
    public SelectBuilder rightJoin(String joinTable, String on) {
        joins.add("RIGHT JOIN " + joinTable + " ON " + on);
        return this;
    }

    // === WHERE ===

    /**
     * Добавить WHERE условие с оператором равенства.
     * Несколько вызовов объединяются через AND.
     *
     * @param column имя колонки
     * @param value  значение для сравнения (null генерирует IS NULL)
     * @return этот билдер для цепочки вызовов
     */
    public SelectBuilder where(String column, Object value) {
        if (value == null) {
            return whereNull(column);
        }
        return where(column, "=", value);
    }

    /**
     * Добавить WHERE условие с произвольным оператором.
     *
     * @param column   имя колонки
     * @param operator оператор сравнения (=, !=, &lt;, &gt;, &lt;=, &gt;=, LIKE и т.д.)
     * @param value    значение для сравнения
     * @return этот билдер для цепочки вызовов
     */
    public SelectBuilder where(String column, String operator, Object value) {
        String paramName = generateParamName(column);
        whereClauses.add(new WhereClause(column + " " + operator + " :" + paramName, "AND"));
        parameters.put(paramName, value);
        return this;
    }

    /**
     * Добавить WHERE IN условие.
     *
     * @param column имя колонки
     * @param values коллекция значений
     * @return этот билдер для цепочки вызовов
     */
    public SelectBuilder whereIn(String column, Collection<?> values) {
        if (values == null || values.isEmpty()) {
            // Пустой IN — добавляем всегда-ложное условие
            whereClauses.add(new WhereClause("1 = 0", "AND"));
            return this;
        }
        String paramName = generateParamName(column);
        whereClauses.add(new WhereClause(column + " IN (:" + paramName + ")", "AND"));
        parameters.put(paramName, values);
        return this;
    }

    /**
     * Добавить WHERE IS NULL условие.
     *
     * @param column имя колонки
     * @return этот билдер для цепочки вызовов
     */
    public SelectBuilder whereNull(String column) {
        whereClauses.add(new WhereClause(column + " IS NULL", "AND"));
        return this;
    }

    /**
     * Добавить WHERE IS NOT NULL условие.
     *
     * @param column имя колонки
     * @return этот билдер для цепочки вызовов
     */
    public SelectBuilder whereNotNull(String column) {
        whereClauses.add(new WhereClause(column + " IS NOT NULL", "AND"));
        return this;
    }

    /**
     * Добавить сырое WHERE условие для сложных случаев (OR, подзапросы).
     * 
     * <p>Используйте {@link #param(String, Object)} для добавления параметров.
     *
     * @param condition сырое SQL-условие
     * @return этот билдер для цепочки вызовов
     */
    public SelectBuilder whereRaw(String condition) {
        whereClauses.add(new WhereClause(condition, "AND"));
        return this;
    }

    /**
     * Условно добавить WHERE условие.
     * Условие добавляется только если флаг равен true.
     *
     * @param condition флаг, указывающий добавлять ли условие
     * @param column    имя колонки
     * @param value     значение для сравнения
     * @return этот билдер для цепочки вызовов
     */
    public SelectBuilder whereIf(boolean condition, String column, Object value) {
        if (condition) {
            return where(column, value);
        }
        return this;
    }

    /**
     * Условно добавить WHERE условие с произвольным оператором.
     *
     * @param condition флаг, указывающий добавлять ли условие
     * @param column    имя колонки
     * @param operator  оператор сравнения
     * @param value     значение для сравнения
     * @return этот билдер для цепочки вызовов
     */
    public SelectBuilder whereIf(boolean condition, String column, String operator, Object value) {
        if (condition) {
            return where(column, operator, value);
        }
        return this;
    }

    /**
     * Добавить именованный параметр для использования с {@link #whereRaw(String)}.
     *
     * @param name  имя параметра
     * @param value значение параметра
     * @return этот билдер для цепочки вызовов
     */
    public SelectBuilder param(String name, Object value) {
        parameters.put(name, value);
        return this;
    }

    // === GROUP BY / HAVING ===

    /**
     * Добавить GROUP BY.
     *
     * @param groupByColumns колонки для группировки
     * @return этот билдер для цепочки вызовов
     */
    public SelectBuilder groupBy(String... groupByColumns) {
        this.groupBy = String.join(", ", groupByColumns);
        return this;
    }

    /**
     * Добавить HAVING.
     *
     * @param havingCondition условие HAVING
     * @return этот билдер для цепочки вызовов
     */
    public SelectBuilder having(String havingCondition) {
        this.having = havingCondition;
        return this;
    }

    // === ORDER BY ===

    /**
     * Добавить ORDER BY (по возрастанию).
     *
     * @param column колонка для сортировки
     * @return этот билдер для цепочки вызовов
     */
    public SelectBuilder orderBy(String column) {
        this.orderBy = column;
        return this;
    }

    /**
     * Добавить ORDER BY с направлением сортировки.
     *
     * @param column колонка для сортировки
     * @param order  направление сортировки
     * @return этот билдер для цепочки вызовов
     */
    public SelectBuilder orderBy(String column, SortOrder order) {
        this.orderBy = column + " " + order.name();
        return this;
    }

    /**
     * Добавить ORDER BY с несколькими колонками.
     *
     * @param orderByClause полная ORDER BY строка (напр.: "name ASC, age DESC")
     * @return этот билдер для цепочки вызовов
     */
    public SelectBuilder orderByRaw(String orderByClause) {
        this.orderBy = orderByClause;
        return this;
    }

    // === LIMIT / OFFSET ===

    /**
     * Добавить LIMIT.
     *
     * @param limitValue максимальное количество строк
     * @return этот билдер для цепочки вызовов
     */
    public SelectBuilder limit(int limitValue) {
        this.limit = limitValue;
        return this;
    }

    /**
     * Добавить OFFSET.
     *
     * @param offsetValue количество строк для пропуска
     * @return этот билдер для цепочки вызовов
     */
    public SelectBuilder offset(int offsetValue) {
        this.offset = offsetValue;
        return this;
    }

    // === Build ===

    @Override
    public Query build() {
        if (table == null || table.isBlank()) {
            throw new DbException("Требуется FROM для SELECT-запроса");
        }

        StringBuilder sql = new StringBuilder();
        
        // SELECT columns
        sql.append("SELECT ");
        sql.append(String.join(", ", columns));
        
        // FROM table
        sql.append(" FROM ");
        sql.append(table);
        
        // JOINs
        for (String join : joins) {
            sql.append(" ").append(join);
        }
        
        // WHERE
        if (!whereClauses.isEmpty()) {
            sql.append(" WHERE ");
            for (int i = 0; i < whereClauses.size(); i++) {
                if (i > 0) {
                    sql.append(" ").append(whereClauses.get(i).connector()).append(" ");
                }
                sql.append(whereClauses.get(i).condition());
            }
        }
        
        // GROUP BY
        if (groupBy != null && !groupBy.isBlank()) {
            sql.append(" GROUP BY ").append(groupBy);
        }
        
        // HAVING
        if (having != null && !having.isBlank()) {
            sql.append(" HAVING ").append(having);
        }
        
        // ORDER BY
        if (orderBy != null && !orderBy.isBlank()) {
            sql.append(" ORDER BY ").append(orderBy);
        }
        
        // LIMIT
        if (limit != null) {
            sql.append(" LIMIT ").append(limit);
        }
        
        // OFFSET
        if (offset != null) {
            sql.append(" OFFSET ").append(offset);
        }

        return new SimpleQuery(sql.toString(), parameters, QueryType.SELECT);
    }

    /**
     * Сгенерировать уникальное имя параметра для колонки.
     */
    protected String generateParamName(String column) {
        // Убираем точки и спецсимволы, добавляем счётчик для уникальности
        String baseName = column.replaceAll("[^a-zA-Z0-9_]", "_");
        return baseName + "_" + paramCounter.incrementAndGet();
    }

    /**
     * Получить текущую карту параметров (для ExecutableSelectBuilder).
     */
    protected Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * Перечисление направлений сортировки.
     */
    public enum SortOrder {
        ASC,
        DESC
    }

    /**
     * Внутренний record для WHERE-условий.
     */
    private record WhereClause(String condition, String connector) {}
}
