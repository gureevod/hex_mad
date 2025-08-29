package com.company.hex.api.service;

import com.company.hex.api.config.ApiConfig;
import com.company.hex.core.config.HexConfigFactory;
import com.company.hex.core.logging.HexLoggerFactory;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.config.SSLConfig;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;

/**
 * Базовый абстрактный класс для всех API сервисов.
 * Инкапсулирует настройку RestAssured RequestSpecification и общую логику.
 * 
 * @author Hex Framework
 * @version 1.0
 */
public abstract class BaseApiService {

    protected final Logger logger = HexLoggerFactory.getApiLogger(this.getClass());
    protected final ApiConfig config;
    protected final RequestSpecification requestSpec;

    /**
     * Конструктор базового API сервиса.
     * Инициализирует конфигурацию и настраивает RequestSpecification.
     */
    protected BaseApiService() {
        this.config = HexConfigFactory.getConfig(ApiConfig.class);
        this.requestSpec = createRequestSpecification();
        logger.info("🌐 Инициализирован API сервис: {} [BaseURL: {}]",
                   this.getClass().getSimpleName(), config.baseUrl());
    }

    /**
     * Конструктор с пользовательской конфигурацией.
     * 
     * @param config пользовательская конфигурация API
     */
    protected BaseApiService(ApiConfig config) {
        this.config = config;
        this.requestSpec = createRequestSpecification();
        logger.info("🌐 Инициализирован API сервис с пользовательской конфигурацией: {} [BaseURL: {}]",
                   this.getClass().getSimpleName(), config.baseUrl());
    }

    /**
     * Создать и настроить RequestSpecification.
     * 
     * @return настроенная спецификация запроса
     */
    private RequestSpecification createRequestSpecification() {
        RequestSpecification spec = RestAssured.given()
            .baseUri(config.baseUrl())
            .contentType(config.defaultContentType())
            .accept(config.defaultAccept())
            .config(createRestAssuredConfig());

        // Настройка аутентификации
        configureAuthentication(spec);

        // Настройка логирования и Allure интеграции
        if (config.loggingEnabled()) {
            spec.filter(new RequestLoggingFilter())
                .filter(new ResponseLoggingFilter());
        }
        
        // Добавляем Allure фильтр для автоматического прикрепления запросов/ответов
        spec.filter(new AllureRestAssured());

        // Настройка таймаутов
        spec.config(RestAssuredConfig.config()
            .httpClient(io.restassured.config.HttpClientConfig.httpClientConfig()
                .setParam("http.connection.timeout", config.apiTimeout() * 1000)
                .setParam("http.socket.timeout", config.apiTimeout() * 1000)));

        logger.debug("🔧 RequestSpecification настроена для базового URL: {} [ContentType: {}, Accept: {}]",
                    config.baseUrl(), config.defaultContentType(), config.defaultAccept());
        return spec;
    }

    /**
     * Создать конфигурацию RestAssured.
     * 
     * @return конфигурация RestAssured
     */
    private RestAssuredConfig createRestAssuredConfig() {
        RestAssuredConfig config = RestAssuredConfig.config();

        // Настройка SSL
        if (!this.config.sslValidationEnabled()) {
            config = config.sslConfig(SSLConfig.sslConfig().relaxedHTTPSValidation());
            logger.debug("🔒 SSL валидация отключена");
        }

        return config;
    }

    /**
     * Настроить аутентификацию для RequestSpecification.
     * 
     * @param spec спецификация запроса для настройки
     */
    private void configureAuthentication(RequestSpecification spec) {
        String authType = config.authType().toLowerCase();
        
        switch (authType) {
            case "basic":
                if (!config.authUsername().isEmpty() && !config.authPassword().isEmpty()) {
                    spec.auth().basic(config.authUsername(), config.authPassword());
                    logger.debug("🔐 Настроена базовая аутентификация для пользователя: {}", config.authUsername());
                }
                break;
                
            case "bearer":
                if (!config.bearerToken().isEmpty()) {
                    spec.header("Authorization", "Bearer " + config.bearerToken());
                    logger.debug("🔐 Настроена Bearer аутентификация");
                }
                break;
                
            case "oauth2":
                // OAuth2 токен должен быть получен отдельно и передан как Bearer
                if (!config.bearerToken().isEmpty()) {
                    spec.header("Authorization", "Bearer " + config.bearerToken());
                    logger.debug("🔐 Настроена OAuth2 аутентификация");
                }
                break;
                
            case "none":
            default:
                logger.debug("🔐 Аутентификация не настроена");
                break;
        }
    }

    /**
     * Получить настроенную спецификацию запроса.
     * 
     * @return RequestSpecification
     */
    public RequestSpecification getRequestSpec() {
        return requestSpec;
    }

    /**
     * Получить конфигурацию API.
     * 
     * @return конфигурация API
     */
    public ApiConfig getConfig() {
        return config;
    }

    /**
     * Создать новую спецификацию запроса на основе базовой.
     * Полезно для создания запросов с дополнительными параметрами.
     * 
     * @return новая RequestSpecification
     */
    protected RequestSpecification newRequest() {
        return RestAssured.given().spec(requestSpec);
    }

    /**
     * Выполнить запрос с повторными попытками при неудаче.
     * 
     * @param requestAction действие для выполнения запроса
     * @param <T> тип возвращаемого результата
     * @return результат выполнения запроса
     */
    protected <T> T executeWithRetry(java.util.function.Supplier<T> requestAction) {
        int attempts = 0;
        int maxAttempts = config.apiRetryCount();
        long delay = config.apiRetryDelay();
        
        while (attempts < maxAttempts) {
            try {
                T result = requestAction.get();
                if (attempts > 0) {
                    logger.info("✅ Запрос успешно выполнен с попытки {}", attempts + 1);
                }
                return result;
            } catch (Exception e) {
                attempts++;
                if (attempts >= maxAttempts) {
                    logger.error("❌ Запрос не удался после {} попыток: {}", maxAttempts, e.getMessage(), e);
                    throw e;
                }
                
                logger.warn("⚠️ Попытка {} не удалась, повтор через {} мс: {}", attempts, delay, e.getMessage());
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Прерван во время ожидания повтора", ie);
                }
            }
        }
        
        throw new RuntimeException("Неожиданное завершение цикла повторов");
    }

    /**
     * Получить имя сервиса для логирования.
     * 
     * @return имя сервиса
     */
    protected String getServiceName() {
        return this.getClass().getSimpleName();
    }
}