package com.company.hex.db.builders;

import com.company.hex.db.core.QueryExecutor;

import java.util.Collection;

/**
 * DeleteBuilder с методами выполнения.
 *
 * <p><b>НЕ потокобезопасен</b> — создавайте новый экземпляр в каждом потоке.
 *
 * <p>Пример использования:
 * <pre>{@code
 * int deleted = db.deleteFrom("users")
 *     .where("id", userId)
 *     .execute();
 * }</pre>
 *
 * @author hex-core-db
 * @since 1.0.0
 */
public class ExecutableDeleteBuilder extends DeleteBuilder {

    private final QueryExecutor executor;

    /**
     * Создать ExecutableDeleteBuilder.
     *
     * @param executor исполнитель запросов
     * @param table    имя таблицы
     */
    public ExecutableDeleteBuilder(QueryExecutor executor, String table) {
        super(table);
        this.executor = executor;
    }

    // === Переопределённые методы для возврата ExecutableDeleteBuilder ===

    @Override
    public ExecutableDeleteBuilder where(String column, Object value) {
        super.where(column, value);
        return this;
    }

    @Override
    public ExecutableDeleteBuilder where(String column, String operator, Object value) {
        super.where(column, operator, value);
        return this;
    }

    @Override
    public ExecutableDeleteBuilder whereIn(String column, Collection<?> values) {
        super.whereIn(column, values);
        return this;
    }

    @Override
    public ExecutableDeleteBuilder whereNull(String column) {
        super.whereNull(column);
        return this;
    }

    @Override
    public ExecutableDeleteBuilder whereNotNull(String column) {
        super.whereNotNull(column);
        return this;
    }

    @Override
    public ExecutableDeleteBuilder all() {
        super.all();
        return this;
    }

    // === Методы выполнения ===

    /**
     * Выполнить DELETE и вернуть количество затронутых строк.
     *
     * @return количество удалённых строк
     */
    public int execute() {
        return executor.execute(build()).affectedRows();
    }
}
