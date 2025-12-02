package com.company.hex.db.annotations.mapping;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Указывает кастомный RowMapper для маппинга результатов запроса.
 *
 * <p>Позволяет использовать пользовательскую логику маппинга вместо
 * автоматического маппинга на основе аннотаций.</p>
 *
 * <h2>Примеры использования</h2>
 * <pre>{@code
 * // Кастомный маппер
 * public class UserWithOrdersMapper implements RowMapper<UserWithOrders> {
 *     @Override
 *     public UserWithOrders mapRow(ResultSet rs, int rowNum) throws SQLException {
 *         return new UserWithOrders(
 *             rs.getLong("user_id"),
 *             rs.getString("user_name"),
 *             rs.getString("order_ids")  // агрегированный список
 *         );
 *     }
 * }
 *
 * // Использование в репозитории
 * @Select("""
 *     SELECT u.id as user_id, u.name as user_name,
 *            string_agg(o.id::text, ',') as order_ids
 *     FROM users u
 *     LEFT JOIN orders o ON o.user_id = u.id
 *     GROUP BY u.id, u.name
 *     """)
 * @RowMapping(UserWithOrdersMapper.class)
 * List<UserWithOrders> findUsersWithOrders();
 *
 * // Маппер для сложных типов
 * @Select("SELECT * FROM audit_log WHERE entity_id = :id")
 * @RowMapping(AuditLogMapper.class)
 * List<AuditEntry> findAuditLog(@Param("id") Long id);
 * }</pre>
 *
 * <h2>Интерфейс RowMapper</h2>
 * <pre>{@code
 * public interface RowMapper<T> {
 *     T mapRow(ResultSet rs, int rowNum) throws SQLException;
 * }
 * }</pre>
 *
 * <h2>Когда использовать</h2>
 * <ul>
 *     <li>Сложные запросы с JOIN и агрегацией</li>
 *     <li>Нестандартные типы данных</li>
 *     <li>Оптимизация производительности маппинга</li>
 *     <li>Legacy схемы с нестандартными именами колонок</li>
 * </ul>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see com.company.hex.db.mapping.RowMapper
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RowMapping {

    /**
     * Класс RowMapper для маппинга результатов.
     *
     * <p>Класс должен реализовывать интерфейс
     * {@link com.company.hex.db.mapping.RowMapper} и иметь
     * конструктор без параметров.</p>
     *
     * @return класс RowMapper
     */
    Class<? extends com.company.hex.db.mapping.RowMapper<?>> value();
}
