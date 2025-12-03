package com.company.hex.db.exception;

/**
 * Исключение для ошибок, связанных с соединениями к базе данных.
 *
 * <p>Возникает в следующих ситуациях:</p>
 * <ul>
 *     <li>Не удалось получить соединение из пула</li>
 *     <li>DataSource не найден или не зарегистрирован</li>
 *     <li>Ошибка конфигурации пула соединений</li>
 *     <li>Таймаут при ожидании соединения</li>
 *     <li>Соединение неожиданно закрыто</li>
 * </ul>
 *
 * <h2>Пример обработки</h2>
 * <pre>{@code
 * try {
 *     Connection conn = connectionProvider.getConnection("primary");
 * } catch (ConnectionException e) {
 *     log.error("Не удалось получить соединение: {}", e.getMessage());
 *     // Возможные действия:
 *     // - Повторная попытка
 *     // - Fallback на другой DataSource
 *     // - Уведомление пользователя
 * }
 * }</pre>
 *
 * <h2>Рекомендации по решению</h2>
 * <ul>
 *     <li>Проверьте корректность JDBC URL</li>
 *     <li>Убедитесь, что БД доступна</li>
 *     <li>Проверьте credentials</li>
 *     <li>Проверьте настройки пула (размер, таймауты)</li>
 *     <li>Убедитесь, что DataSource зарегистрирован</li>
 * </ul>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see com.company.hex.db.connection.ConnectionProvider
 */
public class ConnectionException extends DbException {

    private static final long serialVersionUID = 1L;

    /**
     * Имя DataSource, связанного с ошибкой.
     */
    private final String dataSourceName;

    /**
     * Создаёт исключение с сообщением.
     *
     * @param message сообщение об ошибке
     */
    public ConnectionException(String message) {
        super(message);
        this.dataSourceName = null;
    }

    /**
     * Создаёт исключение с сообщением и причиной.
     *
     * @param message сообщение об ошибке
     * @param cause причина ошибки
     */
    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
        this.dataSourceName = null;
    }

    /**
     * Создаёт исключение с указанием DataSource.
     *
     * @param message сообщение об ошибке
     * @param dataSourceName имя DataSource
     */
    public ConnectionException(String message, String dataSourceName) {
        super(formatMessage(message, dataSourceName));
        this.dataSourceName = dataSourceName;
    }

    /**
     * Создаёт исключение с указанием DataSource и причиной.
     *
     * @param message сообщение об ошибке
     * @param dataSourceName имя DataSource
     * @param cause причина ошибки
     */
    public ConnectionException(String message, String dataSourceName, Throwable cause) {
        super(formatMessage(message, dataSourceName), cause);
        this.dataSourceName = dataSourceName;
    }

    /**
     * Форматирует сообщение с именем DataSource.
     */
    private static String formatMessage(String message, String dataSourceName) {
        if (dataSourceName != null) {
            return message + "\nDataSource: " + dataSourceName;
        }
        return message;
    }

    /**
     * Возвращает имя DataSource, связанного с ошибкой.
     *
     * @return имя DataSource или null
     */
    public String getDataSourceName() {
        return dataSourceName;
    }

    /**
     * Создаёт исключение для случая, когда DataSource не найден.
     *
     * @param dataSourceName имя отсутствующего DataSource
     * @return ConnectionException
     */
    public static ConnectionException dataSourceNotFound(String dataSourceName) {
        return new ConnectionException(
            "DataSource not found: " + dataSourceName
                + ". Make sure it is registered with ConnectionProvider.registerDataSource()",
            dataSourceName);
    }

    /**
     * Создаёт исключение для таймаута получения соединения.
     *
     * @param dataSourceName имя DataSource
     * @param timeoutMs таймаут в миллисекундах
     * @param cause оригинальное исключение
     * @return ConnectionException
     */
    public static ConnectionException connectionTimeout(String dataSourceName,
                                                         long timeoutMs,
                                                         Throwable cause) {
        return new ConnectionException(
            String.format("Connection timeout after %dms for DataSource '%s'. "
                + "Consider increasing pool size or connection timeout.",
                timeoutMs, dataSourceName),
            dataSourceName,
            cause);
    }

    /**
     * Создаёт исключение для ошибки инициализации пула.
     *
     * @param dataSourceName имя DataSource
     * @param cause оригинальное исключение
     * @return ConnectionException
     */
    public static ConnectionException poolInitializationFailed(String dataSourceName,
                                                                Throwable cause) {
        return new ConnectionException(
            "Failed to initialize connection pool for DataSource '" + dataSourceName
                + "'. Check JDBC URL, credentials, and database availability.",
            dataSourceName,
            cause);
    }

    /**
     * Создаёт исключение для закрытого соединения.
     *
     * @param dataSourceName имя DataSource
     * @return ConnectionException
     */
    public static ConnectionException connectionClosed(String dataSourceName) {
        return new ConnectionException(
            "Connection is already closed for DataSource '" + dataSourceName + "'",
            dataSourceName);
    }
}
