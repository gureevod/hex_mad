package com.company.hex.core.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Фабрика для создания логгеров с консистентной конфигурацией в рамках Hex Framework.
 * Предоставляет централизованный способ создания логгеров с автоматической настройкой.
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public class HexLoggerFactory {
    
    /**
     * Приватный конструктор для предотвращения создания экземпляров.
     */
    private HexLoggerFactory() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Создает логгер для указанного класса.
     * 
     * @param clazz класс, для которого создается логгер
     * @return настроенный логгер
     */
    public static Logger getLogger(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Class cannot be null");
        }
        return LoggerFactory.getLogger(clazz);
    }
    
    /**
     * Создает логгер с указанным именем.
     * 
     * @param name имя логгера
     * @return настроенный логгер
     */
    public static Logger getLogger(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Logger name cannot be null or empty");
        }
        return LoggerFactory.getLogger(name.trim());
    }
    
    /**
     * Создает логгер для текущего класса (определяется автоматически).
     * Использует stack trace для определения вызывающего класса.
     * 
     * @return настроенный логгер для вызывающего класса
     */
    public static Logger getLogger() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        
        // Ищем первый элемент stack trace, который не является этим классом
        for (int i = 2; i < stackTrace.length; i++) {
            String className = stackTrace[i].getClassName();
            if (!className.equals(HexLoggerFactory.class.getName())) {
                return LoggerFactory.getLogger(className);
            }
        }
        
        // Fallback: если не удалось определить класс, используем имя по умолчанию
        return LoggerFactory.getLogger("com.company.hex.unknown");
    }
    
    /**
     * Создает логгер для API операций с префиксом.
     * 
     * @param clazz класс, для которого создается логгер
     * @return настроенный API логгер
     */
    public static Logger getApiLogger(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Class cannot be null");
        }
        return LoggerFactory.getLogger("API." + clazz.getName());
    }
    
    /**
     * Создает логгер для UI операций с префиксом.
     * 
     * @param clazz класс, для которого создается логгер
     * @return настроенный UI логгер
     */
    public static Logger getUiLogger(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Class cannot be null");
        }
        return LoggerFactory.getLogger("UI." + clazz.getName());
    }
    
    /**
     * Создает логгер для тестов с префиксом.
     * 
     * @param clazz класс теста, для которого создается логгер
     * @return настроенный тестовый логгер
     */
    public static Logger getTestLogger(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Class cannot be null");
        }
        return LoggerFactory.getLogger("TEST." + clazz.getName());
    }
    
    /**
     * Создает логгер для конфигурации с префиксом.
     * 
     * @param clazz класс конфигурации, для которого создается логгер
     * @return настроенный конфигурационный логгер
     */
    public static Logger getConfigLogger(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Class cannot be null");
        }
        return LoggerFactory.getLogger("CONFIG." + clazz.getName());
    }
    
    /**
     * Создает логгер для утилит с префиксом.
     * 
     * @param clazz класс утилиты, для которого создается логгер
     * @return настроенный утилитарный логгер
     */
    public static Logger getUtilLogger(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Class cannot be null");
        }
        return LoggerFactory.getLogger("UTIL." + clazz.getName());
    }
    
    /**
     * Создает логгер для производительности с префиксом.
     * Используется для логирования метрик производительности.
     * 
     * @param clazz класс, для которого создается логгер
     * @return настроенный логгер производительности
     */
    public static Logger getPerformanceLogger(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Class cannot be null");
        }
        return LoggerFactory.getLogger("PERF." + clazz.getName());
    }
    
    /**
     * Создает логгер для безопасности с префиксом.
     * Используется для логирования событий безопасности.
     * 
     * @param clazz класс, для которого создается логгер
     * @return настроенный логгер безопасности
     */
    public static Logger getSecurityLogger(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Class cannot be null");
        }
        return LoggerFactory.getLogger("SECURITY." + clazz.getName());
    }
}