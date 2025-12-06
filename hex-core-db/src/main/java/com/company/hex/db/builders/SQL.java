package com.company.hex.db.builders;

/**
 * Фабрика SQL-билдеров.
 * 
 * <p>Главная точка входа для создания SQL-запросов через fluent API.
 * Все методы статические и возвращают объекты-билдеры для цепочки вызовов.
 *
 * <p>Примеры использования:
 * <pre>{@code
 * // SELECT запрос
 * Query select = SQL.select("id", "name")
 *     .from("users")
 *     .where("active", true)
 *     .build();
 *
 * // INSERT запрос
 * Query insert = SQL.insertInto("users")
 *     .value("name", "John")
 *     .value("email", "john@example.com")
 *     .build();
 *
 * // UPDATE запрос
 * Query update = SQL.update("users")
 *     .set("name", "Jane")
 *     .where("id", 1)
 *     .build();
 *
 * // DELETE запрос
 * Query delete = SQL.deleteFrom("users")
 *     .where("id", 1)
 *     .build();
 *
 * // Сырой SQL
 * Query raw = SQL.raw("SELECT * FROM users WHERE id = :id")
 *     .param("id", 1)
 *     .build();
 * }</pre>
 *
 * @author hex-core-db
 * @since 1.0.0
 */
public final class SQL {

    private SQL() {
        // Утилитный класс — нельзя создать экземпляр
    }

    /**
     * Создать билдер SELECT-запроса для указанных колонок.
     *
     * @param columns колонки для выборки
     * @return новый экземпляр SelectBuilder
     */
    public static SelectBuilder select(String... columns) {
        return new SelectBuilder(columns);
    }

    /**
     * Создать билдер SELECT * запроса.
     *
     * @return новый экземпляр SelectBuilder для выборки всех колонок
     */
    public static SelectBuilder selectAll() {
        return new SelectBuilder("*");
    }

    /**
     * Создать билдер INSERT-запроса для указанной таблицы.
     *
     * @param table имя таблицы для вставки
     * @return новый экземпляр InsertBuilder
     */
    public static InsertBuilder insertInto(String table) {
        return new InsertBuilder(table);
    }

    /**
     * Создать билдер UPDATE-запроса для указанной таблицы.
     *
     * @param table имя таблицы для обновления
     * @return новый экземпляр UpdateBuilder
     */
    public static UpdateBuilder update(String table) {
        return new UpdateBuilder(table);
    }

    /**
     * Создать билдер DELETE-запроса для указанной таблицы.
     *
     * @param table имя таблицы для удаления
     * @return новый экземпляр DeleteBuilder
     */
    public static DeleteBuilder deleteFrom(String table) {
        return new DeleteBuilder(table);
    }

    /**
     * Создать билдер сырого SQL-запроса для сложных случаев (CTE, подзапросы и т.д.).
     *
     * @param sql сырая SQL-строка с именованными параметрами в формате :name
     * @return новый экземпляр RawQueryBuilder
     */
    public static RawQueryBuilder raw(String sql) {
        return new RawQueryBuilder(sql);
    }

    /**
     * Создать билдер batch INSERT для эффективной вставки множества строк.
     *
     * @param table имя таблицы для вставки
     * @return новый экземпляр BatchInsertBuilder
     */
    public static BatchInsertBuilder batchInsertInto(String table) {
        return new BatchInsertBuilder(table);
    }
}
