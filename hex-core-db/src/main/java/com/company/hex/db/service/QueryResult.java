package com.company.hex.db.service;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Результат выполнения SQL запроса.
 *
 * <p>Содержит результат запроса и метаданные выполнения: количество
 * затронутых строк, время выполнения, сгенерированные ключи и т.д.</p>
 *
 * <h2>Пример использования</h2>
 * <pre>{@code
 * // SELECT запрос
 * QueryResult result = executor.execute(query, context);
 * List<UserEntity> users = result.getList(UserEntity.class);
 * Optional<UserEntity> user = result.getSingle(UserEntity.class);
 *
 * // INSERT с генерацией ключа
 * QueryResult result = executor.execute(insertQuery, context);
 * Long generatedId = result.getGeneratedKey(Long.class);
 *
 * // UPDATE/DELETE
 * QueryResult result = executor.execute(updateQuery, context);
 * int affectedRows = result.getAffectedRows();
 * }</pre>
 *
 * <h2>Метаданные</h2>
 * <ul>
 *     <li>{@code executionTimeMs} — время выполнения в миллисекундах</li>
 *     <li>{@code affectedRows} — количество затронутых строк</li>
 *     <li>{@code generatedKeys} — список сгенерированных ключей</li>
 *     <li>{@code metadata} — произвольные метаданные (для интерцепторов)</li>
 * </ul>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see QueryExecutor
 */
public final class QueryResult {

    private final Object value;
    private final int affectedRows;
    private final long executionTimeMs;
    private final List<Object> generatedKeys;
    private final Map<String, Object> metadata;
    private final boolean empty;

    private QueryResult(Builder builder) {
        this.value = builder.value;
        this.affectedRows = builder.affectedRows;
        this.executionTimeMs = builder.executionTimeMs;
        this.generatedKeys = Collections.unmodifiableList(
            new ArrayList<>(builder.generatedKeys));
        this.metadata = Collections.unmodifiableMap(
            new HashMap<>(builder.metadata));
        this.empty = builder.empty;
    }

    /**
     * Создаёт новый builder для QueryResult.
     *
     * @return новый экземпляр Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Создаёт пустой результат.
     *
     * @return пустой QueryResult
     */
    public static QueryResult empty() {
        return new Builder().empty(true).build();
    }

    /**
     * Создаёт результат для void метода.
     *
     * @param affectedRows количество затронутых строк
     * @param executionTimeMs время выполнения
     * @return QueryResult
     */
    public static QueryResult ofVoid(int affectedRows, long executionTimeMs) {
        return new Builder()
            .affectedRows(affectedRows)
            .executionTimeMs(executionTimeMs)
            .build();
    }

    /**
     * Создаёт результат с одним значением.
     *
     * @param value результат
     * @param executionTimeMs время выполнения
     * @return QueryResult
     */
    public static QueryResult ofSingle(Object value, long executionTimeMs) {
        return new Builder()
            .value(value)
            .affectedRows(value != null ? 1 : 0)
            .executionTimeMs(executionTimeMs)
            .empty(value == null)
            .build();
    }

    /**
     * Создаёт результат со списком значений.
     *
     * @param values список результатов
     * @param executionTimeMs время выполнения
     * @return QueryResult
     */
    public static QueryResult ofList(List<?> values, long executionTimeMs) {
        return new Builder()
            .value(values)
            .affectedRows(values != null ? values.size() : 0)
            .executionTimeMs(executionTimeMs)
            .empty(values == null || values.isEmpty())
            .build();
    }

    /**
     * Создаёт результат с количеством затронутых строк (для UPDATE/DELETE).
     *
     * @param affectedRows количество строк
     * @param executionTimeMs время выполнения
     * @return QueryResult
     */
    public static QueryResult ofAffectedRows(int affectedRows, long executionTimeMs) {
        return new Builder()
            .value(affectedRows)
            .affectedRows(affectedRows)
            .executionTimeMs(executionTimeMs)
            .build();
    }

    /**
     * Создаёт результат INSERT с сгенерированным ключом.
     *
     * @param generatedKey сгенерированный ключ
     * @param executionTimeMs время выполнения
     * @return QueryResult
     */
    public static QueryResult ofGeneratedKey(Object generatedKey, long executionTimeMs) {
        Builder builder = new Builder()
            .value(generatedKey)
            .affectedRows(1)
            .executionTimeMs(executionTimeMs);

        if (generatedKey != null) {
            builder.generatedKey(generatedKey);
        }

        return builder.build();
    }

    // ==================== Getters ====================

    /**
     * Возвращает raw значение результата.
     *
     * @return результат (может быть null)
     */
    public Object getValue() {
        return value;
    }

    /**
     * Возвращает количество затронутых строк.
     *
     * @return количество строк
     */
    public int getAffectedRows() {
        return affectedRows;
    }

    /**
     * Возвращает время выполнения в миллисекундах.
     *
     * @return время в мс
     */
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    /**
     * Возвращает список сгенерированных ключей.
     *
     * @return неизменяемый список ключей
     */
    public List<Object> getGeneratedKeys() {
        return generatedKeys;
    }

    /**
     * Возвращает метаданные результата.
     *
     * @return неизменяемая карта метаданных
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Проверяет, пустой ли результат.
     *
     * @return true если результат пустой
     */
    public boolean isEmpty() {
        return empty;
    }

    /**
     * Проверяет, есть ли результат.
     *
     * @return true если результат не пустой
     */
    public boolean isPresent() {
        return !empty;
    }

    // ==================== Typed Access ====================

    /**
     * Возвращает результат как единственный объект указанного типа.
     *
     * @param <T> тип результата
     * @param type класс результата
     * @return объект или null
     */
    @SuppressWarnings("unchecked")
    public <T> T getSingle(Class<T> type) {
        if (value == null) {
            return null;
        }

        if (type.isInstance(value)) {
            return type.cast(value);
        }

        // Если это список — берём первый элемент
        if (value instanceof List<?> list && !list.isEmpty()) {
            Object first = list.get(0);
            if (type.isInstance(first)) {
                return type.cast(first);
            }
        }

        throw new ClassCastException(
            "Cannot cast " + value.getClass().getName() + " to " + type.getName());
    }

    /**
     * Возвращает результат как Optional.
     *
     * @param <T> тип результата
     * @param type класс результата
     * @return Optional с результатом
     */
    public <T> Optional<T> getOptional(Class<T> type) {
        return Optional.ofNullable(getSingle(type));
    }

    /**
     * Возвращает результат как список.
     *
     * @param <T> тип элементов
     * @param elementType класс элемента
     * @return список результатов
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getList(Class<T> elementType) {
        if (value == null) {
            return Collections.emptyList();
        }

        if (value instanceof List<?> list) {
            // Проверяем тип элементов
            for (Object item : list) {
                if (item != null && !elementType.isInstance(item)) {
                    throw new ClassCastException(
                        "List contains " + item.getClass().getName()
                        + " but expected " + elementType.getName());
                }
            }
            return (List<T>) list;
        }

        // Если это одиночный элемент — оборачиваем в список
        if (elementType.isInstance(value)) {
            return Collections.singletonList(elementType.cast(value));
        }

        throw new ClassCastException(
            "Cannot get list from " + value.getClass().getName());
    }

    /**
     * Возвращает результат как Stream.
     *
     * @param <T> тип элементов
     * @param elementType класс элемента
     * @return Stream результатов
     */
    public <T> Stream<T> getStream(Class<T> elementType) {
        return getList(elementType).stream();
    }

    /**
     * Возвращает первый сгенерированный ключ.
     *
     * @param <T> тип ключа
     * @param type класс ключа
     * @return ключ или null
     */
    @SuppressWarnings("unchecked")
    public <T> T getGeneratedKey(Class<T> type) {
        if (generatedKeys.isEmpty()) {
            return null;
        }

        Object key = generatedKeys.get(0);
        if (key == null) {
            return null;
        }

        // Конвертация числовых типов
        if (type == Long.class && key instanceof Number number) {
            return (T) Long.valueOf(number.longValue());
        }
        if (type == Integer.class && key instanceof Number number) {
            return (T) Integer.valueOf(number.intValue());
        }

        if (type.isInstance(key)) {
            return type.cast(key);
        }

        throw new ClassCastException(
            "Cannot cast generated key " + key.getClass().getName()
            + " to " + type.getName());
    }

    /**
     * Возвращает все сгенерированные ключи.
     *
     * @param <T> тип ключей
     * @param type класс ключа
     * @return список ключей
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getGeneratedKeys(Class<T> type) {
        if (generatedKeys.isEmpty()) {
            return Collections.emptyList();
        }

        List<T> result = new ArrayList<>(generatedKeys.size());
        for (Object key : generatedKeys) {
            if (key == null) {
                result.add(null);
            } else if (type == Long.class && key instanceof Number number) {
                result.add((T) Long.valueOf(number.longValue()));
            } else if (type == Integer.class && key instanceof Number number) {
                result.add((T) Integer.valueOf(number.intValue()));
            } else if (type.isInstance(key)) {
                result.add(type.cast(key));
            } else {
                throw new ClassCastException(
                    "Cannot cast generated key " + key.getClass().getName()
                    + " to " + type.getName());
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Возвращает метаданные по ключу.
     *
     * @param <T> тип значения
     * @param key ключ
     * @param type класс значения
     * @return значение или null
     */
    public <T> T getMetadata(String key, Class<T> type) {
        Object val = metadata.get(key);
        return val != null ? type.cast(val) : null;
    }

    // ==================== Return Type Conversion ====================

    /**
     * Конвертирует результат в ожидаемый тип возврата метода.
     *
     * @param returnType ожидаемый тип возврата
     * @param returnClass класс возврата (без generics)
     * @return объект соответствующего типа
     */
    public Object convertToReturnType(Type returnType, Class<?> returnClass) {
        // void
        if (returnClass == void.class || returnClass == Void.class) {
            return null;
        }

        // int (affected rows)
        if (returnClass == int.class || returnClass == Integer.class) {
            return affectedRows;
        }

        // long (affected rows or generated key)
        if (returnClass == long.class || returnClass == Long.class) {
            if (!generatedKeys.isEmpty()) {
                return getGeneratedKey(Long.class);
            }
            return (long) affectedRows;
        }

        // boolean
        if (returnClass == boolean.class || returnClass == Boolean.class) {
            return affectedRows > 0;
        }

        // Optional
        if (returnClass == Optional.class) {
            Class<?> elementType = extractGenericType(returnType);
            return getOptional(elementType);
        }

        // List
        if (List.class.isAssignableFrom(returnClass)) {
            Class<?> elementType = extractGenericType(returnType);
            return getList(elementType);
        }

        // Stream
        if (Stream.class.isAssignableFrom(returnClass)) {
            Class<?> elementType = extractGenericType(returnType);
            return getStream(elementType);
        }

        // Одиночный объект
        return getSingle(returnClass);
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

    @Override
    public String toString() {
        return "QueryResult{"
            + "value=" + (value instanceof List<?> list ? "List[" + list.size() + "]" : value)
            + ", affectedRows=" + affectedRows
            + ", executionTimeMs=" + executionTimeMs
            + ", generatedKeys=" + generatedKeys
            + ", empty=" + empty
            + '}';
    }

    // ==================== Builder ====================

    /**
     * Builder для создания QueryResult.
     */
    public static final class Builder {

        private Object value;
        private int affectedRows;
        private long executionTimeMs;
        private final List<Object> generatedKeys = new ArrayList<>();
        private final Map<String, Object> metadata = new HashMap<>();
        private boolean empty;

        private Builder() {
        }

        /**
         * Устанавливает значение результата.
         *
         * @param value результат
         * @return этот builder
         */
        public Builder value(Object value) {
            this.value = value;
            return this;
        }

        /**
         * Устанавливает количество затронутых строк.
         *
         * @param affectedRows количество строк
         * @return этот builder
         */
        public Builder affectedRows(int affectedRows) {
            this.affectedRows = affectedRows;
            return this;
        }

        /**
         * Устанавливает время выполнения.
         *
         * @param executionTimeMs время в мс
         * @return этот builder
         */
        public Builder executionTimeMs(long executionTimeMs) {
            this.executionTimeMs = executionTimeMs;
            return this;
        }

        /**
         * Добавляет сгенерированный ключ.
         *
         * @param key ключ
         * @return этот builder
         */
        public Builder generatedKey(Object key) {
            this.generatedKeys.add(key);
            return this;
        }

        /**
         * Устанавливает все сгенерированные ключи.
         *
         * @param keys список ключей
         * @return этот builder
         */
        public Builder generatedKeys(List<Object> keys) {
            this.generatedKeys.clear();
            if (keys != null) {
                this.generatedKeys.addAll(keys);
            }
            return this;
        }

        /**
         * Добавляет метаданные.
         *
         * @param key ключ
         * @param value значение
         * @return этот builder
         */
        public Builder metadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }

        /**
         * Устанавливает все метаданные.
         *
         * @param metadata карта метаданных
         * @return этот builder
         */
        public Builder metadata(Map<String, Object> metadata) {
            this.metadata.clear();
            if (metadata != null) {
                this.metadata.putAll(metadata);
            }
            return this;
        }

        /**
         * Устанавливает флаг пустого результата.
         *
         * @param empty true если результат пустой
         * @return этот builder
         */
        public Builder empty(boolean empty) {
            this.empty = empty;
            return this;
        }

        /**
         * Создаёт QueryResult из настроек builder.
         *
         * @return новый QueryResult
         */
        public QueryResult build() {
            return new QueryResult(this);
        }
    }
}
