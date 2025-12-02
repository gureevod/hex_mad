/**
 * Корневой пакет декларативного модуля для работы с базами данных.
 *
 * <p>Модуль {@code hex-core-db} предоставляет декларативный подход к работе с базами данных
 * в тестах, вдохновлённый Spring Data JDBC и MyBatis. Основные возможности:</p>
 *
 * <ul>
 *     <li>Аннотированные интерфейсы репозиториев с автоматической реализацией</li>
 *     <li>Потокобезопасный доступ к данным для параллельного выполнения тестов</li>
 *     <li>Автоматический маппинг результатов запросов на Java объекты</li>
 *     <li>Система интерцепторов для логирования и мониторинга</li>
 *     <li>Управление транзакциями и connection pooling через HikariCP</li>
 *     <li>Статическая валидация SQL до выполнения</li>
 * </ul>
 *
 * <h2>Быстрый старт</h2>
 * <pre>{@code
 * @DbService(dataSource = "primary")
 * public interface UserRepository {
 *
 *     @Select("SELECT * FROM users WHERE id = :id")
 *     Optional<User> findById(@Param("id") Long id);
 *
 *     @Insert("INSERT INTO users (name, email) VALUES (:name, :email)")
 *     @ReturnGeneratedKeys
 *     Long create(@Param("name") String name, @Param("email") String email);
 * }
 *
 * // Использование
 * UserRepository repo = DbServiceFactory.create(UserRepository.class);
 * Optional<User> user = repo.findById(1L);
 * }</pre>
 *
 * @see com.company.hex.db.service.DbServiceFactory
 * @see com.company.hex.db.annotations.config.DbService
 * @since 1.0.0
 */
package com.company.hex.db;
