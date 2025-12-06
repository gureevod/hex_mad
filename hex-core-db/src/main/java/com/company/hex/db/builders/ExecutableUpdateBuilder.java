package com.company.hex.db.builders;

import com.company.hex.db.core.QueryExecutor;

import java.util.Collection;
import java.util.Map;

/**
 * UpdateBuilder с методами выполнения.
 *
 * <p><b>НЕ потокобезопасен</b> — создавайте новый экземпляр в каждом потоке.
 *
 * <p>Пример использования:
 * <pre>{@code
 * int affected = db.update("users")
 *     .set("name", "New Name")
 *     .where("id", userId)
 *     .execute();
 * }</pre>
 *
 * @author hex-core-db
 * @since 1.0.0
 */
public class ExecutableUpdateBuilder extends UpdateBuilder {

    private final QueryExecutor executor;

    /**
     * Создать ExecutableUpdateBuilder.
     *
     * @param executor исполнитель запросов
     * @param table    имя таблицы
     */
    public ExecutableUpdateBuilder(QueryExecutor executor, String table) {
        super(table);
        this.executor = executor;
    }

    // === Переопределённые методы для возврата ExecutableUpdateBuilder ===

    @Override
    public ExecutableUpdateBuilder set(String column, Object value) {
        super.set(column, value);
        return this;
    }

    @Override
    public ExecutableUpdateBuilder setAll(Map<String, Object> values) {
        super.setAll(values);
        return this;
    }

    @Override
    public ExecutableUpdateBuilder where(String column, Object value) {
        super.where(column, value);
        return this;
    }

    @Override
    public ExecutableUpdateBuilder where(String column, String operator, Object value) {
        super.where(column, operator, value);
        return this;
    }

    @Override
    public ExecutableUpdateBuilder whereIn(String column, Collection<?> values) {
        super.whereIn(column, values);
        return this;
    }

    @Override
    public ExecutableUpdateBuilder whereNull(String column) {
        super.whereNull(column);
        return this;
    }

    @Override
    public ExecutableUpdateBuilder whereNotNull(String column) {
        super.whereNotNull(column);
        return this;
    }

    @Override
    public ExecutableUpdateBuilder all() {
        super.all();
        return this;
    }

    // === Методы выполнения ===

    /**
     * Выполнить UPDATE и вернуть количество затронутых строк.
     *
     * @return количество обновлённых строк
     */
    public int execute() {
        return executor.execute(build()).affectedRows();
    }
}
