package com.company.hex.db.connection;

import com.company.hex.db.config.DbConfig;

/**
 * Конфигурация DataSource для HikariCP.
 *
 * <p>Содержит все параметры, необходимые для создания пула соединений.
 * Может быть создан программно через Builder или из DbConfig.</p>
 *
 * <h2>Использование через Builder</h2>
 * <pre>{@code
 * DataSourceConfig config = DataSourceConfig.builder()
 *     .url("jdbc:postgresql://localhost:5432/testdb")
 *     .username("postgres")
 *     .password("secret")
 *     .poolSize(10)
 *     .build();
 * }</pre>
 *
 * <h2>Создание из DbConfig</h2>
 * <pre>{@code
 * DbConfig dbConfig = HexConfigFactory.getConfig(DbConfig.class);
 * DataSourceConfig config = DataSourceConfig.fromDbConfig("primary", dbConfig);
 * }</pre>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see ConnectionProvider
 */
public final class DataSourceConfig {

    // ==================== Основные настройки ====================

    private final String url;
    private final String username;
    private final String password;
    private final String driverClassName;

    // ==================== Pool settings ====================

    private final int poolSize;
    private final int minIdle;
    private final long maxLifetime;
    private final long connectionTimeout;
    private final long idleTimeout;
    private final long validationTimeout;
    private final String connectionTestQuery;

    // ==================== Поведение ====================

    private final boolean readOnly;
    private final boolean autoCommit;

    /**
     * Приватный конструктор для Builder.
     */
    private DataSourceConfig(Builder builder) {
        this.url = builder.url;
        this.username = builder.username;
        this.password = builder.password;
        this.driverClassName = builder.driverClassName;
        this.poolSize = builder.poolSize;
        this.minIdle = builder.minIdle;
        this.maxLifetime = builder.maxLifetime;
        this.connectionTimeout = builder.connectionTimeout;
        this.idleTimeout = builder.idleTimeout;
        this.validationTimeout = builder.validationTimeout;
        this.connectionTestQuery = builder.connectionTestQuery;
        this.readOnly = builder.readOnly;
        this.autoCommit = builder.autoCommit;
    }

    /**
     * Создаёт Builder для DataSourceConfig.
     *
     * @return новый Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Создаёт DataSourceConfig из DbConfig для указанного DataSource.
     *
     * <p>Поддерживает "primary" DataSource из стандартных полей DbConfig.
     * Для других DataSource можно расширить логику.</p>
     *
     * @param name имя DataSource (например, "primary")
     * @param dbConfig конфигурация из hex.properties
     * @return DataSourceConfig
     */
    public static DataSourceConfig fromDbConfig(String name, DbConfig dbConfig) {
        if (dbConfig == null) {
            throw new IllegalArgumentException("DbConfig cannot be null");
        }

        // Для "primary" используем стандартные методы DbConfig
        if ("primary".equals(name)) {
            return builder()
                .url(dbConfig.getPrimaryUrl())
                .username(dbConfig.getPrimaryUsername())
                .password(dbConfig.getPrimaryPassword())
                .driverClassName(dbConfig.getPrimaryDriver())
                .poolSize(dbConfig.getPoolSize())
                .minIdle(dbConfig.getPoolMinIdle())
                .maxLifetime(dbConfig.getPoolMaxLifetime())
                .connectionTimeout(dbConfig.getPoolConnectionTimeout())
                .build();
        }

        // Для других DataSource возвращаем конфигурацию по умолчанию
        // (можно расширить для поддержки множественных DS через Owner)
        return builder()
            .url(dbConfig.getPrimaryUrl())
            .username(dbConfig.getPrimaryUsername())
            .password(dbConfig.getPrimaryPassword())
            .driverClassName(dbConfig.getPrimaryDriver())
            .poolSize(dbConfig.getPoolSize())
            .minIdle(dbConfig.getPoolMinIdle())
            .maxLifetime(dbConfig.getPoolMaxLifetime())
            .connectionTimeout(dbConfig.getPoolConnectionTimeout())
            .build();
    }

    /**
     * Создаёт конфигурацию для H2 in-memory базы данных (для тестов).
     *
     * @param dbName имя базы данных (уникальное для изоляции)
     * @return DataSourceConfig для H2
     */
    public static DataSourceConfig h2InMemory(String dbName) {
        return builder()
            .url("jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1;MODE=PostgreSQL")
            .username("sa")
            .password("")
            .driverClassName("org.h2.Driver")
            .poolSize(5)
            .minIdle(1)
            .autoCommit(true)
            .build();
    }

    /**
     * Создаёт конфигурацию для H2 in-memory с настройками по умолчанию.
     *
     * @return DataSourceConfig для H2
     */
    public static DataSourceConfig h2InMemoryDefault() {
        return h2InMemory("testdb");
    }

    // ==================== Getters ====================

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public long getMaxLifetime() {
        return maxLifetime;
    }

    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    public long getIdleTimeout() {
        return idleTimeout;
    }

    public long getValidationTimeout() {
        return validationTimeout;
    }

    public String getConnectionTestQuery() {
        return connectionTestQuery;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }

    @Override
    public String toString() {
        return "DataSourceConfig{"
            + "url='" + url + '\''
            + ", username='" + username + '\''
            + ", poolSize=" + poolSize
            + ", minIdle=" + minIdle
            + ", readOnly=" + readOnly
            + '}';
    }

    /**
     * Builder для DataSourceConfig.
     */
    public static final class Builder {

        private String url = "jdbc:h2:mem:testdb";
        private String username = "sa";
        private String password = "";
        private String driverClassName;

        private int poolSize = 10;
        private int minIdle = 2;
        private long maxLifetime = 1800000; // 30 минут
        private long connectionTimeout = 30000; // 30 секунд
        private long idleTimeout = 600000; // 10 минут
        private long validationTimeout = 5000; // 5 секунд
        private String connectionTestQuery;

        private boolean readOnly = false;
        private boolean autoCommit = true;

        private Builder() {
        }

        /**
         * Устанавливает JDBC URL.
         *
         * @param url JDBC URL
         * @return this
         */
        public Builder url(String url) {
            this.url = url;
            return this;
        }

        /**
         * Устанавливает имя пользователя.
         *
         * @param username имя пользователя
         * @return this
         */
        public Builder username(String username) {
            this.username = username;
            return this;
        }

        /**
         * Устанавливает пароль.
         *
         * @param password пароль
         * @return this
         */
        public Builder password(String password) {
            this.password = password;
            return this;
        }

        /**
         * Устанавливает класс JDBC драйвера.
         *
         * @param driverClassName полное имя класса драйвера
         * @return this
         */
        public Builder driverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
            return this;
        }

        /**
         * Устанавливает максимальный размер пула.
         *
         * @param poolSize размер пула
         * @return this
         */
        public Builder poolSize(int poolSize) {
            this.poolSize = poolSize;
            return this;
        }

        /**
         * Устанавливает минимальное количество idle соединений.
         *
         * @param minIdle минимум idle соединений
         * @return this
         */
        public Builder minIdle(int minIdle) {
            this.minIdle = minIdle;
            return this;
        }

        /**
         * Устанавливает максимальное время жизни соединения.
         *
         * @param maxLifetime время в миллисекундах
         * @return this
         */
        public Builder maxLifetime(long maxLifetime) {
            this.maxLifetime = maxLifetime;
            return this;
        }

        /**
         * Устанавливает таймаут получения соединения.
         *
         * @param connectionTimeout таймаут в миллисекундах
         * @return this
         */
        public Builder connectionTimeout(long connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        /**
         * Устанавливает таймаут idle соединения.
         *
         * @param idleTimeout таймаут в миллисекундах
         * @return this
         */
        public Builder idleTimeout(long idleTimeout) {
            this.idleTimeout = idleTimeout;
            return this;
        }

        /**
         * Устанавливает таймаут валидации соединения.
         *
         * @param validationTimeout таймаут в миллисекундах
         * @return this
         */
        public Builder validationTimeout(long validationTimeout) {
            this.validationTimeout = validationTimeout;
            return this;
        }

        /**
         * Устанавливает SQL запрос для проверки соединения.
         *
         * @param connectionTestQuery SQL запрос
         * @return this
         */
        public Builder connectionTestQuery(String connectionTestQuery) {
            this.connectionTestQuery = connectionTestQuery;
            return this;
        }

        /**
         * Устанавливает режим только для чтения.
         *
         * @param readOnly true для read-only
         * @return this
         */
        public Builder readOnly(boolean readOnly) {
            this.readOnly = readOnly;
            return this;
        }

        /**
         * Устанавливает режим auto-commit.
         *
         * @param autoCommit true для auto-commit
         * @return this
         */
        public Builder autoCommit(boolean autoCommit) {
            this.autoCommit = autoCommit;
            return this;
        }

        /**
         * Создаёт DataSourceConfig.
         *
         * @return новый DataSourceConfig
         * @throws IllegalStateException если URL не задан
         */
        public DataSourceConfig build() {
            if (url == null || url.trim().isEmpty()) {
                throw new IllegalStateException("JDBC URL is required");
            }
            return new DataSourceConfig(this);
        }
    }
}
