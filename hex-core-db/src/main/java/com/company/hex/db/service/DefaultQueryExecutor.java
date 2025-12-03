package com.company.hex.db.service;

import com.company.hex.db.connection.ConnectionProvider;
import com.company.hex.db.exception.ConstraintViolationException;
import com.company.hex.db.exception.ForeignKeyException;
import com.company.hex.db.exception.MappingException;
import com.company.hex.db.exception.NotNullException;
import com.company.hex.db.exception.QueryExecutionException;
import com.company.hex.db.exception.QueryTimeoutException;
import com.company.hex.db.exception.UniqueConstraintException;
import com.company.hex.db.interceptor.ExecutionContext;
import com.company.hex.db.mapping.RowMapper;
import com.company.hex.db.service.NamedParameterProcessor.ProcessedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Реализация {@link QueryExecutor} для выполнения SQL запросов через JDBC.
 *
 * <p>Отвечает за:</p>
 * <ul>
 *     <li>Преобразование именованных параметров в позиционные</li>
 *     <li>Выполнение PreparedStatement</li>
 *     <li>Обработку generated keys</li>
 *     <li>Маппинг ResultSet на Java объекты</li>
 *     <li>Корректное освобождение ресурсов</li>
 * </ul>
 *
 * <h2>Пример использования</h2>
 * <pre>{@code
 * ConnectionProvider provider = ConnectionProvider.getInstance();
 * DefaultQueryExecutor executor = new DefaultQueryExecutor(provider);
 *
 * QueryDefinition query = QueryDefinition.builder()
 *     .sql("SELECT * FROM users WHERE id = :id")
 *     .parameter("id", 42L)
 *     .returnType(UserEntity.class)
 *     .methodName("findById")
 *     .queryType(QueryType.SELECT)
 *     .build();
 *
 * ExecutionContext context = new ExecutionContext();
 * Object result = executor.execute(query, context);
 * }</pre>
 *
 * <h2>Потокобезопасность</h2>
 * <p>Экземпляр класса потокобезопасен. Каждый вызов метода использует
 * свой Statement и ResultSet.</p>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see QueryExecutor
 * @see QueryDefinition
 */
public final class DefaultQueryExecutor implements QueryExecutor {

    private static final Logger logger = LoggerFactory.getLogger(DefaultQueryExecutor.class);

    /**
     * Провайдер соединений.
     */
    private final ConnectionProvider connectionProvider;

    /**
     * Процессор именованных параметров.
     */
    private final NamedParameterProcessor parameterProcessor;

    /**
     * Создаёт executor с указанным провайдером соединений.
     *
     * @param connectionProvider провайдер соединений
     */
    public DefaultQueryExecutor(ConnectionProvider connectionProvider) {
        this.connectionProvider = Objects.requireNonNull(connectionProvider,
            "ConnectionProvider cannot be null");
        this.parameterProcessor = new NamedParameterProcessor();
    }

    @Override
    public Object execute(QueryDefinition query, ExecutionContext context) {
        Objects.requireNonNull(query, "QueryDefinition cannot be null");
        Objects.requireNonNull(context, "ExecutionContext cannot be null");

        long startTime = System.currentTimeMillis();

        try {
            return switch (query.getQueryType()) {
                case SELECT -> executeSelect(query, context, startTime);
                case INSERT -> executeInsert(query, context, startTime);
                case UPDATE, DELETE -> executeUpdate(query, context, startTime);
                case CALL -> executeCall(query, context, startTime);
                case SCRIPT -> executeScript(query, context, startTime);
            };
        } catch (QueryExecutionException e) {
            throw e;
        } catch (SQLException e) {
            throw translateException(e, query);
        } catch (Exception e) {
            throw new QueryExecutionException(
                "Unexpected error executing query: " + e.getMessage(),
                query.getMethodName(),
                query.getSql(),
                query.getParameters(),
                e
            );
        }
    }

    /**
     * Выполняет SELECT запрос.
     */
    private Object executeSelect(QueryDefinition query, ExecutionContext context,
                                   long startTime) throws SQLException {
        ProcessedQuery processed = parameterProcessor.process(
            query.getSql(), query.getParameters());

        String dataSourceName = query.getDataSource();
        Connection connection = getConnection(context, dataSourceName);

        try (PreparedStatement stmt = connection.prepareStatement(processed.getSql())) {
            configureStatement(stmt, query, context);
            setParameters(stmt, processed.getPositionalParameters());

            logger.debug("Executing SELECT: {} with params: {}",
                processed.getSql(), processed.getPositionalParameters());

            try (ResultSet rs = stmt.executeQuery()) {
                long executionTime = System.currentTimeMillis() - startTime;
                Object result = mapResultSet(rs, query);

                QueryResult queryResult = buildSelectResult(result, executionTime);
                return queryResult.convertToReturnType(
                    query.getReturnType(), query.getReturnClass());
            }
        } finally {
            releaseConnectionIfNeeded(connection, context, dataSourceName);
        }
    }

    /**
     * Выполняет INSERT запрос.
     */
    private Object executeInsert(QueryDefinition query, ExecutionContext context,
                                   long startTime) throws SQLException {
        ProcessedQuery processed = parameterProcessor.process(
            query.getSql(), query.getParameters());

        String dataSourceName = query.getDataSource();
        Connection connection = getConnection(context, dataSourceName);

        int statementType = query.isReturnGeneratedKeys()
            ? Statement.RETURN_GENERATED_KEYS
            : Statement.NO_GENERATED_KEYS;

        try (PreparedStatement stmt = connection.prepareStatement(
                processed.getSql(), statementType)) {

            configureStatement(stmt, query, context);
            setParameters(stmt, processed.getPositionalParameters());

            logger.debug("Executing INSERT: {} with params: {}",
                processed.getSql(), processed.getPositionalParameters());

            int affectedRows = stmt.executeUpdate();
            long executionTime = System.currentTimeMillis() - startTime;

            // Получаем сгенерированные ключи
            List<Object> generatedKeys = new ArrayList<>();
            if (query.isReturnGeneratedKeys()) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    while (rs.next()) {
                        generatedKeys.add(rs.getObject(1));
                    }
                }
            }

            QueryResult result = QueryResult.builder()
                .value(generatedKeys.isEmpty() ? affectedRows : generatedKeys.get(0))
                .affectedRows(affectedRows)
                .executionTimeMs(executionTime)
                .generatedKeys(generatedKeys)
                .build();

            return result.convertToReturnType(
                query.getReturnType(), query.getReturnClass());

        } finally {
            releaseConnectionIfNeeded(connection, context, dataSourceName);
        }
    }

    /**
     * Выполняет UPDATE или DELETE запрос.
     */
    private Object executeUpdate(QueryDefinition query, ExecutionContext context,
                                   long startTime) throws SQLException {
        ProcessedQuery processed = parameterProcessor.process(
            query.getSql(), query.getParameters());

        String dataSourceName = query.getDataSource();
        Connection connection = getConnection(context, dataSourceName);

        try (PreparedStatement stmt = connection.prepareStatement(processed.getSql())) {
            configureStatement(stmt, query, context);
            setParameters(stmt, processed.getPositionalParameters());

            logger.debug("Executing {}: {} with params: {}",
                query.getQueryType(), processed.getSql(), processed.getPositionalParameters());

            int affectedRows = stmt.executeUpdate();
            long executionTime = System.currentTimeMillis() - startTime;

            QueryResult result = QueryResult.ofAffectedRows(affectedRows, executionTime);
            return result.convertToReturnType(
                query.getReturnType(), query.getReturnClass());

        } finally {
            releaseConnectionIfNeeded(connection, context, dataSourceName);
        }
    }

    /**
     * Выполняет вызов хранимой процедуры.
     */
    private Object executeCall(QueryDefinition query, ExecutionContext context,
                                long startTime) throws SQLException {
        // TODO: Реализация в Story 6.14
        throw new UnsupportedOperationException(
            "Stored procedure calls will be implemented in Story 6.14");
    }

    /**
     * Выполняет SQL скрипт.
     */
    private Object executeScript(QueryDefinition query, ExecutionContext context,
                                  long startTime) throws SQLException {
        // TODO: Реализация в Story 6.14
        throw new UnsupportedOperationException(
            "SQL script execution will be implemented in Story 6.14");
    }

    /**
     * Выполняет batch операцию.
     *
     * @param query определение запроса
     * @param parametersList список наборов параметров
     * @param context контекст выполнения
     * @return массив с количеством затронутых строк для каждой операции
     */
    public int[] executeBatch(QueryDefinition query, List<?> parametersList,
                               ExecutionContext context) {
        Objects.requireNonNull(query, "QueryDefinition cannot be null");
        Objects.requireNonNull(parametersList, "Parameters list cannot be null");

        if (parametersList.isEmpty()) {
            return new int[0];
        }

        String dataSourceName = query.getDataSource();
        Connection connection = getConnection(context, dataSourceName);

        // Для batch используем SQL без обработки параметров
        // Параметры будут добавляться отдельно для каждого набора
        String sql = query.getSql();
        ProcessedQuery template = parameterProcessor.process(sql, query.getParameters());

        try (PreparedStatement stmt = connection.prepareStatement(template.getSql())) {
            configureStatement(stmt, query, context);

            for (Object params : parametersList) {
                @SuppressWarnings("unchecked")
                List<Object> positional = params instanceof List
                    ? (List<Object>) params
                    : List.of(params);

                setParameters(stmt, positional);
                stmt.addBatch();
            }

            logger.debug("Executing batch of {} operations", parametersList.size());

            return stmt.executeBatch();

        } catch (SQLException e) {
            throw translateException(e, query);
        } finally {
            releaseConnectionIfNeeded(connection, context, dataSourceName);
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Получает соединение из контекста или провайдера.
     */
    private Connection getConnection(ExecutionContext context, String dataSourceName) {
        // Сначала проверяем контекст (транзакционное соединение)
        Connection conn = context.getConnection();
        if (conn != null) {
            return conn;
        }

        // Получаем из провайдера
        return connectionProvider.getConnection(dataSourceName);
    }

    /**
     * Освобождает соединение, если оно не транзакционное.
     */
    private void releaseConnectionIfNeeded(Connection connection,
                                            ExecutionContext context,
                                            String dataSourceName) {
        // Не освобождаем транзакционное соединение
        if (context.isTransactionActive()) {
            return;
        }

        connectionProvider.releaseConnection(connection, dataSourceName);
    }

    /**
     * Конфигурирует PreparedStatement.
     */
    private void configureStatement(PreparedStatement stmt, QueryDefinition query,
                                     ExecutionContext context) throws SQLException {
        // Таймаут
        int timeout = query.getTimeout().orElse(context.getQueryTimeout());
        if (timeout > 0) {
            stmt.setQueryTimeout(timeout);
        }

        // Fetch size
        int fetchSize = context.getFetchSize();
        if (fetchSize > 0) {
            stmt.setFetchSize(fetchSize);
        }
    }

    /**
     * Устанавливает параметры в PreparedStatement.
     */
    private void setParameters(PreparedStatement stmt, List<Object> parameters)
            throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            Object value = parameters.get(i);
            setParameter(stmt, i + 1, value);
        }
    }

    /**
     * Устанавливает один параметр с учётом его типа.
     */
    private void setParameter(PreparedStatement stmt, int index, Object value)
            throws SQLException {
        if (value == null) {
            stmt.setNull(index, Types.NULL);
            return;
        }

        // Стандартные типы
        if (value instanceof String s) {
            stmt.setString(index, s);
        } else if (value instanceof Integer i) {
            stmt.setInt(index, i);
        } else if (value instanceof Long l) {
            stmt.setLong(index, l);
        } else if (value instanceof Double d) {
            stmt.setDouble(index, d);
        } else if (value instanceof Float f) {
            stmt.setFloat(index, f);
        } else if (value instanceof Boolean b) {
            stmt.setBoolean(index, b);
        } else if (value instanceof BigDecimal bd) {
            stmt.setBigDecimal(index, bd);
        } else if (value instanceof LocalDateTime ldt) {
            stmt.setTimestamp(index, Timestamp.valueOf(ldt));
        } else if (value instanceof LocalDate ld) {
            stmt.setDate(index, java.sql.Date.valueOf(ld));
        } else if (value instanceof LocalTime lt) {
            stmt.setTime(index, java.sql.Time.valueOf(lt));
        } else if (value instanceof java.util.Date date) {
            stmt.setTimestamp(index, new Timestamp(date.getTime()));
        } else if (value instanceof byte[] bytes) {
            stmt.setBytes(index, bytes);
        } else if (value instanceof Enum<?> e) {
            stmt.setString(index, e.name());
        } else if (value instanceof Collection<?> collection) {
            // Для IN clause — разворачиваем коллекцию
            // Это обрабатывается отдельно в NamedParameterProcessor
            throw new IllegalArgumentException(
                "Collection parameters should be handled by NamedParameterProcessor. "
                + "Use @ParamList for collections.");
        } else {
            // Для других типов пробуем setObject
            stmt.setObject(index, value);
        }
    }

    /**
     * Маппит ResultSet на объект/список в зависимости от return type.
     */
    @SuppressWarnings("unchecked")
    private Object mapResultSet(ResultSet rs, QueryDefinition query) throws SQLException {
        Class<?> returnClass = query.getReturnClass();
        Type returnType = query.getReturnType();

        // void — просто пропускаем
        if (returnClass == void.class || returnClass == Void.class) {
            return null;
        }

        // Optional<T>
        if (returnClass == Optional.class) {
            Class<?> elementType = extractGenericType(returnType);
            if (rs.next()) {
                return Optional.ofNullable(mapRow(rs, elementType, query, 1));
            }
            return Optional.empty();
        }

        // List<T> или другие коллекции
        if (List.class.isAssignableFrom(returnClass)
            || Collection.class.isAssignableFrom(returnClass)) {
            Class<?> elementType = extractGenericType(returnType);
            List<Object> list = new ArrayList<>();
            int rowNum = 0;
            while (rs.next()) {
                list.add(mapRow(rs, elementType, query, ++rowNum));
            }
            return list;
        }

        // Скалярные типы
        if (isScalarType(returnClass)) {
            if (rs.next()) {
                return getScalarValue(rs, 1, returnClass);
            }
            return getDefaultValue(returnClass);
        }

        // Одиночный объект
        if (rs.next()) {
            return mapRow(rs, returnClass, query, 1);
        }
        return null;
    }

    /**
     * Маппит одну строку ResultSet на объект.
     */
    private Object mapRow(ResultSet rs, Class<?> targetClass, QueryDefinition query,
                           int rowNum) throws SQLException {
        // Проверяем кастомный маппер
        Optional<Class<?>> mapperClass = query.getRowMapperClass();
        if (mapperClass.isPresent()) {
            return mapWithCustomMapper(rs, mapperClass.get(), rowNum);
        }

        // Скалярные типы
        if (isScalarType(targetClass)) {
            return getScalarValue(rs, 1, targetClass);
        }

        // Для сложных объектов используем reflection-based маппинг
        // Полная реализация будет в Story 6.6
        return mapToObject(rs, targetClass, query);
    }

    /**
     * Маппит строку с помощью кастомного RowMapper.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object mapWithCustomMapper(ResultSet rs, Class<?> mapperClass, int rowNum)
            throws SQLException {
        try {
            RowMapper mapper = (RowMapper) mapperClass.getDeclaredConstructor().newInstance();
            return mapper.mapRow(rs, rowNum);
        } catch (Exception e) {
            throw new MappingException(
                "Failed to create RowMapper: " + mapperClass.getName(), mapperClass, e);
        }
    }

    /**
     * Простой маппинг на объект по имени колонок.
     * Полная реализация в Story 6.6.
     */
    private Object mapToObject(ResultSet rs, Class<?> targetClass, QueryDefinition query)
            throws SQLException {
        // Record mapping
        if (targetClass.isRecord()) {
            return mapToRecord(rs, targetClass);
        }

        // Обычный объект — создаём через default constructor и сеттеры
        try {
            Object instance = targetClass.getDeclaredConstructor().newInstance();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i);
                if (columnName == null || columnName.isEmpty()) {
                    columnName = metaData.getColumnName(i);
                }

                Object value = rs.getObject(i);
                setFieldValue(instance, columnName, value);
            }

            return instance;

        } catch (ReflectiveOperationException e) {
            throw new MappingException(
                "Failed to map ResultSet to " + targetClass.getName(), targetClass, e);
        }
    }

    /**
     * Маппит ResultSet на Record.
     */
    private Object mapToRecord(ResultSet rs, Class<?> recordClass) throws SQLException {
        var components = recordClass.getRecordComponents();
        Object[] args = new Object[components.length];
        Class<?>[] types = new Class<?>[components.length];

        ResultSetMetaData metaData = rs.getMetaData();

        for (int i = 0; i < components.length; i++) {
            String name = components[i].getName();
            types[i] = components[i].getType();

            // Ищем колонку по имени (camelCase или snake_case)
            int columnIndex = findColumnIndex(metaData, name);
            if (columnIndex > 0) {
                args[i] = convertValue(rs.getObject(columnIndex), types[i]);
            } else {
                args[i] = getDefaultValue(types[i]);
            }
        }

        try {
            return recordClass.getDeclaredConstructor(types).newInstance(args);
        } catch (Exception e) {
            throw new MappingException(
                "Failed to create record " + recordClass.getName(), recordClass, e);
        }
    }

    /**
     * Ищет индекс колонки по имени (поддержка camelCase и snake_case).
     */
    private int findColumnIndex(ResultSetMetaData metaData, String fieldName)
            throws SQLException {
        int columnCount = metaData.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnLabel(i);
            if (columnName == null || columnName.isEmpty()) {
                columnName = metaData.getColumnName(i);
            }

            // Точное совпадение
            if (columnName.equalsIgnoreCase(fieldName)) {
                return i;
            }

            // snake_case → camelCase
            String camelCase = snakeToCamel(columnName);
            if (camelCase.equalsIgnoreCase(fieldName)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Конвертирует snake_case в camelCase.
     */
    private String snakeToCamel(String snake) {
        if (snake == null || !snake.contains("_")) {
            return snake;
        }

        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = false;

        for (char c : snake.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else {
                if (capitalizeNext) {
                    result.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    result.append(Character.toLowerCase(c));
                }
            }
        }

        return result.toString();
    }

    /**
     * Устанавливает значение поля объекта.
     */
    private void setFieldValue(Object instance, String columnName, Object value) {
        if (value == null) {
            return;
        }

        Class<?> clazz = instance.getClass();
        String fieldName = snakeToCamel(columnName);

        // Ищем сеттер
        String setterName = "set" + Character.toUpperCase(fieldName.charAt(0))
            + fieldName.substring(1);

        try {
            for (var method : clazz.getMethods()) {
                if (method.getName().equals(setterName)
                    && method.getParameterCount() == 1) {
                    Class<?> paramType = method.getParameterTypes()[0];
                    Object convertedValue = convertValue(value, paramType);
                    method.invoke(instance, convertedValue);
                    return;
                }
            }

            // Пробуем напрямую установить поле
            try {
                var field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                Object convertedValue = convertValue(value, field.getType());
                field.set(instance, convertedValue);
            } catch (NoSuchFieldException ignored) {
                // Поле не найдено — пропускаем
            }

        } catch (Exception e) {
            logger.trace("Failed to set field '{}' on {}: {}",
                fieldName, clazz.getSimpleName(), e.getMessage());
        }
    }

    /**
     * Конвертирует значение в нужный тип.
     */
    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null) {
            return getDefaultValue(targetType);
        }

        if (targetType.isInstance(value)) {
            return value;
        }

        // Числовые конверсии
        if (value instanceof Number number) {
            if (targetType == Long.class || targetType == long.class) {
                return number.longValue();
            }
            if (targetType == Integer.class || targetType == int.class) {
                return number.intValue();
            }
            if (targetType == Double.class || targetType == double.class) {
                return number.doubleValue();
            }
            if (targetType == Float.class || targetType == float.class) {
                return number.floatValue();
            }
            if (targetType == Short.class || targetType == short.class) {
                return number.shortValue();
            }
            if (targetType == Byte.class || targetType == byte.class) {
                return number.byteValue();
            }
            if (targetType == BigDecimal.class) {
                return BigDecimal.valueOf(number.doubleValue());
            }
        }

        // Timestamp → LocalDateTime
        if (value instanceof Timestamp ts && targetType == LocalDateTime.class) {
            return ts.toLocalDateTime();
        }

        // Date → LocalDate
        if (value instanceof java.sql.Date date && targetType == LocalDate.class) {
            return date.toLocalDate();
        }

        // Time → LocalTime
        if (value instanceof java.sql.Time time && targetType == LocalTime.class) {
            return time.toLocalTime();
        }

        // Boolean
        if (targetType == Boolean.class || targetType == boolean.class) {
            if (value instanceof Number n) {
                return n.intValue() != 0;
            }
            if (value instanceof String s) {
                return Boolean.parseBoolean(s) || "1".equals(s) || "Y".equalsIgnoreCase(s);
            }
        }

        // String
        if (targetType == String.class) {
            return value.toString();
        }

        return value;
    }

    /**
     * Проверяет, является ли тип скалярным.
     */
    private boolean isScalarType(Class<?> type) {
        return type.isPrimitive()
            || type == String.class
            || type == Integer.class
            || type == Long.class
            || type == Double.class
            || type == Float.class
            || type == Boolean.class
            || type == Short.class
            || type == Byte.class
            || type == BigDecimal.class
            || type == LocalDateTime.class
            || type == LocalDate.class
            || type == LocalTime.class;
    }

    /**
     * Получает скалярное значение из ResultSet.
     */
    private Object getScalarValue(ResultSet rs, int columnIndex, Class<?> type)
            throws SQLException {
        Object value = rs.getObject(columnIndex);
        if (value == null) {
            return getDefaultValue(type);
        }
        return convertValue(value, type);
    }

    /**
     * Возвращает значение по умолчанию для типа.
     */
    private Object getDefaultValue(Class<?> type) {
        if (type.isPrimitive()) {
            if (type == int.class) return 0;
            if (type == long.class) return 0L;
            if (type == double.class) return 0.0;
            if (type == float.class) return 0.0f;
            if (type == boolean.class) return false;
            if (type == short.class) return (short) 0;
            if (type == byte.class) return (byte) 0;
            if (type == char.class) return '\0';
        }
        return null;
    }

    /**
     * Извлекает generic тип из параметризованного типа.
     */
    private Class<?> extractGenericType(Type type) {
        if (type instanceof ParameterizedType paramType) {
            Type[] typeArgs = paramType.getActualTypeArguments();
            if (typeArgs.length > 0 && typeArgs[0] instanceof Class<?> clazz) {
                return clazz;
            }
        }
        return Object.class;
    }

    /**
     * Строит QueryResult для SELECT запроса.
     */
    private QueryResult buildSelectResult(Object result, long executionTime) {
        if (result == null) {
            return QueryResult.builder()
                .executionTimeMs(executionTime)
                .empty(true)
                .build();
        }

        if (result instanceof List<?> list) {
            return QueryResult.ofList(list, executionTime);
        }

        if (result instanceof Optional<?> opt) {
            return QueryResult.builder()
                .value(opt.orElse(null))
                .executionTimeMs(executionTime)
                .empty(opt.isEmpty())
                .affectedRows(opt.isPresent() ? 1 : 0)
                .build();
        }

        return QueryResult.ofSingle(result, executionTime);
    }

    /**
     * Преобразует SQLException в типизированное исключение.
     */
    private QueryExecutionException translateException(SQLException e, QueryDefinition query) {
        String sqlState = e.getSQLState();
        int errorCode = e.getErrorCode();
        String message = e.getMessage();

        logger.debug("SQL Exception: state={}, code={}, message={}",
            sqlState, errorCode, message);

        // Таймаут
        if (e instanceof SQLTimeoutException) {
            int timeout = query.getTimeout().orElse(30);
            return new QueryTimeoutException(
                timeout,
                query.getMethodName(),
                query.getSql(),
                query.getParameters(),
                e
            );
        }

        // Constraint violations
        if (e instanceof SQLIntegrityConstraintViolationException || isConstraintViolation(sqlState)) {
            return translateConstraintViolation(e, query, sqlState, errorCode);
        }

        // Общая ошибка выполнения
        return new QueryExecutionException(
            "Query execution failed: " + message,
            query.getMethodName(),
            query.getSql(),
            query.getParameters(),
            sqlState,
            errorCode,
            e
        );
    }

    /**
     * Проверяет, является ли SQLState кодом нарушения ограничения.
     */
    private boolean isConstraintViolation(String sqlState) {
        if (sqlState == null) {
            return false;
        }
        // Класс 23 — Integrity Constraint Violation
        return sqlState.startsWith("23");
    }

    /**
     * Преобразует constraint violation в типизированное исключение.
     */
    private ConstraintViolationException translateConstraintViolation(
            SQLException e, QueryDefinition query, String sqlState, int errorCode) {

        String message = e.getMessage();

        // Unique constraint (23505 — PostgreSQL, 23000 — H2/MySQL duplicate)
        if ("23505".equals(sqlState) || (message != null && message.contains("unique"))) {
            return new UniqueConstraintException(
                "Unique constraint violation: " + message,
                extractConstraintName(message),
                extractTableName(message),
                null,
                null,
                query.getMethodName(),
                query.getSql(),
                query.getParameters(),
                e
            );
        }

        // Foreign key (23503)
        if ("23503".equals(sqlState) || (message != null && message.contains("foreign key"))) {
            return new ForeignKeyException(
                "Foreign key violation: " + message,
                extractConstraintName(message),
                extractTableName(message),
                null,
                null,
                query.getMethodName(),
                query.getSql(),
                query.getParameters(),
                e
            );
        }

        // NOT NULL (23502)
        if ("23502".equals(sqlState) || (message != null && message.contains("null"))) {
            return new NotNullException(
                "NOT NULL constraint violation: " + message,
                extractTableName(message),
                extractColumnName(message),
                query.getMethodName(),
                query.getSql(),
                query.getParameters(),
                e
            );
        }

        // Generic constraint violation
        return new ConstraintViolationException(
            "Constraint violation: " + message,
            extractConstraintName(message),
            extractTableName(message),
            query.getMethodName(),
            query.getSql(),
            query.getParameters(),
            e
        );
    }

    /**
     * Извлекает имя constraint из сообщения об ошибке.
     */
    private String extractConstraintName(String message) {
        if (message == null) {
            return null;
        }

        // Паттерны для разных БД
        // PostgreSQL: "violates unique constraint \"constraint_name\""
        // H2: "Unique index or primary key violation: \"CONSTRAINT_NAME\""
        var pattern = java.util.regex.Pattern.compile(
            "constraint\\s+[\"']?(\\w+)[\"']?",
            java.util.regex.Pattern.CASE_INSENSITIVE
        );
        var matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    /**
     * Извлекает имя таблицы из сообщения об ошибке.
     */
    private String extractTableName(String message) {
        if (message == null) {
            return null;
        }

        var pattern = java.util.regex.Pattern.compile(
            "table\\s+[\"']?(\\w+)[\"']?",
            java.util.regex.Pattern.CASE_INSENSITIVE
        );
        var matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    /**
     * Извлекает имя колонки из сообщения об ошибке.
     */
    private String extractColumnName(String message) {
        if (message == null) {
            return null;
        }

        var pattern = java.util.regex.Pattern.compile(
            "column\\s+[\"']?(\\w+)[\"']?",
            java.util.regex.Pattern.CASE_INSENSITIVE
        );
        var matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }
}
