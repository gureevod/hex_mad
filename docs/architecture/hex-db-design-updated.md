# hex-db: Упрощённый дизайн

> **Версия:** 2.0 (после ревью)  
> **Дата:** 2024-12-06  
> **Принцип:** Простота важнее гибкости

---

## Архитектура модулей

```
hex-db-core          ← Базовые интерфейсы (Row, RowMapper, Query, QueryResult)
    │
    ├── hex-db-builders   ← Fluent билдеры (SQL.select, SQL.insert, ...)
    │
    └── hex-db-executor   ← Выполнение запросов (DefaultQueryExecutor)

hex-db                    ← Главный модуль (Db, DbInstance, DbConfig)

[Опциональные расширения]
    ├── hex-db-mapper     ← Авто-маппинг на record/class
    └── hex-db-hikari     ← HikariCP интеграция
```

---

## 1. Core Module (`hex-db-core`)

### Row — строка результата

```java
package com.framework.hex.db.core;

/**
 * Представляет одну строку результата запроса.
 * Иммутабельная обёртка над данными.
 */
public interface Row {
    
    /**
     * Получить значение колонки с приведением типа.
     * Возвращает null если значение в БД NULL.
     */
    <T> T get(String column, Class<T> type);
    
    /** Получить строку. Эквивалент get(column, String.class) */
    String getString(String column);
    
    /** Получить Long. Автоконвертация из Number. */
    Long getLong(String column);
    
    /** Получить Integer. Автоконвертация из Number. */
    Integer getInt(String column);
    
    /** Получить Boolean. Автоконвертация из Number (0=false) и String. */
    Boolean getBoolean(String column);
    
    /** Получить BigDecimal. Для денежных значений. */
    BigDecimal getBigDecimal(String column);
    
    /** Получить LocalDateTime из TIMESTAMP колонки. */
    LocalDateTime getLocalDateTime(String column);
    
    /** Получить LocalDate из DATE колонки. */
    LocalDate getLocalDate(String column);
    
    /** Конвертировать всю строку в Map. Имена колонок в lowercase. */
    Map<String, Object> toMap();
}
```

### RowMapper — маппинг строки в объект

```java
package com.framework.hex.db.core;

/**
 * Функция преобразования Row в объект.
 * Основная точка расширения для кастомного маппинга.
 */
@FunctionalInterface
public interface RowMapper<T> {
    
    T map(Row row);
    
    /** Встроенный маппер в Map */
    static RowMapper<Map<String, Object>> toMap() {
        return Row::toMap;
    }
}
```

### QueryResult — результат выполнения

```java
package com.framework.hex.db.core;

/**
 * Результат выполнения запроса.
 * 
 * Все методы СРАЗУ читают данные и закрывают ресурсы.
 * Никаких ленивых вычислений — никаких утечек.
 */
public interface QueryResult {
    
    // === SELECT результаты ===
    
    /** Получить все строки как List<Map>. Самый простой случай. */
    List<Map<String, Object>> toList();
    
    /** Получить все строки с маппером. */
    <T> List<T> toList(RowMapper<T> mapper);
    
    /** Получить первую строку или empty. Для запросов с LIMIT 1. */
    Optional<Map<String, Object>> firstRow();
    
    /** Получить первую строку с маппером. */
    <T> Optional<T> firstRow(RowMapper<T> mapper);
    
    /** 
     * Получить единственное скалярное значение.
     * Для COUNT(*), SUM(), MAX() и т.д.
     * Возвращает null если результат пустой.
     */
    <T> T scalar(Class<T> type);
    
    // === INSERT/UPDATE/DELETE результаты ===
    
    /** Количество затронутых строк. */
    int affectedRows();
    
    /** 
     * Получить сгенерированный ключ после INSERT.
     * Требует вызова returningKeys() на билдере.
     */
    <T> T generatedKey(Class<T> type);
}
```

### Query — представление запроса

```java
package com.framework.hex.db.core;

/**
 * Описание запроса для выполнения.
 */
public interface Query {
    
    /** SQL с именованными параметрами :name */
    String sql();
    
    /** Значения параметров */
    Map<String, Object> parameters();
    
    /** Тип запроса */
    QueryType type();
    
    /** Нужно ли возвращать сгенерированные ключи */
    boolean returnGeneratedKeys();
    
    enum QueryType {
        SELECT, INSERT, UPDATE, DELETE, SCRIPT
    }
}
```

### QueryExecutor — исполнитель запросов

```java
package com.framework.hex.db.core;

/**
 * Выполняет запросы к БД.
 * Потокобезопасен — каждый запрос использует свой connection из пула.
 */
public interface QueryExecutor {
    
    /** Выполнить Query объект */
    QueryResult execute(Query query);
    
    /** Выполнить сырой SQL с параметрами */
    QueryResult execute(String sql, Map<String, Object> params);
    
    /** Выполнить сырой SQL без параметров */
    QueryResult execute(String sql);
    
    /** Batch выполнение для множества строк */
    int[] executeBatch(String sql, List<Map<String, Object>> paramsList);
}
```

---

## 2. Builders Module (`hex-db-builders`)

### SQL — точка входа

```java
package com.framework.hex.db.builders;

/**
 * Фабрика билдеров запросов.
 * Статический API для создания запросов.
 */
public final class SQL {
    
    private SQL() {}
    
    /** SELECT column1, column2, ... */
    public static SelectBuilder select(String... columns) {
        return new SelectBuilder(columns);
    }
    
    /** SELECT * */
    public static SelectBuilder selectAll() {
        return new SelectBuilder("*");
    }
    
    /** INSERT INTO table */
    public static InsertBuilder insertInto(String table) {
        return new InsertBuilder(table);
    }
    
    /** UPDATE table */
    public static UpdateBuilder update(String table) {
        return new UpdateBuilder(table);
    }
    
    /** DELETE FROM table */
    public static DeleteBuilder deleteFrom(String table) {
        return new DeleteBuilder(table);
    }
    
    /** Сырой SQL для сложных случаев (CTE, подзапросы) */
    public static RawQueryBuilder raw(String sql) {
        return new RawQueryBuilder(sql);
    }
}
```

### SelectBuilder

```java
package com.framework.hex.db.builders;

/**
 * Билдер SELECT запросов.
 * 
 * НЕ потокобезопасен — создавайте новый экземпляр в каждом потоке.
 * 
 * Пример:
 * <pre>{@code
 * List<User> users = Db.selectAll()
 *     .from("users")
 *     .where("active", true)
 *     .where("age", ">=", 18)
 *     .orderBy("name")
 *     .limit(10)
 *     .toList(userMapper);
 * }</pre>
 */
public class SelectBuilder implements QueryBuilder {
    
    private final List<String> columns;
    private String table;
    private final List<String> joins = new ArrayList<>();
    private final List<WhereClause> whereClauses = new ArrayList<>();
    private final Map<String, Object> parameters = new LinkedHashMap<>();
    private String groupBy;
    private String orderBy;
    private Integer limit;
    private Integer offset;
    
    // === FROM ===
    
    /** Указать таблицу. Обязательный вызов. */
    public SelectBuilder from(String table) {
        this.table = table;
        return this;
    }
    
    // === JOINs ===
    
    /** INNER JOIN table ON condition */
    public SelectBuilder join(String table, String on) {
        joins.add("JOIN " + table + " ON " + on);
        return this;
    }
    
    /** LEFT JOIN table ON condition */
    public SelectBuilder leftJoin(String table, String on) {
        joins.add("LEFT JOIN " + table + " ON " + on);
        return this;
    }
    
    // === WHERE ===
    
    /** 
     * WHERE column = value (через AND).
     * Если value == null, автоматически использует IS NULL.
     */
    public SelectBuilder where(String column, Object value) {
        if (value == null) {
            return whereNull(column);
        }
        return where(column, "=", value);
    }
    
    /** WHERE column operator value (например: "age", ">=", 18) */
    public SelectBuilder where(String column, String operator, Object value) {
        String paramName = generateParamName(column);
        whereClauses.add(new WhereClause(column + " " + operator + " :" + paramName, "AND"));
        parameters.put(paramName, value);
        return this;
    }
    
    /** WHERE column IN (values) */
    public SelectBuilder whereIn(String column, Collection<?> values) {
        String paramName = generateParamName(column);
        whereClauses.add(new WhereClause(column + " IN (:" + paramName + ")", "AND"));
        parameters.put(paramName, values);
        return this;
    }
    
    /** WHERE column IS NULL */
    public SelectBuilder whereNull(String column) {
        whereClauses.add(new WhereClause(column + " IS NULL", "AND"));
        return this;
    }
    
    /** WHERE column IS NOT NULL */
    public SelectBuilder whereNotNull(String column) {
        whereClauses.add(new WhereClause(column + " IS NOT NULL", "AND"));
        return this;
    }
    
    /** 
     * Сырое условие для сложных случаев (OR, подзапросы).
     * Параметры добавляйте через param().
     * 
     * Пример: whereRaw("(status = :s1 OR status = :s2)").param("s1", "A").param("s2", "B")
     */
    public SelectBuilder whereRaw(String condition) {
        whereClauses.add(new WhereClause(condition, "AND"));
        return this;
    }
    
    /** 
     * Условный WHERE — добавляет условие только если condition == true.
     * Очень полезно для динамических фильтров в тестах.
     * 
     * Пример: whereIf(name != null, "name", name)
     */
    public SelectBuilder whereIf(boolean condition, String column, Object value) {
        if (condition) {
            where(column, value);
        }
        return this;
    }
    
    /** Добавить именованный параметр для whereRaw() */
    public SelectBuilder param(String name, Object value) {
        parameters.put(name, value);
        return this;
    }
    
    // === GROUP BY / ORDER BY ===
    
    /** GROUP BY columns */
    public SelectBuilder groupBy(String... columns) {
        this.groupBy = String.join(", ", columns);
        return this;
    }
    
    /** ORDER BY column ASC */
    public SelectBuilder orderBy(String column) {
        this.orderBy = column;
        return this;
    }
    
    /** ORDER BY column ASC|DESC */
    public SelectBuilder orderBy(String column, SortOrder order) {
        this.orderBy = column + " " + order.name();
        return this;
    }
    
    // === LIMIT / OFFSET ===
    
    /** LIMIT n — ограничить количество строк */
    public SelectBuilder limit(int limit) {
        this.limit = limit;
        return this;
    }
    
    /** OFFSET n — пропустить первые n строк */
    public SelectBuilder offset(int offset) {
        this.offset = offset;
        return this;
    }
    
    // === Build ===
    
    @Override
    public Query build() {
        // Собираем SQL из частей
        // SELECT columns FROM table [JOINs] [WHERE] [GROUP BY] [ORDER BY] [LIMIT] [OFFSET]
    }
    
    public enum SortOrder { ASC, DESC }
    
    private record WhereClause(String condition, String connector) {}
}
```

### ExecutableSelectBuilder — билдер + выполнение

```java
package com.framework.hex.db.builders;

/**
 * SelectBuilder с методами выполнения запроса.
 * Возвращается из Db.select() / Db.selectAll().
 */
public class ExecutableSelectBuilder extends SelectBuilder {
    
    private final QueryExecutor executor;
    
    // === Методы выполнения ===
    
    /** Выполнить и получить все строки как List<Map> */
    public List<Map<String, Object>> toList() {
        return executor.execute(build()).toList();
    }
    
    /** Выполнить и получить все строки с маппером */
    public <T> List<T> toList(RowMapper<T> mapper) {
        return executor.execute(build()).toList(mapper);
    }
    
    /** Выполнить и получить первую строку (с LIMIT 1) */
    public Optional<Map<String, Object>> first() {
        return limit(1).executor.execute(build()).firstRow();
    }
    
    /** Выполнить и получить первую строку с маппером */
    public <T> Optional<T> first(RowMapper<T> mapper) {
        return limit(1).executor.execute(build()).firstRow(mapper);
    }
    
    /** 
     * Выполнить и получить одну строку или бросить исключение.
     * Для случаев когда строка ДОЛЖНА существовать.
     */
    public Map<String, Object> single() {
        return first().orElseThrow(() -> 
            new DbException("Expected single row but got empty result"));
    }
    
    /** single() с маппером */
    public <T> T single(RowMapper<T> mapper) {
        return first(mapper).orElseThrow(() -> 
            new DbException("Expected single row but got empty result"));
    }
    
    /** Выполнить и получить скалярное значение (COUNT, SUM, MAX) */
    public <T> T scalar(Class<T> type) {
        return executor.execute(build()).scalar(type);
    }
    
    /** Выполнить COUNT(*) и вернуть количество */
    public long count() {
        return scalar(Long.class);
    }
    
    /** Проверить существование хотя бы одной строки */
    public boolean exists() {
        return first().isPresent();
    }
}
```

### InsertBuilder

```java
package com.framework.hex.db.builders;

/**
 * Билдер INSERT запросов.
 * 
 * Пример:
 * <pre>{@code
 * Long id = Db.insertInto("users")
 *     .value("name", "John")
 *     .value("email", "john@test.com")
 *     .value("active", true)
 *     .executeAndGetKey(Long.class);
 * }</pre>
 */
public class InsertBuilder implements QueryBuilder {
    
    private final String table;
    private final Map<String, Object> values = new LinkedHashMap<>();
    private boolean returnKeys = false;
    
    /** Установить значение колонки */
    public InsertBuilder value(String column, Object value) {
        values.put(column, value);
        return this;
    }
    
    /** Установить все значения из Map */
    public InsertBuilder values(Map<String, Object> values) {
        this.values.putAll(values);
        return this;
    }
    
    /** Включить возврат сгенерированного ключа (для executeAndGetKey) */
    public InsertBuilder returningKeys() {
        this.returnKeys = true;
        return this;
    }
    
    @Override
    public Query build() {
        // INSERT INTO table (col1, col2) VALUES (:col1, :col2)
    }
}
```

### ExecutableInsertBuilder

```java
package com.framework.hex.db.builders;

/**
 * InsertBuilder с методами выполнения.
 */
public class ExecutableInsertBuilder extends InsertBuilder {
    
    private final QueryExecutor executor;
    
    /** Выполнить INSERT, вернуть количество вставленных строк */
    public int execute() {
        return executor.execute(build()).affectedRows();
    }
    
    /** 
     * Выполнить INSERT и вернуть сгенерированный ключ.
     * Автоматически включает returningKeys().
     */
    public <T> T executeAndGetKey(Class<T> keyType) {
        returningKeys();
        return executor.execute(build()).generatedKey(keyType);
    }
}
```

### UpdateBuilder

```java
package com.framework.hex.db.builders;

/**
 * Билдер UPDATE запросов.
 * 
 * Защита от случайного UPDATE без WHERE —
 * build() бросит исключение если нет условий.
 * 
 * Пример:
 * <pre>{@code
 * int affected = Db.update("users")
 *     .set("name", "New Name")
 *     .set("updated_at", LocalDateTime.now())
 *     .where("id", userId)
 *     .execute();
 * }</pre>
 */
public class UpdateBuilder implements QueryBuilder {
    
    private final String table;
    private final Map<String, Object> setValues = new LinkedHashMap<>();
    private final List<String> whereClauses = new ArrayList<>();
    private final Map<String, Object> whereParams = new LinkedHashMap<>();
    
    /** SET column = value */
    public UpdateBuilder set(String column, Object value) {
        setValues.put(column, value);
        return this;
    }
    
    /** SET все значения из Map */
    public UpdateBuilder setAll(Map<String, Object> values) {
        setValues.putAll(values);
        return this;
    }
    
    /** WHERE column = value */
    public UpdateBuilder where(String column, Object value) {
        if (value == null) {
            whereClauses.add(column + " IS NULL");
        } else {
            String paramName = "w_" + column;
            whereClauses.add(column + " = :" + paramName);
            whereParams.put(paramName, value);
        }
        return this;
    }
    
    /** WHERE column IN (values) */
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
                "UPDATE без WHERE запрещён. Это защита от случайного обновления всех строк.");
        }
        // UPDATE table SET col1 = :col1, col2 = :col2 WHERE ...
    }
}
```

### ExecutableUpdateBuilder

```java
package com.framework.hex.db.builders;

/**
 * UpdateBuilder с методами выполнения.
 */
public class ExecutableUpdateBuilder extends UpdateBuilder {
    
    private final QueryExecutor executor;
    
    /** Выполнить UPDATE, вернуть количество обновлённых строк */
    public int execute() {
        return executor.execute(build()).affectedRows();
    }
}
```

### DeleteBuilder

```java
package com.framework.hex.db.builders;

/**
 * Билдер DELETE запросов.
 * 
 * Защита от случайного DELETE без WHERE —
 * используйте all() для явного удаления всех строк.
 * 
 * Пример:
 * <pre>{@code
 * int deleted = Db.deleteFrom("users")
 *     .where("id", userId)
 *     .execute();
 * }</pre>
 */
public class DeleteBuilder implements QueryBuilder {
    
    private final String table;
    private final List<String> whereClauses = new ArrayList<>();
    private final Map<String, Object> parameters = new LinkedHashMap<>();
    
    /** WHERE column = value */
    public DeleteBuilder where(String column, Object value) {
        if (value == null) {
            whereClauses.add(column + " IS NULL");
        } else {
            whereClauses.add(column + " = :" + column);
            parameters.put(column, value);
        }
        return this;
    }
    
    /** WHERE column IN (values) */
    public DeleteBuilder whereIn(String column, Collection<?> values) {
        whereClauses.add(column + " IN (:" + column + ")");
        parameters.put(column, values);
        return this;
    }
    
    /** 
     * Явное разрешение DELETE без WHERE.
     * Требуется для удаления всех строк таблицы.
     */
    public DeleteBuilder all() {
        whereClauses.add("1=1");
        return this;
    }
    
    @Override
    public Query build() {
        if (whereClauses.isEmpty()) {
            throw new IllegalStateException(
                "DELETE без WHERE запрещён. Используйте all() для удаления всех строк.");
        }
        // DELETE FROM table WHERE ...
    }
}
```

### ExecutableDeleteBuilder

```java
package com.framework.hex.db.builders;

/**
 * DeleteBuilder с методами выполнения.
 */
public class ExecutableDeleteBuilder extends DeleteBuilder {
    
    private final QueryExecutor executor;
    
    /** Выполнить DELETE, вернуть количество удалённых строк */
    public int execute() {
        return executor.execute(build()).affectedRows();
    }
}
```

### BatchInsertBuilder

```java
package com.framework.hex.db.builders;

/**
 * Билдер для batch INSERT — вставка множества строк одним запросом.
 * 
 * Пример:
 * <pre>{@code
 * int[] results = Db.batchInsertInto("users")
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
    
    /** Определить колонки для вставки. Вызывать перед row(). */
    public BatchInsertBuilder columns(String... cols) {
        columns.addAll(Arrays.asList(cols));
        return this;
    }
    
    /** Добавить строку со значениями в порядке колонок */
    public BatchInsertBuilder row(Object... values) {
        // Проверяет что columns.size() == values.length
    }
    
    /** Добавить строку как Map */
    public BatchInsertBuilder row(Map<String, Object> row) {
        rows.add(new LinkedHashMap<>(row));
        return this;
    }
    
    /** Добавить множество строк */
    public BatchInsertBuilder rows(List<Map<String, Object>> rows) {
        rows.forEach(this::row);
        return this;
    }
    
    /** Получить SQL для batch insert */
    public String buildSql() {
        // INSERT INTO table (col1, col2) VALUES (:col1, :col2)
    }
    
    /** Получить параметры для каждой строки */
    public List<Map<String, Object>> getRowsParams() {
        return Collections.unmodifiableList(rows);
    }
}
```

### RawQueryBuilder

```java
package com.framework.hex.db.builders;

/**
 * Билдер для сырого SQL.
 * Для сложных запросов (CTE, подзапросы, нестандартный синтаксис).
 * 
 * Пример:
 * <pre>{@code
 * List<Map<String, Object>> stats = Db.raw("""
 *     WITH user_stats AS (
 *         SELECT user_id, COUNT(*) as cnt
 *         FROM orders WHERE created_at > :since
 *         GROUP BY user_id
 *     )
 *     SELECT u.name, s.cnt FROM users u
 *     JOIN user_stats s ON u.id = s.user_id
 *     WHERE s.cnt > :minOrders
 *     """)
 *     .param("since", LocalDateTime.now().minusMonths(1))
 *     .param("minOrders", 10)
 *     .toList();
 * }</pre>
 */
public class RawQueryBuilder implements QueryBuilder {
    
    private final String sql;
    private final Map<String, Object> parameters = new LinkedHashMap<>();
    private QueryType type;
    private boolean returnKeys = false;
    
    /** Добавить именованный параметр */
    public RawQueryBuilder param(String name, Object value) {
        parameters.put(name, value);
        return this;
    }
    
    /** Добавить все параметры из Map */
    public RawQueryBuilder params(Map<String, Object> params) {
        parameters.putAll(params);
        return this;
    }
    
    /** Включить возврат ключей (для INSERT) */
    public RawQueryBuilder returningKeys() {
        this.returnKeys = true;
        return this;
    }
    
    @Override
    public Query build() {
        return new SimpleQuery(sql, parameters, type, returnKeys);
    }
}
```

---

## 3. Executor Module (`hex-db-executor`)

### DefaultQueryExecutor

```java
package com.framework.hex.db.executor;

/**
 * Стандартная реализация QueryExecutor.
 * 
 * Потокобезопасен — каждый запрос получает свой connection из пула
 * и возвращает его после выполнения.
 */
public class DefaultQueryExecutor implements QueryExecutor {
    
    private final DataSource dataSource;
    private final QueryInterceptor interceptor;  // может быть null
    private final Supplier<Connection> transactionConnectionSupplier;
    
    @Override
    public QueryResult execute(Query query) {
        // 1. Вызвать interceptor.beforeExecute() если есть
        // 2. Получить connection (из транзакции или из пула)
        // 3. Подготовить PreparedStatement с параметрами
        // 4. Выполнить запрос
        // 5. Сразу прочитать ВСЕ данные в память
        // 6. Закрыть ResultSet, Statement, Connection
        // 7. Вернуть результат
    }
    
    @Override
    public int[] executeBatch(String sql, List<Map<String, Object>> paramsList) {
        // Batch выполнение через addBatch()/executeBatch()
    }
}
```

### SelectQueryResult — результат SELECT

```java
package com.framework.hex.db.executor;

/**
 * Результат SELECT запроса.
 * 
 * ВАЖНО: Все данные УЖЕ прочитаны в память при создании объекта.
 * Никаких открытых ресурсов, никаких утечек.
 */
public class SelectQueryResult implements QueryResult {
    
    private final List<Map<String, Object>> rows;
    private final List<String> columnNames;
    
    /** Создаётся executor'ом после полного чтения ResultSet */
    SelectQueryResult(List<Map<String, Object>> rows, List<String> columnNames) {
        this.rows = rows;
        this.columnNames = columnNames;
    }
    
    @Override
    public List<Map<String, Object>> toList() {
        return new ArrayList<>(rows);  // Защитная копия
    }
    
    @Override
    public <T> List<T> toList(RowMapper<T> mapper) {
        return rows.stream()
            .map(rowData -> mapper.map(new MapRow(rowData, columnNames)))
            .collect(Collectors.toList());
    }
    
    @Override
    public Optional<Map<String, Object>> firstRow() {
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }
    
    @Override
    public <T> Optional<T> firstRow(RowMapper<T> mapper) {
        return rows.isEmpty() 
            ? Optional.empty() 
            : Optional.of(mapper.map(new MapRow(rows.get(0), columnNames)));
    }
    
    @Override
    public <T> T scalar(Class<T> type) {
        if (rows.isEmpty()) {
            return null;
        }
        Object value = rows.get(0).values().iterator().next();
        return convertValue(value, type);
    }
    
    @Override
    public int affectedRows() {
        throw new UnsupportedOperationException("SELECT не имеет affected rows");
    }
    
    @Override
    public <T> T generatedKey(Class<T> type) {
        throw new UnsupportedOperationException("SELECT не имеет generated keys");
    }
}
```

### ModificationQueryResult — результат INSERT/UPDATE/DELETE

```java
package com.framework.hex.db.executor;

/**
 * Результат модифицирующего запроса (INSERT/UPDATE/DELETE).
 */
public class ModificationQueryResult implements QueryResult {
    
    private final int affectedRows;
    private final List<Object> generatedKeys;
    
    @Override
    public int affectedRows() {
        return affectedRows;
    }
    
    @Override
    public <T> T generatedKey(Class<T> type) {
        if (generatedKeys.isEmpty()) {
            throw new DbException("No generated keys. Did you call returningKeys()?");
        }
        return convertValue(generatedKeys.get(0), type);
    }
    
    // SELECT методы бросают UnsupportedOperationException
}
```

### MapRow — Row на основе Map

```java
package com.framework.hex.db.executor;

/**
 * Реализация Row на основе Map.
 * Используется после того как данные уже прочитаны из ResultSet.
 */
class MapRow implements Row {
    
    private final Map<String, Object> data;
    private final List<String> columns;
    
    @Override
    public <T> T get(String column, Class<T> type) {
        Object value = data.get(column.toLowerCase());
        return convertValue(value, type);
    }
    
    @Override
    public String getString(String column) {
        return get(column, String.class);
    }
    
    // ... остальные типизированные методы
    
    @Override
    public Map<String, Object> toMap() {
        return new LinkedHashMap<>(data);
    }
}
```

---

## 4. Главный модуль (`hex-db`)

### Db — статическая точка входа

```java
package com.framework.hex.db;

/**
 * Главная точка входа в hex-db.
 * 
 * Потокобезопасен. Builders НЕ потокобезопасны —
 * каждый поток должен создавать свой builder.
 * 
 * Пример:
 * <pre>{@code
 * // Настройка (один раз при старте)
 * Db.configure(dataSource);
 * 
 * // Использование в тестах
 * List<User> users = Db.selectAll()
 *     .from("users")
 *     .where("active", true)
 *     .toList(userMapper);
 * }</pre>
 */
public final class Db {
    
    private static final AtomicReference<DbInstance> INSTANCE = new AtomicReference<>();
    private static final ConcurrentHashMap<String, DbInstance> NAMED_INSTANCES = new ConcurrentHashMap<>();
    
    private Db() {}
    
    // === Конфигурация ===
    
    /** Простая настройка с DataSource */
    public static void configure(DataSource dataSource) {
        configure(DbConfig.builder().dataSource(dataSource).build());
    }
    
    /** Полная настройка с конфигурацией */
    public static void configure(DbConfig config) {
        INSTANCE.set(new DbInstance(config));
    }
    
    // === Multi-DataSource ===
    
    /** Зарегистрировать именованный DataSource */
    public static void register(String name, DbConfig config) {
        NAMED_INSTANCES.put(name, new DbInstance(config));
    }
    
    /** Получить DbInstance по имени */
    public static DbInstance use(String name) {
        DbInstance instance = NAMED_INSTANCES.get(name);
        if (instance == null) {
            throw new IllegalStateException("DataSource '" + name + "' не зарегистрирован");
        }
        return instance;
    }
    
    // === Query Builders ===
    
    /** SELECT columns... */
    public static ExecutableSelectBuilder select(String... columns) {
        return instance().select(columns);
    }
    
    /** SELECT * */
    public static ExecutableSelectBuilder selectAll() {
        return instance().selectAll();
    }
    
    /** INSERT INTO table */
    public static ExecutableInsertBuilder insertInto(String table) {
        return instance().insertInto(table);
    }
    
    /** Batch INSERT INTO table */
    public static ExecutableBatchInsertBuilder batchInsertInto(String table) {
        return instance().batchInsertInto(table);
    }
    
    /** UPDATE table */
    public static ExecutableUpdateBuilder update(String table) {
        return instance().update(table);
    }
    
    /** DELETE FROM table */
    public static ExecutableDeleteBuilder deleteFrom(String table) {
        return instance().deleteFrom(table);
    }
    
    /** Сырой SQL */
    public static ExecutableRawBuilder raw(String sql) {
        return instance().raw(sql);
    }
    
    // === SQL из файлов ===
    
    /** Выполнить SQL скрипт из classpath */
    public static void executeScript(String classpathResource) {
        instance().executeScript(classpathResource);
    }
    
    /** Выполнить SQL скрипт из файла */
    public static void executeScript(Path path) {
        instance().executeScript(path);
    }
    
    /** Загрузить SQL из classpath для выполнения с параметрами */
    public static ExecutableRawBuilder fromResource(String classpathResource) {
        return instance().fromResource(classpathResource);
    }
    
    // === Транзакции ===
    
    /** 
     * Выполнить код в транзакции.
     * COMMIT при успехе, ROLLBACK при исключении.
     */
    public static <T> T transaction(Supplier<T> action) {
        return instance().transaction(action);
    }
    
    /** Транзакция без возврата значения */
    public static void transaction(Runnable action) {
        transaction(() -> { action.run(); return null; });
    }
    
    // === Lifecycle ===
    
    /** Закрыть default instance */
    public static void shutdown() {
        DbInstance old = INSTANCE.getAndSet(null);
        if (old != null) old.close();
    }
    
    /** Закрыть все instances */
    public static void shutdownAll() {
        shutdown();
        NAMED_INSTANCES.values().forEach(DbInstance::close);
        NAMED_INSTANCES.clear();
    }
    
    private static DbInstance instance() {
        DbInstance inst = INSTANCE.get();
        if (inst == null) {
            throw new IllegalStateException("Db не настроен. Вызовите Db.configure() сначала.");
        }
        return inst;
    }
}
```

### DbInstance — экземпляр для конкретного DataSource

```java
package com.framework.hex.db;

/**
 * Экземпляр DB для конкретного DataSource.
 * Используется для multi-datasource сценариев.
 * 
 * Потокобезопасен. Транзакции изолированы через ThreadLocal.
 */
public class DbInstance implements AutoCloseable {
    
    private final DbConfig config;
    private final QueryExecutor executor;
    private final ThreadLocal<Connection> transactionConnection = new ThreadLocal<>();
    
    // === Builders ===
    
    public ExecutableSelectBuilder select(String... columns) {
        return new ExecutableSelectBuilder(columns, executor);
    }
    
    public ExecutableSelectBuilder selectAll() {
        return select("*");
    }
    
    public ExecutableInsertBuilder insertInto(String table) {
        return new ExecutableInsertBuilder(table, executor);
    }
    
    public ExecutableBatchInsertBuilder batchInsertInto(String table) {
        return new ExecutableBatchInsertBuilder(table, executor);
    }
    
    public ExecutableUpdateBuilder update(String table) {
        return new ExecutableUpdateBuilder(table, executor);
    }
    
    public ExecutableDeleteBuilder deleteFrom(String table) {
        return new ExecutableDeleteBuilder(table, executor);
    }
    
    public ExecutableRawBuilder raw(String sql) {
        return new ExecutableRawBuilder(sql, executor);
    }
    
    // === SQL из файлов ===
    
    /** Выполнить SQL скрипт из classpath (schema.sql, test-data.sql) */
    public void executeScript(String classpathResource) {
        // Читает файл, разбивает по ; и выполняет каждый statement
    }
    
    /** Выполнить SQL скрипт из файла */
    public void executeScript(Path path) {
        // Читает файл, разбивает по ; и выполняет каждый statement
    }
    
    /** Загрузить SQL из classpath для выполнения с параметрами */
    public ExecutableRawBuilder fromResource(String classpathResource) {
        String sql = readResource(classpathResource);
        return raw(sql);
    }
    
    // === Транзакции ===
    
    /**
     * Выполнить код в транзакции.
     * 
     * Вложенные вызовы используют ту же транзакцию.
     * COMMIT при успехе, ROLLBACK при любом исключении.
     */
    public <T> T transaction(Supplier<T> action) {
        // Если уже в транзакции — просто выполнить action
        if (transactionConnection.get() != null) {
            return action.get();
        }
        
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            transactionConnection.set(conn);
            
            T result = action.get();
            
            conn.commit();
            return result;
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw (e instanceof RuntimeException) ? (RuntimeException) e : new DbException("Transaction failed", e);
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
    
    @Override
    public void close() {
        // Закрыть DataSource если это пул (HikariDataSource)
    }
}
```

### DbConfig — конфигурация

```java
package com.framework.hex.db;

/**
 * Конфигурация hex-db.
 */
public class DbConfig {
    
    private DataSource dataSource;
    private QueryInterceptor interceptor;  // для логирования SQL
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        
        private final DbConfig config = new DbConfig();
        
        /** Установить DataSource */
        public Builder dataSource(DataSource ds) {
            config.dataSource = ds;
            return this;
        }
        
        /** Простая настройка через JDBC URL */
        public Builder jdbc(String url, String username, String password) {
            // Создаёт простой DataSource
        }
        
        /** Настройка HikariCP (рекомендуется) */
        public Builder hikari(Consumer<HikariConfig> configurer) {
            HikariConfig hc = new HikariConfig();
            configurer.accept(hc);
            config.dataSource = new HikariDataSource(hc);
            return this;
        }
        
        /** Установить интерцептор для логирования SQL */
        public Builder interceptor(QueryInterceptor interceptor) {
            config.interceptor = interceptor;
            return this;
        }
        
        /** Включить логирование SQL */
        public Builder enableLogging() {
            config.interceptor = new LoggingInterceptor();
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

## 5. Интерцептор (опционально)

```java
package com.framework.hex.db.core;

/**
 * Интерцептор для логирования SQL.
 * Единственный метод — максимальная простота.
 */
@FunctionalInterface
public interface QueryInterceptor {
    
    /**
     * Вызывается перед выполнением запроса.
     * Может модифицировать Query (для добавления hints и т.д.)
     * 
     * @param query исходный запрос
     * @return модифицированный запрос (или тот же)
     */
    Query beforeExecute(Query query);
}
```

```java
package com.framework.hex.db.interceptors;

/**
 * Логирует SQL с интерполированными параметрами.
 * Включается через DbConfig.builder().enableLogging()
 */
public class LoggingInterceptor implements QueryInterceptor {
    
    private static final Logger log = LoggerFactory.getLogger("hex.db.sql");
    
    @Override
    public Query beforeExecute(Query query) {
        if (log.isDebugEnabled()) {
            log.debug("SQL: {}", interpolate(query.sql(), query.parameters()));
        }
        return query;
    }
    
    /** Подставляет параметры в SQL для читаемости (только для логов!) */
    private String interpolate(String sql, Map<String, Object> params) {
        String result = sql;
        for (var e : params.entrySet()) {
            result = result.replace(":" + e.getKey(), formatValue(e.getValue()));
        }
        return result;
    }
}
```

---

## 6. Использование в тестах

### Базовое использование

```java
class UserServiceTest {
    
    @BeforeAll
    static void setup() {
        Db.configure(DbConfig.builder()
            .jdbc("jdbc:postgresql://localhost:5432/test", "user", "pass")
            .enableLogging()
            .build());
        
        Db.executeScript("db/schema.sql");
        Db.executeScript("db/test-data.sql");
    }
    
    @AfterAll
    static void cleanup() {
        Db.shutdown();
    }
    
    @Test
    void shouldFindActiveUsers() {
        List<Map<String, Object>> users = Db.selectAll()
            .from("users")
            .where("active", true)
            .orderBy("name")
            .toList();
        
        assertThat(users).isNotEmpty();
    }
    
    @Test
    void shouldFindUserById() {
        Optional<Map<String, Object>> user = Db.selectAll()
            .from("users")
            .where("id", 1L)
            .first();
        
        assertThat(user).isPresent();
    }
    
    @Test
    void shouldCountUsers() {
        long count = Db.select("COUNT(*)")
            .from("users")
            .where("active", true)
            .count();
        
        assertThat(count).isGreaterThan(0);
    }
    
    @Test
    void shouldCreateUser() {
        Long id = Db.insertInto("users")
            .value("name", "Test User")
            .value("email", "test@example.com")
            .value("active", true)
            .executeAndGetKey(Long.class);
        
        assertThat(id).isPositive();
        assertThat(Db.selectAll().from("users").where("id", id).exists()).isTrue();
    }
    
    @Test
    void shouldUpdateUser() {
        int affected = Db.update("users")
            .set("name", "Updated Name")
            .where("id", 1L)
            .execute();
        
        assertThat(affected).isEqualTo(1);
    }
    
    @Test
    void shouldDeleteUser() {
        Long id = Db.insertInto("users")
            .value("name", "To Delete")
            .value("email", "delete@test.com")
            .executeAndGetKey(Long.class);
        
        int deleted = Db.deleteFrom("users")
            .where("id", id)
            .execute();
        
        assertThat(deleted).isEqualTo(1);
    }
}
```

### С маппингом на record

```java
class UserMappingTest {
    
    record User(Long id, String name, String email, boolean active) {}
    
    // Маппер как константа — переиспользуем
    private static final RowMapper<User> USER_MAPPER = row -> new User(
        row.getLong("id"),
        row.getString("name"),
        row.getString("email"),
        row.getBoolean("active")
    );
    
    @Test
    void shouldMapToRecord() {
        List<User> users = Db.selectAll()
            .from("users")
            .where("active", true)
            .toList(USER_MAPPER);
        
        assertThat(users).allMatch(User::active);
    }
    
    @Test
    void shouldFindSingleUser() {
        User user = Db.selectAll()
            .from("users")
            .where("id", 1L)
            .single(USER_MAPPER);
        
        assertThat(user.id()).isEqualTo(1L);
    }
}
```

### Сырой SQL для сложных запросов

```java
class ComplexQueryTest {
    
    @Test
    void shouldExecuteComplexQuery() {
        List<Map<String, Object>> results = Db.raw("""
            WITH user_stats AS (
                SELECT user_id, COUNT(*) as order_count
                FROM orders
                WHERE created_at > :since
                GROUP BY user_id
            )
            SELECT u.name, s.order_count
            FROM users u
            JOIN user_stats s ON u.id = s.user_id
            WHERE s.order_count > :minOrders
            """)
            .param("since", LocalDateTime.now().minusMonths(1))
            .param("minOrders", 5)
            .toList();
        
        assertThat(results).isNotEmpty();
    }
    
    @Test
    void shouldLoadSqlFromFile() {
        List<Map<String, Object>> report = Db.fromResource("queries/monthly-report.sql")
            .param("year", 2024)
            .param("month", 12)
            .toList();
        
        assertThat(report).isNotEmpty();
    }
}
```

### Транзакции

```java
class TransactionTest {
    
    @Test
    void shouldCommitTransaction() {
        Long orderId = Db.transaction(() -> {
            Long id = Db.insertInto("orders")
                .value("user_id", 1L)
                .value("total", BigDecimal.valueOf(100))
                .executeAndGetKey(Long.class);
            
            Db.update("users")
                .set("order_count", Db.select("order_count + 1").from("users").where("id", 1L).scalar(Integer.class))
                .where("id", 1L)
                .execute();
            
            return id;
        });
        
        assertThat(orderId).isPositive();
    }
    
    @Test
    void shouldRollbackOnError() {
        Long userId = 999L;
        
        assertThrows(RuntimeException.class, () -> {
            Db.transaction(() -> {
                Db.insertInto("orders")
                    .value("user_id", userId)
                    .value("total", BigDecimal.valueOf(100))
                    .execute();
                
                throw new RuntimeException("Simulated error");
            });
        });
        
        // Заказ не создан
        assertThat(Db.selectAll().from("orders").where("user_id", userId).exists()).isFalse();
    }
}
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
    }
}
```

### Условные запросы

```java
class ConditionalQueryTest {
    
    @Test
    void shouldBuildConditionalQuery() {
        // Параметры фильтрации (могут быть null)
        String nameFilter = "John";
        Boolean activeFilter = null;
        
        List<Map<String, Object>> users = Db.selectAll()
            .from("users")
            .whereIf(nameFilter != null, "name", nameFilter)
            .whereIf(activeFilter != null, "active", activeFilter)
            .toList();
        
        // SQL: SELECT * FROM users WHERE name = ?
        // (active не добавлен, т.к. activeFilter = null)
    }
}
```

### Multi-DataSource

```java
class MultiDataSourceTest {
    
    @BeforeAll
    static void setup() {
        Db.register("primary", DbConfig.builder()
            .jdbc("jdbc:postgresql://primary:5432/app", "user", "pass")
            .build());
        
        Db.register("analytics", DbConfig.builder()
            .jdbc("jdbc:postgresql://analytics:5432/app", "reader", "pass")
            .build());
    }
    
    @Test
    void shouldQueryDifferentDatabases() {
        // Запись в primary
        Long id = Db.use("primary").insertInto("users")
            .value("name", "Test")
            .executeAndGetKey(Long.class);
        
        // Чтение из analytics
        Optional<Map<String, Object>> user = Db.use("analytics").selectAll()
            .from("users")
            .where("id", id)
            .first();
        
        assertThat(user).isPresent();
    }
}
```

---

## Структура файлов

```
hex-db/
├── hex-db-core/
│   └── src/main/java/com/framework/hex/db/core/
│       ├── Row.java
│       ├── RowMapper.java
│       ├── Query.java
│       ├── QueryResult.java
│       ├── QueryExecutor.java
│       ├── QueryInterceptor.java
│       └── DbException.java
│
├── hex-db-builders/
│   └── src/main/java/com/framework/hex/db/builders/
│       ├── SQL.java
│       ├── QueryBuilder.java
│       ├── SelectBuilder.java
│       ├── InsertBuilder.java
│       ├── UpdateBuilder.java
│       ├── DeleteBuilder.java
│       ├── BatchInsertBuilder.java
│       ├── RawQueryBuilder.java
│       ├── SimpleQuery.java
│       └── SqlScriptParser.java
│
├── hex-db-executor/
│   └── src/main/java/com/framework/hex/db/executor/
│       ├── DefaultQueryExecutor.java
│       ├── SelectQueryResult.java
│       ├── ModificationQueryResult.java
│       └── MapRow.java
│
└── hex-db/
    └── src/main/java/com/framework/hex/db/
        ├── Db.java
        ├── DbInstance.java
        ├── DbConfig.java
        └── interceptors/
            └── LoggingInterceptor.java
```

---
