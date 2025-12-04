# Анализ дизайна hex-db библиотеки

**Дата:** 2024-12-04  
**Статус:** Ревью архитектуры  
**Фокус:** Скорость написания тестов, гибкость, безопасность в многопотоке, расширяемость

---

## Общий вердикт: 8/10 — Отличная база с несколькими критическими улучшениями

Дизайн **концептуально правильный** — это именно то, что нужно автотестерам:
- Минималистичный API без boilerplate
- Fluent builders для читаемости
- Sensible defaults из коробки
- Точки расширения для продвинутых сценариев

Однако есть **критические проблемы с thread safety** и несколько архитектурных улучшений, которые сделают библиотеку production-ready.

---

## 🔴 КРИТИЧЕСКИЕ ПРОБЛЕМЫ

### 1. Thread Safety статического `Db` класса

**Проблема:**
```java
private static volatile DbInstance defaultInstance;  // volatile недостаточно!

public static void configure(DbConfig config) {
    synchronized (LOCK) {
        if (defaultInstance != null) {
            defaultInstance.close();  // ← Закрываем, пока кто-то может использовать!
        }
        defaultInstance = new DbInstance(config);
    }
}
```

**Сценарий гонки:**
1. Thread A вызывает `Db.selectAll()` → получает `instance`
2. Thread B вызывает `Db.configure(newConfig)` → закрывает старый instance
3. Thread A продолжает работать с закрытым DataSource → 💥 SQLException

**Решение:** Использовать `AtomicReference` с immutable swap или reference counting:

```java
public final class Db {
    private static final AtomicReference<DbInstance> INSTANCE = new AtomicReference<>();
    
    public static void configure(DbConfig config) {
        DbInstance newInstance = new DbInstance(config);
        DbInstance old = INSTANCE.getAndSet(newInstance);
        // Старый instance НЕ закрываем сразу — он может использоваться
        // Вариант 1: Пользователь сам закрывает через shutdown()
        // Вариант 2: Reference counting + phantom reference
    }
    
    public static void shutdown() {
        DbInstance old = INSTANCE.getAndSet(null);
        if (old != null) {
            old.close();
        }
    }
}
```

---

### 2. Builders не thread-safe (и не должны быть shared)

**Проблема в документации:** Нет явного указания, что builders — mutable и NOT thread-safe.

**Решение:** Добавить в JavaDoc:

```java
/**
 * Билдер SELECT запросов.
 * 
 * <p><b>Thread Safety:</b> Builders are NOT thread-safe and should not be
 * shared between threads. Each thread should create its own builder instance.
 * 
 * <p><b>Usage Pattern:</b>
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
```

---

### 3. Connection Management в `DefaultQueryExecutor`

**Проблема:**
```java
@Override
public QueryResult execute(Query query) {
    try {
        Connection conn = getConnection();
        try {
            return doExecute(conn, processedQuery);  // ResultSet держит connection!
        } finally {
            releaseConnection(conn);  // ← Закрываем ДО чтения ResultSet!
        }
    }
}
```

Для SELECT возвращается `ResultSetQueryResult`, который держит открытый `ResultSet`. Но connection уже закрыт в `finally`!

**Решение:** Connection должен закрываться только после полного чтения ResultSet:

```java
private QueryResult doExecute(Connection conn, Query query) throws SQLException {
    // ...
    if (query.type() == QueryType.SELECT) {
        ResultSet rs = stmt.executeQuery();
        // Connection передаётся в результат и будет закрыт в close()
        return new ResultSetQueryResult(rs, stmt, conn);
    } else {
        int affected = stmt.executeUpdate();
        List<Object> keys = query.returnGeneratedKeys() 
            ? extractGeneratedKeys(stmt) 
            : List.of();
        stmt.close();
        releaseConnection(conn);  // Закрываем только для modification queries
        return new ModificationQueryResult(affected, keys);
    }
}
```

И в `ResultSetQueryResult`:

```java
public class ResultSetQueryResult implements QueryResult {
    private final ResultSet resultSet;
    private final Statement statement;
    private final Connection connection;  // ← Добавляем!
    
    @Override
    public void close() {
        try {
            resultSet.close();
            statement.close();
            connection.close();  // ← Теперь закрываем здесь
        } catch (SQLException e) {
            // Log
        }
    }
}
```

---

### 4. Stream без try-with-resources warning

**Проблема:**
```java
/** Стрим строк для больших результатов */
Stream<Row> stream();
```

Stream нужно закрывать! Но пользователи часто забывают.

**Решение:** Документация + возможно wrapper:

```java
/**
 * Stream rows for large result sets.
 * 
 * <p><b>⚠️ IMPORTANT:</b> The returned Stream MUST be closed to release 
 * database resources. Use try-with-resources:
 * 
 * <pre>{@code
 * try (Stream<Row> rows = result.stream()) {
 *     rows.filter(...).forEach(...);
 * }
 * }</pre>
 * 
 * <p>Or use terminal operations that auto-close (recommended):
 * <pre>{@code
 * result.toList(mapper);     // ✅ auto-closes
 * result.firstRow(mapper);   // ✅ auto-closes
 * }</pre>
 */
Stream<Row> stream();
```

---

## 🟡 ВАЖНЫЕ УЛУЧШЕНИЯ

### 5. Transaction Support — КРИТИЧЕСКИ ВАЖНО для тестов!

Тесты часто требуют транзакций для изоляции и rollback. Текущий дизайн это не поддерживает.

**Добавить:**

```java
public final class Db {
    
    /**
     * Выполнить код в транзакции.
     * Автоматический COMMIT при успехе, ROLLBACK при исключении.
     */
    public static <T> T transaction(Supplier<T> action) {
        return instance().transaction(action);
    }
    
    public static void transaction(Runnable action) {
        transaction(() -> { action.run(); return null; });
    }
}

public class DbInstance {
    
    // ThreadLocal для connection в транзакции
    private final ThreadLocal<Connection> transactionConnection = new ThreadLocal<>();
    
    public <T> T transaction(Supplier<T> action) {
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
            throw (e instanceof RuntimeException re) ? re : new DbException("Transaction failed", e);
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
}
```

**Использование в тестах:**

```java
@Test
void shouldRollbackOnFailure() {
    assertThrows(RuntimeException.class, () -> {
        Db.transaction(() -> {
            Db.insertInto("users").value("name", "Test").execute();
            throw new RuntimeException("Simulated failure");
        });
    });
    
    // Данные откатились
    assertThat(Db.selectAll().from("users").where("name", "Test").exists()).isFalse();
}
```

---

### 6. Test Fixtures / Data Builders

Для автотестеров очень полезно иметь утилиты для быстрого создания тестовых данных:

```java
package com.framework.hex.db.testing;

/**
 * Удобные утилиты для создания тестовых данных.
 */
public final class TestData {
    
    /**
     * Вставить строку и вернуть её с ID.
     */
    public static Map<String, Object> insertAndGet(String table, Map<String, Object> values) {
        Long id = Db.insertInto(table)
            .values(values)
            .executeAndGetKey(Long.class);
        
        return Db.selectAll()
            .from(table)
            .where("id", id)
            .single();
    }
    
    /**
     * Вставить строку с автозаполнением обязательных полей.
     */
    public static InsertBuilder quickInsert(String table) {
        return Db.insertInto(table)
            .value("created_at", LocalDateTime.now())
            .value("updated_at", LocalDateTime.now());
    }
    
    /**
     * Очистить таблицу (для setUp/tearDown).
     */
    public static void truncate(String table) {
        Db.raw("TRUNCATE TABLE " + table + " CASCADE").execute();
    }
    
    /**
     * Очистить несколько таблиц в правильном порядке.
     */
    public static void truncate(String... tables) {
        for (String table : tables) {
            truncate(table);
        }
    }
}
```

---

### 7. Named DataSources API улучшение

Текущий multi-datasource через `Db.create()` неудобен. Лучше:

```java
public final class Db {
    
    private static final Map<String, DbInstance> INSTANCES = new ConcurrentHashMap<>();
    
    /**
     * Зарегистрировать именованный DataSource.
     */
    public static void register(String name, DbConfig config) {
        INSTANCES.put(name, new DbInstance(config));
    }
    
    /**
     * Получить именованный instance.
     */
    public static DbInstance use(String name) {
        DbInstance instance = INSTANCES.get(name);
        if (instance == null) {
            throw new IllegalStateException("DataSource '" + name + "' not registered");
        }
        return instance;
    }
}

// Использование:
Db.register("primary", primaryConfig);
Db.register("analytics", analyticsConfig);

Db.use("primary").insertInto("users")...;
Db.use("analytics").selectAll().from("reports")...;
```

---

### 8. Batch Insert API

Для тестов часто нужно вставить много данных:

```java
public class BatchInsertBuilder {
    
    private final String table;
    private final List<String> columns = new ArrayList<>();
    private final List<Map<String, Object>> rows = new ArrayList<>();
    
    public BatchInsertBuilder columns(String... cols) {
        columns.addAll(Arrays.asList(cols));
        return this;
    }
    
    public BatchInsertBuilder row(Object... values) {
        if (values.length != columns.size()) {
            throw new IllegalArgumentException("Values count must match columns count");
        }
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            row.put(columns.get(i), values[i]);
        }
        rows.add(row);
        return this;
    }
    
    public BatchInsertBuilder rows(List<Map<String, Object>> rows) {
        this.rows.addAll(rows);
        return this;
    }
    
    public int[] execute() {
        // Использует executeBatch
    }
}

// Использование:
Db.batchInsertInto("users")
    .columns("name", "email", "active")
    .row("John", "john@test.com", true)
    .row("Jane", "jane@test.com", true)
    .row("Bob", "bob@test.com", false)
    .execute();
```

---

### 9. Улучшенная обработка NULL в условиях

**Проблема:** `where("column", null)` создаст `column = NULL`, что всегда false в SQL.

**Решение:**

```java
public SelectBuilder where(String column, Object value) {
    if (value == null) {
        return whereNull(column);
    }
    return where(column, "=", value);
}

// Или явный метод:
public SelectBuilder whereEquals(String column, Object value) {
    // Всегда использует =, даже для null (для тех кто точно знает что делает)
}
```

---

### 10. Query Logging с параметрами

**Проблема:** В `LoggingInterceptor` параметры выводятся как Map, что неудобно для отладки.

**Улучшение:**

```java
public class LoggingInterceptor implements QueryInterceptor {
    
    @Override
    public Query beforeExecute(Query query) {
        String interpolated = interpolateSql(query.sql(), query.parameters());
        log.debug("→ SQL: {}", interpolated);
        return query;
    }
    
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
        if (value instanceof String) return "'" + value + "'";
        if (value instanceof LocalDateTime) return "'" + value + "'";
        return String.valueOf(value);
    }
}
```

---

## 🟢 ЧТО УЖЕ ХОРОШО

### ✅ Fluent API Design
Отличная эргономика для написания тестов:
```java
Db.selectAll().from("users").where("active", true).toList();
```

### ✅ Conditional Building (`whereIf`)
Очень полезно для динамических фильтров в тестах.

### ✅ Модульная структура
Правильное разделение на core/builders/executor позволяет подключать только нужное.

### ✅ RowMapper как точка расширения
Функциональный интерфейс — просто и гибко.

### ✅ Защита от опасных операций
`DELETE/UPDATE без WHERE` выбрасывает исключение — отлично!

### ✅ Raw SQL escape hatch
Для сложных случаев можно использовать `Db.raw()` или прямой `Connection`.

---

## 📋 РЕКОМЕНДАЦИИ ПО ПРИОРИТЕТАМ

### P0 — Блокеры (исправить до релиза)
1. ❌ Connection management для SELECT — данные не будут читаться
2. ❌ Thread safety `Db.configure()` — гонки в параллельных тестах

### P1 — Важно для adoption
3. ⚠️ Transaction support — необходимо для тестовой изоляции
4. ⚠️ Batch insert — скорость подготовки тестовых данных
5. ⚠️ Документация thread safety

### P2 — Nice to have
6. 💡 TestData utilities
7. 💡 Named datasources registry
8. 💡 NULL handling в where
9. 💡 Query logging improvements

---

## 🔧 ПРЕДЛАГАЕМЫЕ ДОПОЛНИТЕЛЬНЫЕ ИНТЕРФЕЙСЫ

### TestRule/Extension для JUnit

```java
public class DbTestExtension implements BeforeAllCallback, AfterAllCallback, 
                                         BeforeEachCallback, AfterEachCallback {
    
    private final DbConfig config;
    private final boolean rollbackAfterEach;
    
    @Override
    public void beforeEach(ExtensionContext context) {
        if (rollbackAfterEach) {
            // Начинаем транзакцию
            Db.beginTransaction();
        }
    }
    
    @Override
    public void afterEach(ExtensionContext context) {
        if (rollbackAfterEach) {
            // Откатываем — тесты изолированы!
            Db.rollback();
        }
    }
}

// Использование:
@ExtendWith(DbTestExtension.class)
@DbConfig(url = "jdbc:h2:mem:test", rollbackAfterEach = true)
class UserServiceTest {
    // Каждый тест в своей транзакции, автоматический rollback
}
```

---

## 📊 ИТОГОВАЯ ОЦЕНКА

| Критерий | Оценка | Комментарий |
|----------|--------|-------------|
| Скорость написания тестов | 9/10 | Отличный fluent API |
| Гибкость | 8/10 | Хорошие точки расширения |
| Thread Safety | 5/10 | Критические проблемы! |
| Расширяемость | 8/10 | Интерцепторы, модульность |
| Sensible Defaults | 7/10 | Нет транзакций из коробки |
| Документация | 6/10 | Нужны предупреждения о thread safety |

**Общая оценка: 7.5/10** → После исправления P0/P1 будет **9/10**

---

## ЗАКЛЮЧЕНИЕ

Дизайн библиотеки **концептуально правильный** и хорошо подходит для автотестеров. Основные улучшения:

1. **Исправить connection lifecycle** — это баг, не фича
2. **Добавить транзакции** — must-have для тестов  
3. **Документировать thread safety** — предотвратит ошибки пользователей
4. **Добавить batch operations** — ускорит подготовку данных

После этих изменений библиотека будет отличным инструментом для команд автоматизации.
