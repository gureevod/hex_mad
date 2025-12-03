package com.company.hex.db.connection;

import java.sql.Connection;
import java.sql.Savepoint;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Контекст соединения для транзакционного управления.
 *
 * <p>Хранит соединения, привязанные к текущему потоку в рамках транзакции.
 * Обеспечивает изоляцию между потоками для параллельного выполнения тестов.</p>
 *
 * <h2>Использование</h2>
 * <pre>{@code
 * ConnectionContext ctx = new ConnectionContext();
 *
 * // Привязка соединения к транзакции
 * ctx.bindConnection("primary", connection);
 *
 * // Проверка наличия соединения
 * if (ctx.hasConnection("primary")) {
 *     Connection conn = ctx.getConnection("primary");
 * }
 *
 * // Создание savepoint
 * ctx.setSavepoint(connection.setSavepoint("test_savepoint"));
 *
 * // Освобождение
 * ctx.unbindAll();
 * }</pre>
 *
 * <h2>Потокобезопасность</h2>
 * <p>Каждый поток получает свой экземпляр ConnectionContext через
 * ThreadLocal в {@link ConnectionProvider}. Сам ConnectionContext
 * не является потокобезопасным и не должен разделяться между потоками.</p>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see ConnectionProvider
 */
public final class ConnectionContext {

    /**
     * Соединения, привязанные к DataSource в рамках транзакции.
     */
    private final Map<String, Connection> boundConnections = new HashMap<>();

    /**
     * Текущий savepoint (для вложенных транзакций).
     */
    private Savepoint currentSavepoint;

    /**
     * Флаг, указывающий что транзакция помечена для отката.
     */
    private boolean rollbackOnly = false;

    /**
     * Имя активного DataSource в транзакции.
     */
    private String activeDataSource;

    /**
     * Глубина вложенности транзакций.
     */
    private int transactionDepth = 0;

    /**
     * Кастомные атрибуты контекста.
     */
    private final Map<String, Object> attributes = new HashMap<>();

    /**
     * Создаёт новый контекст соединения.
     */
    public ConnectionContext() {
    }

    /**
     * Привязывает соединение к указанному DataSource.
     *
     * @param dataSourceName имя DataSource
     * @param connection соединение
     */
    public void bindConnection(String dataSourceName, Connection connection) {
        if (dataSourceName == null || connection == null) {
            throw new IllegalArgumentException("DataSource name and connection cannot be null");
        }
        boundConnections.put(dataSourceName, connection);
        if (activeDataSource == null) {
            activeDataSource = dataSourceName;
        }
    }

    /**
     * Отвязывает соединение от указанного DataSource.
     *
     * @param dataSourceName имя DataSource
     * @return отвязанное соединение или null
     */
    public Connection unbindConnection(String dataSourceName) {
        return boundConnections.remove(dataSourceName);
    }

    /**
     * Отвязывает все соединения.
     */
    public void unbindAll() {
        boundConnections.clear();
        activeDataSource = null;
    }

    /**
     * Проверяет, есть ли привязанное соединение.
     *
     * @param dataSourceName имя DataSource
     * @return true если соединение привязано
     */
    public boolean hasConnection(String dataSourceName) {
        return boundConnections.containsKey(dataSourceName);
    }

    /**
     * Возвращает привязанное соединение.
     *
     * @param dataSourceName имя DataSource
     * @return соединение или null
     */
    public Connection getConnection(String dataSourceName) {
        return boundConnections.get(dataSourceName);
    }

    /**
     * Возвращает Optional с соединением.
     *
     * @param dataSourceName имя DataSource
     * @return Optional с соединением
     */
    public Optional<Connection> getOptionalConnection(String dataSourceName) {
        return Optional.ofNullable(boundConnections.get(dataSourceName));
    }

    /**
     * Проверяет, является ли соединение транзакционным.
     *
     * @param connection соединение для проверки
     * @return true если соединение привязано к транзакции
     */
    public boolean isTransactionalConnection(Connection connection) {
        return boundConnections.containsValue(connection);
    }

    /**
     * Возвращает количество привязанных соединений.
     *
     * @return количество соединений
     */
    public int getBoundConnectionCount() {
        return boundConnections.size();
    }

    /**
     * Проверяет, есть ли хотя бы одно привязанное соединение.
     *
     * @return true если есть привязанные соединения
     */
    public boolean hasBoundConnections() {
        return !boundConnections.isEmpty();
    }

    // ==================== Savepoint Management ====================

    /**
     * Устанавливает текущий savepoint.
     *
     * @param savepoint savepoint
     */
    public void setSavepoint(Savepoint savepoint) {
        this.currentSavepoint = savepoint;
    }

    /**
     * Возвращает текущий savepoint.
     *
     * @return savepoint или null
     */
    public Savepoint getSavepoint() {
        return currentSavepoint;
    }

    /**
     * Возвращает Optional с savepoint.
     *
     * @return Optional с savepoint
     */
    public Optional<Savepoint> getOptionalSavepoint() {
        return Optional.ofNullable(currentSavepoint);
    }

    /**
     * Проверяет наличие savepoint.
     *
     * @return true если savepoint установлен
     */
    public boolean hasSavepoint() {
        return currentSavepoint != null;
    }

    /**
     * Очищает savepoint.
     */
    public void clearSavepoint() {
        this.currentSavepoint = null;
    }

    // ==================== Transaction State ====================

    /**
     * Помечает транзакцию для отката.
     */
    public void setRollbackOnly() {
        this.rollbackOnly = true;
    }

    /**
     * Проверяет, помечена ли транзакция для отката.
     *
     * @return true если помечена для отката
     */
    public boolean isRollbackOnly() {
        return rollbackOnly;
    }

    /**
     * Сбрасывает флаг отката.
     */
    public void clearRollbackOnly() {
        this.rollbackOnly = false;
    }

    /**
     * Возвращает активный DataSource.
     *
     * @return имя активного DataSource
     */
    public String getActiveDataSource() {
        return activeDataSource;
    }

    /**
     * Устанавливает активный DataSource.
     *
     * @param dataSourceName имя DataSource
     */
    public void setActiveDataSource(String dataSourceName) {
        this.activeDataSource = dataSourceName;
    }

    // ==================== Transaction Depth ====================

    /**
     * Увеличивает глубину вложенности транзакций.
     *
     * @return новая глубина
     */
    public int incrementDepth() {
        return ++transactionDepth;
    }

    /**
     * Уменьшает глубину вложенности транзакций.
     *
     * @return новая глубина
     */
    public int decrementDepth() {
        return --transactionDepth;
    }

    /**
     * Возвращает текущую глубину вложенности.
     *
     * @return глубина
     */
    public int getTransactionDepth() {
        return transactionDepth;
    }

    /**
     * Проверяет, является ли текущая транзакция корневой.
     *
     * @return true если глубина = 1
     */
    public boolean isRootTransaction() {
        return transactionDepth == 1;
    }

    /**
     * Проверяет, активна ли транзакция.
     *
     * @return true если глубина > 0
     */
    public boolean isTransactionActive() {
        return transactionDepth > 0;
    }

    // ==================== Custom Attributes ====================

    /**
     * Устанавливает кастомный атрибут.
     *
     * @param name имя атрибута
     * @param value значение
     */
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    /**
     * Возвращает кастомный атрибут.
     *
     * @param name имя атрибута
     * @return значение или null
     */
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    /**
     * Возвращает кастомный атрибут с приведением типа.
     *
     * @param <T> тип значения
     * @param name имя атрибута
     * @param type класс типа
     * @return значение или null
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name, Class<T> type) {
        Object value = attributes.get(name);
        if (value == null) {
            return null;
        }
        return (T) value;
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

    /**
     * Очищает все атрибуты.
     */
    public void clearAttributes() {
        attributes.clear();
    }

    // ==================== Lifecycle ====================

    /**
     * Полностью очищает контекст.
     */
    public void clear() {
        boundConnections.clear();
        currentSavepoint = null;
        rollbackOnly = false;
        activeDataSource = null;
        transactionDepth = 0;
        attributes.clear();
    }

    @Override
    public String toString() {
        return "ConnectionContext{"
            + "boundConnections=" + boundConnections.keySet()
            + ", hasSavepoint=" + (currentSavepoint != null)
            + ", rollbackOnly=" + rollbackOnly
            + ", transactionDepth=" + transactionDepth
            + '}';
    }
}
