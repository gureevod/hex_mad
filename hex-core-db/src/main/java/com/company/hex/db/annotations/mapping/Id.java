package com.company.hex.db.annotations.mapping;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Указывает, что поле является первичным ключом таблицы.
 *
 * <p>Аннотация используется для идентификации поля, представляющего
 * первичный ключ записи. Это влияет на поведение операций вставки,
 * обновления и получения сгенерированных ключей.</p>
 *
 * <h2>Примеры использования</h2>
 * <pre>{@code
 * @Table("users")
 * public class User {
 *     @Id
 *     private Long id;
 *
 *     private String name;
 *     private String email;
 * }
 *
 * // С явным указанием имени колонки
 * @Table("users")
 * public class User {
 *     @Id
 *     @Column("user_id")
 *     private Long id;
 *     // ...
 * }
 *
 * // Java Record
 * @Table("users")
 * public record UserRecord(
 *     @Id Long id,
 *     String name,
 *     String email
 * ) {}
 *
 * // Составной первичный ключ
 * @Table("order_items")
 * public class OrderItem {
 *     @Id
 *     @Column("order_id")
 *     private Long orderId;
 *
 *     @Id
 *     @Column("product_id")
 *     private Long productId;
 *
 *     private int quantity;
 * }
 * }</pre>
 *
 * <h2>Поведение</h2>
 * <ul>
 *     <li>При {@code @ReturnGeneratedKeys} возвращается значение поля с {@code @Id}</li>
 *     <li>Поле не включается в UPDATE по умолчанию</li>
 *     <li>Для автогенерируемых ключей поле исключается из INSERT</li>
 * </ul>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see Table
 * @see Column
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Id {

    /**
     * Стратегия генерации значения первичного ключа.
     *
     * <p>По умолчанию используется {@link GenerationType#AUTO}, которая
     * определяет стратегию автоматически на основе СУБД.</p>
     *
     * @return стратегия генерации ключа
     */
    GenerationType strategy() default GenerationType.AUTO;

    /**
     * Стратегии генерации первичного ключа.
     */
    enum GenerationType {
        /**
         * Автоматический выбор стратегии на основе СУБД.
         */
        AUTO,

        /**
         * Ключ генерируется базой данных (IDENTITY, AUTO_INCREMENT).
         */
        IDENTITY,

        /**
         * Ключ генерируется через последовательность (SEQUENCE).
         */
        SEQUENCE,

        /**
         * Ключ задаётся вручную приложением.
         */
        ASSIGNED
    }
}
