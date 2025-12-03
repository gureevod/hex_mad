package com.company.hex.db.service;

import com.company.hex.db.annotations.config.DbService;
import com.company.hex.db.annotations.config.ReadOnly;
import com.company.hex.db.annotations.config.Timeout;
import com.company.hex.db.annotations.config.Transactional;
import com.company.hex.db.annotations.mapping.RowMapping;
import com.company.hex.db.annotations.param.NullableParam;
import com.company.hex.db.annotations.param.Param;
import com.company.hex.db.annotations.param.ParamList;
import com.company.hex.db.annotations.query.Call;
import com.company.hex.db.annotations.query.DangerousQuery;
import com.company.hex.db.annotations.query.Delete;
import com.company.hex.db.annotations.query.Insert;
import com.company.hex.db.annotations.query.ReturnGeneratedKeys;
import com.company.hex.db.annotations.query.Script;
import com.company.hex.db.annotations.query.Select;
import com.company.hex.db.annotations.query.Update;
import com.company.hex.db.exception.DbException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Процессор для парсинга аннотаций методов репозитория.
 *
 * <p>Извлекает метаданные из аннотаций методов интерфейса и создаёт
 * {@link QueryDefinition} объекты для последующего выполнения запросов.</p>
 *
 * <h2>Обрабатываемые аннотации</h2>
 * <ul>
 *     <li>Запросы: {@code @Select}, {@code @Insert}, {@code @Update}, {@code @Delete}, {@code @Call}, {@code @Script}</li>
 *     <li>Параметры: {@code @Param}, {@code @ParamList}, {@code @NullableParam}</li>
 *     <li>Конфигурация: {@code @DbService}, {@code @Timeout}, {@code @ReadOnly}, {@code @Transactional}</li>
 *     <li>Маппинг: {@code @RowMapping}, {@code @ReturnGeneratedKeys}</li>
 *     <li>Безопасность: {@code @DangerousQuery}</li>
 * </ul>
 *
 * <h2>Кэширование</h2>
 * <p>Процессор кэширует метаданные методов для повторного использования.
 * Параметры запроса извлекаются при каждом вызове.</p>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see QueryDefinition
 */
public final class QueryProcessor {

    /**
     * Кэш метаданных методов (без значений параметров).
     */
    private final ConcurrentHashMap<Method, MethodMetadata> metadataCache = new ConcurrentHashMap<>();

    /**
     * Обрабатывает метод и создаёт QueryDefinition.
     *
     * @param method метод репозитория
     * @param args аргументы вызова метода
     * @param serviceInterface интерфейс репозитория
     * @return QueryDefinition с метаданными запроса
     * @throws DbException если метод не имеет корректных аннотаций запроса
     */
    public QueryDefinition process(Method method, Object[] args, Class<?> serviceInterface) {
        // Получаем или создаём метаданные метода
        MethodMetadata metadata = metadataCache.computeIfAbsent(method,
            m -> extractMethodMetadata(m, serviceInterface));

        // Извлекаем значения параметров из аргументов
        Map<String, Object> parameters = extractParameterValues(method, args);

        // Создаём QueryDefinition
        return QueryDefinition.builder()
            .sql(metadata.sql)
            .parameters(parameters)
            .declaredParameters(metadata.declaredParameters)
            .returnType(method.getGenericReturnType())
            .returnClass(extractReturnClass(method.getGenericReturnType()))
            .methodName(method.getName())
            .method(method)
            .queryType(metadata.queryType)
            .returnGeneratedKeys(metadata.returnGeneratedKeys)
            .dangerousAllowed(metadata.dangerousAllowed)
            .dangerousReason(metadata.dangerousReason)
            .timeout(metadata.timeout)
            .readOnly(metadata.readOnly)
            .transactional(metadata.transactional)
            .rowMapperClass(metadata.rowMapperClass)
            .dataSource(metadata.dataSource)
            .build();
    }

    /**
     * Извлекает метаданные метода из аннотаций.
     */
    private MethodMetadata extractMethodMetadata(Method method, Class<?> serviceInterface) {
        MethodMetadata metadata = new MethodMetadata();

        // Извлекаем тип запроса и SQL
        extractQueryInfo(method, metadata);

        // Извлекаем имена объявленных параметров
        metadata.declaredParameters = extractDeclaredParameters(method);

        // Извлекаем дополнительные аннотации
        extractReturnGeneratedKeys(method, metadata);
        extractDangerousQuery(method, metadata);
        extractTimeout(method, serviceInterface, metadata);
        extractReadOnly(method, serviceInterface, metadata);
        extractTransactional(method, serviceInterface, metadata);
        extractRowMapping(method, metadata);
        extractDataSource(serviceInterface, metadata);

        return metadata;
    }

    /**
     * Извлекает тип запроса и SQL из аннотаций.
     */
    private void extractQueryInfo(Method method, MethodMetadata metadata) {
        Select select = method.getAnnotation(Select.class);
        if (select != null) {
            metadata.sql = select.value();
            metadata.queryType = QueryDefinition.QueryType.SELECT;
            return;
        }

        Insert insert = method.getAnnotation(Insert.class);
        if (insert != null) {
            metadata.sql = insert.value();
            metadata.queryType = QueryDefinition.QueryType.INSERT;
            return;
        }

        Update update = method.getAnnotation(Update.class);
        if (update != null) {
            metadata.sql = update.value();
            metadata.queryType = QueryDefinition.QueryType.UPDATE;
            return;
        }

        Delete delete = method.getAnnotation(Delete.class);
        if (delete != null) {
            metadata.sql = delete.value();
            metadata.queryType = QueryDefinition.QueryType.DELETE;
            return;
        }

        Call call = method.getAnnotation(Call.class);
        if (call != null) {
            metadata.sql = call.value();
            metadata.queryType = QueryDefinition.QueryType.CALL;
            return;
        }

        Script script = method.getAnnotation(Script.class);
        if (script != null) {
            metadata.sql = script.resource();
            metadata.queryType = QueryDefinition.QueryType.SCRIPT;
            return;
        }

        throw new DbException(
            "Method " + method.getName() + " must have one of @Select, @Insert, @Update, @Delete, @Call, or @Script annotation",
            method.getName(), null, null, null
        );
    }

    /**
     * Извлекает имена объявленных параметров из аннотаций.
     */
    private Set<String> extractDeclaredParameters(Method method) {
        Set<String> declaredParams = new HashSet<>();
        Parameter[] parameters = method.getParameters();

        for (Parameter param : parameters) {
            Param paramAnnotation = param.getAnnotation(Param.class);
            if (paramAnnotation != null) {
                declaredParams.add(paramAnnotation.value());
            }

            ParamList paramListAnnotation = param.getAnnotation(ParamList.class);
            if (paramListAnnotation != null) {
                declaredParams.add(paramListAnnotation.value());
            }

            NullableParam nullableParamAnnotation = param.getAnnotation(NullableParam.class);
            if (nullableParamAnnotation != null) {
                declaredParams.add(nullableParamAnnotation.value());
            }
        }

        return declaredParams;
    }

    /**
     * Извлекает значения параметров из аргументов вызова.
     */
    private Map<String, Object> extractParameterValues(Method method, Object[] args) {
        Map<String, Object> parameters = new LinkedHashMap<>();
        
        if (args == null || args.length == 0) {
            return parameters;
        }

        Parameter[] methodParams = method.getParameters();

        for (int i = 0; i < methodParams.length && i < args.length; i++) {
            Parameter param = methodParams[i];
            Object value = args[i];

            // Проверяем @Param
            Param paramAnnotation = param.getAnnotation(Param.class);
            if (paramAnnotation != null) {
                parameters.put(paramAnnotation.value(), value);
                continue;
            }

            // Проверяем @ParamList
            ParamList paramListAnnotation = param.getAnnotation(ParamList.class);
            if (paramListAnnotation != null) {
                parameters.put(paramListAnnotation.value(), value);
                continue;
            }

            // Проверяем @NullableParam
            NullableParam nullableParamAnnotation = param.getAnnotation(NullableParam.class);
            if (nullableParamAnnotation != null) {
                parameters.put(nullableParamAnnotation.value(), value);
            }
        }

        return parameters;
    }

    /**
     * Извлекает аннотацию @ReturnGeneratedKeys.
     */
    private void extractReturnGeneratedKeys(Method method, MethodMetadata metadata) {
        ReturnGeneratedKeys annotation = method.getAnnotation(ReturnGeneratedKeys.class);
        metadata.returnGeneratedKeys = annotation != null;
    }

    /**
     * Извлекает аннотацию @DangerousQuery.
     */
    private void extractDangerousQuery(Method method, MethodMetadata metadata) {
        DangerousQuery annotation = method.getAnnotation(DangerousQuery.class);
        if (annotation != null) {
            metadata.dangerousAllowed = true;
            metadata.dangerousReason = annotation.reason();
        }
    }

    /**
     * Извлекает таймаут из @Timeout или @DbService.
     */
    private void extractTimeout(Method method, Class<?> serviceInterface, MethodMetadata metadata) {
        // Сначала проверяем аннотацию на методе
        Timeout methodTimeout = method.getAnnotation(Timeout.class);
        if (methodTimeout != null) {
            metadata.timeout = convertToSeconds(methodTimeout.value(), methodTimeout.unit());
            return;
        }

        // Затем проверяем аннотацию на интерфейсе
        Timeout classTimeout = serviceInterface.getAnnotation(Timeout.class);
        if (classTimeout != null) {
            metadata.timeout = convertToSeconds(classTimeout.value(), classTimeout.unit());
            return;
        }

        // Используем значение из @DbService
        DbService dbService = serviceInterface.getAnnotation(DbService.class);
        if (dbService != null && dbService.queryTimeout() > 0) {
            metadata.timeout = dbService.queryTimeout();
        }
    }

    /**
     * Конвертирует значение таймаута в секунды.
     */
    private int convertToSeconds(int value, Timeout.TimeUnit unit) {
        switch (unit) {
            case MILLISECONDS:
                return Math.max(1, value / 1000);
            case MINUTES:
                return value * 60;
            case SECONDS:
            default:
                return value;
        }
    }

    /**
     * Извлекает флаг @ReadOnly.
     */
    private void extractReadOnly(Method method, Class<?> serviceInterface, MethodMetadata metadata) {
        ReadOnly methodReadOnly = method.getAnnotation(ReadOnly.class);
        if (methodReadOnly != null) {
            metadata.readOnly = true;
            return;
        }

        ReadOnly classReadOnly = serviceInterface.getAnnotation(ReadOnly.class);
        if (classReadOnly != null) {
            metadata.readOnly = true;
        }
    }

    /**
     * Извлекает флаг @Transactional.
     */
    private void extractTransactional(Method method, Class<?> serviceInterface, MethodMetadata metadata) {
        Transactional methodTransactional = method.getAnnotation(Transactional.class);
        if (methodTransactional != null) {
            metadata.transactional = true;
            if (methodTransactional.readOnly()) {
                metadata.readOnly = true;
            }
            return;
        }

        Transactional classTransactional = serviceInterface.getAnnotation(Transactional.class);
        if (classTransactional != null) {
            metadata.transactional = true;
            if (classTransactional.readOnly()) {
                metadata.readOnly = true;
            }
        }
    }

    /**
     * Извлекает @RowMapping.
     */
    private void extractRowMapping(Method method, MethodMetadata metadata) {
        RowMapping rowMapping = method.getAnnotation(RowMapping.class);
        if (rowMapping != null) {
            metadata.rowMapperClass = rowMapping.value();
        }
    }

    /**
     * Извлекает имя DataSource из @DbService.
     */
    private void extractDataSource(Class<?> serviceInterface, MethodMetadata metadata) {
        DbService dbService = serviceInterface.getAnnotation(DbService.class);
        if (dbService != null) {
            metadata.dataSource = dbService.dataSource();
        } else {
            metadata.dataSource = "primary";
        }
    }

    /**
     * Извлекает класс возвращаемого значения из generic типа.
     */
    private Class<?> extractReturnClass(Type returnType) {
        if (returnType instanceof Class) {
            return (Class<?>) returnType;
        }

        if (returnType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) returnType;
            Type rawType = parameterizedType.getRawType();
            
            if (rawType instanceof Class) {
                Class<?> rawClass = (Class<?>) rawType;
                
                // Для Optional, List, Set и т.д. возвращаем внутренний тип
                if (Optional.class.isAssignableFrom(rawClass)
                    || Iterable.class.isAssignableFrom(rawClass)) {
                    Type[] typeArgs = parameterizedType.getActualTypeArguments();
                    if (typeArgs.length > 0 && typeArgs[0] instanceof Class) {
                        return (Class<?>) typeArgs[0];
                    }
                }
                
                return rawClass;
            }
        }

        return Object.class;
    }

    /**
     * Очищает кэш метаданных. Используется для тестирования.
     */
    public void clearCache() {
        metadataCache.clear();
    }

    /**
     * Внутренний класс для хранения метаданных метода.
     */
    private static class MethodMetadata {
        String sql;
        QueryDefinition.QueryType queryType;
        Set<String> declaredParameters;
        boolean returnGeneratedKeys;
        boolean dangerousAllowed;
        String dangerousReason;
        Integer timeout;
        boolean readOnly;
        boolean transactional;
        Class<?> rowMapperClass;
        String dataSource;
    }
}
