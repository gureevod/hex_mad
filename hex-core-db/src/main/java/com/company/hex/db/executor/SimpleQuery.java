package com.company.hex.db.executor;

import com.company.hex.db.core.Query;
import com.company.hex.db.core.QueryType;

import java.util.Collections;
import java.util.Map;

/**
 * Простая реализация Query для внутреннего использования.
 *
 * @author hex-core-db
 * @since 1.0.0
 */
public final class SimpleQuery implements Query {

    private final String sql;
    private final Map<String, Object> parameters;
    private final QueryType type;
    private final boolean returnGeneratedKeys;

    /**
     * Создать запрос со всеми параметрами.
     *
     * @param sql                 SQL-строка
     * @param parameters          параметры запроса
     * @param type                тип запроса
     * @param returnGeneratedKeys флаг возврата ключей
     */
    public SimpleQuery(String sql, Map<String, Object> parameters, 
                       QueryType type, boolean returnGeneratedKeys) {
        this.sql = sql;
        this.parameters = parameters != null 
            ? Collections.unmodifiableMap(parameters) 
            : Collections.emptyMap();
        this.type = type;
        this.returnGeneratedKeys = returnGeneratedKeys;
    }

    /**
     * Создать запрос без возврата ключей.
     *
     * @param sql        SQL-строка
     * @param parameters параметры запроса
     * @param type       тип запроса
     */
    public SimpleQuery(String sql, Map<String, Object> parameters, QueryType type) {
        this(sql, parameters, type, false);
    }

    /**
     * Создать запрос без параметров.
     *
     * @param sql  SQL-строка
     * @param type тип запроса
     */
    public SimpleQuery(String sql, QueryType type) {
        this(sql, Collections.emptyMap(), type, false);
    }

    @Override
    public String sql() {
        return sql;
    }

    @Override
    public Map<String, Object> parameters() {
        return parameters;
    }

    @Override
    public QueryType type() {
        return type;
    }

    @Override
    public boolean returnGeneratedKeys() {
        return returnGeneratedKeys;
    }

    /**
     * Определить тип запроса по первому ключевому слову SQL.
     *
     * @param sql SQL-строка
     * @return определённый тип запроса
     */
    public static QueryType detectType(String sql) {
        if (sql == null || sql.isBlank()) {
            return QueryType.SCRIPT;
        }
        
        String trimmed = sql.trim().toUpperCase();
        
        if (trimmed.startsWith("SELECT") || trimmed.startsWith("WITH")) {
            return QueryType.SELECT;
        } else if (trimmed.startsWith("INSERT")) {
            return QueryType.INSERT;
        } else if (trimmed.startsWith("UPDATE")) {
            return QueryType.UPDATE;
        } else if (trimmed.startsWith("DELETE")) {
            return QueryType.DELETE;
        } else {
            return QueryType.SCRIPT;
        }
    }
}
