package com.company.hex.db.executor;

import com.company.hex.db.DbException;
import com.company.hex.db.core.Query;
import com.company.hex.db.core.QueryExecutor;
import com.company.hex.db.core.QueryInterceptor;
import com.company.hex.db.core.QueryResult;
import com.company.hex.db.core.QueryType;
import com.company.hex.db.core.Row;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

/**
 * Стандартная реализация QueryExecutor.
 * 
 * <p><b>Потокобезопасность:</b> этот класс потокобезопасен.
 * Каждый запрос получает собственное соединение из пула и возвращает его после выполнения.
 *
 * <p>Особенности:
 * <ul>
 *   <li>Поддержка именованных параметров в формате {@code :paramName}</li>
 *   <li>Eager loading всех данных — никаких открытых ресурсов после execute()</li>
 *   <li>Автоматическое определение типа запроса по первому ключевому слову</li>
 *   <li>Поддержка интерцепторов для логирования и модификации запросов</li>
 * </ul>
 *
 * @author hex-core-db
 * @since 1.0.0
 */
public class DefaultQueryExecutor implements QueryExecutor {

    private static final Logger log = LoggerFactory.getLogger(DefaultQueryExecutor.class);
    private static final Pattern NAMED_PARAM_PATTERN = Pattern.compile(":([a-zA-Z_][a-zA-Z0-9_]*)");

    private final DataSource dataSource;
    private final QueryInterceptor interceptor;

    /**
     * Создать executor с DataSource и интерцептором.
     *
     * @param dataSource  источник соединений (обычно HikariDataSource)
     * @param interceptor интерцептор для обработки запросов (может быть null)
     */
    public DefaultQueryExecutor(DataSource dataSource, QueryInterceptor interceptor) {
        if (dataSource == null) {
            throw new IllegalArgumentException("DataSource cannot be null");
        }
        this.dataSource = dataSource;
        this.interceptor = interceptor != null ? interceptor : QueryInterceptor.noOp();
    }

    /**
     * Создать executor только с DataSource.
     *
     * @param dataSource источник соединений
     */
    public DefaultQueryExecutor(DataSource dataSource) {
        this(dataSource, null);
    }

    @Override
    public QueryResult execute(Query query) {
        Query interceptedQuery = interceptor.beforeExecute(query);
        
        try (Connection connection = dataSource.getConnection()) {
            return executeQuery(connection, interceptedQuery);
        } catch (SQLException e) {
            throw new DbException("Ошибка выполнения запроса: " + interceptedQuery.sql(), e);
        }
    }

    @Override
    public QueryResult execute(String sql, Map<String, Object> params) {
        QueryType type = SimpleQuery.detectType(sql);
        Query query = new SimpleQuery(sql, params, type, false);
        return execute(query);
    }

    @Override
    public QueryResult execute(String sql) {
        return execute(sql, Collections.emptyMap());
    }

    @Override
    public int[] executeBatch(String sql, List<Map<String, Object>> paramsList) {
        if (paramsList == null || paramsList.isEmpty()) {
            return new int[0];
        }

        ParsedSql parsedSql = parseNamedParameters(sql, paramsList.get(0));
        
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            
            try (PreparedStatement stmt = connection.prepareStatement(parsedSql.sql)) {
                for (Map<String, Object> params : paramsList) {
                    setParameters(stmt, parsedSql.parameterNames, params);
                    stmt.addBatch();
                }
                
                int[] results = stmt.executeBatch();
                connection.commit();
                return results;
                
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DbException("Ошибка batch-выполнения: " + sql, e);
        }
    }

    private QueryResult executeQuery(Connection connection, Query query) throws SQLException {
        ParsedSql parsedSql = parseNamedParameters(query.sql(), query.parameters());
        
        QueryType type = query.type();
        boolean returnKeys = query.returnGeneratedKeys();
        
        if (type == QueryType.SELECT) {
            return executeSelect(connection, parsedSql, query.parameters());
        } else {
            return executeModification(connection, parsedSql, query.parameters(), returnKeys);
        }
    }

    private QueryResult executeSelect(Connection connection, ParsedSql parsedSql, 
                                       Map<String, Object> params) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(parsedSql.sql)) {
            setParameters(stmt, parsedSql.parameterNames, params);
            
            try (ResultSet rs = stmt.executeQuery()) {
                List<Row> rows = readAllRows(rs);
                return new SelectQueryResult(rows);
            }
        }
    }

    private QueryResult executeModification(Connection connection, ParsedSql parsedSql,
                                             Map<String, Object> params, 
                                             boolean returnKeys) throws SQLException {
        int stmtType = returnKeys 
            ? Statement.RETURN_GENERATED_KEYS 
            : Statement.NO_GENERATED_KEYS;
        
        try (PreparedStatement stmt = connection.prepareStatement(parsedSql.sql, stmtType)) {
            setParameters(stmt, parsedSql.parameterNames, params);
            
            int affectedRows = stmt.executeUpdate();
            
            Object generatedKey = null;
            if (returnKeys) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        generatedKey = keys.getObject(1);
                    }
                }
            }
            
            return new ModificationQueryResult(affectedRows, generatedKey);
        }
    }

    private List<Row> readAllRows(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        String[] columnNames = new String[columnCount];
        
        for (int i = 0; i < columnCount; i++) {
            columnNames[i] = metaData.getColumnLabel(i + 1).toLowerCase();
        }
        
        List<Row> rows = new ArrayList<>();
        
        while (rs.next()) {
            Map<String, Object> rowData = new LinkedHashMap<>();
            for (int i = 0; i < columnCount; i++) {
                rowData.put(columnNames[i], rs.getObject(i + 1));
            }
            rows.add(new MapRow(rowData));
        }
        
        return rows;
    }

    private void setParameters(PreparedStatement stmt, List<String> paramNames, 
                                Map<String, Object> params) throws SQLException {
        for (int i = 0; i < paramNames.size(); i++) {
            String paramName = paramNames.get(i);
            Object value = params.get(paramName);
            stmt.setObject(i + 1, value);
        }
    }

    /**
     * Разобрать SQL с именованными параметрами.
     * Преобразует :paramName в ? и сохраняет порядок параметров.
     */
    private ParsedSql parseNamedParameters(String sql, Map<String, Object> params) {
        if (sql == null || sql.isEmpty()) {
            return new ParsedSql(sql, Collections.emptyList());
        }
        
        List<String> parameterNames = new ArrayList<>();
        Matcher matcher = NAMED_PARAM_PATTERN.matcher(sql);
        StringBuilder result = new StringBuilder();
        int lastEnd = 0;
        
        while (matcher.find()) {
            result.append(sql, lastEnd, matcher.start());
            result.append("?");
            parameterNames.add(matcher.group(1));
            lastEnd = matcher.end();
        }
        
        result.append(sql.substring(lastEnd));
        
        return new ParsedSql(result.toString(), parameterNames);
    }

    /**
     * Результат парсинга SQL с именованными параметрами.
     */
    private static class ParsedSql {
        final String sql;
        final List<String> parameterNames;
        
        ParsedSql(String sql, List<String> parameterNames) {
            this.sql = sql;
            this.parameterNames = parameterNames;
        }
    }
}
