package com.company.hex.api.config;

import com.company.hex.core.config.BaseConfig;
import org.aeonbits.owner.Config;

/**
 * Интерфейс конфигурации для API-тестирования.
 * Расширяет базовую конфигурацию специфичными для API параметрами.
 * 
 * @author Hex Framework
 * @version 1.0
 */
@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources({
    "classpath:hex.properties",
    "classpath:hex-${env}.properties",
    "system:properties",
    "system:env"
})
public interface ApiConfig extends BaseConfig {

    /**
     * Получить базовый URL для API.
     * 
     * @return базовый URL API
     */
    @Key("hex.api.base.url")
    @DefaultValue("http://localhost:8080")
    String baseUrl();

    /**
     * Получить таймаут для API запросов в секундах.
     * 
     * @return таймаут в секундах
     */
    @Key("hex.api.timeout")
    @DefaultValue("30")
    int apiTimeout();

    /**
     * Получить количество попыток повтора для API запросов.
     * 
     * @return количество попыток повтора
     */
    @Key("hex.api.retry.count")
    @DefaultValue("3")
    int apiRetryCount();

    /**
     * Получить задержку между попытками повтора API запросов в миллисекундах.
     * 
     * @return задержка в миллисекундах
     */
    @Key("hex.api.retry.delay")
    @DefaultValue("1000")
    long apiRetryDelay();

    /**
     * Получить тип аутентификации.
     * 
     * @return тип аутентификации (none, basic, bearer, oauth2)
     */
    @Key("hex.api.auth.type")
    @DefaultValue("none")
    String authType();

    /**
     * Получить имя пользователя для базовой аутентификации.
     * 
     * @return имя пользователя
     */
    @Key("hex.api.auth.username")
    @DefaultValue("")
    String authUsername();

    /**
     * Получить пароль для базовой аутентификации.
     * 
     * @return пароль
     */
    @Key("hex.api.auth.password")
    @DefaultValue("")
    String authPassword();

    /**
     * Получить Bearer токен для аутентификации.
     * 
     * @return Bearer токен
     */
    @Key("hex.api.auth.bearer.token")
    @DefaultValue("")
    String bearerToken();

    /**
     * Получить URL для получения OAuth2 токена.
     * 
     * @return URL токена OAuth2
     */
    @Key("hex.api.auth.oauth2.token.url")
    @DefaultValue("")
    String oauth2TokenUrl();

    /**
     * Получить Client ID для OAuth2.
     * 
     * @return Client ID
     */
    @Key("hex.api.auth.oauth2.client.id")
    @DefaultValue("")
    String oauth2ClientId();

    /**
     * Получить Client Secret для OAuth2.
     * 
     * @return Client Secret
     */
    @Key("hex.api.auth.oauth2.client.secret")
    @DefaultValue("")
    String oauth2ClientSecret();

    /**
     * Получить Content-Type по умолчанию для запросов.
     * 
     * @return Content-Type
     */
    @Key("hex.api.content.type")
    @DefaultValue("application/json")
    String defaultContentType();

    /**
     * Получить Accept header по умолчанию для запросов.
     * 
     * @return Accept header
     */
    @Key("hex.api.accept")
    @DefaultValue("application/json")
    String defaultAccept();

    /**
     * Проверить, включено ли логирование запросов и ответов.
     * 
     * @return true, если логирование включено
     */
    @Key("hex.api.logging.enabled")
    @DefaultValue("true")
    boolean loggingEnabled();

    /**
     * Проверить, включена ли валидация SSL сертификатов.
     * 
     * @return true, если валидация SSL включена
     */
    @Key("hex.api.ssl.validation.enabled")
    @DefaultValue("true")
    boolean sslValidationEnabled();

    /**
     * Получить максимальный размер ответа в байтах.
     * 
     * @return максимальный размер ответа
     */
    @Key("hex.api.response.max.size")
    @DefaultValue("10485760")
    long maxResponseSize();
}