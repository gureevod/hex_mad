package com.company.hex.api.service;

import com.company.hex.api.config.ApiConfig;
import com.company.hex.api.converter.JacksonResponseConverter;
import com.company.hex.api.converter.ResponseConverter;
import com.company.hex.api.executor.RequestExecutor;
import com.company.hex.api.executor.RestAssuredExecutor;
import com.company.hex.api.interceptor.Interceptor;
import com.company.hex.api.interceptor.InterceptorChain;
import com.company.hex.api.interceptor.LoggingInterceptor;
import com.company.hex.api.interceptor.RetryInterceptor;
import com.company.hex.api.interceptor.StatusValidationInterceptor;
import com.company.hex.api.processor.AnnotationProcessor;
import com.company.hex.api.proxy.ProxyHandler;
import com.company.hex.api.validation.InterfaceValidator;
import com.company.hex.core.config.HexConfigException;
import com.company.hex.core.logging.HexLoggerFactory;
import org.slf4j.Logger;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fluent builder для создания API сервисов с расширенной конфигурацией.
 * Позволяет гибко настраивать интерцепторы, конвертеры и исполнителей запросов.
 * 
 * Пример использования:
 * <pre>
 * {@code
 * UserApi userApi = ApiServiceFactory.builder()
 *     .withConfig(customConfig)
 *     .addInterceptorFirst(new AuthInterceptor(token))
 *     .addInterceptor(new MetricsInterceptor())
 *     .create(UserApi.class);
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 */
public final class ApiServiceFactoryBuilder {
    
    private static final Logger logger = HexLoggerFactory.getApiLogger(ApiServiceFactoryBuilder.class);
    
    private ApiConfig config;
    private final List<InterceptorEntry> interceptors = new ArrayList<>();
    private ResponseConverter responseConverter;
    private RequestExecutor requestExecutor;
    private boolean useDefaults = true;
    
    /**
     * Внутренний класс для хранения интерцептора и его позиции.
     */
    private static class InterceptorEntry {
        final Interceptor interceptor;
        final Position position;
        final Class<?> afterClass;
        
        enum Position {
            FIRST, NORMAL, AFTER
        }
        
        InterceptorEntry(Interceptor interceptor, Position position, Class<?> afterClass) {
            this.interceptor = interceptor;
            this.position = position;
            this.afterClass = afterClass;
        }
    }
    
    /**
     * Package-private конструктор. Используйте ApiServiceFactory.builder()
     */
    ApiServiceFactoryBuilder() {
    }
    
    /**
     * Устанавливает пользовательскую конфигурацию API.
     * 
     * @param config конфигурация API
     * @return этот builder для fluent API
     */
    public ApiServiceFactoryBuilder withConfig(ApiConfig config) {
        if (config == null) {
            throw new HexConfigException("API конфигурация не может быть null");
        }
        this.config = config;
        return this;
    }
    
    /**
     * Добавляет интерцептор в конец списка.
     * 
     * @param interceptor интерцептор для добавления
     * @return этот builder для fluent API
     */
    public ApiServiceFactoryBuilder addInterceptor(Interceptor interceptor) {
        if (interceptor == null) {
            throw new HexConfigException("Интерцептор не может быть null");
        }
        this.interceptors.add(new InterceptorEntry(interceptor, InterceptorEntry.Position.NORMAL, null));
        this.useDefaults = false;
        logger.debug("Добавлен интерцептор: {}", interceptor.getClass().getSimpleName());
        return this;
    }
    
    /**
     * Добавляет интерцептор в начало списка.
     * Полезно для интерцепторов, которые должны выполняться первыми (например, аутентификация).
     * 
     * @param interceptor интерцептор для добавления
     * @return этот builder для fluent API
     */
    public ApiServiceFactoryBuilder addInterceptorFirst(Interceptor interceptor) {
        if (interceptor == null) {
            throw new HexConfigException("Интерцептор не может быть null");
        }
        this.interceptors.add(new InterceptorEntry(interceptor, InterceptorEntry.Position.FIRST, null));
        this.useDefaults = false;
        logger.debug("Добавлен интерцептор в начало: {}", interceptor.getClass().getSimpleName());
        return this;
    }
    
    /**
     * Добавляет интерцептор после указанного класса интерцептора.
     * 
     * @param afterClass класс интерцептора, после которого нужно вставить
     * @param interceptor интерцептор для добавления
     * @return этот builder для fluent API
     */
    public ApiServiceFactoryBuilder addInterceptorAfter(Class<?> afterClass, Interceptor interceptor) {
        if (afterClass == null) {
            throw new HexConfigException("Класс afterClass не может быть null");
        }
        if (interceptor == null) {
            throw new HexConfigException("Интерцептор не может быть null");
        }
        this.interceptors.add(new InterceptorEntry(interceptor, InterceptorEntry.Position.AFTER, afterClass));
        this.useDefaults = false;
        logger.debug("Добавлен интерцептор после {}: {}", 
                    afterClass.getSimpleName(), interceptor.getClass().getSimpleName());
        return this;
    }
    
    /**
     * Устанавливает пользовательский конвертер ответов.
     * 
     * @param converter конвертер ответов
     * @return этот builder для fluent API
     */
    public ApiServiceFactoryBuilder withConverter(ResponseConverter converter) {
        if (converter == null) {
            throw new HexConfigException("Конвертер не может быть null");
        }
        this.responseConverter = converter;
        logger.debug("Установлен пользовательский конвертер: {}", converter.getClass().getSimpleName());
        return this;
    }
    
    /**
     * Устанавливает пользовательский исполнитель запросов.
     * 
     * @param executor исполнитель запросов
     * @return этот builder для fluent API
     */
    public ApiServiceFactoryBuilder withExecutor(RequestExecutor executor) {
        if (executor == null) {
            throw new HexConfigException("Исполнитель запросов не может быть null");
        }
        this.requestExecutor = executor;
        logger.debug("Установлен пользовательский исполнитель: {}", executor.getClass().getSimpleName());
        return this;
    }
    
    /**
     * Создает экземпляр API сервиса с настроенной конфигурацией.
     * 
     * @param <T> тип интерфейса сервиса
     * @param serviceInterface класс интерфейса сервиса
     * @return прокси-экземпляр сервиса
     */
    public <T> T create(Class<T> serviceInterface) {
        if (serviceInterface == null) {
            throw new HexConfigException("Интерфейс сервиса не может быть null");
        }
        
        if (!serviceInterface.isInterface()) {
            throw new HexConfigException("Класс должен быть интерфейсом: " + serviceInterface.getName());
        }
        
        // Валидируем интерфейс
        InterfaceValidator.validate(serviceInterface);
        
        logger.info("Создание декларативного API сервиса через builder: {}", serviceInterface.getSimpleName());
        
        // Создаем компоненты с пользовательскими или дефолтными значениями
        AnnotationProcessor annotationProcessor = new AnnotationProcessor();
        RequestExecutor executor = this.requestExecutor != null 
            ? this.requestExecutor 
            : createDefaultExecutor();
        ResponseConverter converter = this.responseConverter != null 
            ? this.responseConverter 
            : new JacksonResponseConverter();
        
        // Создаем цепочку интерцепторов
        InterceptorChain interceptorChain = buildInterceptorChain(executor);
        
        // Создаем прокси handler
        ProxyHandler handler = new ProxyHandler(
            serviceInterface,
            annotationProcessor,
            interceptorChain,
            converter
        );
        
        // Создаем и возвращаем прокси
        @SuppressWarnings("unchecked")
        T proxy = (T) Proxy.newProxyInstance(
            serviceInterface.getClassLoader(),
            new Class<?>[] { serviceInterface },
            handler
        );
        
        logger.info("Успешно создан декларативный API сервис через builder: {}", 
                   serviceInterface.getSimpleName());
        return proxy;
    }
    
    /**
     * Создает дефолтный исполнитель запросов.
     */
    private RequestExecutor createDefaultExecutor() {
        return this.config != null 
            ? new RestAssuredExecutor(this.config) 
            : new RestAssuredExecutor();
    }
    
    /**
     * Строит цепочку интерцепторов из настроенных интерцепторов.
     */
    private InterceptorChain buildInterceptorChain(RequestExecutor executor) {
        List<Interceptor> orderedInterceptors = new ArrayList<>();
        
        // Если используются дефолты, добавляем стандартные интерцепторы
        if (useDefaults) {
            orderedInterceptors.add(new RetryInterceptor());
            orderedInterceptors.add(new LoggingInterceptor());
            orderedInterceptors.add(new StatusValidationInterceptor());
            logger.debug("Добавлены дефолтные интерцепторы");
        } else {
            // Обрабатываем пользовательские интерцепторы с учетом порядка
            orderedInterceptors = orderInterceptors();
        }
        
        // Создаем цепочку
        InterceptorChain.Builder chainBuilder = new InterceptorChain.Builder();
        for (Interceptor interceptor : orderedInterceptors) {
            chainBuilder.addInterceptor(interceptor);
        }
        chainBuilder.requestExecutor(executor);
        
        return chainBuilder.build();
    }
    
    /**
     * Упорядочивает интерцепторы согласно указанным позициям.
     */
    private List<Interceptor> orderInterceptors() {
        List<Interceptor> result = new ArrayList<>();
        Map<Class<?>, Integer> classPositions = new LinkedHashMap<>();
        
        // Сначала добавляем все FIRST интерцепторы
        for (InterceptorEntry entry : interceptors) {
            if (entry.position == InterceptorEntry.Position.FIRST) {
                result.add(entry.interceptor);
                classPositions.put(entry.interceptor.getClass(), result.size() - 1);
            }
        }
        
        // Затем добавляем NORMAL интерцепторы
        for (InterceptorEntry entry : interceptors) {
            if (entry.position == InterceptorEntry.Position.NORMAL) {
                result.add(entry.interceptor);
                classPositions.put(entry.interceptor.getClass(), result.size() - 1);
            }
        }
        
        // Наконец, обрабатываем AFTER интерцепторы
        for (InterceptorEntry entry : interceptors) {
            if (entry.position == InterceptorEntry.Position.AFTER) {
                Integer afterIndex = classPositions.get(entry.afterClass);
                if (afterIndex != null) {
                    result.add(afterIndex + 1, entry.interceptor);
                    // Обновляем позиции для последующих интерцепторов
                    updatePositions(classPositions, afterIndex + 1);
                    classPositions.put(entry.interceptor.getClass(), afterIndex + 1);
                } else {
                    logger.warn("Класс {} не найден для addInterceptorAfter, добавляю в конец", 
                               entry.afterClass.getSimpleName());
                    result.add(entry.interceptor);
                    classPositions.put(entry.interceptor.getClass(), result.size() - 1);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Обновляет позиции в map после вставки интерцептора.
     */
    private void updatePositions(Map<Class<?>, Integer> positions, int fromIndex) {
        for (Map.Entry<Class<?>, Integer> entry : positions.entrySet()) {
            if (entry.getValue() >= fromIndex) {
                positions.put(entry.getKey(), entry.getValue() + 1);
            }
        }
    }
}