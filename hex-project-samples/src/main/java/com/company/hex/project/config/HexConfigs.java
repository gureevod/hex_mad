package com.company.hex.project.config;

import com.company.hex.api.config.ApiConfig;
import com.company.hex.core.config.BaseConfig;
import com.company.hex.ui.config.UiConfig;
import org.aeonbits.owner.ConfigFactory;

import java.util.Map;

/**
 * Фасад для удобного доступа к конфигурациям фреймворка Hex.
 * Скрывает детали работы с Owner и предоставляет простой API для получения конфигов.
 * Поддерживает кэширование и точечные переопределения параметров.
 * 
 * Это пример реализации для проекта. Каждый проект может создать свой собственный
 * фасад с нужной функциональностью.
 * 
 * @author Hex Framework
 * @version 1.0
 */
public final class HexConfigs {
    
    private static volatile UiConfig UI;
    private static volatile ApiConfig API;
    private static volatile BaseConfig BASE;

    /**
     * Приватный конструктор для предотвращения создания экземпляров.
     */
    private HexConfigs() {
        throw new UnsupportedOperationException("Утилитный класс не может быть инстанцирован");
    }

    /**
     * Получить глобальный экземпляр базовой конфигурации.
     * Использует ленивую инициализацию с двойной проверкой блокировки.
     * 
     * @return экземпляр BaseConfig
     */
    public static BaseConfig base() {
        if (BASE == null) {
            synchronized (HexConfigs.class) {
                if (BASE == null) {
                    BASE = ConfigFactory.create(BaseConfig.class);
                }
            }
        }
        return BASE;
    }

    /**
     * Получить глобальный экземпляр UI конфигурации.
     * Использует ленивую инициализацию с двойной проверкой блокировки.
     * 
     * @return экземпляр UiConfig
     */
    public static UiConfig ui() {
        if (UI == null) {
            synchronized (HexConfigs.class) {
                if (UI == null) {
                    UI = ConfigFactory.create(UiConfig.class);
                }
            }
        }
        return UI;
    }

    /**
     * Получить глобальный экземпляр API конфигурации.
     * Использует ленивую инициализацию с двойной проверкой блокировки.
     * 
     * @return экземпляр ApiConfig
     */
    public static ApiConfig api() {
        if (API == null) {
            synchronized (HexConfigs.class) {
                if (API == null) {
                    API = ConfigFactory.create(ApiConfig.class);
                }
            }
        }
        return API;
    }

    /**
     * Создать новый экземпляр базовой конфигурации с точечными переопределениями.
     * Полезно для тестов, которым нужны специфичные настройки без влияния на глобальную конфигурацию.
     * 
     * @param overrides карты с переопределениями параметров (ключ-значение)
     * @return новый экземпляр BaseConfig с применёнными переопределениями
     */
    @SafeVarargs
    public static BaseConfig base(Map<?, ?>... overrides) {
        return ConfigFactory.create(BaseConfig.class, overrides);
    }

    /**
     * Создать новый экземпляр UI конфигурации с точечными переопределениями.
     * Полезно для тестов, которым нужны специфичные настройки без влияния на глобальную конфигурацию.
     * 
     * Пример использования:
     * <pre>
     * UiConfig config = HexConfigs.ui(Map.of("hex.ui.headless", "true"));
     * </pre>
     * 
     * @param overrides карты с переопределениями параметров (ключ-значение)
     * @return новый экземпляр UiConfig с применёнными переопределениями
     */
    @SafeVarargs
    public static UiConfig ui(Map<?, ?>... overrides) {
        return ConfigFactory.create(UiConfig.class, overrides);
    }

    /**
     * Создать новый экземпляр API конфигурации с точечными переопределениями.
     * Полезно для тестов, которым нужны специфичные настройки без влияния на глобальную конфигурацию.
     * 
     * Пример использования:
     * <pre>
     * ApiConfig config = HexConfigs.api(Map.of("hex.api.timeout", "60"));
     * </pre>
     * 
     * @param overrides карты с переопределениями параметров (ключ-значение)
     * @return новый экземпляр ApiConfig с применёнными переопределениями
     */
    @SafeVarargs
    public static ApiConfig api(Map<?, ?>... overrides) {
        return ConfigFactory.create(ApiConfig.class, overrides);
    }

    /**
     * Установить профиль окружения для всех последующих конфигураций.
     * Должен вызываться до первого обращения к конфигурациям.
     * 
     * Пример использования:
     * <pre>
     * HexConfigs.useProfile("ci");
     * </pre>
     * 
     * @param profile название профиля (local, ci, qa, prod и т.д.)
     */
    public static void useProfile(String profile) {
        System.setProperty("hex.environment", profile);
    }

    /**
     * Сбросить кэшированные конфигурации.
     * Полезно в тестах для принудительной перезагрузки конфигураций.
     */
    public static void reset() {
        synchronized (HexConfigs.class) {
            UI = null;
            API = null;
            BASE = null;
        }
    }
}