package com.company.hex.api.config;

import com.company.hex.core.config.HexConfigFactory;
import com.company.hex.core.logging.HexLoggerFactory;
import org.slf4j.Logger;

/**
 * Утилита для разрешения плейсхолдеров свойств в значениях конфигурации.
 * Поддерживает множественные источники и значения по умолчанию.
 * 
 * Порядок разрешения:
 * 1. System properties (System.getProperty())
 * 2. HexConfigFactory (Owner-based конфигурация)
 * 3. Environment variables (System.getenv())
 * 4. Значение по умолчанию (если указано через :)
 * 
 * Поддерживаемые форматы:
 * - ${property.name} - без значения по умолчанию
 * - ${property.name:defaultValue} - со значением по умолчанию
 * 
 * @author Hex Framework
 * @version 1.0
 */
public final class PropertyResolver {
    
    private static final Logger logger = HexLoggerFactory.getApiLogger(PropertyResolver.class);
    
    private PropertyResolver() {
        throw new UnsupportedOperationException("Утилитный класс не может быть инстанцирован");
    }
    
    /**
     * Разрешает плейсхолдер свойства используя все доступные источники.
     * 
     * @param placeholder строка, возможно содержащая плейсхолдер ${...}
     * @return разрешенное значение или исходная строка, если плейсхолдер не найден
     */
    public static String resolve(String placeholder) {
        if (placeholder == null || placeholder.isEmpty()) {
            return placeholder;
        }
        
        // Проверяем, является ли это плейсхолдером
        if (!placeholder.startsWith("${") || !placeholder.endsWith("}")) {
            return placeholder;
        }
        
        // Извлекаем имя свойства и значение по умолчанию
        String content = placeholder.substring(2, placeholder.length() - 1);
        String propertyName;
        String defaultValue = null;
        
        int colonIndex = content.indexOf(':');
        if (colonIndex != -1) {
            propertyName = content.substring(0, colonIndex).trim();
            defaultValue = content.substring(colonIndex + 1).trim();
        } else {
            propertyName = content.trim();
        }
        
        return resolve(propertyName, defaultValue);
    }
    
    /**
     * Разрешает свойство по имени с возможным значением по умолчанию.
     * 
     * @param propertyName имя свойства для поиска
     * @param defaultValue значение по умолчанию (может быть null)
     * @return разрешенное значение или значение по умолчанию
     */
    public static String resolve(String propertyName, String defaultValue) {
        if (propertyName == null || propertyName.isEmpty()) {
            logger.warn("Имя свойства пустое или null");
            return defaultValue;
        }
        
        logger.debug("Разрешение свойства: {}", propertyName);
        
        // 1. Проверяем System properties
        String value = System.getProperty(propertyName);
        if (value != null) {
            logger.debug("Свойство '{}' найдено в System properties: {}", propertyName, value);
            return value;
        }
        
        // 2. Проверяем HexConfigFactory (Owner-based config)
        try {
            ApiConfig apiConfig = HexConfigFactory.getConfig(ApiConfig.class);
            value = resolveFromApiConfig(apiConfig, propertyName);
            if (value != null) {
                logger.debug("Свойство '{}' найдено в HexConfigFactory: {}", propertyName, value);
                return value;
            }
        } catch (Exception e) {
            logger.trace("Не удалось получить свойство из HexConfigFactory: {}", e.getMessage());
        }
        
        // 3. Проверяем Environment variables
        value = System.getenv(propertyName);
        if (value != null) {
            logger.debug("Свойство '{}' найдено в Environment variables: {}", propertyName, value);
            return value;
        }
        
        // Также проверяем с замененными точками на подчеркивания (стандарт для env vars)
        String envVarName = propertyName.replace('.', '_').toUpperCase();
        value = System.getenv(envVarName);
        if (value != null) {
            logger.debug("Свойство '{}' найдено в Environment variables как {}: {}", 
                        propertyName, envVarName, value);
            return value;
        }
        
        // 4. Возвращаем значение по умолчанию
        if (defaultValue != null) {
            logger.debug("Свойство '{}' не найдено, используется значение по умолчанию: {}", 
                        propertyName, defaultValue);
            return defaultValue;
        }
        
        logger.warn("Свойство '{}' не найдено ни в одном источнике и нет значения по умолчанию", 
                   propertyName);
        return null;
    }
    
    /**
     * Пытается разрешить свойство из ApiConfig.
     * Маппинг популярных свойств на методы конфигурации.
     */
    private static String resolveFromApiConfig(ApiConfig config, String propertyName) {
        if (config == null) {
            return null;
        }
        
        // Маппинг известных свойств
        switch (propertyName) {
            case "api.base.url":
            case "api.baseUrl":
            case "baseUrl":
                return config.baseUrl();
                
            case "api.timeout":
            case "api.apiTimeout":
            case "apiTimeout":
                return String.valueOf(config.apiTimeout());
                
            case "api.contentType":
            case "api.defaultContentType":
            case "defaultContentType":
                return config.defaultContentType();
                
            case "api.accept":
            case "api.defaultAccept":
            case "defaultAccept":
                return config.defaultAccept();
                
            case "api.ssl.validation":
            case "api.sslValidationEnabled":
            case "sslValidationEnabled":
                return String.valueOf(config.sslValidationEnabled());
                
            default:
                // Свойство не найдено в известных маппингах
                return null;
        }
    }
    
    /**
     * Разрешает все плейсхолдеры в строке.
     * Поддерживает несколько плейсхолдеров в одной строке.
     * 
     * @param input строка с возможными плейсхолдерами
     * @return строка с разрешенными плейсхолдерами
     */
    public static String resolveAll(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        String result = input;
        int startIndex = 0;
        
        while (true) {
            int placeholderStart = result.indexOf("${", startIndex);
            if (placeholderStart == -1) {
                break;
            }
            
            int placeholderEnd = result.indexOf("}", placeholderStart);
            if (placeholderEnd == -1) {
                break;
            }
            
            String placeholder = result.substring(placeholderStart, placeholderEnd + 1);
            String resolved = resolve(placeholder);
            
            if (resolved != null && !resolved.equals(placeholder)) {
                result = result.substring(0, placeholderStart) + resolved + result.substring(placeholderEnd + 1);
                startIndex = placeholderStart + resolved.length();
            } else {
                startIndex = placeholderEnd + 1;
            }
        }
        
        return result;
    }
}