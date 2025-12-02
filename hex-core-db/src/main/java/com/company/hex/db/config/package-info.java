/**
 * Пакет с конфигурацией для модуля hex-core-db.
 *
 * <p>Содержит классы для конфигурации подключения к БД через Owner:</p>
 *
 * <ul>
 *     <li>{@code DbConfig} — Owner интерфейс с настройками DataSource</li>
 *     <li>Поддержка множественных DataSource через prefix</li>
 *     <li>Интеграция с environment variables</li>
 * </ul>
 *
 * <h2>Пример конфигурации (hex.properties)</h2>
 * <pre>{@code
 * # Primary DataSource
 * hex.db.primary.url=jdbc:postgresql://localhost:5432/testdb
 * hex.db.primary.username=postgres
 * hex.db.primary.password=${DB_PASSWORD}
 * hex.db.primary.driver=org.postgresql.Driver
 *
 * # Pool settings
 * hex.db.pool.size=10
 * hex.db.pool.min-idle=2
 * hex.db.pool.connection-timeout=30000
 * }</pre>
 *
 * @see com.company.hex.db.connection.ConnectionProvider
 * @since 1.0.0
 */
package com.company.hex.db.config;
