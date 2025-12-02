/**
 * Пакет с конфигурационными аннотациями для DB сервисов.
 *
 * <p>Содержит аннотации для конфигурации репозиториев:</p>
 *
 * <ul>
 *     <li>{@code @DbService} — основная аннотация для маркировки интерфейса репозитория</li>
 * </ul>
 *
 * <h2>Пример использования</h2>
 * <pre>{@code
 * @DbService(dataSource = "primary", queryTimeout = 30, validateQueries = true)
 * public interface UserRepository {
 *     // методы репозитория
 * }
 * }</pre>
 *
 * @see com.company.hex.db.service.DbServiceFactory
 * @since 1.0.0
 */
package com.company.hex.db.annotations.config;
