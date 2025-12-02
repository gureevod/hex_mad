package com.company.hex.db.annotations.mapping;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Указывает, как должно быть сохранено и прочитано enum значение.
 *
 * <p>По умолчанию enum значения сохраняются как строки (имя константы).
 * Эта аннотация позволяет явно указать стратегию маппинга.</p>
 *
 * <h2>Примеры использования</h2>
 * <pre>{@code
 * public enum UserStatus {
 *     ACTIVE, INACTIVE, SUSPENDED
 * }
 *
 * public enum Priority {
 *     LOW(1), MEDIUM(2), HIGH(3);
 *
 *     private final int value;
 *     Priority(int value) { this.value = value; }
 *     public int getValue() { return value; }
 * }
 *
 * @Table("users")
 * public class User {
 *     @Id
 *     private Long id;
 *
 *     private String name;
 *
 *     // Сохраняется как строка: "ACTIVE", "INACTIVE", "SUSPENDED"
 *     @Enumerated(EnumType.STRING)
 *     private UserStatus status;
 *
 *     // Сохраняется как число: 0, 1, 2 (ordinal)
 *     @Enumerated(EnumType.ORDINAL)
 *     private Priority priority;
 * }
 * }</pre>
 *
 * <h2>Стратегии маппинга</h2>
 * <ul>
 *     <li>{@link EnumType#STRING} — сохраняет имя константы (рекомендуется)</li>
 *     <li>{@link EnumType#ORDINAL} — сохраняет порядковый номер (не рекомендуется)</li>
 * </ul>
 *
 * <h2>Рекомендации</h2>
 * <p>Используйте {@link EnumType#STRING} для production кода, так как
 * {@link EnumType#ORDINAL} ломается при изменении порядка констант в enum.</p>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see Column
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Enumerated {

    /**
     * Стратегия маппинга enum значения.
     *
     * <p>По умолчанию {@link EnumType#STRING}.</p>
     *
     * @return стратегия маппинга
     */
    EnumType value() default EnumType.STRING;

    /**
     * Стратегии маппинга enum значений.
     */
    enum EnumType {
        /**
         * Сохраняет порядковый номер константы (0, 1, 2, ...).
         *
         * <p>⚠️ Не рекомендуется для production, так как изменение
         * порядка констант в enum сломает существующие данные.</p>
         */
        ORDINAL,

        /**
         * Сохраняет имя константы как строку.
         *
         * <p>Рекомендуемый подход, устойчивый к рефакторингу.</p>
         */
        STRING
    }
}
