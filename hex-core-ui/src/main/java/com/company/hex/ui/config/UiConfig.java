package com.company.hex.ui.config;

import com.company.hex.core.config.BaseConfig;
import org.aeonbits.owner.Config;

/**
 * Интерфейс конфигурации для UI-тестирования.
 * Расширяет базовую конфигурацию специфичными для UI параметрами.
 * 
 * @author Hex Framework
 * @version 1.0
 */
@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources({
    "classpath:META-INF/hex-defaults.properties",
    "classpath:hex.properties",
    "classpath:hex-${hex.environment}.properties",
    "classpath:hex-${env}.properties",
    "system:env",
    "system:properties"
})
public interface UiConfig extends BaseConfig {

    /**
     * Получить тип браузера для тестирования.
     * 
     * @return тип браузера (chrome, firefox, edge, safari)
     */
    @Key("hex.ui.browser")
    @DefaultValue("chrome")
    String browser();

    /**
     * Проверить, запускать ли браузер в headless режиме.
     * 
     * @return true, если браузер должен работать в headless режиме
     */
    @Key("hex.ui.headless")
    @DefaultValue("false")
    boolean headless();

    /**
     * Получить ширину окна браузера в пикселях.
     * 
     * @return ширина окна
     */
    @Key("hex.ui.window.width")
    @DefaultValue("1920")
    int windowWidth();

    /**
     * Получить высоту окна браузера в пикселях.
     * 
     * @return высота окна
     */
    @Key("hex.ui.window.height")
    @DefaultValue("1080")
    int windowHeight();

    /**
     * Получить базовый URL для UI-тестирования.
     * 
     * @return базовый URL
     */
    @Key("hex.ui.base.url")
    @DefaultValue("http://localhost:3000")
    String baseUrl();

    /**
     * Получить таймаут для UI элементов в секундах.
     * 
     * @return таймаут в секундах
     */
    @Key("hex.ui.timeout")
    @DefaultValue("10")
    int uiTimeout();

    /**
     * Получить таймаут для загрузки страницы в секундах.
     * 
     * @return таймаут загрузки страницы
     */
    @Key("hex.ui.page.load.timeout")
    @DefaultValue("30")
    int pageLoadTimeout();

    /**
     * Получить неявный таймаут для поиска элементов в секундах.
     * 
     * @return неявный таймаут
     */
    @Key("hex.ui.implicit.timeout")
    @DefaultValue("5")
    int implicitTimeout();

    /**
     * Проверить, включено ли создание скриншотов при ошибках.
     * 
     * @return true, если скриншоты включены
     */
    @Key("hex.ui.screenshots.enabled")
    @DefaultValue("true")
    boolean screenshotsEnabled();

    /**
     * Получить папку для сохранения скриншотов.
     * 
     * @return путь к папке скриншотов
     */
    @Key("hex.ui.screenshots.folder")
    @DefaultValue("target/screenshots")
    String screenshotsFolder();

    /**
     * Проверить, включено ли создание видеозаписи тестов.
     * 
     * @return true, если видеозапись включена
     */
    @Key("hex.ui.video.enabled")
    @DefaultValue("false")
    boolean videoEnabled();

    /**
     * Получить папку для сохранения видеозаписей.
     * 
     * @return путь к папке видеозаписей
     */
    @Key("hex.ui.video.folder")
    @DefaultValue("target/videos")
    String videoFolder();

    /**
     * Получить версию браузера (если требуется конкретная).
     * 
     * @return версия браузера или пустая строка для последней версии
     */
    @Key("hex.ui.browser.version")
    @DefaultValue("")
    String browserVersion();

    /**
     * Получить URL Selenium Grid (если используется).
     * 
     * @return URL Selenium Grid или пустая строка для локального запуска
     */
    @Key("hex.ui.grid.url")
    @DefaultValue("")
    String gridUrl();

    /**
     * Проверить, включено ли логирование действий браузера.
     * 
     * @return true, если логирование включено
     */
    @Key("hex.ui.logging.enabled")
    @DefaultValue("true")
    boolean uiLoggingEnabled();

    /**
     * Получить уровень логирования для браузера.
     * 
     * @return уровень логирования (OFF, SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST, ALL)
     */
    @Key("hex.ui.logging.level")
    @DefaultValue("INFO")
    String uiLoggingLevel();

    /**
     * Проверить, включено ли выделение элементов при взаимодействии.
     * 
     * @return true, если выделение включено
     */
    @Key("hex.ui.highlight.enabled")
    @DefaultValue("false")
    boolean highlightEnabled();

    /**
     * Получить задержку между действиями в миллисекундах.
     * 
     * @return задержка между действиями
     */
    @Key("hex.ui.action.delay")
    @DefaultValue("0")
    long actionDelay();

    /**
     * Получить стратегию ожидания загрузки страницы.
     * 
     * @return стратегия загрузки (none, normal, eager)
     */
    @Key("hex.ui.page.load.strategy")
    @DefaultValue("normal")
    String pageLoadStrategy();
}