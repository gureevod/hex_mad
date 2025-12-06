package com.company.hex.db.builders;

import com.company.hex.db.DbException;
import com.company.hex.db.core.QueryExecutor;
import com.company.hex.db.core.RowMapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * SelectBuilder с методами выполнения.
 * Возвращается из Db.select() / Db.selectAll().
 *
 * <p><b>НЕ потокобезопасен</b> — создавайте новый экземпляр в каждом потоке.
 *
 * <p>Пример использования:
 * <pre>{@code
 * List<User> users = db.selectAll()
 *     .from("users")
 *     .where("active", true)
 *     .orderBy("name")
 *     .limit(10)
 *     .toList(userMapper);
 * }</pre>
 *
 * @author hex-core-db
 * @since 1.0.0
 */
public class ExecutableSelectBuilder extends SelectBuilder {

    private final QueryExecutor executor;

    /**
     * Создать ExecutableSelectBuilder с указанным executor и колонками.
     *
     * @param executor исполнитель запросов
     * @param columns  колонки для выборки
     */
    public ExecutableSelectBuilder(QueryExecutor executor, String... columns) {
        super(columns);
        this.executor = executor;
    }

    // === Переопределённые методы для возврата ExecutableSelectBuilder ===

    @Override
    public ExecutableSelectBuilder from(String table) {
        super.from(table);
        return this;
    }

    @Override
    public ExecutableSelectBuilder join(String joinTable, String on) {
        super.join(joinTable, on);
        return this;
    }

    @Override
    public ExecutableSelectBuilder leftJoin(String joinTable, String on) {
        super.leftJoin(joinTable, on);
        return this;
    }

    @Override
    public ExecutableSelectBuilder rightJoin(String joinTable, String on) {
        super.rightJoin(joinTable, on);
        return this;
    }

    @Override
    public ExecutableSelectBuilder where(String column, Object value) {
        super.where(column, value);
        return this;
    }

    @Override
    public ExecutableSelectBuilder where(String column, String operator, Object value) {
        super.where(column, operator, value);
        return this;
    }

    @Override
    public ExecutableSelectBuilder whereIn(String column, Collection<?> values) {
        super.whereIn(column, values);
        return this;
    }

    @Override
    public ExecutableSelectBuilder whereNull(String column) {
        super.whereNull(column);
        return this;
    }

    @Override
    public ExecutableSelectBuilder whereNotNull(String column) {
        super.whereNotNull(column);
        return this;
    }

    @Override
    public ExecutableSelectBuilder whereRaw(String condition) {
        super.whereRaw(condition);
        return this;
    }

    @Override
    public ExecutableSelectBuilder whereIf(boolean condition, String column, Object value) {
        super.whereIf(condition, column, value);
        return this;
    }

    @Override
    public ExecutableSelectBuilder whereIf(boolean condition, String column, String operator, Object value) {
        super.whereIf(condition, column, operator, value);
        return this;
    }

    @Override
    public ExecutableSelectBuilder param(String name, Object value) {
        super.param(name, value);
        return this;
    }

    @Override
    public ExecutableSelectBuilder groupBy(String... columns) {
        super.groupBy(columns);
        return this;
    }

    @Override
    public ExecutableSelectBuilder having(String havingCondition) {
        super.having(havingCondition);
        return this;
    }

    @Override
    public ExecutableSelectBuilder orderBy(String column) {
        super.orderBy(column);
        return this;
    }

    @Override
    public ExecutableSelectBuilder orderBy(String column, SortOrder order) {
        super.orderBy(column, order);
        return this;
    }

    @Override
    public ExecutableSelectBuilder orderByRaw(String orderByClause) {
        super.orderByRaw(orderByClause);
        return this;
    }

    @Override
    public ExecutableSelectBuilder limit(int limitValue) {
        super.limit(limitValue);
        return this;
    }

    @Override
    public ExecutableSelectBuilder offset(int offsetValue) {
        super.offset(offsetValue);
        return this;
    }

    // === Методы выполнения ===

    /**
     * Выполнить запрос и вернуть все строки как List из Map.
     *
     * @return список строк, каждая строка — Map с именами колонок как ключи
     */
    public List<Map<String, Object>> toList() {
        return executor.execute(build()).toList();
    }

    /**
     * Выполнить запрос и вернуть все строки, преобразованные в объекты.
     *
     * @param mapper функция преобразования строки
     * @param <T>    тип результата
     * @return список преобразованных объектов
     */
    public <T> List<T> toList(RowMapper<T> mapper) {
        return executor.execute(build()).toList(mapper);
    }

    /**
     * Выполнить запрос и вернуть первую строку (автоматически LIMIT 1).
     *
     * @return первая строка или пустой Optional
     */
    public Optional<Map<String, Object>> first() {
        limit(1);
        return executor.execute(build()).firstRow();
    }

    /**
     * Выполнить запрос и вернуть первую строку, преобразованную в объект.
     *
     * @param mapper функция преобразования строки
     * @param <T>    тип результата
     * @return преобразованный объект или пустой Optional
     */
    public <T> Optional<T> first(RowMapper<T> mapper) {
        limit(1);
        return executor.execute(build()).firstRow(mapper);
    }

    /**
     * Выполнить запрос и вернуть ровно одну строку или выбросить исключение.
     *
     * @return единственная строка как Map
     * @throws DbException если строки не найдены
     */
    public Map<String, Object> single() {
        return first().orElseThrow(() -> 
            new DbException("Ожидалась ровно одна строка, но ничего не найдено"));
    }

    /**
     * Выполнить запрос и вернуть ровно один преобразованный объект или выбросить исключение.
     *
     * @param mapper функция преобразования строки
     * @param <T>    тип результата
     * @return преобразованный объект
     * @throws DbException если строки не найдены
     */
    public <T> T single(RowMapper<T> mapper) {
        return first(mapper).orElseThrow(() -> 
            new DbException("Ожидалась ровно одна строка, но ничего не найдено"));
    }

    /**
     * Выполнить запрос и вернуть скалярное значение.
     * Используйте для запросов SELECT COUNT(*), SELECT MAX(id) и т.д.
     *
     * @param type ожидаемый тип значения
     * @param <T>  тип результата
     * @return скалярное значение
     */
    public <T> T scalar(Class<T> type) {
        return executor.execute(build()).scalar(type);
    }

    /**
     * Выполнить COUNT-запрос и вернуть количество.
     * Примечание: SELECT-колонки должны быть COUNT(*) или аналогичными.
     *
     * @return значение счётчика
     */
    public long count() {
        return scalar(Long.class);
    }

    /**
     * Проверить существуют ли строки, соответствующие критериям запроса.
     *
     * @return true если есть хотя бы одна строка
     */
    public boolean exists() {
        return first().isPresent();
    }
}
