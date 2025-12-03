/**
 * Пакет с компонентами управления соединениями.
 *
 * <p>Содержит классы для управления подключениями к БД:</p>
 *
 * <ul>
 *     <li>{@link com.company.hex.db.connection.ConnectionProvider} — управление пулами соединений через HikariCP</li>
 *     <li>{@link com.company.hex.db.connection.DataSourceConfig} — конфигурация DataSource для HikariCP</li>
 *     <li>{@link com.company.hex.db.connection.ConnectionContext} — контекст соединения для транзакций</li>
 * </ul>
 *
 * <h2>Основные возможности</h2>
 * <ul>
 *     <li>Потокобезопасное управление пулами соединений</li>
 *     <li>Поддержка множественных DataSource по имени</li>
 *     <li>ThreadLocal изоляция для параллельных тестов</li>
 *     <li>Метрики пула (active, idle, pending connections)</li>
 *     <li>Graceful shutdown пулов соединений</li>
 * </ul>
 *
 * <h2>Пример использования ConnectionProvider</h2>
 * <pre>{@code
 * // Регистрация DataSource
 * ConnectionProvider provider = ConnectionProvider.getInstance();
 * provider.registerDataSource("primary", DataSourceConfig.builder()
 *     .url("jdbc:postgresql://localhost:5432/testdb")
 *     .username("postgres")
 *     .password("secret")
 *     .poolSize(10)
 *     .build());
 *
 * // Получение соединения
 * try (Connection conn = provider.getConnection("primary")) {
 *     // работа с БД
 * }
 *
 * // Метрики
 * PoolMetrics metrics = provider.getPoolMetrics("primary");
 * System.out.println("Active: " + metrics.activeConnections());
 * }</pre>
 *
 * <h2>Пример для тестов с H2</h2>
 * <pre>{@code
 * // Быстрая настройка H2 in-memory
 * DataSourceConfig h2Config = DataSourceConfig.h2InMemory("testdb");
 * provider.registerDataSource("primary", h2Config);
 * }</pre>
 *
 * @since 1.0.0
 * @see com.company.hex.db.connection.ConnectionProvider
 * @see com.company.hex.db.connection.DataSourceConfig
 */
package com.company.hex.db.connection;
