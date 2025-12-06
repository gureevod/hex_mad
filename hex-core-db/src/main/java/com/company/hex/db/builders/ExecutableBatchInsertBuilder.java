package com.company.hex.db.builders;

import com.company.hex.db.core.QueryExecutor;

import java.util.List;

/**
 * BatchInsertBuilder с методами выполнения.
 *
 * <p><b>НЕ потокобезопасен</b> — создавайте новый экземпляр в каждом потоке.
 *
 * <p>Пример использования:
 * <pre>{@code
 * int[] affected = db.batchInsertInto("users")
 *     .columns("name", "email", "active")
 *     .row("John", "john@example.com", true)
 *     .row("Jane", "jane@example.com", true)
 *     .execute();
 * }</pre>
 *
 * @author hex-core-db
 * @since 1.0.0
 */
public class ExecutableBatchInsertBuilder extends BatchInsertBuilder {

    private final QueryExecutor executor;

    /**
     * Создать ExecutableBatchInsertBuilder.
     *
     * @param executor исполнитель запросов
     * @param table    имя таблицы
     */
    public ExecutableBatchInsertBuilder(QueryExecutor executor, String table) {
        super(table);
        this.executor = executor;
    }

    // === Переопределённые методы для возврата ExecutableBatchInsertBuilder ===

    @Override
    public ExecutableBatchInsertBuilder columns(String... columnNames) {
        super.columns(columnNames);
        return this;
    }

    @Override
    public ExecutableBatchInsertBuilder row(Object... values) {
        super.row(values);
        return this;
    }

    @Override
    public ExecutableBatchInsertBuilder rows(List<Object[]> rowValues) {
        super.rows(rowValues);
        return this;
    }

    // === Методы выполнения ===

    /**
     * Выполнить batch INSERT.
     *
     * @return массив с количеством затронутых строк для каждой вставки
     */
    public int[] execute() {
        return executor.executeBatch(buildSql(), buildParamsList());
    }

    /**
     * Выполнить batch INSERT и вернуть общее количество затронутых строк.
     *
     * @return общее количество вставленных строк
     */
    public int executeAndGetTotalAffected() {
        int[] results = execute();
        int total = 0;
        for (int r : results) {
            if (r > 0) {
                total += r;
            }
        }
        return total;
    }
}
