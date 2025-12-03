package com.company.hex.db.mapping;

import com.company.hex.db.exception.MappingException;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Фабрика для создания {@link RowMapper} экземпляров.
 *
 * <p>Предоставляет методы для создания автоматических и кастомных
 * RowMapper'ов с кэшированием для производительности.</p>
 *
 * <h2>Примеры использования</h2>
 * <pre>{@code
 * // Автоматический маппер для Entity
 * RowMapper<UserEntity> userMapper = RowMapperFactory.forClass(UserEntity.class);
 *
 * // Кастомный маппер
 * RowMapper<UserWithOrders> customMapper = RowMapperFactory.forClass(UserWithOrdersMapper.class);
 *
 * // Использование в репозитории
 * @Select("SELECT * FROM users")
 * @RowMapping(UserMapper.class)
 * List<User> findAll();
 * }</pre>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see RowMapper
 * @see DefaultResultMapper
 */
public final class RowMapperFactory {

    /**
     * Кэш автоматических мапперов.
     */
    private static final ConcurrentHashMap<Class<?>, RowMapper<?>> AUTO_MAPPER_CACHE =
        new ConcurrentHashMap<>();

    /**
     * Кэш кастомных мапперов.
     */
    private static final ConcurrentHashMap<Class<?>, RowMapper<?>> CUSTOM_MAPPER_CACHE =
        new ConcurrentHashMap<>();

    private RowMapperFactory() {
        // Utility class
    }

    /**
     * Создаёт автоматический RowMapper для указанного класса.
     *
     * <p>Маппер использует {@link DefaultResultMapper} для преобразования
     * строки ResultSet в объект. Поддерживает POJO и Java Records.</p>
     *
     * @param <T> тип объекта
     * @param targetClass класс целевого объекта
     * @return RowMapper для указанного класса
     */
    @SuppressWarnings("unchecked")
    public static <T> RowMapper<T> forClass(Class<T> targetClass) {
        Objects.requireNonNull(targetClass, "Target class cannot be null");

        return (RowMapper<T>) AUTO_MAPPER_CACHE.computeIfAbsent(
            targetClass,
            RowMapperFactory::createAutoMapper
        );
    }

    /**
     * Создаёт или возвращает кэшированный экземпляр кастомного RowMapper.
     *
     * <p>Кастомный маппер должен иметь конструктор без параметров.</p>
     *
     * @param <T> тип объекта
     * @param mapperClass класс кастомного RowMapper
     * @return экземпляр RowMapper
     * @throws MappingException если не удалось создать экземпляр маппера
     */
    @SuppressWarnings("unchecked")
    public static <T> RowMapper<T> forCustomMapper(Class<? extends RowMapper<T>> mapperClass) {
        Objects.requireNonNull(mapperClass, "Mapper class cannot be null");

        return (RowMapper<T>) CUSTOM_MAPPER_CACHE.computeIfAbsent(
            mapperClass,
            RowMapperFactory::createCustomMapper
        );
    }

    /**
     * Создаёт или возвращает кэшированный экземпляр кастомного RowMapper по raw class.
     *
     * <p>Этот метод используется когда тип маппера неизвестен во время компиляции,
     * например при загрузке из аннотации @RowMapping.</p>
     *
     * @param mapperClass класс кастомного RowMapper
     * @return экземпляр RowMapper (raw type)
     * @throws MappingException если класс не является RowMapper или не удалось создать экземпляр
     */
    @SuppressWarnings("rawtypes")
    public static RowMapper forCustomMapperRaw(Class<?> mapperClass) {
        Objects.requireNonNull(mapperClass, "Mapper class cannot be null");

        if (!RowMapper.class.isAssignableFrom(mapperClass)) {
            throw new MappingException(
                "Class " + mapperClass.getName() + " does not implement RowMapper",
                mapperClass,
                null
            );
        }

        return (RowMapper) CUSTOM_MAPPER_CACHE.computeIfAbsent(
            mapperClass,
            RowMapperFactory::createCustomMapper
        );
    }

    /**
     * Создаёт экземпляр кастомного RowMapper по классу (без кэширования).
     *
     * @param <T> тип объекта
     * @param mapperClass класс кастомного RowMapper
     * @return новый экземпляр RowMapper
     * @throws MappingException если не удалось создать экземпляр
     */
    @SuppressWarnings("unchecked")
    public static <T> RowMapper<T> createInstance(Class<? extends RowMapper<T>> mapperClass) {
        return (RowMapper<T>) createCustomMapper(mapperClass);
    }

    /**
     * Создаёт RowMapper из функции (для лямбда-выражений).
     *
     * @param <T> тип объекта
     * @param mapper функция маппинга
     * @return RowMapper на основе функции
     */
    public static <T> RowMapper<T> from(RowMapper<T> mapper) {
        return mapper;
    }

    /**
     * Создаёт скалярный RowMapper для простых типов.
     *
     * @param <T> тип результата
     * @param scalarType класс скалярного типа
     * @return RowMapper для скалярного типа
     */
    public static <T> RowMapper<T> forScalar(Class<T> scalarType) {
        Objects.requireNonNull(scalarType, "Scalar type cannot be null");

        return (rs, rowNum) -> {
            Object value = rs.getObject(1);
            if (rs.wasNull()) {
                return TypeConverter.getDefaultValue(scalarType);
            }
            return TypeConverter.convert(value, scalarType);
        };
    }

    /**
     * Очищает все кэши мапперов.
     * Полезно для тестов.
     */
    public static void clearCache() {
        AUTO_MAPPER_CACHE.clear();
        CUSTOM_MAPPER_CACHE.clear();
    }

    // ==================== Private Methods ====================

    /**
     * Создаёт автоматический маппер для класса.
     */
    private static <T> RowMapper<T> createAutoMapper(Class<T> targetClass) {
        // Для скалярных типов используем простой маппер
        if (TypeConverter.isScalarType(targetClass)) {
            return forScalar(targetClass);
        }

        // Кэшируем метаданные для производительности
        EntityMetadata.forClass(targetClass);

        // Создаём маппер, который использует DefaultResultMapper
        return new EntityRowMapper<>(targetClass);
    }

    /**
     * Создаёт экземпляр кастомного маппера.
     */
    @SuppressWarnings("rawtypes")
    private static RowMapper<?> createCustomMapper(Class<?> mapperClass) {
        try {
            return (RowMapper) mapperClass.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            throw new MappingException(
                "RowMapper class " + mapperClass.getName()
                + " must have a no-args constructor",
                e
            );
        } catch (Exception e) {
            throw new MappingException(
                "Failed to create RowMapper instance: " + mapperClass.getName(),
                e
            );
        }
    }

    /**
     * RowMapper для Entity классов, который не вызывает rs.next().
     */
    private static class EntityRowMapper<T> implements RowMapper<T> {
        private final Class<T> targetClass;
        private final EntityMetadata metadata;

        EntityRowMapper(Class<T> targetClass) {
            this.targetClass = targetClass;
            this.metadata = EntityMetadata.forClass(targetClass);
        }

        @Override
        @SuppressWarnings("unchecked")
        public T mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
            try {
                if (metadata.isRecord()) {
                    return (T) mapToRecord(rs);
                } else {
                    return (T) mapToPojo(rs);
                }
            } catch (MappingException e) {
                throw e;
            } catch (Exception e) {
                throw new MappingException(
                    "Failed to map row " + rowNum + " to " + targetClass.getSimpleName(),
                    targetClass,
                    e
                );
            }
        }

        private Object mapToRecord(java.sql.ResultSet rs) throws java.sql.SQLException {
            java.util.List<EntityMetadata.FieldMetadata> fields = metadata.getFields();
            Object[] args = new Object[fields.size()];
            java.sql.ResultSetMetaData rsMetaData = rs.getMetaData();

            for (int i = 0; i < fields.size(); i++) {
                EntityMetadata.FieldMetadata field = fields.get(i);
                args[i] = extractFieldValue(rs, rsMetaData, field);
            }

            try {
                return metadata.getConstructor().newInstance(args);
            } catch (Exception e) {
                throw new MappingException(
                    "Failed to create Record instance: " + targetClass.getSimpleName(),
                    targetClass,
                    e
                );
            }
        }

        private Object mapToPojo(java.sql.ResultSet rs) throws java.sql.SQLException {
            Object instance;
            try {
                instance = metadata.getConstructor().newInstance();
            } catch (Exception e) {
                throw new MappingException(
                    "Failed to create instance of " + targetClass.getSimpleName(),
                    targetClass,
                    e
                );
            }

            java.sql.ResultSetMetaData rsMetaData = rs.getMetaData();
            int columnCount = rsMetaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                String columnLabel = rsMetaData.getColumnLabel(i);
                if (columnLabel == null || columnLabel.isEmpty()) {
                    columnLabel = rsMetaData.getColumnName(i);
                }

                java.util.Optional<EntityMetadata.FieldMetadata> fieldOpt =
                    metadata.getFieldByColumnName(columnLabel);
                if (fieldOpt.isEmpty()) {
                    continue;
                }

                EntityMetadata.FieldMetadata field = fieldOpt.get();
                Object value = extractAndConvertValue(rs, i, field);

                try {
                    field.setValue(instance, value);
                } catch (Exception e) {
                    throw new MappingException(
                        "Failed to set field '" + field.getFieldName() + "'",
                        targetClass,
                        e
                    );
                }
            }

            return instance;
        }

        private Object extractFieldValue(java.sql.ResultSet rs,
                                          java.sql.ResultSetMetaData rsMetaData,
                                          EntityMetadata.FieldMetadata field)
                throws java.sql.SQLException {
            String columnName = field.getColumnName();
            int columnIndex = findColumnIndex(rsMetaData, columnName, field.getFieldName());

            if (columnIndex <= 0) {
                return TypeConverter.getDefaultValue(field.getFieldType());
            }

            return extractAndConvertValue(rs, columnIndex, field);
        }

        private Object extractAndConvertValue(java.sql.ResultSet rs, int columnIndex,
                                               EntityMetadata.FieldMetadata field)
                throws java.sql.SQLException {
            Object rawValue = rs.getObject(columnIndex);
            if (rs.wasNull() || rawValue == null) {
                return TypeConverter.getDefaultValue(field.getFieldType());
            }

            Class<?> targetType = field.getFieldType();

            if (field.isEnum()) {
                @SuppressWarnings({"unchecked", "rawtypes"})
                Class<? extends Enum> enumClass = (Class<? extends Enum>) targetType;
                com.company.hex.db.annotations.mapping.Enumerated.EnumType enumType =
                    field.getEnumType().orElse(
                        com.company.hex.db.annotations.mapping.Enumerated.EnumType.STRING);
                return TypeConverter.toEnum(rawValue, enumClass, enumType);
            }

            return TypeConverter.convert(rawValue, targetType);
        }

        private int findColumnIndex(java.sql.ResultSetMetaData metaData,
                                     String columnName, String fieldName)
                throws java.sql.SQLException {
            int columnCount = metaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                String label = metaData.getColumnLabel(i);
                if (label == null || label.isEmpty()) {
                    label = metaData.getColumnName(i);
                }

                if (label.equalsIgnoreCase(columnName)) {
                    return i;
                }

                if (label.equalsIgnoreCase(fieldName)) {
                    return i;
                }

                String camelLabel = EntityMetadata.snakeToCamel(label);
                if (camelLabel.equalsIgnoreCase(fieldName)) {
                    return i;
                }
            }

            return -1;
        }
    }
}
