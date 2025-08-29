package com.company.hex.core.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Фабрика для создания и управления экземплярами конфигурации.
 * Обеспечивает потокобезопасное создание неизменяемых эксемпляров конфигурации.
 * 
 * @author Hex Framework
 * @version 1.0
 */
public final class HexConfigFactory {

    private static final Logger logger = LoggerFactory.getLogger(HexConfigFactory.class);
    
    /**
     * Кэш для хранения созданных экземпляров конфигурации.
     * Обеспечивает потокобезопасность и предотвращает повторное создание.
     */
    private static final ConcurrentMap<Class<?>, Config> configCache = new ConcurrentHashMap<>();

    /**
     * Приватный конструктор для предотвращения создания экземпляров утилитного класса.
     */
    private HexConfigFactory() {
        throw new UnsupportedOperationException("Утилитный класс не может быть инстанцирован");
    }

    /**
     * Получить экземпляр конфигурации указанного типа.
     * Метод потокобезопасен и возвращает неизменяемый снимок конфигурации.
     * 
     * @param <T> тип конфигурации, расширяющий Config
     * @param configClass класс конфигурации
     * @return экземпляр конфигурации
     * @throws HexConfigException если не удалось создать или валидировать конфигурацию
     */
    @SuppressWarnings("unchecked")
    public static <T extends Config> T getConfig(Class<T> configClass) {
        if (configClass == null) {
            throw new HexConfigException("Класс конфигурации не может быть null");
        }

        try {
            return (T) configCache.computeIfAbsent(configClass, clazz -> {
                logger.debug("Создание нового экземпляра конфигурации для класса: {}", clazz.getName());
                
                T config = ConfigFactory.create(configClass);
                validateConfig(config);
                
                logger.info("Конфигурация успешно создана и валидирована для класса: {}", clazz.getName());
                return config;
            });
        } catch (Exception e) {
            String errorMessage = String.format("Ошибка при создании конфигурации для класса %s", configClass.getName());
            logger.error(errorMessage, e);
            throw new HexConfigException(errorMessage, e);
        }
    }

    /**
     * Создать новый экземпляр конфигурации без кэширования.
     * Используется когда необходимо получить свежий снимок конфигурации.
     * 
     * @param <T> тип конфигурации, расширяющий Config
     * @param configClass класс конфигурации
     * @return новый экземпляр конфигурации
     * @throws HexConfigException если не удалось создать или валидировать конфигурацию
     */
    public static <T extends Config> T createFreshConfig(Class<T> configClass) {
        if (configClass == null) {
            throw new HexConfigException("Класс конфигурации не может быть null");
        }

        try {
            logger.debug("Создание свежего экземпляра конфигурации для класса: {}", configClass.getName());
            
            T config = ConfigFactory.create(configClass);
            validateConfig(config);
            
            logger.info("Свежая конфигурация успешно создана и валидирована для класса: {}", configClass.getName());
            return config;
        } catch (Exception e) {
            String errorMessage = String.format("Ошибка при создании свежей конфигурации для класса %s", configClass.getName());
            logger.error(errorMessage, e);
            throw new HexConfigException(errorMessage, e);
        }
    }

    /**
     * Очистить кэш конфигураций.
     * Используется для принудительного обновления всех кэшированных конфигураций.
     */
    public static void clearCache() {
        logger.info("Очистка кэша конфигураций");
        configCache.clear();
    }

    /**
     * Очистить конфигурацию определенного типа из кэша.
     * 
     * @param configClass класс конфигурации для удаления из кэша
     */
    public static void clearCache(Class<?> configClass) {
        if (configClass != null) {
            logger.info("Очистка кэша для конфигурации класса: {}", configClass.getName());
            configCache.remove(configClass);
        }
    }

    /**
     * Валидировать конфигурацию на корректность.
     * Проверяет основные параметры и выбрасывает исключение при обнаружении проблем.
     * 
     * @param config экземпляр конфигурации для валидации
     * @throws HexConfigException если конфигурация содержит некорректные значения
     */
    private static void validateConfig(Config config) {
        if (config == null) {
            throw new HexConfigException("Конфигурация не может быть null");
        }

        // Валидация базовой конфигурации
        if (config instanceof BaseConfig) {
            BaseConfig baseConfig = (BaseConfig) config;
            
            // Проверка таймаутов
            if (baseConfig.defaultTimeout() <= 0) {
                throw new HexConfigException("Таймаут по умолчанию должен быть положительным числом");
            }
            
            // Проверка количества попыток повтора
            if (baseConfig.retryCount() < 0) {
                throw new HexConfigException("Количество попыток повтора не может быть отрицательным");
            }
            
            // Проверка задержки между попытками
            if (baseConfig.retryDelay() < 0) {
                throw new HexConfigException("Задержка между попытками не может быть отрицательной");
            }
            
            // Проверка количества потоков
            if (baseConfig.parallelThreads() <= 0) {
                throw new HexConfigException("Количество потоков должно быть положительным числом");
            }
            
            logger.debug("Базовая конфигурация успешно валидирована");
        }
    }

    /**
     * Получить размер кэша конфигураций.
     * 
     * @return количество кэшированных конфигураций
     */
    public static int getCacheSize() {
        return configCache.size();
    }
}