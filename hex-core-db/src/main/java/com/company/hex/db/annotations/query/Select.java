package com.company.hex.db.annotations.query;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для определения SELECT запроса к базе данных.
 *
 * <p>Используется для декларативного определения SQL SELECT запросов
 * в методах репозитория. Поддерживает именованные параметры в формате {@code :paramName}.</p>
 *
 * <h2>Примеры использования</h2>
 * <pre>{@code
 * // Простой запрос с одним параметром
 * @Select("SELECT * FROM users WHERE id = :id")
 * Optional<User> findById(@Param("id") Long id);
 *
 * // Запрос с несколькими параметрами
 * @Select("SELECT * FROM users WHERE name = :name AND active = :active")
 * List<User> findByNameAndStatus(@Param("name") String name, @Param("active") boolean active);
 *
 * // Запрос с IN clause
 * @Select("SELECT * FROM users WHERE id IN (:ids)")
 * List<User> findByIds(@ParamList("ids") List<Long> ids);
 *
 * // Запрос с JOIN
 * @Select("""
 *     SELECT u.*, o.id as order_id
 *     FROM users u
 *     LEFT JOIN orders o ON o.user_id = u.id
 *     WHERE u.id = :userId
 *     """)
 * List<UserWithOrders> findUserWithOrders(@Param("userId") Long userId);
 * }</pre>
 *
 * <h2>Возвращаемые типы</h2>
 * <ul>
 *     <li>Single entity: {@code User}, {@code Optional<User>}</li>
 *     <li>Collection: {@code List<User>}, {@code Set<User>}</li>
 *     <li>Scalar: {@code Long}, {@code String}, {@code Integer}</li>
 *     <li>Record: Java Records поддерживаются автоматически</li>
 * </ul>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see Param
 * @see ParamList
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Select {

    /**
     * SQL SELECT запрос для выполнения.
     *
     * <p>Поддерживает именованные параметры в формате {@code :paramName},
     * которые будут заменены на соответствующие значения из аргументов метода,
     * аннотированных {@link Param}.</p>
     *
     * @return SQL SELECT запрос
     */
    String value();
}
