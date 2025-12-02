```markdown
# Epic 6: Database Layer — Declarative DB Module

**Epic Goal:** Реализовать декларативный модуль `hex-core-db` для работы с базами данных в тестах, обеспечивающий потокобезопасный доступ к данным через аннотированные интерфейсы репозиториев. Модуль вдохновлён Spring Data JDBC и MyBatis, предоставляет автоматический маппинг, валидацию SQL, систему интерцепторов и управление транзакциями.

**Epic Value:** 
- Сокращение boilerplate кода при работе с БД на 70-80%
- Потокобезопасность для параллельного выполнения JUnit 5 тестов
- Единообразный подход к работе с данными по аналогии с `hex-core-api`
- Статическая валидация SQL до выполнения

---

## Story 6.1: Bootstrap `hex-core-db` Module Structure and Dependencies

### Status
Done

### Story
**As a** framework maintainer,
**I want** a dedicated `hex-core-db` module with proper structure and pinned dependencies,
**so that** teams can work with databases using the same patterns as other Hex modules.

### Acceptance Criteria
1. Модуль `hex-core-db` создан с корректной структурой пакетов в Maven parent POM.
2. Зависимость от `hex-core` установлена.
3. Зависимости для работы с БД добавлены в dependencyManagement parent POM:
   - HikariCP 5.1.0 (connection pooling)
   - PostgreSQL JDBC Driver 42.7.3
   - H2 Database 2.2.224 (для тестов)
4. Структура пакетов соответствует unified-project-structure:
   - `com.company.hex.db.annotations` — все аннотации
   - `com.company.hex.db.service` — DbServiceFactory, QueryExecutor
   - `com.company.hex.db.mapping` — ResultMapper, RowMapper
   - `com.company.hex.db.connection` — ConnectionProvider, TransactionManager
   - `com.company.hex.db.exception` — иерархия исключений
   - `com.company.hex.db.interceptor` — интерцепторы
   - `com.company.hex.db.validation` — валидаторы
   - `com.company.hex.db.config` — DbConfig и Owner интеграция
5. README.md для модуля создан на русском языке.
6. Модуль успешно собирается вместе с остальными.

### Tasks / Subtasks
- [ ] Task 1: Обновить parent POM
  - [ ] Subtask 1.1: Добавить `hex-core-db` в секцию modules
  - [ ] Subtask 1.2: Добавить версии зависимостей (HikariCP, PostgreSQL, H2)
  - [ ] Subtask 1.3: Добавить зависимости в dependencyManagement
- [ ] Task 2: Создать структуру модуля
  - [ ] Subtask 2.1: Создать `hex-core-db/pom.xml`
  - [ ] Subtask 2.2: Создать структуру пакетов
  - [ ] Subtask 2.3: Добавить зависимости (HikariCP, JDBC drivers)
- [ ] Task 3: Создать документацию
  - [ ] Subtask 3.1: README.md на русском языке
  - [ ] Subtask 3.2: Базовые Javadoc комментарии

### Dev Notes

#### Версии зависимостей для DB модуля
| Dependency | Version | Purpose |
|------------|---------|---------|
| HikariCP | 5.1.0 | Высокопроизводительный connection pool |
| PostgreSQL Driver | 42.7.3 | JDBC драйвер PostgreSQL |
| H2 Database | 2.2.224 | In-memory БД для тестов |
| HSQLDB | 2.7.2 | Альтернативная in-memory БД (опционально) |

#### Структура пакетов
```
hex-core-db/
├── src/main/java/com/company/hex/db/
│   ├── annotations/
│   │   ├── config/      # @DbService
│   │   ├── mapping/     # @Table, @Column, @Id
│   │   ├── param/       # @Param, @ParamList, @NullableParam
│   │   └── query/       # @Select, @Insert, @Update, @Delete
│   ├── config/          # DbConfig (Owner)
│   ├── connection/      # ConnectionProvider, TransactionManager
│   ├── exception/       # DbException hierarchy
│   ├── interceptor/     # DbInterceptor, built-in interceptors
│   ├── mapping/         # ResultMapper, RowMapper
│   ├── service/         # DbServiceFactory, QueryExecutor
│   └── validation/      # QueryValidator
└── pom.xml
```

---

## Story 6.2: Core Annotations — Query and Mapping

### Status
Not Started

### Story
**As an** API test developer,
**I want** core annotations for определения SQL запросов и маппинга,
**so that** I can define database contracts declaratively in repository interfaces.

### Acceptance Criteria
1. Аннотации запросов реализованы:
   - `@Select(String sql)` — SELECT запросы
   - `@Insert(String sql)` — INSERT запросы
   - `@Update(String sql)` — UPDATE запросы
   - `@Delete(String sql)` — DELETE запросы
   - `@ReturnGeneratedKeys` — возврат сгенерированных ключей
2. Аннотации параметров реализованы:
   - `@Param(String name)` — именованный параметр
   - `@ParamList(String name)` — коллекция для IN clause
   - `@NullableParam(String name)` — nullable параметр
3. Аннотации маппинга реализованы:
   - `@Table(String name)` — имя таблицы
   - `@Column(String name)` — имя колонки
   - `@Id` — первичный ключ
   - `@Transient` — игнорировать при маппинге
4. Аннотация конфигурации сервиса:
   - `@DbService(dataSource, queryTimeout, validateQueries)`
5. Javadoc на русском языке для всех аннотаций.
6. Unit-тесты для retention policy и target проверок.

### Tasks / Subtasks
- [ ] Task 1: Реализовать аннотации запросов
  - [ ] Subtask 1.1: `@Select`, `@Insert`, `@Update`, `@Delete`
  - [ ] Subtask 1.2: `@ReturnGeneratedKeys`
  - [ ] Subtask 1.3: `@Script` для SQL скриптов
- [ ] Task 2: Реализовать аннотации параметров
  - [ ] Subtask 2.1: `@Param`, `@ParamList`
  - [ ] Subtask 2.2: `@NullableParam`, `@OutParam`
- [ ] Task 3: Реализовать аннотации маппинга
  - [ ] Subtask 3.1: `@Table`, `@Column`, `@Id`
  - [ ] Subtask 3.2: `@Transient`, `@Enumerated`
- [ ] Task 4: Реализовать конфигурационные аннотации
  - [ ] Subtask 4.1: `@DbService`
  - [ ] Subtask 4.2: `@Timeout`, `@ReadOnly`, `@Transactional`

### Dev Notes

#### Примеры аннотаций
```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Select {
    String value();
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DbService {
    String dataSource() default "primary";
    int queryTimeout() default 30;
    boolean validateQueries() default true;
}
```

---

## Story 6.3: DbServiceFactory and Proxy-Based Architecture

### Status
Not Started

### Story
**As a** test developer,
**I want** a `DbServiceFactory` that creates proxy implementations from annotated interfaces,
**so that** I can use repository interfaces as regular Java objects.

### Acceptance Criteria
1. `DbServiceFactory.create(MyRepository.class)` создаёт работающий прокси.
2. Реализован `DbProxyHandler` для перехвата вызовов методов.
3. Реализован `QueryProcessor` для парсинга аннотаций.
4. Реализован `QueryDefinition` DTO с метаданными запроса.
5. Builder API для гибкой конфигурации:
   - `.withDataSource(name)`
   - `.withConfig(DbConfig)`
   - `.addInterceptor(interceptor)`
   - `.withValidator(validator)`
6. Созданный прокси потокобезопасен.
7. Unit-тесты для фабрики и прокси.

### Tasks / Subtasks
- [ ] Task 1: Реализовать QueryDefinition
  - [ ] Subtask 1.1: DTO с полями sql, parameters, returnType, methodName
  - [ ] Subtask 1.2: Builder pattern
- [ ] Task 2: Реализовать QueryProcessor
  - [ ] Subtask 2.1: Парсинг аннотаций методов
  - [ ] Subtask 2.2: Извлечение параметров
  - [ ] Subtask 2.3: Определение типа запроса
- [ ] Task 3: Реализовать DbProxyHandler
  - [ ] Subtask 3.1: Implements InvocationHandler
  - [ ] Subtask 3.2: Оркестрация вызовов
  - [ ] Subtask 3.3: Кэширование метаданных методов
- [ ] Task 4: Реализовать DbServiceFactory
  - [ ] Subtask 4.1: `create(Class<T>)` метод
  - [ ] Subtask 4.2: Builder API
  - [ ] Subtask 4.3: Потокобезопасный кэш репозиториев

### Dev Notes

#### Архитектура компонентов
```
DbServiceFactory.create(MyRepo.class)
        │
        ▼
┌──────────────────┐
│  DbProxyHandler  │ ◄── InvocationHandler
└────────┬─────────┘
         │
    ┌────┴────┐
    ▼         ▼
QueryProcessor  InterceptorChain
    │              │
    ▼              ▼
QueryDefinition  QueryExecutor
                    │
                    ▼
                ResultMapper
```

---

## Story 6.4: Connection Provider and HikariCP Integration

### Status
Not Started

### Story
**As a** test developer,
**I want** a thread-safe connection provider with HikariCP pooling,
**so that** database connections are efficiently managed across parallel tests.

### Acceptance Criteria
1. `ConnectionProvider` управляет пулами соединений через HikariCP.
2. Поддержка множественных DataSource по имени.
3. Потокобезопасность через ThreadLocal для транзакций.
4. Автоматическое освобождение соединений в пул.
5. Регистрация DataSource программно и через конфигурацию.
6. Метрики пула доступны (active connections, pending).
7. Integration tests с H2 in-memory БД.

### Tasks / Subtasks
- [ ] Task 1: Реализовать ConnectionProvider
  - [ ] Subtask 1.1: ConcurrentHashMap для DataSources
  - [ ] Subtask 1.2: getConnection(dataSourceName)
  - [ ] Subtask 1.3: releaseConnection(connection)
  - [ ] Subtask 1.4: ThreadLocal для транзакционного контекста
- [ ] Task 2: Интегрировать HikariCP
  - [ ] Subtask 2.1: Создание HikariDataSource из DbConfig
  - [ ] Subtask 2.2: Конфигурация pool size, timeout
  - [ ] Subtask 2.3: Graceful shutdown
- [ ] Task 3: Поддержка множественных DataSource
  - [ ] Subtask 3.1: registerDataSource(name, config)
  - [ ] Subtask 3.2: Автообнаружение из hex.properties
- [ ] Task 4: Написать integration tests
  - [ ] Subtask 4.1: Тесты с H2 in-memory
  - [ ] Subtask 4.2: Тесты параллельного доступа

### Dev Notes

#### Конфигурация HikariCP
```properties
hex.db.pool.size=10
hex.db.pool.min-idle=2
hex.db.pool.max-lifetime=1800000
hex.db.pool.connection-timeout=30000
```

---

## Story 6.5: Query Executor and JDBC Execution

### Status
Not Started

### Story
**As a** framework developer,
**I want** a query executor that executes SQL via JDBC,
**so that** queries are executed reliably with proper resource management.

### Acceptance Criteria
1. `QueryExecutor` выполняет SQL через PreparedStatement.
2. Поддержка именованных параметров (`:param` → `?`).
3. Поддержка всех типов запросов (SELECT, INSERT, UPDATE, DELETE).
4. Обработка generated keys для INSERT.
5. Batch операции для списков параметров.
6. Автоматическое закрытие Statement и ResultSet.
7. QueryResult DTO с результатом и метаданными.

### Tasks / Subtasks
- [ ] Task 1: Реализовать NamedParameterProcessor
  - [ ] Subtask 1.1: Парсинг `:param` из SQL
  - [ ] Subtask 1.2: Замена на `?` с сохранением порядка
  - [ ] Subtask 1.3: Подстановка значений параметров
- [ ] Task 2: Реализовать QueryExecutor
  - [ ] Subtask 2.1: executeQuery для SELECT
  - [ ] Subtask 2.2: executeUpdate для INSERT/UPDATE/DELETE
  - [ ] Subtask 2.3: executeBatch для batch операций
  - [ ] Subtask 2.4: Обработка RETURNING / generated keys
- [ ] Task 3: Реализовать QueryResult
  - [ ] Subtask 3.1: DTO с value, affectedRows, executionTime
  - [ ] Subtask 3.2: Методы getSingle(), getList()
- [ ] Task 4: Unit-тесты с mock connection

### Dev Notes

#### Обработка именованных параметров
```java
// Input:  "SELECT * FROM users WHERE id = :id AND name = :name"
// Output: "SELECT * FROM users WHERE id = ? AND name = ?"
// Params: [id → 1, name → 2] (positional mapping)
```

---

## Story 6.6: Result Mapper — Automatic and Custom Mapping

### Status
Not Started

### Story
**As a** test developer,
**I want** automatic mapping from ResultSet to Java objects,
**so that** I don't have to write boilerplate mapping code.

### Acceptance Criteria
1. Автоматический маппинг на Entity с `@Column` аннотациями.
2. Поддержка Java Records (constructor-based mapping).
3. Маппинг скалярных типов (Long, String, Integer, etc.).
4. Маппинг коллекций (List, Set).
5. Поддержка Optional<T> для nullable результатов.
6. Интерфейс `RowMapper<T>` для кастомного маппинга.
7. Аннотация `@RowMapping(CustomMapper.class)`.
8. Конвертация snake_case → camelCase по умолчанию.

### Tasks / Subtasks
- [ ] Task 1: Реализовать RowMapper interface
  - [ ] Subtask 1.1: `T mapRow(ResultSet rs, int rowNum)`
  - [ ] Subtask 1.2: DefaultRowMapper с reflection
- [ ] Task 2: Реализовать ResultMapper
  - [ ] Subtask 2.1: mapSingle для одного объекта
  - [ ] Subtask 2.2: mapList для списка
  - [ ] Subtask 2.3: mapOptional для Optional
  - [ ] Subtask 2.4: mapScalar для примитивных типов
- [ ] Task 3: Поддержка Entity маппинга
  - [ ] Subtask 3.1: Парсинг @Column аннотаций
  - [ ] Subtask 3.2: snake_case → camelCase конвертация
  - [ ] Subtask 3.3: Type conversion (SQL types → Java types)
- [ ] Task 4: Поддержка Records
  - [ ] Subtask 4.1: Определение Record класса
  - [ ] Subtask 4.2: Constructor-based маппинг

### Dev Notes

#### Поддерживаемые типы
| SQL Type | Java Type |
|----------|-----------|
| VARCHAR, CHAR | String |
| INTEGER, INT | Integer, int |
| BIGINT | Long, long |
| DECIMAL, NUMERIC | BigDecimal |
| TIMESTAMP | LocalDateTime |
| DATE | LocalDate |
| TIME | LocalTime |
| BOOLEAN | Boolean, boolean |
| BLOB | byte[] |

---

## Story 6.7: Exception Hierarchy and Error Handling

### Status
Not Started

### Story
**As a** test developer,
**I want** a clear exception hierarchy with meaningful messages,
**so that** I can quickly understand and fix database-related failures.

### Acceptance Criteria
1. Базовый `DbException` extends RuntimeException.
2. Иерархия исключений:
   - `QueryValidationException` — ошибки валидации SQL
   - `ConnectionException` — ошибки подключения
   - `QueryExecutionException` — ошибки выполнения
     - `QueryTimeoutException`
     - `ConstraintViolationException`
       - `UniqueConstraintException`
       - `ForeignKeyException`
       - `NotNullException`
   - `MappingException` — ошибки маппинга
   - `TransactionException` — ошибки транзакций
3. Каждое исключение содержит: methodName, sql, parameters.
4. Информативные сообщения об ошибках с рекомендациями.
5. Маппинг SQLException на типизированные исключения.

### Tasks / Subtasks
- [ ] Task 1: Создать базовый DbException
  - [ ] Subtask 1.1: Поля methodName, sql, parameters
  - [ ] Subtask 1.2: Форматированный message
- [ ] Task 2: Создать иерархию исключений
  - [ ] Subtask 2.1: QueryValidationException
  - [ ] Subtask 2.2: ConnectionException
  - [ ] Subtask 2.3: QueryExecutionException и подклассы
  - [ ] Subtask 2.4: MappingException
  - [ ] Subtask 2.5: TransactionException
- [ ] Task 3: Реализовать ExceptionMapper
  - [ ] Subtask 3.1: Маппинг SQLState на типы исключений
  - [ ] Subtask 3.2: Извлечение constraint name
  - [ ] Subtask 3.3: Поддержка PostgreSQL, H2

### Dev Notes

#### SQLState маппинг
| SQLState | Exception |
|----------|-----------|
| 23505 | UniqueConstraintException |
| 23503 | ForeignKeyException |
| 23502 | NotNullException |
| 57014 | QueryTimeoutException |

---

## Story 6.8: DbConfig and Owner Integration

### Status
Not Started

### Story
**As a** test developer,
**I want** Owner-backed configuration for database settings,
**so that** I can manage connections declaratively across environments.

### Acceptance Criteria
1. `DbConfig` Owner interface с настройками:
   - DataSource URL, username, password, driver
   - Pool settings (size, timeout, max-lifetime)
   - Query settings (timeout, fetch-size)
   - Logging settings
2. Поддержка множественных DataSource через prefix.
3. Поддержка environment variables через `${VAR}` синтаксис.
4. Поддержка профилей (local, ci).
5. Интеграция с `HexConfigFactory`.
6. Валидация конфигурации при старте.

### Tasks / Subtasks
- [ ] Task 1: Создать DbConfig Owner interface
  - [ ] Subtask 1.1: Properties для primary DataSource
  - [ ] Subtask 1.2: Pool settings
  - [ ] Subtask 1.3: Query settings
- [ ] Task 2: Поддержка множественных DataSource
  - [ ] Subtask 2.1: Dynamic prefix: hex.db.{name}.url
  - [ ] Subtask 2.2: DataSourceConfigResolver
- [ ] Task 3: Интегрировать с HexConfigFactory
  - [ ] Subtask 3.1: Автозагрузка при первом использовании
  - [ ] Subtask 3.2: Кэширование конфигурации
- [ ] Task 4: Создать hex.db.properties template
  - [ ] Subtask 4.1: Пример для local
  - [ ] Subtask 4.2: Пример для CI

### Dev Notes

#### Пример hex.properties
```properties
# Primary DataSource
hex.db.primary.url=jdbc:postgresql://localhost:5432/testdb
hex.db.primary.username=postgres
hex.db.primary.password=${DB_PASSWORD}
hex.db.primary.driver=org.postgresql.Driver

# Pool
hex.db.pool.size=10
hex.db.pool.min-idle=2
hex.db.pool.connection-timeout=30000
```

---

## Story 6.9: Transaction Manager

### Status
Not Started

### Story
**As a** test developer,
**I want** programmatic and declarative transaction management,
**so that** I can ensure data consistency in complex test scenarios.

### Acceptance Criteria
1. `TransactionManager` интерфейс с методами:
   - `executeInTransaction(Supplier<T>)`
   - `executeInTransaction(Runnable)`
   - `setRollbackOnly()`
   - `isTransactionActive()`
2. Аннотация `@Transactional` для методов репозитория.
3. Поддержка propagation: REQUIRED, REQUIRES_NEW, NESTED.
4. Savepoint для вложенных транзакций.
5. Автоматический rollback на исключение.
6. ThreadLocal изоляция транзакционного контекста.
7. Integration tests демонстрируют rollback.

### Tasks / Subtasks
- [ ] Task 1: Реализовать TransactionManager
  - [ ] Subtask 1.1: executeInTransaction методы
  - [ ] Subtask 1.2: ThreadLocal TransactionContext
  - [ ] Subtask 1.3: Savepoint поддержка
- [ ] Task 2: Реализовать @Transactional обработку
  - [ ] Subtask 2.1: Аннотация с propagation
  - [ ] Subtask 2.2: TransactionalInterceptor
- [ ] Task 3: Реализовать Propagation enum
  - [ ] Subtask 3.1: REQUIRED, REQUIRES_NEW, NESTED
  - [ ] Subtask 3.2: Логика выбора поведения
- [ ] Task 4: Integration tests
  - [ ] Subtask 4.1: Тест rollback on exception
  - [ ] Subtask 4.2: Тест nested transactions

### Dev Notes

#### Пример использования
```java
TransactionManager tx = DbServiceFactory.getTransactionManager();
tx.executeInTransaction(() -> {
    orderRepo.create(1L, BigDecimal.TEN);
    itemRepo.addItem(orderId, productId, 2);
    return orderId;
});
```

---

## Story 6.10: Interceptor Chain — Logging and Metrics

### Status
Not Started

### Story
**As a** framework user,
**I want** an interceptor system for cross-cutting concerns,
**so that** I can add logging, metrics, and custom logic without modifying repository code.

### Acceptance Criteria
1. Интерфейс `DbInterceptor` с методом `intercept(chain)`.
2. `DbInterceptorChain` для передачи управления.
3. Встроенные интерцепторы:
   - `QueryLoggingInterceptor` — логирует SQL и параметры
   - `SlowQueryInterceptor` — предупреждает о медленных запросах
   - `QueryTimeoutInterceptor` — устанавливает таймаут
   - `ConnectionTrackingInterceptor` — метрики соединений
4. Регистрация через Builder API.
5. Ordering через `getOrder()` метод.
6. Allure интеграция для логирования запросов как steps.

### Tasks / Subtasks
- [ ] Task 1: Реализовать DbInterceptor interface
  - [ ] Subtask 1.1: intercept(chain) метод
  - [ ] Subtask 1.2: getOrder() для ordering
- [ ] Task 2: Реализовать DbInterceptorChain
  - [ ] Subtask 2.1: query() — текущий запрос
  - [ ] Subtask 2.2: context() — контекст выполнения
  - [ ] Subtask 2.3: proceed(query) — продолжить
- [ ] Task 3: Реализовать встроенные интерцепторы
  - [ ] Subtask 3.1: QueryLoggingInterceptor
  - [ ] Subtask 3.2: SlowQueryInterceptor
  - [ ] Subtask 3.3: QueryTimeoutInterceptor
  - [ ] Subtask 3.4: ConnectionTrackingInterceptor
- [ ] Task 4: Allure интеграция
  - [ ] Subtask 4.1: AllureDbInterceptor
  - [ ] Subtask 4.2: SQL как step в отчёте

### Dev Notes

#### Порядок интерцепторов
```
Order: -1000 → QueryLogging (first)
Order: -500  → SlowQuery
Order: 0     → User interceptors
Order: 100   → QueryTimeout
Order: 500   → Transaction
Order: 1000  → QueryExecution (last)
```

---

## Story 6.11: Query Validators — Pre-execution Checks

### Status
Not Started

### Story
**As a** test developer,
**I want** static SQL validation before execution,
**so that** I catch errors early and prevent dangerous operations.

### Acceptance Criteria
1. Интерфейс `QueryValidator` с методом `validate(QueryDefinition)`.
2. Встроенные валидации:
   - Синтаксические: параметры совпадают с SQL
   - Безопасности: UPDATE/DELETE без WHERE требует @DangerousQuery
   - Типов: предупреждение о nullable без Optional
3. Аннотация `@DangerousQuery(reason, requireConfirmation)`.
4. Валидация при создании прокси (fail-fast).
5. Комбинирование валидаторов через CompositeValidator.
6. Отключение валидации через конфигурацию.

### Tasks / Subtasks
- [ ] Task 1: Реализовать QueryValidator interface
  - [ ] Subtask 1.1: validate(query) метод
  - [ ] Subtask 1.2: ValidationResult с warnings и errors
- [ ] Task 2: Реализовать ParameterValidator
  - [ ] Subtask 2.1: Проверка соответствия :param и @Param
  - [ ] Subtask 2.2: Предупреждение о неиспользуемых параметрах
- [ ] Task 3: Реализовать SecurityValidator
  - [ ] Subtask 3.1: Обнаружение опасных операций
  - [ ] Subtask 3.2: Проверка @DangerousQuery
  - [ ] Subtask 3.3: UPDATE/DELETE без WHERE
- [ ] Task 4: Реализовать CompositeQueryValidator
  - [ ] Subtask 4.1: Агрегация результатов
  - [ ] Subtask 4.2: Различение warnings и errors

### Dev Notes

#### Пример валидации
```java
// ❌ QueryValidationException: Parameter mismatch
@Select("SELECT * FROM users WHERE id = :userId")
Optional<User> findById(@Param("id") Long id);

// ⚠️ Warning: UPDATE without WHERE
@Update("UPDATE users SET active = false")
int deactivateAll();

// ✅ Explicit dangerous operation
@Update("UPDATE users SET active = false")
@DangerousQuery(reason = "Deactivate all for cleanup")
int deactivateAll();
```

---

## Story 6.12: Sample Repository and Integration Tests

### Status
Not Started

### Story
**As a** new adopter,
**I want** working examples and integration tests,
**so that** I can quickly understand how to use the DB module.

### Acceptance Criteria
1. Sample Entity `UserEntity` в hex-project-samples.
2. Sample Repository `UserRepository` с CRUD операциями.
3. Integration tests с H2 in-memory database.
4. Пример множественных DataSource.
5. Пример кастомного маппера.
6. Пример с транзакциями.
7. README с примерами использования.

### Tasks / Subtasks
- [ ] Task 1: Создать sample entities
  - [ ] Subtask 1.1: UserEntity с @Table, @Column
  - [ ] Subtask 1.2: OrderEntity с связями
- [ ] Task 2: Создать sample repositories
  - [ ] Subtask 2.1: UserRepository с CRUD
  - [ ] Subtask 2.2: OrderRepository с JOIN
- [ ] Task 3: Создать integration tests
  - [ ] Subtask 3.1: H2 конфигурация
  - [ ] Subtask 3.2: CRUD тесты
  - [ ] Subtask 3.3: Transaction тесты
  - [ ] Subtask 3.4: Parallel execution тесты
- [ ] Task 4: Документация
  - [ ] Subtask 4.1: README с примерами
  - [ ] Subtask 4.2: Javadoc на русском

### Dev Notes

#### Пример использования
```java
@DbService(dataSource = "primary")
public interface UserRepository {
    
    @Select("SELECT * FROM users WHERE id = :id")
    Optional<UserEntity> findById(@Param("id") Long id);
    
    @Insert("INSERT INTO users (name, email) VALUES (:name, :email)")
    @ReturnGeneratedKeys
    Long create(@Param("name") String name, @Param("email") String email);
}

// В тесте
UserRepository repo = DbServiceFactory.create(UserRepository.class);
Long id = repo.create("John", "john@example.com");
Optional<User> user = repo.findById(id);
```

---

## Story 6.13: JUnit 5 Extension for Test Isolation

### Status
Not Started

### Story
**As a** test developer,
**I want** automatic test isolation with rollback,
**so that** each test runs in a clean database state.

### Acceptance Criteria
1. `DbTestExtension` implements BeforeEachCallback, AfterEachCallback.
2. Автоматический savepoint перед каждым тестом.
3. Автоматический rollback после каждого теста.
4. Аннотация `@DbTest` для активации extension.
5. Поддержка параллельного выполнения через ThreadLocal.
6. Опция `@Commit` для коммита вместо rollback.
7. Integration с существующей тестовой инфраструктурой Hex.

### Tasks / Subtasks
- [ ] Task 1: Реализовать DbTestExtension
  - [ ] Subtask 1.1: BeforeEach — создание savepoint
  - [ ] Subtask 1.2: AfterEach — rollback to savepoint
  - [ ] Subtask 1.3: ThreadLocal для параллелизма
- [ ] Task 2: Создать аннотации
  - [ ] Subtask 2.1: @DbTest meta-annotation
  - [ ] Subtask 2.2: @Commit для skip rollback
- [ ] Task 3: Интегрировать с hex-core-testing
  - [ ] Subtask 3.1: BaseDbTest абстрактный класс
  - [ ] Subtask 3.2: Хелперы для создания репозиториев

### Dev Notes

#### Пример использования
```java
@ExtendWith(DbTestExtension.class)
class UserRepositoryTest {
    
    @Test
    void shouldCreateUser() {
        userRepo.create("test", "test@test.com");
        // После теста автоматический rollback
    }
}

// Или с meta-annotation
@DbTest
class UserRepositoryTest { ... }
```

---

## Story 6.14: Advanced Features — Call, Script, Batch

### Status
Not Started

### Story
**As an** advanced user,
**I want** support for stored procedures, scripts, and batch operations,
**so that** I can handle complex database scenarios.

### Acceptance Criteria
1. `@Call("{call procedure(:param)}")` для stored procedures.
2. `@OutParam` и `@InOutParam` для output параметров.
3. `@Script(resource = "sql/file.sql")` для SQL скриптов.
4. `@BatchInsert`, `@BatchUpdate` для batch операций.
5. `@ParamEntity` для маппинга entity на параметры.
6. Примеры и тесты для каждой фичи.

### Tasks / Subtasks
- [ ] Task 1: Реализовать @Call support
  - [ ] Subtask 1.1: CallableStatement execution
  - [ ] Subtask 1.2: OUT/INOUT parameter handling
- [ ] Task 2: Реализовать @Script support
  - [ ] Subtask 2.1: Resource file loading
  - [ ] Subtask 2.2: Script execution
- [ ] Task 3: Реализовать Batch операции
  - [ ] Subtask 3.1: @BatchInsert, @BatchUpdate
  - [ ] Subtask 3.2: @ParamEntity для entity list
  - [ ] Subtask 3.3: executeBatch implementation

### Dev Notes

#### Примеры
```java
// Stored procedure
@Call("{call calculate_total(:orderId, :result)}")
void calculateTotal(@Param("orderId") Long orderId,
                    @OutParam(value = "result", sqlType = Types.DECIMAL) BigDecimal[] result);

// SQL Script
@Script(resource = "db/cleanup.sql")
void cleanup();

// Batch
@BatchInsert("INSERT INTO items (name) VALUES (:name)")
int[] createBatch(@ParamEntity List<ItemEntity> items);
```

---

## Story 6.15: Escape Hatch — Raw Query and Direct Access

### Status
Not Started

### Story
**As a** power user,
**I want** escape hatches for direct database access,
**so that** I can handle edge cases not covered by declarative API.

### Acceptance Criteria
1. `DbServiceFactory.getConnection(dataSource)` для прямого Connection.
2. `DbServiceFactory.getDataSource(dataSource)` для DataSource.
3. `@RawQuery` аннотация для динамического SQL.
4. `QueryExecutor` доступен напрямую.
5. `@SqlTemplate` для загрузки SQL из файлов.
6. Документация по escape hatch сценариям.

### Tasks / Subtasks
- [ ] Task 1: Добавить прямой доступ в DbServiceFactory
  - [ ] Subtask 1.1: getConnection(name)
  - [ ] Subtask 1.2: getDataSource(name)
  - [ ] Subtask 1.3: getQueryExecutor()
- [ ] Task 2: Реализовать @RawQuery
  - [ ] Subtask 2.1: Аннотация
  - [ ] Subtask 2.2: executeRaw метод
- [ ] Task 3: Реализовать @SqlTemplate
  - [ ] Subtask 3.1: Resource loading
  - [ ] Subtask 3.2: @TemplateParam для placeholders
- [ ] Task 4: Документация
  - [ ] Subtask 4.1: Примеры использования
  - [ ] Subtask 4.2: Best practices

### Dev Notes

#### Примеры
```java
// Direct connection
try (Connection conn = DbServiceFactory.getConnection("primary")) {
    PreparedStatement ps = conn.prepareStatement("...");
    // ...
}

// Raw query
@RawQuery
<T> T executeRaw(String sql, Map<String, Object> params, RowMapper<T> mapper);

// SQL template
@Select
@SqlTemplate(resource = "sql/report.sql")
List<ReportRow> generateReport(@Param("startDate") LocalDate start,
                                @TemplateParam("groupBy") String column);
```

---

## Implementation Priority

### Phase 1: Foundation (MVP) — Stories 6.1-6.7
**Цель:** Минимальный работающий модуль с CRUD операциями

1. **Story 6.1** — Структура модуля и зависимости
2. **Story 6.2** — Core аннотации
3. **Story 6.3** — DbServiceFactory и прокси
4. **Story 6.4** — Connection Provider и HikariCP
5. **Story 6.5** — Query Executor
6. **Story 6.6** — Result Mapper
7. **Story 6.7** — Exception Hierarchy

### Phase 2: Configuration & Transactions — Stories 6.8-6.9
**Цель:** Полноценное конфигурирование и транзакции

8. **Story 6.8** — DbConfig и Owner интеграция
9. **Story 6.9** — Transaction Manager

### Phase 3: Observability — Stories 6.10-6.11
**Цель:** Логирование, метрики, валидация

10. **Story 6.10** — Interceptor Chain
11. **Story 6.11** — Query Validators

### Phase 4: Testing & Examples — Stories 6.12-6.13
**Цель:** Примеры и тестовая инфраструктура

12. **Story 6.12** — Sample Repository и Integration Tests
13. **Story 6.13** — JUnit 5 Extension для изоляции

### Phase 5: Advanced Features — Stories 6.14-6.15
**Цель:** Продвинутые возможности

14. **Story 6.14** — Call, Script, Batch
15. **Story 6.15** — Escape Hatch

---

## Technical Dependencies

| Story | Depends On |
|-------|------------|
| 6.2 | 6.1 |
| 6.3 | 6.2 |
| 6.4 | 6.1 |
| 6.5 | 6.3, 6.4 |
| 6.6 | 6.5 |
| 6.7 | 6.1 |
| 6.8 | 6.1 |
| 6.9 | 6.4, 6.7 |
| 6.10 | 6.3 |
| 6.11 | 6.3 |
| 6.12 | 6.6, 6.8 |
| 6.13 | 6.9, 6.12 |
| 6.14 | 6.5 |
| 6.15 | 6.5 |

---

## Success Metrics

| Metric | Target |
|--------|--------|
| Boilerplate reduction | ≥70% vs raw JDBC |
| Query execution overhead | <5% vs raw JDBC |
| Parallel test support | 100% thread-safe |
| Unit test coverage | ≥85% |
| Integration tests | All CRUD + transactions |

---

*Epic создан: 2025-12-02*
*Версия: 1.0*
```
