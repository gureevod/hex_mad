# Руководство по работе с декларативным DB модулем Hex Framework

## Содержание

1. [Введение](#введение)
2. [Быстрый старт](#быстрый-старт)
3. [Архитектура модуля](#архитектура-модуля)
4. [Аннотации](#аннотации)
5. [Создание DB сервисов](#создание-db-сервисов)
6. [Маппинг результатов](#маппинг-результатов)
7. [Валидаторы запросов](#валидаторы-запросов)
8. [Интерцепторы](#интерцепторы)
9. [Управление соединениями и транзакциями](#управление-соединениями-и-транзакциями)
10. [Конфигурация](#конфигурация)
11. [Escape Hatch: Прямой доступ](#escape-hatch-прямой-доступ)
12. [Обработка ошибок](#обработка-ошибок)
13. [Потокобезопасность](#потокобезопасность)
14. [Лучшие практики](#лучшие-практики)

---

## Введение

DB модуль Hex Framework предоставляет декларативный подход к работе с базами данных в тестах, вдохновлённый Spring Data JDBC и MyBatis. Модуль спроектирован для потокобезопасного параллельного выполнения в JUnit 5.

### Преимущества

| Аспект | Императивный JDBC | Декларативный подход |
|--------|-------------------|----------------------|
| Boilerplate | Много try-catch-finally | Минимум кода |
| Маппинг | Ручной ResultSet → Object | Автоматический |
| SQL Injection | Легко допустить ошибку | Prepared statements по умолчанию |
| Ресурсы | Ручное управление | Автоматическое |
| Читаемость | SQL скрыт в коде | SQL как контракт |
| Валидация | Только в runtime | Compile-time + pre-execution |

---

## Быстрый старт

### Шаг 1: Создайте Entity

```java
package com.company.hex.project.db.entity;

import com.company.hex.db.annotations.mapping.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class UserEntity {
    
    @Id
    @Column("id")
    private Long id;
    
    @Column("username")
    private String username;
    
    @Column("email")
    private String email;
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @Column("is_active")
    private boolean active;
}
```

### Шаг 2: Создайте Repository интерфейс

```java
package com.company.hex.project.db.repository;

import com.company.hex.db.annotations.config.DbService;
import com.company.hex.db.annotations.query.*;
import com.company.hex.db.annotations.param.*;
import com.company.hex.project.db.entity.UserEntity;

import java.util.List;
import java.util.Optional;

@DbService(dataSource = "primary")
public interface UserRepository {
    
    @Select("SELECT * FROM users WHERE id = :id")
    Optional<UserEntity> findById(@Param("id") Long id);
    
    @Select("SELECT * FROM users WHERE is_active = true")
    List<UserEntity> findAllActive();
    
    @Insert("INSERT INTO users (username, email, is_active) VALUES (:username, :email, :active)")
    @ReturnGeneratedKeys
    Long create(@Param("username") String username, 
                @Param("email") String email,
                @Param("active") boolean active);
    
    @Update("UPDATE users SET email = :email WHERE id = :id")
    int updateEmail(@Param("id") Long id, @Param("email") String email);
    
    @Delete("DELETE FROM users WHERE id = :id")
    int deleteById(@Param("id") Long id);
}
```

### Шаг 3: Используйте в тестах

```java
import com.company.hex.db.service.DbServiceFactory;
import org.junit.jupiter.api.Test;

class UserRepositoryTest {
    
    @Test
    void shouldFindUserById() {
        UserRepository userRepo = DbServiceFactory.create(UserRepository.class);
        
        Optional<UserEntity> user = userRepo.findById(1L);
        
        assertThat(user).isPresent();
        assertThat(user.get().getUsername()).isNotEmpty();
    }
    
    @Test
    void shouldCreateAndDeleteUser() {
        UserRepository userRepo = DbServiceFactory.create(UserRepository.class);
        
        // Create
        Long newId = userRepo.create("testuser", "test@example.com", true);
        assertThat(newId).isPositive();
        
        // Verify
        Optional<UserEntity> created = userRepo.findById(newId);
        assertThat(created).isPresent();
        
        // Delete
        int deleted = userRepo.deleteById(newId);
        assertThat(deleted).isEqualTo(1);
    }
}
```

---

## Архитектура модуля

```
┌──────────────────────────────────────────────────────────────────┐
│                    Annotated Interface                            │
│                   (UserRepository.java)                           │
└──────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌──────────────────────────────────────────────────────────────────┐
│                      DbProxyHandler                               │
│  - Перехватывает вызовы методов                                   │
│  - Оркестрирует процесс обработки                                │
└──────────────────────────────────────────────────────────────────┘
                              │
          ┌───────────────────┼───────────────────┐
          ▼                   ▼                   ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│ Query           │ │ Interceptor     │ │ Result          │
│ Processor       │ │ Chain           │ │ Mapper          │
│                 │ │                 │ │                 │
│ Парсит аннотации│ │ Logging, Timeout│ │ ResultSet →     │
│ + валидация     │ │ Transaction     │ │ Java Objects    │
└────────┬────────┘ └────────┬────────┘ └─────────────────┘
         │                   │
         ▼                   ▼
┌─────────────────┐ ┌─────────────────┐
│ Query           │ │ Connection      │
│ Validator       │ │ Provider        │
│                 │ │                 │
│ Pre-execution   │ │ HikariCP Pool   │
│ SQL checks      │ │ Thread-safe     │
└─────────────────┘ └─────────────────┘
                             │
                             ▼
                   ┌─────────────────┐
                   │ Query           │
                   │ Executor        │
                   │                 │
                   │ JDBC execution  │
                   └─────────────────┘
```

### Основные компоненты

| Компонент | Класс | Описание |
|-----------|-------|----------|
| **Фабрика сервисов** | `DbServiceFactory` | Создаёт прокси-экземпляры репозиториев |
| **Proxy Handler** | `DbProxyHandler` | Перехватывает вызовы методов интерфейса |
| **Процессор запросов** | `QueryProcessor` | Парсит SQL и параметры из аннотаций |
| **Валидатор** | `QueryValidator` | Статическая валидация SQL перед выполнением |
| **Цепочка интерцепторов** | `DbInterceptorChain` | Pre/post обработка запросов |
| **Connection Provider** | `ConnectionProvider` | Потокобезопасное управление соединениями |
| **Query Executor** | `QueryExecutor` | Выполняет SQL через JDBC |
| **Result Mapper** | `ResultMapper` | Преобразует ResultSet в объекты |

---

## Аннотации

### Аннотации запросов

| Аннотация | Описание | Возвращаемые типы |
|-----------|----------|-------------------|
| `@Select` | SELECT запрос | `T`, `Optional<T>`, `List<T>`, `Stream<T>` |
| `@Insert` | INSERT запрос | `void`, `int`, `Long` (с `@ReturnGeneratedKeys`) |
| `@Update` | UPDATE запрос | `void`, `int` (affected rows) |
| `@Delete` | DELETE запрос | `void`, `int` (affected rows) |
| `@Call` | Stored procedure | Зависит от процедуры |
| `@Script` | Выполнение SQL скрипта | `void` |

```java
public interface ProductRepository {
    
    // SELECT - возвращает данные
    @Select("SELECT * FROM products WHERE id = :id")
    Optional<ProductEntity> findById(@Param("id") Long id);
    
    @Select("SELECT * FROM products WHERE category = :category ORDER BY price")
    List<ProductEntity> findByCategory(@Param("category") String category);
    
    @Select("SELECT * FROM products WHERE price > :minPrice")
    Stream<ProductEntity> streamExpensiveProducts(@Param("minPrice") BigDecimal minPrice);
    
    // INSERT - создание записей
    @Insert("INSERT INTO products (name, price, category) VALUES (:name, :price, :category)")
    void create(@Param("name") String name, 
                @Param("price") BigDecimal price,
                @Param("category") String category);
    
    @Insert("INSERT INTO products (name, price) VALUES (:name, :price)")
    @ReturnGeneratedKeys
    Long createAndReturnId(@Param("name") String name, @Param("price") BigDecimal price);
    
    // UPDATE - обновление записей
    @Update("UPDATE products SET price = :price WHERE id = :id")
    int updatePrice(@Param("id") Long id, @Param("price") BigDecimal price);
    
    // DELETE - удаление записей
    @Delete("DELETE FROM products WHERE id = :id")
    int deleteById(@Param("id") Long id);
    
    // CALL - stored procedures
    @Call("{call calculate_discount(:productId, :percentage)}")
    void applyDiscount(@Param("productId") Long productId, 
                       @Param("percentage") int percentage);
    
    // SCRIPT - выполнение SQL скрипта
    @Script(resource = "db/scripts/cleanup_old_products.sql")
    void cleanupOldProducts();
}
```

### Аннотации параметров

| Аннотация | Описание | Пример |
|-----------|----------|--------|
| `@Param` | Именованный параметр | `@Param("id") Long id` |
| `@ParamList` | Коллекция для IN clause | `@ParamList("ids") List<Long> ids` |
| `@OutParam` | OUT параметр процедуры | `@OutParam("result") Integer result` |
| `@InOutParam` | INOUT параметр | `@InOutParam("counter")` |
| `@NullableParam` | Явно nullable | `@NullableParam("filter") String filter` |

```java
public interface OrderRepository {
    
    // Именованные параметры
    @Select("SELECT * FROM orders WHERE user_id = :userId AND status = :status")
    List<OrderEntity> findByUserAndStatus(@Param("userId") Long userId, 
                                           @Param("status") String status);
    
    // IN clause с коллекцией
    @Select("SELECT * FROM orders WHERE id IN (:ids)")
    List<OrderEntity> findByIds(@ParamList("ids") List<Long> ids);
    
    // Nullable параметр - условный WHERE
    @Select("""
        SELECT * FROM orders 
        WHERE user_id = :userId
        AND (:status IS NULL OR status = :status)
        """)
    List<OrderEntity> findByUserWithOptionalStatus(
        @Param("userId") Long userId,
        @NullableParam("status") String status);
    
    // OUT параметры в процедурах
    @Call("{call get_order_total(:orderId, :total)}")
    void getOrderTotal(@Param("orderId") Long orderId,
                       @OutParam(value = "total", sqlType = Types.DECIMAL) BigDecimal[] total);
}
```

### Конфигурационные аннотации

```java
@DbService(
    dataSource = "primary",                    // Имя DataSource из конфигурации
    queryTimeout = 30,                          // Таймаут в секундах  
    fetchSize = 100,                            // Размер выборки для больших результатов
    validateQueries = true                      // Включить pre-execution валидацию
)
public interface UserRepository {
    
    @Select("SELECT * FROM users WHERE id = :id")
    @Timeout(5)                                 // Переопределить таймаут для метода
    @Cache(ttl = 60)                            // Кэшировать результат на 60 секунд
    Optional<UserEntity> findById(@Param("id") Long id);
    
    @Select("SELECT * FROM users")
    @FetchSize(1000)                            // Большая выборка
    @ReadOnly                                   // Hint для оптимизации
    List<UserEntity> findAll();
    
    @Update("UPDATE users SET last_login = NOW() WHERE id = :id")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    int updateLastLogin(@Param("id") Long id);
    
    @Delete("DELETE FROM users WHERE created_at < :date")
    @DangerousQuery(reason = "Bulk delete", requireConfirmation = true)
    int deleteOldUsers(@Param("date") LocalDate date);
}
```

### Аннотации маппинга

```java
@Table("user_profiles")                         // Имя таблицы (если отличается от класса)
public class UserProfileEntity {
    
    @Id                                         // Первичный ключ
    @Column("profile_id")                       // Имя колонки
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column("user_name")
    private String userName;
    
    @Column(value = "created_at", readOnly = true)  // Только для чтения
    private LocalDateTime createdAt;
    
    @Column("preferences")
    @JsonColumn                                 // JSON колонка → Object маппинг
    private Map<String, Object> preferences;
    
    @Transient                                  // Игнорировать при маппинге
    private transient String tempData;
    
    @Column("status")
    @Enumerated(EnumType.STRING)               // Enum как строка
    private UserStatus status;
}
```

---

## Создание DB сервисов

### Простой подход

```java
// С конфигурацией по умолчанию
UserRepository userRepo = DbServiceFactory.create(UserRepository.class);

// С кастомной конфигурацией
DbConfig config = HexConfigFactory.getConfig(DbConfig.class);
UserRepository userRepo = DbServiceFactory.create(UserRepository.class, config);
```

### Builder API

```java
UserRepository userRepo = DbServiceFactory.builder()
    .withDataSource("primary")                          // Источник данных
    .withConfig(customConfig)                            // Кастомная конфигурация
    .addInterceptor(new QueryLoggingInterceptor())      // Добавить интерцептор
    .addInterceptor(new SlowQueryInterceptor(Duration.ofSeconds(5)))
    .withRowMapperFactory(customMapperFactory)           // Кастомный маппинг
    .withValidator(new StrictQueryValidator())           // Кастомный валидатор
    .enableQueryValidation(true)                         // Включить валидацию
    .create(UserRepository.class);
```

### Multi-DataSource

```java
// Основная БД
@DbService(dataSource = "primary")
public interface UserRepository { ... }

// Аналитическая БД (read-only)
@DbService(dataSource = "analytics", readOnly = true)
public interface AnalyticsRepository { ... }

// Legacy система
@DbService(dataSource = "legacy", schema = "old_schema")
public interface LegacyRepository { ... }
```

---

## Маппинг результатов

### Автоматический маппинг

```java
// Маппинг на Entity с @Column аннотациями
@Select("SELECT * FROM users WHERE id = :id")
Optional<UserEntity> findById(@Param("id") Long id);

// Маппинг на record (Java 16+)
public record UserRecord(Long id, String username, String email) {}

@Select("SELECT id, username, email FROM users WHERE id = :id")
Optional<UserRecord> findRecordById(@Param("id") Long id);

// Скалярные значения
@Select("SELECT COUNT(*) FROM users")
long countAll();

@Select("SELECT email FROM users WHERE id = :id")
Optional<String> findEmailById(@Param("id") Long id);

// Коллекции
@Select("SELECT DISTINCT email FROM users WHERE is_active = true")
Set<String> findAllActiveEmails();
```

### Кастомный маппинг

```java
public interface OrderRepository {
    
    // Inline маппинг
    @Select("SELECT o.*, u.username as user_name FROM orders o JOIN users u ON o.user_id = u.id")
    @RowMapping(OrderWithUserMapper.class)
    List<OrderWithUser> findAllWithUsers();
    
    // Маппинг для сложных типов
    @Select("SELECT * FROM orders WHERE data @> :jsonFilter::jsonb")
    @RowMapping(JsonOrderMapper.class)
    List<OrderEntity> findByJsonFilter(@Param("jsonFilter") String jsonFilter);
}

// Кастомный маппер
public class OrderWithUserMapper implements RowMapper<OrderWithUser> {
    
    @Override
    public OrderWithUser mapRow(ResultSet rs, int rowNum) throws SQLException {
        return OrderWithUser.builder()
            .orderId(rs.getLong("id"))
            .orderDate(rs.getTimestamp("created_at").toLocalDateTime())
            .userName(rs.getString("user_name"))
            .total(rs.getBigDecimal("total"))
            .items(parseItems(rs.getString("items_json")))
            .build();
    }
    
    private List<OrderItem> parseItems(String json) {
        // JSON parsing logic
    }
}
```

### Batch операции с маппингом

```java
public interface ProductRepository {
    
    // Batch insert с Entity
    @BatchInsert("INSERT INTO products (name, price) VALUES (:name, :price)")
    int[] createBatch(@ParamEntity List<ProductEntity> products);
    
    // Batch update
    @BatchUpdate("UPDATE products SET price = :price WHERE id = :id")
    int[] updatePrices(@ParamEntity List<ProductEntity> products);
}
```

---

## Валидаторы запросов

### Встроенные валидации

Валидатор `QueryValidator` выполняет проверки **до отправки запроса** в БД:

```java
public interface QueryValidator {
    
    /**
     * Валидирует запрос перед выполнением.
     * @throws QueryValidationException если запрос невалиден
     */
    void validate(QueryDefinition query);
}
```

#### Синтаксические проверки

```java
// ❌ Ошибка: несовпадение именованных параметров
@Select("SELECT * FROM users WHERE id = :userId")  // Параметр :userId
Optional<UserEntity> findById(@Param("id") Long id);  // Объявлен :id

// Исключение при создании прокси:
// QueryValidationException: Parameter mismatch in findById(): 
//   Query expects [:userId], but method declares [:id]

// ❌ Ошибка: неиспользуемый параметр
@Select("SELECT * FROM users WHERE id = :id")
Optional<UserEntity> findById(@Param("id") Long id, 
                               @Param("unused") String unused);
// Warning: Unused parameter 'unused' in findById()
```

#### Проверки безопасности

```java
// ⚠️ Предупреждение: UPDATE/DELETE без WHERE
@Update("UPDATE users SET is_active = false")  
int deactivateAll();
// Warning: UPDATE without WHERE clause in deactivateAll() - affects all rows

// ❌ Ошибка: опасные операции требуют явного подтверждения
@Script("DROP TABLE users")
void dropUsers();
// QueryValidationException: Dangerous operation DROP TABLE requires @DangerousQuery annotation

// ✅ Правильно
@Script("DROP TABLE users")
@DangerousQuery(reason = "Cleanup for test isolation", requireConfirmation = true)
void dropUsers();
```

#### Проверки типов

```java
// ❌ Ошибка: несоответствие возвращаемого типа
@Select("SELECT * FROM users WHERE id = :id")
UserEntity findById(@Param("id") Long id);  // Должен быть Optional для nullable
// Warning: SELECT may return null, consider using Optional<UserEntity>

// ❌ Ошибка: List для скалярного SELECT
@Select("SELECT COUNT(*) FROM users")
List<Long> countUsers();  // COUNT всегда возвращает одно значение
// Warning: Scalar query should not return List
```

### Кастомные валидаторы

```java
/**
 * Строгий валидатор для production-like окружения.
 */
public class StrictQueryValidator implements QueryValidator {
    
    private final SqlParser parser = new SqlParser();
    
    @Override
    public void validate(QueryDefinition query) {
        // 1. Базовые проверки синтаксиса
        validateSyntax(query);
        
        // 2. Проверка параметров
        validateParameters(query);
        
        // 3. Проверка опасных операций
        validateDangerousOperations(query);
        
        // 4. Проверка индексов (опционально)
        validateIndexUsage(query);
    }
    
    private void validateSyntax(QueryDefinition query) {
        try {
            parser.parse(query.getSql());
        } catch (SqlParseException e) {
            throw new QueryValidationException(
                "SQL syntax error in " + query.getMethodName() + ": " + e.getMessage(),
                query, e
            );
        }
    }
    
    private void validateParameters(QueryDefinition query) {
        Set<String> declaredParams = query.getDeclaredParameters();
        Set<String> queryParams = extractNamedParameters(query.getSql());
        
        // Проверка на отсутствующие параметры
        Set<String> missing = new HashSet<>(queryParams);
        missing.removeAll(declaredParams);
        if (!missing.isEmpty()) {
            throw new QueryValidationException(
                "Missing parameters " + missing + " in " + query.getMethodName(),
                query
            );
        }
        
        // Предупреждение о неиспользуемых
        Set<String> unused = new HashSet<>(declaredParams);
        unused.removeAll(queryParams);
        if (!unused.isEmpty()) {
            log.warn("Unused parameters {} in {}", unused, query.getMethodName());
        }
    }
    
    private void validateDangerousOperations(QueryDefinition query) {
        String sql = query.getSql().toUpperCase();
        
        if (containsDangerousOperation(sql) && !query.isDangerousAllowed()) {
            throw new QueryValidationException(
                "Dangerous operation detected. Use @DangerousQuery to explicitly allow.",
                query
            );
        }
        
        if (isModificationWithoutWhere(sql)) {
            throw new QueryValidationException(
                "UPDATE/DELETE without WHERE clause is not allowed without @DangerousQuery",
                query
            );
        }
    }
    
    private boolean containsDangerousOperation(String sql) {
        return sql.contains("DROP ") || 
               sql.contains("TRUNCATE ") || 
               sql.contains("ALTER ") ||
               sql.contains("CREATE ");
    }
    
    private boolean isModificationWithoutWhere(String sql) {
        return (sql.startsWith("UPDATE ") || sql.startsWith("DELETE ")) 
               && !sql.contains(" WHERE ");
    }
}

// Использование
UserRepository repo = DbServiceFactory.builder()
    .withValidator(new StrictQueryValidator())
    .create(UserRepository.class);
```

### Комбинирование валидаторов

```java
public class CompositeQueryValidator implements QueryValidator {
    
    private final List<QueryValidator> validators;
    
    public CompositeQueryValidator(QueryValidator... validators) {
        this.validators = List.of(validators);
    }
    
    @Override
    public void validate(QueryDefinition query) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        for (QueryValidator validator : validators) {
            try {
                validator.validate(query);
            } catch (QueryValidationException e) {
                if (e.isError()) {
                    errors.add(e.getMessage());
                } else {
                    warnings.add(e.getMessage());
                }
            }
        }
        
        // Логируем warnings
        warnings.forEach(w -> log.warn("Query validation: {}", w));
        
        // Бросаем исключение если есть ошибки
        if (!errors.isEmpty()) {
            throw new QueryValidationException(
                "Query validation failed:\n" + String.join("\n", errors),
                query
            );
        }
    }
}

// Использование
QueryValidator validator = new CompositeQueryValidator(
    new SyntaxValidator(),
    new ParameterValidator(),
    new SecurityValidator(),
    new PerformanceHintValidator()
);
```

---

## Интерцепторы

### Интерфейс DbInterceptor

```java
/**
 * Интерцептор для перехвата выполнения запросов к БД.
 * Thread-safe: каждый вызов получает независимый контекст.
 */
public interface DbInterceptor {
    
    /**
     * Перехватывает выполнение запроса.
     * 
     * @param chain цепочка выполнения
     * @return результат выполнения
     */
    QueryResult intercept(DbInterceptorChain chain);
    
    /**
     * Порядок в цепочке (меньше = раньше).
     */
    default int getOrder() {
        return 0;
    }
}

/**
 * Цепочка выполнения запросов.
 */
public interface DbInterceptorChain {
    
    /** Текущий запрос */
    QueryDefinition query();
    
    /** Контекст выполнения (connection, transaction, etc.) */
    ExecutionContext context();
    
    /** Продолжить выполнение */
    QueryResult proceed(QueryDefinition query);
}

/**
 * Результат выполнения запроса.
 */
public class QueryResult {
    private final Object value;
    private final int affectedRows;
    private final Duration executionTime;
    private final Map<String, Object> metadata;
}
```

### Встроенные интерцепторы

#### QueryLoggingInterceptor

```java
/**
 * Логирует все SQL запросы с параметрами и результатами.
 */
public class QueryLoggingInterceptor implements DbInterceptor {
    
    private static final Logger log = LoggerFactory.getLogger(QueryLoggingInterceptor.class);
    
    @Override
    public QueryResult intercept(DbInterceptorChain chain) {
        QueryDefinition query = chain.query();
        Instant start = Instant.now();
        
        log.info("→ {} | SQL: {}", query.getMethodName(), query.getSql());
        log.debug("  Parameters: {}", query.getParameters());
        
        try {
            QueryResult result = chain.proceed(query);
            Duration duration = Duration.between(start, Instant.now());
            
            log.info("← {} | {} rows | {}ms", 
                query.getMethodName(), 
                result.getAffectedRows(),
                duration.toMillis());
            
            return result;
        } catch (Exception e) {
            Duration duration = Duration.between(start, Instant.now());
            log.error("✗ {} | Error after {}ms: {}", 
                query.getMethodName(), 
                duration.toMillis(), 
                e.getMessage());
            throw e;
        }
    }
    
    @Override
    public int getOrder() {
        return -1000; // Первым в цепочке
    }
}
```

**Пример вывода:**
```
→ findById | SQL: SELECT * FROM users WHERE id = :id
  Parameters: {id=42}
← findById | 1 rows | 12ms

→ updateEmail | SQL: UPDATE users SET email = :email WHERE id = :id
  Parameters: {id=42, email=new@example.com}
← updateEmail | 1 rows | 8ms

✗ findByIds | Error after 5023ms: Query timeout exceeded
```

#### SlowQueryInterceptor

```java
/**
 * Предупреждает о медленных запросах.
 */
public class SlowQueryInterceptor implements DbInterceptor {
    
    private final Duration threshold;
    private final Logger log = LoggerFactory.getLogger(SlowQueryInterceptor.class);
    
    public SlowQueryInterceptor(Duration threshold) {
        this.threshold = threshold;
    }
    
    @Override
    public QueryResult intercept(DbInterceptorChain chain) {
        Instant start = Instant.now();
        
        QueryResult result = chain.proceed(chain.query());
        
        Duration duration = Duration.between(start, Instant.now());
        if (duration.compareTo(threshold) > 0) {
            log.warn("🐢 SLOW QUERY [{}ms > {}ms]: {} | SQL: {}",
                duration.toMillis(),
                threshold.toMillis(),
                chain.query().getMethodName(),
                chain.query().getSql());
        }
        
        return result;
    }
    
    @Override
    public int getOrder() {
        return -500;
    }
}
```

#### QueryTimeoutInterceptor

```java
/**
 * Применяет таймаут к запросам.
 */
public class QueryTimeoutInterceptor implements DbInterceptor {
    
    @Override
    public QueryResult intercept(DbInterceptorChain chain) {
        QueryDefinition query = chain.query();
        ExecutionContext context = chain.context();
        
        // Получаем таймаут из аннотации или конфигурации
        int timeout = query.getTimeout().orElse(context.getDefaultTimeout());
        
        // Устанавливаем таймаут на Statement
        context.setQueryTimeout(timeout);
        
        try {
            return chain.proceed(query);
        } catch (SQLException e) {
            if (isTimeoutException(e)) {
                throw new QueryTimeoutException(
                    "Query " + query.getMethodName() + " exceeded timeout of " + timeout + "s",
                    query, e
                );
            }
            throw e;
        }
    }
    
    private boolean isTimeoutException(SQLException e) {
        return "57014".equals(e.getSQLState()) // PostgreSQL
            || e.getErrorCode() == 1013;        // Oracle
    }
}
```

#### ConnectionTrackingInterceptor

```java
/**
 * Отслеживает использование соединений.
 * Полезно для обнаружения утечек.
 */
public class ConnectionTrackingInterceptor implements DbInterceptor {
    
    private final AtomicInteger activeConnections = new AtomicInteger();
    private final AtomicLong totalQueries = new AtomicLong();
    private final ConcurrentHashMap<String, AtomicLong> queryCounts = new ConcurrentHashMap<>();
    
    @Override
    public QueryResult intercept(DbInterceptorChain chain) {
        activeConnections.incrementAndGet();
        totalQueries.incrementAndGet();
        queryCounts.computeIfAbsent(chain.query().getMethodName(), k -> new AtomicLong())
                   .incrementAndGet();
        
        try {
            return chain.proceed(chain.query());
        } finally {
            activeConnections.decrementAndGet();
        }
    }
    
    // Метрики
    public int getActiveConnections() {
        return activeConnections.get();
    }
    
    public long getTotalQueries() {
        return totalQueries.get();
    }
    
    public Map<String, Long> getQueryCounts() {
        return queryCounts.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));
    }
}
```

### Создание кастомных интерцепторов

```java
/**
 * Интерцептор для аудита изменений данных.
 */
public class AuditInterceptor implements DbInterceptor {
    
    private final AuditLogger auditLogger;
    
    public AuditInterceptor(AuditLogger auditLogger) {
        this.auditLogger = auditLogger;
    }
    
    @Override
    public QueryResult intercept(DbInterceptorChain chain) {
        QueryDefinition query = chain.query();
        
        // Только для модифицирующих операций
        if (!query.isModifying()) {
            return chain.proceed(query);
        }
        
        // Запись аудита до выполнения
        String auditId = auditLogger.logBefore(
            query.getMethodName(),
            query.getSql(),
            query.getParameters(),
            getCurrentUser()
        );
        
        try {
            QueryResult result = chain.proceed(query);
            
            // Запись успешного результата
            auditLogger.logAfter(auditId, result.getAffectedRows(), null);
            
            return result;
        } catch (Exception e) {
            // Запись ошибки
            auditLogger.logAfter(auditId, 0, e);
            throw e;
        }
    }
    
    private String getCurrentUser() {
        return TestContext.current().getUser().orElse("test-user");
    }
}
```

```java
/**
 * Интерцептор для тестовой изоляции.
 * Автоматически откатывает изменения после каждого теста.
 */
public class TestIsolationInterceptor implements DbInterceptor {
    
    private final ThreadLocal<Savepoint> currentSavepoint = new ThreadLocal<>();
    
    @Override
    public QueryResult intercept(DbInterceptorChain chain) {
        ExecutionContext context = chain.context();
        
        // Создаём savepoint перед первой операцией
        if (currentSavepoint.get() == null && chain.query().isModifying()) {
            Connection conn = context.getConnection();
            try {
                conn.setAutoCommit(false);
                currentSavepoint.set(conn.setSavepoint("test_isolation"));
            } catch (SQLException e) {
                throw new DbException("Failed to create savepoint", e);
            }
        }
        
        return chain.proceed(chain.query());
    }
    
    /**
     * Вызывается в @AfterEach для отката изменений.
     */
    public void rollback() {
        Savepoint savepoint = currentSavepoint.get();
        if (savepoint != null) {
            try {
                // Получаем текущее соединение и откатываем
                Connection conn = ConnectionProvider.getCurrentConnection();
                conn.rollback(savepoint);
            } catch (SQLException e) {
                throw new DbException("Failed to rollback to savepoint", e);
            } finally {
                currentSavepoint.remove();
            }
        }
    }
}
```

### Регистрация интерцепторов

```java
// Через Builder API
UserRepository repo = DbServiceFactory.builder()
    .addInterceptor(new QueryLoggingInterceptor())
    .addInterceptor(new SlowQueryInterceptor(Duration.ofSeconds(5)))
    .addInterceptor(new AuditInterceptor(auditLogger))
    .create(UserRepository.class);

// Глобально через конфигурацию
DbConfig config = DbConfig.builder()
    .interceptors(List.of(
        new QueryLoggingInterceptor(),
        new QueryTimeoutInterceptor(),
        new ConnectionTrackingInterceptor()
    ))
    .build();
```

---

## Управление соединениями и транзакциями

### Connection Provider

```java
/**
 * Потокобезопасный провайдер соединений.
 * Использует HikariCP для пулинга.
 */
public class ConnectionProvider {
    
    private final ConcurrentHashMap<String, HikariDataSource> dataSources = new ConcurrentHashMap<>();
    private final ThreadLocal<ConnectionContext> currentContext = new ThreadLocal<>();
    
    /**
     * Получить соединение для указанного источника.
     * Если в текущем потоке активна транзакция - возвращает её соединение.
     */
    public Connection getConnection(String dataSourceName) {
        ConnectionContext ctx = currentContext.get();
        
        // Если есть активная транзакция - используем её соединение
        if (ctx != null && ctx.hasTransactionalConnection(dataSourceName)) {
            return ctx.getTransactionalConnection(dataSourceName);
        }
        
        // Иначе берём из пула
        HikariDataSource ds = dataSources.get(dataSourceName);
        if (ds == null) {
            throw new DbException("Unknown DataSource: " + dataSourceName);
        }
        
        try {
            return ds.getConnection();
        } catch (SQLException e) {
            throw new DbException("Failed to obtain connection from " + dataSourceName, e);
        }
    }
    
    /**
     * Вернуть соединение в пул.
     * Не вызывается если соединение участвует в транзакции.
     */
    public void releaseConnection(Connection conn) {
        ConnectionContext ctx = currentContext.get();
        if (ctx != null && ctx.isPartOfTransaction(conn)) {
            // Соединение управляется транзакцией, не закрываем
            return;
        }
        
        try {
            conn.close();
        } catch (SQLException e) {
            log.warn("Error closing connection", e);
        }
    }
    
    /**
     * Регистрирует DataSource.
     */
    public void registerDataSource(String name, HikariDataSource dataSource) {
        dataSources.put(name, dataSource);
    }
}
```

### Транзакции

```java
public interface OrderRepository {
    
    // Метод выполняется в транзакции
    @Transactional
    @Insert("INSERT INTO orders (user_id, total) VALUES (:userId, :total)")
    @ReturnGeneratedKeys
    Long createOrder(@Param("userId") Long userId, @Param("total") BigDecimal total);
    
    // Вложенная транзакция (savepoint)
    @Transactional(propagation = Propagation.NESTED)
    @Insert("INSERT INTO order_items (order_id, product_id, qty) VALUES (:orderId, :productId, :qty)")
    void addItem(@Param("orderId") Long orderId, 
                 @Param("productId") Long productId,
                 @Param("qty") int qty);
    
    // Read-only транзакция (оптимизация)
    @Transactional(readOnly = true)
    @Select("SELECT * FROM orders WHERE user_id = :userId")
    List<OrderEntity> findByUser(@Param("userId") Long userId);
}
```

### Программное управление транзакциями

```java
class OrderServiceTest {
    
    private OrderRepository orderRepo;
    private ProductRepository productRepo;
    private TransactionManager txManager;
    
    @BeforeEach
    void setUp() {
        orderRepo = DbServiceFactory.create(OrderRepository.class);
        productRepo = DbServiceFactory.create(ProductRepository.class);
        txManager = DbServiceFactory.getTransactionManager();
    }
    
    @Test
    void testComplexTransaction() {
        // Программное управление транзакцией
        txManager.executeInTransaction(() -> {
            Long orderId = orderRepo.createOrder(1L, BigDecimal.valueOf(100));
            
            orderRepo.addItem(orderId, 42L, 2);
            orderRepo.addItem(orderId, 43L, 1);
            
            productRepo.decreaseStock(42L, 2);
            productRepo.decreaseStock(43L, 1);
            
            return orderId;
        });
    }
    
    @Test
    void testRollbackOnError() {
        assertThatThrownBy(() -> {
            txManager.executeInTransaction(() -> {
                orderRepo.createOrder(1L, BigDecimal.valueOf(100));
                throw new RuntimeException("Simulated failure");
            });
        }).isInstanceOf(RuntimeException.class);
        
        // Заказ не должен быть создан
        assertThat(orderRepo.findByUser(1L)).isEmpty();
    }
}
```

### Transaction Manager API

```java
/**
 * Менеджер транзакций с поддержкой вложенности и распространения.
 */
public interface TransactionManager {
    
    /**
     * Выполнить в транзакции с возвратом значения.
     */
    <T> T executeInTransaction(Supplier<T> action);
    
    /**
     * Выполнить в транзакции без возврата.
     */
    void executeInTransaction(Runnable action);
    
    /**
     * Выполнить с кастомной конфигурацией.
     */
    <T> T executeInTransaction(TransactionConfig config, Supplier<T> action);
    
    /**
     * Пометить текущую транзакцию для отката.
     */
    void setRollbackOnly();
    
    /**
     * Проверить, есть ли активная транзакция.
     */
    boolean isTransactionActive();
}

public enum Propagation {
    REQUIRED,       // Использовать существующую или создать новую
    REQUIRES_NEW,   // Всегда создавать новую (приостанавливает текущую)
    NESTED,         // Savepoint внутри текущей
    MANDATORY,      // Требует существующую
    SUPPORTS,       // Использует если есть, иначе без транзакции
    NOT_SUPPORTED,  // Выполнять без транзакции
    NEVER           // Исключение если есть транзакция
}
```

---

## Конфигурация

### DbConfig

```java
@Data
@Builder
@ConfigPrefix("hex.db")
public class DbConfig {
    
    // Primary DataSource
    @ConfigProperty("primary.url")
    @DefaultValue("jdbc:postgresql://localhost:5432/testdb")
    private String primaryUrl;
    
    @ConfigProperty("primary.username")
    @DefaultValue("postgres")
    private String primaryUsername;
    
    @ConfigProperty("primary.password")
    private String primaryPassword;
    
    @ConfigProperty("primary.driver")
    @DefaultValue("org.postgresql.Driver")
    private String primaryDriver;
    
    // Pool settings
    @ConfigProperty("pool.size")
    @DefaultValue("10")
    private int poolSize;
    
    @ConfigProperty("pool.min-idle")
    @DefaultValue("2")
    private int minIdle;
    
    @ConfigProperty("pool.max-lifetime")
    @DefaultValue("1800000")  // 30 minutes
    private long maxLifetime;
    
    @ConfigProperty("pool.connection-timeout")
    @DefaultValue("30000")    // 30 seconds
    private long connectionTimeout;
    
    // Query settings
    @ConfigProperty("query.timeout")
    @DefaultValue("30")
    private int queryTimeout;
    
    @ConfigProperty("query.fetch-size")
    @DefaultValue("100")
    private int fetchSize;
    
    @ConfigProperty("query.validation.enabled")
    @DefaultValue("true")
    private boolean queryValidationEnabled;
    
    @ConfigProperty("query.validation.strict")
    @DefaultValue("false")
    private boolean strictValidation;
    
    // Logging
    @ConfigProperty("logging.enabled")
    @DefaultValue("true")
    private boolean loggingEnabled;
    
    @ConfigProperty("logging.slow-query-threshold-ms")
    @DefaultValue("5000")
    private long slowQueryThresholdMs;
}
```

### hex.properties

```properties
# Primary DataSource
hex.db.primary.url=jdbc:postgresql://localhost:5432/testdb
hex.db.primary.username=postgres
hex.db.primary.password=${DB_PASSWORD}
hex.db.primary.driver=org.postgresql.Driver

# Analytics DataSource (read-only replica)
hex.db.analytics.url=jdbc:postgresql://analytics-host:5432/testdb
hex.db.analytics.username=reader
hex.db.analytics.password=${ANALYTICS_DB_PASSWORD}
hex.db.analytics.read-only=true

# Connection Pool
hex.db.pool.size=10
hex.db.pool.min-idle=2
hex.db.pool.max-lifetime=1800000
hex.db.pool.connection-timeout=30000

# Query Execution
hex.db.query.timeout=30
hex.db.query.fetch-size=100
hex.db.query.validation.enabled=true
hex.db.query.validation.strict=false

# Logging & Monitoring
hex.db.logging.enabled=true
hex.db.logging.slow-query-threshold-ms=5000
```

### Множественные DataSources

```java
// Автоматическое обнаружение из конфигурации
@DbService(dataSource = "primary")
public interface UserRepository { ... }

@DbService(dataSource = "analytics")  
public interface AnalyticsRepository { ... }

// Программная регистрация
ConnectionProvider provider = DbServiceFactory.getConnectionProvider();

HikariDataSource customDs = new HikariDataSource();
customDs.setJdbcUrl("jdbc:h2:mem:testdb");
customDs.setUsername("sa");
customDs.setPassword("");

provider.registerDataSource("custom", customDs);

// Используем
@DbService(dataSource = "custom")
public interface TempRepository { ... }
```

---

## Escape Hatch: Прямой доступ

### Прямой доступ к Connection

```java
// Получить Connection с пула
try (Connection conn = DbServiceFactory.getConnection("primary")) {
    PreparedStatement ps = conn.prepareStatement(
        "SELECT * FROM users WHERE complex_condition = ?"
    );
    ps.setString(1, complexValue);
    ResultSet rs = ps.executeQuery();
    // Обработка результатов
}

// Получить DataSource
DataSource ds = DbServiceFactory.getDataSource("primary");
```

### Raw Query Execution

```java
public interface UserRepository {
    
    /**
     * Выполнить произвольный SQL с ручным маппингом.
     * Escape hatch для случаев, когда декларативный подход не подходит.
     */
    @RawQuery
    <T> T executeRaw(String sql, Map<String, Object> params, 
                     RowMapper<T> mapper);
    
    @RawQuery
    int executeRawUpdate(String sql, Map<String, Object> params);
}

// Использование
UserRepository repo = DbServiceFactory.create(UserRepository.class);

String dynamicSql = buildComplexDynamicQuery(filters);
Map<String, Object> params = buildParams(filters);

List<UserEntity> users = repo.executeRaw(dynamicSql, params, (rs, rowNum) -> {
    UserEntity user = new UserEntity();
    user.setId(rs.getLong("id"));
    user.setUsername(rs.getString("username"));
    // Кастомная логика маппинга
    return user;
});
```

### Query Executor напрямую

```java
// Получить низкоуровневый executor
QueryExecutor executor = DbServiceFactory.getQueryExecutor();

QueryDefinition query = QueryDefinition.builder()
    .sql("SELECT * FROM users WHERE id = :id")
    .parameters(Map.of("id", 42L))
    .returnType(UserEntity.class)
    .build();

QueryResult result = executor.execute(query);
UserEntity user = result.getSingle(UserEntity.class);
```

### Native SQL Templates

```java
public interface ReportRepository {
    
    /**
     * SQL шаблон загружается из ресурсов.
     * Поддерживает динамическую подстановку.
     */
    @Select
    @SqlTemplate(resource = "sql/complex_report.sql")
    List<ReportRow> generateReport(@Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate,
                                    @TemplateParam("groupBy") String groupByColumn);
}

// sql/complex_report.sql
/*
SELECT 
    ${groupBy},
    COUNT(*) as count,
    SUM(amount) as total
FROM orders 
WHERE created_at BETWEEN :startDate AND :endDate
GROUP BY ${groupBy}
ORDER BY total DESC
*/

// Использование
List<ReportRow> report = reportRepo.generateReport(
    LocalDate.now().minusMonths(1),
    LocalDate.now(),
    "category"
);
```

---

## Обработка ошибок

### Иерархия исключений

```
DbException (unchecked, base)
├── QueryValidationException     - Ошибка валидации SQL перед выполнением
├── ConnectionException          - Ошибки подключения к БД
├── QueryExecutionException      - Ошибки выполнения запроса
│   ├── QueryTimeoutException    - Таймаут запроса
│   ├── ConstraintViolationException - Нарушение ограничений
│   │   ├── UniqueConstraintException
│   │   ├── ForeignKeyException
│   │   └── NotNullException
│   └── DataAccessException      - Ошибки доступа к данным
├── MappingException             - Ошибки маппинга результатов
└── TransactionException         - Ошибки транзакций
```

### Примеры исключений

```java
/**
 * Базовое исключение DB модуля.
 */
public class DbException extends RuntimeException {
    
    private final String methodName;
    private final String sql;
    private final Map<String, Object> parameters;
    
    public DbException(String message, String methodName, String sql, 
                       Map<String, Object> parameters, Throwable cause) {
        super(formatMessage(message, methodName, sql, parameters), cause);
        this.methodName = methodName;
        this.sql = sql;
        this.parameters = parameters;
    }
    
    private static String formatMessage(String message, String methodName, 
                                         String sql, Map<String, Object> params) {
        return String.format("""
            %s
            Method: %s
            SQL: %s
            Parameters: %s
            """, message, methodName, sql, params);
    }
}

/**
 * Нарушение уникального ограничения.
 */
public class UniqueConstraintException extends ConstraintViolationException {
    
    private final String constraintName;
    private final String columnName;
    private final Object duplicateValue;
    
    // ...
    
    @Override
    public String getMessage() {
        return String.format("""
            Unique constraint violation
            Constraint: %s
            Column: %s
            Duplicate value: %s
            
            Possible solutions:
              1. Check if record already exists before insert
              2. Use INSERT ... ON CONFLICT for upsert
              3. Delete existing record first
            """, constraintName, columnName, duplicateValue);
    }
}
```

### Обработка в тестах

```java
class UserRepositoryTest {
    
    @Test
    void shouldFailOnDuplicateEmail() {
        UserRepository repo = DbServiceFactory.create(UserRepository.class);
        
        // Первый пользователь
        repo.create("user1", "test@example.com", true);
        
        // Второй с тем же email
        assertThatThrownBy(() -> repo.create("user2", "test@example.com", true))
            .isInstanceOf(UniqueConstraintException.class)
            .hasMessageContaining("email")
            .hasMessageContaining("test@example.com");
    }
    
    @Test
    void shouldHandleQueryTimeout() {
        UserRepository repo = DbServiceFactory.builder()
            .addInterceptor(new QueryTimeoutInterceptor(1)) // 1 секунда
            .create(UserRepository.class);
        
        // Медленный запрос
        assertThatThrownBy(() -> repo.findWithSlowQuery())
            .isInstanceOf(QueryTimeoutException.class)
            .hasMessageContaining("exceeded timeout");
    }
    
    @Test
    void shouldValidateQueryBeforeExecution() {
        // Невалидный интерфейс - параметр не совпадает
        assertThatThrownBy(() -> DbServiceFactory.create(InvalidRepository.class))
            .isInstanceOf(QueryValidationException.class)
            .hasMessageContaining("Parameter mismatch");
    }
}
```

---

## Потокобезопасность

### Гарантии потокобезопасности

| Компонент | Threadsafe | Механизм |
|-----------|------------|----------|
| `DbServiceFactory` | ✅ | Stateless, ConcurrentHashMap для кэша |
| `ConnectionProvider` | ✅ | HikariCP pool, ThreadLocal для транзакций |
| `QueryExecutor` | ✅ | Новый Statement для каждого вызова |
| `DbInterceptorChain` | ✅ | Immutable chain, ThreadLocal context |
| `RowMapper` | ✅/⚠️ | Зависит от реализации (пишите stateless) |
| `TransactionManager` | ✅ | ThreadLocal для transaction context |

### ThreadLocal контексты

```java
/**
 * Контекст выполнения, изолированный для каждого потока.
 */
public class ExecutionContext {
    
    private static final ThreadLocal<ExecutionContext> CURRENT = new ThreadLocal<>();
    
    private Connection transactionalConnection;
    private Savepoint currentSavepoint;
    private boolean rollbackOnly;
    private Map<String, Object> attributes = new HashMap<>();
    
    public static ExecutionContext current() {
        return CURRENT.get();
    }
    
    public static void setCurrent(ExecutionContext ctx) {
        CURRENT.set(ctx);
    }
    
    public static void clear() {
        CURRENT.remove();
    }
    
    // ...
}
```

### Пример параллельного выполнения

```java
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ParallelDbTest {
    
    private UserRepository userRepo;
    private ConnectionTrackingInterceptor tracker;
    
    @BeforeAll
    void setUp() {
        tracker = new ConnectionTrackingInterceptor();
        userRepo = DbServiceFactory.builder()
            .addInterceptor(tracker)
            .create(UserRepository.class);
    }
    
    @RepeatedTest(100)
    @Execution(ExecutionMode.CONCURRENT)
    void parallelReadsAreSafe() {
        // Параллельные чтения безопасны
        Optional<UserEntity> user = userRepo.findById(1L);
        assertThat(user).isPresent();
    }
    
    @RepeatedTest(50)
    @Execution(ExecutionMode.CONCURRENT)
    void parallelWritesWithUniqueData(RepetitionInfo info) {
        // Параллельные записи с уникальными данными
        String username = "parallel_user_" + Thread.currentThread().getId() + "_" + info.getCurrentRepetition();
        String email = username + "@test.com";
        
        Long id = userRepo.create(username, email, true);
        
        assertThat(id).isPositive();
        
        // Проверяем изоляцию
        Optional<UserEntity> created = userRepo.findById(id);
        assertThat(created).isPresent();
        assertThat(created.get().getUsername()).isEqualTo(username);
    }
    
    @AfterAll
    void verifyNoLeaks() {
        // Проверяем, что все соединения вернулись в пул
        assertThat(tracker.getActiveConnections()).isZero();
    }
}
```

### Изоляция транзакций для тестов

```java
/**
 * Extension для автоматического отката после каждого теста.
 */
public class DbTestExtension implements BeforeEachCallback, AfterEachCallback {
    
    private static final ThreadLocal<Savepoint> TEST_SAVEPOINT = new ThreadLocal<>();
    
    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        Connection conn = DbServiceFactory.getConnection("primary");
        conn.setAutoCommit(false);
        TEST_SAVEPOINT.set(conn.setSavepoint("test_" + context.getDisplayName()));
    }
    
    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        Savepoint savepoint = TEST_SAVEPOINT.get();
        if (savepoint != null) {
            Connection conn = DbServiceFactory.getConnection("primary");
            conn.rollback(savepoint);
            conn.setAutoCommit(true);
            TEST_SAVEPOINT.remove();
        }
    }
}

// Использование
@ExtendWith(DbTestExtension.class)
class IsolatedDbTest {
    
    @Test
    void testCreateUser() {
        // Создаём пользователя
        userRepo.create("test", "test@test.com", true);
        
        // После теста автоматически откатится
    }
    
    @Test
    void testAnotherCreate() {
        // Тот же email - не будет конфликта, т.к. предыдущий тест откатился
        userRepo.create("test", "test@test.com", true);
    }
}
```

---

## Лучшие практики

### Структура проекта

```
src/
├── main/java/
│   └── com/company/hex/project/
│       └── db/
│           ├── entity/              # Entity классы
│           │   ├── UserEntity.java
│           │   └── OrderEntity.java
│           ├── repository/          # Repository интерфейсы
│           │   ├── UserRepository.java
│           │   └── OrderRepository.java
│           ├── mapper/              # Кастомные RowMapper'ы
│           │   └── OrderWithDetailsMapper.java
│           └── interceptor/         # Кастомные интерцепторы
│               └── AuditInterceptor.java
├── main/resources/
│   └── sql/                         # SQL скрипты и шаблоны
│       ├── schema.sql
│       └── complex_report.sql
└── test/java/
    └── com/company/hex/project/
        └── tests/db/
            ├── UserRepositoryTest.java
            └── OrderRepositoryTest.java
```

### Convention over Configuration

```java
// Entity соглашения
@Table("users")  // Таблица = snake_case от имени класса, если не указано
public class UserEntity {
    
    @Id                              // Первичный ключ
    private Long id;                 // "id" по умолчанию
    
    @Column("user_name")             // Явное имя колонки
    private String userName;
    
    private String email;            // Автоматически → "email"
    
    private LocalDateTime createdAt; // Автоматически → "created_at"
}

// Repository соглашения
@DbService(dataSource = "primary")
public interface UserRepository {
    
    // Метод начинающийся с find → SELECT
    // Метод начинающийся с count → SELECT COUNT(*)
    // Метод начинающийся с exists → SELECT EXISTS
    // Метод начинающийся с delete → DELETE
    // Метод начинающийся с update → UPDATE
    // Метод начинающийся с create/insert/save → INSERT
}
```

### Проектная фабрика

```java
/**
 * Фабрика репозиториев с проектной конфигурацией.
 */
public final class ProjectDbFactory {
    
    private static final DbConfig CONFIG = HexConfigFactory.getConfig(DbConfig.class);
    private static final List<DbInterceptor> DEFAULT_INTERCEPTORS = List.of(
        new QueryLoggingInterceptor(),
        new SlowQueryInterceptor(Duration.ofSeconds(5)),
        new ConnectionTrackingInterceptor()
    );
    
    private static volatile boolean initialized = false;
    
    public static synchronized void initialize() {
        if (!initialized) {
            // Регистрация DataSources
            registerDataSources();
            // Прогрев пула
            warmUpConnectionPool();
            initialized = true;
        }
    }
    
    public static <T> T create(Class<T> repositoryInterface) {
        initialize();
        
        return DbServiceFactory.builder()
            .withConfig(CONFIG)
            .addInterceptors(DEFAULT_INTERCEPTORS)
            .withValidator(new StrictQueryValidator())
            .create(repositoryInterface);
    }
    
    private static void registerDataSources() {
        // Конфигурация HikariCP
    }
    
    private static void warmUpConnectionPool() {
        // Создать несколько соединений заранее
    }
}

// Использование
UserRepository userRepo = ProjectDbFactory.create(UserRepository.class);
```

### Базовый класс для DB тестов

```java
@ExtendWith(DbTestExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseDbTest {
    
    @BeforeAll
    void initDb() {
        ProjectDbFactory.initialize();
        setupTestData();
    }
    
    /**
     * Переопределить для setup тестовых данных.
     */
    protected void setupTestData() {
        // По умолчанию ничего
    }
    
    /**
     * Создать репозиторий с проектной конфигурацией.
     */
    protected <T> T repository(Class<T> repoInterface) {
        return ProjectDbFactory.create(repoInterface);
    }
    
    /**
     * Выполнить в транзакции.
     */
    protected <T> T inTransaction(Supplier<T> action) {
        return DbServiceFactory.getTransactionManager()
            .executeInTransaction(action);
    }
}

// Использование
class UserRepositoryTest extends BaseDbTest {
    
    private UserRepository userRepo;
    
    @Override
    protected void setupTestData() {
        // Создать тестовые данные
    }
    
    @BeforeEach
    void setUp() {
        userRepo = repository(UserRepository.class);
    }
    
    @Test
    void shouldFindUser() {
        Optional<UserEntity> user = userRepo.findById(1L);
        assertThat(user).isPresent();
    }
}
```

### Тестирование ошибочных сценариев

```java
@Test
void shouldHandleNotFound() {
    Optional<UserEntity> user = userRepo.findById(99999L);
    assertThat(user).isEmpty();
}

@Test
void shouldValidateConstraints() {
    // Null на NOT NULL поле
    assertThatThrownBy(() -> userRepo.create(null, "test@test.com", true))
        .isInstanceOf(NotNullException.class);
}

@Test
void shouldRollbackOnError() {
    Long orderId = null;
    
    assertThatThrownBy(() -> {
        TransactionManager tx = DbServiceFactory.getTransactionManager();
        tx.executeInTransaction(() -> {
            Long id = orderRepo.create(1L, BigDecimal.TEN);
            
            // Ошибка в середине транзакции
            throw new RuntimeException("Simulated error");
        });
    }).isInstanceOf(RuntimeException.class);
    
    // Заказ не должен быть создан
    assertThat(orderRepo.findAll()).isEmpty();
}
```

---

## Заключение

DB модуль Hex Framework предоставляет:

✅ **Декларативный стиль** — SQL запросы как контракт в аннотациях  
✅ **Статическая валидация** — проверка SQL до выполнения  
✅ **Потокобезопасность** — изоляция соединений и транзакций  
✅ **Расширяемость** — интерцепторы, валидаторы, кастомные мапперы  
✅ **Гибкость** — escape hatch для сложных случаев  
✅ **Простота** — convention over configuration

### Типичный workflow

1. Создайте Entity с `@Table` и `@Column` аннотациями
2. Определите Repository интерфейс с `@Select`, `@Insert`, `@Update`, `@Delete`
3. Вызовите `DbServiceFactory.create(MyRepository.class)`
4. Используйте методы интерфейса как обычные Java методы

---

*Документация актуальна для Hex Framework DB Module версии 1.0*