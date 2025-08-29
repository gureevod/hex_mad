package com.company.hex.testing.allure;

import com.company.hex.core.logging.CorrelationIdManager;
import com.company.hex.core.logging.HexLoggerFactory;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.TestResult;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.slf4j.Logger;

import java.util.Optional;

/**
 * Слушатель жизненного цикла тестов для интеграции с Allure Framework.
 * Автоматически управляет correlation ID, логированием и Allure отчетами.
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public class AllureLifecycleListener implements BeforeAllCallback, AfterAllCallback, 
                                               BeforeEachCallback, AfterEachCallback, TestWatcher {
    
    private static final Logger logger = HexLoggerFactory.getTestLogger(AllureLifecycleListener.class);
    private static final String CORRELATION_ID_LABEL = "correlationId";
    private static final String TEST_CLASS_LABEL = "testClass";
    private static final String TEST_METHOD_LABEL = "testMethod";
    
    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        String testClass = context.getRequiredTestClass().getSimpleName();
        logger.info("Starting test class execution: {}", testClass);
        
        // Устанавливаем класс теста в MDC
        CorrelationIdManager.setTestClass(testClass);
        
        // Добавляем информацию о классе в Allure
        Allure.label(TEST_CLASS_LABEL, testClass);
        
        logger.debug("Test class setup completed for: {}", testClass);
    }
    
    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        String testClass = context.getRequiredTestClass().getSimpleName();
        logger.info("Completed test class execution: {}", testClass);
        
        // Очищаем контекст класса
        CorrelationIdManager.clearTestContext();
        
        logger.debug("Test class cleanup completed for: {}", testClass);
    }
    
    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        String testClass = context.getRequiredTestClass().getSimpleName();
        String testMethod = context.getRequiredTestMethod().getName();
        
        // Инициализируем полный контекст теста
        String correlationId = CorrelationIdManager.initializeTestContext(testClass, testMethod);
        
        logger.info("Starting test: {}.{} [correlationId: {}]", testClass, testMethod, correlationId);
        
        // Добавляем метки в Allure
        Allure.label(CORRELATION_ID_LABEL, correlationId);
        Allure.label(TEST_METHOD_LABEL, testMethod);
        
        // Добавляем correlation ID как параметр теста
        Allure.parameter("Correlation ID", correlationId);
        
        logger.debug("Test setup completed for: {}.{}", testClass, testMethod);
    }
    
    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        String testClass = context.getRequiredTestClass().getSimpleName();
        String testMethod = context.getRequiredTestMethod().getName();
        String correlationId = CorrelationIdManager.getCorrelationId();
        
        logger.info("Completed test: {}.{} [correlationId: {}]", testClass, testMethod, correlationId);
        
        // Очищаем MDC контекст
        CorrelationIdManager.clearAll();
        
        logger.debug("Test cleanup completed for: {}.{}", testClass, testMethod);
    }
    
    @Override
    public void testSuccessful(ExtensionContext context) {
        String testClass = context.getRequiredTestClass().getSimpleName();
        String testMethod = context.getRequiredTestMethod().getName();
        String correlationId = CorrelationIdManager.getCorrelationId();
        
        logger.info("✅ Test PASSED: {}.{} [correlationId: {}]", testClass, testMethod, correlationId);
        
        // Добавляем информацию об успешном выполнении в Allure
        Allure.step("Test completed successfully", () -> {
            Allure.addAttachment("Test Result", "PASSED");
            Allure.addAttachment("Correlation ID", correlationId != null ? correlationId : "N/A");
        });
    }
    
    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        String testClass = context.getRequiredTestClass().getSimpleName();
        String testMethod = context.getRequiredTestMethod().getName();
        String correlationId = CorrelationIdManager.getCorrelationId();
        
        logger.error("❌ Test FAILED: {}.{} [correlationId: {}]", testClass, testMethod, correlationId, cause);
        
        // Добавляем информацию об ошибке в Allure
        Allure.step("Test failed with error", () -> {
            Allure.addAttachment("Test Result", "FAILED");
            Allure.addAttachment("Correlation ID", correlationId != null ? correlationId : "N/A");
            Allure.addAttachment("Error Message", cause.getMessage() != null ? cause.getMessage() : "No message");
            Allure.addAttachment("Stack Trace", getStackTrace(cause));
        });
    }
    
    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        String testClass = context.getRequiredTestClass().getSimpleName();
        String testMethod = context.getRequiredTestMethod().getName();
        String correlationId = CorrelationIdManager.getCorrelationId();
        
        logger.warn("⚠️ Test ABORTED: {}.{} [correlationId: {}]", testClass, testMethod, correlationId, cause);
        
        // Добавляем информацию о прерывании в Allure
        Allure.step("Test was aborted", () -> {
            Allure.addAttachment("Test Result", "ABORTED");
            Allure.addAttachment("Correlation ID", correlationId != null ? correlationId : "N/A");
            if (cause != null) {
                Allure.addAttachment("Abort Reason", cause.getMessage() != null ? cause.getMessage() : "No reason provided");
            }
        });
    }
    
    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        String testClass = context.getRequiredTestClass().getSimpleName();
        String testMethod = context.getRequiredTestMethod().getName();
        
        logger.info("⏭️ Test DISABLED: {}.{} - Reason: {}", testClass, testMethod, reason.orElse("No reason provided"));
        
        // Добавляем информацию о отключении в Allure
        Allure.step("Test was disabled", () -> {
            Allure.addAttachment("Test Result", "DISABLED");
            Allure.addAttachment("Disable Reason", reason.orElse("No reason provided"));
        });
    }
    
    /**
     * Получает stack trace как строку.
     * 
     * @param throwable исключение
     * @return stack trace в виде строки
     */
    private String getStackTrace(Throwable throwable) {
        if (throwable == null) {
            return "No stack trace available";
        }
        
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
    
    /**
     * Добавляет кастомную информацию в текущий тест Allure.
     * 
     * @param name имя параметра
     * @param value значение параметра
     */
    public static void addTestParameter(String name, String value) {
        if (name != null && value != null) {
            Allure.parameter(name, value);
            logger.debug("Added test parameter: {} = {}", name, value);
        }
    }
    
    /**
     * Добавляет метку в текущий тест Allure.
     * 
     * @param name имя метки
     * @param value значение метки
     */
    public static void addTestLabel(String name, String value) {
        if (name != null && value != null) {
            Allure.label(name, value);
            logger.debug("Added test label: {} = {}", name, value);
        }
    }
    
    /**
     * Добавляет ссылку в текущий тест Allure.
     * 
     * @param name имя ссылки
     * @param url URL ссылки
     */
    public static void addTestLink(String name, String url) {
        if (name != null && url != null) {
            Allure.link(name, url);
            logger.debug("Added test link: {} = {}", name, url);
        }
    }
}