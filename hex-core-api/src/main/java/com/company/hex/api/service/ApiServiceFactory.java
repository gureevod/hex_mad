package com.company.hex.api.service;

import com.company.hex.api.config.ApiConfig;
import com.company.hex.core.config.HexConfigException;
import com.company.hex.core.config.HexConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Фабрика для создания экземпляров API сервисов.
 * Обеспечивает простой и ясный механизм создания сложных объектов без статических изменяемых синглтонов.
 * 
 * @author Hex Framework
 * @version 1.0
 */
public final class ApiServiceFactory {

    private static final Logger logger = LoggerFactory.getLogger(ApiServiceFactory.class);

    /**
     * Приватный конструктор для предотвращения создания экземпляров утилитного класса.
     */
    private ApiServiceFactory() {
        throw new UnsupportedOperationException("Утилитный класс не может быть инстанцирован");
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
    public static <T extends BaseApiService> T create(Class<T> serviceClass) {
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
    public static <T extends BaseApiService> T create(Class<T> serviceClass, ApiConfig config) {
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
            return create(serviceClass);
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
    public static <T extends BaseApiService, C extends ApiConfig> T create(Class<T> serviceClass, Class<C> configClass) {
        if (serviceClass == null) {
            throw new HexConfigException("Класс API сервиса не может быть null");
        }
        
        if (configClass == null) {
            throw new HexConfigException("Класс конфигурации не может быть null");
        }

        try {
            logger.debug("Загрузка конфигурации {} для API сервиса {}", configClass.getName(), serviceClass.getName());
            
            C config = HexConfigFactory.getConfig(configClass);
            return create(serviceClass, config);
            
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
        return create(serviceClass, ApiConfig.class);
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