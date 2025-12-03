package com.company.hex.db.mapping;

import com.company.hex.db.annotations.mapping.Enumerated;
import com.company.hex.db.exception.MappingException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Конвертер типов для преобразования значений из ResultSet в Java типы.
 *
 * <p>Поддерживает все стандартные SQL типы и их маппинг на Java типы.
 * Также поддерживает конвертацию Enum значений.</p>
 *
 * <h2>Поддерживаемые типы</h2>
 * <table>
 *   <tr><th>SQL Type</th><th>Java Type</th></tr>
 *   <tr><td>VARCHAR, CHAR, TEXT</td><td>String</td></tr>
 *   <tr><td>INTEGER, INT</td><td>Integer, int</td></tr>
 *   <tr><td>BIGINT</td><td>Long, long</td></tr>
 *   <tr><td>SMALLINT</td><td>Short, short</td></tr>
 *   <tr><td>TINYINT</td><td>Byte, byte</td></tr>
 *   <tr><td>DECIMAL, NUMERIC</td><td>BigDecimal</td></tr>
 *   <tr><td>FLOAT, REAL</td><td>Float, float</td></tr>
 *   <tr><td>DOUBLE</td><td>Double, double</td></tr>
 *   <tr><td>BOOLEAN, BIT</td><td>Boolean, boolean</td></tr>
 *   <tr><td>TIMESTAMP</td><td>LocalDateTime, Instant, ZonedDateTime</td></tr>
 *   <tr><td>DATE</td><td>LocalDate</td></tr>
 *   <tr><td>TIME</td><td>LocalTime</td></tr>
 *   <tr><td>BLOB, BYTEA</td><td>byte[]</td></tr>
 *   <tr><td>CLOB, TEXT</td><td>String</td></tr>
 *   <tr><td>UUID</td><td>UUID</td></tr>
 * </table>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 */
public final class TypeConverter {

    /**
     * Регистр конвертеров для типов.
     */
    private static final Map<Class<?>, Function<Object, Object>> CONVERTERS = new HashMap<>();

    static {
        // Числовые типы
        registerConverter(Integer.class, TypeConverter::toInteger);
        registerConverter(int.class, TypeConverter::toInteger);
        registerConverter(Long.class, TypeConverter::toLong);
        registerConverter(long.class, TypeConverter::toLong);
        registerConverter(Short.class, TypeConverter::toShort);
        registerConverter(short.class, TypeConverter::toShort);
        registerConverter(Byte.class, TypeConverter::toByte);
        registerConverter(byte.class, TypeConverter::toByte);
        registerConverter(Float.class, TypeConverter::toFloat);
        registerConverter(float.class, TypeConverter::toFloat);
        registerConverter(Double.class, TypeConverter::toDouble);
        registerConverter(double.class, TypeConverter::toDouble);
        registerConverter(BigDecimal.class, TypeConverter::toBigDecimal);
        registerConverter(BigInteger.class, TypeConverter::toBigInteger);

        // Boolean
        registerConverter(Boolean.class, TypeConverter::toBoolean);
        registerConverter(boolean.class, TypeConverter::toBoolean);

        // String
        registerConverter(String.class, TypeConverter::toString);
        registerConverter(Character.class, TypeConverter::toCharacter);
        registerConverter(char.class, TypeConverter::toCharacter);

        // Date/Time
        registerConverter(LocalDateTime.class, TypeConverter::toLocalDateTime);
        registerConverter(LocalDate.class, TypeConverter::toLocalDate);
        registerConverter(LocalTime.class, TypeConverter::toLocalTime);
        registerConverter(Instant.class, TypeConverter::toInstant);
        registerConverter(ZonedDateTime.class, TypeConverter::toZonedDateTime);
        registerConverter(OffsetDateTime.class, TypeConverter::toOffsetDateTime);
        registerConverter(java.util.Date.class, TypeConverter::toUtilDate);
        registerConverter(java.sql.Date.class, TypeConverter::toSqlDate);
        registerConverter(Timestamp.class, TypeConverter::toTimestamp);
        registerConverter(Time.class, TypeConverter::toTime);

        // Binary
        registerConverter(byte[].class, TypeConverter::toByteArray);

        // UUID
        registerConverter(UUID.class, TypeConverter::toUUID);
    }

    private TypeConverter() {
        // Utility class
    }

    private static void registerConverter(Class<?> type, Function<Object, Object> converter) {
        CONVERTERS.put(type, converter);
    }

    /**
     * Конвертирует значение из ResultSet в указанный Java тип.
     *
     * @param <T> целевой тип
     * @param value значение из ResultSet
     * @param targetType целевой класс
     * @return конвертированное значение
     * @throws MappingException если конвертация невозможна
     */
    @SuppressWarnings("unchecked")
    public static <T> T convert(Object value, Class<T> targetType) {
        if (value == null) {
            return getDefaultValue(targetType);
        }

        // Если типы совпадают — возвращаем как есть
        if (targetType.isInstance(value)) {
            return (T) value;
        }

        // Enum требует специальной обработки
        if (targetType.isEnum()) {
            return (T) toEnum(value, (Class<? extends Enum>) targetType, Enumerated.EnumType.STRING);
        }

        // Ищем конвертер
        Function<Object, Object> converter = CONVERTERS.get(targetType);
        if (converter != null) {
            try {
                return (T) converter.apply(value);
            } catch (Exception e) {
                throw new MappingException(
                    "Failed to convert value '" + value + "' (" + value.getClass().getSimpleName()
                    + ") to " + targetType.getSimpleName(),
                    e
                );
            }
        }

        // Пробуем привести напрямую
        try {
            return targetType.cast(value);
        } catch (ClassCastException e) {
            throw new MappingException(
                "Cannot convert value '" + value + "' (" + value.getClass().getSimpleName()
                + ") to " + targetType.getSimpleName(),
                e
            );
        }
    }

    /**
     * Конвертирует значение в Enum.
     *
     * @param <E> тип Enum
     * @param value значение (String или Integer)
     * @param enumClass класс Enum
     * @param enumType тип маппинга (STRING или ORDINAL)
     * @return значение Enum
     */
    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>> E toEnum(Object value, Class<E> enumClass,
                                                Enumerated.EnumType enumType) {
        if (value == null) {
            return null;
        }

        if (enumClass.isInstance(value)) {
            return (E) value;
        }

        E[] constants = enumClass.getEnumConstants();

        // Маппинг по ordinal
        if (enumType == Enumerated.EnumType.ORDINAL) {
            int ordinal;
            if (value instanceof Number num) {
                ordinal = num.intValue();
            } else {
                ordinal = Integer.parseInt(value.toString());
            }

            if (ordinal >= 0 && ordinal < constants.length) {
                return constants[ordinal];
            }
            throw new MappingException(
                "Invalid ordinal " + ordinal + " for enum " + enumClass.getSimpleName()
                + ". Valid range: 0-" + (constants.length - 1)
            );
        }

        // Маппинг по имени (STRING) — по умолчанию
        String name = value.toString().trim();
        for (E constant : constants) {
            if (constant.name().equalsIgnoreCase(name)) {
                return constant;
            }
        }

        throw new MappingException(
            "Unknown enum value '" + name + "' for " + enumClass.getSimpleName()
            + ". Valid values: " + java.util.Arrays.toString(constants)
        );
    }

    /**
     * Возвращает значение по умолчанию для примитивных типов.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getDefaultValue(Class<T> type) {
        if (!type.isPrimitive()) {
            return null;
        }

        if (type == int.class) return (T) Integer.valueOf(0);
        if (type == long.class) return (T) Long.valueOf(0L);
        if (type == double.class) return (T) Double.valueOf(0.0);
        if (type == float.class) return (T) Float.valueOf(0.0f);
        if (type == boolean.class) return (T) Boolean.FALSE;
        if (type == short.class) return (T) Short.valueOf((short) 0);
        if (type == byte.class) return (T) Byte.valueOf((byte) 0);
        if (type == char.class) return (T) Character.valueOf('\0');

        return null;
    }

    /**
     * Проверяет, является ли тип скалярным.
     */
    public static boolean isScalarType(Class<?> type) {
        return type.isPrimitive()
            || type == String.class
            || Number.class.isAssignableFrom(type)
            || type == Boolean.class
            || type == Character.class
            || type == LocalDateTime.class
            || type == LocalDate.class
            || type == LocalTime.class
            || type == Instant.class
            || type == UUID.class
            || type == byte[].class
            || type.isEnum();
    }

    /**
     * Читает значение из ResultSet и конвертирует в указанный тип.
     *
     * @param rs ResultSet
     * @param columnIndex индекс колонки (1-based)
     * @param targetType целевой тип
     * @return конвертированное значение
     */
    public static <T> T readValue(ResultSet rs, int columnIndex, Class<T> targetType)
            throws SQLException {
        Object value = rs.getObject(columnIndex);
        if (rs.wasNull()) {
            return getDefaultValue(targetType);
        }
        return convert(value, targetType);
    }

    /**
     * Читает значение из ResultSet по имени колонки.
     */
    public static <T> T readValue(ResultSet rs, String columnName, Class<T> targetType)
            throws SQLException {
        Object value = rs.getObject(columnName);
        if (rs.wasNull()) {
            return getDefaultValue(targetType);
        }
        return convert(value, targetType);
    }

    // ==================== Конвертеры ====================

    private static Object toInteger(Object value) {
        if (value instanceof Number num) {
            return num.intValue();
        }
        if (value instanceof String str) {
            return Integer.parseInt(str.trim());
        }
        if (value instanceof Boolean bool) {
            return bool ? 1 : 0;
        }
        throw new IllegalArgumentException("Cannot convert to Integer: " + value);
    }

    private static Object toLong(Object value) {
        if (value instanceof Number num) {
            return num.longValue();
        }
        if (value instanceof String str) {
            return Long.parseLong(str.trim());
        }
        if (value instanceof java.util.Date date) {
            return date.getTime();
        }
        throw new IllegalArgumentException("Cannot convert to Long: " + value);
    }

    private static Object toShort(Object value) {
        if (value instanceof Number num) {
            return num.shortValue();
        }
        if (value instanceof String str) {
            return Short.parseShort(str.trim());
        }
        throw new IllegalArgumentException("Cannot convert to Short: " + value);
    }

    private static Object toByte(Object value) {
        if (value instanceof Number num) {
            return num.byteValue();
        }
        if (value instanceof String str) {
            return Byte.parseByte(str.trim());
        }
        throw new IllegalArgumentException("Cannot convert to Byte: " + value);
    }

    private static Object toFloat(Object value) {
        if (value instanceof Number num) {
            return num.floatValue();
        }
        if (value instanceof String str) {
            return Float.parseFloat(str.trim());
        }
        throw new IllegalArgumentException("Cannot convert to Float: " + value);
    }

    private static Object toDouble(Object value) {
        if (value instanceof Number num) {
            return num.doubleValue();
        }
        if (value instanceof String str) {
            return Double.parseDouble(str.trim());
        }
        throw new IllegalArgumentException("Cannot convert to Double: " + value);
    }

    private static Object toBigDecimal(Object value) {
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        if (value instanceof Number num) {
            if (num instanceof BigInteger bi) {
                return new BigDecimal(bi);
            }
            return BigDecimal.valueOf(num.doubleValue());
        }
        if (value instanceof String str) {
            return new BigDecimal(str.trim());
        }
        throw new IllegalArgumentException("Cannot convert to BigDecimal: " + value);
    }

    private static Object toBigInteger(Object value) {
        if (value instanceof BigInteger bi) {
            return bi;
        }
        if (value instanceof BigDecimal bd) {
            return bd.toBigInteger();
        }
        if (value instanceof Number num) {
            return BigInteger.valueOf(num.longValue());
        }
        if (value instanceof String str) {
            return new BigInteger(str.trim());
        }
        throw new IllegalArgumentException("Cannot convert to BigInteger: " + value);
    }

    private static Object toBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof Number num) {
            return num.intValue() != 0;
        }
        if (value instanceof String str) {
            String s = str.trim().toLowerCase();
            return "true".equals(s) || "1".equals(s) || "yes".equals(s) || "y".equals(s);
        }
        throw new IllegalArgumentException("Cannot convert to Boolean: " + value);
    }

    private static Object toString(Object value) {
        if (value instanceof Clob clob) {
            try {
                return clob.getSubString(1, (int) clob.length());
            } catch (SQLException e) {
                throw new RuntimeException("Failed to read CLOB", e);
            }
        }
        if (value instanceof byte[] bytes) {
            return new String(bytes);
        }
        return value.toString();
    }

    private static Object toCharacter(Object value) {
        if (value instanceof Character ch) {
            return ch;
        }
        if (value instanceof String str && !str.isEmpty()) {
            return str.charAt(0);
        }
        if (value instanceof Number num) {
            return (char) num.intValue();
        }
        throw new IllegalArgumentException("Cannot convert to Character: " + value);
    }

    private static Object toLocalDateTime(Object value) {
        if (value instanceof LocalDateTime ldt) {
            return ldt;
        }
        if (value instanceof Timestamp ts) {
            return ts.toLocalDateTime();
        }
        if (value instanceof java.util.Date date) {
            return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        }
        if (value instanceof String str) {
            return LocalDateTime.parse(str.trim());
        }
        if (value instanceof Long millis) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
        }
        throw new IllegalArgumentException("Cannot convert to LocalDateTime: " + value);
    }

    private static Object toLocalDate(Object value) {
        if (value instanceof LocalDate ld) {
            return ld;
        }
        if (value instanceof Date date) {
            return date.toLocalDate();
        }
        if (value instanceof Timestamp ts) {
            return ts.toLocalDateTime().toLocalDate();
        }
        if (value instanceof java.util.Date date) {
            return LocalDate.ofInstant(date.toInstant(), ZoneId.systemDefault());
        }
        if (value instanceof String str) {
            return LocalDate.parse(str.trim());
        }
        throw new IllegalArgumentException("Cannot convert to LocalDate: " + value);
    }

    private static Object toLocalTime(Object value) {
        if (value instanceof LocalTime lt) {
            return lt;
        }
        if (value instanceof Time time) {
            return time.toLocalTime();
        }
        if (value instanceof Timestamp ts) {
            return ts.toLocalDateTime().toLocalTime();
        }
        if (value instanceof String str) {
            return LocalTime.parse(str.trim());
        }
        throw new IllegalArgumentException("Cannot convert to LocalTime: " + value);
    }

    private static Object toInstant(Object value) {
        if (value instanceof Instant instant) {
            return instant;
        }
        if (value instanceof Timestamp ts) {
            return ts.toInstant();
        }
        if (value instanceof java.util.Date date) {
            return date.toInstant();
        }
        if (value instanceof Long millis) {
            return Instant.ofEpochMilli(millis);
        }
        if (value instanceof String str) {
            return Instant.parse(str.trim());
        }
        throw new IllegalArgumentException("Cannot convert to Instant: " + value);
    }

    private static Object toZonedDateTime(Object value) {
        if (value instanceof ZonedDateTime zdt) {
            return zdt;
        }
        if (value instanceof Timestamp ts) {
            return ts.toInstant().atZone(ZoneId.systemDefault());
        }
        if (value instanceof java.util.Date date) {
            return date.toInstant().atZone(ZoneId.systemDefault());
        }
        if (value instanceof String str) {
            return ZonedDateTime.parse(str.trim());
        }
        throw new IllegalArgumentException("Cannot convert to ZonedDateTime: " + value);
    }

    private static Object toOffsetDateTime(Object value) {
        if (value instanceof OffsetDateTime odt) {
            return odt;
        }
        if (value instanceof ZonedDateTime zdt) {
            return zdt.toOffsetDateTime();
        }
        if (value instanceof Timestamp ts) {
            return ts.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        }
        if (value instanceof String str) {
            return OffsetDateTime.parse(str.trim());
        }
        throw new IllegalArgumentException("Cannot convert to OffsetDateTime: " + value);
    }

    private static Object toUtilDate(Object value) {
        if (value instanceof java.util.Date date) {
            return date;
        }
        if (value instanceof Long millis) {
            return new java.util.Date(millis);
        }
        throw new IllegalArgumentException("Cannot convert to java.util.Date: " + value);
    }

    private static Object toSqlDate(Object value) {
        if (value instanceof Date date) {
            return date;
        }
        if (value instanceof LocalDate ld) {
            return Date.valueOf(ld);
        }
        if (value instanceof java.util.Date date) {
            return new Date(date.getTime());
        }
        if (value instanceof Long millis) {
            return new Date(millis);
        }
        throw new IllegalArgumentException("Cannot convert to java.sql.Date: " + value);
    }

    private static Object toTimestamp(Object value) {
        if (value instanceof Timestamp ts) {
            return ts;
        }
        if (value instanceof LocalDateTime ldt) {
            return Timestamp.valueOf(ldt);
        }
        if (value instanceof java.util.Date date) {
            return new Timestamp(date.getTime());
        }
        if (value instanceof Long millis) {
            return new Timestamp(millis);
        }
        throw new IllegalArgumentException("Cannot convert to Timestamp: " + value);
    }

    private static Object toTime(Object value) {
        if (value instanceof Time time) {
            return time;
        }
        if (value instanceof LocalTime lt) {
            return Time.valueOf(lt);
        }
        throw new IllegalArgumentException("Cannot convert to Time: " + value);
    }

    private static Object toByteArray(Object value) {
        if (value instanceof byte[] bytes) {
            return bytes;
        }
        if (value instanceof Blob blob) {
            try {
                return blob.getBytes(1, (int) blob.length());
            } catch (SQLException e) {
                throw new RuntimeException("Failed to read BLOB", e);
            }
        }
        if (value instanceof String str) {
            return str.getBytes();
        }
        throw new IllegalArgumentException("Cannot convert to byte[]: " + value);
    }

    private static Object toUUID(Object value) {
        if (value instanceof UUID uuid) {
            return uuid;
        }
        if (value instanceof String str) {
            return UUID.fromString(str.trim());
        }
        if (value instanceof byte[] bytes && bytes.length == 16) {
            long msb = 0;
            long lsb = 0;
            for (int i = 0; i < 8; i++) {
                msb = (msb << 8) | (bytes[i] & 0xff);
            }
            for (int i = 8; i < 16; i++) {
                lsb = (lsb << 8) | (bytes[i] & 0xff);
            }
            return new UUID(msb, lsb);
        }
        throw new IllegalArgumentException("Cannot convert to UUID: " + value);
    }
}
