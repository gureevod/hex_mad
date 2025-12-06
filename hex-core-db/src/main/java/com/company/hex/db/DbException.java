package com.company.hex.db;

/**
 * Исключение, выбрасываемое при ошибках работы с базой данных.
 * 
 * <p>Оборачивает все SQLException и другие низкоуровневые исключения
 * в единый тип для упрощения обработки ошибок в тестах.
 *
 * @author hex-core-db
 * @since 1.0.0
 */
public class DbException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Создать исключение с сообщением.
     *
     * @param message описание ошибки
     */
    public DbException(String message) {
        super(message);
    }

    /**
     * Создать исключение с сообщением и причиной.
     *
     * @param message описание ошибки
     * @param cause   исходное исключение
     */
    public DbException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Создать исключение с причиной.
     *
     * @param cause исходное исключение
     */
    public DbException(Throwable cause) {
        super(cause);
    }
}
