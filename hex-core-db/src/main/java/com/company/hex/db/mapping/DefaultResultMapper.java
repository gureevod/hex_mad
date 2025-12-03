package com.company.hex.db.mapping;

import com.company.hex.db.annotations.mapping.Enumerated;
import com.company.hex.db.exception.MappingException;
import com.company.hex.db.mapping.EntityMetadata.FieldMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Реализация {@link ResultMapper} по умолчанию.
 *
 * <p>Выполняет автоматический маппинг ResultSet на Java объекты
 * с использованием:</p>
 * <ul>
 *     <li>Аннотаций {@code @Column}, {@code @Id}, {@code @Transient}</li>
 *     <li>Конвенции snake_case → camelCase для имён колонок</li>
 *     <li>Reflection для создания объектов и установки значений</li>
 *     <li>Кэширования метаданных классов для производительности</li>
 * </ul>
 *
 * <h2>Поддерживаемые классы</h2>
 * <ul>
 *     <li>POJO с default конструктором и сеттерами/полями</li>
 *     <li>Java Records (Java 16+) — маппинг через конструктор</li>
 *     <li>Скалярные типы (Long, String, Integer и т.д.)</li>
 * </ul>
 *
 * <h2>Пример использования</h2>
 * <pre>{@code
 * ResultMapper mapper = new DefaultResultMapper();
 *
 * // Entity
 * List<UserEntity> users = mapper.mapList(rs, UserEntity.class);
 *
 * // Record
 * Optional<UserRecord> user = mapper.mapOptional(rs, UserRecord.class);
 *
 * // Scalar
 * Long count = mapper.mapScalar(rs, Long.class);
 * }</pre>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see ResultMapper
 * @see EntityMetadata
 * @see TypeConverter
 */
public final class DefaultResultMapper implements ResultMapper {

    private static final Logger logger = LoggerFactory.getLogger(DefaultResultMapper.class);

    /**
     * Singleton instance.
     */
    private static final DefaultResultMapper INSTANCE = new DefaultResultMapper();

    /**
     * Возвращает singleton экземпляр маппера.
     *
     * @return экземпляр DefaultResultMapper
     */
    public static DefaultResultMapper getInstance() {
        return INSTANCE;
    }

    /**
     * Создаёт новый экземпляр маппера.
     * Предпочтительнее использовать {@link #getInstance()}.
     */
    public DefaultResultMapper() {
        // Public constructor for flexibility
    }

    @Override
    public <T> T mapSingle(ResultSet rs, Class<T> targetClass) throws SQLException {
        return mapSingle(rs, targetClass, targetClass);
    }

    @Override
    public <T> T mapSingle(ResultSet rs, Class<T> targetClass, Type genericType) throws SQLException {
        if (!rs.next()) {
            return null;
        }
        return mapCurrentRow(rs, targetClass, 1);
    }

    @Override
    public <T> Optional<T> mapOptional(ResultSet rs, Class<T> targetClass) throws SQLException {
        T result = mapSingle(rs, targetClass);
        return Optional.ofNullable(result);
    }

    @Override
    public <T> List<T> mapList(ResultSet rs, Class<T> targetClass) throws SQLException {
        List<T> results = new ArrayList<>();
        int rowNum = 0;
        while (rs.next()) {
            T item = mapCurrentRow(rs, targetClass, ++rowNum);
            results.add(item);
        }
        return results;
    }

    @Override
    public <T> Set<T> mapSet(ResultSet rs, Class<T> targetClass) throws SQLException {
        Set<T> results = new HashSet<>();
        int rowNum = 0;
        while (rs.next()) {
            T item = mapCurrentRow(rs, targetClass, ++rowNum);
            results.add(item);
        }
        return results;
    }

    @Override
    public <T> Stream<T> mapStream(ResultSet rs, Class<T> targetClass) throws SQLException {
        Iterator<T> iterator = new ResultSetIterator<>(rs, targetClass, this);
        Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(
            iterator, Spliterator.ORDERED);
        return StreamSupport.stream(spliterator, false);
    }

    @Override
    public <T> T mapScalar(ResultSet rs, Class<T> targetClass) throws SQLException {
        if (!rs.next()) {
            return TypeConverter.getDefaultValue(targetClass);
        }
        Object value = rs.getObject(1);
        if (rs.wasNull()) {
            return TypeConverter.getDefaultValue(targetClass);
        }
        return TypeConverter.convert(value, targetClass);
    }

    @Override
    public <T> List<T> mapScalarList(ResultSet rs, Class<T> targetClass) throws SQLException {
        List<T> results = new ArrayList<>();
        while (rs.next()) {
            Object value = rs.getObject(1);
            if (!rs.wasNull()) {
                results.add(TypeConverter.convert(value, targetClass));
            } else {
                results.add(TypeConverter.getDefaultValue(targetClass));
            }
        }
        return results;
    }

    @Override
    public <T> List<T> mapWithRowMapper(ResultSet rs, RowMapper<T> rowMapper) throws SQLException {
        List<T> results = new ArrayList<>();
        int rowNum = 0;
        while (rs.next()) {
            results.add(rowMapper.mapRow(rs, ++rowNum));
        }
        return results;
    }

    @Override
    public <T> T mapSingleWithRowMapper(ResultSet rs, RowMapper<T> rowMapper) throws SQLException {
        if (!rs.next()) {
            return null;
        }
        return rowMapper.mapRow(rs, 1);
    }

    @Override
    public boolean isScalarType(Class<?> type) {
        return TypeConverter.isScalarType(type);
    }

    // ==================== Private Methods ====================

    /**
     * Маппит текущую строку ResultSet на объект указанного класса.
     *
     * @param rs ResultSet, позиционированный на текущей строке
     * @param targetClass целевой класс
     * @param rowNum номер строки (для логирования)
     * @return объект указанного типа
     */
    @SuppressWarnings("unchecked")
    private <T> T mapCurrentRow(ResultSet rs, Class<T> targetClass, int rowNum)
            throws SQLException {
        // Скалярные типы
        if (isScalarType(targetClass)) {
            return mapScalar(rs, targetClass);
        }

        // Entity или Record
        EntityMetadata metadata = EntityMetadata.forClass(targetClass);

        try {
            if (metadata.isRecord()) {
                return (T) mapToRecord(rs, metadata);
            } else {
                return (T) mapToPojo(rs, metadata);
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

    /**
     * Маппит текущую строку на Record.
     */
    private Object mapToRecord(ResultSet rs, EntityMetadata metadata) throws SQLException {
        List<FieldMetadata> fields = metadata.getFields();
        Object[] args = new Object[fields.size()];
        Class<?>[] types = new Class<?>[fields.size()];

        ResultSetMetaData rsMetaData = rs.getMetaData();

        for (int i = 0; i < fields.size(); i++) {
            FieldMetadata field = fields.get(i);
            types[i] = field.getFieldType();
            args[i] = extractFieldValue(rs, rsMetaData, field);
        }

        try {
            Constructor<?> constructor = metadata.getConstructor();
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw new MappingException(
                "Failed to create Record instance: " + metadata.getEntityClass().getSimpleName(),
                metadata.getEntityClass(),
                e
            );
        }
    }

    /**
     * Маппит текущую строку на POJO.
     */
    private Object mapToPojo(ResultSet rs, EntityMetadata metadata) throws SQLException {
        Object instance;
        try {
            instance = metadata.getConstructor().newInstance();
        } catch (Exception e) {
            throw new MappingException(
                "Failed to create instance of " + metadata.getEntityClass().getSimpleName(),
                metadata.getEntityClass(),
                e
            );
        }

        ResultSetMetaData rsMetaData = rs.getMetaData();
        int columnCount = rsMetaData.getColumnCount();

        // Маппим по колонкам из ResultSet
        for (int i = 1; i <= columnCount; i++) {
            String columnLabel = rsMetaData.getColumnLabel(i);
            if (columnLabel == null || columnLabel.isEmpty()) {
                columnLabel = rsMetaData.getColumnName(i);
            }

            Optional<FieldMetadata> fieldOpt = metadata.getFieldByColumnName(columnLabel);
            if (fieldOpt.isEmpty()) {
                logger.trace("No field found for column '{}' in {}",
                    columnLabel, metadata.getEntityClass().getSimpleName());
                continue;
            }

            FieldMetadata field = fieldOpt.get();
            Object value = extractAndConvertValue(rs, i, field);

            try {
                field.setValue(instance, value);
            } catch (Exception e) {
                throw new MappingException(
                    "Failed to set field '" + field.getFieldName() + "'",
                    metadata.getEntityClass(),
                    columnLabel,
                    null, null, null,
                    e
                );
            }
        }

        return instance;
    }

    /**
     * Извлекает значение поля из ResultSet по метаданным.
     */
    private Object extractFieldValue(ResultSet rs, ResultSetMetaData rsMetaData,
                                      FieldMetadata field) throws SQLException {
        String columnName = field.getColumnName();
        int columnIndex = findColumnIndex(rsMetaData, columnName, field.getFieldName());

        if (columnIndex <= 0) {
            logger.trace("Column '{}' not found in ResultSet, using default value",
                columnName);
            return TypeConverter.getDefaultValue(field.getFieldType());
        }

        return extractAndConvertValue(rs, columnIndex, field);
    }

    /**
     * Извлекает и конвертирует значение из ResultSet.
     */
    private Object extractAndConvertValue(ResultSet rs, int columnIndex,
                                           FieldMetadata field) throws SQLException {
        Object rawValue = rs.getObject(columnIndex);
        if (rs.wasNull() || rawValue == null) {
            return TypeConverter.getDefaultValue(field.getFieldType());
        }

        Class<?> targetType = field.getFieldType();

        // Enum требует специальной обработки
        if (field.isEnum()) {
            @SuppressWarnings("unchecked")
            Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) targetType;
            Enumerated.EnumType enumType = field.getEnumType()
                .orElse(Enumerated.EnumType.STRING);
            return TypeConverter.toEnum(rawValue, (Class) enumClass, enumType);
        }

        return TypeConverter.convert(rawValue, targetType);
    }

    /**
     * Ищет индекс колонки в ResultSet по имени (с fallback на camelCase).
     */
    private int findColumnIndex(ResultSetMetaData metaData, String columnName,
                                 String fieldName) throws SQLException {
        int columnCount = metaData.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            String label = metaData.getColumnLabel(i);
            if (label == null || label.isEmpty()) {
                label = metaData.getColumnName(i);
            }

            // Точное совпадение (case insensitive)
            if (label.equalsIgnoreCase(columnName)) {
                return i;
            }

            // Совпадение с именем поля
            if (label.equalsIgnoreCase(fieldName)) {
                return i;
            }

            // snake_case → camelCase conversion
            String camelLabel = EntityMetadata.snakeToCamel(label);
            if (camelLabel.equalsIgnoreCase(fieldName)) {
                return i;
            }
        }

        return -1;
    }

    // ==================== Inner Classes ====================

    /**
     * Итератор по ResultSet для ленивого маппинга в Stream.
     */
    private static class ResultSetIterator<T> implements Iterator<T> {

        private final ResultSet rs;
        private final Class<T> targetClass;
        private final DefaultResultMapper mapper;
        private int rowNum = 0;
        private Boolean hasNext;

        ResultSetIterator(ResultSet rs, Class<T> targetClass, DefaultResultMapper mapper) {
            this.rs = rs;
            this.targetClass = targetClass;
            this.mapper = mapper;
        }

        @Override
        public boolean hasNext() {
            if (hasNext != null) {
                return hasNext;
            }
            try {
                hasNext = rs.next();
                return hasNext;
            } catch (SQLException e) {
                throw new RuntimeException("Error iterating ResultSet", e);
            }
        }

        @Override
        public T next() {
            if (hasNext == null) {
                hasNext();
            }
            if (!hasNext) {
                throw new java.util.NoSuchElementException();
            }
            hasNext = null; // Reset for next call
            try {
                return mapper.mapCurrentRow(rs, targetClass, ++rowNum);
            } catch (SQLException e) {
                throw new RuntimeException("Error mapping row " + rowNum, e);
            }
        }
    }
}
