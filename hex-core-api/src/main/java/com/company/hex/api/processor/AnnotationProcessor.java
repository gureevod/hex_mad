package com.company.hex.api.processor;

import com.company.hex.api.annotations.config.ApiService;
import com.company.hex.api.annotations.config.Headers;
import com.company.hex.api.annotations.config.Retry;
import com.company.hex.api.annotations.config.ExpectedStatus;
import com.company.hex.api.annotations.http.DELETE;
import com.company.hex.api.annotations.http.GET;
import com.company.hex.api.annotations.http.PATCH;
import com.company.hex.api.annotations.http.POST;
import com.company.hex.api.annotations.http.PUT;
import com.company.hex.api.annotations.param.Body;
import com.company.hex.api.annotations.param.FormParam;
import com.company.hex.api.annotations.param.Header;
import com.company.hex.api.annotations.param.Path;
import com.company.hex.api.annotations.param.Query;
import com.company.hex.api.config.PropertyResolver;
import com.company.hex.api.model.RequestDefinition;
import com.company.hex.core.logging.HexLoggerFactory;
import org.slf4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Processes method annotations to extract HTTP request metadata.
 * Converts annotated interface methods into RequestDefinition objects.
 * 
 * @author Hex Framework
 * @version 1.0
 */
public class AnnotationProcessor {
    
    private static final Logger logger = HexLoggerFactory.getApiLogger(AnnotationProcessor.class);
    
    // Кэш для метаданных методов (потокобезопасный)
    private final ConcurrentHashMap<Method, MethodMetadata> metadataCache = new ConcurrentHashMap<>();
    
    /**
     * Внутренний класс для хранения кэшированных метаданных методов.
     * Содержит все статические данные, извлеченные из аннотаций.
     */
    private static class MethodMetadata {
        final String httpMethod;
        final String path;
        final List<ParameterMetadata> parameters;
        final boolean hasRetry;
        final Retry retryConfig;
        final int[] expectedStatuses;
        final Map<String, String> staticHeaders;
        
        MethodMetadata(String httpMethod, String path, List<ParameterMetadata> parameters,
                      boolean hasRetry, Retry retryConfig, int[] expectedStatuses,
                      Map<String, String> staticHeaders) {
            this.httpMethod = httpMethod;
            this.path = path;
            this.parameters = parameters;
            this.hasRetry = hasRetry;
            this.retryConfig = retryConfig;
            this.expectedStatuses = expectedStatuses;
            this.staticHeaders = staticHeaders;
        }
    }
    
    /**
     * Метаданные для параметра метода.
     */
    private static class ParameterMetadata {
        final int index;
        final ParameterType type;
        final String name;
        
        enum ParameterType {
            PATH, QUERY, HEADER, FORM_PARAM, BODY
        }
        
        ParameterMetadata(int index, ParameterType type, String name) {
            this.index = index;
            this.type = type;
            this.name = name;
        }
    }
    
    /**
     * Process a method and its arguments to create a RequestDefinition.
     * 
     * @param method the interface method being invoked
     * @param args the arguments passed to the method
     * @param serviceClass the service interface class
     * @return a RequestDefinition containing all request metadata
     */
    public RequestDefinition process(Method method, Object[] args, Class<?> serviceClass) {
        logger.debug("Обработка метода: {}.{}", serviceClass.getSimpleName(), method.getName());
        
        // Получаем или парсим метаданные метода (с кэшированием)
        MethodMetadata metadata = metadataCache.computeIfAbsent(method, this::parseMethodMetadata);
        
        // Строим RequestDefinition используя кэшированные метаданные и runtime аргументы
        RequestDefinition definition = buildRequestDefinition(metadata, method, args, serviceClass);
        
        logger.debug("Создан RequestDefinition: {}", definition);
        return definition;
    }
    
    /**
     * Парсит метаданные метода из аннотаций (вызывается один раз, результат кэшируется).
     *
     * @param method метод для парсинга
     * @return кэшированные метаданные
     */
    private MethodMetadata parseMethodMetadata(Method method) {
        logger.debug("Парсинг метаданных метода: {}", method.getName());
        
        // Извлекаем HTTP метод и путь
        String httpMethod = null;
        String path = null;
        
        if (method.isAnnotationPresent(GET.class)) {
            GET get = method.getAnnotation(GET.class);
            httpMethod = "GET";
            path = get.value();
        } else if (method.isAnnotationPresent(POST.class)) {
            POST post = method.getAnnotation(POST.class);
            httpMethod = "POST";
            path = post.value();
        } else if (method.isAnnotationPresent(PUT.class)) {
            PUT put = method.getAnnotation(PUT.class);
            httpMethod = "PUT";
            path = put.value();
        } else if (method.isAnnotationPresent(DELETE.class)) {
            DELETE delete = method.getAnnotation(DELETE.class);
            httpMethod = "DELETE";
            path = delete.value();
        } else if (method.isAnnotationPresent(PATCH.class)) {
            PATCH patch = method.getAnnotation(PATCH.class);
            httpMethod = "PATCH";
            path = patch.value();
        }
        
        // Извлекаем информацию о параметрах
        List<ParameterMetadata> parameterMetadata = new ArrayList<>();
        Parameter[] parameters = method.getParameters();
        
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            
            if (parameter.isAnnotationPresent(Path.class)) {
                Path pathAnnotation = parameter.getAnnotation(Path.class);
                parameterMetadata.add(new ParameterMetadata(i, ParameterMetadata.ParameterType.PATH, pathAnnotation.value()));
            } else if (parameter.isAnnotationPresent(Query.class)) {
                Query queryAnnotation = parameter.getAnnotation(Query.class);
                parameterMetadata.add(new ParameterMetadata(i, ParameterMetadata.ParameterType.QUERY, queryAnnotation.value()));
            } else if (parameter.isAnnotationPresent(Header.class)) {
                Header headerAnnotation = parameter.getAnnotation(Header.class);
                parameterMetadata.add(new ParameterMetadata(i, ParameterMetadata.ParameterType.HEADER, headerAnnotation.value()));
            } else if (parameter.isAnnotationPresent(FormParam.class)) {
                FormParam formParamAnnotation = parameter.getAnnotation(FormParam.class);
                parameterMetadata.add(new ParameterMetadata(i, ParameterMetadata.ParameterType.FORM_PARAM, formParamAnnotation.value()));
            } else if (parameter.isAnnotationPresent(Body.class)) {
                parameterMetadata.add(new ParameterMetadata(i, ParameterMetadata.ParameterType.BODY, null));
            }
        }
        
        // Извлекаем конфигурацию повторных попыток
        boolean hasRetry = method.isAnnotationPresent(Retry.class);
        Retry retryConfig = hasRetry ? method.getAnnotation(Retry.class) : null;
        
        // Извлекаем ожидаемые статусы
        int[] expectedStatuses = new int[0];
        if (method.isAnnotationPresent(ExpectedStatus.class)) {
            ExpectedStatus expectedStatus = method.getAnnotation(ExpectedStatus.class);
            expectedStatuses = expectedStatus.value();
        }
        
        // Извлекаем статические заголовки из @Headers аннотации
        Map<String, String> staticHeaders = processHeadersAnnotation(method);
        
        logger.debug("Метаданные метода {} кэшированы: HTTP={}, Path={}, Parameters={}, StaticHeaders={}",
                    method.getName(), httpMethod, path, parameterMetadata.size(), staticHeaders.size());
        
        return new MethodMetadata(httpMethod, path, parameterMetadata, hasRetry, retryConfig, expectedStatuses, staticHeaders);
    }
    
    /**
     * Строит RequestDefinition используя кэшированные метаданные и runtime аргументы.
     *
     * @param metadata кэшированные метаданные метода
     * @param method метод интерфейса
     * @param args аргументы вызова метода
     * @param serviceClass класс сервисного интерфейса
     * @return RequestDefinition для выполнения
     */
    private RequestDefinition buildRequestDefinition(MethodMetadata metadata, Method method,
                                                     Object[] args, Class<?> serviceClass) {
        RequestDefinition.Builder builder = RequestDefinition.builder();
        
        // Сохраняем ссылку на метод
        builder.method(method);
        
        // Устанавливаем HTTP метод и путь из кэша
        builder.httpMethod(metadata.httpMethod);
        builder.path(metadata.path);
        
        // Обрабатываем аннотацию на уровне сервиса
        processServiceAnnotation(serviceClass, builder);
        
        // Добавляем статические заголовки из метаданных
        for (Map.Entry<String, String> header : metadata.staticHeaders.entrySet()) {
            builder.addHeader(header.getKey(), header.getValue());
            logger.debug("Статический header: {} = {}", header.getKey(), header.getValue());
        }
        
        // Обрабатываем параметры используя кэшированные метаданные
        if (args != null && args.length > 0) {
            for (ParameterMetadata paramMetadata : metadata.parameters) {
                Object arg = args[paramMetadata.index];
                
                switch (paramMetadata.type) {
                    case PATH:
                        builder.addPathParam(paramMetadata.name, arg);
                        logger.debug("Path параметр: {} = {}", paramMetadata.name, arg);
                        break;
                    case QUERY:
                        builder.addQueryParam(paramMetadata.name, arg);
                        logger.debug("Query параметр: {} = {}", paramMetadata.name, arg);
                        break;
                    case HEADER:
                        builder.addHeader(paramMetadata.name, arg);
                        logger.debug("Header: {} = {}", paramMetadata.name, arg);
                        break;
                    case FORM_PARAM:
                        builder.addFormParam(paramMetadata.name, arg);
                        logger.debug("Form параметр: {} = {}", paramMetadata.name, arg);
                        break;
                    case BODY:
                        builder.body(arg);
                        logger.debug("Request body установлен: {}", arg != null ? arg.getClass().getSimpleName() : "null");
                        break;
                }
            }
        }
        
        return builder.build();
    }
    
    /**
     * Process the @ApiService annotation on the interface.
     */
    private void processServiceAnnotation(Class<?> serviceClass, RequestDefinition.Builder builder) {
        ApiService apiService = serviceClass.getAnnotation(ApiService.class);
        if (apiService != null) {
            String baseUrl = apiService.baseUrl();
            if (!baseUrl.isEmpty()) {
                // Разрешаем плейсхолдеры свойств
                baseUrl = PropertyResolver.resolve(baseUrl);
                builder.baseUrl(baseUrl);
                logger.debug("Установлен base URL из @ApiService: {}", baseUrl);
            }
            
            String basePath = apiService.basePath();
            if (!basePath.isEmpty()) {
                logger.debug("Base path from @ApiService: {}", basePath);
                // Base path will be prepended to method path
            }
        }
    }
    
    /**
     * Обрабатывает аннотацию @Headers и извлекает статические заголовки.
     *
     * @param method метод для обработки
     * @return Map заголовков (название -> значение)
     */
    private Map<String, String> processHeadersAnnotation(Method method) {
        Map<String, String> headers = new HashMap<>();
        
        if (!method.isAnnotationPresent(Headers.class)) {
            return headers;
        }
        
        Headers headersAnnotation = method.getAnnotation(Headers.class);
        String[] headerStrings = headersAnnotation.value();
        
        for (String headerString : headerStrings) {
            // Парсим формат "Header-Name: Header-Value"
            int colonIndex = headerString.indexOf(':');
            if (colonIndex == -1) {
                logger.warn("Неверный формат заголовка '{}' в методе {}. Ожидается 'Header-Name: Header-Value'",
                           headerString, method.getName());
                continue;
            }
            
            String headerName = headerString.substring(0, colonIndex).trim();
            String headerValue = headerString.substring(colonIndex + 1).trim();
            
            // Разрешаем плейсхолдеры в значении заголовка
            headerValue = PropertyResolver.resolve(headerValue);
            
            headers.put(headerName, headerValue);
            logger.debug("Извлечен статический заголовок из @Headers: {} = {}", headerName, headerValue);
        }
        
        return headers;
    }
}