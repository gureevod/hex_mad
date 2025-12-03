package com.company.hex.db.connection;

import com.company.hex.db.config.DbConfig;
import com.company.hex.db.exception.ConnectionException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Потокобезопасный провайдер соединений с базой данных.
 *
 * <p>Использует HikariCP для эффективного пулинга соединений.
 * Поддерживает множественные DataSource и ThreadLocal изоляцию
 * для транзакционного контекста.</p>
 *
 * <h2>Основные возможности</h2>
 * <ul>
 *     <li>Управление пулами соединений через HikariCP</li>
 *     <li>Поддержка множественных DataSource по имени</li>
 *     <li>ThreadLocal изоляция для транзакций</li>
 *     <li>Автоматическое освобождение соединений</li>
 *     <li>Метрики пула (active connections, pending)</li>
 * </ul>
 *
 * <h2>Пример использования</h2>
 * <pre>{@code
 * // Получение singleton instance
 * ConnectionProvider provider = ConnectionProvider.getInstance();
 *
 * // Регистрация DataSource
 * provider.registerDataSource("primary", dbConfig);
 *
 * // Получение соединения
 * try (Connection conn = provider.getConnection("primary")) {
 *     // работа с БД
 * }
 *
 * // Метрики
 * int active = provider.getActiveConnections("primary");
 * }</pre>
 *
 * <h2>Транзакционный контекст</h2>
 * <p>При активной транзакции соединение привязывается к текущему потоку
 * через ThreadLocal и переиспользуется для всех запросов в рамках транзакции.</p>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see DataSourceConfig
 * @see ConnectionContext
 */
public final class ConnectionProvider {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionProvider.class);

    /**
     * Singleton instance.
     */
    private static volatile ConnectionProvider instance;

    /**
     * Lock для double-checked locking.
     */
    private static final Object LOCK = new Object();

    /**
     * Зарегистрированные DataSource по имени.
     */
    private final ConcurrentHashMap<String, HikariDataSource> dataSources = new ConcurrentHashMap<>();

    /**
     * ThreadLocal контекст для транзакций.
     */
    private final ThreadLocal<ConnectionContext> transactionContext = new ThreadLocal<>();

    /**
     * Флаг инициализации.
     */
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    /**
     * Счётчик активных соединений (для метрик).
     */
    private final ConcurrentHashMap<String, AtomicInteger> activeConnectionCounts = new ConcurrentHashMap<>();

    /**
     * Приватный конструктор для singleton.
     */
    private ConnectionProvider() {
        logger.debug("ConnectionProvider создан");
    }

    /**
     * Возвращает singleton instance ConnectionProvider.
     *
     * @return экземпляр ConnectionProvider
     */
    public static ConnectionProvider getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new ConnectionProvider();
                }
            }
        }
        return instance;
    }

    /**
     * Сбрасывает singleton для тестирования.
     * <strong>Только для тестов!</strong>
     */
    static void resetForTesting() {
        synchronized (LOCK) {
            if (instance != null) {
                instance.shutdown();
                instance = null;
            }
        }
    }

    /**
     * Регистрирует DataSource с указанными настройками.
     *
     * @param name имя DataSource
     * @param config конфигурация DataSource
     * @throws ConnectionException если регистрация не удалась
     */
    public void registerDataSource(String name, DataSourceConfig config) {
        if (name == null || name.trim().isEmpty()) {
            throw new ConnectionException("DataSource name cannot be null or empty");
        }
        if (config == null) {
            throw new ConnectionException("DataSource config cannot be null");
        }

        logger.info("Регистрация DataSource: {}", name);

        try {
            HikariConfig hikariConfig = createHikariConfig(name, config);
            HikariDataSource dataSource = new HikariDataSource(hikariConfig);

            HikariDataSource previous = dataSources.putIfAbsent(name, dataSource);
            if (previous != null) {
                // Закрываем новый, если уже был зарегистрирован
                dataSource.close();
                logger.warn("DataSource '{}' уже зарегистрирован, пропускаем", name);
            } else {
                activeConnectionCounts.put(name, new AtomicInteger(0));
                logger.info("DataSource '{}' успешно зарегистрирован. Pool size: {}",
                    name, config.getPoolSize());
            }

            initialized.set(true);

        } catch (Exception e) {
            throw new ConnectionException(
                "Не удалось зарегистрировать DataSource: " + name, e);
        }
    }

    /**
     * Регистрирует DataSource на основе DbConfig.
     *
     * @param name имя DataSource
     * @param dbConfig конфигурация из hex.properties
     */
    public void registerDataSource(String name, DbConfig dbConfig) {
        DataSourceConfig config = DataSourceConfig.fromDbConfig(name, dbConfig);
        registerDataSource(name, config);
    }

    /**
     * Регистрирует готовый HikariDataSource.
     *
     * @param name имя DataSource
     * @param dataSource готовый HikariDataSource
     */
    public void registerDataSource(String name, HikariDataSource dataSource) {
        if (name == null || name.trim().isEmpty()) {
            throw new ConnectionException("DataSource name cannot be null or empty");
        }
        if (dataSource == null) {
            throw new ConnectionException("DataSource cannot be null");
        }

        HikariDataSource previous = dataSources.putIfAbsent(name, dataSource);
        if (previous != null) {
            logger.warn("DataSource '{}' уже зарегистрирован, пропускаем", name);
        } else {
            activeConnectionCounts.put(name, new AtomicInteger(0));
            initialized.set(true);
            logger.info("DataSource '{}' зарегистрирован (готовый)", name);
        }
    }

    /**
     * Получает соединение для указанного DataSource.
     *
     * <p>Если активна транзакция в текущем потоке — возвращает
     * транзакционное соединение. Иначе берёт новое из пула.</p>
     *
     * @param dataSourceName имя DataSource
     * @return соединение с базой данных
     * @throws ConnectionException если соединение не может быть получено
     */
    public Connection getConnection(String dataSourceName) {
        validateDataSourceName(dataSourceName);

        // Проверяем транзакционный контекст
        ConnectionContext ctx = transactionContext.get();
        if (ctx != null && ctx.hasConnection(dataSourceName)) {
            logger.trace("Возвращаем транзакционное соединение для '{}'", dataSourceName);
            return ctx.getConnection(dataSourceName);
        }

        // Получаем из пула
        HikariDataSource ds = dataSources.get(dataSourceName);
        if (ds == null) {
            throw new ConnectionException("DataSource не найден: " + dataSourceName
                + ". Доступные: " + dataSources.keySet());
        }

        try {
            Connection conn = ds.getConnection();
            activeConnectionCounts.get(dataSourceName).incrementAndGet();
            logger.trace("Получено соединение из пула '{}'. Active: {}",
                dataSourceName, getActiveConnections(dataSourceName));
            return conn;
        } catch (SQLException e) {
            throw new ConnectionException(
                "Не удалось получить соединение из " + dataSourceName, e);
        }
    }

    /**
     * Получает соединение для DataSource по умолчанию ("primary").
     *
     * @return соединение с базой данных
     * @throws ConnectionException если соединение не может быть получено
     */
    public Connection getConnection() {
        return getConnection("primary");
    }

    /**
     * Освобождает соединение (возвращает в пул).
     *
     * <p>Если соединение участвует в транзакции — не закрывает его.</p>
     *
     * @param connection соединение для освобождения
     * @param dataSourceName имя DataSource
     */
    public void releaseConnection(Connection connection, String dataSourceName) {
        if (connection == null) {
            return;
        }

        // Проверяем, не транзакционное ли соединение
        ConnectionContext ctx = transactionContext.get();
        if (ctx != null && ctx.isTransactionalConnection(connection)) {
            logger.trace("Соединение управляется транзакцией, не закрываем");
            return;
        }

        try {
            connection.close();
            if (dataSourceName != null) {
                AtomicInteger counter = activeConnectionCounts.get(dataSourceName);
                if (counter != null) {
                    counter.decrementAndGet();
                }
            }
            logger.trace("Соединение возвращено в пул '{}'", dataSourceName);
        } catch (SQLException e) {
            logger.warn("Ошибка при закрытии соединения: {}", e.getMessage());
        }
    }

    /**
     * Освобождает соединение без указания DataSource.
     *
     * @param connection соединение для освобождения
     */
    public void releaseConnection(Connection connection) {
        releaseConnection(connection, null);
    }

    /**
     * Возвращает DataSource по имени.
     *
     * @param name имя DataSource
     * @return DataSource или null если не найден
     */
    public DataSource getDataSource(String name) {
        return dataSources.get(name);
    }

    /**
     * Возвращает HikariDataSource по имени.
     *
     * @param name имя DataSource
     * @return HikariDataSource или null
     */
    public HikariDataSource getHikariDataSource(String name) {
        return dataSources.get(name);
    }

    /**
     * Проверяет, зарегистрирован ли DataSource.
     *
     * @param name имя DataSource
     * @return true если зарегистрирован
     */
    public boolean hasDataSource(String name) {
        return dataSources.containsKey(name);
    }

    /**
     * Возвращает имена всех зарегистрированных DataSource.
     *
     * @return неизменяемое множество имён
     */
    public Set<String> getDataSourceNames() {
        return Collections.unmodifiableSet(dataSources.keySet());
    }

    /**
     * Устанавливает транзакционный контекст для текущего потока.
     *
     * @param context контекст транзакции
     */
    public void setTransactionContext(ConnectionContext context) {
        transactionContext.set(context);
        logger.trace("Установлен транзакционный контекст для потока {}",
            Thread.currentThread().getName());
    }

    /**
     * Получает транзакционный контекст текущего потока.
     *
     * @return контекст или null
     */
    public ConnectionContext getTransactionContext() {
        return transactionContext.get();
    }

    /**
     * Очищает транзакционный контекст текущего потока.
     */
    public void clearTransactionContext() {
        transactionContext.remove();
        logger.trace("Очищен транзакционный контекст для потока {}",
            Thread.currentThread().getName());
    }

    // ==================== Метрики ====================

    /**
     * Возвращает количество активных соединений.
     *
     * @param dataSourceName имя DataSource
     * @return количество активных соединений
     */
    public int getActiveConnections(String dataSourceName) {
        HikariDataSource ds = dataSources.get(dataSourceName);
        if (ds != null && ds.getHikariConfigMXBean() != null) {
            try {
                return ds.getHikariPoolMXBean().getActiveConnections();
            } catch (Exception e) {
                logger.trace("Не удалось получить метрики для '{}': {}", dataSourceName, e.getMessage());
            }
        }
        return 0;
    }

    /**
     * Возвращает количество idle соединений.
     *
     * @param dataSourceName имя DataSource
     * @return количество idle соединений
     */
    public int getIdleConnections(String dataSourceName) {
        HikariDataSource ds = dataSources.get(dataSourceName);
        if (ds != null && ds.getHikariConfigMXBean() != null) {
            try {
                return ds.getHikariPoolMXBean().getIdleConnections();
            } catch (Exception e) {
                logger.trace("Не удалось получить метрики для '{}': {}", dataSourceName, e.getMessage());
            }
        }
        return 0;
    }

    /**
     * Возвращает количество ожидающих потоков.
     *
     * @param dataSourceName имя DataSource
     * @return количество ожидающих потоков
     */
    public int getPendingThreads(String dataSourceName) {
        HikariDataSource ds = dataSources.get(dataSourceName);
        if (ds != null && ds.getHikariConfigMXBean() != null) {
            try {
                return ds.getHikariPoolMXBean().getThreadsAwaitingConnection();
            } catch (Exception e) {
                logger.trace("Не удалось получить метрики для '{}': {}", dataSourceName, e.getMessage());
            }
        }
        return 0;
    }

    /**
     * Возвращает общее количество соединений в пуле.
     *
     * @param dataSourceName имя DataSource
     * @return размер пула
     */
    public int getTotalConnections(String dataSourceName) {
        HikariDataSource ds = dataSources.get(dataSourceName);
        if (ds != null && ds.getHikariConfigMXBean() != null) {
            try {
                return ds.getHikariPoolMXBean().getTotalConnections();
            } catch (Exception e) {
                logger.trace("Не удалось получить метрики для '{}': {}", dataSourceName, e.getMessage());
            }
        }
        return 0;
    }

    /**
     * Возвращает метрики всех DataSource.
     *
     * @return карта имя → метрики
     */
    public Map<String, PoolMetrics> getAllPoolMetrics() {
        ConcurrentHashMap<String, PoolMetrics> metrics = new ConcurrentHashMap<>();
        for (String name : dataSources.keySet()) {
            metrics.put(name, getPoolMetrics(name));
        }
        return Collections.unmodifiableMap(metrics);
    }

    /**
     * Возвращает метрики конкретного пула.
     *
     * @param dataSourceName имя DataSource
     * @return метрики пула
     */
    public PoolMetrics getPoolMetrics(String dataSourceName) {
        HikariDataSource ds = dataSources.get(dataSourceName);
        if (ds == null) {
            return new PoolMetrics(dataSourceName, 0, 0, 0, 0);
        }

        try {
            var poolMXBean = ds.getHikariPoolMXBean();
            if (poolMXBean != null) {
                return new PoolMetrics(
                    dataSourceName,
                    poolMXBean.getActiveConnections(),
                    poolMXBean.getIdleConnections(),
                    poolMXBean.getTotalConnections(),
                    poolMXBean.getThreadsAwaitingConnection()
                );
            }
        } catch (Exception e) {
            logger.trace("Не удалось получить метрики для '{}': {}", dataSourceName, e.getMessage());
        }

        return new PoolMetrics(dataSourceName, 0, 0, 0, 0);
    }

    // ==================== Управление жизненным циклом ====================

    /**
     * Проверяет, инициализирован ли провайдер.
     *
     * @return true если есть хотя бы один DataSource
     */
    public boolean isInitialized() {
        return initialized.get() && !dataSources.isEmpty();
    }

    /**
     * Завершает работу всех пулов соединений.
     *
     * <p>Вызывается при завершении работы приложения
     * или в тестах для очистки ресурсов.</p>
     */
    public void shutdown() {
        logger.info("Завершение работы ConnectionProvider...");

        for (Map.Entry<String, HikariDataSource> entry : dataSources.entrySet()) {
            try {
                entry.getValue().close();
                logger.info("DataSource '{}' закрыт", entry.getKey());
            } catch (Exception e) {
                logger.warn("Ошибка при закрытии DataSource '{}': {}",
                    entry.getKey(), e.getMessage());
            }
        }

        dataSources.clear();
        activeConnectionCounts.clear();
        transactionContext.remove();
        initialized.set(false);

        logger.info("ConnectionProvider завершил работу");
    }

    /**
     * Закрывает конкретный DataSource.
     *
     * @param name имя DataSource
     */
    public void closeDataSource(String name) {
        HikariDataSource ds = dataSources.remove(name);
        if (ds != null) {
            try {
                ds.close();
                activeConnectionCounts.remove(name);
                logger.info("DataSource '{}' закрыт", name);
            } catch (Exception e) {
                logger.warn("Ошибка при закрытии DataSource '{}': {}",
                    name, e.getMessage());
            }
        }
    }

    // ==================== Private Methods ====================

    /**
     * Создаёт HikariConfig из DataSourceConfig.
     */
    private HikariConfig createHikariConfig(String name, DataSourceConfig config) {
        HikariConfig hikariConfig = new HikariConfig();

        // Основные настройки
        hikariConfig.setPoolName("HexDB-" + name);
        hikariConfig.setJdbcUrl(config.getUrl());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());

        // Драйвер (опционально, HikariCP обычно определяет автоматически)
        if (config.getDriverClassName() != null && !config.getDriverClassName().isEmpty()) {
            hikariConfig.setDriverClassName(config.getDriverClassName());
        }

        // Pool settings
        hikariConfig.setMaximumPoolSize(config.getPoolSize());
        hikariConfig.setMinimumIdle(config.getMinIdle());
        hikariConfig.setMaxLifetime(config.getMaxLifetime());
        hikariConfig.setConnectionTimeout(config.getConnectionTimeout());
        hikariConfig.setIdleTimeout(config.getIdleTimeout());

        // Валидация соединений
        hikariConfig.setValidationTimeout(config.getValidationTimeout());
        if (config.getConnectionTestQuery() != null) {
            hikariConfig.setConnectionTestQuery(config.getConnectionTestQuery());
        }

        // Read-only
        hikariConfig.setReadOnly(config.isReadOnly());

        // Auto-commit
        hikariConfig.setAutoCommit(config.isAutoCommit());

        // Регистрация MBeans для метрик
        hikariConfig.setRegisterMbeans(true);

        logger.debug("Создан HikariConfig для '{}': url={}, poolSize={}",
            name, config.getUrl(), config.getPoolSize());

        return hikariConfig;
    }

    /**
     * Валидирует имя DataSource.
     */
    private void validateDataSourceName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ConnectionException("DataSource name cannot be null or empty");
        }
    }

    /**
     * Метрики пула соединений.
     */
    public record PoolMetrics(
        String dataSourceName,
        int activeConnections,
        int idleConnections,
        int totalConnections,
        int pendingThreads
    ) {
        @Override
        public String toString() {
            return String.format(
                "PoolMetrics[%s: active=%d, idle=%d, total=%d, pending=%d]",
                dataSourceName, activeConnections, idleConnections,
                totalConnections, pendingThreads);
        }
    }
}
