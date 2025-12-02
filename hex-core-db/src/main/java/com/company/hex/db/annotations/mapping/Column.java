package com.company.hex.db.annotations.mapping;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Связывает поле Java класса с колонкой в таблице базы данных.
 *
 * <p>Аннотация указывает имя колонки, с которой будет маппиться
 * данное поле при чтении из ResultSet и записи в PreparedStatement.</p>
 *
 * <h2>Примеры использования</h2>
 * <pre>{@code
 * @Table("users")
 * public class User {
 *     @Id
 *     @Column("user_id")
 *     private Long id;
 *
 *     @Column("user_name")
 *     private String name;
 *
 *     @Column("email_address")
 *     private String email;
 *
 *     @Column("created_at")
 *     private LocalDateTime createdAt;
 *
 *     // Без аннотации: имя колонки = "active" (по имени поля)
 *     private boolean active;
 * }
 *
 * // Java Record с @Column
 * @Table("orders")
 * public record OrderRecord(
 *     @Id Long id,
 *     @Column("order_date") LocalDate orderDate,
 *     @Column("total_amount") BigDecimal totalAmount
 * ) {}
 * }</pre>
 *
 * <h2>Автоматическое определение имени</h2>
 * <p>Если {@code @Column} не указана, имя колонки определяется автоматически
 * из имени поля с преобразованием camelCase → snake_case:
 * {@code createdAt} → {@code created_at}.</p>
 *
 * <h2>Дополнительные параметры</h2>
 * <ul>
 *     <li>{@link #insertable()} — включать ли поле при INSERT</li>
 *     <li>{@link #updatable()} — включать ли поле при UPDATE</li>
 *     <li>{@link #nullable()} — может ли колонка содержать NULL</li>
 * </ul>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see Table
 * @see Id
 * @see Transient
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

    /**
     * Имя колонки в таблице базы данных.
     *
     * <p>Если не указано, имя определяется автоматически
     * из имени поля с преобразованием camelCase → snake_case.</p>
     *
     * @return имя колонки
     */
    String value() default "";

    /**
     * Имя колонки (альтернатива {@link #value()}).
     *
     * @return имя колонки
     */
    String name() default "";

    /**
     * Включать ли поле при INSERT операциях.
     *
     * <p>Если {@code false}, поле будет пропущено при генерации
     * INSERT запросов. Полезно для автогенерируемых полей.</p>
     *
     * @return {@code true} если поле включается в INSERT
     */
    boolean insertable() default true;

    /**
     * Включать ли поле при UPDATE операциях.
     *
     * <p>Если {@code false}, поле будет пропущено при генерации
     * UPDATE запросов. Полезно для неизменяемых полей.</p>
     *
     * @return {@code true} если поле включается в UPDATE
     */
    boolean updatable() default true;

    /**
     * Может ли колонка содержать NULL значение.
     *
     * <p>Используется для валидации при маппинге.
     * Если {@code false} и значение NULL, будет выброшено исключение.</p>
     *
     * @return {@code true} если колонка nullable
     */
    boolean nullable() default true;
}
