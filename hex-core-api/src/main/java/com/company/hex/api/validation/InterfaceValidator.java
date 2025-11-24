package com.company.hex.api.validation;

import com.company.hex.api.annotations.http.DELETE;
import com.company.hex.api.annotations.http.GET;
import com.company.hex.api.annotations.http.PATCH;
import com.company.hex.api.annotations.http.POST;
import com.company.hex.api.annotations.http.PUT;
import com.company.hex.api.annotations.param.Body;
import com.company.hex.api.annotations.param.Path;
import com.company.hex.core.config.HexConfigException;
import com.company.hex.core.logging.HexLoggerFactory;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Валидатор интерфейсов API сервисов.
 * Проверяет корректность аннотаций на методах перед созданием прокси.
 * 
 * @author Hex Framework
 * @version 1.0
 */
public final class InterfaceValidator {
    
    private static final Logger logger = HexLoggerFactory.getApiLogger(InterfaceValidator.class);
    private static final Pattern PATH_PARAM_PATTERN = Pattern.compile("\\{([^}]+)\\}");
    
    private InterfaceValidator() {
        throw new UnsupportedOperationException("Утилитный класс не может быть инстанцирован");
    }
    
    /**
     * Валидирует интерфейс API сервиса.
     * Проверяет все методы на корректность аннотаций.
     * 
     * @param serviceInterface интерфейс для валидации
     * @throws HexConfigException если найдены ошибки валидации
     */
    public static void validate(Class<?> serviceInterface) {
        if (serviceInterface == null) {
            throw new HexConfigException("Интерфейс сервиса не может быть null");
        }
        
        if (!serviceInterface.isInterface()) {
            throw new HexConfigException("Класс должен быть интерфейсом: " + serviceInterface.getName());
        }
        
        logger.debug("Валидация интерфейса API сервиса: {}", serviceInterface.getSimpleName());
        
        Method[] methods = serviceInterface.getDeclaredMethods();
        
        for (Method method : methods) {
            // Пропускаем методы Object
            if (method.getDeclaringClass() == Object.class) {
                continue;
            }
            
            validateHttpMethodAnnotation(method);
            validateParameterAnnotations(method);
            validateReturnType(method);
        }
        
        logger.debug("Валидация интерфейса {} успешно завершена", serviceInterface.getSimpleName());
    }
    
    /**
     * Проверяет наличие ровно одной HTTP аннотации на методе.
     */
    private static void validateHttpMethodAnnotation(Method method) {
        int httpAnnotationCount = 0;
        
        if (method.isAnnotationPresent(GET.class)) httpAnnotationCount++;
        if (method.isAnnotationPresent(POST.class)) httpAnnotationCount++;
        if (method.isAnnotationPresent(PUT.class)) httpAnnotationCount++;
        if (method.isAnnotationPresent(DELETE.class)) httpAnnotationCount++;
        if (method.isAnnotationPresent(PATCH.class)) httpAnnotationCount++;
        
        if (httpAnnotationCount == 0) {
            throw new HexConfigException(String.format(
                "Метод '%s.%s()' должен иметь ровно одну HTTP аннотацию (@GET, @POST, @PUT, @DELETE, @PATCH)",
                method.getDeclaringClass().getSimpleName(),
                method.getName()
            ));
        }
        
        if (httpAnnotationCount > 1) {
            throw new HexConfigException(String.format(
                "Метод '%s.%s()' имеет более одной HTTP аннотации. Допускается только одна.",
                method.getDeclaringClass().getSimpleName(),
                method.getName()
            ));
        }
    }
    
    /**
     * Проверяет корректность аннотаций параметров.
     */
    private static void validateParameterAnnotations(Method method) {
        // Проверка: @Body не используется с GET запросами
        if (method.isAnnotationPresent(GET.class)) {
            for (Parameter parameter : method.getParameters()) {
                if (parameter.isAnnotationPresent(Body.class)) {
                    throw new HexConfigException(String.format(
                        "Метод '%s.%s()' использует @Body с @GET. GET запросы не должны иметь тела запроса.",
                        method.getDeclaringClass().getSimpleName(),
                        method.getName()
                    ));
                }
            }
        }
        
        // Проверка: все @Path параметры присутствуют в пути
        String path = extractPath(method);
        if (path != null) {
            validatePathParameters(method, path);
        }
        
        // Проверка: максимум один @Body параметр
        int bodyCount = 0;
        for (Parameter parameter : method.getParameters()) {
            if (parameter.isAnnotationPresent(Body.class)) {
                bodyCount++;
            }
        }
        
        if (bodyCount > 1) {
            throw new HexConfigException(String.format(
                "Метод '%s.%s()' имеет более одного @Body параметра. Допускается только один.",
                method.getDeclaringClass().getSimpleName(),
                method.getName()
            ));
        }
    }
    
    /**
     * Извлекает путь из HTTP аннотации.
     */
    private static String extractPath(Method method) {
        if (method.isAnnotationPresent(GET.class)) {
            return method.getAnnotation(GET.class).value();
        } else if (method.isAnnotationPresent(POST.class)) {
            return method.getAnnotation(POST.class).value();
        } else if (method.isAnnotationPresent(PUT.class)) {
            return method.getAnnotation(PUT.class).value();
        } else if (method.isAnnotationPresent(DELETE.class)) {
            return method.getAnnotation(DELETE.class).value();
        } else if (method.isAnnotationPresent(PATCH.class)) {
            return method.getAnnotation(PATCH.class).value();
        }
        return null;
    }
    
    /**
     * Проверяет соответствие @Path параметров шаблону пути.
     */
    private static void validatePathParameters(Method method, String path) {
        // Извлекаем все плейсхолдеры из пути
        Set<String> pathPlaceholders = new HashSet<>();
        Matcher matcher = PATH_PARAM_PATTERN.matcher(path);
        while (matcher.find()) {
            pathPlaceholders.add(matcher.group(1));
        }
        
        // Извлекаем все @Path параметры из метода
        Set<String> pathParameters = new HashSet<>();
        for (Parameter parameter : method.getParameters()) {
            if (parameter.isAnnotationPresent(Path.class)) {
                Path pathAnnotation = parameter.getAnnotation(Path.class);
                pathParameters.add(pathAnnotation.value());
            }
        }
        
        // Проверяем, что все плейсхолдеры имеют соответствующие параметры
        for (String placeholder : pathPlaceholders) {
            if (!pathParameters.contains(placeholder)) {
                throw new HexConfigException(String.format(
                    "Метод '%s.%s()': путь содержит плейсхолдер '{%s}', но не найден соответствующий @Path(\"%s\") параметр",
                    method.getDeclaringClass().getSimpleName(),
                    method.getName(),
                    placeholder,
                    placeholder
                ));
            }
        }
        
        // Проверяем, что нет лишних @Path параметров
        for (String paramName : pathParameters) {
            if (!pathPlaceholders.contains(paramName)) {
                logger.warn("Метод '{}.{}()': найден @Path(\"{}\") параметр, но нет соответствующего плейсхолдера в пути '{}'",
                    method.getDeclaringClass().getSimpleName(),
                    method.getName(),
                    paramName,
                    path
                );
            }
        }
    }
    
    /**
     * Проверяет корректность типа возврата.
     */
    private static void validateReturnType(Method method) {
        Class<?> returnType = method.getReturnType();
        
        // В текущей реализации мы принимаем любой тип возврата
        // Можно добавить дополнительные проверки при необходимости
        logger.trace("Тип возврата метода '{}.{}()': {}",
            method.getDeclaringClass().getSimpleName(),
            method.getName(),
            returnType.getSimpleName()
        );
    }
}