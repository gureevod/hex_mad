package com.company.hex.db.annotations.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Маркирует интерфейс как DB сервис и предоставляет конфигурацию уровня сервиса.
 *
 * <p>Аннотация определяет интерфейс репозитория для работы с базой данных.
 * Фреймворк создаёт proxy-реализацию этого интерфейса через
 * {@code DbServiceFactory.create(MyRepository.class)}.</p>
 *
 * <h2>Примеры использования</h2>
 * <pre>{@code
 * // Базовое использование с primary DataSource
 * @DbService
 * public interface UserRepository {
 *     @Select("SELECT * FROM users WHERE id = :id")
 *     Optional<User> findById(@Param("id") Long id);
 * }
 *
 * // Указание конкретного DataSource
 * @DbService(dataSource = "analytics")
 * public interface AnalyticsRepository {
 *     @Select("SELECT count(*) FROM events WHERE date = :date")
 *     long countEventsByDate(@Param("date") LocalDate date);
 * }
 *
 * // Полная конфигурация
 * @DbService(
 *     dataSource = "primary",
 *     queryTimeout = 60,
 *     validateQueries = true
 * )
 * public interface OrderRepository {
 *     @Select("SELECT * FROM orders WHERE user_id = :userId")
 *     List<Order> findByUserId(@Param("userId") Long userId);
 *
 *     @Insert("INSERT INTO orders (user_id, total) VALUES (:userId, :total)")
 *     @ReturnGeneratedKeys
 *     Long create(@Param("userId") Long userId, @Param("total") BigDecimal total);
 * }
 *
 * // Использование в тесте
 * UserRepository repo = DbServiceFactory.create(UserRepository.class);
 * Optional<User> user = repo.findById(1L);
 * }</pre>
 *
 * <h2>Конфигурация DataSource</h2>
 * <p>DataSource настраивается в {@code hex.properties}:</p>
 * <pre>{@code
 * # Primary DataSource
 * hex.db.primary.url=jdbc:postgresql://localhost:5432/testdb
 * hex.db.primary.username=postgres
 * hex.db.primary.password=${DB_PASSWORD}
 *
 * # Secondary DataSource
 * hex.db.analytics.url=jdbc:postgresql://analytics-host:5432/analytics
 * hex.db.analytics.username=analytics_user
 * hex.db.analytics.password=${ANALYTICS_DB_PASSWORD}
 * }</pre>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see com.company.hex.db.service.DbServiceFactory
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DbService {

    /**
     * Имя DataSource для использования.
     *
     * <p>Имя должно соответствовать конфигурации в {@code hex.properties}
     * с префиксом {@code hex.db.{dataSource}}.</p>
     *
     * <p>По умолчанию {@code "primary"}.</p>
     *
     * @return имя DataSource
     */
    String dataSource() default "primary";

    /**
     * Таймаут выполнения запроса в секундах.
     *
     * <p>Применяется ко всем методам сервиса, если не переопределено
     * аннотацией {@link Timeout} на уровне метода.</p>
     *
     * <p>Значение 0 означает отсутствие таймаута (по умолчанию 30 секунд).</p>
     *
     * @return таймаут в секундах
     */
    int queryTimeout() default 30;

    /**
     * Включить валидацию SQL запросов при создании прокси.
     *
     * <p>Если {@code true}, все SQL запросы будут провалидированы
     * при создании сервиса (fail-fast поведение):</p>
     * <ul>
     *     <li>Проверка соответствия параметров</li>
     *     <li>Предупреждения об опасных операциях</li>
     *     <li>Проверка синтаксиса (базовая)</li>
     * </ul>
     *
     * @return {@code true} для включения валидации
     */
    boolean validateQueries() default true;

    /**
     * Fetch size для ResultSet.
     *
     * <p>Определяет количество строк, загружаемых драйвером за один
     * раз при итерации по ResultSet. Влияет на производительность
     * для больших результатов.</p>
     *
     * <p>Значение 0 использует значение по умолчанию драйвера.</p>
     *
     * @return fetch size
     */
    int fetchSize() default 0;
}
