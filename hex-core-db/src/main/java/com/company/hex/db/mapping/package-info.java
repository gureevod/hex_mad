/**
 * Пакет с компонентами маппинга результатов запросов.
 *
 * <p>Предоставляет автоматический и кастомный маппинг ResultSet на Java объекты:</p>
 *
 * <ul>
 *     <li>{@code ResultMapper} — основной маппер результатов</li>
 *     <li>{@code RowMapper<T>} — интерфейс для кастомного маппинга строк</li>
 *     <li>{@code DefaultRowMapper} — маппер по умолчанию с reflection</li>
 *     <li>Поддержка Java Records</li>
 *     <li>Конвертация snake_case → camelCase</li>
 * </ul>
 *
 * <h2>Поддерживаемые типы</h2>
 * <table>
 *     <tr><th>SQL Type</th><th>Java Type</th></tr>
 *     <tr><td>VARCHAR, CHAR</td><td>String</td></tr>
 *     <tr><td>INTEGER, INT</td><td>Integer, int</td></tr>
 *     <tr><td>BIGINT</td><td>Long, long</td></tr>
 *     <tr><td>DECIMAL, NUMERIC</td><td>BigDecimal</td></tr>
 *     <tr><td>TIMESTAMP</td><td>LocalDateTime</td></tr>
 *     <tr><td>DATE</td><td>LocalDate</td></tr>
 *     <tr><td>TIME</td><td>LocalTime</td></tr>
 *     <tr><td>BOOLEAN</td><td>Boolean, boolean</td></tr>
 *     <tr><td>BLOB</td><td>byte[]</td></tr>
 * </table>
 *
 * <h2>Пример кастомного RowMapper</h2>
 * <pre>{@code
 * public class UserRowMapper implements RowMapper<User> {
 *     @Override
 *     public User mapRow(ResultSet rs, int rowNum) throws SQLException {
 *         return new User(
 *             rs.getLong("id"),
 *             rs.getString("name"),
 *             rs.getString("email")
 *         );
 *     }
 * }
 * }</pre>
 *
 * @since 1.0.0
 */
package com.company.hex.db.mapping;
