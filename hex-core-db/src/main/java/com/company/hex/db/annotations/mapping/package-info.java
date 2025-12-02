/**
 * Пакет с аннотациями маппинга для entity классов.
 *
 * <p>Содержит аннотации для маппинга Java классов на таблицы БД:</p>
 *
 * <ul>
 *     <li>{@code @Table} — указывает имя таблицы</li>
 *     <li>{@code @Column} — указывает имя колонки</li>
 *     <li>{@code @Id} — маркирует первичный ключ</li>
 *     <li>{@code @Transient} — исключает поле из маппинга</li>
 *     <li>{@code @Enumerated} — настройка маппинга enum</li>
 * </ul>
 *
 * <h2>Пример использования</h2>
 * <pre>{@code
 * @Table("users")
 * public class UserEntity {
 *     @Id
 *     @Column("user_id")
 *     private Long id;
 *
 *     @Column("user_name")
 *     private String name;
 *
 *     @Transient
 *     private String cachedValue;
 * }
 * }</pre>
 *
 * @see com.company.hex.db.mapping.ResultMapper
 * @since 1.0.0
 */
package com.company.hex.db.annotations.mapping;
