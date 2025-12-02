# Hex Core DB

## Описание

Декларативный модуль для работы с базами данных в тестах. Предоставляет потокобезопасный доступ к данным через аннотированные интерфейсы репозиториев, вдохновлённый Spring Data JDBC и MyBatis.

## Основные возможности

- **Декларативные репозитории** — определение SQL запросов через аннотации
- **Автоматический маппинг** — преобразование ResultSet в Java объекты
- **Потокобезопасность** — полная поддержка параллельного выполнения JUnit 5 тестов
- **Управление транзакциями** — программное и декларативное управление
- **Connection Pooling** — высокопроизводительный пул соединений через HikariCP
- **Система интерцепторов** — логирование, метрики, кастомная логика
- **Валидация SQL** — статическая проверка запросов до выполнения
- **Множественные DataSource** — поддержка нескольких баз данных

## Зависимости

| Библиотека | Версия | Назначение |
|------------|--------|------------|
| HikariCP | 5.1.0 | Высокопроизводительный connection pool |
| PostgreSQL Driver | 42.7.3 | JDBC драйвер PostgreSQL |
| H2 Database | 2.2.224 | In-memory БД для тестов |

## Структура пакетов

```
com.company.hex.db
├── annotations/
│   ├── config/      # @DbService
│   ├── mapping/     # @Table, @Column, @Id
│   ├── param/       # @Param, @ParamList, @NullableParam
│   └── query/       # @Select, @Insert, @Update, @Delete
├── config/          # DbConfig (Owner)
├── connection/      # ConnectionProvider, TransactionManager
├── exception/       # DbException hierarchy
├── interceptor/     # DbInterceptor, built-in interceptors
├── mapping/         # ResultMapper, RowMapper
├── service/         # DbServiceFactory, QueryExecutor
└── validation/      # QueryValidator
```

## Быстрый старт

### Подключение

```xml
<dependency>
    <groupId>com.company.hex</groupId>
    <artifactId>hex-core-db</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Конфигурация (hex.properties)

```properties
# Primary DataSource
hex.db.primary.url=jdbc:postgresql://localhost:5432/testdb
hex.db.primary.username=postgres
hex.db.primary.password=${DB_PASSWORD}
hex.db.primary.driver=org.postgresql.Driver

# Pool settings
hex.db.pool.size=10
hex.db.pool.min-idle=2
hex.db.pool.max-lifetime=1800000
hex.db.pool.connection-timeout=30000
```

### Определение Entity

```java
@Table("users")
public class UserEntity {
    @Id
    @Column("user_id")
    private Long id;

    @Column("user_name")
    private String name;

    @Column("email")
    private String email;

    @Transient
    private String cachedValue;

    // getters/setters
}
```

### Определение репозитория

```java
@DbService(dataSource = "primary", queryTimeout = 30, validateQueries = true)
public interface UserRepository {

    @Select("SELECT * FROM users WHERE id = :id")
    Optional<UserEntity> findById(@Param("id") Long id);

    @Select("SELECT * FROM users WHERE email = :email")
    Optional<UserEntity> findByEmail(@Param("email") String email);

    @Select("SELECT * FROM users WHERE status IN (:statuses)")
    List<UserEntity> findByStatuses(@ParamList("statuses") List<String> statuses);

    @Insert("INSERT INTO users (name, email) VALUES (:name, :email)")
    @ReturnGeneratedKeys
    Long create(@Param("name") String name, @Param("email") String email);

    @Update("UPDATE users SET name = :name WHERE id = :id")
    int updateName(@Param("id") Long id, @Param("name") String name);

    @Delete("DELETE FROM users WHERE id = :id")
    int deleteById(@Param("id") Long id);
}
```

### Использование в тестах

```java
class UserRepositoryTest {

    private final UserRepository userRepo = DbServiceFactory.create(UserRepository.class);

    @Test
    void shouldCreateAndFindUser() {
        // Create
        Long id = userRepo.create("John Doe", "john@example.com");
        assertThat(id).isNotNull();

        // Find
        Optional<UserEntity> user = userRepo.findById(id);
        assertThat(user).isPresent();
        assertThat(user.get().getName()).isEqualTo("John Doe");
    }
}
```

## Продвинутые возможности

### Транзакции

```java
TransactionManager tx = DbServiceFactory.getTransactionManager();
tx.executeInTransaction(() -> {
    Long orderId = orderRepo.create(customerId, BigDecimal.TEN);
    itemRepo.addItem(orderId, productId, 2);
    return orderId;
});
```

### Декларативные транзакции

```java
@DbService
public interface OrderRepository {

    @Transactional
    @Insert("INSERT INTO orders (...) VALUES (...)")
    Long createOrder(...);
}
```

### Интерцепторы

```java
UserRepository repo = DbServiceFactory.builder(UserRepository.class)
    .withDataSource("primary")
    .addInterceptor(new QueryLoggingInterceptor())
    .addInterceptor(new SlowQueryInterceptor(Duration.ofSeconds(5)))
    .build();
```

### Кастомный маппер

```java
public class UserRowMapper implements RowMapper<UserEntity> {
    @Override
    public UserEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        UserEntity user = new UserEntity();
        user.setId(rs.getLong("user_id"));
        user.setName(rs.getString("user_name"));
        user.setEmail(rs.getString("email"));
        return user;
    }
}

@Select("SELECT * FROM users WHERE id = :id")
@RowMapping(UserRowMapper.class)
Optional<UserEntity> findById(@Param("id") Long id);
```

### Batch операции

```java
@BatchInsert("INSERT INTO items (name) VALUES (:name)")
int[] createBatch(@ParamEntity List<ItemEntity> items);
```

### Stored Procedures

```java
@Call("{call calculate_total(:orderId, :result)}")
void calculateTotal(
    @Param("orderId") Long orderId,
    @OutParam(value = "result", sqlType = Types.DECIMAL) BigDecimal[] result
);
```

### SQL скрипты

```java
@Script(resource = "db/cleanup.sql")
void cleanup();
```

## Валидация

Модуль проводит статическую валидацию SQL при создании прокси:

```java
// ❌ QueryValidationException: Parameter mismatch - :userId не найден в @Param
@Select("SELECT * FROM users WHERE id = :userId")
Optional<UserEntity> findById(@Param("id") Long id);

// ⚠️ Warning: UPDATE без WHERE - требует @DangerousQuery
@Update("UPDATE users SET active = false")
int deactivateAll();

// ✅ Явное указание опасной операции
@Update("UPDATE users SET active = false")
@DangerousQuery(reason = "Deactivate all for cleanup")
int deactivateAll();
```

## Иерархия исключений

```
DbException
├── QueryValidationException
├── ConnectionException
├── QueryExecutionException
│   ├── QueryTimeoutException
│   └── ConstraintViolationException
│       ├── UniqueConstraintException
│       ├── ForeignKeyException
│       └── NotNullException
├── MappingException
└── TransactionException
```

## JUnit 5 Extension

```java
@ExtendWith(DbTestExtension.class)
class UserRepositoryTest {

    @Test
    void shouldCreateUser() {
        userRepo.create("test", "test@test.com");
        // После теста автоматический rollback
    }

    @Test
    @Commit  // Пропустить rollback для этого теста
    void shouldPersistUser() {
        userRepo.create("persistent", "persist@test.com");
    }
}
```

Или с meta-annotation:

```java
@DbTest
class UserRepositoryTest {
    // ...
}
```

## Примеры

Подробные примеры использования находятся в модуле `hex-project-samples`.
