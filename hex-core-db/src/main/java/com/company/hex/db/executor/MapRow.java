package com.company.hex.db.executor;

import com.company.hex.db.DbException;
import com.company.hex.db.core.Row;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Реализация Row на основе Map.
 * 
 * <p>Хранит данные строки в виде Map с именами колонок в нижнем регистре.
 * Обеспечивает автоматическую конвертацию типов при получении значений.
 *
 * @author hex-core-db
 * @since 1.0.0
 */
public class MapRow implements Row {

    private final Map<String, Object> data;

    /**
     * Создать строку из Map.
     * 
     * <p>Имена колонок приводятся к нижнему регистру.
     *
     * @param data данные строки
     */
    public MapRow(Map<String, Object> data) {
        this.data = normalizeKeys(data);
    }

    private static Map<String, Object> normalizeKeys(Map<String, Object> original) {
        if (original == null || original.isEmpty()) {
            return Collections.emptyMap();
        }
        
        Map<String, Object> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : original.entrySet()) {
            String key = entry.getKey();
            if (key != null) {
                normalized.put(key.toLowerCase(Locale.ROOT), entry.getValue());
            }
        }
        return normalized;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String column, Class<T> type) {
        Object value = data.get(normalizeColumn(column));
        
        if (value == null) {
            return null;
        }
        
        if (type.isInstance(value)) {
            return (T) value;
        }
        
        return convertValue(value, type);
    }

    @Override
    public String getString(String column) {
        Object value = data.get(normalizeColumn(column));
        return value != null ? value.toString() : null;
    }

    @Override
    public Long getLong(String column) {
        Object value = data.get(normalizeColumn(column));
        
        if (value == null) {
            return null;
        }
        
        if (value instanceof Long) {
            return (Long) value;
        }
        
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                throw new DbException(
                    "Невозможно преобразовать значение '" + value + "' в Long для колонки '" + column + "'", e);
            }
        }
        
        throw new DbException(
            "Невозможно преобразовать " + value.getClass().getName() + " в Long для колонки '" + column + "'");
    }

    @Override
    public Integer getInt(String column) {
        Object value = data.get(normalizeColumn(column));
        
        if (value == null) {
            return null;
        }
        
        if (value instanceof Integer) {
            return (Integer) value;
        }
        
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                throw new DbException(
                    "Невозможно преобразовать значение '" + value + "' в Integer для колонки '" + column + "'", e);
            }
        }
        
        throw new DbException(
            "Невозможно преобразовать " + value.getClass().getName() + " в Integer для колонки '" + column + "'");
    }

    @Override
    public Boolean getBoolean(String column) {
        Object value = data.get(normalizeColumn(column));
        
        if (value == null) {
            return null;
        }
        
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        
        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        
        if (value instanceof String) {
            String strValue = ((String) value).toLowerCase(Locale.ROOT);
            return "true".equals(strValue) || "1".equals(strValue) 
                || "yes".equals(strValue) || "y".equals(strValue);
        }
        
        throw new DbException(
            "Невозможно преобразовать " + value.getClass().getName() + " в Boolean для колонки '" + column + "'");
    }

    @Override
    public BigDecimal getBigDecimal(String column) {
        Object value = data.get(normalizeColumn(column));
        
        if (value == null) {
            return null;
        }
        
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        
        if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                throw new DbException(
                    "Невозможно преобразовать значение '" + value + "' в BigDecimal для колонки '" + column + "'", e);
            }
        }
        
        throw new DbException(
            "Невозможно преобразовать " + value.getClass().getName() + " в BigDecimal для колонки '" + column + "'");
    }

    @Override
    public LocalDateTime getLocalDateTime(String column) {
        Object value = data.get(normalizeColumn(column));
        
        if (value == null) {
            return null;
        }
        
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        
        if (value instanceof Timestamp) {
            return ((Timestamp) value).toLocalDateTime();
        }
        
        if (value instanceof String) {
            try {
                return LocalDateTime.parse((String) value);
            } catch (Exception e) {
                throw new DbException(
                    "Невозможно преобразовать значение '" + value + "' в LocalDateTime для колонки '" + column + "'", e);
            }
        }
        
        throw new DbException(
            "Невозможно преобразовать " + value.getClass().getName() + " в LocalDateTime для колонки '" + column + "'");
    }

    @Override
    public LocalDate getLocalDate(String column) {
        Object value = data.get(normalizeColumn(column));
        
        if (value == null) {
            return null;
        }
        
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        }
        
        if (value instanceof Date) {
            return ((Date) value).toLocalDate();
        }
        
        if (value instanceof Timestamp) {
            return ((Timestamp) value).toLocalDateTime().toLocalDate();
        }
        
        if (value instanceof String) {
            try {
                return LocalDate.parse((String) value);
            } catch (Exception e) {
                throw new DbException(
                    "Невозможно преобразовать значение '" + value + "' в LocalDate для колонки '" + column + "'", e);
            }
        }
        
        throw new DbException(
            "Невозможно преобразовать " + value.getClass().getName() + " в LocalDate для колонки '" + column + "'");
    }

    @Override
    public Map<String, Object> toMap() {
        return Collections.unmodifiableMap(data);
    }

    private String normalizeColumn(String column) {
        return column != null ? column.toLowerCase(Locale.ROOT) : null;
    }

    @SuppressWarnings("unchecked")
    private <T> T convertValue(Object value, Class<T> type) {
        if (type == String.class) {
            return (T) value.toString();
        }
        
        if (type == Long.class || type == long.class) {
            if (value instanceof Number) {
                return (T) Long.valueOf(((Number) value).longValue());
            }
            if (value instanceof String) {
                return (T) Long.valueOf((String) value);
            }
        }
        
        if (type == Integer.class || type == int.class) {
            if (value instanceof Number) {
                return (T) Integer.valueOf(((Number) value).intValue());
            }
            if (value instanceof String) {
                return (T) Integer.valueOf((String) value);
            }
        }
        
        if (type == Boolean.class || type == boolean.class) {
            if (value instanceof Number) {
                return (T) Boolean.valueOf(((Number) value).intValue() != 0);
            }
            if (value instanceof String) {
                String strValue = ((String) value).toLowerCase(Locale.ROOT);
                return (T) Boolean.valueOf(
                    "true".equals(strValue) || "1".equals(strValue) 
                    || "yes".equals(strValue) || "y".equals(strValue));
            }
        }
        
        if (type == BigDecimal.class) {
            if (value instanceof Number) {
                return (T) BigDecimal.valueOf(((Number) value).doubleValue());
            }
            if (value instanceof String) {
                return (T) new BigDecimal((String) value);
            }
        }
        
        if (type == LocalDateTime.class) {
            if (value instanceof Timestamp) {
                return (T) ((Timestamp) value).toLocalDateTime();
            }
        }
        
        if (type == LocalDate.class) {
            if (value instanceof Date) {
                return (T) ((Date) value).toLocalDate();
            }
            if (value instanceof Timestamp) {
                return (T) ((Timestamp) value).toLocalDateTime().toLocalDate();
            }
        }
        
        throw new DbException(
            "Невозможно преобразовать " + value.getClass().getName() + " в " + type.getName());
    }
}
