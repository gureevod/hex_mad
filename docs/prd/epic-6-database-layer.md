# Epic 6: Database Layer (hex-core-db)
**Epic Goal:** Предоставить лёгкий, типобезопасный и потокобезопасный модуль для работы с базами данных в тестах. Модуль должен включать fluent SQL-билдеры, HikariCP пул соединений из коробки, поддержку транзакций и логирование SQL-запросов.

**Story 6.1: Core Abstractions и Query Executor**
As a тест-разработчик,
I want базовые интерфейсы и исполнитель запросов с поддержкой HikariCP,
so that я могу безопасно выполнять SQL-запросы из тестов без утечки ресурсов.
**Acceptance Criteria:**
1. Создан модуль `hex-core-db` с зависимостями HikariCP и SLF4J.
2. Реализованы core-интерфейсы: `Row`, `RowMapper<T>`, `Query`, `QueryResult`, `QueryExecutor`.
3. Реализованы `DefaultQueryExecutor`, `SelectQueryResult`, `ModificationQueryResult`, `MapRow`.
4. Все данные читаются в память при создании `QueryResult` — никаких открытых ресурсов.
5. `QueryExecutor` потокобезопасен — каждый запрос получает свой connection из пула.
7. Javadoc на русском для всех публичных API.

**Story 6.2: SQL Builders и Fluent API**
As a тест-разработчик,
I want fluent SQL-билдеры для SELECT, INSERT, UPDATE, DELETE,
so that я могу строить запросы типобезопасно и читаемо без написания сырого SQL.
**Acceptance Criteria:**
1. Реализован класс `SQL` как точка входа для создания билдеров.
2. Реализованы `SelectBuilder` и `ExecutableSelectBuilder` с поддержкой JOIN, WHERE (включая `whereIn`, `whereNull`, `whereIf`), GROUP BY, ORDER BY, LIMIT/OFFSET.
3. Реализованы `InsertBuilder` и `ExecutableInsertBuilder` с поддержкой `returningKeys()`.
4. Реализованы `UpdateBuilder` и `ExecutableUpdateBuilder` с защитой от UPDATE без WHERE.
5. Реализованы `DeleteBuilder` и `ExecutableDeleteBuilder` с защитой от DELETE без WHERE (требуется явный вызов `all()`).
6. Реализованы `BatchInsertBuilder` и `RawQueryBuilder` для batch-операций и сложных запросов.
7. Билдеры НЕ потокобезопасны — документация явно указывает создавать новый экземпляр в каждом потоке.

**Story 6.3: Db API, Конфигурация и Транзакции**
As a тест-разработчик,
I want единую точку входа `Db` с простой конфигурацией и поддержкой транзакций,
so that я могу настроить подключение в одну строку и использовать транзакции для изоляции тестовых данных.
**Acceptance Criteria:**
1. Реализован класс `Db` как статическая точка входа с методами `configure()`, `select()`, `insertInto()`, `update()`, `deleteFrom()`, `raw()`.
2. Реализован `DbInstance` для поддержки multi-datasource сценариев (`Db.register()`, `Db.use()`).
3. Реализован `DbConfig` с builder API для простой и продвинутой настройки HikariCP.
4. Поддержка транзакций через `Db.transaction(Supplier<T>)` с ThreadLocal-изоляцией.
5. Реализован `QueryInterceptor` и `LoggingInterceptor` для логирования SQL с параметрами.
6. Методы `executeScript()` и `fromResource()` для выполнения SQL из файлов.
7. Методы `shutdown()` и `shutdownAll()` для корректного закрытия пула.
8. README.md модуля на русском с примерами использования.