package com.company.hex.db.annotations.param;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Связывает параметр метода с именованным параметром в SQL запросе.
 *
 * <p>Используется для передачи значений аргументов метода в SQL запрос.
 * Имя параметра должно соответствовать плейсхолдеру {@code :paramName}
 * в SQL запросе.</p>
 *
 * <h2>Примеры использования</h2>
 * <pre>{@code
 * // Простые параметры
 * @Select("SELECT * FROM users WHERE id = :id")
 * Optional<User> findById(@Param("id") Long id);
 *
 * // Несколько параметров
 * @Select("SELECT * FROM users WHERE name = :name AND age >= :minAge")
 * List<User> findByNameAndMinAge(@Param("name") String name, @Param("minAge") int minAge);
 *
 * // Вложенные свойства объекта
 * @Insert("INSERT INTO users (name, email) VALUES (:user.name, :user.email)")
 * @ReturnGeneratedKeys
 * Long create(@Param("user") User user);
 *
 * // Параметры для IN clause используют @ParamList
 * @Select("SELECT * FROM users WHERE id IN (:ids)")
 * List<User> findByIds(@ParamList("ids") List<Long> ids);
 * }</pre>
 *
 * <h2>Поддерживаемые типы</h2>
 * <ul>
 *     <li>Примитивы и их обёртки: {@code int}, {@code Long}, {@code Double}, etc.</li>
 *     <li>{@code String}</li>
 *     <li>{@code java.time.*}: {@code LocalDate}, {@code LocalDateTime}, {@code Instant}</li>
 *     <li>{@code java.math.BigDecimal}, {@code java.math.BigInteger}</li>
 *     <li>{@code byte[]}</li>
 *     <li>Entity объекты (с доступом к вложенным свойствам через точку)</li>
 * </ul>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see ParamList
 * @see NullableParam
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {

    /**
     * Имя параметра в SQL запросе.
     *
     * <p>Должно соответствовать плейсхолдеру {@code :paramName} в SQL.
     * Для доступа к вложенным свойствам объекта используется точечная
     * нотация: {@code :user.address.city}.</p>
     *
     * @return имя параметра
     */
    String value();
}
