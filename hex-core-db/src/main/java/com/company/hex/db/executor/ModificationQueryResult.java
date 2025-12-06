package com.company.hex.db.executor;

import com.company.hex.db.DbException;
import com.company.hex.db.core.QueryResult;
import com.company.hex.db.core.RowMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Результат модифицирующего запроса (INSERT/UPDATE/DELETE).
 * 
 * <p>Содержит информацию о количестве затронутых строк и сгенерированных ключах.
 *
 * @author hex-core-db
 * @since 1.0.0
 */
public class ModificationQueryResult implements QueryResult {

    private final int affectedRows;
    private final Object generatedKey;

    /**
     * Создать результат без сгенерированного ключа.
     *
     * @param affectedRows количество затронутых строк
     */
    public ModificationQueryResult(int affectedRows) {
        this.affectedRows = affectedRows;
        this.generatedKey = null;
    }

    /**
     * Создать результат со сгенерированным ключом.
     *
     * @param affectedRows  количество затронутых строк
     * @param generatedKey  сгенерированный ключ
     */
    public ModificationQueryResult(int affectedRows, Object generatedKey) {
        this.affectedRows = affectedRows;
        this.generatedKey = generatedKey;
    }

    @Override
    public int affectedRows() {
        return affectedRows;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T generatedKey(Class<T> type) {
        if (generatedKey == null) {
            throw new DbException("Сгенерированный ключ недоступен. " 
                + "Убедитесь, что запрос был выполнен с returnGeneratedKeys=true.");
        }
        
        if (type.isInstance(generatedKey)) {
            return (T) generatedKey;
        }
        
        return convertKey(generatedKey, type);
    }

    // === SELECT методы — не поддерживаются ===

    @Override
    public List<Map<String, Object>> toList() {
        throw new UnsupportedOperationException(
            "toList() is not supported for modification queries. Use affectedRows().");
    }

    @Override
    public <T> List<T> toList(RowMapper<T> mapper) {
        throw new UnsupportedOperationException(
            "toList(mapper) is not supported for modification queries. Use affectedRows().");
    }

    @Override
    public Optional<Map<String, Object>> firstRow() {
        throw new UnsupportedOperationException(
            "firstRow() is not supported for modification queries. Use affectedRows().");
    }

    @Override
    public <T> Optional<T> firstRow(RowMapper<T> mapper) {
        throw new UnsupportedOperationException(
            "firstRow(mapper) is not supported for modification queries. Use affectedRows().");
    }

    @Override
    public <T> T scalar(Class<T> type) {
        throw new UnsupportedOperationException(
            "scalar() is not supported for modification queries. Use affectedRows().");
    }

    /**
     * Проверить, был ли затронут хотя бы один ряд.
     *
     * @return true, если affectedRows > 0
     */
    public boolean hasAffectedRows() {
        return affectedRows > 0;
    }

    /**
     * Проверить, доступен ли сгенерированный ключ.
     *
     * @return true, если ключ был сгенерирован
     */
    public boolean hasGeneratedKey() {
        return generatedKey != null;
    }

    @SuppressWarnings("unchecked")
    private <T> T convertKey(Object key, Class<T> type) {
        if (type == Long.class || type == long.class) {
            if (key instanceof Number) {
                return (T) Long.valueOf(((Number) key).longValue());
            }
            if (key instanceof String) {
                return (T) Long.valueOf((String) key);
            }
        }
        
        if (type == Integer.class || type == int.class) {
            if (key instanceof Number) {
                return (T) Integer.valueOf(((Number) key).intValue());
            }
            if (key instanceof String) {
                return (T) Integer.valueOf((String) key);
            }
        }
        
        if (type == String.class) {
            return (T) key.toString();
        }
        
        if (type == BigDecimal.class) {
            if (key instanceof Number) {
                return (T) BigDecimal.valueOf(((Number) key).doubleValue());
            }
            if (key instanceof String) {
                return (T) new BigDecimal((String) key);
            }
        }
        
        throw new DbException(
            "Невозможно преобразовать сгенерированный ключ типа " + key.getClass().getName() 
            + " в " + type.getName());
    }
}
