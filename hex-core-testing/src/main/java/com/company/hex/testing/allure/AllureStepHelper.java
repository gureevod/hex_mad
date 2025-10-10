package com.company.hex.testing.allure;

import com.company.hex.core.logging.HexLoggerFactory;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.slf4j.Logger;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Утилитарный класс для работы с Allure шагами и аннотациями.
 * Предоставляет удобные методы для создания шагов с автоматическим логированием.
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public class AllureStepHelper {
    
    private static final Logger logger = HexLoggerFactory.getUtilLogger(AllureStepHelper.class);
    
    /**
     * Приватный конструктор для предотвращения создания экземпляров.
     */
    private AllureStepHelper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Выполняет шаг без возвращаемого значения.
     * 
     * @param stepName название шага
     * @param action действие для выполнения
     */
    public static void step(String stepName, Runnable action) {
        logger.debug("Executing step: {}", stepName);
        
        Allure.step(stepName, () -> {
            try {
                action.run();
                logger.debug("Step completed successfully: {}", stepName);
            } catch (Exception e) {
                logger.error("Step failed: {} - Error: {}", stepName, e.getMessage(), e);
                throw e;
            }
        });
    }
    
    /**
     * Выполняет шаг с возвращаемым значением.
     * 
     * @param stepName название шага
     * @param supplier функция для выполнения
     * @param <T> тип возвращаемого значения
     * @return результат выполнения функции
     */
    public static <T> T step(String stepName, Supplier<T> supplier) {
        logger.debug("Executing step with return value: {}", stepName);
        
        return Allure.step(stepName, () -> {
            try {
                T result = supplier.get();
                logger.debug("Step completed successfully with result: {} - Result type: {}", 
                           stepName, result != null ? result.getClass().getSimpleName() : "null");
                return result;
            } catch (Exception e) {
                logger.error("Step failed: {} - Error: {}", stepName, e.getMessage(), e);
                throw e;
            }
        });
    }
    
    /**
     * Выполняет шаг с параметром.
     * 
     * @param stepName название шага
     * @param parameter параметр для передачи в действие
     * @param action действие для выполнения
     * @param <T> тип параметра
     */
    public static <T> void stepWithParameter(String stepName, T parameter, Consumer<T> action) {
        String fullStepName = String.format("%s [Parameter: %s]", stepName, parameter);
        logger.debug("Executing step with parameter: {}", fullStepName);
        
        Allure.step(fullStepName, () -> {
            try {
                action.accept(parameter);
                logger.debug("Step with parameter completed successfully: {}", fullStepName);
            } catch (Exception e) {
                logger.error("Step with parameter failed: {} - Error: {}", fullStepName, e.getMessage(), e);
                throw e;
            }
        });
    }
    
    /**
     * Выполняет шаг с параметром и возвращаемым значением.
     * 
     * @param stepName название шага
     * @param parameter параметр для передачи в функцию
     * @param function функция для выполнения
     * @param <T> тип параметра
     * @param <R> тип возвращаемого значения
     * @return результат выполнения функции
     */
    public static <T, R> R stepWithParameter(String stepName, T parameter, Function<T, R> function) {
        String fullStepName = String.format("%s [Parameter: %s]", stepName, parameter);
        logger.debug("Executing step with parameter and return value: {}", fullStepName);
        
        return Allure.step(fullStepName, () -> {
            try {
                R result = function.apply(parameter);
                logger.debug("Step with parameter completed successfully with result: {} - Result type: {}", 
                           fullStepName, result != null ? result.getClass().getSimpleName() : "null");
                return result;
            } catch (Exception e) {
                logger.error("Step with parameter failed: {} - Error: {}", fullStepName, e.getMessage(), e);
                throw e;
            }
        });
    }
    
    /**
     * Создает API шаг с автоматическим префиксом.
     * 
     * @param stepName название шага
     * @param action действие для выполнения
     */
    public static void apiStep(String stepName, Runnable action) {
        step("🌐 API: " + stepName, action);
    }
    
    /**
     * Создает API шаг с возвращаемым значением.
     * 
     * @param stepName название шага
     * @param supplier функция для выполнения
     * @param <T> тип возвращаемого значения
     * @return результат выполнения функции
     */
    public static <T> T apiStep(String stepName, Supplier<T> supplier) {
        return step("🌐 API: " + stepName, supplier);
    }
    
    /**
     * Создает UI шаг с автоматическим префиксом.
     * 
     * @param stepName название шага
     * @param action действие для выполнения
     */
    public static void uiStep(String stepName, Runnable action) {
        step("🖥️ UI: " + stepName, action);
    }
    
    /**
     * Создает UI шаг с возвращаемым значением.
     * 
     * @param stepName название шага
     * @param supplier функция для выполнения
     * @param <T> тип возвращаемого значения
     * @return результат выполнения функции
     */
    public static <T> T uiStep(String stepName, Supplier<T> supplier) {
        return step("🖥️ UI: " + stepName, supplier);
    }
    
    /**
     * Создает шаг проверки с автоматическим префиксом.
     * 
     * @param stepName название шага
     * @param action действие для выполнения
     */
    public static void verifyStep(String stepName, Runnable action) {
        step("✅ Verify: " + stepName, action);
    }
    
    /**
     * Создает шаг проверки с возвращаемым значением.
     * 
     * @param stepName название шага
     * @param supplier функция для выполнения
     * @param <T> тип возвращаемого значения
     * @return результат выполнения функции
     */
    public static <T> T verifyStep(String stepName, Supplier<T> supplier) {
        return step("✅ Verify: " + stepName, supplier);
    }
    
    /**
     * Создает шаг подготовки данных с автоматическим префиксом.
     * 
     * @param stepName название шага
     * @param action действие для выполнения
     */
    public static void setupStep(String stepName, Runnable action) {
        step("🔧 Setup: " + stepName, action);
    }
    
    /**
     * Создает шаг подготовки данных с возвращаемым значением.
     * 
     * @param stepName название шага
     * @param supplier функция для выполнения
     * @param <T> тип возвращаемого значения
     * @return результат выполнения функции
     */
    public static <T> T setupStep(String stepName, Supplier<T> supplier) {
        return step("🔧 Setup: " + stepName, supplier);
    }
    
    /**
     * Создает шаг очистки с автоматическим префиксом.
     * 
     * @param stepName название шага
     * @param action действие для выполнения
     */
    public static void cleanupStep(String stepName, Runnable action) {
        step("🧹 Cleanup: " + stepName, action);
    }
    
    /**
     * Добавляет вложенный шаг в текущий шаг.
     * 
     * @param parentStepName название родительского шага
     * @param childStepName название дочернего шага
     * @param action действие для выполнения
     */
    public static void nestedStep(String parentStepName, String childStepName, Runnable action) {
        step(parentStepName, () -> {
            step("  └─ " + childStepName, action);
        });
    }
    
    /**
     * Добавляет информационное сообщение как шаг.
     * 
     * @param message информационное сообщение
     */
    public static void infoStep(String message) {
        step("ℹ️ Info: " + message, () -> {
            logger.info("Info step: {}", message);
        });
    }
    
    /**
     * Добавляет предупреждение как шаг.
     * 
     * @param message предупреждающее сообщение
     */
    public static void warningStep(String message) {
        step("⚠️ Warning: " + message, () -> {
            logger.warn("Warning step: {}", message);
        });
    }
}