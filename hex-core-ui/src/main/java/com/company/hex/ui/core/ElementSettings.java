package com.company.hex.ui.core;

import com.company.hex.ui.annotations.Element;
import com.company.hex.ui.annotations.Elements;
import com.company.hex.ui.config.UiConfig;

import java.time.Duration;

/**
 * Настройки для UI элемента (timeout, polling interval и т.д.).
 * Иммутабельный класс для потокобезопасности.
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public class ElementSettings {
    
    private final Duration timeout;
    private final Duration pollingInterval;
    
    /**
     * Создать настройки элемента.
     * 
     * @param timeout таймаут ожидания элемента
     * @param pollingInterval интервал polling при ожидании
     */
    public ElementSettings(Duration timeout, Duration pollingInterval) {
        if (timeout == null) {
            throw new IllegalArgumentException("Timeout не может быть null");
        }
        if (pollingInterval == null) {
            throw new IllegalArgumentException("Polling interval не может быть null");
        }
        
        this.timeout = timeout;
        this.pollingInterval = pollingInterval;
    }
    
    /**
     * Создать настройки из аннотации @Element.
     * 
     * @param annotation аннотация @Element
     * @param config конфигурация UI
     * @return настройки элемента
     */
    public static ElementSettings fromAnnotation(Element annotation, UiConfig config) {
        if (annotation == null) {
            throw new IllegalArgumentException("Аннотация не может быть null");
        }
        if (config == null) {
            throw new IllegalArgumentException("Конфигурация не может быть null");
        }
        
        // Если timeout > 0, используем из аннотации, иначе глобальное значение
        Duration timeout = annotation.timeout() > 0
            ? Duration.ofSeconds(annotation.timeout())
            : Duration.ofSeconds(config.uiTimeout());
        
        // Если pollingInterval > 0, используем из аннотации, иначе глобальное значение  
        Duration polling = annotation.pollingInterval() > 0
            ? Duration.ofMillis(annotation.pollingInterval())
            : Duration.ofMillis(config.pollingInterval());
        
        return new ElementSettings(timeout, polling);
    }
    
    /**
     * Создать настройки из аннотации @Elements.
     * 
     * @param annotation аннотация @Elements
     * @param config конфигурация UI
     * @return настройки элемента
     */
    public static ElementSettings fromAnnotation(Elements annotation, UiConfig config) {
        if (annotation == null) {
            throw new IllegalArgumentException("Аннотация не может быть null");
        }
        if (config == null) {
            throw new IllegalArgumentException("Конфигурация не может быть null");
        }
        
        // Если timeout > 0, используем из аннотации, иначе глобальное значение
        Duration timeout = annotation.timeout() > 0
            ? Duration.ofSeconds(annotation.timeout())
            : Duration.ofSeconds(config.uiTimeout());
        
        // Если pollingInterval > 0, используем из аннотации, иначе глобальное значение
        Duration polling = annotation.pollingInterval() > 0
            ? Duration.ofMillis(annotation.pollingInterval())
            : Duration.ofMillis(config.pollingInterval());
        
        return new ElementSettings(timeout, polling);
    }
    
    /**
     * Создать настройки по умолчанию из конфигурации.
     * 
     * @param config конфигурация UI
     * @return настройки элемента
     */
    public static ElementSettings fromConfig(UiConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Конфигурация не может быть null");
        }
        
        Duration timeout = Duration.ofSeconds(config.uiTimeout());
        Duration polling = Duration.ofMillis(config.pollingInterval());
        
        return new ElementSettings(timeout, polling);
    }
    
    /**
     * Получить таймаут ожидания элемента.
     * 
     * @return таймаут
     */
    public Duration getTimeout() {
        return timeout;
    }
    
    /**
     * Получить интервал polling.
     * 
     * @return интервал polling
     */
    public Duration getPollingInterval() {
        return pollingInterval;
    }
    
    /**
     * Создать новые настройки с измененным timeout.
     * 
     * @param newTimeout новый timeout
     * @return новые настройки
     */
    public ElementSettings withTimeout(Duration newTimeout) {
        return new ElementSettings(newTimeout, this.pollingInterval);
    }
    
    /**
     * Создать новые настройки с измененным polling interval.
     * 
     * @param newPollingInterval новый polling interval
     * @return новые настройки
     */
    public ElementSettings withPollingInterval(Duration newPollingInterval) {
        return new ElementSettings(this.timeout, newPollingInterval);
    }
    
    @Override
    public String toString() {
        return String.format("ElementSettings[timeout=%s, pollingInterval=%s]",
            timeout, pollingInterval);
    }
}