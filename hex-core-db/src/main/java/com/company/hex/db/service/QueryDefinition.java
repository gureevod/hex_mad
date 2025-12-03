package com.company.hex.db.service;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * DTO содержащий метаданные запроса к базе данных.
 *
 * <p>Объект создаётся {@link QueryProcessor} при парсинге аннотаций метода
 * репозитория и содержит всю необходимую информацию для выполнения запроса:</p>
 * <ul>
 *     <li>SQL запрос с именованными параметрами</li>
 *     <li>Значения параметров</li>
 *     <li>Тип возвращаемого значения</li>
 *     <li>Метаданные операции (timeout, опасность и т.д.)</li>
 * </ul>
 *
 * <h2>Пример создания</h2>
 * <pre>{@code
 * QueryDefinition query = QueryDefinition.builder()
 *     .sql("SELECT * FROM users WHERE id = :id")
 *     .parameter("id", 42L)
 *     .returnType(UserEntity.class)
 *     .methodName("findById")
 *     .queryType(QueryType.SELECT)
 *     .build();
 * }</pre>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see QueryProcessor
 */
public final class QueryDefinition {

    /**
     * Типы SQL запросов.
     */
    public enum QueryType {
        /**
         * SELECT запрос для получения данных.
         */
        SELECT,

        /**
         * INSERT запрос для создания записей.
         */
        INSERT,

        /**
         * UPDATE запрос для обновления записей.
         */
        UPDATE,

        /**
         * DELETE запрос для удаления записей.
         */
        DELETE,

        /**
         * Вызов хранимой процедуры.
         */
        CALL,

        /**
         * Выполнение SQL скрипта.
         */
        SCRIPT
    }

    private final String sql;
    private final Map<String, Object> parameters;
    private final Set<String> declaredParameters;
    private final Type returnType;
    private final Class<?> returnClass;
    private final String methodName;
    private final Method method;
    private final QueryType queryType;
    private final boolean returnGeneratedKeys;
    private final boolean dangerousAllowed;
    private final String dangerousReason;
    private final Integer timeout;
    private final boolean readOnly;
    private final boolean transactional;
    private final Class<?> rowMapperClass;
    private final String dataSource;

    private QueryDefinition(Builder builder) {
        this.sql = builder.sql;
        this.parameters = Collections.unmodifiableMap(new LinkedHashMap<>(builder.parameters));
        this.declaredParameters = Collections.unmodifiableSet(builder.declaredParameters);
        this.returnType = builder.returnType;
        this.returnClass = builder.returnClass;
        this.methodName = builder.methodName;
        this.method = builder.method;
        this.queryType = builder.queryType;
        this.returnGeneratedKeys = builder.returnGeneratedKeys;
        this.dangerousAllowed = builder.dangerousAllowed;
        this.dangerousReason = builder.dangerousReason;
        this.timeout = builder.timeout;
        this.readOnly = builder.readOnly;
        this.transactional = builder.transactional;
        this.rowMapperClass = builder.rowMapperClass;
        this.dataSource = builder.dataSource;
    }

    /**
     * Создаёт новый builder для QueryDefinition.
     *
     * @return новый экземпляр Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * SQL запрос с именованными параметрами.
     *
     * @return SQL запрос
     */
    public String getSql() {
        return sql;
    }

    /**
     * Карта параметров запроса (имя → значение).
     *
     * @return неизменяемая карта параметров
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * Набор объявленных параметров из @Param аннотаций.
     *
     * @return набор имён объявленных параметров
     */
    public Set<String> getDeclaredParameters() {
        return declaredParameters;
    }

    /**
     * Generic тип возвращаемого значения метода.
     *
     * @return тип возвращаемого значения
     */
    public Type getReturnType() {
        return returnType;
    }

    /**
     * Класс возвращаемого значения (без generic параметров).
     *
     * @return класс возвращаемого значения
     */
    public Class<?> getReturnClass() {
        return returnClass;
    }

    /**
     * Имя метода репозитория.
     *
     * @return имя метода
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Ссылка на метод репозитория.
     *
     * @return метод
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Тип SQL запроса.
     *
     * @return тип запроса
     */
    public QueryType getQueryType() {
        return queryType;
    }

    /**
     * Нужно ли возвращать сгенерированные ключи.
     *
     * @return true если нужны generated keys
     */
    public boolean isReturnGeneratedKeys() {
        return returnGeneratedKeys;
    }

    /**
     * Проверяет, является ли запрос модифицирующим (INSERT/UPDATE/DELETE).
     *
     * @return true если запрос модифицирует данные
     */
    public boolean isModifying() {
        return queryType == QueryType.INSERT
            || queryType == QueryType.UPDATE
            || queryType == QueryType.DELETE;
    }

    /**
     * Разрешена ли опасная операция через @DangerousQuery.
     *
     * @return true если опасная операция разрешена
     */
    public boolean isDangerousAllowed() {
        return dangerousAllowed;
    }

    /**
     * Причина пометки операции как опасной.
     *
     * @return причина или null
     */
    public String getDangerousReason() {
        return dangerousReason;
    }

    /**
     * Таймаут выполнения запроса в секундах.
     *
     * @return Optional с таймаутом или empty
     */
    public Optional<Integer> getTimeout() {
        return Optional.ofNullable(timeout);
    }

    /**
     * Является ли запрос read-only.
     *
     * @return true если запрос только на чтение
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Нужно ли выполнять запрос в транзакции.
     *
     * @return true если требуется транзакция
     */
    public boolean isTransactional() {
        return transactional;
    }

    /**
     * Класс кастомного RowMapper для маппинга результатов.
     *
     * @return Optional с классом маппера или empty
     */
    public Optional<Class<?>> getRowMapperClass() {
        return Optional.ofNullable(rowMapperClass);
    }

    /**
     * Имя DataSource для выполнения запроса.
     *
     * @return имя DataSource
     */
    public String getDataSource() {
        return dataSource;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QueryDefinition that = (QueryDefinition) o;
        return returnGeneratedKeys == that.returnGeneratedKeys
            && dangerousAllowed == that.dangerousAllowed
            && readOnly == that.readOnly
            && transactional == that.transactional
            && Objects.equals(sql, that.sql)
            && Objects.equals(parameters, that.parameters)
            && Objects.equals(methodName, that.methodName)
            && queryType == that.queryType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sql, parameters, methodName, queryType,
            returnGeneratedKeys, dangerousAllowed, readOnly, transactional);
    }

    @Override
    public String toString() {
        return "QueryDefinition{"
            + "queryType=" + queryType
            + ", methodName='" + methodName + '\''
            + ", sql='" + sql + '\''
            + ", parameters=" + parameters
            + ", returnType=" + returnType
            + '}';
    }

    /**
     * Builder для создания QueryDefinition.
     */
    public static final class Builder {

        private String sql;
        private final Map<String, Object> parameters = new LinkedHashMap<>();
        private Set<String> declaredParameters = Collections.emptySet();
        private Type returnType;
        private Class<?> returnClass;
        private String methodName;
        private Method method;
        private QueryType queryType = QueryType.SELECT;
        private boolean returnGeneratedKeys;
        private boolean dangerousAllowed;
        private String dangerousReason;
        private Integer timeout;
        private boolean readOnly;
        private boolean transactional;
        private Class<?> rowMapperClass;
        private String dataSource = "primary";

        private Builder() {
        }

        /**
         * Устанавливает SQL запрос.
         *
         * @param sql SQL запрос
         * @return этот builder
         */
        public Builder sql(String sql) {
            this.sql = sql;
            return this;
        }

        /**
         * Добавляет параметр запроса.
         *
         * @param name имя параметра
         * @param value значение параметра
         * @return этот builder
         */
        public Builder parameter(String name, Object value) {
            this.parameters.put(name, value);
            return this;
        }

        /**
         * Устанавливает все параметры запроса.
         *
         * @param parameters карта параметров
         * @return этот builder
         */
        public Builder parameters(Map<String, Object> parameters) {
            this.parameters.clear();
            if (parameters != null) {
                this.parameters.putAll(parameters);
            }
            return this;
        }

        /**
         * Устанавливает набор объявленных параметров.
         *
         * @param declaredParameters набор имён параметров
         * @return этот builder
         */
        public Builder declaredParameters(Set<String> declaredParameters) {
            this.declaredParameters = declaredParameters != null
                ? declaredParameters
                : Collections.emptySet();
            return this;
        }

        /**
         * Устанавливает тип возвращаемого значения.
         *
         * @param returnType generic тип
         * @return этот builder
         */
        public Builder returnType(Type returnType) {
            this.returnType = returnType;
            return this;
        }

        /**
         * Устанавливает класс возвращаемого значения.
         *
         * @param returnClass класс возвращаемого значения
         * @return этот builder
         */
        public Builder returnClass(Class<?> returnClass) {
            this.returnClass = returnClass;
            return this;
        }

        /**
         * Устанавливает имя метода.
         *
         * @param methodName имя метода
         * @return этот builder
         */
        public Builder methodName(String methodName) {
            this.methodName = methodName;
            return this;
        }

        /**
         * Устанавливает ссылку на метод.
         *
         * @param method метод
         * @return этот builder
         */
        public Builder method(Method method) {
            this.method = method;
            return this;
        }

        /**
         * Устанавливает тип запроса.
         *
         * @param queryType тип запроса
         * @return этот builder
         */
        public Builder queryType(QueryType queryType) {
            this.queryType = queryType;
            return this;
        }

        /**
         * Устанавливает флаг возврата сгенерированных ключей.
         *
         * @param returnGeneratedKeys true для возврата ключей
         * @return этот builder
         */
        public Builder returnGeneratedKeys(boolean returnGeneratedKeys) {
            this.returnGeneratedKeys = returnGeneratedKeys;
            return this;
        }

        /**
         * Устанавливает флаг разрешённой опасной операции.
         *
         * @param dangerousAllowed true если опасная операция разрешена
         * @return этот builder
         */
        public Builder dangerousAllowed(boolean dangerousAllowed) {
            this.dangerousAllowed = dangerousAllowed;
            return this;
        }

        /**
         * Устанавливает причину пометки операции как опасной.
         *
         * @param dangerousReason причина
         * @return этот builder
         */
        public Builder dangerousReason(String dangerousReason) {
            this.dangerousReason = dangerousReason;
            return this;
        }

        /**
         * Устанавливает таймаут выполнения.
         *
         * @param timeout таймаут в секундах
         * @return этот builder
         */
        public Builder timeout(Integer timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Устанавливает флаг read-only.
         *
         * @param readOnly true если запрос только на чтение
         * @return этот builder
         */
        public Builder readOnly(boolean readOnly) {
            this.readOnly = readOnly;
            return this;
        }

        /**
         * Устанавливает флаг транзакционности.
         *
         * @param transactional true если требуется транзакция
         * @return этот builder
         */
        public Builder transactional(boolean transactional) {
            this.transactional = transactional;
            return this;
        }

        /**
         * Устанавливает класс кастомного RowMapper.
         *
         * @param rowMapperClass класс маппера
         * @return этот builder
         */
        public Builder rowMapperClass(Class<?> rowMapperClass) {
            this.rowMapperClass = rowMapperClass;
            return this;
        }

        /**
         * Устанавливает имя DataSource.
         *
         * @param dataSource имя DataSource
         * @return этот builder
         */
        public Builder dataSource(String dataSource) {
            this.dataSource = dataSource != null ? dataSource : "primary";
            return this;
        }

        /**
         * Создаёт QueryDefinition из настроек builder.
         *
         * @return новый QueryDefinition
         * @throws IllegalStateException если обязательные поля не заполнены
         */
        public QueryDefinition build() {
            validate();
            return new QueryDefinition(this);
        }

        private void validate() {
            if (sql == null || sql.isBlank()) {
                throw new IllegalStateException("SQL query cannot be null or blank");
            }
            if (methodName == null || methodName.isBlank()) {
                throw new IllegalStateException("Method name cannot be null or blank");
            }
            if (queryType == null) {
                throw new IllegalStateException("Query type cannot be null");
            }
        }
    }
}
