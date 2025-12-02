package com.company.hex.db.annotations.mapping;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Указывает, что поле должно быть исключено из маппинга.
 *
 * <p>Поля, помеченные {@code @Transient}, не будут читаться из ResultSet
 * и не будут включаться в SQL запросы INSERT/UPDATE.</p>
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
 *
 *     // Вычисляемое поле, не хранится в БД
 *     @Transient
 *     private String displayName;
 *
 *     // Кэшированные данные
 *     @Transient
 *     private List<Order> cachedOrders;
 *
 *     public String getDisplayName() {
 *         return name + " <" + email + ">";
 *     }
 * }
 *
 * // Java Record — используйте обычные поля без @Transient
 * // для временных данных, так как record fields всегда маппятся
 * }</pre>
 *
 * <h2>Когда использовать</h2>
 * <ul>
 *     <li>Вычисляемые поля, производные от других данных</li>
 *     <li>Кэшированные связанные объекты</li>
 *     <li>Временные данные для бизнес-логики</li>
 *     <li>Поля, управляемые другими механизмами (не БД)</li>
 * </ul>
 *
 * <h2>Альтернатива</h2>
 * <p>Поля с модификатором {@code transient} также игнорируются
 * при маппинге, но {@code @Transient} предпочтительнее для
 * явного указания намерения.</p>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see Column
 * @see Table
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Transient {
}
