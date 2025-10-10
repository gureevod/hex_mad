package com.company.hex.ui.core;

import com.company.hex.core.config.HexConfigException;
import com.company.hex.core.config.HexConfigFactory;
import com.company.hex.ui.config.UiConfig;
import com.codeborne.selenide.Configuration;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Фабрика для создания и управления экземплярами WebDriver.
 * Обеспечивает потокобезопасное создание WebDriver с поддержкой различных браузеров.
 * Интегрируется с конфигурацией Selenide.
 * 
 * @author Hex Framework
 * @version 1.0
 */
public final class WebDriverFactory {

    private static final Logger logger = LoggerFactory.getLogger(WebDriverFactory.class);
    
    /**
     * Хранилище WebDriver экземпляров для каждого потока.
     * Обеспечивает изоляцию между параллельными тестами.
     */
    private static final ConcurrentMap<Long, WebDriver> driverMap = new ConcurrentHashMap<>();

    /**
     * Приватный конструктор для предотвращения создания экземпляров утилитного класса.
     */
    private WebDriverFactory() {
        throw new UnsupportedOperationException("Утилитный класс не может быть инстанцирован");
    }

    /**
     * Создать новый экземпляр WebDriver с конфигурацией по умолчанию.
     * Каждый вызов возвращает новый экземпляр для обеспечения потокобезопасности.
     * 
     * @return новый экземпляр WebDriver
     * @throws HexConfigException если не удалось создать WebDriver
     */
    public static WebDriver createDriver() {
        UiConfig config = HexConfigFactory.getConfig(UiConfig.class);
        return createDriver(config);
    }

    /**
     * Создать новый экземпляр WebDriver с пользовательской конфигурацией.
     * 
     * @param config пользовательская конфигурация UI
     * @return новый экземпляр WebDriver
     * @throws HexConfigException если не удалось создать WebDriver
     */
    public static WebDriver createDriver(UiConfig config) {
        if (config == null) {
            throw new HexConfigException("Конфигурация UI не может быть null");
        }

        try {
            logger.debug("Создание WebDriver для браузера: {}", config.browser());
            
            // Настройка Selenide конфигурации
            configureSelenide(config);
            
            WebDriver driver;
            String gridUrl = config.gridUrl();
            
            if (gridUrl != null && !gridUrl.trim().isEmpty()) {
                driver = createRemoteDriver(config, gridUrl);
            } else {
                driver = createLocalDriver(config);
            }
            
            // Настройка таймаутов
            configureTimeouts(driver, config);
            
            // Настройка размера окна
            configureWindowSize(driver, config);
            
            logger.info("WebDriver успешно создан для браузера: {}", config.browser());
            return driver;
            
        } catch (Exception e) {
            String errorMessage = String.format("Ошибка при создании WebDriver для браузера %s", config.browser());
            logger.error(errorMessage, e);
            throw new HexConfigException(errorMessage, e);
        }
    }

    /**
     * Получить WebDriver для текущего потока.
     * Если WebDriver не существует, создает новый.
     * 
     * @return WebDriver для текущего потока
     */
    public static WebDriver getDriverForCurrentThread() {
        long threadId = Thread.currentThread().getId();
        return driverMap.computeIfAbsent(threadId, id -> {
            logger.debug("Создание нового WebDriver для потока: {}", threadId);
            return createDriver();
        });
    }

    /**
     * Получить WebDriver для текущего потока с пользовательской конфигурацией.
     * 
     * @param config пользовательская конфигурация UI
     * @return WebDriver для текущего потока
     */
    public static WebDriver getDriverForCurrentThread(UiConfig config) {
        long threadId = Thread.currentThread().getId();
        return driverMap.computeIfAbsent(threadId, id -> {
            logger.debug("Создание нового WebDriver с пользовательской конфигурацией для потока: {}", threadId);
            return createDriver(config);
        });
    }

    /**
     * Закрыть WebDriver для текущего потока.
     */
    public static void closeDriverForCurrentThread() {
        long threadId = Thread.currentThread().getId();
        WebDriver driver = driverMap.remove(threadId);
        
        if (driver != null) {
            try {
                logger.debug("Закрытие WebDriver для потока: {}", threadId);
                driver.quit();
                logger.info("WebDriver успешно закрыт для потока: {}", threadId);
            } catch (Exception e) {
                logger.warn("Ошибка при закрытии WebDriver для потока {}: {}", threadId, e.getMessage());
            }
        }
    }

    /**
     * Закрыть все WebDriver экземпляры.
     */
    public static void closeAllDrivers() {
        logger.info("Закрытие всех WebDriver экземпляров");
        
        driverMap.forEach((threadId, driver) -> {
            try {
                logger.debug("Закрытие WebDriver для потока: {}", threadId);
                driver.quit();
            } catch (Exception e) {
                logger.warn("Ошибка при закрытии WebDriver для потока {}: {}", threadId, e.getMessage());
            }
        });
        
        driverMap.clear();
        logger.info("Все WebDriver экземпляры закрыты");
    }

    /**
     * Создать локальный WebDriver.
     * 
     * @param config конфигурация UI
     * @return локальный WebDriver
     */
    private static WebDriver createLocalDriver(UiConfig config) {
        String browser = config.browser().toLowerCase();
        
        switch (browser) {
            case "chrome":
                return createChromeDriver(config);
            case "firefox":
                return createFirefoxDriver(config);
            case "edge":
                return createEdgeDriver(config);
            default:
                throw new HexConfigException("Неподдерживаемый браузер: " + browser);
        }
    }

    /**
     * Создать удаленный WebDriver для Selenium Grid.
     * 
     * @param config конфигурация UI
     * @param gridUrl URL Selenium Grid
     * @return удаленный WebDriver
     */
    private static WebDriver createRemoteDriver(UiConfig config, String gridUrl) {
        try {
            URL url = new URL(gridUrl);
            String browser = config.browser().toLowerCase();
            
            switch (browser) {
                case "chrome":
                    return new RemoteWebDriver(url, createChromeOptions(config));
                case "firefox":
                    return new RemoteWebDriver(url, createFirefoxOptions(config));
                case "edge":
                    return new RemoteWebDriver(url, createEdgeOptions(config));
                default:
                    throw new HexConfigException("Неподдерживаемый браузер для Grid: " + browser);
            }
        } catch (MalformedURLException e) {
            throw new HexConfigException("Некорректный URL Selenium Grid: " + gridUrl, e);
        }
    }

    /**
     * Создать Chrome WebDriver.
     * 
     * @param config конфигурация UI
     * @return Chrome WebDriver
     */
    private static WebDriver createChromeDriver(UiConfig config) {
        ChromeOptions options = createChromeOptions(config);
        return new ChromeDriver(options);
    }

    /**
     * Создать опции для Chrome.
     * 
     * @param config конфигурация UI
     * @return опции Chrome
     */
    private static ChromeOptions createChromeOptions(UiConfig config) {
        ChromeOptions options = new ChromeOptions();
        
        if (config.headless()) {
            options.addArguments("--headless");
        }
        
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=" + config.windowWidth() + "," + config.windowHeight());
        
        if (!config.browserVersion().isEmpty()) {
            options.setBrowserVersion(config.browserVersion());
        }
        
        return options;
    }

    /**
     * Создать Firefox WebDriver.
     * 
     * @param config конфигурация UI
     * @return Firefox WebDriver
     */
    private static WebDriver createFirefoxDriver(UiConfig config) {
        FirefoxOptions options = createFirefoxOptions(config);
        return new FirefoxDriver(options);
    }

    /**
     * Создать опции для Firefox.
     * 
     * @param config конфигурация UI
     * @return опции Firefox
     */
    private static FirefoxOptions createFirefoxOptions(UiConfig config) {
        FirefoxOptions options = new FirefoxOptions();
        
        if (config.headless()) {
            options.addArguments("--headless");
        }
        
        if (!config.browserVersion().isEmpty()) {
            options.setBrowserVersion(config.browserVersion());
        }
        
        return options;
    }

    /**
     * Создать Edge WebDriver.
     * 
     * @param config конфигурация UI
     * @return Edge WebDriver
     */
    private static WebDriver createEdgeDriver(UiConfig config) {
        EdgeOptions options = createEdgeOptions(config);
        return new EdgeDriver(options);
    }

    /**
     * Создать опции для Edge.
     * 
     * @param config конфигурация UI
     * @return опции Edge
     */
    private static EdgeOptions createEdgeOptions(UiConfig config) {
        EdgeOptions options = new EdgeOptions();
        
        if (config.headless()) {
            options.addArguments("--headless");
        }
        
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        
        if (!config.browserVersion().isEmpty()) {
            options.setBrowserVersion(config.browserVersion());
        }
        
        return options;
    }

    /**
     * Настроить Selenide конфигурацию.
     * 
     * @param config конфигурация UI
     */
    private static void configureSelenide(UiConfig config) {
        Configuration.browser = config.browser();
        Configuration.headless = config.headless();
        Configuration.browserSize = config.windowWidth() + "x" + config.windowHeight();
        Configuration.baseUrl = config.baseUrl();
        Configuration.timeout = config.uiTimeout() * 1000L;
        Configuration.pageLoadTimeout = config.pageLoadTimeout() * 1000L;
        Configuration.screenshots = config.screenshotsEnabled();
        Configuration.reportsFolder = config.screenshotsFolder();
        
        if (!config.gridUrl().isEmpty()) {
            Configuration.remote = config.gridUrl();
        }
        
        logger.debug("Selenide конфигурация настроена");
    }

    /**
     * Настроить таймауты WebDriver.
     * 
     * @param driver WebDriver
     * @param config конфигурация UI
     */
    private static void configureTimeouts(WebDriver driver, UiConfig config) {
        driver.manage().timeouts()
            .implicitlyWait(Duration.ofSeconds(config.implicitTimeout()))
            .pageLoadTimeout(Duration.ofSeconds(config.pageLoadTimeout()));
        
        logger.debug("Таймауты WebDriver настроены");
    }

    /**
     * Настроить размер окна браузера.
     * 
     * @param driver WebDriver
     * @param config конфигурация UI
     */
    private static void configureWindowSize(WebDriver driver, UiConfig config) {
        driver.manage().window().setSize(new org.openqa.selenium.Dimension(config.windowWidth(), config.windowHeight()));
        logger.debug("Размер окна браузера установлен: {}x{}", config.windowWidth(), config.windowHeight());
    }

    /**
     * Получить количество активных WebDriver экземпляров.
     * 
     * @return количество активных драйверов
     */
    public static int getActiveDriverCount() {
        return driverMap.size();
    }
}