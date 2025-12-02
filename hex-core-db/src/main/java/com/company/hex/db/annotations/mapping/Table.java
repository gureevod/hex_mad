package com.company.hex.db.annotations.mapping;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Связывает Java класс с таблицей в базе данных.
 *
 * <p>Аннотация указывает имя таблицы, из которой/в которую будут
 * маппиться объекты данного класса. Используется для автоматического
 * маппинга результатов SELECT запросов.</p>
 *
 * <h2>Примеры использования</h2>
 * <pre>{@code
 * // Простой entity
 * @Table("users")
 * public class User {
 *     @Id
 *     @Column("id")
 *     private Long id;
 *
 *     @Column("user_name")
 *     private String name;
 *
 *     @Column("email")
 *     private String email;
 * }
 *
 * // Entity со схемой
 * @Table(name = "orders", schema = "sales")
 * public class Order {
 *     @Id
 *     private Long id;
 *     // ...
 * }
 *
 * // Java Record
 * @Table("users")
 * public record UserRecord(
 *     @Id Long id,
 *     @Column("user_name") String name,
 *     String email
 * ) {}
 * }</pre>
 *
 * <h2>Автоматическое определение имени</h2>
 * <p>Если имя таблицы не указано, используется имя класса в snake_case:
 * {@code UserProfile} → {@code user_profile}.</p>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see Column
 * @see Id
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {

    /**
     * Имя таблицы в базе данных.
     *
     * <p>Если не указано, имя определяется автоматически
     * на основе имени класса (преобразование в snake_case).</p>
     *
     * @return имя таблицы
     */
    String value() default "";

    /**
     * Имя таблицы в базе данных (альтернатива {@link #value()}).
     *
     * <p>Используется для более явного указания:
     * {@code @Table(name = "users")}.</p>
     *
     * @return имя таблицы
     */
    String name() default "";

    /**
     * Схема базы данных, в которой находится таблица.
     *
     * <p>Если указана, полное имя таблицы будет {@code schema.table}.</p>
     *
     * @return имя схемы
     */
    String schema() default "";

    /**
     * Каталог базы данных, в котором находится таблица.
     *
     * <p>Используется для СУБД, поддерживающих каталоги
     * (например, MySQL с несколькими базами данных).</p>
     *
     * @return имя каталога
     */
    String catalog() default "";
}
