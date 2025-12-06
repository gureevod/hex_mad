package com.company.hex.db.builders;

import com.company.hex.db.DbException;
import com.company.hex.db.core.QueryExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Билдер для batch INSERT-операций.
 *
 * <p>Позволяет эффективно вставлять множество строк за одну batch-операцию.
 *
 * <p><b>НЕ потокобезопасен</b> — создавайте новый экземпляр в каждом потоке.
 *
 * <p>Пример использования:
 * <pre>{@code
 * int[] affected = SQL.batchInsertInto("users")
 *     .columns("name", "email", "active")
 *     .row("John", "john@example.com", true)
 *     .row("Jane", "jane@example.com", true)
 *     .row("Bob", "bob@example.com", false)
 *     .execute(executor);
 * }</pre>
 *
 * @author hex-core-db
 * @since 1.0.0
 */
public class BatchInsertBuilder {

    private final String table;
    private List<String> columns = new ArrayList<>();
    private final List<Object[]> rows = new ArrayList<>();

    /**
     * Создать BatchInsertBuilder для указанной таблицы.
     *
     * @param table имя таблицы
     */
    public BatchInsertBuilder(String table) {
        this.table = table;
    }

    /**
     * Указать колонки для вставки.
     *
     * @param columnNames имена колонок
     * @return этот билдер для цепочки вызовов
     */
    public BatchInsertBuilder columns(String... columnNames) {
        this.columns = Arrays.asList(columnNames);
        return this;
    }

    /**
     * Добавить строку значений в batch.
     * Значения должны соответствовать порядку и количеству указанных колонок.
     *
     * @param values значения для этой строки
     * @return этот билдер для цепочки вызовов
     * @throws DbException если колонки не указаны или количество значений не совпадает
     */
    public BatchInsertBuilder row(Object... values) {
        if (columns.isEmpty()) {
            throw new DbException("Колонки должны быть указаны перед добавлением строк");
        }
        if (values.length != columns.size()) {
            throw new DbException(
                "Количество значений (" + values.length + ") не совпадает с количеством колонок (" + columns.size() + ")");
        }
        rows.add(values);
        return this;
    }

    /**
     * Добавить несколько строк сразу.
     *
     * @param rowValues список массивов значений, каждый соответствует количеству колонок
     * @return этот билдер для цепочки вызовов
     */
    public BatchInsertBuilder rows(List<Object[]> rowValues) {
        for (Object[] values : rowValues) {
            row(values);
        }
        return this;
    }

    /**
     * Получить имя таблицы.
     */
    public String getTable() {
        return table;
    }

    /**
     * Получить колонки.
     */
    public List<String> getColumns() {
        return columns;
    }

    /**
     * Получить строки.
     */
    public List<Object[]> getRows() {
        return rows;
    }

    /**
     * Построить SQL-шаблон для batch insert.
     *
     * @return SQL-строка с именованными параметрами
     */
    public String buildSql() {
        if (table == null || table.isBlank()) {
            throw new DbException("Требуется имя таблицы для batch INSERT");
        }
        if (columns.isEmpty()) {
            throw new DbException("Требуется хотя бы одна колонка для batch INSERT");
        }
        if (rows.isEmpty()) {
            throw new DbException("Требуется хотя бы одна строка для batch INSERT");
        }

        // INSERT INTO table (col1, col2) VALUES (:col1, :col2)
        String columnList = String.join(", ", columns);
        String placeholders = columns.stream()
            .map(col -> ":" + col)
            .collect(Collectors.joining(", "));

        return "INSERT INTO " + table + " (" + columnList + ") VALUES (" + placeholders + ")";
    }

    /**
     * Построить список карт параметров для batch-выполнения.
     *
     * @return список карт параметров, по одной на строку
     */
    public List<Map<String, Object>> buildParamsList() {
        List<Map<String, Object>> paramsList = new ArrayList<>();
        
        for (Object[] rowValues : rows) {
            Map<String, Object> params = new LinkedHashMap<>();
            for (int i = 0; i < columns.size(); i++) {
                params.put(columns.get(i), rowValues[i]);
            }
            paramsList.add(params);
        }
        
        return paramsList;
    }

    /**
     * Выполнить batch insert.
     *
     * @param executor исполнитель запросов
     * @return массив с количеством затронутых строк для каждой вставки
     */
    public int[] execute(QueryExecutor executor) {
        return executor.executeBatch(buildSql(), buildParamsList());
    }
}
