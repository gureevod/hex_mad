package com.company.hex.ui.core;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverProvider;
import com.company.hex.core.config.HexConfigException;
import com.company.hex.core.config.HexConfigFactory;
import com.company.hex.ui.config.UiConfig;
import org.openqa.selenium.Capabilities;
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

import javax.annotation.Nonnull;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Гибкая фабрика WebDriver, реализующая интерфейс Selenide WebDriverProvider.
 * Позволяет регистрировать кастомные стратегии создания драйверов и модифицировать опции.
 */
public class WebDriverFactory implements WebDriverProvider {

    private static final Logger logger = LoggerFactory.getLogger(WebDriverFactory.class);

    // Реестр стратегий создания драйверов (Браузер -> Функция создания)
    private static final Map<String, Function<UiConfig, WebDriver>> driverStrategies = new ConcurrentHashMap<>();
    
    // Реестр модификаторов опций (Браузер -> Консумер для настройки опций)
    private static final Map<String, Consumer<Object>> optionsCustomizers = new ConcurrentHashMap<>();

    // Статический блок инициализации стандартных стратегий
    static {
        registerStrategy("chrome", WebDriverFactory::createChromeDriver);
        registerStrategy("firefox", WebDriverFactory::createFirefoxDriver);
        registerStrategy("edge", WebDriverFactory::createEdgeDriver);
    }

    /**
     * Метод, вызываемый Selenide, когда ему нужен драйвер.
     */
    @Nonnull
    @Override
    public WebDriver createDriver(@Nonnull Capabilities capabilities) {
        UiConfig config = HexConfigFactory.getConfig(UiConfig.class);
        logger.info("Selenide запросил создание драйвера. Браузер: {}", config.browser());

        // 1. Проверяем, есть ли Grid URL
        if (config.gridUrl() != null && !config.gridUrl().trim().isEmpty()) {
            return createRemoteDriver(config);
        }

        // 2. Ищем стратегию для локального запуска
        String browserName = config.browser().toLowerCase();
        Function<UiConfig, WebDriver> strategy = driverStrategies.get(browserName);

        if (strategy == null) {
            throw new HexConfigException("Не найдена стратегия для браузера: " + browserName + 
                ". Используйте WebDriverFactory.registerStrategy() для добавления.");
        }

        WebDriver driver = strategy.apply(config);
        configureCommonSettings(driver, config);
        return driver;
    }

    // --- Public API для настройки ---

    /**
     * Регистрация кастомной стратегии создания драйвера.
     * Позволяет добавить поддержку Opera, Safari или кастомного Chrome.
     */
    public static void registerStrategy(String browserName, Function<UiConfig, WebDriver> creator) {
        driverStrategies.put(browserName.toLowerCase(), creator);
    }

    /**
     * Добавить кастомные настройки для опций браузера перед созданием.
     * Пример: добавить расширение или специфичный флаг.
     */
    @SuppressWarnings("unchecked")
    public static <T> void addOptionsCustomizer(String browserName, Class<T> optionsClass, Consumer<T> customizer) {
        optionsCustomizers.put(browserName.toLowerCase(), (Consumer<Object>) customizer);
    }

    /**
     * Инициализация Selenide, чтобы он использовал эту фабрику.
     * Этот метод нужно вызвать в @BeforeSuite или в начале тестов.
     */
    public static void initSelenide() {
        UiConfig config = HexConfigFactory.getConfig(UiConfig.class);
        
        // Говорим Selenide использовать ЭТОТ класс как провайдер
        Configuration.browser = WebDriverFactory.class.getName();
        
        // Базовые настройки Selenide
        Configuration.baseUrl = config.baseUrl();
        Configuration.timeout = config.uiTimeout() * 1000L;
        Configuration.pageLoadTimeout = config.pageLoadTimeout() * 1000L;
        Configuration.headless = config.headless();
        Configuration.browserSize = config.windowWidth() + "x" + config.windowHeight();
        Configuration.screenshots = config.screenshotsEnabled();
        Configuration.reportsFolder = config.screenshotsFolder();
        
        logger.info("Selenide инициализирован с использованием WebDriverFactory");
    }

    // --- Внутренние методы создания (Default Strategies) ---

    private static WebDriver createChromeDriver(UiConfig config) {
        ChromeOptions options = new ChromeOptions();
        applyDefaultChromeOptions(options, config);
        applyCustomOptions("chrome", options);
        return new ChromeDriver(options);
    }

    private static WebDriver createFirefoxDriver(UiConfig config) {
        FirefoxOptions options = new FirefoxOptions();
        if (config.headless()) options.addArguments("-headless");
        if (!config.browserVersion().isEmpty()) options.setBrowserVersion(config.browserVersion());
        applyCustomOptions("firefox", options);
        return new FirefoxDriver(options);
    }

    private static WebDriver createEdgeDriver(UiConfig config) {
        EdgeOptions options = new EdgeOptions();
        if (config.headless()) options.addArguments("--headless");
        applyCustomOptions("edge", options);
        return new EdgeDriver(options);
    }

    private static WebDriver createRemoteDriver(UiConfig config) {
        try {
            URL gridUrl = new URL(config.gridUrl());
            Capabilities capabilities;
            String browser = config.browser().toLowerCase();

            switch (browser) {
                case "chrome":
                    ChromeOptions co = new ChromeOptions();
                    applyDefaultChromeOptions(co, config);
                    applyCustomOptions("chrome", co);
                    capabilities = co;
                    break;
                case "firefox":
                    FirefoxOptions fo = new FirefoxOptions();
                    applyCustomOptions("firefox", fo);
                    capabilities = fo;
                    break;
                default:
                    // Если браузер нестандартный, пытаемся найти стратегию, которая вернет драйвер,
                    // и вытащить из него опции, либо бросаем ошибку.
                    // Для простоты здесь базовый вариант:
                    throw new HexConfigException("Remote driver пока поддерживает только chrome/firefox/edge в базовой реализации");
            }
            
            RemoteWebDriver driver = new RemoteWebDriver(gridUrl, capabilities);
            configureCommonSettings(driver, config);
            return driver;
        } catch (MalformedURLException e) {
            throw new HexConfigException("Invalid Grid URL", e);
        }
    }

    // --- Хелперы ---

    private static void applyDefaultChromeOptions(ChromeOptions options, UiConfig config) {
        if (config.headless()) options.addArguments("--headless=new");
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu");
        if (!config.browserVersion().isEmpty()) options.setBrowserVersion(config.browserVersion());
    }

    private static void applyCustomOptions(String browser, Object options) {
        if (optionsCustomizers.containsKey(browser)) {
            logger.debug("Применение кастомных настроек для {}", browser);
            optionsCustomizers.get(browser).accept(options);
        }
    }

    private static void configureCommonSettings(WebDriver driver, UiConfig config) {
        // Selenide сам управляет таймаутами, но если нужно жестко задать на уровне драйвера:
        // driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(config.implicitTimeout()));
        // driver.manage().window().setSize(...) - Selenide делает это через Configuration.browserSize
    }
}