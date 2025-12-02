package com.company.hex.db.annotations.query;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для определения UPDATE запроса к базе данных.
 *
 * <p>Используется для декларативного определения SQL UPDATE запросов
 * в методах репозитория. Поддерживает именованные параметры в формате {@code :paramName}.</p>
 *
 * <h2>Примеры использования</h2>
 * <pre>{@code
 * // Простой UPDATE
 * @Update("UPDATE users SET name = :name WHERE id = :id")
 * int updateName(@Param("id") Long id, @Param("name") String name);
 *
 * // UPDATE нескольких полей
 * @Update("UPDATE users SET name = :name, email = :email, updated_at = :updatedAt WHERE id = :id")
 * int updateUser(@Param("id") Long id,
 *                @Param("name") String name,
 *                @Param("email") String email,
 *                @Param("updatedAt") LocalDateTime updatedAt);
 *
 * // UPDATE с условием
 * @Update("UPDATE users SET active = false WHERE last_login < :date")
 * int deactivateInactiveUsers(@Param("date") LocalDate date);
 *
 * // UPDATE без WHERE (требует @DangerousQuery)
 * @Update("UPDATE users SET active = false")
 * @DangerousQuery(reason = "Деактивация всех пользователей для очистки")
 * int deactivateAll();
 * }</pre>
 *
 * <h2>Возвращаемые типы</h2>
 * <ul>
 *     <li>{@code void} — результат не возвращается</li>
 *     <li>{@code int} — количество затронутых строк</li>
 *     <li>{@code boolean} — {@code true} если затронута хотя бы одна строка</li>
 * </ul>
 *
 * <h2>Безопасность</h2>
 * <p>UPDATE запросы без WHERE clause считаются потенциально опасными.
 * Валидатор выдаст предупреждение, если WHERE clause отсутствует.
 * Для явного подтверждения намерения используйте аннотацию {@code @DangerousQuery}.</p>
 *
 * @author Hex Framework
 * @version 1.0
 * @since 1.0.0
 * @see Param
 * @see DangerousQuery
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Update {

    /**
     * SQL UPDATE запрос для выполнения.
     *
     * <p>Поддерживает именованные параметры в формате {@code :paramName},
     * которые будут заменены на соответствующие значения из аргументов метода,
     * аннотированных {@link Param}.</p>
     *
     * @return SQL UPDATE запрос
     */
    String value();
}
