package com.company.hex.ui.logging;

import com.company.hex.core.logging.CorrelationIdManager;
import com.company.hex.core.logging.HexLoggerFactory;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Утилитарный класс для детального логирования UI действий с интеграцией Allure.
 * Предоставляет методы для логирования действий, создания скриншотов и прикрепления к отчетам.
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public class UiActionLogger {
    
    private static final Logger logger = HexLoggerFactory.getUiLogger(UiActionLogger.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    
    /**
     * Приватный конструктор для предотвращения создания экземпляров.
     */
    private UiActionLogger() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Логирует начало UI действия.
     * 
     * @param action описание действия
     * @param element описание элемента
     */
    public static void logActionStart(String action, String element) {
        String correlationId = CorrelationIdManager.getCorrelationId();
        logger.info("🖥️ [{}] Начало действия: {} с элементом '{}'", correlationId, action, element);
    }
    
    /**
     * Логирует успешное завершение UI действия.
     * 
     * @param action описание действия
     * @param element описание элемента
     */
    public static void logActionSuccess(String action, String element) {
        String correlationId = CorrelationIdManager.getCorrelationId();
        logger.info("✅ [{}] Успешно выполнено: {} с элементом '{}'", correlationId, action, element);
    }
    
    /**
     * Логирует ошибку при выполнении UI действия.
     * 
     * @param action описание действия
     * @param element описание элемента
     * @param error ошибка
     */
    public static void logActionError(String action, String element, Throwable error) {
        String correlationId = CorrelationIdManager.getCorrelationId();
        logger.error("❌ [{}] Ошибка при выполнении: {} с элементом '{}' - {}", 
                    correlationId, action, element, error.getMessage(), error);
        
        // Автоматически создаем скриншот при ошибке
        takeScreenshotOnError(action, element, error);
    }
    
    /**
     * Создает скриншот и прикрепляет к Allure отчету.
     * 
     * @param screenshotName имя скриншота
     * @return true если скриншот создан успешно
     */
    @Step("📸 Создание скриншота: {screenshotName}")
    public static boolean takeScreenshot(String screenshotName) {
        String correlationId = CorrelationIdManager.getCorrelationId();
        logger.info("📸 [{}] Создание скриншота: {}", correlationId, screenshotName);
        
        try {
            if (!WebDriverRunner.hasWebDriverStarted()) {
                logger.warn("⚠️ [{}] WebDriver не запущен, скриншот не может быть создан", correlationId);
                return false;
            }
            
            WebDriver driver = WebDriverRunner.getWebDriver();
            if (!(driver instanceof TakesScreenshot)) {
                logger.warn("⚠️ [{}] WebDriver не поддерживает создание скриншотов", correlationId);
                return false;
            }
            
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String fullName = String.format("%s_%s", screenshotName, timestamp);
            
            // Прикрепляем к Allure
            Allure.addAttachment(fullName, "image/png", 
                               new java.io.ByteArrayInputStream(screenshot), "png");
            
            logger.debug("✅ [{}] Скриншот '{}' успешно создан и прикреплен", correlationId, fullName);
            return true;
            
        } catch (Exception e) {
            logger.error("❌ [{}] Ошибка при создании скриншота '{}': {}", 
                        correlationId, screenshotName, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Создает скриншот при ошибке.
     * 
     * @param action действие, при котором произошла ошибка
     * @param element элемент, с которым работали
     * @param error ошибка
     */
    private static void takeScreenshotOnError(String action, String element, Throwable error) {
        String screenshotName = String.format("Error_%s_%s", 
                                            action.replaceAll("[^a-zA-Z0-9]", "_"), 
                                            element.replaceAll("[^a-zA-Z0-9]", "_"));
        
        if (takeScreenshot(screenshotName)) {
            // Также прикрепляем информацию об ошибке
            String errorInfo = String.format("Action: %s\nElement: %s\nError: %s\nStack Trace: %s",
                                            action, element, error.getMessage(), getStackTrace(error));
            Allure.addAttachment("Error Details", "text/plain", errorInfo);
        }
    }
    
    /**
     * Логирует навигацию на страницу.
     * 
     * @param url URL страницы
     */
    @Step("🌐 Навигация на страницу: {url}")
    public static void logNavigation(String url) {
        String correlationId = CorrelationIdManager.getCorrelationId();
        logger.info("🌐 [{}] Навигация на страницу: {}", correlationId, url);
        
        try {
            String currentUrl = WebDriverRunner.getWebDriver().getCurrentUrl();
            logger.debug("📍 [{}] Текущий URL: {}", correlationId, currentUrl);
            
            // Прикрепляем информацию о навигации к Allure
            Allure.addAttachment("Navigation Info", 
                               String.format("Target URL: %s\nCurrent URL: %s", url, currentUrl));
            
        } catch (Exception e) {
            logger.warn("⚠️ [{}] Не удалось получить текущий URL: {}", correlationId, e.getMessage());
        }
    }
    
    /**
     * Логирует ожидание элемента.
     * 
     * @param element описание элемента
     * @param condition условие ожидания
     * @param timeoutMs таймаут в миллисекундах
     */
    @Step("⏳ Ожидание элемента '{element}': {condition} ({timeoutMs} мс)")
    public static void logWait(String element, String condition, long timeoutMs) {
        String correlationId = CorrelationIdManager.getCorrelationId();
        logger.info("⏳ [{}] Ожидание элемента '{}': {} ({} мс)", 
                   correlationId, element, condition, timeoutMs);
    }
    
    /**
     * Логирует проверку (assertion).
     * 
     * @param element описание элемента
     * @param condition условие проверки
     * @param expectedValue ожидаемое значение
     */
    @Step("✅ Проверка элемента '{element}': {condition} = '{expectedValue}'")
    public static void logAssertion(String element, String condition, String expectedValue) {
        String correlationId = CorrelationIdManager.getCorrelationId();
        logger.info("✅ [{}] Проверка элемента '{}': {} = '{}'", 
                   correlationId, element, condition, expectedValue);
    }
    
    /**
     * Логирует успешную проверку.
     * 
     * @param element описание элемента
     * @param condition условие проверки
     */
    public static void logAssertionSuccess(String element, String condition) {
        String correlationId = CorrelationIdManager.getCorrelationId();
        logger.debug("✅ [{}] Проверка пройдена для элемента '{}': {}", 
                    correlationId, element, condition);
    }
    
    /**
     * Логирует неудачную проверку.
     * 
     * @param element описание элемента
     * @param condition условие проверки
     * @param actualValue фактическое значение
     * @param expectedValue ожидаемое значение
     */
    public static void logAssertionFailure(String element, String condition, 
                                         String actualValue, String expectedValue) {
        String correlationId = CorrelationIdManager.getCorrelationId();
        logger.error("❌ [{}] Проверка не пройдена для элемента '{}': {} - ожидалось '{}', получено '{}'", 
                    correlationId, element, condition, expectedValue, actualValue);
        
        // Создаем скриншот при неудачной проверке
        takeScreenshot(String.format("AssertionFailure_%s", element.replaceAll("[^a-zA-Z0-9]", "_")));
        
        // Прикрепляем детали проверки
        String assertionDetails = String.format("Element: %s\nCondition: %s\nExpected: %s\nActual: %s",
                                               element, condition, expectedValue, actualValue);
        Allure.addAttachment("Assertion Failure Details", "text/plain", assertionDetails);
    }
    
    /**
     * Логирует информацию о браузере и странице.
     */
    @Step("ℹ️ Получение информации о браузере и странице")
    public static void logBrowserInfo() {
        String correlationId = CorrelationIdManager.getCorrelationId();
        
        try {
            if (!WebDriverRunner.hasWebDriverStarted()) {
                logger.info("ℹ️ [{}] WebDriver не запущен", correlationId);
                return;
            }
            
            WebDriver driver = WebDriverRunner.getWebDriver();
            String currentUrl = driver.getCurrentUrl();
            String title = driver.getTitle();
            String windowHandle = driver.getWindowHandle();
            
            logger.info("ℹ️ [{}] Информация о браузере:", correlationId);
            logger.info("  📍 URL: {}", currentUrl);
            logger.info("  📄 Заголовок: {}", title);
            logger.info("  🪟 Window Handle: {}", windowHandle);
            
            // Прикрепляем к Allure
            String browserInfo = String.format("URL: %s\nTitle: %s\nWindow Handle: %s\nUser Agent: %s",
                                             currentUrl, title, windowHandle, 
                                             Selenide.executeJavaScript("return navigator.userAgent;"));
            Allure.addAttachment("Browser Info", "text/plain", browserInfo);
            
        } catch (Exception e) {
            logger.error("❌ [{}] Ошибка при получении информации о браузере: {}", 
                        correlationId, e.getMessage(), e);
        }
    }
    
    /**
     * Логирует выполнение JavaScript кода.
     * 
     * @param script JavaScript код
     * @param result результат выполнения
     */
    @Step("🔧 Выполнение JavaScript: {script}")
    public static void logJavaScriptExecution(String script, Object result) {
        String correlationId = CorrelationIdManager.getCorrelationId();
        logger.info("🔧 [{}] Выполнение JavaScript: {}", correlationId, script);
        logger.debug("📋 [{}] Результат JavaScript: {}", correlationId, result);
        
        // Прикрепляем к Allure
        String jsInfo = String.format("Script: %s\nResult: %s", script, result);
        Allure.addAttachment("JavaScript Execution", "text/plain", jsInfo);
    }
    
    /**
     * Создает полный отчет о состоянии страницы.
     */
    @Step("📊 Создание полного отчета о состоянии страницы")
    public static void createPageStateReport() {
        String correlationId = CorrelationIdManager.getCorrelationId();
        logger.info("📊 [{}] Создание полного отчета о состоянии страницы", correlationId);
        
        // Создаем скриншот
        takeScreenshot("PageState");
        
        // Получаем информацию о браузере
        logBrowserInfo();
        
        try {
            // Получаем HTML страницы
            String pageSource = WebDriverRunner.getWebDriver().getPageSource();
            Allure.addAttachment("Page Source", "text/html", pageSource);
            
            // Получаем логи консоли
            String consoleLogs = Selenide.executeJavaScript(
                "return JSON.stringify(console.logs || [], null, 2);"
            );
            if (consoleLogs != null && !consoleLogs.equals("[]")) {
                Allure.addAttachment("Console Logs", "application/json", consoleLogs);
            }
            
        } catch (Exception e) {
            logger.warn("⚠️ [{}] Не удалось получить дополнительную информацию о странице: {}", 
                       correlationId, e.getMessage());
        }
    }
    
    /**
     * Получает stack trace как строку.
     * 
     * @param throwable исключение
     * @return stack trace в виде строки
     */
    private static String getStackTrace(Throwable throwable) {
        if (throwable == null) {
            return "No stack trace available";
        }
        
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}