package com.company.hex.api.service;

import com.company.hex.api.config.ApiConfig;
import com.company.hex.api.converter.JacksonResponseConverter;
import com.company.hex.api.converter.ResponseConverter;
import com.company.hex.api.executor.RequestExecutor;
import com.company.hex.api.executor.RestAssuredExecutor;
import com.company.hex.api.processor.AnnotationProcessor;
import com.company.hex.api.proxy.ProxyHandler;
import com.company.hex.core.config.HexConfigException;
import com.company.hex.core.config.HexConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

/**
 * Factory for creating API service instances.
 * Supports both declarative interface-based services and traditional BaseApiService implementations.
 *
 * @author Hex Framework
 * @version 1.0
 */
public final class ApiServiceFactory {

    private static final Logger logger = LoggerFactory.getLogger(ApiServiceFactory.class);

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private ApiServiceFactory() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Create a declarative API service from an annotated interface.
     * This is the primary method for creating type-safe, annotation-driven API clients.
     *
     * @param <T> the service interface type
     * @param serviceInterface the annotated interface class
     * @return a proxy implementation of the interface
     */
    public static <T> T create(Class<T> serviceInterface) {
        if (serviceInterface == null) {
            throw new HexConfigException("Service interface cannot be null");
        }
        
        if (!serviceInterface.isInterface()) {
            throw new HexConfigException("Service class must be an interface: " + serviceInterface.getName());
        }
        
        logger.info("Creating declarative API service: {}", serviceInterface.getSimpleName());
        
        // Create default components
        AnnotationProcessor annotationProcessor = new AnnotationProcessor();
        RequestExecutor requestExecutor = new RestAssuredExecutor();
        ResponseConverter responseConverter = new JacksonResponseConverter();
        
        // Create proxy handler
        ProxyHandler handler = new ProxyHandler(
            serviceInterface,
            annotationProcessor,
            requestExecutor,
            responseConverter
        );
        
        // Create and return proxy instance
        @SuppressWarnings("unchecked")
        T proxy = (T) Proxy.newProxyInstance(
            serviceInterface.getClassLoader(),
            new Class<?>[] { serviceInterface },
            handler
        );
        
        logger.info("Successfully created declarative API service: {}", serviceInterface.getSimpleName());
        return proxy;
    }
    
    /**
     * Create a declarative API service with custom configuration.
     *
     * @param <T> the service interface type
     * @param serviceInterface the annotated interface class
     * @param config custom API configuration
     * @return a proxy implementation of the interface
     */
    public static <T> T create(Class<T> serviceInterface, ApiConfig config) {
        if (serviceInterface == null) {
            throw new HexConfigException("Service interface cannot be null");
        }
        
        if (!serviceInterface.isInterface()) {
            throw new HexConfigException("Service class must be an interface: " + serviceInterface.getName());
        }
        
        if (config == null) {
            throw new HexConfigException("API configuration cannot be null");
        }
        
        logger.info("Creating declarative API service with custom config: {}", serviceInterface.getSimpleName());
        
        // Create components with custom config
        AnnotationProcessor annotationProcessor = new AnnotationProcessor();
        RequestExecutor requestExecutor = new RestAssuredExecutor(config);
        ResponseConverter responseConverter = new JacksonResponseConverter();
        
        // Create proxy handler
        ProxyHandler handler = new ProxyHandler(
            serviceInterface,
            annotationProcessor,
            requestExecutor,
            responseConverter
        );
        
        // Create and return proxy instance
        @SuppressWarnings("unchecked")
        T proxy = (T) Proxy.newProxyInstance(
            serviceInterface.getClassLoader(),
            new Class<?>[] { serviceInterface },
            handler
        );
        
        logger.info("Successfully created declarative API service with custom config: {}", serviceInterface.getSimpleName());
        return proxy;
    }

    /**
     * Создать экземпляр API сервиса указанного типа с конфигурацией по умолчанию.
     * Каждый вызов возвращает новый экземпляр для обеспечения потокобезопасности.
     * 
     * @param <T> тип API сервиса, расширяющий BaseApiService
     * @param serviceClass класс API сервиса
     * @return новый экземпляр API сервиса
     * @throws HexConfigException если не удалось создать экземпляр сервиса
     */
    public static <T extends BaseApiService> T createLegacy(Class<T> serviceClass) {
        if (serviceClass == null) {
            throw new HexConfigException("Класс API сервиса не может быть null");
        }

        try {
            logger.debug("Создание экземпляра API сервиса: {}", serviceClass.getName());
            
            // Попытка создать экземпляр через конструктор по умолчанию
            Constructor<T> defaultConstructor = serviceClass.getDeclaredConstructor();
            defaultConstructor.setAccessible(true);
            T service = defaultConstructor.newInstance();
            
            logger.info("Успешно создан экземпляр API сервиса: {}", serviceClass.getSimpleName());
            return service;
            
        } catch (NoSuchMethodException e) {
            String errorMessage = String.format("Класс %s должен иметь конструктор по умолчанию", serviceClass.getName());
            logger.error(errorMessage, e);
            throw new HexConfigException(errorMessage, e);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            String errorMessage = String.format("Ошибка при создании экземпляра API сервиса %s", serviceClass.getName());
            logger.error(errorMessage, e);
            throw new HexConfigException(errorMessage, e);
        }
    }

    /**
     * Создать экземпляр API сервиса указанного типа с пользовательской конфигурацией.
     * Каждый вызов возвращает новый экземпляр для обеспечения потокобезопасности.
     * 
     * @param <T> тип API сервиса, расширяющий BaseApiService
     * @param serviceClass класс API сервиса
     * @param config пользовательская конфигурация API
     * @return новый экземпляр API сервиса
     * @throws HexConfigException если не удалось создать экземпляр сервиса
     */
    public static <T extends BaseApiService> T createLegacy(Class<T> serviceClass, ApiConfig config) {
        if (serviceClass == null) {
            throw new HexConfigException("Класс API сервиса не может быть null");
        }
        
        if (config == null) {
            throw new HexConfigException("Конфигурация API не может быть null");
        }

        try {
            logger.debug("Создание экземпляра API сервиса с пользовательской конфигурацией: {}", serviceClass.getName());
            
            // Попытка создать экземпляр через конструктор с ApiConfig
            Constructor<T> configConstructor = serviceClass.getDeclaredConstructor(ApiConfig.class);
            configConstructor.setAccessible(true);
            T service = configConstructor.newInstance(config);
            
            logger.info("Успешно создан экземпляр API сервиса с пользовательской конфигурацией: {}", serviceClass.getSimpleName());
            return service;
            
        } catch (NoSuchMethodException e) {
            // Если конструктор с ApiConfig не найден, пытаемся использовать конструктор по умолчанию
            logger.debug("Конструктор с ApiConfig не найден, используется конструктор по умолчанию для: {}", serviceClass.getName());
            return createLegacy(serviceClass);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            String errorMessage = String.format("Ошибка при создании экземпляра API сервиса %s с пользовательской конфигурацией", serviceClass.getName());
            logger.error(errorMessage, e);
            throw new HexConfigException(errorMessage, e);
        }
    }

    /**
     * Создать экземпляр API сервиса с конфигурацией, загруженной из указанного класса конфигурации.
     * 
     * @param <T> тип API сервиса, расширяющий BaseApiService
     * @param <C> тип конфигурации, расширяющий ApiConfig
     * @param serviceClass класс API сервиса
     * @param configClass класс конфигурации
     * @return новый экземпляр API сервиса
     * @throws HexConfigException если не удалось создать экземпляр сервиса или загрузить конфигурацию
     */
    public static <T extends BaseApiService, C extends ApiConfig> T createLegacy(Class<T> serviceClass, Class<C> configClass) {
        if (serviceClass == null) {
            throw new HexConfigException("Класс API сервиса не может быть null");
        }
        
        if (configClass == null) {
            throw new HexConfigException("Класс конфигурации не может быть null");
        }

        try {
            logger.debug("Загрузка конфигурации {} для API сервиса {}", configClass.getName(), serviceClass.getName());
            
            C config = HexConfigFactory.getConfig(configClass);
            return createLegacy(serviceClass, config);
            
        } catch (Exception e) {
            String errorMessage = String.format("Ошибка при создании API сервиса %s с конфигурацией %s", 
                serviceClass.getName(), configClass.getName());
            logger.error(errorMessage, e);
            throw new HexConfigException(errorMessage, e);
        }
    }

    /**
     * Создать экземпляр API сервиса с базовой конфигурацией API.
     * Удобный метод для быстрого создания сервиса с стандартной конфигурацией.
     * 
     * @param <T> тип API сервиса, расширяющий BaseApiService
     * @param serviceClass класс API сервиса
     * @return новый экземпляр API сервиса с базовой конфигурацией
     * @throws HexConfigException если не удалось создать экземпляр сервиса
     */
    public static <T extends BaseApiService> T createWithDefaultConfig(Class<T> serviceClass) {
        return createLegacy(serviceClass, ApiConfig.class);
    }

    /**
     * Проверить, может ли фабрика создать экземпляр указанного класса сервиса.
     * 
     * @param serviceClass класс API сервиса для проверки
     * @return true, если сервис может быть создан
     */
    public static boolean canCreate(Class<? extends BaseApiService> serviceClass) {
        if (serviceClass == null) {
            return false;
        }

        try {
            // Проверяем наличие конструктора по умолчанию
            serviceClass.getDeclaredConstructor();
            return true;
        } catch (NoSuchMethodException e) {
            try {
                // Проверяем наличие конструктора с ApiConfig
                serviceClass.getDeclaredConstructor(ApiConfig.class);
                return true;
            } catch (NoSuchMethodException ex) {
                logger.debug("Класс {} не имеет подходящих конструкторов", serviceClass.getName());
                return false;
            }
        }
    }
}