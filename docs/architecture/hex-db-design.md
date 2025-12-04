## Архитектура модулей

```
hex-db-core          ← Базовые интерфейсы и типы (0 зависимостей)
    │
    ├── hex-db-builders   ← Fluent билдеры запросов
    │
    ├── hex-db-executor   ← Выполнение запросов
    │       │
    │       └── hex-db-hikari  ← HikariCP интеграция (опционально)
    │
    └── [Extensions - пользователь пишет сам или мы даём опционально]
            ├── hex-db-mapper      ← Автоматический маппинг на объекты
            ├── hex-db-validation  ← Валидация SQL
            └── hex-db-interceptors ← Интерцепторы
```

---

## 1. Core Module (`hex-db-core`)

Минимальные интерфейсы и типы:

```java
package com.framework.hex.db.core;

/**
 * Представляет строку результата запроса.
 * Простая обёртка над данными без магии.
 */
public interface Row {
    
    <T> T get(String column, Class<T> type);
    
    <T> T get(int index, Class<T> type);
    
    String getString(String column);
    Long getLong(String column);
    Integer getInt(String column);
    Boolean getBoolean(String column);
    BigDecimal getBigDecimal(String column);
    LocalDateTime getLocalDateTime(String column);
    LocalDate getLocalDate(String column);
    byte[] getBytes(String column);
    
    /** Получить как nullable */
    <T> Optional<T> getOptional(String column, Class<T> type);
    
    /** Все колонки в Map */
    Map<String, Object> toMap();
    
    /** Список имён колонок */
    List<String> columns();
    
    /** Проверить наличие колонки */
    boolean hasColumn(String column);
}
```

```java
package com.framework.hex.db.core;

/**
 * Маппер строки в объект.
 * Единственная точка расширения для кастомного маппинга.
 */
@FunctionalInterface
public interface RowMapper<T> {
    T map(Row row);
    
    /** Удобный маппер в Map */
    static RowMapper<Map<String, Object>> toMap() {
        return Row::toMap;
    }
}
```

```java
package com.framework.hex.db.core;

/**
 * Результат выполнения запроса.
 * Ленивый — данные читаются при вызове методов.
 */
public interface QueryResult extends AutoCloseable {
    
    // === Получение данных ===
    
    /** Все строки как List<Map> — самый простой случай */
    List<Map<String, Object>> toList();
    
    /** Все строки с маппером */
    <T> List<T> toList(RowMapper<T> mapper);
    
    /** Одна строка как Map (или пусто) */
    Optional<Map<String, Object>> firstRow();
    
    /** Одна строка с маппером */
    <T> Optional<T> firstRow(RowMapper<T> mapper);
    
    /** Единственное скалярное значение */
    <T> T scalar(Class<T> type);
    
    /** Скалярное или null */
    <T> Optional<T> scalarOptional(Class<T> type);
    
    /** Стрим строк для больших результатов */
    Stream<Row> stream();
    
    /** Affected rows для INSERT/UPDATE/DELETE */
    int affectedRows();
    
    /** Generated keys после INSERT */
    List<Object> generatedKeys();
    
    /** Первый сгенерированный ключ */
    <T> T generatedKey(Class<T> type);
}
```

```java
package com.framework.hex.db.core;

/**
 * Представление запроса — что выполнять.
 */
public interface Query {
    
    /** SQL с плейсхолдерами :name */
    String sql();
    
    /** Параметры */
    Map<String, Object> parameters();
    
    /** Тип запроса */
    QueryType type();
    
    /** Нужны ли сгенерированные ключи */
    boolean returnGeneratedKeys();
    
    enum QueryType {
        SELECT, INSERT, UPDATE, DELETE, CALL, SCRIPT
    }
}
```

```java
package com.framework.hex.db.core;

/**
 * Исполнитель запросов — центральный компонент.
 */
public interface QueryExecutor {
    
    /** Выполнить запрос */
    QueryResult execute(Query query);
    
    /** Выполнить сырой SQL с параметрами */
    QueryResult execute(String sql, Map<String, Object> params);
    
    /** Выполнить сырой SQL без параметров */
    QueryResult execute(String sql);
    
    /** Batch выполнение */
    int[] executeBatch(String sql, List<Map<String, Object>> paramsList);
    
    /** Получить соединение напрямую (escape hatch) */
    Connection getConnection();
    
    /** Вернуть соединение */
    void releaseConnection(Connection connection);
}
```

```java
package com.framework.hex.db.core;

/**
 * Провайдер DataSource — точка расширения для разных пулов.
 */
public interface DataSourceProvider {
    
    DataSource getDataSource(String name);
    
    DataSource getDefaultDataSource();
    
    void register(String name, DataSource dataSource);
    
    void shutdown();
}
```

---

## 2. Builders Module (`hex-db-builders`)

Fluent API для построения запросов:

```java
package com.framework.hex.db.builders;

/**
 * Точка входа для создания запросов.
 */
public final class SQL {
    
    private SQL() {}
    
    public static SelectBuilder select(String... columns) {
        return new SelectBuilder(columns);
    }
    
    public static SelectBuilder selectAll() {
        return new SelectBuilder("*");
    }
    
    public static InsertBuilder insertInto(String table) {
        return new InsertBuilder(table);
    }
    
    public static UpdateBuilder update(String table) {
        return new UpdateBuilder(table);
    }
    
    public static DeleteBuilder deleteFrom(String table) {
        return new DeleteBuilder(table);
    }
    
    /** Сырой SQL для сложных случаев */
    public static RawQueryBuilder raw(String sql) {
        return new RawQueryBuilder(sql);
    }
}
```

```java
package com.framework.hex.db.builders;

/**
 * Билдер SELECT запросов.
 * 
 * <p><b>Потокобезопасность:</b> Билдеры НЕ являются потокобезопасными и не должны
 * использоваться совместно между потоками. Каждый поток должен создавать свой экземпляр билдера.
 * 
 * <p><b>Паттерн использования:</b>
 * <pre>{@code
 * // ✅ Правильно — создаём builder в каждом потоке
 * List<User> users = Db.selectAll().from("users").toList(mapper);
 * 
 * // ❌ Неправильно — не делайте так!
 * SelectBuilder shared = Db.selectAll().from("users");
 * // Thread 1: shared.where("a", 1).toList();
 * // Thread 2: shared.where("b", 2).toList();  // Гонка!
 * }</pre>
 */
public class SelectBuilder implements QueryBuilder {
    
    private final List<String> columns = new ArrayList<>();
    private String table;
    private final List<String> joins = new ArrayList<>();
    private final List<WhereClause> whereClauses = new ArrayList<>();
    private final Map<String, Object> parameters = new LinkedHashMap<>();
    private String groupBy;
    private String having;
    private String orderBy;
    private Integer limit;
    private Integer offset;
    
    SelectBuilder(String... columns) {
        this.columns.addAll(Arrays.asList(columns));
    }
    
    // === FROM ===
    
    public SelectBuilder from(String table) {
        this.table = table;
        return this;
    }
    
    // === JOINs ===
    
    public SelectBuilder join(String table, String on) {
        joins.add("JOIN " + table + " ON " + on);
        return this;
    }
    
    public SelectBuilder leftJoin(String table, String on) {
        joins.add("LEFT JOIN " + table + " ON " + on);
        return this;
    }
    
    public SelectBuilder rightJoin(String table, String on) {
        joins.add("RIGHT JOIN " + table + " ON " + on);
        return this;
    }
    
    // === WHERE ===
    
    /**
     * Добавить условие WHERE column = value.
     * 
     * <p><b>NULL handling:</b> Если value == null, автоматически 
     * использует IS NULL вместо = NULL (который всегда false в SQL).
     */
    public SelectBuilder where(String column, Object value) {
        if (value == null) {
            return whereNull(column);
        }
        return where(column, "=", value);
    }
    
    /**
     * Добавить условие WHERE с явным оператором.
     * Для null значений используйте whereNull() / whereNotNull().
     */
    public SelectBuilder where(String column, String operator, Object value) {
        String paramName = generateParamName(column);
        whereClauses.add(new WhereClause(column + " " + operator + " :" + paramName, "AND"));
        parameters.put(paramName, value);
        return this;
    }
    
    public SelectBuilder whereRaw(String condition) {
        whereClauses.add(new WhereClause(condition, "AND"));
        return this;
    }
    
    public SelectBuilder whereIn(String column, Collection<?> values) {
        String paramName = generateParamName(column);
        whereClauses.add(new WhereClause(column + " IN (:" + paramName + ")", "AND"));
        parameters.put(paramName, values);
        return this;
    }
    
    public SelectBuilder whereNull(String column) {
        whereClauses.add(new WhereClause(column + " IS NULL", "AND"));
        return this;
    }
    
    public SelectBuilder whereNotNull(String column) {
        whereClauses.add(new WhereClause(column + " IS NOT NULL", "AND"));
        return this;
    }
    
    public SelectBuilder whereBetween(String column, Object from, Object to) {
        String paramFrom = generateParamName(column + "_from");
        String paramTo = generateParamName(column + "_to");
        whereClauses.add(new WhereClause(
            column + " BETWEEN :" + paramFrom + " AND :" + paramTo, "AND"));
        parameters.put(paramFrom, from);
        parameters.put(paramTo, to);
        return this;
    }
    
    public SelectBuilder and(String column, Object value) {
        return where(column, value);
    }
    
    public SelectBuilder and(String column, String operator, Object value) {
        return where(column, operator, value);
    }
    
    public SelectBuilder or(String column, Object value) {
        return or(column, "=", value);
    }
    
    public SelectBuilder or(String column, String operator, Object value) {
        String paramName = generateParamName(column);
        whereClauses.add(new WhereClause(column + " " + operator + " :" + paramName, "OR"));
        parameters.put(paramName, value);
        return this;
    }
    
    // === Условная сборка (очень полезно для тестов!) ===
    
    public SelectBuilder whereIf(boolean condition, String column, Object value) {
        if (condition) {
            where(column, value);
        }
        return this;
    }
    
    public SelectBuilder whereIf(boolean condition, 
                                  Consumer<SelectBuilder> builderConsumer) {
        if (condition) {
            builderConsumer.accept(this);
        }
        return this;
    }
    
    // === GROUP BY / HAVING / ORDER BY ===
    
    public SelectBuilder groupBy(String... columns) {
        this.groupBy = String.join(", ", columns);
        return this;
    }
    
    public SelectBuilder having(String condition) {
        this.having = condition;
        return this;
    }
    
    public SelectBuilder orderBy(String column) {
        this.orderBy = column;
        return this;
    }
    
    public SelectBuilder orderBy(String column, SortOrder order) {
        this.orderBy = column + " " + order.name();
        return this;
    }
    
    public SelectBuilder orderByDesc(String column) {
        return orderBy(column, SortOrder.DESC);
    }
    
    // === LIMIT / OFFSET ===
    
    public SelectBuilder limit(int limit) {
        this.limit = limit;
        return this;
    }
    
    public SelectBuilder offset(int offset) {
        this.offset = offset;
        return this;
    }
    
    // === Build ===
    
    @Override
    public Query build() {
        StringBuilder sql = new StringBuilder();
        
        // SELECT
        sql.append("SELECT ");
        sql.append(columns.isEmpty() ? "*" : String.join(", ", columns));
        
        // FROM
        sql.append(" FROM ").append(table);
        
        // JOINs
        for (String join : joins) {
            sql.append(" ").append(join);
        }
        
        // WHERE
        if (!whereClauses.isEmpty()) {
            sql.append(" WHERE ");
            for (int i = 0; i < whereClauses.size(); i++) {
                WhereClause clause = whereClauses.get(i);
                if (i > 0) {
                    sql.append(" ").append(clause.connector()).append(" ");
                }
                sql.append(clause.condition());
            }
        }
        
        // GROUP BY
        if (groupBy != null) {
            sql.append(" GROUP BY ").append(groupBy);
        }
        
        // HAVING
        if (having != null) {
            sql.append(" HAVING ").append(having);
        }
        
        // ORDER BY
        if (orderBy != null) {
            sql.append(" ORDER BY ").append(orderBy);
        }
        
        // LIMIT / OFFSET
        if (limit != null) {
            sql.append(" LIMIT ").append(limit);
        }
        if (offset != null) {
            sql.append(" OFFSET ").append(offset);
        }
        
        return new SimpleQuery(sql.toString(), parameters, QueryType.SELECT, false);
    }
    
    // === Добавление параметров вручную ===
    
    public SelectBuilder param(String name, Object value) {
        parameters.put(name, value);
        return this;
    }
    
    private String generateParamName(String column) {
        String base = column.replaceAll("[^a-zA-Z0-9]", "_");
        if (parameters.containsKey(base)) {
            int counter = 1;
            while (parameters.containsKey(base + "_" + counter)) {
                counter++;
            }
            return base + "_" + counter;
        }
        return base;
    }
    
    private record WhereClause(String condition, String connector) {}
    
    public enum SortOrder { ASC, DESC }
}
```

```java
package com.framework.hex.db.builders;

/**
 * Билдер INSERT запросов.
 * 
 * <p><b>Потокобезопасность:</b> НЕ потокобезопасен. Создавайте новый экземпляр для каждого потока.
 */
public class InsertBuilder implements QueryBuilder {
    
    private final String table;
    private final Map<String, Object> values = new LinkedHashMap<>();
    private boolean returnKeys = false;
    
    InsertBuilder(String table) {
        this.table = table;
    }
    
    public InsertBuilder value(String column, Object value) {
        values.put(column, value);
        return this;
    }
    
    public InsertBuilder values(Map<String, Object> values) {
        this.values.putAll(values);
        return this;
    }
    
    /** Удобно для тестов — добавить значение если не null */
    public InsertBuilder valueIfNotNull(String column, Object value) {
        if (value != null) {
            values.put(column, value);
        }
        return this;
    }
    
    public InsertBuilder returningKeys() {
        this.returnKeys = true;
        return this;
    }
    
    @Override
    public Query build() {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(table).append(" (");
        sql.append(String.join(", ", values.keySet()));
        sql.append(") VALUES (");
        sql.append(values.keySet().stream()
            .map(k -> ":" + k)
            .collect(Collectors.joining(", ")));
        sql.append(")");
        
        return new SimpleQuery(sql.toString(), values, QueryType.INSERT, returnKeys);
    }
}
```

```java
package com.framework.hex.db.builders;

/**
 * Билдер UPDATE запросов.
 * 
 * <p><b>Потокобезопасность:</b> НЕ потокобезопасен. Создавайте новый экземпляр для каждого потока.
 */
public class UpdateBuilder implements QueryBuilder {
    
    private final String table;
    private final Map<String, Object> setValues = new LinkedHashMap<>();
    private final List<String> whereClauses = new ArrayList<>();
    private final Map<String, Object> whereParams = new LinkedHashMap<>();
    
    UpdateBuilder(String table) {
        this.table = table;
    }
    
    public UpdateBuilder set(String column, Object value) {
        setValues.put(column, value);
        return this;
    }
    
    public UpdateBuilder setAll(Map<String, Object> values) {
        setValues.putAll(values);
        return this;
    }
    
    /** SET column = column + value */
    public UpdateBuilder increment(String column, Number value) {
        String paramName = column + "_inc";
        setValues.put(column, new RawExpression(column + " + :" + paramName));
        whereParams.put(paramName, value);
        return this;
    }
    
    /**
     * Добавить условие WHERE.
     * Если value == null, использует IS NULL.
     */
    public UpdateBuilder where(String column, Object value) {
        if (value == null) {
            whereClauses.add(column + " IS NULL");
            return this;
        }
        String paramName = "w_" + column;
        whereClauses.add(column + " = :" + paramName);
        whereParams.put(paramName, value);
        return this;
    }
    
    public UpdateBuilder whereIn(String column, Collection<?> values) {
        String paramName = "w_" + column;
        whereClauses.add(column + " IN (:" + paramName + ")");
        whereParams.put(paramName, values);
        return this;
    }
    
    @Override
    public Query build() {
        if (whereClauses.isEmpty()) {
            throw new IllegalStateException(
                "UPDATE без WHERE опасен. Используйте whereRaw(\"1=1\") если это намеренно.");
        }
        
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(table).append(" SET ");
        
        List<String> setClauses = new ArrayList<>();
        for (Map.Entry<String, Object> entry : setValues.entrySet()) {
            if (entry.getValue() instanceof RawExpression raw) {
                setClauses.add(entry.getKey() + " = " + raw.expression());
            } else {
                setClauses.add(entry.getKey() + " = :" + entry.getKey());
            }
        }
        sql.append(String.join(", ", setClauses));
        
        sql.append(" WHERE ");
        sql.append(String.join(" AND ", whereClauses));
        
        Map<String, Object> allParams = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : setValues.entrySet()) {
            if (!(e.getValue() instanceof RawExpression)) {
                allParams.put(e.getKey(), e.getValue());
            }
        }
        allParams.putAll(whereParams);
        
        return new SimpleQuery(sql.toString(), allParams, QueryType.UPDATE, false);
    }
    
    private record RawExpression(String expression) {}
}
```

```java
package com.framework.hex.db.builders;

/**
 * Билдер DELETE запросов.
 * 
 * <p><b>Потокобезопасность:</b> НЕ потокобезопасен. Создавайте новый экземпляр для каждого потока.
 */
public class DeleteBuilder implements QueryBuilder {
    
    private final String table;
    private final List<String> whereClauses = new ArrayList<>();
    private final Map<String, Object> parameters = new LinkedHashMap<>();
    
    DeleteBuilder(String table) {
        this.table = table;
    }
    
    public DeleteBuilder where(String column, Object value) {
        if (value == null) {
            whereClauses.add(column + " IS NULL");
            return this;
        }
        String paramName = column;
        whereClauses.add(column + " = :" + paramName);
        parameters.put(paramName, value);
        return this;
    }
    
    public DeleteBuilder whereIn(String column, Collection<?> values) {
        whereClauses.add(column + " IN (:" + column + ")");
        parameters.put(column, values);
        return this;
    }
    
    /** Явное разрешение DELETE без WHERE */
    public DeleteBuilder all() {
        whereClauses.add("1=1");
        return this;
    }
    
    @Override
    public Query build() {
        if (whereClauses.isEmpty()) {
            throw new IllegalStateException(
                "DELETE без WHERE опасен. Используйте all() если это намеренно.");
        }
        
        StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM ").append(table);
        sql.append(" WHERE ");
        sql.append(String.join(" AND ", whereClauses));
        
        return new SimpleQuery(sql.toString(), parameters, QueryType.DELETE, false);
    }
}
```

```java
package com.framework.hex.db.builders;

/**
 * Билдер для batch INSERT запросов.
 * Позволяет эффективно вставлять множество строк одним запросом.
 * 
 * <p><b>Потокобезопасность:</b> НЕ потокобезопасен. Создавайте новый экземпляр для каждого потока.
 * 
 * <p>Пример использования:
 * <pre>{@code
 * Db.batchInsertInto("users")
 *     .columns("name", "email", "active")
 *     .row("John", "john@test.com", true)
 *     .row("Jane", "jane@test.com", true)
 *     .row("Bob", "bob@test.com", false)
 *     .execute();
 * }</pre>
 */
public class BatchInsertBuilder {
    
    private final String table;
    private final List<String> columns = new ArrayList<>();
    private final List<Map<String, Object>> rows = new ArrayList<>();
    
    public BatchInsertBuilder(String table) {
        this.table = table;
    }
    
    /**
     * Определить колонки для вставки.
     * Должен быть вызван перед добавлением строк.
     */
    public BatchInsertBuilder columns(String... cols) {
        columns.addAll(Arrays.asList(cols));
        return this;
    }
    
    /**
     * Добавить строку со значениями в порядке колонок.
     * 
     * @throws IllegalArgumentException если количество значений не совпадает с количеством колонок
     */
    public BatchInsertBuilder row(Object... values) {
        if (columns.isEmpty()) {
            throw new IllegalStateException("Вызовите columns() перед добавлением строк");
        }
        if (values.length != columns.size()) {
            throw new IllegalArgumentException(
                "Количество значений (" + values.length + ") должно совпадать с количеством колонок (" + columns.size() + ")");
        }
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            row.put(columns.get(i), values[i]);
        }
        rows.add(row);
        return this;
    }
    
    /**
     * Добавить строку как Map.
     */
    public BatchInsertBuilder row(Map<String, Object> row) {
        if (columns.isEmpty()) {
            // Если колонки не заданы — берём из первой строки
            columns.addAll(row.keySet());
        }
        rows.add(new LinkedHashMap<>(row));
        return this;
    }
    
    /**
     * Добавить множество строк.
     */
    public BatchInsertBuilder rows(List<Map<String, Object>> rows) {
        for (Map<String, Object> row : rows) {
            row(row);
        }
        return this;
    }
    
    /**
     * Построить SQL для batch insert.
     */
    public String buildSql() {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(table).append(" (");
        sql.append(String.join(", ", columns));
        sql.append(") VALUES (");
        sql.append(columns.stream().map(c -> ":" + c).collect(Collectors.joining(", ")));
        sql.append(")");
        return sql.toString();
    }
    
    /**
     * Получить список параметров для каждой строки.
     */
    public List<Map<String, Object>> getRowsParams() {
        return Collections.unmodifiableList(rows);
    }
    
    /**
     * Количество строк для вставки.
     */
    public int size() {
        return rows.size();
    }
}
```

```java
package com.framework.hex.db.builders;

/**
 * Билдер для сырого SQL.
 * 
 * <p><b>Потокобезопасность:</b> НЕ потокобезопасен. Создавайте новый экземпляр для каждого потока.
 */
public class RawQueryBuilder implements QueryBuilder {
    
    private final String sql;
    private final Map<String, Object> parameters = new LinkedHashMap<>();
    private QueryType type = QueryType.SELECT;
    private boolean returnKeys = false;
    
    RawQueryBuilder(String sql) {
        this.sql = sql;
        this.type = detectType(sql);
    }
    
    public RawQueryBuilder param(String name, Object value) {
        parameters.put(name, value);
        return this;
    }
    
    public RawQueryBuilder params(Map<String, Object> params) {
        parameters.putAll(params);
        return this;
    }
    
    public RawQueryBuilder asInsert() {
        this.type = QueryType.INSERT;
        return this;
    }
    
    public RawQueryBuilder asUpdate() {
        this.type = QueryType.UPDATE;
        return this;
    }
    
    public RawQueryBuilder returningKeys() {
        this.returnKeys = true;
        return this;
    }
    
    @Override
    public Query build() {
        return new SimpleQuery(sql, parameters, type, returnKeys);
    }
    
    private QueryType detectType(String sql) {
        String upper = sql.trim().toUpperCase();
        if (upper.startsWith("SELECT")) return QueryType.SELECT;
        if (upper.startsWith("INSERT")) return QueryType.INSERT;
        if (upper.startsWith("UPDATE")) return QueryType.UPDATE;
        if (upper.startsWith("DELETE")) return QueryType.DELETE;
        if (upper.startsWith("CALL")) return QueryType.CALL;
        return QueryType.SELECT;
    }
}
```

```java
package com.framework.hex.db.builders;

/**
 * Парсер SQL скриптов для разбиения на отдельные statements.
 */
class SqlScriptParser {
    
    /**
     * Разбить SQL скрипт на отдельные statements.
     * Учитывает строковые литералы и комментарии.
     */
    static List<String> splitStatements(String script) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inString = false;
        boolean inLineComment = false;
        boolean inBlockComment = false;
        char stringChar = 0;
        
        for (int i = 0; i < script.length(); i++) {
            char c = script.charAt(i);
            char next = (i + 1 < script.length()) ? script.charAt(i + 1) : 0;
            
            // Обработка комментариев
            if (!inString && !inBlockComment && c == '-' && next == '-') {
                inLineComment = true;
            }
            if (inLineComment && (c == '\n' || c == '\r')) {
                inLineComment = false;
            }
            if (!inString && !inLineComment && c == '/' && next == '*') {
                inBlockComment = true;
            }
            if (inBlockComment && c == '*' && next == '/') {
                inBlockComment = false;
                current.append(c).append(next);
                i++;
                continue;
            }
            
            // Обработка строк
            if (!inLineComment && !inBlockComment) {
                if (!inString && (c == '\'' || c == '"')) {
                    inString = true;
                    stringChar = c;
                } else if (inString && c == stringChar) {
                    // Проверяем escape: ''
                    if (next == stringChar) {
                        current.append(c).append(next);
                        i++;
                        continue;
                    }
                    inString = false;
                }
            }
            
            // Разделитель statements
            if (!inString && !inLineComment && !inBlockComment && c == ';') {
                String stmt = current.toString().trim();
                if (!stmt.isEmpty()) {
                    statements.add(stmt);
                }
                current = new StringBuilder();
                continue;
            }
            
            current.append(c);
        }
        
        // Последний statement без ;
        String last = current.toString().trim();
        if (!last.isEmpty()) {
            statements.add(last);
        }
        
        return statements;
    }
}
```

---

## 3. Executor Module (`hex-db-executor`)

```java
package com.framework.hex.db.executor;

/**
 * Стандартная реализация QueryExecutor.
 * 
 * <p><b>Thread Safety:</b> Этот класс потокобезопасен.
 * Каждый запрос использует свой connection из пула.
 */
public class DefaultQueryExecutor implements QueryExecutor {
    
    private final DataSourceProvider dataSourceProvider;
    private final String dataSourceName;
    private final List<QueryInterceptor> interceptors;
    private final Supplier<Connection> transactionConnectionSupplier;
    
    public DefaultQueryExecutor(DataSourceProvider provider) {
        this(provider, "default", List.of(), () -> null);
    }
    
    public DefaultQueryExecutor(DataSourceProvider provider, 
                                 String dataSourceName,
                                 List<QueryInterceptor> interceptors,
                                 Supplier<Connection> transactionConnectionSupplier) {
        this.dataSourceProvider = provider;
        this.dataSourceName = dataSourceName;
        this.interceptors = List.copyOf(interceptors);  // Неизменяемая копия для потокобезопасности
        this.transactionConnectionSupplier = transactionConnectionSupplier;
    }
    
    @Override
    public QueryResult execute(Query query) {
        // Применяем интерцепторы
        Query processedQuery = applyInterceptors(query);
        Instant start = Instant.now();
        
        try {
            Connection conn = getConnectionInternal();
            boolean ownsConnection = !isInTransaction();
            
            return doExecute(conn, processedQuery, ownsConnection);
        } catch (SQLException e) {
            Duration duration = Duration.between(start, Instant.now());
            notifyError(query, e, duration);
            throw translateException(e, query);
        }
    }
    
    private boolean isInTransaction() {
        return transactionConnectionSupplier.get() != null;
    }
    
    private Connection getConnectionInternal() throws SQLException {
        // Если в транзакции — используем транзакционный connection
        Connection txConn = transactionConnectionSupplier.get();
        if (txConn != null) {
            return txConn;
        }
        return dataSourceProvider.getDataSource(dataSourceName).getConnection();
    }
    
    @Override
    public QueryResult execute(String sql, Map<String, Object> params) {
        return execute(new SimpleQuery(sql, params, detectType(sql), false));
    }
    
    @Override
    public QueryResult execute(String sql) {
        return execute(sql, Map.of());
    }
    
    private QueryResult doExecute(Connection conn, Query query, boolean ownsConnection) 
            throws SQLException {
        String sql = query.sql();
        Map<String, Object> params = query.parameters();
        
        // Конвертируем named params в positional
        ParsedQuery parsed = parseNamedParameters(sql, params);
        
        PreparedStatement stmt = query.returnGeneratedKeys()
            ? conn.prepareStatement(parsed.sql(), Statement.RETURN_GENERATED_KEYS)
            : conn.prepareStatement(parsed.sql());
        
        // Устанавливаем параметры
        setParameters(stmt, parsed.values());
        
        // Выполняем
        if (query.type() == QueryType.SELECT) {
            ResultSet rs = stmt.executeQuery();
            // Connection передаётся в результат — будет закрыт в close()
            // ownsConnection = false если мы в транзакции (connection не закрываем)
            return new ResultSetQueryResult(rs, stmt, ownsConnection ? conn : null);
        } else {
            int affected = stmt.executeUpdate();
            List<Object> keys = query.returnGeneratedKeys() 
                ? extractGeneratedKeys(stmt) 
                : List.of();
            stmt.close();
            // Закрываем connection только если он наш (не транзакционный)
            if (ownsConnection) {
                conn.close();
            }
            return new ModificationQueryResult(affected, keys);
        }
    }
    
    private ParsedQuery parseNamedParameters(String sql, Map<String, Object> params) {
        // :paramName -> ? и собираем значения в правильном порядке
        StringBuilder result = new StringBuilder();
        List<Object> values = new ArrayList<>();
        
        Pattern pattern = Pattern.compile(":(\\w+)");
        Matcher matcher = pattern.matcher(sql);
        
        while (matcher.find()) {
            String paramName = matcher.group(1);
            Object value = params.get(paramName);
            
            if (value instanceof Collection<?> collection) {
                // IN clause: :ids -> ?, ?, ?
                String placeholders = collection.stream()
                    .map(v -> "?")
                    .collect(Collectors.joining(", "));
                matcher.appendReplacement(result, placeholders);
                values.addAll(collection);
            } else {
                matcher.appendReplacement(result, "?");
                values.add(value);
            }
        }
        matcher.appendTail(result);
        
        return new ParsedQuery(result.toString(), values);
    }
    
    private void setParameters(PreparedStatement stmt, List<Object> values) 
            throws SQLException {
        for (int i = 0; i < values.size(); i++) {
            Object value = values.get(i);
            int paramIndex = i + 1;
            
            if (value == null) {
                stmt.setNull(paramIndex, Types.NULL);
            } else if (value instanceof String s) {
                stmt.setString(paramIndex, s);
            } else if (value instanceof Long l) {
                stmt.setLong(paramIndex, l);
            } else if (value instanceof Integer n) {
                stmt.setInt(paramIndex, n);
            } else if (value instanceof Boolean b) {
                stmt.setBoolean(paramIndex, b);
            } else if (value instanceof BigDecimal bd) {
                stmt.setBigDecimal(paramIndex, bd);
            } else if (value instanceof LocalDateTime ldt) {
                stmt.setTimestamp(paramIndex, Timestamp.valueOf(ldt));
            } else if (value instanceof LocalDate ld) {
                stmt.setDate(paramIndex, Date.valueOf(ld));
            } else if (value instanceof byte[] bytes) {
                stmt.setBytes(paramIndex, bytes);
            } else if (value instanceof Enum<?> e) {
                stmt.setString(paramIndex, e.name());
            } else {
                stmt.setObject(paramIndex, value);
            }
        }
    }
    
    @Override
    public Connection getConnection() {
        try {
            return dataSourceProvider.getDataSource(dataSourceName).getConnection();
        } catch (SQLException e) {
            throw new DbException("Не удалось получить соединение", e);
        }
    }
    
    @Override
    public void releaseConnection(Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            // Логируем, но не бросаем исключение
        }
    }
    
    private record ParsedQuery(String sql, List<Object> values) {}
}
```

```java
package com.framework.hex.db.executor;

/**
 * Результат SELECT запроса.
 * 
 * <p><b>Управление ресурсами:</b> Этот класс держит открытые ресурсы БД
 * (ResultSet, Statement, Connection). Все терминальные операции (toList, 
 * firstRow, scalar) автоматически закрывают ресурсы после чтения.
 * 
 * <p>При использовании stream() ресурсы НЕ закрываются автоматически —
 * используйте try-with-resources!
 */
public class ResultSetQueryResult implements QueryResult {
    
    private static final Logger log = LoggerFactory.getLogger(ResultSetQueryResult.class);
    
    private final ResultSet resultSet;
    private final Statement statement;
    private final Connection connection;  // null если connection управляется транзакцией
    private final long createdAt = System.nanoTime();
    private ResultSetMetaData metadata;
    private List<String> columnNames;
    private volatile boolean closed = false;
    
    // Cleaner для safety-net от утечки ресурсов
    private static final Cleaner CLEANER = Cleaner.create();
    private final Cleaner.Cleanable cleanable;
    
    ResultSetQueryResult(ResultSet rs, Statement stmt, Connection conn) {
        this.resultSet = rs;
        this.statement = stmt;
        this.connection = conn;
        
        // Safety-net: если пользователь забыл закрыть stream, 
        // Cleaner закроет ресурсы и залогирует предупреждение
        CleanupAction cleanup = new CleanupAction(rs, stmt, conn, createdAt);
        this.cleanable = CLEANER.register(this, cleanup);
    }
    
    /**
     * Действие очистки для Cleaner.
     * Должен быть static чтобы не держать ссылку на ResultSetQueryResult.
     */
    private static class CleanupAction implements Runnable {
        private final ResultSet rs;
        private final Statement stmt;
        private final Connection conn;
        private final long createdAt;
        
        CleanupAction(ResultSet rs, Statement stmt, Connection conn, long createdAt) {
            this.rs = rs;
            this.stmt = stmt;
            this.conn = conn;
            this.createdAt = createdAt;
        }
        
        @Override
        public void run() {
            long aliveMs = (System.nanoTime() - createdAt) / 1_000_000;
            log.warn("⚠️ ОБНАРУЖЕНА УТЕЧКА РЕСУРСОВ! ResultSetQueryResult не был закрыт. " +
                     "Жил {} мс. Автозакрытие сейчас. " +
                     "Используйте try-with-resources с stream() или терминальные операции наподобие toList().",
                     aliveMs);
            closeQuietly(rs);
            closeQuietly(stmt);
            closeQuietly(conn);
        }
        
        private void closeQuietly(AutoCloseable resource) {
            if (resource != null) {
                try { resource.close(); } catch (Exception ignored) {}
            }
        }
    }
    
    @Override
    public List<Map<String, Object>> toList() {
        return toList(Row::toMap);
    }
    
    @Override
    public <T> List<T> toList(RowMapper<T> mapper) {
        List<T> results = new ArrayList<>();
        try {
            ensureMetadata();
            while (resultSet.next()) {
                Row row = new ResultSetRow(resultSet, columnNames);
                results.add(mapper.map(row));
            }
            return results;
        } catch (SQLException e) {
            throw new DbException("Не удалось прочитать результаты", e);
        } finally {
            close();
        }
    }
    
    @Override
    public Optional<Map<String, Object>> firstRow() {
        return firstRow(Row::toMap);
    }
    
    @Override
    public <T> Optional<T> firstRow(RowMapper<T> mapper) {
        try {
            ensureMetadata();
            if (resultSet.next()) {
                Row row = new ResultSetRow(resultSet, columnNames);
                return Optional.of(mapper.map(row));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DbException("Не удалось прочитать первую строку", e);
        } finally {
            close();
        }
    }
    
    @Override
    public <T> T scalar(Class<T> type) {
        return scalarOptional(type)
            .orElseThrow(() -> new DbException("Ожидался скалярный результат, но получено пусто"));
    }
    
    @Override
    public <T> Optional<T> scalarOptional(Class<T> type) {
        try {
            if (resultSet.next()) {
                Object value = resultSet.getObject(1);
                return Optional.ofNullable(convertValue(value, type));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DbException("Не удалось прочитать скалярное значение", e);
        } finally {
            close();
        }
    }
    
    /**
     * Стрим строк для больших результатов.
     * 
     * <p><b>⚠️ ВАЖНО:</b> Возвращаемый Stream ДОЛЖЕН быть закрыт для освобождения
     * ресурсов БД. Используйте try-with-resources:
     * 
     * <pre>{@code
     * try (Stream<Row> rows = result.stream()) {
     *     rows.filter(...).forEach(...);
     * }
     * }</pre>
     * 
     * <p>Или используйте терминальные операции с автозакрытием (рекомендуется):
     * <pre>{@code
     * result.toList(mapper);     // ✅ автозакрытие
     * result.firstRow(mapper);   // ✅ автозакрытие
     * }</pre>
     */
    @Override
    public Stream<Row> stream() {
        ensureMetadata();
        Iterator<Row> iterator = new Iterator<>() {
            private Boolean hasNext = null;
            
            @Override
            public boolean hasNext() {
                if (hasNext == null) {
                    try {
                        hasNext = resultSet.next();
                    } catch (SQLException e) {
                        throw new DbException("Ошибка при итерации", e);
                    }
                }
                return hasNext;
            }
            
            @Override
            public Row next() {
                if (!hasNext()) throw new NoSuchElementException();
                hasNext = null;
                return new ResultSetRow(resultSet, columnNames);
            }
        };
        
        return StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false)
            .onClose(this::close);
    }
    
    @Override
    public int affectedRows() {
        throw new UnsupportedOperationException("SELECT не имеет затронутых строк");
    }
    
    @Override
    public List<Object> generatedKeys() {
        return List.of();
    }
    
    @Override
    public <T> T generatedKey(Class<T> type) {
        throw new UnsupportedOperationException("SELECT не имеет сгенерированных ключей");
    }
    
    @Override
    public void close() {
        if (closed) {
            return;  // Идемпотентность
        }
        closed = true;
        
        // Отменяем Cleaner — мы сами закрыли ресурсы корректно
        cleanable.clean();
        
        try {
            resultSet.close();
        } catch (SQLException e) {
            // Логируем, но не бросаем
        }
        try {
            statement.close();
        } catch (SQLException e) {
            // Логируем, но не бросаем
        }
        // Закрываем connection только если он наш (не транзакционный)
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // Логируем, но не бросаем
            }
        }
    }
    
    private void ensureMetadata() {
        if (metadata == null) {
            try {
                metadata = resultSet.getMetaData();
                columnNames = new ArrayList<>();
                for (int i = 1; i <= metadata.getColumnCount(); i++) {
                    columnNames.add(metadata.getColumnLabel(i).toLowerCase());
                }
            } catch (SQLException e) {
                throw new DbException("Не удалось прочитать метаданные", e);
            }
        }
    }
}
```

```java
package com.framework.hex.db.executor;

/**
 * Реализация Row для ResultSet.
 */
public class ResultSetRow implements Row {
    
    private final ResultSet rs;
    private final List<String> columns;
    
    ResultSetRow(ResultSet rs, List<String> columns) {
        this.rs = rs;
        this.columns = columns;
    }
    
    @Override
    public <T> T get(String column, Class<T> type) {
        try {
            Object value = rs.getObject(column);
            return convertValue(value, type);
        } catch (SQLException e) {
            throw new DbException("Не удалось прочитать колонку: " + column, e);
        }
    }
    
    @Override
    public <T> T get(int index, Class<T> type) {
        try {
            Object value = rs.getObject(index + 1);
            return convertValue(value, type);
        } catch (SQLException e) {
            throw new DbException("Не удалось прочитать колонку по индексу: " + index, e);
        }
    }
    
    @Override
    public String getString(String column) {
        return get(column, String.class);
    }
    
    @Override
    public Long getLong(String column) {
        return get(column, Long.class);
    }
    
    @Override
    public Integer getInt(String column) {
        return get(column, Integer.class);
    }
    
    @Override
    public Boolean getBoolean(String column) {
        return get(column, Boolean.class);
    }
    
    @Override
    public BigDecimal getBigDecimal(String column) {
        return get(column, BigDecimal.class);
    }
    
    @Override
    public LocalDateTime getLocalDateTime(String column) {
        try {
            Timestamp ts = rs.getTimestamp(column);
            return ts != null ? ts.toLocalDateTime() : null;
        } catch (SQLException e) {
            throw new DbException("Не удалось прочитать timestamp: " + column, e);
        }
    }
    
    @Override
    public LocalDate getLocalDate(String column) {
        try {
            Date date = rs.getDate(column);
            return date != null ? date.toLocalDate() : null;
        } catch (SQLException e) {
            throw new DbException("Не удалось прочитать дату: " + column, e);
        }
    }
    
    @Override
    public byte[] getBytes(String column) {
        try {
            return rs.getBytes(column);
        } catch (SQLException e) {
            throw new DbException("Не удалось прочитать байты: " + column, e);
        }
    }
    
    @Override
    public <T> Optional<T> getOptional(String column, Class<T> type) {
        return Optional.ofNullable(get(column, type));
    }
    
    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        for (String col : columns) {
            try {
                map.put(col, rs.getObject(col));
            } catch (SQLException e) {
                throw new DbException("Failed to read column: " + col, e);
            }
        }
        return map;
    }
    
    @Override
    public List<String> columns() {
        return Collections.unmodifiableList(columns);
    }
    
    @Override
    public boolean hasColumn(String column) {
        return columns.contains(column.toLowerCase());
    }
    
    @SuppressWarnings("unchecked")
    private <T> T convertValue(Object value, Class<T> type) {
        if (value == null) return null;
        if (type.isInstance(value)) return type.cast(value);
        
        // Базовые преобразования
        if (type == Long.class && value instanceof Number n) {
            return (T) Long.valueOf(n.longValue());
        }
        if (type == Integer.class && value instanceof Number n) {
            return (T) Integer.valueOf(n.intValue());
        }
        if (type == String.class) {
            return (T) value.toString();
        }
        if (type == Boolean.class) {
            if (value instanceof Number n) return (T) Boolean.valueOf(n.intValue() != 0);
            if (value instanceof String s) return (T) Boolean.valueOf(s);
        }
        
        return type.cast(value);
    }
}
```

---

## 4. Главный Entry Point — класс `Db`

```java
package com.framework.hex.db;

import com.framework.hex.db.builders.*;
import com.framework.hex.db.core.*;
import com.framework.hex.db.executor.*;

/**
 * Главная точка входа в DB модуль.
 * 
 * <p><b>Thread Safety:</b> Этот класс полностью потокобезопасен. 
 * Все операции с instance используют AtomicReference для lock-free доступа.
 * Однако builders, возвращаемые методами select/insert/update/delete, 
 * НЕ являются потокобезопасными — каждый поток должен создавать свой builder.
 * 
 * <p>Пример использования:
 * <pre>{@code
 * // Простой запрос
 * List<Map<String, Object>> users = Db.selectAll()
 *     .from("users")
 *     .where("active", true)
 *     .toList();
 * 
 * // С маппингом
 * List<User> users = Db.selectAll()
 *     .from("users")
 *     .toList(row -> new User(row.getLong("id"), row.getString("name")));
 * 
 * // Insert
 * Long id = Db.insertInto("users")
 *     .value("name", "John")
 *     .value("email", "john@example.com")
 *     .returningKeys()
 *     .executeAndGetKey(Long.class);
 * 
 * // Транзакция
 * Db.transaction(() -> {
 *     Db.insertInto("orders").value("user_id", userId).execute();
 *     Db.update("users").set("order_count", orderCount + 1).where("id", userId).execute();
 * });
 * }</pre>
 */
public final class Db {
    
    // Thread-safe instance management с AtomicReference
    private static final AtomicReference<DbInstance> INSTANCE = new AtomicReference<>();
    
    // Реестр именованных DataSource для multi-datasource сценариев
    private static final ConcurrentHashMap<String, DbInstance> NAMED_INSTANCES = new ConcurrentHashMap<>();
    
    private Db() {}
    
    // === Конфигурация ===
    
    /**
     * Инициализация с DataSource.
     */
    public static void configure(DataSource dataSource) {
        configure(DbConfig.builder().dataSource(dataSource).build());
    }
    
    /**
     * Инициализация с полной конфигурацией.
     * 
     * <p><b>Thread Safety:</b> Этот метод потокобезопасен. При повторном вызове
     * старый instance НЕ закрывается автоматически — используйте shutdown() 
     * перед переконфигурацией, если нужно освободить ресурсы.
     */
    public static void configure(DbConfig config) {
        DbInstance newInstance = new DbInstance(config);
        INSTANCE.set(newInstance);
    }
    
    /**
     * Создать отдельный экземпляр (для multi-datasource).
     * @deprecated Используйте {@link #register(String, DbConfig)} и {@link #use(String)}
     */
    @Deprecated
    public static DbInstance create(DbConfig config) {
        return new DbInstance(config);
    }
    
    // === Named DataSources Registry ===
    
    /**
     * Зарегистрировать именованный DataSource.
     * Удобно для работы с несколькими базами данных.
     * 
     * <pre>{@code
     * Db.register("primary", primaryConfig);
     * Db.register("analytics", analyticsConfig);
     * 
     * Db.use("primary").insertInto("users")...;
     * Db.use("analytics").selectAll().from("reports")...;
     * }</pre>
     */
    public static void register(String name, DbConfig config) {
        NAMED_INSTANCES.put(name, new DbInstance(config));
    }
    
    /**
     * Получить именованный instance.
     * 
     * @throws IllegalStateException если DataSource с таким именем не зарегистрирован
     */
    public static DbInstance use(String name) {
        DbInstance instance = NAMED_INSTANCES.get(name);
        if (instance == null) {
            throw new IllegalStateException("DataSource '" + name + "' не зарегистрирован. " +
                "Вызовите Db.register(\"" + name + "\", config) сначала.");
        }
        return instance;
    }
    
    // === Query Builders (статические методы делегируют в instance) ===
    
    public static SelectBuilder select(String... columns) {
        return instance().select(columns);
    }
    
    public static SelectBuilder selectAll() {
        return instance().selectAll();
    }
    
    public static InsertBuilder insertInto(String table) {
        return instance().insertInto(table);
    }
    
    public static BatchInsertBuilder batchInsertInto(String table) {
        return instance().batchInsertInto(table);
    }
    
    public static UpdateBuilder update(String table) {
        return instance().update(table);
    }
    
    public static DeleteBuilder deleteFrom(String table) {
        return instance().deleteFrom(table);
    }
    
    public static RawQueryBuilder raw(String sql) {
        return instance().raw(sql);
    }
    
    // === SQL from File ===
    
    /**
     * Выполнить SQL скрипт из файла.
     * Поддерживает несколько statements, разделённых ;
     * 
     * <pre>{@code
     * Db.executeScript(Path.of("src/test/resources/schema.sql"));
     * }</pre>
     */
    public static void executeScript(Path path) {
        instance().executeScript(path);
    }
    
    /**
     * Выполнить SQL скрипт из classpath ресурса.
     * 
     * <pre>{@code
     * Db.executeScript("db/schema.sql");
     * Db.executeScript("db/test-data.sql");
     * }</pre>
     */
    public static void executeScript(String classpathResource) {
        instance().executeScript(classpathResource);
    }
    
    /**
     * Загрузить SQL из файла для использования в запросе с параметрами.
     * 
     * <pre>{@code
     * List<Map<String, Object>> results = Db.fromFile(Path.of("queries/report.sql"))
     *     .param("startDate", startDate)
     *     .param("endDate", endDate)
     *     .toList();
     * }</pre>
     */
    public static RawQueryBuilder fromFile(Path path) {
        return instance().fromFile(path);
    }
    
    /**
     * Загрузить SQL из classpath ресурса для использования в запросе.
     * 
     * <pre>{@code
     * List<Map<String, Object>> results = Db.fromResource("queries/user-stats.sql")
     *     .param("minOrders", 10)
     *     .toList();
     * }</pre>
     */
    public static RawQueryBuilder fromResource(String classpathResource) {
        return instance().fromResource(classpathResource);
    }
    
    // === Transactions ===
    
    /**
     * Выполнить код в транзакции с возвратом значения.
     * Автоматический COMMIT при успехе, ROLLBACK при исключении.
     * 
     * <pre>{@code
     * Long orderId = Db.transaction(() -> {
     *     Long id = Db.insertInto("orders")
     *         .value("user_id", userId)
     *         .executeAndGetKey(Long.class);
     *     Db.update("users")
     *         .increment("order_count", 1)
     *         .where("id", userId)
     *         .execute();
     *     return id;
     * });
     * }</pre>
     */
    public static <T> T transaction(Supplier<T> action) {
        return instance().transaction(action);
    }
    
    /**
     * Выполнить код в транзакции без возврата значения.
     * 
     * <pre>{@code
     * Db.transaction(() -> {
     *     Db.insertInto("audit_log").value("action", "delete").execute();
     *     Db.deleteFrom("users").where("id", userId).execute();
     * });
     * }</pre>
     */
    public static void transaction(Runnable action) {
        transaction(() -> { action.run(); return null; });
    }
    
    // === Escape Hatch ===
    
    public static Connection getConnection() {
        return instance().getConnection();
    }
    
    public static QueryExecutor executor() {
        return instance().executor();
    }
    
    // === Lifecycle ===
    
    /**
     * Закрыть default instance и освободить ресурсы.
     */
    public static void shutdown() {
        DbInstance old = INSTANCE.getAndSet(null);
        if (old != null) {
            old.close();
        }
    }
    
    /**
     * Закрыть все instances (default + named) и освободить ресурсы.
     */
    public static void shutdownAll() {
        shutdown();
        NAMED_INSTANCES.values().forEach(DbInstance::close);
        NAMED_INSTANCES.clear();
    }
    
    private static DbInstance instance() {
        DbInstance inst = INSTANCE.get();
        if (inst == null) {
            throw new IllegalStateException(
                "Db не сконфигурирован. Вызовите Db.configure(dataSource) сначала.");
        }
        return inst;
    }
}
```

```java
package com.framework.hex.db;

/**
 * Экземпляр DB для работы с конкретным DataSource.
 * Используется для multi-datasource сценариев.
 * 
 * <p><b>Thread Safety:</b> Этот класс потокобезопасен.
 * Транзакции изолированы по потокам через ThreadLocal.
 */
public class DbInstance implements AutoCloseable {
    
    private final DbConfig config;
    private final DataSourceProvider dataSourceProvider;
    private final QueryExecutor executor;
    
    // ThreadLocal для connection в транзакции
    private final ThreadLocal<Connection> transactionConnection = new ThreadLocal<>();
    
    DbInstance(DbConfig config) {
        this.config = config;
        this.dataSourceProvider = createDataSourceProvider(config);
        this.executor = new DefaultQueryExecutor(
            dataSourceProvider, 
            "default",
            config.getInterceptors(),
            this::getTransactionConnection  // Передаём supplier для транзакционного connection
        );
    }
    
    // === Builders ===
    
    public SelectBuilder select(String... columns) {
        return new ExecutableSelectBuilder(columns, executor);
    }
    
    public SelectBuilder selectAll() {
        return select("*");
    }
    
    public InsertBuilder insertInto(String table) {
        return new ExecutableInsertBuilder(table, executor);
    }
    
    public BatchInsertBuilder batchInsertInto(String table) {
        return new ExecutableBatchInsertBuilder(table, executor);
    }
    
    public UpdateBuilder update(String table) {
        return new ExecutableUpdateBuilder(table, executor);
    }
    
    public DeleteBuilder deleteFrom(String table) {
        return new ExecutableDeleteBuilder(table, executor);
    }
    
    public RawQueryBuilder raw(String sql) {
        return new ExecutableRawBuilder(sql, executor);
    }
    
    // === SQL from File ===
    
    /**
     * Выполнить SQL скрипт из файла.
     */
    public void executeScript(Path path) {
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            executeScriptContent(content);
        } catch (IOException e) {
            throw new DbException("Не удалось прочитать SQL файл: " + path, e);
        }
    }
    
    /**
     * Выполнить SQL скрипт из classpath ресурса.
     */
    public void executeScript(String classpathResource) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(classpathResource)) {
            if (is == null) {
                throw new DbException("Ресурс не найден: " + classpathResource);
            }
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            executeScriptContent(content);
        } catch (IOException e) {
            throw new DbException("Не удалось прочитать SQL ресурс: " + classpathResource, e);
        }
    }
    
    /**
     * Загрузить SQL из файла для использования с параметрами.
     */
    public RawQueryBuilder fromFile(Path path) {
        try {
            String sql = Files.readString(path, StandardCharsets.UTF_8);
            return raw(sql);
        } catch (IOException e) {
            throw new DbException("Не удалось прочитать SQL файл: " + path, e);
        }
    }
    
    /**
     * Загрузить SQL из classpath ресурса для использования с параметрами.
     */
    public RawQueryBuilder fromResource(String classpathResource) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(classpathResource)) {
            if (is == null) {
                throw new DbException("Ресурс не найден: " + classpathResource);
            }
            String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return raw(sql);
        } catch (IOException e) {
            throw new DbException("Не удалось прочитать SQL ресурс: " + classpathResource, e);
        }
    }
    
    private void executeScriptContent(String content) {
        // Разбиваем на statements по ; (учитывая строки и комментарии)
        List<String> statements = SqlScriptParser.splitStatements(content);
        
        try (Connection conn = getConnection()) {
            for (String sql : statements) {
                String trimmed = sql.trim();
                if (!trimmed.isEmpty()) {
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute(trimmed);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DbException("Не удалось выполнить SQL скрипт", e);
        }
    }
    
    // === Transactions ===
    
    /**
     * Выполнить код в транзакции.
     * Автоматический COMMIT при успехе, ROLLBACK при исключении.
     * 
     * <p><b>Потокобезопасность:</b> Каждый поток имеет свою изолированную транзакцию.
     * Вложенные вызовы transaction() используют ту же транзакцию (не создают savepoint).
     */
    public <T> T transaction(Supplier<T> action) {
        // Проверяем, не находимся ли уже в транзакции
        if (transactionConnection.get() != null) {
            // Вложенная транзакция — просто выполняем код
            return action.get();
        }
        
        Connection conn = null;
        try {
            conn = dataSourceProvider.getDataSource("default").getConnection();
            conn.setAutoCommit(false);
            transactionConnection.set(conn);
            
            T result = action.get();
            
            conn.commit();
            return result;
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            if (e instanceof RuntimeException re) {
                throw re;
            }
            throw new DbException("Транзакция не удалась", e);
        } finally {
            transactionConnection.remove();
            if (conn != null) {
                try { 
                    conn.setAutoCommit(true);
                    conn.close(); 
                } catch (SQLException ignored) {}
            }
        }
    }
    
    /**
     * Получить connection текущей транзакции или null.
     */
    Connection getTransactionConnection() {
        return transactionConnection.get();
    }
    
    // === Direct access ===
    
    /**
     * Получить соединение напрямую.
     * 
     * <p><b>⚠️ ВАЖНО:</b> Нельзя вызывать внутри транзакции! Используйте
     * builder-методы (select, insert, update, delete) для работы в транзакции.
     * 
     * @throws IllegalStateException если вызван внутри транзакции
     */
    public Connection getConnection() {
        // Запрещаем получение raw connection внутри транзакции,
        // т.к. пользователь может закрыть его и сломать транзакцию
        Connection txConn = transactionConnection.get();
        if (txConn != null) {
            throw new IllegalStateException(
                "Нельзя получить raw connection внутри транзакции. " +
                "Используйте методы-билдеры (select, insert, update, delete), " +
                "или вызовите getConnection() вне блока транзакции.");
        }
        return executor.getConnection();
    }
    
    public QueryExecutor executor() {
        return executor;
    }
    
    @Override
    public void close() {
        dataSourceProvider.shutdown();
    }
    
    private DataSourceProvider createDataSourceProvider(DbConfig config) {
        SimpleDataSourceProvider provider = new SimpleDataSourceProvider();
        provider.register("default", config.getDataSource());
        return provider;
    }
}
```

---

## 5. Executable Builders (связывают Builder с Executor)

```java
package com.framework.hex.db.builders;

/**
 * SelectBuilder с методами выполнения.
 */
public class ExecutableSelectBuilder extends SelectBuilder {
    
    private final QueryExecutor executor;
    
    public ExecutableSelectBuilder(String[] columns, QueryExecutor executor) {
        super(columns);
        this.executor = executor;
    }
    
    // === Выполнение ===
    
    /** Получить все строки как List<Map> */
    public List<Map<String, Object>> toList() {
        return executor.execute(build()).toList();
    }
    
    /** Получить все строки с маппером */
    public <T> List<T> toList(RowMapper<T> mapper) {
        return executor.execute(build()).toList(mapper);
    }
    
    /** Получить первую строку */
    public Optional<Map<String, Object>> first() {
        return limit(1).executor.execute(build()).firstRow();
    }
    
    /** Получить первую строку с маппером */
    public <T> Optional<T> first(RowMapper<T> mapper) {
        return limit(1).executor.execute(build()).firstRow(mapper);
    }
    
    /** Получить одну строку или бросить исключение */
    public Map<String, Object> single() {
        return first().orElseThrow(() -> 
            new DbException("Expected single row but got empty result"));
    }
    
    /** Получить одну строку с маппером или бросить исключение */
    public <T> T single(RowMapper<T> mapper) {
        return first(mapper).orElseThrow(() -> 
            new DbException("Expected single row but got empty result"));
    }
    
    /** Получить скалярное значение */
    public <T> T scalar(Class<T> type) {
        return executor.execute(build()).scalar(type);
    }
    
    /** Получить количество (для COUNT запросов) */
    public long count() {
        return scalar(Long.class);
    }
    
    /** Проверить существование */
    public boolean exists() {
        return first().isPresent();
    }
    
    /** Stream для больших результатов */
    public Stream<Row> stream() {
        return executor.execute(build()).stream();
    }
    
    // Переопределяем все методы билдера чтобы возвращать ExecutableSelectBuilder
    @Override
    public ExecutableSelectBuilder from(String table) {
        super.from(table);
        return this;
    }
    
    @Override
    public ExecutableSelectBuilder where(String column, Object value) {
        super.where(column, value);
        return this;
    }
    
    // ... и так далее для всех методов
}
```

```java
package com.framework.hex.db.builders;

/**
 * InsertBuilder с методами выполнения.
 */
public class ExecutableInsertBuilder extends InsertBuilder {
    
    private final QueryExecutor executor;
    
    public ExecutableInsertBuilder(String table, QueryExecutor executor) {
        super(table);
        this.executor = executor;
    }
    
    /** Выполнить INSERT */
    public int execute() {
        return executor.execute(build()).affectedRows();
    }
    
    /** Выполнить INSERT и вернуть сгенерированный ключ */
    public <T> T executeAndGetKey(Class<T> keyType) {
        returningKeys();
        return executor.execute(build()).generatedKey(keyType);
    }
    
    /** Выполнить INSERT и вернуть все сгенерированные ключи */
    public List<Object> executeAndGetKeys() {
        returningKeys();
        return executor.execute(build()).generatedKeys();
    }
    
    @Override
    public ExecutableInsertBuilder value(String column, Object value) {
        super.value(column, value);
        return this;
    }
    
    @Override
    public ExecutableInsertBuilder values(Map<String, Object> values) {
        super.values(values);
        return this;
    }
}
```

```java
package com.framework.hex.db.builders;

/**
 * UpdateBuilder с методами выполнения.
 */
public class ExecutableUpdateBuilder extends UpdateBuilder {
    
    private final QueryExecutor executor;
    
    public ExecutableUpdateBuilder(String table, QueryExecutor executor) {
        super(table);
        this.executor = executor;
    }
    
    /** Выполнить UPDATE, вернуть количество изменённых строк */
    public int execute() {
        return executor.execute(build()).affectedRows();
    }
    
    @Override
    public ExecutableUpdateBuilder set(String column, Object value) {
        super.set(column, value);
        return this;
    }
    
    @Override
    public ExecutableUpdateBuilder where(String column, Object value) {
        super.where(column, value);
        return this;
    }
}
```

```java
package com.framework.hex.db.builders;

/**
 * DeleteBuilder с методами выполнения.
 */
public class ExecutableDeleteBuilder extends DeleteBuilder {
    
    private final QueryExecutor executor;
    
    public ExecutableDeleteBuilder(String table, QueryExecutor executor) {
        super(table);
        this.executor = executor;
    }
    
    /** Выполнить DELETE, вернуть количество удалённых строк */
    public int execute() {
        return executor.execute(build()).affectedRows();
    }
    
    @Override
    public ExecutableDeleteBuilder where(String column, Object value) {
        super.where(column, value);
        return this;
    }
}
```

```java
package com.framework.hex.db.builders;

/**
 * BatchInsertBuilder с методами выполнения.
 */
public class ExecutableBatchInsertBuilder extends BatchInsertBuilder {
    
    private final QueryExecutor executor;
    
    public ExecutableBatchInsertBuilder(String table, QueryExecutor executor) {
        super(table);
        this.executor = executor;
    }
    
    /**
     * Выполнить batch INSERT.
     * @return массив количества затронутых строк для каждой вставки
     */
    public int[] execute() {
        if (size() == 0) {
            return new int[0];
        }
        return executor.executeBatch(buildSql(), getRowsParams());
    }
    
    @Override
    public ExecutableBatchInsertBuilder columns(String... cols) {
        super.columns(cols);
        return this;
    }
    
    @Override
    public ExecutableBatchInsertBuilder row(Object... values) {
        super.row(values);
        return this;
    }
    
    @Override
    public ExecutableBatchInsertBuilder row(Map<String, Object> row) {
        super.row(row);
        return this;
    }
    
    @Override
    public ExecutableBatchInsertBuilder rows(List<Map<String, Object>> rows) {
        super.rows(rows);
        return this;
    }
}
```

---

## 6. Конфигурация

```java
package com.framework.hex.db;

/**
 * Конфигурация DB модуля.
 */
public class DbConfig {
    
    private DataSource dataSource;
    private int queryTimeout = 30;
    private int fetchSize = 100;
    private List<QueryInterceptor> interceptors = new ArrayList<>();
    
    private DbConfig() {}
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Геттеры...
    
    public static class Builder {
        private final DbConfig config = new DbConfig();
        
        public Builder dataSource(DataSource ds) {
            config.dataSource = ds;
            return this;
        }
        
        /** Простая настройка через JDBC URL */
        public Builder jdbc(String url, String username, String password) {
            // Создаём простой DataSource
            config.dataSource = createSimpleDataSource(url, username, password);
            return this;
        }
        
        /** HikariCP настройка */
        public Builder hikari(Consumer<HikariConfig> configurer) {
            HikariConfig hc = new HikariConfig();
            configurer.accept(hc);
            config.dataSource = new HikariDataSource(hc);
            return this;
        }
        
        public Builder queryTimeout(int seconds) {
            config.queryTimeout = seconds;
            return this;
        }
        
        public Builder fetchSize(int size) {
            config.fetchSize = size;
            return this;
        }
        
        public Builder addInterceptor(QueryInterceptor interceptor) {
            config.interceptors.add(interceptor);
            return this;
        }
        
        public DbConfig build() {
            Objects.requireNonNull(config.dataSource, "DataSource обязателен");
            return config;
        }
    }
}
```

---

## 7. Точки расширения

### QueryInterceptor (минимальный)

```java
package com.framework.hex.db.core;

/**
 * Интерцептор запросов — точка расширения для логирования, метрик и т.д.
 */
public interface QueryInterceptor {
    
    /** Вызывается до выполнения. Может модифицировать Query. */
    default Query beforeExecute(Query query) {
        return query;
    }
    
    /** Вызывается после успешного выполнения */
    default void afterExecute(Query query, QueryResult result, Duration duration) {
    }
    
    /** Вызывается при ошибке */
    default void onError(Query query, Exception error, Duration duration) {
    }
}
```

### Пример: LoggingInterceptor

```java
package com.framework.hex.db.interceptors;

/**
 * Интерцептор для логирования SQL запросов.
 * Выводит SQL с интерполированными параметрами для удобства отладки.
 */
public class LoggingInterceptor implements QueryInterceptor {
    
    private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);
    
    @Override
    public Query beforeExecute(Query query) {
        if (log.isDebugEnabled()) {
            String interpolated = interpolateSql(query.sql(), query.parameters());
            log.debug("→ SQL: {}", interpolated);
        }
        return query;
    }
    
    @Override
    public void afterExecute(Query query, QueryResult result, Duration duration) {
        log.debug("← {} ms", duration.toMillis());
    }
    
    @Override
    public void onError(Query query, Exception error, Duration duration) {
        log.error("✗ {} ms | Error: {} | SQL: {}", 
            duration.toMillis(), 
            error.getMessage(),
            interpolateSql(query.sql(), query.parameters()));
    }
    
    /**
     * Интерполировать параметры в SQL для отладки.
     * НЕ использовать для выполнения — только для логирования!
     */
    private String interpolateSql(String sql, Map<String, Object> params) {
        String result = sql;
        for (Map.Entry<String, Object> e : params.entrySet()) {
            String value = formatValue(e.getValue());
            result = result.replace(":" + e.getKey(), value);
        }
        return result;
    }
    
    private String formatValue(Object value) {
        if (value == null) return "NULL";
        if (value instanceof String s) return "'" + escapeSql(s) + "'";
        if (value instanceof LocalDateTime || value instanceof LocalDate) {
            return "'" + value + "'";
        }
        if (value instanceof Collection<?> coll) {
            return coll.stream()
                .map(this::formatValue)
                .collect(Collectors.joining(", "));
        }
        return String.valueOf(value);
    }
    
    private String escapeSql(String s) {
        return s.replace("'", "''");
    }
}
```

### Пример: SlowQueryInterceptor

```java
package com.framework.hex.db.interceptors;

public class SlowQueryInterceptor implements QueryInterceptor {
    
    private final Duration threshold;
    private static final Logger log = LoggerFactory.getLogger(SlowQueryInterceptor.class);
    
    public SlowQueryInterceptor(Duration threshold) {
        this.threshold = threshold;
    }
    
    @Override
    public void afterExecute(Query query, QueryResult result, Duration duration) {
        if (duration.compareTo(threshold) > 0) {
            log.warn("🐢 SLOW QUERY [{}ms]: {}", duration.toMillis(), query.sql());
        }
    }
}
```

---

## 8. Extension: Object Mapping (опционально)

Если кто-то хочет маппить на объекты — пусть сам напишет или использует наше расширение:

```java
package com.framework.hex.db.mapper;

/**
 * Расширение для автоматического маппинга.
 * Пользователь может использовать, а может написать своё.
 */
public final class RowMappers {
    
    private RowMappers() {}
    
    /**
     * Маппинг на record/class через reflection.
     * 
     * Пример:
     * List<User> users = Db.selectAll()
     *     .from("users")
     *     .toList(RowMappers.to(User.class));
     */
    public static <T> RowMapper<T> to(Class<T> type) {
        if (type.isRecord()) {
            return new RecordMapper<>(type);
        }
        return new BeanMapper<>(type);
    }
    
    /**
     * Маппинг на Map с трансформацией ключей (camelCase).
     */
    public static RowMapper<Map<String, Object>> toCamelCaseMap() {
        return row -> {
            Map<String, Object> result = new LinkedHashMap<>();
            for (String col : row.columns()) {
                result.put(toCamelCase(col), row.toMap().get(col));
            }
            return result;
        };
    }
    
    private static String toCamelCase(String snakeCase) {
        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;
        for (char c : snakeCase.toCharArray()) {
            if (c == '_') {
                nextUpper = true;
            } else if (nextUpper) {
                result.append(Character.toUpperCase(c));
                nextUpper = false;
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
```

```java
package com.framework.hex.db.mapper;

/**
 * Маппер для record классов.
 */
class RecordMapper<T> implements RowMapper<T> {
    
    private final Class<T> recordClass;
    private final RecordComponent[] components;
    private final Constructor<T> constructor;
    
    RecordMapper(Class<T> recordClass) {
        this.recordClass = recordClass;
        this.components = recordClass.getRecordComponents();
        try {
            Class<?>[] paramTypes = Arrays.stream(components)
                .map(RecordComponent::getType)
                .toArray(Class<?>[]::new);
            this.constructor = recordClass.getDeclaredConstructor(paramTypes);
        } catch (NoSuchMethodException e) {
            throw new DbException("Не удалось найти конструктор record", e);
        }
    }
    
    @Override
    public T map(Row row) {
        try {
            Object[] args = new Object[components.length];
            for (int i = 0; i < components.length; i++) {
                String columnName = toSnakeCase(components[i].getName());
                Class<?> type = components[i].getType();
                args[i] = row.get(columnName, type);
            }
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw new DbException("Не удалось преобразовать строку в " + recordClass.getSimpleName(), e);
        }
    }
    
    private String toSnakeCase(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
```

---

## Использование в тестах

### Базовое использование

```java
class UserServiceTest {
    
    @BeforeAll
    static void setupDb() {
        Db.configure(DbConfig.builder()
            .jdbc("jdbc:postgresql://localhost:5432/test", "user", "pass")
            .addInterceptor(new LoggingInterceptor())
            .build());
    }
    
    @AfterAll
    static void tearDown() {
        Db.shutdown();
    }
    
    @Test
    void shouldFindActiveUsers() {
        // Простой SELECT → List<Map>
        List<Map<String, Object>> users = Db.selectAll()
            .from("users")
            .where("active", true)
            .orderBy("created_at")
            .limit(10)
            .toList();
        
        assertThat(users).isNotEmpty();
        assertThat(users.get(0)).containsKey("id");
    }
    
    @Test
    void shouldFindUserById() {
        // Одна строка
        Optional<Map<String, Object>> user = Db.selectAll()
            .from("users")
            .where("id", 1L)
            .first();
        
        assertThat(user).isPresent();
        assertThat(user.get().get("name")).isNotNull();
    }
    
    @Test
    void shouldCountUsers() {
        // Скалярное значение
        long count = Db.select("COUNT(*)")
            .from("users")
            .where("active", true)
            .scalar(Long.class);
        
        assertThat(count).isGreaterThan(0);
    }
    
    @Test
    void shouldCreateUser() {
        // INSERT с возвратом ID
        Long newId = Db.insertInto("users")
            .value("name", "Test User")
            .value("email", "test@example.com")
            .value("active", true)
            .executeAndGetKey(Long.class);
        
        assertThat(newId).isPositive();
        
        // Проверяем
        boolean exists = Db.selectAll()
            .from("users")
            .where("id", newId)
            .exists();
        
        assertThat(exists).isTrue();
    }
    
    @Test
    void shouldUpdateUser() {
        int affected = Db.update("users")
            .set("name", "Updated Name")
            .set("updated_at", LocalDateTime.now())
            .where("id", 1L)
            .execute();
        
        assertThat(affected).isEqualTo(1);
    }
    
    @Test
    void shouldDeleteUser() {
        // Сначала создадим
        Long id = Db.insertInto("users")
            .value("name", "To Delete")
            .value("email", "delete@example.com")
            .value("active", false)
            .executeAndGetKey(Long.class);
        
        // Теперь удалим
        int deleted = Db.deleteFrom("users")
            .where("id", id)
            .execute();
        
        assertThat(deleted).isEqualTo(1);
    }
}
```

### С кастомным маппингом

```java
class UserMappingTest {
    
    // Record для маппинга
    record User(Long id, String name, String email, boolean active) {}
    
    @Test
    void shouldMapToRecord() {
        List<User> users = Db.selectAll()
            .from("users")
            .where("active", true)
            .toList(RowMappers.to(User.class));
        
        assertThat(users).allMatch(User::active);
    }
    
    @Test
    void shouldMapWithCustomMapper() {
        // Встроенный маппер
        List<String> emails = Db.select("email")
            .from("users")
            .where("active", true)
            .toList(row -> row.getString("email"));
        
        assertThat(emails).allMatch(e -> e.contains("@"));
    }
    
    @Test
    void shouldMapComplexQuery() {
        // Сложный JOIN с кастомным маппингом
        record OrderWithUser(Long orderId, String userName, BigDecimal total) {}
        
        List<OrderWithUser> orders = Db.select("o.id", "u.name", "o.total")
            .from("orders o")
            .leftJoin("users u", "o.user_id = u.id")
            .where("o.status", "completed")
            .toList(row -> new OrderWithUser(
                row.getLong("id"),
                row.getString("name"),
                row.getBigDecimal("total")
            ));
        
        assertThat(orders).isNotEmpty();
    }
}
```

### Сырой SQL для сложных случаев

```java
class ComplexQueryTest {
    
    @Test
    void shouldExecuteRawSql() {
        String sql = """
            WITH user_stats AS (
                SELECT user_id, COUNT(*) as order_count, SUM(total) as total_spent
                FROM orders
                WHERE created_at > :since
                GROUP BY user_id
            )
            SELECT u.name, s.order_count, s.total_spent
            FROM users u
            JOIN user_stats s ON u.id = s.user_id
            WHERE s.total_spent > :minSpent
            ORDER BY s.total_spent DESC
            """;
        
        List<Map<String, Object>> results = Db.raw(sql)
            .param("since", LocalDateTime.now().minusMonths(1))
            .param("minSpent", BigDecimal.valueOf(1000))
            .toList();
        
        assertThat(results).isNotEmpty();
    }
    
    @Test
    void shouldUseNativeConnection() {
        // Аварийный выход для совсем сложных случаев
        try (Connection conn = Db.getConnection()) {
            // Делаем что угодно с connection
            DatabaseMetaData meta = conn.getMetaData();
            assertThat(meta.getDatabaseProductName()).isNotEmpty();
        }
    }
}
```

### Multi-DataSource

```java
class MultiDataSourceTest {
    
    @BeforeAll
    static void setup() {
        // Регистрируем именованные DataSource
        Db.register("primary", DbConfig.builder()
            .jdbc("jdbc:postgresql://primary:5432/app", "user", "pass")
            .build());
        
        Db.register("analytics", DbConfig.builder()
            .jdbc("jdbc:postgresql://analytics:5432/app", "reader", "pass")
            .build());
    }
    
    @AfterAll
    static void cleanup() {
        Db.shutdownAll();
    }
    
    @Test
    void shouldQueryDifferentDatabases() {
        // Запись в primary
        Long id = Db.use("primary").insertInto("users")
            .value("name", "Test")
            .executeAndGetKey(Long.class);
        
        // Чтение из analytics (replica)
        Optional<Map<String, Object>> user = Db.use("analytics").selectAll()
            .from("users")
            .where("id", id)
            .first();
        
        assertThat(user).isPresent();
    }
}
```

### Транзакции

```java
class TransactionTest {
    
    @Test
    void shouldCommitOnSuccess() {
        Long orderId = Db.transaction(() -> {
            // Создаём заказ
            Long id = Db.insertInto("orders")
                .value("user_id", userId)
                .value("total", BigDecimal.valueOf(100))
                .executeAndGetKey(Long.class);
            
            // Обновляем счётчик заказов пользователя
            Db.update("users")
                .increment("order_count", 1)
                .where("id", userId)
                .execute();
            
            return id;
        });
        
        assertThat(orderId).isPositive();
    }
    
    @Test
    void shouldRollbackOnFailure() {
        Long userId = createTestUser();
        
        assertThrows(RuntimeException.class, () -> {
            Db.transaction(() -> {
                Db.insertInto("orders")
                    .value("user_id", userId)
                    .value("total", BigDecimal.valueOf(100))
                    .execute();
                
                // Симулируем ошибку
                throw new RuntimeException("Симуляция ошибки");
            });
        });
        
        // Заказ не создан — транзакция откатилась
        long orderCount = Db.select("COUNT(*)")
            .from("orders")
            .where("user_id", userId)
            .scalar(Long.class);
        
        assertThat(orderCount).isZero();
    }
    
    @Test
    void shouldIsolateParallelTransactions() throws Exception {
        // Транзакции в разных потоках изолированы
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        Future<Long> future1 = executor.submit(() -> 
            Db.transaction(() -> {
                Thread.sleep(100);  // Имитация работы
                return Db.insertInto("users")
                    .value("name", "Thread1")
                    .executeAndGetKey(Long.class);
            })
        );
        
        Future<Long> future2 = executor.submit(() -> 
            Db.transaction(() -> {
                return Db.insertInto("users")
                    .value("name", "Thread2")
                    .executeAndGetKey(Long.class);
            })
        );
        
        Long id1 = future1.get();
        Long id2 = future2.get();
        
        assertThat(id1).isNotEqualTo(id2);
        executor.shutdown();
    }
}
```

### SQL из файлов

```java
class SqlFromFileTest {
    
    @BeforeAll
    static void setupSchema() {
        // Загружаем схему БД из файлов
        Db.executeScript("db/schema.sql");
        Db.executeScript("db/test-data.sql");
    }
    
    @AfterAll
    static void cleanup() {
        Db.executeScript("db/cleanup.sql");
    }
    
    @Test
    void shouldExecuteComplexQueryFromFile() {
        // Сложный запрос хранится в отдельном .sql файле
        List<Map<String, Object>> results = Db.fromResource("queries/user-stats.sql")
            .param("minOrders", 5)
            .param("since", LocalDate.now().minusMonths(3))
            .toList();
        
        assertThat(results).isNotEmpty();
    }
    
    @Test
    void shouldLoadFromAbsolutePath() {
        Path sqlFile = Paths.get("src/test/resources/queries/monthly-report.sql");
        
        List<Map<String, Object>> report = Db.fromFile(sqlFile)
            .param("year", 2024)
            .param("month", 12)
            .toList();
        
        assertThat(report).isNotEmpty();
    }
}
```

**Структура тестовых ресурсов:**
```
src/test/resources/
├── db/
│   ├── schema.sql          # Выражения CREATE TABLE
│   ├── test-data.sql       # Тестовые данные для INSERT
│   └── cleanup.sql         # TRUNCATE/DELETE
└── queries/
    ├── user-stats.sql      # Сложные SELECT с :параметрами
    └── monthly-report.sql
```

### Batch Insert

```java
class BatchInsertTest {
    
    @Test
    void shouldInsertMultipleRows() {
        int[] results = Db.batchInsertInto("users")
            .columns("name", "email", "active")
            .row("John", "john@test.com", true)
            .row("Jane", "jane@test.com", true)
            .row("Bob", "bob@test.com", false)
            .execute();
        
        assertThat(results).hasSize(3);
        assertThat(Arrays.stream(results).sum()).isEqualTo(3);
    }
    
    @Test
    void shouldInsertFromList() {
        List<Map<String, Object>> users = List.of(
            Map.of("name", "User1", "email", "u1@test.com", "active", true),
            Map.of("name", "User2", "email", "u2@test.com", "active", true),
            Map.of("name", "User3", "email", "u3@test.com", "active", false)
        );
        
        int[] results = Db.batchInsertInto("users")
            .rows(users)
            .execute();
        
        assertThat(results).hasSize(3);
    }
}
```

### Условные запросы (очень полезно для тестов)

```java
@Test
void shouldBuildConditionalQuery() {
    // Параметры фильтрации могут быть null
    String nameFilter = "John";
    Boolean activeFilter = null;  // не фильтровать
    Integer minAge = 18;
    
    List<Map<String, Object>> users = Db.selectAll()
        .from("users")
        .whereIf(nameFilter != null, "name", nameFilter)
        .whereIf(activeFilter != null, "active", activeFilter)
        .whereIf(minAge != null, builder -> builder.where("age", ">=", minAge))
        .toList();
    
    // SQL будет: SELECT * FROM users WHERE name = ? AND age >= ?
    // (без условия active, т.к. activeFilter = null)
}
```

---

## Итоговая структура модулей

```
hex-db/
├── hex-db-core/              # Интерфейсы: Query, Row, RowMapper, QueryExecutor
│   └── src/main/java/
│       └── com/framework/hex/db/core/
│
├── hex-db-builders/          # SQL.select(), SQL.insert(), etc.
│   └── src/main/java/
│       └── com/framework/hex/db/builders/
│
├── hex-db-executor/          # DefaultQueryExecutor, результаты
│   └── src/main/java/
│       └── com/framework/hex/db/executor/
│
├── hex-db/                   # Главный модуль: класс Db, DbInstance, DbConfig
│   └── src/main/java/
│       └── com/framework/hex/db/
│
└── [опциональные расширения создаются по мере надобности]
    ├── hex-db-mapper/        # RowMappers.to(Class)
    ├── hex-db-validation/    # SQL валидация
    └── hex-db-hikari/        # HikariCP интеграция готовая
```

---