package com.company.hex.db.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Интерфейс для кастомного маппинга строки ResultSet на Java объект.
 *
 * <p>Реализации этого интерфейса используются для преобразования
 * результатов SQL запросов в объекты Java. Может использоваться
 * для сложных случаев маппинга, когда автоматический маппинг
 * недостаточен.</p>
 *
 * <h2>Примеры реализации</h2>
 * <pre>{@code
 * // Простой маппер
 * public class UserMapper implements RowMapper<User> {
 *     @Override
 *     public User mapRow(ResultSet rs, int rowNum) throws SQLException {
 *         return new User(
 *             rs.getLong("id"),
 *             rs.getString("name"),
 *             rs.getString("email")
 *         );
 *     }
 * }
 *
 * // Маппер для Record
 * public class UserRecordMapper implements RowMapper<UserRecord> {
 *     @Override
 *     public UserRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
 *         return new UserRecord(
 *             rs.getLong("id"),
 *             rs.getString("name"),
 *             rs.getString("email"),
 *             rs.getTimestamp("created_at").toLocalDateTime()
 *         );
 *     }
 * }
 *
 * // Маппер для сложного объекта с агрегацией
 * public class UserWithOrdersMapper implements RowMapper<UserWithOrders> {
 *     @Override
 *     public UserWithOrders mapRow(ResultSet rs, int rowNum) throws SQLException {
 *         User user = new User(
 *             rs.getLong("user_id"),
 *             rs.getString("user_name")
 *         );
 *
 *         String orderIds = rs.getString("order_ids");
 *         List<Long> ids = orderIds != null
 *             ? Arrays.stream(orderIds.split(","))
 *                 .map(Long::parseLong)
 *                 .toList()
 *             : List.of();
 *
 *         return new UserWithOrders(user, ids);
 *     }
 * }
 * }</pre>
 *
 * <h2>Использование с аннотацией @RowMapping</h2>
 * <pre>{@code
 * @Select("SELECT id, name, email FROM users WHERE id = :id")
 * @RowMapping(UserMapper.class)
 * Optional<User> findById(@Param("id") Long id);
 * }</pre>
 *
 * <h2>Лямбда-выражения</h2>
 * <p>Интерфейс является функциональным, поэтому может использоваться
 * как лямбда-выражение в программном API:</p>
 * <pre>{@code
 * RowMapper<User> mapper = (rs, rowNum) -> new User(
 *     rs.getLong("id"),
 *     rs.getString("name")
 * );
 * }</pre>
 *
 * @param <T> тип объекта, на который маппится строка
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see com.company.hex.db.annotations.mapping.RowMapping
 */
@FunctionalInterface
public interface RowMapper<T> {

    /**
     * Маппит текущую строку ResultSet на объект типа T.
     *
     * <p>Метод вызывается для каждой строки в ResultSet.
     * Не следует вызывать {@code rs.next()} внутри этого метода —
     * итерация по ResultSet управляется фреймворком.</p>
     *
     * @param rs     ResultSet, позиционированный на текущей строке
     * @param rowNum номер текущей строки (начиная с 1)
     * @return объект, смаппленный из текущей строки
     * @throws SQLException если происходит ошибка при чтении из ResultSet
     */
    T mapRow(ResultSet rs, int rowNum) throws SQLException;
}
