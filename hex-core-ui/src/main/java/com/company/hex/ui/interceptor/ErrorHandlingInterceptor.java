package com.company.hex.ui.interceptor;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.ex.ElementNotFound;
import com.codeborne.selenide.ex.UIAssertionError;
import com.company.hex.ui.core.BaseElement;
import com.company.hex.ui.exception.ElementNotFoundException;
import io.qameta.allure.Allure;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static com.codeborne.selenide.WebDriverRunner.hasWebDriverStarted;

/**
 * Interceptor для обработки ошибок с элементами.
 * Трансформирует низкоуровневые исключения в информативные.
 */
public class ErrorHandlingInterceptor implements ElementInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ErrorHandlingInterceptor.class);

    private final boolean captureScreenshot;
    private final boolean capturePageSource;
    private final boolean enrichExceptions;

    public ErrorHandlingInterceptor() {
        this(true, true, true);
    }

    public ErrorHandlingInterceptor(boolean captureScreenshot,
                                    boolean capturePageSource,
                                    boolean enrichExceptions) {
        this.captureScreenshot = captureScreenshot;
        this.capturePageSource = capturePageSource;
        this.enrichExceptions = enrichExceptions;
    }

    @Override
    public <R> R intercept(ActionContext context, ActionChain<R> chain) {
        try {
            return chain.proceed();
        } catch (Exception e) {
            logger.debug("ErrorHandlingInterceptor поймал исключение: {} - {}",
                    e.getClass().getName(), e.getMessage());

            // Сначала собираем артефакты
            handleErrorArtifacts(context, e);

            // Затем трансформируем и выбрасываем
            RuntimeException transformed = safeTransformException(context, e);
            throw transformed;
        }
    }

    /**
     * Безопасная трансформация исключения (не выбросит новое исключение).
     */
    private RuntimeException safeTransformException(ActionContext context, Exception e) {
        try {
            return transformException(context, e);
        } catch (Exception transformError) {
            logger.error("Ошибка при трансформации исключения: {}",
                    transformError.getMessage(), transformError);
            // Возвращаем оригинальное исключение если трансформация не удалась
            return wrapIfNeeded(e);
        }
    }

    /**
     * Трансформировать исключение в более информативное.
     */
    private RuntimeException transformException(ActionContext context, Exception e) {
        if (!enrichExceptions) {
            return wrapIfNeeded(e);
        }

        logger.debug("Анализ исключения: {}, isElementNotFound={}",
                e.getClass().getSimpleName(), isElementNotFoundError(e));

        // Проверяем, является ли это ошибкой "элемент не найден"
        if (isElementNotFoundError(e)) {
            return createElementNotFoundException(context, e);
        }

        // Проверяем StaleElementReferenceException
        if (isStaleElementError(e)) {
            return createStaleElementException(context, e);
        }

        // Проверяем timeout
        if (isTimeoutError(e)) {
            return createTimeoutException(context, e);
        }

        // Остальные ошибки оборачиваем как есть
        return wrapIfNeeded(e);
    }

    /**
     * Проверить, является ли ошибка типом "элемент не найден".
     */
    private boolean isElementNotFoundError(Throwable e) {
        // Selenide ElementNotFound (основной случай)
        if (e instanceof ElementNotFound) {
            return true;
        }

        // Selenium NoSuchElementException
        if (e instanceof NoSuchElementException) {
            return true;
        }

        // Selenide UIAssertionError с сообщением о ненайденном элементе
        if (e instanceof UIAssertionError) {
            String message = e.getMessage();
            if (message != null &&
                    (message.contains("Element not found") ||
                            message.contains("element not found") ||
                            message.contains("не найден"))) {
                return true;
            }
        }

        // Проверяем cause рекурсивно
        Throwable cause = e.getCause();
        if (cause != null && cause != e && cause instanceof Exception) {
            return isElementNotFoundError((Exception) cause);
        }

        return false;
    }

    /**
     * Проверить, является ли ошибка типом "устаревший элемент".
     */
    private boolean isStaleElementError(Exception e) {
        if (e instanceof StaleElementReferenceException) {
            return true;
        }

        Throwable cause = e.getCause();
        return cause instanceof StaleElementReferenceException;
    }

    /**
     * Проверить, является ли ошибка timeout.
     */
    private boolean isTimeoutError(Exception e) {
        if (e instanceof TimeoutException) {
            return true;
        }

        String message = e.getMessage();
        return message != null &&
                (message.contains("Timeout") || message.contains("timeout") ||
                        message.contains("timed out"));
    }

    /**
     * Создать ElementNotFoundException с полным контекстом.
     */
    private ElementNotFoundException createElementNotFoundException(ActionContext context, Exception e) {
        BaseElement element = context.getElement();

        String currentUrl = safeGetCurrentUrl();
        String pageTitle = safeGetPageTitle();
        String locator = extractLocator(element, e);
        Duration timeout = extractTimeout(element, context);

        logger.debug("Создание ElementNotFoundException: element={}, locator={}, page={}",
                element.getName(), locator, element.getPageName());

        return ElementNotFoundException.builder()
                .elementName(element.getName())
                .locator(locator)
                .pageName(element.getPageName())
                .componentName(element.getComponentName())
                .timeout(timeout)
                .currentUrl(currentUrl)
                .pageTitle(pageTitle)
                .cause(e)
                .build();
    }

    /**
     * Создать исключение для StaleElement.
     */
    private RuntimeException createStaleElementException(ActionContext context, Exception e) {
        BaseElement element = context.getElement();

        String message = String.format(
                "\n╔══════════════════════════════════════════════════════════════╗\n" +
                        "║              ⚠️ ЭЛЕМЕНТ СТАЛ УСТАРЕВШИМ                      ║\n" +
                        "╠══════════════════════════════════════════════════════════════╣\n" +
                        "║ Элемент:   %-48s ║\n" +
                        "║ Действие:  %-48s ║\n" +
                        "║ Страница:  %-48s ║\n" +
                        "╠══════════════════════════════════════════════════════════════╣\n" +
                        "║ DOM был изменён между поиском элемента и действием.         ║\n" +
                        "╠══════════════════════════════════════════════════════════════╣\n" +
                        "║ 💡 Рекомендации:                                             ║\n" +
                        "║   • Добавить ожидание стабильности страницы                  ║\n" +
                        "║   • Использовать retry-логику                                ║\n" +
                        "║   • Искать элемент заново перед каждым действием             ║\n" +
                        "╚══════════════════════════════════════════════════════════════╝",
                truncate(element.getName(), 48),
                truncate(context.getActionName(), 48),
                truncate(element.getPageName(), 48)
        );

        return new IllegalStateException(message, e);
    }

    /**
     * Создать исключение для Timeout.
     */
    private RuntimeException createTimeoutException(ActionContext context, Exception e) {
        BaseElement element = context.getElement();
        Duration timeout = extractTimeout(element, context);

        String timeoutStr = timeout != null ? formatDuration(timeout) : "N/A";

        String message = String.format(
                "\n╔══════════════════════════════════════════════════════════════╗\n" +
                        "║                    ⏱️ TIMEOUT                                ║\n" +
                        "╠══════════════════════════════════════════════════════════════╣\n" +
                        "║ Элемент:   %-48s ║\n" +
                        "║ Действие:  %-48s ║\n" +
                        "║ Страница:  %-48s ║\n" +
                        "║ Timeout:   %-48s ║\n" +
                        "╠══════════════════════════════════════════════════════════════╣\n" +
                        "║ 💡 Рекомендации:                                             ║\n" +
                        "║   • Увеличить timeout для данного элемента                   ║\n" +
                        "║   • Проверить загрузку страницы                              ║\n" +
                        "║   • Убедиться что элемент существует                         ║\n" +
                        "╚══════════════════════════════════════════════════════════════╝",
                truncate(element.getName(), 48),
                truncate(context.getActionName(), 48),
                truncate(element.getPageName(), 48),
                timeoutStr
        );

        return new TimeoutException(message, e);
    }

    /**
     * Извлечь локатор из элемента или исключения.
     */
    private String extractLocator(BaseElement element, Throwable e) {
        // 1. Пробуем получить из сообщения Selenide исключения
        if (e instanceof ElementNotFound) {
            String msg = e.getMessage();
            if (msg != null) {
                // Паттерн "Element not found {By.xpath: ...}"
                int start = msg.indexOf("{By.");
                if (start >= 0) {
                    int end = msg.indexOf("}", start);
                    if (end > start) {
                        return msg.substring(start + 1, end);
                    }
                }
            }
        }

        // 2. Пробуем получить из UIAssertionError
        if (e instanceof UIAssertionError) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("By.")) {
                int start = msg.indexOf("By.");
                int end = findLocatorEnd(msg, start);
                if (end > start) {
                    return msg.substring(start, end);
                }
            }
        }

        // 3. Пробуем получить describe от Selenide элемента
        try {
            String description = element.getElement().toString();
            if (description != null && !description.isEmpty() && !description.equals("null")) {
                return description;
            }
        } catch (Exception ignored) {
            // Игнорируем ошибки
        }

        // 4. Fallback - ищем в любом месте сообщения
        String msg = e.getMessage();
        if (msg != null) {
            // Ищем xpath
            if (msg.contains("xpath:")) {
                int start = msg.indexOf("xpath:");
                int end = findLocatorEnd(msg, start);
                return msg.substring(start, end);
            }
            // Ищем css
            if (msg.contains("css selector:")) {
                int start = msg.indexOf("css selector:");
                int end = findLocatorEnd(msg, start);
                return msg.substring(start, end);
            }
        }

        return "Unknown locator";
    }

    private int findLocatorEnd(String msg, int start) {
        int end = msg.length();

        // Ищем конец локатора по различным признакам
        int newLine = msg.indexOf('\n', start);
        if (newLine > start) end = Math.min(end, newLine);

        int curlyBrace = msg.indexOf('}', start);
        if (curlyBrace > start) end = Math.min(end, curlyBrace + 1);

        int expected = msg.indexOf("Expected:", start);
        if (expected > start) end = Math.min(end, expected);

        int screenshot = msg.indexOf("Screenshot:", start);
        if (screenshot > start) end = Math.min(end, screenshot);

        return end;
    }

    /**
     * Извлечь timeout из настроек элемента.
     */
    private Duration extractTimeout(BaseElement element, ActionContext context) {
        // Из настроек элемента
        if (element.getSettings() != null && element.getSettings().getTimeout() != null) {
            return element.getSettings().getTimeout();
        }

        // Из контекста
        Object timeoutAttr = context.getAttribute("timeout");
        if (timeoutAttr instanceof Duration) {
            return (Duration) timeoutAttr;
        }

        // Default
        return Duration.ofSeconds(10);
    }

    /**
     * Обработать артефакты ошибки: скриншот, HTML, контекст.
     */
    private void handleErrorArtifacts(ActionContext context, Exception e) {
        BaseElement element = context.getElement();
        String actionName = context.getActionName();

        logger.error("❌ Ошибка при выполнении '{}' на элементе '{}': {}",
                actionName, element.getName(), e.getMessage());

        // Скриншот
        if (captureScreenshot) {
            captureScreenshotToAllure(context);
        }

        // HTML страницы
        if (capturePageSource) {
            capturePageSourceToAllure(context);
        }

        // Дополнительная информация в Allure
        attachContextInfo(context, e);
    }

    /**
     * Сделать скриншот и добавить в Allure.
     */
    private void captureScreenshotToAllure(ActionContext context) {
        if (!hasWebDriverStarted()) {
            logger.debug("WebDriver не запущен, скриншот пропущен");
            return;
        }

        try {
            String screenshotName = String.format("Error_%s_%s_%d",
                    sanitize(context.getElement().getName()),
                    sanitize(context.getActionName()),
                    System.currentTimeMillis()
            );

            byte[] screenshot = Selenide.screenshot(OutputType.BYTES);

            if (screenshot != null && screenshot.length > 0) {
                Allure.addAttachment(screenshotName, "image/png",
                        new ByteArrayInputStream(screenshot), "png");

                logger.debug("📸 Скриншот сохранён: {}", screenshotName);
            }
        } catch (Exception e) {
            logger.warn("Не удалось сделать скриншот: {}", e.getMessage());
        }
    }

    /**
     * Сохранить HTML страницы в Allure.
     */
    private void capturePageSourceToAllure(ActionContext context) {
        if (!hasWebDriverStarted()) {
            return;
        }

        try {
            String pageSource = getWebDriver().getPageSource();

            if (pageSource != null && !pageSource.isEmpty()) {
                String attachName = String.format("PageSource_%s_%s",
                        sanitize(context.getElement().getName()),
                        sanitize(context.getActionName())
                );

                Allure.addAttachment(attachName, "text/html",
                        new ByteArrayInputStream(pageSource.getBytes(StandardCharsets.UTF_8)), "html");

                logger.debug("📄 HTML страницы сохранён: {}", attachName);
            }
        } catch (Exception e) {
            logger.warn("Не удалось сохранить HTML страницы: {}", e.getMessage());
        }
    }

    /**
     * Добавить контекстную информацию в Allure.
     */
    private void attachContextInfo(ActionContext context, Exception e) {
        try {
            StringBuilder info = new StringBuilder();
            info.append("=== Error Context ===\n\n");

            info.append("Element: ").append(context.getElement().getName()).append("\n");
            info.append("Action: ").append(context.getActionName()).append("\n");
            info.append("Page: ").append(context.getElement().getPageName()).append("\n");
            info.append("Component: ").append(context.getElement().getComponentName()).append("\n");
            info.append("Elapsed Time: ").append(context.getElapsedMillis()).append("ms\n");
            info.append("\n");

            info.append("=== Arguments ===\n");
            Object[] args = context.getArgs();
            if (args != null && args.length > 0) {
                for (int i = 0; i < args.length; i++) {
                    info.append("  [").append(i).append("]: ").append(args[i]).append("\n");
                }
            } else {
                info.append("  (none)\n");
            }
            info.append("\n");

            info.append("=== Browser Info ===\n");
            info.append("URL: ").append(safeGetCurrentUrl()).append("\n");
            info.append("Title: ").append(safeGetPageTitle()).append("\n");
            info.append("\n");

            info.append("=== Exception ===\n");
            info.append("Type: ").append(e.getClass().getName()).append("\n");
            info.append("Message: ").append(e.getMessage()).append("\n");

            if (e.getCause() != null) {
                info.append("Cause: ").append(e.getCause().getClass().getName())
                        .append(" - ").append(e.getCause().getMessage()).append("\n");
            }

            Allure.addAttachment("Error_Context", "text/plain",
                    new ByteArrayInputStream(info.toString().getBytes(StandardCharsets.UTF_8)), "txt");

        } catch (Exception ex) {
            logger.warn("Не удалось добавить контекст ошибки: {}", ex.getMessage());
        }
    }

    // ==================== Utility Methods ====================

    private String safeGetCurrentUrl() {
        try {
            if (hasWebDriverStarted()) {
                return getWebDriver().getCurrentUrl();
            }
        } catch (Exception e) {
            logger.trace("Не удалось получить URL: {}", e.getMessage());
        }
        return "N/A";
    }

    private String safeGetPageTitle() {
        try {
            if (hasWebDriverStarted()) {
                return getWebDriver().getTitle();
            }
        } catch (Exception e) {
            logger.trace("Не удалось получить title: {}", e.getMessage());
        }
        return "N/A";
    }

    private String formatDuration(Duration duration) {
        if (duration == null) return "N/A";

        long millis = duration.toMillis();
        if (millis < 1000) {
            return millis + "ms";
        }
        return String.format("%.1fs", millis / 1000.0);
    }

    private RuntimeException wrapIfNeeded(Exception e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new RuntimeException(e);
    }

    private String truncate(String str, int maxLen) {
        if (str == null) return "";
        if (str.length() <= maxLen) return str;
        return str.substring(0, maxLen - 3) + "...";
    }

    private String sanitize(String str) {
        if (str == null) return "unknown";
        return str.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    @Override
    public int getOrder() {
        // Должен выполняться последним (ближе всего к реальному действию)
        return Integer.MAX_VALUE - 100;
    }

    @Override
    public boolean supports(String actionName) {
        return true;
    }
}