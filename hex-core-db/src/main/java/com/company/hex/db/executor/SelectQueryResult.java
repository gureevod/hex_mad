package com.company.hex.db.executor;

import com.company.hex.db.DbException;
import com.company.hex.db.core.QueryResult;
import com.company.hex.db.core.Row;
import com.company.hex.db.core.RowMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Результат SELECT-запроса.
 * 
 * <p>Все данные загружаются в память при создании объекта (eager loading).
 * После создания все JDBC-ресурсы уже закрыты — никаких утечек.
 *
 * @author hex-core-db
 * @since 1.0.0
 */
public class SelectQueryResult implements QueryResult {

    private final List<Row> rows;

    /**
     * Создать результат из списка строк.
     *
     * @param rows список строк результата
     */
    public SelectQueryResult(List<Row> rows) {
        this.rows = rows != null ? new ArrayList<>(rows) : Collections.emptyList();
    }

    /**
     * Создать результат из списка Map.
     *
     * @param data список данных в виде Map
     * @return результат запроса
     */
    public static SelectQueryResult fromMaps(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) {
            return new SelectQueryResult(Collections.emptyList());
        }
        
        List<Row> rows = new ArrayList<>(data.size());
        for (Map<String, Object> map : data) {
            rows.add(new MapRow(map));
        }
        return new SelectQueryResult(rows);
    }

    @Override
    public List<Map<String, Object>> toList() {
        List<Map<String, Object>> result = new ArrayList<>(rows.size());
        for (Row row : rows) {
            result.add(row.toMap());
        }
        return result;
    }

    @Override
    public <T> List<T> toList(RowMapper<T> mapper) {
        List<T> result = new ArrayList<>(rows.size());
        for (Row row : rows) {
            result.add(mapper.map(row));
        }
        return result;
    }

    @Override
    public Optional<Map<String, Object>> firstRow() {
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(rows.get(0).toMap());
    }

    @Override
    public <T> Optional<T> firstRow(RowMapper<T> mapper) {
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(mapper.map(rows.get(0)));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T scalar(Class<T> type) {
        if (rows.isEmpty()) {
            throw new DbException("Невозможно получить скалярное значение: результат пуст");
        }
        
        Row firstRow = rows.get(0);
        Map<String, Object> data = firstRow.toMap();
        
        if (data.isEmpty()) {
            throw new DbException("Невозможно получить скалярное значение: строка не содержит колонок");
        }
        
        // Получаем первую колонку
        Object value = data.values().iterator().next();
        
        if (value == null) {
            return null;
        }
        
        if (type.isInstance(value)) {
            return (T) value;
        }
        
        // Конвертация типов
        return convertScalar(value, type);
    }

    @Override
    public int affectedRows() {
        throw new UnsupportedOperationException(
            "affectedRows() is not supported for SELECT queries. Use toList() or firstRow().");
    }

    @Override
    public <T> T generatedKey(Class<T> type) {
        throw new UnsupportedOperationException(
            "generatedKey() is not supported for SELECT queries.");
    }

    /**
     * Получить количество строк в результате.
     *
     * @return количество строк
     */
    public int size() {
        return rows.size();
    }

    /**
     * Проверить, пуст ли результат.
     *
     * @return true, если результат не содержит строк
     */
    public boolean isEmpty() {
        return rows.isEmpty();
    }

    @SuppressWarnings("unchecked")
    private <T> T convertScalar(Object value, Class<T> type) {
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
        
        if (type == Double.class || type == double.class) {
            if (value instanceof Number) {
                return (T) Double.valueOf(((Number) value).doubleValue());
            }
            if (value instanceof String) {
                return (T) Double.valueOf((String) value);
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
        
        if (type == Boolean.class || type == boolean.class) {
            if (value instanceof Number) {
                return (T) Boolean.valueOf(((Number) value).intValue() != 0);
            }
            if (value instanceof String) {
                return (T) Boolean.valueOf((String) value);
            }
        }
        
        throw new DbException(
            "Невозможно преобразовать скалярное значение типа " + value.getClass().getName() 
            + " в " + type.getName());
    }
}
