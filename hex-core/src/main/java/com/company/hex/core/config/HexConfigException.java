package com.company.hex.core.config;

/**
 * Исключение, выбрасываемое при ошибках конфигурации фреймворка Hex.
 * Используется для обозначения проблем с загрузкой, валидацией или обработкой конфигурации.
 * 
 * @author Hex Framework
 * @version 1.0
 */
public class HexConfigException extends RuntimeException {

    /**
     * Создает новое исключение конфигурации с указанным сообщением.
     * 
     * @param message сообщение об ошибке
     */
    public HexConfigException(String message) {
        super(message);
    }

    /**
     * Создает новое исключение конфигурации с указанным сообщением и причиной.
     * 
     * @param message сообщение об ошибке
     * @param cause причина исключения
     */
    public HexConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Создает новое исключение конфигурации с указанной причиной.
     * 
     * @param cause причина исключения
     */
    public HexConfigException(Throwable cause) {
        super(cause);
    }
}