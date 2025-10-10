package com.company.hex.core.logging;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * Менеджер для управления correlation ID в контексте логирования.
 * Использует SLF4J MDC (Mapped Diagnostic Context) для корреляции логов в рамках одного теста.
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public class CorrelationIdManager {
    
    /**
     * Ключ для correlation ID в MDC контексте.
     */
    public static final String CORRELATION_ID_KEY = "correlationId";
    
    /**
     * Ключ для имени теста в MDC контексте.
     */
    public static final String TEST_NAME_KEY = "testName";
    
    /**
     * Ключ для класса теста в MDC контексте.
     */
    public static final String TEST_CLASS_KEY = "testClass";
    
    /**
     * Приватный конструктор для предотвращения создания экземпляров.
     */
    private CorrelationIdManager() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Генерирует и устанавливает новый correlation ID для текущего потока.
     * 
     * @return сгенерированный correlation ID
     */
    public static String generateAndSetCorrelationId() {
        String correlationId = generateCorrelationId();
        setCorrelationId(correlationId);
        return correlationId;
    }
    
    /**
     * Генерирует новый correlation ID.
     * 
     * @return сгенерированный correlation ID
     */
    public static String generateCorrelationId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
    
    /**
     * Устанавливает correlation ID для текущего потока.
     * 
     * @param correlationId correlation ID для установки
     */
    public static void setCorrelationId(String correlationId) {
        if (correlationId != null && !correlationId.trim().isEmpty()) {
            MDC.put(CORRELATION_ID_KEY, correlationId.trim());
        }
    }
    
    /**
     * Получает текущий correlation ID из MDC контекста.
     * 
     * @return текущий correlation ID или null, если не установлен
     */
    public static String getCorrelationId() {
        return MDC.get(CORRELATION_ID_KEY);
    }
    
    /**
     * Устанавливает информацию о тесте в MDC контекст.
     * 
     * @param testClass класс теста
     * @param testName имя теста
     */
    public static void setTestContext(String testClass, String testName) {
        if (testClass != null && !testClass.trim().isEmpty()) {
            MDC.put(TEST_CLASS_KEY, testClass.trim());
        }
        if (testName != null && !testName.trim().isEmpty()) {
            MDC.put(TEST_NAME_KEY, testName.trim());
        }
    }
    
    /**
     * Устанавливает имя теста в MDC контекст.
     * 
     * @param testName имя теста
     */
    public static void setTestName(String testName) {
        if (testName != null && !testName.trim().isEmpty()) {
            MDC.put(TEST_NAME_KEY, testName.trim());
        }
    }
    
    /**
     * Устанавливает класс теста в MDC контекст.
     * 
     * @param testClass класс теста
     */
    public static void setTestClass(String testClass) {
        if (testClass != null && !testClass.trim().isEmpty()) {
            MDC.put(TEST_CLASS_KEY, testClass.trim());
        }
    }
    
    /**
     * Получает имя теста из MDC контекста.
     * 
     * @return имя теста или null, если не установлено
     */
    public static String getTestName() {
        return MDC.get(TEST_NAME_KEY);
    }
    
    /**
     * Получает класс теста из MDC контекста.
     * 
     * @return класс теста или null, если не установлен
     */
    public static String getTestClass() {
        return MDC.get(TEST_CLASS_KEY);
    }
    
    /**
     * Очищает correlation ID из MDC контекста.
     */
    public static void clearCorrelationId() {
        MDC.remove(CORRELATION_ID_KEY);
    }
    
    /**
     * Очищает информацию о тесте из MDC контекста.
     */
    public static void clearTestContext() {
        MDC.remove(TEST_NAME_KEY);
        MDC.remove(TEST_CLASS_KEY);
    }
    
    /**
     * Очищает весь MDC контекст для текущего потока.
     */
    public static void clearAll() {
        MDC.clear();
    }
    
    /**
     * Инициализирует полный контекст для теста.
     * Генерирует correlation ID и устанавливает информацию о тесте.
     * 
     * @param testClass класс теста
     * @param testName имя теста
     * @return сгенерированный correlation ID
     */
    public static String initializeTestContext(String testClass, String testName) {
        String correlationId = generateAndSetCorrelationId();
        setTestContext(testClass, testName);
        return correlationId;
    }
    
    /**
     * Инициализирует полный контекст для теста с кастомным correlation ID.
     * 
     * @param correlationId кастомный correlation ID
     * @param testClass класс теста
     * @param testName имя теста
     */
    public static void initializeTestContext(String correlationId, String testClass, String testName) {
        setCorrelationId(correlationId);
        setTestContext(testClass, testName);
    }
}