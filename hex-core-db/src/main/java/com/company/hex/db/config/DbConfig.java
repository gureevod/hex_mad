package com.company.hex.db.config;

/**
 * Интерфейс конфигурации для DB модуля.
 *
 * <p>Содержит настройки подключения к базе данных, пула соединений,
 * выполнения запросов и логирования.</p>
 *
 * <h2>Использование с Owner</h2>
 * <pre>{@code
 * // Загрузка конфигурации
 * DbConfig config = HexConfigFactory.getConfig(DbConfig.class);
 *
 * // Использование
 * String url = config.getPrimaryUrl();
 * int poolSize = config.getPoolSize();
 * }</pre>
 *
 * <h2>Конфигурационный файл (hex.properties)</h2>
 * <pre>{@code
 * # Primary DataSource
 * hex.db.primary.url=jdbc:postgresql://localhost:5432/testdb
 * hex.db.primary.username=postgres
 * hex.db.primary.password=${DB_PASSWORD}
 *
 * # Pool
 * hex.db.pool.size=10
 * hex.db.pool.min-idle=2
 * hex.db.pool.connection-timeout=30000
 *
 * # Query
 * hex.db.query.timeout=30
 * hex.db.query.validation.enabled=true
 * }</pre>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 */
public interface DbConfig {

    // ==================== Primary DataSource ====================

    /**
     * URL подключения к primary DataSource.
     *
     * @return JDBC URL
     */
    default String getPrimaryUrl() {
        return "jdbc:h2:mem:testdb";
    }

    /**
     * Имя пользователя для primary DataSource.
     *
     * @return имя пользователя
     */
    default String getPrimaryUsername() {
        return "sa";
    }

    /**
     * Пароль для primary DataSource.
     *
     * @return пароль
     */
    default String getPrimaryPassword() {
        return "";
    }

    /**
     * Класс JDBC драйвера.
     *
     * @return полное имя класса драйвера
     */
    default String getPrimaryDriver() {
        return "org.h2.Driver";
    }

    // ==================== Pool Settings ====================

    /**
     * Размер пула соединений.
     *
     * @return максимальное количество соединений в пуле
     */
    default int getPoolSize() {
        return 10;
    }

    /**
     * Минимальное количество idle соединений.
     *
     * @return минимальное количество соединений
     */
    default int getPoolMinIdle() {
        return 2;
    }

    /**
     * Максимальное время жизни соединения в миллисекундах.
     *
     * @return время жизни в мс
     */
    default long getPoolMaxLifetime() {
        return 1800000; // 30 минут
    }

    /**
     * Таймаут получения соединения из пула в миллисекундах.
     *
     * @return таймаут в мс
     */
    default long getPoolConnectionTimeout() {
        return 30000; // 30 секунд
    }

    // ==================== Query Settings ====================

    /**
     * Таймаут выполнения запроса в секундах.
     *
     * @return таймаут в секундах
     */
    default int getQueryTimeout() {
        return 30;
    }

    /**
     * Размер выборки для ResultSet.
     *
     * @return fetch size
     */
    default int getQueryFetchSize() {
        return 100;
    }

    /**
     * Включена ли валидация запросов.
     *
     * @return true если валидация включена
     */
    default boolean isQueryValidationEnabled() {
        return true;
    }

    /**
     * Включена ли строгая валидация.
     *
     * @return true если строгая валидация включена
     */
    default boolean isQueryValidationStrict() {
        return false;
    }

    // ==================== Logging Settings ====================

    /**
     * Включено ли логирование запросов.
     *
     * @return true если логирование включено
     */
    default boolean isLoggingEnabled() {
        return true;
    }

    /**
     * Порог для предупреждения о медленных запросах в миллисекундах.
     *
     * @return порог в мс
     */
    default long getSlowQueryThresholdMs() {
        return 5000; // 5 секунд
    }
}
