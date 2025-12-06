package com.company.hex.db.builders;

import com.company.hex.db.core.QueryExecutor;

import java.util.Map;

/**
 * InsertBuilder с методами выполнения.
 *
 * <p><b>НЕ потокобезопасен</b> — создавайте новый экземпляр в каждом потоке.
 *
 * <p>Пример использования:
 * <pre>{@code
 * Long id = db.insertInto("users")
 *     .value("name", "John")
 *     .value("email", "john@example.com")
 *     .executeAndGetKey(Long.class);
 * }</pre>
 *
 * @author hex-core-db
 * @since 1.0.0
 */
public class ExecutableInsertBuilder extends InsertBuilder {

    private final QueryExecutor executor;

    /**
     * Создать ExecutableInsertBuilder.
     *
     * @param executor исполнитель запросов
     * @param table    имя таблицы
     */
    public ExecutableInsertBuilder(QueryExecutor executor, String table) {
        super(table);
        this.executor = executor;
    }

    // === Переопределённые методы для возврата ExecutableInsertBuilder ===

    @Override
    public ExecutableInsertBuilder value(String column, Object value) {
        super.value(column, value);
        return this;
    }

    @Override
    public ExecutableInsertBuilder values(Map<String, Object> columnValues) {
        super.values(columnValues);
        return this;
    }

    @Override
    public ExecutableInsertBuilder returningKeys() {
        super.returningKeys();
        return this;
    }

    // === Методы выполнения ===

    /**
     * Выполнить INSERT и вернуть количество затронутых строк.
     *
     * @return количество вставленных строк (обычно 1)
     */
    public int execute() {
        return executor.execute(build()).affectedRows();
    }

    /**
     * Выполнить INSERT и вернуть сгенерированный ключ.
     *
     * @param keyType тип сгенерированного ключа
     * @param <T>     тип ключа
     * @return значение сгенерированного ключа
     */
    public <T> T executeAndGetKey(Class<T> keyType) {
        returningKeys();
        return executor.execute(build()).generatedKey(keyType);
    }
}
