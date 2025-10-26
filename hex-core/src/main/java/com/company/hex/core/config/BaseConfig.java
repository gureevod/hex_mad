package com.company.hex.core.config;

import org.aeonbits.owner.Config;

/**
 * Базовый интерфейс конфигурации для всех модулей фреймворка Hex.
 * Содержит общие свойства, используемые во всех профилях окружения.
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
public interface BaseConfig extends Config {

    /**
     * Получить текущий профиль окружения.
     * 
     * @return профиль окружения (local, ci)
     */
    @Key("hex.environment")
    @DefaultValue("local")
    String environment();

    /**
     * Получить уровень логирования.
     * 
     * @return уровень логирования
     */
    @Key("hex.logging.level")
    @DefaultValue("INFO")
    String loggingLevel();

    /**
     * Получить таймаут по умолчанию в секундах.
     * 
     * @return таймаут в секундах
     */
    @Key("hex.timeout.default")
    @DefaultValue("30")
    int defaultTimeout();

    /**
     * Проверить, включен ли режим отладки.
     * 
     * @return true, если режим отладки включен
     */
    @Key("hex.debug.enabled")
    @DefaultValue("false")
    boolean debugEnabled();

    /**
     * Получить количество попыток повтора по умолчанию.
     * 
     * @return количество попыток повтора
     */
    @Key("hex.retry.count")
    @DefaultValue("3")
    int retryCount();

    /**
     * Получить задержку между попытками повтора в миллисекундах.
     * 
     * @return задержка в миллисекундах
     */
    @Key("hex.retry.delay")
    @DefaultValue("1000")
    long retryDelay();

    /**
     * Проверить, включена ли параллельная обработка.
     * 
     * @return true, если параллельная обработка включена
     */
    @Key("hex.parallel.enabled")
    @DefaultValue("true")
    boolean parallelEnabled();

    /**
     * Получить количество потоков для параллельной обработки.
     * 
     * @return количество потоков
     */
    @Key("hex.parallel.threads")
    @DefaultValue("4")
    int parallelThreads();
}