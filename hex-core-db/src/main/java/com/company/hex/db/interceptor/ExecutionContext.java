package com.company.hex.db.interceptor;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Контекст выполнения запроса.
 *
 * <p>Содержит информацию о текущем соединении, транзакции,
 * таймаутах и других параметрах выполнения. Изолирован для
 * каждого потока через ThreadLocal.</p>
 *
 * <h2>Использование</h2>
 * <pre>{@code
 * ExecutionContext ctx = chain.context();
 * Connection conn = ctx.getConnection();
 * int timeout = ctx.getQueryTimeout();
 *
 * // Установка кастомных атрибутов
 * ctx.setAttribute("startTime", System.currentTimeMillis());
 * Long startTime = ctx.getAttribute("startTime", Long.class);
 * }</pre>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see DbInterceptorChain
 */
public class ExecutionContext {

    private static final ThreadLocal<ExecutionContext> CURRENT = new ThreadLocal<>();

    private Connection connection;
    private boolean transactionActive;
    private int queryTimeout = 30;
    private int fetchSize = 0;
    private boolean readOnly;
    private String dataSourceName = "primary";
    private final Map<String, Object> attributes = new HashMap<>();

    /**
     * Создаёт новый контекст выполнения.
     */
    public ExecutionContext() {
    }

    /**
     * Возвращает текущий контекст для данного потока.
     *
     * @return текущий контекст или null
     */
    public static ExecutionContext current() {
        return CURRENT.get();
    }

    /**
     * Устанавливает текущий контекст для данного потока.
     *
     * @param context контекст для установки
     */
    public static void setCurrent(ExecutionContext context) {
        CURRENT.set(context);
    }

    /**
     * Очищает текущий контекст для данного потока.
     */
    public static void clear() {
        CURRENT.remove();
    }

    /**
     * Возвращает текущее соединение с БД.
     *
     * @return соединение или null
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Устанавливает текущее соединение.
     *
     * @param connection соединение с БД
     */
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * Проверяет, активна ли транзакция.
     *
     * @return true если транзакция активна
     */
    public boolean isTransactionActive() {
        return transactionActive;
    }

    /**
     * Устанавливает флаг активной транзакции.
     *
     * @param transactionActive true если транзакция активна
     */
    public void setTransactionActive(boolean transactionActive) {
        this.transactionActive = transactionActive;
    }

    /**
     * Возвращает таймаут запроса в секундах.
     *
     * @return таймаут в секундах
     */
    public int getQueryTimeout() {
        return queryTimeout;
    }

    /**
     * Устанавливает таймаут запроса.
     *
     * @param queryTimeout таймаут в секундах
     */
    public void setQueryTimeout(int queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    /**
     * Возвращает размер выборки для ResultSet.
     *
     * @return fetch size
     */
    public int getFetchSize() {
        return fetchSize;
    }

    /**
     * Устанавливает размер выборки.
     *
     * @param fetchSize размер выборки
     */
    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }

    /**
     * Проверяет, является ли контекст read-only.
     *
     * @return true если read-only
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Устанавливает флаг read-only.
     *
     * @param readOnly true для read-only
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Возвращает имя DataSource.
     *
     * @return имя DataSource
     */
    public String getDataSourceName() {
        return dataSourceName;
    }

    /**
     * Устанавливает имя DataSource.
     *
     * @param dataSourceName имя DataSource
     */
    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    /**
     * Устанавливает кастомный атрибут.
     *
     * @param name имя атрибута
     * @param value значение атрибута
     */
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    /**
     * Возвращает значение кастомного атрибута.
     *
     * @param name имя атрибута
     * @return значение или null
     */
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    /**
     * Возвращает значение кастомного атрибута с приведением типа.
     *
     * @param <T> тип значения
     * @param name имя атрибута
     * @param type класс типа
     * @return значение или null
     */
    public <T> T getAttribute(String name, Class<T> type) {
        Object value = attributes.get(name);
        if (value == null) {
            return null;
        }
        return type.cast(value);
    }

    /**
     * Возвращает Optional с кастомным атрибутом.
     *
     * @param <T> тип значения
     * @param name имя атрибута
     * @param type класс типа
     * @return Optional с значением
     */
    public <T> Optional<T> getOptionalAttribute(String name, Class<T> type) {
        return Optional.ofNullable(getAttribute(name, type));
    }

    /**
     * Проверяет наличие атрибута.
     *
     * @param name имя атрибута
     * @return true если атрибут существует
     */
    public boolean hasAttribute(String name) {
        return attributes.containsKey(name);
    }

    /**
     * Удаляет атрибут.
     *
     * @param name имя атрибута
     * @return удалённое значение или null
     */
    public Object removeAttribute(String name) {
        return attributes.remove(name);
    }
}
